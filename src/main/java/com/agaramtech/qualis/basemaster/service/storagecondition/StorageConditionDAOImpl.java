package com.agaramtech.qualis.basemaster.service.storagecondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.StorageCondition;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.submitter.service.city.CityDAOImpl;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "storagecondition" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class StorageConditionDAOImpl implements StorageConditionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(StorageConditionDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available storageConditions for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         storageConditions
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception {
		LOGGER.info("getStorageCondition");
		final String strQuery = "select s.nstorageconditioncode, s.sstorageconditionname, s.sdescription, s.ndefaultstatus,"
				+ " s.nsitecode, s.nstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
				+ "from storagecondition s,transactionstatus ts where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstorageconditioncode<>"
				+ Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " and ts.ntranscode=s.ndefaultstatus and s.nsitecode = " + userInfo.getNmastersitecode() + ";";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new StorageCondition()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active storageCondition object based on the
	 * specified nstorageconditionCode.
	 * 
	 * @param nstorageconditionCode [int] primary key of storageCondition object
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         storageCondition object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public StorageCondition getActiveStorageConditionById(final int nstorageconditioncode) throws Exception {
		final String strQuery = "select s.nstorageconditioncode, s.sstorageconditionname, s.sdescription, s.ndefaultstatus,"
				+ " s.nsitecode, s.nstatus from storagecondition s where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstorageconditioncode = "
				+ nstorageconditioncode + ";";
		return (StorageCondition) jdbcUtilityFunction.queryForObject(strQuery, StorageCondition.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to storagecondition table.
	 * storageCondition Name is unique across the database. Need to check for
	 * duplicate entry of storageCondition name for the specified site before saving
	 * into database. * Need to check that there should be only one default
	 * storageCondition for a site.
	 * 
	 * @param objStorageCondition [StorageCondition] object holding details to be
	 *                            added in storagecondition table
	 * @param userInfo            [UserInfo] holding logged in user details based on
	 *                            which the list is to be fetched
	 * @return saved storagecondition object with status code 200 if saved
	 *         successfully else if the storagecondition already exists, response
	 *         will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createStorageCondition(final StorageCondition basemasterStorageCondition, final UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table storagecondition "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedStorageConditionList = new ArrayList<>();

		final StorageCondition storageConditionByName = getStorageConditionByName(
				basemasterStorageCondition.getSstorageconditionname(), basemasterStorageCondition.getNsitecode());

		if (storageConditionByName == null) {
			if (basemasterStorageCondition.getNdefaultstatus() == Enumeration.TransactionStatus.YES
					.gettransactionstatus()) {
				final StorageCondition defaultStorageCondition = getStorageConditionByDefaultStatus(
						basemasterStorageCondition.getNsitecode());
				if (defaultStorageCondition != null) {
					final StorageCondition storageconditionBeforeSave = SerializationUtils
							.clone(defaultStorageCondition);
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(storageconditionBeforeSave);
					defaultStorageCondition
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					final String updateQueryString = " update storagecondition set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " , dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstorageconditioncode ="
							+ defaultStorageCondition.getNstorageconditioncode() + " and nsitecode="
							+ userInfo.getNmastersitecode() + ";";
					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultStorageCondition);
					multilingualIDList.add("IDS_EDITSTORAGECONDITION");
					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
					multilingualIDList.clear();
				}
			}
			final String sequencequery = "select nsequenceno from SeqNoBasemaster where stablename ='storagecondition' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;

			final String insertquery = "Insert into storagecondition (nstorageconditioncode,sstorageconditionname,sdescription,"
					+ "ndefaultstatus,dmodifieddate,nsitecode,nstatus)" + "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(basemasterStorageCondition.getSstorageconditionname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(basemasterStorageCondition.getSdescription()) + "',"
					+ basemasterStorageCondition.getNdefaultstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			final String updatequery = "update SeqNoBasemaster set nsequenceno =" + nsequenceno
					+ " where stablename ='storagecondition';";
			jdbcTemplate.execute(updatequery);
			basemasterStorageCondition.setNstorageconditioncode(nsequenceno);
			savedStorageConditionList.add(basemasterStorageCondition);
			multilingualIDList.add("IDS_ADDSTORAGECONDITION");
			auditUtilityFunction.fnInsertAuditAction(savedStorageConditionList, 1, null, multilingualIDList, userInfo);
			return getStorageCondition(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the storagecondition object for the specified
	 * storageCondition name and site.
	 * 
	 * @param sstorageconditionname [String] name of the storageCondition
	 * @param nmasterSiteCode       [int] site code of the storageCondition
	 * @return storageCondition object based on the specified storageCondition name
	 *         and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private StorageCondition getStorageConditionByName(final String storagecondition, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select nstorageconditioncode from storagecondition where sstorageconditionname = N'"
				+ stringUtilityFunction.replaceQuote(storagecondition) + "'" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (StorageCondition) jdbcUtilityFunction.queryForObject(strQuery, StorageCondition.class, jdbcTemplate);

	}

	/**
	 * This method is used to get the default storageCondition object with respect
	 * to the site.
	 * 
	 * @param nmasterSiteCode [int] Site code
	 * @return StorageCondition Object
	 * @throws Exception that are thrown from this DAO layer
	 */
	private StorageCondition getStorageConditionByDefaultStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = " select s.nstorageconditioncode, s.sstorageconditionname, s.sdescription, s.ndefaultstatus,"
				+ " s.nsitecode, s.nstatus from storagecondition s " + " where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and s.nsitecode = " + nmasterSiteCode;
		return (StorageCondition) jdbcUtilityFunction.queryForObject(strQuery, StorageCondition.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in storagecondition table. Need to
	 * validate that the storageCondition object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * storageCondition name for the specified site before saving into database.
	 * Need to check that there should be only one default storageCondition for a
	 * site
	 * 
	 * @param objStorageCondition [StorageCondition] object holding details to be
	 *                            updated in storagecondition table
	 * @param userInfo            [UserInfo] holding logged in user details based on
	 *                            which the list is to be fetched
	 * @return saved storageCondition object with status code 200 if saved
	 *         successfully else if the storageCondition already exists, response
	 *         will be returned as 'Already Exists' with status code 409 else if the
	 *         storageCondition to be updated is not available, response will be
	 *         returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateStorageCondition(final StorageCondition basemasterStorageCondition, final UserInfo userInfo)
			throws Exception {
		final StorageCondition storagecondition = getActiveStorageConditionById(
				basemasterStorageCondition.getNstorageconditioncode());
		if (storagecondition == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nstorageconditioncode from storagecondition where sstorageconditionname = N'"
					+ stringUtilityFunction.replaceQuote(basemasterStorageCondition.getSstorageconditionname())
					+ "' and nstorageconditioncode <> " + basemasterStorageCondition.getNstorageconditioncode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + userInfo.getNmastersitecode() + ";";
			final List<StorageCondition> storageconditionList = jdbcTemplate.query(queryString, new StorageCondition());
			if (storageconditionList.isEmpty()) {
				final List<Object> listBeforeUpdate = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				if (basemasterStorageCondition.getNdefaultstatus() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {
					final StorageCondition defaultStorageCondition = getStorageConditionByDefaultStatus(
							basemasterStorageCondition.getNsitecode());
					if (defaultStorageCondition != null && defaultStorageCondition
							.getNstorageconditioncode() != basemasterStorageCondition.getNstorageconditioncode()) {
						final StorageCondition storageconditionBeforeSave = SerializationUtils
								.clone(defaultStorageCondition);
						final List<Object> listBeforeUpdatedefault = new ArrayList<>();
						final List<Object> listAfterUpdatedefault = new ArrayList<>();
						listBeforeUpdatedefault.add(storageconditionBeforeSave);
						defaultStorageCondition
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						final String updateQueryString = "update storagecondition set ndefaultstatus="
								+ defaultStorageCondition.getNdefaultstatus() + ", dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstorageconditioncode="
								+ defaultStorageCondition.getNstorageconditioncode() + " and nsitecode="
								+ userInfo.getNmastersitecode() + ";";

						jdbcTemplate.execute(updateQueryString);
						listAfterUpdatedefault.add(defaultStorageCondition);
						auditUtilityFunction.fnInsertAuditAction(listAfterUpdatedefault, 2, listBeforeUpdatedefault,
								Arrays.asList("IDS_EDITSTORAGECONDITION"), userInfo);
					}
				}
				final String updateQueryString = "update storagecondition set sstorageconditionname=N'"
						+ stringUtilityFunction.replaceQuote(basemasterStorageCondition.getSstorageconditionname())
						+ "', sdescription =N'"
						+ stringUtilityFunction.replaceQuote(basemasterStorageCondition.getSdescription())
						+ "', ndefaultstatus=" + basemasterStorageCondition.getNdefaultstatus() + ", dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstorageconditioncode="
						+ basemasterStorageCondition.getNstorageconditioncode() + ";";

				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(basemasterStorageCondition);
				listBeforeUpdate.add(storagecondition);
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
						Arrays.asList("IDS_EDITSTORAGECONDITION"), userInfo);
				return getStorageCondition(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in storagecondition table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as 'retrievalcertificate'
	 * 
	 * @param objStorageCondition [StorageCondition] an Object holds the record to
	 *                            be deleted
	 * @param userInfo            [UserInfo] holding logged in user details based on
	 *                            which the list is to be fetched
	 * @return a response entity with list of available storageCondition objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteStorageCondition(final StorageCondition basemasterStorageCondition, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedStorageConditionList = new ArrayList<>();
		final StorageCondition storagecondition = (StorageCondition) getActiveStorageConditionById(
				basemasterStorageCondition.getNstorageconditioncode());
		if (storagecondition == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_RETRIEVALCERTIFICATE' as Msg from retrievalcertificate where nstorageconditioncode= "
					+ basemasterStorageCondition.getNstorageconditioncode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
//					+ " union all "
//					+ " SELECT 'IDS_REGISTRATION' as Msg FROM registration r "
//					+ " JOIN jsonb_each(r.jsondata) d ON true where "
//					+ " d.value->>'pkey' ='nunitcode' and d.value->>'nquerybuildertablecode'='253' "
//					+ " and d.value->>'value'='" + objUnit.getNunitcode()  + "'"
//					+ " union all "
//					+ " SELECT 'IDS_REGISTRATIONSAMPLE' as Msg FROM registrationsample rs "
//					+ " JOIN jsonb_each(rs.jsondata) d1 ON true where "
//					+ " d1.value->>'pkey' ='nunitcode' and d1.value->>'nquerybuildertablecode'='253' "
//					+ " and d1.value->>'value'='" + objUnit.getNunitcode()  + "'";

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(basemasterStorageCondition.getNstorageconditioncode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final String updateQueryString = "update storagecondition set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nstorageconditioncode="
						+ basemasterStorageCondition.getNstorageconditioncode() + ";";
				jdbcTemplate.execute(updateQueryString);
				basemasterStorageCondition
						.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedStorageConditionList.add(basemasterStorageCondition);
				multilingualIDList.add("IDS_DELETESTORAGECONDITION");
				auditUtilityFunction.fnInsertAuditAction(deletedStorageConditionList, 1, null, multilingualIDList,
						userInfo);
				return getStorageCondition(userInfo);

			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
