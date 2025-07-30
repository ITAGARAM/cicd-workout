package com.agaramtech.qualis.archivalandpurging.restoreindividual.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.archivalandpurging.purge.model.PurgeMaster;
import com.agaramtech.qualis.archivalandpurging.restoreindividual.model.RestoreFilter;
//import com.agaramtech.qualis.basemaster.model.Unit;
//import com.agaramtech.qualis.basemaster.service.unit.UnitDAOImpl;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
//import com.agaramtech.qualis.global.CommonFunction;
//import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
//import com.agaramtech.qualis.global.ProjectDAOSupport;
//import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
//import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.release.model.COAParent;
//import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
/**
 * 
 */


@AllArgsConstructor
@Repository
public class RestoreIndividualDAOImpl implements RestoreIndividualDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestoreIndividualDAOImpl.class);
	
	//private final StringUtilityFunction stringUtilityFunction;
	//private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	//private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	//private final ProjectDAOSupport projectDAOSupport;
	//private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	
	//private static int dataBaseType = 0;
	//private ObjectMapper mapper = new ObjectMapper();
	

	@Override
	public ResponseEntity<Object> getRestoreIndividual(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		LOGGER.info("getRestoreIndividual Start: "+LocalDateTime.now());
		final Map<String, Object> returnMap = new HashMap<>();
		//Integer nSiteCode = (Integer) inputMap.get("nsitecode");
		Site selectedSite = null;
		PurgeMaster selectedPurgeMaster=null;
		RestoreFilter selectedRestoreFilter=null;
		//String toDate = (String) inputMap.get("dto");
		
		String sQuery="select lt.nsequencenocomposite nsitecode,s.ssitename from public.lims_purg_master_tables_transaction lt,"
				+" site s where lt.nsequencenocomposite = s.nsitecode and lt.nstatus = "
				+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and s.nstatus = "
				+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and lt.nsequencenocomposite > 0 "
				+ " and s.nmastersitecode = "+userInfo.getNmastersitecode()
				+ " group by lt.nsequencenocomposite,s.ssitename order by lt.nsequencenocomposite";
		LOGGER.info("getRestoreIndividual sQuery1: "+sQuery);
		LOGGER.info("getRestoreIndividual Start1: "+LocalDateTime.now());
		final List<Site> lstSite = jdbcTemplate.query(sQuery, new Site());
		LOGGER.info("getRestoreIndividual Start2: "+LocalDateTime.now());
		returnMap.put("Site", lstSite);
		if(lstSite !=null && lstSite.size()>0)
		{
		selectedSite = lstSite.get(0);
		
		returnMap.put("selectedSite", selectedSite);
		}
		else
		{
			
			returnMap.put("selectedSite", null);
		}
		//dd/MM/yyyy'
		List<PurgeMaster> lstPurge=new ArrayList();
		if(lstSite.size()>0)
		{
		sQuery = "select npurgemastercode,to_char(dtodate, '"+userInfo.getSdatetimeformat()
		+"') as stodate from purgemaster  where nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and npurgemastercode > 0 and ntransactionstatus = "+Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
		+" and npurgesitecode="+lstSite.get(0).getNsitecode()+" and nsitecode = " + userInfo.getNmastersitecode()
		+" order by npurgemastercode;";
		LOGGER.info("getRestoreIndividual Start 3 sQuery: "+sQuery);
		LOGGER.info("getRestoreIndividual Start3: "+LocalDateTime.now());
		 lstPurge = jdbcTemplate.query(sQuery, new PurgeMaster());
		 LOGGER.info("getRestoreIndividual Start4: "+LocalDateTime.now());
		 returnMap.put("PurgeMaster", lstPurge);
		 if(lstPurge != null && lstPurge.size()>0)
		 {
		   selectedPurgeMaster = lstPurge.get(0);
		   
		   returnMap.put("selectedPurgeMaster", selectedPurgeMaster);
		 }
		 else
		 {
			 
			 returnMap.put("selectedPurgeMaster", null);
		 }
		}
		else
		{
			returnMap.put("PurgeMaster", lstPurge);
			returnMap.put("selectedPurgeMaster", null);
		}
		sQuery = "select nrestorefiltercode,srestorefiltername,nsorter from restorefiltertype  where "
				+" nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and nsitecode = " + userInfo.getNmastersitecode()+" order by nsorter asc;";
		LOGGER.info("getRestoreIndividual Start5: "+sQuery);
		LOGGER.info("getRestoreIndividual Start5: "+LocalDateTime.now());
		final List<RestoreFilter> lstFilter= jdbcTemplate.query(sQuery, new RestoreFilter());
		LOGGER.info("getRestoreIndividual Start6: "+LocalDateTime.now());
		if(lstFilter !=null && lstFilter.size()>0)
		{
		  selectedRestoreFilter = lstFilter.get(0);
		  returnMap.put("RestoreFilter", lstFilter);
		  returnMap.put("selectedRestoreFilter", selectedRestoreFilter);
		  returnMap.put("TempRestoreFilter", selectedRestoreFilter);
		}
		else
		{
			returnMap.put("RestoreFilter", lstFilter);
			  returnMap.put("selectedRestoreFilter",null);
		}

		int nTempSiteCode = -1;
		int nTempPurgeMasterCode = -1;
		sQuery="";
		List<Map<String, Object>> lstRestoreIndividual = new ArrayList(); 
		if(lstSite != null && lstSite.size()>0 && lstPurge != null && lstPurge.size()>0)
		{
			nTempSiteCode =lstSite.get(0).getNsitecode();
			nTempPurgeMasterCode = lstPurge.get(0).getNpurgemastercode();
		
	sQuery="select distinct cp.ncoaparentcode,cp.sreportno,cp.nsitecode,s.ssitename,"
			+ " to_char(pm.dtodate, '"+userInfo.getSdatetimeformat()+"') as stodate,ts.stransstatus "
			+ " from purge_coaparent cp,purge_coachild cc,site s,lims_purg_master_tables_transaction lt,"
			+" purgemaster pm,transactionstatus ts,purge_releasehistory rh "
			+ " where cp.ncoaparentcode = cc.ncoaparentcode and cp.ncoaparentcode = lt.nsequenceno and "
			+" cc.ncoaparentcode = lt.nsequenceno and cp.nsitecode = s.nsitecode and "
			+" s.nsitecode = lt.nsequencenocomposite and cp.nsitecode = lt.nsequencenocomposite and "
			+" lt.nsequenceno > 0 and cp.ncoaparentcode = rh.ncoaparentcode and rh.ntransactionstatus = "
			+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+" and "
			+" rh.ntransactionstatus = ts.ntranscode and cp.nsitecode = "+nTempSiteCode     //lstSite.get(0).getNsitecode()
			+" and lt.npurgmastercode = pm.npurgemastercode and "
			+ " s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " lt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " pm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " rh.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+" pm.npurgemastercode = "+nTempPurgeMasterCode                    
			+" group by cp.ncoaparentcode,cp.sreportno,cp.nsitecode,s.ssitename,"
			+" to_char(pm.dtodate, '"+userInfo.getSdatetimeformat()+"'),"
			+ "ts.stransstatus  order by cp.ncoaparentcode";
				
	LOGGER.info("getRestoreIndividual Start7: "+sQuery);
	LOGGER.info("getRestoreIndividual Start7: "+LocalDateTime.now());
		
	 lstRestoreIndividual = jdbcTemplate.queryForList(sQuery);
		}
		LOGGER.info("getRestoreIndividual Start8: "+LocalDateTime.now());
		
		returnMap.put("RestoreIndividual", lstRestoreIndividual);
		
		
		String scoaparentcode="-1";
		String ssitecode="-1";
		if(lstRestoreIndividual !=null && lstRestoreIndividual.size()>0)
		{
		returnMap.put("selectedRestoreIndividual", Arrays.asList(lstRestoreIndividual.get(lstRestoreIndividual.size()-1)));
		
		scoaparentcode = ((Map<String, Object>)lstRestoreIndividual.get(lstRestoreIndividual.size()-1)).get("ncoaparentcode").toString();
		ssitecode = ((Map<String, Object>)lstRestoreIndividual.get(lstRestoreIndividual.size()-1)).get("nsitecode").toString();
		}
		else
		{
			returnMap.put("selectedRestoreIndividual", null);
		}

		List<Map<String, Object>> lstRestoreSample = new ArrayList();
		if(scoaparentcode != "-1" && ssitecode != "-1")
		{
		sQuery="select cp.ncoaparentcode,cc.npreregno,cc.ntransactionsamplecode,cc.ntransactiontestcode, "
				+ "r.jsondata->'Sample Name'->>'label' ssamplename,rs.jsondata->>'Sample Name_child' ssubsample,"
				+ "rt.jsondata->>'stestname' stestname,rp.jsondata->>'sparametersynonym' sparametername,"
				+ "ra.sarno,rt.jsondata->>'ssectionname' ssectionname,g.sgradename,rp.jsondata->>'sfinal' sresult,"
				+ " (select case when (rstv.jsondata->>'nneedsubsample')::text='true' then  "
				+ " (select ssamplearno from Purge_registrationsamplearno prsa where  prsa.npreregno = r.npreregno) "
				+ " else '-' end from regsubtypeconfigversion rstv where "
				+ " r.nregsubtypeversioncode = rstv.nregsubtypeversioncode) samplearno  "
				+ "from purge_releaseparameter rp "
				+ "JOIN purge_registrationtest rt ON rp.npreregno = rt.npreregno and "
				+ "rp.ntransactiontestcode = rt.ntransactiontestcode and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN purge_coachild cc ON rt.npreregno = cc.npreregno and "
				+ "rt.ntransactionsamplecode = cc.ntransactionsamplecode and "
				+ "rt.ntransactiontestcode = cc.ntransactiontestcode and cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN purge_registrationsample rs ON cc.npreregno = rs.npreregno and "
				+ "cc.ntransactionsamplecode = rs.ntransactionsamplecode and rs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN purge_registrationarno ra ON rs.npreregno = ra.npreregno and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN purge_registration r ON ra.npreregno = r.npreregno and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN purge_coaparent cp ON cc.ncoaparentcode = cp.ncoaparentcode and cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN grade g ON rp.ngradecode = g.ngradecode and g.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " where cp.ncoaparentcode = "+scoaparentcode+" and cp.nsitecode = "+ssitecode+";";
		
		LOGGER.info("getRestoreIndividual Start9: "+sQuery);
		LOGGER.info("getRestoreIndividual Start9: "+LocalDateTime.now());
		
		lstRestoreSample = jdbcTemplate.queryForList(sQuery);
		}
		LOGGER.info("getRestoreIndividual Start10: "+LocalDateTime.now());
		
		returnMap.put("RestoreSample", lstRestoreSample);
		
		if(lstRestoreSample.size()>0)
		{
		returnMap.put("selectedRestoreSample", Arrays.asList(lstRestoreSample.get(lstRestoreSample.size()-1)));
		}
		else
		{
			returnMap.put("selectedRestoreSample", null);	
		}
		
		returnMap.put("bFilterSubmitFlag", true);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	
	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createRestoreIndividual(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		LOGGER.info("Create RestoreIndividual Start : "+LocalDateTime.now());
		
		final List<Object> listObject = new ArrayList<Object>();
		Integer nSiteCode = (Integer) inputMap.get("nsitecode");
		String sParentCode = (String) inputMap.get("ncoaparentcode");
		String strArray[] = sParentCode.split(",");
		String sQuery="";
		if(strArray.length>0)
		{
			LOGGER.info("Create RestoreIndividual lockcancelreject : "+LocalDateTime.now());
			String sLockQuery = " lock  table lockcancelreject "
			+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
	jdbcTemplate.execute(sLockQuery);
	
	LOGGER.info("Create RestoreIndividual lockresultparamcomment : "+LocalDateTime.now());
	sLockQuery = " lock  table lockresultparamcomment "
			+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
	jdbcTemplate.execute(sLockQuery);
	LOGGER.info("Create RestoreIndividual lockresultcorrection : "+LocalDateTime.now());
	sLockQuery = " lock  table lockresultcorrection "
			+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
	jdbcTemplate.execute(sLockQuery);
	
	LOGGER.info("Create RestoreIndividual approvalparameter DISABLE : "+LocalDateTime.now());
	jdbcTemplate.execute("ALTER TABLE public.approvalparameter DISABLE TRIGGER approvalparameter_trigger;");
	LOGGER.info("Create RestoreIndividual coachild DISABLE : "+LocalDateTime.now());
	jdbcTemplate.execute("ALTER TABLE public.coachild DISABLE TRIGGER externalorderreleasestatus_trigger;");

		for (int i = 0; i < strArray.length; i++) {
			LOGGER.info("Create RestoreIndividual strArray for LOOP : "+LocalDateTime.now());
			sQuery="select * from purge_coaparent where ncoaparentcode = "+strArray[i]+" and "
					+" nsitecode = "+nSiteCode+"";
			LOGGER.info("Create RestoreIndividual sQuery : "+sQuery);
			LOGGER.info("Create RestoreIndividual sQuery1 : "+LocalDateTime.now());
			 
			  COAParent objCoaparent =(COAParent) jdbcUtilityFunction.queryForObject(sQuery, COAParent.class, jdbcTemplate);
			  
			  LOGGER.info("Create RestoreIndividual sQuery2 : "+LocalDateTime.now());
			  listObject.add(Arrays.asList(objCoaparent));	
			  LOGGER.info("Create RestoreIndividual sQuery3 : "+LocalDateTime.now());
			
		sQuery="Select * from public.fn_purgingrestorebyparentcode("+strArray[i]+","+nSiteCode+");";
		LOGGER.info("Create RestoreIndividual sQuery4 : "+sQuery);
		LOGGER.info("Create RestoreIndividual sQuery4 : "+LocalDateTime.now());
		  jdbcTemplate.execute(sQuery);
		  LOGGER.info("Create RestoreIndividual sQuery5 : "+LocalDateTime.now());
		  
		  	  
		}
		LOGGER.info("Create RestoreIndividual approvalparameter ENABLE : "+LocalDateTime.now());
		jdbcTemplate.execute("ALTER TABLE public.approvalparameter ENABLE TRIGGER approvalparameter_trigger;");
		LOGGER.info("Create RestoreIndividual coachild ENABLE : "+LocalDateTime.now());
		jdbcTemplate.execute("ALTER TABLE public.coachild ENABLE TRIGGER externalorderreleasestatus_trigger;");
		LOGGER.info("Create RestoreIndividual Audit Start : "+LocalDateTime.now());
		
		auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, Arrays.asList("IDS_RESTORERELEASEDDATA"), userInfo);
		LOGGER.info("Create RestoreIndividual Audit End : "+LocalDateTime.now());
		}
		return getReleasedSamples(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getRestoreSampleData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		int nSiteCode = (Integer) inputMap.get("nsitecode");
		String scoaParentcode=(String)inputMap.get("ncoaparentcode");
		
		String sQuery="select distinct cp.ncoaparentcode,cp.sreportno,cp.nsitecode,s.ssitename,"
				+ "to_char(pm.dtodate, '"+userInfo.getSdatetimeformat()+"') as stodate,ts.stransstatus "
				+ " from purge_coaparent cp,purge_coachild cc,site s,lims_purg_master_tables_transaction lt,"
				+" purgemaster pm,transactionstatus ts,purge_releasehistory rh "
				+ " where cp.ncoaparentcode = cc.ncoaparentcode and cp.ncoaparentcode = lt.nsequenceno "
				+" and cc.ncoaparentcode = lt.nsequenceno and cp.nsitecode = s.nsitecode and "
				+" s.nsitecode = lt.nsequencenocomposite and cp.nsitecode = lt.nsequencenocomposite and "
				+ " lt.nsequenceno > 0 and cp.ncoaparentcode = rh.ncoaparentcode and rh.ntransactionstatus = "
				+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+" and "
				+ " rh.ntransactionstatus = ts.ntranscode and cp.nsitecode = "+nSiteCode+" and "
				+ " cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " lt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " pm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " rh.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+" lt.npurgmastercode = pm.npurgemastercode and cp.ncoaparentcode in ("
				+scoaParentcode+") group by cp.ncoaparentcode,cp.sreportno,cp.nsitecode,s.ssitename,"
				+ " to_char(pm.dtodate, '"+userInfo.getSdatetimeformat()+"'),ts.stransstatus  "
				+" order by cp.ncoaparentcode";
		final List<Map<String, Object>> lstRestoreIndividual = jdbcTemplate.queryForList(sQuery);
		if(lstRestoreIndividual.size()>0)
		{
			returnMap.put("selectedRestoreIndividual", lstRestoreIndividual);
		}
		else
		{
			returnMap.put("selectedRestoreIndividual", null);
		}
		
		sQuery="select cp.ncoaparentcode,cc.npreregno,cc.ntransactionsamplecode,cc.ntransactiontestcode, "
				+ "r.jsondata->'Sample Name'->>'label' ssamplename,rs.jsondata->>'Sample Name_child' ssubsample,"
				+ "rt.jsondata->>'stestname' stestname,rp.jsondata->>'sparametersynonym' sparametername,"
				+ "ra.sarno,rt.jsondata->>'ssectionname' ssectionname,g.sgradename,rp.jsondata->>'sfinal' sresult,"
				+ " (select case when (rstv.jsondata->>'nneedsubsample')::text='true' then  "
				+ " (select ssamplearno from Purge_registrationsamplearno prsa where  prsa.npreregno = r.npreregno) "
				+ " else '-' end from regsubtypeconfigversion rstv where "
				+ " r.nregsubtypeversioncode = rstv.nregsubtypeversioncode) samplearno  "
				+ "from purge_releaseparameter rp "
				+ "JOIN purge_registrationtest rt ON rp.npreregno = rt.npreregno and "
				+ "rp.ntransactiontestcode = rt.ntransactiontestcode and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN purge_coachild cc ON rt.npreregno = cc.npreregno and "
				+ "rt.ntransactionsamplecode = cc.ntransactionsamplecode and "
				+ "rt.ntransactiontestcode = cc.ntransactiontestcode and cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN purge_registrationsample rs ON cc.npreregno = rs.npreregno and "
				+ "cc.ntransactionsamplecode = rs.ntransactionsamplecode and rs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN purge_registrationarno ra ON rs.npreregno = ra.npreregno and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN purge_registration r ON ra.npreregno = r.npreregno and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN purge_coaparent cp ON cc.ncoaparentcode = cp.ncoaparentcode and cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN grade g ON rp.ngradecode = g.ngradecode and g.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " where cp.ncoaparentcode in ( "+scoaParentcode+") and cp.nsitecode = "+nSiteCode+";";
		
		final List<Map<String, Object>> lstRestoreSample = jdbcTemplate.queryForList(sQuery);
		
		returnMap.put("RestoreSample", lstRestoreSample);
		
		if(lstRestoreSample.size()>0)
		{
		returnMap.put("selectedRestoreSample", Arrays.asList(lstRestoreSample.get(lstRestoreSample.size()-1)));
		}
		else
		{
			returnMap.put("selectedRestoreSample", null);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getPurgeDate(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		
		final Map<String, Object> returnMap = new HashMap<>();
		
		final String strQuery = "select npurgemastercode,to_char(dtodate, '"+userInfo.getSdatetimeformat()+"') as "
				+" stodate from purgemaster  where nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and npurgemastercode > 0 and ntransactionstatus = "+Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
				+" and npurgesitecode="+inputMap.get("nrestoresitecode")+" and nsitecode = " + userInfo.getNmastersitecode();
		
		final List<Map<String, Object>> lstPurgeMaster = jdbcTemplate.queryForList(strQuery);
		returnMap.put("PurgeMaster", lstPurgeMaster);
		if(lstPurgeMaster.size()>0)
		{
		returnMap.put("selectedPurgeMaster", Arrays.asList(lstPurgeMaster.get(0)));
		}
		else
		{
			returnMap.put("selectedPurgeMaster", null);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	@Override
	public ResponseEntity<Object> getReleasedSamples(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		LOGGER.info("Create RestoreIndividual getReleasedSamples Start : "+LocalDateTime.now());
		final Map<String, Object> returnMap = new HashMap<>();
		int nSiteCode = (int) inputMap.get("nsitecode");
		int nPurgeMasterCode = (int) inputMap.get("npurgemastercode");
		int nRestoreFilterCode = (int) inputMap.get("nrestorefiltercode");
	    String sReleaseNo = (String) inputMap.get("sreleaseno");
		String sARNo = (String) inputMap.get("sarno");
		String sMoreReleaseNo = (String) inputMap.get("smorereleaseno");
		String sMoreARNo = (String) inputMap.get("smorearno");
				
		Boolean bFlag=false;
				
		String sQuery="";
	if(nRestoreFilterCode==Enumeration.RestoreFilterType.PURGEDATE.getnrestorefiltertype())
	{
		
	 sQuery="select distinct cp.ncoaparentcode,cp.sreportno,cp.nsitecode,s.ssitename,"
			+ " to_char(pm.dtodate, '"+userInfo.getSdatetimeformat()+"') as stodate,ts.stransstatus "
			+ " from purge_coaparent cp,purge_coachild cc,site s,lims_purg_master_tables_transaction lt,"
			+" purgemaster pm,transactionstatus ts,purge_releasehistory rh "
			+ " where cp.ncoaparentcode = cc.ncoaparentcode and cp.ncoaparentcode = lt.nsequenceno and "
			+" cc.ncoaparentcode = lt.nsequenceno and cp.nsitecode = s.nsitecode and "
			+" s.nsitecode = lt.nsequencenocomposite and cp.nsitecode = lt.nsequencenocomposite and "
			+" lt.nsequenceno > 0 and cp.ncoaparentcode = rh.ncoaparentcode and rh.ntransactionstatus = "
			+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+" and "
			+" rh.ntransactionstatus = ts.ntranscode and cp.nsitecode = "+nSiteCode+" and "
			+ " cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " lt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " pm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+ " rh.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			+" lt.npurgmastercode = pm.npurgemastercode and pm.npurgemastercode = "+nPurgeMasterCode+"";
			
	  if(sMoreReleaseNo.length()>0 && sMoreARNo.length()>0)
	  {
		 final String  sTempQuery="select distinct COALESCE(sreportno,'') sreportno from purge_coaparent "
				 +" cp,purge_coachild cc,purge_registrationarno ra where ra.npreregno = cc.npreregno and "
				 + " cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				 + " cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				 + " ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				 +" cc.ncoaparentcode = cp.ncoaparentcode and ra.sarno = '"+sMoreARNo+"';";
			
		 LOGGER.info("Create RestoreIndividual sTempQuery : "+sTempQuery);
		 LOGGER.info("Create RestoreIndividual sTempQuery : "+LocalDateTime.now());
		 List<String> lstCoaParent = jdbcTemplate.queryForList(sTempQuery, String.class);
		 LOGGER.info("Create RestoreIndividual sTempQuery1 : "+LocalDateTime.now());
		 String sReportNO=	 lstCoaParent.stream().collect(Collectors.joining(","));
		 LOGGER.info("Create RestoreIndividual sReportNO : "+LocalDateTime.now());
		 if(sReportNO.length()==0)
		 {
			 sReportNO="''";
		 }
     	  sQuery=sQuery+" and cp.sreportno in ('"+sMoreReleaseNo+"','"+sReportNO+"')";
	  }
	  else if(sMoreReleaseNo.length()>0 && sMoreARNo.length()==0 )
	  {
		  sQuery=sQuery+" and cp.sreportno = '"+sMoreReleaseNo+"'";
	  }
	  else if(sMoreReleaseNo.length()==0 && sMoreARNo.length()>0)
	  {
		  final String  sTempQuery="select distinct COALESCE(sreportno,'') sreportno from purge_coaparent cp,"
				  +" purge_coachild cc,purge_registrationarno ra where ra.npreregno = cc.npreregno and "
				  + " cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				  + " cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				  + " ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				  +" cc.ncoaparentcode = cp.ncoaparentcode and ra.sarno = '"+sMoreARNo+"';";
		  
		  LOGGER.info("Create RestoreIndividual sTempQuery11 : "+sTempQuery);
		  LOGGER.info("Create RestoreIndividual sTempQuery12 : "+LocalDateTime.now());
		  List<String> lstCoaParent = jdbcTemplate.queryForList(sTempQuery, String.class);
		  LOGGER.info("Create RestoreIndividual sTempQuery13 : "+LocalDateTime.now());
			 String sReportNO=	 lstCoaParent.stream().collect(Collectors.joining(","));
			 LOGGER.info("Create RestoreIndividual sTempQuery14 : "+LocalDateTime.now());
			 if(sReportNO.length()==0)
			 {
				 sReportNO="''";
			 }
			 
			sQuery=sQuery+" and cp.sreportno in ('"+sReportNO+"') ";
	  }
	 
	   sQuery=sQuery+" group by cp.ncoaparentcode,cp.sreportno,cp.nsitecode,"
		  		+ "	s.ssitename,to_char(pm.dtodate, '"+userInfo.getSdatetimeformat()+"'),ts.stransstatus "
		  		+" order by cp.ncoaparentcode;";
	 				
	}
	else if(nRestoreFilterCode == Enumeration.RestoreFilterType.RELEASENO.getnrestorefiltertype())
	{
		
		sQuery="select distinct cp.ncoaparentcode,cp.sreportno,cp.nsitecode,s.ssitename,"
				+ " to_char(pm.dtodate, '"+userInfo.getSdatetimeformat()+"') as stodate,ts.stransstatus "
				+ " from purge_coaparent cp,purge_coachild cc,site s,lims_purg_master_tables_transaction lt,"
				+" purgemaster pm,transactionstatus ts,purge_releasehistory rh "
				+ " where cp.ncoaparentcode = cc.ncoaparentcode and cp.ncoaparentcode = lt.nsequenceno "
				+" and cc.ncoaparentcode = lt.nsequenceno and cp.nsitecode = s.nsitecode and "
				+" s.nsitecode = lt.nsequencenocomposite and cp.nsitecode = lt.nsequencenocomposite and "
				+" lt.nsequenceno > 0 and cp.ncoaparentcode = rh.ncoaparentcode and "
				+" rh.ntransactionstatus = "+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+" and "
				+" rh.ntransactionstatus = ts.ntranscode and cp.nsitecode = "+nSiteCode+" and "
				+ " cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " lt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " pm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " rh.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+" lt.npurgmastercode = pm.npurgemastercode and cp.sreportno = '"+sReleaseNo
				+"' group by cp.ncoaparentcode,cp.sreportno,cp.nsitecode,"
				+" s.ssitename,to_char(pm.dtodate, '"+userInfo.getSdatetimeformat()+"'),ts.stransstatus "
				+" order by cp.ncoaparentcode;";
	}
	else
	{
		sQuery="select distinct COALESCE(sreportno,'') sreportno from purge_coaparent cp,purge_coachild cc,"
				+" purge_registrationarno ra where ra.npreregno = cc.npreregno and "
				+ " cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			    + " cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			    + " ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+" cc.ncoaparentcode = cp.ncoaparentcode and ra.sarno = '"+sARNo+"';";
		LOGGER.info("Create RestoreIndividual sTempQuery15 : "+sQuery);
		LOGGER.info("Create RestoreIndividual sTempQuery15 : "+LocalDateTime.now());
		List<String> lstCoaParent = jdbcTemplate.queryForList(sQuery, String.class);
		LOGGER.info("Create RestoreIndividual sTempQuery16 : "+LocalDateTime.now());
		 String sReportNO=	 lstCoaParent.stream().collect(Collectors.joining(","));
		 LOGGER.info("Create RestoreIndividual sTempQuery17 : "+LocalDateTime.now());
		 if(sReportNO.length()==0)
		 {
			 sReportNO="''";
		 }

		sQuery="select distinct cp.ncoaparentcode,cp.sreportno,cp.nsitecode,s.ssitename,"
				+ " to_char(pm.dtodate, '"+userInfo.getSdatetimeformat()+"') as stodate,ts.stransstatus "
				+ " from purge_coaparent cp,purge_coachild cc,site s,lims_purg_master_tables_transaction lt,"
				+" purgemaster pm,transactionstatus ts,purge_releasehistory rh "
				+ " where cp.ncoaparentcode = cc.ncoaparentcode and cp.ncoaparentcode = lt.nsequenceno and "
				+" cc.ncoaparentcode = lt.nsequenceno and cp.nsitecode = s.nsitecode and "
				+" s.nsitecode = lt.nsequencenocomposite and cp.nsitecode = lt.nsequencenocomposite and "
				+" lt.nsequenceno > 0 and cp.ncoaparentcode = rh.ncoaparentcode and "
				+" rh.ntransactionstatus = "+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
				+" and rh.ntransactionstatus = ts.ntranscode and cp.nsitecode = "+nSiteCode+" and "
				+ " cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " s.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " lt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " pm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " rh.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+" lt.npurgmastercode = pm.npurgemastercode and cp.sreportno in ('"+sReportNO+"') group "
				+" by cp.ncoaparentcode,cp.sreportno,cp.nsitecode,"
				+" s.ssitename,to_char(pm.dtodate, '"+userInfo.getSdatetimeformat()+"'),ts.stransstatus "
				+" order by cp.ncoaparentcode;";
	}
	LOGGER.info("Create RestoreIndividual sTempQuery17 sQuery: "+sQuery);
	LOGGER.info("Create RestoreIndividual sTempQuery17 sQuery: "+LocalDateTime.now());
		final List<Map<String, Object>> lstRestoreIndividual = jdbcTemplate.queryForList(sQuery);
		LOGGER.info("Create RestoreIndividual sTempQuery18 sQuery: "+LocalDateTime.now());
		
		returnMap.put("RestoreIndividual", lstRestoreIndividual);
		String scoaparentcode = "-1";
		String ssitecode = "-1";
		if(lstRestoreIndividual.size()>0)
		{
			bFlag=true;
		returnMap.put("selectedRestoreIndividual", Arrays.asList(lstRestoreIndividual.get(lstRestoreIndividual.size()-1)));
		scoaparentcode = ((Map<String, Object>)lstRestoreIndividual.get(lstRestoreIndividual.size()-1)).get("ncoaparentcode").toString();
		ssitecode = ((Map<String, Object>)lstRestoreIndividual.get(lstRestoreIndividual.size()-1)).get("nsitecode").toString();
		}
		else
		{
			bFlag=false;
			returnMap.put("selectedRestoreIndividual", null);
		}
		List<Map<String, Object>> lstRestoreSample=new ArrayList<Map<String,Object>>();
		if(scoaparentcode!="-1")
		{
	
			sQuery="select cp.ncoaparentcode,cc.npreregno,cc.ntransactionsamplecode,cc.ntransactiontestcode, "
					+ "r.jsondata->'Sample Name'->>'label' ssamplename,rs.jsondata->>'Sample Name_child' ssubsample,"
					+ "rt.jsondata->>'stestname' stestname,rp.jsondata->>'sparametersynonym' sparametername,"
					+ "ra.sarno,rt.jsondata->>'ssectionname' ssectionname,g.sgradename,rp.jsondata->>'sfinal' sresult,"
					+ " (select case when (rstv.jsondata->>'nneedsubsample')::text='true' then  "
					+ " (select ssamplearno from Purge_registrationsamplearno prsa where  prsa.npreregno = r.npreregno) "
					+ " else '-' end from regsubtypeconfigversion rstv where "
					+ " r.nregsubtypeversioncode = rstv.nregsubtypeversioncode) samplearno  "
					+ "from Purge_releaseparameter rp "
					+ "JOIN purge_registrationtest rt ON rp.npreregno = rt.npreregno and "
					+ "rp.ntransactiontestcode = rt.ntransactiontestcode and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+" and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN purge_coachild cc ON rt.npreregno = cc.npreregno and "
					+ "rt.ntransactionsamplecode = cc.ntransactionsamplecode and "
					+ "rt.ntransactiontestcode = cc.ntransactiontestcode and cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN purge_registrationsample rs ON cc.npreregno = rs.npreregno and "
					+ "cc.ntransactionsamplecode = rs.ntransactionsamplecode and rs.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN purge_registrationarno ra ON rs.npreregno = ra.npreregno and ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN purge_registration r ON ra.npreregno = r.npreregno and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN purge_coaparent cp ON cc.ncoaparentcode = cp.ncoaparentcode and cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN grade g ON rp.ngradecode = g.ngradecode and g.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " where cp.ncoaparentcode =  "+scoaparentcode+" and cp.nsitecode = "+ssitecode+";";
			
			LOGGER.info("Create RestoreIndividual sTempQuery19 sQuery: "+sQuery);
			LOGGER.info("Create RestoreIndividual sTempQuery19 sQuery: "+LocalDateTime.now());
		      lstRestoreSample = jdbcTemplate.queryForList(sQuery);
		      LOGGER.info("Create RestoreIndividual sTempQuery20 sQuery: "+LocalDateTime.now());
		}
			
		returnMap.put("RestoreSample", lstRestoreSample);
		
		if(lstRestoreSample.size()>0)
		{
			bFlag=true;
		returnMap.put("selectedRestoreSample", Arrays.asList(lstRestoreSample.get(lstRestoreSample.size()-1)));
		}
		else
		{
			bFlag=false;
			returnMap.put("selectedRestoreSample", null);
		}
		if(bFlag)
		{
		returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		}
		else
		{
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.NODATAFOUND.getreturnstatus());
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getRegistrationSampleDetails(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final Map<String, Object> returnMap = new HashMap<>();
		int nPreregNo = (int) inputMap.get("npreregno");
		
		String sQuery="select r.jsonuidata,ra.sarno,cp.sreportno from purge_registration r,"
				+" purge_registrationarno ra,purge_coachild cc,purge_coaparent cp "
				+ " where r.npreregno = ra.npreregno and ra.npreregno = cc.npreregno and "
				+" r.npreregno = cc.npreregno and cc.ncoaparentcode = cp.ncoaparentcode and "
				+ " r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " ra.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " cc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+" r.npreregno = "+nPreregNo+" group by r.jsonuidata,ra.sarno,cp.sreportno";
		final List<Map<String, Object>> lstRegSample = jdbcTemplate.queryForList(sQuery);
		returnMap.put("RegistrationSample", lstRegSample);
		
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
	
}
