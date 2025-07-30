package com.agaramtech.qualis.compentencemanagement.service.trainingcategory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.compentencemanagement.model.TrainingCategory;
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
public class TrainingCategoryDAOImpl implements TrainingCategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrainingCategoryDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getTrainingCategory(final UserInfo userInfo) throws Exception {
		final String strQuery = "select t.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus from trainingcategory t,transactionstatus ts"
				+ " where t.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and t.ntrainingcategorycode>0 and ts.ntranscode=t.ndefaultstatus and t.nsitecode="
				+ userInfo.getNmastersitecode();
		LOGGER.info("getTrainingCategory-->" + strQuery);

		// List<String> lstcolumns = new ArrayList<>();
		// lstcolumns.add("sdisplaystatus");

		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new TrainingCategory()), HttpStatus.OK);
	}

	public ResponseEntity<Object> createTrainingCategory(final TrainingCategory objTrainingCategory,
			final UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table trainingcategory "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final TrainingCategory traningCategoryByName = getTraningCategoryByName(
				objTrainingCategory.getStrainingcategoryname(), objTrainingCategory.getNsitecode());

		if (traningCategoryByName == null) {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedProductList = new ArrayList<>();

			if (objTrainingCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final TrainingCategory defaultTraningCategory = getTrainingCategoryByDefaultStatus(
						objTrainingCategory.getNsitecode());

				if (defaultTraningCategory != null) {

					final TrainingCategory TraningCategoryBeforeSave = SerializationUtils.clone(defaultTraningCategory);

					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(TraningCategoryBeforeSave);

					defaultTraningCategory
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

					final String updateQueryString = " update trainingcategory set ndefaultstatus="
													+ Enumeration.TransactionStatus.NO.gettransactionstatus()
													+ " where ntrainingcategorycode = " + defaultTraningCategory.getNtrainingcategorycode()
													+ "and nsitecode=" + userInfo.getNmastersitecode();
					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultTraningCategory);

					multilingualIDList.add("IDS_EDITTRAININGCATEGORY");

					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
					multilingualIDList.clear();
				}
			}

			final String sGetSeqNoQuery = "select nsequenceno from seqnocompetencemanagement where stablename = 'trainingcategory';";
			int nSeqNo = jdbcTemplate.queryForObject(sGetSeqNoQuery, Integer.class);
			objTrainingCategory.setNtrainingcategorycode(nSeqNo++);

			final String sInsertQuery = "insert into trainingcategory (ntrainingcategorycode, strainingcategoryname, sdescription, ndefaultstatus,dmodifieddate, nsitecode, nstatus) "
										+ "values (" + nSeqNo + ", N'"
										+ stringUtilityFunction.replaceQuote(objTrainingCategory.getStrainingcategoryname()) + "', N'"
										+ stringUtilityFunction.replaceQuote(objTrainingCategory.getSdescription()) + "', "
										+ objTrainingCategory.getNdefaultstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
										+ " '," + userInfo.getNmastersitecode() + ", "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			jdbcTemplate.execute(sInsertQuery);

			final String sUpdateSeqNoQuery = "update seqnocompetencemanagement set nsequenceno = " + nSeqNo
					+ " where stablename = 'trainingcategory';";
			jdbcTemplate.execute(sUpdateSeqNoQuery);
			objTrainingCategory.setNtrainingcategorycode(nSeqNo);

			savedProductList.add(objTrainingCategory);
			multilingualIDList.add("IDS_ADDTRAININGCATEGORY");

			auditUtilityFunction.fnInsertAuditAction(savedProductList, 1, null, multilingualIDList, userInfo);

			return getTrainingCategory(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
	}

	private TrainingCategory getTrainingCategoryByDefaultStatus(int nsitecode) throws Exception {
		final String strQuery = "select * from trainingcategory t" + " where t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and t.nsitecode = " + nsitecode;
		return (TrainingCategory) jdbcUtilityFunction.queryForObject(strQuery, TrainingCategory.class, jdbcTemplate);
	}

	private TrainingCategory getTraningCategoryByName(String strainingcategoryname, int nsitecode)
			throws Exception {
		final String strQuery = "select ntrainingcategorycode from trainingcategory where strainingcategoryname = N'"
									+ stringUtilityFunction.replaceQuote(strainingcategoryname) + "' and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nsitecode;
		return (TrainingCategory) jdbcUtilityFunction.queryForObject( strQuery, TrainingCategory.class, jdbcTemplate);
	}

	public TrainingCategory getActiveTrainingCategoryById(int ntrainingcategorycode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select t.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "',"
								+ "ts.jsondata->'stransdisplaystatus'->>'en-US')  as sdisplaystatus from trainingcategory t,transactionstatus ts "
								+ " where t.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ts.ntranscode = t.ndefaultstatus" + " and t.ntrainingcategorycode = " + ntrainingcategorycode;
		return (TrainingCategory) jdbcUtilityFunction.queryForObject(strQuery, TrainingCategory.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateTrainingCategory(final TrainingCategory objTrainingCategory,
			final UserInfo userInfo) throws Exception {

		final TrainingCategory TrainingCategory = getActiveTrainingCategoryById(
				objTrainingCategory.getNtrainingcategorycode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();

		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		if (TrainingCategory == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ntrainingcategorycode from trainingcategory where strainingcategoryname = '"
										+ stringUtilityFunction.replaceQuote(objTrainingCategory.getStrainingcategoryname())
										+ "' and ntrainingcategorycode <> " + objTrainingCategory.getNtrainingcategorycode()
										+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and nsitecode=" + userInfo.getNmastersitecode();

			final List<TrainingCategory> TrainingCategoryList = (List<TrainingCategory>) jdbcTemplate.query(queryString,
					new TrainingCategory());

			if (TrainingCategoryList.isEmpty()) {
				if (objTrainingCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {

					final TrainingCategory defaultTrainingCategory = getTrainingCategoryByDefaultStatus(
							objTrainingCategory.getNsitecode());

					if (defaultTrainingCategory != null && defaultTrainingCategory
							.getNtrainingcategorycode() != objTrainingCategory.getNtrainingcategorycode()) {

						final TrainingCategory TraningCategoryBeforeSave = SerializationUtils
								.clone(defaultTrainingCategory);
						listBeforeUpdate.add(TraningCategoryBeforeSave);

						defaultTrainingCategory
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = " update trainingcategory set ndefaultstatus="
															+ Enumeration.TransactionStatus.NO.gettransactionstatus()
															+ " where ntrainingcategorycode=" + defaultTrainingCategory.getNtrainingcategorycode();
						jdbcTemplate.execute(updateQueryString);

						listAfterUpdate.add(defaultTrainingCategory);

					}

				}
				final String updateQueryString = "update trainingcategory set strainingcategoryname=N'"
													+ stringUtilityFunction.replaceQuote(objTrainingCategory.getStrainingcategoryname())
													+ "', sdescription ='"
													+ stringUtilityFunction.replaceQuote(objTrainingCategory.getSdescription())
													+ "', ndefaultstatus=" + objTrainingCategory.getNdefaultstatus() + ",dmodifieddate='"
													+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ntrainingcategorycode="
													+ objTrainingCategory.getNtrainingcategorycode() + ";";

				jdbcTemplate.execute(updateQueryString);

				listAfterUpdate.add(objTrainingCategory);
				listBeforeUpdate.add(TrainingCategory);

				multilingualIDList.add("IDS_EDITTRAININGCATEGORY");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return getTrainingCategory(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteTrainingCategory(final TrainingCategory objTrainingCategory,
			final UserInfo userInfo) throws Exception {
		final TrainingCategory TrainingCategory = getActiveTrainingCategoryById(
				objTrainingCategory.getNtrainingcategorycode(), userInfo);

		if (TrainingCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = " select 'IDS_TRAININGANDCERTIFICATION' as Msg from trainingcertification where ntrainingcategorycode= "
					+ objTrainingCategory.getNtrainingcategorycode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(objTrainingCategory.getNtrainingcategorycode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedTrainingCategoryList = new ArrayList<>();
				final String updateQueryString = "update trainingcategory set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ntrainingcategorycode="
						+ objTrainingCategory.getNtrainingcategorycode();

				jdbcTemplate.execute(updateQueryString);
				objTrainingCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedTrainingCategoryList.add(objTrainingCategory);
				multilingualIDList.add("IDS_DELETETRAININGCATEGORY");
				auditUtilityFunction.fnInsertAuditAction(deletedTrainingCategoryList, 1, null, multilingualIDList,
						userInfo);
				return getTrainingCategory(userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

}
