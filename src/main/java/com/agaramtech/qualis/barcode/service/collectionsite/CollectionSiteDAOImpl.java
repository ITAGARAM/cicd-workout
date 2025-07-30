package com.agaramtech.qualis.barcode.service.collectionsite;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.barcode.model.CollectionSite;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;
import com.agaramtech.qualis.project.service.projecttype.*;

/**
 * This class is used to perform CRUD Operation on "collectionsite" table by
 * implementing methods from its interface.
 * 
 */
@RequiredArgsConstructor
@Repository
public class CollectionSiteDAOImpl implements CollectionSiteDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionSiteDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectTypeDAO projectTypeDao;

	/**
	 * This method is used to retrieve list of all active Collection Site for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Collection Site
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCollectionSite(UserInfo userInfo) throws Exception {
		final String strQuery = "select cs.ncollectionsitecode, cs.ssitename, cs.scode, cs.nsitecode, cs.nstatus, cs.nprojecttypecode, p.sprojecttypename"
				+ " from collectionsite cs, projecttype p  where cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cs.ncollectionsitecode > 0 and cs.nsitecode = " + userInfo.getNmastersitecode()
				+ " and cs.nprojecttypecode = p.nprojecttypecode";
		LOGGER.info("getSamplePunchSite:" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new CollectionSite()), HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active collectionsite objects for the
	 * specified ssitename name and code.
	 * 
	 * @param ssitename       [String] and code it to be checked tha duplicate in
	 *                        the collectionsite table.
	 * @param nmasterSiteCode [int] site code of the unit
	 * @return list of active collectionsite code(s) based on the specified
	 *         ssitename name and code
	 * @throws Exception
	 */
	private CollectionSite getExistingRecord(int ncollectionsitecode, CollectionSite collectionSite, UserInfo userInfo)
			throws Exception {
		String sitecode = "";
		if (ncollectionsitecode != 0) {
			sitecode = "and ncollectionsitecode <> " + collectionSite.getNcollectionsitecode() + "";
		}
		final String strQuery = "SELECT " + "CASE WHEN EXISTS ( "
				+ "SELECT ssitename FROM collectionsite WHERE ssitename = '"
				+ stringUtilityFunction.replaceQuote(collectionSite.getSsitename()) + "' " + "AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND nprojecttypecode = "
				+ collectionSite.getNprojecttypecode() + " " + sitecode
				+ ") THEN TRUE ELSE FALSE END AS iscollectionsite, " + "CASE WHEN EXISTS ( "
				+ "SELECT scode FROM collectionsite WHERE scode = '"
				+ stringUtilityFunction.replaceQuote(collectionSite.getScode()) + "' " + "AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND nprojecttypecode = "
				+ collectionSite.getNprojecttypecode() + " " + sitecode + ") THEN TRUE ELSE FALSE END AS iscode";
		return (CollectionSite) jdbcUtilityFunction.queryForObject(strQuery, CollectionSite.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to Collection Site table. Need to
	 * check for duplicate entry of Collection Site name based on ProjectType for
	 * the specified site before saving into database.
	 * 
	 * @param collection Site [Collection Site] object holding details to be added
	 *                   in Collection Site table
	 * @return inserted Collection Site object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createCollectionSite(CollectionSite collectionSite, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedcollectionSiteList = new ArrayList<>();
		final ResponseEntity<Object> activeProjectType = projectTypeDao
				.getActiveProjectTypeById(collectionSite.getNprojecttypecode(), userInfo);
		if (activeProjectType.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			final String alert = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final CollectionSite existingCollectionSite = getExistingRecord(collectionSite.getNcollectionsitecode(),
				collectionSite, userInfo);
		if (existingCollectionSite != null && existingCollectionSite.getIscollectionsite() == false
				&& existingCollectionSite.getIscode() == false) {
			if (!collectionSite.getScode().isEmpty()) {
				String sequencenoquery = "select nsequenceno from seqnobarcode where stablename = 'collectionsite' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
				nsequenceno++;
				String insertquery = "Insert into collectionsite (ncollectionsitecode,ssitename,scode,nprojecttypecode,dmodifieddate,nsitecode,nstatus)"
						+ "values(" + nsequenceno + ",N'"
						+ stringUtilityFunction.replaceQuote(collectionSite.getSsitename()) + "',N'"
						+ stringUtilityFunction.replaceQuote(collectionSite.getScode()) + "',"
						+ collectionSite.getNprojecttypecode() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " " + userInfo.getNmastersitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);
				String updatequery = "update seqnobarcode set nsequenceno =" + nsequenceno
						+ " where stablename='collectionsite' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(updatequery);
				collectionSite.setNcollectionsitecode(nsequenceno);
				savedcollectionSiteList.add(collectionSite);
				multilingualIDList.add("IDS_ADDCOLLECTIONSITE");
				auditUtilityFunction.fnInsertAuditAction(savedcollectionSiteList, 1, null, multilingualIDList,
						userInfo);
				return getCollectionSite(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			String alert = "";
			final boolean siteName = existingCollectionSite.getIscollectionsite();
			final boolean siteCode = existingCollectionSite.getIscode();
			if (siteName == true && siteCode == true) {
				alert = commonFunction.getMultilingualMessage("IDS_SITENAME", userInfo.getSlanguagefilename()) + " and "
						+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
			} else if (siteName == true) {
				alert = commonFunction.getMultilingualMessage("IDS_SITENAME", userInfo.getSlanguagefilename());
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
	 * This method is used to retrieve active Collection Site object based on the
	 * specified ncollectionsitecode.
	 * 
	 * @param ncollectionsitecode [int] primary key of Collection Site object
	 * @return response entity object holding response status and data of Collection
	 *         Site object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public CollectionSite getActiveCollectionSiteById(int ncollectionsitecode, UserInfo userInfo) throws Exception {
		final String strQuery = "select cs.ncollectionsitecode, cs.ssitename, cs.scode, cs.nsitecode, cs.nstatus, cs.nprojecttypecode, p.sprojecttypename from collectionsite cs, projecttype p"
				+ " where cs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ncollectionsitecode = " + ncollectionsitecode + " and cs.nprojecttypecode = p.nprojecttypecode";
		return (CollectionSite) jdbcUtilityFunction.queryForObject(strQuery, CollectionSite.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in Collection Site table. Need to
	 * validate that the Collection Site object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of Collection
	 * Site name for the specified site before saving into database.
	 * 
	 * @param collection Site [Collection Site] object holding details to be updated
	 *                   in Collection Site table
	 * @return response entity object holding response status and data of updated
	 *         Collection Site object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateCollectionSite(CollectionSite collectionSite, UserInfo userInfo)
			throws Exception {
		final CollectionSite objCollectionSite = getActiveCollectionSiteById(collectionSite.getNcollectionsitecode(),
				userInfo);
		final ResponseEntity<Object> activeProjectType = projectTypeDao
				.getActiveProjectTypeById(collectionSite.getNprojecttypecode(), userInfo);
		if (activeProjectType.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			final String alert = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		if (objCollectionSite == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final CollectionSite existingCollectionSite = getExistingRecord(collectionSite.getNcollectionsitecode(),
					collectionSite, userInfo);
			if (existingCollectionSite!=null && existingCollectionSite.getIscollectionsite() == false && existingCollectionSite.getIscode() == false) {
				if (!collectionSite.getScode().isEmpty()) {
					final String updateQueryString = "update collectionsite set ssitename='"
							+ stringUtilityFunction.replaceQuote(collectionSite.getSsitename()) + "', " + "scode ='"
							+ stringUtilityFunction.replaceQuote(collectionSite.getScode()) + "',dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nprojecttypecode="
							+ collectionSite.getNprojecttypecode() + " where ncollectionsitecode ="
							+ collectionSite.getNcollectionsitecode();
					jdbcTemplate.execute(updateQueryString);
					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add("IDS_EDITCOLLECTIONSITE");
					final List<Object> listAfterSave = new ArrayList<>();
					listAfterSave.add(collectionSite);
					final List<Object> listBeforeSave = new ArrayList<>();
					listBeforeSave.add(objCollectionSite);
					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);
					return getCollectionSite(userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			} else {
				String alert = "";
				final boolean siteName = existingCollectionSite.getIscollectionsite();
				final boolean siteCode = existingCollectionSite.getIscode();
				if (siteName == true && siteCode == true) {
					alert = commonFunction.getMultilingualMessage("IDS_SITENAME", userInfo.getSlanguagefilename())
							+ " and "
							+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				} else if (siteName == true) {
					alert = commonFunction.getMultilingualMessage("IDS_SITENAME", userInfo.getSlanguagefilename());
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
	 * This method id used to delete an entry in Collection Site table Need to check
	 * the record is already deleted or not
	 * 
	 * @param collection Site [Collection Site] an Object holds the record to be
	 *                   deleted
	 * @return a response entity with corresponding HTTP status and an Collection
	 *         Site object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteCollectionSite(CollectionSite collectionSite, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedcollectionSiteList = new ArrayList<>();
		final CollectionSite objcollectionsite = getActiveCollectionSiteById(collectionSite.getNcollectionsitecode(),
				userInfo);
		if (objcollectionsite == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String updateQueryString = "update collectionsite set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where ncollectionsitecode="
					+ collectionSite.getNcollectionsitecode();
			jdbcTemplate.execute(updateQueryString);
			collectionSite.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			savedcollectionSiteList.add(collectionSite);
			multilingualIDList.add("IDS_DELETECOLLECTIONSITE");
			auditUtilityFunction.fnInsertAuditAction(savedcollectionSiteList, 1, null, multilingualIDList, userInfo);
			return getCollectionSite(userInfo);
		}
	}
}