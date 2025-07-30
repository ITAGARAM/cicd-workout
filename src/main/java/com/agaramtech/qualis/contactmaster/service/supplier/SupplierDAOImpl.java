package com.agaramtech.qualis.contactmaster.service.supplier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.SerializationUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.contactmaster.model.Supplier;
import com.agaramtech.qualis.contactmaster.model.SupplierContact;
import com.agaramtech.qualis.contactmaster.model.SupplierFile;
import com.agaramtech.qualis.contactmaster.model.SupplierMatrix;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "supplier" table by
 * implementing methods from its interface.
 * 
 * @author ATE090
 * @version
 * @since 30- Jun- 2020
 */

@AllArgsConstructor
@Repository
public class SupplierDAOImpl implements SupplierDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SupplierDAOImpl.class);

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
	 * This method is used to retrieve list of all active supplier for the specified
	 * site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         suppliers
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSupplier(Integer nsuppliercode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		Supplier selectedSupplier = null;

		if (nsuppliercode != null && nsuppliercode > 0) {
			Supplier objSup = getActiveSupplierById(nsuppliercode, userInfo);
			if (objSup == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}

		final String strQuery = "select a.*,c.scountryname as scountryname, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,  coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',ts1.jsondata->'stransdisplaystatus'->>'en-US') as sapprovalstatus "
				+ " from supplier a, transactionstatus ts,country c ,transactionstatus ts1 where a.ntransactionstatus = ts.ntranscode and  a.napprovalstatus = ts1.ntranscode and a.ncountrycode=c.ncountrycode and nsuppliercode > 0 "
				+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.nsitecode= " + userInfo.getNmastersitecode() + " and a.nsitecode = " + userInfo.getNmastersitecode() + " order by a.nsuppliercode";/// +
																										/// users.getnmastersitecode();
		// return new ResponseEntity<>((List<Supplier>) jdbcTemplate.query(strQuery,
		// Supplier.class),HttpStatus.OK);
		final List<Supplier> supplierList = (List<Supplier>) jdbcTemplate.query(strQuery, new Supplier());

		if (supplierList == null || supplierList.isEmpty()) {
			outputMap.put("SelectedSupplier", null);
			outputMap.put("SupplierCategory", supplierList);
			outputMap.put("Supplier", supplierList);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			outputMap.put("Supplier", supplierList);
			if (nsuppliercode == null) {
				selectedSupplier = ((Supplier) supplierList.get(supplierList.size() - 1));
				nsuppliercode = selectedSupplier.getNsuppliercode();
			} else {
				selectedSupplier = getActiveSupplierById(nsuppliercode, userInfo);
			}
		}
		if (selectedSupplier == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedSupplier", selectedSupplier);

			outputMap.putAll((Map<String, Object>) getSupplierMatrix(nsuppliercode, userInfo).getBody());

			outputMap.putAll((Map<String, Object>) getSupplierMatrixMaterial(nsuppliercode, userInfo).getBody());

			outputMap.putAll((Map<String, Object>) getSupplierFile(nsuppliercode, userInfo).getBody());

			outputMap.putAll((Map<String, Object>) getSupplierContact(nsuppliercode, userInfo).getBody());

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

	}

	/**
	 * This method is used to retrieve list of all active suppliermatrix for the
	 * specified site.
	 * 
	 * @param nsuppliercode [int] key of supplier object for which the list is to be
	 *                      fetched
	 * @return response entity object holding response status and list of all active
	 *         suppliers
	 * @throws Exception that are thrown from this DAO layer
	 */

	public ResponseEntity<Object> getSupplierMatrix(final int nsuppliercode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery = "Select sm.*,sc.ssuppliercatname," + " coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus ,'11' smaterialcatname from suppliermatrix sm,suppliercategory sc,transactionstatus ts  where "
				+ " sm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and sm.ntypecode = " + Enumeration.CategoryType.SUPPLIERCATEGORY.getCategoryType() + " and sm.ncategorycode = sc.nsuppliercatcode and ts.ntranscode = sm.ntransactionstatus and "
				+ " sm.nsuppliercode = " + nsuppliercode;

		final List<SupplierMatrix> supplierMatrixList = (List<SupplierMatrix>) jdbcTemplate.query(strQuery,
				new SupplierMatrix());

		outputMap.put("SupplierCategory", supplierMatrixList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active suppliermatrix for the
	 * specified site.
	 * 
	 * @param nsuppliercode [int] key of supplier object for which the list is to be
	 *                      fetched
	 * @return response entity object holding response status and list of all active
	 *         suppliers
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> getSupplierMatrixMaterial(final int nsuppliercode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery = "Select sm.*,mc.smaterialcatname," + " coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus ,'22' ssuppliercatname from suppliermatrix sm,materialcategory mc,transactionstatus ts  where "
				+ " sm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and mc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sm.ntypecode = " + Enumeration.CategoryType.MATERIALCATEGORY.getCategoryType() + " and sm.ncategorycode = mc.nmaterialcatcode and ts.ntranscode = sm.ntransactionstatus and "
				+ " sm.nsuppliercode = " + nsuppliercode;

		final List<SupplierMatrix> supplierMatrixList = (List<SupplierMatrix>) jdbcTemplate.query(strQuery,
				new SupplierMatrix());

		outputMap.put("MaterialCategory", supplierMatrixList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active supplier object based on the specified
	 * nsupplierCode.
	 * 
	 * @param nsupplierCode [int] primary key of supplier object
	 * @return supplier object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Supplier getActiveSupplierById(final int nsupplierCode, final UserInfo userInfo) throws Exception {

		final String strQuery = "select a.*,c.scountryname as scountryname,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() +"', ts1.jsondata->'stransdisplaystatus'->>'en-US') as sapprovalstatus "
				+ " from supplier a,country c, transactionstatus ts,transactionstatus ts1 where a.ntransactionstatus = ts.ntranscode and nsuppliercode > 0  and ts1.ntranscode=a.napprovalstatus and a.ncountrycode=c.ncountrycode "
				+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and a.nsuppliercode = " + nsupplierCode;/// + users.getnmastersitecode();

		//return (Supplier) jdbcQueryForObject(strQuery, Supplier.class);
		return (Supplier) jdbcUtilityFunction.queryForObject(strQuery, Supplier.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to supplier table. Need to check for
	 * duplicate entry of supplier name for the specified site before saving into
	 * database.
	 * 
	 * @param supplier [Supplier] object holding details to be added in supplier
	 *                 table
	 * @return response entity object holding response status and data of added
	 *         supplier object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSupplier(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		
		final String sQuery = " lock  table locksupplier "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		//getJdbcTemplate().execute(sQuery);
		jdbcTemplate.execute(sQuery);
		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSupplierList = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();
		final Supplier objSupplier = objmapper.convertValue(inputMap.get("supplier"), Supplier.class);
		final SupplierContact objSupplierContact = objmapper.convertValue(inputMap.get("suppliercontact"),
				SupplierContact.class);
		objSupplierContact.setIsreadonly(true);
		Integer nSupplierCode = null;
		final Supplier supplierListByName = getSupplierListByName(objSupplier.getSsuppliername(),
				objSupplier.getNsitecode());

		if (supplierListByName == null) {
			//int nSeqNo = getJdbcTemplate().queryForObject(
			//		"select nsequenceno from seqnocontactmaster where stablename='supplier'", Integer.class);
			int nSeqNo = (int) jdbcUtilityFunction.queryForObject("select nsequenceno from seqnocontactmaster where stablename='supplier'", Integer.class, jdbcTemplate);
			
			nSeqNo++;
			String sAddress2=objSupplier.getSaddress2()==null?"":objSupplier.getSaddress2();
			String sAddress3=objSupplier.getSaddress3()==null?"":objSupplier.getSaddress3();
			String sPhoneNo=objSupplier.getSphoneno()==null?"":objSupplier.getSphoneno();
			String sFaxNo=objSupplier.getSfaxno()==null?"":objSupplier.getSfaxno();
			String smoblieno=objSupplier.getSmobileno()==null?"":objSupplier.getSmobileno();
			String sEmail=objSupplier.getSemail()==null?"":objSupplier.getSemail();

//			String supplierInsert="insert into supplier(nsuppliercode,ncountrycode,ssuppliername,saddress1,saddress2,saddress3,sphoneno,smobileno,sfaxno,semail,ntransactionstatus,nsitecode,nstatus)"
//					              + " values("+nSeqNo+","+supplier.getNcountrycode()+",N'"+replaceQuote(supplier.getSsuppliername())+"',N'"+replaceQuote(supplier.getSaddress1())+"',N'"+replaceQuote(sAddress2)+"',N'"+replaceQuote(sAddress3)+"' "
//					              + ",N'"+replaceQuote(sPhoneNo)+"',N'"+replaceQuote(supplier.getSmobileno())+"',N'"+replaceQuote(sFaxNo)+"',N'"+replaceQuote(supplier.getSemail())+"','"+supplier.getNtransactionstatus()+"',-1,1)";
			String supplierInsert = "insert into supplier(nsuppliercode,ssuppliername,saddress1,saddress2,saddress3,ncountrycode,sphoneno,smobileno,sfaxno,semail,napprovalstatus,ntransactionstatus,dmodifieddate,nsitecode,nstatus)"
					+ " values(" + nSeqNo + ",N'" + stringUtilityFunction.replaceQuote(objSupplier.getSsuppliername()) + "',N'"+stringUtilityFunction.replaceQuote(objSupplier.getSaddress1())+"',N'"+stringUtilityFunction.replaceQuote(sAddress2)+"',N'"+stringUtilityFunction.replaceQuote(sAddress3)+"',"+objSupplier.getNcountrycode()
					+ ",N'"+stringUtilityFunction.replaceQuote(sPhoneNo)+"',N'"+stringUtilityFunction.replaceQuote(smoblieno)+"',N'"+stringUtilityFunction.replaceQuote(sFaxNo)+"',N'"+stringUtilityFunction.replaceQuote(sEmail)+"',"+objSupplier.getNapprovalstatus()+","+objSupplier.getNtransactionstatus()+",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNmastersitecode() + ","+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +")";
			
			//getJdbcTemplate().execute(supplierInsert);
			jdbcTemplate.execute(supplierInsert);
			
			//getJdbcTemplate()
			//		.execute("update seqnocontactmaster set nsequenceno = " + nSeqNo + " where stablename='supplier'");
			jdbcTemplate.execute("update seqnocontactmaster set nsequenceno = " + nSeqNo + " where stablename='supplier'");
			
			objSupplierContact.setNsuppliercode(nSeqNo);
			objSupplier.setNsuppliercode(nSeqNo);
			savedSupplierList.add(objSupplier);
			final Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("SupplierContact", createSupplierContact(objSupplierContact, userInfo));
			multilingualIDList.add("IDS_ADDSUPPLIER");

			auditUtilityFunction.fnInsertAuditAction(savedSupplierList, 1, null, multilingualIDList, userInfo);

			// status code:200
			// return new ResponseEntity<>(supplier, HttpStatus.OK);
			return getSupplier(nSupplierCode, userInfo);
		} else {
			// Conflict = 409 - Duplicate entry
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	/**
	 * This method is used to fetch the active supplier objects for the specified
	 * supplier name and site.
	 * 
	 * @param ssupplierName   [String] supplier name for which the records are to be
	 *                        fetched
	 * @param nmasterSiteCode [int] primary key of site object
	 * @return list of active suppliers based on the specified supplier name and
	 *         site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Supplier getSupplierListByName(final String ssupplierName, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ssuppliername from supplier where ssuppliername = N'"
				+ stringUtilityFunction.replaceQuote(ssupplierName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;// users.getnmastersitecode();

		//return (Supplier) jdbcQueryForObject(strQuery, Supplier.class);
		return (Supplier) jdbcUtilityFunction.queryForObject(strQuery, Supplier.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in supplier table. Need to validate that
	 * the supplier object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of supplier name for the
	 * specified site before saving into database.
	 * 
	 * @param supplier [Supplier] object holding details to be updated in supplier
	 *                 table
	 * @return response entity object holding response status and data of updated
	 *         supplier object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> updateSupplier(Supplier supplier, UserInfo userInfo) throws Exception {

		final Supplier supplierByID = (Supplier) getActiveSupplierById(supplier.getNsuppliercode(), userInfo);
		//Integer nSupplierCode = null;
		if (supplierByID == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nsuppliercode from supplier where ssuppliername = N'"
					+ stringUtilityFunction.replaceQuote(supplier.getSsuppliername()) + "' and nsuppliercode <> "
					+ supplier.getNsuppliercode() + " and nsitecode = " + userInfo.getNmastersitecode()
					+ " and  nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<Supplier> supplierList = (List<Supplier>) jdbcTemplate.query(queryString, new Supplier());

			if (supplierList.isEmpty()) {
//			    final String updateQueryString = "update supplier set ssuppliername='" 
//			    				+ replaceQuote(supplier.getSsuppliername()) + "', ncountrycode =  " + supplier.getNcountrycode()  
//			    				+ ", saddress1 =N'" + replaceQuote(supplier.getSaddress1())
//			    				+ "', saddress2 =N'" + replaceQuote(supplier.getSaddress2()) + "', saddress3 = N'" + replaceQuote(supplier.getSaddress3()) 
//			    				+ "', sphoneno= N'" + replaceQuote(supplier.getSphoneno()) + "', smobileno= N'" + replaceQuote(supplier.getSmobileno()) + "', sfaxno= N'" + replaceQuote(supplier.getSfaxno())
//			    				+ "', semail= N'" + replaceQuote(supplier.getSemail()) + "', ntransactionstatus= " + supplier.getNtransactionstatus() + " where nsuppliercode=" + supplier.getNsuppliercode();
				final String updateQueryString = "update supplier set ssuppliername='"
						+ stringUtilityFunction.replaceQuote(supplier.getSsuppliername())+ "', ncountrycode =  " + supplier.getNcountrycode() 
						+ ", saddress1 =N'" + stringUtilityFunction.replaceQuote(supplier.getSaddress1())
	    				+ "', saddress2 =N'" + stringUtilityFunction.replaceQuote(supplier.getSaddress2()) + "', saddress3 = N'" + stringUtilityFunction.replaceQuote(supplier.getSaddress3()) 
	    				+ "', sphoneno= N'" + stringUtilityFunction.replaceQuote(supplier.getSphoneno()) + "', smobileno= N'" + stringUtilityFunction.replaceQuote(supplier.getSmobileno()) + "', sfaxno= N'" + stringUtilityFunction.replaceQuote(supplier.getSfaxno())
	    				+ "', semail= N'" + stringUtilityFunction.replaceQuote(supplier.getSemail()) + "', ntransactionstatus= " + supplier.getNtransactionstatus()
	    				+ "," + "dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ " where nsuppliercode="
						+ supplier.getNsuppliercode();

				//getJdbcTemplate().execute(updateQueryString);
				jdbcTemplate.execute(updateQueryString);

				final List<String> multilingualIDList = new ArrayList<>();

				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(supplierByID);

				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(supplier);

				multilingualIDList.add("IDS_EDITSUPPLIER");

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);

				// status code:200
				// return new ResponseEntity<>(supplier, HttpStatus.OK);
				return getSupplier(supplier.getNsuppliercode(), userInfo);
			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);

			}
		}

	}

	/**
	 * This method is used to delete entry in supplier table. Need to validate that
	 * the specified supplier object is active and is not associated with any of its
	 * child tables before updating its nstatus to -1.
	 * 
	 * @param supplier [Supplier] object holding detail to be deleted in supplier
	 *                 table
	 * @return response entity object holding response status and data of deleted
	 *         supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteSupplier(Supplier supplier, UserInfo userInfo) throws Exception {

		final Supplier suplierByID = (Supplier) getActiveSupplierById(supplier.getNsuppliercode(), userInfo);
		Integer nSupplierCode = null;
		if (suplierByID == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			//ALPD-2802 In Multi tab approved record deleted. Issue fixed by Saravanan 13-11-2024.
			String selectQuery = "select napprovalstatus from supplier where"
					+ " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsuppliercode = " + supplier.getNsuppliercode() + "";
			
			//Supplier oldSupplier = (Supplier) jdbcQueryForObject(selectQuery,Supplier.class);
			Supplier oldSupplier = (Supplier) jdbcUtilityFunction.queryForObject(selectQuery, Supplier.class, jdbcTemplate);
			
			if (oldSupplier != null && oldSupplier.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			// deleteValidation

			final String query = "select 'IDS_INSTRUMENT' as Msg from instrument where"
					+ " nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsuppliercode = " + supplier.getNsuppliercode() 
					+" union all "
					+ " select 'IDS_MATERIALINVENTORY' as Msg from materialinventory where"
					+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and (jsondata->'Supplier'->>'value')::integer = " + supplier.getNsuppliercode();
			
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);   	
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
			{		
				validRecord = true;
				//ALPD-4513--Vignesh R(05-09-2024)
				Map<String,Object> objOneToManyValidation=new HashMap<String,Object>();
				objOneToManyValidation.put("primaryKeyValue", Integer.toString(supplier.getNsuppliercode()));
				objOneToManyValidation.put("stablename", "supplier");
				
				valiDatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, userInfo);
				
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
				{					
					validRecord = true;
				}
				else 
				{
					validRecord = false;
				}
			}
			if(validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedSupplierList = new ArrayList<>();
				final List<Object> deletedSupplierMatrix = new ArrayList<>();
				final List<String> multilingualSupplierMatrixIDList = new ArrayList<>();
				final String updateQueryString = "update supplier set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' , nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsuppliercode=" + supplier.getNsuppliercode();

				//getJdbcTemplate().execute(updateQueryString);
				jdbcTemplate.execute(updateQueryString);

				supplier.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedSupplierList.add(supplier);
				multilingualIDList.add("IDS_DELETESUPPLIER");

				String strCategory = "select sm.ncategorycode as nsuppliercatcode,sm.* "
						+ " from suppliermatrix sm, suppliercategory sc "
						+ " where sm.ncategorycode=sc.nsuppliercatcode " + " and sm.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sm.ntypecode = " + Enumeration.CategoryType.SUPPLIERCATEGORY.getCategoryType() 
						+ " and sm.nsuppliercode=" + supplier.getNsuppliercode() ;
				List<SupplierMatrix> lstSuppliercategory = (List<SupplierMatrix>) jdbcTemplate.query(strCategory,
						new SupplierMatrix());
				if (!lstSuppliercategory.isEmpty()) {
					multilingualSupplierMatrixIDList.add("IDS_DELETESUPPLIERCATEGORY");
					deletedSupplierMatrix.add(lstSuppliercategory);
				}

				strCategory = "select sm.ncategorycode as nmaterialcatcode,sm.* "
						+ " from materialcategory mc,suppliermatrix sm "
						+ " where sm.ncategorycode=mc.nmaterialcatcode " + " and sm.nstatus=" 
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sm.ntypecode = " + Enumeration.CategoryType.MATERIALCATEGORY.getCategoryType()
						+ " and sm.nsuppliercode=" + supplier.getNsuppliercode() ;
				List<SupplierMatrix> lstMaterialCategory = (List<SupplierMatrix>) jdbcTemplate.query(strCategory,
						new SupplierMatrix());
				if (!lstMaterialCategory.isEmpty()) {
					multilingualSupplierMatrixIDList.add("IDS_DELETEMATERIALCATEGORY");
					deletedSupplierMatrix.add(lstMaterialCategory);
				}
				strCategory = "select a.*"
						+ " from suppliercontact a " + " where a.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and a.nsuppliercode=" + supplier.getNsuppliercode();
				List<SupplierContact> lstSupplierContact = (List<SupplierContact>) jdbcTemplate.query(strCategory,
						new SupplierContact());
				if (!lstSupplierContact.isEmpty()) {
					multilingualSupplierMatrixIDList.add("IDS_DELETESUPPLIERCONTACT");
					deletedSupplierMatrix.add(lstSupplierContact);
				}
				strCategory = "select a.*"
						+ " from supplierfile a " + " where a.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and a.nsuppliercode=" + supplier.getNsuppliercode();
				List<SupplierFile> lstSupplierfile = (List<SupplierFile>) jdbcTemplate.query(strCategory,
						new SupplierFile());
				if (!lstSupplierfile.isEmpty()) {
					multilingualSupplierMatrixIDList.add("IDS_DELETESUPPLIERFILE");
					deletedSupplierMatrix.add(lstSupplierfile);
				}
				String updateMatrixQuery = "update SupplierMatrix set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' ,nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsuppliercode="
						+ supplier.getNsuppliercode() + ";";

				updateMatrixQuery = updateMatrixQuery + "update suppliercontact set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' , nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsuppliercode="
						+ supplier.getNsuppliercode()+";";
				
				updateMatrixQuery = updateMatrixQuery + "update supplierfile set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' , nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsuppliercode="
						+ supplier.getNsuppliercode();
				
				//getJdbcTemplate().execute(updateMatrixQuery);
				jdbcTemplate.execute(updateMatrixQuery);


				auditUtilityFunction.fnInsertAuditAction(savedSupplierList, 1, null, multilingualIDList, userInfo);
			//	if (!deletedSupplierMatrix.isEmpty()) {
				auditUtilityFunction.fnInsertListAuditAction(deletedSupplierMatrix, 1, null, multilingualSupplierMatrixIDList, userInfo);
			//}
				// status code:200
				// return new ResponseEntity<>(supplier, HttpStatus.OK);
				return getSupplier(nSupplierCode, userInfo);
			} else {
				// status code:417
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
			}
			else
			{
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTODELETE",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * Need to validate that the specified supplier object is active.
	 * 
	 * @param supplier [Supplier] object holding detail to be deleted in supplier
	 *                 table
	 * @return response entity object holding response status and data of deleted
	 *         supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> approveSupplier(Supplier supplier, UserInfo userInfo) throws Exception {

		final Supplier suplierByID = (Supplier) getActiveSupplierById(supplier.getNsuppliercode(), userInfo);

		if (suplierByID == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			if (suplierByID.getNapprovalstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYAPPROVED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedSupplierList = new ArrayList<>();

				final String updateQueryString = "update supplier set  dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' ,napprovalstatus = "
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " where nsuppliercode="
						+ supplier.getNsuppliercode();

				//getJdbcTemplate().execute(updateQueryString);
				jdbcTemplate.execute(updateQueryString);

				supplier.setNapprovalstatus((short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());

				savedSupplierList.add(supplier);
				multilingualIDList.add("IDS_APPROVESUPPLIER");

				// fnInsertAuditAction(savedSupplierList, 1, null, multilingualIDList,
				// userInfo);
				auditUtilityFunction.fnInsertAuditAction(savedSupplierList, 2, Arrays.asList(suplierByID), multilingualIDList, userInfo);

				// status code:200
				return getSupplier(supplier.getNsuppliercode(), userInfo);

			}

		}

	}

	/**
	 * Need to validate that the specified supplier object is active.
	 * 
	 * @param supplier [Supplier] object holding detail to be blacked in supplier
	 *                 table
	 * @return response entity object holding response status and data of blacked
	 *         supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> blackListSupplier(Supplier supplier, UserInfo userInfo) throws Exception {

		final Supplier suplierByID = (Supplier) getActiveSupplierById(supplier.getNsuppliercode(), userInfo);

		if (suplierByID == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			if (suplierByID.getNapprovalstatus() == Enumeration.TransactionStatus.BLACKLIST.gettransactionstatus()) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYBLACKLIST", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if(suplierByID.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SELECTAPPROVERECORD", userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedSupplierList = new ArrayList<>();

				final String updateQueryString = "update supplier set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' , napprovalstatus = "
						+ Enumeration.TransactionStatus.BLACKLIST.gettransactionstatus() + " where nsuppliercode="
						+ supplier.getNsuppliercode();

				//getJdbcTemplate().execute(updateQueryString);
				jdbcTemplate.execute(updateQueryString);

				supplier.setNapprovalstatus((short) Enumeration.TransactionStatus.BLACKLIST.gettransactionstatus());

				savedSupplierList.add(supplier);
				multilingualIDList.add("IDS_BLACKLISTSUPPLIER");

				// fnInsertAuditAction(savedSupplierList, 1, null, multilingualIDList,
				// userInfo);

				auditUtilityFunction.fnInsertAuditAction(savedSupplierList, 2, Arrays.asList(suplierByID), multilingualIDList, userInfo);

				// status code:200
				return getSupplier(supplier.getNsuppliercode(), userInfo);
			}

		}

	}


	@Override
	public ResponseEntity<Object> getApprovedSupplier(UserInfo userInfo) throws Exception {

		final String getQuery = "select nsuppliercode,ssuppliername from supplier  where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode();

		List<Supplier> approvedSupplierList = (List<Supplier>) jdbcTemplate.query(getQuery, new Supplier());

		return new ResponseEntity<Object>(approvedSupplierList, HttpStatus.OK);
	}


	public ResponseEntity<Object> getSupplierFile(final int nsuppliercode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String query = "select tf.noffsetdcreateddate,tf.nsupplierfilecode,(select  count(nsupplierfilecode) from supplierfile where nsupplierfilecode>0 and nsuppliercode = "
				+ nsuppliercode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ") as ncount,tf.sdescription,"
				+ " tf.nsupplierfilecode as nprimarycode,tf.sfilename,tf.nsuppliercode,tf.ssystemfilename,"
				+ " tf.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when tf.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
				+ " end slinkname, tf.nfilesize," + " case when tf.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(tf.dcreateddate,'"
				+ objUserInfo.getSpgdatetimeformat() + "'),'-') end  as screateddate, "
				+ " tf.nlinkcode, case when tf.nlinkcode = -1 then tf.nfilesize::varchar(1000) else '-' end sfilesize"
				+ " from supplierfile tf,attachmenttype at, linkmaster lm  "
				+ " where at.nattachmenttypecode = tf.nattachmenttypecode and at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and lm.nlinkcode = tf.nlinkcode and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tf.nsuppliercode=" + nsuppliercode
				+ " order by tf.nsupplierfilecode;";
		//final List<SupplierFile> supplierFileList = (List<SupplierFile>) jdbcTemplate.query(query, new SupplierFile());
//getSpgsitedatetime   --getSpgdatetimeformat
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<SupplierFile> lstSupplierfile = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(query, new SupplierFile()),
						Arrays.asList("screateddate"),
						null, objUserInfo, false, null, false),
				new TypeReference<List<SupplierFile>>() {
				});
		
		//outputMap.put("supplierFile", supplierFileList);
		outputMap.put("supplierFile", lstSupplierfile);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	public Supplier checKSupplierIsPresent(final int nsuppliercode) throws Exception {
		String strQuery = "select nsuppliercode,napprovalstatus from supplier where"
				+ " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsuppliercode = " + nsuppliercode;
		
		//Supplier objTest = (Supplier) jdbcQueryForObject(strQuery, Supplier.class);
		Supplier objTest = (Supplier) jdbcUtilityFunction.queryForObject(strQuery, Supplier.class, jdbcTemplate);
		
		return objTest;
	}
	
	@Override
	public ResponseEntity<Object> createSupplierFile(MultipartHttpServletRequest request, final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		
		final String sQuery = " lock  table supplierfile "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		
		//getJdbcTemplate().execute(sQuery);
		jdbcTemplate.execute(sQuery);
		
		final List<SupplierFile> lstReqSupplierFile = objMapper.readValue(request.getParameter("supplierfile"), new TypeReference<List<SupplierFile>>() {});
		
		if(lstReqSupplierFile != null && lstReqSupplierFile.size() > 0)
		{
			final Supplier objSupplier = checKSupplierIsPresent(lstReqSupplierFile.get(0).getNsuppliercode());
			
			if(objSupplier != null) 
			{
				if(objSupplier.getNapprovalstatus() != Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if(lstReqSupplierFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					
					sReturnString = ftpUtilityFunction.getFileFTPUpload(request,-1, objUserInfo); //Folder Name - master
				}
				
				if(Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
//					final String sQuery = "select * from supplierfile where nsuppliercode = "+lstReqSupplierFile.get(0).getNsuppliercode()
//											+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//											//+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
//					final SupplierFile lstSupplierFiles = (SupplierFile) jdbcQueryForObject(sQuery,  SupplierFile.class);
					
				
						
				
						//SupplierFile objSupplierFile=lstReqSupplierFile.get(0);
						
//						if(objSupplierFile.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) 
//						{
//							if(lstSupplierFiles!=null) {
//									
//								final SupplierFile SupplierFileBeforeSave = new SupplierFile(
//										lstSupplierFiles);
//
//								final List<Object> defaultListBeforeSave = new ArrayList<>();
//								defaultListBeforeSave.add(SupplierFileBeforeSave);
//								
//								
//							  final String updateQueryString =" update supplierfile set ndefaultstatus="
//							  		+ " "+Enumeration.TransactionStatus.NO.gettransactionstatus() +" "
//							  		+ " where nsupplierfilecode ="+lstSupplierFiles.getNsupplierfilecode();
//									  getJdbcTemplate().execute(updateQueryString);
//									  
//									  lstSupplierFiles.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
//										final List<Object> defaultListAfterSave = new ArrayList<>();
//										defaultListAfterSave.add(lstSupplierFiles);
//
//										fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
//												Arrays.asList("IDS_EDITSUPPLIERFILE"), objUserInfo);
//							
//					    }
					
					//}
					
//					final Instant utcDate = objGeneral.getUTCDateTime();
//					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//					LocalDateTime ldt = Date.from(utcDate).toInstant().atZone(ZoneId.of(objUserInfo.getStimezoneid())).toLocalDateTime();
					
					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
                    final int noffset=dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
					
					lstReqSupplierFile.forEach(objtf-> {
						//objtf.setDcreateddate(utcDate);
						objtf.setDcreateddate(instantDate);
						if(objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							//objtf.setScreateddate(formatter.format(ldt));
							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(noffset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						}
						
					});
					
					String sequencequery ="select nsequenceno from seqnocontactmaster where stablename ='supplierfile'";
					//int nsequenceno =getJdbcTemplate().queryForObject(sequencequery, Integer.class);
					int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class, jdbcTemplate);
					
					nsequenceno++;
					String insertquery = "Insert into supplierfile(nsupplierfilecode,nsuppliercode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nsitecode,nstatus)"
							+ "values (" + nsequenceno + "," + lstReqSupplierFile.get(0).getNsuppliercode() + ","
							+ lstReqSupplierFile.get(0).getNlinkcode() + ","
							+ lstReqSupplierFile.get(0).getNattachmenttypecode() + "," + " N'"
							+ stringUtilityFunction.replaceQuote(lstReqSupplierFile.get(0).getSfilename()) + "',N'"
							+ stringUtilityFunction.replaceQuote(lstReqSupplierFile.get(0).getSdescription()) + "',"
							+ lstReqSupplierFile.get(0).getNfilesize() + "," + " '"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ lstReqSupplierFile.get(0).getNoffsetdcreateddate() + "," + objUserInfo.getNtimezonecode()
							+ ",N'" + lstReqSupplierFile.get(0).getSsystemfilename() + "','"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
							//+ lstReqSupplierFile.get(0).getNdefaultstatus() + ","
							+objUserInfo.getNmastersitecode()+","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					
                         	//getJdbcTemplate().execute(insertquery);
                         	jdbcTemplate.execute(insertquery);
				
					String updatequery="update seqnocontactmaster set nsequenceno ="+nsequenceno+" where stablename ='supplierfile'";
					
					//getJdbcTemplate().execute(updatequery);
					jdbcTemplate.execute(updatequery);
					
					final List<String> multilingualIDList = new ArrayList<>();
					
					multilingualIDList.add(lstReqSupplierFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()?"IDS_ADDSUPPLIERFILE": "IDS_ADDSUPPLIERLINK");
					final List<Object> listObject = new ArrayList<Object>();
//					for(int i=0;i<lstReqSupplierFile.size();i++) {
//						lstReqSupplierFile.get(i).setNsupplierfilecode(nsequenceno);
//					}
					String auditqry = "select * from supplierfile where nsuppliercode = "+ lstReqSupplierFile.get(0).getNsuppliercode() + " and nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsupplierfilecode = "+ nsequenceno ;
					final List<SupplierFile> lstvalidate=(List<SupplierFile>) jdbcTemplate.query(auditqry, new SupplierFile());
					
					listObject.add(lstvalidate);
					
					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, objUserInfo);
				} 
				else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUPPLIERALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return (getSupplierFile(lstReqSupplierFile.get(0).getNsuppliercode(), objUserInfo));
		} else {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	
	
	@Override
	public ResponseEntity<Object> updateSupplierFile(MultipartHttpServletRequest request, UserInfo objUserInfo) throws Exception
	{
		final ObjectMapper objMapper = new ObjectMapper();
		
		final List<SupplierFile> lstSupplierFile = objMapper.readValue(request.getParameter("supplierfile"), new TypeReference<List<SupplierFile>>() {});
		if(lstSupplierFile != null && lstSupplierFile.size() > 0) 
		{
			final SupplierFile objSupplierFile = lstSupplierFile.get(0);
			final Supplier objSupplier = checKSupplierIsPresent(objSupplierFile.getNsuppliercode());
			
			if(objSupplier != null) 
			{
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				
				if(isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if(objSupplierFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}
				
				if(Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select * from supplierfile where"
							+" nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsupplierfilecode = "+objSupplierFile.getNsupplierfilecode();
					
					//final SupplierFile objTF = (SupplierFile) jdbcQueryForObject(sQuery, SupplierFile.class);
					final SupplierFile objTF = (SupplierFile) jdbcUtilityFunction.queryForObject(sQuery, SupplierFile.class, jdbcTemplate);
					
					//final String sCheckDefaultQuery = "select * from supplierfile where nsuppliercode = "+objSupplierFile.getNsuppliercode()
					//	+" and nsupplierfilecode!="+objSupplierFile.getNsupplierfilecode()
					//	+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					//final List<SupplierFile> lstDefSupplierFiles = (List<SupplierFile>) jdbcTemplate.query(sCheckDefaultQuery, new SupplierFile());
					
					
					if(objTF != null) {
						String ssystemfilename = "";
						if(objSupplierFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							ssystemfilename = objSupplierFile.getSsystemfilename();
						}
							
//					if(lstSupplierFile.get(0).getNdefaultstatus()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
//						
//						final String sDefaultQuery = "select * from supplierfile where nsuppliercode = "+objSupplierFile.getNsuppliercode()
//							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
//						final List<SupplierFile> lstSupplierFiles = (List<SupplierFile>) jdbcTemplate.query(sDefaultQuery, new SupplierFile());
//						
//						if(!lstSupplierFiles.isEmpty()) {
//						  final String updateQueryString =" update supplierfile set ndefaultstatus="
//							  		+ " "+Enumeration.TransactionStatus.NO.gettransactionstatus()+" "
//							  		+ " where nsupplierfilecode ="+lstSupplierFiles.get(0).getNsupplierfilecode();
//									  getJdbcTemplate().execute(updateQueryString);
//						}
//					}
//					else {
//						final String sDefaultQuery = "select * from supplierfile where nsuppliercode = "+objSupplierFile.getNsuppliercode()
//							+" and nsupplierfilecode="+objSupplierFile.getNsupplierfilecode()+""
//							+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+" and ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus();
//						final List<SupplierFile> lstSupplierFiles = (List<SupplierFile>) jdbcTemplate.query(sDefaultQuery, new SupplierFile());
//					
//						if(lstDefSupplierFiles.size()>0) {
//							 if(!lstSupplierFiles.isEmpty()) 
//							 {
//								 final String sEditDefaultQuery = "update supplierfile set "
//										+ " ndefaultstatus = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
//										+ " where nsupplierfilecode = "+lstDefSupplierFiles.get(lstDefSupplierFiles.size()-1).getNsupplierfilecode();
//										 getJdbcTemplate().execute(sEditDefaultQuery);
//							 }
//						}
//						else {
//							return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.DEFAULTCANNOTCHANGED.getreturnstatus(),
//									objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//						 }
//				  }
					final String sUpdateQuery = "update supplierfile set sfilename=N'"
							+ stringUtilityFunction.replaceQuote(objSupplierFile.getSfilename()) + "'," + " sdescription=N'"
							+ stringUtilityFunction.replaceQuote(objSupplierFile.getSdescription()) + "', ssystemfilename= N'"
							+ ssystemfilename + "'," + " nattachmenttypecode = "
							+ objSupplierFile.getNattachmenttypecode() + ", nlinkcode="
							+ objSupplierFile.getNlinkcode() + "," + " nfilesize = "
							+ objSupplierFile.getNfilesize() + ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
							+ "' "
							//+ ", " + "ndefaultstatus = " + objSupplierFile.getNdefaultstatus() + ""
							+ "  where nsupplierfilecode = " + objSupplierFile.getNsupplierfilecode();
				 objSupplierFile.setDcreateddate(objTF.getDcreateddate());
				 
				 //getJdbcTemplate().execute(sUpdateQuery);
				 jdbcTemplate.execute(sUpdateQuery);
						
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList.add(objSupplierFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()?"IDS_EDITSUPPLIERFILE": "IDS_EDITSUPPLIERLINK");
						lstOldObject.add(objTF);
						
						auditUtilityFunction.fnInsertAuditAction(lstSupplierFile, 2, lstOldObject, multilingualIDList, objUserInfo);
						return (getSupplierFile(objSupplierFile.getNsuppliercode(), objUserInfo));
					} else {
						//status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					//status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUPPLIERALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}



	@Override
	public ResponseEntity<Object> deleteSupplierFile(SupplierFile objSupplierFile, UserInfo objUserInfo)
			throws Exception {
		final Supplier supplier = checKSupplierIsPresent(objSupplierFile.getNsuppliercode());
		if (supplier != null) {
			if(supplier.getNapprovalstatus() != Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			if (objSupplierFile != null) {
				final String sQuery = "select * from supplierfile where"
						+ " nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsupplierfilecode = "+ objSupplierFile.getNsupplierfilecode();
				
				//final SupplierFile objTF = (SupplierFile) jdbcQueryForObject(sQuery, SupplierFile.class);
				final SupplierFile objTF = (SupplierFile) jdbcUtilityFunction.queryForObject(sQuery, SupplierFile.class, jdbcTemplate);
				
				if (objTF != null) {
					if (objSupplierFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
//						deleteFTPFile(Arrays.asList(objTF.getSsystemfilename()), "master", objUserInfo);
					} else {
						objSupplierFile.setScreateddate(null);
					}

//					if (objTF.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
//						final String sDeleteQuery = "select * from supplierfile where " + " nsupplierfilecode !="
//								+ objSupplierFile.getNsupplierfilecode() + "" + " and nsuppliercode="
//								+ objSupplierFile.getNsuppliercode() + "" + " and nstatus = "
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//						List<SupplierFile> lstSupplierFiles = (List<SupplierFile>) jdbcTemplate.query(sDeleteQuery,
//								new SupplierFile());
//						String sDefaultQuery = "";
//						if (lstSupplierFiles.isEmpty()) {
//							sDefaultQuery = " update supplierfile set  " + "  dmodifieddate ='"
//									+ getCurrentDateTime(objUserInfo) + "', ntzmodifieddate ="
//									+ objUserInfo.getNtimezonecode() + ", noffsetdmodifieddate ="
//									+ getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + " ,nstatus = "
//									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
//									+ " where nsupplierfilecode = " + objSupplierFile.getNsupplierfilecode();
//						} else {
//							sDefaultQuery = "update supplierfile set " + "  dmodifieddate ='"
//									+ getCurrentDateTime(objUserInfo) + "', ntzmodifieddate ="
//									+ objUserInfo.getNtimezonecode() + ", noffsetdmodifieddate ="
//									+ getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + ", ndefaultstatus = "
//									+ Enumeration.TransactionStatus.YES.gettransactionstatus()
//									+ " where nsupplierfilecode = "
//									+ lstSupplierFiles.get(lstSupplierFiles.size() - 1).getNsupplierfilecode();
//						}
//						getJdbcTemplate().execute(sDefaultQuery);
//					}
					final String sUpdateQuery = "update supplierfile set" + "  dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
							+ ", nstatus = " + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ " where nsupplierfilecode = " + objSupplierFile.getNsupplierfilecode();
					
					//getJdbcTemplate().execute(sUpdateQuery);
					jdbcTemplate.execute(sUpdateQuery);
					
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList
							.add(objSupplierFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_DELETESUPPLIERFILE"
									: "IDS_DELETESUPPLIERLINK");
					lstObject.add(objSupplierFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return getSupplierFile(objSupplierFile.getNsuppliercode(), objUserInfo);
		} else {
			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUPPLIERALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> editSupplierFile(final SupplierFile objSupplierFile, final UserInfo objUserInfo)
			throws Exception {
		final String sEditQuery = "select s.napprovalstatus, tf.nsupplierfilecode, tf.nsuppliercode, tf.nlinkcode, tf.nattachmenttypecode, tf.sfilename, tf.sdescription, tf.nfilesize,"
				+ " tf.ssystemfilename,  lm.jsondata->>'slinkname' as slinkname"
				+ " from supplierfile tf, linkmaster lm,supplier s where lm.nlinkcode = tf.nlinkcode" + " and tf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nsuppliercode=s.nsuppliercode and tf.nsupplierfilecode = "
				+ objSupplierFile.getNsupplierfilecode();
		
		//final SupplierFile objTF = (SupplierFile) jdbcQueryForObject(sEditQuery, SupplierFile.class);
		final SupplierFile objTF = (SupplierFile) jdbcUtilityFunction.queryForObject(sEditQuery, SupplierFile.class, jdbcTemplate);
		
		if (objTF != null) {
			return new ResponseEntity<Object>(objTF, HttpStatus.OK);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public Map<String, Object> viewAttachedSupplierFile(SupplierFile objSupplierFile, UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		final Supplier objsupplier = checKSupplierIsPresent(objSupplierFile.getNsuppliercode());
		if (objsupplier != null) {
			String sQuery = "select * from supplierfile where"
					+ " nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsupplierfilecode = "+ objSupplierFile.getNsupplierfilecode();
			
			//final SupplierFile objTF = (SupplierFile) jdbcQueryForObject(sQuery, SupplierFile.class);
			final SupplierFile objTF = (SupplierFile) jdbcUtilityFunction.queryForObject(sQuery, SupplierFile.class, jdbcTemplate);
			
			if (objTF != null) {
				if (objTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(objTF.getSsystemfilename(), -1, objUserInfo, "", "");// Folder Name - master
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where"
							+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nlinkcode=" + objTF.getNlinkcode() ;
					
					//LinkMaster objlinkmaster = (LinkMaster) jdbcQueryForObject(sQuery, LinkMaster.class);
					LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class, jdbcTemplate);
					
					map.put("AttachLink", objlinkmaster.getSlinkname() + objTF.getSfilename());
					objSupplierFile.setScreateddate(null);
				}
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList
						.add(objSupplierFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWSUPPLIERFILE"
								: "IDS_VIEWSUPPLIERLINK");
				lstObject.add(objSupplierFile);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);

//				return new ResponseEntity<Object>(map, HttpStatus.OK);
			} else {
				// status code:417
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			// status code:417
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUPPLIERALREADYDELETED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		return map;
	}
	
	public ResponseEntity<Object> getSupplierContact(final int nsuppliercode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strQuery = "select sc.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus  from  suppliercontact sc ,transactionstatus ts where "
				+ " ts.ntranscode=sc.ndefaultstatus  and sc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sc.nsuppliercode = "
				+ nsuppliercode;

		final List<SupplierContact> supplierContactList = (List<SupplierContact>) jdbcTemplate.query(strQuery,
				new SupplierContact());

		outputMap.put("SupplierContact", supplierContactList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private Supplier getSupplierByIdForInsert(final int nsuppliercode, final UserInfo userInfo) throws Exception {

		final String strQuery = "  select  a.napprovalstatus,a.nsuppliercode,a.ssuppliername,a.ntransactionstatus,a.nsitecode,a.nstatus,"
				+ " coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as stransdisplaystatus"
				+ " from supplier a,transactionstatus ts where ts.ntranscode = a.ntransactionstatus "
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsuppliercode = "
				+ nsuppliercode;

		//return (Supplier) jdbcQueryForObject(strQuery, Supplier.class);
		return (Supplier) jdbcUtilityFunction.queryForObject(strQuery, Supplier.class, jdbcTemplate);
	}

	private SupplierContact getSupplierContactListByName(final String supplierContactName, final int nsuppliercode)
			throws Exception {
		final String strQuery = "select  ssuppliercontactname from suppliercontact where ssuppliercontactname = N'"
				+ stringUtilityFunction.replaceQuote(supplierContactName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsuppliercode = " + nsuppliercode;
		
		//return (SupplierContact) jdbcQueryForObject(strQuery, SupplierContact.class);
		return (SupplierContact) jdbcUtilityFunction.queryForObject(strQuery, SupplierContact.class, jdbcTemplate);
	}

	private SupplierContact getSupplierContactByDefault(final int nsuppliercode) throws Exception {
		final String strQuery = " select a.*,"
				+ " coalesce(ts.jsondata->'sactiondisplaystatus'->>'en-US', ts.jsondata->'sactiondisplaystatus'->>'en-US') as sdisplaystatus from suppliercontact a,transactionstatus ts "
				+ " where ts.ntranscode = a.ndefaultstatus and  a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and a.nsuppliercode = " + nsuppliercode;
		
		//return (SupplierContact) jdbcQueryForObject(strQuery, SupplierContact.class);
		return (SupplierContact) jdbcUtilityFunction.queryForObject(strQuery, SupplierContact.class, jdbcTemplate);
	}

	
	@Override
	public ResponseEntity<Object> createSupplierContact(SupplierContact objSupplierContact, UserInfo userInfo)
			throws Exception {
		
		final String sQuery = " lock  table locksupplier "+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		
		//getJdbcTemplate().execute(sQuery);
		jdbcTemplate.execute(sQuery);
		
		//final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final List<Object> savedSupplierContactList = new ArrayList<>();

		final Supplier nsuppliercode = (Supplier) getSupplierByIdForInsert(objSupplierContact.getNsuppliercode(),
				userInfo);

		if (nsuppliercode == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUPPLIERALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			if(nsuppliercode.getNapprovalstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			final SupplierContact supplierContactByName = getSupplierContactListByName(
					objSupplierContact.getSsuppliercontactname(), objSupplierContact.getNsuppliercode());

			if (supplierContactByName == null) {
				if (objSupplierContact.getNdefaultstatus() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {

					final SupplierContact defaultSupplierContact = getSupplierContactByDefault(
							objSupplierContact.getNsuppliercode());

					if (defaultSupplierContact != null) {
						final List<String> multilingualIDList = new ArrayList<>();

						
						final SupplierContact supplierContactBeforeSave = SerializationUtils.clone(defaultSupplierContact);

						final List<Object> defaultListBeforeSave = new ArrayList<>();
						defaultListBeforeSave.add(supplierContactBeforeSave);

						defaultSupplierContact
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = " update suppliercontact set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus()
								+ " where nsuppliercontactcode =" + defaultSupplierContact.getNsuppliercontactcode();
						
						//getJdbcTemplate().execute(updateQueryString);
						jdbcTemplate.execute(updateQueryString);

						final List<Object> defaultListAfterSave = new ArrayList<>();
						defaultListAfterSave.add(defaultSupplierContact);

						multilingualIDList.add("IDS_EDITSUPPLIERCONTACT");

						auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
						 multilingualIDList, userInfo);
						 multilingualIDList.clear();
					}

				}
				String supplierContactSeq = "";
				supplierContactSeq = "select nsequenceno from seqnocontactmaster where stablename='suppliercontact'";
				//int seqNo = jdbcTemplate.queryForObject(supplierContactSeq, Integer.class);
				int seqNo = (int) jdbcUtilityFunction.queryForObject(supplierContactSeq, Integer.class, jdbcTemplate);
				
				seqNo = seqNo + 1;
				String sEmail = objSupplierContact.getSemail() == null ? "" : objSupplierContact.getSemail();
				String sMoblieNo = objSupplierContact.getSmobileno() == null ? "" : objSupplierContact.getSmobileno();
				String stelephoneNo = objSupplierContact.getStelephoneno() == null ? "" : objSupplierContact.getStelephoneno();
				String sDescription = objSupplierContact.getSdescription() == null ? ""
						: objSupplierContact.getSdescription();
				String sDesignation = objSupplierContact.getSdesignation() == null ? ""
						: objSupplierContact.getSdesignation();
				final List<String> multilingualIDList = new ArrayList<>();

				String supplierContactInsert = "insert into suppliercontact(nsuppliercontactcode,nsuppliercode,ssuppliercontactname,sdescription,sdesignation,stelephoneno,smobileno,semail,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
						+ " values(" + seqNo + "," + objSupplierContact.getNsuppliercode() + ",N'"
						+ stringUtilityFunction.replaceQuote(objSupplierContact.getSsuppliercontactname()) + "',N'"
						+ stringUtilityFunction.replaceQuote(sDescription) + "',N'"
						+ stringUtilityFunction.replaceQuote(sDesignation) + "',N'"
						 + stringUtilityFunction.replaceQuote(stelephoneNo) + "',N'"
						+ stringUtilityFunction.replaceQuote(sMoblieNo) + "',N'"
						+ stringUtilityFunction.replaceQuote(sEmail) + "'," + objSupplierContact.getNdefaultstatus() + "," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					    + userInfo.getNmastersitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

				//getJdbcTemplate().execute(supplierContactInsert);
				jdbcTemplate.execute(supplierContactInsert);

				supplierContactSeq = "update seqnocontactmaster set nsequenceno=" + seqNo
						+ " where stablename='suppliercontact'";
				//getJdbcTemplate().execute(supplierContactSeq);
				jdbcTemplate.execute(supplierContactSeq);

				multilingualIDList.add("IDS_ADDSUPPLIERCONTACT");
				objSupplierContact.setNsuppliercontactcode(seqNo);
				savedSupplierContactList.add(objSupplierContact);

				auditUtilityFunction.fnInsertAuditAction(savedSupplierContactList, 1, null, multilingualIDList, userInfo);

				if (objSupplierContact.isIsreadonly() != true) {
					return getSupplierContact(objSupplierContact.getNsuppliercode(), userInfo);

				} else {
					return getSupplier(0, userInfo);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public SupplierContact getActiveSupplierContactById(final int nsuppliercontactcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select a.*,s.napprovalstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
				+ " from suppliercontact a, transactionstatus ts,supplier s where a.ndefaultstatus = ts.ntranscode and nsuppliercontactcode > 0 "
				+ "  and s.nsuppliercode = a.nsuppliercode and  a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsuppliercontactcode = "
				+ nsuppliercontactcode;/// + users.getnmastersitecode();

		//return (SupplierContact) jdbcQueryForObject(strQuery, SupplierContact.class);
		return (SupplierContact) jdbcUtilityFunction.queryForObject(strQuery, SupplierContact.class, jdbcTemplate);
	}


	@Override
	public ResponseEntity<Object> updateSupplierContact(SupplierContact objSupplierContact, UserInfo userInfo)
			throws Exception {

		final SupplierContact supplierContactByID = (SupplierContact) getActiveSupplierContactById(
				objSupplierContact.getNsuppliercontactcode(), userInfo);
		//Integer nSupplierCode = null;
		final List<String> multilingualIDList = new ArrayList<>();

		final List<Object> listBeforeSave = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		if (supplierContactByID == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nsuppliercontactcode from suppliercontact where ssuppliercontactname = N'"
					+ stringUtilityFunction.replaceQuote(objSupplierContact.getSsuppliercontactname()) + "' and nsuppliercontactcode <> "
					+ objSupplierContact.getNsuppliercontactcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + userInfo.getNmastersitecode();

			final List<SupplierContact> suppliercontactList = (List<SupplierContact>) jdbcTemplate.query(queryString,
					new SupplierContact());

			if (supplierContactByID.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()
					&& supplierContactByID.getNdefaultstatus() != objSupplierContact.getNdefaultstatus()) {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.DEFAULTCANNOTCHANGED.getreturnstatus(),
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			if (suppliercontactList.isEmpty()) {
				if (objSupplierContact.getNdefaultstatus() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {

					final SupplierContact defaultSupplierContact = getSupplierContactByDefault(
							objSupplierContact.getNsuppliercode());

					if (defaultSupplierContact != null && defaultSupplierContact
							.getNsuppliercontactcode() != objSupplierContact.getNsuppliercontactcode()) {

						final SupplierContact supplierContactBeforeSave = SerializationUtils.clone(defaultSupplierContact);

						listBeforeSave.add(supplierContactBeforeSave);

						defaultSupplierContact
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = " update suppliercontact set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus()
								+ " where nsuppliercontactcode =" + defaultSupplierContact.getNsuppliercontactcode();
						//getJdbcTemplate().execute(updateQueryString);
						jdbcTemplate.execute(updateQueryString);

						listAfterSave.add(defaultSupplierContact);
						// multilingualIDList.add("IDS_EDITCLIENTDEPARTMENT");

					}

				}
				final String updateQueryString = "update suppliercontact set ssuppliercontactname='"
						+ stringUtilityFunction.replaceQuote(objSupplierContact.getSsuppliercontactname()) + "'"
						 + ", sdescription =N'"
						+ stringUtilityFunction.replaceQuote(objSupplierContact.getSdescription()) + "', sdesignation =N'"
						+ stringUtilityFunction.replaceQuote(objSupplierContact.getSdesignation()) + "', smobileno =N'"
						+ stringUtilityFunction.replaceQuote(objSupplierContact.getSmobileno()) + "', stelephoneno= N'"
						+ stringUtilityFunction.replaceQuote(objSupplierContact.getStelephoneno()) + "', semail= N'"
						+ stringUtilityFunction.replaceQuote(objSupplierContact.getSemail()) + "', ndefaultstatus= "
						+ objSupplierContact.getNdefaultstatus() + ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						 + "' where nsuppliercontactcode="
						+ objSupplierContact.getNsuppliercontactcode();
//				 final String updateQueryString = "update suppliercontact set ssuppliercontactname='" 
//		    				+ replaceQuote(objSupplierContact.getSsuppliercontactname()) + "',"
//		    				
//		    			    + "dmodifieddate='"+getCurrentDateTime(userInfo)
//				    	+ "',ntzmodifieddate="+userInfo.getNtimezonecode()
				// + ",
				// noffsetdmodifieddate="+getCurrentDateTimeOffset(userInfo.getStimezoneid())
				// +", ntransactionstatus= " + objSupplierContact.getNdefaultstatus() +
//		    				" where nsuppliercontactcode=" + objSupplierContact.getNsuppliercontactcode();

				//getJdbcTemplate().execute(updateQueryString);
				jdbcTemplate.execute(updateQueryString);

				listBeforeSave.add(supplierContactByID);

				listAfterSave.add(objSupplierContact);

				multilingualIDList.add("IDS_EDITSUPPLIERCONTACT");

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);

				// status code:200
				// return new ResponseEntity<>(supplier, HttpStatus.OK);
				return getSupplierContact(objSupplierContact.getNsuppliercode(), userInfo);
			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);

			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteSupplierContact(final SupplierContact objSupplierContact,
			final UserInfo objUserInfo) throws Exception {
		String sQuery = "select * from suppliercontact where nsuppliercontactcode = "
				+ objSupplierContact.getNsuppliercontactcode() + " and nstatus  = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		//SupplierContact objsuppliercontact = (SupplierContact) jdbcQueryForObject(sQuery, SupplierContact.class);
		SupplierContact objsuppliercontact = (SupplierContact) jdbcUtilityFunction.queryForObject(sQuery, SupplierContact.class, jdbcTemplate);
		
		if (objsuppliercontact != null) {
			if(objSupplierContact.getNapprovalstatus()!=Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			if (objsuppliercontact.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(),
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				final List<String> strArray = new ArrayList<>();
				final List<Object> objlst = new ArrayList<>();
				String sdeleteQuery = "update suppliercontact set dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)+"', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsuppliercontactcode="
						+ objSupplierContact.getNsuppliercontactcode();
				//getJdbcTemplate().execute(sdeleteQuery);
				jdbcTemplate.execute(sdeleteQuery);
				
				
				objlst.add(objSupplierContact);
				// strArray.add("IDS_DELETETESTSECTION");
				strArray.add("IDS_DELETESUPPLIERCONTACT");
				auditUtilityFunction.fnInsertAuditAction(objlst, 1, null, strArray, objUserInfo);
				return getSupplierContact(objSupplierContact.getNsuppliercode(), objUserInfo);
			}
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	

}
