package com.agaramtech.qualis.configuration.service.sitehospitalmapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.SiteType;
import com.agaramtech.qualis.configuration.model.SiteHospitalMapping;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.Hospital;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SiteHospitalMappingDAOImpl implements SiteHospitalMappingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SiteHospitalMappingDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getSiteHospitalMapping(UserInfo userInfo) {
		// TODO Auto-generated method stub

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		//final String strQuery = "SELECT nsitecode as nmappingsitecode, ssitename , ssitecode, nstatus FROM site where nsitecode>0  ;";
		
		final String strQuery="SELECT c.nsitecode as nmappingsitecode, c.ssitename , c.ssitecode,b.nsitetypecode, COALESCE(b.ssitetypename, '-') AS ssitetypename "
				+ "FROM site c LEFT JOIN  siteconfig a ON c.nsitecode = a.nsitecode And a.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "LEFT JOIN sitetype b ON a.nsitetypecode = b.nsitetypecode And b.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" And b.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "WHERE c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND c.nsitecode>0  ORDER BY c.nsitecode ASC ; ";
		
		final List<SiteHospitalMapping> lstSiteHospitalMapping = (List<SiteHospitalMapping>) jdbcTemplate
				.query(strQuery, new SiteHospitalMapping());
		if (lstSiteHospitalMapping != null) {
			outputMap.put("siteMasterRecord", lstSiteHospitalMapping);
			
			outputMap.put( "lsthospitalQuery", getHospitalSite( lstSiteHospitalMapping.get(lstSiteHospitalMapping.size()-1).getNmappingsitecode(),userInfo));
			outputMap.put("selectedsiteMasterRecord", lstSiteHospitalMapping.get(lstSiteHospitalMapping.size()-1));

		} else {
			outputMap.put("siteMasterRecord", null);
			outputMap.put("selectedsiteMasterRecord", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	/**
	 * This method is used to retrieve list of available Hospital(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}"nmappingcode":1}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	private List<Hospital> getHospitalSite(int nmappingsitecode,UserInfo userInfo) {
		final String hospitalQuery="select  shm.nmappingsitecode,shm.nsitehospitalmappingcode,h.nhospitalcode,h.shospitalname FROM  hospital h,sitehospitalmapping shm "
				+ "where h.nhospitalcode=shm.nhospitalcode and shm.nmappingsitecode="+nmappingsitecode+" "
				+ "and h.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and shm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and h.nsitecode="+userInfo.getNmastersitecode()+" and shm.nsitecode="+userInfo.getNmastersitecode()+"  ORDER BY h.nhospitalcode ASC";
		
		return (List<Hospital>) jdbcTemplate
				.query(hospitalQuery, new Hospital());
	}
	
	/**
	 * This method is used to retrieve list of available Hospital(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getSiteHospitalMappingRecord(int nmappingsitecode,UserInfo userInfo) {
		// TODO Auto-generated method stub

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		//final String strQuery = "SELECT nsitecode as nmappingsitecode, ssitename , ssitecode, nstatus FROM site where nsitecode="+nmappingsitecode+"  ;";
		
		final String strQuery="SELECT c.nsitecode as nmappingsitecode, c.ssitename , c.ssitecode,b.nsitetypecode, COALESCE(b.ssitetypename, '-') AS ssitetypename "
				+ "FROM site c LEFT JOIN  siteconfig a ON c.nsitecode = a.nsitecode And a.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "LEFT JOIN sitetype b ON a.nsitetypecode = b.nsitetypecode And b.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" And b.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "WHERE c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND c.nsitecode="+nmappingsitecode+" AND c.nsitecode>0  ORDER BY c.nsitecode ASC";
				
		final List<SiteHospitalMapping> lstSiteHospitalMapping = (List<SiteHospitalMapping>) jdbcTemplate
				.query(strQuery, new SiteHospitalMapping());
		

				
		if (lstSiteHospitalMapping != null) {
			outputMap.put("selectedsiteMasterRecord", lstSiteHospitalMapping.get(lstSiteHospitalMapping.size()-1));
			outputMap.put( "lsthospitalQuery", getHospitalSite( lstSiteHospitalMapping.get(lstSiteHospitalMapping.size()-1).getNmappingsitecode(),userInfo));
			outputMap.put("flag", "");
		} else {
			outputMap.put( "lsthospitalQuery", null);
			outputMap.put("selectedsiteMasterRecord", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> editSiteAndBioBank(Map<String, Object> inputMap,UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		final SiteHospitalMapping objSiteHospitalMapping = objmapper.convertValue(inputMap.get("siteRecord"), SiteHospitalMapping.class);

//		final String strQuery = "SELECT nsitecode as nmappingsitecode, ssitename , ssitecode, nstatus"
//				+ " FROM site ;";
		
		final String strQuery="SELECT c.nsitecode as nmappingsitecode, c.ssitename , c.ssitecode,b.nsitetypecode, COALESCE(b.ssitetypename, '-') AS ssitetypename "
				+ "FROM site c LEFT JOIN  siteconfig a ON c.nsitecode = a.nsitecode And a.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "LEFT JOIN sitetype b ON a.nsitetypecode = b.nsitetypecode And b.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" And b.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "WHERE c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND c.nsitecode="+objSiteHospitalMapping.getNmappingsitecode()+" AND c.nsitecode>0  ORDER BY c.nsitecode ASC ;";
		
		
		final SiteHospitalMapping lstSiteHospitalMapping =  jdbcTemplate
				.queryForObject(strQuery, new SiteHospitalMapping());
		
		final String strQueryBioBank = " SELECT nsitetypecode, ssitetypename, nhierarchicalorderno, sdescription "
				+ "FROM sitetype where nsitetypecode>0 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode()+";";
		final List<SiteType> lstBioBankType = (List<SiteType>) jdbcTemplate
				.query(strQueryBioBank, new SiteType());
		outputMap.put("BioBanktype", lstBioBankType);
		outputMap.put("siteConfig", lstSiteHospitalMapping);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1},"SiteHospitalMapping" = {
	 *                 "nmappingsitecode": 1, "ssitecode": 'AS', "sdescription": '',
	 *                 "ssitetypename": 1, "nsitetypecode": 'Cancer', "ssitename":
	 *                 'LIMS' }}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> createSiteAndBioBank(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final ObjectMapper objmapper = new ObjectMapper();
		String alert="";
		final SiteHospitalMapping objSiteHospitalMapping = objmapper.convertValue(inputMap.get("SiteHospitalMapping"), SiteHospitalMapping.class);

		final String validationSsiteCode="select s.nsitecode as nmappingsitecode ,sc.nsitetypecode from site s,siteconfig sc where s.nsitecode=sc.nsitecode and s.ssitecode='"+objSiteHospitalMapping.getSsitecode()+"'  and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode<>"+objSiteHospitalMapping.getNmappingsitecode()+" ;";
		
		final List<SiteHospitalMapping> lstSiteHospitalMapping = (List<SiteHospitalMapping>) jdbcTemplate
				.query(validationSsiteCode, new SiteHospitalMapping());
		
		final String AuditQuery="select s.nsitecode as nmappingsitecode , s.ssitename,s.ssitecode,sc.nsitetypecode from site s,siteconfig sc where s.nsitecode=sc.nsitecode and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode ="+objSiteHospitalMapping.getNmappingsitecode()+" ;";
		
		final SiteHospitalMapping objSiteHospitalMappingAudit=(SiteHospitalMapping) jdbcUtilityTemplateFunction.queryForObject(AuditQuery, SiteHospitalMapping.class, jdbcTemplate);
		if(lstSiteHospitalMapping.isEmpty()) {
			
			String updateQuery = "UPDATE site SET  ssitecode='"+objSiteHospitalMapping.getSsitecode()+"' "
					+ "WHERE nsitecode="+objSiteHospitalMapping.getNmappingsitecode()+";";
			updateQuery=updateQuery+"UPDATE siteconfig SET nsitetypecode="+objSiteHospitalMapping.getNsitetypecode()+" "
					+ "WHERE nsitecode="+objSiteHospitalMapping.getNmappingsitecode()+";";

			jdbcTemplate.execute(updateQuery);
			

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			listAfterUpdate.add(objSiteHospitalMapping);
			listBeforeUpdate.add(objSiteHospitalMappingAudit);
			multilingualIDList.add("IDS_EDITSITEDETAILS");
			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
					userInfo);
			
		}else {
			 alert = commonFunction.getMultilingualMessage("IDS_SITECODE", userInfo.getSlanguagefilename());

			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	
		
		
		return getSiteHospitalMappingRecord(objSiteHospitalMapping.getNmappingsitecode(),userInfo);
	}

	/**
	 * This method is used to retrieve list of available Hospital(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getHospitalMaster(Map<String, Object> inputMap,UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final String strQuery = "SELECT nhospitalcode,shospitalname FROM  hospital "
				+ "WHERE nhospitalcode NOT IN (SELECT nhospitalcode FROM sitehospitalmapping WHERE nmappingsitecode = "+inputMap.get("nmappingsitecode")+" "
				+ "AND nsitecode = "+userInfo.getNmastersitecode()+" AND nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") "
				+ "AND nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  AND nsitecode = "+userInfo.getNmastersitecode()+" AND nhospitalcode > 0"
				+ ";";

		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Hospital()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1} "nhospitalcode":
	 *                 1,2,3,4,5, "nmappingsitecode": 'LIMS',
	 *                 "nsitehospitalmappingcode": 1, "shospitalname": 'JK,KL,LO,OI,IU,,}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> createHospitalMaster(Map<String, Object> inputMap,UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		
		final String hospitalQuery="select  shm.nmappingsitecode,shm.nsitehospitalmappingcode,h.nhospitalcode,h.shospitalname FROM  hospital h,sitehospitalmapping shm "
				+ "where h.nhospitalcode=shm.nhospitalcode and shm.nmappingsitecode="+inputMap.get("nmappingsitecode")+" and h.nhospitalcode in ("+inputMap.get("nhospitalcode")+") "
				+ "and h.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and shm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and h.nsitecode="+userInfo.getNmastersitecode()+" and shm.nsitecode="+userInfo.getNmastersitecode()+" ;";
		
		final List<SiteHospitalMapping> lstSiteHospitalMapping = (List<SiteHospitalMapping>) jdbcTemplate
				.query(hospitalQuery, new SiteHospitalMapping());
		
		if (!lstSiteHospitalMapping.isEmpty()) {
			
			final String alert =lstSiteHospitalMapping.stream().map(SiteHospitalMapping::getShospitalname).collect(Collectors.joining(", "));
			
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
			
		} else {
			
			final String sequencequery = "select nsequenceno from seqnocredentialmanagement where stablename ='sitehospitalmapping'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			//nsequenceno++;
			
			final String queryString = "INSERT INTO sitehospitalmapping (nsitehospitalmappingcode,nmappingsitecode,nhospitalcode,sdescription,"
					+ "dmodifieddate,nsitecode,nstatus)"
					+ "SELECT "+nsequenceno+" + RANK() OVER (ORDER BY nhospitalcode) AS nsitehospitalmappingcode,"+inputMap.get("nmappingsitecode")+" AS nmappingsitecode,"
					+ "nhospitalcode,'' AS sdescription, '"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ "FROM hospital WHERE nhospitalcode IN ("+inputMap.get("nhospitalcode")+");";
			
					jdbcTemplate.execute(queryString);

			final String updatequery = "update seqnocredentialmanagement set nsequenceno =(select max(nsitehospitalmappingcode) from sitehospitalmapping) where stablename ='sitehospitalmapping' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
					jdbcTemplate.execute(updatequery);
					
			
			final String hospitalStrg="select STRING_AGG(h.shospitalname, ', ') AS shospitalname from hospital h "
					+ "where h.nhospitalcode in ("+inputMap.get("nhospitalcode")+") and h.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+";";
			final String sHospitalName = jdbcTemplate.queryForObject(hospitalStrg, String.class);

			final String AuditQuery="select s.nsitecode as nmappingsitecode , s.ssitename,s.ssitecode,sc.nsitetypecode from site s,siteconfig sc where s.nsitecode=sc.nsitecode and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode ="+inputMap.get("nmappingsitecode")+" ;";
			
			final SiteHospitalMapping objSiteHospitalMappingAudit=(SiteHospitalMapping) jdbcUtilityTemplateFunction.queryForObject(AuditQuery, SiteHospitalMapping.class, jdbcTemplate);
			objSiteHospitalMappingAudit.setShospitalname(sHospitalName);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedTestList = new ArrayList<>();
			savedTestList.add(objSiteHospitalMappingAudit);
			multilingualIDList.add("IDS_ADDHOSPITAL");
			
			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);
			
			return getSiteHospitalMappingRecord((int)inputMap.get("nmappingsitecode"),userInfo);
			
		}
	
	}

	/**
	 * This method is used to retrieve list of available SiteHospitalMapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1} "nhospitalcode":
	 *                 1"nmappingsitecode": 'LIMS',
	 *                 "nsitehospitalmappingcode": 1, "shospitalname": 'JK'}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> deleteHospitalMaster(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final String deleteValidation="SELECT * FROM sitehospitalmapping "
						+ "WHERE nsitehospitalmappingcode="+inputMap.get("nsitehospitalmappingcode")+" AND nmappingsitecode="+inputMap.get("nmappingsitecode")+" AND nhospitalcode="+inputMap.get("nhospitalcode")+" AND nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  ;";
		
		final List<SiteHospitalMapping> lstSiteHospitalMapping = (List<SiteHospitalMapping>) jdbcTemplate
				.query(deleteValidation, new SiteHospitalMapping());
		
		if (lstSiteHospitalMapping.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
		
		final String deleteQuery="UPDATE sitehospitalmapping SET nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+" "
				+ "WHERE nsitehospitalmappingcode="+inputMap.get("nsitehospitalmappingcode")+" AND nmappingsitecode="+inputMap.get("nmappingsitecode")+" AND nhospitalcode="+inputMap.get("nhospitalcode")+"  ;";
		
		jdbcTemplate.execute(deleteQuery);
		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedHospital = new ArrayList<>();
		final String hospitalStrg="select shospitalname from hospital h "
				+ "where h.nhospitalcode in ("+inputMap.get("nhospitalcode")+") and h.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+";";
		final String sHospitalName = jdbcTemplate.queryForObject(hospitalStrg, String.class);

		final String AuditQuery="select s.nsitecode as nmappingsitecode , s.ssitename,s.ssitecode,sc.nsitetypecode from site s,siteconfig sc where s.nsitecode=sc.nsitecode and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode ="+inputMap.get("nmappingsitecode")+" ;";
		
		final SiteHospitalMapping objSiteHospitalMappingAudit=(SiteHospitalMapping) jdbcUtilityTemplateFunction.queryForObject(AuditQuery, SiteHospitalMapping.class, jdbcTemplate);
		objSiteHospitalMappingAudit.setShospitalname(sHospitalName);
		deletedHospital.add(objSiteHospitalMappingAudit);
		multilingualIDList.add("IDS_DELETEHOSPITAL");
		auditUtilityFunction.fnInsertAuditAction(deletedHospital, 1, null, multilingualIDList, userInfo);
		
		
		return getSiteHospitalMappingRecord((int)inputMap.get("nmappingsitecode"),userInfo);
		}
	}

	

}
