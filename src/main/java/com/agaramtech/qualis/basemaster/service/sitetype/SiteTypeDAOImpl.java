package com.agaramtech.qualis.basemaster.service.sitetype;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.SiteType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "sitetype" table by 
 * implementing methods from its interface. 
 */
/**
 * @author sujatha.v BGSI-5 02/07/2025
 */
@RequiredArgsConstructor
@Repository
public class SiteTypeDAOImpl implements SiteTypeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SiteTypeDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;
	private final CommonFunction commonFunction;
	private ValidatorDel validatorDel;

	/**
	 * This method is used to retrieve list of all available sitetype's for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         sitetype's
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSiteType(UserInfo userInfo) throws Exception {

		final String strQuery = " select s.nsitetypecode, s.ssitetypename, s.nhierarchicalorderno, s.sdescription, s.dmodifieddate, s.nsitecode, s.nstatus"
				+ " from sitetype s where s.nsitetypecode> 0 and s.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode= "
				+ userInfo.getNmastersitecode();
		LOGGER.info("getSiteType() called " + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SiteType()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active sitetype object based on the specified
	 * nsitetypecode.
	 * 
	 * @param nsitetypecode [int] primary key of sitetype object
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return response entity object holding response status and data of sitetype
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public SiteType getActiveSiteTypeById(int nsitetypecode, UserInfo userInfo) throws Exception {

		final String strQuery = " select s.nsitetypecode, s.ssitetypename, s.nhierarchicalorderno, s.sdescription, s.dmodifieddate, s.nsitecode, s.nstatus"
				+ " from sitetype s where s.nsitetypecode=" + nsitetypecode + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (SiteType) jdbcUtilityFunction.queryForObject(strQuery, SiteType.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to sitetype table. Site Type Name and
	 * Hierarchical Order No is unique across the database. Need to check for
	 * duplicate entry of sitetype name and hierarchicalorderno for the specified
	 * site before saving into database.
	 * 
	 * @param objSiteType [SiteType] object holding details to be added in sitetype
	 *                    table
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return saved sitetype object with status code 200 if saved successfully else
	 *         if the sitetype already exists, response will be returned as 'Already
	 *         Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSiteType(SiteType objSiteType, UserInfo userInfo) throws Exception {
		final SiteType siteTypeByName = getSiteTypeByName(objSiteType.getSsitetypename(),
				userInfo.getNmastersitecode());
		String alert = "";

		if (siteTypeByName == null) {
			final SiteType siteTypeByHierarchicalOrderNo = getSiteTypeByHierarchicalOrderNo(
					objSiteType.getNhierarchicalorderno(), userInfo.getNmastersitecode());
			if (siteTypeByHierarchicalOrderNo == null) {
				final String sQuery = " lock table sitetype "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(sQuery);

				final List<String> multilingualIdList = new ArrayList<>();
				final List<Object> saveSiteTypeList = new ArrayList<>();

				final String sequenceNoQuery = " select nsequenceno from seqnobasemaster where stablename ='sitetype'";
				int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
				nsequenceNo++;
				final String insertQuery = " insert into sitetype(nsitetypecode, ssitetypename, nhierarchicalorderno, sdescription, dmodifieddate, nsitecode, nstatus )"
						+ " values(" + nsequenceNo + ",N'"
						+ stringUtilityFunction.replaceQuote(objSiteType.getSsitetypename()) + "', "
						+ objSiteType.getNhierarchicalorderno() + ",N'"
						+ stringUtilityFunction.replaceQuote(objSiteType.getSdescription()) + "' ,'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
						+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )";
				jdbcTemplate.execute(insertQuery);

				final String updateQuery = " update seqnobasemaster set nsequenceno= " + nsequenceNo
						+ " where stablename='sitetype'";
				jdbcTemplate.execute(updateQuery);

				objSiteType.setNsitetypecode(nsequenceNo);
				saveSiteTypeList.add(objSiteType);

				multilingualIdList.add("IDS_ADDSITETYPE");

				auditUtilityFunction.fnInsertAuditAction(saveSiteTypeList, 1, null, multilingualIdList, userInfo);
				return getSiteType(userInfo);
			} else {
				alert = commonFunction.getMultilingualMessage("IDS_HIERARCHICALORDERNO",
						userInfo.getSlanguagefilename());
				return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			alert = commonFunction.getMultilingualMessage("IDS_SITETYPE", userInfo.getSlanguagefilename());

			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to fetch the sitetype object for the specified sitetype
	 * name and site.
	 * 
	 * @param ssitetypename   [String] name of the sitetype
	 * @param nmasterSiteCode [int] site code of the sitetype
	 * @return sitetype object based on the specified sitetype name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private SiteType getSiteTypeByName(final String ssitetypename, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select s.nsitetypecode from sitetype s where s.ssitetypename= N'"
				+ stringUtilityFunction.replaceQuote(ssitetypename) + "' and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode= " + nmasterSiteCode;
		return (SiteType) jdbcUtilityFunction.queryForObject(strQuery, SiteType.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the sitetype object for the specified
	 * nhierarchicalorderno and site.
	 * 
	 * @param nhierarchicalorderno [Short] order no of the sitetype
	 * @param nmasterSiteCode      [int] site code of the sitetype
	 * @return sitetype object based on the specified nhierarchicalorderno and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private SiteType getSiteTypeByHierarchicalOrderNo(final Short nhierarchicalorderno, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select s.nsitetypecode from sitetype s where s.nhierarchicalorderno= "
				+ nhierarchicalorderno + " and s.nsitetypecode> 0 and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode= " + nmasterSiteCode;
		return (SiteType) jdbcUtilityFunction.queryForObject(strQuery, SiteType.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in sitetype table. Need to validate that
	 * the sitetype object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of sitetype name and hierarchical
	 * order no for the specified site before saving into database.
	 * 
	 * @param objSiteType [SiteType] object holding details to be updated in
	 *                    sitetype table
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return saved sitetype object with status code 200 if saved successfully else
	 *         if the sitetype already exists, response will be returned as 'Already
	 *         Exists' with status code 409 else if the unit to be updated is not
	 *         available, response will be returned as 'Already Deleted' with status
	 *         code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSiteType(SiteType objSiteType, UserInfo userInfo) throws Exception {
		String alert = "";
		final SiteType siteType = getActiveSiteTypeById(objSiteType.getNsitetypecode(), userInfo);
		if (siteType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();

			final String queryString = " select s.nsitetypecode from sitetype s where s.ssitetypename=N'"
					+ stringUtilityFunction.replaceQuote(objSiteType.getSsitetypename()) + "' and s.nsitetypecode<> "
					+ objSiteType.getNsitetypecode() + " and nsitecode= " + objSiteType.getNsitecode()
					+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final SiteType checkForSameName = (SiteType) jdbcUtilityFunction.queryForObject(queryString, SiteType.class,
					jdbcTemplate);
			if (checkForSameName == null) {

				final String query = " select s.nsitetypecode from sitetype s where s.nhierarchicalorderno="
						+ objSiteType.getNhierarchicalorderno() + " and s.nsitetypecode<> "
						+ objSiteType.getNsitetypecode() + " and nsitecode= " + objSiteType.getNsitecode()
						+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final SiteType checkForSameHierachicalNo = (SiteType) jdbcUtilityFunction.queryForObject(query,
						SiteType.class, jdbcTemplate);
				if (checkForSameHierachicalNo == null) {

					final String updateQuery = " update sitetype set ssitetypename=N'"
							+ stringUtilityFunction.replaceQuote(objSiteType.getSsitetypename())
							+ "', nhierarchicalorderno=" + objSiteType.getNhierarchicalorderno() + ", sdescription=N'"
							+ stringUtilityFunction.replaceQuote(objSiteType.getSdescription()) + "', dmodifieddate= '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nsitetypecode= "
							+ objSiteType.getNsitetypecode();

					jdbcTemplate.execute(updateQuery);
					listAfterUpdate.add(objSiteType);
					listBeforeUpdate.add(siteType);

					multilingualIDList.add("IDS_EDITSITETYPE");
					auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
							userInfo);

					return getSiteType(userInfo);
				} else {
					alert = commonFunction.getMultilingualMessage("IDS_HIERARCHICALORDERNO",
							userInfo.getSlanguagefilename());
					return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				alert = commonFunction.getMultilingualMessage("IDS_SITETYPE", userInfo.getSlanguagefilename());
				return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method id used to delete an entry in sitetype table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as 'siteconfig'
	 * 
	 * @param objSiteType [SiteType] an Object holds the record to be deleted
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return a response entity with list of available sitetype objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteSiteType(SiteType objSiteType, UserInfo userInfo) throws Exception {

		final SiteType siteType = getActiveSiteTypeById(objSiteType.getNsitetypecode(), userInfo);
		if (siteType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> deletedSiteTypeList = new ArrayList<>();

			final String query = " Select 'IDS_SITECONFIG' as Msg from siteconfig where nsitetypecode= "
					+ objSiteType.getNsitetypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objSiteType.getNsitetypecode()),
						userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQuery = " update sitetype set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' , nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsitetypecode="
						+ objSiteType.getNsitetypecode();

				jdbcTemplate.execute(updateQuery);
				siteType.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedSiteTypeList.add(objSiteType);
				multilingualIDList.add("IDS_DELETESITETYPE");

				auditUtilityFunction.fnInsertAuditAction(deletedSiteTypeList, 1, null, multilingualIDList, userInfo);
				return getSiteType(userInfo);

			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
