package com.agaramtech.qualis.testmanagement.service.testprice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.testmanagement.model.TestPriceDetail;
import com.agaramtech.qualis.testmanagement.model.TestPriceVersion;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TestPriceVersionDAOImpl implements TestPriceVersionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestPriceVersionDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
//	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getTestPriceVersion(Integer npriceVersionCode, final UserInfo userInfo)
			throws Exception {
		LOGGER.info("getTestPriceVersion");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		TestPriceVersion selectedVersion = null;
		if (npriceVersionCode == null) {
			final String query = "select tpv.npriceversioncode, tpv.sversionname, tpv.sdescription, "
					+ " case when  tpv.nversionno = " + Enumeration.TransactionStatus.NA.gettransactionstatus()
					+ " then '-' else cast(tpv.nversionno as character varying(10)) end sversionno, tpv.ntransactionstatus, "
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ " ts.jsondata->'stransdisplaystatus'->>'en-US') sversionstatus from "
					+ " testpriceversion tpv, transactionstatus ts where tpv.ntransactionstatus=ts.ntranscode "
					+ " and tpv.nstatus=ts.nstatus and tpv.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tpv.npriceversioncode > 0 and tpv.nsitecode=" + userInfo.getNmastersitecode()
					+ " order by tpv.npriceversioncode desc";

			final List<TestPriceVersion> versionList = jdbcTemplate.query(query, new TestPriceVersion());
			if (versionList.isEmpty()) {
				outputMap.put("TestPriceVersion", versionList);
				outputMap.put("SelectedTestPriceVersion", null);
				outputMap.put("TestPrice", null);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				outputMap.put("TestPriceVersion", versionList);
				selectedVersion = versionList.get(0);
				npriceVersionCode = selectedVersion.getNpriceversioncode();
			}
		} else {
			selectedVersion = getActiveTestPriceVersionById(npriceVersionCode, userInfo);
		}
		if (selectedVersion == null) {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedTestPriceVersion", selectedVersion);

			final String queryStr = "select tpv.npriceversioncode, tpv.sversionname, tpv.sdescription, "
					+ " case when  tpv.nversionno = " + Enumeration.TransactionStatus.NA.gettransactionstatus()
					+ " then '-' else cast(tpv.nversionno as character varying(10)) end sversionno, tpv.ntransactionstatus, "
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ " ts.jsondata->'stransdisplaystatus'->>'en-US') sversionstatus from "
					+ " testpriceversion tpv, transactionstatus ts where  tpv.ntransactionstatus=ts.ntranscode "
					+ " and tpv.nstatus=ts.nstatus and tpv.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tpv.npriceversioncode > 0 and tpv.nsitecode=" + userInfo.getNmastersitecode()
					+ " order by tpv.npriceversioncode desc";

			final List<TestPriceVersion> versionList = jdbcTemplate.query(queryStr, new TestPriceVersion());

			outputMap.put("TestPriceVersion", versionList);

			final String priceListQuery = "select tp.ntestpricedetailcode,tp.npriceversioncode,"
					+ " tp.ntestcode, tp.ncost,tm.stestname from testpricedetail tp, testmaster tm "
					+ " where tm.ntestcode=tp.ntestcode and  tp.npriceversioncode ="
					+ selectedVersion.getNpriceversioncode() + " and tp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<TestPriceDetail> priceList = jdbcTemplate.query(priceListQuery, new TestPriceDetail());
			outputMap.put("TestPrice", priceList);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	public TestPriceVersion getActiveTestPriceVersionById(final int npriceVersionCode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select tpv.npriceversioncode, tpv.sversionname, tpv.sdescription, "
				+ " case when  tpv.nversionno = " + Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " then '-' else cast(tpv.nversionno as character varying(10)) end sversionno, tpv.ntransactionstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') sversionstatus from "
				+ " testpriceversion tpv, transactionstatus ts where  tpv.ntransactionstatus=ts.ntranscode "
				+ " and tpv.nstatus=ts.nstatus and tpv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tpv.npriceversioncode ="
				+ npriceVersionCode;
		return (TestPriceVersion) jdbcUtilityFunction.queryForObject(strQuery, TestPriceVersion.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> createTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception {
		final String queryString = "select * from testpriceversion where sversionname =N'"
				+ stringUtilityFunction.replaceQuote(testPriceVersion.getSversionname()) + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();

		final TestPriceVersion testPrice = (TestPriceVersion) jdbcUtilityFunction.queryForObject(queryString,
				TestPriceVersion.class, jdbcTemplate);
		if (testPrice == null) {

			String sQuery = "select nsequenceno, stablename from seqnotestmanagement  where stablename in ('testpriceversion', 'testpricedetail') and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

//			final List<?> lstMultiSeqNo = (List<?>) projectDAOSupport.getMutlipleEntityResultsUsingPlainSql(sQuery,
//					jdbcTemplate, SeqNoTestManagement.class);
//
//			final List<SeqNoTestManagement> lstSeqNo = (List<SeqNoTestManagement>) lstMultiSeqNo.get(0);

//			final Map<String, Object> seqNoMap = lstSeqNo.stream().collect(Collectors.toMap(
//					SeqNoTestManagement::getStablename, seqNoTestManagement -> seqNoTestManagement.getNsequenceno()));

			final List<Map<String, Object>> lstSeqNo = jdbcTemplate.queryForList(sQuery);

			final Map<String, Object> seqNoMap = lstSeqNo.stream()
					.collect(Collectors.toMap(row -> (String) row.get("stablename"), row -> row.get("nsequenceno")));

			final int priceVersionSeqNo = (int) seqNoMap.get("testpriceversion") + 1;
			int priceSeqNo = (int) seqNoMap.get("testpricedetail") + 1;

			sQuery = "insert into testpriceversion (npriceversioncode,sversionname,sdescription,nversionno,ntransactionstatus,dmodifieddate,nsitecode, nstatus) "
					+ " values (" + priceVersionSeqNo + ",N'"
					+ stringUtilityFunction.replaceQuote(testPriceVersion.getSversionname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(testPriceVersion.getSdescription()) + "'," + "-1,"
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + "," + testPriceVersion.getNsitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			jdbcTemplate.execute(sQuery);

			testPriceVersion.setNpriceversioncode(priceVersionSeqNo);
			testPriceVersion.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());

			sQuery = "update seqnotestmanagement set nsequenceno=" + priceVersionSeqNo
					+ " where stablename='testpriceversion'";

			jdbcTemplate.execute(sQuery);

			final List<Object> savedList = new ArrayList<>();
			savedList.add(Arrays.asList(testPriceVersion));

			final List<String> multiLingualIDList = new ArrayList<String>();
			multiLingualIDList.add("IDS_ADDTESTPRICEVERSION");

			final String testQuery = "select ntestcode, ncost from testmaster where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode >"
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + " order by ntestcode asc ";
			final List<TestMaster> testList = jdbcTemplate.query(testQuery, new TestMaster());

			final int testCount = priceSeqNo + testList.size() - 1;

			final List<String> queryList = new ArrayList<>();

			for (final TestMaster test : testList) {
				queryList.add("(" + priceSeqNo++ + "," + testPriceVersion.getNpriceversioncode() + ","
						+ test.getNtestcode() + "," + test.getNcost() + "," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + "," + userInfo.getNmastersitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
			}

			if (queryList.size() > 0) {

				final String priceInsertQuery = " insert into testpricedetail (ntestpricedetailcode,npriceversioncode,ntestcode,ncost,dmodifieddate,nsitecode, nstatus) values "
						+ String.join(",", queryList) + "; update seqnotestmanagement set nsequenceno=" + testCount
						+ " where stablename='testpricedetail';";
				jdbcTemplate.execute(priceInsertQuery);

				final String testPriceQuery = "select * from testpricedetail where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and npriceversioncode = "
						+ testPriceVersion.getNpriceversioncode();
				final List<TestPriceDetail> addedTestPriceList = jdbcTemplate.query(testPriceQuery,
						new TestPriceDetail());

				savedList.add(addedTestPriceList);
				multiLingualIDList.add("IDS_ADDTESTPRICE");

			}

			auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null, multiLingualIDList, userInfo);

			return getTestPriceVersion(null, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> updateTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception {
		final TestPriceVersion versionById = getActiveTestPriceVersionById(testPriceVersion.getNpriceversioncode(),
				userInfo);
		if (versionById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (versionById.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				final String queryString = "select * from testpriceversion where sversionname =N'"
						+ stringUtilityFunction.replaceQuote(testPriceVersion.getSversionname()) + "' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and npriceversioncode != "
						+ testPriceVersion.getNpriceversioncode() + " and nsitecode=" + userInfo.getNmastersitecode();

				final TestPriceVersion testPrice = (TestPriceVersion) jdbcUtilityFunction.queryForObject(queryString,
						TestPriceVersion.class, jdbcTemplate);
				if (testPrice == null) {

					final String updateQuery = "update testpriceversion set sversionname =N'"
							+ stringUtilityFunction.replaceQuote(testPriceVersion.getSversionname())
							+ "', sdescription=N'"
							+ stringUtilityFunction.replaceQuote(testPriceVersion.getSdescription())
							+ "', dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "'  where npriceversioncode = " + testPriceVersion.getNpriceversioncode();

					jdbcTemplate.execute(updateQuery);

					auditUtilityFunction.fnInsertAuditAction(Arrays.asList(testPriceVersion), 2,
							Arrays.asList(versionById), Arrays.asList("IDS_EDITTESTPRICEVERSION"), userInfo);

					return getTestPriceVersion(testPriceVersion.getNpriceversioncode(), userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTTESTPRICEVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception {
		final TestPriceVersion versionById = getActiveTestPriceVersionById(testPriceVersion.getNpriceversioncode(),
				userInfo);

		if (versionById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (versionById.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				String updateQuery = "update testpriceversion set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where npriceversioncode = "
						+ testPriceVersion.getNpriceversioncode();
				jdbcTemplate.execute(updateQuery);

				testPriceVersion.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				final String priceListQuery = "select * from testpricedetail where npriceversioncode="
						+ testPriceVersion.getNpriceversioncode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<TestPriceDetail> priceList = jdbcTemplate.query(priceListQuery, new TestPriceDetail());

				updateQuery = "update testpricedetail set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where npriceversioncode="
						+ testPriceVersion.getNpriceversioncode();
				jdbcTemplate.execute(updateQuery);

				auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(priceList, Arrays.asList(testPriceVersion)),
						1, null, Arrays.asList("IDS_DELETETESTPRICE", "IDS_DELETETESTPRICEVERSION"), userInfo);

				return getTestPriceVersion(null, userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTTESTPRICEVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> copyTestPriceVersion(final TestPriceVersion testPriceVersion, final UserInfo userInfo)
			throws Exception {
		final TestPriceVersion versionById = getActiveTestPriceVersionById(testPriceVersion.getNpriceversioncode(),
				userInfo);
		final int npriceversioncode = testPriceVersion.getNpriceversioncode();
		if (versionById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select * from testpriceversion where sversionname =N'"
					+ stringUtilityFunction.replaceQuote(testPriceVersion.getSversionname()) + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			final TestPriceVersion testPrice = (TestPriceVersion) jdbcUtilityFunction.queryForObject(queryString,
					TestPriceVersion.class, jdbcTemplate);
			if (testPrice == null) {

				String sQuery = "select nsequenceno, stablename from seqnotestmanagement  where stablename in ('testpriceversion', 'testpricedetail') and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

//				final List<?> lstMultiSeqNo = (List<?>) projectDAOSupport.getMutlipleEntityResultsUsingPlainSql(sQuery,
//						jdbcTemplate, SeqNoTestManagement.class);
//
//				final List<SeqNoTestManagement> lstSeqNo = (List<SeqNoTestManagement>) lstMultiSeqNo.get(0);
//
//				final Map<String, Object> seqNoMap = lstSeqNo.stream()
//						.collect(Collectors.toMap(SeqNoTestManagement::getStablename,
//								seqNoTestManagement -> seqNoTestManagement.getNsequenceno()));

				final List<Map<String, Object>> lstSeqNo = jdbcTemplate.queryForList(sQuery);

				final Map<String, Object> seqNoMap = lstSeqNo.stream().collect(
						Collectors.toMap(row -> (String) row.get("stablename"), row -> row.get("nsequenceno")));

				final int priceVersionSeqNo = (int) seqNoMap.get("testpriceversion") + 1;
				int priceSeqNo = (int) seqNoMap.get("testpricedetail") + 1;

				sQuery = "insert into testpriceversion (npriceversioncode,sversionname,sdescription,nversionno,ntransactionstatus,dmodifieddate,nsitecode, nstatus) "
						+ " values (" + priceVersionSeqNo + ",N'"
						+ stringUtilityFunction.replaceQuote(testPriceVersion.getSversionname()) + "',N'"
						+ stringUtilityFunction.replaceQuote(testPriceVersion.getSdescription()) + "'," + "-1,"
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + "," + testPriceVersion.getNsitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				jdbcTemplate.execute(sQuery);

				testPriceVersion.setNpriceversioncode(priceVersionSeqNo);
				testPriceVersion
						.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());

				sQuery = "update seqnotestmanagement set nsequenceno=" + priceVersionSeqNo
						+ " where stablename='testpriceversion'";

				jdbcTemplate.execute(sQuery);

				final List<Object> savedList = new ArrayList<>();
				savedList.add(Arrays.asList(testPriceVersion));

				final List<String> multiLingualIDList = new ArrayList<String>();
				multiLingualIDList.add("IDS_COPYTESTPRICEVERSION");

				final String testQuery = "select t.ntestcode, tp.ncost from testmaster t,testpricedetail tp where t.ntestcode=tp.ntestcode and "
						+ " npriceversioncode=" + npriceversioncode + " and t.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and t.ntestcode >"
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " order by t.ntestcode asc ";

				final List<TestMaster> testList = jdbcTemplate.query(testQuery, new TestMaster());

				final int testCount = priceSeqNo + testList.size() - 1;

				final List<String> queryList = new ArrayList<>();

				for (final TestMaster test : testList) {
					queryList.add("(" + priceSeqNo++ + "," + testPriceVersion.getNpriceversioncode() + ","
							+ test.getNtestcode() + "," + test.getNcost() + "," + "'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ","
							+ userInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
				}

				if (queryList.size() > 0) {

					final String priceInsertQuery = " insert into testpricedetail (ntestpricedetailcode,npriceversioncode,ntestcode,ncost,dmodifieddate,nsitecode, nstatus) values "
							+ String.join(",", queryList) + "; update seqnotestmanagement set nsequenceno=" + testCount
							+ " where stablename='testpricedetail';";
					jdbcTemplate.execute(priceInsertQuery);

					final String testPriceQuery = "select * from testpricedetail where nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and npriceversioncode = "
							+ testPriceVersion.getNpriceversioncode();
					final List<TestPriceDetail> addedTestPriceList = jdbcTemplate.query(testPriceQuery,
							new TestPriceDetail());

					savedList.add(addedTestPriceList);
					multiLingualIDList.add("IDS_COPYTESTPRICE");

				}

				auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null, multiLingualIDList, userInfo);

				return getTestPriceVersion(null, userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

//			
//			jdbcTemplate.execute("update seqnotestmanagement set nsequenceno = " +  priceVersionSeqNo 
//										+ " where stablename = 'testpriceversion'");

//			final String priceQuery = "insert into testpricedetail (ntestpricedetailcode,npriceversioncode,ntestcode,ncost,dmodifieddate,nsitecode, nstatus) "
//									+ " (select  "+priceSeqNo+"+rank()over(order by ntestpricedetailcode) ntestpricedetailcode," 
//									//+ priceVersionSeqNo+"+rank()over(order by npriceversioncode) npriceversioncode,"
//									+ priceVersionSeqNo  + " npriceversioncode, "
//									+ " ntestcode,ncost,"
//									+ "'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' dmodifieddate,"
//									+ testPriceVersion.getNsitecode() + " nsitecode ," +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//									+ " nstatus "
//									+ " from testpricedetail where npriceversioncode = "+versionById.getNpriceversioncode()
//									+ " and nstatus=" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//									+")";
//				
//			jdbcTemplate.execute(priceQuery);
//			
//			jdbcTemplate.execute("update seqnotestmanagement set nsequenceno = " +  priceSeqUpdate 
//										+ " where stablename = 'testpricedetail'");

//			final String versionComments = commonFunction.getMultilingualMessage("IDS_FROM", 
//										userInfo.getSlanguagefilename())  + ":"+ versionById.getSversionname() + "  "
//										+ commonFunction.getMultilingualMessage("IDS_TO", userInfo.getSlanguagefilename())
//										+ ":" + testPriceVersion.getSversionname()
//										+ ";" + commonFunction.getMultilingualMessage("IDS_DESCRIPTION", userInfo.getSlanguagefilename())
//										+ ":"+testPriceVersion.getSdescription();
//			insertAuditAction(userInfo, "IDS_COPYTESTPRICEVERSION", versionComments, 
//								new HashMap<String, Object>(){{put("sprimarykeyvalue", priceVersionSeqNo);}}
//								);

//			final String priceComments = commonFunction.getMultilingualMessage("IDS_TESTPRICINGCOPIED", userInfo.getSlanguagefilename())
//					+ " "+
//					commonFunction.getMultilingualMessage("IDS_FROM", 
//					userInfo.getSlanguagefilename())  + ":"+ versionById.getSversionname() + " "
//					+ commonFunction.getMultilingualMessage("IDS_TO", userInfo.getSlanguagefilename())
//					+ ":" + testPriceVersion.getSversionname();
//			insertAuditAction(userInfo, "IDS_COPYTESTPRICE", priceComments, 
//					new HashMap<String, Object>(){{put("sprimarykeyvalue", priceSeqNo);}}
//					);
//			return getTestPriceVersion(null, userInfo);

//auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null, multiLingualIDList, userInfo);

		}
	}

	@Override
	public ResponseEntity<Object> approveTestPriceVersion(final TestPriceVersion testPriceVersion,
			final UserInfo userInfo) throws Exception {
		final TestPriceVersion versionById = getActiveTestPriceVersionById(testPriceVersion.getNpriceversioncode(),
				userInfo);
		if (versionById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			if (versionById.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				final String priceListQuery = "select * from testpricedetail where npriceversioncode="
						+ testPriceVersion.getNpriceversioncode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode();
				final List<TestPriceDetail> priceList = jdbcTemplate.query(priceListQuery, new TestPriceDetail());

				if (priceList.isEmpty()) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_ADDTEST", userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					final String approvedVersionQuery = " select * from testpriceversion where ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNmastersitecode();
					final TestPriceVersion versiontoRetire = (TestPriceVersion) jdbcUtilityFunction
							.queryForObject(approvedVersionQuery, TestPriceVersion.class, jdbcTemplate);

					final List<Object> auditList = new ArrayList<Object>();
					final List<String> multilingualIDList = new ArrayList<String>();

					short versionNo = 1;
					if (versiontoRetire != null) {
						final String retireQuery = "update testpriceversion set ntransactionstatus="
								+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  where npriceversioncode="
								+ versiontoRetire.getNpriceversioncode();
						jdbcTemplate.execute(retireQuery);

						versiontoRetire.setNtransactionstatus(
								(short) Enumeration.TransactionStatus.RETIRED.gettransactionstatus());
						auditList.add(Arrays.asList(versiontoRetire));
						multilingualIDList.add("IDS_RETIRETESTPRICEVERSION");
						versionNo = (short) (versiontoRetire.getNversionno() + 1);
					}
					final String updateQuery = "update testpriceversion set ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ",nversionno=" + versionNo
							+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "'  where npriceversioncode=" + testPriceVersion.getNpriceversioncode();
					jdbcTemplate.execute(updateQuery);
					testPriceVersion.setNtransactionstatus(
							(short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
					auditList.add(Arrays.asList(testPriceVersion));
					multilingualIDList.add("IDS_APPROVETESTPRICEVERSION");

					auditUtilityFunction.fnInsertListAuditAction(auditList, 1, null, multilingualIDList, userInfo);

				}
				return getTestPriceVersion(testPriceVersion.getNpriceversioncode(), userInfo);

			} else if (versionById.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED
					.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYAPPROVED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTTESTPRICEVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}