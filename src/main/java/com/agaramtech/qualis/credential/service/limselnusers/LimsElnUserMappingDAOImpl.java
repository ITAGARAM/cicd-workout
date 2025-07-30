package com.agaramtech.qualis.credential.service.limselnusers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.credential.model.LimsElnUserMapping;
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
import lombok.Data;

@Data
@AllArgsConstructor
@Repository
public class LimsElnUserMappingDAOImpl implements LimsElnUserMappingDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(LimsElnUserMappingDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * Fetches the LIMS ELN User Mapping details based on the given UserInfo.
	 *
	 * @param userInfo User information for filtering the data
	 * @return ResponseEntity containing the list of LIMS ELN User Mappings
	 * @throws Exception if there is an error executing the query
	 */
	@Override
	public ResponseEntity<Object> getlimselnusermapping(final UserInfo userInfo) throws Exception {
		final String query = "SELECT lem.nlimsusercode, lem.nelnusermappingcode, lem.selnusername AS userfullname, "
				+ "lem.nelncode AS usercode, lem.selnuserid AS username, us.sloginid, CONCAT(us.sfirstname, ' ', us.slastname) AS slimsusername "
				+ "FROM limselnusermapping lem, users us " + "WHERE us.nusercode = lem.nlimsusercode "
				+ "AND us.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND lem.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND lem.nsitecode = " + userInfo.getNmastersitecode();
		LOGGER.info("getlimselnusermapping -->");
		return new ResponseEntity<>((List<LimsElnUserMapping>) jdbcTemplate.query(query, new LimsElnUserMapping()),
				HttpStatus.OK);
	}

	/**
	 * Creates a new LIMS ELN User Mapping.
	 *
	 * @param limselnuser LIMS ELN User Mapping object to be created
	 * @param userInfo    User information to capture audit and other details
	 * @return ResponseEntity containing updated LIMS ELN User Mappings
	 * @throws Exception if there is an error during the insert process
	 */
	@Override
	public ResponseEntity<Object> createLimsElnUsermapping(LimsElnUserMapping limselnuser, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedReasonList = new ArrayList<>();
		String sequencequery = "SELECT nsequenceno FROM seqnocredentialmanagement WHERE stablename = 'limselnusermapping'";
		int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
		nsequenceno++;
		String insertquery = "INSERT INTO limselnusermapping (nelnusermappingcode, nlimsusercode, nelncode, nelnusergroupcode, "
				+ "selnuserid, selnusername, dmodifieddate, nsitecode, nstatus) " + "VALUES (" + nsequenceno + ", "
				+ limselnuser.getNlimsusercode() + ", " + limselnuser.getUsercode() + ", "
				+ limselnuser.getNelnusergroupcode() + ", N'"
				+ stringUtilityFunction.replaceQuote(limselnuser.getUsername()) + "', " + "N'"
				+ stringUtilityFunction.replaceQuote(limselnuser.getUserfullname()) + "', '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		jdbcTemplate.execute(insertquery);
		String updatequery = "UPDATE seqnocredentialmanagement SET nsequenceno = " + nsequenceno
				+ " WHERE stablename = 'limselnusermapping'";
		jdbcTemplate.execute(updatequery);
		savedReasonList.add(limselnuser);
		multilingualIDList.add("IDS_ADDLIMSELNUSER");
		auditUtilityFunction.fnInsertAuditAction(savedReasonList, 1, null, multilingualIDList, userInfo);
		return getlimselnusermapping(userInfo);
	}

	/**
	 * Fetches users who are eligible for LIMS ELN User Mapping.
	 *
	 * @param userInfo User information for filtering the data
	 * @return ResponseEntity containing the list of available LIMS users for
	 *         mapping
	 * @throws Exception if there is an error executing the query
	 */
	@Override
	public ResponseEntity<Object> getLimsUsers(final UserInfo userInfo) throws Exception {
		final String query = "SELECT nusercode AS nlimsusercode, sloginid, CONCAT(sfirstname, ' ', slastname) AS slimsusername "
				+ "FROM users WHERE nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND ntransactionstatus != 7 AND nusercode NOT IN (SELECT nlimsusercode FROM limselnusermapping WHERE nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
				+ "AND nusercode IN (SELECT ur.nusercode FROM usermultirole umr, userssite ur WHERE ur.nusersitecode = umr.nusersitecode "
				+ "AND umr.nuserrolecode IN (SELECT nuserrolecode FROM treetemplatetransactionrole ttrl "
				+ "WHERE ttrl.nlevelno = (SELECT MAX(ttr.nlevelno) FROM treeversiontemplate tvt, treetemplatetransactionrole ttr, "
				+ "approvalconfigversion acv, approvalconfig ac WHERE ac.napprovalconfigcode = acv.napprovalconfigcode "
				+ "AND ac.napprovalsubtypecode = 2 AND tvt.ntreeversiontempcode = ttr.ntreeversiontempcode "
				+ "AND tvt.ntreeversiontempcode = acv.ntreeversiontempcode "
				+ "AND ttrl.ntreeversiontempcode = ttr.ntreeversiontempcode AND acv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ttr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " GROUP BY ttr.ntreeversiontempcode)))";
		return new ResponseEntity<>((List<LimsElnUserMapping>) jdbcTemplate.query(query, new LimsElnUserMapping()),
				HttpStatus.OK);
	}

	/**
	 * Deletes a LIMS ELN User Mapping.
	 *
	 * @param limselnuser LIMS ELN User Mapping to be deleted
	 * @param userInfo    User information for audit purposes
	 * @return ResponseEntity containing the updated list of LIMS ELN User Mappings
	 * @throws Exception if there is an error during deletion
	 */
	@Override
	public ResponseEntity<Object> deleteLimsElnUsermapping(LimsElnUserMapping limselnuser, UserInfo userInfo)
			throws Exception {
		final LimsElnUserMapping reasonbyID = getActiveLimsElnUsermappingById(limselnuser.getNelnusermappingcode());
		if (reasonbyID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedReasonList = new ArrayList<>();
			final String updateQueryString = "UPDATE limselnusermapping SET nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " WHERE nelnusermappingcode = "
					+ limselnuser.getNelnusermappingcode();
			jdbcTemplate.execute(updateQueryString);
			limselnuser.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			savedReasonList.add(limselnuser);
			multilingualIDList.add("IDS_DELETELIMSELNUSER");
			auditUtilityFunction.fnInsertAuditAction(savedReasonList, 1, null, multilingualIDList, userInfo);
			return getlimselnusermapping(userInfo);
		}
	}

	/**
	 * Fetches an active LIMS ELN User Mapping by its ID.
	 *
	 * @param nelnusermappingcode The mapping ID to look for
	 * @return The LIMS ELN User Mapping object if found
	 * @throws Exception if there is an error during the query
	 */
	@Override
	public LimsElnUserMapping getActiveLimsElnUsermappingById(final int nelnusermappingcode) throws Exception {
		final String strQuery = "SELECT lem.nelnusermappingcode, lem.selnusername AS userfullname, "
				+ "lem.nelncode AS usercode, lem.selnuserid AS username, us.sloginid, lem.nlimsusercode, "
				+ "CONCAT(us.sfirstname, ' ', us.slastname) AS slimsusername "
				+ "FROM limselnusermapping lem, users us " + "WHERE us.nusercode = lem.nlimsusercode AND lem.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND us.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND lem.nelnusermappingcode = "
				+ nelnusermappingcode;
		return (LimsElnUserMapping) jdbcUtilityFunction.queryForObject(strQuery, LimsElnUserMapping.class,
				jdbcTemplate);
	}

	/**
	 * Updates an existing LIMS ELN User Mapping.
	 *
	 * @param limselnuser LIMS ELN User Mapping object to update
	 * @param userInfo    User information for auditing
	 * @return ResponseEntity containing updated LIMS ELN User Mappings
	 * @throws Exception if there is an error during the update process
	 */
	@Override
	public ResponseEntity<Object> updateLimsElnUsermapping(LimsElnUserMapping limselnuser, UserInfo userInfo)
			throws Exception {
		final LimsElnUserMapping objReason = getActiveLimsElnUsermappingById(limselnuser.getNelnusermappingcode());
		if (objReason == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			final String updateQueryString = "UPDATE limselnusermapping SET nlimsusercode = "
					+ limselnuser.getNlimsusercode() + ", nelncode = " + limselnuser.getNelncode() + ", selnuserid = '"
					+ limselnuser.getUsername() + "', " + "selnusername = '" + limselnuser.getUserfullname()
					+ "', dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "' WHERE nelnusermappingcode = " + limselnuser.getNelnusermappingcode();
			jdbcTemplate.execute(updateQueryString);
			final List<String> multilingualIDList = new ArrayList<>();
			multilingualIDList.add("IDS_EDITLIMSELNUSER");
			final List<Object> listAfterSave = new ArrayList<>();
			listAfterSave.add(limselnuser);
			final List<Object> listBeforeSave = new ArrayList<>();
			listBeforeSave.add(objReason);
			auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
			return getlimselnusermapping(userInfo);
		}
	}
}
