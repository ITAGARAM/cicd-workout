package com.agaramtech.qualis.archivalandpurging.purge.service;

import java.util.Date;  
//import java.text.DateFormat;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.HashMap;
import java.util.List;
//import java.util.Locale;
import java.util.Map;
//import java.util.stream.Collectors;

//import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.archivalandpurging.purge.model.PurgeMaster;
//import com.agaramtech.qualis.archivalandpurging.restoremaster.model.RestoreMaster;
//import com.agaramtech.configuration.pojo.DeleteValidation;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
//import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
//import com.agaramtech.qualis.global.ValidatorDel;
//import com.agaramtech.qualis.scheduler.model.ScheduleMaster;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.ibm.icu.text.SimpleDateFormat;
import java.text.SimpleDateFormat;
import lombok.AllArgsConstructor;

/**
 *  This class is used to perform CRUD Operation on "purgemaster" table by implementing methods from its interface. 
 */
@AllArgsConstructor
@Repository
public class PurgeMasterDAOImpl implements PurgeMasterDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PurgeMasterDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	//private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	//private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	//private static int dataBaseType = 0;
	
	/**
	 * This method is used to retrieve list of all active purgemaster for the specified site.
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active purgemaster
	 * @throws Exception that are thrown from this DAO layer
	 */
	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getPurge(UserInfo userInfo) throws Exception {
		String strQuery = "select l.npurgemastercode,l.npurgesitecode,l.dfromdate,l.dtodate,l.noffsetdfromdate,l.nfromdatetimezonecode,l.noffsetdtodate,l.ntodatetimezonecode,"
				+ "l.nretrycount,l.ntransactionstatus,l.sdescription,l.dmodifieddate,l.noffsetdmodifieddate,l.nmodifieddatetimezonecode,l.nmodifiedby,l.nsitecode,l.nstatus,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus,"
				+ " to_char(l.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate,"
		     	+ " to_char(l.dfromdate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as sfromdate,"
				+ " to_char(l.dtodate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as stodate,"
			    +" s.ssitename,u.sfirstname ||' '||u.slastname smodifiedby "
				+ " from PurgeMaster l,transactionstatus ts,site s,users u "
				+ " where l.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nmastersitecode = "+userInfo.getNmastersitecode()
				+" and u.nsitecode = "+userInfo.getNmastersitecode()
				+ " and l.npurgemastercode > 0 and ts.ntranscode=l.ntransactionstatus and l.npurgesitecode = s.nsitecode and "
				+ " l.nmodifiedby = u.nusercode and  l.nsitecode="+userInfo.getNmastersitecode();
		LOGGER.info("Get Method:"+ strQuery);
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<PurgeMaster> purgeList = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new PurgeMaster()),
						Arrays.asList("sfromdate","stodate","smodifieddate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<PurgeMaster>>() {
				});
		
		return new ResponseEntity<>(purgeList, HttpStatus.OK);
		
	}

	
	/**
	 * This method is used to retrieve active purgemaster object based on the specified npurgemastercode.
	 * @param npurgemastercode [PurgeMaster] primary key of purgemaster object
	 * @return response entity  object holding response status and data of purgemaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	//@Override
	public PurgeMaster getPurgeMasterById(int npurgemastercode, UserInfo userInfo) throws Exception {
		final String strQuery = "select l.npurgemastercode,l.npurgesitecode,l.dfromdate,l.dtodate,l.noffsetdfromdate,l.nfromdatetimezonecode,l.noffsetdtodate,l.ntodatetimezonecode,"
				+ "l.nretrycount,l.ntransactionstatus,l.sdescription,l.dmodifieddate,l.noffsetdmodifieddate,l.nmodifieddatetimezonecode,l.nmodifiedby,l.nsitecode,l.nstatus,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus,"
				+ " to_char(l.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate,"
				+ " to_char(l.dfromdate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as sfromdate,"
				+ " to_char(l.dtodate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as stodate,"
				+" s.ssitename,u.sfirstname ||' '||u.slastname smodifiedby "
				+ " from PurgeMaster l,transactionstatus ts,site s,users u "
				+ " where l.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.ntranscode=l.ntransactionstatus and l.npurgesitecode = s.nsitecode and "
				+ " l.nmodifiedby = u.nusercode and l.npurgemastercode = " + npurgemastercode;
		
		return (PurgeMaster) jdbcUtilityFunction.queryForObject(strQuery, PurgeMaster.class, jdbcTemplate);
	}

	
	
	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getActivePurgeMasterById(int npurgemastercode, UserInfo userInfo) throws Exception {
		
		final PurgeMaster purgemaster =  getPurgeMasterById(npurgemastercode, userInfo);
//		final List<String> multilingualIDList  = new ArrayList<>();
//		
//		final List<Object> listAfterUpdate = new ArrayList<>();		
//		final List<Object> listBeforeUpdate = new ArrayList<>();	
	
		if (purgemaster == null) 
		{
			//status code:205
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else if(purgemaster.getNtransactionstatus()!=Enumeration.TransactionStatus.DRAFT.gettransactionstatus())
		{
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTRECORD.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else 
		{
			final String strQuery = "select l.npurgemastercode,l.npurgesitecode,l.dfromdate,l.dtodate,l.noffsetdfromdate,l.nfromdatetimezonecode,l.noffsetdtodate,l.ntodatetimezonecode,"
					+ "l.nretrycount,l.ntransactionstatus,l.sdescription,l.dmodifieddate,l.noffsetdmodifieddate,l.nmodifieddatetimezonecode,l.nmodifiedby,l.nsitecode,l.nstatus,"
					+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus,"
					+ " to_char(l.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate,"
					+ " to_char(l.dfromdate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as sfromdate,"
					+ " to_char(l.dtodate, 'dd/MM/yyyy') as stodate,"
				    + " s.ssitename,u.sfirstname ||' '||u.slastname smodifiedby "
					+ " from PurgeMaster l,transactionstatus ts,site s,users u "
					+ " where l.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts.ntranscode=l.ntransactionstatus and l.npurgesitecode = s.nsitecode and "
					+ " l.nmodifiedby = u.nusercode and l.npurgemastercode = " + npurgemastercode;
			
			return new ResponseEntity<>((PurgeMaster) jdbcUtilityFunction.queryForObject(strQuery, PurgeMaster.class,jdbcTemplate), HttpStatus.OK);
						
		}
	}

	
	/**
	 * This method is used to add a new entry to purgemaster table.
	 * Need to check for duplicate entry of todate for the specified site before saving into database. 
	 * @param objPurgeMaster [PurgeMaster] object holding details to be added in purgemaster table
	 * @return inserted purgemaster object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createPurgeMaster(PurgeMaster objPurgeMaster, UserInfo userInfo) throws Exception {
		
		final String sQuery = " lock  table purgemaster "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		LOGGER.info("Create Purgemaster function Start");
		LOGGER.info("Create Purgemaster Lock table");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedlimspurgmasterList = new ArrayList<>();
		
		final List<String> lstDateField = new ArrayList<>();
		final List<String> lstDatecolumn = new ArrayList<>();
		final List<String> lstTZcolumn = new ArrayList<>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		LOGGER.info("Create Purgemaster -1");
		final int nsitecode = objPurgeMaster.getNpurgesitecode();
		String strDate = objPurgeMaster.getStodate();
		 strDate=strDate.substring(0,strDate.length()-1);
		 strDate=strDate.replace('T',' ');
		 String strPurgeDate=strDate.substring(0,10);
		 LOGGER.info("Create Purgemaster strDate : "+strDate);
		 String strTempToDate=strDate.substring(0,10);
		 LOGGER.info("Create Purgemaster strTempToDate : "+strTempToDate);
		 String strcurrentdate=dateUtilityFunction.getCurrentDateTime(userInfo).toString();
		 LOGGER.info("Create Purgemaster strcurrentdate : "+strcurrentdate);
		 strcurrentdate=strcurrentdate.substring(0,10);
		 LOGGER.info("Create Purgemaster strcurrentdate1 : "+strcurrentdate);
		 
		 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		 LOGGER.info("Create Purgemaster formatter : "+formatter);
		 Date dtodate = formatter.parse(strTempToDate);
		 LOGGER.info("Create Purgemaster dtodate1 : "+dtodate);
		 Date dcurrentdate = formatter.parse(strcurrentdate);
		 LOGGER.info("Create Purgemaster currentdate : "+dcurrentdate);
		 if(!(dtodate.before(dcurrentdate)) || (dtodate.equals(dcurrentdate)))
		 {
			 LOGGER.info("Create Purgemaster SELECTLESSTHANCURRENTDATE : ");
			 return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTLESSTHANCURRENTDATE.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		 }
		 
		 final String queryString = "select npurgemastercode from purgemaster where dtodate = '" + stringUtilityFunction.replaceQuote(strDate) 
			+ "' and npurgesitecode = " + nsitecode + " and  nstatus = " 
			+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<PurgeMaster> purgeList =  jdbcTemplate.query(queryString, new PurgeMaster());
			LOGGER.info("Create Purgemaster queryString : " +queryString);
			LOGGER.info("Create Purgemaster purgeList : ");
        if(purgeList.isEmpty())
        {
        	List<Map<String, Object>> limsPurgeMasterListByDate = (List<Map<String, Object>>) getLimsPurgeMasterListByDate(strDate,nsitecode, objPurgeMaster.getNsitecode()).getBody();
        	LOGGER.info("Create Purgemaster limsPurgeMasterListByDate : ");
			if (!limsPurgeMasterListByDate.isEmpty()) 
			{	
				LOGGER.info("Create Purgemaster limsPurgeMasterListByDate1 : ");
				if (objPurgeMaster.getStodate() != null) {
					
					lstDateField.add("stodate");
					lstDatecolumn.add(objPurgeMaster.getStztodate());
					lstTZcolumn.add("ntodatetimezonecode");
					if(userInfo.getNtimezonecode()>0)
					{
						objPurgeMaster.setNoffsetdtodate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(objPurgeMaster.getStodate(),userInfo.getStimezoneid()));
					}
				}
				
				final PurgeMaster convertedObject = objMapper.convertValue(
						dateUtilityFunction.convertInputDateToUTCByZone(objPurgeMaster, lstDateField, lstTZcolumn, true, userInfo),
						new TypeReference<PurgeMaster>() {
						});
				LOGGER.info("Create Purgemaster limsPurgeMasterListByDate2 : ");
				 String sequencenoquery ="select nsequenceno from seqnobackupmanagement where stablename ='purgemaster' and nstatus = 1";
			   		int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			   		nsequenceno++;
			   		LOGGER.info("Create Purgemaster nsequenceno : "+nsequenceno);
			   		String insertquery ="Insert into purgemaster (npurgemastercode,npurgesitecode,dfromdate,dtodate,nretrycount,ntransactionstatus,sdescription,dmodifieddate,nsitecode,nstatus,nmodifiedby,noffsetdfromdate,nfromdatetimezonecode,noffsetdtodate,ntodatetimezonecode,noffsetdmodifieddate,nmodifieddatetimezonecode) "
			   				+ " values(" + nsequenceno + ","+objPurgeMaster.getNpurgesitecode()+",'" + convertedObject.getStodate() + "','" + convertedObject.getStodate() + "',0,8,N'"+stringUtilityFunction.replaceQuote(objPurgeMaster.getSdescription())+"','"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"+ userInfo.getNmastersitecode() + ",1,"+userInfo.getNusercode() +","+objPurgeMaster.getNoffsetdtodate()
							+ ","+objPurgeMaster.getNtodatetimezonecode()+","+objPurgeMaster.getNoffsetdtodate()+","+objPurgeMaster.getNtodatetimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()+")";
			   		jdbcTemplate.execute(insertquery);
			   		LOGGER.info("Create Purgemaster insertquery : "+insertquery);
			   		String updatequery ="update seqnobackupmanagement set nsequenceno ="+nsequenceno+" where stablename='purgemaster'";
			   		jdbcTemplate.execute(updatequery);
			   		LOGGER.info("Create Purgemaster update nsequenceno : "+nsequenceno);
	
			   		objPurgeMaster.setNpurgemastercode(nsequenceno);
			   		savedlimspurgmasterList.add(objPurgeMaster);
				
				multilingualIDList.add("IDS_ADDPURGEMASTER");
				
				auditUtilityFunction.fnInsertAuditAction(savedlimspurgmasterList, 1, null, multilingualIDList, userInfo);
				LOGGER.info("Create Purgemaster fnInsertAuditAction : ");
				
				
//				String sLockQuery = " lock  table lockcancelreject "
//						+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sLockQuery);
//				
//				sLockQuery = " lock  table lockresultparamcomment "
//						+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sLockQuery);
//				
//				sLockQuery = " lock  table lockresultcorrection "
//						+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sLockQuery);
//				
//				jdbcTemplate.execute("ALTER TABLE public.approvalparameter DISABLE TRIGGER approvalparameter_trigger;");
//				jdbcTemplate.execute("ALTER TABLE public.coachild DISABLE TRIGGER externalorderreleasestatus_trigger;");
//				
//	                 jdbcTemplate.execute("select * from fn_purginginsertseq('"+strPurgeDate+"','"+strPurgeDate+"',"+objPurgeMaster.getNpurgemastercode()+","+objPurgeMaster.getNpurgesitecode()+")");
//				
//				     jdbcTemplate.execute("select * from fn_purginginsert("+objPurgeMaster.getNpurgemastercode()+")");
//				     
//				     
//				     jdbcTemplate.execute("ALTER TABLE public.approvalparameter ENABLE TRIGGER approvalparameter_trigger;");
//					jdbcTemplate.execute("ALTER TABLE public.coachild ENABLE TRIGGER externalorderreleasestatus_trigger;");
				
				return getPurge(userInfo);
			
			} 
			else 
			{
				//Conflict = 409 - Duplicate entry --getSlanguagetypecode
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.RELEASEDSAMPLESNOTAVAILABLEFORPURGE.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
        }
        else
        {
        	return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
        }
	}

	
	

	/**
	 * This method is used to update entry in purgemaster  table.
	 * Need to validate that the purgemaster object to be updated is active before updating details in database.
	 * Need to check for duplicate entry of todate for the specified site before saving into database.
	 * @param objPurgeMaster [PurgeMaster] object holding details to be updated in purgemaster table
	 * @return response entity object holding response status and data of updated purgemaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updatePurgeMaster(PurgeMaster objPurgeMaster,UserInfo userInfo) throws Exception {
		
		final PurgeMaster purgemaster =  getPurgeMasterById(objPurgeMaster.getNpurgemastercode(), userInfo);
		final List<String> multilingualIDList  = new ArrayList<>();
		
		final List<Object> listAfterUpdate = new ArrayList<>();		
		final List<Object> listBeforeUpdate = new ArrayList<>();
		
		List<String> lstDateField = new ArrayList<>();
		List<String> lstDatecolumn = new ArrayList<>();
		List<String> lstTZcolumn = new ArrayList<>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
	
		if (purgemaster == null) 
		{
			//status code:205
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		if(purgemaster.getNtransactionstatus()!=Enumeration.TransactionStatus.DRAFT.gettransactionstatus())
		{
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTRECORD.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else 
		{				

			final int nsitecode = objPurgeMaster.getNpurgesitecode();
			String strDate = objPurgeMaster.getStodate();
			 strDate=strDate.substring(0,strDate.length()-1);
			 strDate=strDate.replace('T',' ');
			 
			 String strTempToDate=strDate.substring(0,10);
			 String strcurrentdate=dateUtilityFunction.getCurrentDateTime(userInfo).toString();
			 strcurrentdate=strcurrentdate.substring(0,10);
			 
			 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

			 Date dtodate = formatter.parse(strTempToDate);
			 Date dcurrentdate = formatter.parse(strcurrentdate);

			 if(!(dtodate.before(dcurrentdate)) || (dtodate.equals(dcurrentdate)))
			 {
				 return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.NODATAAVAILABLEINLIMS.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			 }

			 final String queryString = "select npurgemastercode from purgemaster where dtodate = '" + stringUtilityFunction.replaceQuote(strDate) 
				+ "' and npurgesitecode = " + nsitecode + " and npurgemastercode <> " + objPurgeMaster.getNpurgemastercode() + " and  nstatus = " 
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final List<PurgeMaster> purgeList =  jdbcTemplate.query(queryString, new PurgeMaster());
	        if(purgeList.isEmpty())
	        {
	        	 
	            List<Map<String, Object>> limsPurgeMasterListByDate = (List<Map<String, Object>>) getLimsPurgeMasterListByDate(strDate,nsitecode, objPurgeMaster.getNsitecode()).getBody();
			if (!limsPurgeMasterListByDate.isEmpty()) 
			{	
				
				if (objPurgeMaster.getStodate() != null) {
					
					lstDateField.add("stodate");
					lstDatecolumn.add(objPurgeMaster.getStztodate());
					lstTZcolumn.add("ntodatetimezonecode");
					if(userInfo.getNtimezonecode()>0)
					{
						objPurgeMaster.setNoffsetdtodate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(objPurgeMaster.getStodate(),userInfo.getStimezoneid()));
					}
				}
				
				final PurgeMaster convertedObject = objMapper.convertValue(
						dateUtilityFunction.convertInputDateToUTCByZone(objPurgeMaster, lstDateField, lstTZcolumn, true, userInfo),
						new TypeReference<PurgeMaster>() {
						});
				
				final String updateQueryString = "update purgemaster set dfromdate='" 
					+ (convertedObject.getStodate()) 
					+ "',dtodate='"+(convertedObject.getStodate())+"', sdescription ='" + stringUtilityFunction.replaceQuote(objPurgeMaster.getSdescription()) 
					+"',dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nmodifiedby = "+userInfo.getNusercode()+",npurgesitecode="+objPurgeMaster.getNpurgesitecode()
					+",noffsetdfromdate="+objPurgeMaster.getNoffsetdtodate()
					+",nfromdatetimezonecode="+objPurgeMaster.getNtodatetimezonecode()+",noffsetdtodate="+objPurgeMaster.getNoffsetdtodate()
					+",ntodatetimezonecode="+objPurgeMaster.getNtodatetimezonecode()+",noffsetdmodifieddate="+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+",nmodifieddatetimezonecode="+userInfo.getNtimezonecode()+" where npurgemastercode=" + objPurgeMaster.getNpurgemastercode()+";";
								
				jdbcTemplate.execute(updateQueryString);
				
				listAfterUpdate.add(objPurgeMaster);
				listBeforeUpdate.add(purgemaster);
				
				multilingualIDList.add("IDS_EDITPURGEMASTER");				
									
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);
				
				return getPurge(userInfo);
			} 
			else 
			{	
				//Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.RELEASEDSAMPLESNOTAVAILABLEFORPURGE.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
	        }
	        else
	        {
	        	return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
	        }
		}
	}

	/**
	 * This method id used to delete an entry in PurgeMaster table
	 * Need to check the record is already deleted or not
	 * @param objPurgeMaster [PurgeMaster] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an purgemaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deletePurgeMaster(PurgeMaster objPurgeMaster,UserInfo userInfo) throws Exception {
		final PurgeMaster purgemaster =  getPurgeMasterById(objPurgeMaster.getNpurgemastercode(), userInfo);
		
		if (purgemaster == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else if(purgemaster.getNtransactionstatus()!=Enumeration.TransactionStatus.DRAFT.gettransactionstatus())
		{
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTRECORD.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
							
			
				final List<String> multilingualIDList  = new ArrayList<>();
				final List<Object> deletedLimsPurgMasterList = new ArrayList<>();
				final String updateQueryString = "update purgemaster set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
				 + ",nmodifiedby = "+userInfo.getNusercode()+",noffsetdmodifieddate="+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+",nmodifieddatetimezonecode="+userInfo.getNtimezonecode()+" where npurgemastercode=" + objPurgeMaster.getNpurgemastercode();
					
			    jdbcTemplate.execute(updateQueryString);
			    objPurgeMaster.setNstatus( (short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
							
			    deletedLimsPurgMasterList.add(objPurgeMaster);						
			    multilingualIDList.add("IDS_DELETEPURGEMASTER");
				
				auditUtilityFunction.fnInsertAuditAction(deletedLimsPurgMasterList, 1, null, multilingualIDList, userInfo);
				return getPurge(userInfo);
			
		}
	}

	@Override
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception {
		final String strQuery = "select nsitecode as npurgesitecode,ssitename from site  where nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode > 0  and nmastersitecode = " + userInfo.getNmastersitecode();
		return new ResponseEntity<>( jdbcTemplate.query(strQuery, new Site()),HttpStatus.OK);		
	}
	
	
	/**
	 * This method is used to fetch the active limspurgmaster objects for the specified todate and site.
	 * @param dtodate [String] date of the limspurgmaster
	 * @param nmasterSiteCode [int] site code of the limspurgmaster
	 * @return list of active purgemaster code(s) based on the specified todate and site
	 * @throws Exception 
	 */
	private ResponseEntity<Object> getLimsPurgeMasterListByDate(final String stodate,final int nsitecode,final int nmasterSiteCode) throws Exception {		

		//Map<String, Object> returnMap = new HashMap<>();
		String strQuery="select (select max(npurgmastertabletransactioncode) from  Lims_purg_master_tables_transaction)+RANK()OVER ( "
				+ " ORDER BY ncoaparentcode ASC "
				+ " ) as npurgmastertabletransactioncode, "
				+ " 1 as npurgmastercode ,1 as npurgmastertablecode,ncoaparentcode,nsitecode,CURRENT_TIMESTAMP,ROW_TO_JSON(a.*),-1 as nmastersitecode,1 as nstatus "
				+ " from (select c.ncoaparentcode,max(c.nregtypecode) nregtypecode,max(c.nregsubtypecode) nregsubtypecode,max(c.napproveconfversioncode) napproveconfversioncode, "
				+ " max(c.ncoareporttypecode) ncoareporttypecode,max(c.nreporttemplatecode) nreporttemplatecode,max(c.ntransactionstatus) ntransactionstatus, "
				+ " max(c.nenteredby) nenteredby,max(c.nenteredrole) nenteredrole,max(c.ndeputyenteredby) ndeputyenteredby,max(c.ndeputyenteredrole) ndeputyenteredrole, "
				+ " max(c.dmodifieddate) dmodifieddate,max(c.nsitecode) nsitecode,max(c.nstatus) nstatus,max(c.sreportno) sreportno,max(cr.dtransactiondate) dtransactiondate,max(cr.nsitecode) nnsitecode from ( "
				+ " select r.npreregno,count(r.ntransactiontestcode) testcount " 
				+ "	from registrationtest r,coachild c ,coaparent cp,registrationtesthistory rth "
				+ " where c.ncoaparentcode=cp.ncoaparentcode and rth.ntransactionstatus="+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+" and r.npreregno = rth.npreregno and "
				+ "	r.nsitecode=rth.nsitecode and r.ntransactiontestcode=rth.ntransactiontestcode and r.npreregno = c.npreregno "
				+ " and cp.ntransactionstatus="+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+" and r.ntransactiontestcode=c.ntransactiontestcode and "
				+ " c.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "	and r.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and cp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				+ " rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rth.ntesthistorycode=any "
				+ " (select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ " where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by ntransactiontestcode,npreregno) group by r.npreregno)historycount, "
				+ " (select rth.npreregno,count(ntesthistorycode) testcount "
				+ " from registrationtest rt,registrationtesthistory rth "
				+ " where ntransactionstatus not in ("+Enumeration.TransactionStatus.CANCELED.gettransactionstatus()+","+Enumeration.TransactionStatus.REJECTED.gettransactionstatus()+","+Enumeration.TransactionStatus.RETEST.gettransactionstatus()+") and rt.nsitecode=rth.nsitecode "
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and rth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and rth.ntesthistorycode = any( "
				+ " select max(ntesthistorycode) ntesthistorycode from registrationtesthistory "
				+ " where nstatus=1 group by ntransactiontestcode,npreregno) "
				+ " group by rth.npreregno) testcount,coaparent c,coachild ch,registrationarno arno,registration r,registrationhistory rh,releasehistory cr "
				+ " where historycount.npreregno=testcount.npreregno "
				+ " and historycount.testcount=testcount.testcount and r.npreregno=rh.npreregno and historycount.npreregno=r.npreregno and "
				+ " rh.nreghistorycode =any(select max(rh1.nreghistorycode) from registrationhistory rh1 "
				+ " group by npreregno) and rh.ntransactionstatus="+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+" and r.npreregno=arno.npreregno and r.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and ch.npreregno=r.npreregno and c.ncoaparentcode=ch.ncoaparentcode "
				+ " and cr.nreleasehistorycode =any(select max(nreleasehistorycode) from releasehistory group by ncoaparentcode) "
				+ " and c.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ch.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and arno.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rh.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cr.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cr.ntransactionstatus="+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+" and cr.ncoaparentcode=c.ncoaparentcode group by c.ncoaparentcode) a "
				+ " where a.nsitecode = "+nsitecode+" and TO_CHAR(a.dtransactiondate, 'yyyy-mm-dd') <= to_char('"+stodate+"'::timestamp without time zone,'yyyy-mm-dd')";
						
						
		List<Map<String, Object>> lstRecordCount = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(lstRecordCount, HttpStatus.OK);
	}
	
