package com.agaramtech.qualis.dynamicpreregdesign.service.registrationsubtypemaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.Period;
import com.agaramtech.qualis.configuration.model.ApprovalConfig;
import com.agaramtech.qualis.configuration.model.ApprovalStatusConfig;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.SeqNoConfigurationMaster;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegSubTypeConfigVersion;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegSubTypeConfigVersionRelease;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.dynamicpreregdesign.model.SeqNoRegSubTypeVersion;
import com.agaramtech.qualis.dynamicpreregdesign.service.registrationtypemaster.RegistrationTypeMasterDAO;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.DynamicStatusValidation;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.TransactionValidation;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;



@AllArgsConstructor
@Repository
public class RegistrationSubTypeDAOImpl implements RegistrationSubTypeDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationSubTypeDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	private RegistrationTypeMasterDAO regTypeDAO;

	@Override
	public ResponseEntity<Object> getRegistrationSubTypeInitial(UserInfo userInfo) throws Exception {
		Map<String, Object> returnObj = new HashMap<>();
		final String getSampleType = "select nsampletypecode,nsorter,coalesce(jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',jsondata->'sampletypename'->>'en-US') as ssampletypename "
				+ " from sampletype where napprovalconfigview = " + Enumeration.TransactionStatus.YES.gettransactionstatus() 
				+ " and nsitecode = " + userInfo.getNmastersitecode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter;";
		List<SampleType> sampleTypeList = jdbcTemplate.query(getSampleType, new SampleType());
		if (sampleTypeList.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {

			List<RegistrationType> regTypList = (List<RegistrationType>) regTypeDAO
					.getRegistrationTypeBySampleType(sampleTypeList.get(0).getNsampletypecode(), userInfo).getBody();
			if (regTypList != null && !regTypList.isEmpty()) {
				returnObj.putAll(
						(Map<String, Object>) getRegistrationSubType(regTypList.get(0).getNregtypecode(), userInfo, -1)
								.getBody());
			}
			returnObj.put("SampleTypes", sampleTypeList);
			returnObj.put("RegistrationTypes", regTypList);
			return new ResponseEntity<>(returnObj, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getRegistrationSubType(int regTypeCode, UserInfo userInfo, int regSubTypeCode) throws Exception {
		final String getRegistrationType = "select *,coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,"
				+  " coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename,"
				+  " coalesce(st.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',st.jsondata->'sampletypename'->>'en-US') as sampletypename,"
				+ " coalesce(rst.jsondata->>'sdescription','') as sdescription "
				+ " from registrationsubtype rst,approvalconfig ac,registrationtype rt,sampletype st where "
				+ " rst.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nregsubtypecode >0 "
				+ " and ac.nregtypecode = rst.nregtypecode and ac.nregsubtypecode = rst.nregsubtypecode"
				+ " and ac.nregtypecode = rt.nregtypecode and st.nsampletypecode=rt.nsampletypecode "
				+ " and st.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and st.nsitecode = " + userInfo.getNmastersitecode()
				+ " and rt.nsitecode = " + userInfo.getNmastersitecode()
				+ " and rst.nsitecode = " + userInfo.getNmastersitecode()
				+ " and ac.nsitecode = " + userInfo.getNmastersitecode()
				+ " and rst.nregtypecode = " + regTypeCode
				+ " order by rst.nregsubtypecode;";
		List<RegistrationSubType> registrationSubTypeList = jdbcTemplate.query(
				getRegistrationType, new RegistrationSubType());
		Map<String, Object> returnObj = new HashMap<>();
		returnObj.put("RegistrationSubType", registrationSubTypeList);
		if (!registrationSubTypeList.isEmpty()) {
			RegistrationSubType selectedRegistrationSubType ;
		
			if(regSubTypeCode>0) {
				final String strquery = "select nregsubtypecode from registrationsubtype where"
						+ " nsitecode = " + userInfo.getNmastersitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nregsubtypecode = "+ regSubTypeCode +";";
				RegistrationSubType deletedno = (RegistrationSubType) jdbcUtilityFunction.queryForObject(strquery,RegistrationSubType.class, jdbcTemplate);
				if(deletedno != null) {
					selectedRegistrationSubType = registrationSubTypeList.stream().filter(x->x.getNregsubtypecode()==regSubTypeCode).collect(Collectors.toList()).get(0);
					returnObj.put("selectedRegistrationSubType", selectedRegistrationSubType);
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYDELETED",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}else {
				selectedRegistrationSubType = registrationSubTypeList
						.get(registrationSubTypeList.size() - 1);
				returnObj.put("selectedRegistrationSubType", selectedRegistrationSubType);
			}
			final List<RegSubTypeConfigVersion> formatList = getSeqnoFormats(regTypeCode,
					selectedRegistrationSubType.getNregsubtypecode(), userInfo);
			returnObj.put("versions", formatList);
			if (!formatList.isEmpty())
				returnObj.put("selectedVersion", formatList.get(formatList.size() - 1));

		}else {
			returnObj.put("selectedRegistrationSubType", null);
			returnObj.put("versions", null);
		}
		return new ResponseEntity<>(returnObj, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegistrationSubTypeById(final short regSubTypeCode, final UserInfo userInfo)
			throws Exception {
		final String getRegistrationType = "select *,coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,"
				+  " coalesce(rt.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rt.jsondata->'sregtypename'->>'en-US') as sregtypename,"
				+  " coalesce(st.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',st.jsondata->'sampletypename'->>'en-US') as sampletypename,"
				+ " coalesce(rt.jsondata->>'description', '') as sdescription "
				+ " from registrationsubtype rst,approvalconfig ac,registrationtype rt,sampletype st where "
				+ " rst.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rst.nregsubtypecode >0 "			
				+ " and ac.nregtypecode = rst.nregtypecode and ac.nregtypecode = rt.nregtypecode "
			    + " and ac.nregtypecode = rt.nregtypecode and st.nsampletypecode = rt.nsampletypecode "
				+ " and ac.nregsubtypecode = rst.nregsubtypecode"
				+ " and st.nsitecode = " + userInfo.getNmastersitecode()
				+ " and rt.nsitecode = " + userInfo.getNmastersitecode()
				+ " and rst.nsitecode = " + userInfo.getNmastersitecode()
				+ " and ac.nsitecode = " + userInfo.getNmastersitecode()
				+ " and st.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rst.nregsubtypecode = " + regSubTypeCode +";";
		RegistrationSubType selectedRegistrationSubType = (RegistrationSubType) jdbcUtilityFunction.queryForObject(getRegistrationType, RegistrationSubType.class, jdbcTemplate);
		Map<String, Object> returnObj = new HashMap<>();
		if (selectedRegistrationSubType != null) {
			returnObj.put("selectedRegistrationSubType", selectedRegistrationSubType);
			final List<RegSubTypeConfigVersion> formatList = getSeqnoFormats(
					selectedRegistrationSubType.getNregtypecode(), selectedRegistrationSubType.getNregsubtypecode(),
					userInfo);
			returnObj.put("versions", formatList);
			if (!formatList.isEmpty())
				returnObj.put("selectedVersion", formatList.get(formatList.size() - 1));

		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
		return new ResponseEntity<>(returnObj, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createRegistrationSubType(RegistrationSubType regSubType, UserInfo userInfo)
			throws Exception {
		
		final ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> lstAudit = new ArrayList<>();
		JSONObject jsonAuditObject = new JSONObject();
		JSONObject actionType = new JSONObject();
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final Map<String,Object> sregsubtypename = (Map<String, Object>) regSubType.getJsondata().get("sregsubtypename");

		final List<Map<String,Object>> registrationSubType = getRegistrationSubTypeByName(sregsubtypename,regSubType.getNregtypecode(),regSubType.getNregsubtypecode(), userInfo);
		final String getRegistrationType = "select nregtypecode from registrationtype where"
				+ " nsitecode = " + userInfo.getNmastersitecode() 
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nregtypecode="+regSubType.getNregtypecode();
		List<RegistrationType> registrationTypeList = jdbcTemplate.query(getRegistrationType, new RegistrationType());		
		if (!registrationTypeList.isEmpty() ) {
		if (!(registrationSubType.size()>0)) {
			final String getSequenceNo = "select stablename,nsequenceno from seqnoregtemplateversion where stablename in ('registrationsubtype','approvalconfig')";
			final List<SeqNoConfigurationMaster> seqNumbers = jdbcTemplate.query(
					getSequenceNo, new SeqNoConfigurationMaster());
			Map<String, Integer> seqNoMap = seqNumbers.stream().collect(Collectors
					.toMap(SeqNoConfigurationMaster::getStablename, SeqNoConfigurationMaster::getNsequenceno));
			final int seqNo = seqNoMap.get("registrationsubtype") + 1;
			final int approvalConfigCode = seqNoMap.get("approvalconfig") + 1;
			
			final String insertQuery = "INSERT INTO registrationsubtype(nregsubtypecode,nregtypecode,jsondata,dmodifieddate,nsorter,nsitecode,nstatus) values "
					+ "(" + seqNo + "," + regSubType.getNregtypecode() + ","
					+ "'"+ stringUtilityFunction.replaceQuote(mapper.writeValueAsString(regSubType.getJsondata()).toString())  + "','" + dateUtilityFunction.getCurrentDateTime(userInfo)+"', 0,"
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");"
					+ " insert into approvalconfig (napprovalconfigcode,nregsubtypecode,nregtypecode,napprovalsubtypecode,nneedanalyst,sdescription,dmodifieddate,nstatus,nsitecode)"
					+ " values (" + approvalConfigCode + "," + seqNo + "," + regSubType.getNregtypecode() + "," + ""
					+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + "," + ""
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "," + "N'"
					+ stringUtilityFunction.replaceQuote(regSubType.getSregsubtypename()) + "','"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+", "+userInfo.getNmastersitecode() + ");";
			final String updateSeqNo = "update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename in ('registrationsubtype','approvalconfig');";
			jdbcTemplate.execute(insertQuery + updateSeqNo);
			objmap.put("nregtypecode", -1);
			objmap.put("nregsubtypecode", -1);
			objmap.put("ndesigntemplatemappingcode", -1);
			actionType.put("registrationsubtype", "IDS_ADDREGSUBTYPE");
		
			
			List languagelst =userInfo.getActivelanguagelist();
			List<String> regsubtypenamelst=(List<String>) languagelst.stream().map(x->"'sregsubtypename "+x+"',rs.jsondata->'sregsubtypename'->>'"+x+"'").collect(Collectors.toList());
			String regsubtypenameQry = String.join(",",regsubtypenamelst); //Joiner.on(",").join(regsubtypenamelst);
			
			final String strJsonArray4 = (String) jdbcUtilityFunction.queryForObject(
					"select json_agg(rs.jsondata||jsonb_build_object('nregsubtypecode',"+seqNo+","
					+ "'sampletypename', st.jsondata->'sampletypename'->>'"+userInfo.getSlanguagetypecode()
					+ "',"
					+ "'sregsubtypename',rs.jsondata->'sregsubtypename'->>'"+ userInfo.getSlanguagetypecode()
					+ "',"
					+ "'sregtypename',r.jsondata->'sregtypename'->>'"+ userInfo.getSlanguagetypecode()+"',"+regsubtypenameQry+ ")::jsonb) "
					+ " from registrationtype r,"
					+ " registrationsubtype rs,sampletype st"
					+ " where r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and st.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and r.nsitecode = " + userInfo.getNmastersitecode() 
					+ " and rs.nsitecode = " + userInfo.getNmastersitecode() 
					+ " and st.nsitecode = " + userInfo.getNmastersitecode() 
					+ " and rs.nregtypecode=r.nregtypecode "
					+ " and r.nsampletypecode=st.nsampletypecode and "
					+ " nregsubtypecode="+ seqNo, String.class, jdbcTemplate);
					
			JSONArray newJsonArray=new JSONArray(strJsonArray4);
			jsonAuditObject.put("registrationsubtype", newJsonArray);
			
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, objmap, false, userInfo);
			
			
			return getRegistrationSubType(regSubType.getNregtypecode(), userInfo, -1);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		}
		else {
			
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REGISTRATIONSUBTYPEALREADYDELETED",
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	private List<Map<String,Object>> getRegistrationSubTypeByName(Map<String,Object> regSubTypeName,final short regTypcode,final short regSubTypeCode,UserInfo userInfo) throws Exception {
		String lstKeyStr ="";
		if(regSubTypeCode ==0) {
			final String conditionStr = userInfo.getActivelanguagelist().stream().map(lang -> String.valueOf("lower(jsondata->'sregsubtypename'->>'"+lang+"')"))
	                .collect(Collectors.joining(","));
			lstKeyStr = userInfo.getActivelanguagelist().stream().map(lang -> String.valueOf("lower('"+stringUtilityFunction.replaceQuote(regSubTypeName.get(lang).toString())+"')"
					+" in ("+conditionStr+")")).collect(Collectors.joining(" or "));
		}else {
			final String conditionStr = regSubTypeName.keySet().stream().map(lang -> String.valueOf("lower(jsondata->'sregsubtypename'->>'"+lang+"')"))
	                .collect(Collectors.joining(","));
			lstKeyStr = regSubTypeName.keySet().stream().map(lang -> String.valueOf("lower('"+stringUtilityFunction.replaceQuote(regSubTypeName.get(lang).toString())+"')"
					+" in ("+conditionStr+")")).collect(Collectors.joining(" or "));
		}
		
	
		final String getRegistrationSubType = "select nregsubtypecode from registrationsubtype where ("+lstKeyStr+") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode();
		return (List<Map<String,Object>>) jdbcTemplate.queryForList(getRegistrationSubType);
		

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateRegistrationSubType(RegistrationSubType registrationSubType, UserInfo userInfo)
			throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();

		JSONObject actionType = new JSONObject();
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		

		List languagelst =userInfo.getActivelanguagelist();
		List<String> regsubtypenamelst=(List<String>) languagelst.stream().map(x->"'sregsubtypename "+x+"',rs.jsondata->'sregsubtypename'->>'"+x+"'").collect(Collectors.toList());
		String regsubtypenameQry = String.join(",",regsubtypenamelst); //Joiner.on(",").join(regsubtypenamelst);
		
		final String regSubtype = "select nregsubtypecode from registrationsubtype  where"
				+ " nsitecode = "+userInfo.getNmastersitecode()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nregsubtypecode="+registrationSubType.getNregsubtypecode();
		RegistrationSubType regsubtype = (RegistrationSubType) jdbcUtilityFunction.queryForObject(regSubtype, RegistrationSubType.class, jdbcTemplate);

		
		if(regsubtype == null ) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}else {
			
			final String strJsonArray1 = (String) jdbcUtilityFunction.queryForObject("select json_agg(rs.jsondata||jsonb_build_object('nregsubtypecode',"+ registrationSubType.getNregsubtypecode()+","
					+ "'sampletypename',st.jsondata->'sampletypename'->>'"+userInfo.getSlanguagetypecode()
					+ "',"
					+ "'sregsubtypename',rs.jsondata->'sregsubtypename'->>'"+ userInfo.getSlanguagetypecode()				
					+"',"
					+ "'sregtypename',r.jsondata->'sregtypename'->>'"+ userInfo.getSlanguagetypecode()+"', "
					+ regsubtypenameQry + ")::jsonb) from registrationtype r,"
					+ " registrationsubtype rs,sampletype st"
					+ " where r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and st.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and r.nsitecode = " + userInfo.getNmastersitecode() 
					+ " and rs.nsitecode = " + userInfo.getNmastersitecode() 
					+ " and st.nsitecode = " + userInfo.getNmastersitecode() 
					+ " and rs.nregtypecode=r.nregtypecode "
					+ " and r.nsampletypecode=st.nsampletypecode and "
					+ " nregsubtypecode="+ registrationSubType.getNregsubtypecode(), String.class, jdbcTemplate);
					
		final JSONArray oldJsonArray=new JSONArray(strJsonArray1);
		final RegistrationSubType validRegSubtype = getRegSubTypeById(registrationSubType.getNregsubtypecode(),
				userInfo);
		
		if (validRegSubtype != null) {
			final Map<String,Object> sregsubtypename = (Map<String, Object>) registrationSubType.getJsondata().get("sregsubtypename");

			final List<Map<String,Object>> regSubType = getRegistrationSubTypeByName(sregsubtypename,registrationSubType.getNregtypecode(),registrationSubType.getNregsubtypecode(), userInfo);


			if ((regSubType.size()<1) || (regSubType.size()==1 && regSubType.get(0).get("nregsubtypecode").equals((int) registrationSubType.getNregsubtypecode()))) {
	
				final String updateQuery = "update registrationsubtype set jsondata = jsondata || '"
						+ stringUtilityFunction.replaceQuote(mapper.writeValueAsString(registrationSubType.getJsondata()).toString())  + "'::jsonb , "
						+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' "
						+ " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode = " + userInfo.getNmastersitecode() 
						+ " and nregsubtypecode = " + registrationSubType.getNregsubtypecode() + ";";
				
				jdbcTemplate.execute(updateQuery);
				
				final String strJsonArray2 = (String) jdbcUtilityFunction.queryForObject("select json_agg(rs.jsondata||jsonb_build_object('nregsubtypecode',"+ registrationSubType.getNregsubtypecode()+","
						+ "'sampletypename',st.jsondata->'sampletypename'->>'"+userInfo.getSlanguagetypecode()
						+ "',"
						+ "'sregsubtypename',rs.jsondata->'sregsubtypename'->>'"+ userInfo.getSlanguagetypecode()				
						+"',"
						+ "'sregtypename',r.jsondata->'sregtypename'->>'"+
												userInfo.getSlanguagetypecode()+"', "+regsubtypenameQry+")::jsonb) from registrationtype r,"
						+ " registrationsubtype rs,sampletype st"
						+ " where r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and st.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and r.nsitecode = " + userInfo.getNmastersitecode() 
						+ " and rs.nsitecode = " + userInfo.getNmastersitecode() 
						+ " and st.nsitecode = " + userInfo.getNmastersitecode() 
						+ " and rs.nregtypecode=r.nregtypecode "
						+ " and r.nsampletypecode=st.nsampletypecode and "
						+ " nregsubtypecode="+ registrationSubType.getNregsubtypecode(), String.class, jdbcTemplate);
						
				JSONArray newJsonArray=new JSONArray(strJsonArray2);
				
//COMMENTED BY SYED 14-APR-2025			
//				JSONArray newJsonArray=new JSONArray(jdbcTemplate.queryForObject("select json_agg(jsondata) from registrationsubtype rstc where "
//						+ " nregsubtypecode="+ registrationSubType.getNregsubtypecode(), String.class));
				
				objmap.put("nregtypecode", -1);
				objmap.put("nregsubtypecode", -1);
				objmap.put("ndesigntemplatemappingcode", -1);
				actionType.put("registrationsubtype", "IDS_EDITREGSUBTYPE");
				jsonAuditNew.put("registrationsubtype", newJsonArray);
				jsonAuditOld.put("registrationsubtype", oldJsonArray);
				
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, objmap, false, userInfo);
				return getRegistrationSubType(registrationSubType.getNregtypecode(), userInfo, registrationSubType.getNregsubtypecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}
}

	@Override
	public ResponseEntity<Object> deleteRegistrationSubType(RegistrationSubType objRegistrationSubType,
			UserInfo userInfo) throws Exception {
		final RegistrationSubType regSubType = getRegSubTypeById(objRegistrationSubType.getNregsubtypecode(), userInfo);
		JSONObject jsonAuditObjectOld = new JSONObject();
		JSONObject actionType = new JSONObject(); 
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		if (regSubType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "select nregsubtypeversioncode from regsubtypeconfigversion sf,approvalconfig ac"
					+ " where ntransactionstatus =" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and sf.napprovalconfigcode = ac.napprovalconfigcode"  
					+ " and ac.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and sf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and sf.nsitecode = " + userInfo.getNmastersitecode() 
					+ " and ac.nsitecode = " + userInfo.getNmastersitecode()
					+ " and nregsubtypecode = " + objRegistrationSubType.getNregsubtypecode() 
					+ " and nregtypecode = " + objRegistrationSubType.getNregtypecode();
			RegSubTypeConfigVersion objDeleteValidation = (RegSubTypeConfigVersion) jdbcUtilityFunction.queryForObject(query,
					RegSubTypeConfigVersion.class, jdbcTemplate);

			if (objDeleteValidation == null) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedRegistrationSubTypeList = new ArrayList<>();
				final String updateQueryString = "update registrationsubtype set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
						+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' "
                        + " where nregsubtypecode="
						+ objRegistrationSubType.getNregsubtypecode();
				
				List languagelst =userInfo.getActivelanguagelist();
				List<String> regsubtypenamelst=(List<String>) languagelst.stream().map(x->"'sregsubtypename "+x+"',rs.jsondata->'sregsubtypename'->>'"+x+"'").collect(Collectors.toList());
				String regsubtypenameQry = String.join(",",regsubtypenamelst); 
				//COMMENTED BY SYED 14-APR-2025
				//Joiner.on(",").join(regsubtypenamelst);

				final String strJsonArray3 = (String) jdbcUtilityFunction.queryForObject("select json_agg(rs.jsondata||jsonb_build_object('nregsubtypecode',"+ objRegistrationSubType.getNregsubtypecode()+","
						+ "'sampletypename',st.jsondata->'sampletypename'->>'"+userInfo.getSlanguagetypecode()
						+ "',"
						+ "'sregsubtypename',rs.jsondata->'sregsubtypename'->>'"+ userInfo.getSlanguagetypecode()
						+"',"
						+ "'sregtypename',r.jsondata->'sregtypename'->>'"+ userInfo.getSlanguagetypecode()+"',"+ regsubtypenameQry +")::jsonb) from registrationtype r,"
						+ " registrationsubtype rs,sampletype st"
						+ " where r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rs.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and st.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and r.nsitecode = " + userInfo.getNmastersitecode() 
						+ " and rs.nsitecode = " + userInfo.getNmastersitecode()
						+ " and st.nsitecode = " + userInfo.getNmastersitecode()
						+ " and rs.nregtypecode=r.nregtypecode "
						+ " and r.nsampletypecode=st.nsampletypecode and "
						+ " nregsubtypecode="+ objRegistrationSubType.getNregsubtypecode(), String.class, jdbcTemplate);
				
				JSONArray jsonarray=new JSONArray(strJsonArray3);
				
				jdbcTemplate.execute(updateQueryString);
				objRegistrationSubType.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedRegistrationSubTypeList.add(objRegistrationSubType);
				multilingualIDList.add("IDS_DELETEREGISTRATIONSUBTYPE");
				objmap.put("nregtypecode", -1);
				objmap.put("nregsubtypecode", -1);
				objmap.put("ndesigntemplatemappingcode", -1);
				actionType.put("registrationsubtype", "IDS_DELETEREGSUBTYPE");

				
				jsonAuditObjectOld.put("registrationsubtype",jsonarray);
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, null, actionType, objmap, false, userInfo);


				return getRegistrationSubType(objRegistrationSubType.getNregtypecode(), userInfo, -1);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_APPROVEDVERSIONFOUND", userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public RegistrationSubType getRegSubTypeById(short regSubTypeCode, UserInfo userInfo) throws Exception {
		final String getRegistrationSubType = "select *,coalesce(rst.jsondata->'sregsubtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',rst.jsondata->'sregsubtypename'->>'en-US') as sregsubtypename,"
				+ " coalesce(rst.jsondata->>'sdescription','') as sdescription "
				+ " from registrationsubtype rst where rst.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and rst.nsitecode = " + userInfo.getNmastersitecode()
				+ " and rst.nregsubtypecode = "
				+ regSubTypeCode + ";";
		return (RegistrationSubType) jdbcUtilityFunction.queryForObject(getRegistrationSubType, RegistrationSubType.class, jdbcTemplate);
	}

	public ApprovalConfig getApprovalConfig(int regTypeCode, int regSubTypeCode, UserInfo userInfo) throws Exception {
		final String getApprovalConfig = "select ac.*,ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' as sneedsubsample ,ts2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' as sneedscheduler ,ts3.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' as sneedsampledby"
				+ " from approvalconfig ac,transactionstatus ts1,transactionstatus ts2,transactionstatus ts3"
				+ " where ts1.ntranscode = ac.nneedsubsample and ts2.ntranscode = ac.nneedscheduler and ts3.ntranscode = ac.nneedsampledby"
				+ " and ts1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts3.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ac.nsitecode = " + userInfo.getNmastersitecode()
				+ " and nregtypecode = " + regTypeCode + " and nregsubtypecode =" + regSubTypeCode;
		return (ApprovalConfig) jdbcUtilityFunction.queryForObject(getApprovalConfig, ApprovalConfig.class, jdbcTemplate);
	}

	private List<RegSubTypeConfigVersion> getSeqnoFormats(final int nregTypeCode, final int nregSubTypeCode,
			final UserInfo userInfo) throws Exception {
		final String getQuery = "select sf.nregsubtypeversioncode,sf.napprovalconfigcode,sf.nseqnoarnogencode,to_char(((sf.jsondata->>'dseqresetdate'):: timestamp ),'"+ userInfo.getSpgsitedatetime()+"') as sseqresetdate,"
				+ "to_char(((rcr.jsondata->>'dseqresetdate'):: timestamp ),'"+ userInfo.getSpgsitedatetime()+"') as sreleaseseqresetdate,"
				+ "	sf.nperiodcode,rcr.nperiodcode,sf.jsondata    || Jsonb_build_object('nregsubtypeversioncode', sf.nregsubtypeversioncode) ||"
				+ "		Jsonb_build_object('sreleaseformat',rcr.jsondata->'sreleaseformat')  ||"
				+ "		Jsonb_build_object('srelaeseexampleformat',rcr.jsondata->'srelaeseexampleformat') || "
				+ "		Jsonb_build_object('nneedsitewisearnorelease',rcr.jsondata->'nneedsitewisearnorelease') ||"
				+ "		Jsonb_build_object('nregsubtypeversionreleasecode', rcr.nregsubtypeversionreleasecode) ||"
				+ "		Jsonb_build_object('seqnolengthrelease',rcr.jsondata->'seqnolength')"

				+ "	as	jsondata ,sf.jsonuidata||jsonb_build_object('nregsubtypeversioncode',sf.nregsubtypeversioncode) ||	Jsonb_build_object('sreleaseformat',rcr.jsonuidata->'sreleaseformat')  || "
				+ "		Jsonb_build_object('srelaeseexampleformat',rcr.jsonuidata->'srelaeseexampleformat') || "
				+ "		Jsonb_build_object('nneedsitewisearnorelease',rcr.jsonuidata->'nneedsitewisearnorelease') ||"
				+ "	Jsonb_build_object('nregsubtypeversionreleasecode', rcr.nregsubtypeversionreleasecode) ||"
				+ "	Jsonb_build_object('seqnolengthrelease',rcr.jsonuidata->'seqnolength') as"
				+ "	 jsonuidata "
				+ ", sf.ntransactionstatus,rcr.ntransactionstatus, ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()+""
				+ "' as stransdisplaystatus," + " pr.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()+""
				+ "' as speriodname, pr1.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()+""
				+ "' as sreleaseperiodname" + "  "
				+ " from regsubtypeconfigversion sf "
				+" left join regsubtypeconfigversionrelease rcr on   sf.nregsubtypeversioncode = rcr.nregsubtypeversionreleasecode and rcr.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and rcr.nsitecode = " + userInfo.getNmastersitecode()+""
				+" left join approvalconfig ac1 on ac1.napprovalconfigcode = rcr.napprovalconfigcode AND  ac1.nregtypecode = " + nregTypeCode+" AND ac1.nregsubtypecode = " + nregSubTypeCode +" AND ac1.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and ac1.nsitecode = " + userInfo.getNmastersitecode()+""
				+" left join  period pr1 on rcr.nperiodcode=pr1.nperiodcode and pr1.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and pr1.nsitecode = " + userInfo.getNmastersitecode() +", "
				+ " approvalconfig ac,period pr, transactionstatus ts "
				+ " where ac.napprovalconfigcode = sf.napprovalconfigcode and pr.nperiodcode = sf.nperiodcode and ts.ntranscode = sf.ntransactionstatus" + " "
				+ " and sf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sf.nsitecode = " + userInfo.getNmastersitecode()
				+ " and pr.nsitecode = " + userInfo.getNmastersitecode()
				+ " and ac.nsitecode = " + userInfo.getNmastersitecode()
				+ " and ac.nregsubtypecode = " + nregSubTypeCode
				+ " and ac.nregtypecode = " + nregTypeCode
				+ " order by nregsubtypeversioncode;";
		return jdbcTemplate.query(getQuery, new RegSubTypeConfigVersion());
	}

	@Override
	public RegSubTypeConfigVersion getVersionDetails(final int nregSubTypeConfigCode, final UserInfo userInfo)
			throws Exception {
		final String getQuery = "select sf.*, ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus," + " pr.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "' as speriodname" + " from regsubtypeconfigversion sf,period pr, transactionstatus ts"
				+ " where pr.nperiodcode = sf.nperiodcode"
				+ " and ts.ntranscode = sf.ntransactionstatus" + " and sf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sf.nsitecode = " + userInfo.getNmastersitecode()
				+ " and pr.nsitecode = " + userInfo.getNmastersitecode()
				+ " and nregsubtypeversioncode = " + nregSubTypeConfigCode;
		return (RegSubTypeConfigVersion) jdbcUtilityFunction.queryForObject(getQuery, RegSubTypeConfigVersion.class, jdbcTemplate);
	}
	@Override
	public RegSubTypeConfigVersionRelease getVersionDetailsByRelease(final int nregSubTypeConfigCode, final UserInfo userInfo)
			throws Exception {
		final String getQuery = "select rcr.*, ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus," + " pr.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "' as speriodname" + " from regsubtypeconfigversionrelease rcr,period pr, transactionstatus ts"
				+ " where pr.nperiodcode = rcr.nperiodcode"
				+ " and ts.ntranscode = rcr.ntransactionstatus" + " and rcr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rcr.nsitecode = " + userInfo.getNmastersitecode()
				+ " and pr.nsitecode = " + userInfo.getNmastersitecode()
				+ " and nregsubtypeversionreleasecode =" + nregSubTypeConfigCode;
		return (RegSubTypeConfigVersionRelease) jdbcUtilityFunction.queryForObject(getQuery, RegSubTypeConfigVersionRelease.class, jdbcTemplate);
	}
	private List<RegSubTypeConfigVersion> getSeqnoFormatsByApprovalConfig(final int approvalConfigCode,
			final UserInfo userInfo) throws Exception {
		final String getQuery = "select sf.nregsubtypeversioncode,sf.napprovalconfigcode,sf.nseqnoarnogencode,"
				+ "	sf.nperiodcode,rcr.nperiodcode,sf.jsondata    || Jsonb_build_object('nregsubtypeversioncode', sf.nregsubtypeversioncode) ||"
				+ "		Jsonb_build_object('sreleaseformat',rcr.jsondata->'sreleaseformat')  ||"
				+ "		Jsonb_build_object('srelaeseexampleformat',rcr.jsondata->'srelaeseexampleformat') || "
				+ "		Jsonb_build_object('nneedsitewisearnorelease',rcr.jsondata->'nneedsitewisearnorelease') ||"
				+ "		Jsonb_build_object('nregsubtypeversionreleasecode', rcr.nregsubtypeversionreleasecode) "
				+ "	as	jsondata ,sf.jsonuidata||jsonb_build_object('nregsubtypeversioncode',sf.nregsubtypeversioncode) ||	Jsonb_build_object('sreleaseformat',rcr.jsonuidata->'sreleaseformat')  || "
				+ "		Jsonb_build_object('srelaeseexampleformat',rcr.jsonuidata->'srelaeseexampleformat') || "
				+ "		Jsonb_build_object('nneedsitewisearnorelease',rcr.jsonuidata->'nneedsitewisearnorelease') ||"
				+ "		Jsonb_build_object('nregsubtypeversionreleasecode', rcr.nregsubtypeversionreleasecode) "
				+ "    as  jsonuidata "
				+ ", sf.ntransactionstatus,rcr.ntransactionstatus, ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus," + " pr.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "' as speriodname, pr1.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()+""
				+ "' as sreleaseperiodname" + " from regsubtypeconfigversion sf "
				+" left join regsubtypeconfigversionrelease rcr on   sf.nregsubtypeversioncode = rcr.nregsubtypeversionreleasecode and rcr.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rcr.nsitecode = " + userInfo.getNmastersitecode()+""
				+" left join approvalconfig ac1 on ac1.napprovalconfigcode = rcr.napprovalconfigcode   AND ac1.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac1.nsitecode = " + userInfo.getNmastersitecode() +""
				+" left join  period pr1 on rcr.nperiodcode=pr1.nperiodcode and pr1.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pr1.nsitecode = " + userInfo.getNmastersitecode() +", "				
				+ " period pr, transactionstatus ts"
				+ " where pr.nperiodcode = sf.nperiodcode and ts.ntranscode = sf.ntransactionstatus" 
				+ " and sf.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and pr.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sf.nsitecode = " + userInfo.getNmastersitecode()
				+ " and pr.nsitecode = " + userInfo.getNmastersitecode()
				+ " and sf.napprovalconfigcode = " + approvalConfigCode + " "
				+" order by sf.nregsubtypeversioncode";
		return jdbcTemplate.query(getQuery, new RegSubTypeConfigVersion());
	}

	@Override
	public ResponseEntity<Object> getAllSeqNoFormats(UserInfo userInfo) throws Exception {

		final String getQuery = "select jsondata->>'ssampleformat' as ssampleformat from regsubtypeconfigversion " + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode()
				+ " group by jsondata->>'ssampleformat' ;";
		return new ResponseEntity<>(jdbcTemplate.query(getQuery, new RegSubTypeConfigVersion()), HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getAllSeqNoFormatsByRelease(UserInfo userInfo) throws Exception {

		final String getQuery = "select jsondata->>'sreleaseformat' as sreleaseformat from regsubtypeconfigversionrelease " + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nregsubtypeversionreleasecode>0  and ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode()
				+ " group by jsondata->>'sreleaseformat' ;";
		return new ResponseEntity<>(jdbcTemplate.query(getQuery, new RegSubTypeConfigVersionRelease()), HttpStatus.OK);
	}


	@Override
	public ResponseEntity<Object> getPeriods(UserInfo userInfo) throws Exception {

		final String getQuery = "select pc.ndefaultstatus,p.nperiodcode,p.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "' as speriodname,(p.jsondata->'ndata')::int ndata from period p,periodconfig pc  where"
				+ " p.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and pc.nstatus =  "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nsitecode = " + userInfo.getNmastersitecode()
				+ " and p.nperiodcode = pc.nperiodcode and pc.nformcode = "+userInfo.getNformcode();
		return new ResponseEntity<>(jdbcTemplate.query(getQuery, new Period()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createVersion(final RegSubTypeConfigVersion version, final UserInfo userInfo)
			throws Exception {
		
		//ALPD-897
		final RegistrationSubType validRegSubtype = getRegSubTypeById(version.getNregsubtypecode(),	userInfo);
		
		if (validRegSubtype == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REGSUBTYPEDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		else {
			List<Map<String, Object>> lstAudit = new ArrayList<>();
			JSONObject jsonAuditObject = new JSONObject();
			JSONObject actionType = new JSONObject();
			Map<String, Object> objmap = new LinkedHashMap<String, Object>();
			JSONObject jsonuidata = new JSONObject(version.getJsonuidata());
	
			final String getSequenceNo = "select nsequenceno+1 from seqnoregtemplateversion where stablename ='regsubtypeconfigversion'";
			final int seqNo = (int) jdbcUtilityFunction.queryForObject(getSequenceNo, Integer.class, jdbcTemplate);
			List<Map<String, Object>> seqNoArnoGenCodeList = validateFormat(
					(String) version.getJsondata().get("ssampleformat"),userInfo);
			String updateSeqNo = "update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename = 'regsubtypeconfigversion';";
			
			String insertNewSeqNo = "";
			int seqNoArnoGenCode;
			int periodCode = version.getNperiodcode();
			int resetDuartion;
			if (seqNoArnoGenCodeList.isEmpty()) {
				final String getArnoSequenceNo = "select nsequenceno+1 from seqnoregtemplateversion where stablename ='seqnoarnogenerator'";
				seqNoArnoGenCode = (int) jdbcUtilityFunction.queryForObject(getArnoSequenceNo, Integer.class, jdbcTemplate);
				//ALPD-2550 - added dmodifieddate field
				insertNewSeqNo = "insert into seqnoarnogenerator(nseqnoarnogencode,nsequenceno,nstatus, dmodifieddate) " + " values ("
						+ seqNoArnoGenCode + ",0," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ ",'" +dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ ");";
	
				updateSeqNo += " update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename = 'seqnoarnogenerator';";
				version.setNseqnoarnogencode(seqNoArnoGenCode);
			} else {
				seqNoArnoGenCode = (int) seqNoArnoGenCodeList.get(0).get("nseqnoarnogencode");
				resetDuartion = (int) seqNoArnoGenCodeList.get(0).get("nresetduration");
				periodCode = Short.valueOf(seqNoArnoGenCodeList.get(0).get("nperiodcode").toString());
				Map<String, Object> jsonData = version.getJsondata();
				jsonData.put("nresetduration", resetDuartion);
				version.setJsondata(jsonData);
			}
			final JSONObject jsonData = new JSONObject(version.getJsondata());
			
			
			//ALPD-3835
			final String insertQuery = "insert into regsubtypeconfigversion(nregsubtypeversioncode,napprovalconfigcode,nseqnoarnogencode,"
					+ "nperiodcode,jsondata,jsonuidata,dmodifieddate,ntransactionstatus,nsitecode,nstatus) " + "values (" + seqNo + ","
					+ version.getNapprovalconfigcode() + "," + seqNoArnoGenCode + "," + periodCode + "," + "'"
					+    stringUtilityFunction.replaceQuote(jsonData.toString())  + "','"+stringUtilityFunction.replaceQuote(jsonuidata.toString()) + "'::jsonb,"
					+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' ,"
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");";
	
			jdbcTemplate.execute(insertQuery + updateSeqNo + insertNewSeqNo);
			String lengthssampleformat=(String) version.getJsondata().get("ssampleformat");
			//ALPD-5254--added by  neeraj
			if(lengthssampleformat.replaceAll("[{}]", "").length()<40) {
			
		        ArrayList<String> arrayList = new ArrayList<>();
		      //ALPD-3835
				arrayList=(ArrayList<String>) version.getJsondata().get("formatArray");
				 String formatArray=commonFunction.getMultilingualMessage("IDS_REF", userInfo.getSlanguagefilename())+"-"+arrayList.get(0).toString();
				 arrayList.set(0, formatArray);
				 
				jsonData.put("formatArray",arrayList);

				jsonData.put("sreleaseformat", commonFunction.getMultilingualMessage("IDS_REF", userInfo.getSlanguagefilename())+"-"+version.getJsondata().get("ssampleformat"));
				jsonData.put("sreleaseexampleformat", commonFunction.getMultilingualMessage("IDS_REF", userInfo.getSlanguagefilename())+"-"+version.getJsondata().get("exampleformat"));
				jsonData.put("splaintext", commonFunction.getMultilingualMessage("IDS_REF", userInfo.getSlanguagefilename())+"-");
				 //ALPD-3835
				if(version.getJsonuidata().containsKey("formatArray")) {
					jsonuidata.put("formatArray", arrayList);
				}
				jsonuidata.put("sreleaseformat",commonFunction.getMultilingualMessage("IDS_REF", userInfo.getSlanguagefilename())+"-"+version.getJsonuidata().get("ssampleformat"));
				jsonuidata.put("sreleaseexampleformat",commonFunction.getMultilingualMessage("IDS_REF", userInfo.getSlanguagefilename())+"-"+version.getJsonuidata().get("exampleformat"));
				jsonuidata.put("splaintext", commonFunction.getMultilingualMessage("IDS_REF", userInfo.getSlanguagefilename())+"-");

			}
			
			else {
				
				jsonData.put("sreleaseformat",version.getJsondata().get("ssampleformat"));
				jsonData.put("sreleaseexampleformat", version.getJsondata().get("exampleformat"));
				jsonuidata.put("sreleaseformat", version.getJsonuidata().get("ssampleformat"));
				jsonuidata.put("sreleaseexampleformat", version.getJsonuidata().get("exampleformat"));
			}
			if(version.getJsondata().containsKey("nneedsitewisearno")) {
				jsonData.put("nneedsitewisearnorelease", version.getJsondata().get("nneedsitewisearno"));

			}
			


			List<Map<String, Object>> seqNoReleaseGenCodeList = validateReleaseFormat(
					jsonData.get("sreleaseformat").toString(),userInfo);
			int seqNoReleseGenCode;
			String insertNewSeqNoRelease="";
			String updateReleseSeqNo="";
			
			if (seqNoReleaseGenCodeList.isEmpty()) {
				final String getRelesenoSequenceNo = "select nsequenceno+1 from seqnoregtemplateversion where stablename ='seqnoreleasenogenerator'";
				seqNoReleseGenCode = (int) jdbcUtilityFunction.queryForObject(getRelesenoSequenceNo, Integer.class, jdbcTemplate);
				insertNewSeqNoRelease = "insert into seqnoreleasenogenerator(nseqnoreleasenogencode,nsequenceno,nstatus, dmodifieddate) " + " values ("
						+ seqNoReleseGenCode + ",0," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ ",'" +dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ ");";
	
				updateReleseSeqNo += " update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename = 'seqnoreleasenogenerator';";
			} else {
				seqNoReleseGenCode = (int) seqNoReleaseGenCodeList.get(0).get("nseqnoreleasenogencode");
				resetDuartion = (int) seqNoReleaseGenCodeList.get(0).get("nresetduration");
				periodCode = Short.valueOf(seqNoArnoGenCodeList.get(0).get("nperiodcode").toString());
				Map<String, Object> relesejsonData = version.getJsondata();
				relesejsonData.put("nresetduration", resetDuartion);
				version.setJsondata(relesejsonData);
			}
			
			jsonData.put("nresetduration", version.getJsondata().get("nresetduration"));

			
			jsonuidata.put("nresetduration", version.getJsonuidata().get("nresetduration"));
			jsonuidata.put("nneedsitewisearnorelease", version.getJsonuidata().get("nneedsitewisearno"));

			jsonData.remove("ssampleformat");
			jsonData.remove("exampleformat");
			jsonuidata.remove("ssampleformat");
			jsonuidata.remove("exampleformat");
			final String insertreleaseArNo = "insert into regsubtypeconfigversionrelease(nregsubtypeversionreleasecode,napprovalconfigcode,nseqnoreleasenogencode,"
					+ "nperiodcode,jsondata,jsonuidata,dmodifieddate,ntransactionstatus,nsitecode,nstatus) " + "values (" + seqNo + ","
					+ version.getNapprovalconfigcode() + "," + seqNoReleseGenCode + "," + periodCode + "," + "'"
					+    stringUtilityFunction.replaceQuote(jsonData.toString())  + "','"+stringUtilityFunction.replaceQuote(jsonuidata.toString()) + "'::jsonb,"
					+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' ,"
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");";
			jdbcTemplate.execute(insertreleaseArNo +updateReleseSeqNo + insertNewSeqNoRelease);

			Map<String, Object> returnObj = new HashMap<>();
			List<RegSubTypeConfigVersion> formatList = getSeqnoFormatsByApprovalConfig(version.getNapprovalconfigcode(),
					userInfo);
			returnObj.put("versions", formatList);
			if (!formatList.isEmpty())
				returnObj.put("selectedVersion", formatList.get(formatList.size() - 1));
			objmap.put("nregtypecode", -1);
			objmap.put("nregsubtypecode", -1);
			objmap.put("ndesigntemplatemappingcode", -1);
			actionType.put("regsubtypeconfigversion", "IDS_ADDREGSUBTYPECONFIGVERSION");
			//returnmap.putAll(getMaterialFile((int) jsonObj.get("nmaterialcode"), objUserInfo));
			lstAudit.add(((RegSubTypeConfigVersion)((List<Map<String, Object>>) returnObj.get("versions"))
					.get(((List<Map<String, Object>>) returnObj.get("versions")).size() - 1)).getJsonuidata());
			jsonAuditObject.put("regsubtypeconfigversion", lstAudit);
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, objmap, false, userInfo);
	
			return new ResponseEntity<>(returnObj, HttpStatus.OK);
		}
	}

	private List<Map<String, Object>> validateFormat(String format, final UserInfo userInfo) {
		final String getSeqNo = "select nregsubtypeversioncode,nseqnoarnogencode,(jsondata->>'nresetduration')::INTEGER nresetduration ,nperiodcode from regsubtypeconfigversion"
				+ " where jsondata->>'ssampleformat' = '" + format + "' " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode();
		return jdbcTemplate.queryForList(getSeqNo);
	}
	private List<Map<String, Object>> validateReleaseFormat(String format, final UserInfo userInfo) {
		final String getSeqNo = "select nregsubtypeversionreleasecode,nseqnoreleasenogencode,(jsondata->>'nresetduration')::INTEGER nresetduration ,nperiodcode from regsubtypeconfigversionrelease"
				+ " where jsondata->>'sreleaseformat' = '" + format + "' " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForList(getSeqNo);
	}
	@Override
	public ResponseEntity<Object> updateVersion(final RegSubTypeConfigVersion version, final UserInfo userInfo)
			throws Exception {
		
		//ALPD-897
		final RegistrationSubType validRegSubtype = getRegSubTypeById(version.getNregsubtypecode(),	userInfo);
		
		if (validRegSubtype == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REGSUBTYPEDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		else {
			//ObjectMapper objmapper = new ObjectMapper();
			JSONObject jsonAuditOld = new JSONObject();
			JSONObject jsonAuditNew = new JSONObject();
			//JSONArray newJsonUiDataArray = new JSONArray();
			JSONObject actionType = new JSONObject();
			Map<String, Object> objmap = new LinkedHashMap<String, Object>();
			/*
			 * JSONArray oldJsonArray=new JSONArray(jdbcTemplate.
			 * queryForObject("select json_agg(jsondata||json_build_object('ntransactionstatus',ntransactionstatus)::jsonb) from regsubtypeconfigversion rstc where "
			 * + " nregsubtypeversioncode="+ version.getNregsubtypeversioncode(),
			 * String.class));
			 */
			//	ALPD-6025	to avoid null error by Mullai Balaji(16-06-2025)
			final String strJsonArray5 = (String) jdbcUtilityFunction.queryForObject(
				    "SELECT COALESCE(json_agg(jsonuidata || jsonb_build_object('nregsubtypeversioncode'," 
				    + version.getNregsubtypeversioncode() + ")::jsonb), '[]') FROM regsubtypeconfigversion rstc WHERE"
				    + " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				    + " AND nsitecode = " + userInfo.getNmastersitecode()
				    + " AND nregsubtypeversioncode = " + version.getNregsubtypeversioncode(),
				    String.class,
				    jdbcTemplate
				);

				JSONArray oldJsonArray = new JSONArray(strJsonArray5); 

			RegSubTypeConfigVersion oldVersion = getVersionById(version.getNregsubtypeversioncode(), userInfo);
			if (oldVersion != null) {
				List<Map<String, Object>> seqNoArnoGenCodeList = validateFormat(
						(String) version.getJsondata().get("ssampleformat"),userInfo);
				String insertNewSeqNo = "";
				int seqNoArnoGenCode;
				short periodCode = version.getNperiodcode();
				int resetDuartion;
				String updateSeqNo = "";
				if (seqNoArnoGenCodeList.isEmpty()) {
					final String getArnoSequenceNo = "select nsequenceno+1 from seqnoregtemplateversion where stablename ='seqnoarnogenerator'";
					seqNoArnoGenCode = (int) jdbcUtilityFunction.queryForObject(getArnoSequenceNo, Integer.class, jdbcTemplate);
					//ALPD-2550 - added dmodifieddate field
					insertNewSeqNo = "insert into seqnoarnogenerator(nseqnoarnogencode,nsequenceno,nstatus, dmodifieddate) " + " values ("
							+ seqNoArnoGenCode + ",0," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ ",'" +dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
							+ ");";
	
					updateSeqNo += " update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename = 'seqnoarnogenerator';";
					version.setNseqnoarnogencode(seqNoArnoGenCode);
				} else {
	
					Map<String, Object> jsonData = version.getJsondata();
					if((int)seqNoArnoGenCodeList.get(0).get("nregsubtypeversioncode")!= version.getNregsubtypeversioncode()) {
						seqNoArnoGenCode = (int) seqNoArnoGenCodeList.get(0).get("nseqnoarnogencode");
						resetDuartion = (int) seqNoArnoGenCodeList.get(0).get("nresetduration");
//						periodCode = Short.valueOf(seqNoArnoGenCodeList.get(0).get("nperiodcode").toString());
						jsonData.put("nresetduration", resetDuartion);
					}else {
						seqNoArnoGenCode = (int) seqNoArnoGenCodeList.get(0).get("nseqnoarnogencode");
						resetDuartion = 1;
//						periodCode = version.getNperiodcode();
						jsonData.put("nresetduration", resetDuartion);
					}
					version.setJsondata(jsonData);
					version.setNseqnoarnogencode(seqNoArnoGenCode);
					version.setNperiodcode(periodCode);
				}
				final JSONObject jsonData = new JSONObject(version.getJsondata());
				final JSONObject jsonuiData = new JSONObject(version.getJsonuidata());
	
				 String updateQuery = "update regsubtypeconfigversion set jsondata = '" + stringUtilityFunction.replaceQuote(jsonData.toString())   + "'"
						+ ",jsonuidata='"+ stringUtilityFunction.replaceQuote(jsonuiData.toString())+"' ,"
					    + " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' ,"
						+ " nperiodcode=" + periodCode + "," + " nseqnoarnogencode = " + seqNoArnoGenCode
						+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nregsubtypeversioncode = " + version.getNregsubtypeversioncode() + ";";

				 jdbcTemplate.execute(updateQuery + updateSeqNo + insertNewSeqNo);
//	
				Map<String, Object> returnObj = new HashMap<>();
				List<RegSubTypeConfigVersion> formatList = getSeqnoFormatsByApprovalConfig(version.getNapprovalconfigcode(),
						userInfo);
				returnObj.put("versions", formatList);
				if (!formatList.isEmpty())
					returnObj.put("selectedVersion",
							formatList.stream()
									.filter(x -> x.getNregsubtypeversioncode() == version.getNregsubtypeversioncode())
									.collect(Collectors.toList()).get(0));
				
				//SYED commented on 10-APR-2025
				//JSONArray newJsonArray=new JSONArray(jdbcUtilityFunction.queryForObject("select json_agg(jsondata||json_build_object('ntransactionstatus',ntransactionstatus)::jsonb) from regsubtypeconfigversion rstc where "
				//		+ " nregsubtypeversioncode="+ version.getNregsubtypeversioncode(), String.class, jdbcTemplate));
				
				objmap.put("nregtypecode", -1);
				objmap.put("nregsubtypecode", -1);
				objmap.put("ndesigntemplatemappingcode", -1);
				actionType.put("regsubtypeconfigversion", "IDS_EDITREGSUBTYPEVERSION");
				jsonAuditNew.put("regsubtypeconfigversion", new JSONArray().put((jsonuiData)));
				jsonAuditOld.put("regsubtypeconfigversion", oldJsonArray);
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, objmap, false, userInfo);
				return new ResponseEntity<>(returnObj, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}

	}

	public RegSubTypeConfigVersion getVersionById(final int nregSubTypeVersionCode, final UserInfo userInfo)
			throws Exception {
		final String getQuery = "select sf.*, ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as stransdisplaystatus," + " pr.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "' as speriodname" + " from regsubtypeconfigversion sf, period pr, transactionstatus ts"
				+ " where pr.nperiodcode = sf.nperiodcode" + " and ts.ntranscode = sf.ntransactionstatus"
				+ " and sf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sf.nsitecode = " + userInfo.getNmastersitecode()
				+ " and pr.nsitecode = " + userInfo.getNmastersitecode()
				+ " and sf.nregsubtypeversioncode = " + nregSubTypeVersionCode;
		return (RegSubTypeConfigVersion) jdbcUtilityFunction.queryForObject(getQuery, RegSubTypeConfigVersion.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> deleteVersion(RegSubTypeConfigVersion version, UserInfo userInfo) throws Exception {
		
		//ALPD-897
		final RegistrationSubType validRegSubtype = getRegSubTypeById(version.getNregsubtypecode(),	userInfo);
		
		if (validRegSubtype == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTODELETE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		else {
			final RegSubTypeConfigVersion status = getVersionStatus(version.getNregsubtypeversioncode());
			JSONObject jsonAuditObjectOld = new JSONObject();

			JSONObject actionType = new JSONObject(); 
			
			//SYED commented on 10-APR-2025
			//List<Map<String, Object>> lstAuditOld = new ArrayList<>();
			
	
			Map<String, Object> objmap = new LinkedHashMap<String, Object>();
			if (status == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if (status.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
					 String deleteQuery = "update regsubtypeconfigversion set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() +" ,"
						    + " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' "
							+ " where nregsubtypeversioncode = " + version.getNregsubtypeversioncode()+";";
				deleteQuery = deleteQuery+"update regsubtypeconfigversionrelease set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() +" ,"
						    + " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' "
							+ " where nregsubtypeversionreleasecode = " + version.getNregsubtypeversioncode();
				jdbcTemplate.execute(deleteQuery);
				

					Map<String, Object> returnObj = new HashMap<>();
					List<RegSubTypeConfigVersion> formatList = getSeqnoFormatsByApprovalConfig(version.getNapprovalconfigcode(),
							userInfo);
					returnObj.put("versions", formatList);
					if (!formatList.isEmpty())
						returnObj.put("selectedVersion", formatList.get(formatList.size() - 1));
					objmap.put("nregtypecode", -1);
					objmap.put("nregsubtypecode", -1);
					objmap.put("ndesigntemplatemappingcode", -1);
					actionType.put("regsubtypeconfigversion", "IDS_DELETEREGSUBTYPEVERSION");
					
					JSONArray jsonarray=new JSONArray(jdbcTemplate.queryForObject("select json_agg(jsonuidata||jsonb_build_object('nregsubtypeversioncode' ," + version.getNregsubtypeversioncode()+")) from regsubtypeconfigversion rstc where "
							+ " nregsubtypeversioncode="+ version.getNregsubtypeversioncode(), String.class));
					
					//ADDED BY SYED 14-APR-2025
					/*
					final String strJsonArray6 = (String) jdbcUtilityFunction.queryForObject("select json_agg(jsonuidata||jsonb_build_object('nregsubtypeversioncode' ," + version.getNregsubtypeversioncode()+")) from regsubtypeconfigversion rstc where"
							+ " nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode = " + userInfo.getNmastersitecode()
							+ " and nregsubtypeversioncode="+ version.getNregsubtypeversioncode(), String.class, jdbcTemplate);
					
					JSONArray jsonarray=new JSONArray(strJsonArray6); 
					*/
					jsonAuditObjectOld.put("regsubtypeconfigversion",jsonarray);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, null, actionType, objmap, false, userInfo);
					return new ResponseEntity<>(returnObj, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTODELETE",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
	}

	@Override
	public ResponseEntity<Object> approveVersion(RegSubTypeConfigVersion version, UserInfo userInfo) throws Exception {
		
		//ALPD-897
		final RegistrationSubType validRegSubtype = getRegSubTypeById(version.getNregsubtypecode(),	userInfo);
		
		if (validRegSubtype == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REGSUBTYPEDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		else {
			
			JSONObject jsonAuditOld = new JSONObject();
			JSONObject jsonAuditNew = new JSONObject();
		
			JSONObject actionType = new JSONObject();
			Map<String, Object> objmap = new LinkedHashMap<String, Object>();
			final RegSubTypeConfigVersion status = getVersionStatus(version.getNregsubtypeversioncode());
			final JSONObject jsonuiData = new JSONObject(version.getJsonuidata());
	
			if (status == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if (status.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
					final RegSubTypeConfigVersion approvedVersion = getApprovedVersion(version.getNapprovalconfigcode(), userInfo);
				
					if (approvedVersion != null) {					
						
						
						//ALPD-1913 
						/** added option to approve the new draft version for an existing Sub Type
						 *  when the samples are rejected/cancelled/reached the final level of approval.
						 */
						final String approvedSampleQuery = "select r.* from  approvalconfigrole acr, approvalconfig ac, "
															+ " regsubtypeconfigversion rstcv,  registration r, registrationhistory rh "
															+ " where acr.napprovalconfigcode = ac.napprovalconfigcode "
															+ " and acr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ " and ac.nstatus = " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ " and ac.napprovalconfigcode=rstcv.napprovalconfigcode "
															+ " and rstcv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ "	and rstcv.nregsubtypeversioncode = r.nregsubtypeversioncode "
															+ " and r.nstatus=" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ " and acr.nlevelno=1 and acr.ntransactionstatus= " +Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
															+ " and r.npreregno = rh.npreregno and rh.nstatus= " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ " and acr.nsitecode = " + userInfo.getNmastersitecode()
															+ " and ac.nsitecode = " + userInfo.getNmastersitecode()
															+ " and rstcv.nsitecode = " + userInfo.getNmastersitecode()
															+ " and rh.ntransactionstatus not in ("
															+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
															+ ","
															+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() +","
															+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+""
															+ ") "
															+ " and nreghistorycode = any( "
															+ " 	select max(nreghistorycode) from registrationhistory where "
															+ " 	r.npreregno = npreregno and nstatus= " +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
															+ " 	group by npreregno order by npreregno "
															+ " ) and r.nregsubtypeversioncode =  " + approvedVersion.getNregsubtypeversioncode();
						
						final List<Registration> unapprovedSampleList = (List<Registration>) jdbcTemplate.query(approvedSampleQuery, new Registration());
						if (unapprovedSampleList.isEmpty()) {	
							
							final String strJsonArray7 = (String) jdbcUtilityFunction.queryForObject("select json_agg(jsonuidata || json_build_object('nregsubtypeversioncode',nregsubtypeversioncode)::jsonb) from regsubtypeconfigversion rstc where"
									+ " nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and nsitecode = " + userInfo.getNmastersitecode()
									+ " and nregsubtypeversioncode="+ approvedVersion.getNregsubtypeversioncode(), String.class, jdbcTemplate);
							
							JSONArray jsonarrayold=new JSONArray(strJsonArray7); 
							
							 String retireQuery = "update regsubtypeconfigversion set jsonuidata='"+stringUtilityFunction.replaceQuote(jsonuiData.toString())
							+"'||jsonb_build_object('stransdisplaystatus','"+commonFunction.getMultilingualMessage("IDS_RETIRED",
	 								userInfo.getSlanguagefilename())+"')::jsonb, ntransactionstatus = "
														+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()+" ,"
														+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' "
														+ " where nregsubtypeversioncode = " + approvedVersion.getNregsubtypeversioncode()+";";
							retireQuery = retireQuery+"update regsubtypeconfigversionrelease set jsonuidata='"+stringUtilityFunction.replaceQuote(jsonuiData.toString())
							+"'||jsonb_build_object('stransdisplaystatus','"+commonFunction.getMultilingualMessage("IDS_RETIRED",
	 								userInfo.getSlanguagefilename())+"')::jsonb, ntransactionstatus = "
														+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()+" ,"
														+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' "
														+ " where nregsubtypeversionreleasecode = " + approvedVersion.getNregsubtypeversioncode()+ ";";
//							ALPD-6044	Added update script for filterdetail and filtername tables by Vishakh (03-07-2025)
							String updateFilterDetail = " update filterdetail set nstatus="+ Enumeration.TransactionStatus.NA.gettransactionstatus()
														+ ", dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nregsubtypecode="
														+ version.getNregsubtypecode()+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
							updateFilterDetail += " update filtername set nstatus="+ Enumeration.TransactionStatus.NA.gettransactionstatus()
														+ ", dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nregsubtypecode="
														+ version.getNregsubtypecode()+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
							
							jdbcTemplate.execute(retireQuery+ updateFilterDetail);
							
							actionType.put("regsubtypeconfigversion", "IDS_RETIREREGSUBTYPEVERSION");
							
							final String strJsonArray8 = (String) jdbcUtilityFunction.queryForObject("select json_agg(jsonuidata|| json_build_object('nregsubtypeversioncode',nregsubtypeversioncode)::jsonb) from regsubtypeconfigversion rstc where"
									+ " nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and nsitecode = " + userInfo.getNmastersitecode()
									+ " and nregsubtypeversioncode="+ approvedVersion.getNregsubtypeversioncode(), String.class, jdbcTemplate);
							JSONArray jsonarraynew=new JSONArray(strJsonArray8); 
							
							objmap.put("nregtypecode", -1);
							objmap.put("nregsubtypecode", -1);
							objmap.put("ndesigntemplatemappingcode", -1);
							jsonAuditOld.put("regsubtypeconfigversion", jsonarrayold);
							jsonAuditNew.put("regsubtypeconfigversion", jsonarraynew);
							auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, objmap, false, userInfo);
							
	
						}
						else {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLEFLOWINCOMPLETE",
									userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					}
					jsonAuditOld.clear();
					jsonAuditNew.clear();
					actionType.clear();
					final int versionNo = getVersionNo(version.getNapprovalconfigcode());
					
					final String strJsonArray9 = (String) jdbcUtilityFunction.queryForObject("select json_agg(jsonuidata||jsonb_build_object('nregsubtypeversioncode' ," + version.getNregsubtypeversioncode()+")::jsonb) from regsubtypeconfigversion rstc where"
							+ " nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode = " + userInfo.getNmastersitecode()
							+ " and nregsubtypeversioncode="+ version.getNregsubtypeversioncode(), String.class, jdbcTemplate);
							
					JSONArray jsonarrayapproveold=new JSONArray(strJsonArray9);
					
					 String updateQuery = "update regsubtypeconfigversion set jsonuidata='"+stringUtilityFunction.replaceQuote(jsonuiData.toString())
							+ "'||jsonb_build_object('stransdisplaystatus','"+commonFunction.getMultilingualMessage("IDS_APPROVED", 
							 userInfo.getSlanguagefilename())+"')::jsonb, ntransactionstatus = "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+","
						    + " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' ,"
							+ " jsondata = jsondata || '{\"nversionno\" : "+versionNo+",\"sversiondesc\" : \""+versionNo+"\"}'" 
							+ " where nregsubtypeversioncode = " + version.getNregsubtypeversioncode()+";";
					
					  updateQuery = updateQuery+"update regsubtypeconfigversionrelease set jsonuidata='"+stringUtilityFunction.replaceQuote(jsonuiData.toString())
					+ "'||jsonb_build_object('stransdisplaystatus','"+commonFunction.getMultilingualMessage("IDS_APPROVED", 
					 userInfo.getSlanguagefilename())+"')::jsonb, ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+","
				    + " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' ,"
					+ " jsondata = jsondata || '{\"nversionno\" : "+versionNo+",\"sversiondesc\" : \""+versionNo+"\"}'" 
					+ " where nregsubtypeversionreleasecode = " + version.getNregsubtypeversioncode();
					
					jdbcTemplate.execute(updateQuery);
					
					final String strJsonArray10 = (String) jdbcUtilityFunction.queryForObject("select json_agg(jsonuidata || json_build_object('nregsubtypeversioncode',nregsubtypeversioncode)::jsonb) from regsubtypeconfigversion rstc where"
							+ " nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode = " + userInfo.getNmastersitecode()
							+ " and nregsubtypeversioncode="+ version.getNregsubtypeversioncode(), String.class, jdbcTemplate);
							
					JSONArray jsonarrayapprovenew=new JSONArray(strJsonArray10);
					
					manageScreenStatus(new JSONObject(status.getJsondata()), version.getNregtypecode(), version.getNregsubtypecode(), userInfo);
					
					manageValidationStatus(new JSONObject(status.getJsondata()), version.getNregtypecode(), version.getNregsubtypecode(), userInfo);
					
					final List<RegSubTypeConfigVersion> formatList = getSeqnoFormatsByApprovalConfig(version.getNapprovalconfigcode(),userInfo);
					Map<String, Object> returnObj = new HashMap<>();
					returnObj.put("versions", formatList);
					
					if (!formatList.isEmpty())
						returnObj.put("selectedVersion", formatList.stream().filter(x->x.getNregsubtypeversioncode()==version.getNregsubtypeversioncode())
													.collect(Collectors.toList()).get(0));
					actionType.put("regsubtypeconfigversion", "IDS_APPPROVEREGSUBTYPEVERSION");
					
					//SYED commented on 10-APR-2025
					//JSONArray jsonuidata=new JSONArray(jdbcUtilityFunction.queryForObject("select json_agg(jsondata||json_build_object('ntransactionstatus',ntransactionstatus)::jsonb) from regsubtypeconfigversion rstc where "
					//		+ " nregsubtypeversioncode="+ version.getNregsubtypeversioncode(), String.class, jdbcTemplate)); 
					

					objmap.put("nregtypecode", -1);
					objmap.put("nregsubtypecode", -1);
					objmap.put("ndesigntemplatemappingcode", -1);
			
					jsonAuditOld.put("regsubtypeconfigversion", jsonarrayapproveold);
					jsonAuditNew.put("regsubtypeconfigversion", jsonarrayapprovenew);
	
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, objmap, false, userInfo);
					return new ResponseEntity<>(returnObj,HttpStatus.OK);
				} 
				else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTOAPPROVE",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
	}

	private int getVersionNo(final short napprovalConfigCode) throws Exception {
		final String getVersionNo = "select nsequenceno from seqnoregsubtypeversion"
				+ " where napprovalconfigcode = "+napprovalConfigCode
				+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final SeqNoRegSubTypeVersion version = (SeqNoRegSubTypeVersion) jdbcUtilityFunction.queryForObject(getVersionNo, SeqNoRegSubTypeVersion.class, jdbcTemplate);
		if(version == null ) {
			final String insertQuery = "insert into seqnoregsubtypeversion (napprovalconfigcode,nsequenceno,nstatus) values("+napprovalConfigCode+",1,1)";
			jdbcTemplate.execute(insertQuery);
			return 1;
		}else {
			final String updateQuery = "update seqnoregsubtypeversion set nsequenceno = nsequenceno+1 where napprovalconfigcode = "+napprovalConfigCode;
			jdbcTemplate.execute(updateQuery);
			return version.getNsequenceno()+1;
		}
		
	}

	private RegSubTypeConfigVersion getVersionStatus(final int nregSubTypeVersionCode) throws Exception {
		final String validateVersion = "select * from regsubtypeconfigversion" + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nregsubtypeversioncode ="
				+ nregSubTypeVersionCode;
		return (RegSubTypeConfigVersion) jdbcUtilityFunction.queryForObject(validateVersion, RegSubTypeConfigVersion.class, jdbcTemplate);

	}

	public RegSubTypeConfigVersion getApprovedVersion(final int napprovalConfigCode, final UserInfo userInfo) throws Exception {
		final String validateVersion = "select *"				
				+ " from regsubtypeconfigversion"
				+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode()
				+ " and ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and napprovalconfigcode = " + napprovalConfigCode;
		return (RegSubTypeConfigVersion) jdbcUtilityFunction.queryForObject(validateVersion, RegSubTypeConfigVersion.class, jdbcTemplate);
	}
	
	private void manageScreenStatus(final JSONObject jsonData, final int nregTypeCode, 
				final int nregSubTypeCode, final UserInfo userInfo) throws Exception{
		
		final Boolean needJobAllocation = jsonData.has("nneedjoballocation") ? (Boolean)jsonData.get("nneedjoballocation") :false;
		final Boolean needMyJob = jsonData.has("nneedmyjob") ?  (Boolean)jsonData.get("nneedmyjob") :false;
		final Boolean needTestInitiate = jsonData.has("nneedtestinitiate") ?  (Boolean)jsonData.get("nneedtestinitiate") :false;
		
		final String getSequenceNo = "select nsequenceno+1 from seqnoconfigurationmaster where stablename ='approvalstatusconfig'";
		int seqNo = (int) jdbcUtilityFunction.queryForObject(getSequenceNo, Integer.class, jdbcTemplate);
	
		
		Map<String, Object> queryMap = new HashMap<String, Object>() {{
			put("InsertQuery", new ArrayList<>());
			put("UpdateQuery", "");
			put("SeqNo", seqNo);
			
		}};		
				
		queryMap = manageRegistrationStatus(nregTypeCode, nregSubTypeCode, userInfo, needJobAllocation, needMyJob, needTestInitiate, queryMap);
			
		queryMap = manageJobAllocationStatus(nregTypeCode, nregSubTypeCode, userInfo, needJobAllocation,needMyJob, needTestInitiate, queryMap);
		
		//Start of My Jobs
		queryMap = manageMyJobStatus(nregTypeCode, nregSubTypeCode, userInfo, needJobAllocation, needMyJob, queryMap, Enumeration.QualisForms.MYJOB.getqualisforms());
		
		queryMap = manageMyJobStatus(nregTypeCode, nregSubTypeCode, userInfo, needJobAllocation, needMyJob, queryMap, Enumeration.QualisForms.TESTWISEMYJOB.getqualisforms());
		
		//End of My Jobs
		
		queryMap = manageBatchStatus(nregTypeCode, nregSubTypeCode, userInfo, queryMap);
		
		queryMap = manageWorkListStatus(nregTypeCode, nregSubTypeCode, userInfo, queryMap);
		
		//Start of ResultEntry
		queryMap = manageResultEntryStatus(nregTypeCode, nregSubTypeCode, userInfo, needJobAllocation, needMyJob, needTestInitiate, queryMap);
		//End of ResultEntry
		
		queryMap = manageReleaseStatus(nregTypeCode, nregSubTypeCode, userInfo, queryMap);

		
		final int finalSeqNo = (int) queryMap.get("SeqNo");
		List<String> insertQueryList = (List) queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");
		 
		String insertQuery = "";
		if (insertQueryList.size() > 0) {
			insertQuery = "insert into approvalstatusconfig"
						+ " (nstatusconfigcode, nstatusfunctioncode, nformcode, napprovalsubtypecode, nregtypecode, nregsubtypecode, ntranscode, nsorter,dmodifieddate, nsitecode, nstatus)"
					    + " values " + String.join(",", insertQueryList);
			updateQuery +=  ";update seqnoconfigurationmaster set nsequenceno = " + (finalSeqNo-1) 
								+ " where stablename = 'approvalstatusconfig'";
		}
		
		final String finalQuery = insertQuery + ";" + updateQuery;
		if (finalQuery.trim().length() > 1)
		{
			jdbcTemplate.execute(finalQuery);	
		}			
	
	}
	
	private Map<String, Object> manageRegistrationStatus(final int nregTypeCode, final int nregSubTypeCode, final UserInfo userInfo,
			final Boolean nneedJobAllocation, final Boolean nneedMyJob,
			final Boolean nneedTestInitiate, Map<String, Object> queryMap) throws Exception
	{
		
		int seqNo = (int) queryMap.get("SeqNo");
		List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");
		
		//ALPD-3848- Added Released status
		final List<Integer> statusList = Arrays.asList(Enumeration.TransactionStatus.ALL.gettransactionstatus(),
														Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus(),
														Enumeration.TransactionStatus.REGISTERED.gettransactionstatus(),
														Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus(),
														Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus(),
														Enumeration.TransactionStatus.INITIATED.gettransactionstatus(),	
														Enumeration.TransactionStatus.COMPLETED.gettransactionstatus(),
														Enumeration.TransactionStatus.REJECTED.gettransactionstatus(),
														Enumeration.TransactionStatus.CANCELED.gettransactionstatus(),
														Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus(),
														Enumeration.TransactionStatus.RELEASED.gettransactionstatus()		
														);
		final String codeString = statusList.stream().map(i->i.toString()).collect(Collectors.joining(", "));
		
		final int formCode = Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms();
		
		final String query = " select * from approvalstatusconfig  where "
						+ " nsitecode = " + userInfo.getNmastersitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		                + " and nregtypecode = " + nregTypeCode
						+ " and nregsubtypecode = " + nregSubTypeCode
						+ " and nstatusfunctioncode = "+Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
						+ " and nformcode = " + formCode
						+ " and ntranscode in ("+ codeString + ")";
		
		final List<ApprovalStatusConfig> transValidationList = (List<ApprovalStatusConfig>) jdbcTemplate.query(query, new ApprovalStatusConfig());
		
		final Map<Short, Short> validationMap = transValidationList.stream().collect(Collectors.toMap(
									ApprovalStatusConfig::getNtranscode, control -> control.getNstatus()));
		
		
		final List<String> updateStatusList = new ArrayList<String>();
		int nsorter = 1;
		
		for(Integer statusCode: statusList)
		{
			if (validationMap.containsKey(statusCode.shortValue())) {
				if (validationMap.get(statusCode.shortValue()) == Enumeration.TransactionStatus.DELETED.gettransactionstatus()){
					//update nstatus=1
					updateStatusList.add(Integer.toString(statusCode));
				}						
			}
			else {
				//insert control+initiate status
				insertControlQueryList.add("(" + seqNo++ + ","
						+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
						+ formCode + ","
						+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
						+ nregTypeCode + "," + nregSubTypeCode + "," 
						+ statusCode +","
						+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ")");
			}
		}				

		if(updateStatusList.size()> 0) {
			updateQuery += " ;update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "  where nregtypecode = " + nregTypeCode
						+ " and nregsubtypecode = " + nregSubTypeCode
						+ " and nstatusfunctioncode ="+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
						+ " and nformcode = " + formCode
						+ " and ntranscode in ("+String.join(",", updateStatusList) + ");";		
		}	
			
		queryMap.put("InsertQuery", insertControlQueryList);
		queryMap.put("UpdateQuery", updateQuery);
		queryMap.put("SeqNo", seqNo);
		return queryMap;
		
	}
	
	private Map<String, Object> manageJobAllocationStatus(final int nregTypeCode, final int nregSubTypeCode, final UserInfo userInfo,
			 Boolean nneedJobAllocation,Boolean needMyJob, Boolean needTestInitiate, Map<String, Object> queryMap) throws Exception
	{		
		int seqNo = (int) queryMap.get("SeqNo");
		List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");
		
		if (nneedJobAllocation == true) 
		{
			final List<Integer>statusList = new ArrayList<Integer>();
			statusList.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());	
			statusList.add(Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus());
			statusList.add(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus());		
			statusList.add(Enumeration.TransactionStatus.CANCELED.gettransactionstatus());		

			if(needMyJob) {
				statusList.add(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus());
			}
			if(needTestInitiate) {
				statusList.add(Enumeration.TransactionStatus.INITIATED.gettransactionstatus());
			}
			

			
			final String codeString = statusList.stream().map(i->i.toString()).collect(Collectors.joining(", "));
			
			final int formCode = Enumeration.QualisForms.JOBALLOCATION.getqualisforms();
			
			final String query = " select * from approvalstatusconfig  where "
							+ " nsitecode = " + userInfo.getNmastersitecode()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			                + " and nregtypecode = " + nregTypeCode
							+ " and nregsubtypecode = " + nregSubTypeCode
							+ " and nstatusfunctioncode = "+Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
							+ " and nformcode = " + formCode
							+ " and ntranscode in ("+ codeString + ")";
			
				
			final List<ApprovalStatusConfig> transValidationList = (List<ApprovalStatusConfig>) jdbcTemplate.query(query, new ApprovalStatusConfig());
			
			final Map<Short, Short> validationMap = transValidationList.stream().collect(Collectors.toMap(
										ApprovalStatusConfig::getNtranscode, control -> control.getNstatus()));
			
			
			final List<String> updateStatusList = new ArrayList<String>();
			int nsorter = 1;
			
			for(Integer statusCode: statusList)
			{
				if (validationMap.containsKey(statusCode.shortValue())) {
					if (validationMap.get(statusCode.shortValue()) == Enumeration.TransactionStatus.DELETED.gettransactionstatus()){
						//update nstatus=1
						updateStatusList.add(Integer.toString(statusCode));
					}						
				}
				else {
					//insert control+initiate status
					insertControlQueryList.add("(" + seqNo++ + ","
							+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
							+ formCode + ","
							+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
							+ nregTypeCode + "," + nregSubTypeCode + "," 
							+ statusCode +","
							+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ")");
				}
			}				
			
			if (updateStatusList.size() > 0) {
				updateQuery += " update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " where nregtypecode = " + nregTypeCode
							+ " and nregsubtypecode = " + nregSubTypeCode
							+ " and nstatusfunctioncode ="+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
							+ " and nformcode = " + formCode
							+ " and ntranscode in ("+String.join(",", updateStatusList) + ");";
			}
		}
		else {
			///-Joballocationscreen: registered(-1), receiveinlab(-1), allotted(-1), all(-1)
			updateQuery +=  " update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
					        + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ "  where nregtypecode = " + nregTypeCode
							+ " and nregsubtypecode = " + nregSubTypeCode
							+ " and nstatusfunctioncode = "+Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
							+ " and nformcode = " + Enumeration.QualisForms.JOBALLOCATION.getqualisforms()+";";

		}
		
		queryMap.put("InsertQuery", insertControlQueryList);
		queryMap.put("UpdateQuery", updateQuery);
		queryMap.put("SeqNo", seqNo);
		return queryMap;
		

	}
	
	private Map<String, Object> manageMyJobStatus(final int nregTypeCode, final int nregSubTypeCode, 
			final UserInfo userInfo, final Boolean nneedJobAllocation, final Boolean nneedMyJob,
			Map<String, Object> queryMap, final int formCode) throws Exception{
		
	
			int seqNo = (int) queryMap.get("SeqNo");
			List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
			String updateQuery = (String) queryMap.get("UpdateQuery");			
					
			if(nneedMyJob == true) 
			{		

				final List<Integer> statusList = new ArrayList<Integer>();
				
				if (nneedJobAllocation == true) {
					statusList.add(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus());
				}
				else {
					statusList.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());		
				}
				statusList.add(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus());
		
				final String codeString = statusList.stream().map(i->i.toString()).collect(Collectors.joining(", "));
				
				final String query = " select * from approvalstatusconfig  where "
			                + " nregtypecode = " + nregTypeCode
							+ " and nregsubtypecode = " + nregSubTypeCode
							+ " and nstatusfunctioncode = "+Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
							+ " and nformcode = " + formCode
							+ " and ntranscode in ("+ codeString + ")";
		
			
				final List<ApprovalStatusConfig> transValidationList = (List<ApprovalStatusConfig>) jdbcTemplate.query(query, new ApprovalStatusConfig());
				
				final Map<Short, Short> validationMap = transValidationList.stream().collect(Collectors.toMap(
											ApprovalStatusConfig::getNtranscode, control -> control.getNstatus()));
				
				
				final List<String> updateStatusList = new ArrayList<String>();
				int nsorter = 1;
				
				for(Integer statusCode: statusList)
				{
					if (validationMap.containsKey(statusCode.shortValue())) {
						if (validationMap.get(statusCode.shortValue()) == Enumeration.TransactionStatus.DELETED.gettransactionstatus()){
							//update nstatus=1
							updateStatusList.add(Integer.toString(statusCode));
						}						
					}
					else {
						//insert status
						insertControlQueryList.add("(" + seqNo++ + ","
								+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
								+ formCode + ","
								+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
								+ nregTypeCode + "," + nregSubTypeCode + "," 
								+ statusCode +","
								+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', "+ userInfo.getNmastersitecode() +","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ ")");
					}
				}				
				
				if (updateStatusList.size() > 0) {
					updateQuery += " update approvalstatusconfig set nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "  where nregtypecode = " + nregTypeCode
								+ " and nregsubtypecode = " + nregSubTypeCode
								+ " and nstatusfunctioncode ="+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
								+ " and nformcode = " + formCode
								+ " and ntranscode in ("+String.join(",", updateStatusList) + ");";
				}
				
				if (nneedJobAllocation == true) {
					updateQuery += " update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+ "  where nregtypecode = " + nregTypeCode
								+ " and nregsubtypecode = " + nregSubTypeCode
								+ " and nstatusfunctioncode ="+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
								+ " and nformcode = " + formCode
								+ " and ntranscode = " + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+";";
				}
				else {
					updateQuery += " update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+ "  where nregtypecode = " + nregTypeCode
								+ " and nregsubtypecode = " + nregSubTypeCode
								+ " and nstatusfunctioncode ="+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
								+ " and nformcode = " + formCode
								+ " and ntranscode = " + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()+";";
				}
			}
			else {
				updateQuery += " update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+ "  where nregtypecode = " + nregTypeCode
								+ " and nregsubtypecode = " + nregSubTypeCode
								+ " and nstatusfunctioncode ="+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
								+ " and nformcode = " + formCode+";";
								
			}
			
			queryMap.put("InsertQuery", insertControlQueryList);
			queryMap.put("UpdateQuery", updateQuery);
			queryMap.put("SeqNo", seqNo);
			
//		}
		return queryMap;

	}
	
	private Map<String, Object> manageResultEntryStatus(final int nregTypeCode, final int nregSubTypeCode, 
			final UserInfo userInfo, final Boolean nneedJobAllocation, final Boolean nneedMyJob,
			final Boolean nneedTestInitiate, Map<String, Object> queryMap) throws Exception{
		
		int seqNo = (int) queryMap.get("SeqNo");
		List<String> insertQueryList = (List) queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");
		
		String query = "";
		//ApprovalStatusConfig statusConfig = null;
		int nsorter = 1;
		
		query =  " select * from approvalstatusconfig  where "
				+ " nsitecode = " + userInfo.getNmastersitecode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
                + " and nregtypecode = " + nregTypeCode
				+ " and nregsubtypecode = " + nregSubTypeCode
				+ " and nstatusfunctioncode =" + Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
				+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
				+ " and ntranscode in (" 
				+ Enumeration.TransactionStatus.ALL.gettransactionstatus()
				+ "," + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
				+ "," + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()
				+ "," + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
				+ "," + Enumeration.TransactionStatus.INITIATED.gettransactionstatus() 
				+ "," + Enumeration.TransactionStatus.RECALC.gettransactionstatus()+")";
		
		final List<ApprovalStatusConfig> transValidationList = (List<ApprovalStatusConfig>) jdbcTemplate.query(query, new ApprovalStatusConfig());
		
		final Map<Short, Short> statusMap = transValidationList.stream().collect(Collectors.toMap(			
				ApprovalStatusConfig::getNtranscode, control -> control.getNstatus()));	
		
		if(!statusMap.containsKey((short)Enumeration.TransactionStatus.ALL.gettransactionstatus())) {
			
			insertQueryList.add( "(" + seqNo++ + ","
					+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
					+ Enumeration.QualisForms.RESULTENTRY.getqualisforms()  + ","
							+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
					+ nregTypeCode + "," + nregSubTypeCode + "," 
					+ Enumeration.TransactionStatus.ALL.gettransactionstatus() +","
					+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")");
		}
		
		if(nneedTestInitiate == true)
		{
			
			if(!statusMap.containsKey((short)Enumeration.TransactionStatus.INITIATED.gettransactionstatus())) {
				//add test initiate
				insertQueryList.add( "(" + seqNo++ + ","
							+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
							+ Enumeration.QualisForms.RESULTENTRY.getqualisforms()  + ","
									+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
							+ nregTypeCode + "," + nregSubTypeCode + "," 
							+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() +","
							+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ")");
			}
			else {
			
				if(statusMap.get((short)Enumeration.TransactionStatus.INITIATED.gettransactionstatus()) == Enumeration.TransactionStatus.DELETED.gettransactionstatus()) {
					//update testinitiate = 1
					
					updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)
								+"', nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "  where nregtypecode = " + nregTypeCode
								+ " and nregsubtypecode = " + nregSubTypeCode
								+ " and nstatusfunctioncode =  " + Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
								+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
								+ " and ntranscode =" + Enumeration.TransactionStatus.INITIATED.gettransactionstatus()+";";
				}
			}
			
			if (nneedMyJob == true) {
				
				if(!statusMap.containsKey((short)Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()))
				{
			    	insertQueryList.add( "(" + seqNo++ + ","
							+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
							+ Enumeration.QualisForms.RESULTENTRY.getqualisforms()  + ","
									+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
							+ nregTypeCode + "," + nregSubTypeCode + "," 
							+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() +","
							+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ")");
			    }
			    else {
			    	updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)
			    			+"', nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "  where nregtypecode = " + nregTypeCode
							+ " and nregsubtypecode = " + nregSubTypeCode
							+ " and nstatusfunctioncode = "+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
							+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
							+ " and ntranscode =" + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
							+ ";";
			    }
				
				updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
				        + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ "  where nregtypecode = " + nregTypeCode
						+ " and nregsubtypecode = " + nregSubTypeCode
						+ " and nstatusfunctioncode ="
								+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
						+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
						+ " and ntranscode in (" + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()
						+ ","+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + ");";
		
			}
			else {
				if (nneedJobAllocation == true ) {
					
					
					if(!statusMap.containsKey((short)Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()))
					{
				    	insertQueryList.add("(" + seqNo++ + ","
								+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
								+ Enumeration.QualisForms.RESULTENTRY.getqualisforms()  + ","
										+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
								+ nregTypeCode + "," + nregSubTypeCode + "," 
								+ Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus() +","
								+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ ")");
				    }
				    else {
				    	updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
						        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "  where nregtypecode = " + nregTypeCode
								+ " and nregsubtypecode = " + nregSubTypeCode
								+ " and nstatusfunctioncode = "
										+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
								+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
								+ " and ntranscode =" + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()
								+ ";";
				    }
					
					updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
					        + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ "  where nregtypecode = " + nregTypeCode
							+ " and nregsubtypecode = " + nregSubTypeCode
							+ " and nstatusfunctioncode =  "
									+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
							+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
							+ " and ntranscode in(" + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
							+ ","+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + ");";
				}
				else {
					
							
					if(!statusMap.containsKey((short)Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()))
					{
				    	insertQueryList.add("(" + seqNo++ + ","
								+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
								+ Enumeration.QualisForms.RESULTENTRY.getqualisforms()  + ","
										+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
								+ nregTypeCode + "," + nregSubTypeCode + "," 
								+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() +","
								+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ ")");
				    }
				    else {
				    	updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
						        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "  where nregtypecode = " + nregTypeCode
								+ " and nregsubtypecode = " + nregSubTypeCode
								+ " and nstatusfunctioncode ="
										+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
								+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
								+ " and ntranscode =" + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
								+ ";";
				    }
					
					updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
					        + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ "  where nregtypecode = " + nregTypeCode
							+ " and nregsubtypecode = " + nregSubTypeCode
							+ " and nstatusfunctioncode ="
									+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
							+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
							+ " and ntranscode in(" + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()
							+ ","+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ");";
				}
			}	

			//End of  nneedTestInitiate == true
		}
		else {
			//Start nneedTestInitiate == false
			
			//status to remove =test initiated	
	
			updateQuery += " update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
						        + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+ "  where nregtypecode = " + nregTypeCode
								+ " and nregsubtypecode = " + nregSubTypeCode
								+ " and nstatusfunctioncode ="
										+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
								+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
								+ " and ntranscode =" + Enumeration.TransactionStatus.INITIATED.gettransactionstatus()+ ";";
			
			
			if (nneedMyJob == true) {
				
				if(!statusMap.containsKey((short)Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus())) {
					
			    	insertQueryList.add("(" + seqNo++ + ","
							+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
							+ Enumeration.QualisForms.RESULTENTRY.getqualisforms() + ","
							+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
							+ nregTypeCode + "," + nregSubTypeCode + "," 
							+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() +","
							+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ")");
			    }
			    else {
			    	updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
					        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "  where nregtypecode = " + nregTypeCode
							+ " and nregsubtypecode = " + nregSubTypeCode
							+ " and nstatusfunctioncode = "
									+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
							+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
							+ " and ntranscode =" + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
							+ ";";
			    }
			    
				updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
					        + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ "  where nregtypecode = " + nregTypeCode
							+ " and nregsubtypecode = " + nregSubTypeCode
							+ " and nstatusfunctioncode = "
									+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
							+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
							+ " and ntranscode in(" + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()
							+ ","+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + ");";
			
			}
			else {
				if (nneedJobAllocation == true ) {
					
					if(!statusMap.containsKey((short)Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus())) {
						
				    	insertQueryList.add("(" + seqNo++ + ","
								+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +"," 
								+ Enumeration.QualisForms.RESULTENTRY.getqualisforms() + ","
								+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
								+ nregTypeCode + "," + nregSubTypeCode + "," 
								+ Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus() +","
								+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ ")");
				    }
				    else {
				    	updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
						        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "  where nregtypecode = " + nregTypeCode
								+ " and nregsubtypecode = " + nregSubTypeCode
								+ " and nstatusfunctioncode = "
										+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
								+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
								+ " and ntranscode =" + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()
								+ ";";
				    }
					
					updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
					        + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ "  where nregtypecode = " + nregTypeCode
							+ " and nregsubtypecode = " + nregSubTypeCode
							+ " and nstatusfunctioncode ="
									+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
							+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
							+ " and ntranscode in(" + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
							+ ","+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ");";
				
				}
				else {
					
					if(!statusMap.containsKey((short)Enumeration.TransactionStatus.REGISTERED.gettransactionstatus())) {
						
				    	insertQueryList.add("(" + seqNo++ + ","
								+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
								+ Enumeration.QualisForms.RESULTENTRY.getqualisforms() + ","
								+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
								+ nregTypeCode + "," + nregSubTypeCode + "," 
								+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() +","
								+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ ")");
				    }
				    else {
				    	updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
						        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "  where nregtypecode = " + nregTypeCode
								+ " and nregsubtypecode = " + nregSubTypeCode
								+ " and nstatusfunctioncode = "
										+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
								+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
								+ " and ntranscode =" + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
								+ ";";
				    }
					
					updateQuery += ";update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus="
					        + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ "  where nregtypecode = " + nregTypeCode
							+ " and nregsubtypecode = " + nregSubTypeCode
							+ " and nstatusfunctioncode = "
									+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() 
							+ " and nformcode = " + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
							+ " and ntranscode in(" + Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()
							+ ","+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ");";
				
				}
			}	
			//End of  nneedTestInitiate == false
		}			
		

		
		if(!statusMap.containsKey((short)Enumeration.TransactionStatus.RECALC.gettransactionstatus())) {
			
			insertQueryList.add( "(" + seqNo++ + ","
					+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
					+ Enumeration.QualisForms.RESULTENTRY.getqualisforms()  + ","
							+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
					+ nregTypeCode + "," + nregSubTypeCode + "," 
					+ Enumeration.TransactionStatus.RECALC.gettransactionstatus() +","
					+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")");
		}
		queryMap.put("InsertQuery", insertQueryList);
		queryMap.put("UpdateQuery", updateQuery);
		queryMap.put("SeqNo", seqNo);
		return queryMap;
		
	}	
	
	
	private Map<String, Object> manageReleaseStatus(final int nregTypeCode, final int nregSubTypeCode, final UserInfo userInfo,
			 Map<String, Object> queryMap) throws Exception
	{
		
		int seqNo = (int) queryMap.get("SeqNo");
		List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");
		
		final List<Integer> statusList = Arrays.asList(Enumeration.TransactionStatus.RELEASED.gettransactionstatus());
		final String codeString = statusList.stream().map(i->i.toString()).collect(Collectors.joining(", "));
		
		final int formCode = Enumeration.QualisForms.RELEASE.getqualisforms();
		
		final String query = " select * from approvalstatusconfig  where "
						+ " nsitecode = " + userInfo.getNmastersitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		                + " and nregtypecode = " + nregTypeCode
						+ " and nregsubtypecode = " + nregSubTypeCode
						+ " and nstatusfunctioncode = "+Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
						+ " and nformcode = " + formCode
						+ " and ntranscode in ("+ codeString + ")";
		
		final List<ApprovalStatusConfig> transValidationList = (List<ApprovalStatusConfig>) jdbcTemplate.query(query, new ApprovalStatusConfig());
		
		final Map<Short, Short> validationMap = transValidationList.stream().collect(Collectors.toMap(
									ApprovalStatusConfig::getNtranscode, control -> control.getNstatus()));
		
		
		final List<String> updateStatusList = new ArrayList<String>();
		int nsorter = 1;
		
		for(Integer statusCode: statusList)
		{
			if (validationMap.containsKey(statusCode.shortValue())) {
				if (validationMap.get(statusCode.shortValue()) == Enumeration.TransactionStatus.DELETED.gettransactionstatus()){
					//update nstatus=1
					updateStatusList.add(Integer.toString(statusCode));
				}						
			}
			else {
				insertControlQueryList.add("(" + seqNo++ + ","
						+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
						+ formCode + ","
						+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
						+ nregTypeCode + "," + nregSubTypeCode + "," 
						+ statusCode +","
						+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ")");
			}
		}	
		
		if(updateStatusList.size()> 0) {
			updateQuery += " ;update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "  where nregtypecode = " + nregTypeCode
						+ " and nregsubtypecode = " + nregSubTypeCode
						+ " and nstatusfunctioncode ="+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
						+ " and nformcode = " + formCode
						+ " and ntranscode in ("+String.join(",", updateStatusList) + ");";
		
		}	
		queryMap.put("InsertQuery", insertControlQueryList);
		queryMap.put("UpdateQuery", updateQuery);
		queryMap.put("SeqNo", seqNo);
		return queryMap;		
	}
	
	private Map<String, Object> manageWorkListStatus(final int nregTypeCode, final int nregSubTypeCode, final UserInfo userInfo,
			Map<String, Object> queryMap) throws Exception
	{
		
		int seqNo = (int) queryMap.get("SeqNo");
		List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");
		
		final List<Integer> statusList = Arrays.asList(Enumeration.TransactionStatus.DRAFT.gettransactionstatus(),
														Enumeration.TransactionStatus.PREPARED.gettransactionstatus(),Enumeration.TransactionStatus.ALL.gettransactionstatus());
		final String codeString = statusList.stream().map(i->i.toString()).collect(Collectors.joining(", "));
		
		final int formCode = Enumeration.QualisForms.WORKLIST.getqualisforms();
		
		final String query = " select * from approvalstatusconfig  where "
						+ " nsitecode = " + userInfo.getNmastersitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		                + " and nregtypecode = " + nregTypeCode
						+ " and nregsubtypecode = " + nregSubTypeCode
						+ " and nstatusfunctioncode = "+Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
						+ " and nformcode = " + formCode
						+ " and ntranscode in ("+ codeString + ")";
		
		final List<ApprovalStatusConfig> transValidationList = (List<ApprovalStatusConfig>) 
					jdbcTemplate.query(query, new ApprovalStatusConfig());
		
		final Map<Short, Short> validationMap = transValidationList.stream().collect(Collectors.toMap(
									ApprovalStatusConfig::getNtranscode, control -> control.getNstatus()));
		
		
		final List<String> updateStatusList = new ArrayList<String>();
		int nsorter = 1;
		
		for(Integer statusCode: statusList)
		{
			if (validationMap.containsKey(statusCode.shortValue())) {
				if (validationMap.get(statusCode.shortValue()) == Enumeration.TransactionStatus.DELETED.gettransactionstatus()){
					//update nstatus=1
					updateStatusList.add(Integer.toString(statusCode));
				}						
			}
			else {
				//insert control+initiate status
				insertControlQueryList.add("(" + seqNo++ + ","
						+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
						+ formCode + ","
						+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
						+ nregTypeCode + "," + nregSubTypeCode + "," 
						+ statusCode +","
						+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ")");
			}
		}				

		if(updateStatusList.size()> 0) {
			updateQuery += " ;update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "  where nregtypecode = " + nregTypeCode
						+ " and nregsubtypecode = " + nregSubTypeCode
						+ " and nstatusfunctioncode ="+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
						+ " and nformcode = " + formCode
						+ " and ntranscode in ("+String.join(",", updateStatusList) + ");";		
		}	
			
		queryMap.put("InsertQuery", insertControlQueryList);
		queryMap.put("UpdateQuery", updateQuery);
		queryMap.put("SeqNo", seqNo);
		return queryMap;
		
	}
	
	
	private Map<String, Object> manageBatchStatus(final int nregTypeCode, final int nregSubTypeCode, final UserInfo userInfo,
			Map<String, Object> queryMap) throws Exception
	{
		
		int seqNo = (int) queryMap.get("SeqNo");
		List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");
		//ALPD-5076 added by Dhanushya RI,All status is added when add the registrationsubtype
		final List<Integer> statusList = Arrays.asList(Enumeration.TransactionStatus.ALL.gettransactionstatus(),Enumeration.TransactionStatus.DRAFT.gettransactionstatus(),
														Enumeration.TransactionStatus.INITIATED.gettransactionstatus(),
														Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
														
														);
		final String codeString = statusList.stream().map(i->i.toString()).collect(Collectors.joining(", "));
		
		final int formCode = Enumeration.QualisForms.BATCHCREATION.getqualisforms();
		
		final String query = " select * from approvalstatusconfig  where "
						+ " nsitecode = " + userInfo.getNmastersitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		                + " and nregtypecode = " + nregTypeCode
						+ " and nregsubtypecode = " + nregSubTypeCode
						+ " and nstatusfunctioncode = "+Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
						+ " and nformcode = " + formCode
						+ " and ntranscode in ("+ codeString + ")";
		
		final List<ApprovalStatusConfig> transValidationList = (List<ApprovalStatusConfig>) 
					jdbcTemplate.query(query, new ApprovalStatusConfig());
		
		final Map<Short, Short> validationMap = transValidationList.stream().collect(Collectors.toMap(
									ApprovalStatusConfig::getNtranscode, control -> control.getNstatus()));
		
		
		final List<String> updateStatusList = new ArrayList<String>();
		int nsorter = 1;
		
		for(Integer statusCode: statusList)
		{
			if (validationMap.containsKey(statusCode.shortValue())) {
				if (validationMap.get(statusCode.shortValue()) == Enumeration.TransactionStatus.DELETED.gettransactionstatus()){
					//update nstatus=1
					updateStatusList.add(Integer.toString(statusCode));
				}						
			}
			else {
				//insert control+initiate status
				insertControlQueryList.add("(" + seqNo++ + ","
						+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype() +","
						+ formCode + ","
						+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
						+ nregTypeCode + "," + nregSubTypeCode + "," 
						+ statusCode +","
						+ nsorter++ +",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ")");
			}
		}				

		if(updateStatusList.size()> 0) {
			updateQuery += " ;update approvalstatusconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "  where nregtypecode = " + nregTypeCode
						+ " and nregsubtypecode = " + nregSubTypeCode
						+ " and nstatusfunctioncode ="+ Enumeration.ApprovalStatusFunction.SAMPLEREGISTRATIONFILTER.getNstatustype()
						+ " and nformcode = " + formCode
						+ " and ntranscode in ("+String.join(",", updateStatusList) + ");";		
		}	
			
		queryMap.put("InsertQuery", insertControlQueryList);
		queryMap.put("UpdateQuery", updateQuery);
		queryMap.put("SeqNo", seqNo);
		return queryMap;
		
	}
	
	@SuppressWarnings("serial")
	private void manageValidationStatus(final JSONObject jsonData, final int nregTypeCode, 
			final int nregSubTypeCode, final UserInfo userInfo) throws Exception
	{	
		final Boolean needJobAllocation = jsonData.has("nneedjoballocation") ? (Boolean)jsonData.get("nneedjoballocation") :false;
		final Boolean needMyJob = jsonData.has("nneedmyjob") ?  (Boolean)jsonData.get("nneedmyjob") :false;
		final Boolean needTestInitiate = jsonData.has("nneedtestinitiate") ?  (Boolean)jsonData.get("nneedtestinitiate") :false;
		final Boolean needBatch = jsonData.has("nneedbatch") ?  (Boolean)jsonData.get("nneedbatch") :false;
		final Boolean needWorkList = jsonData.has("nneedworklist") ?  (Boolean)jsonData.get("nneedworklist") :false;
		
		//SYED commented on 10-APR-2025
		//final Boolean needSubSample = jsonData.has("nneedsubsample") ?  (Boolean)jsonData.get("nneedsubsample") :false;
		
		
		final String getSequenceNo = "select nsequenceno+1 from seqnoconfigurationmaster where stablename ='transactionvalidation'";
		final int seqNo = (int) jdbcUtilityFunction.queryForObject(getSequenceNo, Integer.class, jdbcTemplate);
		
		Map<String, Object> queryMap = new HashMap<String, Object>() {{
			put("InsertQuery", new ArrayList<>());
			put("UpdateQuery", "");
			put("SeqNo", seqNo);
		}};		
	
		//Sample Registration Screen
		queryMap = manageRegistrationValidationStatus(nregTypeCode, nregSubTypeCode, userInfo, needTestInitiate, needJobAllocation, needMyJob, queryMap);
		//End of Sample Registration
		
		//Job Allocation Validation Status
		queryMap = manageJobAllocationValidationStatus(nregTypeCode, nregSubTypeCode, userInfo, needMyJob, needJobAllocation, needTestInitiate, queryMap)	;
		//End Allocation Validation Status
		
		//MyJob Screen Validation Status
				
		 queryMap = manageMyJobValidationStatus(nregTypeCode, nregSubTypeCode, userInfo, needMyJob, 
				 needJobAllocation, queryMap, Enumeration.QualisForms.MYJOB.getqualisforms());		
		
		 queryMap = manageMyJobValidationStatus(nregTypeCode, nregSubTypeCode, userInfo, needMyJob, 
				 needJobAllocation, queryMap, Enumeration.QualisForms.TESTWISEMYJOB.getqualisforms());		
			
		 //End MyJob Screen  Validation Status		 
		 
		queryMap = manageBatchValidationStatus(nregTypeCode, nregSubTypeCode, userInfo, needMyJob, needJobAllocation, needBatch, queryMap);
		
		queryMap = manageWorkListValidationStatus(nregTypeCode, nregSubTypeCode, userInfo, needMyJob, needJobAllocation, needWorkList, queryMap);
				 
		//Result Entry Screen Validation Status
		 queryMap = manageResultEntryValidationStatus(nregTypeCode, nregSubTypeCode, userInfo, needMyJob, needJobAllocation, needTestInitiate, queryMap);		
		//End Result Entry Screen  Validation Status
		 
	
		final int finalSeqNo = (int) queryMap.get("SeqNo");
		List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");
		 
		String insertQuery = "";
		if (insertControlQueryList.size() > 0) {
			insertQuery = "insert into transactionvalidation"
						+ " (ntransactionvalidationcode,nformcode,nregtypecode, nregsubtypecode,ncontrolcode,ntransactionstatus,nsitecode,dmodifieddate,nstatus)"
					    + " values " + String.join(",", insertControlQueryList);
			updateQuery +=  ";update seqnoconfigurationmaster set nsequenceno = " + (finalSeqNo-1) 
								+ " where stablename = 'transactionvalidation'";
		}
		
		final String finalQuery = insertQuery + ";" + updateQuery;
		if (finalQuery.trim().length() > 1)
		{
			System.out.println("query:"+ finalQuery);
			jdbcTemplate.execute(finalQuery);	
		}

	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> manageRegistrationValidationStatus(final int nregTypeCode, 
			final int nregSubTypeCode, final UserInfo userInfo, final Boolean needTestInitiate,
			final Boolean needJobAllocation, final Boolean needMyJob,
			final Map<String, Object> queryMap) throws Exception
	{		
		String query = " select ncontrolcode, scontrolname from controlmaster where nformcode="
						+ Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()
						+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<ControlMaster> controlList = jdbcTemplate.query(query, new ControlMaster());		
		
		final Map<String, Short> controlMap = controlList.stream().collect(Collectors.toMap(
					ControlMaster::getScontrolname,control -> control.getNcontrolcode()));	
		final short cancelSampleCode = controlMap.get("CancelReject");
		final short cancelTestCode = controlMap.get("CancelTest");
		final short cancelSubSampleCode = controlMap.get("CancelSubSample");
		final List<Short> controlCodeList = Arrays.asList(cancelSampleCode, cancelTestCode, cancelSubSampleCode);	
		
		
		int seqNo = (int) queryMap.get("SeqNo");
		List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");
		
		
		final List<Integer> statusList = new ArrayList<Integer>();
		statusList.add(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
		statusList.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
		statusList.add(Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus());
		if (needJobAllocation) 
		{		
			//Product FRS-00410 - Test can be cancelled before Test Completed (including Retest process).
			statusList.add(Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus());	
			statusList.add(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus());	
		}
		else {			
			updateQuery += "update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()
							+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
							+ "	and ntransactionstatus = " +  Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus() +";";	
			
			updateQuery += "update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()
							+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
							+ "	and ntransactionstatus = " +  Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus() +";";
		
		}			
		if (needMyJob) 
		{		
			//Product FRS-00410 - Test can be cancelled before Test Completed (including Retest process).
			statusList.add(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus());	
		}
		else {			
			updateQuery += "update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()
							+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
							+ "	and ntransactionstatus = " +  Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() +";";		
		
		}
		// ALPD-1969
		if (needTestInitiate) 
		{		
			//Product FRS-00410 - Test can be cancelled before Test Completed (including Retest process).
			statusList.add(Enumeration.TransactionStatus.INITIATED.gettransactionstatus());	
		}
		else {			
			updateQuery += "update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()
							+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
							+ "	and ntransactionstatus = " +  Enumeration.TransactionStatus.INITIATED.gettransactionstatus() +";";		
	
		}	
		
		
		queryMap.put("InsertQuery", insertControlQueryList);
		queryMap.put("UpdateQuery", updateQuery);
		queryMap.put("SeqNo", seqNo);	
		
		return generateValidationStatusQuery(statusList, nregTypeCode,	nregSubTypeCode, controlCodeList, userInfo,
				Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms(), queryMap);
		
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> manageJobAllocationValidationStatus(final int nregTypeCode, 
			final int nregSubTypeCode, final UserInfo userInfo, final Boolean needMyJob,
			final Boolean needJobAllocation, final Boolean needTestInitiate, Map<String, Object> queryMap) throws Exception
	{
			int seqNo = (int) queryMap.get("SeqNo");
			List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
			String updateQuery = (String) queryMap.get("UpdateQuery");	
			
			final int formCode = Enumeration.QualisForms.JOBALLOCATION.getqualisforms();
		
			if( needJobAllocation == true) {
				
				String query = " select ncontrolcode, scontrolname from controlmaster where nformcode="
								+ Enumeration.QualisForms.JOBALLOCATION.getqualisforms()
								+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<ControlMaster> controlList = jdbcTemplate.query(query, new ControlMaster());
				
				
				
				/**
				 * ReceiveInLab, Allot, Allot Another User, Reschedule, Cancel
				 * ReceiveInLab- for Registered test
				 * Allot - for ReceivedInLab Tests
				 * Allot Another User- for Allotted/Accepted/Test Initiate Tests
				 * Reschedule - for Allotted Tests
				 * Cancel - Registered/receiveinLab/Allotted
				 * */				
		
				
				
				query = " select ntransactionvalidationcode, nformcode, nregtypecode, nregsubtypecode, ncontrolcode, ntransactionstatus, "
						+ "nsitecode, nstatus from transactionvalidation where nformcode="	+ formCode
						+ " and nsitecode = " + userInfo.getNmastersitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode;
				final List<TransactionValidation> transValidationList = (List<TransactionValidation>)
						jdbcTemplate.query(query, new TransactionValidation());
		
				if (transValidationList.isEmpty()) {
					
					for(ControlMaster controlMaster:controlList) 
					{					
						if (controlMaster.getScontrolname().equalsIgnoreCase("ReceiveinLab")) {				
							insertControlQueryList.add(	"(" + seqNo++ + ","
									+ formCode + ","
									+ nregTypeCode + ","+ nregSubTypeCode + ","
									+ controlMaster.getNcontrolcode() + ","
									+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
									+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");		
						}
						else if (controlMaster.getScontrolname().equalsIgnoreCase("Allotted")||controlMaster.getScontrolname().equalsIgnoreCase("AllotCalender")) {				
							insertControlQueryList.add(	"(" + seqNo++ + ","
									+ formCode + ","
									+ nregTypeCode + ","+ nregSubTypeCode + ","
									+ controlMaster.getNcontrolcode() + ","
									+ Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
									+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");			
						}
					
						else if (controlMaster.getScontrolname().equalsIgnoreCase("AllotAnotherUser")) {
							insertControlQueryList.add(	"(" + seqNo++ + ","
									+ formCode + ","
									+ nregTypeCode + ","+ nregSubTypeCode + ","
									+ controlMaster.getNcontrolcode() + ","
									+ Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
									+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
							if(needMyJob) {
								insertControlQueryList.add(	"(" + seqNo++ + ","
										+ formCode + ","
										+ nregTypeCode + ","+ nregSubTypeCode + ","
										+ controlMaster.getNcontrolcode() + ","
										+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
										+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
							}
							if (needTestInitiate) {
							   insertControlQueryList.add(	"(" + seqNo++ + ","
										+ formCode + ","
										+ nregTypeCode + ","+ nregSubTypeCode + ","
										+ controlMaster.getNcontrolcode() + ","
										+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
										+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
							}
						}
						else if (controlMaster.getScontrolname().equalsIgnoreCase("Reschedule")) {
							insertControlQueryList.add(	"(" + seqNo++ + ","
									+ formCode + ","
									+ nregTypeCode + ","+ nregSubTypeCode + ","
									+ controlMaster.getNcontrolcode() + ","
									+ Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
									+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
							
							if(needMyJob) {
								insertControlQueryList.add(	"(" + seqNo++ + ","
										+ formCode + ","
										+ nregTypeCode + ","+ nregSubTypeCode + ","
										+ controlMaster.getNcontrolcode() + ","
										+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
										+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
							}
							
						}
						else if (controlMaster.getScontrolname().equalsIgnoreCase("Cancel")) {
							
							insertControlQueryList.add(	"(" + seqNo++ + ","
									+ formCode + ","
									+ nregTypeCode + ","+ nregSubTypeCode + ","
									+ controlMaster.getNcontrolcode() + ","
									+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
									+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
							insertControlQueryList.add(	"(" + seqNo++ + ","
									+ formCode + ","
									+ nregTypeCode + ","+ nregSubTypeCode + ","
									+ controlMaster.getNcontrolcode() + ","
									+ Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
									+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
							insertControlQueryList.add(	"(" + seqNo++ + ","
									+ formCode + ","
									+ nregTypeCode + ","+ nregSubTypeCode + ","
									+ controlMaster.getNcontrolcode() + ","
									+ Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
									+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
							
							if(needMyJob) {
								insertControlQueryList.add(	"(" + seqNo++ + ","
										+ formCode + ","
										+ nregTypeCode + ","+ nregSubTypeCode + ","
										+ controlMaster.getNcontrolcode() + ","
										+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
										+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
							}
						}
						else if(controlMaster.getScontrolname().equalsIgnoreCase("ChangeTestSection")) {
							queryMap=dynamicStatusValidation(seqNo,nregTypeCode,nregSubTypeCode,formCode,controlMaster.getNcontrolcode(),userInfo,transValidationList,queryMap );
							insertControlQueryList.addAll((List<String>) queryMap.get("insertList"));
							seqNo=(int) queryMap.get("SeqNo");
						}
												
					}
					
					queryMap.put("InsertQuery", insertControlQueryList);
					queryMap.put("UpdateQuery", updateQuery);
					queryMap.put("SeqNo", seqNo);
				}
				else 
				{
					for(ControlMaster controlMaster:controlList) 
					{
						if (controlMaster.getScontrolname().equalsIgnoreCase("ReceiveinLab")) {			
							int transCode = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
							
							queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, formCode, 
									controlMaster.getNcontrolcode(), transCode, userInfo, transValidationList, queryMap);
						
						}
						else if (controlMaster.getScontrolname().equalsIgnoreCase("Allotted")||controlMaster.getScontrolname().equalsIgnoreCase("AllotCalender")) {	
							
							int transCode = Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus();
							
							queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, formCode, 
									controlMaster.getNcontrolcode(), transCode, userInfo, transValidationList, queryMap);
									
						}
						else if (controlMaster.getScontrolname().equalsIgnoreCase("AllotAnotherUser")) {
							
							int transCode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
							
							queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, formCode, 
									controlMaster.getNcontrolcode(), transCode, userInfo, transValidationList, queryMap);
							
							
							if(needMyJob) {
								
								transCode = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
								
								queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, formCode, 
										controlMaster.getNcontrolcode(), transCode, userInfo, transValidationList, queryMap);
								
							}
							if (needTestInitiate) {
								
								transCode = Enumeration.TransactionStatus.INITIATED.gettransactionstatus();
								
								queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, formCode, 
										controlMaster.getNcontrolcode(), transCode, userInfo, transValidationList, queryMap);								
							  
							}
						}
						else if (controlMaster.getScontrolname().equalsIgnoreCase("Reschedule")) {
							
							int transCode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
							
							queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, formCode, 
									controlMaster.getNcontrolcode(), transCode, userInfo, transValidationList, queryMap);	
							
						}
						else if (controlMaster.getScontrolname().equalsIgnoreCase("Cancel")) {
							
							int transCode = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
							
							queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, formCode, 
									controlMaster.getNcontrolcode(), transCode, userInfo, transValidationList, queryMap);	
							
							transCode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
							
							queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, formCode, 
									controlMaster.getNcontrolcode(), transCode, userInfo, transValidationList, queryMap);	
							
							transCode = Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus();
							
							queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, formCode, 
									controlMaster.getNcontrolcode(), transCode, userInfo, transValidationList, queryMap);							
							
						}
							else if (controlMaster.getScontrolname().equalsIgnoreCase("Reschedule")) {
							
							int transCode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
							
							queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, formCode, 
									controlMaster.getNcontrolcode(), transCode, userInfo, transValidationList, queryMap);	
							
						}
							else if(controlMaster.getScontrolname().equalsIgnoreCase("ChangeTestSection")) {
								queryMap=dynamicStatusValidation(seqNo,nregTypeCode,nregSubTypeCode,formCode,controlMaster.getNcontrolcode(),userInfo,transValidationList,queryMap );

								}
						
					}					
					
				}				
			}
			else {
				updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
							+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
							+";";
				
				queryMap.put("InsertQuery", insertControlQueryList);
				queryMap.put("UpdateQuery", updateQuery);
				queryMap.put("SeqNo", seqNo);
			}				
			
			return queryMap;
			
	}
	
	private Map<String, Object> manageMyJobValidationStatus(final int nregTypeCode, 
			final int nregSubTypeCode, final UserInfo userInfo, final Boolean needMyJob,
			final Boolean needJobAllocation, Map<String, Object> queryMap, final int formCode) throws Exception
	{		
	
		
		int seqNo = (int) queryMap.get("SeqNo");
		List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");

			
			String query = " select ncontrolcode, scontrolname from controlmaster where nformcode=" + formCode
						+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and scontrolname in ('Accept', 'Revert');";
			final List<ControlMaster> controlList = jdbcTemplate.query(query, new ControlMaster());		
			
			if (needMyJob == true) 
			{		
				//start of myjob=true
				//accept control : joballocation=true:-> alloted(1), registered(-1), 
				//joballocation=false:->allotted(-1), registered(1)
				
				query = " select ntransactionvalidationcode, nformcode, nregtypecode, nregsubtypecode, ncontrolcode, ntransactionstatus, "
						+ "nsitecode, nstatus from transactionvalidation where nformcode="	+ formCode
						+ " and nsitecode = " + userInfo.getNmastersitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode;
				final List<TransactionValidation> transValidationList = (List<TransactionValidation>)
					jdbcTemplate.query(query, new TransactionValidation());
	
				if (transValidationList.isEmpty()) 
				{					
					for(ControlMaster controlMaster:controlList) 
					{
						if (controlMaster.getScontrolname().equalsIgnoreCase("Accept")) {
						
							if(needJobAllocation == true) {
								//alloted(1)
								insertControlQueryList.add(	"(" + seqNo++ + ","
										+ formCode + ","
										+ nregTypeCode + ","+ nregSubTypeCode + ","
										+ controlMaster.getNcontrolcode() + ","
										+  Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus() 
										+ "," + userInfo.getNmastersitecode() + ",'"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"'," 
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");									
								
							}
							else {
								// registered(1)
								insertControlQueryList.add(	"(" + seqNo++ + ","
										+ formCode + ","
										+ nregTypeCode + ","+ nregSubTypeCode + ","
										+ controlMaster.getNcontrolcode() + ","
										+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
										+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");				
							}								
						}
						else if (controlMaster.getScontrolname().equalsIgnoreCase("Revert")) {
							insertControlQueryList.add(	"(" + seqNo++ + ","
									+ formCode + ","
									+ nregTypeCode + ","+ nregSubTypeCode + ","
									+ controlMaster.getNcontrolcode() + ","
									+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + "," + userInfo.getNmastersitecode() + ",'" 
									+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
						}
					}					
					
					queryMap.put("InsertQuery", insertControlQueryList);
					queryMap.put("UpdateQuery", updateQuery);
					queryMap.put("SeqNo", seqNo);
				}
				else {
					for(ControlMaster controlMaster:controlList) 
					{
						int insertTransCode = 0;
						int updateTransCode = 0;
				
						if (controlMaster.getScontrolname().equalsIgnoreCase("Accept")) {
						
							if(needJobAllocation == true) {
								//alloted(1), registered(-1)	
								insertTransCode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
								updateTransCode = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
							
								queryMap = constructQueryMap(nregTypeCode, nregSubTypeCode, formCode,
										controlMaster.getNcontrolcode(), insertTransCode, userInfo, transValidationList, queryMap);
								
								updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
											+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
											+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
											+ "	and ncontrolcode=" + controlMaster.getNcontrolcode()
											+ " and ntransactionstatus =" + updateTransCode+";";
							}
							else {
								//allotted(-1), registered(1)
								insertTransCode = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
								updateTransCode = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
								
								queryMap = constructQueryMap(nregTypeCode, nregSubTypeCode, formCode,
										controlMaster.getNcontrolcode(), insertTransCode, userInfo, transValidationList, queryMap);
								
								
								updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
											+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
											+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
											+ "	and ncontrolcode=" + controlMaster.getNcontrolcode()
											+ " and ntransactionstatus =" + updateTransCode+";";
							}								
						}
						else if (controlMaster.getScontrolname().equalsIgnoreCase("Revert")) {
							insertTransCode = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();							

							queryMap = constructQueryMap(nregTypeCode, nregSubTypeCode, formCode,
									controlMaster.getNcontrolcode(), insertTransCode, userInfo, transValidationList, queryMap);
							
						}			
					}
					
					//queryMap.put("InsertQuery", insertControlQueryList);
					queryMap.put("UpdateQuery", updateQuery);
					//queryMap.put("SeqNo", seqNo);
				}			
				
				//end of myjob=true				
			}
			else {
				////accept control :  alloted(-1), registered(-1)
					
				updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
								+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode+";";
								
				
				queryMap.put("UpdateQuery", updateQuery);
			}	
			
	//	}
		return queryMap;
	}	

	private Map<String, Object> manageResultEntryValidationStatus(final int nregTypeCode, 
			final int nregSubTypeCode, final UserInfo userInfo, final Boolean needMyJob,
			final Boolean needJobAllocation, final Boolean needTestInitiate, final Map<String, Object> queryMap) throws Exception
	{
			String query = " select ncontrolcode, scontrolname from controlmaster where nformcode="
						+ Enumeration.QualisForms.RESULTENTRY.getqualisforms()
						+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and scontrolname in ('ResultEnter', 'SetDefaultResult', 'CompleteResult','TestStart','AddParameterComments','EnforceResult','EBCFormulaCalculation','ExportResult','ImportResult')";
			final List<ControlMaster> controlList = jdbcTemplate.query(query, new ControlMaster());		
			
			final Map<String, Short> controlMap = controlList.stream().collect(Collectors.toMap(
						ControlMaster::getScontrolname,control -> control.getNcontrolcode()));	

			final short testStartCode = controlMap.get("TestStart");		

			
			final List<Short> allControlCodeList = controlList.stream().map(item->item.getNcontrolcode()).collect(Collectors.toList());
			
			///For controls other than Test Start
			

			final List<Short> controlCodeList = allControlCodeList.stream().filter(item->item != testStartCode).collect(Collectors.toList());;
			
			int seqNo = (int) queryMap.get("SeqNo");
			List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
			String updateQuery = (String) queryMap.get("UpdateQuery");
			
			if (needTestInitiate == true) {
				/**	Enter Result:testinitiated(1),recalc(1),accepted(-1),allotted(-1), registered(-1)
				 *	Set Default Result:testinitiated(1),recalc(1),accepted(-1),allotted(-1), registered(-1)
				 *	Complete Result:testinitiated(1),recalc(1), accepted(-1),allotted(-1), registered(-1)
				 * */									
				
				int testInitiateStatus = 0;
				
				final List<String> testStartDeleteList = new ArrayList<String>();
				if(needMyJob == true) {
					testInitiateStatus = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
					testStartDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
					testStartDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
				}
				else {
					if(needJobAllocation == true)
					{
						testInitiateStatus = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
						testStartDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
						testStartDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));
					}
					else {
						testInitiateStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
						testStartDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
						testStartDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));
					}
				}

				
				final List<Short> testStartCodeList = Arrays.asList(testStartCode);
				List<Integer> statusList = Arrays.asList(testInitiateStatus);
				
				if (testStartDeleteList.size()> 0) {
					updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
									+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
									+ "	and ncontrolcode =" +testStartCode
									+ " and ntransactionstatus in (" +String.join(",", testStartDeleteList) + ");";
				}
				
				queryMap.put("InsertQuery", insertControlQueryList);
				queryMap.put("UpdateQuery", updateQuery);
				queryMap.put("SeqNo", seqNo);
				
				final Map<String, Object> returnMap =  generateValidationStatusQuery(statusList, nregTypeCode,	nregSubTypeCode, testStartCodeList, userInfo,
						Enumeration.QualisForms.RESULTENTRY.getqualisforms(), queryMap);
				
				seqNo = (int) returnMap.get("SeqNo");
				insertControlQueryList = (List) returnMap.get("InsertQuery");
				updateQuery = (String) returnMap.get("UpdateQuery");
				
			
//				///For controls other than Test Start

				final String deleteCodeList = controlCodeList.stream().map(i->i.toString()).collect(Collectors.joining(", "));
				
				final List<String> deleteStatusList = Arrays.asList(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()),
													Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()),
													Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
				
				//String insertQuery = "";
				updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
									+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
									+ "	and ncontrolcode in (" +deleteCodeList + ")"
									+ " and ntransactionstatus in (" +String.join(",", deleteStatusList) + ");";
										
				statusList = Arrays.asList(Enumeration.TransactionStatus.INITIATED.gettransactionstatus(),
																Enumeration.TransactionStatus.RECALC.gettransactionstatus());
				
				queryMap.put("InsertQuery", insertControlQueryList);
				queryMap.put("UpdateQuery", updateQuery);
				queryMap.put("SeqNo", seqNo);
				
				return generateValidationStatusQuery(statusList, nregTypeCode,	nregSubTypeCode, controlCodeList, userInfo,
						Enumeration.QualisForms.RESULTENTRY.getqualisforms(), queryMap);
				
				//Code take from here		
				
				/**
				 * End of test inititate=true
				 */
			}
			else {
				/**
				 * start- test initiate=false
				 * Enter Result, Set Default Result, Complete Result
				 * myjob-true ? [accepted(1),allotted(-1), registered(-1), testinitiated(-1)] 
				 * : joballocation-true ? [allotted(1),  registered(-1), testinitiated(-1),accepted(-1)] : [registered(1), allotted(-1),testinitiated(-1),accepted(-1)]
				 */			

				updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
								+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
								+ "	and ncontrolcode = " +  testStartCode +";";
							
				
				if(needMyJob == true) {
					//Start- My Job enabled
					//accepted(1),allotted(-1), registered(-1), testinitiated(-1)
					
					final List<Integer> statusList = Arrays.asList(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus(),
													Enumeration.TransactionStatus.RECALC.gettransactionstatus());
					
					
					final String deleteCodeList = controlCodeList.stream().map(i->i.toString()).collect(Collectors.joining(", "));
					
					final List<String> deleteStatusList = Arrays.asList(Integer.toString(Enumeration.TransactionStatus.INITIATED.gettransactionstatus()),
							Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()),
									Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));

					
					updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
										+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
										+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
										+ "	and ncontrolcode in (" +deleteCodeList + ")"
										+ " and ntransactionstatus in (" +String.join(",", deleteStatusList) + ");";
					
					
					queryMap.put("InsertQuery", insertControlQueryList);
					queryMap.put("UpdateQuery", updateQuery);
					queryMap.put("SeqNo", seqNo);
					
					return generateValidationStatusQuery(statusList, nregTypeCode,	nregSubTypeCode, controlCodeList, userInfo,
							Enumeration.QualisForms.RESULTENTRY.getqualisforms(), queryMap);
					
	
					//End- My Job enabled
				}
				else {
					//Start- My Job disabled
					if(needJobAllocation == true) {
						//Start- Job Allocation enabled
						//allotted(1),  registered(-1), testinitiated(-1),accepted(-1)
						
						final List<Integer> statusList = Arrays.asList(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus(),
								Enumeration.TransactionStatus.RECALC.gettransactionstatus());
						
				
						final String deleteCodeList = controlCodeList.stream().map(i->i.toString()).collect(Collectors.joining(", "));
						
						final List<String> deleteStatusList = Arrays.asList(Integer.toString(Enumeration.TransactionStatus.INITIATED.gettransactionstatus()),
								Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()),
										Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
						
						
						updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
											+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
											+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
											+ "	and ncontrolcode in (" +deleteCodeList + ")"
											+ " and ntransactionstatus in (" +String.join(",", deleteStatusList) + ");";
						
						queryMap.put("InsertQuery", insertControlQueryList);
						queryMap.put("UpdateQuery", updateQuery);
						queryMap.put("SeqNo", seqNo);
						
						return generateValidationStatusQuery(statusList, nregTypeCode,	nregSubTypeCode, controlCodeList, userInfo,
								Enumeration.QualisForms.RESULTENTRY.getqualisforms(), queryMap);
						
						//End- Job Allocation enabled
					}
					else {
						//Start- Job Allocation disabled
						//registered(1), allotted(-1),testinitiated(-1),accepted(-1)
						
						final List<Integer> statusList = Arrays.asList(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus(),
								Enumeration.TransactionStatus.RECALC.gettransactionstatus());
						
						controlCodeList.add(testStartCode);

						final String deleteCodeList = controlCodeList.stream().map(i->i.toString()).collect(Collectors.joining(", "));
						
						final List<String> deleteStatusList = Arrays.asList(Integer.toString(Enumeration.TransactionStatus.INITIATED.gettransactionstatus()),
								Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()),
										Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
						
						
						updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
											+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + Enumeration.QualisForms.RESULTENTRY.getqualisforms()
											+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
											+ "	and ncontrolcode in (" +deleteCodeList + ")"
											+ " and ntransactionstatus in (" +String.join(",", deleteStatusList) + ");";
						
						queryMap.put("InsertQuery", insertControlQueryList);
						queryMap.put("UpdateQuery", updateQuery);
						queryMap.put("SeqNo", seqNo);
						
						return generateValidationStatusQuery(statusList, nregTypeCode,	nregSubTypeCode, controlCodeList, userInfo,
								Enumeration.QualisForms.RESULTENTRY.getqualisforms(), queryMap);
						
						//End- Job Allocation disabled
					}
					//End- My Job disabled
				}
							
				//End- test initiate=false
			}
	}
	
	
	private Map<String, Object> manageBatchValidationStatus(final int nregTypeCode, 
			final int nregSubTypeCode, final UserInfo userInfo, final Boolean needMyJob,
			final Boolean needJobAllocation, final Boolean needBatch, Map<String, Object> queryMap) throws Exception
	{
			final int formCode = Enumeration.QualisForms.BATCHCREATION.getqualisforms();
			
			String query = " select ncontrolcode, scontrolname from controlmaster where nformcode="
						+ formCode 	+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<ControlMaster> controlList = jdbcTemplate.query(query, new ControlMaster());		
			
			final Map<String, Short> controlMap = controlList.stream().collect(Collectors.toMap(
						ControlMaster::getScontrolname,control -> control.getNcontrolcode()));	
			final short addBatch = controlMap.get("AddBatchCreation");
			final short addSample = controlMap.get("AddSamples");
			final short initiateBatch = controlMap.get("BatchInitiate");
			final short completeBatch = controlMap.get("BatchComplete");
			
			int seqNo = (int) queryMap.get("SeqNo");
			List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
			String updateQuery = (String) queryMap.get("UpdateQuery");
			
			if(needBatch) {
							
				int addBatchStatus = 0;
				int addSampleStatus = 0;
				int initiateBatchStatus = 0;
				int completeBatchStatus = 0;
				
				final List<String> addBatchDeleteList = new ArrayList<String>();
				final List<String> addSampleDeleteList = new ArrayList<String>();
				final List<String> initiateBatchDeleteList = new ArrayList<String>();
				
				final Map<Short, Integer> saveMap = new HashMap<Short, Integer>();					
				
				if(needMyJob == true) {
					addBatchStatus = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
					addSampleStatus = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
					initiateBatchStatus = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
									
					addBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
					addBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
					
					addSampleDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
					addSampleDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
					
					
					initiateBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
					initiateBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
				}
				else {
					if(needJobAllocation == true)
					{
						addBatchStatus = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
						addSampleStatus = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
						initiateBatchStatus = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();						
						
						addBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
						addBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));						
						
						addSampleDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
						addSampleDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));
						
						initiateBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
						initiateBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));
					}
					else {
						addBatchStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
						addSampleStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
						initiateBatchStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
						
						addBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
						addBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));
						
						addSampleDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
						addSampleDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));
						
						initiateBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
						initiateBatchDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));
					}
				}
				completeBatchStatus = Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();
				
				saveMap.put(addBatch, addBatchStatus);
				saveMap.put(addSample, addSampleStatus);
				saveMap.put(initiateBatch, initiateBatchStatus);
				saveMap.put(completeBatch, completeBatchStatus);
				
				query = " select ntransactionvalidationcode, nformcode, nregtypecode, nregsubtypecode, ncontrolcode, ntransactionstatus, "
						+ "nsitecode, nstatus from transactionvalidation where nformcode="	+ formCode
						+ " and nsitecode = " + userInfo.getNmastersitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode;				
				final List<TransactionValidation> transValidationList = (List<TransactionValidation>)
												jdbcTemplate.query(query, new TransactionValidation());	
				
				if (transValidationList.isEmpty()) 
				{
					for(Map.Entry<Short, Integer> insertMap: saveMap.entrySet()) {
						insertControlQueryList.add("(" + seqNo++ + ","
								+ formCode + ","
								+ nregTypeCode + ","+ nregSubTypeCode + ","
								+ insertMap.getKey() + "," + insertMap.getValue() + "," 
								+ userInfo.getNmastersitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");
					}
					
					queryMap.put("InsertQuery", insertControlQueryList);
					queryMap.put("UpdateQuery", updateQuery);
					queryMap.put("SeqNo", seqNo);
				}
				else {
					for(Map.Entry<Short, Integer> insertMap: saveMap.entrySet()) {
						
						queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode,formCode, 
								insertMap.getKey(), insertMap.getValue(), userInfo, transValidationList, queryMap);
					}
					seqNo = (int) queryMap.get("SeqNo");
					insertControlQueryList = (List) queryMap.get("InsertQuery");
					updateQuery = (String) queryMap.get("UpdateQuery");

					if (addBatchDeleteList.size()> 0) {
						updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
										+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
										+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
										+ "	and ncontrolcode =" +addBatch
										+ " and ntransactionstatus in (" +String.join(",", addBatchDeleteList) + ");";
					}
					
					if(addSampleDeleteList.size() > 0) {
						updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
						+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
						+ "	and ncontrolcode =" +addSample
						+ " and ntransactionstatus in (" +String.join(",", addSampleDeleteList) + ");";
					}
					
					if (initiateBatchDeleteList.size()> 0) {
						updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
										+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
										+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
										+ "	and ncontrolcode =" +initiateBatch
										+ " and ntransactionstatus in (" +String.join(",", initiateBatchDeleteList) + ");";
					}
					
					queryMap.put("InsertQuery", insertControlQueryList);
					queryMap.put("UpdateQuery", updateQuery);
					queryMap.put("SeqNo", seqNo);
				}				
			}
			else {
				updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
								+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode+";";
				
				queryMap.put("UpdateQuery", updateQuery);
			}
			return queryMap;
	}
	
	private Map<String, Object> manageWorkListValidationStatus(final int nregTypeCode, 
			final int nregSubTypeCode, final UserInfo userInfo, final Boolean needMyJob,
			final Boolean needJobAllocation, final Boolean needWorklist, Map<String, Object> queryMap) throws Exception
	{
			final int formCode = Enumeration.QualisForms.WORKLIST.getqualisforms();
			
			String query = " select ncontrolcode, scontrolname from controlmaster where nformcode="
						+ formCode 	+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<ControlMaster> controlList = jdbcTemplate.query(query, new ControlMaster());		
			
			final Map<String, Short> controlMap = controlList.stream().collect(Collectors.toMap(
						ControlMaster::getScontrolname,control -> control.getNcontrolcode()));	
			final short addWorkListCode = controlMap.get("AddWorklist");
			final short addSampleCode = controlMap.get("AddSamples");
			final short generateWorkListCode = controlMap.get("GenerateWorklist");
			
			int seqNo = (int) queryMap.get("SeqNo");
			List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
			String updateQuery = (String) queryMap.get("UpdateQuery");
						
			if(needWorklist) {
							
				int addTestStatus = 0;
				int addSampleStatus = 0;
				int generateWorklistStatus = 0;
				
				final List<String> addWorkListDeleteList = new ArrayList<String>();
				//final List<String> addSampleDeleteList = new ArrayList<String>();
				final List<String> generateWorkListDeleteList = new ArrayList<String>();
				
				final Map<Short, Integer> saveMap = new HashMap<Short, Integer>();					
				
				if(needMyJob == true) {
					addTestStatus = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
					addSampleStatus = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
					generateWorklistStatus = Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();
					
					addWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
					addWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
					
					generateWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
					generateWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
				}
				else {
					if(needJobAllocation == true)
					{
						addTestStatus = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
						addSampleStatus = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();
						generateWorklistStatus = Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus();						
						
						addWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
						addWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));
						
						generateWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
						generateWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));
					}
					else {
						addTestStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
						addSampleStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
						generateWorklistStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
						
						addWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
						addWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));
						
						generateWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus()));
						generateWorkListDeleteList.add(Integer.toString(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()));
					}
				}
				
				saveMap.put(addWorkListCode, addTestStatus);
				saveMap.put(addSampleCode, addSampleStatus);
				saveMap.put(generateWorkListCode, generateWorklistStatus);
				
				query = " select ntransactionvalidationcode, nformcode, nregtypecode, nregsubtypecode, ncontrolcode, ntransactionstatus, "
							+ "nsitecode, nstatus from transactionvalidation where nformcode="	+ formCode
							+ " and nsitecode = " + userInfo.getNmastersitecode()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode;			
				final List<TransactionValidation> transValidationList = (List<TransactionValidation>)
												jdbcTemplate.query(query, new TransactionValidation());	
				
				if (transValidationList.isEmpty()) 
				{
					for(Map.Entry<Short, Integer> insertMap: saveMap.entrySet()) {
						insertControlQueryList.add("(" + seqNo++ + ","
								+ formCode + ","
								+ nregTypeCode + ","+ nregSubTypeCode + ","
								+ insertMap.getKey() + "," + insertMap.getValue() + "," 
								+ userInfo.getNmastersitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");
					}
					
					queryMap.put("InsertQuery", insertControlQueryList);
			
					queryMap.put("SeqNo", seqNo);
								
				}
				else {
					for(Map.Entry<Short, Integer> insertMap: saveMap.entrySet()) {
						
						queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode,formCode, 
								insertMap.getKey(), insertMap.getValue(), userInfo, transValidationList, queryMap);
					}
					
					seqNo = (int) queryMap.get("SeqNo");
					insertControlQueryList = (List) queryMap.get("InsertQuery");
					updateQuery = (String) queryMap.get("UpdateQuery");
					
					if (addWorkListDeleteList.size()> 0) {
						updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
										+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
										+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
										+ "	and ncontrolcode =" +addWorkListCode
										+ " and ntransactionstatus in (" +String.join(",", addWorkListDeleteList) + ");";
					}
					
					if (generateWorkListDeleteList.size()> 0) {
						updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
										+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
										+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
										+ "	and ncontrolcode =" +generateWorkListCode
										+ " and ntransactionstatus in (" +String.join(",", generateWorkListDeleteList) + ");";
					}
					
					queryMap.put("InsertQuery", insertControlQueryList);
					queryMap.put("UpdateQuery", updateQuery);
					queryMap.put("SeqNo", seqNo);
								
				}				
				
			}
			else {
				updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
								+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
								+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode+";";
				
				queryMap.put("UpdateQuery", updateQuery);
			}
			return queryMap;
	}
	
	
	@SuppressWarnings({ "unchecked" })
	private Map<String, Object> generateValidationStatusQuery(final List<Integer> statusList,
			final int nregTypeCode, final int nregSubTypeCode, final List<Short> controlCodeList,
			final UserInfo userInfo, final Integer nformCode, Map<String, Object> queryMap) throws Exception {
		
		int seqNo = (int)queryMap.get("SeqNo");		

		final List<String> insertControlQueryList = (List<String>)queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");
	
		final String query = " select ntransactionvalidationcode, nformcode, nregtypecode, nregsubtypecode, ncontrolcode, "
				+ "ntransactionstatus, nsitecode, nstatus from transactionvalidation where nformcode="+ nformCode
				+ " and nsitecode = " + userInfo.getNmastersitecode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode;		
		final List<TransactionValidation> transValidationList = (List<TransactionValidation>) jdbcTemplate
					.query(query, new TransactionValidation());			
				
		if (transValidationList.isEmpty()) {
			
			for(Short controlCode: controlCodeList)
			{		
				for(Integer transCode: statusList)
				{		
					insertControlQueryList.add("(" + seqNo++ + ","
							+ nformCode + ","
							+ nregTypeCode + ","+ nregSubTypeCode + ","
							+ controlCode + "," + transCode + "," 
							+ userInfo.getNmastersitecode() + ",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," 
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");
				}
			}			

			queryMap.put("InsertQuery", insertControlQueryList);
			queryMap.put("UpdateQuery", updateQuery);
			queryMap.put("SeqNo", seqNo);
		}
		else {
			
			for(Short controlCode: controlCodeList)
			{
				for(Integer transCode: statusList)	
				{
					queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, nformCode, 
							controlCode, transCode, userInfo, transValidationList, queryMap);
				
				}
			}
		}				

		return queryMap;
		
	}

	
		
	
	private  Map<String, Object> constructQueryMap(final int nregTypeCode, 
			final int nregSubTypeCode, final int formCode, final short controlCode, final int transCode,
			final UserInfo userInfo, final List<TransactionValidation> transValidationList, 
			final Map<String, Object> queryMap) throws Exception{
		
		int seqNo = (int) queryMap.get("SeqNo");
		List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
		String updateQuery = (String) queryMap.get("UpdateQuery");	
		
		final TransactionValidation matchingObject = transValidationList.stream().
			    filter(t -> t.getNtransactionstatus() == transCode && t.getNcontrolcode() == controlCode)
			    .findAny().orElse(null);
		
		if(matchingObject == null) {
			insertControlQueryList.add(	"(" + seqNo++ + ","
					+ formCode + ","
					+ nregTypeCode + ","+ nregSubTypeCode + ","
					+ controlCode + ","
					+  transCode + "," + userInfo.getNmastersitecode() + ",'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'," 
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
		}
		else if( matchingObject.getNstatus() ==	Enumeration.TransactionStatus.DELETED.gettransactionstatus())
		{
			//Activate
			updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + formCode
							+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
							+ "	and ncontrolcode=" + controlCode
							+ " and ntransactionstatus =" + transCode+";";
		}
		
		queryMap.put("InsertQuery", insertControlQueryList);
		queryMap.put("UpdateQuery", updateQuery);
		queryMap.put("SeqNo", seqNo);
		
		return queryMap;
	}
	
	//SYED commented on 10-APR-2025 
	/*
	private Map<String, Object> manageAttachmentCommentStatus(final int nregTypeCode, 
			final int nregSubTypeCode, final UserInfo userInfo,final Boolean needSubSample,
			final Boolean needMyJob, final Boolean needJobAllocation, final Boolean needTestInitiate,
			 Map<String, Object> queryMap) throws Exception
	{	
			
		
		final List<String> formCodeList = new ArrayList<String>();
		formCodeList.add(Integer.toString(Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms()));
		//Enable below lines to have validation for Attachment and comments for mentioned screens
//		formCodeList.add(Integer.toString(Enumeration.QualisForms.RESULTENTRY.getqualisforms()));
//		formCodeList.add(Integer.toString(Enumeration.QualisForms.APPROVAL.getqualisforms()));
//		
//		if(needJobAllocation) {
//			formCodeList.add(Integer.toString(Enumeration.QualisForms.JobAllocation.getqualisforms()));
//		}
//		if(needMyJob){
//			formCodeList.add(Integer.toString(Enumeration.QualisForms.MyJob.getqualisforms()));
//			formCodeList.add(Integer.toString(Enumeration.QualisForms.TESTWISEMYJOB.getqualisforms()));
//		}
		
		final List<Integer> resultEntryList = new ArrayList<Integer>();	
		final List<Integer> resultEntryDeleteList = new ArrayList<Integer>();	
		final List<Integer> myJobList = new ArrayList<Integer>();	
		final List<Integer> myJobDeleteList = new ArrayList<Integer>();
		
		if(needTestInitiate) {
			resultEntryList.add(Enumeration.TransactionStatus.INITIATED.gettransactionstatus());
		}
		else {
			resultEntryDeleteList.add(Enumeration.TransactionStatus.INITIATED.gettransactionstatus());
			if(needMyJob) {
				resultEntryList.add(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus());
			}
			else if(needJobAllocation) {
				resultEntryDeleteList.add(Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus());
				resultEntryList.add(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus());
				myJobList.add(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus());
			}
			else{
				resultEntryDeleteList.add(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus());
				myJobDeleteList.add(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus());
				resultEntryList.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
				myJobList.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
			}
		}
			
		
		final HashMap<Integer, List<Integer>> formStatusMap = new HashMap<Integer, List<Integer>>();
		formStatusMap.put(Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms(), Arrays.asList(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus(),
																									 //Enumeration.TransactionStatus.REGISTERED.gettransactionstatus(), 
																									 Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()));
		
//		formStatusMap.put(Enumeration.QualisForms.RESULTENTRY.getqualisforms(), resultEntryList);
//		formStatusMap.put(Enumeration.QualisForms.APPROVAL.getqualisforms(), new ArrayList<>());
//		formStatusMap.put(Enumeration.QualisForms.JobAllocation.getqualisforms(), Arrays.asList(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()));
//		formStatusMap.put(Enumeration.QualisForms.MyJob.getqualisforms(), myJobList);
//		formStatusMap.put(Enumeration.QualisForms.TESTWISEMYJOB.getqualisforms(), myJobList);
		
		final List<String> controlNameList =new ArrayList<String>(Arrays.asList("'AddSampleAttachment'", "'AddTestAttachment'",
															 "'EditSampleAttachment'", "'EditTestAttachment'",
															 "'DeleteSampleAttachment'", "'DeleteTestAttachment'",
															 "'AddSampleComment'", "'AddTestComment'",
															 "'EditSampleComment'",  "'EditTestComment'",
															 "'DeleteSampleComment'",  "'DeleteTestComment'"));
		final List<String> subSampleControlNameList = Arrays.asList( "'AddSubSampleAttachment'","'EditSubSampleAttachment'",  "'DeleteSubSampleAttachment'",
					"'AddSubSampleComment'", "'EditSubSampleComment'","'DeleteSubSampleComment'");

		//if(needSubSample) {
			controlNameList.addAll(subSampleControlNameList);
		//}
		
		String query = " select nformcode, ncontrolcode, ntransactionstatus, nstatus, nsitecode from transactionvalidation "
						+ " where nformcode in (" 
						+ String.join(",", formCodeList) + ")" 	+ " and nregtypecode=" + nregTypeCode 
						+ " and nregsubtypecode="+ nregSubTypeCode + " and nsitecode="+userInfo.getNmastersitecode();
			
		final List<TransactionValidation> transList = (List<TransactionValidation>) jdbcTemplate
					.query(query, new TransactionValidation());	
		
		final Map<Short, List<TransactionValidation>> transValidationMap = transList.stream()
				.collect(Collectors.groupingBy(TransactionValidation::getNformcode, Collectors.toList()));
						
		query = " select nformcode, ncontrolcode, scontrolname, jsondata from controlmaster where scontrolname in"
							+ "(" +  String.join(",", controlNameList)+ ") and "
							+ " nformcode in (" + String.join(",", formCodeList) + ")"
							+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<ControlMaster> controlList = jdbcTemplate.query(query, new ControlMaster());		
		
		final Map<Short, List<ControlMaster>> controlMap = controlList.stream()
				.collect(Collectors.groupingBy(ControlMaster::getNformcode, Collectors.toList()));
		
		for(Map.Entry<Short, List<ControlMaster>> entrySet: controlMap.entrySet()) 
		{
			
			int seqNo = (int) queryMap.get("SeqNo");
			List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
			String updateQuery = (String) queryMap.get("UpdateQuery");	
			
			
			final List<TransactionValidation> transValidationList = transValidationMap.get(entrySet.getKey());
			final List<ControlMaster> formControlList = entrySet.getValue();
			final List<Integer> statusList = new ArrayList<Integer>();		
			if(transValidationList == null) {
				
				for(ControlMaster controlMaster: formControlList) {
					for(Integer transCode: statusList) {					
							
							insertControlQueryList.add(	"(" + seqNo++ + ","
									+ (int)entrySet.getKey() + ","
									+ nregTypeCode + ","+ nregSubTypeCode + ","
									+ controlMaster.getNcontrolcode() + ","
									+  transCode + "," + userInfo.getNmastersitecode() + "," 
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
					}
				}	
				
				queryMap.put("InsertQuery", insertControlQueryList);
				queryMap.put("UpdateQuery", updateQuery);
				queryMap.put("SeqNo", seqNo);
							
			}			
			else {
				
				for(ControlMaster controlMaster: formControlList) {
					if(!needSubSample && subSampleControlNameList.contains("'"+controlMaster.getScontrolname()+"'")){
						updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
										+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + (int)entrySet.getKey()
										+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
										+ "	and ncontrolcode=" + controlMaster.getNcontrolcode() + ";";
										//+ " and ntransactionstatus =" + transCode+";";
						queryMap.put("UpdateQuery", updateQuery);
					}
					if((!needMyJob && (int)entrySet.getKey() == Enumeration.QualisForms.MYJOB.getqualisforms())
								|| (!needJobAllocation && (int)entrySet.getKey() == Enumeration.QualisForms.JOBALLOCATION.getqualisforms()))
					{
							updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
											+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + (int)entrySet.getKey()
											+ " and nregtypecode=" + nregTypeCode + " and nregsubtypecode="+ nregSubTypeCode
											+ "	and ncontrolcode=" + controlMaster.getNcontrolcode() + ";";						
							queryMap.put("UpdateQuery", updateQuery);
					}
					else
					{
						for(Integer transCode: formStatusMap.get((int)entrySet.getKey())) {
							queryMap =  constructQueryMap(nregTypeCode, nregSubTypeCode, (int)entrySet.getKey(), 
									controlMaster.getNcontrolcode(), transCode,	userInfo, transValidationList, queryMap);
						}
					}
				}
				
				
			}
		
		}
		
		return queryMap;		
	}
	*/
	
	
	
	@Override
	public ResponseEntity<Object> getSiteWiseAllSeqNoFormats(UserInfo userInfo) throws Exception {

		final String getQuery = "select jsondata->>'ssampleformat' as ssampleformat from regsubtypeconfigversion " + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode()
				+ " and (jsondata->>'nneedsitewisearno')::jsonb ='true'group by jsondata->>'ssampleformat' ;";
		return new ResponseEntity<>(jdbcTemplate.query(getQuery, new RegSubTypeConfigVersion()), HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getSiteWiseAllSeqNoFormatsRelease(UserInfo userInfo) throws Exception {

		final String getQuery = "select jsondata->>'sreleaseformat' as sreleaseformat from regsubtypeconfigversionrelease " + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode()
				+ " and (jsondata->>'nneedsitewisearnorelease')::jsonb ='true'group by jsondata->>'sreleaseformat' ;";
		return new ResponseEntity<>(jdbcTemplate.query(getQuery, new RegSubTypeConfigVersionRelease()), HttpStatus.OK);
	}
	
	public Map<String,Object> dynamicStatusValidation(int seqno,int nregtypecode,int nregsubypecode,int nformcode,int ncontrocode,UserInfo userInfo,final List<TransactionValidation> transValidationList,final Map<String, Object> queryMap) throws Exception {
		List<String> insertControlQueryList1 =new ArrayList<>();
		Map<String,Object> map=new HashMap<>();

		
		
		final String query = " select ntranscode from dynamicstatusvalidation where nformcode="
				+ nformcode
				+ " and nsitecode = " + userInfo.getNmastersitecode()
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ncontrolcode="+ncontrocode;
		
				final List<DynamicStatusValidation> validationList = jdbcTemplate.query(query, new DynamicStatusValidation());
				
		if(transValidationList.isEmpty()){
			int seqNo=seqno;
			for (DynamicStatusValidation ntransactiostatus:validationList) {
				insertControlQueryList1.add("(" + seqNo++ + ","
					+ nformcode + ","
					+ nregtypecode + ","+ nregsubypecode + ","
					+ ncontrocode + ","
					+ ntransactiostatus.getNtranscode()+ "," + userInfo.getNmastersitecode() + ",'" 
					+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
		
		
			}
			map.put("insertList", insertControlQueryList1);	
			map.put("SeqNo", seqNo);

		}
		else{
			int seqNo = (int) queryMap.get("SeqNo");
			List<String> insertControlQueryList = (List) queryMap.get("InsertQuery");
			String updateQuery = (String) queryMap.get("UpdateQuery");	

			for (DynamicStatusValidation ntransactiostatus:validationList) {
				final TransactionValidation matchingObject = transValidationList.stream().
					    filter(t -> t.getNtransactionstatus() == ntransactiostatus.getNtranscode() && t.getNcontrolcode() == ncontrocode)
					    .findAny().orElse(null);
				if(matchingObject==null) {
					 insertControlQueryList.add("(" + seqNo++ + ","
								+ nformcode + ","
								+ nregtypecode + ","+ nregsubypecode + ","
								+ ncontrocode + ","
								+ ntransactiostatus.getNtranscode()+ "," + userInfo.getNmastersitecode() + ",'" 
								+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")");	
					
				}
				
					else if( matchingObject.getNstatus() ==	Enumeration.TransactionStatus.DELETED.gettransactionstatus())
					{
						//Activate
						updateQuery += ";update transactionvalidation set nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "' where nformcode=" + nformcode
										+ " and nregtypecode=" + nregtypecode + " and nregsubtypecode="+ nregsubypecode
										+ "	and ncontrolcode=" + ncontrocode
										+ " and ntransactionstatus =" + ntransactiostatus.getNtranscode()+";";
					
				}
			
			
			}
			
			map.put("InsertQuery", insertControlQueryList);	
			map.put("UpdateQuery", updateQuery);
			map.put("SeqNo", seqNo);

		}
	
		
	
				
				return map;
	}
	
	@Override
	public ResponseEntity<Object> updateReleaseArNoVersion(final RegSubTypeConfigVersionRelease releaseversion, final UserInfo userInfo)
			throws Exception {
		
		//ALPD-897
		final RegistrationSubType validRegSubtype = getRegSubTypeById(releaseversion.getNregsubtypecode(),	userInfo);
		Map<String, Object> returnObj = new HashMap<>();

		if (validRegSubtype == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REGSUBTYPEDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		else {
			JSONObject jsonAuditOld = new JSONObject();
			JSONObject jsonAuditNew = new JSONObject();
			JSONObject actionType = new JSONObject();
			Map<String, Object> objmap = new LinkedHashMap<String, Object>();
			
			final String strJsonArray11 = (String) jdbcUtilityFunction.queryForObject("select json_agg(jsonuidata||jsonb_build_object('nregsubtypeversionreleasecode' ," + releaseversion.getNregsubtypeversionreleasecode()+")::jsonb) from regsubtypeconfigversionrelease rstc where "
					+ " nsitecode = " + userInfo.getNmastersitecode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nregsubtypeversionreleasecode="+ releaseversion.getNregsubtypeversionreleasecode(), String.class, jdbcTemplate);
					
			JSONArray oldJsonArray=new JSONArray(strJsonArray11);
			RegSubTypeConfigVersion oldVersion = getVersionById(releaseversion.getNregsubtypeversionreleasecode(), userInfo);
			if (oldVersion != null) {
				
			short periodCode = releaseversion.getNperiodcode();

			List<Map<String, Object>> seqNoReleaseGenCodeList = validateReleaseFormat(
					(String) releaseversion.getJsondata().get("sreleaseformat"),userInfo);
			int seqNoReleseGenCode;
			String insertNewSeqNoRelease="";
			String updateReleseSeqNo="";
			int resetDuartion;
			Map<String, Object> relesejsonData = releaseversion.getJsondata();

			if (seqNoReleaseGenCodeList.isEmpty()) {
				final String getRelesenoSequenceNo = "select nsequenceno+1 from seqnoregtemplateversion where stablename ='seqnoreleasenogenerator'";
				seqNoReleseGenCode = (int) jdbcUtilityFunction.queryForObject(getRelesenoSequenceNo, Integer.class, jdbcTemplate);
				insertNewSeqNoRelease = "insert into seqnoreleasenogenerator(nseqnoreleasenogencode,nsequenceno,nstatus, dmodifieddate) " + " values ("
						+ seqNoReleseGenCode + ",0," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ ",'" +dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ ");";
	
				updateReleseSeqNo += " update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename = 'seqnoreleasenogenerator';";
				releaseversion.setNseqnoreleasenogencode(seqNoReleseGenCode);
			} else {
				if((int)seqNoReleaseGenCodeList.get(0).get("nregsubtypeversionreleasecode")!= releaseversion.getNregsubtypeversionreleasecode()) {
					seqNoReleseGenCode = (int) seqNoReleaseGenCodeList.get(0).get("nseqnoreleasenogencode");
					resetDuartion = (int) seqNoReleaseGenCodeList.get(0).get("nresetduration");

					
				}else {
					seqNoReleseGenCode = (int) seqNoReleaseGenCodeList.get(0).get("nseqnoreleasenogencode");
					resetDuartion = 1;

					
				}
				releaseversion.setNseqnoreleasenogencode(seqNoReleseGenCode);

				relesejsonData.put("nresetduration", resetDuartion);
				releaseversion.setJsondata(relesejsonData);

				
			}

			final JSONObject jsonData = new JSONObject(releaseversion.getJsondata());
			final JSONObject jsonuiData = new JSONObject(releaseversion.getJsonuidata());

	
				final String updateQuery = "update regsubtypeconfigversionrelease set jsondata = '" + stringUtilityFunction.replaceQuote(jsonData.toString())   + "'"
						+ ",jsonuidata='"+ stringUtilityFunction.replaceQuote(jsonuiData.toString())+"' ,"
					    + " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"' ,"
						+ " nperiodcode=" + periodCode + ", "
						+" nseqnoreleasenogencode = " + seqNoReleseGenCode
						+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nregsubtypeversionreleasecode = " + releaseversion.getNregsubtypeversionreleasecode() + ";";
	
				jdbcTemplate.execute(updateQuery+updateReleseSeqNo+ insertNewSeqNoRelease);
	
				
				List<RegSubTypeConfigVersion> formatList = getSeqnoFormatsByApprovalConfig(releaseversion.getNapprovalconfigcode(),
						userInfo);
				
				returnObj.put("versions", formatList);
				if (!formatList.isEmpty())
					returnObj.put("selectedVersion",
							formatList.stream()
									.filter(x -> x.getNregsubtypeversioncode() == releaseversion.getNregsubtypeversionreleasecode())
									.collect(Collectors.toList()).get(0));
				
		
				objmap.put("nregtypecode", -1);
				objmap.put("nregsubtypecode", -1);
				objmap.put("ndesigntemplatemappingcode", -1);
				actionType.put("regsubtypeconfigversionrelease", "IDS_EDITREGSUBTYPEVERSIONRELEASE");
				jsonAuditNew.put("regsubtypeconfigversionrelease", new JSONArray().put((jsonuiData)));
				jsonAuditOld.put("regsubtypeconfigversionrelease", oldJsonArray);
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, objmap, false, userInfo);
			}
			
		}
		return new ResponseEntity<>(returnObj, HttpStatus.OK);

	}

	
}

