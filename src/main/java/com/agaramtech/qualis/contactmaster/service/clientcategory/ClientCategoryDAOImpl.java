package com.agaramtech.qualis.contactmaster.service.clientcategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.contactmaster.model.ClientCategory;
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
 * This class is used to perform CRUD Operation on "clientcategory" table by
 * implementing methods from its interface.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 08- Sep- 2020
 */
@AllArgsConstructor
@Repository
public class ClientCategoryDAOImpl implements ClientCategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CityDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active clientcategory for the
	 * specified site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         clientcategory
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<List<ClientCategory>> getClientCategory(final UserInfo userInfo) throws Exception {
		LOGGER.info("getClientCategory");
		final String strQuery = "select c.nclientcatcode, c.sclientcatname, c.sdescription, c.ndefaultstatus, c.nsitecode, c.nstatus,"
				+ " ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "' as sdisplaystatus from clientcategory c," + "transactionstatus ts where c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nclientcatcode<>"
				+ Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " and ts.ntranscode=c.ndefaultstatus and c.nsitecode = " + userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new ClientCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active clientcategory for the
	 * specified site for the portal.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         clientcategory
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getClientCategoryForPortal(final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("ClientCategory", getClientCategory(userInfo).getBody());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active clientcategory object based on the
	 * specified nclientcatcode.
	 * 
	 * @param nclientcatcode [int] primary key of clientcategory object
	 * @return response entity object holding response status and data of
	 *         clientcategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ClientCategory getActiveClientCategoryById(final int nclientcatcode) throws Exception {
		final String strQuery = "select c.nclientcatcode, c.sclientcatname, c.sdescription, c.ndefaultstatus, c.nsitecode, c.nstatus"
				+ " from clientcategory c where c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nclientcatcode = "
				+ nclientcatcode;
		return (ClientCategory) jdbcUtilityFunction.queryForObject(strQuery, ClientCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to clientcategory table. Need to check
	 * for duplicate entry of sclientcatname for the specified site before saving
	 * into database.
	 * 
	 * @param clientcategory [ClientCategory] object holding details to be added in
	 *                       clientcategory table
	 * @return response entity object holding response status and data of added
	 *         clientcategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<? extends Object> createClientCategory(final ClientCategory clientCategory,
			final UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table clientcategory " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<Object> savedClientCategoryList = new ArrayList<>();
		final ClientCategory clientCategoryByName = getClientCategoryByName(clientCategory.getSclientcatname(),
				clientCategory.getNsitecode());

		if (clientCategoryByName == null) {
			if (clientCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

				final ClientCategory defaultClientCategory = getClientCategoryByDefaultStatus(
						clientCategory.getNsitecode());

				if (defaultClientCategory != null) {

					final ClientCategory clientCategoryBeforeSave = SerializationUtils.clone(defaultClientCategory);

					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(clientCategoryBeforeSave);

					defaultClientCategory
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

					final String updateQueryString = " update clientcategory set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nclientcatcode ="
							+ defaultClientCategory.getNclientcatcode();
					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultClientCategory);

					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							Arrays.asList("IDS_EDITCLIENTCATEGORY"), userInfo);
				}
			}
			final String sequencenoquery = "select nsequenceno from seqnocontactmaster where stablename ='clientcategory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			final String insertquery = "Insert into clientcategory (nclientcatcode, sclientcatname, sdescription, ndefaultstatus, dmodifieddate, nsitecode, nstatus) "
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(clientCategory.getSclientcatname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(clientCategory.getSdescription()) + "', "
					+ clientCategory.getNdefaultstatus() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);

			final String updatequery = "update seqnocontactmaster set nsequenceno =" + nsequenceno
					+ " where stablename='clientcategory'";
			jdbcTemplate.execute(updatequery);
			clientCategory.setNclientcatcode(nsequenceno);
			savedClientCategoryList.add(clientCategory);

			auditUtilityFunction.fnInsertAuditAction(savedClientCategoryList, 1, null,
					Arrays.asList("IDS_ADDCLIENTCATEGORY"), userInfo);
			return getClientCategory(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to get a default unit object with respect to the site
	 * code
	 * 
	 * @param nmasterSiteCode [int] Site code
	 * @param nclientcatcode  [int] primary key of clientcategory table
	 *                        nclientcatcode
	 * @return a clientcategory Object
	 * @throws Exception that are from DAO layer
	 */
	private ClientCategory getClientCategoryByDefaultStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select c.nclientcatcode, c.sclientcatname, c.sdescription, c.ndefaultstatus, c.nsitecode, c.nstatus from clientcategory c"
				+ " where c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and c.nsitecode = " + nmasterSiteCode;
		return (ClientCategory) jdbcUtilityFunction.queryForObject(strQuery, ClientCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active clientcategory objects for the
	 * specified clientcategory name and site.
	 * 
	 * @param sclientcatname  [String] clientcategory for which the records are to
	 *                        be fetched
	 * @param nmasterSiteCode [int] primary key of site object
	 * @return list of active clientcategory based on the specified clientcategory
	 *         and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private ClientCategory getClientCategoryByName(final String sclientcatname, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select nclientcatcode from clientcategory where sclientcatname = N'"
				+ stringUtilityFunction.replaceQuote(sclientcatname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (ClientCategory) jdbcUtilityFunction.queryForObject(strQuery, ClientCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in clientcategory table. Need to validate
	 * that the clientcategory object to be updated is active before updating
	 * details in database. Need to check for duplicate entry of sclientcatname for
	 * the specified site before saving into database.
	 * 
	 * @param clientcategory [ClientCategory] object holding details to be updated
	 *                       in clientcategory table
	 * @return response entity object holding response status and data of updated
	 *         clientcategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<? extends Object> updateClientCategory(final ClientCategory clientCategory,
			final UserInfo userInfo) throws Exception {

		final ClientCategory objClientCategory = getActiveClientCategoryById(clientCategory.getNclientcatcode());

		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		if (objClientCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nclientcatcode from clientcategory where sclientcatname = N'"
					+ stringUtilityFunction.replaceQuote(clientCategory.getSclientcatname())
					+ "' and nclientcatcode <> " + clientCategory.getNclientcatcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			final List<ClientCategory> clientCategoryList = (List<ClientCategory>) jdbcTemplate.query(queryString,
					new ClientCategory());

			if (clientCategoryList.isEmpty()) {
				if (clientCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

					final ClientCategory defaultClientCategory = getClientCategoryByDefaultStatus(
							userInfo.getNmastersitecode());

					if (defaultClientCategory != null
							&& defaultClientCategory.getNclientcatcode() != clientCategory.getNclientcatcode()) {

						final ClientCategory clientCategoryBeforeSave = SerializationUtils.clone(defaultClientCategory);
						listBeforeUpdate.add(clientCategoryBeforeSave);

						defaultClientCategory
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = " update clientcategory set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nclientcatcode="
								+ defaultClientCategory.getNclientcatcode();
						jdbcTemplate.execute(updateQueryString);

						listAfterUpdate.add(defaultClientCategory);
					}

				}

				final String updateQueryString = "update clientcategory set sclientcatname=N'"
						+ stringUtilityFunction.replaceQuote(clientCategory.getSclientcatname()) + "', sdescription =N'"
						+ stringUtilityFunction.replaceQuote(clientCategory.getSdescription()) + "',ndefaultstatus = "
						+ clientCategory.getNdefaultstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nclientcatcode="
						+ clientCategory.getNclientcatcode() + " and nsitecode=" + userInfo.getNmastersitecode();

				jdbcTemplate.execute(updateQueryString);

				listAfterUpdate.add(clientCategory);
				listBeforeUpdate.add(objClientCategory);

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
						Arrays.asList("IDS_EDITCLIENTCATEGORY"), userInfo);

				return getClientCategory(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to delete entry in clientcategory table. Need to validate
	 * that the specified clientcategory object is active and is not associated with
	 * any of its child tables before updating its nstatus to -1.
	 * 
	 * @param clientcategory [ClientCategory] object holding detail to be deleted in
	 *                       clientcategory table
	 * @return response entity object holding response status and data of deleted
	 *         clientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<? extends Object> deleteClientCategory(final ClientCategory clientCategory,
			final UserInfo userInfo) throws Exception {
		final ClientCategory clientCategoryByID = (ClientCategory) getActiveClientCategoryById(
				clientCategory.getNclientcatcode());

		if (clientCategoryByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "select 'IDS_CLIENT' as Msg from client where nclientcatcode= "
					+ clientCategory.getNclientcatcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + " union all "
					+ "select 'IDS_QUOTATION' as Msg from quotation where nclientcatcode= "
					+ clientCategory.getNclientcatcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "  union all "
					+ "select  'IDS_PROJECTMASTER' as Msg from  projectmaster pm where pm.nclientcatcode = "
					+ clientCategory.getNclientcatcode() + " and nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pm.nsitecode = "
					+ userInfo.getNtranssitecode() + " union all "
					+ "select  'IDS_GOODSIN' as Msg from  goodsin gi where gi.nclientcatcode = "
					+ clientCategory.getNclientcatcode() + " and gi.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and gi.nsitecode = "
					+ userInfo.getNtranssitecode();

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(clientCategory.getNclientcatcode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final List<Object> savedClientCategoryList = new ArrayList<>();
				final String updateQueryString = "update clientcategory set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nclientcatcode="
						+ clientCategory.getNclientcatcode() + " and nsitecode=" + userInfo.getNmastersitecode();

				jdbcTemplate.execute(updateQueryString);

				clientCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedClientCategoryList.add(clientCategory);

				auditUtilityFunction.fnInsertAuditAction(savedClientCategoryList, 1, null,
						Arrays.asList("IDS_DELETECLIENTCATEGORY"), userInfo);
				return getClientCategory(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
