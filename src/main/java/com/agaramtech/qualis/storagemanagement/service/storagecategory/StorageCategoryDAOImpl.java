package com.agaramtech.qualis.storagemanagement.service.storagecategory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.SampleStorageCommons;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.storagemanagement.model.StorageCategory;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class StorageCategoryDAOImpl implements StorageCategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(StorageCategoryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final SampleStorageCommons sampleStorageCommons;

	/**
	 * This method is used to retrieve list of all available storagecategory for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         storagecategory
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<List<StorageCategory>> getStorageCategory(final UserInfo userInfo) throws Exception {
		LOGGER.info("getStorageCategory()");
		return sampleStorageCommons.getStorageCategory(userInfo);
	}

	/**
	 * This method is used to retrieve active storagecategory object based on the
	 * specified nstorageCategoryCode.
	 * 
	 * @param nstorageCategoryCode [int] primary key of storagecategory object
	 * @param userInfo             [UserInfo] holding logged in user details based
	 *                             on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         storagecategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public StorageCategory getActiveStorageCategoryById(final int nstorageCategoryCode, final UserInfo userInfo)
			throws Exception {
		return sampleStorageCommons.getActiveStorageCategoryById(nstorageCategoryCode, userInfo);
	}

	/**
	 * This method is used to fetch the storagecategory object for the specified
	 * storagecategory name and site.
	 * 
	 * @param sstoragecategoryname [String] name of the storagecategory
	 * @param nmasterSiteCode      [int] site code of the storagecategory
	 * @return storagecategory object based on the specified storagecategory name
	 *         and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private StorageCategory getStorageCategoryByName(final String sstorageCategoryName, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select nstoragecategorycode from storagecategory where sstoragecategoryname = N'"
				+ stringUtilityFunction.replaceQuote(sstorageCategoryName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (StorageCategory) jdbcUtilityFunction.queryForObject(strQuery, StorageCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to storagecategory table. Storage
	 * Category Name is unique across the database. Need to check for duplicate
	 * entry of storage category name for the specified site before saving into
	 * database.
	 * 
	 * @param objStorageCategory [StorageCategory] object holding details to be
	 *                           added in storagecategory table
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return saved storagecategory object with status code 200 if saved
	 *         successfully else if the storagecategory already exists, response
	 *         will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<? extends Object> createStorageCategory(final StorageCategory objStorageCategory,
			final UserInfo userInfo) throws Exception {
		final StorageCategory storageCategoryByName = getStorageCategoryByName(
				objStorageCategory.getSstoragecategoryname(), objStorageCategory.getNsitecode());
		if (storageCategoryByName == null) {
			final String sQuery = " lock  table storagecategory "
					+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedStorageCategoryList = new ArrayList<>();

			String sequenceNoQuery = "select nsequenceno from seqnostoragemanagement where stablename ='storagecategory'";
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
			nsequenceNo++;

			String insertQuery = "Insert into storagecategory (nstoragecategorycode,sstoragecategoryname,sdescription,dmodifieddate,nsitecode,nstatus) "
					+ "values(" + nsequenceNo + ",N'"
					+ stringUtilityFunction.replaceQuote(objStorageCategory.getSstoragecategoryname()) + "'," + "N'"
					+ stringUtilityFunction.replaceQuote(objStorageCategory.getSdescription()) + "'," + "'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertQuery);

			String updateQuery = "update seqnostoragemanagement set nsequenceno =" + nsequenceNo
					+ " where stablename='storagecategory'";
			jdbcTemplate.execute(updateQuery);

			objStorageCategory.setNstoragecategorycode(nsequenceNo);
			savedStorageCategoryList.add(objStorageCategory);
			multilingualIDList.add("IDS_ADDSTORAGECATEGORY");
			auditUtilityFunction.fnInsertAuditAction(savedStorageCategoryList, 1, null, multilingualIDList, userInfo);
			return getStorageCategory(userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to update entry in storagecategory table. Need to
	 * validate that the storagecategory object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of storage
	 * category name for the specified site before saving into database.
	 * 
	 * @param objStorageCategory [StorageCategory] object holding details to be
	 *                           updated in storagecategory table
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return saved storagecategory object with status code 200 if saved
	 *         successfully else if the storagecategory already exists, response
	 *         will be returned as 'Already Exists' with status code 409 else if the
	 *         storagecategory to be updated is not available, response will be
	 *         returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<? extends Object> updateStorageCategory(final StorageCategory objStorageCategory,
			final UserInfo userInfo) throws Exception {

		final StorageCategory storagecategory = getActiveStorageCategoryById(
				objStorageCategory.getNstoragecategorycode(), userInfo);
		if (storagecategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listBeforeSave = new ArrayList<>();
			final List<Object> listAfterSave = new ArrayList<>();

			final String queryString = "select nstoragecategorycode from storagecategory where sstoragecategoryname = '"
					+ stringUtilityFunction.replaceQuote(objStorageCategory.getSstoragecategoryname()) + "'  "
					+ " and nstoragecategorycode <> " + objStorageCategory.getNstoragecategorycode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final StorageCategory availableStorageCategory = (StorageCategory) jdbcUtilityFunction
					.queryForObject(queryString, StorageCategory.class, jdbcTemplate);

			if (availableStorageCategory == null) {
				final String updateQueryString = "update storagecategory set sstoragecategoryname='"
						+ stringUtilityFunction.replaceQuote(objStorageCategory.getSstoragecategoryname()) + "',"
						+ " sdescription='" + stringUtilityFunction.replaceQuote(objStorageCategory.getSdescription())
						+ "'," + "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nstoragecategorycode =" + objStorageCategory.getNstoragecategorycode();
				jdbcTemplate.execute(updateQueryString);

				multilingualIDList.add("IDS_EDITSTORAGECATEGORY");
				listAfterSave.add(objStorageCategory);
				listBeforeSave.add(storagecategory);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getStorageCategory(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in storagecategory table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other table such as 'samplestoragelocation'
	 * 
	 * @param objStorageCategory [StorageCategory] an Object holds the record to be
	 *                           deleted
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return a response entity with list of available storagecategory objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<? extends Object> deleteStorageCategory(final StorageCategory objStorageCategory,
			final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedStorageCategoryList = new ArrayList<>();
		final StorageCategory storageCategory = getActiveStorageCategoryById(
				objStorageCategory.getNstoragecategorycode(), userInfo);

		if (storageCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_STORAGESTRUCTUREAUDIT' as Msg from samplestoragelocation where nstoragecategorycode= "
					+ objStorageCategory.getNstoragecategorycode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;

			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objStorageCategory.getNstoragecategorycode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQueryString = "update storagecategory set nstatus ="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstoragecategorycode="
						+ storageCategory.getNstoragecategorycode();
				jdbcTemplate.execute(updateQueryString);
				objStorageCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedStorageCategoryList.add(objStorageCategory);
				multilingualIDList.add("IDS_DELETESTORAGECATEGORY");
				auditUtilityFunction.fnInsertAuditAction(savedStorageCategoryList, 1, null, multilingualIDList,
						userInfo);
				return getStorageCategory(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
	//comment by Vigensh(23-07-2025)-->This code not applicable for checking validation 
	/*	public Settings getSettings(final  UserInfo userInfo) throws Exception {
		
		final String str = "select ssettingvalue from settings where nsettingcode=84 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		return (Settings) jdbcUtilityFunction.queryForObject(str, Settings.class, jdbcTemplate);
	}*/
	

}