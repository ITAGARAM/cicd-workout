package com.agaramtech.qualis.project.service.disease;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.project.model.Disease;
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
 * This class is used to perform CRUD Operation on "disease" table by
 * implementing methods from its interface.
 * @author Mullai Balaji.V 
 * BGSI-2
 * 26/06/2025 
 */
@AllArgsConstructor
@Repository
public class DiseaseDAOImpl implements DiseaseDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(DiseaseDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available diseases for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         diseases
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDisease(final UserInfo userInfo) throws Exception {

		final String strQuery = "SELECT d.ndiseasecode, d.ndiseasecategorycode, d.sdiseasename, d.sdescription, "
				+ "d.nsitecode, d.nstatus, dc.sdiseasecategoryname, "
				+ "COALESCE(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') AS sdisplaystatus "
				+ "FROM disease d, diseasecategory dc, transactionstatus ts " + "WHERE d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND d.ndiseasecode > 0 "
				+ "AND d.ndiseasecategorycode = dc.ndiseasecategorycode " + "AND dc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND d.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "AND ts.ntranscode = d.nstatus "
				+ "ORDER BY d.ndiseasecode DESC";
		LOGGER.info("Get Method" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Disease()), HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve active disease object based on the specified
	 * ndiseaseCode.
	 * 
	 * @param ndiseaseCode [int] primary key of disease object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of disease
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Disease getActiveDiseaseById(final int ndiseaseCode, UserInfo userInfo) throws Exception {

		final String strQuery = "SELECT d.ndiseasecode, d.ndiseasecategorycode, d.sdiseasename, d.sdescription, d.dmodifieddate, d.nsitecode, "
				+ "d.nstatus, dc.ndiseasecategorycode, dc.sdiseasecategoryname " + "FROM disease d, diseasecategory dc "
				+ "WHERE d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND dc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND d.ndiseasecategorycode = dc.ndiseasecategorycode " + "AND d.ndiseasecode = " + ndiseaseCode;

		return (Disease) jdbcUtilityFunction.queryForObject(strQuery, Disease.class, jdbcTemplate);

	}

	/**
	 * This method is used to add a new entry to disease table. Disease Name is
	 * unique across the database. Need to check for duplicate entry of disease name
	 * for the specified site before saving into database. * Need to check that
	 * there should be only one default disease for a site.
	 * 
	 * @param objDisease [Disease] object holding details to be added in disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details based on which
	 *                   the list is to be fetched
	 * @return saved disease object with status code 200 if saved successfully else
	 *         if the disease already exists, response will be returned as 'Already
	 *         Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createDisease(Disease objDisease, UserInfo userInfo) throws Exception {
		final Disease diseaseByName = getDiseaseByName(objDisease.getSdiseasename(), objDisease.getNsitecode());

		if (diseaseByName == null) {

			final String sQuery = "lock table disease " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedDiseaseList = new ArrayList<>();

			final String sequenceNoQuery = "select nsequenceno from seqnoprojectmanagement where stablename ='disease'";
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
			nsequenceNo++;

			final String insertQuery = "insert into disease (ndiseasecode,ndiseasecategorycode, sdiseasename, sdescription, dmodifieddate, nsitecode, nstatus) "
					+ "values(" + nsequenceNo + ", " + objDisease.getNdiseasecategorycode() + ", N'"
					+ stringUtilityFunction.replaceQuote(objDisease.getSdiseasename()) + "', N'"
					+ stringUtilityFunction.replaceQuote(objDisease.getSdescription()) + "', '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertQuery);

			final String updateQuery = "update seqnoprojectmanagement set nsequenceno = " + nsequenceNo
					+ " where stablename = 'disease'";
			jdbcTemplate.execute(updateQuery);

			objDisease.setNdiseasecode(nsequenceNo);
			savedDiseaseList.add(objDisease);
			multilingualIDList.add("IDS_ADDDISEASE");

			auditUtilityFunction.fnInsertAuditAction(savedDiseaseList, 1, null, multilingualIDList, userInfo);

			return getDisease(userInfo);
		} else {
			// Conflict = 409 - Duplicate entry --getSlanguagetypecode
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the disease object for the specified disease
	 * name and site.
	 * 
	 * @param sdiseasename    [String] name of the disease
	 * @param nmasterSiteCode [int] site code of the disease
	 * @return disease object based on the specified disease name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Disease getDiseaseByName(String sdiseasename, int nmasterSiteCode) throws Exception {
		final String strQuery = "select ndiseasecode from disease where sdiseasename = N'"
				+ stringUtilityFunction.replaceQuote(sdiseasename) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (Disease) jdbcUtilityFunction.queryForObject(strQuery, Disease.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in disease table. Need to validate that
	 * the disease object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of disease name for the specified
	 * site before saving into database. Need to check that there should be only one
	 * default disease for a site
	 * 
	 * @param objDisease [Disease] object holding details to be updated in disease
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details based on which
	 *                   the list is to be fetched
	 * @return saved disease object with status code 200 if saved successfully else
	 *         if the disease already exists, response will be returned as 'Already
	 *         Exists' with status code 409 else if the disease to be updated is not
	 *         available, response will be returned as 'Already Deleted' with status
	 *         code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateDisease(Disease objDisease, UserInfo userInfo) throws Exception {
		final Disease disease = getActiveDiseaseById(objDisease.getNdiseasecode(), userInfo);

		if (disease == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();

			final String queryString = "select ndiseasecode from disease where sdiseasename = N'"
					+ stringUtilityFunction.replaceQuote(objDisease.getSdiseasename()) + "' and ndiseasecode <> "
					+ objDisease.getNdiseasecode() + " and nsitecode =" + objDisease.getNsitecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			final Disease availabledisease = (Disease) jdbcUtilityFunction.queryForObject(queryString, Disease.class,
					jdbcTemplate);

			if (availabledisease == null) {

				final String updateQueryString = "update disease set sdiseasename=N'"
						+ stringUtilityFunction.replaceQuote(objDisease.getSdiseasename()) + "', sdescription =N'"
						+ stringUtilityFunction.replaceQuote(objDisease.getSdescription()) + "', dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ndiseasecode="
						+ objDisease.getNdiseasecode() + ";";

				jdbcTemplate.execute(updateQueryString);

				listAfterUpdate.add(objDisease);
				listBeforeUpdate.add(disease);
				multilingualIDList.add("IDS_EDITDISEASE");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return getDisease(userInfo);
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
	 * This method id used to delete an entry in Disease table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as 'ProjectType'
	 * 
	 * @param objDisease [Disease] an Object holds the record to be deleted
	 * @param userInfo   [UserInfo] holding logged in user details based on which
	 *                   the list is to be fetched
	 * @return a response entity with list of available disease objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteDisease(Disease objDisease, UserInfo userInfo) throws Exception {

		final Disease disease = getActiveDiseaseById(objDisease.getNdiseasecode(), userInfo);

		if (disease == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = " select 'IDS_BIOPROJECT' as Msg from bioproject where ndiseasecode= "
							+ objDisease.getNdiseasecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;

			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objDisease.getNdiseasecode()),
						userInfo);

				validRecord = validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue();
			}

			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedDiseaseList = new ArrayList<>();

				final String updateQueryString = "update disease set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ndiseasecode="
						+ objDisease.getNdiseasecode() + ";";

				jdbcTemplate.execute(updateQueryString);

				objDisease.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedDiseaseList.add(objDisease);
				multilingualIDList.add("IDS_DELETEDISEASE");

				auditUtilityFunction.fnInsertAuditAction(deletedDiseaseList, 1, null, multilingualIDList, userInfo);

				return getDisease(userInfo);

			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

}
