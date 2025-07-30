package com.agaramtech.qualis.biobank.service.bioparentsamplereceiving;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.biobank.model.BioParentSampleCollection;
import com.agaramtech.qualis.biobank.model.BioParentSampleReceiving;
import com.agaramtech.qualis.configuration.model.SiteHospitalMapping;
import com.agaramtech.qualis.configuration.service.sitehospitalmapping.SiteHospitalMappingDAOImpl;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.instrumentmanagement.model.Instrument;
import com.agaramtech.qualis.product.model.ProductCategory;
import com.agaramtech.qualis.project.model.BioProject;
import com.agaramtech.qualis.project.model.Disease;
import com.agaramtech.qualis.project.model.Hospital;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BioParentSampleReceivingDAOImpl implements BioParentSampleReceivingDAO{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SiteHospitalMappingDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	
	@Override
	public ResponseEntity<Object> getBioParentSampleReceiving(UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select bspr.nbioparentsamplecode, bspr.sparentsamplecode, bspr.ssubjectid, bspr.scasetype, bspr.ndiseasecode, d.sdiseasename, bspr.nbioprojectcode, bp.sprojecttitle, bp.sprojectcode, "
				+ "u.sfirstname||' '|| u.slastname as sprojectincharge, bspr.ncohortno, bspr.ncollectionsitecode, cs.ssitename as scollectionsitename, "
				+ "bspr.ncollectedhospitalcode, ch.shospitalname, bspr.nsamplestoragelocationcode, ssl.ssamplestoragelocationname, "
				+ "bspr.darrivaldate, bspr.ntzarrivaldate, bspr.noffsetarrivaldate, tz1.stimezoneid AS stzarrivaldate, "
				+ "COALESCE(TO_CHAR(bspr.darrivaldate,'" + userInfo.getSsitedate() +"'), '') AS sarrivaldate, "
				+ "bspr.dtransactiondate, bspr.ntztransactiondate, bspr.noffsetarrivaldate, tz2.stimezoneid AS stztransactiondate, "
				+ "COALESCE(TO_CHAR(bspr.dtransactiondate, '" + userInfo.getSsitedate()+ "'), '') AS stransactiondate "
				+ "from bioparentsamplereceiving bspr, disease d, bioproject bp, users u, site cs, hospital ch, samplestoragelocation ssl, timezone tz1, "
				+ "timezone tz2 "
				+ "where bspr.ndiseasecode = d.ndiseasecode and bspr.nbioprojectcode = bp.nbioprojectcode and bp.nusercode = u.nusercode "
				+ "and bspr.ncollectionsitecode = cs.nsitecode and bspr.ncollectedhospitalcode = ch.nhospitalcode "
				+ "and ssl.nsamplestoragelocationcode = bspr.nsamplestoragelocationcode and bspr.ntzarrivaldate = tz1.ntimezonecode "
				+ "and bspr.ntztransactiondate = tz2.ntimezonecode "
				+ "and bspr.nsitecode = "+userInfo.getNtranssitecode()+ " "
				+ "and d.nsitecode = "+userInfo.getNmastersitecode()+ " "
				+ "and bp.nsitecode= "+userInfo.getNmastersitecode()+ " "
				+ "and u.nsitecode = "+userInfo.getNmastersitecode()+ " "
				+ "and ch.nsitecode= "+userInfo.getNmastersitecode()+ " "
//				+ "and ssl.nsitecode= "+userInfo.getNtranssitecode()+ " "
				+ "and bspr.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and d.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and u.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and cs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and ch.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and ssl.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and tz1.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and tz2.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ " order by bspr.nbioparentsamplecode";
		
		final List<BioParentSampleReceiving> lstBioParentSampleReceiving = (List<BioParentSampleReceiving>) jdbcTemplate
				.query(strQuery, new BioParentSampleReceiving());
		
		if (!lstBioParentSampleReceiving.isEmpty()) {
			outputMap.put("lstBioParentSampleReceiving", lstBioParentSampleReceiving);
			outputMap.put("selectedBioParentSampleReceiving", 
					lstBioParentSampleReceiving.get(lstBioParentSampleReceiving.size() - 1));
			outputMap.put( "lstBioParentSampleCollection", getBioParentSampleCollection( 
					lstBioParentSampleReceiving.get(lstBioParentSampleReceiving.size() - 1).getNbioparentsamplecode(),
					userInfo));

			
		} else {
			outputMap.put("lstBioParentSampleReceiving", null);
			outputMap.put("selectedBioParentSampleReceiving", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	
	public List<BioParentSampleCollection> getBioParentSampleCollection(int nbioparentsamplecode, UserInfo userInfo) throws Exception {
		
		final String strQuery = "select bpsc.nbioparentsamplecollectioncode, bpsc.nbioparentsamplecode, bpsr.sparentsamplecode, bpsr.ncohortno, u.sfirstname||' '|| u.slastname as sprojectincharge, bp.sprojecttitle, bp.sprojectcode, "
				+ "cs.ssitename as scollectionsitename, pc.sproductcatname, bpsc.nproductcatcode,  bpsc.nnoofsamples,  bpsc.ntemperature, "
				+ "bpsc.scollectorname, bpsc.stemporarystoragename, bpsc.ssendername, bpsc.sinformation, bpsc.nrecipientusercode, "
				+ "u1.sfirstname||' '|| u1.slastname as srecipientusername, bpsc.jsonuidata, bpsc.ntransactionstatus, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
				+ "bpsc.dsamplecollectiondate, bpsc.ntzsamplecollectiondate, bpsc.noffsetsamplecollectiondate, tz1.stimezoneid AS stzsamplecollectiondate, "
				+ "COALESCE(TO_CHAR(bpsc.dsamplecollectiondate,'" + userInfo.getSsitedate() +"'), '') AS ssamplecollectiondate, "
				+ "bpsc.dbiobankarrivaldate, bpsc.ntzbiobankarrivaldate, bpsc.noffsetbiobankarrivaldate, tz1.stimezoneid AS stzbiobankarrivaldate, "
				+ "COALESCE(TO_CHAR(bpsc.dbiobankarrivaldate,'" + userInfo.getSsitedate() +"'), '') AS sbiobankarrivaldate, "
				+ "bpsc.dtemporarystoragedate, bpsc.ntztemporarystoragedate, bpsc.noffsettemporarystoragedate, tz1.stimezoneid AS stztemporarystoragedate, "
				+ "COALESCE(TO_CHAR(bpsc.dtemporarystoragedate,'" + userInfo.getSsitedate() +"'), '') AS stemporarystoragedate, "
				+ "bpsc.dtransactiondate, bpsc.ntztransactiondate, bpsc.noffsettransactiondate, tz1.stimezoneid AS stztransactiondate, "
				+ "COALESCE(TO_CHAR(bpsc.dtransactiondate,'" + userInfo.getSsitedate() +"'), '') AS stransactiondate "
				+ "from bioparentsamplecollection bpsc, bioparentsamplereceiving bpsr, bioproject bp, users u, site cs, site rs, productcategory pc, "
				+ "users u1, transactionstatus ts, timezone tz1, timezone tz2, timezone tz3, timezone tz4 "
				+ "where bpsc.nbioparentsamplecode = bpsr.nbioparentsamplecode "
				+ "and bpsc.nbioparentsamplecode = "+nbioparentsamplecode+ " "
				+ "and bpsc.nsitecode = "+userInfo.getNtranssitecode()+ " "
				+ "and bpsr.nsitecode = "+userInfo.getNtranssitecode()+ " "
				+ "and bpsc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bpsr.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bpsr.nbioprojectcode = bp.nbioprojectcode "
				+ "and bp.nsitecode = "+userInfo.getNmastersitecode()+ " "
				+ "and bp.nusercode = u.nusercode "
				+ "and u.nsitecode = "+userInfo.getNmastersitecode()+ " "
				+ "and u.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bpsr.ncollectionsitecode = cs.nsitecode and cs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bpsc.nreceivingsitecode = rs.nsitecode and rs.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bpsc.nproductcatcode =  pc.nproductcatcode and pc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bpsc.nrecipientusercode = u1.nusercode and u1.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bpsc.ntransactionstatus = ts.ntranscode "
				+ "and bpsc.ntzsamplecollectiondate = tz1.ntimezonecode and tz1.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bpsc.ntzbiobankarrivaldate = tz2.ntimezonecode and tz2.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bpsc.ntztemporarystoragedate = tz3.ntimezonecode and tz3.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bpsc.ntztransactiondate = tz4.ntimezonecode and tz4.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by bpsc.nbioparentsamplecollectioncode desc";

		return (List<BioParentSampleCollection>) jdbcTemplate.query(strQuery, new BioParentSampleCollection());
	}
	
	@Override
	public ResponseEntity<Object> getActiveBioParentSampleReceiving(final int nbioparentsamplecode, final UserInfo userInfo) throws Exception {
		
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select bspr.nbioparentsamplecode, bspr.sparentsamplecode, bspr.ssubjectid, bspr.scasetype, bspr.ndiseasecode, d.sdiseasename, bspr.nbioprojectcode, bp.sprojecttitle, bp.sprojectcode, "
				+ "u.sfirstname||' '|| u.slastname as sprojectincharge, bspr.ncohortno, bspr.ncollectionsitecode, cs.ssitename as scollectionsitename, "
				+ "bspr.ncollectedhospitalcode, ch.shospitalname, bspr.nsamplestoragelocationcode, ssl.ssamplestoragelocationname, ssl.nstoragelocationtemp, "
				+ "bspr.darrivaldate, bspr.ntzarrivaldate, bspr.noffsetarrivaldate, tz1.stimezoneid AS stzarrivaldate, "
				+ "COALESCE(TO_CHAR(bspr.darrivaldate,'" + userInfo.getSsitedate() +"'), '') AS sarrivaldate, "
				+ "bspr.dtransactiondate, bspr.ntztransactiondate, bspr.noffsetarrivaldate, tz2.stimezoneid AS stztransactiondate, "
				+ "COALESCE(TO_CHAR(bspr.dtransactiondate, '" + userInfo.getSsitedate() + "'), '') AS stransactiondate "
				+ "from bioparentsamplereceiving bspr, disease d, bioproject bp, users u, site cs, hospital ch, samplestoragelocation ssl, timezone tz1, "
				+ "timezone tz2 "
				+ "where bspr.nbioparentsamplecode = "+ nbioparentsamplecode+ " "
				+ "and bspr.ndiseasecode = d.ndiseasecode and bspr.nbioprojectcode = bp.nbioprojectcode and bp.nusercode = u.nusercode "
				+ "and bspr.ncollectionsitecode = cs.nsitecode and bspr.ncollectedhospitalcode = ch.nhospitalcode "
				+ "and ssl.nsamplestoragelocationcode = bspr.nsamplestoragelocationcode and bspr.ntzarrivaldate = tz1.ntimezonecode "
				+ "and bspr.ntztransactiondate = tz2.ntimezonecode "
				+ "and bspr.nsitecode = "+userInfo.getNtranssitecode()+ " "
				+ "and d.nsitecode = "+userInfo.getNmastersitecode()+ " "
				+ "and bp.nsitecode= "+userInfo.getNmastersitecode()+ " "
				+ "and u.nsitecode = "+userInfo.getNmastersitecode()+ " "
				+ "and ch.nsitecode= "+userInfo.getNmastersitecode()+ " "
//				+ "and ssl.nsitecode= "+userInfo.getNtranssitecode()+ " "
				+ "and bspr.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and d.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and bp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and u.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and cs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and ch.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and ssl.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and tz1.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " "
				+ "and tz2.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by bspr.nbioparentsamplecode";
		
		
		final BioParentSampleReceiving objBioParentSampleReceiving = (BioParentSampleReceiving) jdbcUtilityTemplateFunction
				.queryForObject(strQuery, BioParentSampleReceiving.class, jdbcTemplate);
		
		if (objBioParentSampleReceiving != null) {
			outputMap.putAll(getUserAndProductCatDetails(userInfo));
			outputMap.put("selectedBioParentSampleReceiving", objBioParentSampleReceiving);
			outputMap.put( "lstBioParentSampleCollection", getBioParentSampleCollection( objBioParentSampleReceiving.getNbioparentsamplecode(),userInfo));

			
		} else {
			outputMap.put("sampleTypeList", null);
			outputMap.put("recipientsList", null);
			outputMap.put("selectedBioParentSampleReceiving", null);
			outputMap.put( "lstBioParentSampleCollection", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
		
	}
	
	public Map<String, Object> getUserAndProductCatDetails (UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strUserQuery = "SELECT CONCAT(sfirstname, ' ', slastname) AS srecipientusername,nusercode as nrecipientusercode "
				+ "FROM users where nusercode > 0 and  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " order by nusercode desc;";
		List<Map<String, Object>> lstRecipients = jdbcTemplate.queryForList(strUserQuery);

		final String strSampleType = "select nproductcatcode, sproductcatname, ndefaultstatus from productcategory where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nproductcatcode > 0 order by nproductcatcode desc";
		List<ProductCategory> lstSampleType = jdbcTemplate.query(strSampleType, new ProductCategory());
		outputMap.put("sampleTypeList", lstSampleType);
		outputMap.put("recipientsList", lstRecipients);
		return outputMap;
	}
	
	@Override
	public ResponseEntity<Object> getBioProjectforLoggedInSite(int ndiseasecode, UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select bp.nbioprojectcode, bp.sprojecttitle, bp.sprojectcode, u.sfirstname||' '||u.slastname as suserName "
				+ "from bioproject bp, users u, projectsitemapping psm "
				+ "where bp.ndiseasecode = "+ndiseasecode +" "
				+ "and psm.nbioprojectcode = bp.nbioprojectcode "
				+ "and bp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and psm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and bp.nusercode = u.nusercode "
				+ "and u.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and psm.nnodesitecode = "+userInfo.getNtranssitecode();
		
		final List<BioProject> lstBioProject = (List<BioProject>) jdbcTemplate
				.query(strQuery, new BioProject());
		
		if (!lstBioProject.isEmpty()) {
			outputMap.put("lstBioProject", lstBioProject);
			outputMap.put("selectedBioProject", lstBioProject.get(lstBioProject.size() - 1));
		
		} else {
			outputMap.put("lstBioProject", null);
			outputMap.put("selectedBioProject", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getDiseaseforLoggedInSite(UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select ndiseasecode,sdiseasename from disease where nstatus = "
		+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
		
		final List<Disease> lstDisease = (List<Disease>) jdbcTemplate
				.query(strQuery, new Disease());
		
		if (!lstDisease.isEmpty()) {
			outputMap.put("lstDisease", lstDisease);
			outputMap.put("selectedDisease", lstDisease.get(lstDisease.size() - 1));
			outputMap.put("lstBioProject", getBioProjectforLoggedInSite( lstDisease.get(lstDisease.size() - 1).getNdiseasecode(),userInfo));
		
		} else {
			outputMap.put("lstDisease", null);
			outputMap.put("selectedDisease", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getCollectionSiteBasedonProject(int nbioprojectcode, UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
	    String strQuery = "select shcd.schildsitecode "
				+ "from projectsitehierarchymapping pshm, sitehierarchyconfigdetails shcd "
				+ "where pshm.nsitehierarchyconfigcode = shcd.nsitehierarchyconfigcode "
				+ "and pshm.nbioprojectcode = "+nbioprojectcode+" "
				+ "and shcd.nnodesitecode = "+userInfo.getNtranssitecode()+" "
				+ "and pshm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and shcd.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
		
		final String schildsitecode = jdbcTemplate.queryForObject(strQuery, String.class);
		
		String ssitecode = ""+userInfo.getNtranssitecode();
		if (schildsitecode != null && !schildsitecode.isEmpty()) {
			ssitecode = ssitecode+","+schildsitecode;
		}
		strQuery = "select nsitecode, ssitename from site "
				+ "where nsitecode in ("+ssitecode+") "
				+ "and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
		
		
		final List<Site> lstSite = (List<Site>) jdbcTemplate
				.query(strQuery, new Site());
		
		if (!lstSite.isEmpty()) {
			outputMap.put("lstSite", lstSite);
			outputMap.put("selectedSite", lstSite.get(lstSite.size() - 1));
			outputMap.put("lstSiteHospital", getHospitalBasedonSite( lstSite.get(lstSite.size() - 1).getNsitecode(),userInfo));
		
		} else {
			outputMap.put("lstSite", null);
			outputMap.put("selectedSite", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getHospitalBasedonSite(int ncollectionsitecode, UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select h.nhospitalcode, h.shospitalname from hospital h, sitehospitalmapping shm "
				+ "where shm.nhospitalcode = h.nhospitalcode and shm.nmappingsitecode = "+ncollectionsitecode+ " "
				+ "and h.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and shm.nstatus = "
		+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
		
		final List<Hospital> lstHospital = (List<Hospital>) jdbcTemplate
				.query(strQuery, new Hospital());
		
		if (!lstHospital.isEmpty()) {
			outputMap.put("lstHospital", lstHospital);
			outputMap.put("selectedHospital", lstHospital.get(lstHospital.size() - 1));
		
		} else {
			outputMap.put("lstHospital", null);
			outputMap.put("selectedHospital", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getStorageStructureBasedonSite(UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select nsamplestoragelocationcode, ssamplestoragelocationname, nstoragelocationtemp "
				+ "from samplestoragelocation where nsitecode = "+userInfo.getNtranssitecode()+ " "
				+ "and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
		
		final List<Map<String,Object>> lstStorageStructure = jdbcTemplate
				.queryForList(strQuery);
		
		if (!lstStorageStructure.isEmpty()) {
			outputMap.put("lstStorageStructure", lstStorageStructure);
			outputMap.put("selectedStorageStructure", lstStorageStructure.get(lstStorageStructure.size() - 1));
		
		} else {
			outputMap.put("lstStorageStructure", null);
			outputMap.put("selectedStorageStructure", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> validateSubjectID(final String ssubjectid, final UserInfo userInfo) throws Exception {
		
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		outputMap.put("isValidSubjectID", true);
		outputMap.put("scasetype", "Control");
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
		
	}
	
	@Override
	public ResponseEntity<Object> updateBioParentSampleReceiving( final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		
		ObjectMapper objMapper = new ObjectMapper();
		final JavaTimeModule javaTimeModule = new JavaTimeModule();
		objMapper.registerModule(javaTimeModule);
		final List<String> lstDateField = new ArrayList<String>();
		List<String> lstDatecolumn = new ArrayList<String>();

		String strArrivalDate = (String) inputMap.get("darrivaldate");
		Instant darrivaldate = Instant.parse(strArrivalDate);
		
		BioParentSampleReceiving objBioParentSamplereceiving = new BioParentSampleReceiving();
		if (darrivaldate != null) {
			objBioParentSamplereceiving.setSarrivaldate(
					dateUtilityFunction.instantDateToString(darrivaldate).replace("T", " ").replace("Z", ""));
			lstDateField.add("sarrivaldate");
			lstDatecolumn.add("ntzarrivaldate");
		}
		
		final BioParentSampleReceiving convertedObject = objMapper.convertValue(
				dateUtilityFunction.convertInputDateToUTCByZone(objBioParentSamplereceiving, lstDateField, lstDatecolumn, true, userInfo),
				new TypeReference<BioParentSampleReceiving>() {
				});
		String arrivalDate;
		if (convertedObject.getSarrivaldate() == null) {
			arrivalDate = null;
		} else {
			arrivalDate = "'" + convertedObject.getSarrivaldate() + "'";
		}
		short ncollectionsitecode = ((Number) inputMap.get("ncollectionsitecode")).shortValue();
		int ncollectedhospitalcode = (int) inputMap.get("ncollectedhospitalcode");
		int nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");
		int nbioparentsamplecode = (int) inputMap.get("nbioparentsamplecode");

			
			final String updateQueryString = "update bioparentsamplereceiving set ncollectionsitecode = " 
												+ ncollectionsitecode
												+", ncollectedhospitalcode = "
												+ ncollectedhospitalcode
												+", nsamplestoragelocationcode = "
												+ nsamplestoragelocationcode
												+", darrivaldate = "+arrivalDate
												+", ntzarrivaldate = "+convertedObject.getNtzarrivaldate()
												+", noffsetarrivaldate = "+convertedObject.getNoffsetarrivaldate()
												+", dtransactiondate = '"+dateUtilityFunction.getCurrentDateTime(userInfo)
												+"', ntztransactiondate = "+userInfo.getNtimezonecode()
												+", noffsettransactiondate = "+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())									
												+ " where nbioparentsamplecode=" + nbioparentsamplecode +";";
				
				jdbcTemplate.execute(updateQueryString);
				
				
				//status code:200
				return getActiveBioParentSampleReceiving(nbioparentsamplecode,userInfo);
			
		
	}
	
	@Override
	public ResponseEntity<Object> createBioParentSampleReceiving( Map<String, Object> inputMap, UserInfo userInfo)throws Exception{
		boolean isValidSubjectID = (boolean) inputMap.get("isValidSubjectID");
		if (isValidSubjectID) {
			ObjectMapper objMapper = new ObjectMapper();
			String sQuery = " lock  table lockbioparentreceiving " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
			String ssubjectid = (String) inputMap.get("ssubjectid");
			int nbioprojectcode = (int) inputMap.get("nbioprojectcode");
			short ncohortno = 1;
			String sparentsamplecode = checkSubjectIdAndGetParentSampleCode(ssubjectid, nbioprojectcode);
			if (sparentsamplecode == null) {
				sQuery = "select sprojectcode from bioproject where nbioprojectcode = "+nbioprojectcode
						+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				String sprojectcode= jdbcTemplate.queryForObject(sQuery, String.class);
				sQuery = "select nsequenceno from bioparentseqno where nbioprojectcode = "+nbioprojectcode
						+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int nseqno= (int)jdbcTemplate.queryForObject(sQuery, Integer.class);
				nseqno = nseqno + 1;
				String paddedSeq = String.format("%05d", nseqno);
				sparentsamplecode = sprojectcode + paddedSeq;
				
				String updateQry = " update  bioparentseqno set nsequenceno = "+nseqno
						+" where nbioprojectcode = "+nbioprojectcode
						+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
                        
		        jdbcTemplate.execute(updateQry);
			}
			else{
				sQuery = "SELECT MAX(ncohortno)+1 FROM bioparentsamplereceiving WHERE sparentsamplecode = N'"+stringUtilityFunction.replaceQuote(sparentsamplecode)
						+"' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				ncohortno = jdbcTemplate.queryForObject(sQuery, Short.class);
			}
			
			final JavaTimeModule javaTimeModule = new JavaTimeModule();
			objMapper.registerModule(javaTimeModule);
			final List<String> lstDateField = new ArrayList<String>();
			List<String> lstDatecolumn = new ArrayList<String>();

			String strArrivalDate = (String) inputMap.get("darrivaldate");
			Instant darrivaldate = Instant.parse(strArrivalDate);
			
			BioParentSampleReceiving objBioParentSamplereceiving = new BioParentSampleReceiving();
			if (darrivaldate != null) {
				objBioParentSamplereceiving.setSarrivaldate(
						dateUtilityFunction.instantDateToString(darrivaldate).replace("T", " ").replace("Z", ""));
				lstDateField.add("sarrivaldate");
				lstDatecolumn.add("ntzarrivaldate");
			}
			
			final BioParentSampleReceiving convertedObject = objMapper.convertValue(
					dateUtilityFunction.convertInputDateToUTCByZone(objBioParentSamplereceiving, lstDateField, lstDatecolumn, true, userInfo),
					new TypeReference<BioParentSampleReceiving>() {
					});
			String arrivalDate;
			if (convertedObject.getSarrivaldate() == null) {
				arrivalDate = null;
			} else {
				arrivalDate = "'" + convertedObject.getSarrivaldate() + "'";
			}
			
			String scasetype = (String) inputMap.get("scasetype");
			int ndiseasecode = (int) inputMap.get("ndiseasecode");
			short ncollectionsitecode = ((Number) inputMap.get("ncollectionsitecode")).shortValue();
			int ncollectedhospitalcode = (int) inputMap.get("ncollectedhospitalcode");
			int nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");
			
			sQuery = "SELECT nsequenceno+1 FROM seqnobiobankmanagement WHERE stablename = 'bioparentsamplereceiving' "
					+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nbioparentsamplecode = (int) jdbcTemplate.queryForObject(sQuery, Integer.class);
			
			final String insertquery = "INSERT INTO public.bioparentsamplereceiving("
					+ "	nbioparentsamplecode, sparentsamplecode, ssubjectid, scasetype, ndiseasecode, nbioprojectcode, ncohortno, ncollectionsitecode, ncollectedhospitalcode, nsamplestoragelocationcode,"
					+ " darrivaldate, ntzarrivaldate, noffsetarrivaldate, dtransactiondate, ntztransactiondate, noffsettransactiondate, nusercode, nuserrolecode, nsitecode, nstatus)"
					+ "	VALUES (" +nbioparentsamplecode+", N'"+stringUtilityFunction.replaceQuote(sparentsamplecode)+"', N'"+stringUtilityFunction.replaceQuote(ssubjectid)+"', N'"
					+ stringUtilityFunction.replaceQuote(scasetype)+ "', "+ndiseasecode+", "+nbioprojectcode+", "+ncohortno+", "+ncollectionsitecode
					+", "+ncollectedhospitalcode+", "+nsamplestoragelocationcode+", "
					+arrivalDate+", "+convertedObject.getNtzarrivaldate()+", "+convertedObject.getNoffsetarrivaldate()+", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo)+"', "+userInfo.getNtimezonecode()+", "+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+", "
					+ userInfo.getNusercode() + ", "
					+ userInfo.getNuserrole() + ", "+userInfo.getNtranssitecode()
					+", "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
			
			jdbcTemplate.execute(insertquery);
			
            String updateQry = " update  seqnobiobankmanagement set nsequenceno = "+nbioparentsamplecode
                              +" where stablename = 'bioparentsamplereceiving'"
                              +" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updateQry);
			
			return getBioParentSampleReceiving(userInfo);
			
			
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.NOTVALIDSUBJECTID.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}
	
	
	public String checkSubjectIdAndGetParentSampleCode(String ssubjectid, int nbioprojectcode) throws Exception {
        
		String sparentsamplecode = null;

	    final String strQuery = "SELECT sparentsamplecode FROM bioparentsamplereceiving " +
	            "WHERE ssubjectid = N'"+stringUtilityFunction.replaceQuote(ssubjectid)
	            +"' AND nbioprojectcode = "+nbioprojectcode
	            +" AND nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	            +" group by sparentsamplecode";

	    final BioParentSampleReceiving result =
				(BioParentSampleReceiving) jdbcUtilityTemplateFunction.queryForObject(strQuery, BioParentSampleReceiving.class, jdbcTemplate);
		

	    if (result != null) {
	       sparentsamplecode = result.getSparentsamplecode();
	    } 

	    return sparentsamplecode;
	}

	@Override
	public ResponseEntity<Object> getActiveBioParentSampleCollection(final int nbioparentsamplecollectioncode,
			final UserInfo userInfo) throws Exception {

		boolean boolValue = validateParentSampleCollection(nbioparentsamplecollectioncode, userInfo);
		if (boolValue) {
			final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
			final String strQuery = "select bpsc.nbioparentsamplecollectioncode, bpsc.nbioparentsamplecode, bpsr.sparentsamplecode, bpsr.ncohortno, u.sfirstname||' '|| u.slastname as sprojectincharge, bp.sprojecttitle, bp.sprojectcode, "
					+ "cs.ssitename as scollectionsitename, pc.sproductcatname, bpsc.nproductcatcode,  bpsc.nnoofsamples,  bpsc.ntemperature, "
					+ "bpsc.scollectorname, bpsc.stemporarystoragename, bpsc.ssendername, bpsc.sinformation, bpsc.nrecipientusercode, "
					+ "u1.sfirstname||' '|| u1.slastname as srecipientusername, bpsc.jsonuidata, bpsc.ntransactionstatus, "
					+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
					+ "bpsc.dsamplecollectiondate, bpsc.ntzsamplecollectiondate, bpsc.noffsetsamplecollectiondate, tz1.stimezoneid AS stzsamplecollectiondate, "
					+ "COALESCE(TO_CHAR(bpsc.dsamplecollectiondate,'" + userInfo.getSpgsitedatetime()
					+ "'), '') AS ssamplecollectiondate, "
					+ "bpsc.dbiobankarrivaldate, bpsc.ntzbiobankarrivaldate, bpsc.noffsetbiobankarrivaldate, tz1.stimezoneid AS stzbiobankarrivaldate, "
					+ "COALESCE(TO_CHAR(bpsc.dbiobankarrivaldate,'" + userInfo.getSpgsitedatetime()
					+ "'), '') AS sbiobankarrivaldate, "
					+ "bpsc.dtemporarystoragedate, bpsc.ntztemporarystoragedate, bpsc.noffsettemporarystoragedate, tz1.stimezoneid AS stztemporarystoragedate, "
					+ "COALESCE(TO_CHAR(bpsc.dtemporarystoragedate,'" + userInfo.getSpgsitedatetime()
					+ "'), '') AS stemporarystoragedate, "
					+ "bpsc.dtransactiondate, bpsc.ntztransactiondate, bpsc.noffsettransactiondate, tz1.stimezoneid AS stztransactiondate, "
					+ "COALESCE(TO_CHAR(bpsc.dtransactiondate,'" + userInfo.getSpgsitedatetime()
					+ "'), '') AS stransactiondate "
					+ "from bioparentsamplecollection bpsc, bioparentsamplereceiving bpsr, bioproject bp, users u, site cs, site rs, productcategory pc, "
					+ "users u1, transactionstatus ts, timezone tz1, timezone tz2, timezone tz3, timezone tz4 "
					+ "where bpsc.nbioparentsamplecode = bpsr.nbioparentsamplecode "
					+ "and bpsc.nbioparentsamplecollectioncode = " + nbioparentsamplecollectioncode + " "
					+ "and bpsc.nsitecode = " + userInfo.getNtranssitecode() + " " + "and bpsr.nsitecode = "
					+ userInfo.getNtranssitecode() + " " + "and bpsc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bpsr.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and bpsr.nbioprojectcode = bp.nbioprojectcode " + "and bp.nsitecode = "
					+ userInfo.getNmastersitecode() + " " + "and bp.nusercode = u.nusercode " + "and u.nsitecode = "
					+ userInfo.getNmastersitecode() + " " + "and u.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and bpsr.ncollectionsitecode = cs.nsitecode and cs.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and bpsc.nreceivingsitecode = rs.nsitecode and rs.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and bpsc.nproductcatcode =  pc.nproductcatcode and pc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and bpsc.nrecipientusercode = u1.nusercode and u1.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and bpsc.ntransactionstatus = ts.ntranscode "
					+ "and bpsc.ntzsamplecollectiondate = tz1.ntimezonecode and tz1.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and bpsc.ntzbiobankarrivaldate = tz2.ntimezonecode and tz2.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and bpsc.ntztemporarystoragedate = tz3.ntimezonecode and tz3.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and bpsc.ntztransactiondate = tz4.ntimezonecode and tz4.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by bpsc.nbioparentsamplecollectioncode desc";
			final BioParentSampleCollection objBioParentSampleCollection = (BioParentSampleCollection) jdbcUtilityTemplateFunction
					.queryForObject(strQuery, BioParentSampleCollection.class, jdbcTemplate);

			if (objBioParentSampleCollection != null) {
				outputMap.putAll(getUserAndProductCatDetails(userInfo));
				outputMap.put("selectedBioParentSampleCollection", objBioParentSampleCollection);
			} else {
				outputMap.put("sampleTypeList", null);
				outputMap.put("recipientsList", null);
				outputMap.put("selectedBioParentSampleCollection", null);
			}

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTNOTYETPROCESSEDSTATUSRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

	}

	@Override
	public ResponseEntity<Object> createParentSampleCollection(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		
		String sQuery = " lock  table lockbioparentcollection " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		
		int seqNoSampleCollection = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='bioparentsamplecollection' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);
		int seqNoSampleCollectionHistory = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='bioparentsamplecollectionhistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);
		seqNoSampleCollection = seqNoSampleCollection + 1;
		seqNoSampleCollectionHistory = seqNoSampleCollectionHistory + 1;

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		final BioParentSampleCollection objParentSampleCollection = objmapper
				.convertValue(inputMap.get("parentsamplecollection"), BioParentSampleCollection.class);

		final String ssamplecollectiondate = (objParentSampleCollection.getSsamplecollectiondate() != null
				&& !objParentSampleCollection.getSsamplecollectiondate().isEmpty())
						? "'" + objParentSampleCollection.getSsamplecollectiondate().toString().replace("T", " ")
								.replace("Z", "") + "'"
						: null;
		final String sbiobankarrivaldate = (objParentSampleCollection.getSbiobankarrivaldate() != null
				&& !objParentSampleCollection.getSbiobankarrivaldate().isEmpty())
						? "'" + objParentSampleCollection.getSbiobankarrivaldate().toString().replace("T", " ")
								.replace("Z", "") + "'"
						: null;
		final String stemporarystoragedate = (objParentSampleCollection.getStemporarystoragedate() != null
				&& !objParentSampleCollection.getStemporarystoragedate().isEmpty())
						? "'" + objParentSampleCollection.getStemporarystoragedate().toString().replace("T", " ")
								.replace("Z", "") + "'"
						: null;
		JSONObject jsonObject = new JSONObject(objParentSampleCollection.getJsonuidata());

		String strInsert = "insert into bioparentsamplecollection (nbioparentsamplecollectioncode, nbioparentsamplecode, "
				+ "nreceivingsitecode, nproductcatcode, nnoofsamples, ntemperature, scollectorname, stemporarystoragename, "
				+ "ssendername, nrecipientusercode, sinformation, jsonuidata, dsamplecollectiondate, ntzsamplecollectiondate, "
				+ "noffsetsamplecollectiondate, dbiobankarrivaldate, ntzbiobankarrivaldate, noffsetbiobankarrivaldate, "
				+ "dtemporarystoragedate, ntztemporarystoragedate, noffsettemporarystoragedate, dtransactiondate, "
				+ "ntztransactiondate, noffsettransactiondate, ntransactionstatus, nusercode, nuserrolecode, nsitecode, "
				+ "nstatus) values (" + seqNoSampleCollection + ", "
				+ objParentSampleCollection.getNbioparentsamplecode() + ", "
				+ objParentSampleCollection.getNreceivingsitecode() + ", "
				+ objParentSampleCollection.getNproductcatcode() + ", " + objParentSampleCollection.getNnoofsamples()
				+ ", " + objParentSampleCollection.getNtemperature() + ", '"
				+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getScollectorname()) + "', '"
				+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getStemporarystoragename()) + "', '"
				+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getSsendername()) + "', "
				+ objParentSampleCollection.getNrecipientusercode() + ", '"
				+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getSinformation()) + "', '" + jsonObject
				+ "', " + ssamplecollectiondate + ", " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + sbiobankarrivaldate
				+ ", " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + stemporarystoragedate
				+ ", " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ Enumeration.TransactionStatus.NOTYETPROCESSED.gettransactionstatus() + ", " + userInfo.getNusercode()
				+ ", " + userInfo.getNuserrole() + ", " + userInfo.getNtranssitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
		strInsert += "insert into bioparentsamplecollectionhistory (nbioparentsamplecolhistorycode,"
				+ " nbioparentsamplecollectioncode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,"
				+ " ntransactionstatus, scomments, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode,"
				+ " nsitecode, nstatus) values (" + seqNoSampleCollectionHistory + ", " + seqNoSampleCollection + ", "
				+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
				+ userInfo.getNdeputyuserrole() + ", "
				+ Enumeration.TransactionStatus.NOTYETPROCESSED.gettransactionstatus() + ", null, '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ userInfo.getNtimezonecode() + ", " + userInfo.getNtranssitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
		strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoSampleCollection
				+ " where stablename='bioparentsamplecollection';";
		strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoSampleCollectionHistory
				+ " where stablename='bioparentsamplecollectionhistory';";
		jdbcTemplate.execute(strInsert);

		List<BioParentSampleCollection> lstBioParentSampleCollection = getBioParentSampleCollection(
				objParentSampleCollection.getNbioparentsamplecode(), userInfo);
		outputMap.put("lstBioParentSampleCollection", lstBioParentSampleCollection);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateParentSampleCollection(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		
		final ObjectMapper objmapper = new ObjectMapper();
		final BioParentSampleCollection objParentSampleCollection = objmapper
				.convertValue(inputMap.get("parentsamplecollection"), BioParentSampleCollection.class);

		boolean boolValue = validateParentSampleCollection(objParentSampleCollection.getNbioparentsamplecollectioncode(), userInfo);
		if (boolValue) {
			final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
			final String ssamplecollectiondate = (objParentSampleCollection.getSsamplecollectiondate() != null
					&& !objParentSampleCollection.getSsamplecollectiondate().isEmpty())
							? "'" + objParentSampleCollection.getSsamplecollectiondate().toString().replace("T", " ")
									.replace("Z", "") + "'"
							: null;
			final String sbiobankarrivaldate = (objParentSampleCollection.getSbiobankarrivaldate() != null
					&& !objParentSampleCollection.getSbiobankarrivaldate().isEmpty())
							? "'" + objParentSampleCollection.getSbiobankarrivaldate().toString().replace("T", " ")
									.replace("Z", "") + "'"
							: null;
			final String stemporarystoragedate = (objParentSampleCollection.getStemporarystoragedate() != null
					&& !objParentSampleCollection.getStemporarystoragedate().isEmpty())
							? "'" + objParentSampleCollection.getStemporarystoragedate().toString().replace("T", " ")
									.replace("Z", "") + "'"
							: null;
			JSONObject jsonObject = new JSONObject(objParentSampleCollection.getJsonuidata());

			String strUpdate = "update bioparentsamplecollection set nbioparentsamplecode="
					+ objParentSampleCollection.getNbioparentsamplecode() + ", nreceivingsitecode="
					+ objParentSampleCollection.getNreceivingsitecode() + ", nproductcatcode="
					+ objParentSampleCollection.getNproductcatcode() + ", nnoofsamples="
					+ objParentSampleCollection.getNnoofsamples() + ", ntemperature="
					+ objParentSampleCollection.getNtemperature() + ", scollectorname='"
					+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getScollectorname())
					+ "', stemporarystoragename='"
					+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getStemporarystoragename())
					+ "', ssendername='"
					+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getSsendername())
					+ "', nrecipientusercode=" + objParentSampleCollection.getNrecipientusercode() + ", sinformation='"
					+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getSinformation())
					+ "', jsonuidata='" + jsonObject + "', dsamplecollectiondate=" + ssamplecollectiondate
					+ ", ntzsamplecollectiondate=" + userInfo.getNtimezonecode() + ", noffsetsamplecollectiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", dbiobankarrivaldate="
					+ sbiobankarrivaldate + ", ntzbiobankarrivaldate=" + userInfo.getNtimezonecode()
					+ ", noffsetbiobankarrivaldate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ ", dtemporarystoragedate=" + stemporarystoragedate + ", ntztemporarystoragedate="
					+ userInfo.getNtimezonecode() + ", noffsettemporarystoragedate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsettransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", nusercode="
					+ userInfo.getNusercode() + ", nuserrolecode=" + userInfo.getNuserrole()
					+ " where nbioparentsamplecollectioncode="
					+ objParentSampleCollection.getNbioparentsamplecollectioncode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(strUpdate);
			List<BioParentSampleCollection> lstBioParentSampleCollection = getBioParentSampleCollection(
					objParentSampleCollection.getNbioparentsamplecode(), userInfo);
			outputMap.put("lstBioParentSampleCollection", lstBioParentSampleCollection);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTNOTYETPROCESSEDSTATUSRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

	}

	@Override
	public ResponseEntity<Object> deleteParentSampleCollection(final int nbioparentsamplecollectioncode,
			final int nbioparentsamplecode, final UserInfo userInfo) throws Exception {
		boolean boolValue = validateParentSampleCollection(nbioparentsamplecollectioncode, userInfo);
		if (boolValue) {
			final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
			String udpateStr = "update bioparentsamplecollection set nstatus="
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " where nbioparentsamplecollectioncode="
					+ nbioparentsamplecollectioncode;
			jdbcTemplate.execute(udpateStr);
			List<BioParentSampleCollection> lstBioParentSampleCollection = getBioParentSampleCollection(
					nbioparentsamplecode, userInfo);
			outputMap.put("lstBioParentSampleCollection", lstBioParentSampleCollection);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTNOTYETPROCESSEDSTATUSRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	public boolean validateParentSampleCollection(int nbioparentsamplecollectioncode, UserInfo userInfo)
			throws Exception {
		String strValidate = "select ntransactionstatus from bioparentsamplecollection where nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbioparentsamplecollectioncode="
				+ nbioparentsamplecollectioncode;
		int intValidate = jdbcTemplate.queryForObject(strValidate, Integer.class);
		boolean boolValue = false;
		if (intValidate == Enumeration.TransactionStatus.NOTYETPROCESSED.gettransactionstatus()) {
			boolValue = true;
		}
		return boolValue;
	}

}
