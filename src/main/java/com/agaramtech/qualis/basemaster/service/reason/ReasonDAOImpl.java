package com.agaramtech.qualis.basemaster.service.reason;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.Reason;
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
 * This class is used to perform CRUD Operation on "reason" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class ReasonDAOImpl implements ReasonDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReasonDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available reasons for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         reasons
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception {

		final String strQuery = "select c.nreasoncode,c.sreason,c.sdescription,c.nsitecode,c.nstatus from reason c where c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nreasoncode > 0 "
				+ " and c.nsitecode = " + userInfo.getNmastersitecode() + " order by nreasoncode";
		LOGGER.info("Get Reason query:--->"+strQuery);
		return new ResponseEntity<Object>((List<Reason>) jdbcTemplate.query(strQuery, new Reason()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active reason object based on the specified
	 * nreasoncode along with the nsitecode from [UserInfo].
	 * 
	 * @param nreasoncode [int] primary key of reason object
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of reason
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Reason getActiveReasonById(int nreasoncode, UserInfo userInfo) throws Exception {
		final String strQuery = "select c.nreasoncode,c.sreason,c.sdescription,c.nsitecode,c.nstatus from reason c where c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nreasoncode = " + nreasoncode;
		return (Reason) jdbcUtilityFunction.queryForObject(strQuery, Reason.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the unit object for the specified reason name
	 * and site.
	 * 
	 * @param sreasonname     [String] name of the reason
	 * @param nmasterSiteCode [int] site code of the reason
	 * @return reason object based on the specified unit name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createReason(Reason reason, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedReasonList = new ArrayList<>();
		final Reason ReasonListByName = getReasonByName(reason.getSreason(), userInfo.getNmastersitecode());
		if (ReasonListByName == null) {
			String sequencequery = "select nsequenceno from SeqNoBasemaster where stablename ='reason'";
			int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class, jdbcTemplate);
			nsequenceno++;
			String insertquery = "Insert  into reason (nreasoncode,sreason,sdescription,dmodifieddate,nsitecode,nstatus) "
					+ "values(" + nsequenceno + ",N'" + stringUtilityFunction.replaceQuote(reason.getSreason()) + "',"
					+ "N'" + stringUtilityFunction.replaceQuote(reason.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ " " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update SeqNoBasemaster set nsequenceno=" + nsequenceno
					+ " where stablename ='reason'";
			jdbcTemplate.execute(updatequery);
			reason.setNreasoncode(nsequenceno);
			savedReasonList.add(reason);
			multilingualIDList.add("IDS_ADDREASON");
			auditUtilityFunction.fnInsertAuditAction(savedReasonList, 1, null, multilingualIDList, userInfo);
			return getReason(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the reason object for the specified reason name
	 * and site.
	 * 
	 * @param sreasonname     [String] name of the reason
	 * @param nmasterSiteCode [int] site code of the reason
	 * @return reason object based on the specified reason name and site
	 * @throws Exception that are thrown from this DAO layer
	 */

	private Reason getReasonByName(String reason, int nmasterSiteCode) throws Exception {
		final String strQuery = "select nreasoncode from reason where sreason = N'"
				+ stringUtilityFunction.replaceQuote(reason) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (Reason) jdbcUtilityFunction.queryForObject(strQuery, Reason.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in reason table. Need to validate that
	 * the reason object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of reason name for the specified
	 * site before saving into database. Need to check that there should be only one
	 * default reason for a site
	 * 
	 * @param objReason [Reason] object holding details to be updated in reason
	 *                  table
	 * @param userInfo  [UserInfo] holding logged in user details based on which the
	 *                  list is to be fetched
	 * @return saved reason object with status code 200 if saved successfully else
	 *         if the reason already exists, response will be returned as 'Already
	 *         Exists' with status code 409 else if the reason to be updated is not
	 *         available, response will be returned as 'Already Deleted' with status
	 *         code 417
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateReason(Reason reason, UserInfo userInfo) throws Exception {
		final Reason objReason = getActiveReasonById(reason.getNreasoncode(), userInfo);
		if (objReason == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			final String queryString = "select nreasoncode from reason where sreason = '"
					+ stringUtilityFunction.replaceQuote(reason.getSreason()) + "' and nreasoncode <> "
					+ reason.getNreasoncode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
					+ userInfo.getNmastersitecode();
			final List<Reason> reasonList = (List<Reason>) jdbcTemplate.query(queryString, new Reason());
			if (reasonList.isEmpty()) {
				final String updateQueryString = "update reason set sreason='"
						+ stringUtilityFunction.replaceQuote(reason.getSreason()) + "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(reason.getSdescription()) + "', dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nreasoncode="
						+ reason.getNreasoncode() + "";
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITREASON");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(reason);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(objReason);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getReason(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in reason table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as '?'
	 * 
	 * @param objUnit  [Reason] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on which the
	 *                 list is to be fetched
	 * @return a response entity with list of available Reason objects
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteReason(Reason reason, UserInfo userInfo) throws Exception {
		final Reason reasonbyID = (Reason) getActiveReasonById(reason.getNreasoncode(), userInfo);
		if (reasonbyID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_ESIGN' as Msg from auditaction where nreasoncode= "
					+ reasonbyID.getNreasoncode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedReasonList = new ArrayList<>();
				final String updateQueryString = "update reason set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nreasoncode="
						+ reason.getNreasoncode() + "";
				jdbcTemplate.execute(updateQueryString);
				reason.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedReasonList.add(reason);
				multilingualIDList.add("IDS_DELETEREASON");
				auditUtilityFunction.fnInsertAuditAction(savedReasonList, 1, null, multilingualIDList, userInfo);
				return getReason(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
