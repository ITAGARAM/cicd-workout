package com.agaramtech.qualis.testgroup.service.testgrouprulesengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductCategory;
import com.agaramtech.qualis.testgroup.model.TestGroupRulesEngine;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestCharParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestNumericParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;
import com.agaramtech.qualis.testmanagement.model.Grade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "sqlquery" table by
 * implementing methods from its interface.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 05- Oct- 2020
 */
@AllArgsConstructor
@Repository
public class TestGroupRulesEngineDAOImpl implements TestGroupRulesEngineDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestGroupRulesEngineDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getTestGroupRulesEngine(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String jsonstr = "";
		List<Map<String, Object>> lstrulesEngine = new ArrayList<>();
		ObjectMapper objmapper = new ObjectMapper();
		try {
			jsonstr = jdbcTemplate.queryForObject("select json_agg(x.*) from "
					+ "(select  t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "' as stransdisplaystatus,tre.* from testgrouprulesengine tre,transactionstatus t "
					+ " where tre.ntransactionstatus=t.ntranscode " + "  and tre.ntestgrouptestcode= "
					+ inputMap.get("ntestgrouptestcode") + "  and tre.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tre.nsitecode="
					+ userInfo.getNmastersitecode() + " order by nruleexecorder asc)x", String.class);
			LOGGER.info(jsonstr);
			// }

		} catch (Exception e) {
			jsonstr = null;
		}
		if (jsonstr != null) {
			lstrulesEngine = objmapper.readValue(jsonstr.toString(), new TypeReference<List<Map<String, Object>>>() {
			});
		}

		outputMap.put("RulesEngine", lstrulesEngine);
		if (lstrulesEngine.size() > 0) {
			outputMap.put("SelectedRulesEngine", lstrulesEngine.get(0));
		} else {
			outputMap.put("SelectedRulesEngine", new HashMap<>());
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getEditTestGroupRulesEngine(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		ObjectMapper objmapper = new ObjectMapper();
		String jsonstr = "";
		List<Map<String, Object>> lstrulesEngine = new ArrayList<>();
		try {
			jsonstr = jdbcTemplate.queryForObject("select json_agg(x.*) from "
					+ "(select  t.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "' as stransdisplaystatus,tre.* from testgrouprulesengine tre,transactionstatus t "
					+ " where tre.ntransactionstatus=t.ntranscode and tre.ntestgrouprulesenginecode="
					+ (int) inputMap.get("ntestgrouprulesenginecode") + "  and tre.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by tre.ntestgrouprulesenginecode asc)x", String.class);
		} catch (Exception e) {
			jsonstr = null;
		}
		if (jsonstr != null) {
			lstrulesEngine = objmapper.readValue(jsonstr.toString(), new TypeReference<List<Map<String, Object>>>() {
			});
		}
		outputMap.put("RulesEngineEdit", lstrulesEngine);
		int ntransactionstatus = (int) lstrulesEngine.get(0).get("ntransactionstatus");
		if (ntransactionstatus != Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRULE", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmap = new ObjectMapper();
		objmap.registerModule(new JavaTimeModule());
		final TestGroupRulesEngine testGroupRulesEngine = objmap.convertValue(inputMap.get("selectedValueForAudit"),
				new TypeReference<TestGroupRulesEngine>() {
				});
		final String sQuery = " lock  table locktestgrouprulesengine "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(sQuery);
		final String strQuery = "select ntestgrouprulesenginecode from testgrouprulesengine where srulename = N'"
				+ stringUtilityFunction.replaceQuote(inputMap.get("srulename").toString()) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestgrouptestcode  in ("
				+ inputMap.get("ntestgrouptestcode") + ") and nsitecode=" + userInfo.getNmastersitecode() + ";";
		List<Map<String, Object>> lstduplicatecheck = jdbcTemplate.queryForList(strQuery);
		final String strorderQuery = "select ntestgrouprulesenginecode from testgrouprulesengine where nruleexecorder = "
				+ inputMap.get("nruleexecorder") + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus <>"
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " and ntestgrouptestcode  in ("
				+ inputMap.get("ntestgrouptestcode") + ") and nsitecode=" + userInfo.getNmastersitecode() + ";";
		List<Map<String, Object>> lstduplicateordercheck = jdbcTemplate.queryForList(strorderQuery);
		if (lstduplicateordercheck.isEmpty()) {
			if (lstduplicatecheck.isEmpty()) {
				String insertQueryString = "";
				JSONArray jsonarray = new JSONArray(inputMap.get("jsondata").toString());
				JSONObject jsonobject = new JSONObject(inputMap.get("outcomeList").toString());
				Integer seqno = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnotestgroupmanagement where stablename='testgrouprulesengine' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"",
						Integer.class);
				seqno++;
				int nruleexecorder = jdbcTemplate
						.queryForObject("select count(*) from testgrouprulesengine where ntestgrouptestcode="
								+ inputMap.get("ntestgrouptestcode") + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
								+ userInfo.getNmastersitecode(), Integer.class);
				nruleexecorder++;
				insertQueryString = " insert into testgrouprulesengine (   ntestgrouprulesenginecode,ntestgrouptestcode, "
						+ "	 srulename, "
						+ "    jsondata  ,  	jsonuidata,ntransactionstatus,nruleexecorder  , dmodifieddate,nsitecode,     nstatus ) values ("
						+ seqno + "," + (int) inputMap.get("ntestgrouptestcode") + ",'"
						+ stringUtilityFunction.replaceQuote(inputMap.get("srulename").toString()) + "','"
						+ stringUtilityFunction.replaceQuote(jsonarray.toString()) + "'::jsonb,'"
						+ stringUtilityFunction.replaceQuote(jsonobject.toString()) + "'::jsonb,"
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + nruleexecorder + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") ;";
				insertQueryString += " update seqnotestgroupmanagement set nsequenceno=" + seqno
						+ " where stablename='testgrouprulesengine' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				jdbcTemplate.execute(insertQueryString);
				testGroupRulesEngine
						.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedTestList = new ArrayList<>();
				savedTestList.add(testGroupRulesEngine);
				multilingualIDList.add("IDS_ADDRULESENGINE");
				auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

				return new ResponseEntity<>(getTestGroupRulesEngine(inputMap, userInfo).getBody(), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_EXECUTIONORDERALREADYEXIST",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> updateTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmap = new ObjectMapper();
		objmap.registerModule(new JavaTimeModule());
		final TestGroupRulesEngine testGroupRulesEngine = objmap.convertValue(inputMap.get("selectedValueForAudit"),
				new TypeReference<TestGroupRulesEngine>() {
				});

		int ntestgrouptestcode = (int) inputMap.get("ntestgrouptestcode");
		String updateQueryString = "";

		String strQue = jdbcTemplate
				.queryForObject("select srulename from testgrouprulesengine  where ntestgrouprulesenginecode ="
						+ (int) inputMap.get("ntestgrouprulesenginecode"), String.class);
		final String strQuery = "select ntestgrouprulesenginecode from testgrouprulesengine where srulename = N'"
				+ stringUtilityFunction.replaceQuote(inputMap.get("srulename").toString()) + "'"
				+ "  and ntestgrouprulesenginecode <> " + (int) inputMap.get("ntestgrouprulesenginecode")
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntestgrouptestcode=" + ntestgrouptestcode + " and nsitecode=" + userInfo.getNmastersitecode();
		List<Map<String, Object>> lstduplicatecheck = jdbcTemplate.queryForList(strQuery);
		final String strorderQuery = "select ntestgrouprulesenginecode from testgrouprulesengine where nruleexecorder =  "
				+ inputMap.get("nruleexecorder") + "  and ntestgrouprulesenginecode <> "
				+ (int) inputMap.get("ntestgrouprulesenginecode") + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransactionstatus <>"
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " and ntestgrouptestcode="
				+ ntestgrouptestcode + " and nsitecode=" + userInfo.getNmastersitecode();
		List<Map<String, Object>> lstduplicateordercheck = jdbcTemplate.queryForList(strorderQuery);
		if (lstduplicateordercheck.isEmpty()) {
			if (lstduplicatecheck.isEmpty()) {
				JSONArray jsonarray = new JSONArray(inputMap.get("jsondata").toString());
				JSONObject jsonobject = new JSONObject(inputMap.get("outcomeList").toString());
				updateQueryString = " update testgrouprulesengine set" + " ntestgrouptestcode=" + ntestgrouptestcode
						+ ",srulename='" + stringUtilityFunction.replaceQuote(inputMap.get("srulename").toString())
						+ "', jsondata = '" + stringUtilityFunction.replaceQuote(jsonarray.toString())
						+ "' ,jsonuidata='" + stringUtilityFunction.replaceQuote(jsonobject.toString())
						// + "', nruleexecorder=" + inputMap.get("nruleexecorder")
						+ "',dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where ntestgrouprulesenginecode = " + (int) inputMap.get("ntestgrouprulesenginecode") + "";
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();
				testGroupRulesEngine
						.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
				listAfterUpdate.add(testGroupRulesEngine);
				TestGroupRulesEngine testGroupRulesEnginebefore = new TestGroupRulesEngine();
				testGroupRulesEnginebefore.setSrulename(strQue);
				testGroupRulesEnginebefore.setSproductcatname(testGroupRulesEngine.getSproductcatname());
				testGroupRulesEnginebefore.setSproductname(testGroupRulesEngine.getSproductname());
				testGroupRulesEnginebefore.setSleveldescription(testGroupRulesEngine.getSleveldescription());
				testGroupRulesEnginebefore.setSspecname(testGroupRulesEngine.getSspecname());
				testGroupRulesEnginebefore.setStestsynonym(testGroupRulesEngine.getStestsynonym());
				testGroupRulesEnginebefore.setScomponentname(testGroupRulesEngine.getScomponentname());
				testGroupRulesEnginebefore
						.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
				listBeforeUpdate.add(testGroupRulesEnginebefore);
				multilingualIDList.add("IDS_EDITRULES");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return new ResponseEntity<>(getSelectedTestGroupRulesEngine(inputMap, userInfo).getBody(),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_EXECUTIONORDERALREADYEXIST",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> approveTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmap = new ObjectMapper();
		objmap.registerModule(new JavaTimeModule());
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final TestGroupRulesEngine testGroupRulesEngine = objmap.convertValue(inputMap.get("selectedValueForAudit"),
				new TypeReference<TestGroupRulesEngine>() {
				});
		TestGroupRulesEngine testGroupRulesEnginebefore = new TestGroupRulesEngine();
		int nflag = (int) inputMap.get("nflag");
		String updateQueryString = "";
		Integer ntransactionstatus = jdbcTemplate
				.queryForObject("select ntransactionstatus from testgrouprulesengine where ntestgrouprulesenginecode = "
						+ (int) inputMap.get("ntestgrouprulesenginecode"), Integer.class);

		if (nflag == 1) {
			if (ntransactionstatus == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRULETOAPPROVE",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

			}
			if (ntransactionstatus != Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				updateQueryString += " update testgrouprulesengine set   ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " where ntestgrouprulesenginecode = " + (int) inputMap.get("ntestgrouprulesenginecode") + ";";

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYAPPROVED", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);

			}
			testGroupRulesEngine
					.setNtransactionstatus((short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
			testGroupRulesEnginebefore
					.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
			multilingualIDList.add("IDS_APPROVERULES");

		} else {
			if (ntransactionstatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| ntransactionstatus == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDRULE",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

			}
			updateQueryString = " update testgrouprulesengine set   ntransactionstatus="
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
					+ " where ntestgrouprulesenginecode = " + (int) inputMap.get("ntestgrouprulesenginecode") + "";
			testGroupRulesEngine
					.setNtransactionstatus((short) Enumeration.TransactionStatus.RETIRED.gettransactionstatus());
			testGroupRulesEnginebefore
					.setNtransactionstatus((short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());
			multilingualIDList.add("IDS_RETIRERULES");
		}
		jdbcTemplate.execute(updateQueryString);

		listAfterUpdate.add(testGroupRulesEngine);

		testGroupRulesEnginebefore.setSrulename(testGroupRulesEngine.getSrulename());
		testGroupRulesEnginebefore.setSproductcatname(testGroupRulesEngine.getSproductcatname());
		testGroupRulesEnginebefore.setSproductname(testGroupRulesEngine.getSproductname());
		testGroupRulesEnginebefore.setSleveldescription(testGroupRulesEngine.getSleveldescription());
		testGroupRulesEnginebefore.setSspecname(testGroupRulesEngine.getSspecname());
		testGroupRulesEnginebefore.setStestsynonym(testGroupRulesEngine.getStestsynonym());
		testGroupRulesEnginebefore.setScomponentname(testGroupRulesEngine.getScomponentname());
		listBeforeUpdate.add(testGroupRulesEnginebefore);
		multilingualIDList.add("IDS_APPROVERULES");

		auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);
		return new ResponseEntity<>(getSelectedTestGroupRulesEngine(inputMap, userInfo).getBody(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSelectedTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String jsonstr = "";
		List<Map<String, Object>> lstrulesEngine = new ArrayList<>();
		ObjectMapper objmapper = new ObjectMapper();
		try {
			jsonstr = jdbcTemplate.queryForObject(
					"select json_agg(x.*) from " + "(select  t.jsondata->'stransdisplaystatus'->>'"
							+ userInfo.getSlanguagetypecode()
							+ "' as stransdisplaystatus,tre.* from testgrouprulesengine tre,transactionstatus t "
							+ " where tre.ntransactionstatus=t.ntranscode  and tre.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tre.ntestgrouptestcode=" + inputMap.get("ntestgrouptestcode")
							+ " and tre.nsitecode=" + userInfo.getNmastersitecode() + " order by nruleexecorder asc)x",
					String.class);
		} catch (Exception e) {
			jsonstr = null;
		}
		if (jsonstr != null) {
			lstrulesEngine = objmapper.readValue(jsonstr.toString(), new TypeReference<List<Map<String, Object>>>() {
			});
		}
		Optional<Map<String, Object>> SelectedRulesEngine = lstrulesEngine.stream().filter(
				x -> ((int) x.get("ntestgrouprulesenginecode")) == (int) inputMap.get("ntestgrouprulesenginecode"))
				.findAny();
		outputMap.put("SelectedRulesEngine", SelectedRulesEngine.get());
		outputMap.put("RulesEngine", lstrulesEngine);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmap = new ObjectMapper();
		objmap.registerModule(new JavaTimeModule());
		final TestGroupRulesEngine testGroupRulesEngine = objmap.convertValue(inputMap.get("selectedValueForAudit"),
				new TypeReference<TestGroupRulesEngine>() {
				});
		int ntestgrouprulesenginecode = Integer.valueOf(inputMap.get("ntestgrouprulesenginecode").toString());
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		String insertQueryString = "";
		Integer ntransactionstatus = jdbcTemplate
				.queryForObject("select ntransactionstatus from testgrouprulesengine where ntestgrouprulesenginecode = "
						+ ntestgrouprulesenginecode, Integer.class);
		if (ntransactionstatus != Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRULE", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
		insertQueryString = " update testgrouprulesengine set nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ntestgrouprulesenginecode = "
				+ (int) inputMap.get("ntestgrouprulesenginecode") + "";

		jdbcTemplate.execute(insertQueryString);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTestList = new ArrayList<>();
		testGroupRulesEngine.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
		savedTestList.add(testGroupRulesEngine);
		multilingualIDList.add("IDS_DELETERULES");
		auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

		returnMap.putAll((Map<String, Object>) getTestGroupRulesEngine(inputMap, userInfo).getBody());
		inputMap.put("TestGroupRulesEngine", returnMap.get("RulesEngine"));
		returnMap.putAll((Map<String, Object>) updateExecutionOrder(inputMap, userInfo).getBody());
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTestGroupRulesEngineAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> outputMap = new HashMap<>();

		String sDeleValidation = "select napprovalstatus from testgroupspecification where" + "  nallottedspeccode = "
				+ inputMap.get("nallottedspeccode") + "  and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		final TestGroupSpecification objTestGroupSpec = (TestGroupSpecification) jdbcUtilityFunction
				.queryForObject(sDeleValidation, TestGroupSpecification.class, jdbcTemplate);
		if (objTestGroupSpec != null) {
			String validationString = "select (select napprovalstatus from testgroupspecification where nallottedspeccode = "
					+ inputMap.get("nallottedspeccode") + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " = case when  (select napprovalstatuscode from approvalconfigrole acr where acr.napproveconfversioncode = "
					+ (int) inputMap.get("napproveconfversioncode") + ""
					+ "     and acr.nlevelno = 1  and acr.napprovalconfigcode = "
					+ Enumeration.ApprovalSubType.STUDYPLANAPPROVAL.getnsubtype() + "   and acr.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nsitecode="
					+ userInfo.getNmastersitecode() + ") is null then 0 "
					+ " else (select napprovalstatuscode from approvalconfigrole acr where acr.napproveconfversioncode = "
					+ (int) inputMap.get("napproveconfversioncode") + ""
					+ "     and acr.nlevelno = 1  and acr.napprovalconfigcode = "
					+ Enumeration.ApprovalSubType.STUDYPLANAPPROVAL.getnsubtype() + "   and acr.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nsitecode="
					+ userInfo.getNmastersitecode() + ")" + " end as napprovalstatuscode ;";
			final boolean validation = jdbcTemplate.queryForObject(validationString, Boolean.class);
			if (validation) {
				final String testQuery = "SELECT  tg.stestsynonym || '(' ||  tp.sparametersynonym "
						+ "       || ')' AS stestparametersynonym, tg.stestsynonym as stestsynonym ,      tp.*   FROM   testgrouptestparameter tp, "
						+ " testgrouptest tg " + " WHERE   tp.ntestgrouptestcode in ("
						+ " ( select  ntestgrouptestcode from testgrouptest where  nspecsampletypecode="
						+ inputMap.get("nspecsampletypecode") + " and nsitecode=" + userInfo.getNmastersitecode()
						+ ")) and tg.ntestgrouptestcode=tp.ntestgrouptestcode " + " AND tp.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tg.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nresultmandatory="
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and tp.nisadhocparameter <>"
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and tp.nsitecode="
						+ userInfo.getNmastersitecode();
				List<TestGroupTestParameter> TestParameterList = jdbcTemplate.query(testQuery,
						new TestGroupTestParameter());

				String getGrades = "select g.ngradecode," + "case g.sgradename when 'NA' then '' else"
						+ " coalesce(g.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
						+ " g.jsondata->'sdisplayname'->>'en-US') end sgradename ,"
						+ "cm.ncolorcode,cm.scolorname,cm.scolorhexcode from grade g,colormaster cm "
						+ " where g.ncolorcode = cm.ncolorcode and g.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and cm.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
						+ " select 0 ngradecode,'' sgradename, 0 ncolorcode, '' scolorname,'' scolorhexcode;";

				List<Grade> lstGrade = mapper.convertValue(
						commonFunction.getMultilingualMessageList(jdbcTemplate.query(getGrades, new Grade()),
								Arrays.asList("sgradename"), userInfo.getSlanguagefilename()),
						new TypeReference<List<Grade>>() {
						});
				outputMap.put("GradeValues",
						lstGrade.stream().collect(Collectors.groupingBy(Grade::getNgradecode, Collectors.toList())));
				outputMap.put("TestParameter", TestParameterList);
				final String diagnosticcaseQuery = "select ndiagnosticcasecode,jsondata->'sdiagnosticcasename'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "' as sdiagnosticcasename from diagnosticcase where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndiagnosticcasecode>-1"
						+ " and nsitecode=" + userInfo.getNmastersitecode();
				List<Map<String, Object>> lstdiagnosticcaseQuery = jdbcTemplate.queryForList(diagnosticcaseQuery);
				outputMap.put("DiagnosticCase", lstdiagnosticcaseQuery);
				final String resultypeQuery = "select nresultypecode,jsondata->'sdisplayname'->>'"
						+ userInfo.getSlanguagetypecode() + "' as sdisplayname from resultype where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nresultypecode>-1"
						+ " and nsitecode=" + userInfo.getNmastersitecode();
				List<Map<String, Object>> lstresultypeQuery = jdbcTemplate.queryForList(resultypeQuery);
				outputMap.put("ResultType", lstresultypeQuery);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				final String salertString = "select ts.jsondata->'salertdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode()
						+ "' as salertdisplaystatus from approvalconfigrole acr,transactionstatus ts where  "
						+ "acr.napprovalstatuscode = ts.ntranscode	and acr.napproveconfversioncode = "
						+ (int) inputMap.get("napproveconfversioncode") + " "
						+ "	and acr.nlevelno = 1 and acr.napprovalconfigcode = "
						+ Enumeration.ApprovalSubType.STUDYPLANAPPROVAL.getnsubtype() + " and acr.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acr.nsitecode="
						+ userInfo.getNmastersitecode() + ";";

				TransactionStatus objAlertString = (TransactionStatus) jdbcUtilityFunction.queryForObject(salertString,
						TransactionStatus.class, jdbcTemplate);
				if (objAlertString == null) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SPECIFICATIONMUSTNOTBEDRAFTSTATUS",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_RULESCANBEADDED",
							userInfo.getSlanguagefilename()) + " " + objAlertString.getSalertdisplaystatus() + " "
							+ commonFunction.getMultilingualMessage("IDS_SPEC", userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	public ResponseEntity<Object> getProductCategoryTestGroupRulesEngine(final UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new LinkedHashMap<>();
		final String strQuery = "select p.* from productcategory p,transactionstatus ts,transactionstatus ts1 where p.ncategorybasedflow=ts.ntranscode and ts1.ntranscode=p.ndefaultstatus and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nsitecode ="
				+ userInfo.getNmastersitecode()
				+ " and p.nproductcatcode in (select tm.nproductcatcode from treetemplatemanipulation tm,testgroupspecification tgs "
				+ " where  tm.nsampletypecode=" + Enumeration.SampleType.CLINICALSPEC.getType()
				+ " and tgs.napprovalstatus= " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and tm.nsitecode=" + userInfo.getNmastersitecode() + " and tgs.nsitecode="
				+ userInfo.getNmastersitecode() + " group by nproductcatcode) order by p.nproductcatcode";
		List<ProductCategory> lstProductCategory = jdbcTemplate.query(strQuery, new ProductCategory());
		returnMap.put("ProductCategory", lstProductCategory);
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getParameterResultValue(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new LinkedHashMap<>();
		int nparametertypecode = (int) inputMap.get("nparametertypecode");
		int ntestgrouptestparametercode = (int) inputMap.get("ntestgrouptestparametercode");
		String strQuery = "";
		if (nparametertypecode == Enumeration.ParameterType.PREDEFINED.getparametertype()) {
			strQuery = "select * from testgrouptestpredefparameter where ntestgrouptestparametercode="
					+ ntestgrouptestparametercode + " and nsitecode=" + userInfo.getNmastersitecode();
			List<TestGroupTestPredefinedParameter> lstTestGroupTestPredefinedParameter = jdbcTemplate.query(strQuery,
					new TestGroupTestPredefinedParameter());
			returnMap.put("PredefinedParameterRulesEngine", lstTestGroupTestPredefinedParameter);
		} else if (nparametertypecode == Enumeration.ParameterType.NUMERIC.getparametertype()) {
			strQuery = "select * from testgrouptestnumericparameter where ntestgrouptestparametercode="
					+ ntestgrouptestparametercode + " and nsitecode=" + userInfo.getNmastersitecode();
			List<TestGroupTestNumericParameter> lstTestGroupTestNumericParameter = jdbcTemplate.query(strQuery,
					new TestGroupTestNumericParameter());
			returnMap.put("NumericParameterRulesEngine", lstTestGroupTestNumericParameter);
		} else {
			strQuery = "select * from testgrouptestcharparameter where ntestgrouptestparametercode="
					+ ntestgrouptestparametercode + " and nsitecode=" + userInfo.getNmastersitecode();
			List<TestGroupTestCharParameter> lstTestGroupTestCharParameter = jdbcTemplate.query(strQuery,
					new TestGroupTestCharParameter());
			returnMap.put("CharParameterRulesEngine", lstTestGroupTestCharParameter);
		}
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getParameterRulesEngine(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new LinkedHashMap<>();
		int tabIndex = (int) inputMap.get("tabIndex");
		String strQuery = "";
		String getPreDefinedParams = "";
		String getGrades = "";
		ObjectMapper mapper = new ObjectMapper();
		List<Grade> lstGrade = new ArrayList<Grade>();
		List<TestGroupTestPredefinedParameter> lst = new ArrayList<TestGroupTestPredefinedParameter>();
		if (tabIndex == 1) {

			strQuery = " select tgtp.* from testgrouptestparameter tgtp" + " where  tgtp.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  tgtp.ntestgrouptestcode="
					+ inputMap.get("ntestgrouptestcode") + " and  tgtp.nresultmandatory="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and   tgtp.nisadhocparameter <>"
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and  tgtp.nparametertypecode not in (  "
					+ Enumeration.ParameterType.ATTACHEMENT.getparametertype() // +","+Enumeration.ParameterType.NUMERIC.getparametertype()
					+ ") and tgtp.ntestgrouptestparametercode " + " NOT IN(select  ntestgrouptestparametercode "
					+ "				 from testgrouptestformula) and tgtp.nsitecode=" + userInfo.getNmastersitecode();

			getPreDefinedParams = " select tgpp.ntestgrouptestpredefcode, tgpp.ntestgrouptestparametercode, tgpp.ngradecode,  "
					+ " tgpp.spredefinedname||' ('||tgpp.spredefinedsynonym||')' as sresultpredefinedname,tgpp.spredefinedname,"
					+ " tgpp.spredefinedsynonym, " + " tgpp.spredefinedcomments, tgpp.salertmessage, "
					+ " tgpp.nneedresultentryalert, tgpp.nneedsubcodedresult, tgpp.ndefaultstatus, tgpp.nsitecode, "
					+ " tgpp.dmodifieddate, tgpp.nstatus,rp.ntestgrouptestcode,rp.ntestgrouptestparametercode,rp.ntestgrouptestparametercode "
					+ " from testgrouptestparameter rp,testgrouptestpredefparameter tgpp"
					+ " where rp.ntestgrouptestparametercode = tgpp.ntestgrouptestparametercode and rp.ntestgrouptestcode in ("
					+ inputMap.get("ntestgrouptestcode") + ") " + " and rp.nparametertypecode = "
					+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " and rp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tgpp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgpp.spredefinedname <> '' "
					+ " and rp.nsitecode=" + userInfo.getNmastersitecode() + " and tgpp.nsitecode="
					+ userInfo.getNmastersitecode() + ";";
			lst = jdbcTemplate.query(getPreDefinedParams, new TestGroupTestPredefinedParameter());

			getGrades = "select g.ngradecode," + "case g.sgradename when 'NA' then '' else"
					+ " coalesce(g.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ " g.jsondata->'sdisplayname'->>'en-US') end sgradename ,"
					+ "cm.ncolorcode,cm.scolorname,cm.scolorhexcode from grade g,colormaster cm "
					+ " where g.ncolorcode = cm.ncolorcode and g.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and cm.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ " select 0 ngradecode,'' sgradename, 0 ncolorcode, '' scolorname,'' scolorhexcode;";

			lstGrade = mapper.convertValue(
					commonFunction.getMultilingualMessageList(jdbcTemplate.query(getGrades, new Grade()),
							Arrays.asList("sgradename"), userInfo.getSlanguagefilename()),
					new TypeReference<List<Grade>>() {
					});

		} else {
			strQuery = "select * from testgrouptestparameter " + " where  nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestgrouptestcode="
					+ inputMap.get("ntestgrouptestcode") + " and nresultmandatory="
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " and  nisadhocparameter <>"
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
		}
		returnMap.put("GradeValues",
				lstGrade.stream().collect(Collectors.groupingBy(Grade::getNgradecode, Collectors.toList())));

		returnMap.put("PredefinedValues", lst.stream().collect(Collectors
				.groupingBy(TestGroupTestPredefinedParameter::getNtestgrouptestparametercode, Collectors.toList())));
		returnMap.put("TestGroupTestParameterRulesEngine", jdbcTemplate.queryForList(strQuery));
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateExecutionOrder(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		String updateQueryString = "";
		List<Map<String, Object>> lstTestGroupRulesEngine = objMapper.convertValue(inputMap.get("TestGroupRulesEngine"),
				new TypeReference<List<Map<String, Object>>>() {
				});
		int nruleexecorder = 1;
		for (int i = 0; i < lstTestGroupRulesEngine.size(); i++) {
			Map<String, Object> objTestGroupRulesEngine = lstTestGroupRulesEngine.get(i);
			updateQueryString += " update testgrouprulesengine set nruleexecorder=" + nruleexecorder
					+ " where ntestgrouprulesenginecode in (" + objTestGroupRulesEngine.get("ntestgrouprulesenginecode")
					+ ");";
			nruleexecorder++;
		}

		jdbcTemplate.execute(updateQueryString);
		return new ResponseEntity<>(getTestGroupRulesEngine(inputMap, userInfo).getBody(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getProductCategory(UserInfo userInfo, final int ntestcode,
			final int ncategorybasedflow) throws Exception {
		Map<String, Object> returnMap = new LinkedHashMap<>();
		// ALPD-4984--Added by Vignesh (10-04-2025)-->Test group: Copy Rules engine
		String sproduct = "";
		String productJoin = "";
		String nsitecode = "";
		if (ncategorybasedflow == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			sproduct = ",product pp";
			productJoin = " pc.nproductcatcode=ttv.nproductcatcode and ";
			nsitecode = " and pp.nsitecode = " + userInfo.getNmastersitecode() + " and pp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		}
		String sQuery = " select pc.nproductcatcode,pc.sproductcatname,pc.sdescription,pc.ncategorybasedflow,pc.ndefaultstatus "
				+ " from productcategory pc, treetemplatemanipulation ttv ,testgroupspecification tgs , "
				+ " testgroupspecsampletype tgst,testgrouptest tgt ,testgrouprulesengine trs " + sproduct + " where "
				+ " " + productJoin + "  trs.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and tgs.ntemplatemanipulationcode=ttv.ntemplatemanipulationcode and tgs.napprovalstatus in ("
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ")" + " and pc.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + nsitecode + " " + " and ttv.nsitecode = "
				+ userInfo.getNmastersitecode() + " and tgs.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " and tgst.nsitecode = " + userInfo.getNmastersitecode() + " and tgt.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + " and trs.nsitecode = " + userInfo.getNmastersitecode()
				+ " and tgst.nallottedspeccode=tgs.nallottedspeccode and tgt.nspecsampletypecode=tgst.nspecsampletypecode "
				+ " and trs.ntestgrouptestcode=tgt.ntestgrouptestcode  and tgt.ntestcode=" + ntestcode
				+ " and trs.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and tgs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and  tgt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgt.nspecsampletypecode>0 group by pc.nproductcatcode ";
		List<ProductCategory> rootQueryList = jdbcTemplate.query(sQuery, new ProductCategory());
		returnMap.put("getProductCategory", rootQueryList);
		if (rootQueryList.isEmpty()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_APPROVEDRULESNOTAVAILABLE",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getProductByProductCat(UserInfo userInfo, final int nproductCatCode)
			throws Exception {
		Map<String, Object> returnMap = new LinkedHashMap<>();
		String sQuery = "";
		sQuery = "select * from product where nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode() + " and nproductcatcode = " + nproductCatCode
				+ " and nproductcatcode > " + Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " order by nproductcode";
		List<Product> rootQueryList = jdbcTemplate.query(sQuery, new Product());
		returnMap.put("getProduct", rootQueryList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	// ALPD-4984
	// Added by Neeraj
	@Override
	public ResponseEntity<Object> getProfileRoot(UserInfo userInfo, int nproductcatcode, int nproductcode,
			int ntestcode) throws Exception {
		Map<String, Object> returnMap = new LinkedHashMap<>();

		String rootQuery = "select ttm.sleveldescription,ttm.ntemplatemanipulationcode "
				+ " from treetemptranstestgroup ttg,treetemplatemanipulation ttm ,testgroupspecification tgs "
				+ " where   ttg.ntemptranstestgroupcode=ttm.ntemptranstestgroupcode and ttm.nproductcatcode="
				+ nproductcatcode + " and ttm.nproductcode=" + nproductcode
				+ " and tgs.ntemplatemanipulationcode=ttm.ntemplatemanipulationcode and  tgs.napprovalstatus in ("
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ")"
				+ " and tgs.nallottedspeccode =any(select tgst.nallottedspeccode "
				+ " from testgroupspecsampletype tgst,testgrouptest tgt,testgrouprulesengine trs "
				+ " where tgt.nspecsampletypecode=tgst.nspecsampletypecode  " + " and tgt.ntestcode= " + ntestcode
				+ " and trs.ntestgrouptestcode=tgt.ntestgrouptestcode and trs.ntransactionstatus ="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and tgst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and trs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgst.nsitecode = "
				+ userInfo.getNmastersitecode() + "" + " and tgt.nsitecode = " + userInfo.getNmastersitecode()
				+ " and trs.nsitecode = " + userInfo.getNmastersitecode() + ") "
				+ " and ttg.schildnode ='-1' and ttg.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "  and ttg.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ttm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgs.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttg.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + " and ttm.nsitecode = " + userInfo.getNmastersitecode()
				+ " and tgs.nsitecode = " + userInfo.getNmastersitecode() + " group by ttm.ntemplatemanipulationcode";
		List<TreeTemplateManipulation> rootQueryList = jdbcTemplate.query(rootQuery, new TreeTemplateManipulation());
		returnMap.put("getProfileRoot", rootQueryList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	// ALPD-4984
	// Added by Neeraj
	@Override
	public ResponseEntity<Object> getSpecification(UserInfo userInfo, int ntemplatemanipulationcode, int ntestcode)
			throws Exception {
		Map<String, Object> returnMap = new LinkedHashMap<>();
		String rootQuery = " select sspecname,nallottedspeccode,ncomponentrequired from testgroupspecification "
				+ " where napprovalstatus in (" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") "
				+ " and ntemplatemanipulationcode=" + ntemplatemanipulationcode + " and nallottedspeccode = any "
				+ " (select  ts.nallottedspeccode from testgrouptest tt,testgroupspecsampletype ts,testgrouprulesengine tr where "
				+ " tt.ntestcode=" + ntestcode
				+ " and ts.nspecsampletypecode=tt.nspecsampletypecode and tr.ntestgrouptestcode=tt.ntestgrouptestcode"
				+ " and tr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and   tr.ntransactionstatus =" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "and tt.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + " and ts.nsitecode = " + userInfo.getNmastersitecode()
				+ " and tr.nsitecode = " + userInfo.getNmastersitecode() + ")" + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode();
		List<TestGroupSpecification> rootQueryList = jdbcTemplate.query(rootQuery, new TestGroupSpecification());
		returnMap.put("getSpecification", rootQueryList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	// ALPD-4984
	// Added by Neeraj
	@Override
	public ResponseEntity<Object> getComponent(UserInfo userInfo, int nallottedspeccode, int ncomponentcode,
			int ntestcode) throws Exception {
		Map<String, Object> returnMap = new LinkedHashMap<>();
		String needComponentQuery = "";
		if (ncomponentcode != -1) {
			needComponentQuery = "and tgs.ncomponentcode <> " + ncomponentcode;
		}
		String rootQuery = " select tgs.nspecsampletypecode,tgs.nallottedspeccode, tgs.ncomponentcode,cc.scomponentname from "
				+ " testgroupspecsampletype tgs,component cc ,testgrouptest tt,testgrouprulesengine tr where tgs.ncomponentcode=cc.ncomponentcode "
				+ " and tt.ntestcode=" + ntestcode
				+ " and tgs.nspecsampletypecode=tt.nspecsampletypecode and tr.ntestgrouptestcode=tt.ntestgrouptestcode and tr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  tr.ntransactionstatus ="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and tgs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgs.nallottedspeccode="
				+ nallottedspeccode + " " + needComponentQuery + "  and cc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + " and tgs.nsitecode = " + userInfo.getNmastersitecode()
				+ " and tt.nsitecode = " + userInfo.getNmastersitecode() + " and tr.nsitecode = "
				+ userInfo.getNmastersitecode()
				+ " group by tgs.nspecsampletypecode,tgs.nallottedspeccode, tgs.ncomponentcode,cc.scomponentname";
		List<TestGroupSpecSampleType> rootQueryList = jdbcTemplate.query(rootQuery, new TestGroupSpecSampleType());
		returnMap.put("getComponent", rootQueryList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	// ALPD-4984
	@SuppressWarnings({ "unchecked", "unused" })
	// Added by Neeraj
	@Override
	public ResponseEntity<Object> getTestBasedOnRules(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new LinkedHashMap<>();
		boolean isPresent = false;
		String validateName = "";
		int ntestcode = (Integer) inputMap.get("ntestcode");
		int ntestgrouptestcode = (Integer) inputMap.get("ntestgrouptestcode");
		int selectedSpecCode = (Integer) inputMap.get("selectedSpecCode");
		int selectedComponentCode = (Integer) inputMap.get("selectedComponentCode");
		String query = "select * from testgrouptest tgt,testgroupspecsampletype tgst where tgt.nspecsampletypecode=tgst.nspecsampletypecode "
				+ " and  tgst.nallottedspeccode=" + inputMap.get("nallottedspeccode") + " and ntestcode=" + ntestcode
				+ " and tgst.ncomponentcode =" + inputMap.get("ncomponentcode") + " " + " and tgst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nsitecode="
				+ userInfo.getNmastersitecode() + " and tgst.nsitecode=" + userInfo.getNmastersitecode();
		List<TestGroupTest> rootQueryList = jdbcTemplate.query(query, new TestGroupTest());

		String querys = "select * from testgrouptest tgt,testgroupspecsampletype tgst where tgt.nspecsampletypecode=tgst.nspecsampletypecode "
				+ " and  tgst.nallottedspeccode=" + selectedSpecCode + " and tgst.ncomponentcode ="
				+ selectedComponentCode + " " + " and tgst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.nsitecode="
				+ userInfo.getNmastersitecode() + " and tgst.nsitecode=" + userInfo.getNmastersitecode();
		List<TestGroupTest> selectedTestList = jdbcTemplate.query(querys, new TestGroupTest());

		String rulesQuery = "select * from testgrouprulesengine where ntestgrouptestcode="
				+ rootQueryList.get(0).getNtestgrouptestcode() + " and  ntransactionstatus ="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		List<TestGroupRulesEngine> rulesQueryList = jdbcTemplate.query(rulesQuery, new TestGroupRulesEngine());
		String ntestgrouprulescode = "";

		if (rulesQueryList.size() > 0) {
			for (TestGroupRulesEngine rulesEngine : rulesQueryList) {
				List<?> testInvolvedRules = (List<?>) rulesEngine.getJsonuidata().get("testsInvolvedInRules");
				String ntestgrouprulesenginecode = testInvolvedRules.stream().map(item -> String.valueOf(item))
						.collect(Collectors.joining(","));

				String test = "select * from testgrouptest where ntestgrouptestcode in (" + ntestgrouprulesenginecode
						+ ")";
				List<TestGroupTest> selectedTest = jdbcTemplate.query(test, new TestGroupTest());

				isPresent = selectedTestList.stream().anyMatch(
						st -> selectedTest.stream().anyMatch(code -> code.getNtestcode() == st.getNtestcode()));

				List<Map<String, Object>> testRepeatTests = (List<Map<String, Object>>) rulesEngine.getJsonuidata()
						.get("testRepeatTests");
				List<Map<String, Object>> testEnforceTests = (List<Map<String, Object>>) rulesEngine.getJsonuidata()
						.get("testenforceTests");
				List<Map<String, Object>> testCommentsTests = (List<Map<String, Object>>) rulesEngine.getJsonuidata()
						.get("testCommentsTests");
				List<Map<String, Object>> testInitiateTests = (List<Map<String, Object>>) rulesEngine.getJsonuidata()
						.get("testInitiateTests");

				if (isPresent) {
					if (testInitiateTests.size() > 0) {
						isPresent = selectedTestList.stream().anyMatch(st -> testInitiateTests.stream().anyMatch(
								code -> Integer.parseInt(code.get("ntestcode").toString()) == st.getNtestcode()));
					}
					if (testCommentsTests.size() > 0) {
						isPresent = selectedTestList.stream().anyMatch(st -> testCommentsTests.stream().anyMatch(
								code -> Integer.parseInt(code.get("ntestcode").toString()) == st.getNtestcode()));
					}

					if (testRepeatTests.size() > 0) {
						isPresent = selectedTestList.stream().anyMatch(st -> testRepeatTests.stream().anyMatch(
								code -> Integer.parseInt(code.get("ntestcode").toString()) == st.getNtestcode()));
					}

					if (testEnforceTests.size() > 0) {
						isPresent = selectedTestList.stream().anyMatch(st -> testEnforceTests.stream().anyMatch(
								code -> Integer.parseInt(code.get("ntestcode").toString()) == st.getNtestcode()));
					}

					if (!isPresent) {
						validateName = "IDS_OUTCOMEVALIDATE";
						break;
					}
				}
				if (isPresent) {
					ntestgrouprulescode += String.valueOf(rulesEngine.getNtestgrouprulesenginecode()) + ",";
				}
			}

		}
		if (!ntestgrouprulescode.equals("")) {
			returnMap.put("getRules", rulesQueryList);
		} else {
			returnMap.put("getRules", new ArrayList<>());
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	// ALPD-4984
	@Override
	public ResponseEntity<Object> copyTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmap = new ObjectMapper();
		objmap.registerModule(new JavaTimeModule());
		int ntestgrouptestcode = (Integer) inputMap.get("ntestgrouptestcode");
		List<Object> savedRulesList = new ArrayList<>();
		List<Object> listTestGroupRulesEngine = new ArrayList<>();

		final List<TestGroupRulesEngine> tgtTestInputList = objmap.convertValue(inputMap.get("selectedRules"),
				new TypeReference<List<TestGroupRulesEngine>>() {
				});

		String sortercode = "select COALESCE(max(nruleexecorder),0) from testgrouprulesengine where ntestgrouptestcode="
				+ ntestgrouptestcode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNmastersitecode();
		int nsortercount = jdbcTemplate.queryForObject(sortercode, Integer.class);

		String sequencenoquery = "select nsequenceno from seqnotestgroupmanagement where stablename ='testgrouprulesengine' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
		int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);

		String namevalidation = "Select srulename from testgrouprulesengine where ntestgrouptestcode="
				+ ntestgrouptestcode + "" + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		List<String> rulesName = jdbcTemplate.queryForList(namevalidation, String.class);
		String insertQuery = "";
		String srulename = "";
		for (TestGroupRulesEngine tgtTestInput : tgtTestInputList) {
			nsequenceno++;
			String policyname = stringUtilityFunction.replaceQuote(tgtTestInput.getSrulename());
			if (rulesName.size() > 0) {
				Boolean isTrue = rulesName.stream().anyMatch(x -> x.equals(tgtTestInput.getSrulename()));
				if (isTrue) {
					int count = 0;
					Boolean check = false;
					int j = 0;
					Inner: for (int i = 0; i < rulesName.size(); i++) {
						if (!rulesName.get(i).equals(policyname)) {
							if (rulesName.get(i).equals(policyname) || rulesName.get(i).contains(policyname + "-"
									+ commonFunction.getMultilingualMessage("IDS_COPY", userInfo.getSlanguagefilename())
									+ "(")) {
								j = j + 1;

								String val = policyname + "-" + commonFunction.getMultilingualMessage("IDS_COPY",
										userInfo.getSlanguagefilename()) + "(" + j + ")";

								List<String> objPasswordPolicy2 = rulesName.stream().filter(x -> x.equals(val))
										.collect(Collectors.toList());
								if (objPasswordPolicy2.isEmpty()) {
									policyname = policyname + "-" + commonFunction.getMultilingualMessage("IDS_COPY",
											userInfo.getSlanguagefilename()) + "(" + j + ")";
									check = true;
									break Inner;
								} else {
									count = count + 1;
								}
							}
						}
					}
					if (!check) {
						if (count == 0) {
							count = 1;
							policyname = policyname + "-"
									+ commonFunction.getMultilingualMessage("IDS_COPY", userInfo.getSlanguagefilename())
									+ "(" + count + ")";
						} else {
							count = count + 1;
							policyname = policyname + "-"
									+ commonFunction.getMultilingualMessage("IDS_COPY", userInfo.getSlanguagefilename())
									+ "(" + count + ")";
						}
					}
					srulename = policyname;
				} else {
					srulename = tgtTestInput.getSrulename();
				}
			} else {
				srulename = tgtTestInput.getSrulename();
			}

			insertQuery = insertQuery
					+ "INSERT INTO public.testgrouprulesengine(ntestgrouprulesenginecode, ntestgrouptestcode, srulename,"
					+ " jsondata, jsonuidata, ntransactionstatus, nruleexecorder,dmodifieddate, nsitecode, nstatus) "
					+ "select " + nsequenceno + "  ntestgrouprulesenginecode," + ntestgrouptestcode
					+ " as ntestgrouptestcode ,'" + srulename + "' , jsondata,jsonuidata,"
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + nsortercount
					+ " +Rank() over (order by ntestgrouprulesenginecode) as nruleexecorder,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from testgrouprulesengine "
					+ " where ntestgrouprulesenginecode in (" + tgtTestInput.getNtestgrouprulesenginecode()
					+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ";";
			final TestGroupRulesEngine testGroupRulesEngine = objmap.convertValue(inputMap.get("selectedValueForAudit"),
					new TypeReference<TestGroupRulesEngine>() {
					});
			testGroupRulesEngine.setSrulename(srulename);
			testGroupRulesEngine
					.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
			listTestGroupRulesEngine.add(testGroupRulesEngine);
		}

		jdbcTemplate.execute(insertQuery);

		String updatequery = "update seqnotestgroupmanagement set nsequenceno ="
				+ (nsequenceno + tgtTestInputList.size()) + " where stablename='testgrouprulesengine' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
		jdbcTemplate.execute(updatequery);
		savedRulesList.add(listTestGroupRulesEngine);
		auditUtilityFunction.fnInsertListAuditAction(savedRulesList, 1, null, Arrays.asList("IDS_COPYRULES"), userInfo);

		return new ResponseEntity<>(getTestGroupRulesEngine(inputMap, userInfo).getBody(), HttpStatus.OK);

	}

}
