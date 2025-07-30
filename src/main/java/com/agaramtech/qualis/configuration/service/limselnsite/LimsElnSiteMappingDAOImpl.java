package com.agaramtech.qualis.configuration.service.limselnsite;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.LimsElnSiteMapping;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "limsElnSiteMapping" table by
 * implementing methods from its interface.
 */

@AllArgsConstructor
@Repository
public class LimsElnSiteMappingDAOImpl implements LimsElnSiteMappingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(LimsElnSiteMappingDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available limsElnSiteMappings for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         limsElnSiteMappings
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getlimselnsitemapping(final UserInfo userInfo) throws Exception {
		final String query = " select lem.nelnsitemappingcode,lem.nlimssitecode,us.ssitename,lem.nelnsitecode as sitecode,lem.selnsitename as sitename "
				+ "	from limselnsitemapping lem,site us " + "	where us.nsitecode=lem.nlimssitecode and lem.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lem.nsitecode = "
				+ userInfo.getNmastersitecode();
		LOGGER.info("getlimselnsitemapping-->" + query);
		return new ResponseEntity<>((List<LimsElnSiteMapping>) jdbcTemplate.query(query, new LimsElnSiteMapping()),
				HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to limsElnSiteMapping table.
	 * LimsElnSiteMapping Name is unique across the database. Need to check for
	 * duplicate entry of limsElnSiteMapping name for the specified site before
	 * saving into database. * Need to check that there should be only one default
	 * limsElnSiteMapping for a site.
	 * 
	 * @param objLimsElnSiteMapping [LimsElnSiteMapping] object holding details to
	 *                              be added in limsElnSiteMapping table
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return saved limsElnSiteMapping object with status code 200 if saved
	 *         successfully else if the limsElnSiteMapping already exists, response
	 *         will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createLimsElnSitemapping(final LimsElnSiteMapping limselnsite,
			final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedReasonList = new ArrayList<>();
		final String countQuery = "select count(nelnsitemappingcode) from limselnsitemapping where limselnsitemapping.nlimssitecode="
				+ limselnsite.getNlimssitecode() + " and limselnsitemapping.nelnsitecode=" + limselnsite.getSitecode()
				+ " and limselnsitemapping.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode() + "";
		final int count = jdbcTemplate.queryForObject(countQuery, Integer.class);
		if (count == 0) {
			final String sequencequery = "select nsequenceno from seqnocredentialmanagement where stablename ='limselnsitemapping'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;
			final String insertquery = "Insert  into limselnsitemapping (nelnsitemappingcode,nlimssitecode,nelnsitecode,selnsitename,dmodifieddate,nsitecode,nstatus) "
					+ "values(" + nsequenceno + "," + limselnsite.getNlimssitecode() + "," + limselnsite.getSitecode()
					+ ",N'" + stringUtilityFunction.replaceQuote(limselnsite.getSitename()) + "'" + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ " " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			final String updatequery = "update seqnocredentialmanagement set nsequenceno=" + nsequenceno
					+ " where stablename ='limselnsitemapping'";
			jdbcTemplate.execute(updatequery);
			savedReasonList.add(limselnsite);
			multilingualIDList.add("IDS_ADDLIMSELNSITE");
			auditUtilityFunction.fnInsertAuditAction(savedReasonList, 1, null, multilingualIDList, userInfo);
			return getlimselnsitemapping(userInfo);
		}
		return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_ALREADYMAPPED", userInfo.getSlanguagefilename()),
				HttpStatus.CONFLICT);
	}

	/**
	 * This method is used to retrieve list of all available limsUsers for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         limsUsers
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getLimsSite(final UserInfo userInfo) throws Exception {
		final String query = "select site.nsitecode as nlimssitecode,site.ssitename from site where site.nsitecode not in(select limselnsitemapping.nlimssitecode from limselnsitemapping where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) and site.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and site.nsitecode>0"
				+ " and nsitecode = " + userInfo.getNmastersitecode();
		return new ResponseEntity<>((List<LimsElnSiteMapping>) jdbcTemplate.query(query, new LimsElnSiteMapping()),
				HttpStatus.OK);
	}

	/**
	 * This method id used to delete an entry in LimsElnSiteMapping table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as 'limselnsitemapping'
	 * 
	 * @param objLimsElnSiteMapping [LimsElnSiteMapping] an Object holds the record
	 *                              to be deleted
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return a response entity with list of available limsElnSiteMapping objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteLimsElnSitemapping(final LimsElnSiteMapping limselnuser,
			final UserInfo userInfo) throws Exception {
		final LimsElnSiteMapping reasonbyID = getActiveLimsElnSitemappingById(limselnuser.getNelnsitemappingcode());
		if (reasonbyID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedReasonList = new ArrayList<>();
			final String updateQueryString = "update limselnsitemapping set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nelnsitemappingcode="
					+ limselnuser.getNelnsitemappingcode();
			jdbcTemplate.execute(updateQueryString);
			limselnuser.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			savedReasonList.add(limselnuser);
			multilingualIDList.add("IDS_DELETELIMSELNSITE");
			auditUtilityFunction.fnInsertAuditAction(savedReasonList, 1, null, multilingualIDList, userInfo);
			return getlimselnsitemapping(userInfo);
		}
	}

	/**
	 * This method is used to retrieve active limsElnSiteMapping object based on the
	 * specified nlimsElnSiteMappingCode.
	 * 
	 * @param nlimsElnSiteMappingCode [int] primary key of limsElnSiteMapping object
	 * @param userInfo                [UserInfo] holding logged in user details
	 *                                based on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         limsElnSiteMapping object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public LimsElnSiteMapping getActiveLimsElnSitemappingById(final int nelnsitemappingcode)
			throws Exception {
		final String strQuery = " select lem.nelnsitemappingcode,lem.nlimssitecode,us.ssitename,lem.nelnsitecode as sitecode,lem.selnsitename as sitename "
				+ " from limselnsitemapping lem,site us  " + " where us.nsitecode=lem.nlimssitecode and lem.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lem.nelnsitemappingcode="
				+ nelnsitemappingcode;
		return (LimsElnSiteMapping) jdbcUtilityFunction.queryForObject(strQuery, LimsElnSiteMapping.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to update entry in limsElnSiteMapping table. Need to
	 * validate that the limsElnSiteMapping object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * limsElnSiteMapping name for the specified site before saving into database.
	 * Need to check that there should be only one default limsElnSiteMapping for a
	 * site
	 * 
	 * @param objLimsElnSiteMapping [LimsElnSiteMapping] object holding details to
	 *                              be updated in limsElnSiteMapping table
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return saved limsElnSiteMapping object with status code 200 if saved
	 *         successfully else if the limsElnSiteMapping already exists, response
	 *         will be returned as 'Already Exists' with status code 409 else if the
	 *         limsElnSiteMapping to be updated is not available, response will be
	 *         returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateLimsElnSitemapping(final LimsElnSiteMapping limselnuser,
			final UserInfo userInfo) throws Exception {
		final LimsElnSiteMapping objReason = getActiveLimsElnSitemappingById(limselnuser.getNelnsitemappingcode());
		if (objReason == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			final String updateQueryString = "update limselnsitemapping set nlimssitecode="
					+ limselnuser.getNlimssitecode() + ",nelnsitecode=" + limselnuser.getSitecode() + ",selnsitename='"
					+ limselnuser.getSitename() + "',dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'where nelnsitemappingcode="
					+ limselnuser.getNelnsitemappingcode();
			jdbcTemplate.execute(updateQueryString);
			final List<String> multilingualIDList = new ArrayList<>();
			multilingualIDList.add("IDS_EDITLIMSELNSITE");
			final List<Object> listAfterSave = new ArrayList<>();
			listAfterSave.add(limselnuser);
			final List<Object> listBeforeSave = new ArrayList<>();
			listBeforeSave.add(objReason);
			auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
			return getlimselnsitemapping(userInfo);
		}
	}
}