//private List<PurgeMaster> getLimsPurgMasterListByDateForUpdate(int npurgmastercode,String stodate, int nmasterSiteCode) throws Exception {		
//
//		
//		//String strQuery = "select max(dtodate) dtodate from public.lims_purg_master where ntransactionstatus = "+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
//	String strQuery ="select dtodate from public.lims_purg_master where nstatus = 1 and dtodate = '"+stodate+"' and npurgmastercode !="+npurgmastercode;
//	//String sexitdate = jdbcTemplate.queryForObject(strQuery, String.class);
//	List<Map<String, Object>> lstExitDate = getJdbcTemplate().queryForList(strQuery);
//	if(lstExitDate!=null && lstExitDate.size()>0)
//	{
//		
//		strQuery="select * from public.lims_purg_master where dtodate = '"+stodate+"'";
//		return (List<PurgeMaster>) jdbcTemplate.query(strQuery, new PurgeMaster());
//		
//	    
//	}
//	else
//	{
//		
//		strQuery = "select max(dtodate) dtodate from public.lims_purg_master where nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and npurgmastercode !="+npurgmastercode+";";
//		String sMaxdate = jdbcTemplate.queryForObject(strQuery, String.class);
//		if(sMaxdate.length()>0)
//		{
//			
//			 Date dMaxdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sMaxdate);
//			 
//			Date dpurgdate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stodate);
//			
//			
//		
//		  if(dpurgdate.compareTo(dMaxdate) > 0)   
//		  {  
//			 strQuery="select * from public.lims_purg_master where dtodate = '"+stodate+"' and npurgmastercode !="+npurgmastercode+";";
//			 return (List<PurgeMaster>) jdbcTemplate.query(strQuery, new PurgeMaster());  
//		  }   
//		  else if(dpurgdate.compareTo(dMaxdate) < 0)   
//		  {  
//			 strQuery="select * from public.lims_purg_master where dtodate = '"+sMaxdate+"'";
//			 return (List<PurgeMaster>) jdbcTemplate.query(strQuery, new PurgeMaster());  
//		  }   
//		  else //if(dpurgdate.compareTo(dMaxdate) == 0)   
//		  {  
//			 strQuery="select * from public.lims_purg_master where dtodate = '"+stodate+"'";
//			 return (List<PurgeMaster>) jdbcTemplate.query(strQuery, new PurgeMaster());    
//		  }  
//		
//		}
//		else
//		{
//			strQuery="select * from public.lims_purg_master where dtodate = '"+stodate+"'";
//			return (List<PurgeMaster>) jdbcTemplate.query(strQuery, new PurgeMaster());
//		}
//		
//		
//		
//	}
//
//
//}
}
