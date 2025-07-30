package com.agaramtech.qualis.checklist.service.checklistqbcategory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.checklist.model.ChecklistQBCategory;
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
 * This class is used to perform CRUD Operation on "qbcategory" table by
 * implementing methods from its interface.
 */

@AllArgsConstructor
@Repository
public class QBCategoryDAOImpl implements QBCategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(QBCategoryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available qbcategorys for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         qbcategorys
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getQBCategory(int nmasterSiteCode) throws Exception {

		final String strQuery = "select * from checklistqbcategory where nchecklistqbcategorycode >0 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode=" + nmasterSiteCode
				+ " order by nchecklistqbcategorycode";
		LOGGER.info("Get Query -->" + strQuery);
		return new ResponseEntity<Object>(
				(List<ChecklistQBCategory>) jdbcTemplate.query(strQuery, new ChecklistQBCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to qbcategory table. qbcategory Name
	 * is unique across the database. Need to check for duplicate entry of
	 * qbcategory name for the specified site before saving into database. * Need to
	 * check that there should be only one default qbcategory for a site.
	 * 
	 * @param objqbcategory [qbcategory] object holding details to be added in
	 *                      qbcategory table
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return saved qbcategory object with status code 200 if saved successfully
	 *         else if the qbcategory already exists, response will be returned as
	 *         'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table checklistqbcategory "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final ChecklistQBCategory ChecklistQBCategoryByName = getChecklistQBCategoryByName(
				objChecklistQBCategory.getSchecklistqbcategoryname(), objChecklistQBCategory.getNsitecode());
		if (ChecklistQBCategoryByName == null) {
			String sequencequery = "select nsequenceno from SeqNoChecklist where stablename ='checklistqbcategory'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;
			String insertquery = "Insert into checklistqbcategory (nchecklistqbcategorycode,schecklistqbcategoryname,sdescription,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(objChecklistQBCategory.getSchecklistqbcategoryname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objChecklistQBCategory.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			String Updatequery = "update SeqNoChecklist set nsequenceno=" + nsequenceno
					+ " where stablename ='checklistqbcategory'";
			jdbcTemplate.execute(Updatequery);
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedChecklistQBCategoryList = new ArrayList<>();
			objChecklistQBCategory.setNchecklistqbcategorycode(nsequenceno);
			savedChecklistQBCategoryList.add(objChecklistQBCategory);
			multilingualIDList.add("IDS_ADDCHECKLISTQBCATEGORY");
			auditUtilityFunction.fnInsertAuditAction(savedChecklistQBCategoryList, 1, null, multilingualIDList,
					userInfo);
			return getQBCategory(objChecklistQBCategory.getNsitecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the qbcategory object for the specified
	 * qbcategory name and site.
	 * 
	 * @param sqbcategoryname [String] name of the qbcategory
	 * @param nmasterSiteCode [int] site code of the qbcategory
	 * @return qbcategory object based on the specified qbcategory name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private ChecklistQBCategory getChecklistQBCategoryByName(String schecklistQBCategoryName, int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select schecklistqbcategoryname from checklistqbcategory where schecklistqbcategoryname = N'"
				+ stringUtilityFunction.replaceQuote(schecklistQBCategoryName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (ChecklistQBCategory) jdbcUtilityFunction.queryForObject(strQuery, ChecklistQBCategory.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to update entry in qbcategory table. Need to validate
	 * that the qbcategory object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of qbcategory name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default qbcategory for a site
	 * 
	 * @param objqbcategory [qbcategory] object holding details to be updated in
	 *                      qbcategory table
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return saved qbcategory object with status code 200 if saved successfully
	 *         else if the qbcategory already exists, response will be returned as
	 *         'Already Exists' with status code 409 else if the qbcategory to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)
			throws Exception {
		final ChecklistQBCategory checklistQBCategory = (ChecklistQBCategory) getActiveQBCategoryById(
				objChecklistQBCategory.getNchecklistqbcategorycode());
		if (checklistQBCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select schecklistqbcategoryname from checklistqbcategory where schecklistqbcategoryname = N'"
					+ stringUtilityFunction.replaceQuote(objChecklistQBCategory.getSchecklistqbcategoryname())
					+ "' and nchecklistqbcategorycode <> " + objChecklistQBCategory.getNchecklistqbcategorycode()
					+ " and nsitecode=" + userInfo.getNmastersitecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<ChecklistQBCategory> checklistQBCategoryList = (List<ChecklistQBCategory>) jdbcTemplate
					.query(queryString, new ChecklistQBCategory());
			if (checklistQBCategoryList.isEmpty()) {
				final String updateQueryString = "update checklistqbcategory set schecklistqbcategoryname= N'"
						+ stringUtilityFunction.replaceQuote(objChecklistQBCategory.getSchecklistqbcategoryname())
						+ "', sdescription = N'"
						+ stringUtilityFunction.replaceQuote(objChecklistQBCategory.getSdescription())
						+ "', dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nchecklistqbcategorycode=" + objChecklistQBCategory.getNchecklistqbcategorycode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterSave = new ArrayList<>();
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(checklistQBCategory);
				listAfterSave.add(objChecklistQBCategory);
				multilingualIDList.add("IDS_EDITCHECKLISTQBCATEGORY");
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getQBCategory(objChecklistQBCategory.getNsitecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in qbcategory table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objqbcategory [qbcategory] an Object holds the record to be deleted
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return a response entity with list of available qbcategory objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteQBCategory(ChecklistQBCategory objChecklistQBCategory, UserInfo userInfo)
			throws Exception {
		final ChecklistQBCategory ChecklistQBCategory = (ChecklistQBCategory) getActiveQBCategoryById(
				objChecklistQBCategory.getNchecklistqbcategorycode());
		if (ChecklistQBCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_QUESTIONBANK' as Msg from checklistqb"
					+ " where nchecklistqbcategorycode= " + objChecklistQBCategory.getNchecklistqbcategorycode() + " "
					+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(objChecklistQBCategory.getNchecklistqbcategorycode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedQBCategoryList = new ArrayList<>();
				final String updateQueryString = " update checklistqbcategory set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nchecklistqbcategorycode=" + objChecklistQBCategory.getNchecklistqbcategorycode();
				jdbcTemplate.execute(updateQueryString);
				objChecklistQBCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedQBCategoryList.add(objChecklistQBCategory);
				multilingualIDList.add("IDS_DELETECHECKLISTQBCATEGORY");
				auditUtilityFunction.fnInsertAuditAction(deletedQBCategoryList, 1, null, multilingualIDList, userInfo);
				return getQBCategory(objChecklistQBCategory.getNsitecode());
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to retrieve active qbcategory object based on the
	 * specified nqbcategoryCode.
	 * 
	 * @param nqbcategoryCode [int] primary key of qbcategory object
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of qbcategory
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ChecklistQBCategory getActiveQBCategoryById(int nchecklistQBCategorycode) throws Exception {
		final String strQuery = "select * from checklistqbcategory m where m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nchecklistqbcategorycode = "
				+ nchecklistQBCategorycode;
		return (ChecklistQBCategory) jdbcUtilityFunction.queryForObject(strQuery, ChecklistQBCategory.class,
				jdbcTemplate);
	}

}
