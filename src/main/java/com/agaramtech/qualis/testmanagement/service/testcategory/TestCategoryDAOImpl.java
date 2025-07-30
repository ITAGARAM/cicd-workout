package com.agaramtech.qualis.testmanagement.service.testcategory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.SerializationUtils;

import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.testmanagement.model.TestCategory;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "testcategory" table by
 * implementing methods from its interface.
 */

@AllArgsConstructor
@Repository
public class TestCategoryDAOImpl implements TestCategoryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCategoryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available testcategorys for the
	 * specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         testcategorys
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getTestCategory(final UserInfo userInfo) throws Exception {
		final String strQuery = "select t.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, to_char(t.dmodifieddate,'"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate,"
				+ "  coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "  ts1.jsondata->'stransdisplaystatus'->>'en-US') as sclinicalstatus from testcategory t,"
				+ "  transactionstatus ts ,transactionstatus ts1 where t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and t.ntestcategorycode>0 and ts.ntranscode=t.ndefaultstatus and ts1.ntranscode=t.nclinicaltyperequired"
				+ "  and t.nsitecode = " + userInfo.getNmastersitecode();
		LOGGER.info("Get Method:" + strQuery);
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new TestCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active testcategory object based on the
	 * specified ntestcategorycode.
	 * @param ntestcategorycode [int] primary key of testcategorys object
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         testcategorys object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public TestCategory getActiveTestCategoryById(final int ntestcategorycode) throws Exception {
		final String strQuery = "select * from testcategory t where t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t.ntestcategorycode = "
				+ ntestcategorycode;

		return (TestCategory) jdbcUtilityFunction.queryForObject(strQuery, TestCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the testcategory object for the specified
	 * testcategory name and site.
	 * @param stestcatname    [String] name of the testcategory
	 * @param nmasterSiteCode [int] site code of the testcategory
	 * @return unit object based on the specified testcategory name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table testcategory " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTestCategoryList = new ArrayList<>();
		final TestCategory testCategoryListByName = getTestCategoryByName(testCategory.getStestcategoryname(),
				testCategory.getNsitecode());
		if (testCategoryListByName == null) {
			if (testCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final TestCategory defaultTestCategory = getTestCategoryByDefaultStatus(testCategory.getNsitecode());
				if (defaultTestCategory != null) {
					final TestCategory testCategoryBeforeSave = SerializationUtils.clone(defaultTestCategory);
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(testCategoryBeforeSave);
					defaultTestCategory
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					final String updateQueryString = " update testcategory set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where ntestcategorycode ="
							+ defaultTestCategory.getNtestcategorycode();
					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultTestCategory);
					multilingualIDList.add("IDS_EDITTESTCATEGORY");
					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
					multilingualIDList.clear();
				}

			}
			final String sequencequery = "select nsequenceno from SeqNoTestManagement where stablename ='testcategory'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;
			final String insertquery = "Insert  into testcategory (ntestcategorycode,stestcategoryname,sdescription,ndefaultstatus,dmodifieddate,nsitecode,nstatus,nclinicaltyperequired) "
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(testCategory.getStestcategoryname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(testCategory.getSdescription()) + "',"
					+ testCategory.getNdefaultstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNmastersitecode() + "," + " "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
					+ testCategory.getNclinicaltyperequired() + ")";

			jdbcTemplate.execute(insertquery);

			final String updatequery = "update SeqNoTestManagement set nsequenceno=" + nsequenceno
					+ " where stablename ='testcategory'";
			jdbcTemplate.execute(updatequery);
			testCategory.setNtestcategorycode(nsequenceno);
			savedTestCategoryList.add(testCategory);
			multilingualIDList.add("IDS_ADDTESTCATEGORY");
			auditUtilityFunction.fnInsertAuditAction(savedTestCategoryList, 1, null, multilingualIDList, userInfo);
			return getTestCategory(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to get the default testcategory object with respect to
	 * the site.
	 * @param nmasterSiteCode [int] Site code
	 * @return TestCategory Object
	 * @throws Exception that are thrown from this DAO layer
	 */
	private TestCategory getTestCategoryByDefaultStatus(int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from testcategory t" + " where t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and t.nsitecode = " + nmasterSiteCode;
		return (TestCategory) jdbcUtilityFunction.queryForObject(strQuery, TestCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the unit object for the specified testcategory
	 * name and site.
	 * @param stestcategoryname [String] name of the testcategory
	 * @param nmasterSiteCode   [int] site code of the testcategory
	 * @return unit object based on the specified testcategory name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private TestCategory getTestCategoryByName(final String stestcategoryname, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select ntestcategorycode from testcategory where stestcategoryname = N'"
				+ stringUtilityFunction.replaceQuote(stestcategoryname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;

		return (TestCategory) jdbcUtilityFunction.queryForObject(strQuery, TestCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in testcategory table. Need to validate
	 * that the testcategory object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of testcategory name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default testcategory for a site
	 * @param objTestCategory [TestCategory] object holding details to be updated in
	 *                        testcategory table
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return saved testcategory object with status code 200 if saved successfully
	 *         else if the testcategory already exists, response will be returned as
	 *         'Already Exists' with status code 409 else if the testcategory to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception {

		final TestCategory objTestCategory = getActiveTestCategoryById(testCategory.getNtestcategorycode());
		final List<String> multilingualIDList = new ArrayList<>();

		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		final String Squery = "select tc.* from testmaster  tm,testcategory tc  where tm.ntestcategorycode= "
				+ testCategory.getNtestcategorycode() + " "
				+ " and tm.ntestcategorycode=tc.ntestcategorycode and tc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<TestCategory> isactive = jdbcTemplate.query(Squery, new TestCategory());

		if (isactive.size() > 0
				&& isactive.get(0).getNclinicaltyperequired() != testCategory.getNclinicaltyperequired()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TESTALREADYCONFIGURED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		} else {
			if (objTestCategory == null) {
				// status code:205
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final String queryString = "select ntestcategorycode from testcategory where stestcategoryname = N'"
						+ stringUtilityFunction.replaceQuote(testCategory.getStestcategoryname())
						+ "' and ntestcategorycode <> " + testCategory.getNtestcategorycode() + " and  nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final List<TestCategory> testCategoryList = (List<TestCategory>) jdbcTemplate.query(queryString,
						new TestCategory());

				if (testCategoryList.isEmpty()) {
					if (testCategory.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

						final TestCategory defaultTestCategory = getTestCategoryByDefaultStatus(
								testCategory.getNsitecode());

						if (defaultTestCategory != null
								&& defaultTestCategory.getNtestcategorycode() != testCategory.getNtestcategorycode()) {

							// Copy of object before update
							final TestCategory testCategoryBeforeSave = SerializationUtils.clone(defaultTestCategory);
							listBeforeUpdate.add(testCategoryBeforeSave);

							defaultTestCategory
									.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

							final String updateQueryString = " update testcategory set ndefaultstatus="
									+ Enumeration.TransactionStatus.NO.gettransactionstatus()
									+ " where ntestcategorycode=" + defaultTestCategory.getNtestcategorycode();
							jdbcTemplate.execute(updateQueryString);

							listAfterUpdate.add(defaultTestCategory);
							multilingualIDList.add("IDS_EDITTESTCATEGORY");
						}

					}

					final String updateQueryString = "update testcategory set stestcategoryname=N'"
							+ stringUtilityFunction.replaceQuote(testCategory.getStestcategoryname())
							+ "', sdescription =N'" + stringUtilityFunction.replaceQuote(testCategory.getSdescription())
							+ "',ndefaultstatus = " + testCategory.getNdefaultstatus() + ",nclinicaltyperequired = "
							+ testCategory.getNclinicaltyperequired() + ",dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ntestcategorycode="
							+ testCategory.getNtestcategorycode();

					jdbcTemplate.execute(updateQueryString);

					listAfterUpdate.add(testCategory);
					listBeforeUpdate.add(objTestCategory);

					multilingualIDList.add("IDS_EDITTESTCATEGORY");

					auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
							userInfo);

					// status code:200
					return getTestCategory(userInfo);
				} else {
					// Conflict = 409 - Duplicate entry
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}
		// return outputMap;
	}

	/**
	 * This method id used to delete an entry in TestCategory table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other tables such as 'testmaster'
	 * @param objTestCategory [TestCategory] an Object holds the record to be
	 *                        deleted
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return a response entity with list of available TestCategory objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception {
		final TestCategory testCategoryByID = (TestCategory) getActiveTestCategoryById(
				testCategory.getNtestcategorycode());// .getBody();

		if (testCategoryByID == null) {

			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "Select 'IDS_TESTMASTER' as Msg from testmaster Where ntestcategorycode = "
					+ testCategory.getNtestcategorycode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(testCategory.getNtestcategorycode()), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedTestCategoryList = new ArrayList<>();
				final String updateQueryString = "update testcategory set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ntestcategorycode="
						+ testCategory.getNtestcategorycode();

				jdbcTemplate.execute(updateQueryString);

				testCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedTestCategoryList.add(testCategory);
				multilingualIDList.add("IDS_DELETETESTCATEGORY");

				auditUtilityFunction.fnInsertAuditAction(savedTestCategoryList, 1, null, multilingualIDList, userInfo);

				// status code:200
				// return new ResponseEntity<>(testCategory, HttpStatus.OK);
				return getTestCategory(userInfo);
			} else {
				// status code:417
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to retrieve list of all available testcategorys for the
	 * specified site and Test master.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         testcategorys
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getTestCategoryForTestMaster(final UserInfo objUserInfo) throws Exception {
		final String sQuery = "select ntestcategorycode,stestcategoryname,ndefaultstatus,nclinicaltyperequired from"
				+ " testcategory where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntestcategorycode not in( -1,-3) and nsitecode = " + objUserInfo.getNmastersitecode()
				+ " order by ntestcategorycode asc";
		return new ResponseEntity<>(jdbcTemplate.query(sQuery, new TestCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all available testcategorys for the
	 * specified site and Test Group.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         testcategorys
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getTestCategoryForTestGroup(final UserInfo objUserInfo, int nclinicaltyperequired)
			throws Exception {
		final String sQuery = "select ntestcategorycode,stestcategoryname,ndefaultstatus,nclinicaltyperequired from"
				+ " testcategory where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntestcategorycode <> -1 and nclinicaltyperequired=" + nclinicaltyperequired
				+ " and nsitecode = " + objUserInfo.getNmastersitecode() + " order by ntestcategorycode asc";
		return new ResponseEntity<>(jdbcTemplate.query(sQuery, new TestCategory()), HttpStatus.OK);
	}
}
