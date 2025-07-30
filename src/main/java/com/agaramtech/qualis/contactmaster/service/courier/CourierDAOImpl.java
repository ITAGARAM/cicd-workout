package com.agaramtech.qualis.contactmaster.service.courier;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.contactmaster.model.Courier;
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
 * This class is used to perform CRUD Operation on "courier" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class CourierDAOImpl implements CourierDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CourierDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active courier for the specified
	 * site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         manufacturers
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getCourier(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select c.*,cy.scountryname from courier c,country cy where cy.ncountrycode=c.ncountrycode and  c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cy.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ncouriercode > 0 "
				+ " and c.nsitecode = " + nmasterSiteCode + "";
		LOGGER.info("getCourier-->" + strQuery);
		return new ResponseEntity<>((List<Courier>) jdbcTemplate.query(strQuery, new Courier()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to courier table. Need to check for
	 * duplicate entry of courier name for the specified site before saving into
	 * database.
	 * 
	 * @param courier [Courier] object holding details to be added in courier table
	 * @return response entity object holding response status and data of added
	 *         courier object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createCourier(Courier courier, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedCourierList = new ArrayList<>();
		final Courier courierByName = getCourierByName(courier.getScouriername(), courier.getNsitecode());
		if (courierByName == null) {
			final String sequenceNoQuery = "select nsequenceno from seqnocontactmaster where stablename ='courier' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
			nsequenceNo++;
			final String insertQuery = "INSERT INTO courier(ncouriercode, scouriername, scontactperson, saddress1, saddress2, saddress3, ncountrycode, sphoneno,"
					+ "smobileno, sfaxno, semail, dmodifieddate, nsitecode, nstatus) values(" + nsequenceNo + ",N'"
					+ stringUtilityFunction.replaceQuote(courier.getScouriername()) + "',N'"
					+ stringUtilityFunction.replaceQuote(courier.getScontactperson()) + "',N'"
					+ stringUtilityFunction.replaceQuote(courier.getSaddress1()) + "',N'"
					+ stringUtilityFunction.replaceQuote(courier.getSaddress2()) + "',N'"
					+ stringUtilityFunction.replaceQuote(courier.getSaddress3()) + "'," + courier.getNcountrycode()
					+ ",N'" + stringUtilityFunction.replaceQuote(courier.getSphoneno()) + "',N'"
					+ stringUtilityFunction.replaceQuote(courier.getSmobileno()) + "',N'"
					+ stringUtilityFunction.replaceQuote(courier.getSfaxno()) + "',N'"
					+ stringUtilityFunction.replaceQuote(courier.getSemail()) + "'" + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			jdbcTemplate.execute(insertQuery);
			final String updateQuery = "update seqnocontactmaster set nsequenceno =" + nsequenceNo
					+ " where stablename='courier' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			jdbcTemplate.execute(updateQuery);
			multilingualIDList.add("IDS_ADDCOURIER");
			auditUtilityFunction.fnInsertAuditAction(savedCourierList, 1, null, multilingualIDList, userInfo);
			return getCourier(userInfo.getNmastersitecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the active Courier objects for the specified
	 * courier name and site.
	 * 
	 * @param CourierName     [String] Courier name for which the records are to be
	 *                        fetched
	 * @param nmasterSiteCode [int] primary key of site object
	 * @return list of active Courier based on the specified Courier name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Courier getCourierByName(final String courierName, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ncouriercode from courier where scouriername = N'"
				+ stringUtilityFunction.replaceQuote(courierName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (Courier) jdbcUtilityFunction.queryForObject(strQuery, Courier.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in courier table. Need to validate that
	 * the manufaturer object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of courier name for the specified
	 * site before saving into database.
	 * 
	 * @param courier [Courier] object holding details to be updated in courier
	 *                table
	 * @return response entity object holding response status and data of updated
	 *         courier object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateCourier(Courier courier, UserInfo userInfo) throws Exception {
		final Courier objCourier = getActiveCourierById(courier.getNcouriercode());
		if (objCourier == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			final String queryString = "select ncouriercode from courier where scouriername = '"
					+ stringUtilityFunction.replaceQuote(courier.getScouriername()) + "' and ncouriercode <> "
					+ courier.getNcouriercode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNmastersitecode();
			final List<Courier> courierList = (List<Courier>) jdbcTemplate.query(queryString, new Courier());
			if (courierList.isEmpty()) {
				final String updateQueryString = "update courier set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', scouriername=N'"
						+ stringUtilityFunction.replaceQuote(courier.getScouriername()) + "',scontactperson =N'"
						+ stringUtilityFunction.replaceQuote(courier.getScontactperson()) + "',saddress1 =N'"
						+ stringUtilityFunction.replaceQuote(courier.getSaddress1()) + "',saddress2 =N'"
						+ stringUtilityFunction.replaceQuote(courier.getSaddress2()) + "',saddress3 =N'"
						+ stringUtilityFunction.replaceQuote(courier.getSaddress3()) + "',ncountrycode ="
						+ courier.getNcountrycode() + ",sphoneno =N'"
						+ stringUtilityFunction.replaceQuote(courier.getSphoneno()) + "',smobileno ='"
						+ stringUtilityFunction.replaceQuote(courier.getSmobileno()) + "',sfaxno =N'"
						+ stringUtilityFunction.replaceQuote(courier.getSfaxno()) + "',semail =N'"
						+ stringUtilityFunction.replaceQuote(courier.getSemail()) + "' where ncouriercode="
						+ courier.getNcouriercode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITCOURIER");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(courier);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(objCourier);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getCourier(courier.getNsitecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to retrieve active Courier object based on the specified
	 * ncouriercode.
	 * 
	 * @param ncouriercode [int] primary key of Courier object
	 * @return response entity object holding response status and data of Courier
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Courier getActiveCourierById(int ncouriercode) throws Exception {
		final String strQuery = "select * from courier c where c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.ncouriercode = " + ncouriercode;
		return (Courier) jdbcUtilityFunction.queryForObject(strQuery, Courier.class, jdbcTemplate);
	}

	/**
	 * This method is used to delete entry in courier table. Need to validate that
	 * the specified courier object is active and is not associated with any of its
	 * child tables before updating its nstatus to -1.
	 * 
	 * @param courier [Courier] object holding detail to be deleted in courier table
	 * @return response entity object holding response status and data of deleted
	 *         courier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteCourier(Courier courier, UserInfo userInfo) throws Exception {
		final Courier courierbyID = getActiveCourierById(courier.getNcouriercode());
		if (courierbyID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_GOODSIN' as Msg from goodsin where ncouriercode="
					+ courier.getNcouriercode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(courier.getNcouriercode()),
						userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedCourierList = new ArrayList<>();
				final String updateQueryString = "update courier set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncouriercode="
						+ courier.getNcouriercode();
				jdbcTemplate.execute(updateQueryString);
				courier.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedCourierList.add(courier);
				multilingualIDList.add("IDS_DELETECOURIER");
				auditUtilityFunction.fnInsertAuditAction(savedCourierList, 1, null, multilingualIDList, userInfo);
				return getCourier(courier.getNsitecode());
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getAllActiveCourier(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from courier d,country c where d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and d.ncouriercode > 0 and d.ncountrycode=c.ncountrycode" + " and d.nsitecode = " + nmasterSiteCode;
		return new ResponseEntity<>((List<Courier>) jdbcTemplate.query(strQuery, new Courier()), HttpStatus.OK);
	}
}
