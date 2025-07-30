package com.agaramtech.qualis.contactmaster.service.suppliercategory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.contactmaster.model.SupplierCategory;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on
 * "contactmastersuppliercategory" table by implementing methods from its
 * interface.
 * 
 * @author ATE184
 * @version 9.0.0.1
 * @since 30- Jun- 2020
 */

@AllArgsConstructor
@Repository
public class SupplierCategoryDAOImpl implements SupplierCategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SupplierCategoryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active suppliercategory for the
	 * specified site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         suppliercategory
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getSupplierCategory(final UserInfo userInfo) throws Exception {

		final String strQuery = "select s.nsuppliercatcode, s.ssuppliercatname, s.sdescription, s.nsitecode, s.nstatus from suppliercategory s where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode = "
				+ userInfo.getNmastersitecode() + " and s.nsuppliercatcode > 0";
		
		return new ResponseEntity<>((List<SupplierCategory>) jdbcTemplate.query(strQuery, new SupplierCategory()),
				HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active suppliercategory for the
	 * specified site.
	 * 
	 * @param nsuppliercode [int] key of supplier object for which the list is to be
	 *                      fetched
	 * @return response entity object holding response status and list of all active
	 *         suppliercategory
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getSupplierCategoryBySupplierCode(final int nmasterSiteCode, final int nsuppliercode)
			throws Exception {

		final String strQuery = "select s.nsuppliercatcode, s.ssuppliercatname, s.sdescription, s.nsitecode, s.nstatus from suppliercategory s where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode = " + nmasterSiteCode
				+ " and s.nsuppliercatcode > 0 and s.nsuppliercatcode not in "
				+ " (Select ncategorycode from suppliermatrix where ntypecode = 1 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsuppliercode = " + nsuppliercode
				+ " and nsitecode = " + nmasterSiteCode + ")";
		return new ResponseEntity<>((List<SupplierCategory>) jdbcTemplate.query(strQuery, new SupplierCategory()),
				HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active contactmastersuppliercategory object
	 * based on the specified nsuppliercatcode.
	 * 
	 * @param nsuppliercatcode [int] primary key of contactmastersuppliercategory
	 *                         object
	 * @return response entity object holding response status and data of
	 *         suppliercategory object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public SupplierCategory getActiveSupplierCategoryById(int nsuppliercatcode, UserInfo userInfo) throws Exception {
		final String strQuery = "select s.nsuppliercatcode, s.ssuppliercatname, s.sdescription, s.nsitecode, s.nstatus from suppliercategory s where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode = "
				+ userInfo.getNmastersitecode() + " and s.nsuppliercatcode = " + nsuppliercatcode;

		// return (SupplierCategory) jdbcQueryForObject (strQuery,
		// SupplierCategory.class);
		return (SupplierCategory) jdbcUtilityFunction.queryForObject(strQuery, SupplierCategory.class, jdbcTemplate);

	}

	/**
	 * This method is used to add a new entry to contactmastersuppliercategory
	 * table. Need to check for duplicate entry of suppliercategory name for the
	 * specified site before saving into database.
	 * 
	 * @param contactmasterSupplierCategory [SupplierCategory] object holding
	 *                                      details to be added in
	 *                                      contactmastersuppliercategory table
	 * @param userInfo                      [UserInfo] object holding details of
	 *                                      loggedin user
	 * @return response entity object holding response status and data of added
	 *         suppliercategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSupplierCategory(SupplierCategory contactmasterSupplierCategory,
			UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table suppliercategory "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		// getJdbcTemplate().execute(sQuery);
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSupplierCategoryList = new ArrayList<>();

		final SupplierCategory supplierCategoryListByName = getSupplierCategoryListByName(
				contactmasterSupplierCategory.getSsuppliercatname(), contactmasterSupplierCategory.getNsitecode());

		if (supplierCategoryListByName == null) {
//			contactmasterSupplierCategory = (SupplierCategory) insertObject(contactmasterSupplierCategory,
//					SeqNoContactMaster.class, "nsequenceno");

			String sequencenoquery = "select nsequenceno from seqnocontactmaster where stablename ='suppliercategory'";
			// int nsequenceno = getJdbcTemplate().queryForObject(sequencenoquery,
			// Integer.class);
			int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencenoquery, Integer.class, jdbcTemplate);

			nsequenceno++;
			String insertquery = "Insert into suppliercategory (nsuppliercatcode, ssuppliercatname, sdescription, dmodifieddate, nsitecode, nstatus) "
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(contactmasterSupplierCategory.getSsuppliercatname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(contactmasterSupplierCategory.getSdescription()) + "', '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			// getJdbcTemplate().execute(insertquery);
			jdbcTemplate.execute(insertquery);

			String updatequery = "update seqnocontactmaster set nsequenceno =" + nsequenceno
					+ " where stablename='suppliercategory'";
			// getJdbcTemplate().execute(updatequery);
			jdbcTemplate.execute(updatequery);

			contactmasterSupplierCategory.setNsuppliercatcode(nsequenceno);
			savedSupplierCategoryList.add(contactmasterSupplierCategory);
			multilingualIDList.add("IDS_ADDCONTACTMASTERSUPPLIERCATEGORY");

			// fnInsertAuditAction(savedSupplierCategoryList, 1, null, multilingualIDList,
			// userInfo);
			auditUtilityFunction.fnInsertAuditAction(savedSupplierCategoryList, 1, null, multilingualIDList, userInfo);

			// status code:200
			return getSupplierCategory(userInfo);
			// return getSupplierCategory(contactmasterSupplierCategory.getNsitecode());

		} else {
			// Conflict = 409 - Duplicate entry
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the active contactmastersuppliercategory objects
	 * for the specified suppliercategory name and site.
	 * 
	 * @param ssuppliercatname [String] ssuppliercatname for which the records are
	 *                         to be fetched
	 * @param nmasterSiteCode  [int] primary key of site object
	 * @return list of active contactmastersuppliercategory based on the specified
	 *         suppliercategory name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private SupplierCategory getSupplierCategoryListByName(final String suppliercategory, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select nsuppliercatcode from suppliercategory where ssuppliercatname = N'"
				+ stringUtilityFunction.replaceQuote(suppliercategory) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;

		// return (SupplierCategory) jdbcQueryForObject (strQuery,
		// SupplierCategory.class);
		return (SupplierCategory) jdbcUtilityFunction.queryForObject(strQuery, SupplierCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in contactmastersuppliercategory table.
	 * Need to validate that the suppliercategory object to be updated is active
	 * before updating details in database. Need to check for suppliercategory entry
	 * of ssuppliercatname for the specified site before saving into database.
	 * 
	 * @param contactmasterSupplierCategory [SupplierCategory] object holding
	 *                                      details to be updated in
	 *                                      contactmastersuppliercategory table
	 * @param userInfo                      [UserInfo] object holding details of
	 *                                      loggedin user
	 * @return response entity object holding response status and data of updated
	 *         suppliercategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> updateSupplierCategory(SupplierCategory contactmasterSupplierCategory,
			UserInfo userInfo) throws Exception {

		final SupplierCategory supplierCategory = getActiveSupplierCategoryById(
				contactmasterSupplierCategory.getNsuppliercatcode(), userInfo);

		if (supplierCategory == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nsuppliercatcode from suppliercategory where ssuppliercatname = N'"
					+ stringUtilityFunction.replaceQuote(contactmasterSupplierCategory.getSsuppliercatname())
					+ "' and nsuppliercatcode <> " + contactmasterSupplierCategory.getNsuppliercatcode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
					+ userInfo.getNmastersitecode();

			final List<SupplierCategory> suppliercategoryList = (List<SupplierCategory>) jdbcTemplate.query(queryString,
					new SupplierCategory());

			if (suppliercategoryList.isEmpty()) {
				final String updateQueryString = "update suppliercategory set ssuppliercatname=N'"
						+ stringUtilityFunction.replaceQuote(contactmasterSupplierCategory.getSsuppliercatname())
						+ "', sdescription =N'"
						+ stringUtilityFunction.replaceQuote(contactmasterSupplierCategory.getSdescription()) + "', "
						+ " dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nsuppliercatcode=" + contactmasterSupplierCategory.getNsuppliercatcode() 
						+ " and nsitecode =" + userInfo.getNmastersitecode();

				// getJdbcTemplate().execute(updateQueryString);
				jdbcTemplate.execute(updateQueryString);

				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITCONTACTMASTERSUPPLIERCATEGORY");

				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(contactmasterSupplierCategory);

				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(supplierCategory);

				// fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
				// userInfo);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);

				// return getSupplierCategory(contactmasterSupplierCategory.getNsitecode());
				// status code:200
				return getSupplierCategory(userInfo);
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
	 * This method is used to delete entry in contactmastersuppliercategory table.
	 * Need to validate that the specified suppliercategory object is active and is
	 * not associated with any of its child tables before updating its nstatus to
	 * -1.
	 * 
	 * @param contactmasterSupplierCategory [SupplierCategory] object holding detail
	 *                                      to be deleted in
	 *                                      contactmastersuppliercategory table
	 * @param userInfo                      [UserInfo] object holding details of
	 *                                      loggedin user
	 * @return response entity object holding response status and data of deleted
	 *         suppliercategory object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteSupplierCategory(SupplierCategory contactmasterSupplierCategory,
			UserInfo userInfo) throws Exception {
		final SupplierCategory suppliercategory = getActiveSupplierCategoryById(
				contactmasterSupplierCategory.getNsuppliercatcode(), userInfo);

		if (suppliercategory == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			// deleteValidation
			final String query = "select 'IDS_SUPPLIER' as Msg from suppliermatrix " + "where ncategorycode= "
					+ suppliercategory.getNsuppliercatcode() + " and ntypecode="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			// ValidatorDel objDeleteValidation = getTransactionInfo(query, userInfo);
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(contactmasterSupplierCategory.getNsuppliercatcode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedSupplierCategoryList = new ArrayList<>();
				final String updateQueryString = "update suppliercategory set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nsuppliercatcode="
						+ contactmasterSupplierCategory.getNsuppliercatcode() + " and nsitecode =" + userInfo.getNmastersitecode();

				// getJdbcTemplate().execute(updateQueryString);
				jdbcTemplate.execute(updateQueryString);

				contactmasterSupplierCategory
						.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedSupplierCategoryList.add(contactmasterSupplierCategory);
				multilingualIDList.add("IDS_DELETECONTACTMASTERSUPPLIERCATEGORY");

				// fnInsertAuditAction(savedSupplierCategoryList, 1, null, multilingualIDList,
				// userInfo);
				auditUtilityFunction.fnInsertAuditAction(savedSupplierCategoryList, 1, null, multilingualIDList,
						userInfo);

				// return getSupplierCategory(contactmasterSupplierCategory.getNsitecode());
				// status code:200
				return getSupplierCategory(userInfo);

			} else {
				// status code:417
				// return new ResponseEntity<>(objDeleteValidation.getsreturnmessage(),
				// HttpStatus.EXPECTATION_FAILED);
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);

			}
		}
	}

}
