package com.agaramtech.qualis.basemaster.service.sampletestcomments;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.CommentSubType;
import com.agaramtech.qualis.basemaster.model.CommentType;
import com.agaramtech.qualis.basemaster.model.SampleTestComments;
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
 * This class is used to perform CRUD Operation on "sampletestcomments" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class SampleTestCommentsDAOImpl implements SampleTestCommentsDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(SampleTestCommentsDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available sampletestcomments for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         sampletestcomments
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleTestComments(final UserInfo userInfo) throws Exception {
		final String getQuery = "select a.nsampletestcommentscode, a.ncommenttypecode,coalesce(b.jsondata->'scommenttype'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "b.jsondata->'scommenttype'->>'en-US') as scommenttype,a.ncommentsubtypecode,coalesce(c.jsondata->'scommentsubtype'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "c.jsondata->'scommentsubtype'->>'en-US') as scommentsubtype,a.spredefinedname,a.sdescription,"
				+ "a.nsitecode,a.nstatus from sampletestcomments a,commenttype b,commentsubtype c where a.ncommenttypecode = b.ncommenttypecode and "
				+ " a.ncommentsubtypecode = c.ncommentsubtypecode and a.nsampletestcommentscode > 0  and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode="
				+ userInfo.getNmastersitecode();
		LOGGER.info("getSampleTestComments-->" + getQuery);
		return new ResponseEntity<>(jdbcTemplate.query(getQuery, new SampleTestComments()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active sampletestcomments object based on the
	 * specified nsampletestcommentsCode.
	 * 
	 * @param nsampletestcommentsCode [int] primary key of sampletestcomments object
	 * @param userInfo                [UserInfo] holding logged in user details
	 *                                based on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         sampletestcomments object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCommentType(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select ncommenttypecode,coalesce(g.jsondata->'scommenttype'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " g.jsondata->'scommenttype'->>'en-US') as scommenttype,ndefaultstatus,nsitecode,nstatus "
				+ " from commenttype g where g.ncommenttypecode > 0 and g.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNmastersitecode() ;
		outputMap.put("CommentType", jdbcTemplate.query(strQuery, new CommentType()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getCommentSubType(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select ncommentsubtypecode,coalesce(g.jsondata->'scommentsubtype'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " g.jsondata->'scommentsubtype'->>'en-US') as scommentsubtype,coalesce(g.jsondata->'predefinedenabled','false') as spredefinedenable,ndefaultstatus,nsitecode,nstatus "
				+ " from commentsubtype g where g.ncommentsubtypecode > 0 and g.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and nsitecode=" + userInfo.getNmastersitecode()
				+ " order by ncommentsubtypecode";				;
		outputMap.put("CommentSubType", jdbcTemplate.query(strQuery, new CommentSubType()));
		outputMap.put("SelectedCommentSubType", jdbcTemplate.query(strQuery, new CommentSubType()).get(0));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to sampletestcomments table.
	 * sampletestcomments Name is unique across the database. Need to check for
	 * duplicate entry of sampletestcomments name for the specified site before
	 * saving into database. * Need to check that there should be only one default
	 * sampletestcomments for a site.
	 * 
	 * @param objsampletestcomments [sampletestcomments] object holding details to
	 *                              be added in sampletestcomments table
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return saved sampletestcomments object with status code 200 if saved
	 *         successfully else if the sampletestcomments already exists, response
	 *         will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createSampleTestComments(final SampleTestComments objSampleTestComments,
			final UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table sampletestcomments "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedsampletestcommentsList = new ArrayList<>();
		List<SampleTestComments> sampleTestCommentsListById = new ArrayList<>();
		List<SampleTestComments> sampleTestCommentsListByName = new ArrayList<>();
		if (objSampleTestComments.getNcommentsubtypecode() == Enumeration.CommentSubType.GENERAL_COMMENTS
				.getncommentsubtype()
				|| objSampleTestComments.getNcommentsubtypecode() == Enumeration.CommentSubType.DEVIATION_COMMENTS
						.getncommentsubtype()) {
			sampleTestCommentsListById = getSampleTestCommentsListById(objSampleTestComments.getNcommentsubtypecode(),
					objSampleTestComments.getNsitecode());
		}
		if (sampleTestCommentsListById.isEmpty()) {
			if (objSampleTestComments.getNcommentsubtypecode() == Enumeration.CommentSubType.PREDEFINED_TEST_COMMENTS
					.getncommentsubtype()) {
				sampleTestCommentsListByName = getPreDefinedName(objSampleTestComments.getSpredefinedname(),
						objSampleTestComments.getNsitecode());
			}
			if (sampleTestCommentsListByName.isEmpty()) {
				String seqnoquery = "select nsequenceno from seqnobasemaster where stablename='sampletestcomments'";
				int ncount = jdbcTemplate.queryForObject(seqnoquery, Integer.class);
				ncount++;
				final String insertquery = "INSERT INTO public.sampletestcomments (nsampletestcommentscode,ncommenttypecode,ncommentsubtypecode,spredefinedname,sdescription,dmodifieddate,nsitecode,nstatus)"
						+ "VALUES (" + ncount + "," + objSampleTestComments.getNcommenttypecode() + ","
						+ objSampleTestComments.getNcommentsubtypecode() + ",N'"
						+ stringUtilityFunction.replaceQuote(objSampleTestComments.getSpredefinedname()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objSampleTestComments.getSdescription()) + "','"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);
				String updatequery = "update seqnobasemaster set nsequenceno =" + ncount
						+ "  where stablename ='sampletestcomments'";
				jdbcTemplate.execute(updatequery);
				objSampleTestComments.setNsampletestcommentscode(ncount);
				savedsampletestcommentsList.add(objSampleTestComments);
				multilingualIDList.add("IDS_ADDSAMPLETESTCOMMENTS");
				auditUtilityFunction.fnInsertAuditAction(savedsampletestcommentsList, 1, null, multilingualIDList,
						userInfo);
				return getSampleTestComments(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to update entry in sampletestcomments table. Need to
	 * validate that the sampletestcomments object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * sampletestcomments name for the specified site before saving into database.
	 * Need to check that there should be only one default sampletestcomments for a
	 * site
	 * 
	 * @param objsampletestcomments [sampletestcomments] object holding details to
	 *                              be updated in sampletestcomments table
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return saved sampletestcomments object with status code 200 if saved
	 *         successfully else if the sampletestcomments already exists, response
	 *         will be returned as 'Already Exists' with status code 409 else if the
	 *         sampletestcomments to be updated is not available, response will be
	 *         returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSampleTestComments(final SampleTestComments objSampleTestComments,
			final UserInfo userInfo) throws Exception {
		final SampleTestComments sampletestcomments = getActiveSampleTestCommentsById(
				objSampleTestComments.getNsampletestcommentscode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		String queryString = "";
		if (sampletestcomments == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (objSampleTestComments.getNcommentsubtypecode() == 3) {
				queryString = "select nsampletestcommentscode from sampletestcomments where spredefinedname = '"
						+ stringUtilityFunction.replaceQuote(objSampleTestComments.getSpredefinedname())
						+ "' and ncommentsubtypecode = 3 and nsampletestcommentscode <> "
						+ objSampleTestComments.getNsampletestcommentscode() 
						+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode="  + userInfo.getNmastersitecode() ;
			} else {
				queryString = "select nsampletestcommentscode from sampletestcomments where "
						+ " nsampletestcommentscode <> " + objSampleTestComments.getNsampletestcommentscode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ncommentsubtypecode = " + objSampleTestComments.getNcommentsubtypecode()
						+ " and nsitecode=" + userInfo.getNmastersitecode();
			}
			final List<SampleTestComments> sampletestcommentsList = (List<SampleTestComments>) jdbcTemplate
					.query(queryString, new SampleTestComments());
			if (sampletestcommentsList.isEmpty()) {
				final String updateQueryString = "update sampletestcomments set spredefinedname=N'"
						+ stringUtilityFunction.replaceQuote(objSampleTestComments.getSpredefinedname())
						+ "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(objSampleTestComments.getSdescription())
						+ "', ncommenttypecode=" + objSampleTestComments.getNcommenttypecode()
						+ ",ncommentsubtypecode = " + objSampleTestComments.getNcommentsubtypecode()
						+ ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nsampletestcommentscode=" + objSampleTestComments.getNsampletestcommentscode() + ";";
				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(objSampleTestComments);
				listBeforeUpdate.add(sampletestcomments);
				multilingualIDList.add("IDS_EDITSAMPLETESTCOMMENTS");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getSampleTestComments(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in sampletestcomments table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objsampletestcomments [sampletestcomments] an Object holds the record
	 *                              to be deleted
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return a response entity with list of available sampletestcomments objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteSampleTestComments(final SampleTestComments objSampleTestComments,
			final UserInfo userInfo) throws Exception {
		final SampleTestComments sampletestcomments = getActiveSampleTestCommentsById(
				objSampleTestComments.getNsampletestcommentscode(), userInfo);
		if (sampletestcomments == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = " select 'IDS_SAMPLETESTCOMMENTS' as Msg from registrationtestcomments where nsamplecommentscode= "
					+ objSampleTestComments.getNsampletestcommentscode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(objSampleTestComments.getNsampletestcommentscode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedsampletestcommentsList = new ArrayList<>();
				final String updateQueryString = " update sampletestcomments set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nsampletestcommentscode="
						+ objSampleTestComments.getNsampletestcommentscode();
				jdbcTemplate.execute(updateQueryString);
				objSampleTestComments.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedsampletestcommentsList.add(objSampleTestComments);
				multilingualIDList.add("IDS_DELETESAMPLETESTCOMMENTS");
				auditUtilityFunction.fnInsertAuditAction(deletedsampletestcommentsList, 1, null, multilingualIDList,
						userInfo);
				return getSampleTestComments(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active sampletestcomments object based on the specified
	 * nsampletestcommentsCode.
	 * 
	 * @param nsampletestcommentsCode [int] primary key of sampletestcomments object
	 * @param userInfo                [UserInfo] holding logged in user details
	 *                                based on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         sampletestcomments object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public SampleTestComments getActiveSampleTestCommentsById(final int nsampletestcommentcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = " select stc.nsampletestcommentscode, stc.ncommenttypecode, stc.ncommentsubtypecode, stc.spredefinedname, stc.sdescription, stc.nsitecode, stc.nstatus,coalesce(g.jsondata->'predefinedenabled','false') as spredefinedenable  "
				+ " from sampletestcomments stc ,commentsubtype g  where stc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and g.ncommentsubtypecode = stc.ncommentsubtypecode and stc.nsampletestcommentscode = "
				+ nsampletestcommentcode;
		return (SampleTestComments) jdbcUtilityFunction.queryForObject(strQuery, SampleTestComments.class,
				jdbcTemplate);
	}

	private List<SampleTestComments> getPreDefinedName(final String spredefinedname, final int nmastersitecode)
			throws Exception {
		final String strQuery = " select nsampletestcommentscode from sampletestcomments where spredefinedname = N'"
				+ stringUtilityFunction.replaceQuote(spredefinedname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ncommentsubtypecode = 3 and nsitecode =" + nmastersitecode;
		return (List<SampleTestComments>) jdbcTemplate.query(strQuery, new SampleTestComments());
	}

	private List<SampleTestComments> getSampleTestCommentsListById(final int ncommentsubtypecode,
			final int nmastersitecode) throws Exception {
		final String strQuery = " select nsampletestcommentscode, ncommenttypecode, ncommentsubtypecode, spredefinedname, sdescription, nsitecode, nstatus"
				+ " from sampletestcomments where ncommentsubtypecode = " + ncommentsubtypecode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmastersitecode;
		return (List<SampleTestComments>) jdbcTemplate.query(strQuery, new SampleTestComments());
	}

}
