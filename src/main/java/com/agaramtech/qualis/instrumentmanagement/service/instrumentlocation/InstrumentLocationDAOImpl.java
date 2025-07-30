package com.agaramtech.qualis.instrumentmanagement.service.instrumentlocation;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
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
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentLocation;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "InstrumentLocation" table by
 * implementing methods from its interface.
 */

@AllArgsConstructor
@Repository
public class InstrumentLocationDAOImpl implements InstrumentLocationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentLocationDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active instrumentlocation for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         instrumentlocation
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getInstrumentLocation(UserInfo userInfo) throws Exception {
		final String query = "select i.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus"
				+ " from instrumentlocation i,transactionstatus ts where i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and i.ninstrumentlocationcode > 0 and ts.ntranscode = i.ndefaultstatus and i.nsitecode = "
				+ userInfo.getNmastersitecode();
		LOGGER.info("getInstrumentLocation -->");
		return new ResponseEntity<>((List<InstrumentLocation>) jdbcTemplate.query(query, new InstrumentLocation()),
				HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active instrument location object based on
	 * the specified ninstrumentlocationcode.
	 * 
	 * @param ninstrumentlocationcode [int] primary key of instrument location
	 *                                object
	 * @return response entity object holding response status and data of instrument
	 *         location object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public InstrumentLocation getActiveInstrumentLocationById(int ninstrumentlocationcode, UserInfo userInfo)
			throws Exception {
		final String strQuery = "select i.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
				+ " from instrumentlocation i,transactionstatus ts " + " where i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode = i.ndefaultstatus"
				+ " and i.ninstrumentlocationcode = " + ninstrumentlocationcode;
		return (InstrumentLocation) jdbcUtilityFunction.queryForObject(strQuery, InstrumentLocation.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to instrumentlocation table. Need to
	 * check for duplicate entry of instrumentlocation name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default instrumentlocation for a site
	 * 
	 * @param instrumentLocation [InstrumentLocation] object holding details to be
	 *                           added in instrumentlocation table
	 * @return inserted instrumentlocation object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createInstrumentLocation(InstrumentLocation instrumentLocation, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedInstrumentLocationList = new ArrayList<>();
		final InstrumentLocation instrumentlocationByName = getInstrumentLocationByName(
				instrumentLocation.getSinstrumentlocationname(), instrumentLocation.getNsitecode());
		if (instrumentlocationByName == null) {
			if (instrumentLocation.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final InstrumentLocation defaultlocation = getInstrumentLocationByDefaultStatus(
						instrumentLocation.getNsitecode());
				if (defaultlocation != null) {
					final InstrumentLocation locationBeforeSave = SerializationUtils.clone(defaultlocation);
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(locationBeforeSave);
					locationBeforeSave
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					final String updateQueryString = " update instrumentlocation set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus()
							+ " where ninstrumentlocationcode =" + defaultlocation.getNinstrumentlocationcode();
					jdbcTemplate.execute(updateQueryString);
					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultlocation);
					multilingualIDList.add("IDS_EDITINSTRUMENTLOCATION");
					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
					multilingualIDList.clear();
				}
			}
			String sequencenoquery = "select nsequenceno from SeqNoInstrumentManagement where stablename ='instrumentlocation'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			String insertquery = "Insert into instrumentlocation (ninstrumentlocationcode,sinstrumentlocationname,sdescription,ndefaultstatus,dmodifieddate,nsitecode,nstatus) "
					+ " values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(instrumentLocation.getSinstrumentlocationname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(instrumentLocation.getSdescription()) + "',"
					+ instrumentLocation.getNdefaultstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update SeqNoInstrumentManagement set nsequenceno =" + nsequenceno
					+ " where stablename='instrumentlocation'";
			jdbcTemplate.execute(updatequery);
			instrumentLocation.setNinstrumentlocationcode(nsequenceno);
			savedInstrumentLocationList.add(instrumentLocation);
			multilingualIDList.add("IDS_ADDINSTRUMENTLOCATION");
			auditUtilityFunction.fnInsertAuditAction(savedInstrumentLocationList, 1, null, multilingualIDList,
					userInfo);
			return getInstrumentLocation(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the active instrumentlocation objects for the
	 * specified instrument location name and site.
	 * 
	 * @param sinstrumentlocationname [String] name of the instrumentlocation
	 * @param nmasterSiteCode         [int] site code of the instrumentlocation
	 * @return list of active instrumentlocation code(s) based on the specified
	 *         instrumentlocation name and site
	 * @throws Exception
	 */
	private InstrumentLocation getInstrumentLocationByName(final String instrumentlocationName,
			final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ninstrumentlocationcode from instrumentlocation where sinstrumentlocationname = N'"
				+ stringUtilityFunction.replaceQuote(instrumentlocationName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (InstrumentLocation) jdbcUtilityFunction.queryForObject(strQuery, InstrumentLocation.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to update entry in instrumentlocation table. Need to
	 * validate that the instrumentlocation object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * instrumentlocation name for the specified site before saving into database.
	 * Need to check that there should be only one default instrumentlocation for a
	 * site
	 * 
	 * @param instrumentLocation [InstrumentLocation] object holding details to be
	 *                           updated in instrumentlocation table
	 * @return response entity object holding response status and data of updated
	 *         instrumentLocation object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateInstrumentLocation(InstrumentLocation instrumentLocation, UserInfo userInfo)
			throws Exception {
		final InstrumentLocation instrument = getActiveInstrumentLocationById(
				instrumentLocation.getNinstrumentlocationcode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		if (instrument == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ninstrumentlocationcode from instrumentlocation where sinstrumentlocationname = '"
					+ stringUtilityFunction.replaceQuote(instrumentLocation.getSinstrumentlocationname())
					+ "' and ninstrumentlocationcode <> " + instrumentLocation.getNinstrumentlocationcode()
					+ " and nsitecode=" + userInfo.getNmastersitecode() + "" + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final InstrumentLocation instrumentlocation = (InstrumentLocation) jdbcUtilityFunction
					.queryForObject(queryString, InstrumentLocation.class, jdbcTemplate);
			if (instrumentlocation == null) {
				if (instrumentLocation.getNdefaultstatus() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {
					final InstrumentLocation defaultlocation = getInstrumentLocationByDefaultStatus(
							instrumentLocation.getNsitecode());
					if (defaultlocation != null && defaultlocation.getNinstrumentlocationcode() != instrumentLocation
							.getNinstrumentlocationcode()) {
						final InstrumentLocation locationBeforeSave = SerializationUtils.clone(defaultlocation);
						listBeforeUpdate.add(locationBeforeSave);
						defaultlocation
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						final String updateQueryString = " update instrumentlocation set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus()
								+ " where ninstrumentlocationcode=" + defaultlocation.getNinstrumentlocationcode();
						jdbcTemplate.execute(updateQueryString);
						listAfterUpdate.add(defaultlocation);
					}
				}
				final String updateQueryString = "update instrumentlocation set sinstrumentlocationname=N'"
						+ stringUtilityFunction.replaceQuote(instrumentLocation.getSinstrumentlocationname())
						+ "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(instrumentLocation.getSdescription())
						+ "', ndefaultstatus=" + instrumentLocation.getNdefaultstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ninstrumentlocationcode="
						+ instrumentLocation.getNinstrumentlocationcode() + ";";
				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(instrumentLocation);
				listBeforeUpdate.add(instrument);
				multilingualIDList.add("IDS_EDITINSTRUMENTLOCATION");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getInstrumentLocation(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to get a default instrumentlocation object with respect
	 * to the site code
	 * 
	 * @param nmasterSiteCode         [int] Site code
	 * @param ninstrumentlocationcode [int] primary key of unit table
	 *                                ninstrumentlocationcode
	 * @return a InstrumentLocation Object
	 * @throws Exception that are from DAO layer
	 */
	private InstrumentLocation getInstrumentLocationByDefaultStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from instrumentlocation s where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and s.nsitecode = " + nmasterSiteCode;
		return (InstrumentLocation) jdbcUtilityFunction.queryForObject(strQuery, InstrumentLocation.class,
				jdbcTemplate);
	}

	/**
	 * This method id used to delete an entry in InstrumentLocation table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param instrumentlocation [InstrumentLocation] an Object holds the record to
	 *                           be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         instrumentLocation object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteInstrumentLocation(InstrumentLocation instrumentlocation, UserInfo userInfo)
			throws Exception {
		final InstrumentLocation instrumentlocationById = getActiveInstrumentLocationById(
				instrumentlocation.getNinstrumentlocationcode(), userInfo);
		if (instrumentlocationById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final InstrumentLocation location = (InstrumentLocation) getInstrumentLocationByDefaultStatus(
					instrumentlocation.getNinstrumentlocationcode());
			final String query = "select 'IDS_INSTRUMENT' as Msg from instrument where ninstrumentlocationcode= "
					+ instrumentlocation.getNinstrumentlocationcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(instrumentlocation.getNinstrumentlocationcode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedInstrumentLocationList = new ArrayList<>();
				final String updateQueryString = "update instrumentlocation set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where ninstrumentlocationcode=" + instrumentlocation.getNinstrumentlocationcode();
				jdbcTemplate.execute(updateQueryString);
				instrumentlocation.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedInstrumentLocationList.add(instrumentlocation);
				multilingualIDList.add("IDS_DELETEINSTRUMENTLOCATION");
				auditUtilityFunction.fnInsertAuditAction(deletedInstrumentLocationList, 1, null, multilingualIDList,
						userInfo);
				return getInstrumentLocation(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
