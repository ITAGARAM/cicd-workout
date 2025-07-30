package com.agaramtech.qualis.submitter.service.institutioncategory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.submitter.model.InstitutionCategory;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "institutioncategory" table
 * by implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class InstitutionCategoryDAOImpl implements InstitutionCategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstitutionCategoryDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available institutioncategorys
	 * for the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         institutioncategorys
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<List<InstitutionCategory>> getInstitutionCategory(final UserInfo userInfo) throws Exception {
		final String strInstitutionCat = "select ninstitutioncatcode, sinstitutioncatname, sdescription, nsitecode, nstatus, "
				+ "to_char(dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate"
				+ " from institutioncategory where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ninstitutioncatcode <>-1 and nsitecode =" + userInfo.getNmastersitecode() + "";
		LOGGER.info("Calling getInstitutionCategory service and its query is :---------> " + strInstitutionCat);
		return new ResponseEntity<>(jdbcTemplate.query(strInstitutionCat, new InstitutionCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to institutioncategory table.
	 * institutioncategory Name is unique across the database. Need to check for
	 * duplicate entry of institutioncategory name for the specified site before
	 * saving into database. * Need to check that there should be only one default
	 * institutioncategory for a site.
	 * 
	 * @param objinstitutioncategory [institutioncategory] object holding details to
	 *                               be added in institutioncategory table
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return saved institutioncategory object with status code 200 if saved
	 *         successfully else if the institutioncategory already exists, response
	 *         will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<? extends Object> createInstitutionCategory(final InstitutionCategory submitterInstitutionCategory,
			final UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table institutioncategory "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedInstitutionCategoryList = new ArrayList<>();
		final InstitutionCategory institutionCategoryListByName = getInstitutionCategoryByName(
				submitterInstitutionCategory.getSinstitutioncatname(), submitterInstitutionCategory.getNsitecode());
		if (institutionCategoryListByName == null) {
			final String sequencenoquery = "select nsequenceno from seqnosubmittermanagement where stablename ='institutioncategory'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			final String insertquery = "Insert into institutioncategory (ninstitutioncatcode,sinstitutioncatname,sdescription,nsitecode,dmodifieddate, "
					+ " nstatus) values (" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(submitterInstitutionCategory.getSinstitutioncatname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(submitterInstitutionCategory.getSdescription()) + "', "
					+ userInfo.getNmastersitecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			final String updatequery = "update seqnosubmittermanagement set nsequenceno =" + nsequenceno
					+ " where stablename='institutioncategory'";
			jdbcTemplate.execute(updatequery);
			submitterInstitutionCategory.setNinstitutioncatcode(nsequenceno);
			savedInstitutionCategoryList.add(submitterInstitutionCategory);
			multilingualIDList.add("IDS_ADDINSTITUTIONCATEGORY");
			auditUtilityFunction.fnInsertAuditAction(savedInstitutionCategoryList, 1, null, multilingualIDList,
					userInfo);
			return getInstitutionCategory(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the institutioncategory object for the specified
	 * institutioncategory name and site.
	 * 
	 * @param sinstitutioncategoryname [String] name of the institutioncategory
	 * @param nmasterSiteCode          [int] site code of the institutioncategory
	 * @return institutioncategory object based on the specified institutioncategory
	 *         name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private InstitutionCategory getInstitutionCategoryByName(final String institutioncategory,
			final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ninstitutioncatcode from institutioncategory where sinstitutioncatname = N'"
				+ stringUtilityFunction.replaceQuote(institutioncategory) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (InstitutionCategory) jdbcUtilityFunction.queryForObject(strQuery, InstitutionCategory.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to retrieve active institutioncategory object based on
	 * the specified ninstitutioncategoryCode.
	 * 
	 * @param ninstitutioncategoryCode [int] primary key of institutioncategory
	 *                                 object
	 * @param userInfo                 [UserInfo] holding logged in user details
	 *                                 based on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         institutioncategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public InstitutionCategory getActiveInstitutionCategoryById(final int ninstitutioncatcode) throws Exception {
		final String strQuery = "select ninstitutioncatcode, sinstitutioncatname, sdescription, nsitecode, nstatus "
				+ " from institutioncategory where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ninstitutioncatcode = "
				+ ninstitutioncatcode;
		return (InstitutionCategory) jdbcUtilityFunction.queryForObject(strQuery, InstitutionCategory.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to update entry in institutioncategory table. Need to
	 * validate that the institutioncategory object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * institutioncategory name for the specified site before saving into database.
	 * Need to check that there should be only one default institutioncategory for a
	 * site
	 * 
	 * @param objinstitutioncategory [institutioncategory] object holding details to
	 *                               be updated in institutioncategory table
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return saved institutioncategory object with status code 200 if saved
	 *         successfully else if the institutioncategory already exists, response
	 *         will be returned as 'Already Exists' with status code 409 else if the
	 *         institutioncategory to be updated is not available, response will be
	 *         returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<? extends Object> updateInstitutionCategory(final InstitutionCategory submitterInstitutionCategory,
			final UserInfo userInfo) throws Exception {
		final InstitutionCategory institutioncategory = getActiveInstitutionCategoryById(
				submitterInstitutionCategory.getNinstitutioncatcode());
		if (institutioncategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ninstitutioncatcode from institutioncategory where sinstitutioncatname = '"
					+ stringUtilityFunction.replaceQuote(submitterInstitutionCategory.getSinstitutioncatname())
					+ "' and ninstitutioncatcode <> " + submitterInstitutionCategory.getNinstitutioncatcode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNmastersitecode() + "";
			final List<InstitutionCategory> institutioncategoryList = jdbcTemplate.query(queryString,
					new InstitutionCategory());
			if (institutioncategoryList.isEmpty()) {
				final String updateQueryString = "update institutioncategory set sinstitutioncatname='"
						+ stringUtilityFunction.replaceQuote(submitterInstitutionCategory.getSinstitutioncatname())
						+ "', " + "sdescription ='"
						+ stringUtilityFunction.replaceQuote(submitterInstitutionCategory.getSdescription())
						+ "', dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where ninstitutioncatcode=" + submitterInstitutionCategory.getNinstitutioncatcode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITINSTITUTIONCATEGORY");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(submitterInstitutionCategory);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(institutioncategory);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getInstitutionCategory(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in institutioncategory table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objinstitutioncategory [institutioncategory] an Object holds the
	 *                               record to be deleted
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return a response entity with list of available institutioncategory objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<? extends Object> deleteInstitutionCategory(final InstitutionCategory submitterInstitutionCategory,
			final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedInstitutionCategoryList = new ArrayList<>();
		final InstitutionCategory institutioncategory = getActiveInstitutionCategoryById(
				submitterInstitutionCategory.getNinstitutioncatcode());
		if (institutioncategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_INSTITUTION' as Msg from institution where ninstitutioncatcode= "
					+ submitterInstitutionCategory.getNinstitutioncatcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ "select 'IDS_SUBMITTER' as Msg from submitter where ninstitutioncatcode= "
					+ submitterInstitutionCategory.getNinstitutioncatcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(submitterInstitutionCategory.getNinstitutioncatcode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQueryString = "update institutioncategory set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstitutioncatcode="
						+ submitterInstitutionCategory.getNinstitutioncatcode();
				jdbcTemplate.execute(updateQueryString);
				submitterInstitutionCategory
						.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedInstitutionCategoryList.add(submitterInstitutionCategory);
				multilingualIDList.add("IDS_DELETEINSTITUTIONCATEGORY");
				auditUtilityFunction.fnInsertAuditAction(savedInstitutionCategoryList, 1, null, multilingualIDList,
						userInfo);
				return getInstitutionCategory(userInfo);

			} else {

				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
