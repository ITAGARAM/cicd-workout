package com.agaramtech.qualis.product.service.sampleappearance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.product.model.SampleAppearance;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SampleAppearanceDAOImpl implements SampleAppearanceDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleAppearanceDAOImpl.class);

	private final CommonFunction commonFunction;
	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	public ResponseEntity<Object> getSampleAppearance(final UserInfo userInfo) throws Exception {
		final String strQuery = "select s.nsampleappearancecode,s.ssampleappearance,s.sdescription,s.ndefaultstatus,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus," + "to_char(s.dmodifieddate, '"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate "
				+ " from sampleappearance s,transactionstatus ts where ts.ntranscode=s.ndefaultstatus and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode ="
				+ userInfo.getNmastersitecode() + " and s.nsampleappearancecode >0 order by s.nsampleappearancecode";
		LOGGER.info("getSampleAppearance -->" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SampleAppearance()), HttpStatus.OK);
	}

	/**
	 * Retrieves a list of sample appearances with the specified name.
	 * 
	 * @param ssampleappearance [String] The name of the sample appearance to search
	 *                          for.
	 * @return List of SampleAppearance objects matching the name.
	 * @throws Exception If any error occurs while fetching the records.
	 */
	private SampleAppearance getSampleAppearanceByName(final String ssampleappearance, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select nsampleappearancecode from sampleappearance where ssampleappearance = N'"
				+ stringUtilityFunction.replaceQuote(ssampleappearance) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+nmasterSiteCode+"";
		return (SampleAppearance) jdbcUtilityFunction.queryForObject(strQuery, SampleAppearance.class, jdbcTemplate);
	}

	/**
	 * Retrieves the default sample appearance for the specified site code.
	 * 
	 * @param nmasterSiteCode [int] The site code for which the default sample
	 *                        appearance is being retrieved.
	 * @return The default SampleAppearance object, or null if no default exists.
	 * @throws Exception If any error occurs while fetching the record.
	 */
	private SampleAppearance getSampleAppearanceByDefaultStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select nsampleappearancecode,ssampleappearance,sdescription,ndefaultstatus from sampleappearance "
				+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and nsitecode = " + nmasterSiteCode;
		return (SampleAppearance) jdbcUtilityFunction.queryForObject(strQuery, SampleAppearance.class, jdbcTemplate);
	}

	/**
	 * Creates a new sample appearance if it does not already exist.
	 * 
	 * @param inputMap         [Map<String, Object>] A map containing additional
	 *                         data for creating the sample appearance.
	 * @param sampleAppearance [SampleAppearance] The sample appearance object to be
	 *                         created.
	 * @param userInfo         [UserInfo] The user details including site
	 *                         information for validation.
	 * @return ResponseEntity containing the created sample appearance or a conflict
	 *         response if the name exists.
	 * @throws Exception If any error occurs during the creation.
	 */
	@Override
	public ResponseEntity<Object> createSampleAppearance(final Map<String, Object> inputMap,
			final SampleAppearance sampleAppearance, final UserInfo UserInfo) throws Exception {

		final SampleAppearance SampleAppearanceByName = getSampleAppearanceByName(
				sampleAppearance.getSsampleappearance(),UserInfo.getNmastersitecode());
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedAppearanceList = new ArrayList<>();
		if (SampleAppearanceByName == null) {
			if (sampleAppearance.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final SampleAppearance defaultSampleAppearance = getSampleAppearanceByDefaultStatus(
						sampleAppearance.getNsitecode());
				if (defaultSampleAppearance != null) {
					final SampleAppearance SampleAppearanceBeforeSave = new SampleAppearance();
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(SampleAppearanceBeforeSave);
					defaultSampleAppearance
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					final String updateQueryString = " update sampleappearance set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nsampleappearancecode ="
							+ defaultSampleAppearance.getNsampleappearancecode();
					jdbcTemplate.execute(updateQueryString);
					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultSampleAppearance);
					multilingualIDList.add("IDS_EDITSAMPLEAPPEARANCE");
					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, UserInfo);
					multilingualIDList.clear();
				}
			}
			int nSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from SeqNoProductManagement where stablename='SampleAppearance'",
					Integer.class);
			nSeqNo++;
			final String sdesc = sampleAppearance.getSdescription() == null ? "" : sampleAppearance.getSdescription();
			final String sampleappInsert = "insert into sampleappearance(nsampleappearancecode,ssampleappearance,sdescription,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
					+ " values(" + nSeqNo + ",N'"
					+ stringUtilityFunction.replaceQuote(sampleAppearance.getSsampleappearance()) + "',N'"
					+ stringUtilityFunction.replaceQuote(sdesc) + "'," + sampleAppearance.getNdefaultstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(UserInfo) + "'," + UserInfo.getNmastersitecode() + ","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
			jdbcTemplate.execute(sampleappInsert);
			jdbcTemplate.execute("update SeqNoProductManagement set nsequenceno = " + nSeqNo
					+ " where stablename='SampleAppearance'");
			sampleAppearance.setNsampleappearancecode(nSeqNo);
			savedAppearanceList.add(sampleAppearance);
			multilingualIDList.add("IDS_ADDSAMPLEAPPEARANCE");
			auditUtilityFunction.fnInsertAuditAction(savedAppearanceList, 1, null, multilingualIDList, UserInfo);
			return getSampleAppearance(UserInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							UserInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * Retrieves an active sample appearance by its unique ID.
	 * 
	 * @param nsampleappearancecode [int] The ID of the sample appearance to
	 *                              retrieve.
	 * @param userInfo              [UserInfo] The user details to verify access.
	 * @return The SampleAppearance object matching the ID.
	 * @throws Exception If any error occurs during the retrieval.
	 */
	@Override
	public SampleAppearance getActiveSampleAppearanceById(final int nsampleappearancecode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select s.nsampleappearancecode,s.ssampleappearance,s.sdescription,s.ndefaultstatus,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
				+ " from SampleAppearance s,transactionstatus ts " + " where s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode = s.ndefaultstatus"
				+ " and s.nsampleappearancecode = " + nsampleappearancecode + " and s.nsitecode ="
				+ userInfo.getNmastersitecode();
		return (SampleAppearance) jdbcUtilityFunction.queryForObject(strQuery, SampleAppearance.class, jdbcTemplate);
	}

	/**
	 * Updates an existing sample appearance in the database.
	 * 
	 * @param objSampleAppearance [SampleAppearance] The updated sample appearance
	 *                            data.
	 * @param userInfo            [UserInfo] The user details for auditing and
	 *                            validation.
	 * @return ResponseEntity containing the updated sample appearance or a conflict
	 *         response.
	 * @throws Exception If any error occurs during the update.
	 */
	@Override
	public ResponseEntity<Object> updateSampleAppearance(final SampleAppearance objSampleAppearance,
			final UserInfo userInfo) throws Exception {
	final SampleAppearance sampleAppearance = getActiveSampleAppearanceById(
				objSampleAppearance.getNsampleappearancecode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		if (sampleAppearance == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nsampleappearancecode from sampleappearance where ssampleappearance = '"
					+ stringUtilityFunction.replaceQuote(objSampleAppearance.getSsampleappearance())
					+ "' and nsampleappearancecode <> " + objSampleAppearance.getNsampleappearancecode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode =" + userInfo.getNmastersitecode();
			final SampleAppearance sampleApperance = (SampleAppearance) jdbcUtilityFunction.queryForObject(queryString,
					SampleAppearance.class,jdbcTemplate);
			if (sampleApperance==null) {
				if (objSampleAppearance.getNdefaultstatus() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {
					final SampleAppearance defaultSampleAppearance = getSampleAppearanceByDefaultStatus(
							objSampleAppearance.getNsitecode());
					if (defaultSampleAppearance != null && defaultSampleAppearance
							.getNsampleappearancecode() != objSampleAppearance.getNsampleappearancecode()) {
						final SampleAppearance sampleBeforeSave = SerializationUtils.clone(defaultSampleAppearance);
						listBeforeUpdate.add(sampleBeforeSave);
						defaultSampleAppearance
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						final String updateQueryString = " update sampleappearance set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus()
								+ " where nsampleappearancecode=" + defaultSampleAppearance.getNsampleappearancecode();
						jdbcTemplate.execute(updateQueryString);
						listAfterUpdate.add(defaultSampleAppearance);
					}
				}
				final String updateQueryString = "update SampleAppearance set ssampleappearance=N'"
						+ stringUtilityFunction.replaceQuote(objSampleAppearance.getSsampleappearance())
						+ "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(objSampleAppearance.getSdescription())
						+ "', ndefaultstatus=" + objSampleAppearance.getNdefaultstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nsampleappearancecode="
						+ objSampleAppearance.getNsampleappearancecode() + ";";
				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(objSampleAppearance);
				listBeforeUpdate.add(sampleAppearance);
				multilingualIDList.add("IDS_EDITSAMPLEAPPEARANCE");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getSampleAppearance(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * Deletes a sample appearance by its unique ID.
	 * 
	 * @param nsampleappearancecode [int] The ID of the sample appearance to delete.
	 * @param userInfo              [UserInfo] The user details for auditing and
	 *                              validation.
	 * @return ResponseEntity containing the result of the deletion operation.
	 * @throws Exception If any error occurs during the deletion.
	 */
	@Override
	public ResponseEntity<Object> deleteSampleAppearance(final SampleAppearance objsample, final UserInfo userInfo)
			throws Exception {
		final SampleAppearance sampleappearance = getActiveSampleAppearanceById(objsample.getNsampleappearancecode(),
				userInfo);
		if (sampleappearance == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String strquery = "select 'IDS_SUBSAMPLEREGISTRATION' as Msg from externalordersample where nsampleappearancecode= "
					+ objsample.getNsampleappearancecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			validatorDel = projectDAOSupport.getTransactionInfo(strquery, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objsample.getNsampleappearancecode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedSampleList = new ArrayList<>();
				final String updateQueryString = "update sampleappearance set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsampleappearancecode="
						+ objsample.getNsampleappearancecode();
				jdbcTemplate.execute(updateQueryString);
				objsample.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedSampleList.add(objsample);
				multilingualIDList.add("IDS_DELETESAMPLEAPPEARANCE");
				auditUtilityFunction.fnInsertAuditAction(deletedSampleList, 1, null, multilingualIDList, userInfo);
				return getSampleAppearance(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
