package com.agaramtech.qualis.barcode.service.visitnumber;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.barcode.model.VisitNumber;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.project.service.projecttype.ProjectTypeDAO;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "VisitNumber" table by
 * implementing methods from its interface.
 * 
 */
@AllArgsConstructor
@Repository
public class VisitNumberDAOImpl implements VisitNumberDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(VisitNumberDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectTypeDAO projectTypeDAO;

	/**
	 * This method is used to retrieve list of all active VisitNumber for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         VisitNumber
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getVisitNumber(UserInfo userInfo) throws Exception {
		final String strQuery = "select v.nvisitnumbercode, v.svisitnumber,v.ncodelength, v.scode, v.nsitecode, v.nstatus, v.nprojecttypecode, p.sprojecttypename"
				+ " from visitnumber v, projecttype p " + " where v.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and v.nvisitnumbercode > 0 and v.nsitecode = " + userInfo.getNmastersitecode()
				+ " and p.nsitecode = " + userInfo.getNmastersitecode()
				+ " and v.nprojecttypecode = p.nprojecttypecode";
		LOGGER.info("getSampleAppearance -->" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new VisitNumber()), HttpStatus.OK);

	}

	/**
	 * This method is used to fetch the active visitnumber objects for the specified
	 * svisitnumber name and code.
	 * 
	 * @param nvisitnumbercode [int] primary key in visitnumber table.
	 * @param svisitnumber    [String] and code it to be checked the duplicate in
	 *                        the visitnumber table.
	 * @param nmasterSiteCode [int] site code of the unit
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return list of active visitnumber code(s) based on the specified
	 *         svisitnumber name and code
	 * @throws Exception that are thrown from this DAO layer
	 */
	private List<VisitNumber> getExistingRecord(int nvisitnumbercode, VisitNumber visitNumber, UserInfo userInfo)
			throws Exception {
		String visitcode = "";
		if (nvisitnumbercode != 0) {
			visitcode = "and nvisitnumbercode <> " + visitNumber.getNvisitnumbercode() + "";
		}
		final String strQuery = "select "
				+ "CASE WHEN EXISTS(select svisitnumber from visitnumber where svisitnumber = N'"
				+ stringUtilityFunction.replaceQuote(visitNumber.getSvisitnumber()) + "' and nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprojecttypecode = "
				+ visitNumber.getNprojecttypecode() + " " + visitcode + " )" + " THEN TRUE" + " ELSE FALSE "
				+ " End AS isvisitnumber, CASE when exists (select scode from visitnumber where scode = '"
				+ stringUtilityFunction.replaceQuote(visitNumber.getScode()) + "' and nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprojecttypecode = "
				+ visitNumber.getNprojecttypecode() + " " + visitcode + " )" + " THEN TRUE " + " ELSE FALSE "
				+ " End AS iscode ";
		return jdbcTemplate.query(strQuery, new VisitNumber());
	}

	/**
	 * This method is used to add a new entry to VisitNumber table. Need to check
	 * for duplicate entry of VisitNumber based on ProjectType for the specified
	 * site before saving into database.
	 * 
	 * @param visitnumber [VisitNumber] object holding details to be added in
	 *                    VisitNumber table
	 *  @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return inserted VisitNumber object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createVisitNumber(VisitNumber visitnumber, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedVisitNumberList = new ArrayList<>();
		final ResponseEntity<Object> activeProjectType = projectTypeDAO
				.getActiveProjectTypeById(visitnumber.getNprojecttypecode(), userInfo);
		if (activeProjectType.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			final String alert = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final List<VisitNumber> existingNumberList = getExistingRecord(visitnumber.getNvisitnumbercode(), visitnumber,
				userInfo);

		if (existingNumberList.get(0).getIsvisitnumber() == false && existingNumberList.get(0).getIscode() == false) {
			if (!visitnumber.getScode().isEmpty()) {
				String sequencenoquery = "select nsequenceno from seqnobarcode where stablename = 'visitnumber'";
				int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
				nsequenceno++;
				String insertquery = "Insert into visitnumber (nvisitnumbercode,svisitnumber,ncodelength,scode,nprojecttypecode,dmodifieddate,nsitecode,nstatus)"
						+ "values(" + nsequenceno + ",N'"
						+ stringUtilityFunction.replaceQuote(visitnumber.getSvisitnumber()) + "',"
						+ visitnumber.getNcodelength() + ",N'"
						+ stringUtilityFunction.replaceQuote(visitnumber.getScode()) + "',"
						+ visitnumber.getNprojecttypecode() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "'," + " " + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);

				String updatequery = "update seqnobarcode set nsequenceno =" + nsequenceno
						+ " where stablename='visitnumber'";
				jdbcTemplate.execute(updatequery);
				visitnumber.setNvisitnumbercode(nsequenceno);
				savedVisitNumberList.add(visitnumber);
				multilingualIDList.add("IDS_ADDVISITNUMBER");
				auditUtilityFunction.fnInsertAuditAction(savedVisitNumberList, 1, null, multilingualIDList, userInfo);
				return getVisitNumber(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			String alert = "";
			final Boolean visitno = existingNumberList.get(0).getIsvisitnumber();
			final Boolean code = existingNumberList.get(0).getIscode();
			if (visitno == true && code == true) {
				alert = commonFunction.getMultilingualMessage("IDS_VISITNUMBER", userInfo.getSlanguagefilename())
						+ " and " + commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
			} else if (visitno == true) {
				alert = commonFunction.getMultilingualMessage("IDS_VISITNUMBER", userInfo.getSlanguagefilename());
			} else {
				alert = commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
			}
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve active VisitNumber object based on the
	 * specified nvisitnumbercode.
	 * 
	 * @param nvisitnumbercode [int] primary key of VisitNumber object
	 * @param userInfo         [UserInfo] holding logged in user details based on
	 *                         which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         VisitNumber object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public VisitNumber getActiveVisitNumberById(int nvisitnumbercode, UserInfo userInfo) throws Exception {
		final String strQuery = "select v.nvisitnumbercode, v.svisitnumber,v.ncodelength, v.scode, v.nsitecode, v.nstatus, v.nprojecttypecode, p.sprojecttypename from visitnumber v, projecttype p"
				+ " where v.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and v.nsitecode=" + userInfo.getNmastersitecode() + " and p.nsitecode="
				+ userInfo.getNmastersitecode() + " and nvisitnumbercode = " + nvisitnumbercode
				+ " and v.nprojecttypecode = p.nprojecttypecode";
		return (VisitNumber) jdbcUtilityFunction.queryForObject(strQuery, VisitNumber.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in VisitNumber table. Need to validate
	 * that the VisitNumber object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of VisitNumber for the
	 * specified site before saving into database.
	 * 
	 * @param visitNumber [VisitNumber] object holding details to be updated in
	 *                    VisitNumber table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         VisitNumber object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateVisitNumber(VisitNumber visitNumber, UserInfo userInfo) throws Exception {
		final VisitNumber objvisitNumber = getActiveVisitNumberById(visitNumber.getNvisitnumbercode(), userInfo);
		final ResponseEntity<Object> activeProjectType = projectTypeDAO
				.getActiveProjectTypeById(visitNumber.getNprojecttypecode(), userInfo);

		if (activeProjectType.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			final String alert = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		if (objvisitNumber == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<VisitNumber> existingNumberList = getExistingRecord(visitNumber.getNvisitnumbercode(),
					visitNumber, userInfo);

			if (existingNumberList.get(0).getIsvisitnumber() == false && existingNumberList.get(0).getIscode() == false
					&& visitNumber.getScode().length() > 0) {
				final String updateQueryString = "update visitnumber set svisitnumber='"
						+ stringUtilityFunction.replaceQuote(visitNumber.getSvisitnumber()) + "', ncodelength = "
						+ visitNumber.getNcodelength() + ", " + "scode ='"
						+ stringUtilityFunction.replaceQuote(visitNumber.getScode()) + "',dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nprojecttypecode="
						+ visitNumber.getNprojecttypecode() + " where nsitecode=" + userInfo.getNmastersitecode()
						+ " and nvisitnumbercode =" + visitNumber.getNvisitnumbercode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITVISITNUMBER");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(visitNumber);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(objvisitNumber);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getVisitNumber(userInfo);
			} else {
				String alert = "";
				final Boolean visitno = existingNumberList.get(0).getIsvisitnumber();
				final Boolean code = existingNumberList.get(0).getIscode();
				if (visitno == true && code == true) {
					alert = commonFunction.getMultilingualMessage("IDS_VISITNUMBER", userInfo.getSlanguagefilename())
							+ " and "
							+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				} else if (visitno == true) {
					alert = commonFunction.getMultilingualMessage("IDS_VISITNUMBER", userInfo.getSlanguagefilename());
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
	 * This method id used to delete an entry in VisitNumber table Need to check the
	 * record is already deleted or not
	 * 
	 * @param visitNumber [VisitNumber] an Object holds the record to be deleted
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return a response entity with corresponding HTTP status and an VisitNumber
	 *         object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteVisitNumber(VisitNumber visitNumber, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedvisitNumberList = new ArrayList<>();
		final VisitNumber objvisitNumber = getActiveVisitNumberById(visitNumber.getNvisitnumbercode(), userInfo);
		if (objvisitNumber == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String recordInProcessType = "select 'IDS_ALIQUOTPLAN' as Msg from aliquotplan where "
					+ " nvisitnumbercode=" + visitNumber.getNvisitnumbercode() + "  " + " and nsitecode="
					+ userInfo.getNmastersitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			valiDatorDel = projectDAOSupport.getTransactionInfo(recordInProcessType, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(visitNumber.getNvisitnumbercode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {

				final String updateQueryString = "update visitnumber set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where nsitecode="
						+ userInfo.getNmastersitecode() + " and nvisitnumbercode=" + visitNumber.getNvisitnumbercode();

				jdbcTemplate.execute(updateQueryString);
				visitNumber.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedvisitNumberList.add(visitNumber);
				multilingualIDList.add("IDS_DELETEVISITNUMBER");
				auditUtilityFunction.fnInsertAuditAction(savedvisitNumberList, 1, null, multilingualIDList, userInfo);

			} else {

				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);

			}

			return getVisitNumber(userInfo);

		}
	}

}
