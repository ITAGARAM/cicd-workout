package com.agaramtech.qualis.basemaster.service.samplingpointcategory;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.SamplingPointCategory;
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
 * This class is used to perform CRUD Operation on "samplingpointcategory" table
 * by implementing methods from its interface.
 * 
 * @author AT-E143
 * @version 10.0.0.2
 * @since 06- February- 2025
 */
@AllArgsConstructor
@Repository
public class SamplingPointCategoryDAOImpl implements SamplingPointCategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamplingPointCategoryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final ProjectDAOSupport projectDAOSupport;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active samplingpointcategory for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         samplingpointcategory
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getSamplingPointCategory(final UserInfo userInfo) throws Exception {
		final String strQuery = "select spc.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, " + "to_char(spc.dmodifieddate, '"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate "
				+ "from samplingpointcategory spc " + "join transactionstatus ts on ts.ntranscode=spc.ndefaultstatus "
				+ "where spc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and spc.nsamplingpointcatcode > 0   and spc.nsitecode=" + userInfo.getNmastersitecode();
		LOGGER.info("getSamplingPointCategory -> " + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SamplingPointCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active samplingpointcategory object based on
	 * the specified nsamplingPointCatCode.
	 * 
	 * @param nsamplingPointCatCode [int] primary key of samplingpointcategory
	 *                              object
	 * @return response entity object holding response status and data of
	 *         samplingpointcategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public SamplingPointCategory getActiveSamplingPointCategoryById(final int nsamplingPointCatCode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = "select spc.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, " + "to_char(spc.dmodifieddate, '"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate "
				+ "from samplingpointcategory spc " + "join transactionstatus ts on ts.ntranscode=spc.ndefaultstatus "
				+ "where spc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and spc.nsamplingpointcatcode > 0 and spc.nsamplingpointcatcode=" + nsamplingPointCatCode
				+ " and spc.nsitecode=" + userInfo.getNmastersitecode();
		return (SamplingPointCategory) jdbcUtilityFunction.queryForObject(strQuery, SamplingPointCategory.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to samplingpointcategory table. Need
	 * to check for duplicate entry of samplingpointcategory name for the specified
	 * site before saving into database. Need to check that there should be only one
	 * default samplingpointcategory for a site
	 * 
	 * @param objSamplingPointCategory [SamplingPointCategory] object holding
	 *                                 details to be added in samplingpointcategory
	 *                                 table
	 * @return inserted samplingpointcategory object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> createSamplingPointCategory(final SamplingPointCategory objSamplingPointCategory,
			final UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table samplingpointcategory "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSPCList = new ArrayList<>();

		final SamplingPointCategory samplingPointCategoryByName = getSamplingPointCategoryByName(
				objSamplingPointCategory.getSsamplingpointcatname(), objSamplingPointCategory.getNsitecode());
		if (samplingPointCategoryByName == null) {
			if (objSamplingPointCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES
					.gettransactionstatus()) {
				final SamplingPointCategory defaultSamplingPointCategory = getSamplingPointCategoryByDefaultStatus(
						objSamplingPointCategory.getNsitecode());
				if (defaultSamplingPointCategory != null) {
					final SamplingPointCategory spcBeforeSave = SerializationUtils.clone(defaultSamplingPointCategory);
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(spcBeforeSave);
					defaultSamplingPointCategory
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

					final String updateQueryString = " update samplingpointcategory set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nsamplingpointcatcode ="
							+ defaultSamplingPointCategory.getNsamplingpointcatcode();
					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultSamplingPointCategory);

					multilingualIDList.add("IDS_EDITSAMPLINGPOINTCATEGORY");

					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
					multilingualIDList.clear();
				}

			}
			final String sequencenoquery = "select nsequenceno from seqnobasemaster where stablename ='samplingpointcategory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;

			final String insertquery = "Insert into samplingpointcategory (nsamplingpointcatcode,ssamplingpointcatname,sdescription,ndefaultstatus,dmodifieddate,nsitecode,nstatus) "
					+ " values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(objSamplingPointCategory.getSsamplingpointcatname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objSamplingPointCategory.getSdescription()) + "',"
					+ objSamplingPointCategory.getNdefaultstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);

			final String updatequery = "update seqnobasemaster set nsequenceno =" + nsequenceno
					+ " where stablename='samplingpointcategory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			jdbcTemplate.execute(updatequery);

			objSamplingPointCategory.setNsamplingpointcatcode(nsequenceno);
			savedSPCList.add(objSamplingPointCategory);
			multilingualIDList.add("IDS_ADDSAMPLINGPOINTCATEGORY");
			auditUtilityFunction.fnInsertAuditAction(savedSPCList, 1, null, multilingualIDList, userInfo);
			return getSamplingPointCategory(userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to update entry in samplingpointcategory table. Need to
	 * validate that the samplingpointcategory object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * samplingpointcategory name for the specified site before saving into
	 * database. Need to check that there should be only one default
	 * samplingpointcategory for a site
	 * 
	 * @param objSamplingPointCategory [SamplingPointCategory] object holding
	 *                                 details to be updated in
	 *                                 samplingpointcategory table
	 * @return response entity object holding response status and data of updated
	 *         samplingpointcategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> updateSamplingPointCategory(final SamplingPointCategory objSamplingPointCategory,
			final UserInfo userInfo) throws Exception {
		final SamplingPointCategory samplingpointcategory = getActiveSamplingPointCategoryById(
				objSamplingPointCategory.getNsamplingpointcatcode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		if (samplingpointcategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nsamplingpointcatcode from samplingpointcategory where ssamplingpointcatname = '"
					+ stringUtilityFunction.replaceQuote(objSamplingPointCategory.getSsamplingpointcatname())
					+ "' and nsamplingpointcatcode <> " + objSamplingPointCategory.getNsamplingpointcatcode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + userInfo.getNmastersitecode();

			final List<SamplingPointCategory> samplingpointcategoryList = (List<SamplingPointCategory>) jdbcTemplate
					.query(queryString, new SamplingPointCategory());

			if (samplingpointcategoryList.isEmpty()) {
				if (objSamplingPointCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {

					final SamplingPointCategory defaultSamplingPointCategory = getSamplingPointCategoryByDefaultStatus(
							objSamplingPointCategory.getNsitecode());

					if (defaultSamplingPointCategory != null && defaultSamplingPointCategory
							.getNsamplingpointcatcode() != objSamplingPointCategory.getNsamplingpointcatcode()) {

						final SamplingPointCategory spcBeforeSave = SerializationUtils
								.clone(defaultSamplingPointCategory);
						listBeforeUpdate.add(spcBeforeSave);

						defaultSamplingPointCategory
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = " update samplingpointcategory set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus()
								+ " where nsamplingpointcatcode="
								+ defaultSamplingPointCategory.getNsamplingpointcatcode();
						jdbcTemplate.execute(updateQueryString);
						listAfterUpdate.add(defaultSamplingPointCategory);
					}

				}
				final String updateQueryString = "update samplingpointcategory set ssamplingpointcatname=N'"
						+ stringUtilityFunction.replaceQuote(objSamplingPointCategory.getSsamplingpointcatname())
						+ "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(objSamplingPointCategory.getSdescription())
						+ "', ndefaultstatus=" + objSamplingPointCategory.getNdefaultstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nsamplingpointcatcode="
						+ objSamplingPointCategory.getNsamplingpointcatcode() + ";";

				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(objSamplingPointCategory);
				listBeforeUpdate.add(samplingpointcategory);
				multilingualIDList.add("IDS_EDITSAMPLINGPOINTCATEGORY");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getSamplingPointCategory(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in samplingpointcategory table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as 'samplingpoint'
	 * 
	 * @param objSamplingPointCategory [SamplingPointCategory] an Object holds the
	 *                                 record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         samplingpointcategory object
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteSamplingPointCategory(final SamplingPointCategory objSamplingPointCategory,
			final UserInfo userInfo) throws Exception {
		final SamplingPointCategory samplingpointcategory = getActiveSamplingPointCategoryById(
				objSamplingPointCategory.getNsamplingpointcatcode(), userInfo);
		if (samplingpointcategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_SAMPLINGPOINT' as Msg from samplingpoint where nsamplingpointcatcode= "
					+ objSamplingPointCategory.getNsamplingpointcatcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(objSamplingPointCategory.getNsamplingpointcatcode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedSamplingPointCatList = new ArrayList<>();
				final String updateQueryString = "update samplingpointcategory set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsamplingpointcatcode="
						+ objSamplingPointCategory.getNsamplingpointcatcode();

				jdbcTemplate.execute(updateQueryString);
				objSamplingPointCategory
						.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedSamplingPointCatList.add(objSamplingPointCategory);
				multilingualIDList.add("IDS_DELETESAMPLINGPOINTCATEGORY");
				auditUtilityFunction.fnInsertAuditAction(deletedSamplingPointCatList, 1, null, multilingualIDList,
						userInfo);
				return getSamplingPointCategory(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to get a default samplingpointcategory object with
	 * respect to the site code
	 * 
	 * @param nmasterSiteCode       [int] Site code
	 * @param nsamplingPointCatCode [int] primary key of samplingpointcategory table
	 *                              nsamplingpointcatcode
	 * @return a samplingpointcategory Object
	 * @throws Exception that are from DAO layer
	 */
	private SamplingPointCategory getSamplingPointCategoryByDefaultStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = " select * from samplingpointcategory spc where spc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and spc.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and spc.nsitecode = " + nmasterSiteCode;
		return (SamplingPointCategory) jdbcUtilityFunction.queryForObject(strQuery, SamplingPointCategory.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active samplingpointcategory objects for the
	 * specified ssamplingpointcatname name and site.
	 * 
	 * @param ssamplingPointCategoryName [String] name of the samplingpointcategory
	 * @param nmasterSiteCode            [int] site code of the
	 *                                   samplingpointcategory
	 * @return list of active samplingpoint code(s) based on the specified
	 *         samplingpointcategory name and site
	 * @throws Exception
	 */
	private SamplingPointCategory getSamplingPointCategoryByName(final String ssamplingPointCategoryName,
			final int nmasterSiteCode) throws Exception {
		final String strQuery = "select nsamplingpointcatcode from samplingpointcategory where ssamplingpointcatname = N'"
				+ stringUtilityFunction.replaceQuote(ssamplingPointCategoryName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (SamplingPointCategory) jdbcUtilityFunction.queryForObject(strQuery, SamplingPointCategory.class,
				jdbcTemplate);
	}

}
