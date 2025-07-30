package com.agaramtech.qualis.credential.service.designation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.credential.model.Designation;
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

@AllArgsConstructor
@Repository
public class DesignationDAOImpl implements DesignationDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(DesignationDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active designation for the
	 * specified site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         designation
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDesignation(final UserInfo userInfo) throws Exception {
		final String strQuery = "select d.*,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus"
				+ " from designation d,transactionstatus ts where d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.ndesignationcode > 0"
				+ " and ts.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and d.ndefaultstatus=ts.ntranscode and d.nsitecode = " + userInfo.getNmastersitecode();
		LOGGER.info(" Get Designation By Site qry :====>"+strQuery);
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new Designation()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active credentialdesignation object based on
	 * the specified ndesignationcode.
	 * 
	 * @param ndesignationcode [int] primary key of credentialdesignation object
	 * @return response entity object holding response status and data of
	 *         designation object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public Designation getActiveDesignationById(final int ndesignationcode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select d.*,COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus from designation d,transactionstatus ts "
				+ " where d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and d.ndefaultstatus=ts.ntranscode" + " and d.ndesignationcode = " + ndesignationcode;
		return (Designation) jdbcUtilityFunction.queryForObject(strQuery, Designation.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to credentialdesignation table. Need
	 * to check for duplicate entry of designation name for the specified site
	 * before saving into database.
	 * 
	 * @param credentialDesignation [Designation] object holding details to be added
	 *                              in credentialdesignation table
	 * @param userInfo              [UserInfo] object holding details of loggedin
	 *                              user
	 * @return response entity object holding response status and data of added
	 *         designation object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createDesignation(final Designation credentialDesignation,final  UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table designation " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedDesignationList = new ArrayList<>();
		final Designation designationByName = getDesignationByName(
				credentialDesignation.getSdesignationname(), userInfo.getNmastersitecode());
		if (designationByName==null) {
			if (credentialDesignation.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final Designation defaultDesignation = getDesignationByDefaultStatus(userInfo.getNmastersitecode());
				if (defaultDesignation != null) {
					final Designation designationBeforeSave = SerializationUtils.clone(defaultDesignation);
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(designationBeforeSave);
					defaultDesignation
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					final String updateQueryString = " update designation set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where ndesignationcode ="
							+ defaultDesignation.getNdesignationcode();
					jdbcTemplate.execute(updateQueryString);
					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultDesignation);
					multilingualIDList.add("IDS_EDITDESIGNATION");
					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
					multilingualIDList, userInfo);
					multilingualIDList.clear();
				}
			}
			String sequencenoquery = "select nsequenceno from SeqNoCredentialManagement where stablename='designation'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			final String insertquery = "Insert into designation (ndesignationcode,sdescription,sdesignationname,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(credentialDesignation.getSdescription()) + "',N'"
					+ stringUtilityFunction.replaceQuote(credentialDesignation.getSdesignationname()) + "',"
					+ credentialDesignation.getNdefaultstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			final String updatequery = "update SeqNoCredentialManagement set nsequenceno =" + nsequenceno
					+ " where stablename ='designation'";
			jdbcTemplate.execute(updatequery);
			credentialDesignation.setNdesignationcode(nsequenceno);
			savedDesignationList.add(credentialDesignation);
			multilingualIDList.add("IDS_ADDDESIGNATION");
			auditUtilityFunction.fnInsertAuditAction(savedDesignationList, 1, null, multilingualIDList, userInfo);
			return getDesignation(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the active credentialdesignation objects for the
	 * specified designation name and site.
	 * 
	 * @param designationname [String] designation name for which the records are to
	 *                        be fetched
	 * @param nmasterSiteCode [int] primary key of site object
	 * @return list of active credentialdesignation based on the specified
	 *         designation name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Designation getDesignationByName(final String designation, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select ndesignationcode from designation where sdesignationname = N'"
				+ stringUtilityFunction.replaceQuote(designation) + "' and nstatus = "+ 
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		//return (Designation) jdbcTemplate.query(strQuery, new Designation());
		return (Designation) jdbcUtilityFunction.queryForObject(strQuery, Designation.class, jdbcTemplate);
	}

	/**
	 * This method is used to get a default designation object with respect to the
	 * site code
	 * 
	 * @param nmasterSiteCode  [int] Site code
	 * @param ndesignationcode [int] primary key of designation table
	 *                         ndesignationcode
	 * @return a Designation Object
	 * @throws Exception that are from DAO layer
	 */
	private Designation getDesignationByDefaultStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select d.* from designation d where d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and d.nsitecode = " + nmasterSiteCode+ "";
		return (Designation) jdbcUtilityFunction.queryForObject(strQuery, Designation.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in credentialdesignation table. Need to
	 * validate that the designation object to be updated is active before updating
	 * details in database. Need to check for duplicate entry of designation name
	 * for the specified site before saving into database.
	 * 
	 * @param credentialDesignation [Designation] object holding details to be
	 *                              updated in credentialdesignation table
	 * @param userInfo              [UserInfo] object holding details of loggedin
	 *                              user
	 * @return response entity object holding response status and data of updated
	 *         designation object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateDesignation(final Designation credentialDesignation,final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final Designation designation = getActiveDesignationById(credentialDesignation.getNdesignationcode(), userInfo);
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		if (designation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString ="select ndesignationcode from designation where sdesignationname =  '"
					+ stringUtilityFunction.replaceQuote(credentialDesignation.getSdesignationname())
					+ "' and ndesignationcode <> " + credentialDesignation.getNdesignationcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and nsitecode = " + userInfo.getNmastersitecode();
			final List<Designation> designationList = (List<Designation>) jdbcTemplate.query(queryString,
					new Designation());
			if (designationList.isEmpty()) {
				if (credentialDesignation.getNdefaultstatus() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {
					final Designation defaultDesignation = getDesignationByDefaultStatus(userInfo.getNmastersitecode());
					if (defaultDesignation != null && defaultDesignation.getNdesignationcode() != credentialDesignation
							.getNdesignationcode()) {
						final Designation designationBeforeSave = SerializationUtils.clone(defaultDesignation);
						listBeforeUpdate.add(designationBeforeSave);
						defaultDesignation
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						final String updateQueryString = "update designation set ndefaultstatus = "
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where ndesignationcode="
								+ defaultDesignation.getNdesignationcode()+ "";
						jdbcTemplate.execute(updateQueryString);
						listAfterUpdate.add(defaultDesignation);
						multilingualIDList.add("IDS_EDITDESIGNATION");
					}
				}
				final String updateQueryString = "update designation set sdesignationname= N'"
						+ stringUtilityFunction.replaceQuote(credentialDesignation.getSdesignationname())
						+ "', sdescription = N'"
						+ stringUtilityFunction.replaceQuote(credentialDesignation.getSdescription())
						+ "', ndefaultstatus=" + credentialDesignation.getNdefaultstatus() + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ndesignationcode="
						+ credentialDesignation.getNdesignationcode() +  "";
				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(credentialDesignation);
				listBeforeUpdate.add(designation);
				multilingualIDList.add("IDS_EDITDESIGNATION");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getDesignation(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to delete entry in credentialdesignation table. Need to
	 * validate that the specified designation object is active and is not
	 * associated with any of its child tables before updating its nstatus to -1.
	 * 
	 * @param credentialDesignation [Designation] object holding detail to be
	 *                              deleted in credentialdesignation table
	 * @param userInfo              [UserInfo] object holding details of loggedin
	 *                              user
	 * @return response entity object holding response status and data of deleted
	 *         designation object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteDesignation(final Designation credentialDesignation,  final UserInfo userInfo)
			throws Exception {
		final Designation designation = getActiveDesignationById(credentialDesignation.getNdesignationcode(), userInfo);
		if (designation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_USERS' as Msg from users where ndesignationcode= "
					+ designation.getNdesignationcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);   
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(designation.getNdesignationcode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedDesignationList = new ArrayList<>();
				final String updateQueryString = "update designation set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ndesignationcode="
						+ credentialDesignation.getNdesignationcode();
				jdbcTemplate.execute(updateQueryString);
				credentialDesignation.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedDesignationList.add(credentialDesignation);
				multilingualIDList.add("IDS_DELETEDESIGNATION");
				auditUtilityFunction.fnInsertAuditAction(deletedDesignationList, 1, null, multilingualIDList, userInfo);
				return getDesignation(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
	/**
	 * This method is used to retrieve active designation object based on the specified ndesignationCode.
	 * @param ndesignationCode [int] primary key of designation object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of designation object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getAllActiveDesignation(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from designation d where d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.ndesignationcode > 0"
				+ " and d.nsitecode = " + nmasterSiteCode;
		return new ResponseEntity<>((List<Designation>) jdbcTemplate.query(strQuery, new Designation()), HttpStatus.OK);
	}

}
