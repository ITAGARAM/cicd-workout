package com.agaramtech.qualis.barcode.service.patientcategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.barcode.model.PatientCategory;
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
 * This class is used to perform CRUD Operation on "patientcategory" table by
 * implementing methods from its interface.
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@AllArgsConstructor
@Repository
public class PatientCategoryDAOImpl implements PatientCategoryDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(PatientCategoryDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active patientcategory for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         patientcategory
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getPatientCategory(UserInfo userInfo) throws Exception {

		final String strQuery = "select pt.sprojecttypename,pt.nprojecttypecode,p.npatientcatcode,p.spatientcatname,p.ncode,p.nsitecode,p.nstatus"
				+ " from patientcategory p,projecttype pt"
				+ " where p.nprojecttypecode=pt.nprojecttypecode and p.nsitecode=pt.nsitecode and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.npatientcatcode>0 and p.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ "and pt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " order by p.npatientcatcode desc";
		LOGGER.info("getPatientCategory -->" + strQuery);

		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new PatientCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to patientcategory table. Need to
	 * check for duplicate entry of Patient Category Name and Code for the specified
	 * site before saving into database.
	 * Patient Category Name and Code is unique across the database. 
	 * @param objPatientCategory [PatientCategory] object holding details to be
	 *                           added in sampledonor table.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return inserted patientcategory object with HTTP Status.
	 *  @return saved objPatientCategory object with status code 200 if saved successfully else if the patientcategory already exists, 
	 * 			response will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createPatientCategory(PatientCategory objPatientCategory, UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSampleDonorList = new ArrayList<>();

		final List<PatientCategory> patientCategoryList = getPatientCategoryName(
				objPatientCategory.getNprojecttypecode(), objPatientCategory.getSpatientcatname(),
				objPatientCategory.getNcode(), userInfo.getNmastersitecode(), objPatientCategory.getNpatientcatcode());

		if (patientCategoryList.get(0).isIscodeexists() == false
				&& patientCategoryList.get(0).isIspatientcatnameexists() == false) {
			if (objPatientCategory.getNcode() > 0) {
				final String sQuery = " lock  table patientcategory "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(sQuery);

				String sequencenoquery = "select nsequenceno from seqnobarcode where stablename ='patientcategory'";

				int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
				nsequenceno++;
//ALPD-4462Patient Category -> Added the project type Combo box. 

				final String insertquery = "Insert into patientcategory (npatientcatcode ,nprojecttypecode ,spatientcatname,ncode,dmodifieddate,nsitecode,nstatus) "
						+ "values(" + nsequenceno + "," + objPatientCategory.getNprojecttypecode() + ",   N'"
						+ stringUtilityFunction.replaceQuote(objPatientCategory.getSpatientcatname()) + "',"
						+ objPatientCategory.getNcode() + "," + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "'," + " " + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);

				final String updatequery = "update seqnobarcode set nsequenceno =" + nsequenceno
						+ " where stablename='patientcategory'";
				jdbcTemplate.execute(updatequery);

				objPatientCategory.setNpatientcatcode(nsequenceno);

				savedSampleDonorList.add(objPatientCategory);

				multilingualIDList.add("IDS_ADDPATIENTCATEGORY");

				auditUtilityFunction.fnInsertAuditAction(savedSampleDonorList, 1, null, multilingualIDList, userInfo);
				return getPatientCategory(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			String alert = "";

			boolean isPatientCatName = patientCategoryList.get(0).isIspatientcatnameexists();
			boolean code = patientCategoryList.get(0).isIscodeexists();

			if (isPatientCatName == true && code == true) {
				alert = commonFunction.getMultilingualMessage("IDS_PATIENTCATEGORY", userInfo.getSlanguagefilename())
						+ " and " + commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
			} else if (isPatientCatName == true) {
				alert = commonFunction.getMultilingualMessage("IDS_PATIENTCATEGORY", userInfo.getSlanguagefilename());
			} else {
				alert = commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
			}
			return new ResponseEntity<>(alert + " "
					+ commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()).toLowerCase(),
					HttpStatus.CONFLICT);
		}

	}

	/**
	 * This method is used to retrieve active patientcategory object based on the
	 * specified nsampledonorcode.
	 * 
	 * @param nsampledonorcode [int] primary key of sampledonor object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         sampledonor object otherwise response will be returned as 'Already Deleted'
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getActivePatientCategoryById(final int npatientcatcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select pt.sprojecttypename,pt.nprojecttypecode,p.* from patientcategory p,projecttype pt where p.nprojecttypecode=pt.nprojecttypecode  and p.nsitecode=pt.nsitecode and  p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and p.npatientcatcode ="
				+ npatientcatcode + "  and p.nsitecode=" + userInfo.getNmastersitecode() + " ; ";

		final PatientCategory patientcategory = (PatientCategory) jdbcUtilityFunction.queryForObject(strQuery,
				PatientCategory.class, jdbcTemplate);

		if (patientcategory == null) {
			// status code: 417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// status code: 200
			return new ResponseEntity<>(patientcategory, HttpStatus.OK);
		}

	}

	/**
	 * This method is used to update entry in patientcategory table. Need to
	 * validate that the patientcategory object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * PatientCategory Name and Code Name for the specified site before saving into
	 * database.
	 * 
	 * @param objPatientCategory [PatientCategory] object holding details to be
	 *                           updated in patientcategory table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         patientcategory object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updatePatientCategory(PatientCategory objPatientCategory, UserInfo userInfo)
			throws Exception {
		final ResponseEntity<Object> patientcategoryResponse = getActivePatientCategoryById(
				objPatientCategory.getNpatientcatcode(), userInfo);

		if (patientcategoryResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return patientcategoryResponse;
		}

		else {

			final List<PatientCategory> patientCategoryList = getPatientCategoryName(
					objPatientCategory.getNprojecttypecode(), objPatientCategory.getSpatientcatname(),
					objPatientCategory.getNcode(), userInfo.getNmastersitecode(),
					objPatientCategory.getNpatientcatcode());

			// final List<PatientCategory> patientCategoryList =
			// jdbcTemplate.query(strQuery, new PatientCategory());

			if (patientCategoryList.get(0).isIscodeexists() == false
					&& patientCategoryList.get(0).isIspatientcatnameexists() == false) {
				if (objPatientCategory.getNcode() > 0) {
					final String updateQueryString = "update patientcategory set  nprojecttypecode="
							+ objPatientCategory.getNprojecttypecode() + ",  spatientcatname='"
							+ stringUtilityFunction.replaceQuote(objPatientCategory.getSpatientcatname()) + "', "
							+ "ncode =" + objPatientCategory.getNcode() + ",dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where npatientcatcode  ="
							+ objPatientCategory.getNpatientcatcode();
					jdbcTemplate.execute(updateQueryString);

					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add("IDS_EDITPATIENTCATEGORY");

					final List<Object> listAfterSave = new ArrayList<>();
					listAfterSave.add(objPatientCategory);

					final List<Object> listBeforeSave = new ArrayList<>();
					listBeforeSave.add(patientcategoryResponse.getBody());

					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);
					return getPatientCategory(userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				String alert = "";

				boolean isPatientCatName = patientCategoryList.get(0).isIspatientcatnameexists();
				boolean isCode = patientCategoryList.get(0).isIscodeexists();
				if (isPatientCatName == true && isCode == true) {

					alert = commonFunction.getMultilingualMessage("IDS_PATIENTCATEGORY",
							userInfo.getSlanguagefilename()) + " and "
							+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				}

				else if (isPatientCatName == true) {
					alert = commonFunction.getMultilingualMessage("IDS_PATIENTCATEGORY",
							userInfo.getSlanguagefilename());
				} else {
					alert = commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

		}
	}

	/**
	 * This method id used to delete an entry in patientcategory table.
	 * Need to check the record is already deleted or not
	 * Need to check whether the record is used in other tables  such as aliquotplan
	 * @param objPatientCategory [PatientCategory] an Object holds the record to be
	 *                           deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with corresponding HTTP status and an
	 *         patientcategory object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deletePatientCategory(PatientCategory objPatientCategory, UserInfo userInfo)
			throws Exception {

		final ResponseEntity<Object> patientcategoryResponse = getActivePatientCategoryById(
				objPatientCategory.getNpatientcatcode(), userInfo);

		if (patientcategoryResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return patientcategoryResponse;
		} else {

			final String recordInProcessType = "select 'IDS_ALIQUOTPLAN' as Msg from aliquotplan where "
					+ "npatientcatcode=" + objPatientCategory.getNpatientcatcode() + "  " + "and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			valiDatorDel = projectDAOSupport.getTransactionInfo(recordInProcessType, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objPatientCategory.getNpatientcatcode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {

				final List<Object> deletedPatientCategoryList = new ArrayList<>();
				final String updateQueryString = "update patientcategory set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where npatientcatcode="
						+ objPatientCategory.getNpatientcatcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ";
				jdbcTemplate.execute(updateQueryString);

				objPatientCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedPatientCategoryList.add(objPatientCategory);
				auditUtilityFunction.fnInsertAuditAction(deletedPatientCategoryList, 1, null,
						Arrays.asList("IDS_DELETEPATIENTCATEGORY"), userInfo);

				return getPatientCategory(userInfo);

			} else {

				// status code:417
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to fetch the active patientcategory objects for the
	 * specified Patient Category Name and Code.
	 * 
	 * @param nprojecttypecode [int] for validating specified project type.
	 * 
	 * @param spatientcatname [String] and code it to be checked the duplicate in
	 *                        the patientcategory table.
	 * @param nmasterSiteCode [int] site code of the patientcategory
	 * @return list of active patientcategory code(s) based on the specified Patient
	 *         Category Name and Code
	 * @throws Exception
	 */
	private List<PatientCategory> getPatientCategoryName(int nprojecttypecode, String spatientcatname, short code,
			int nmasterSiteCode, int npatientcatcode) throws Exception {
		String spatientcatquery = "";
		if (npatientcatcode != 0) {
			spatientcatquery = "and npatientcatcode  <> " + npatientcatcode + "";
		}
//ALPD-4462Patient Category -> Added the project type Combo box. 
		String strQuery = "SELECT"
				+ " CASE WHEN EXISTS (SELECT spatientcatname  FROM patientcategory WHERE nprojecttypecode="
				+ nprojecttypecode + " and spatientcatname=N'" + stringUtilityFunction.replaceQuote(spatientcatname)
				+ "' and nsitecode=" + nmasterSiteCode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + spatientcatquery + ")"
				+ " THEN true " + " ELSE false"
				+ " END AS ispatientcatnameexists,CASE WHEN EXISTS (SELECT ncode  FROM patientcategory WHERE  nprojecttypecode="
				+ nprojecttypecode + " and ncode=" + code + " and nsitecode=" + nmasterSiteCode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + spatientcatquery + ")"
				+ " THEN true" + " ELSE false" + " END AS iscodeexists";

		return (List<PatientCategory>) jdbcTemplate.query(strQuery, new PatientCategory());
	}

}
