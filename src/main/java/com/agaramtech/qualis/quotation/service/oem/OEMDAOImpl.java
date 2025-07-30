package com.agaramtech.qualis.quotation.service.oem;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.basemaster.service.unit.UnitDAOImpl;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.quotation.model.OEM;

import lombok.AllArgsConstructor;


@AllArgsConstructor
@Repository
public class OEMDAOImpl implements OEMDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OEMDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	
	//private static int dataBaseType = 0;
	
	//private final AgaramtechGeneralfunction objGeneral;
	
	//@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getOEM(UserInfo userInfo) throws Exception {
		
		final String strQuery = "select o.* from OEM o"
				+ " where o.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and o.noemcode>0 "
				+ " and o.nsitecode="+userInfo.getNmastersitecode()+" order by o.noemcode asc";
		
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new OEM()), HttpStatus.OK);

	}

	
	@Override
	public ResponseEntity<Object> createOEM(OEM oem, UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedOEMList = new ArrayList<>();
		

		final OEM oemListByName = getOEMListByName(
				oem.getSoemname(), userInfo.getNmastersitecode());

		if (oemListByName == null) {
			
			
			int nSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoquotationmanagement where stablename='oem'", Integer.class);
			nSeqNo++;
			
			String oemInsert = "insert into oem(noemcode,soemname,sdescription,dmodifieddate,nsitecode,nstatus)"
					+ " values(" + nSeqNo + ",N'" + stringUtilityFunction.replaceQuote(oem.getSoemname()) + "',N'" + stringUtilityFunction.replaceQuote(oem.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNmastersitecode() +","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
			
			jdbcTemplate.execute(oemInsert);
			jdbcTemplate
					.execute("update seqnoquotationmanagement set nsequenceno = " + nSeqNo + " where stablename='oem'");
			
			oem.setNsitecode(userInfo.getNmastersitecode());
			savedOEMList.add(oem);
			multilingualIDList.add("IDS_ADDOEM");

			auditUtilityFunction.fnInsertAuditAction(savedOEMList, 1, null, multilingualIDList, userInfo);
			return getOEM(oem.getNsitecode());
		} else {
		
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}
	
	
	@Override
	public ResponseEntity<Object> getOEM(final int nmasterSiteCode) throws Exception {

		final String strQuery = "select * from oem o where o.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
			    + " and noemcode > 0 and o.nsitecode = " + nmasterSiteCode+"";
				
		return new ResponseEntity<>((List<OEM>) jdbcTemplate.query(strQuery, new OEM()),
				HttpStatus.OK); // status code:200
	}
	
	
	private OEM getOEMListByName(final String soemname, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select noemcode from oem where soemname = N'"
				+ stringUtilityFunction.replaceQuote(soemname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (OEM) jdbcUtilityFunction.queryForObject(strQuery, OEM.class, jdbcTemplate);
	}
	
	@Override
	public ResponseEntity<Object> getActiveOEMById(final int noemcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select * from oem o where o.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and o.noemcode = "
				+ noemcode;

		final OEM oem =(OEM) jdbcUtilityFunction.queryForObject(strQuery, OEM.class, jdbcTemplate);
		if (oem == null) {
			// status code: 417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// status code: 200
			return new ResponseEntity<>(oem, HttpStatus.OK);
		}
       // }
	}


	@Override
	public ResponseEntity<Object> updateOEM(OEM OEM, UserInfo userInfo)
			throws Exception {
		final ResponseEntity<Object> oemReponse = getActiveOEMById(
				OEM.getNoemcode(), userInfo);

		if (oemReponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return oemReponse;
		} else {
			final String queryString = "select noemcode from OEM where soemname = N'"+ stringUtilityFunction.replaceQuote(OEM.getSoemname())+"'"
									+ " and noemcode <> " + OEM.getNoemcode()+" and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
									
			final List<OEM> oemList = (List<OEM>) jdbcTemplate.query(queryString,
					new OEM());

			if (oemList.isEmpty()) {
				final String updateQueryString = "update OEM set soemname=N'"+ stringUtilityFunction.replaceQuote(OEM.getSoemname()) + "',sdescription='"+stringUtilityFunction.replaceQuote(OEM.getSdescription())+"', "
												+" dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' where noemcode="+ OEM.getNoemcode();

				jdbcTemplate.execute(updateQueryString);

				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITOEM");

				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(OEM);

				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(oemReponse.getBody());

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
				return getOEM(OEM.getNsitecode());
			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}
	
	
	@Override
	public ResponseEntity<Object> deleteOEM(OEM OEM, UserInfo userInfo)
			throws Exception {

		final ResponseEntity<Object> oemReponse = getActiveOEMById(
				OEM.getNoemcode(), userInfo);
		
		boolean validRecord = true;
		
		if (oemReponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			//status code:417
			return oemReponse;	
			} 
		else {
			
			final String query= "select 'IDS_QUOTATION' as Msg from quotation where noemcode= " 
			        + OEM.getNoemcode() + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";  
			
			ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);    			
			
				 validRecord = false;
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
			{		
				validRecord = true;
				objDeleteValidation = projectDAOSupport.validateDeleteRecord(Integer.toString(OEM.getNoemcode()), userInfo);
				if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
				{					
					validRecord = true;
				}
				else {
					validRecord = false;
				}
			}  
			
			if(validRecord) {
				final List<String> multilingualIDList  = new ArrayList<>();
				final List<Object> deletedOEMList = new ArrayList<>();
				final String updateQueryString = "update OEM set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+" ,dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"'"
												+ " where noemcode=" + OEM.getNoemcode();
					
			    jdbcTemplate.execute(updateQueryString);
			    OEM.setNstatus( (short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
							
			    deletedOEMList.add(OEM);						
			    multilingualIDList.add("IDS_DELETEOEM");
				auditUtilityFunction.fnInsertAuditAction(deletedOEMList, 1, null, multilingualIDList, userInfo);
			}
			else{
				//status code:417
				return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
		return getOEM(OEM.getNsitecode());
	}
	
}
