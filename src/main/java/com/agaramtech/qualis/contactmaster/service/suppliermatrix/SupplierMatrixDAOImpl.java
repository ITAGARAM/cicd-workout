package com.agaramtech.qualis.contactmaster.service.suppliermatrix;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.contactmaster.model.SupplierMatrix;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "supplier" table by implementing
 * methods from its interface. 
 * @author ATE113
 * @version 
 * @since   12- Aug- 2020
 */

@AllArgsConstructor
@Repository
public class SupplierMatrixDAOImpl implements SupplierMatrixDAO{

	private static final Logger LOGGER = LoggerFactory.getLogger(SupplierMatrixDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSuppcommonFunctionort;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	 /**
		 * This method is used to retrieve list of all active suppliermatrix for the
		 * specified site.
		 * @param nsuppliercode [int] primary key of site object for which the list is to be fetched
		 * @return response entity  object holding response status and list of all active suppliermatrix
		 * @throws Exception that are thrown from this DAO layer
		 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSupplierMatrix(final int nsuppliercode,final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery= "Select sm.*,sc.ssuppliercatname,"
			               	   + " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				               + " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,'11' smaterialcatname from suppliermatrix sm,suppliercategory sc,transactionstatus ts  where " 
				               +" sm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				               +" and sm.ntypecode = 1 and sm.ncategorycode = sc.nsuppliercatcode and ts.ntranscode = sm.ntransactionstatus and "
				               +" sm.nsuppliercode = "+nsuppliercode;

		final List<SupplierMatrix> supplierMatrixList = (List<SupplierMatrix>) jdbcTemplate.query(strQuery,
				new SupplierMatrix());
		
		outputMap.put("SupplierCategory", supplierMatrixList);
		
		final String queryStr= "Select sm.*,mc.smaterialcatname,"
				   +" coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				   +" ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,'22' ssuppliercatname from suppliermatrix sm,materialcategory mc,transactionstatus ts  where " 
	               +" sm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
	               +" and sm.ntypecode = 2 and sm.ncategorycode = mc.nmaterialcatcode and ts.ntranscode = sm.ntransactionstatus and "
	               +" sm.nsuppliercode = "+nsuppliercode;

        final List<SupplierMatrix> supplierMatrixMaterialList = (List<SupplierMatrix>) jdbcTemplate.query(queryStr,
        		new SupplierMatrix());

        
         outputMap.put("MaterialCategory", supplierMatrixMaterialList);
		
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	

	
	/**
	 * This method is used to retrieve active suppliermatrix object based
	 * on the specified nsuppliermatrixcode.
	 * @param nsuppliermatrixcode [int] primary key of suppliermatrix object
	 * @return suppliermatrix object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public SupplierMatrix getActiveSupplierMatrixById(int nsuppliermatrixcode,final UserInfo userInfo) throws Exception {
//		
		
		final String strQuery = "select a.*,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus from suppliermatrix a, transactionstatus ts where a.ntransactionstatus = ts.ntranscode and nsuppliermatrixcode > 0 "
                + " and a.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
                + " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsuppliermatrixcode = " + nsuppliermatrixcode;/// + users.getnmastersitecode();

		return (SupplierMatrix) jdbcUtilityFunction.queryForObject(strQuery, SupplierMatrix.class, jdbcTemplate);	
	}

	//ALPD-861 Fix
	public SupplierMatrix checKSupplierIsPresent(final int nsuppliercode) throws Exception {
		String strQuery = "select nsuppliercode,napprovalstatus from supplier where nsuppliercode = " + nsuppliercode
				+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		SupplierMatrix objTest = (SupplierMatrix) jdbcUtilityFunction.queryForObject(strQuery, SupplierMatrix.class, jdbcTemplate);
		return objTest;
	}
	
	/**
	 * This method is used to add a new entry to suppliermatrix  table.
	 * @param suppliermatrix [SupplierMatrix] object holding details to be added in suppliermatrix table
	 * @return response entity object holding response status and data of added suppliermatrix object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createSupplierMatrix(List<SupplierMatrix> supplierMatrix, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table suppliermatrix "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSupplierMatrixList = new ArrayList<>();	
		//List<SupplierCategory> supplierCatList=new ArrayList<>();

		//ALPD-861 Fix
		 SupplierMatrix lstSupplier = checKSupplierIsPresent(supplierMatrix.get(0).getNsuppliercode());
		  
	   if(lstSupplier != null) {
		   if(lstSupplier.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			List<SupplierMatrix> lstSupplierMatrix = getSupplierMatrixListByCattegoryCode(supplierMatrix.get(0).getNsuppliercode(), supplierMatrix.get(0).getNtypecode());
			
			List<SupplierMatrix> filteredList = supplierMatrix.stream().filter(source -> lstSupplierMatrix.stream()
	                .noneMatch(check -> source.getNcategorycode() == check.getNcategorycode())).collect(Collectors.toList());
			
			final String categorycodevalue = filteredList.stream().map(x -> String.valueOf(x.getNcategorycode())).collect(Collectors.joining(","));

			
			if(filteredList != null && filteredList.size() > 0) {	
			int nSeqNo=(int) jdbcUtilityFunction.queryForObject("select nsequenceno from seqnocontactmaster where stablename='suppliermatrix'", Integer.class , jdbcTemplate);
			//nSeqNo++;
           //	String supplierInsert="insert into suppliermatrix(nsuppliermatrixcode,nsuppliercode,ncategorycode,ntypecode,sremarks,ntransactionstatus,nstatus)"
			String str="";
			String categorycode="";
			if(supplierMatrix.get(0).getNtypecode()==1) {
				categorycode= "nsuppliercatcode";
				str="suppliercategory where nsuppliercatcode in("+categorycodevalue+")";
			}else {
				categorycode= "nmaterialcatcode";
				str="materialcategory where nmaterialcatcode in("+categorycodevalue+")";
			}
			String Seqaudit="(select "+nSeqNo+" +rank()over(order by "+categorycode+")as nsuppliermatrixcode,"+supplierMatrix.get(0).getNsuppliercode()+","+categorycode+" as ncategorycode,"
					+ ""+supplierMatrix.get(0).getNtypecode()+",N'"+stringUtilityFunction.replaceQuote(supplierMatrix.get(0).getSremarks())+"',"
					+ ""+supplierMatrix.get(0).getNtransactionstatus()+",'"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" from  "+str+"  )";
			List<SupplierMatrix> seqnoaudit=(List<SupplierMatrix>) jdbcTemplate.query(Seqaudit, new SupplierMatrix());
			String seqnoauditlist=seqnoaudit.stream().map(object->String.valueOf(object.getNsuppliermatrixcode())).collect(Collectors.joining(","));

			String supplierInsert="insert into suppliermatrix(nsuppliermatrixcode,nsuppliercode,ncategorycode,ntypecode,sremarks,ntransactionstatus,dmodifieddate,nsitecode,nstatus)"
					+"(select "+nSeqNo+" +rank()over(order by "+categorycode+")as nsuppliermatrixcode,"+supplierMatrix.get(0).getNsuppliercode()+","+categorycode+" as ncategorycode,"
					+ ""+supplierMatrix.get(0).getNtypecode()+",N'"+stringUtilityFunction.replaceQuote(supplierMatrix.get(0).getSremarks())+"',"
					+ ""+supplierMatrix.get(0).getNtransactionstatus()+",'"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" from  "+str+"  )";
//			String supplierInsert="insert into suppliermatrix(nsuppliermatrixcode,nsuppliercode,ncategorycode,ntypecode,sremarks,ntransactionstatus,nstatus)"
//					              + " values("+nSeqNo+","+supplierMatrix.get(0).getNsuppliercode()+","+supplierMatrix.get(0).getNcategorycode()+","+supplierMatrix.get(0).getNtypecode()+","
//					              + " N'"+ReplaceQuote(supplierMatrix.get(0).getSremarks())+"',"+supplierMatrix.get(0).getNtransactionstatus()+",1)";
			jdbcTemplate.execute(supplierInsert);
		    Integer sequnenceno=nSeqNo + filteredList.size();
		    jdbcTemplate.execute("update seqnocontactmaster set nsequenceno = "+sequnenceno+" where stablename='suppliermatrix'");
			//String sMatrix=supplierMatrix.stream().map(object->String.valueOf(object.getNsuppliermatrixcode())).collect(Collectors.joining(","));
		
			if(supplierMatrix.get(0).getNtypecode()==1) {
				String strCategory="select sm.ncategorycode as nsuppliercatcode,sm.* from suppliercategory sc,suppliermatrix sm where sm.ncategorycode=sc.nsuppliercatcode and sm.nstatus=1 and sm.nsuppliermatrixcode in ("+seqnoauditlist+")";
				List<SupplierMatrix> lstSuppliercategory=(List<SupplierMatrix>) jdbcTemplate.query(strCategory, new SupplierMatrix());
				multilingualIDList.add("IDS_ADDSUPPLIERCATEGORY");
				savedSupplierMatrixList.add(lstSuppliercategory);
				
			}else {
				String strCategory="select sm.ncategorycode as nmaterialcatcode,sm.* from materialcategory mc,suppliermatrix sm where sm.ncategorycode=mc.nmaterialcatcode and sm.nstatus=1 and sm.nsuppliermatrixcode in ("+seqnoauditlist+")";
				List<SupplierMatrix> lstMaterialCategory=(List<SupplierMatrix>) jdbcTemplate.query(strCategory, new SupplierMatrix());
				multilingualIDList.add("IDS_ADDMATERIALCATEGORY");
				savedSupplierMatrixList.add(lstMaterialCategory);
			}
			auditUtilityFunction.fnInsertListAuditAction(savedSupplierMatrixList, 1,null, multilingualIDList, userInfo);
			
			// ALPD-5387 - Gowtham - In Supplier Screen with Multi Tab adding same supplier category no alert is thrown - 12/02/2025
			return getSupplierMatrix(supplierMatrix.get(0).getNsuppliercode(),userInfo);
			
			} else {
				// ALPD-5387 - Gowtham - In Supplier Screen with Multi Tab adding same supplier category no alert is thrown - 12/02/2025
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
	   }
		else {
			//ALPD-861 Fix
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		}
	else {
		//ALPD-861 Fix
		return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUPPLIERALREADYDELETED", userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	}
		
	}

	/**
	 * This method is used to retrieve active suppliermatrix object based
	 * on the specified nsuppliermatrixcode.
	 * @param nsuppliermatrixcode [int] primary key of suppliermatrix object
	 * @return suppliermatrix object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	private List<SupplierMatrix> getSupplierMatrixListByCattegoryCode(final int suppliercode, final int typecode) throws Exception {
		final String strQuery = "Select * from SupplierMatrix where nsuppliercode = "+suppliercode+ 
			     " and ntypecode = "+typecode+
			     " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (List<SupplierMatrix>) jdbcTemplate.query(strQuery, new SupplierMatrix());
	}
	
	/**
	 * This method is used to fetch the active suppliermatrix objects for the specified suppliermatrix
	 * name and site.
	 * @param ssupplierName [String] supplier name for which the records are to be
	 * fetched
	 * @param nmasterSiteCode [int] primary key of site object
	 * @return list of active suppliers based on the specified supplier name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
//	@SuppressWarnings("unchecked")
//	private List<Supplier> getSupplierListByName(final String ssupplierName, final int nmasterSiteCode) throws Exception
//	{		
//		final String strQuery = "select  ssuppliername from supplier where ssuppliername = N'"
//				+ ReplaceQuote(ssupplierName) + "' and nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+" and nsitecode =" + nmasterSiteCode;//users.getnmastersitecode();
//		
//		return (List<Supplier>) jdbcTemplate.query(strQuery, Supplier.class);
//	}
//	
	/**
	 * This method is used to update entry in suppliermatrix  table.
	 * Need to validate that the suppliermatrix object to be updated is active before updating details
	 * in database.
	 * @param suppliermatrix [SupplierMatrix] object holding details to be updated in suppliermatrix table
	 * @return response entity object holding response status and data of updated suppliermatrix object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateSupplierMatrix(SupplierMatrix supplierMatrix, UserInfo userInfo) throws Exception {
	
		final SupplierMatrix supplierMatrixByID = (SupplierMatrix) getActiveSupplierMatrixById(supplierMatrix.getNsuppliermatrixcode(),userInfo);
		//Integer nSupplierCode=null;
		if (supplierMatrixByID == null) 
		{
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} 
		else 
		{			

											
				
			    final String updateQueryString = "update suppliermatrix set nsuppliercode =  " + supplierMatrix.getNsuppliercode()  
			    				+ ", ncategorycode =" + supplierMatrix.getNcategorycode() + ",ntypecode =" + supplierMatrix.getNcategorycode() 
			    				+ ", sremarks = N'" + stringUtilityFunction.replaceQuote(supplierMatrix.getSremarks()) + ", ntransactionstatus= " + supplierMatrix.getNtransactionstatus() 
			    				+"dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"'"
			    				+ " where nsuppliermatrixcode=" + supplierMatrix.getNsuppliermatrixcode();
							
			    jdbcTemplate.execute(updateQueryString);
			
			    final List<String> multilingualIDList  = new ArrayList<>();
			    
			    final List<Object> listBeforeSave = new ArrayList<>();			
			    listBeforeSave.add(supplierMatrixByID);
			    
				final List<Object> listAfterSave = new ArrayList<>();			
			    listAfterSave.add(supplierMatrix);	  
							
			    multilingualIDList.add("IDS_EDITSUPPLIERMATRIX");
										
			    auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);						
			
				//status code:200
				//return new ResponseEntity<>(supplier, HttpStatus.OK);
				return getSupplierMatrix(supplierMatrix.getNsuppliercode(),userInfo);
			 

		}

	}
	
//	public ResponseEntity<Object> deleteSupplierMatrixRecord(final int nsuppliercode) throws Exception {
//
//
//		
//			 
//					final String deleteQueryString = "delete from suppliermatrix where nsuppliercode=" + nsuppliercode;
//						
//				    getJdbcTemplate().execute(deleteQueryString);
//							    
//				
//					//status code:200
//					//return new ResponseEntity<>(supplier, HttpStatus.OK);
//					return getSupplierMatrix(nsuppliercode);
//
//			
//	}
	
	@SuppressWarnings("unchecked")
	/**
	 * This method is used to delete entry in suppliermatrix table.
	 * Need to validate that the specified suppliermatrix object is active and is not associated with any of its child tables
	 * before updating its nstatus to -1.
	 * @param suppliermatrix [SupplierMatrix] object holding detail to be deleted in suppliermatrix table
	 * @return response entity object holding response status and data of deleted suppliermatrix object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteSupplierMatrix(SupplierMatrix supplierMatrix, UserInfo userInfo) throws Exception {
		//ALPD-861 Fix
		SupplierMatrix lstSupplier = checKSupplierIsPresent(supplierMatrix.getNsuppliercode());

		if(lstSupplier != null) {
     if(lstSupplier.getNapprovalstatus()!=Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
               }
		 final SupplierMatrix suplierMatrixByID = (SupplierMatrix) getActiveSupplierMatrixById(supplierMatrix.getNsuppliermatrixcode(),userInfo);
		 //Integer nSupplierCode=null;
			if (suplierMatrixByID == null) {
				
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} 
			else {
					
				//deleteValidation
				
//				final String query= "select 'IDS_INSTRUMENT' as Msg from instrument where nsuppliercode = " + supplier.getNsuppliercode() + 
//						" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
//						
//				final ValidatorDel objDeleteValidation = getTransactionInfo(query, userInfo);
//
//				if (objDeleteValidation.getnreturnstatus() == Enumeration.Deletevalidator.SUCESS.getReturnvalue()) {
					
					final List<String> multilingualIDList  = new ArrayList<>();
					final List<Object> savedSupplierMatrixList = new ArrayList<>();
					final String updateQueryString = "update suppliermatrix set   dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() 
							+ " where nsuppliermatrixcode=" + supplierMatrix.getNsuppliermatrixcode();
						
				    if(supplierMatrix.getNtypecode()==1) {
						String strCategory="select sm.ncategorycode as nsuppliercatcode,sm.* from suppliercategory sc,suppliermatrix sm where sm.ncategorycode=sc.nsuppliercatcode and sm.nstatus=1 and sm.nsuppliermatrixcode="+supplierMatrix.getNsuppliermatrixcode();
						SupplierMatrix lstSuppliercategory=(SupplierMatrix) jdbcUtilityFunction.queryForObject(strCategory, SupplierMatrix.class, jdbcTemplate);
						multilingualIDList.add("IDS_DELETESUPPLIERCATEGORY");
						savedSupplierMatrixList.add(lstSuppliercategory);
						
					}else {
						String strCategory="select sm.ncategorycode as nmaterialcatcode,sm.* from materialcategory mc,suppliermatrix sm where sm.ncategorycode=mc.nmaterialcatcode and sm.nstatus=1 and sm.nsuppliermatrixcode="+supplierMatrix.getNsuppliermatrixcode();
						SupplierMatrix lstMaterialCategory=(SupplierMatrix) jdbcUtilityFunction.queryForObject(strCategory, SupplierMatrix.class, jdbcTemplate);
						multilingualIDList.add("IDS_DELETEMATERIALCATEGORY");
						savedSupplierMatrixList.add(lstMaterialCategory);
					}
					
				    jdbcTemplate.execute(updateQueryString);
					
				   // supplierMatrix.setNstatus(Enumeration.TransactionStatus.DELETED.gettransactionstatus());
								
				    auditUtilityFunction.fnInsertAuditAction(savedSupplierMatrixList, 1, null, multilingualIDList, userInfo);
				
					//status code:200
					//return new ResponseEntity<>(supplier, HttpStatus.OK);
					return getSupplierMatrix(supplierMatrix.getNsuppliercode(),userInfo);
//				} else {
//					//status code:417
//					return new ResponseEntity<>(objDeleteValidation.getsreturnmessage(), HttpStatus.EXPECTATION_FAILED);
//				}
			}
		} else {
			//ALPD-861 Fix
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUPPLIERALREADYDELETED", userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	
		
}

