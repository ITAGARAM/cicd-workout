package com.agaramtech.qualis.quotation.service.vatband;

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
import com.agaramtech.qualis.quotation.model.VATBand;

import lombok.AllArgsConstructor;

/**
 * This class implements methods to perform CRUD operations on the VATBand
 * table, allowing for creating, reading, updating, and deleting VAT band
 * entries.
 */
@AllArgsConstructor
@Repository
public class VATBandDAOImpl implements VATBandDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(VATBandDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method retrieves a list of all active VAT bands for the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged-in user details
	 * @return ResponseEntity containing status and list of all active VAT bands
	 * @throws Exception if any error occurs in this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getVATBand(final UserInfo userInfo) throws Exception {
		String strQuery = "select vb.*,concat(svatbandname,' (',namount,'%)') as svatbandname1" + " from VATBand vb"
				+ " where vb.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and vb.nvatbandcode>0 " + " and vb.nsitecode=" + userInfo.getNmastersitecode()
				+ " order by vb.nvatbandcode asc";
		LOGGER.info("getVATBand -->" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new VATBand()), HttpStatus.OK);
	}

	/**
	 * This method creates a new VAT Band in the database.
	 * 
	 * @param vatband  [VATBand] object to be created
	 * @param userInfo [UserInfo] logged-in user details
	 * @return ResponseEntity containing status and the updated list of VAT bands
	 * @throws Exception if any error occurs during the creation process
	 */
	@Override
	public ResponseEntity<Object> createVATBand(final VATBand vatband, final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedVATbandList = new ArrayList<>();
		final VATBand vatBandListByName = getVATBandByName(vatband.getSvatbandname(), userInfo.getNmastersitecode());
		if (vatBandListByName == null) {
			int nSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoquotationmanagement where stablename='vatband'", Integer.class);
			nSeqNo++;
			String projecttypeInsert = "insert into vatband(nvatbandcode,svatbandname,namount,dmodifieddate,nsitecode,nstatus)"
					+ " values(" + nSeqNo + ",N'" + stringUtilityFunction.replaceQuote(vatband.getSvatbandname()) + "',"
					+ vatband.getNamount() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")";
			jdbcTemplate.execute(projecttypeInsert);
			jdbcTemplate.execute(
					"update seqnoquotationmanagement set nsequenceno = " + nSeqNo + " where stablename='vatband'");
			vatband.setNvatbandcode(nSeqNo);
			savedVATbandList.add(vatband);
			multilingualIDList.add("IDS_ADDVATBAND");
			auditUtilityFunction.fnInsertAuditAction(savedVATbandList, 1, null, multilingualIDList, userInfo);
			return getVATBand(userInfo.getNmastersitecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method retrieves a list of active VAT Bands for the specified site.
	 * 
	 * @param nmasterSiteCode [int] site code to fetch VAT Bands for
	 * @return ResponseEntity containing status and the list of VAT Bands
	 * @throws Exception if any error occurs in this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getVATBand(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from vatband vb where vb.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nvatbandcode > 0 "
				+ " and vb.nsitecode = " + nmasterSiteCode;
		return new ResponseEntity<>((List<VATBand>) jdbcTemplate.query(strQuery, new VATBand()), HttpStatus.OK);
	}

	/**
	 * This method checks if a VAT Band with the specified name exists for the given
	 * site.
	 * 
	 * @param svatbandname    [String] name of the VAT Band to check for
	 * @param nmasterSiteCode [int] site code to check against
	 * @return VATBand object if found, null otherwise
	 * @throws Exception if any error occurs in this DAO layer
	 */
	private VATBand getVATBandByName(final String svatbandname, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select nvatbandcode from vatband where svatbandname = N'"
				+ stringUtilityFunction.replaceQuote(svatbandname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = " + nmasterSiteCode;
		return (VATBand) jdbcUtilityFunction.queryForObject(strQuery, VATBand.class, jdbcTemplate);
	}

	/**
	 * This method retrieves an active VAT Band by its ID.
	 * 
	 * @param nvatbandcode [int] primary key of the VAT Band
	 * @param userInfo     [UserInfo] logged-in user details
	 * @return ResponseEntity containing the VAT Band object
	 * @throws Exception if any error occurs in this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveVATBandById(final int nvatbandcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select * from vatband vb where vb.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and vb.nvatbandcode = "
				+ nvatbandcode;
		final VATBand vatband = (VATBand) jdbcUtilityFunction.queryForObject(strQuery, VATBand.class, jdbcTemplate);
		if (vatband == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(vatband, HttpStatus.OK);
		}
	}

	/**
	 * This method updates an existing VAT Band's details.
	 * 
	 * @param VATBand  [VATBand] object with updated data
	 * @param userInfo [UserInfo] logged-in user details
	 * @return ResponseEntity containing the updated VAT Bands list
	 * @throws Exception if any error occurs during the update process
	 */
	@Override
	public ResponseEntity<Object> updateVATBand(final VATBand VATBand, final UserInfo userInfo) throws Exception {

		final ResponseEntity<Object> vatBandResponse = getActiveVATBandById(VATBand.getNvatbandcode(), userInfo);

		if (vatBandResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return vatBandResponse;
		} else {
			final String queryString = "select nvatbandcode from VATBand where svatbandname = N'"
					+ stringUtilityFunction.replaceQuote(VATBand.getSvatbandname()) + "' and nvatbandcode <> "
					+ VATBand.getNvatbandcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode="
					+ userInfo.getNmastersitecode() + "";
			final VATBand vatBand = (VATBand) jdbcUtilityFunction.queryForObject(queryString, VATBand.class,
					jdbcTemplate);
			if (vatBand == null) {
				final String updateQueryString = "update VATBand set svatbandname=N'"
						+ stringUtilityFunction.replaceQuote(VATBand.getSvatbandname()) + "', namount ="
						+ VATBand.getNamount() + ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nvatbandcode=" + VATBand.getNvatbandcode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITVATBAND");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(VATBand);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(vatBandResponse.getBody());
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getVATBand(userInfo.getNmastersitecode());
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method deletes a VAT Band.
	 * 
	 * @param VATBand  [VATBand] object to be deleted
	 * @param userInfo [UserInfo] logged-in user details
	 * @return ResponseEntity containing the status of the deletion
	 * @throws Exception if any error occurs during the deletion process
	 */
	@Override
	public ResponseEntity<Object> deleteVATBand(final VATBand VATBand, final UserInfo userInfo) throws Exception {
		final ResponseEntity<Object> vatBandResponse = getActiveVATBandById(VATBand.getNvatbandcode(), userInfo);
		boolean validRecord = true;
		if (vatBandResponse.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			return vatBandResponse;
		} else {
			final String query = "select 'IDS_QUOTATION' as Msg from quotationtotalamount where nvatbandcode= "
					+ VATBand.getNvatbandcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(VATBand.getNvatbandcode()),
						userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedVatBandList = new ArrayList<>();
				final String updateQueryString = "update VATBand set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nvatbandcode="
						+ VATBand.getNvatbandcode();
				jdbcTemplate.execute(updateQueryString);
				VATBand.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedVatBandList.add(VATBand);
				multilingualIDList.add("IDS_DELETEVATBAND");
				auditUtilityFunction.fnInsertAuditAction(deletedVatBandList, 1, null, multilingualIDList, userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
		return getVATBand(userInfo.getNmastersitecode());
	}
}
