package com.agaramtech.qualis.barcode.service.samplecycle;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.barcode.model.SampleCycle;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.service.projecttype.ProjectTypeDAO;
import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "samplecycle" table by
 * implementing methods from its interface.
 */
@Repository
@RequiredArgsConstructor
public class SampleCycleDAOImpl implements SampleCycleDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleCycleDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectTypeDAO projectTypeDAO;

	/**
	 * This method is used to retrieve list of all active Sample Cycle for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Sample Cycle
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleCycle(UserInfo userInfo) throws Exception {
		final String strQuery = "select sc.nsamplecyclecode, sc.ssamplecyclename, sc.ncode, sc.nsitecode, sc.nstatus, sc.nprojecttypecode, p.sprojecttypename"
				+ " from samplecycle sc, projecttype p  where sc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sc.nsamplecyclecode > 0 and sc.nsitecode = " + userInfo.getNmastersitecode()
				+ " and sc.nprojecttypecode = p.nprojecttypecode";
		LOGGER.info("getSampleCycle -->" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SampleCycle()), HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active samplecycle objects for the specified
	 *                        ssamplecycle name and code.
	 * @param ssamplecycle    [String] and code it to be checked the duplicate in
	 *                        the samplecycle table.
	 * @param nsamplecyclecode [int] primary key in Sample Cycle
	 * @param nmasterSiteCode [int] site code of the SampleCycle
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return list of active samplecycle code(s) based on the specified
	 *         ssamplecycle name and code
	 * @throws Exception that are thrown from this DAO layer
	 */
	private SampleCycle getExistingRecord(int nsamplecyclecode, SampleCycle sampleCycle, UserInfo userInfo)
			throws Exception {
		String samplecode = "";
		if (nsamplecyclecode != 0) {
			samplecode = "and nsamplecyclecode <> " + sampleCycle.getNsamplecyclecode() + "";
		}
		final String existingCycle = "select "
				+ "CASE WHEN EXISTS(select ssamplecyclename from samplecycle where ssamplecyclename = N'"
				+ stringUtilityFunction.replaceQuote(sampleCycle.getSsamplecyclename()) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprojecttypecode = "
				+ sampleCycle.getNprojecttypecode() + " " + samplecode + " )" + " THEN TRUE" + " ELSE FALSE "
				+ " End AS issamplecycle, CASE when exists (select ncode from samplecycle where ncode = "
				+ sampleCycle.getNcode() + " and nsitecode = " + userInfo.getNmastersitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nprojecttypecode = "
				+ sampleCycle.getNprojecttypecode() + " " + samplecode + " )" + " THEN TRUE " + " ELSE FALSE "
				+ " End AS iscode ";
		return (SampleCycle) jdbcUtilityFunction.queryForObject(existingCycle, SampleCycle.class, jdbcTemplate);

	}

	/**
	 * This method is used to add a new entry to Sample Cycle table. Need to check
	 * for duplicate entry of Sample Cycle name based on ProjectType for the
	 * specified site before saving into database.
	 * 
	 * @param sampleCycle [Sample Cycle] object holding details to be added in
	 *                    Sample Cycle table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return inserted Sample Cycle object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSampleCycle(SampleCycle sampleCycle, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSampleCycleList = new ArrayList<>();
		final ResponseEntity<Object> activeProjectType = projectTypeDAO
				.getActiveProjectTypeById(sampleCycle.getNprojecttypecode(), userInfo);
		if (activeProjectType.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			final String alert = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final SampleCycle existingSampleCycle = getExistingRecord(sampleCycle.getNsamplecyclecode(), sampleCycle,
				userInfo);
		if (existingSampleCycle != null && existingSampleCycle.getIssamplecycle() == false
				&& existingSampleCycle.getIscode() == false) {
			if (sampleCycle.getNcode() > 0) {
				String sequencenoquery = "select nsequenceno from seqnobarcode where stablename = 'samplecycle' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
				nsequenceno++;
				String insertquery = "Insert into samplecycle (nsamplecyclecode,ssamplecyclename,ncode,nprojecttypecode,dmodifieddate,nsitecode,nstatus)"
						+ "values(" + nsequenceno + ",N'"
						+ stringUtilityFunction.replaceQuote(sampleCycle.getSsamplecyclename()) + "',"
						+ sampleCycle.getNcode() + "," + sampleCycle.getNprojecttypecode() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " " + userInfo.getNmastersitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);

				String updatequery = "update seqnobarcode set nsequenceno =" + nsequenceno
						+ " where stablename='samplecycle'";
				jdbcTemplate.execute(updatequery);
				sampleCycle.setNsamplecyclecode(nsequenceno);
				savedSampleCycleList.add(sampleCycle);
				multilingualIDList.add("IDS_ADDSAMPLECYCLE");
				auditUtilityFunction.fnInsertAuditAction(savedSampleCycleList, 1, null, multilingualIDList, userInfo);
				return getSampleCycle(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			String alert = "";
			final boolean cycleName = existingSampleCycle.getIssamplecycle();
			final boolean cycleCode = existingSampleCycle.getIscode();
			if (cycleName == true && cycleCode == true) {
				alert = commonFunction.getMultilingualMessage("IDS_SAMPLECYCLENAME", userInfo.getSlanguagefilename())
						+ " and " + commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
			} else if (cycleName == true) {
				alert = commonFunction.getMultilingualMessage("IDS_SAMPLECYCLENAME", userInfo.getSlanguagefilename());
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
	 * This method is used to retrieve active Sample Cycle object based on the
	 * specified nsamplecyclecode.
	 * 
	 * @param nsamplecyclecode [int] primary key of Sample Cycle object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of Sample
	 *         Cycle object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public SampleCycle getActiveSampleCycleById(int nsamplecyclecode, UserInfo userInfo) throws Exception {
		final String strQuery = "select sc.nsamplecyclecode, sc.ssamplecyclename, sc.ncode, sc.nsitecode, sc.nstatus, sc.nprojecttypecode, p.sprojecttypename from samplecycle sc, projecttype p"
				+ " where sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsamplecyclecode = " + nsamplecyclecode + " and sc.nprojecttypecode = p.nprojecttypecode";
		return (SampleCycle) jdbcUtilityFunction.queryForObject(strQuery, SampleCycle.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in Sample Cycle table. Need to validate
	 * that the sampleCycle object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of Sample Cycle name for the
	 * specified site before saving into database.
	 * 
	 * @param sampleCycle [Sample Cycle] object holding details to be updated in
	 *                    Sample Cycle table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         Sample Cycle object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSampleCycle(SampleCycle sampleCycle, UserInfo userInfo) throws Exception {
		final SampleCycle objsamplecycle = getActiveSampleCycleById(sampleCycle.getNsamplecyclecode(), userInfo);
		final ResponseEntity<Object> activeProjectType = projectTypeDAO
				.getActiveProjectTypeById(sampleCycle.getNprojecttypecode(), userInfo);
		if (activeProjectType.getStatusCode() == HttpStatus.EXPECTATION_FAILED) {
			final String alert = commonFunction.getMultilingualMessage("IDS_PROJECTTYPE",
					userInfo.getSlanguagefilename());
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		if (objsamplecycle == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final SampleCycle existingSampleCycle = getExistingRecord(sampleCycle.getNsamplecyclecode(), sampleCycle,
					userInfo);

			if (existingSampleCycle != null && existingSampleCycle.getIssamplecycle() == false
					&& existingSampleCycle.getIscode() == false) {
				if (sampleCycle.getNcode() > 0) {
					final String updateQueryString = "update samplecycle set ssamplecyclename='"
							+ stringUtilityFunction.replaceQuote(sampleCycle.getSsamplecyclename()) + "', " + "ncode ='"
							+ sampleCycle.getNcode() + "',dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nprojecttypecode="
							+ sampleCycle.getNprojecttypecode() + " where nsamplecyclecode ="
							+ sampleCycle.getNsamplecyclecode();
					jdbcTemplate.execute(updateQueryString);
					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add("IDS_EDITSAMPLECYCLE");
					final List<Object> listAfterSave = new ArrayList<>();
					listAfterSave.add(sampleCycle);
					final List<Object> listBeforeSave = new ArrayList<>();
					listBeforeSave.add(objsamplecycle);
					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);
					return getSampleCycle(userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CODEGREATERZERO",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			} else {
				String alert = "";
				final boolean cycleName = existingSampleCycle.getIssamplecycle();
				final boolean cycleCode = existingSampleCycle.getIscode();
				if (cycleName == true && cycleCode == true) {
					alert = commonFunction.getMultilingualMessage("IDS_SAMPLECYCLENAME",
							userInfo.getSlanguagefilename()) + " and "
							+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
				} else if (cycleName == true) {
					alert = commonFunction.getMultilingualMessage("IDS_SAMPLECYCLENAME",
							userInfo.getSlanguagefilename());
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
	 * This method id used to delete an entry in Sample Cycle table Need to check
	 * the record is already deleted or not
	 * 
	 * @param sampleCycle [Sample Cycle] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with corresponding HTTP status and an Sample Cycle
	 *         object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteSampleCycle(SampleCycle samplecycle, UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSampleCycleList = new ArrayList<>();
		final SampleCycle objsamplecycle = getActiveSampleCycleById(samplecycle.getNsamplecyclecode(), userInfo);
		if (objsamplecycle == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String updateQueryString = "update samplecycle set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where nsamplecyclecode="
					+ samplecycle.getNsamplecyclecode();
			jdbcTemplate.execute(updateQueryString);
			samplecycle.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			savedSampleCycleList.add(samplecycle);
			multilingualIDList.add("IDS_DELETESAMPLECYCLE");
			auditUtilityFunction.fnInsertAuditAction(savedSampleCycleList, 1, null, multilingualIDList, userInfo);
			return getSampleCycle(userInfo);

		}
	}

}
