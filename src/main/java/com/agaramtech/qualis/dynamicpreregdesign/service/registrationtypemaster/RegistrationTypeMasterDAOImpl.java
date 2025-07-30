package com.agaramtech.qualis.dynamicpreregdesign.service.registrationtypemaster;

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

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.contactmaster.service.suppliercategory.SupplierCategoryDAOImpl;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class RegistrationTypeMasterDAOImpl implements RegistrationTypeMasterDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SupplierCategoryDAOImpl.class);
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;


	@Override
	public ResponseEntity<Object> getSampleType(UserInfo userInfo) throws Exception {
		Map<String, Object> returnObj = new HashMap<>();
		final String getSampleType = "select nsampletypecode,coalesce(jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "', jsondata->'sampletypename'->>'en-US') as ssampletypename "
				+ " from sampletype where napprovalconfigview = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and nsitecode = " + userInfo.getNmastersitecode()
				+ " order by nsampletypecode;";
		List<SampleType> sampleTypeList = jdbcTemplate.query(getSampleType, new SampleType());
		if (sampleTypeList.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			returnObj.put("SampleTypes", sampleTypeList);
			return new ResponseEntity<>(returnObj, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getRegistrationTypeBySampleType(final int sampleTypeCode, final UserInfo userInfo)
			throws Exception {
		final String getRegistrationBySubType = "select r.*,coalesce(r.jsondata->'sregtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',r.jsondata->'sregtypename'->>'en-US') as sregtypename, " 
				+"coalesce(s.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',s.jsondata->'sampletypename'->>'en-US') as ssampletypename " 
				+ " from sampletype s, registrationtype r"
				+ " where r.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nsitecode = " + userInfo.getNmastersitecode()
				+ " and r.nsitecode = " + userInfo.getNmastersitecode()
				+ " and s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and r.nsampletypecode = " + sampleTypeCode
				+ " and r.nsampletypecode=s.nsampletypecode and r.nregtypecode >0 ";
		List<RegistrationType> registrationTypeList = jdbcTemplate.query(
				getRegistrationBySubType, new RegistrationType());
		return new ResponseEntity<>(registrationTypeList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegistrationType(final UserInfo userInfo) throws Exception {
		final String getRegistrationType = "select r.*,coalesce(r.jsondata->'sregtypename'->>'"+ userInfo.getSlanguagetypecode() + "',r.jsondata->'sregtypename'->>'en-US') as sregtypename,"
				+ " coalesce(r.jsondata->>'sdescription','') as sdescription, "
				+ " coalesce(s.jsondata->'sampletypename'->>'"+ userInfo.getSlanguagetypecode() + "',s.jsondata->'sampletypename'->>'en-US') as ssampletypename"
				+ " from sampletype s, registrationtype r"
				+ " where r.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nsitecode = " + userInfo.getNmastersitecode()
				+ " and r.nsitecode = " + userInfo.getNmastersitecode()
				+ " and s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and r.nsampletypecode=s.nsampletypecode and r.nregtypecode >0 ";
		List<RegistrationType> registrationTypeList = jdbcTemplate.query(getRegistrationType,
				new RegistrationType());
		return new ResponseEntity<>(registrationTypeList, HttpStatus.OK);
	}

	private List<Map<String,Object>> getRegistrationTypeByName(Map<String,Object> regTypeName,UserInfo userInfo) throws Exception {
		final String conditionStr = userInfo.getActivelanguagelist().stream().map(lang -> String.valueOf("lower(jsondata->'sregtypename'->>'"+lang+"')"))
                .collect(Collectors.joining(","));
		String lstKeyStr =  userInfo.getActivelanguagelist().stream().map(lang -> String.valueOf("lower('"+stringUtilityFunction.replaceQuote(regTypeName.get(lang).toString())+"')"
				+" in ("+conditionStr+")")).collect(Collectors.joining(" or "));
		final String getRegistrationType = "select nregtypecode from registrationtype where ("+lstKeyStr+") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode();
		return (List<Map<String,Object>>) jdbcTemplate.queryForList(getRegistrationType);

	}

	@Override
	public RegistrationType getRegistrationTypeById(int regTypeCode, UserInfo userInfo) throws Exception {
		final String getRegistrationType = "select r.*,coalesce(r.jsondata->'sregtypename'->>'"+ userInfo.getSlanguagetypecode() + "',r.jsondata->'sregtypename'->>'en-US') as sregtypename,"
				+ " coalesce(r.jsondata->>'sdescription','') as sdescription, "
				+ " coalesce(s.jsondata->'sampletypename'->>'"+ userInfo.getSlanguagetypecode() + "',s.jsondata->'sampletypename'->>'en-US') as ssampletypename  from sampletype s, registrationtype r "
				+ " where r.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and r.nsampletypecode=s.nsampletypecode and s.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nsitecode = " + userInfo.getNmastersitecode()
				+ " and r.nsitecode = " + userInfo.getNmastersitecode()
				+ " and nregtypecode =" + regTypeCode ;
		return (RegistrationType) jdbcUtilityFunction.queryForObject(getRegistrationType, RegistrationType.class, jdbcTemplate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createRegistrationType(RegistrationType registrationType, UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		JSONObject jsonAuditObject = new JSONObject();
		JSONObject actionType = new JSONObject();
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();

		final RegistrationType registrationTypeAudit = objmapper.convertValue(inputMap.get("registrationtypeaudit"),
				RegistrationType.class);
		
		final Map<String,Object> sregtypename = (Map<String, Object>) registrationType.getJsondata().get("sregtypename");
//		final RegistrationType regType = getRegistrationTypeByName((String)sregtypename.get(userInfo.getSlanguagetypecode()), userInfo);
		final List<Map<String,Object>> regType = getRegistrationTypeByName(sregtypename, userInfo);
		if (!(regType.size()>0)) {
			
			final String getSequenceNo = "select nsequenceno+1 from seqnoregtemplateversion where stablename ='registrationtype'";
			final int seqNo = (int) jdbcUtilityFunction.queryForObject(getSequenceNo, Integer.class, jdbcTemplate);
			final String insertQuery = "INSERT INTO registrationtype(nregtypecode,nsampletypecode,napprovalsubtypecode,jsondata,jsonuidata,nsorter,dmodifieddate,nsitecode,nstatus) values "
					+ "(" + seqNo + "," + registrationType.getNsampletypecode() + ","
					+ Enumeration.ApprovalSubType.TESTRESULTAPPROVAL.getnsubtype() + ","
					+ "'"+   stringUtilityFunction.replaceQuote(mapper.writeValueAsString(registrationType.getJsondata()).toString()) +"', "
					+ "'"+stringUtilityFunction.replaceQuote(mapper.writeValueAsString(registrationTypeAudit.getJsonuidata()).toString())
					+ "',0,'" + dateUtilityFunction.getCurrentDateTime(userInfo)+"', " + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			final String updateSeqNo = "update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename = 'registrationtype';";
			jdbcTemplate.execute(insertQuery + updateSeqNo);

			objmap.put("nregtypecode", -1);
			objmap.put("nregsubtypecode", -1);
			objmap.put("ndesigntemplatemappingcode", -1);
			registrationTypeAudit.setNregtypecode((short) seqNo);
			actionType.put("Registrationtype", "IDS_ADDREGISTRATIONTYPE");
			
			List languagelst =userInfo.getActivelanguagelist();
			List<String> regtypenamelst=(List<String>) languagelst.stream().map(x->"'sregtypename "+x+"',r.jsondata->'sregtypename'->>'"+x+"'").collect(Collectors.toList());
			String regtypenameQry = String.join(",",regtypenamelst); //Joiner.on(",").join(regtypenamelst);
						
			JSONArray newJsonArray=new JSONArray(jdbcTemplate.queryForObject(
					"select json_agg(r.jsondata||jsonb_build_object('nregtypecode',"+seqNo+","
					+ "'ssampletypename', st.jsondata->'sampletypename'->>'"+userInfo.getSlanguagetypecode()
					+ "',"
					+ "'sregtypename',r.jsondata->'sregtypename'->>'"+ userInfo.getSlanguagetypecode()+"',"+regtypenameQry+ ")::jsonb) "
					+ " from registrationtype r,sampletype st"
					+ " where r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and st.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and st.nsitecode = " + userInfo.getNmastersitecode()
					+ " and r.nsitecode = " + userInfo.getNmastersitecode()
					+ " and r.nsampletypecode=st.nsampletypecode and "
					+ " nregtypecode="+ seqNo, String.class));
			jsonAuditObject.put("Registrationtype", newJsonArray );
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, objmap, false, userInfo);
			
			return getRegistrationType(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateRegistrationType(RegistrationType registrationType, UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		
		final ObjectMapper objmapper = new ObjectMapper();
		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		JSONObject actionType = new JSONObject();
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();

		final RegistrationType registrationTypeAudit = objmapper.convertValue(inputMap.get("registrationtypeaudit"),
				RegistrationType.class);
		

		List languagelst =userInfo.getActivelanguagelist();
		List<String> regtypenamelst=(List<String>) languagelst.stream().map(x->"'sregtypename "+x+"',r.jsondata->'sregtypename'->>'"+x+"'").collect(Collectors.toList());
		String regtypenameQry = String.join(",",regtypenamelst); //Joiner.on(",").join(regtypenamelst);
		
		final RegistrationType validRegtype = getRegistrationTypeById(registrationType.getNregtypecode(), userInfo);

		if (validRegtype != null) 
		{
			
			
			JSONArray oldJsonUiDataArray=new JSONArray(jdbcTemplate.queryForObject(
					"select json_agg(r.jsondata||jsonb_build_object('nregtypecode',"+registrationType.getNregtypecode()+","
					+ "'ssampletypename', st.jsondata->'sampletypename'->>'"+userInfo.getSlanguagetypecode()
					+ "','sregtypename',r.jsondata->'sregtypename'->>'"+ userInfo.getSlanguagetypecode()
					+ "',"+regtypenameQry+ ")::jsonb) "
					+ " from registrationtype r,sampletype st"
					+ " where r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and st.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and st.nsitecode = " + userInfo.getNmastersitecode()
					+ " and r.nsitecode = " + userInfo.getNmastersitecode()
					+ " and r.nsampletypecode=st.nsampletypecode and "
					+ " nregtypecode="+ registrationType.getNregtypecode(), String.class));
			
			//ALPD-2041
			final String queryString = " select * from registrationsubtype where nregtypecode="
					                   +   registrationType.getNregtypecode() 
					                   + " and nsitecode = " + userInfo.getNmastersitecode()
					                   + " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final List<RegistrationSubType> regSubTypeList = jdbcTemplate.query(queryString, new RegistrationSubType());
			boolean flag = true;
			if (!regSubTypeList.isEmpty()) {
				
				if(validRegtype.getNsampletypecode() != registrationType.getNsampletypecode())
				{
					flag = false;
				}
			}
			if(flag)
			{
			final Map<String,Object> sregtypename = (Map<String, Object>) registrationType.getJsondata().get("sregtypename");

			final List<Map<String,Object>> regType = getRegistrationTypeByName(sregtypename, userInfo);

			if ((regType.size()<1) || (regType.size()==1 && regType.get(0).get("nregtypecode").equals((int) registrationType.getNregtypecode()))) {
				final String updateQuery = "update registrationtype set jsondata = jsondata||'"
						+  stringUtilityFunction.replaceQuote( mapper.writeValueAsString(registrationType.getJsondata()).toString()) + "'::jsonb,"
						+ " nsampletypecode = "+ registrationType.getNsampletypecode() 
						+ " where nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nregtypecode = "+ registrationType.getNregtypecode() + ";";
				 
				jdbcTemplate.execute(updateQuery);
				
				actionType.put("Registrationtype", "IDS_EDITREGISTRATIONTYPE");
				
				JSONArray newJsonUiDataArray=new JSONArray(jdbcTemplate.queryForObject(
						"select json_agg(r.jsondata||jsonb_build_object('nregtypecode',"+registrationType.getNregtypecode()+","
						+ "'ssampletypename', st.jsondata->'sampletypename'->>'"+userInfo.getSlanguagetypecode()
						+ "','sregtypename',r.jsondata->'sregtypename'->>'"+ userInfo.getSlanguagetypecode()
						+ "',"+regtypenameQry+ ")::jsonb) "
						+ " from registrationtype r,sampletype st"
						+ " where r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and st.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and st.nsitecode = " + userInfo.getNmastersitecode()
						+ " and r.nsitecode = " + userInfo.getNmastersitecode()
						+ " and r.nsampletypecode=st.nsampletypecode and "
						+ " nregtypecode="+ registrationType.getNregtypecode(), String.class));
				
				jsonAuditObject.put("Registrationtype", newJsonUiDataArray);
				jsonAuditOld.put("Registrationtype",oldJsonUiDataArray);
				
				objmap.put("nregtypecode", -1);
				objmap.put("nregsubtypecode", -1);
				objmap.put("ndesigntemplatemappingcode", -1);
				
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditObject, actionType, objmap, false, userInfo);
				
				return getRegistrationType(userInfo);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			}
			else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REGSUBTYPECREATEDSAMPLETYPECANNOTBECHANGED",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}

	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> deleteRegistrationType(RegistrationType objRegistrationType, UserInfo userInfo)
			throws Exception {
		final RegistrationType registrationType = getRegistrationTypeById(
				objRegistrationType.getNregtypecode(), userInfo);

		if (registrationType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} 
		else {

			JSONObject jsonAuditOld = new JSONObject();
			JSONObject actionType = new JSONObject();
			Map<String, Object> objmap = new LinkedHashMap<String, Object>();


			List languagelst =userInfo.getActivelanguagelist();
			List<String> regtypenamelst=(List<String>) languagelst.stream().map(x->"'sregtypename "+x+"',r.jsondata->'sregtypename'->>'"+x+"'").collect(Collectors.toList());
			String regtypenameQry = String.join(",",regtypenamelst); 
			//COMMENTED BY SYED 10-APR-2025
			//Joiner.on(",").join(regtypenamelst);
			
			JSONArray oldJsonUiDataArray=new JSONArray(jdbcTemplate.queryForObject(
					"select json_agg(r.jsondata||jsonb_build_object('nregtypecode',"+registrationType.getNregtypecode()+","
					+ "'ssampletypename', st.jsondata->'sampletypename'->>'"+userInfo.getSlanguagetypecode()
					+ "',"
					+ "'sregtypename',r.jsondata->'sregtypename'->>'"+ userInfo.getSlanguagetypecode()+"',"+ regtypenameQry +")::jsonb) "
					+ " from registrationtype r,sampletype st"
					+ " where r.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and st.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and st.nsitecode = " + userInfo.getNmastersitecode()
					+ " and r.nsitecode = " + userInfo.getNmastersitecode()
					+ " and r.nsampletypecode=st.nsampletypecode and "
					+ " nregtypecode="+ registrationType.getNregtypecode(), String.class));
			final String query = "select 'IDS_REGISTRATIONSUBTYPE' as Msg from registrationsubtype where nregtypecode= "
					+ objRegistrationType.getNregtypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + userInfo.getNmastersitecode();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {

				final String updateQueryString = "update registrationtype set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nregtypecode=" + objRegistrationType.getNregtypecode();

				jdbcTemplate.execute(updateQueryString);
				objRegistrationType.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				actionType.put("Registrationtype", "IDS_DELETEREGISTRATIONTYPE");
				jsonAuditOld.put("Registrationtype",oldJsonUiDataArray);
				objmap.put("nregtypecode", -1);
				objmap.put("nregsubtypecode", -1);
				objmap.put("ndesigntemplatemappingcode", -1);

				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, null, actionType, objmap, false, userInfo);

				return getRegistrationType(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
