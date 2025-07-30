package com.agaramtech.qualis.configuration.service.sampletype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.credential.model.TransactionFilterType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SampleTypeDAOImpl implements SampleTypeDAO{

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleTypeDAOImpl.class);
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	
	
	
	@Override
	public ResponseEntity<Object> getSampleType(final UserInfo userInfo) throws Exception {
		String strQuery	= " select tft.ntransfiltertypecode,st.nsampletypecode,coalesce(st.jsondata->'sampletypename'->>'"+ userInfo.getSlanguagetypecode() + "',st.jsondata->'sampletypename'->>'en-US') ssampletypename, "
				+" coalesce(ts.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode() + "',ts.jsondata->'stransdisplaystatus'->>'en-US') scategorybasedflowrequired,"
				+"  coalesce(ts1.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode() + "',ts1.jsondata->'stransdisplaystatus'->>'en-US') clinicaltyperequired,"
				+"  coalesce(ts2.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode() + "',ts2.jsondata->'stransdisplaystatus'->>'en-US') sportalrequired,"
				+"  coalesce(ts3.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode() + "',ts3.jsondata->'stransdisplaystatus'->>'en-US') sprojectspecrequired, "
				+"  coalesce(ts4.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode() + "',ts4.jsondata->'stransdisplaystatus'->>'en-US') scomponentrequired, "
				+"  coalesce(ts5.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode() + "',ts5.jsondata->'stransdisplaystatus'->>'en-US') srulesenginerequired,"
				+"  coalesce(ts6.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode() + "',ts6.jsondata->'stransdisplaystatus'->>'en-US') soutsourcerequired,"
				+"  coalesce(tft.jsondata->'stransfiltertypename'->>'"+ userInfo.getSlanguagetypecode() + "',tft.jsondata->'stransfiltertypename'->>'en-US') stransfiltertypename "
				+ " from sampletype st,transactionstatus ts,transactionstatus ts1,transactionstatus ts2,transactionstatus ts3, "
				+ " transactionstatus ts4,transactionstatus ts5,transactionstatus ts6,transactionfiltertype tft where st.nsampletypecode > 0 and st.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		         +" and st.ncategorybasedflowrequired =ts.ntranscode and st.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				 +" and st.nclinicaltyperequired =ts1.ntranscode and st.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				 +" and st.nportalrequired =ts2.ntranscode and st.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				 +" and st.nprojectspecrequired =ts3.ntranscode and st.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				 +" and st.ncomponentrequired =ts4.ntranscode and st.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				 +" and st.nrulesenginerequired =ts5.ntranscode and st.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				 +" and st.noutsourcerequired =ts6.ntranscode and st.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				 +" and st.ntransfiltertypecode =tft.ntransfiltertypecode and st.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				 + " and st.nsitecode = "+userInfo.getNmastersitecode()+" and tft.nsitecode = "+userInfo.getNmastersitecode();
		LOGGER.info("Get Method:"+ strQuery);
		final List<SampleType> lstSampleType= jdbcTemplate.query(strQuery,new  SampleType());
		return new ResponseEntity<Object>(lstSampleType,HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> deleteSampleType(SampleType objSampleType,UserInfo userInfo) throws Exception {
		final SampleType sampletype = getActiveSampleTypeById(objSampleType.getNsampletypecode(),userInfo);
	
	     
		if (sampletype == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} 
		else {
			
				//final List<Object> deletedSiteList = new ArrayList<>();
				String updateQueryString = "update sampletype set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
												+ ",dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)
												+ "'  where nsampletypecode=" + sampletype.getNsampletypecode()+";";							
			    
			    jdbcTemplate.execute(updateQueryString);
				return getSampleType( userInfo);

	}
}
	
	@Override
	public SampleType getActiveSampleTypeById(final int nsampletypecode, UserInfo userInfo) throws Exception {
		
		final String strQuery ="select  st.nsampletypecode,	st.ncategorybasedflowrequired, "
					+ " st.nclinicaltyperequired, st.nprojectspecrequired, "
					+ " st.ncomponentrequired, st.nportalrequired, "
					+ " st.nrulesenginerequired,  st.noutsourcerequired, "
					+ " coalesce(st.jsondata->'sampletypename'->>'"+ userInfo.getSlanguagetypecode() 
					+ "',st.jsondata->'sampletypename'->>'en-US') ssampletypename "
				    +" ,tf.ntransfiltertypecode,coalesce(tf.jsondata->'stransfiltertypename'->>'"+ userInfo.getSlanguagetypecode() 
				    + "',tf.jsondata->'stransfiltertypename'->>'en-US') stransfiltertypename "
				    + " from sampletype st,transactionfiltertype tf "
				    + " where st.ntransfiltertypecode =tf.ntransfiltertypecode and st.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				    + "  and tf.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				    + " st.nsitecode = "+userInfo.getNmastersitecode()+" and tf.nsitecode = "+userInfo.getNmastersitecode()
				    + " and st.nsampletypecode="+nsampletypecode+" ";
		
		return (SampleType) jdbcUtilityFunction.queryForObject(strQuery, SampleType.class, jdbcTemplate);
	}
	
	
	
//	
	
	
	
	@Override
	public ResponseEntity<Object> updateSampleType(SampleType reason, UserInfo userInfo,boolean DeleteExistingRecord) throws Exception {		
		
		final SampleType objReason =getActiveSampleTypeById(reason.getNsampletypecode(),userInfo);		
		final Map<String, String> Validation = new HashMap<>();
		String Str="Select ntransfiltertypecode from sampletype where nsampletypecode="+reason.getNsampletypecode();
		
		SampleType oldSampleType=(SampleType) jdbcUtilityFunction.queryForObject(Str, SampleType.class, jdbcTemplate);
		
		if(!DeleteExistingRecord && oldSampleType.getNtransfiltertypecode()!=reason.getNtransfiltertypecode()) {
			String validation="select count(ntransfiltertypeconfigcode) from transactionfiltertypeconfig where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "+userInfo.getNmastersitecode()
					+" and nsampletypecode="+reason.getNsampletypecode();
			final int count = jdbcTemplate.queryForObject(validation, Integer.class);
			final String flag = count == 0 ? "false" :"true";
			Validation.put("flag",flag);
			if(flag.equals("true"))
			{
			      return new ResponseEntity<>(Validation, HttpStatus.CONFLICT);
			}
			else {
				  DeleteExistingRecord=true;
			}
		}
		
		if(DeleteExistingRecord) {
			if (objReason == null) 
			{
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			} 
			else 
			{	
			    if(oldSampleType.getNtransfiltertypecode()!=reason.getNtransfiltertypecode())
			    {
				   //ALPD-4041
			      String updateQueryString = "update sampletype set "
					     + " ntransfiltertypecode="+reason.getNtransfiltertypecode()
			             +" where nsampletypecode="+reason.getNsampletypecode(); 
			
			      updateQueryString=updateQueryString +";update transactionusers set nstatus="+Enumeration.TransactionStatus.NA.gettransactionstatus()
			             +" where ntransfiltertypeconfigcode in( "
			             +"	select ntransfiltertypeconfigcode from transactionfiltertypeconfig where nsitecode = "+userInfo.getNmastersitecode()
			             +" and nsampletypecode="+reason.getNsampletypecode()+"); "
			             +"	update transactionfiltertypeconfig set nstatus="+Enumeration.TransactionStatus.NA.gettransactionstatus()+" where nsampletypecode="+reason.getNsampletypecode();
			
			      jdbcTemplate.execute(updateQueryString);
			
			     final List<String> multilingualIDList  = new ArrayList<>();
				 multilingualIDList.add("IDS_EDITREASON");			
				 final List<Object> listAfterSave = new ArrayList<>();
				 listAfterSave.add(reason);			    
			     final List<Object> listBeforeSave = new ArrayList<>();
				 listBeforeSave.add(objReason);			
					
				 auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
			  }
			
			  return getSampleType(userInfo);
		  }

		}
	
		return getSampleType(userInfo);
		
	}
	

	
	@Override
	public ResponseEntity<Object> getTransactionFilterType(final UserInfo userInfo) throws Exception {
		final String strQuery	= " select st.ntransfiltertypecode,coalesce(st.jsondata->'stransfiltertypename'->>'"+ userInfo.getSlanguagetypecode() + "',st.jsondata->'stransfiltertypename'->>'en-US') stransfiltertypename "
						+ " from transactionfiltertype st where  st.ntransfiltertypecode>0 "
						//+ "and st.nsitecode = "+userInfo.getNmastersitecode()+" 
						+" and  st.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<TransactionFilterType> lstTransactionFilterType= jdbcTemplate.query(strQuery,new  TransactionFilterType());
		return new ResponseEntity<Object>(lstTransactionFilterType,HttpStatus.OK);
	}

	
}
