package com.agaramtech.qualis.quotation.service.discountband;

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
import com.agaramtech.qualis.quotation.model.DiscountBand;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class DiscountBandDAOImpl implements DiscountBandDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(DiscountBandDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available discountBands for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         discountBands
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDiscountBand(final UserInfo userInfo) throws Exception {

		final String strQuery = "select db.*,concat(sdiscountbandname,' (',namount,'%)') as sdiscountbandname1"
				+ " from DiscountBand db" + " where db.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and db.ndiscountbandcode>0 "
				+ " and db.nsitecode=" + userInfo.getNmastersitecode() + " order by db.ndiscountbandcode asc";

		LOGGER.info("getDiscountBand--->" + strQuery);
		final List<String> lstcolumns = new ArrayList<>();
		lstcolumns.add("sdisplaystatus");
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new DiscountBand()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to discountBand table. DiscountBand
	 * Name is unique across the database. Need to check for duplicate entry of
	 * discountBand name for the specified site before saving into database. * Need
	 * to check that there should be only one default discountBand for a site.
	 * 
	 * @param objDiscountBand [DiscountBand] object holding details to be added in
	 *                        discountBand table
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return saved discountBand object with status code 200 if saved successfully
	 *         else if the discountBand already exists, response will be returned as
	 *         'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createDiscountBand(final DiscountBand discountband, final UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedVATBandList = new ArrayList<>();

		final DiscountBand discountBandByName = getDiscountBandByName(discountband.getSdiscountbandname(),
				userInfo.getNmastersitecode());

		if (discountBandByName == null) {

			int nSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoquotationmanagement where stablename='discountband'", Integer.class);
			nSeqNo++;

			final String projecttypeInsert = "insert into discountband(ndiscountbandcode,sdiscountbandname,namount,dmodifieddate,nsitecode,nstatus)"
					+ " values(" + nSeqNo + ",N'"
					+ stringUtilityFunction.replaceQuote(discountband.getSdiscountbandname()) + "',"
					+ discountband.getNamount() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")";

			jdbcTemplate.execute(projecttypeInsert);
			jdbcTemplate.execute(
					"update seqnoquotationmanagement set nsequenceno = " + nSeqNo + " where stablename='discountband'");

			discountband.setNdiscountbandcode(nSeqNo);
			savedVATBandList.add(discountband);
			multilingualIDList.add("IDS_ADDDISCOUNTBAND");

			auditUtilityFunction.fnInsertAuditAction(savedVATBandList, 1, null, multilingualIDList, userInfo);
			return getDiscountBand(userInfo);
		} else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the discountBand object for the specified
	 * discountBand name and site.
	 * 
	 * @param sdiscountbandname [String] name of the discountBand
	 * @param nmasterSiteCode   [int] site code of the discountBand
	 * @return discountBand object based on the specified discountBand name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private DiscountBand getDiscountBandByName(final String sdiscountbandname, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select ndiscountbandcode from discountband where sdiscountbandname = N'"
				+ stringUtilityFunction.replaceQuote(sdiscountbandname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (DiscountBand) jdbcUtilityFunction.queryForObject(strQuery, DiscountBand.class, jdbcTemplate);
	}

	/**
	 * This method is used to retrieve active discountBand object based on the
	 * specified ndiscountbandcode.
	 * 
	 * @param ndiscountbandcode [int] primary key of discountBand object
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         discountBand object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveDiscountBandById(final int ndiscountbandcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select * from discountband db where db.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and db.ndiscountbandcode = "
				+ ndiscountbandcode;

		final DiscountBand discountband = (DiscountBand) jdbcUtilityFunction.queryForObject(strQuery,
				DiscountBand.class, jdbcTemplate);
		if (discountband == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(discountband, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to update entry in discountBand table. Need to validate
	 * that the discountBand object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of discountBand name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default discountBand for a site
	 * 
	 * @param objDiscountBand [DiscountBand] object holding details to be updated in
	 *                        discountBand table
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return saved discountBand object with status code 200 if saved successfully
	 *         else if the discountBand already exists, response will be returned as
	 *         'Already Exists' with status code 409 else if the discountBand to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateDiscountBand(final DiscountBand DiscountBand, final UserInfo userInfo)
			throws Exception {
		final ResponseEntity<Object> discountBandResponse = getActiveDiscountBandById(
				DiscountBand.getNdiscountbandcode(), userInfo);

		if (discountBandResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return discountBandResponse;
		} else {
			final String queryString = "select ndiscountbandcode from DiscountBand where sdiscountbandname = N'"
					+ stringUtilityFunction.replaceQuote(DiscountBand.getSdiscountbandname())
					+ "' and ndiscountbandcode <> " + DiscountBand.getNdiscountbandcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			final List<DiscountBand> discountbandList = (List<DiscountBand>) jdbcTemplate.query(queryString,
					new DiscountBand());

			if (discountbandList.isEmpty()) {
				final String updateQueryString = "update DiscountBand set sdiscountbandname=N'"
						+ stringUtilityFunction.replaceQuote(DiscountBand.getSdiscountbandname()) + "', namount ="
						+ DiscountBand.getNamount() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ndiscountbandcode="
						+ DiscountBand.getNdiscountbandcode();

				jdbcTemplate.execute(updateQueryString);

				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITDISCOUNTBAND");

				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(DiscountBand);

				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(discountBandResponse.getBody());

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);

				return getDiscountBand(userInfo);
			} else {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in DiscountBand table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other tables such as 'quotationtotalamount'
	 * 
	 * @param objDiscountBand [DiscountBand] an Object holds the record to be
	 *                        deleted
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return a response entity with list of available discountBand objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteDiscountBand(final DiscountBand DiscountBand, final UserInfo userInfo)
			throws Exception {

		final ResponseEntity<Object> discountBandResponse = getActiveDiscountBandById(
				DiscountBand.getNdiscountbandcode(), userInfo);

		boolean validRecord = true;

		if (discountBandResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return discountBandResponse;
		} else {

			final String query = "select 'IDS_QUOTATION' as Msg from quotationtotalamount where ndiscountbandcode= "
					+ DiscountBand.getNdiscountbandcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(DiscountBand.getNdiscountbandcode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedVATBandList = new ArrayList<>();
				final String updateQueryString = "update DiscountBand set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ndiscountbandcode="
						+ DiscountBand.getNdiscountbandcode();

				jdbcTemplate.execute(updateQueryString);
				DiscountBand.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedVATBandList.add(DiscountBand);
				multilingualIDList.add("IDS_DELETEDISCOUNTBAND");
				auditUtilityFunction.fnInsertAuditAction(deletedVATBandList, 1, null, multilingualIDList, userInfo);

			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
		return getDiscountBand(userInfo);
	}

}
