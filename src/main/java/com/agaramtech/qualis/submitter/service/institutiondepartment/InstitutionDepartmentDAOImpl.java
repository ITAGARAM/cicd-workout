package com.agaramtech.qualis.submitter.service.institutiondepartment;

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
import com.agaramtech.qualis.submitter.model.InstitutionDepartment;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "InstitutionDepartment" table
 * by implementing methods from its interface.
 * 
 * @author ATE224
 * @version 10.0.0.2
 * @since 14 - July -2022
 */
@AllArgsConstructor
@Repository

public class InstitutionDepartmentDAOImpl implements InstitutionDepartmentDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstitutionDepartmentDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active InstitutionDepartment for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InstitutionDepartment
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getInstitutionDepartment(final UserInfo userInfo) throws Exception {
		final String strQuery = "select ninstitutiondeptcode,sinstitutiondeptname,sinstitutiondeptcode,nsitecode,nstatus, to_char(dmodifieddate, '"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate from institutiondepartment "
				+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ninstitutiondeptcode>0  and nsitecode=" + userInfo.getNmastersitecode();
		LOGGER.info("getInstitutionDepartment--->" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new InstitutionDepartment()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active InstitutionDepartment object based on
	 * the specified ninstitutiondeptcode.
	 * 
	 * @param ninstitutiondeptcode [int] primary key of InstitutionDepartment object
	 * @return response entity object holding response status and data of
	 *         InstitutionDepartment object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public InstitutionDepartment getActiveInstitutionDepartmentById(final int ninstitutiondeptcode) throws Exception {
		final String strQuery = "select ninstitutiondeptcode,sinstitutiondeptname,sinstitutiondeptcode,nsitecode,nstatus "
				+ " from institutiondepartment " + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ninstitutiondeptcode = "
				+ ninstitutiondeptcode;
		return (InstitutionDepartment) jdbcUtilityFunction.queryForObject(strQuery, InstitutionDepartment.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to InstitutionDepartment table. Need
	 * to check for duplicate entry of InstitutionDepartment name for the specified
	 * site before saving into database.
	 * 
	 * @param objInstitutionDepartment [InstitutionDepartment] object holding
	 *                                 details to be added in InstitutionDepartment
	 *                                 table
	 * @return inserted InstitutionDepartment object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createInstitutionDepartment(final InstitutionDepartment objInstitutionDepartment,
			final UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table institutiondepartment "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedInstitutionDepartmentList = new ArrayList<>();
		final InstitutionDepartment institutionDepartmentByName = getInstitutionDepartmentByName(
				objInstitutionDepartment.getSinstitutiondeptname(), objInstitutionDepartment.getNsitecode());
		final InstitutionDepartment institutionDepartmentByCode = getInstitutionDepartmentByCode(
				objInstitutionDepartment.getSinstitutiondeptcode(), objInstitutionDepartment.getNsitecode());
		String alert = "";
		if (institutionDepartmentByName == null) {
			String sequencenoquery = "select nsequenceno from seqnosubmittermanagement where stablename ='institutiondepartment'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			final String insertquery = "Insert into institutiondepartment (ninstitutiondeptcode,sinstitutiondeptname,sinstitutiondeptcode,dmodifieddate,nsitecode,nstatus) "
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(objInstitutionDepartment.getSinstitutiondeptname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objInstitutionDepartment.getSinstitutiondeptcode()) + "',"
					+ "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			final String updatequery = "update seqnosubmittermanagement set nsequenceno =" + nsequenceno
					+ " where stablename='institutiondepartment'";
			jdbcTemplate.execute(updatequery);
			objInstitutionDepartment.setNinstitutiondeptcode(nsequenceno);
			savedInstitutionDepartmentList.add(objInstitutionDepartment);
			multilingualIDList.add("IDS_ADDINSTITUTIONDEPARTMENT");
			auditUtilityFunction.fnInsertAuditAction(savedInstitutionDepartmentList, 1, null, multilingualIDList,
					userInfo);
			return getInstitutionDepartment(userInfo);
		} else {
			if (institutionDepartmentByName != null && institutionDepartmentByCode != null) {
				alert = commonFunction.getMultilingualMessage("IDS_INSTITUTIONDEPARTMENTNAME",
						userInfo.getSlanguagefilename()) + " and "
						+ commonFunction.getMultilingualMessage("IDS_INSTITUTIONDEPARTMENTCODE",
								userInfo.getSlanguagefilename());
			}
			if (institutionDepartmentByName != null) {
				alert = commonFunction.getMultilingualMessage("IDS_INSTITUTIONDEPARTMENTNAME",
						userInfo.getSlanguagefilename());
			}
			if (institutionDepartmentByCode != null) {
				alert = commonFunction.getMultilingualMessage("IDS_INSTITUTIONDEPARTMENTCODE",
						userInfo.getSlanguagefilename());
			}
			return new ResponseEntity<>(alert + " "
					+ commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()).toLowerCase(),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the active InstitutionDepartment objects for the
	 * specified InstitutionDepartment name and site.
	 * 
	 * @param sinstitutiondeptname [String] InstitutionDepartment of the unit
	 * @param nmasterSiteCode      [int] site code of the unit
	 * @return list of active InstitutionDepartment code(s) based on the specified
	 *         InstitutionDepartment name and site
	 * @throws Exception
	 */
	private InstitutionDepartment getInstitutionDepartmentByName(final String sinstitutiondeptname,
			final int nmasterSiteCode) throws Exception {
		final String strQuery = "select sinstitutiondeptname,sinstitutiondeptcode from institutiondepartment where "
				+ "  nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
				+ nmasterSiteCode + " and sinstitutiondeptname = N'"
				+ stringUtilityFunction.replaceQuote(sinstitutiondeptname) + "' ";
		return (InstitutionDepartment) jdbcUtilityFunction.queryForObject(strQuery, InstitutionDepartment.class,
				jdbcTemplate);
	}

	private InstitutionDepartment getInstitutionDepartmentByCode(final String sinstitutiondeptcode,
			final int nmasterSiteCode) throws Exception {
		final String strQuery = "select sinstitutiondeptname,sinstitutiondeptcode from institutiondepartment where "
				+ "  nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
				+ nmasterSiteCode + " and sinstitutiondeptcode = N'"
				+ stringUtilityFunction.replaceQuote(sinstitutiondeptcode) + "' ";
		return (InstitutionDepartment) jdbcUtilityFunction.queryForObject(strQuery, InstitutionDepartment.class,jdbcTemplate);
	}

	/**
	 * This method is used to update entry in InstitutionDepartment table. Need to
	 * validate that the InstitutionDepartment object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * InstitutionDepartment name for the specified site before saving into
	 * database. Need to check that there should be only one default
	 * InstitutionDepartment for a site
	 * 
	 * @param objInstitutionDepartment [InstitutionDepartment] object holding
	 *                                 details to be updated in
	 *                                 InstitutionDepartment table
	 * @return response entity object holding response status and data of updated
	 *         InstitutionDepartment object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateInstitutionDepartment(final InstitutionDepartment objInstitutionDepartment,
			final UserInfo userInfo) throws Exception {
		final InstitutionDepartment objinstitutionDepartment = getActiveInstitutionDepartmentById(
				objInstitutionDepartment.getNinstitutiondeptcode());
		String alert = "";

		if (objinstitutionDepartment == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryName = "select sinstitutiondeptcode,sinstitutiondeptname from institutiondepartment where sinstitutiondeptname = '"
					+ stringUtilityFunction.replaceQuote(objInstitutionDepartment.getSinstitutiondeptname())
					+ "'  and ninstitutiondeptcode <> " + objInstitutionDepartment.getNinstitutiondeptcode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode =" + userInfo.getNmastersitecode();
			final List<InstitutionDepartment> institutionDepartmentNameList = jdbcTemplate.query(queryName,
					new InstitutionDepartment());
			final String queryCode = "select sinstitutiondeptcode,sinstitutiondeptname from institutiondepartment where sinstitutiondeptcode='"
					+ stringUtilityFunction.replaceQuote(objInstitutionDepartment.getSinstitutiondeptcode())
					+ "'  and ninstitutiondeptcode <> " + objInstitutionDepartment.getNinstitutiondeptcode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode =" + userInfo.getNmastersitecode();
			final List<InstitutionDepartment> institutionDepartmentCodeList = jdbcTemplate.query(queryCode,
					new InstitutionDepartment());
			if (institutionDepartmentNameList.isEmpty() && institutionDepartmentCodeList.isEmpty()) {
				final String updateQueryString = "update institutiondepartment set sinstitutiondeptname='"
						+ stringUtilityFunction.replaceQuote(objInstitutionDepartment.getSinstitutiondeptname()) + "', "
						+ "sinstitutiondeptcode ='"
						+ stringUtilityFunction.replaceQuote(objInstitutionDepartment.getSinstitutiondeptcode())
						+ "',dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where ninstitutiondeptcode =" + objInstitutionDepartment.getNinstitutiondeptcode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITINSTITUTIONDEPARTMENT");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(objInstitutionDepartment);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(objinstitutionDepartment);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getInstitutionDepartment(userInfo);

			} else {
				if (!institutionDepartmentNameList.isEmpty() && !institutionDepartmentCodeList.isEmpty()) {
					alert = commonFunction.getMultilingualMessage("IDS_INSTITUTIONDEPARTMENTNAME",
							userInfo.getSlanguagefilename()) + " and "
							+ commonFunction.getMultilingualMessage("IDS_INSTITUTIONDEPARTMENTCODE",
									userInfo.getSlanguagefilename());
				} else if (!institutionDepartmentNameList.isEmpty()) {
					alert = commonFunction.getMultilingualMessage("IDS_INSTITUTIONDEPARTMENTNAME",
							userInfo.getSlanguagefilename());
				} else {
					alert = commonFunction.getMultilingualMessage("IDS_INSTITUTIONDEPARTMENTCODE",
							userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(alert + " "
						+ commonFunction
								.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
										userInfo.getSlanguagefilename())
								.toLowerCase(),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in institutiondepartment table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used inside transaction screen.
	 * 
	 * @param objinstitutiondepartment [institutiondepartment] an Object holds the
	 *                                 record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         institutiondepartment object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteInstitutionDepartment(final InstitutionDepartment objInstitutionDepartment,
			final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedInstitutionDepartmentList = new ArrayList<>();
		final InstitutionDepartment institutionDepartment = getActiveInstitutionDepartmentById(
				objInstitutionDepartment.getNinstitutiondeptcode());
		if (institutionDepartment == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_SUBMITTER' as Msg from submitter where ninstitutiondeptcode= "
					+ objInstitutionDepartment.getNinstitutiondeptcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(objInstitutionDepartment.getNinstitutiondeptcode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final String updateQueryString = "update institutiondepartment set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ninstitutiondeptcode="
						+ institutionDepartment.getNinstitutiondeptcode();
				jdbcTemplate.execute(updateQueryString);
				objInstitutionDepartment
						.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedInstitutionDepartmentList.add(objInstitutionDepartment);
				multilingualIDList.add("IDS_DELETEINSTITUTIONDEPARTMENT");
				auditUtilityFunction.fnInsertAuditAction(savedInstitutionDepartmentList, 1, null, multilingualIDList,
						userInfo);
				return getInstitutionDepartment(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}