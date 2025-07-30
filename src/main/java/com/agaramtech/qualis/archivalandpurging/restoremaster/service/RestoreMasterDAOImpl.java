package com.agaramtech.qualis.archivalandpurging.restoremaster.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
//import java.util.Date;  
//import java.text.DateFormat;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
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

//import com.agaramtech.basemaster.pojo.SeqNoBasemaster;
import com.agaramtech.qualis.archivalandpurging.restoremaster.model.RestoreMaster;
import com.agaramtech.qualis.archivalandpurging.purge.model.PurgeMaster;
import com.agaramtech.qualis.credential.model.Site;
//import com.agaramtech.configuration.pojo.DeleteValidation;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
//import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
//import com.agaramtech.qualis.global.ValidatorDel;
//import com.agaramtech.product.pojo.EBCFactor;
//import com.agaramtech.scheduler.pojo.Schedulemaster;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.ibm.icu.text.SimpleDateFormat;
//import java.text.SimpleDateFormat;
import lombok.AllArgsConstructor;

/**
 *  This class is used to perform CRUD Operation on "restoremaster" table by implementing methods from its interface. 
 * @author ATE113
 * @version 9.0.0.1
 * @since 17 - Sep -2024
 */
@AllArgsConstructor
@Repository
public class RestoreMasterDAOImpl implements RestoreMasterDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RestoreMasterDAOImpl.class);
	
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
	 * This method is used to retrieve list of all active restoremaster for the specified site.
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active restoremaster
	 * @throws Exception that are thrown from this DAO layer
	 */
	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getRestoreMaster(UserInfo userInfo) throws Exception {
		//final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String strQuery = "select l.nrestoremastercode,l.nrestoresitecode,l.npurgemastercode,l.dfromdate,l.dtodate,l.noffsetdfromdate,l.nfromdatetimezonecode,l.noffsetdtodate,l.ntodatetimezonecode,"
				+ "l.nretrycount,l.ntransactionstatus,l.sdescription,l.dmodifieddate,l.noffsetdmodifieddate,l.nmodifieddatetimezonecode,l.nmodifiedby,l.nsitecode,l.nstatus,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus,"
				+ " to_char(l.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate,"
				+ " to_char(l.dfromdate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as sfromdate,"
				+ " to_char(l.dtodate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as stodate,"
				+" s.ssitename,u.sfirstname ||' '||u.slastname smodifiedby "
				+ " from RestoreMaster l,transactionstatus ts,site s,users u "
				+ " where l.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nmastersitecode = "+userInfo.getNmastersitecode()
				+ " and u.nsitecode = "+userInfo.getNmastersitecode()
				+ " and l.nrestoremastercode > 0 and ts.ntranscode=l.ntransactionstatus and l.nrestoresitecode = s.nsitecode and "
				+ " l.nmodifiedby = u.nusercode and l.nsitecode="+userInfo.getNmastersitecode();
		
		LOGGER.info("Get Method:"+ strQuery);
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<RestoreMaster> restoreList = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(strQuery, new RestoreMaster()),
						Arrays.asList("sfromdate","stodate","smodifieddate"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<RestoreMaster>>() {
				});
		
		return new ResponseEntity<>(restoreList, HttpStatus.OK);
		

	}

	
	/**
	 * This method is used to retrieve active restoremaster object based on the specified nrestoremastercode.
	 * @param nrestoremastercode [RestoreMaster] primary key of restoremaster object
	 * @return response entity  object holding response status and data of restoremaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	//@Override
	public RestoreMaster getRestoreMasterById(int nrestoremastercode, UserInfo userInfo) throws Exception {
		final String strQuery = "select l.nrestoremastercode,l.nrestoresitecode,l.npurgemastercode,l.dfromdate,l.dtodate,l.noffsetdfromdate,l.nfromdatetimezonecode,l.noffsetdtodate,l.ntodatetimezonecode,"
				+ "l.nretrycount,l.ntransactionstatus,l.sdescription,l.dmodifieddate,l.noffsetdmodifieddate,l.nmodifieddatetimezonecode,l.nmodifiedby,l.nsitecode,l.nstatus, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus,"
				+ " to_char(l.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate,"
				+ " to_char(l.dfromdate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as sfromdate,"
				+ " to_char(l.dtodate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as stodate,"
				+" s.ssitename,u.sfirstname ||' '||u.slastname smodifiedby "
				+ " from RestoreMaster l,transactionstatus ts,site s,users u  "
				+ " where l.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.ntranscode=l.ntransactionstatus and l.nrestoresitecode = s.nsitecode and "
				+ "	l.nmodifiedby = u.nusercode and l.nrestoremastercode = " + nrestoremastercode;
		
		return (RestoreMaster) jdbcUtilityFunction.queryForObject(strQuery, RestoreMaster.class, jdbcTemplate);
	}

	
	
	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getActiveRestoreMasterById(int nrestoremastercode, UserInfo userInfo) throws Exception {
		//final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final RestoreMaster restoremaster =  getRestoreMasterById(nrestoremastercode, userInfo);
		//final List<String> multilingualIDList  = new ArrayList<>();
		
		//final List<Object> listAfterUpdate = new ArrayList<>();		
		//final List<Object> listBeforeUpdate = new ArrayList<>();	
	
		if (restoremaster == null) 
		{
			//status code:205
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else if(restoremaster.getNtransactionstatus()!=Enumeration.TransactionStatus.DRAFT.gettransactionstatus())
		{
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTRECORD.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else 
		{
			final String strQuery = "select l.nrestoremastercode,l.nrestoresitecode,l.npurgemastercode,l.dfromdate,l.dtodate,l.noffsetdfromdate,l.nfromdatetimezonecode,l.noffsetdtodate,l.ntodatetimezonecode,"
					+ "l.nretrycount,l.ntransactionstatus,l.sdescription,l.dmodifieddate,l.noffsetdmodifieddate,l.nmodifieddatetimezonecode,l.nmodifiedby,l.nsitecode,l.nstatus, "
					+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus,"
					+ " to_char(l.dmodifieddate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as smodifieddate,"
					+ " to_char(l.dfromdate, '"+userInfo.getSpgsitedatetime().replace("'T'", " ")+"') as sfromdate,"
					+ " to_char(l.dtodate, 'dd/MM/yyyy') as stodate,"
				    + " s.ssitename,u.sfirstname ||' '||u.slastname smodifiedby "
					+ " from RestoreMaster l,transactionstatus ts,site s,users u   "
					+ " where l.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and u.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts.ntranscode=l.ntransactionstatus and l.nrestoresitecode = s.nsitecode and "
					+ "	l.nmodifiedby = u.nusercode and l.nrestoremastercode = " + nrestoremastercode;
			
			return new ResponseEntity<>((RestoreMaster) jdbcUtilityFunction.queryForObject(strQuery, RestoreMaster.class, jdbcTemplate), HttpStatus.OK);
			
		}
	}

	
	/**
	 * This method is used to add a new entry to restoremaster table.
	 * Need to check for duplicate entry of todate for the specified site before saving into database. 
	 * @param objRestoreMaster [RestoreMaster] object holding details to be added in restoremaster table
	 * @return inserted restoremaster object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createRestoreMaster(RestoreMaster objRestoreMaster, UserInfo userInfo) throws Exception {
		
		final String sQuery = " lock  table restoremaster "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		LOGGER.info("Create Restoremaster function Start");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedrestoremasterList = new ArrayList<>();
		
		final List<String> lstDateField = new ArrayList<>();
		final List<String> lstDatecolumn = new ArrayList<>();
		final List<String> lstTZcolumn = new ArrayList<>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
				
		String strDate = objRestoreMaster.getStodate();
		//strDate=strDate.replace('T',' ');
		
		LOGGER.info("Create Restoremaster strDate : "+strDate);
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = inputFormat.parse(strDate);
        
        // Step 2: Format the date in the desired format "yyyy-MM-dd"
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = outputFormat.format(date);
         
		 objRestoreMaster.setStodate(formattedDate);
     		if (objRestoreMaster.getStodate() != null) {
				lstDateField.add("stodate");
				lstDatecolumn.add(objRestoreMaster.getStztodate());
				lstTZcolumn.add("ntodatetimezonecode");
				LOGGER.info("Create Restoremaster strDate1 : "+strDate);
				if(userInfo.getNtimezonecode()>0)
				{
					objRestoreMaster.setNoffsetdtodate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(objRestoreMaster.getStodate(),userInfo.getStimezoneid()));
				}
			}
			LOGGER.info("Create Restoremaster strDate2 : "+strDate);
			final RestoreMaster convertedObject = objMapper.convertValue(
					dateUtilityFunction.convertInputDateToUTCByZone(objRestoreMaster, lstDateField, lstTZcolumn, true, userInfo),
					new TypeReference<RestoreMaster>() {
					});
			
			 String sequencenoquery ="select nsequenceno from seqnobackupmanagement where stablename ='restoremaster' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		   		int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
		   		nsequenceno++;
		   		LOGGER.info("Create Restoremaster nsequenceno : "+nsequenceno);
		   		String insertquery ="Insert into restoremaster (nrestoremastercode,nrestoresitecode, npurgemastercode,dfromdate,dtodate,nretrycount,ntransactionstatus,sdescription,dmodifieddate,nsitecode,nstatus,nmodifiedby,noffsetdfromdate,nfromdatetimezonecode,noffsetdtodate,ntodatetimezonecode,noffsetdmodifieddate,nmodifieddatetimezonecode) "
		   				+ " values(" + nsequenceno + ","+objRestoreMaster.getNrestoresitecode()+","+objRestoreMaster.getNpurgemastercode()+",'" + convertedObject.getStodate() + "','" + convertedObject.getStodate() + "',0,8,N'"+stringUtilityFunction.replaceQuote(objRestoreMaster.getSdescription())+"','"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"+ userInfo.getNmastersitecode() + ",1,"+userInfo.getNusercode() +","+objRestoreMaster.getNoffsetdtodate()
						+","+objRestoreMaster.getNtodatetimezonecode()+","+objRestoreMaster.getNoffsetdtodate()+","+objRestoreMaster.getNtodatetimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()+")";
		   		LOGGER.info("Create Restoremaster insertquery : "+insertquery);
		   		jdbcTemplate.execute(insertquery);
		   		LOGGER.info("Create Restoremaster insertquery1 : "+insertquery);
		   		String updatequery ="update seqnobackupmanagement set nsequenceno ="+nsequenceno+" where stablename='restoremaster'";
		   		jdbcTemplate.execute(updatequery);
		   		LOGGER.info("Create Restoremaster updatequery : "+updatequery);
		   		
		   		final String updateQueryStringPurg = "update purgemaster set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',noffsetdmodifieddate = "+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
		   		+",nmodifieddatetimezonecode = "+userInfo.getNtimezonecode()+",nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
				+ " where npurgemastercode=" + objRestoreMaster.getNpurgemastercode();

                jdbcTemplate.execute(updateQueryStringPurg);
                LOGGER.info("Create Restoremaster updateQueryStringPurg : "+updateQueryStringPurg);
		   		objRestoreMaster.setNrestoremastercode(nsequenceno);
		   		savedrestoremasterList.add(objRestoreMaster);
			
			multilingualIDList.add("IDS_ADDRESTOREMASTER");
			
			auditUtilityFunction.fnInsertAuditAction(savedrestoremasterList, 1, null, multilingualIDList, userInfo);
			
//			String sLockQuery = " lock  table lockcancelreject "
//					+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sLockQuery);
//			
//
//			sLockQuery = " lock  table lockresultparamcomment "
//					+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sLockQuery);
//			
//			sLockQuery = " lock  table lockresultcorrection "
//					+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sLockQuery);
//			
//			jdbcTemplate.execute("ALTER TABLE public.approvalparameter DISABLE TRIGGER approvalparameter_trigger;");
//			jdbcTemplate.execute("ALTER TABLE public.coachild DISABLE TRIGGER externalorderreleasestatus_trigger;");
//			
//			jdbcTemplate.execute("select * from public.fn_purgingrestoreset("+objRestoreMaster.getNpurgemastercode()+","+objRestoreMaster.getNrestoremastercode()+")");
//			
//			jdbcTemplate.execute("ALTER TABLE public.approvalparameter ENABLE TRIGGER approvalparameter_trigger;");
//			jdbcTemplate.execute("ALTER TABLE public.coachild ENABLE TRIGGER externalorderreleasestatus_trigger;");
			
			
			LOGGER.info("Create Restoremaster Audit : ");
			return getRestoreMaster(userInfo);
			
			
		//} 

	}

	
	

	/**
	 * This method is used to update entry in restoremaster  table.
	 * Need to validate that the restoremaster object to be updated is active before updating details in database.
	 * Need to check for duplicate entry of todate for the specified site before saving into database.
	 * @param objRestoreMaster [RestoreMaster] object holding details to be updated in restoremaster table
	 * @return response entity object holding response status and data of updated restoremaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateRestoreMaster(RestoreMaster objRestoreMaster,UserInfo userInfo) throws Exception {
		
		final RestoreMaster restoremaster =  getRestoreMasterById(objRestoreMaster.getNrestoremastercode(), userInfo);
		final List<String> multilingualIDList  = new ArrayList<>();
		
		final List<Object> listAfterUpdate = new ArrayList<>();		
		final List<Object> listBeforeUpdate = new ArrayList<>();
		
		List<String> lstDateField = new ArrayList<>();
		List<String> lstDatecolumn = new ArrayList<>();
		List<String> lstTZcolumn = new ArrayList<>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
	
		if (restoremaster == null) 
		{
			//status code:205
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else if(restoremaster.getNtransactionstatus()!=Enumeration.TransactionStatus.DRAFT.gettransactionstatus())
		{
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTRECORD.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else 
		{				

			String strDate = objRestoreMaster.getStodate();
			 strDate=strDate.substring(0,strDate.length()-1);
			 strDate=strDate.replace('T',' ');
			 
			//{		
				
				if (objRestoreMaster.getDtodate() != null) {
					objRestoreMaster.setStodate(dateUtilityFunction.instantDateToString(objRestoreMaster.getDtodate()).replace("T", " "));
					lstDateField.add("stodate");
					lstDatecolumn.add(objRestoreMaster.getStztodate());
					lstTZcolumn.add("ntodatetimezonecode");
					if(userInfo.getNtimezonecode()>0)
					{
						objRestoreMaster.setNoffsetdtodate(dateUtilityFunction.getCurrentDateTimeOffsetFromDate(objRestoreMaster.getStodate(),userInfo.getStimezoneid()));
					}
				}
				
				final RestoreMaster convertedObject = objMapper.convertValue(
						dateUtilityFunction.convertInputDateToUTCByZone(objRestoreMaster, lstDateField, lstTZcolumn, true, userInfo),
						new TypeReference<RestoreMaster>() {
						});
				
				final String updateQueryString = "update restoremaster set dfromdate='" 
					+ (convertedObject.getStodate()) 
					+ "',dtodate='"+(convertedObject.getStodate())+"', sdescription ='" + stringUtilityFunction.replaceQuote(objRestoreMaster.getSdescription()) 
					+"',dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nmodifiedby = "+userInfo.getNusercode()+",nrestoresitecode="+objRestoreMaster.getNrestoresitecode()
					+",npurgemastercode="+objRestoreMaster.getNpurgemastercode()+",noffsetdfromdate="+objRestoreMaster.getNoffsetdtodate()
					+",nfromdatetimezonecode="+objRestoreMaster.getNtodatetimezonecode()+",noffsetdtodate="+objRestoreMaster.getNoffsetdtodate()
					+",ntodatetimezonecode="+objRestoreMaster.getNtodatetimezonecode()+",noffsetdmodifieddate="+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+",nmodifieddatetimezonecode="+userInfo.getNtimezonecode()+" where nrestoremastercode=" + objRestoreMaster.getNrestoremastercode()+";";
				
				jdbcTemplate.execute(updateQueryString);
				
				listAfterUpdate.add(objRestoreMaster);
				listBeforeUpdate.add(restoremaster);
				
				multilingualIDList.add("IDS_EDITRESTOREMASTER");				
									
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);
				//status code:200
				//return new ResponseEntity<>(objUnit, HttpStatus.OK);
				return getRestoreMaster(userInfo);
			//} 

		}
	}

	/**
	 * This method id used to delete an entry in RestoreMaster table
	 * Need to check the record is already deleted or not
	 * @param objRestoreMaster [RestoreMaster] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an restoremaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteRestoreMaster(RestoreMaster objRestoreMaster,UserInfo userInfo) throws Exception {
		final RestoreMaster restoremaster =  getRestoreMasterById(objRestoreMaster.getNrestoremastercode(), userInfo);
		
		if (restoremaster == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else if(restoremaster.getNtransactionstatus()!=Enumeration.TransactionStatus.DRAFT.gettransactionstatus())
		{
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.SELECTDRAFTRECORD.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
				
			//if(validRecord) {
				final List<String> multilingualIDList  = new ArrayList<>();
				final List<Object> deletedRestoreMasterList = new ArrayList<>();
				final String updateQueryString = "update restoremaster set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
												 + ",nmodifiedby = "+userInfo.getNusercode()+",noffsetdmodifieddate="+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+",nmodifieddatetimezonecode="+userInfo.getNtimezonecode()+" where nrestoremastercode=" + objRestoreMaster.getNrestoremastercode();
					
			    jdbcTemplate.execute(updateQueryString);
			    
			    final String updateQueryStringPurg = "update purgemaster set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " ,nmodifiedby = "+userInfo.getNusercode()+",noffsetdmodifieddate="+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+",nmodifieddatetimezonecode="+userInfo.getNtimezonecode()+" where npurgemastercode=" + objRestoreMaster.getNpurgemastercode();

                jdbcTemplate.execute(updateQueryStringPurg);

			    objRestoreMaster.setNstatus( (short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
							
			    deletedRestoreMasterList.add(objRestoreMaster);						
			    multilingualIDList.add("IDS_DELETERESTOREMASTER");
				auditUtilityFunction.fnInsertAuditAction(deletedRestoreMasterList, 1, null, multilingualIDList, userInfo);
				return getRestoreMaster(userInfo);
			
		}
	}

	@Override
	public ResponseEntity<Object> getPurgeDate(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		final String strQuery = "select npurgemastercode,to_char(dtodate, 'dd/MM/yyyy') as stodate from purgemaster  where nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and npurgemastercode > 0 and ntransactionstatus = "+Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()+" and npurgesitecode="+inputMap.get("nrestoresitecode")+" and nsitecode = " + userInfo.getNmastersitecode();
		return new ResponseEntity<>((List<PurgeMaster>) jdbcTemplate.query(strQuery, new PurgeMaster()),HttpStatus.OK);		
	}
	
	@Override
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception {
		final String strQuery = "select nsitecode as nrestoresitecode,ssitename from site  where nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode > 0  and nmastersitecode = " + userInfo.getNmastersitecode();
		return new ResponseEntity<>((List<Site>) jdbcTemplate.query(strQuery, new Site()),HttpStatus.OK);		
	}
	
	/**
	 * This method is used to fetch the active restoremaster objects for the specified todate and site.
	 * @param dtodate [String] date of the restoremaster
	 * @param nmasterSiteCode [int] site code of the restoremaster
	 * @return list of active restoremaster code(s) based on the specified todate and site
	 * @throws Exception 
	 */
