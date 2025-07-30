package com.agaramtech.qualis.testmanagement.service.testprice;

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
public class TestPriceDAOImpl implements TestPriceDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestPriceDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final TestPriceVersionDAO testPriceVersionDAO;

	@Override
	public ResponseEntity<Object> getTestPrice(final int npriceVersionCode, final Integer ntestPriceCode,
			final UserInfo userInfo) throws Exception {
		LOGGER.info("getTestPrice");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final TestPriceVersion versionById = testPriceVersionDAO.getActiveTestPriceVersionById(npriceVersionCode,
				userInfo);

		if (versionById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		if (ntestPriceCode == null) {
			final String priceListQuery = "select tp.ntestpricedetailcode,tp.npriceversioncode,"
					+ " tp.ntestcode, tp.ncost,tm.stestname from testpricedetail tp, testmaster tm "
					+ " where tm.ntestcode=tp.ntestcode and  tp.npriceversioncode =" + npriceVersionCode
					+ " and tp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<TestPriceDetail> priceList = jdbcTemplate.query(priceListQuery, new TestPriceDetail());
			outputMap.put("TestPrice", priceList);
		} else {
			final String sQuery = "select tp.ntestpricedetailcode,tp.npriceversioncode,"
					+ " tp.ntestcode, tp.ncost,tm.stestname from testpricedetail tp, testmaster tm "
					+ " where tm.ntestcode=tp.ntestcode and  tp.ntestpricedetailcode =" + ntestPriceCode
					+ " and tp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final TestPriceDetail testPriceById = (TestPriceDetail) jdbcUtilityFunction.queryForObject(sQuery,
					TestPriceDetail.class, jdbcTemplate);

			outputMap.put("SelectedTestPrice", testPriceById);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getPriceUnmappedTest(final int npriceVersionCode, final UserInfo userInfo)
			throws Exception {
		final TestPriceVersion versionById = testPriceVersionDAO.getActiveTestPriceVersionById(npriceVersionCode,
				userInfo);
		if (versionById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final String queryString = "select tm.ntestcode,tm.stestname from testmaster tm where ntestcode  not in "
				+ " (select tp.ntestcode from testpricedetail tp where  npriceversioncode=" + npriceVersionCode
				+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
				+ " and tm.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  tm.ntestcode >-1 order by stestname";

		final List<TestMaster> testList = jdbcTemplate.query(queryString, new TestMaster());
		return new ResponseEntity<>(testList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createTestPrice(final List<TestPriceDetail> testPriceList,
			final int npriceVersionCode, final UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table testpricedetail "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final TestPriceVersion versionById = testPriceVersionDAO.getActiveTestPriceVersionById(npriceVersionCode,
				userInfo);
		if (versionById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (versionById.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				final String testCode = stringUtilityFunction.fnDynamicListToString(testPriceList, "getNtestcode");

				final String seqString = "select nsequenceno from seqnotestmanagement  where stablename ='testpricedetail' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final int nsequenceNo = jdbcTemplate.queryForObject(seqString, Integer.class);

				final int addedTestCount = (nsequenceNo) + testPriceList.size();

				String queryString = "INSERT INTO testpricedetail (ntestpricedetailcode, npriceversioncode, ntestcode, ncost,"
						+ "	dmodifieddate,nsitecode, nstatus) " + " SELECT rank() over(order by ntestcode asc)+ "
						+ nsequenceNo + " as ntestpricedetailcode, " + npriceVersionCode + " , ntestcode, ncost, " + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + versionById.getNsitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from testmaster tm 	where ntestcode in (" + testCode + ") and NOT EXISTS "
						+ " ( SELECT * FROM testpricedetail tpd WHERE tm.ntestcode = tpd.ntestcode "
						+ " and tpd.npriceversioncode=" + npriceVersionCode + " and tpd.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

				jdbcTemplate.execute(queryString);

				queryString = "update seqnotestmanagement set nsequenceno = " + addedTestCount
						+ " where stablename = 'testpricedetail'";
				jdbcTemplate.execute(queryString);

				final String insertedQuery = "select * from testpricedetail where npriceversioncode ="
						+ npriceVersionCode + " and ntestcode in(" + testCode + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

				final List<TestPriceDetail> testList = jdbcTemplate.query(insertedQuery, new TestPriceDetail());

				final List<Object> savedList = new ArrayList<>();
				savedList.add(testList);

				final List<String> multiLingualIDList = new ArrayList<String>();
				multiLingualIDList.add("IDS_ADDTESTPRICE");

				auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null, multiLingualIDList, userInfo);

				return getTestPrice(npriceVersionCode, null, userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTTESTPRICEVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteTestPrice(final TestPriceDetail testPrice, final int npriceVersionCode,
			final UserInfo userInfo) throws Exception {
		final TestPriceVersion versionById = testPriceVersionDAO.getActiveTestPriceVersionById(npriceVersionCode,
				userInfo);

		if (versionById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (versionById.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				final List<String> multilingualIDList = new ArrayList<String>();
				final List<Object> savedList = new ArrayList<>();

				String sQuery = "select * from testpricedetail where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestpricedetailcode = "
						+ testPrice.getNtestpricedetailcode();
				final TestPriceDetail testPriceById = (TestPriceDetail) jdbcUtilityFunction.queryForObject(sQuery,
						TestPriceDetail.class, jdbcTemplate);

				if (testPriceById == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				} else {
					sQuery = "update testpricedetail set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ntestpricedetailcode = "
							+ testPrice.getNtestpricedetailcode();
					jdbcTemplate.execute(sQuery);

					testPrice.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

					savedList.add(testPrice);
					multilingualIDList.add("IDS_DELETETESTPRICE");
					auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);

					return getTestPrice(npriceVersionCode, null, userInfo);
				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTTESTPRICEVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> updateTestPrice(final List<TestPriceDetail> testPriceList,
			final int npriceVersionCode, final UserInfo userInfo) throws Exception {

		final TestPriceVersion versionById = testPriceVersionDAO.getActiveTestPriceVersionById(npriceVersionCode,
				userInfo);

		if (versionById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_VERSIONALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (versionById.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				final String testCode = stringUtilityFunction.fnDynamicListToString(testPriceList, "getNtestcode");

				String insertedQuery = "select * from testpricedetail where npriceversioncode =" + npriceVersionCode
						+ " and ntestcode in(" + testCode + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by ntestpricedetailcode";

				final List<TestPriceDetail> beforeTestList = jdbcTemplate.query(insertedQuery, new TestPriceDetail());

				final List<Object> beforeUpdateList = new ArrayList<>();
				beforeUpdateList.add(beforeTestList);

				String queryString = "";
				for (final TestPriceDetail testPrice : testPriceList) {
					queryString = queryString + ";update testpricedetail set ncost = " + testPrice.getNcost()
							+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "'  where ntestpricedetailcode = " + testPrice.getNtestpricedetailcode();
				}

				jdbcTemplate.execute(queryString);

				insertedQuery = "select * from testpricedetail where npriceversioncode =" + npriceVersionCode
						+ " and ntestcode in(" + testCode + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by ntestpricedetailcode";

				final List<TestPriceDetail> updatedTestList = jdbcTemplate.query(insertedQuery, new TestPriceDetail());

				final List<Object> savedList = new ArrayList<>();
				savedList.add(updatedTestList);

				final List<String> multiLingualIDList = new ArrayList<String>();
				multiLingualIDList.add("IDS_EDITTESTPRICE");

				auditUtilityFunction.fnInsertListAuditAction(savedList, 2, beforeUpdateList, multiLingualIDList,
						userInfo);

				return getTestPrice(npriceVersionCode, null, userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTTESTPRICEVERSION",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

//	@Override
//	public Map<String,Object> insertupdateTestPrice(Map<String, Object> objMap) throws Exception {
//		
//		Map<String,Object>map =new HashMap<>();
//		List<String> lstactionType1 = new ArrayList<String>();
//		List<Object> lstObject1=new ArrayList<>();
//		TestPrice objTestPrice=new TestPrice();
//		String returnStr = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
//		int npriceversioncode =(int) objMap.get("npriceversioncode");
//		int npricelistcode=0;
//		
//		ObjectMapper objObjectMapper =new ObjectMapper();
//		List<TestPrice> lstTestPrice=objObjectMapper.convertValue(objMap.get("TestPrice"),new TypeReference<List<TestPrice>>(){});
//
//		String ntestcode = "";
//		for(TestPrice obTestPrice :lstTestPrice){
//			ntestcode=ntestcode.concat(String.valueOf(obTestPrice.getntestcode()).concat(","));
//		}
//			ntestcode=ntestcode.substring(0, ntestcode.length()-1);
//
//		
//		String Squery="select * from TestPrice where npriceversioncode ="+npriceversioncode+" and ntestcode in("+ntestcode+") and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
//		JQTestPriceVersion objjqtest = (JQTestPriceVersion) findBySingleObjectPlainSql(Squery, JQTestPriceVersion.class);
//	
//		boolean isUniqueValue = true;
//		 if(objjqtest !=null){
//			 returnStr = "IDS_TESTNAMEALREADYEXIST";
//			 isUniqueValue=false;
//		 }
//		if(isUniqueValue){
//		StringBuilder sb  = new StringBuilder();
// 		sb.delete(0, sb.length());
//		
//		
//				
//		String	sQuery = "select nsequenceno from sequencenogenerator "+Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+" where stablename = 'TestPrice'";
//		int nsequenceno = jdbcTemplate.queryForObject(sQuery, Integer.class);
//		
//		int nsequenceno1=nsequenceno+lstTestPrice.size();
//		sQuery = "update sequencenogenerator set nsequenceno = "+nsequenceno1+" where stablename = 'TestPrice';";
//		sb.append(sQuery);
//		
//		String str = "insert into TestPrice (npricelistcode,npriceversioncode,ntestcode,ncost,nstatus) "+
//					 "select  rank() over (order by ntestcode asc)+  "+nsequenceno+" as npricelistcode,"+npriceversioncode+",ntestcode,ncost,"+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" " +
//					 "from testmaster where ntestcode  in ("+ntestcode+")";
//		
//		sb.append(str);
//		jdbcTemplate.execute(sb.toString());
//		objTestPrice.setnpricelistcode(nsequenceno1);
//		npricelistcode=nsequenceno1;
//		}
//		
//		if(returnStr==Enumeration.ReturnStatus.SUCCESS.getreturnstatus()){
//			String str1="select npricelistcode from TestPrice where npriceversioncode="+npriceversioncode+" and ntestcode in("+ntestcode+") and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
//			List<TestPrice> lst=(List<TestPrice>) findBySinglePlainSql(str1,TestPrice.class);
//			
//			String npricelistcode1 ="";
//			for(TestPrice obj :lst){
//				npricelistcode1=npricelistcode1.concat(String.valueOf(obj.getnpricelistcode()).concat(","));
//			}
//				npricelistcode1=npricelistcode1.substring(0, npricelistcode1.length()-1);
//				String strQuery="select jtpl.npricelistcode,jtpl.npriceversioncode,jtpl.ntestcode,jtpl.ncost,tm.stestname  from TestPrice jtpl,testmaster tm where  tm.ntestcode=jtpl.ntestcode  and jtpl.npricelistcode in ("+npricelistcode1+") and npriceversioncode ="+npriceversioncode+" and jtpl.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by 1 desc ";
//
//				
//			List<TestPrice> lst1=(List<TestPrice>) findBySinglePlainSql(strQuery,TestPrice.class);
//			
//			lstObject1.add(lst1);
//			lstactionType1.add("IDS_ADDTESTPRICELIST");
//			fnInsertListAuditAction(lstObject1, 1, lstactionType1);
//			
//			
//			map.put("value", lst1);
//		}
//			map.put("rtn", returnStr);
//		
//		return map;
//	}
//	
}
