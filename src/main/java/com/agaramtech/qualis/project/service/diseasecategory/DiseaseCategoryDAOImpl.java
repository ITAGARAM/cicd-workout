package com.agaramtech.qualis.project.service.diseasecategory;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.project.model.DiseaseCategory;
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
 * This class is used to perform CRUD Operation on "diseasecategory" table by 
 * implementing methods from its interface. 
 */
/**
 * @author sujatha.v BGSI-1 26/06/2025
 */
@Repository
@AllArgsConstructor
public class DiseaseCategoryDAOImpl implements DiseaseCategoryDAO {

	private final static Logger LOGGER = LoggerFactory.getLogger(DiseaseCategoryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final CommonFunction commonFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private ValidatorDel validatorDel;

	/**
	 * This method is used to retrieve list of all available diseasecategory's for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         diseasecategory's
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDiseaseCategory(final UserInfo userInfo) throws Exception {

		final String strQuery = "select d.ndiseasecategorycode, d.sdiseasecategoryname, d.sdescription, d.dmodifieddate, d.nsitecode, d.nstatus from diseasecategory d"
				+ " where ndiseasecategorycode > 0" + " and d.nsitecode= " + userInfo.getNmastersitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		LOGGER.info("getDiseaseCategory() called " + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new DiseaseCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active diseasecategory object based on the
	 * specified ndiseasecategorycode.
	 * 
	 * @param ndiseasecategorycode [int] primary key of diseasecategory object
	 * @param userInfo             [UserInfo] holding logged in user details based
	 *                             on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         diseasecategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public DiseaseCategory getActiveDiseaseCategoryById(final int ndiseasecategorycode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select d.ndiseasecategorycode, d.sdiseasecategoryname, d.sdescription, d.dmodifieddate, d.nsitecode, d.nstatus from diseasecategory d"
				+ " where d.ndiseasecategorycode= " + ndiseasecategorycode + " and d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (DiseaseCategory) jdbcUtilityTemplateFunction.queryForObject(strQuery, DiseaseCategory.class,
				jdbcTemplate);

	}

	/**
	 * This method is used to add a new entry to diseasecategory table.
	 * DiseaseCategory Name is unique across the database. Need to check for
	 * duplicate entry of diseasecategory name for the specified site before saving
	 * into database. *
	 * 
	 * @param objDiseaseCategory [DiseaseCategory] object holding details to be
	 *                           added in diseasecategory table
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return saved diseasecategory object with status code 200 if saved
	 *         successfully else if the diseasecategory already exists, response
	 *         will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createDiseaseCategory(final DiseaseCategory objDiseaseCategory,
			final UserInfo userInfo) throws Exception {
		final DiseaseCategory diseaseCategoryByName = getDiseaseCategoryByName(
				objDiseaseCategory.getSdiseasecategoryname(), userInfo.getNmastersitecode());

		if (diseaseCategoryByName == null) {
			final String sQuery = " lock table diseasecategory "
					+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final List<String> multilingualIdList = new ArrayList<>();
			final List<Object> saveDiseaseCategoryList = new ArrayList<>();

			String sequenceNoQuery = " select nsequenceno from seqnoprojectmanagement where stablename ='diseasecategory'";
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
			nsequenceNo++;
			final String insertQuery = " insert into diseasecategory (ndiseasecategorycode, sdiseasecategoryname, sdescription, dmodifieddate, nsitecode, nstatus )"
					+ " values(" + nsequenceNo + ",N'"
					+ stringUtilityFunction.replaceQuote(objDiseaseCategory.getSdiseasecategoryname()) + "' ,N'"
					+ stringUtilityFunction.replaceQuote(objDiseaseCategory.getSdescription()) + "' ,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )";
			jdbcTemplate.execute(insertQuery);

			final String updateQuery = " update seqnoprojectmanagement set nsequenceno= " + nsequenceNo
					+ " where stablename='diseasecategory'";
			jdbcTemplate.execute(updateQuery);

			objDiseaseCategory.setNdiseasecategorycode(nsequenceNo);
			saveDiseaseCategoryList.add(objDiseaseCategory);

			multilingualIdList.add("IDS_ADDDISEASECATECORY");

			auditUtilityFunction.fnInsertAuditAction(saveDiseaseCategoryList, 1, null, multilingualIdList, userInfo);
			return getDiseaseCategory(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	/**
	 * This method is used to fetch the diseasecategory object for the specified
	 * diseasecategory name and site.
	 * 
	 * @param sdiseasecategoryname [String] name of the diseasecategory
	 * @param nmasterSiteCode      [int] site code of the diseasecategory
	 * @return diseasecategory object based on the specified diseasecategory name
	 *         and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private DiseaseCategory getDiseaseCategoryByName(final String sdiseasecategoryname, int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select ndiseasecategorycode from diseasecategory d where d.sdiseasecategoryname= N'"
				+ stringUtilityFunction.replaceQuote(sdiseasecategoryname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nsitecode= " + nmasterSiteCode;
		return (DiseaseCategory) jdbcUtilityTemplateFunction.queryForObject(strQuery, DiseaseCategory.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to update entry in diseasecategory table. Need to
	 * validate that the diseasecategory object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * diseasecategory name for the specified site before saving into database.
	 * 
	 * @param objDiseaseCategory [DiseaseCategory] object holding details to be
	 *                           updated in diseasecategory table
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return saved diseasecategory object with status code 200 if saved
	 *         successfully else if the diseasecategory already exists, response
	 *         will be returned as 'Already Exists' with status code 409 else if the
	 *         unit to be updated is not available, response will be returned as
	 *         'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateDiseaseCategory(final DiseaseCategory objDiseaseCategory,
			final UserInfo userInfo) throws Exception {

		final DiseaseCategory diseaseCategory = getActiveDiseaseCategoryById(
				objDiseaseCategory.getNdiseasecategorycode(), userInfo);
		if (diseaseCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();

			final String queryString = " select d.ndiseasecategorycode from diseasecategory d where d.sdiseasecategoryname=N'"
					+ stringUtilityFunction.replaceQuote(objDiseaseCategory.getSdiseasecategoryname())
					+ "' and d.ndiseasecategorycode<> " + objDiseaseCategory.getNdiseasecategorycode()
					+ " and nsitecode= " + objDiseaseCategory.getNsitecode() + " and nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final DiseaseCategory availableDiseaseCategory = (DiseaseCategory) jdbcUtilityTemplateFunction
					.queryForObject(queryString, DiseaseCategory.class, jdbcTemplate);
			if (availableDiseaseCategory == null) {
				final String updateQuery = " update diseasecategory set sdiseasecategoryname=N'"
						+ stringUtilityFunction.replaceQuote(objDiseaseCategory.getSdiseasecategoryname())
						+ "', sdescription=N'"
						+ stringUtilityFunction.replaceQuote(objDiseaseCategory.getSdescription())
						+ "', dmodifieddate= '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where ndiseasecategorycode= " + objDiseaseCategory.getNdiseasecategorycode();

				jdbcTemplate.execute(updateQuery);
				listAfterUpdate.add(objDiseaseCategory);
				listBeforeUpdate.add(diseaseCategory);

				multilingualIDList.add("IDS_EDITDISEASECATEGORY");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return getDiseaseCategory(userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in DiseaseCategory table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other tables such as 'disease'
	 * 
	 * @param objDiseaseCategory [DiseaseCategory] an Object holds the record to be
	 *                           deleted
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return a response entity with list of available diseasecategory objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteDiseaseCategory(final DiseaseCategory objDiseaseCategory,
			final UserInfo userInfo) throws Exception {

		final DiseaseCategory diseaseCategory = getActiveDiseaseCategoryById(
				objDiseaseCategory.getNdiseasecategorycode(), userInfo);
		if (diseaseCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> deletedDiseaseCategoryList = new ArrayList<>();

			final String query = " Select 'IDS_DISEASE' as Msg from disease where ndiseasecategorycode= "
					+ objDiseaseCategory.getNdiseasecategorycode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + " union all "
					+ " select 'IDS_BIOPROJECT' as Msg from bioproject where ndiseasecategorycode= "
					+ objDiseaseCategory.getNdiseasecategorycode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objDiseaseCategory.getNdiseasecategorycode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQuery = " update diseasecategory set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' , nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ndiseasecategorycode="
						+ objDiseaseCategory.getNdiseasecategorycode();

				jdbcTemplate.execute(updateQuery);
				objDiseaseCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedDiseaseCategoryList.add(objDiseaseCategory);
				multilingualIDList.add("IDS_DELETEDISEASECATEGORY");

				auditUtilityFunction.fnInsertAuditAction(deletedDiseaseCategoryList, 1, null, multilingualIDList,
						userInfo);
				return getDiseaseCategory(userInfo);

			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