//	private List<RestoreMaster> getRestoreMasterListByDate(String stodate, int nmasterSiteCode) throws Exception {		
//
//		String strQuery="select (select max(npurgmastertabletransactioncode) from  Lims_purg_master_tables_transaction)+RANK()OVER ( "
//				+ " ORDER BY ncoaparentcode ASC "
//				+ " ) as npurgmastertabletransactioncode, "
//				+ " 1 as npurgmastercode ,1 as npurgmastertablecode,ncoaparentcode,nsitecode,CURRENT_TIMESTAMP,ROW_TO_JSON(a.*),-1 as nmastersitecode,1 as nstatus "
//				+ " from (select c.ncoaparentcode,max(c.nregtypecode) nregtypecode,max(c.nregsubtypecode) nregsubtypecode,max(c.napproveconfversioncode) napproveconfversioncode, "
//				+ " max(c.ncoareporttypecode) ncoareporttypecode,max(c.nreporttemplatecode) nreporttemplatecode,max(c.ntransactionstatus) ntransactionstatus, "
//				+ " max(c.nenteredby) nenteredby,max(c.nenteredrole) nenteredrole,max(c.ndeputyenteredby) ndeputyenteredby,max(c.ndeputyenteredrole) ndeputyenteredrole, "
//				+ " max(c.dmodifieddate) dmodifieddate,max(c.nsitecode) nsitecode,max(c.nstatus) nstatus,max(c.sreportno) sreportno,max(cr.dtransactiondate) dtransactiondate,max(cr.nsitecode) nnsitecode from ( "
//				+ " select r.npreregno,count(r.ntransactiontestcode) testcount from purg_registrationtest r,purg_coachild c ,purg_coaparent cp where r.ntransactiontestcode=any( "
//				+ " select ntransactiontestcode from purg_registrationtesthistory "
//				+ " where ntransactionstatus=33 and nstatus=1 "
//				+ " group by npreregno,ntransactiontestcode) and c.ncoaparentcode=cp.ncoaparentcode "
//				+ " and cp.ntransactionstatus=33 and r.ntransactiontestcode=c.ntransactiontestcode "
//				+ " group by r.npreregno) historycount,( "
//				+ " select rth.npreregno,count(ntesthistorycode) testcount "
//				+ " from purg_registrationtest rt,purg_registrationtesthistory rth "
//				+ " where ntransactionstatus not in (35,34,32) and rt.nsitecode=rth.nsitecode "
//				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
//				+ " and rth.ntesthistorycode = any( "
//				+ " select max(ntesthistorycode) ntesthistorycode from purg_registrationtesthistory "
//				+ " where nstatus=1 group by ntransactiontestcode,npreregno) "
//				+ " group by rth.npreregno) testcount,purg_coaparent c,purg_coachild ch,purg_registrationarno arno,purg_registration r,purg_registrationhistory rh,purg_releasehistory cr "
//				+ " where historycount.npreregno=testcount.npreregno "
//				+ " and historycount.testcount=testcount.testcount and r.npreregno=rh.npreregno and historycount.npreregno=r.npreregno and "
//				+ " rh.nreghistorycode =any(select max(rh1.nreghistorycode) from purg_registrationhistory rh1 "
//				+ " group by npreregno) and rh.ntransactionstatus=33 and r.npreregno=arno.npreregno and r.nstatus=1 "
//				+ " and ch.npreregno=r.npreregno and c.ncoaparentcode=ch.ncoaparentcode "
//				+ " and cr.nreleasehistorycode =any(select max(nreleasehistorycode) from purg_releasehistory group by ncoaparentcode) "
//				+ " and cr.ntransactionstatus=33 and cr.ncoaparentcode=c.ncoaparentcode group by c.ncoaparentcode) a "
//				+ " where TO_CHAR(a.dtransactiondate, 'yyyy-mm-dd') <= to_char('"+stodate+"'::timestamp without time zone,'yyyy-mm-dd')";
//		List<Map<String, Object>> lstRecordCount = getJdbcTemplate().queryForList(strQuery);
//		if(lstRecordCount!=null && lstRecordCount.size()>0)
//		{
//			strQuery="select * from restoremaster where 1=0;";
//			return (List<RestoreMaster>) jdbcTemplate.query(strQuery, new RestoreMaster());
//		   
//		}
//		else
//		{
//			strQuery="select * from restoremaster;";
//		
//			List<RestoreMaster> lstrestore= jdbcTemplate.query(strQuery, new RestoreMaster());
//			if(lstrestore != null && lstrestore.size() >0)
//			{
//				strQuery="select * from restoremaster where 1=1;";
//				return (List<RestoreMaster>) jdbcTemplate.query(strQuery, new RestoreMaster());
//			}
//			else
//			{
//				strQuery="select * from restoremaster where 1=0;";
//				return (List<RestoreMaster>) jdbcTemplate.query(strQuery, new RestoreMaster());
//			}
//		}
//
//	}
	

}
