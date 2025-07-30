package com.agaramtech.qualis.resultentry.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.attachmentscomments.model.RegistrationTestComment;
import com.agaramtech.qualis.attachmentscomments.service.attachments.AttachmentDAO;
import com.agaramtech.qualis.attachmentscomments.service.comments.CommentDAO;
import com.agaramtech.qualis.attachmentscomments.service.comments.CommentService;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.ApprovalConfigRole;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.ExternalOrderSupport;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.service.materialcategory.MaterialCategoryDAO;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationHistory;
import com.agaramtech.qualis.registration.model.RegistrationSample;
import com.agaramtech.qualis.registration.model.RegistrationSampleHistory;
import com.agaramtech.qualis.registration.model.RegistrationSection;
import com.agaramtech.qualis.registration.model.RegistrationTest;
import com.agaramtech.qualis.registration.model.ResultParameter;
import com.agaramtech.qualis.registration.model.SeqNoRegistration;
import com.agaramtech.qualis.registration.service.RegistrationDAO;
import com.agaramtech.qualis.registration.service.RegistrationDAOImpl;
import com.agaramtech.qualis.registration.service.RegistrationDAOSupport;
import com.agaramtech.qualis.testgroup.model.TestGroupRulesEngine;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestNumericParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;




@AllArgsConstructor
@Repository
public class RulesEngineFunctions {

	private static final Logger LOGGER = LoggerFactory.getLogger(RulesEngineFunctions.class);

	private final TransactionDAOSupport transactionDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final StringUtilityFunction stringUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final RegistrationDAOSupport registrationDAOSupport;
	private final AuditUtilityFunction auditUtilityFunction;
	private final CommentDAO commentDAO;
	private final RegistrationDAOImpl registrationDAOImpl;
	private final MaterialCategoryDAO materialCatDAO;
	private final RegistrationDAO RegistrationDAO;
	

	@Autowired
	CommentService objCommentService;

	public ResponseEntity<Object> rulesEngineValidationBasedOnSample(String stransactiontestcode,
			List<RegistrationTest> lstonGoingTransactionTests, String npreregno, boolean isRuleDependent,
			final UserInfo userInfo) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> objoutCome = new LinkedHashMap<String, Object>();
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		boolean isApplicableforReadingRule = false;
		List<Integer> transactiontestholdList = new ArrayList<>();
		List<Map<String, Object>> testsGroupTestsforAlert = new ArrayList<>();
		List<Map<String, Object>> transactionTestsforAlert = new ArrayList<>();
		Map<Object, List<Map<String, Object>>> ReturntestsGroupTestsforAlert = new LinkedHashMap<Object, List<Map<String, Object>>>();
		Map<Object, List<Map<String, Object>>> ReturntransactionTestsforAlert = new LinkedHashMap<Object, List<Map<String, Object>>>();

		String transactionTests = "select max(rth.ntransactionstatus) as ntransactionstatus ,rt.jsondata->>'stestsynonym' as "
				+ " stestsynonym ,rs.ssamplearno as samplearno ,r.sarno,rt.* "
				+ " from registrationtest rt, registrationtesthistory rth,"
				+ "	registrationsamplearno rs,registrationarno r where rt.ntransactionsamplecode in ( select rsa.ntransactionsamplecode  from"
				+ " registrationtest rsa where rsa.ntransactiontestcode in (" + stransactiontestcode
				+ ")) and rt.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rs.ntransactionsamplecode=rt.ntransactionsamplecode and r.npreregno=rs.npreregno "
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ " group by rth.ntransactiontestcode,rt.ntransactiontestcode, rt.ntransactionsamplecode, rt.npreregno,rt.ntestgrouptestcode, "
				+ " rt.ntestcode,rt.nsectioncode,rt.nmethodcode,rt.ninstrumentcatcode,rt.nchecklistversioncode,rt.ntestrepeatno,rt.ntestretestno, "
				+ " rt.jsondata,rt.dmodifieddate,rt.nsitecode,rs.ssamplearno,r.sarno";
		/******************************************************************************
		 * Step-1: We are Taking All Test under the current Transaction test's Sample in
		 * lsttransactiontest List.
		 ******************************************************************************/
		final List<RegistrationTest> lsttransactiontest = jdbcTemplate.query(transactionTests, new RegistrationTest());
		/******************************************************************************
		 * Step-2: We are Fetching the Rules for The current Transaction tests in
		 * lstAllRules list.
		 ******************************************************************************/
		String query = "select * from testgrouprulesengine where ntestgrouptestcode in (select  ntestgrouptestcode from "
				+ " registrationtest where ntransactiontestcode in (" + stransactiontestcode + "))"
				+ " and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<TestGroupRulesEngine> lstAllRules = jdbcTemplate.query(query, new TestGroupRulesEngine());

		List<RegistrationTest> lstTransRulesTests = lstonGoingTransactionTests.stream()
				.filter(x -> lstAllRules.stream().anyMatch(y -> y.getNtestgrouptestcode() == x.getNtestgrouptestcode()))
				.collect(Collectors.toList());

		/******************************************************************************
		 * Step-3: We are iterating the Current Transaction Tests that will have Rule.
		 ******************************************************************************/
		for (int i = 0; i < lstTransRulesTests.size(); i++) {
			final int Index = i;
			final int ongoingSampleCode = lstTransRulesTests.get(i).getNtransactionsamplecode();
			final int ongoingTransactionTestCode = lstTransRulesTests.get(i).getNtransactiontestcode();
			final int ongoingtestgrouptestcode = lstTransRulesTests.get(i).getNtestgrouptestcode();
			RegistrationTest ongoingtransactionTest = lstTransRulesTests.get(i);

			/******************************************************************************
			 * Step-4: Fetching Rule for the current iteration Transaction Test in
			 * lstTestGroupRulesEngine List .
			 ******************************************************************************/
			List<TestGroupRulesEngine> lstTestGroupRulesEngine = lstAllRules.stream()
					.filter(x -> x.getNtestgrouptestcode() == ongoingtestgrouptestcode).collect(Collectors.toList());

			/******************************************************************************
			 * Step-5: If a Transaction Test have Multiple Approved Rules we need to check
			 * which test to hold under this Transaction Test's Sample on each Rule.
			 ******************************************************************************/
			for (int j = 0; j < lstTestGroupRulesEngine.size(); j++) {
				int ntestgrouptestcode = lstTestGroupRulesEngine.get(j).getNtestgrouptestcode();
				objoutCome = (Map<String, Object>) lstTestGroupRulesEngine.get(j).getJsonuidata();
				final List<Integer> lsttestGroupcodesInvolvedInRules = new ArrayList<>();

				lsttestGroupcodesInvolvedInRules.addAll((List<Integer>) objoutCome.get("testsInvolvedInRules"));

				final List<Map<String, Object>> lsttestGroupTestsInvolvedInRules = (List<Map<String, Object>>) objoutCome
						.get("testsNameInvolvedInRules");

				for (int k = 0; k < lsttestGroupTestsInvolvedInRules.size(); k++) {
					Map<String, Object> ruleDependentTestgroupTest = lsttestGroupTestsInvolvedInRules.get(k);
					/******************************************************************************
					 * Step-6: We are Checking Rule Dependent Tests Under the Current Transaction
					 * Test's Rule
					 ******************************************************************************/
					List<RegistrationTest> lsttransactiontestunderSample = lsttransactiontest.stream()
							.filter(x -> x.getNtestgrouptestcode() == (int) ruleDependentTestgroupTest
									.get("ntestgrouptestcode"))
							.filter(y -> y.getNtransactionsamplecode() == ongoingSampleCode)
							.collect(Collectors.toList());

					/******************************************************************************
					 * Step-7: If Rule Dependent Test available under Current Transaction Test's
					 * Sample We will hold that test and will allow the Test to complete if that
					 * test is under current Transaction.
					 ******************************************************************************/
					if (lsttransactiontestunderSample.size() > 0) {
						for (int l = 0; l < lsttransactiontestunderSample.size(); l++) {
							final int sampleCodeunderSample = lsttransactiontestunderSample.get(l)
									.getNtransactionsamplecode();
							final int transactionTestCodeunderSample = lsttransactiontestunderSample.get(l)
									.getNtransactiontestcode();
							final int testgrouptestcodeunderSample = lsttransactiontestunderSample.get(l)
									.getNtestgrouptestcode();
							RegistrationTest transactionTestunderSample = lsttransactiontestunderSample.get(l);
							if (
							
							transactionTestunderSample
									.getNtransactionstatus() == Enumeration.TransactionStatus.PREREGISTER
											.gettransactionstatus()
									|| transactionTestunderSample
											.getNtransactionstatus() == Enumeration.TransactionStatus.REGISTERED
													.gettransactionstatus()
									|| transactionTestunderSample
											.getNtransactionstatus() == Enumeration.TransactionStatus.RECIEVED_IN_SECTION
													.gettransactionstatus()
									|| transactionTestunderSample
											.getNtransactionstatus() == Enumeration.TransactionStatus.ALLOTTED
													.gettransactionstatus()
									|| transactionTestunderSample
											.getNtransactionstatus() == Enumeration.TransactionStatus.ACCEPTED
													.gettransactionstatus()
									|| transactionTestunderSample
											.getNtransactionstatus() == Enumeration.TransactionStatus.INITIATED
													.gettransactionstatus()
									|| transactionTestunderSample
											.getNtransactionstatus() == Enumeration.TransactionStatus.RECALC
													.gettransactionstatus()
									|| transactionTestunderSample
											.getNtransactionstatus() == Enumeration.TransactionStatus.RECOM_RECALC
													.gettransactionstatus()
									|| transactionTestunderSample
											.getNtransactionstatus() == Enumeration.TransactionStatus.RECOM_RETEST
													.gettransactionstatus()
									|| transactionTestunderSample
											.getNtransactionstatus() == Enumeration.TransactionStatus.QUARENTINE
													.gettransactionstatus()
									|| transactionTestunderSample
											.getNtransactionstatus() == Enumeration.TransactionStatus.RELEASED
													.gettransactionstatus()

							) {
								Optional<RegistrationTest> ongoingtransactionTestunderSample = lstonGoingTransactionTests
										.stream()
										.filter(x -> x.getNtransactiontestcode() == transactionTestCodeunderSample)
										.findAny();

								if (ongoingtransactionTestunderSample.isPresent()) {
									/******************************************************************************
									 * We are allowing The current Transaction Test to Complete if it is under
									 * Current Transaction. (We Wont be doing anything if this Condition Satisfied)
									 ******************************************************************************/
								} else {
									if (!transactiontestholdList.contains(ongoingTransactionTestCode)) {
										transactiontestholdList.add(ongoingTransactionTestCode);
									}
									Map<String, Object> RegistrationTestMap = objectMapper.convertValue(
											transactionTestunderSample, new TypeReference<Map<String, Object>>() {
											});
									RegistrationTestMap.put("nparenttestgrouptestcode", ntestgrouptestcode);
									RegistrationTestMap.put("nparenttransactiontestcode", ongoingTransactionTestCode);
									transactionTestsforAlert.add(RegistrationTestMap);
								}
							}
						}
					} else {
						/******************************************************************************
						 * Step-8: If Rule Dependent Test Not available under Current Transaction Test's
						 * Sample We will hold that Transaction Test.
						 ******************************************************************************/
						if (!transactiontestholdList.contains(ongoingTransactionTestCode)) {
							transactiontestholdList.add(ongoingTransactionTestCode);
						}
						ruleDependentTestgroupTest.put("nparenttestgrouptestcode", ntestgrouptestcode);
						ruleDependentTestgroupTest.put("ntransactionsamplecode", ongoingSampleCode);
						ruleDependentTestgroupTest.put("nparenttransactiontestcode", ongoingTransactionTestCode);
						testsGroupTestsforAlert.add(ruleDependentTestgroupTest);
					}
				}

			}

		}

		/******************************************************************************
		 * Step-9: Re-checking whether All rule dependent test holded. (case: if Test A
		 * has a rule in which depends on Test B and Both the Test are under same
		 * transaction so we will allow Test A to Complete so it wont be present in
		 * transactiontestholdList, in case if Test B gets holded due to its Rule it
		 * will be present in transactiontestholdList, in this Case we Need to Hold Test
		 * A too).
		 ******************************************************************************/
		for (int m = 0; m < lstTransRulesTests.size(); m++) {
			final int checktestgrouptestcode = lstTransRulesTests.get(m).getNtestgrouptestcode();
			final int checkntransactiontestcode = lstTransRulesTests.get(m).getNtransactiontestcode();
			final int checkntransactionsamplecode = lstTransRulesTests.get(m).getNtransactionsamplecode();
			RegistrationTest checktransactionTest = lstTransRulesTests.get(m);
			for (int n = 0; n < transactiontestholdList.size(); n++) {
				final int holdedntransactiontestcode = transactiontestholdList.get(n);

				List<RegistrationTest> lsttransactiontestunderSample = lsttransactiontest.stream()
						.filter(x -> x.getNtransactiontestcode() == holdedntransactiontestcode)
						.filter(y -> y.getNtransactionsamplecode() == checkntransactionsamplecode)
						.collect(Collectors.toList());

				for (int o = 0; o < lsttransactiontestunderSample.size(); o++) {
					final int holdedtestgrouptestcode = lsttransactiontestunderSample.get(o).getNtestgrouptestcode();
					List<TestGroupRulesEngine> lstTestGroupRulesEngine = lstAllRules.stream()
							.filter(x -> x.getNtestgrouptestcode() == checktestgrouptestcode)
							.collect(Collectors.toList());
					for (int p = 0; p < lstTestGroupRulesEngine.size(); p++) {
						objoutCome = (Map<String, Object>) lstTestGroupRulesEngine.get(p).getJsonuidata();
						final List<Integer> lsttestGroupTestsCodesInvolvedInRules = (List<Integer>) objoutCome
								.get("testsInvolvedInRules");
						if (lsttestGroupTestsCodesInvolvedInRules.contains(holdedtestgrouptestcode)) {
							if (!transactiontestholdList.contains(checkntransactiontestcode)) {
								transactiontestholdList.add(checkntransactiontestcode);
							}
						}
					}

				}

			}
		}

		ReturntransactionTestsforAlert = transactionTestsforAlert.stream().distinct()
				.collect(Collectors.groupingBy(item -> item.get("nparenttransactiontestcode")));
		ReturntestsGroupTestsforAlert = testsGroupTestsforAlert.stream().distinct()
				.collect(Collectors.groupingBy(item -> item.get("nparenttransactiontestcode")));

		returnMap.put("RegistrationTestAlert", ReturntransactionTestsforAlert);
		returnMap.put("NewTestGroupTestAlert", ReturntestsGroupTestsforAlert);
		returnMap.put("ruleAppliedntestgrouptestcode", transactiontestholdList);
		if (transactiontestholdList.size() > 0) {
			isApplicableforReadingRule = true;
		}
		returnMap.put("isApplicableforReadingRule", isApplicableforReadingRule);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> rulesEngineValidation(String stransactiontestcode,
			List<RegistrationTest> lstonGoingTransactionTests, String npreregno, boolean isRuleDependent,
			final UserInfo userInfo) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> objoutCome = new LinkedHashMap<String, Object>();
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		boolean isApplicableforReadingRule = false;
		Map<Object, List<Map<String, Object>>> ReturnTestsforAlert = new LinkedHashMap<Object, List<Map<String, Object>>>();
		Map<Object, List<Map<String, Object>>> ReturnTestsforAlertnew = new LinkedHashMap<Object, List<Map<String, Object>>>();
		List<Map<String, Object>> lstNewTestneedTobeAddedAlert = new ArrayList<>();
		List<Map<String, Object>> newtestsforAlert = new ArrayList<>();
		List<Map<String, Object>> testsforAlert = new ArrayList<>();
		List<Integer> lstntestgrouptestcode = new ArrayList<>();
		List<Integer> testholdList = new ArrayList<>();
		List<Integer> transactiontestholdList = new ArrayList<>();

		String transactionTests = "select max(rth.ntransactionstatus) as ntransactionstatus ,rt.jsondata->>'stestsynonym' as "
				+ " stestsynonym ,rs.ssamplearno as samplearno ,r.sarno,rt.* "
				+ " from registrationtest rt, registrationtesthistory rth,"
				+ "	registrationsamplearno rs,registrationarno r where rt.ntransactionsamplecode in ( select rsa.ntransactionsamplecode  from"
				+ " registrationtest rsa where rsa.ntransactiontestcode in (" + stransactiontestcode
				+ ")) and rt.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rs.ntransactionsamplecode=rt.ntransactionsamplecode and r.npreregno=rs.npreregno "
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ " group by rth.ntransactiontestcode,rt.ntransactiontestcode, rt.ntransactionsamplecode, rt.npreregno,rt.ntestgrouptestcode, "
				+ " rt.ntestcode,rt.nsectioncode,rt.nmethodcode,rt.ninstrumentcatcode,rt.nchecklistversioncode,rt.ntestrepeatno,rt.ntestretestno, "
				+ " rt.jsondata,rt.dmodifieddate,rt.nsitecode,rs.ssamplearno,r.sarno";
		final List<RegistrationTest> lsttransactiontest = jdbcTemplate.query(transactionTests, new RegistrationTest());

		final List<Integer> transactionsampleCode = lsttransactiontest.stream().map(x -> x.getNtransactionsamplecode())
				.distinct().collect(Collectors.toList());
		
		String query = "select * from testgrouprulesengine where ntestgrouptestcode in (select  ntestgrouptestcode from "
				+ " registrationtest where ntransactiontestcode in (" + stransactiontestcode + "))"
				+ " and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<TestGroupRulesEngine> lstRules = jdbcTemplate.query(query, new TestGroupRulesEngine());

		List<RegistrationTest> lstTransRulesTests = lsttransactiontest.stream()
				.filter(x -> lstRules.stream().anyMatch(y -> y.getNtestgrouptestcode() == x.getNtestgrouptestcode()))
				.collect(Collectors.toList());

		for (int s = 0; s < lstTransRulesTests.size(); s++) {
			final int Index = s;
			final int currentSampleCode = lstTransRulesTests.get(s).getNtransactionsamplecode();
			final int currentTransactionTestCode = lstTransRulesTests.get(s).getNtransactiontestcode();

			final List<Integer> transntestgrouptestcode = lsttransactiontest.stream()
					.filter(x -> x.getNtransactionsamplecode() == currentSampleCode).map(y -> y.getNtestgrouptestcode())
					.collect(Collectors.toList());
			List<TestGroupRulesEngine> lstTestGroupRulesEngine = lstRules.stream()
					.filter(x -> x.getNtestgrouptestcode() == lstTransRulesTests.get(Index).getNtestgrouptestcode())
					.collect(Collectors.toList());
			for (int r = 0; r < lstTestGroupRulesEngine.size(); r++) {
				int ntestgrouptestcode = lstTestGroupRulesEngine.get(r).getNtestgrouptestcode();
				objoutCome = (Map<String, Object>) lstTestGroupRulesEngine.get(r).getJsonuidata();
				final List<Integer> lsttestsInvolvedInRules = new ArrayList<>();
				lsttestsInvolvedInRules.addAll((List<Integer>) objoutCome.get("testsInvolvedInRules"));
				final List<Map<String, Object>> lsttestsNameInvolvedInRules = (List<Map<String, Object>>) objoutCome
						.get("testsNameInvolvedInRules");
				if (isRuleDependent) {
					List<RegistrationTest> lstReturnTestsforAlert = new ArrayList<>();
					lstReturnTestsforAlert = lsttransactiontest.stream()
							.filter(x -> x.getNtransactionsamplecode() == currentSampleCode)
							.filter(ignoreTest -> ignoreTest.getNtestgrouptestcode() != ntestgrouptestcode)
							.filter(test -> test.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
									.gettransactionstatus()
											? test.getNtransactionstatus() != Enumeration.TransactionStatus.COMPLETED
													.gettransactionstatus()
											: test.getNtransactionstatus() == test.getNtransactionstatus())
							.filter(incompleteTest -> lsttestsInvolvedInRules
									.contains(incompleteTest.getNtestgrouptestcode()))
							.collect(Collectors.toList());
					// For Adding Test from Test group which is not in Transaction Start
					lsttestsInvolvedInRules.removeAll(transntestgrouptestcode);
					lstNewTestneedTobeAddedAlert = lsttestsNameInvolvedInRules.stream().filter(
							x -> lsttestsInvolvedInRules.stream().anyMatch(y -> y == x.get("ntestgrouptestcode")))
							.collect(Collectors.toList());
					lstNewTestneedTobeAddedAlert = lstNewTestneedTobeAddedAlert.stream()
							.filter(x -> !lstonGoingTransactionTests.stream()
									.filter(z -> z.getNtransactionsamplecode() == currentSampleCode)
									.anyMatch(y -> (int) x.get("ntestgrouptestcode") == y.getNtestgrouptestcode()))
							.collect(Collectors.toList());
					lstNewTestneedTobeAddedAlert.forEach(x -> {
						x.put("nparenttestgrouptestcode", ntestgrouptestcode);
						x.put("ntransactionsamplecode", currentSampleCode);
					});
					newtestsforAlert.addAll(lstNewTestneedTobeAddedAlert);
					newtestsforAlert = newtestsforAlert.stream().distinct().collect(Collectors.toList());

					lstReturnTestsforAlert = lstReturnTestsforAlert.stream()
							.filter(x -> !lstonGoingTransactionTests.stream()
									.filter(z -> z.getNtransactionsamplecode() == currentSampleCode)
									.anyMatch(y -> x.getNtestgrouptestcode() == y.getNtestgrouptestcode()))
							.collect(Collectors.toList());
					List<Map<String, Object>> lstforAlert = objectMapper.convertValue(lstReturnTestsforAlert,
							new TypeReference<List<Map<String, Object>>>() {
							});
					lstforAlert.forEach(x -> x.put("nparenttestgrouptestcode", ntestgrouptestcode));
					testsforAlert.addAll(lstforAlert);
					if (testsforAlert.size() > 0 || newtestsforAlert.size() > 0) {
						isApplicableforReadingRule = true;
					} else {
						isApplicableforReadingRule = false;
					}
					if (lstReturnTestsforAlert.size() > 0 || lstNewTestneedTobeAddedAlert.size() > 0) {
						
						if (!testholdList.contains(ntestgrouptestcode)) {
							testholdList.add(ntestgrouptestcode);
						}
						if (!transactiontestholdList.contains(currentTransactionTestCode)) {
							transactiontestholdList.add(currentTransactionTestCode);
						}
						testholdList.addAll(((List<Integer>) objoutCome.get("testsInvolvedInRules")).stream().filter(
								x -> lstTestGroupRulesEngine.stream().anyMatch(y -> y.getNtestgrouptestcode() == x))
								.collect(Collectors.toList()));
						testholdList = testholdList.stream().distinct().collect(Collectors.toList());
					}
					
					returnMap.put("isApplicableforReadingRule", isApplicableforReadingRule);
					testsforAlert = testsforAlert.stream().distinct().collect(Collectors.toList());
				}
			}
			
			// For Collecting Actual Test to Hold for Completion(Skip)
			for (int r = 0; r < lstTestGroupRulesEngine.size(); r++) {
				objoutCome = (Map<String, Object>) lstTestGroupRulesEngine.get(r).getJsonuidata();
				final List<Integer> testsInvolvedInRules = (List<Integer>) objoutCome.get("testsInvolvedInRules");
				boolean isTestPresent = testholdList.stream().anyMatch(testsInvolvedInRules::contains);
				int ntestgrouptestcode = lstTestGroupRulesEngine.get(r).getNtestgrouptestcode();
				if (isTestPresent) {
					if (!testholdList.contains(ntestgrouptestcode)) {
						testholdList.add(ntestgrouptestcode);
					}
				}
			}
		}

		ReturnTestsforAlert = testsforAlert.stream()
				.collect(Collectors.groupingBy(item -> item.get("nparenttestgrouptestcode")));
		ReturnTestsforAlertnew = newtestsforAlert.stream()
				.collect(Collectors.groupingBy(item -> item.get("nparenttestgrouptestcode")));
		returnMap.put("RegistrationTestAlert", ReturnTestsforAlert);
		returnMap.put("NewTestGroupTestAlert", ReturnTestsforAlertnew);
		returnMap.put("ruleAppliedntestgrouptestcode", testholdList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> readRulesOriginal(String stransactiontestcode, String transactionSampleCode,
			Map<String, Object> inputMap, boolean isRuleDependent, final UserInfo userInfo) throws Exception {
		List<Map<String, Object>> lstrules = new ArrayList<>();
		Map<String, Object> objrule = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstgroup = new ArrayList<>();
		List<Map<String, Object>> groupListJoins = new ArrayList<>();
		Map<String, Object> groupListJoin = new LinkedHashMap<String, Object>();
		List<String> lstConditions = new ArrayList<>();
		List<String> lstresultEntered = new ArrayList<>();
		List<TestGroupTest> testInitiateTests = new ArrayList<>();
		List<TestGroupTest> testRepeatTests = new ArrayList<>();
		List<Map<String, Object>> testCommentsTests = new ArrayList<>();
		List<Map<String, Object>> testenforceTestsParameter = new ArrayList<>();
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		RegistrationTest systemGeneratedTestMap = new RegistrationTest();
		ResultParameter ResultParameterMap = new ResultParameter();
		List<Map<String, Object>> lsttestcommentOutcome = new ArrayList<>();
		List<Integer> lstntestgrouptestparametercode = new ArrayList<>();
		List<Map<String, Object>> multipleRuleSameParamCodes = new ArrayList<>();
		List<Boolean> lstRuleboolean = new ArrayList<>();
		List<Boolean> lstGroupboolean = new ArrayList<>();
		List<Boolean> lstboolNumericParam = new ArrayList<>();
		List<TestGroupRulesEngine> lstTestGroupRulesEngine = new ArrayList<>();
		List<Integer> lstCheckRepeatTestCode = new ArrayList<>();
		String sntestgrouprulesenginecode = "";
		Map<Integer, String> rulesBasedOnSample = new LinkedHashMap<Integer, String>();
		List<RegistrationTest> lstruleRepeatTest = new ArrayList<>();
		int npreregno = 0;
		String rtnstr = "Rules read Successfully";

		ObjectMapper objectMapper = new ObjectMapper();
		int nparametertypecode = 0;
		boolean readOutcome = false;
		boolean isnumericParameter = false;
		boolean isApplicableforReadingRule = false;
		Map<String, Object> objoutCome = new LinkedHashMap<String, Object>();
		final Map<String, Object> registrationDatas = getRegistrationDiagnosticCaseType(inputMap, userInfo);

		String sampleParameters = "select rt.ntransactionsamplecode,rp.jsondata,rp.jsondata->>'sresult' as sresult,rp.jsondata->>'sfinal' as sfinal, "
				+ "  rp.ntestgrouptestparametercode,rp.nunitcode,rp.ngradecode,rp.ntransactiontestcode,rp.ntransactionresultcode,rp.npreregno, "
				+ "	  rp.nresultmandatory, "
				+ "	   rt.ntestgrouptestcode,rp.ntransactionstatus from resultparameter rp,registrationtest rt where rp.ntransactiontestcode in (select  ntransactiontestcode from "
				+ " registrationtest where ntransactionsamplecode in ( select  ntransactionsamplecode from registrationtest where ntransactiontestcode in( "
				+ stransactiontestcode + ")))" + " and rp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and rp.ntransactiontestcode=rt.ntransactiontestcode";
		List<ResultParameter> lstresultparam = jdbcTemplate.query(sampleParameters, new ResultParameter());

		String transactionTests = "select max(rth.ntransactionstatus) as ntransactionstatus ,rt.jsondata->>'stestsynonym' as stestsynonym,rt.* from registrationtest rt, registrationtesthistory rth "
				+ "where rt.ntransactionsamplecode in ( " + transactionSampleCode + ") and rt.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ " group by rth.ntransactiontestcode,rt.ntransactiontestcode, rt.ntransactionsamplecode, rt.npreregno,rt.ntestgrouptestcode, "
				+ " rt.ntestcode,rt.nsectioncode,rt.nmethodcode,rt.ninstrumentcatcode,rt.nchecklistversioncode,rt.ntestrepeatno,rt.ntestretestno, "
				+ " rt.jsondata,rt.dmodifieddate,rt.nsitecode";
		final List<RegistrationTest> lsttransactiontest = jdbcTemplate.query(transactionTests, new RegistrationTest());

		String onGoingTransactionTests = "select max(rth.ntransactionstatus) as ntransactionstatus ,rt.jsondata->>'stestsynonym' as stestsynonym"
				+ " ,rt.*"
				+ " from registrationtest rt, registrationtesthistory rth " + "where rt.ntransactiontestcode in ("
				+ stransactiontestcode + ") and rt.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ " group by rth.ntransactiontestcode,rt.ntransactiontestcode, rt.ntransactionsamplecode, rt.npreregno,rt.ntestgrouptestcode, "
				+ " rt.ntestcode,rt.nsectioncode,rt.nmethodcode,rt.ninstrumentcatcode,rt.nchecklistversioncode,rt.ntestrepeatno,rt.ntestretestno, "
				+ " rt.jsondata,rt.dmodifieddate,rt.nsitecode";
		final List<RegistrationTest> lstonGoingTransactionTests = jdbcTemplate.query(onGoingTransactionTests,
				new RegistrationTest());

		String query = "select * from testgrouprulesengine where ntestgrouptestcode in (select  rt.ntestgrouptestcode from "
				+ " registrationtest rt where rt.ntransactiontestcode in (" + stransactiontestcode + ")"
				+ "  and  (select max(rth.ntransactionstatus) from registrationtesthistory rth "
				+ "	 where rth.ntransactiontestcode=rt.ntransactiontestcode group by rth.ntransactiontestcode )="
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ") and  ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";
		final List<TestGroupRulesEngine> lstTestGroupRule = jdbcTemplate.query(query, new TestGroupRulesEngine());
		List<RegistrationTest> lstTransRulesTests = lstonGoingTransactionTests.stream().filter(
				x -> lstTestGroupRule.stream().anyMatch(y -> y.getNtestgrouptestcode() == x.getNtestgrouptestcode()))
				.collect(Collectors.toList());
		final List<Integer> lstTransRulesTestsSampleCode = lstTransRulesTests.stream()
				.map(x -> x.getNtransactionsamplecode()).distinct().collect(Collectors.toList());
		// if (lstTestGroupRulesEngine.size() > 0) {
		for (int q = 0; q < lstTestGroupRule.size(); q++) {
			final int index = q;
			// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>For Collecting Multiple Approved
			// Rules For A particular Test<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
			if (!lstCheckRepeatTestCode.contains(lstTestGroupRule.get(index).getNtestgrouptestcode())) {
				lstCheckRepeatTestCode.add(lstTestGroupRule.get(index).getNtestgrouptestcode());
			} else {
				continue;
			}
			lstTestGroupRulesEngine = lstTestGroupRule.stream()
					.filter(x -> x.getNtestgrouptestcode() == lstTestGroupRule.get(index).getNtestgrouptestcode())
					.sorted(Comparator.comparingInt(TestGroupRulesEngine::getNruleexecorder))
					.collect(Collectors.toList());

			for (int r = 0; r < lstTestGroupRulesEngine.size(); r++) {
				lstgroup = (List<Map<String, Object>>) lstTestGroupRulesEngine.get(r).getJsondata();
				final int ntestgrouprulesenginecode = lstTestGroupRulesEngine.get(r).getNtestgrouprulesenginecode();
				objoutCome = (Map<String, Object>) lstTestGroupRulesEngine.get(r).getJsonuidata();
				groupListJoins = (List<Map<String, Object>>) objoutCome.get("groupListJoins");
				int ntestgrouptestcode = lstTestGroupRulesEngine.get(r).getNtestgrouptestcode();
				ResultParameterMap = lstresultparam.stream()
						.filter(x -> x.getNtestgrouptestcode() == ntestgrouptestcode).findAny().get();
				final List<Integer> lsttestsInvolvedInRules = (List<Integer>) objoutCome.get("testsInvolvedInRules");
				
				for (int i = 0; i < lstgroup.size(); i++) {
					Map<String, Object> group = lstgroup.get(i);
					if (group.containsKey("button_and")) {
						lstrules = (List<Map<String, Object>>) group.get("button_and");
					}
					if (group.containsKey("button_not_button_and")) {
						lstrules = (List<Map<String, Object>>) group.get("button_not_button_and");
					}
					if (group.containsKey("button_or")) {
						lstrules = (List<Map<String, Object>>) group.get("button_or");
					}
					if (group.containsKey("button_not_button_or")) {
						lstrules = (List<Map<String, Object>>) group.get("button_not_button_or");
					}
					for (int j = 0; j < lstrules.size(); j++) {
						objrule = lstrules.get(j);
						String ruleStatement = "";
						boolean isruleSatisfied = false;
						int orderresulttype = (int) ((Map<String, Object>) objrule.get("orderresulttype")).get("value");

						int ntestgrouptestparametercode = (Integer) ((Map<String, Object>) objrule.get("stestname"))
								.get("value");

						String ssymbolname = ((Map<String, Object>) objrule.get("ssymbolname")).get("label").toString();

						List<ResultParameter> bothlsthasSameCode = lstresultparam.stream()
								.filter(x -> ((ResultParameter) x)
										.getNtestgrouptestparametercode() == ntestgrouptestparametercode)
								.collect(Collectors.toList());

						multipleRuleSameParamCodes = lstrules.stream()
								.filter(rule -> ((int) ((Map<String, Object>) rule.get("stestname"))
										.get("value")) == ntestgrouptestparametercode)
								.collect(Collectors.toList());

						for (int k = 0; k < bothlsthasSameCode.size(); k++) {
							if (orderresulttype == 1) {
								int rulendiagnosticcasecode = (int) ((Map<String, Object>) objrule
										.get("ndiagnosticcasecode")).get("value");
								Map<String, Object> registrationData = ((Map<String, Object>) registrationDatas
										.get(String.valueOf(bothlsthasSameCode.get(k).getNpreregno())));
								int resultdiagnosticcasecode = registrationData.containsKey("ndiagnosticcasecode")
										? (int) registrationData.get("ndiagnosticcasecode")
										: 0;
								if (ssymbolname.equals("equals(=)")) {
									if (resultdiagnosticcasecode == rulendiagnosticcasecode) {
										isruleSatisfied = true;
									} else {
										isruleSatisfied = false;
									}
								} else if (ssymbolname.equals("!=")) {
									if (resultdiagnosticcasecode != rulendiagnosticcasecode) {
										isruleSatisfied = true;
									} else {
										isruleSatisfied = false;
									}
								}
								lstRuleboolean.add(isruleSatisfied);
							} else if (orderresulttype == 2) {
								int rulengradecode = (int) ((Map<String, Object>) objrule.get("ngradecode"))
										.get("value");
								int resultngradecode = (int) bothlsthasSameCode.get(k).getNgradecode();

								if (ssymbolname.equals("equals(=)")) {
									if (resultngradecode == rulengradecode) {
										isruleSatisfied = true;
									} else {
										isruleSatisfied = false;
									}
								} else if (ssymbolname.equals("!=")) {
									if (resultngradecode != rulengradecode) {
										isruleSatisfied = true;
									} else {
										isruleSatisfied = false;
									}
								}
								lstRuleboolean.add(isruleSatisfied);

							} else {
								nparametertypecode = (int) ((Map<String, Object>) ((Map<String, Object>) objrule
										.get("stestname")).get("item")).get("nparametertypecode");
								if (nparametertypecode == Enumeration.ParameterType.CHARACTER.getparametertype()) {
									String rulesfinal = (String) objrule.get("sfinal");
									String resultsfinal = bothlsthasSameCode.get(k).getSfinal();
									if (ssymbolname.equals("equals(=)")) {
										if (resultsfinal.equals(rulesfinal)) {
											isruleSatisfied = true;
										} else {
											isruleSatisfied = false;
										}
									} else if (ssymbolname.equals("!=")) {
										if (!resultsfinal.equals(rulesfinal)) {
											isruleSatisfied = true;
										} else {
											isruleSatisfied = false;
										}
									}
									lstRuleboolean.add(isruleSatisfied);

								} else if (nparametertypecode == Enumeration.ParameterType.PREDEFINED
										.getparametertype()) {
									String rulesfinal = (String) objrule.get("sfinal");
									String resultsfinal = bothlsthasSameCode.get(k).getSfinal();
									if (ssymbolname.equals("equals(=)")) {
										if (resultsfinal.equals(rulesfinal)) {
											isruleSatisfied = true;
										} else {
											isruleSatisfied = false;
										}
									} else if (ssymbolname.equals("!=")) {
										if (!resultsfinal.equals(rulesfinal)) {
											isruleSatisfied = true;
										} else {
											isruleSatisfied = false;
										}
									}
									lstRuleboolean.add(isruleSatisfied);
								} else {
									// >>>>>>>>>>>>>Numeric Type<<<<<<<<<<<<<<<<<<
									double rulesfinalnum = Double.parseDouble(objrule.get("sfinal").toString());
									ResultParameter resultObject = bothlsthasSameCode.get(k);
									double resultfinal = 0.0;
									String rulesSymbol = "";
									double rulesresultfinal = 0.0;
									if (group.containsKey("button_and") || group.containsKey("button_not_button_and")) {
										resultfinal = Double.parseDouble(resultObject.getSfinal());
										for (int l = 0; l < multipleRuleSameParamCodes.size(); l++) {
											rulesSymbol = ((Map<String, Object>) multipleRuleSameParamCodes.get(l)
													.get("ssymbolname")).get("label").toString();
											rulesresultfinal = Double.parseDouble(
													multipleRuleSameParamCodes.get(l).get("sfinal").toString());
											if (rulesSymbol.equals("equals(=)")) {
												if (resultfinal == rulesresultfinal) {
													isruleSatisfied = true;
												} else {
													isruleSatisfied = false;
												}
											} else if (rulesSymbol.equals("!=")) {
												if (resultfinal != rulesresultfinal) {
													isruleSatisfied = true;
												} else {
													isruleSatisfied = false;
												}
											} else if (rulesSymbol.equals("<")) {
												if (resultfinal < rulesresultfinal) {
													isruleSatisfied = true;
												} else {
													isruleSatisfied = false;
												}
											} else if (rulesSymbol.equals(">")) {
												if (resultfinal > rulesresultfinal) {
													isruleSatisfied = true;
												} else {
													isruleSatisfied = false;
												}
											} else if (rulesSymbol.equals("<=")) {
												if (resultfinal <= rulesresultfinal) {
													isruleSatisfied = true;
												} else {
													isruleSatisfied = false;
												}
											} else {
												if (resultfinal >= rulesresultfinal) {
													isruleSatisfied = true;
												} else {
													isruleSatisfied = false;
												}
											}
											lstboolNumericParam.add(isruleSatisfied);
										}
										if (!lstboolNumericParam.contains(false)) {
											isruleSatisfied = true;
										} else {
											isruleSatisfied = false;
										}
										lstboolNumericParam.clear();
									} else {
										resultfinal = Double.parseDouble(resultObject.getSfinal());
										if (ssymbolname.equals("equals(=)")) {
											if (resultfinal == rulesfinalnum) {
												isruleSatisfied = true;
											}
										} else if (ssymbolname.equals("!=")) {
											if (resultfinal != rulesfinalnum) {
												isruleSatisfied = true;
											}
										} else if (ssymbolname.equals("<")) {
											if (resultfinal < rulesfinalnum) {
												isruleSatisfied = true;
											}
										} else if (ssymbolname.equals(">")) {
											if (resultfinal > rulesfinalnum) {
												isruleSatisfied = true;
											}
										} else if (ssymbolname.equals("<=")) {
											if (resultfinal <= rulesfinalnum) {
												isruleSatisfied = true;
											}
										} else {
											if (resultfinal >= rulesfinalnum) {
												isruleSatisfied = true;
											}
										}
									}

									lstRuleboolean.add(isruleSatisfied);
								}
							}
						}

					}
					if (group.containsKey("button_and")) {
						if (!lstRuleboolean.contains(false)) {
							lstGroupboolean.add(true);
						} else {
							lstGroupboolean.add(false);
						}

					}
					if (group.containsKey("button_not_button_and")) {
						if (!lstRuleboolean.contains(false)) {
							lstGroupboolean.add(false);
						} else {
							lstGroupboolean.add(true);
						}

					}
					if (group.containsKey("button_or")) {
						if (lstRuleboolean.contains(true)) {
							lstGroupboolean.add(true);
						} else {
							lstGroupboolean.add(false);
						}
					}
					if (group.containsKey("button_not_button_or")) {
						if (lstRuleboolean.contains(true)) {
							lstGroupboolean.add(false);
						} else {
							lstGroupboolean.add(true);
						}
					}
					lstRuleboolean.clear();
				}
				if (groupListJoins.size() > 0) {
					for (int i = 0; i < groupListJoins.size(); i++) {
						groupListJoin = groupListJoins.get(i);
						if (groupListJoin.containsKey("button_and")
								&& (boolean) groupListJoin.get("button_and") == true) {

							if (groupListJoin.containsKey("button_not")
									&& (boolean) groupListJoin.get("button_not") == true) {
								// Both AND and NOT True => NAND
								if (lstGroupboolean.get(i) != true && lstGroupboolean.get(i + 1) != true) {
									readOutcome = true;
								} else {
									readOutcome = false;
								}
							} else {
								// Only AND True
								if (lstGroupboolean.get(i) == true && lstGroupboolean.get(i + 1) == true) {
									readOutcome = true;
								} else {
									readOutcome = false;
								}
							}

						}

						if (groupListJoin.containsKey("button_or")
								&& (boolean) groupListJoin.get("button_or") == true) {
							if (groupListJoin.containsKey("button_not")
									&& (boolean) groupListJoin.get("button_not") == true) {
								// Both OR and NOT True => NOR
								if (lstGroupboolean.get(i) != true || lstGroupboolean.get(i + 1) != true) {
									readOutcome = true;
								} else {
									readOutcome = false;
								}
							} else {
								// Only OR True
								if (lstGroupboolean.get(i) == true || lstGroupboolean.get(i + 1) == true) {
									readOutcome = true;
								} else {
									readOutcome = false;
								}
							}

						}

					}
				} else {
					if (lstGroupboolean.size() > 0 && lstGroupboolean.get(0) == true) {
						readOutcome = true;
					} else {
						readOutcome = false;
					}
				}
				lstGroupboolean.clear();
				if (readOutcome) {
					// ((List<Map<String, Object>>) objoutCome.get("testCommentsTests")).
					// forEach(x->((Map<String, Object>)x).put("ntestgrouprulesenginecode",
					// ntestgrouprulesenginecode));;
					/*
					 * if(testInitiateTests.size()>0) { List<TestGroupTest>
					 * outcometestInitiateTests=(List<TestGroupTest>)
					 * objoutCome.get("testInitiateTests"); for(int
					 * o=0;o<outcometestInitiateTests.size();o++) { TestGroupTest
					 * outcomeTestInitiateTestsObj=objectMapper.convertValue(
					 * outcometestInitiateTests.get(o),new TypeReference<TestGroupTest>(){});
					 * if(testInitiateTests.stream()
					 * .filter(x->x.getNtestgrouptestcode()==outcomeTestInitiateTestsObj.
					 * getNtestgrouptestcode()).findAny().isPresent()) { TestGroupTest
					 * tempObj=testInitiateTests.stream()
					 * .filter(x->x.getNtestgrouptestcode()==outcomeTestInitiateTestsObj.
					 * getNtestgrouptestcode()).findAny().get(); testInitiateTests.remove(tempObj);
					 * tempObj.setNrepeatcountno((short)(tempObj.getNrepeatcountno()+2));
					 * testInitiateTests.add(tempObj); }else {
					 * testInitiateTests.add(outcomeTestInitiateTestsObj); } } }else {
					 * testInitiateTests.addAll(objectMapper.convertValue(objoutCome.get(
					 * "testInitiateTests"),new TypeReference<List<TestGroupTest>>(){})); }
					 */
					sntestgrouprulesenginecode += sntestgrouprulesenginecode
							+ String.valueOf(lstTestGroupRulesEngine.get(r).getNtestgrouprulesenginecode()) + ",";
					lstruleRepeatTest = lstonGoingTransactionTests.stream()
							.filter(x -> x.getNtestgrouptestcode() == ntestgrouptestcode).collect(Collectors.toList());
					// if Multiple Tests have Same Rule-->Outcome will be Same
					if (lstruleRepeatTest.size() > 0) {
						for (int l = 0; l < lstruleRepeatTest.size(); l++) {
							testInitiateTests.addAll((List<TestGroupTest>) objoutCome.get("testInitiateTests"));
							testRepeatTests.addAll((List<TestGroupTest>) objoutCome.get("testRepeatTests"));
						}
					} else {
						testInitiateTests.addAll((List<TestGroupTest>) objoutCome.get("testInitiateTests"));
						testRepeatTests.addAll((List<TestGroupTest>) objoutCome.get("testRepeatTests"));
					}

					// End of Collecting Data for Test Initiate And Test Repeat Outcome
					testCommentsTests.addAll((List<Map<String, Object>>) objoutCome.get("testCommentsTests"));
					// End Of Collecting Data for Test Comments Outcome
					testenforceTestsParameter.addAll((List<Map<String, Object>>) objoutCome.get("testenforceTests"));
					// End Of Collecting Data for Test Enforce Outcome

					// Break if one of Multiple Approved Rules Satisfied-Continue Outer LOOP
					break;
				}
			}
		}
		// Outcome Method Calls Start
		List<TestGroupTest> lstTestGroupTest = objectMapper.convertValue(testInitiateTests,
				new TypeReference<List<TestGroupTest>>() {
				});
		lstTestGroupTest.addAll(objectMapper.convertValue(testRepeatTests, new TypeReference<List<TestGroupTest>>() {
		}));
		if (lstTestGroupTest.size() > 0) {
			List<RegistrationSubType> lstRegistrationSubType = new ArrayList<RegistrationSubType>();
			try {
				lstRegistrationSubType = transactionDAOSupport.getRegistrationSubType((int) inputMap.get("nregtypecode"),userInfo);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			RegistrationSubType objRegistrationSubType = lstRegistrationSubType.stream()
					.filter(x -> x.getNregsubtypecode() == (int) inputMap.get("nregsubtypecode")).findAny().get();
			List<String> listSample = new ArrayList<>();
			List<Map<String, Object>> lstclinicaltype = new ArrayList<>();

			inputMap.put("nneedjoballocation", objRegistrationSubType.isNneedjoballocation());
			inputMap.put("TestGroupTest", lstTestGroupTest);
			inputMap.put("RegistrationSample", listSample);
			// inputMap.put("nfilterstatus", 0);
			for (int i = 0; i < lstTransRulesTests.size(); i++) {
				if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()) {

					Map<String, Object> clinicaltypeMap = new LinkedHashMap<String, Object>();
					Map<String, Object> registrationData = (Map<String, Object>) registrationDatas
							.get(String.valueOf(lstTransRulesTests.get(i).getNpreregno()));
					clinicaltypeMap.put("nage", (int) registrationData.get("nage"));
					clinicaltypeMap.put("npreregno", lstTransRulesTests.get(i).getNpreregno());
					clinicaltypeMap.put("ngendercode", registrationData.get("ngendercode"));
					lstclinicaltype.add(clinicaltypeMap);
					inputMap.put("ageData", lstclinicaltype);
				}
				listSample.add(String.valueOf(lstTransRulesTests.get(i).getNtransactionsamplecode()));
			}
			// extra parameters
			// String
			// sntestgrouprulesenginecode=lstTestGroupRule.stream().map(x->String.valueOf(x.getNtestgrouprulesenginecode())).collect(Collectors.joining(","));
			inputMap.put("ntestgrouprulesenginecode",
					sntestgrouprulesenginecode.substring(0, sntestgrouprulesenginecode.length() - 1));
			inputMap.put("RulesEngineGet", true);
			try {
				returnMap.putAll((Map<String, Object>) createTestRulesEngine(userInfo, listSample, lstTestGroupTest,
						(int) inputMap.get("nregtypecode"), (int) inputMap.get("nregsubtypecode"), inputMap).getBody());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// End of Creating Test and Test Repeat Outcome
		testCommentsTests = (List<Map<String, Object>>) testCommentsTests
				.stream().filter(
						x -> lsttransactiontest.stream()
								.anyMatch(y -> String.valueOf(y.getNtestgrouptestcode())
										.equals(String.valueOf(x.get("ntestgrouptestcode")))))
				.collect(Collectors.toList());
		stransactiontestcode = "";
		if (testCommentsTests.size() > 0) {
			for (int i = 0; i < testCommentsTests.size(); i++) {
				Map<String, Object> testCommentsTestsMap = testCommentsTests.get(i);

				List<RegistrationTest> lstCompletedTests = lsttransactiontest.stream()
						.filter(x -> x.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
								.gettransactionstatus())
						.filter(x -> String.valueOf(x.getNtestgrouptestcode())
								.equals(String.valueOf(testCommentsTestsMap.get("ntestgrouptestcode"))))
						.collect(Collectors.toList());
				for (int j = 0; j < lstCompletedTests.size(); j++) {
					// systemGeneratedTestMap = lsttransactiontest.stream()
//						.filter(x -> String.valueOf(x.getNtestgrouprulesenginecode())
//								.equals(String.valueOf(testCommentsTestsMap.get("ntestgrouprulesenginecode"))))
					// .filter(x -> String.valueOf(x.getNtestgrouptestcode())
					// .equals(String.valueOf(testCommentsTestsMap.get("ntestgrouptestcode"))))
					// .findAny().get();
					systemGeneratedTestMap = lstCompletedTests.get(j);
					List<Map<String, Object>> commentsArray = (List<Map<String, Object>>) testCommentsTestsMap
							.get("commentsArray");
					stransactiontestcode += systemGeneratedTestMap.getNtransactiontestcode() + ",";
					for (int k = 0; k < commentsArray.size(); k++) {
						Map<String, Object> comments = commentsArray.get(k);
						Map<String, Object> testcommentsMap = new LinkedHashMap<String, Object>();
						testcommentsMap.put("npreregno", systemGeneratedTestMap.getNpreregno());
						testcommentsMap.put("ntransactionsamplecode",
								systemGeneratedTestMap.getNtransactionsamplecode());
						testcommentsMap.put("ntransactiontestcode", systemGeneratedTestMap.getNtransactiontestcode());
						testcommentsMap.put("nformcode", userInfo.getNformcode());
						testcommentsMap.put("nusercode", userInfo.getNusercode());
						testcommentsMap.put("nuserrolecode", userInfo.getNuserrole());
						testcommentsMap.put("nsamplecommentscode", -1);
						testcommentsMap.put("ntestcommentcode", 0);
						testcommentsMap.put("nstatus", 1);
						testcommentsMap.put("jsondata", comments);
						lsttestcommentOutcome.add(testcommentsMap);
					}
				}
			}
			final List<RegistrationTestComment> listTestComment = objectMapper.convertValue(lsttestcommentOutcome,
					new TypeReference<List<RegistrationTestComment>>() {
					});
			objCommentService.createTestComment(listTestComment,
					stransactiontestcode.substring(0, stransactiontestcode.length() - 1), userInfo);
		}
		// End of Creating Test Comments Outcome
		testenforceTestsParameter = (List<Map<String, Object>>) testenforceTestsParameter.stream()
				.filter(x -> lstresultparam.stream()
						.anyMatch(y -> String.valueOf(y.getNtestgrouptestparametercode())
								.equals(String.valueOf(x.get("ntestgrouptestparametercode")))))
				.collect(Collectors.toList());
		if (testenforceTestsParameter.size() > 0) {
			// updateEnforceResult(testenforceTestsParameter, lstresultparam, userInfo);
		}
		// End of Creating Enforce Test
		System.out.print(rtnstr);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	/**
	 * This Method will Read Rules and will create Outcome For the Tests that will
	 * have Rule in Test Group(This Method is Applicable for Both Sub Sample and Non
	 * Sample Configuration for the Registration Type)
	 * 
	 * @author Allwin Giftson.H
	 * @param stransactiontestcode
	 * @param transactionSampleCode
	 * @param inputMap
	 * @param userInfo
	 * @throws Exception
	 */
	public ResponseEntity<Object> readRulesBasedOnSample(String stransactiontestcode, String transactionSampleCode,
			Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		List<Map<String, Object>> lstrules = new ArrayList<>();
		Map<String, Object> objrule = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstgroup = new ArrayList<>();
		List<Map<String, Object>> groupListJoins = new ArrayList<>();
		Map<String, Object> groupListJoin = new LinkedHashMap<String, Object>();
		List<String> lstConditions = new ArrayList<>();
		List<String> lstresultEntered = new ArrayList<>();
		List<TestGroupTest> testInitiateTests = new ArrayList<>();
		List<TestGroupTest> testRepeatTests = new ArrayList<>();
		List<Map<String, Object>> testCommentsTests = new ArrayList<>();
		Map<Integer, Object> testenforceTestsWithKey = new LinkedHashMap<Integer, Object>();
		Map<Integer, Object> testCommentsTestsWithKey = new LinkedHashMap<Integer, Object>();
		List<Map<String, Object>> testenforceTestsParameter = new ArrayList<>();
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		RegistrationTest systemGeneratedTestMap = new RegistrationTest();
		ResultParameter ResultParameterMap = new ResultParameter();
		List<Map<String, Object>> lsttestcommentOutcome = new ArrayList<>();
		List<Integer> lstntestgrouptestparametercode = new ArrayList<>();
		List<Map<String, Object>> multipleRuleSameParamCodes = new ArrayList<>();
		List<Boolean> lstRuleboolean = new ArrayList<>();
		List<Boolean> lstGroupboolean = new ArrayList<>();
		List<Boolean> lstboolNumericParam = new ArrayList<>();
		List<TestGroupRulesEngine> lstTestGroupRulesEngine = new ArrayList<>();
		List<Integer> lstCheckRepeatTestCode = new ArrayList<>();
		List<Integer> lstCheckSampleCode = new ArrayList<>();
		String sntestgrouprulesenginecode = "";
		Map<Integer, String> rulesBasedOnSample = new LinkedHashMap<Integer, String>();
		List<RegistrationTest> lstruleRepeatTest = new ArrayList<>();
		int npreregno = 0;
		List<RegistrationTest> lstSatisfiedTransRulesTests = new ArrayList<>();
		String rtnstr = "Rules read Successfully";

		ObjectMapper objectMapper = new ObjectMapper();
		int nparametertypecode = 0;
		boolean readOutcome = false;
		boolean isnumericParameter = false;
		boolean isApplicableforReadingRule = false;
		Map<String, Object> objoutCome = new LinkedHashMap<String, Object>();
		List<Map<String,Object>> newSampleTest = new ArrayList<>();
		final Map<String, Object> registrationDatas = getRegistrationDiagnosticCaseType(inputMap, userInfo);
//		String sampleParameters = "select rp.jsondata,rp.jsondata->>'sresult' as sresult,rp.jsondata->>'sfinal' as sfinal, "
//				+ "  rp.ntestgrouptestparametercode,rp.nunitcode,rp.ngradecode,rp.ntransactiontestcode,rp.ntransactionresultcode,rp.npreregno, "
//				+ "	  rp.nresultmandatory, "
//				+ "	   rt.ntestgrouptestcode,rp.ntransactionstatus from resultparameter rp,registrationtest rt where rp.ntransactiontestcode in (select  ntransactiontestcode from "
//				+ " registrationtest where ntransactionsamplecode in ( select  ntransactionsamplecode from registrationsample where npreregno in( "
//				+ inputMap.get("npreregno") + ")))" + " and rp.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ "  and rp.ntransactiontestcode=rt.ntransactiontestcode";
		String sampleParameters = "select rt.ntransactionsamplecode,rp.jsondata,rp.jsondata->>'sresult' as sresult,rp.jsondata->>'sfinal' as sfinal, "
				+ "  rp.ntestgrouptestparametercode,rp.nunitcode,rp.ngradecode,rp.ntransactiontestcode,rp.ntransactionresultcode,rp.npreregno, "
				+ "	  rp.nresultmandatory, "
				+ "	   rt.ntestgrouptestcode,rp.ntransactionstatus from resultparameter rp,registrationtest rt where rp.ntransactiontestcode in (select  ntransactiontestcode from "
				+ " registrationtest where ntransactionsamplecode in ( select  ntransactionsamplecode from registrationtest where ntransactiontestcode in( "
				+ stransactiontestcode + ")))" + " and rp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and rp.ntransactiontestcode=rt.ntransactiontestcode";
		List<ResultParameter> lstresultparam = jdbcTemplate.query(sampleParameters, new ResultParameter());

		String transactionTests = "select max(rth.ntransactionstatus) as ntransactionstatus ,rt.jsondata->>'stestsynonym' as stestsynonym,rt.* from registrationtest rt, registrationtesthistory rth "
				+ "where rt.ntransactionsamplecode in ( " + transactionSampleCode + ") and rt.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ " group by rth.ntransactiontestcode,rt.ntransactiontestcode, rt.ntransactionsamplecode, rt.npreregno,rt.ntestgrouptestcode, "
				+ " rt.ntestcode,rt.nsectioncode,rt.nmethodcode,rt.ninstrumentcatcode,rt.nchecklistversioncode,rt.ntestrepeatno,rt.ntestretestno, "
				+ " rt.jsondata,rt.dmodifieddate,rt.nsitecode";
		final List<RegistrationTest> lsttransactiontest = jdbcTemplate.query(transactionTests, new RegistrationTest());

		String onGoingTransactionTests = "select max(rth.ntransactionstatus) as ntransactionstatus ,rt.jsondata->>'stestsynonym' as stestsynonym"
				+ " ,rt.*"
//				+ "    COALESCE ((select ntestgrouprulesenginecode from  "
//				+ "				  testgrouprulesengine where ntestgrouptestcode=rt.ntestgrouptestcode "
//				+ "				 and  ntransactionstatus="
//				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "),-1) "
//				+ "	   as ntestgrouprulesenginecode "
				+ " from registrationtest rt, registrationtesthistory rth " + "where rt.ntransactiontestcode in ("
				+ stransactiontestcode + ") and rt.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.ntransactiontestcode=rth.ntransactiontestcode "
				+ " group by rth.ntransactiontestcode,rt.ntransactiontestcode, rt.ntransactionsamplecode, rt.npreregno,rt.ntestgrouptestcode, "
				+ " rt.ntestcode,rt.nsectioncode,rt.nmethodcode,rt.ninstrumentcatcode,rt.nchecklistversioncode,rt.ntestrepeatno,rt.ntestretestno, "
				+ " rt.jsondata,rt.dmodifieddate,rt.nsitecode";
		final List<RegistrationTest> lstonGoingTransactionTests = jdbcTemplate.query(onGoingTransactionTests,
				new RegistrationTest());

		String query = "select * from testgrouprulesengine where ntestgrouptestcode in (select  rt.ntestgrouptestcode from "
				+ " registrationtest rt where rt.ntransactiontestcode in (" + stransactiontestcode + ")"
				+ "  and  (select max(rth.ntransactionstatus) from registrationtesthistory rth "
				+ "	 where rth.ntransactiontestcode=rt.ntransactiontestcode group by rth.ntransactiontestcode )="
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ") and  ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";
		final List<TestGroupRulesEngine> lstTestGroupAllRules = jdbcTemplate.query(query, new TestGroupRulesEngine());
		List<RegistrationTest> lstTransRulesTests = lstonGoingTransactionTests.stream()
				.filter(x -> lstTestGroupAllRules.stream()
						.anyMatch(y -> y.getNtestgrouptestcode() == x.getNtestgrouptestcode()))
				.collect(Collectors.toList());
		final List<Integer> lstTransRulesTestsSampleCode = lstTransRulesTests.stream()
				.map(x -> x.getNtransactionsamplecode()).distinct().collect(Collectors.toList());
		// if (lstTestGroupRulesEngine.size() > 0) {
		for (int t = 0; t < lstTransRulesTests.size(); t++) {
			// Get Current Test SampleCode
			final int currentSampleCode = lstTransRulesTests.get(t).getNtransactionsamplecode();
			final int currenttrasnsactionTestCode = lstTransRulesTests.get(t).getNtransactiontestcode();

			// we are Maintining Sample Code in a List to clear testgrouptestcode for A new
			// Sample
			if (!lstCheckSampleCode.contains(currentSampleCode)) {
				lstCheckRepeatTestCode.clear();
				lstCheckSampleCode.add(currentSampleCode);
			}

			// Get Current Test Object
			RegistrationTest objTest = lstTransRulesTests.get(t);
			// Get Current Test's Rule if Applicable
			final List<TestGroupRulesEngine> lstTestGroupRule = lstTestGroupAllRules.stream()
					.filter(x -> x.getNtestgrouptestcode() == objTest.getNtestgrouptestcode())
					.collect(Collectors.toList());

			for (int q = 0; q < lstTestGroupRule.size(); q++) {
				final int index = q;
				// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>For Collecting Multiple Approved
				// Rules For A particular Test<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
				if (!lstCheckRepeatTestCode.contains(lstTestGroupRule.get(index).getNtestgrouptestcode())) {
					lstCheckRepeatTestCode.add(lstTestGroupRule.get(index).getNtestgrouptestcode());
				} else {
					continue;
				}
				lstTestGroupRulesEngine = lstTestGroupRule.stream()
						// .filter(x -> x.getNtestgrouptestcode() ==
						// lstTestGroupRule.get(index).getNtestgrouptestcode())
						.sorted(Comparator.comparingInt(TestGroupRulesEngine::getNruleexecorder))
						.collect(Collectors.toList());

				for (int r = 0; r < lstTestGroupRulesEngine.size(); r++) {
					lstgroup = (List<Map<String, Object>>) lstTestGroupRulesEngine.get(r).getJsondata();
					final int ntestgrouprulesenginecode = lstTestGroupRulesEngine.get(r).getNtestgrouprulesenginecode();
					objoutCome = (Map<String, Object>) lstTestGroupRulesEngine.get(r).getJsonuidata();
					groupListJoins = (List<Map<String, Object>>) objoutCome.get("groupListJoins");
					int ntestgrouptestcode = lstTestGroupRulesEngine.get(r).getNtestgrouptestcode();
					ResultParameterMap = lstresultparam.stream()
							.filter(x -> x.getNtestgrouptestcode() == ntestgrouptestcode).findAny().get();
					final List<Integer> lsttestsInvolvedInRules = (List<Integer>) objoutCome
							.get("testsInvolvedInRules");
					/*
					 * if (isRuleDependent) { List<RegistrationTest> lstReturnTestsforAlert =
					 * lsttransactiontest.stream() .filter(ignoreTest ->
					 * ignoreTest.getNtestgrouptestcode() != ntestgrouptestcode) .filter(test ->
					 * test.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
					 * .gettransactionstatus() ? test.getNtransactionstatus() !=
					 * Enumeration.TransactionStatus.COMPLETED .gettransactionstatus() :
					 * test.getNtransactionstatus() == test.getNtransactionstatus())
					 * .filter(incompleteTest -> lsttestsInvolvedInRules
					 * .contains(incompleteTest.getNtestgrouptestcode()))
					 * .collect(Collectors.toList()); lstReturnTestsforAlert =
					 * lstReturnTestsforAlert.stream() .filter(x ->
					 * !lstonGoingTransactionTests.stream() .anyMatch(y -> x.getNtestgrouptestcode()
					 * == y.getNtestgrouptestcode())) .collect(Collectors.toList()); if
					 * (lstReturnTestsforAlert.size() > 0) { isApplicableforReadingRule = true; }
					 * else { isApplicableforReadingRule = false; }
					 * returnMap.put("ruleAppliedntestgrouptestcode", ntestgrouptestcode);
					 * returnMap.put("isApplicableforReadingRule", isApplicableforReadingRule);
					 * returnMap.put("RegistrationTestAlert", lstReturnTestsforAlert); return new
					 * ResponseEntity<>(returnMap, HttpStatus.OK); }
					 */
					// if(ResultParameterMap.getSfinal()==null) {
					// //No need to Read Rule if the Rules Applied Test Results Not Entered
					// //return new ResponseEntity<>(returnMap, HttpStatus.OK);
					// continue;
					// }
					for (int i = 0; i < lstgroup.size(); i++) {
						Map<String, Object> group = lstgroup.get(i);
						if (group.containsKey("button_and")) {
							lstrules = (List<Map<String, Object>>) group.get("button_and");
						}
						if (group.containsKey("button_not_button_and")) {
							lstrules = (List<Map<String, Object>>) group.get("button_not_button_and");
						}
						if (group.containsKey("button_or")) {
							lstrules = (List<Map<String, Object>>) group.get("button_or");
						}
						if (group.containsKey("button_not_button_or")) {
							lstrules = (List<Map<String, Object>>) group.get("button_not_button_or");
						}
						for (int j = 0; j < lstrules.size(); j++) {
							objrule = lstrules.get(j);
							String ruleStatement = "";
							boolean isruleSatisfied = false;
							int orderresulttype = (int) ((Map<String, Object>) objrule.get("orderresulttype"))
									.get("value");

							int ntestgrouptestparametercode = (Integer) ((Map<String, Object>) objrule.get("stestname"))
									.get("value");

							String ssymbolname = ((Map<String, Object>) objrule.get("ssymbolname")).get("label")
									.toString();
							// For Comparing Result with Parameter under Sample
							List<ResultParameter> bothlsthasSameCode = lstresultparam.stream()
									.filter(x -> ((ResultParameter) x).getNtransactionsamplecode() == currentSampleCode)
									.filter(y -> ((ResultParameter) y)
											.getNtestgrouptestparametercode() == ntestgrouptestparametercode)
									.collect(Collectors.toList());

							multipleRuleSameParamCodes = lstrules.stream()
									.filter(rule -> ((int) ((Map<String, Object>) rule.get("stestname"))
											.get("value")) == ntestgrouptestparametercode)
									.filter(rule -> ((int) ((Map<String, Object>) rule.get("orderresulttype"))
											.get("value")) == 3)
									.collect(Collectors.toList());

							for (int k = 0; k < bothlsthasSameCode.size(); k++) {
								if (orderresulttype == 1) {
									int rulendiagnosticcasecode = (int) ((Map<String, Object>) objrule
											.get("ndiagnosticcasecode")).get("value");
									Map<String, Object> registrationData = ((Map<String, Object>) registrationDatas
											.get(String.valueOf(bothlsthasSameCode.get(k).getNpreregno())));
									int resultdiagnosticcasecode = registrationData.containsKey("ndiagnosticcasecode")
											? (int) registrationData.get("ndiagnosticcasecode")
											: 0;
									if (ssymbolname.equals("equals(=)")) {
										if (resultdiagnosticcasecode == rulendiagnosticcasecode) {
											isruleSatisfied = true;
										} else {
											isruleSatisfied = false;
										}
									} else if (ssymbolname.equals("!=")) {
										if (resultdiagnosticcasecode != rulendiagnosticcasecode) {
											isruleSatisfied = true;
										} else {
											isruleSatisfied = false;
										}
									}
									lstRuleboolean.add(isruleSatisfied);
								} else if (orderresulttype == 2) {
									int rulengradecode = (int) ((Map<String, Object>) objrule.get("ngradecode"))
											.get("value");
									int resultngradecode = (int) bothlsthasSameCode.get(k).getNgradecode();

									if (ssymbolname.equals("equals(=)")) {
										if (resultngradecode == rulengradecode) {
											isruleSatisfied = true;
										} else {
											isruleSatisfied = false;
										}
									} else if (ssymbolname.equals("!=")) {
										if (resultngradecode != rulengradecode) {
											isruleSatisfied = true;
										} else {
											isruleSatisfied = false;
										}
									}
									lstRuleboolean.add(isruleSatisfied);

								} else {
									nparametertypecode = (int) ((Map<String, Object>) ((Map<String, Object>) objrule
											.get("stestname")).get("item")).get("nparametertypecode");
									if (nparametertypecode == Enumeration.ParameterType.CHARACTER.getparametertype()) {
										String rulesfinal = (String) objrule.get("sfinal");
										String resultsfinal = bothlsthasSameCode.get(k).getSfinal();
										if (ssymbolname.equals("equals(=)")) {
											if (resultsfinal.equals(rulesfinal)) {
												isruleSatisfied = true;
											} else {
												isruleSatisfied = false;
											}
										} else if (ssymbolname.equals("!=")) {
											if (!resultsfinal.equals(rulesfinal)) {
												isruleSatisfied = true;
											} else {
												isruleSatisfied = false;
											}
										}
										lstRuleboolean.add(isruleSatisfied);

									} else if (nparametertypecode == Enumeration.ParameterType.PREDEFINED
											.getparametertype()) {
										String rulesfinal = (String) objrule.get("sfinal");
										String resultsfinal = bothlsthasSameCode.get(k).getSresult();
										if (ssymbolname.equals("equals(=)")) {
											if (resultsfinal.equals(rulesfinal)) {
												isruleSatisfied = true;
											} else {
												isruleSatisfied = false;
											}
										} else if (ssymbolname.equals("!=")) {
											if (!resultsfinal.equals(rulesfinal)) {
												isruleSatisfied = true;
											} else {
												isruleSatisfied = false;
											}
										}
										lstRuleboolean.add(isruleSatisfied);
									} else if (nparametertypecode == Enumeration.ParameterType.ATTACHEMENT
											.getparametertype()) {
										String rulesfinal = (String) objrule.get("sfinal");
										String resultsfinal = bothlsthasSameCode.get(k).getSfinal();
										if (ssymbolname.equals("equals(=)")) {
											if (resultsfinal.equals(rulesfinal)) {
												isruleSatisfied = true;
											} else {
												isruleSatisfied = false;
											}
										} else if (ssymbolname.equals("!=")) {
											if (!resultsfinal.equals(rulesfinal)) {
												isruleSatisfied = true;
											} else {
												isruleSatisfied = false;
											}
										}
										lstRuleboolean.add(isruleSatisfied);
									} else {
										// >>>>>>>>>>>>>Numeric Type<<<<<<<<<<<<<<<<<<
										double rulesfinalnum = Double.parseDouble(objrule.get("sfinal").toString());
										ResultParameter resultObject = bothlsthasSameCode.get(k);
										double resultfinal = 0.0;
										String rulesSymbol = "";
										double rulesresultfinal = 0.0;
										if (group.containsKey("button_and")
												|| group.containsKey("button_not_button_and")) {
											resultfinal = Double.parseDouble(resultObject.getSfinal());
											for (int l = 0; l < multipleRuleSameParamCodes.size(); l++) {
												rulesSymbol = ((Map<String, Object>) multipleRuleSameParamCodes.get(l)
														.get("ssymbolname")).get("label").toString();
												rulesresultfinal = Double.parseDouble(
														multipleRuleSameParamCodes.get(l).get("sfinal").toString());
												if (rulesSymbol.equals("equals(=)")) {
													if (resultfinal == rulesresultfinal) {
														isruleSatisfied = true;
													} else {
														isruleSatisfied = false;
													}
												} else if (rulesSymbol.equals("!=")) {
													if (resultfinal != rulesresultfinal) {
														isruleSatisfied = true;
													} else {
														isruleSatisfied = false;
													}
												} else if (rulesSymbol.equals("<")) {
													if (resultfinal < rulesresultfinal) {
														isruleSatisfied = true;
													} else {
														isruleSatisfied = false;
													}
												} else if (rulesSymbol.equals(">")) {
													if (resultfinal > rulesresultfinal) {
														isruleSatisfied = true;
													} else {
														isruleSatisfied = false;
													}
												} else if (rulesSymbol.equals("<=")) {
													if (resultfinal <= rulesresultfinal) {
														isruleSatisfied = true;
													} else {
														isruleSatisfied = false;
													}
												} else {
													if (resultfinal >= rulesresultfinal) {
														isruleSatisfied = true;
													} else {
														isruleSatisfied = false;
													}
												}
												lstboolNumericParam.add(isruleSatisfied);
											}
											if (!lstboolNumericParam.contains(false)) {
												isruleSatisfied = true;
											} else {
												isruleSatisfied = false;
											}
											lstboolNumericParam.clear();
										} else {
											resultfinal = Double.parseDouble(resultObject.getSfinal());
											if (ssymbolname.equals("equals(=)")) {
												if (resultfinal == rulesfinalnum) {
													isruleSatisfied = true;
												}
											} else if (ssymbolname.equals("!=")) {
												if (resultfinal != rulesfinalnum) {
													isruleSatisfied = true;
												}
											} else if (ssymbolname.equals("<")) {
												if (resultfinal < rulesfinalnum) {
													isruleSatisfied = true;
												}
											} else if (ssymbolname.equals(">")) {
												if (resultfinal > rulesfinalnum) {
													isruleSatisfied = true;
												}
											} else if (ssymbolname.equals("<=")) {
												if (resultfinal <= rulesfinalnum) {
													isruleSatisfied = true;
												}
											} else {
												if (resultfinal >= rulesfinalnum) {
													isruleSatisfied = true;
												}
											}
										}

										lstRuleboolean.add(isruleSatisfied);
									}
								}
							}

						}
						if (group.containsKey("button_and")) {
							if (!lstRuleboolean.contains(false)) {
								lstGroupboolean.add(true);
							} else {
								lstGroupboolean.add(false);
							}

						}
						if (group.containsKey("button_not_button_and")) {
							if (!lstRuleboolean.contains(false)) {
								lstGroupboolean.add(false);
							} else {
								lstGroupboolean.add(true);
							}

						}
						if (group.containsKey("button_or")) {
							if (lstRuleboolean.contains(true)) {
								lstGroupboolean.add(true);
							} else {
								lstGroupboolean.add(false);
							}
						}
						if (group.containsKey("button_not_button_or")) {
							if (lstRuleboolean.contains(true)) {
								lstGroupboolean.add(false);
							} else {
								lstGroupboolean.add(true);
							}
						}
						lstRuleboolean.clear();
					}
					if (groupListJoins.size() > 0) {
						for (int i = 0; i < groupListJoins.size(); i++) {
							groupListJoin = groupListJoins.get(i);
							if (groupListJoin.containsKey("button_and")
									&& (boolean) groupListJoin.get("button_and") == true) {

								if (groupListJoin.containsKey("button_not")
										&& (boolean) groupListJoin.get("button_not") == true) {
									// Both AND and NOT True => NAND
									if (lstGroupboolean.get(i) != true && lstGroupboolean.get(i + 1) != true) {
										readOutcome = true;
									} else {
										readOutcome = false;
									}
								} else {
									// Only AND True
									if (lstGroupboolean.get(i) == true && lstGroupboolean.get(i + 1) == true) {
										readOutcome = true;
									} else {
										readOutcome = false;
									}
								}

							}

							if (groupListJoin.containsKey("button_or")
									&& (boolean) groupListJoin.get("button_or") == true) {
								if (groupListJoin.containsKey("button_not")
										&& (boolean) groupListJoin.get("button_not") == true) {
									// Both OR and NOT True => NOR
									if (lstGroupboolean.get(i) != true || lstGroupboolean.get(i + 1) != true) {
										readOutcome = true;
									} else {
										readOutcome = false;
									}
								} else {
									// Only OR True
									if (lstGroupboolean.get(i) == true || lstGroupboolean.get(i + 1) == true) {
										readOutcome = true;
									} else {
										readOutcome = false;
									}
								}

							}

						}
					} else {
						if (lstGroupboolean.size() > 0 && lstGroupboolean.get(0) == true) {
							readOutcome = true;
						} else {
							readOutcome = false;
						}
					}
					lstGroupboolean.clear();
					if (readOutcome) {
						// ((List<Map<String, Object>>) objoutCome.get("testCommentsTests")).
						// forEach(x->((Map<String, Object>)x).put("ntestgrouprulesenginecode",
						// ntestgrouprulesenginecode));;
						/*
						 * if(testInitiateTests.size()>0) { List<TestGroupTest>
						 * outcometestInitiateTests=(List<TestGroupTest>)
						 * objoutCome.get("testInitiateTests"); for(int
						 * o=0;o<outcometestInitiateTests.size();o++) { TestGroupTest
						 * outcomeTestInitiateTestsObj=objectMapper.convertValue(
						 * outcometestInitiateTests.get(o),new TypeReference<TestGroupTest>(){});
						 * if(testInitiateTests.stream()
						 * .filter(x->x.getNtestgrouptestcode()==outcomeTestInitiateTestsObj.
						 * getNtestgrouptestcode()).findAny().isPresent()) { TestGroupTest
						 * tempObj=testInitiateTests.stream()
						 * .filter(x->x.getNtestgrouptestcode()==outcomeTestInitiateTestsObj.
						 * getNtestgrouptestcode()).findAny().get(); testInitiateTests.remove(tempObj);
						 * tempObj.setNrepeatcountno((short)(tempObj.getNrepeatcountno()+2));
						 * testInitiateTests.add(tempObj); }else {
						 * testInitiateTests.add(outcomeTestInitiateTestsObj); } } }else {
						 * testInitiateTests.addAll(objectMapper.convertValue(objoutCome.get(
						 * "testInitiateTests"),new TypeReference<List<TestGroupTest>>(){})); }
						 */
						lstSatisfiedTransRulesTests.add(objTest);

						sntestgrouprulesenginecode += sntestgrouprulesenginecode
								+ String.valueOf(lstTestGroupRulesEngine.get(r).getNtestgrouprulesenginecode()) + ",";
						lstruleRepeatTest = lstonGoingTransactionTests.stream()
								.filter(x -> x.getNtestgrouptestcode() == ntestgrouptestcode)
								.collect(Collectors.toList());
						// if Multiple Tests have Same Rule-->Outcome will be Same
//						if (lstruleRepeatTest.size() > 0) {
//							for (int l = 0; l < lstruleRepeatTest.size(); l++) {
//								testInitiateTests.addAll((List<TestGroupTest>) objoutCome.get("testInitiateTests"));
//								testRepeatTests.addAll((List<TestGroupTest>) objoutCome.get("testRepeatTests"));
//							}
//						} else {
						testInitiateTests.addAll((List<TestGroupTest>) objoutCome.get("testInitiateTests"));
						testRepeatTests.addAll((List<TestGroupTest>) objoutCome.get("testRepeatTests"));
						
						
						newSampleTest.addAll(((List<Map<String,Object>>) objoutCome.get("testInitiateTests")).stream()
						        .filter(x -> (int)x.get("nneedsample")== Enumeration.TransactionStatus.YES.gettransactionstatus())
						        .collect(Collectors.toList()));

						// End of Collecting Data for Test Initiate And Test Repeat Outcome
						List<Map<String, Object>> lstTemp = (List<Map<String, Object>>) objoutCome
								.get("testCommentsTests");
//						lstTemp.stream().forEach(x->{
//							x.put("ntransactionsamplecode", currentSampleCode);
//							x.put("ntransactiontestcode", currenttrasnsactionTestCode);
//						}) ;
//						for(int i = 0; i < lstTemp.size(); i++) {
//							Map<String, Object> testcommenttest=lstTemp.get(i);
//							testcommenttest.put("ntransactionsamplecode", currentSampleCode);
//							testcommenttest.put("ntransactiontestcode", currenttrasnsactionTestCode);
//							testCommentsTests.add(testcommenttest);
//						} 
						testCommentsTestsWithKey.put(currenttrasnsactionTestCode, lstTemp);
						// End Of Collecting Data for Test Comments Outcome
						lstTemp = (List<Map<String, Object>>) objoutCome.get("testenforceTests");
//						lstTemp.stream().forEach(x->{
//							x.put("ntransactionsamplecode", currentSampleCode);
//							x.put("ntransactiontestcode", currenttrasnsactionTestCode);
//						}) ;
//						for(int i = 0; i < lstTemp.size(); i++) {
//							Map<String, Object> testenforceTest=lstTemp.get(i);
//							testenforceTest.put("ntransactionsamplecode", currentSampleCode);
//							testenforceTest.put("ntransactiontestcode", currenttrasnsactionTestCode);
//							testenforceTest.put(String.valueOf(currenttrasnsactionTestCode), lstTemp); 
//							testenforceTestsParameter.add(testenforceTest);
//						} 
						testenforceTestsWithKey.put(currenttrasnsactionTestCode, lstTemp);
//						testenforceTestsParameter
//								.addAll(lstTemp);
						// End Of Collecting Data for Test Enforce Outcome

						// Break if one of Multiple Approved Rules Satisfied-Continue Outer LOOP
						break;
					}
				}
			}
		}
		// Outcome Method Calls Start
		List<TestGroupTest> lstTestGroupTest = objectMapper.convertValue(testInitiateTests,
				new TypeReference<List<TestGroupTest>>() {
				});
		lstTestGroupTest.addAll(objectMapper.convertValue(testRepeatTests, new TypeReference<List<TestGroupTest>>() {
		}));
		if (lstTestGroupTest.size() > 0) {
			List<RegistrationSubType> lstRegistrationSubType = new ArrayList<RegistrationSubType>();
			try {
				lstRegistrationSubType = transactionDAOSupport.getRegistrationSubType((int) inputMap.get("nregtypecode"),
						userInfo);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			RegistrationSubType objRegistrationSubType = lstRegistrationSubType.stream()
					.filter(x -> x.getNregsubtypecode() == (int) inputMap.get("nregsubtypecode")).findAny().get();
			List<String> listSample = new ArrayList<>();
			List<Map<String, Object>> lstclinicaltype = new ArrayList<>();

			inputMap.put("nneedjoballocation", objRegistrationSubType.isNneedjoballocation());
			inputMap.put("TestGroupTest", lstTestGroupTest);
			inputMap.put("RegistrationSample", listSample);
			// inputMap.put("nfilterstatus", 0);
			 Map<String, Object> mainSampleMapping = new HashMap<String, Object>();
			for (int i = 0; i < lstSatisfiedTransRulesTests.size(); i++) {
				if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()) {

					Map<String, Object> clinicaltypeMap = new LinkedHashMap<String, Object>();
					Map<String, Object> registrationData = (Map<String, Object>) registrationDatas
							.get(String.valueOf(lstSatisfiedTransRulesTests.get(i).getNpreregno()));
					clinicaltypeMap.put("nage", (int) registrationData.get("nage"));
					clinicaltypeMap.put("npreregno", lstSatisfiedTransRulesTests.get(i).getNpreregno());
					clinicaltypeMap.put("ngendercode", registrationData.get("ngendercode"));
					lstclinicaltype.add(clinicaltypeMap);
					inputMap.put("ageData", lstclinicaltype);
				}
				listSample.add(String.valueOf(lstSatisfiedTransRulesTests.get(i).getNtransactionsamplecode()));
			}
			// extra parameters
			// String
			// sntestgrouprulesenginecode=lstTestGroupRule.stream().map(x->String.valueOf(x.getNtestgrouprulesenginecode())).collect(Collectors.joining(","));
			inputMap.put("ntestgrouprulesenginecode",
					sntestgrouprulesenginecode.substring(0, sntestgrouprulesenginecode.length() - 1));
			inputMap.put("RulesEngineGet", true);
			
			if(newSampleTest.size()>0) {
				List<Map<String,Object>> AvailableSample= new ArrayList();
				List<TestGroupTest> lstTestGroupTests = objectMapper.convertValue(newSampleTest,
						new TypeReference<List<TestGroupTest>>() {
						});
				inputMap.put("testgrouptest", lstTestGroupTests);
				inputMap.put("AvalSample",listSample);
				returnMap.putAll((Map<String, Object>) createSubSample( inputMap).getBody());
				inputMap.put("sampleCode",returnMap.get("ntransactionsamplecode"));
				listSample.add(((List<RegistrationSampleHistory>)returnMap.get("AvailableSampleHistory"))
						.stream().map(x -> String.valueOf(x.getNtransactionsamplecode()))
						.collect(Collectors.joining(",")));
				inputMap.putAll(returnMap);
				//ALPD-4828
                //Result entry-->sample disappeared when complete the single test
				inputMap.put("getNewSample",true);
			}

			try {
				returnMap.putAll((Map<String, Object>) createTestRulesEngine(userInfo, listSample, lstTestGroupTest,
						(int) inputMap.get("nregtypecode"), (int) inputMap.get("nregsubtypecode"), inputMap).getBody());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// End of Creating Test and Test Repeat Outcome
//		testCommentsTests = (List<Map<String, Object>>) testCommentsTests
//				.stream().filter(
//						x -> lsttransactiontest.stream()
//								.anyMatch(y -> String.valueOf(y.getNtestgrouptestcode())
//										.equals(String.valueOf(x.get("ntestgrouptestcode")))))
//				.collect(Collectors.toList());
		stransactiontestcode = "";
		// if (testCommentsTests.size() > 0) {

		for (int t = 0; t < lstSatisfiedTransRulesTests.size(); t++) {
			final int currentSampleCode = lstSatisfiedTransRulesTests.get(t).getNtransactionsamplecode();
			final int currenttrasnsactionTestCode = lstSatisfiedTransRulesTests.get(t).getNtransactiontestcode();
			if (testCommentsTestsWithKey.containsKey(currenttrasnsactionTestCode)) {
				testCommentsTests = (List<Map<String, Object>>) testCommentsTestsWithKey
						.get(currenttrasnsactionTestCode);
				for (int i = 0; i < testCommentsTests.size(); i++) {
					Map<String, Object> testCommentsTestsMap = testCommentsTests.get(i);
					// Need to put comments for Tests under Sample of the rules Applied Test
//					if((Integer)testCommentsTestsMap.get("ntransactionsamplecode") ==currentSampleCode
//							&&(Integer)testCommentsTestsMap.get("ntransactiontestcode") ==currenttrasnsactionTestCode
//							) {
					List<RegistrationTest> lstCompletedTests = lsttransactiontest.stream()
							.filter(x -> x.getNtransactionsamplecode() == currentSampleCode)
							//Commented on 1-04-2023 start
							//.filter(x -> x.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
							//		.gettransactionstatus())
							//Commented on 1-04-2023 end
							.filter(x -> String.valueOf(x.getNtestgrouptestcode())
									.equals(String.valueOf(testCommentsTestsMap.get("ntestgrouptestcode"))))
							.collect(Collectors.toList());
					for (int j = 0; j < lstCompletedTests.size(); j++) {
						// systemGeneratedTestMap = lsttransactiontest.stream()
//						.filter(x -> String.valueOf(x.getNtestgrouprulesenginecode())
//								.equals(String.valueOf(testCommentsTestsMap.get("ntestgrouprulesenginecode"))))
						// .filter(x -> String.valueOf(x.getNtestgrouptestcode())
						// .equals(String.valueOf(testCommentsTestsMap.get("ntestgrouptestcode"))))
						// .findAny().get();
						systemGeneratedTestMap = lstCompletedTests.get(j);
						List<Map<String, Object>> commentsArray = (List<Map<String, Object>>) testCommentsTestsMap
								.get("commentsArray");
						stransactiontestcode += systemGeneratedTestMap.getNtransactiontestcode() + ",";
						for (int k = 0; k < commentsArray.size(); k++) {
							Map<String, Object> comments = commentsArray.get(k);
							Map<String, Object> testcommentsMap = new LinkedHashMap<String, Object>();
							testcommentsMap.put("npreregno", systemGeneratedTestMap.getNpreregno());
							testcommentsMap.put("ntransactionsamplecode",
									systemGeneratedTestMap.getNtransactionsamplecode());
							testcommentsMap.put("ntransactiontestcode",
									systemGeneratedTestMap.getNtransactiontestcode());
							testcommentsMap.put("nformcode", userInfo.getNformcode());
							testcommentsMap.put("nusercode", userInfo.getNusercode());
							testcommentsMap.put("nuserrolecode", userInfo.getNuserrole());
							testcommentsMap.put("nsamplecommentscode", -1);
							testcommentsMap.put("ntestcommentcode", 0);
							testcommentsMap.put("nstatus", 1);
							testcommentsMap.put("jsondata", comments);
							lsttestcommentOutcome.add(testcommentsMap);
						}
					}
					// }
				}
			}
		}
		if (lsttestcommentOutcome.size() > 0) {
			final List<RegistrationTestComment> listTestComment = objectMapper.convertValue(lsttestcommentOutcome,
					new TypeReference<List<RegistrationTestComment>>() {
					});
			objCommentService.createTestComment(listTestComment,
					stransactiontestcode.substring(0, stransactiontestcode.length() - 1), userInfo);
		}
		// }
		// End of Creating Test Comments Outcome
		testenforceTestsParameter = (List<Map<String, Object>>) testenforceTestsParameter.stream()
				.filter(x -> lstresultparam.stream()
						.anyMatch(y -> String.valueOf(y.getNtestgrouptestparametercode())
								.equals(String.valueOf(x.get("ntestgrouptestparametercode")))))
				.collect(Collectors.toList());
		// if (testenforceTestsParameter.size() > 0) {
		updateEnforceResult(testenforceTestsWithKey, lstresultparam, lstSatisfiedTransRulesTests, userInfo);
		// }
		// End of Creating Enforce Test
		System.out.print(rtnstr);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	public void updateEnforceResult(Map<Integer, Object> testenforceTestsWithKey, List<ResultParameter> lstresultparam,
			List<RegistrationTest> lstTransRulesTests, UserInfo userInfo) throws Exception {
		final String lockquery = "lock lockresultupdate" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(lockquery);

		ResultParameter ResultParameterMap = new ResultParameter();
		List<Map<String, Object>> testenforceTestsParameter = new ArrayList<Map<String, Object>>();
		List<Integer> lstntransactionresultcode = new ArrayList<Integer>();
		int ntransactionresultcode = 0;
		String resultChangeHistoryInsert = "";
		String resultChangeHistoryInsertValues = "";
		String seqUpdateQuery = "";
		String querys = "";
		String updatequery = "";
		String insertApprovalParam = "";
		String insertApprovalParamValues = "";
		final String resultChangeSeqNoGet = "select nsequenceno from seqnoregistration where stablename=N'resultchangehistory';";
		final String insertApprovalParamSeqNoGet = "select max(ntransactionresultcode) from approvalparameter";
		int resultChangeSeqNo = jdbcTemplate.queryForObject(resultChangeSeqNoGet, Integer.class);
		
		for (int k = 0; k < lstTransRulesTests.size(); k++) {
			final int currentSampleCode = lstTransRulesTests.get(k).getNtransactionsamplecode();
			final int currenttrasnsactionTestCode = lstTransRulesTests.get(k).getNtransactiontestcode();
			if (testenforceTestsWithKey.containsKey(currenttrasnsactionTestCode)) {
				testenforceTestsParameter = (List<Map<String, Object>>) testenforceTestsWithKey
						.get(currenttrasnsactionTestCode);
				for (int i = 0; i < testenforceTestsParameter.size(); i++) {

					JSONObject jsonobject = new JSONObject();
					JSONObject jsonobjectApprovalparam = new JSONObject();
					Map<String, Object> testenforceTestsParameterMap = testenforceTestsParameter.get(i);

					List<ResultParameter> lstCompletedresultparam = lstresultparam.stream()
							.filter(z -> z.getNtransactionsamplecode() == currentSampleCode)

							.filter(x -> String.valueOf(x.getNtestgrouptestparametercode()).equals(
									String.valueOf(testenforceTestsParameterMap.get("ntestgrouptestparametercode"))))
							.collect(Collectors.toList());
					for (int j = 0; j < lstCompletedresultparam.size(); j++) {

						ResultParameterMap = lstCompletedresultparam.get(j);

						if (ResultParameterMap.getNresultmandatory() == Enumeration.TransactionStatus.NO
								.gettransactionstatus()
								&& (ResultParameterMap.getSfinal() == null
										|| ResultParameterMap.getSfinal().isEmpty())) {
							updatequery += "update resultparameter set ntransactionstatus="
									+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
									+ " where   nsitecode = " + userInfo.getNtranssitecode()
									+ " and ntransactionresultcode in ("
									+ ResultParameterMap.getNtransactionresultcode() + ");";
							jsonobjectApprovalparam = new JSONObject(ResultParameterMap.getJsondata());
							jsonobjectApprovalparam.put("sfinal", testenforceTestsParameterMap.get("senforceresult"));
							jsonobjectApprovalparam.put("sresult", testenforceTestsParameterMap.get("senforceresult"));

							ntransactionresultcode = ResultParameterMap.getNtransactionresultcode();
							if (!lstntransactionresultcode.contains(ntransactionresultcode)) {
								lstntransactionresultcode.add(ntransactionresultcode);
								insertApprovalParamValues += " (" + ntransactionresultcode + ","
										+ ResultParameterMap.getNtransactiontestcode() + ","
										+ ResultParameterMap.getNpreregno() + "," + " "
										+ ResultParameterMap.getNtestgrouptestparametercode() + ", "
										+ ResultParameterMap.getNtestparametercode() + ","
										+ ResultParameterMap.getNtestgrouptestformulacode() + ","
										+ Enumeration.ParameterType.CHARACTER.getparametertype() + ","
										+ ResultParameterMap.getNcalculatedresult() + "," + " "
										+ testenforceTestsParameterMap.get("ngradecode") + ","
										+ ResultParameterMap.getNreportmandatory() + ","
										+ ResultParameterMap.getNresultmandatory() + ","
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",-1,-1," + " "
										+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ","
										+ ResultParameterMap.getNunitcode() + "," + userInfo.getNusercode() + ","
										+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
										+ userInfo.getNdeputyuserrole() + ", '"
										+stringUtilityFunction.replaceQuote(jsonobjectApprovalparam.toString()) + "',"
										+ userInfo.getNtranssitecode() + ", "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
							}

						}
						jsonobject.put("sfinal", testenforceTestsParameterMap.get("senforceresult"));
						jsonobject.put("sresult", testenforceTestsParameterMap.get("senforceresult"));
						jsonobject.put("dentereddate",dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", ""));
						jsonobject.put("noffsetdentereddate",dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));
						jsonobject.put("dentereddatetimezonecode", userInfo.getNtimezonecode());
						if (ResultParameterMap.getNresultmandatory() == Enumeration.TransactionStatus.NO
								.gettransactionstatus() && ResultParameterMap.getSfinal() != null) {
							resultChangeSeqNo++;
							resultChangeHistoryInsertValues += " (" + resultChangeSeqNo + ","
									+ ResultParameterMap.getNtransactionresultcode() + ","
									+ ResultParameterMap.getNtransactiontestcode() + ","
									+ ResultParameterMap.getNpreregno() + ","
									+ ResultParameterMap.getNtestgrouptestparametercode() + ","
									+ Enumeration.ParameterType.CHARACTER.getparametertype() + ","
									+ userInfo.getNformcode() + ","
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ","
									+ testenforceTestsParameterMap.get("ngradecode") + "," + userInfo.getNusercode()
									+ "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
									+ userInfo.getNdeputyuserrole() + ",'" +stringUtilityFunction.replaceQuote(jsonobject.toString()) + "',"
									+ userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
						}

						updatequery += "update resultparameter set nparametertypecode="
								+ Enumeration.ParameterType.CHARACTER.getparametertype()
								+ " ,jsondata=jsondata||jsonb_build_object('sfinal','"
								+ testenforceTestsParameterMap.get("senforceresult") + "','" + "sresult','"
								+ testenforceTestsParameterMap.get("senforceresult") + "')::jsonb,ngradecode="
								+ testenforceTestsParameterMap.get("ngradecode") + " where ntransactionresultcode="
								+ ResultParameterMap.getNtransactionresultcode() + ";";
						updatequery += "update approvalparameter set nparametertypecode="
								+ Enumeration.ParameterType.CHARACTER.getparametertype()
								+ " ,jsondata=jsondata||jsonb_build_object('sfinal','"
								+ testenforceTestsParameterMap.get("senforceresult") + "','" + "sresult','"
								+ testenforceTestsParameterMap.get("senforceresult") + "')::jsonb,ngradecode="
								+ testenforceTestsParameterMap.get("ngradecode") + " where ntransactionresultcode="
								+ ResultParameterMap.getNtransactionresultcode() + ";";
					}
					// }
				}
			}
		}
		if (resultChangeHistoryInsertValues.length() > 0) {
			resultChangeHistoryInsert = "insert into resultchangehistory (nresultchangehistorycode,ntransactionresultcode,ntransactiontestcode,"
					+ "npreregno,ntestgrouptestparametercode,nparametertypecode,nformcode,nenforceresult,"
					+ "nenforcestatus,ngradecode,nenteredby,nenteredrole,ndeputyenteredby,ndeputyenteredrole,jsondata,nsitecode,nstatus) "
					+ " values"
					+ resultChangeHistoryInsertValues.substring(0, resultChangeHistoryInsertValues.length() - 1) + ";";
			seqUpdateQuery = "update seqnoregistration  set nsequenceno=" + (resultChangeSeqNo)
					+ " where stablename=N'resultchangehistory';";
		}
		if (insertApprovalParamValues.length() > 0) {
			insertApprovalParam = "insert into approvalparameter (ntransactionresultcode,ntransactiontestcode,npreregno,ntestgrouptestparametercode,"
					+ " ntestparametercode,ntestgrouptestformulacode,nparametertypecode,ncalculatedresult,ngradecode,nreportmandatory,nresultmandatory,"
					+ " nenforcestatus,nenforceresult,nlinkcode,nattachmenttypecode,ntransactionstatus,nunitcode,nenteredby,"
					+ "  nenteredrole,ndeputyenteredby,ndeputyenteredrole, jsondata,nsitecode,nstatus)" + " values"
					+ insertApprovalParamValues.substring(0, insertApprovalParamValues.length() - 1) + ";";
		}
		querys = insertApprovalParam + resultChangeHistoryInsert + seqUpdateQuery + updatequery;
		jdbcTemplate.execute(querys);
	}

	public Map<String, Object> getRegistrationDiagnosticCaseType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
		int ndiagnosticcasecode = 0;
		int nage = 0;
		int ngendercode = 0;
		List<Registration> lstRegistration = jdbcTemplate.query("select * from registration where npreregno in (" + inputMap.get("npreregno") + ")",new Registration());
		
		for (int i = 0; i < lstRegistration.size(); i++) {
			Map<String, Object> objRegistration = lstRegistration.get(i).getJsondata();
			List<String> keys = new ArrayList<>(objRegistration.keySet());
			Map<String, Object> RegistrationMap = new LinkedHashMap<String, Object>();
			for (int j = 0; j < keys.size(); j++) {
				String key = keys.get(j);
				if (objRegistration.get(key).getClass().toString().equals("class java.util.HashMap") || objRegistration.get(key).getClass().toString().equals("class java.util.LinkedHashMap")) {
					Map<String, Object> object = (Map<String, Object>) objRegistration.get(key);
					if (object.containsKey("source") && object.get("source").equals("diagnosticcase")) { 
						ndiagnosticcasecode = (int) object.get("value");
						RegistrationMap.put("ndiagnosticcasecode", ndiagnosticcasecode);
					}
					if (object.containsKey("source") && object.get("source").equals("view_externalorder")) {
						int nexternalordercode= (int) object.get("value");
						String str="select  ndiagnosticcasecode from externalorder where  nexternalordercode="+nexternalordercode+";";
						ndiagnosticcasecode=jdbcTemplate.queryForObject(str, Integer.class);
						RegistrationMap.put("ndiagnosticcasecode", ndiagnosticcasecode);
					}
					if (object.containsKey("nage")) {
						nage = (int) object.get("nage");
						RegistrationMap.put("nage", nage);
					}
					if (object.containsKey("ngendercode")) {
						ngendercode = (int) object.get("ngendercode");
						RegistrationMap.put("ngendercode", ngendercode);
					}
				}
			}
			returnMap.put(String.valueOf(lstRegistration.get(i).getNpreregno()), RegistrationMap);
		}
		return returnMap;
	}

	public ResponseEntity<Object> createTestRulesEngine(UserInfo userInfo, List<String> listSample,
			List<TestGroupTest> listTest, int nregtypecode, int nregsubtypecode, Map<String, Object> inputMap)
			throws Exception {
		Map<String, Object> objmap = getCreateTestSequenceNo(userInfo, listSample, listTest, nregtypecode,
				nregsubtypecode, inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objmap);
			return createTest(userInfo, listSample, listTest, nregtypecode, nregsubtypecode, inputMap);
		} else {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	
	public ResponseEntity<Object> createSubSample(Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objmap1 = new HashMap<>();
		Map<String, Object> objmap = validateSeqnoSubSampleNo(inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objmap);
			objmap1 = createSubSamples(inputMap);
			return new ResponseEntity<>(objmap1, HttpStatus.OK);

		} else {
			if (objmap.containsKey("NeedConfirmAlert") && (Boolean) objmap.get("NeedConfirmAlert") == true) {
				return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
			} else {
				objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
				return new ResponseEntity<>(objmap1, HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	

	@SuppressWarnings("unchecked")
	public Map<String, Object> getCreateTestSequenceNo(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			Map<String, Object> inputMap) throws Exception {

		Map<String, Object> outputMap = new HashMap<String, Object>();
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode");

		boolean needJobAllocation = (boolean) inputMap.get("nneedjoballocation");

		final String inactiveTestQuery = " select tgt.stestsynonym, tm.ntestcode from testgrouptest tgt,"
				+ " testmaster tm where tgt.ntestgrouptestcode in (" + sntestgrouptestcode + ") "
				+ " and tgt.ntestcode=tm.ntestcode and tgt.nstatus=tm.nstatus and tm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.ntransactionstatus= "
				+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus();

		final List<TestGroupTest> inactiveTestList = jdbcTemplate.query(inactiveTestQuery, new TestGroupTest());
		if (inactiveTestList.isEmpty()) {
			String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final String sFindSubSampleQuery = "select npreregno, ntransactionstatus, ntransactionsamplecode from registrationsamplehistory"
					+ " where nsamplehistorycode = any (select max(nsamplehistorycode) from registrationsamplehistory where ntransactionsamplecode"
					+ " in (" + String.join(",", listSample) + ")  and nsitecode=" + userInfo.getNtranssitecode()
					+ " group by ntransactionsamplecode) and ntransactionstatus not in ("
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")  and nsitecode="
					+ userInfo.getNtranssitecode() + ";";

			List<RegistrationSampleHistory> listAvailableSample = jdbcTemplate.query(sFindSubSampleQuery,
					new RegistrationSampleHistory());

			if (!listAvailableSample.isEmpty()) {
				final String sFindSampleQuery = "select * from registrationhistory where nreghistorycode = "
						+ " any(select max(nreghistorycode) from registrationhistory rh,"
						+ " registrationsamplehistory rs  " + " where rs.ntransactionsamplecode in ("
						+ String.join(",", listSample) + ") and rh.npreregno=rs.npreregno and rs.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rh.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rh.nsitecode=rs.nsitecode and rs.nsitecode= " + userInfo.getNtranssitecode()
						+ " group by rh.npreregno,rs.ntransactionsamplecode) and ntransactionstatus "
						+ " != " + Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + "	and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode();

				List<RegistrationSampleHistory> releasedSampleList = jdbcTemplate.query(sFindSampleQuery,
						new RegistrationSampleHistory());
				if (!releasedSampleList.isEmpty()) {
					// Samples not yet released
					final String sApprovalStatusQry = "select apr.napprovalstatuscode ,acap.nneedautocomplete from approvalconfig ap,"
							+ " approvalconfigrole apr,approvalconfigversion apv,approvalconfigautoapproval acap"
							+ " where  ap.nregsubtypecode = " + nregsubtypecode + " and ap.nregtypecode = "
							+ nregtypecode
							+ " and ap.napprovalconfigcode = apv.napprovalconfigcode and acap.napprovalconfigversioncode=apv.napproveconfversioncode "
							+ " and apr.napproveconfversioncode = apv.napproveconfversioncode  " + " and acap.nstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and apv.ntransactionstatus = "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and ap.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and apr.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and apv.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by apr.nlevelno;";

					final List<ApprovalConfigRole> approvalConfigRole = jdbcTemplate.query(sApprovalStatusQry,
							new ApprovalConfigRole());
					int nApprovalStatus = -2;
					int nAutocomplete = Enumeration.TransactionStatus.NO.gettransactionstatus();
					if (!approvalConfigRole.isEmpty()) {
						nApprovalStatus = approvalConfigRole.get(0).getNapprovalstatuscode();
						nAutocomplete = approvalConfigRole.get(0).getNneedautocomplete();
					}
					boolean isApprovedSample = false;
					if (nApprovalStatus > 0) {
						// to get the samples that reached final level of approval
						final String sSampleQuery = "select npreregno, ntransactionstatus from registrationhistory "
								+ " where nreghistorycode = any (select max(nreghistorycode)"
								+ " from registrationhistory where nsitecode= " + userInfo.getNtranssitecode()
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and npreregno in (" +stringUtilityFunction.fnDynamicListToString(listAvailableSample, "getNpreregno")
								+ ") group by npreregno) and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ntransactionstatus in (" + nApprovalStatus + ") and npreregno in ("
								+ stringUtilityFunction.fnDynamicListToString(listAvailableSample, "getNpreregno") + ") and nsitecode="
								+ userInfo.getNtranssitecode() + ";";

						List<RegistrationHistory> listFinalAvailableSample = jdbcTemplate.query(sSampleQuery,
								new RegistrationHistory());
						// to filter samples from input that not yet reached final level of approval
						listAvailableSample = listAvailableSample.stream()
								.filter(source -> listFinalAvailableSample.stream()
										.noneMatch(dest -> source.getNpreregno() == dest.getNpreregno()))
								.collect(Collectors.toList());
						if (!listFinalAvailableSample.isEmpty()) {
							// some samples reached final level of appproval
							isApprovedSample = true;
						}
					}

					List<RegistrationSampleHistory> sampleWithoutPreregQuarantineStatus = listAvailableSample.stream()
							.filter(x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
									.gettransactionstatus()
									&& x.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
											.gettransactionstatus())
							.collect(Collectors.toList());

					String sValidationString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

					List<String> listPreregNo = new ArrayList<String>();
					List<Integer> removePreregNo = new ArrayList<Integer>();
					List<String> listValidationString = new ArrayList<String>();

					if (!sampleWithoutPreregQuarantineStatus.isEmpty()) {

						// Samples are available btw registered and final level of approval

						final String sApprovalConfigQuery = "select acap.*, ra.npreregno, ra.sarno from registrationarno ra, approvalconfigautoapproval acap"
								+ " where acap.napprovalconfigversioncode = ra.napprovalversioncode and acap.nstatus = ra.nstatus"
								+ " and ra.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ra.npreregno in ("
								+ stringUtilityFunction.fnDynamicListToString(sampleWithoutPreregQuarantineStatus, "getNpreregno") + ")";
						
						final List<ApprovalConfigAutoapproval> approvalConfigAutoApprovals = jdbcTemplate.query(sApprovalConfigQuery, new ApprovalConfigAutoapproval());

						for (ApprovalConfigAutoapproval objApprovalConfigAutoapproval : approvalConfigAutoApprovals) {
							if (objApprovalConfigAutoapproval.getNneedautocomplete() == Enumeration.TransactionStatus.YES.gettransactionstatus()
									&& objApprovalConfigAutoapproval.getNneedautoapproval() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {

								// sample needs only auto complete and but not auto approval option
								sValidationString = validateTestAutoComplete(listSample, listTest, userInfo);

								if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sValidationString)) {
									listPreregNo.add(String.valueOf(objApprovalConfigAutoapproval.getNpreregno()));
								} else {
									listValidationString.add(objApprovalConfigAutoapproval.getSarno());
									removePreregNo.add(objApprovalConfigAutoapproval.getNpreregno());
								}
							} else {
								listPreregNo.add(String.valueOf(objApprovalConfigAutoapproval.getNpreregno()));
							}
						}
						outputMap.put("approvalconfigautoapproval", approvalConfigAutoApprovals);
					} else {

					}

					if (!removePreregNo.isEmpty()) {
						listAvailableSample = listAvailableSample.stream()
								.filter(available -> removePreregNo.stream()
										.noneMatch(remove -> remove == available.getNpreregno()))
								.collect(Collectors.toList());
					}
					if (!listAvailableSample.isEmpty()) {

						final String sFindParameterQuery = "select ntestgrouptestparametercode, ntestgrouptestcode from testgrouptestparameter "
								+ " where ntestgrouptestcode in ("
								+stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode") + ") and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						final String sSampleQuery = "select npreregno, ntransactionstatus from registrationhistory where nreghistorycode = "
								+ " any (select max(nreghistorycode)" + " from registrationhistory where nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and npreregno in ("
								+ stringUtilityFunction.fnDynamicListToString(listAvailableSample, "getNpreregno") + ") "
								+ " and nsitecode = " + userInfo.getNtranssitecode() + " group by npreregno)"
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ntransactionstatus not in ("
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + "," + " "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")"
								+ " and npreregno in (" +stringUtilityFunction.fnDynamicListToString(listAvailableSample, "getNpreregno")
								+ ")" + " and nsitecode = " + userInfo.getNtranssitecode() + ";";

						final String llinterQuery = "select tgt.ntestgrouptestcode from testgrouptest tgt, instrumentcategory ic "
								+ " where ic.ninstrumentcatcode = tgt.ninstrumentcatcode" + " and tgt.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgt.ntestgrouptestcode in ("
								+ stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode") + ")"
								+ " and tgt.ninstrumentcatcode > 0;";

						final String sSeqNoQuery = "select * from seqnoregistration "
								+ " where stablename in ('registrationtest', 'registrationtesthistory', 'registrationparameter',"
								+ "'joballocation', 'resultparametercomments', 'registrationhistory',"
								+ " 'registrationsection', 'registrationsectionhistory', 'llinter');";

						Map<String, List<?>> mapList =  projectDAOSupport.getMutlipleEntityResultsInListUsingPlainSql(sFindParameterQuery + sSampleQuery + sSeqNoQuery + llinterQuery,jdbcTemplate,
								TestGroupTestParameter.class,RegistrationHistory.class,SeqNoRegistration.class,TestGroupTest.class);

						final List<TestGroupTestParameter> listAvailableParameter = (List<TestGroupTestParameter>) mapList.get("TestGroupTestParameter");
						final List<RegistrationHistory> registrationHistory = (List<RegistrationHistory>) mapList.get("RegistrationHistory");
						final List<SeqNoRegistration> lstSeqNo = (List<SeqNoRegistration>) mapList.get("SeqNoRegistration");
						final List<TestGroupTest> testGroupTest = (List<TestGroupTest>) mapList.get("TestGroupTest");

						List<RegistrationHistory> availableRegistrationHistory = registrationHistory.stream().filter(
								source -> source.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
										|| approvalConfigRole.stream().anyMatch(check -> source.getNtransactionstatus() == check.getNapprovalstatuscode() && check.getNlevelno() != 1))
								.collect(Collectors.toList());

						outputMap.putAll(lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
								SeqNoRegistration -> SeqNoRegistration.getNsequenceno())));

						int testCount = listTest.stream()
								.mapToInt(testgrouptest -> testgrouptest.getNrepeatcountno() == 0 ? 1
										: testgrouptest.getNrepeatcountno())
								.sum();

						int parameterCount = listTest.stream()
								.mapToInt(testgrouptest -> ((testgrouptest.getNrepeatcountno() == 0 ? 1
										: testgrouptest.getNrepeatcountno()) * testgrouptest.getNparametercount()))
								.sum();

						String sUpdateSeqNoQry = "update seqnoregistration" + " set nsequenceno = "
								+ ((int) outputMap.get("registrationparameter")
										+ (listAvailableSample.size() * parameterCount))
								+ " where stablename = 'registrationparameter';";

						jdbcTemplate.execute(sUpdateSeqNoQry);

						sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
								+ ((int) outputMap.get("registrationtest")
										+ (listAvailableSample.size() * testCount))
								+ " where stablename = 'registrationtest';";
						jdbcTemplate.execute(sUpdateSeqNoQry);

						if (nAutocomplete == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

							sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
									+ ((int) outputMap.get("registrationtesthistory")
											+ (listAvailableSample.size() * testCount * 2))
									+ " where stablename = 'registrationtesthistory';";
							jdbcTemplate.execute(sUpdateSeqNoQry);
						} else {
							sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
									+ ((int) outputMap.get("registrationtesthistory")
											+ (listAvailableSample.size() * testCount))
									+ " where stablename = 'registrationtesthistory';";
							jdbcTemplate.execute(sUpdateSeqNoQry);
						}
						if (!sampleWithoutPreregQuarantineStatus.isEmpty()) {
							if (!listPreregNo.isEmpty() && !needJobAllocation) {
								sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
										+ ((int) outputMap.get("joballocation")
												+ (listPreregNo.size() * testCount))
										+ " where stablename = 'joballocation';";
							jdbcTemplate.execute(sUpdateSeqNoQry);
							}

							sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
									+ (int) outputMap.get("registrationparameter")
									+ (sampleWithoutPreregQuarantineStatus.size() * parameterCount)
									+ " where stablename = 'resultparametercomments';";
							jdbcTemplate.execute(sUpdateSeqNoQry);

							if (!availableRegistrationHistory.isEmpty()) {
								sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
										+ ((int) outputMap.get("registrationhistory")
												+ availableRegistrationHistory.size())
										+ " where stablename = 'registrationhistory';";
							jdbcTemplate.execute(sUpdateSeqNoQry);
							}

							final List<TestGroupTestParameter> llinterParameterList = listAvailableParameter.stream()
									.filter(source -> testGroupTest.stream().anyMatch(
											dest -> source.getNtestgrouptestcode() == dest.getNtestgrouptestcode()))
									.collect(Collectors.toList());
							if (testGroupTest.size() > 0) {
								sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
										+ ((int) outputMap.get("llinter") + llinterParameterList.size())
										+ "where stablename = 'llinter';";
								jdbcTemplate.execute(sUpdateSeqNoQry);

								outputMap.put("llinterParameterList", llinterParameterList.size());
							}

							if (!listPreregNo.isEmpty()) {
								Set<String> listSectionCode = listTest.stream()
										.map(obj -> String.valueOf(obj.getNsectioncode())).collect(Collectors.toSet());

								final String ssectionCountQuery = " select npreregno, case when count(nsectioncode) = "
										+ listSectionCode.size() + " then 0 else " + listSectionCode.size()
										+ " - count(nsectioncode) end nsectioncode "
										+ " from registrationsection rs where npreregno in ("
										+ String.join(",", listPreregNo) + ") and nsectioncode in("
										+ String.join(",", listSectionCode) + ") and nsitecode = "
										+ userInfo.getNtranssitecode() + " and nstatus= "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " group by npreregno";

								final List<RegistrationSection> preregSectionList = jdbcTemplate.query(ssectionCountQuery, new RegistrationSection());

								// Sum of new sections count.
								final int sectionPreregCount = preregSectionList.stream()
										.mapToInt(regSection -> regSection.getNsectioncode()).sum();

								// Sample count for which the new sections are not available
								long unavailPreregSectionCount = listPreregNo.stream()
										.filter(item -> preregSectionList.stream()
												.noneMatch(item1 -> item1.getNpreregno() == Integer.parseInt(item)))
										.count();

								// Sum of both the above counts to be updated in sequence table for
								int prergCountToAdd = sectionPreregCount + (int) unavailPreregSectionCount;
								if (prergCountToAdd > 0) {
									sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
											+ (prergCountToAdd + (int) outputMap.get("registrationsection"))
											+ " where stablename = 'registrationsection';";

									sUpdateSeqNoQry = sUpdateSeqNoQry + ";update seqnoregistration set nsequenceno = "
											+ (prergCountToAdd + (int) outputMap.get("registrationsectionhistory"))
											+ " where stablename = 'registrationsectionhistory';";
									jdbcTemplate.execute(sUpdateSeqNoQry);
								}
							}
						}

						outputMap.put("ApprovalConfigRole", approvalConfigRole);
						outputMap.put("RegistrationHistory", registrationHistory);
						outputMap.put("parametercount", listAvailableParameter.size());		
						outputMap.put("AvailableSample", listAvailableSample);
						// outputMap.put("boolMyJobsScreen", boolMyJobsScreen);
						outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					} else {
						if (isApprovedSample) {
							outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									commonFunction.getMultilingualMessage("IDS_SAMPLESAREINFINALLEVELAPPROVE",
											userInfo.getSlanguagefilename()));
						} else {
							outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									commonFunction.getMultilingualMessage("IDS_DEFAULTRESULTISNOTAVAILABLEFOR",
											userInfo.getSlanguagefilename()) + " "
											+ String.join(",", listValidationString));
						}
					}

				} else {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							commonFunction.getMultilingualMessage("IDS_CANNOTADDTESTFORRELEASEDSAMPLE",
									userInfo.getSlanguagefilename()));
				}
			} else {
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
						.getMultilingualMessage("IDS_SAMPLEISREJECTEDORCANCELLED", userInfo.getSlanguagefilename()));
			}
		} else {
			outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),commonFunction.getMultilingualMessage("IDS_INACTIVETESTCANNOTBEREGISTERED",
							userInfo.getSlanguagefilename()));
		}
		return outputMap;
	}

	
	@SuppressWarnings({ "unchecked" })
	public ResponseEntity<Object> createTest(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			Map<String, Object> inputMap) throws Exception {

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();

		int nTransactionTestCode = (int) inputMap.get("registrationtest") + 1;
		int nTesthistoryCode = (int) inputMap.get("registrationtesthistory") + 1;
		int nRegistrationParameterCode = (int) inputMap.get("registrationparameter") + 1;

		boolean needJobAllocation = (boolean) inputMap.get("nneedjoballocation");
		//ALPD-4649--Vignesh R(16-09-2024)
		boolean nneedtestinitiate = (boolean) inputMap.get("nneedtestinitiate");
		
		final List<RegistrationSampleHistory> listAvailableSample = (List<RegistrationSampleHistory>) inputMap
				.get("AvailableSample");
		// final int nParameterCount = (Integer) inputMap.get("parametercount");

		int nJoballocationCode = inputMap.containsKey("joballocation") ? (int) inputMap.get("joballocation") : 0;

		int nRegHistoryCode = inputMap.containsKey("registrationhistory") ? (int) inputMap.get("registrationhistory")
				: 0;

		int nregSectionCode = inputMap.containsKey("registrationsection") ? (int) inputMap.get("registrationsection")
				: 0;

		int nSectionHistoryCode = inputMap.containsKey("registrationsectionhistory")
				? (int) inputMap.get("registrationsectionhistory")
				: 0;
		int nllintercode = 0;
		if (inputMap.containsKey("llinterParameterList")) {
			nllintercode = (int) inputMap.get("llinter");
		}
		int llinterParameterListCount = 0;
		if (inputMap.containsKey("llinterParameterList")) {
			llinterParameterListCount = (int) inputMap.get("llinterParameterList");
		}
		int approvalConfigVersionCode = -1;
		List<ApprovalConfigAutoapproval> approvalConfigAutoApprovals = new ArrayList<ApprovalConfigAutoapproval>();
		if (inputMap.containsKey("approvalconfigautoapproval")) {
			approvalConfigAutoApprovals = (List<ApprovalConfigAutoapproval>) inputMap.get("approvalconfigautoapproval");
		}

		final Instant utcDateTime = dateUtilityFunction.getCurrentDateTime(userInfo);

		String stestStatus = "-2";
		int parameterStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
		// int nRecievedStatus = -2;

		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode");

		final String sParameterQuery = "select case when tgtp.nparametertypecode = "
				+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then tgtnp.sresultvalue"
				+ " else case when tgtp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
				+ " then tgtpp.spredefinedname else case when tgtp.nparametertypecode = "
				+ Enumeration.ParameterType.CHARACTER.getparametertype()
				+ " then tgtcp.scharname else null end end end sresultvalue,"
				+ " tgtp.nparametertypecode, tgtp.ntestgrouptestparametercode, tgt.ntestgrouptestcode, tgtp.nresultmandatory"
				+ " from testgrouptest tgt inner join testgrouptestparameter tgtp on tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode"
				+ " and tgtp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestcharparameter tgtcp on tgtcp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
				+ " and tgtcp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
				+ " and tgtnp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestpredefparameter tgtpp on tgtpp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
				+ " and tgtpp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " where tgt.ntestgrouptestcode in (" + sntestgrouptestcode + ") and tgt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();// + ";";

		final List<TestGroupTestParameter> parameterList = jdbcTemplate.query(sParameterQuery,new TestGroupTestParameter());

		List<String> replicateTestList = new ArrayList<String>();
		List<String> replicateTestHistoryList = new ArrayList<String>();
		List<String> replicateTestParameterList = new ArrayList<String>();
		List<Integer> autoCompleteTestCodeList = new ArrayList<Integer>();
		List<Integer> nonAutoCompleteTestCodeList = new ArrayList<Integer>();
		Set<Integer> jobAllocationTestList = new HashSet<>();
		List<Integer> transactionTestCodeList = new ArrayList<Integer>();

		String addedTestCode = "";
		String strTestHistory = "";

		boolean directAddTest = true;
		if (inputMap.containsKey("directAddTest") && (boolean) inputMap.get("directAddTest") == false) {
			directAddTest = false;
		}

		for (RegistrationSampleHistory objRegistrationSample : listAvailableSample) {

			boolean isAutoComplete = false;
			boolean isAutoJobAllocation = false;

			if (!approvalConfigAutoApprovals.isEmpty()) {

				List<ApprovalConfigAutoapproval> approvalConfigAutoApproval = approvalConfigAutoApprovals.stream()
						.filter(x -> x.getNpreregno() == objRegistrationSample.getNpreregno())
						.collect(Collectors.toList());

				// Based on approval config the test status will change here...
				if (!approvalConfigAutoApproval.isEmpty()) {
					ApprovalConfigAutoapproval objApprovalConfigAutoapproval = approvalConfigAutoApproval.get(0);
					if (objApprovalConfigAutoapproval.getNneedautoapproval() == Enumeration.TransactionStatus.NO
							.gettransactionstatus()
							&& objApprovalConfigAutoapproval.getNneedautocomplete() == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
						// Only auto complete
						isAutoComplete = isAutoJobAllocation = true;
						approvalConfigVersionCode = objApprovalConfigAutoapproval.getNapprovalconfigversioncode();

						// commented by L.Subashini as this check is invalid
						// if (isAutoComplete) {
						List<String> rejectTest = new ArrayList<String>();

						for (int testindex = 0; testindex < listTest.size(); testindex++) {
							TestGroupTest objTestGroupTestParameter = listTest.get(testindex);

							// Filtering the relevant parameters of the specific test
							final List<TestGroupTestParameter> testParameterList = parameterList.stream()
									.filter(parameter -> parameter.getNtestgrouptestcode() == objTestGroupTestParameter
											.getNtestgrouptestcode())
									.collect(Collectors.toList());

							final List<TestGroupTestParameter> nonMandatoryTestList = testParameterList.stream().filter(
									parameter -> parameter.getNresultmandatory() == Enumeration.TransactionStatus.NO
											.gettransactionstatus() && parameter.getSresultvalue() == null)
									.collect(Collectors.toList());

							if (nonMandatoryTestList.size() == testParameterList.size()) {
								rejectTest.add(String.valueOf(objTestGroupTestParameter.getNtestgrouptestcode()));
							}

						}
						List<TestGroupTest> testGroupTest = listTest.stream()
								.filter(test -> rejectTest.stream()
										.noneMatch(reject -> Integer.valueOf(reject) == test.getNtestgrouptestcode()))
								.collect(Collectors.toList());

						if (testGroupTest.size() > 0) {
							final String testCodeToComplete = testGroupTest.stream()
									.map(testGroup -> String.valueOf(testGroup.getNtestgrouptestcode()))
									.collect(Collectors.joining(","));

							stestStatus = String
									.valueOf(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()) + ","
									+ String.valueOf(Enumeration.TransactionStatus.COMPLETED.gettransactionstatus());

							parameterStatus = Enumeration.TransactionStatus.COMPLETED.gettransactionstatus();

							final Map<String, Object> testMap = createQueryCreateTestParameterHistory(
									objRegistrationSample, // nRecievedStatus,
									testCodeToComplete, stestStatus, nTransactionTestCode, nTesthistoryCode,
									nRegistrationParameterCode, replicateTestList, replicateTestHistoryList,
									replicateTestParameterList, userInfo, sntestgrouptestcode, inputMap,
									transactionTestCodeList, autoCompleteTestCodeList, nonAutoCompleteTestCodeList,
									jobAllocationTestList, isAutoComplete, needJobAllocation,nneedtestinitiate);

							replicateTestHistoryList = (ArrayList<String>) testMap.get("replicateTestHistoryList");
							replicateTestList = (ArrayList<String>) testMap.get("replicateTestList");
							replicateTestParameterList = (ArrayList<String>) testMap.get("replicateTestParameterList");
							nTransactionTestCode = (int) testMap.get("nTransactionTestCode");
							nTesthistoryCode = (int) testMap.get("nTesthistoryCode");
							nRegistrationParameterCode = (int) testMap.get("nRegistrationParameterCode");
							autoCompleteTestCodeList = (ArrayList<Integer>) testMap.get("autoCompleteTestCodeList");
							nonAutoCompleteTestCodeList = (ArrayList<Integer>) testMap
									.get("nonAutoCompleteTestCodeList");
							jobAllocationTestList = (HashSet<Integer>) testMap.get("jobAllocationTestList");
							transactionTestCodeList = (ArrayList<Integer>) testMap.get("transactionTestCodeList");

						}
						if (rejectTest.size() > 0) {
							String rejString = rejectTest.stream().map(String::valueOf)
									.collect(Collectors.joining(","));
							stestStatus = String
									.valueOf(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()) + ","
									+ String.valueOf(Enumeration.TransactionStatus.REJECTED.gettransactionstatus());

							final Map<String, Object> testMap = createQueryCreateTestParameterHistory(
									objRegistrationSample, // nRecievedStatus,
									rejString, stestStatus, nTransactionTestCode, nTesthistoryCode,
									nRegistrationParameterCode, replicateTestList, replicateTestHistoryList,
									replicateTestParameterList, userInfo, sntestgrouptestcode, inputMap,
									transactionTestCodeList, autoCompleteTestCodeList, nonAutoCompleteTestCodeList,
									jobAllocationTestList, isAutoComplete, needJobAllocation,nneedtestinitiate);

							replicateTestHistoryList = (ArrayList<String>) testMap.get("replicateTestHistoryList");
							replicateTestList = (ArrayList<String>) testMap.get("replicateTestList");
							replicateTestParameterList = (ArrayList<String>) testMap.get("replicateTestParameterList");
							nTransactionTestCode = (int) testMap.get("nTransactionTestCode");
							nTesthistoryCode = (int) testMap.get("nTesthistoryCode");
							nRegistrationParameterCode = (int) testMap.get("nRegistrationParameterCode");
							autoCompleteTestCodeList = (ArrayList<Integer>) testMap.get("autoCompleteTestCodeList");
							nonAutoCompleteTestCodeList = (ArrayList<Integer>) testMap
									.get("nonAutoCompleteTestCodeList");
							jobAllocationTestList = (HashSet<Integer>) testMap.get("jobAllocationTestList");
							transactionTestCodeList = (ArrayList<Integer>) testMap.get("transactionTestCodeList");
						}
						// }

					} else if (needJobAllocation && objApprovalConfigAutoapproval.getNautoallot() == Enumeration.TransactionStatus.YES.gettransactionstatus())
					{
						isAutoJobAllocation = true;
						stestStatus = String
								.valueOf(Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus());
					} else if (!needJobAllocation ||(needJobAllocation && objApprovalConfigAutoapproval
							.getNautoallot() == Enumeration.TransactionStatus.NO.gettransactionstatus())) {
					
						stestStatus = String.valueOf(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());

					}

				}
			}

			// Preregister sample test status is decided here...
			if (objRegistrationSample.getNtransactionstatus() == Enumeration.TransactionStatus.PREREGISTER
					.gettransactionstatus()
					|| objRegistrationSample.getNtransactionstatus() == Enumeration.TransactionStatus.QUARENTINE
							.gettransactionstatus()) {
				stestStatus = String.valueOf(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
			}

			List<ApprovalConfigRole> approvalConfigRole = (List<ApprovalConfigRole>) inputMap.get("ApprovalConfigRole");

			if (directAddTest) {

				List<RegistrationHistory> registrationHistory = (List<RegistrationHistory>) inputMap
						.get("RegistrationHistory");

				List<RegistrationHistory> checkSample = registrationHistory.stream()
						.filter(x -> x.getNpreregno() == objRegistrationSample.getNpreregno())
						.collect(Collectors.toList());

				/**
				 * Samples in complete & not in final level of approval has to be marked as
				 * "partial" if a new test is added
				 */
				List<RegistrationHistory> checkWithSampleStatus = checkSample.stream()
						.filter(source -> (source.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
								.gettransactionstatus())
								|| approvalConfigRole.stream().anyMatch(
										check -> source.getNtransactionstatus() == check.getNapprovalstatuscode()
												&& check.getNlevelno() != 1))
						.collect(Collectors.toList());

				if (!checkWithSampleStatus.isEmpty()) {
					nRegHistoryCode = nRegHistoryCode + 1;
					strTestHistory = strTestHistory.concat(" (" + nRegHistoryCode + ", "
							+ objRegistrationSample.getNpreregno() + ", "
							+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + "," + " '" + utcDateTime
							+ "', " + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + " " + userInfo.getNdeputyuserrole() + ", N'"
							+stringUtilityFunction.replaceQuote(stringUtilityFunction.replaceQuote(userInfo.getSreason())) + "', " + userInfo.getNtranssitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
							+ "),");
					inputMap.put("ntype", 3);
				}

			}
			
			if (objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
					.gettransactionstatus()
					&& objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
							.gettransactionstatus()) {

				final String query21 = "insert into registrationsection (nregistrationsectioncode, npreregno, nsectioncode, nsitecode, nstatus)"
						+ " select rank() over(order by tgt.ntestgrouptestcode)+" + nregSectionCode
						+ " nregistrationsectioncode, " + objRegistrationSample.getNpreregno() + " npreregno,"
						+ " tgt.nsectioncode, " + userInfo.getNtranssitecode() + " nsitecode ,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
						+ " from testgrouptest tgt where tgt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and not exists ("
						+ " select * from registrationsection rsh where rsh.nsectioncode in ("
						+ stringUtilityFunction.fnDynamicListToString(listTest, "getNsectioncode") + ") and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and npreregno = "
						+ objRegistrationSample.getNpreregno() + " and nsitecode =" + userInfo.getNtranssitecode()
						+ " and rsh.nsectioncode = tgt.nsectioncode) and tgt.ntestgrouptestcode in ("
						+ sntestgrouptestcode + ");";
				
				final int addedSectionCount = jdbcTemplate.update(query21);
				nregSectionCode = nregSectionCode + addedSectionCount;

				final String query11 = "insert into registrationsectionhistory (nsectionhistorycode, npreregno, nsectioncode, ntransactionstatus, nsitecode, nstatus)"
						+ " select rank() over(order by tgt.ntestgrouptestcode)+" + nSectionHistoryCode
						+ " nsectionhistorycode, " + objRegistrationSample.getNpreregno() + " npreregno,"
						+ " tgt.nsectioncode, " + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
						+ " ntransactionstatus, " + userInfo.getNtranssitecode() + " nsitecode ,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
						+ " from testgrouptest tgt where tgt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and not exists ("
						+ " select * from registrationsectionhistory rsh where rsh.nsectioncode in ("
						+ stringUtilityFunction.fnDynamicListToString(listTest, "getNsectioncode") + ") and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and npreregno = " + objRegistrationSample.getNpreregno()
						+ " and rsh.nsectioncode = tgt.nsectioncode) and tgt.ntestgrouptestcode in ("
						+ sntestgrouptestcode + ");";
			
				final int addedSecHistoryCount = jdbcTemplate.update(query11);
				nSectionHistoryCode = nSectionHistoryCode + addedSecHistoryCount;
			}

			if (!isAutoComplete) {

				final Map<String, Object> testMap = createQueryCreateTestParameterHistory(objRegistrationSample, // nRecievedStatus,
						sntestgrouptestcode, stestStatus, nTransactionTestCode, nTesthistoryCode,
						nRegistrationParameterCode, replicateTestList, replicateTestHistoryList,
						replicateTestParameterList, userInfo, sntestgrouptestcode, inputMap, transactionTestCodeList,
						autoCompleteTestCodeList, nonAutoCompleteTestCodeList, jobAllocationTestList, isAutoComplete,
						needJobAllocation,nneedtestinitiate);

				replicateTestHistoryList = (ArrayList<String>) testMap.get("replicateTestHistoryList");
				replicateTestList = (ArrayList<String>) testMap.get("replicateTestList");
				replicateTestParameterList = (ArrayList<String>) testMap.get("replicateTestParameterList");
				nTransactionTestCode = (int) testMap.get("nTransactionTestCode");
				nTesthistoryCode = (int) testMap.get("nTesthistoryCode");
				nRegistrationParameterCode = (int) testMap.get("nRegistrationParameterCode");
				autoCompleteTestCodeList = (ArrayList<Integer>) testMap.get("autoCompleteTestCodeList");
				nonAutoCompleteTestCodeList = (ArrayList<Integer>) testMap.get("nonAutoCompleteTestCodeList");
				jobAllocationTestList = (HashSet<Integer>) testMap.get("jobAllocationTestList");
				transactionTestCodeList = (ArrayList<Integer>) testMap.get("transactionTestCodeList");

				if (objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
						&& objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()) {
					
					final String query7 = "insert into llinter(nllintercode, nscreentypecode, npreregno, nsampletypecode, nsamplecode, nregtypecode, nregsubtypecode, nproductcode, "
							+ "nallottedspeccode, ntransactiontestcode, ntestgrouptestcode, nretestno, ntestrepeatcount, ntransactionresultcode, ntestgrouptestparametercode,"
							+ " nparametertypecode, nrounddingdigits, nunitcode, sresult, nresult, nlltestcodde, nllparametercode, dreceiveddate, dregdate, sllarno,"
							+ " sllparamkey, sllinterstatus, sllparamtype, sllcomponentname, slltestname, sllparametername, sproductname, sunitname, smanufname, smanuflotno,"
							+ " smethodname, srefproductgroupname, susername, sclientname, nsitecode, nstatus)"
							+ " (select " + nllintercode
							+ "+RANK() over(order by rp.ntransactionresultcode) nllintercode, r.nsampletypecode,r.npreregno, "
							+ " rs.ncomponentcode nsampletypecode, rs.ntransactionsamplecode nsamplecode, r.nregtypecode, r.nregsubtypecode, r.nproductcode, r.nallottedspeccode,"
							+ " rt.ntransactiontestcode, rt.ntestgrouptestcode, rt.ntestretestno nretestno,  rt.ntestrepeatno ntestrepeatcount, rp.ntransactionresultcode,"
							+ " rp.ntestgrouptestparametercode, rp.nparametertypecode, rp.nroundingdigits nrounddingdigits, rp.nunitcode, NULL sresult, NULL nresult,"
							+ " tm.ntestcode nlltestcodde, tp.ntestparametercode nllparametercode, rg.dreceiveddate, '"
							+ utcDateTime + "' dregdate, CAST(ra.sarno as varchar(20))+'@'+ "
							+ " CAST(rt.ntestrepeatno as varchar(20))+'@'+  CAST(rt.ntestretestno as varchar(20)) as sllarno, NULL sllparamkey, NULL sllinterstatus,"
							+ " case when rp.nparametertypecode = "
							+ Enumeration.ParameterType.NUMERIC.getparametertype()
							+ " then 'V' else 'D' end as sllparamtype,"
							+ " c.scomponentname sllcomponentname, tm.stestname slltestname, tp.sparametername sllparametername, p.sproductname, u.sunitname,"
							+ " mf.smanufname, rg.smanuflotno smanflotno, md.smethodname, ep.seprotocolname srefproductgroupname, us.sfirstname+' '+us.slastname as susername,"
							+ " ct.sclientname, r.nsitecode, "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
							+ " from registrationtesthistory rth,registration r, registrationgeneral rg, registrationarno ra, registrationsample rs, registrationtest rt, registrationparameter rp, "
							+ " testgrouptest tgt, testgrouptestparameter tgtp, testmaster tm, testparameter tp, product p, users us, component c, method md, "
							+ " unit u, instrumentcategory ic, eprotocol ep, client ct, manufacturer mf where "
							+ " rth.npreregno = r.npreregno and rth.ntesthistorycode = any ("
							+ " select max(ntesthistorycode) from registrationtesthistory "
							+ " where ntransactiontestcode = any(select " + nTransactionTestCode
							+ "+RANK() over(order by ntestgrouptestcode) from testgrouptest tgt"
							+ " where ntestgrouptestcode in (" + sntestgrouptestcode + ") and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and npreregno = "
							+ objRegistrationSample.getNpreregno()
							+ " group by npreregno, ntransactionsamplecode, ntransactiontestcode"
							+ " ) and rs.ntransactionsamplecode = rth.ntransactionsamplecode and rt.ntransactiontestcode = rth.ntransactiontestcode"
							+ " and r.npreregno = ra.npreregno"
							+ " and r.npreregno = rg.npreregno and r.npreregno = rs.npreregno and r.npreregno = rt.npreregno and rs.ntransactionsamplecode = rt.ntransactionsamplecode"
							+ " and r.npreregno = rt.npreregno and rt.ntransactiontestcode = rp.ntransactiontestcode"
							+ " and r.npreregno = rp.npreregno"
							+ " and tgt.ntestgrouptestcode = rt.ntestgrouptestcode and tgt.ntestcode = rt.ntestcode"
							+ " and tgtp.ntestgrouptestparametercode = rp.ntestgrouptestparametercode and tm.ntestcode = tgt.ntestcode "
							+ " and tm.ntestcode = tp.ntestcode and tp.ntestparametercode = tgtp.ntestparametercode"
							+ " and p.nproductcode = r.nproductcode and us.nusercode = rth.nusercode and rth.ntransactionstatus = "
							+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
							+ " and c.ncomponentcode = rs.ncomponentcode and md.nmethodcode = tgt.nmethodcode and md.nmethodcode = rt.nmethodcode "
							+ " and u.nunitcode = rp.nunitcode and ic.ninstrumentcatcode = tgt.ninstrumentcatcode and ic.ninstrumentcatcode = tgt.ninstrumentcatcode "
							// + " and ic.ninterfacetypecode = " +
							// Enumeration.InterFaceType.LOGILAB.getInterFaceType()
							+ " and ep.neprotocolcode = rg.neprotocolcode"
							+ " and ct.nclientcode = rg.nclientcode and mf.nmanufcode = rg.nmanufcode"
							+ " and r.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rg.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgtp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and us.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and md.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ep.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and mf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and r.npreregno = " + objRegistrationSample.getNpreregno() + " and ra.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rth.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "  and rt.ninstrumentcatcode > 0);";

					nllintercode = nllintercode + llinterParameterListCount;
				}
			}
		}

		if (replicateTestList.size() > 0) {
			final String strRegistrationTest = "insert into registrationtest (ntransactiontestcode,ntransactionsamplecode,npreregno,"
					+ "ntestgrouptestcode,ninstrumentcatcode,nchecklistversioncode,ntestrepeatno,ntestretestno,jsondata,nsitecode,dmodifieddate, "
					+ " nstatus,ntestcode,nsectioncode,nmethodcode) values ";
			jdbcTemplate.execute(strRegistrationTest + String.join(",", replicateTestList));

		}
		if (replicateTestHistoryList.size() > 0) {
			final String strRegistrationTestHistory = "insert into registrationtesthistory (ntesthistorycode, ntransactiontestcode, "
					+ "ntransactionsamplecode, npreregno, nformcode, ntransactionstatus,"
					+ "	nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,scomments,"
					+ " dtransactiondate,nsampleapprovalhistorycode, nsitecode,"
					+ " nstatus,noffsetdtransactiondate,ntransdatetimezonecode) values";
			jdbcTemplate.execute(strRegistrationTestHistory + String.join(",", replicateTestHistoryList));
		}
		if (replicateTestParameterList.size() > 0) {
			final String strRegTestParameter = " insert into registrationparameter (ntransactionresultcode,"
					+ " npreregno,ntransactiontestcode,ntestgrouptestparametercode,"
					+ " ntestparametercode,nparametertypecode,ntestgrouptestformulacode,"
					+ " nunitcode, nresultmandatory,nreportmandatory,jsondata,nsitecode,nstatus) values";
			jdbcTemplate.execute(strRegTestParameter + String.join(",", replicateTestParameterList));
		}

		final String createdTestCode = String.join(",",
				transactionTestCodeList.stream().map(String::valueOf).collect(Collectors.toList()));

		if (autoCompleteTestCodeList.size() > 0 || nonAutoCompleteTestCodeList.size() > 0) {
			// Sample Status not in preregister or quarantine

			if (autoCompleteTestCodeList.size() > 0) {

				addedTestCode = String.join(",",
						autoCompleteTestCodeList.stream().map(String::valueOf).collect(Collectors.toList()));
				// with grade and result in sql query
				
				String query5 = "insert into resultparameter( ntransactionresultcode, npreregno, ntransactiontestcode, ntestgrouptestparametercode, ntestparametercode,"
						+ " nparametertypecode, nroundingdigits, nresultmandatory, nreportmandatory, ntestgrouptestformulacode, nunitcode, ngradecode, ntransactionstatus,"
						+ " sfinal, sresult, nenforcestatus, nenforceresult,ncalculatedresult,smina, sminb, smaxb, smaxa, sminlod, smaxlod, sminloq, smaxloq, sdisregard, sresultvalue,"
						+ " dentereddate, nenteredby, nenteredrole, ndeputyenteredby, ndeputyenteredrole, nfilesize, ssystemfilename, nlinkcode, nattachmenttypecode, nsitecode,nstatus)"
						+ " select ntransactionresultcode, rp.npreregno, rp.ntransactiontestcode, rp.ntestgrouptestparametercode,"
						+ " rp.ntestparametercode, rp.nparametertypecode, rp.nroundingdigits, rp.nresultmandatory, rp.nreportmandatory, rp.ntestgrouptestformulacode,"
						+ " rp.nunitcode, case when rp.nparametertypecode = "
						+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then ("
						+ " case when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is not null "
						+ " then  " + " case when cast(tgtnp.sresultvalue as float)<=tgtnp.smaxb then "
						+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
						+ " when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is null then "
						+ " case when cast(tgtnp.sresultvalue as float)<=tgtnp.smaxa then "
						+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
						+ " when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
						+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smaxa and tgtnp.smaxb then "
						+ Enumeration.Grade.OOS.getGrade() + " when cast(tgtnp.sresultvalue as float) > tgtnp.smaxb "
						+ " then " + Enumeration.Grade.OOT.getGrade() + " else " + Enumeration.Grade.PASS.getGrade()
						+ " end "

						+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is null "
						+ " then case when cast(tgtnp.sresultvalue as float)>=tgtnp.smina then "
						+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "

						+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is not null then "
						+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxb then "
						+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "

						+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is null "
						+ " then case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
						+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "

						+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
						+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
						+ Enumeration.Grade.PASS.getGrade()
						+ " when cast(tgtnp.sresultvalue as float) between tgtnp.smaxa and tgtnp.smaxb then "
						+ Enumeration.Grade.OOS.getGrade() + " else " + Enumeration.Grade.OOT.getGrade() + " end "

						+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is null then "
						+ " case when cast(tgtnp.sresultvalue as float) >= tgtnp.sminb then "
						+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "

						+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is not null then "
						+ " case when cast(cast(tgtnp.sresultvalue as float) as float) between tgtnp.sminb and tgtnp.smaxb then "
						+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "

						+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is null then "
						+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smaxa then "
						+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "

						+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
						+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smaxa then "
						+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "

						+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is null then "
						+ " case when cast(tgtnp.sresultvalue as float)	between tgtnp.sminb and tgtnp.smina then "
						+ Enumeration.Grade.OOS.getGrade() + " "
						+ " when cast(tgtnp.sresultvalue as float) < tgtnp.sminb then "
						+ Enumeration.Grade.OOS.getGrade() + " when cast(tgtnp.sresultvalue as float) >= tgtnp.smina  "
						+ " then " + Enumeration.Grade.PASS.getGrade() + " end  "

						+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is not null "
						+ " then case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxb then "
						+ Enumeration.Grade.PASS.getGrade()
						+ " when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smina " + " then "
						+ Enumeration.Grade.OOS.getGrade() + " when cast(tgtnp.sresultvalue as float) > tgtnp.smaxb"
						+ " or cast(tgtnp.sresultvalue as float) < tgtnp.sminb then " + Enumeration.Grade.OOT.getGrade()
						+ " end "

						+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is null  then "
						+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
						+ Enumeration.Grade.PASS.getGrade()
						+ " when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smina then "
						+ Enumeration.Grade.OOS.getGrade() + " else " + Enumeration.Grade.OOT.getGrade() + " end"

						+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
						+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
						+ Enumeration.Grade.PASS.getGrade() + "  "

						+ " when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smaxb then "
						+ Enumeration.Grade.OOT.getGrade() + " "
						+ " when (cast(tgtnp.sresultvalue as float) < tgtnp.sminb or tgtnp.smaxb < cast(tgtnp.sresultvalue as float))"
						+ " and (tgtnp.sminb!=0 and tgtnp.smaxb!=0)then " + Enumeration.Grade.OOS.getGrade() + " else "
						+ Enumeration.Grade.PASS.getGrade() + " end else " + Enumeration.Grade.NA.getGrade() + " end)"

						+ " when rp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
						+ " then tgtpp.ngradecode" + " when rp.nparametertypecode = "
						+ Enumeration.ParameterType.CHARACTER.getparametertype() + " then "
						+ Enumeration.Grade.FIO.getGrade() + " " + " when rp.nparametertypecode = "
						+ Enumeration.ParameterType.ATTACHEMENT.getparametertype() + " then "
						+ Enumeration.Grade.PASS.getGrade() + " " + " else "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " end as ngradecode,"

						+ " case when (case  when rp.nparametertypecode = "
						+ Enumeration.ParameterType.NUMERIC.getparametertype()
						+ " then tgtnp.sresultvalue when rp.nparametertypecode = "
						+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " then tgtpp.spredefinedname"
						+ " when rp.nparametertypecode = " + Enumeration.ParameterType.CHARACTER.getparametertype()
						+ " then tgtcp.scharname" + " when rp.nparametertypecode = "
						+ Enumeration.ParameterType.ATTACHEMENT.getparametertype() + " then NULL else NULL end) is null"
						+ " then " + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
						+ parameterStatus + " end as ntransactionstatus, "

						+ " case " + " when rp.nparametertypecode = "
						+ Enumeration.ParameterType.NUMERIC.getparametertype()
						+ " then (case when CHARINDEX('.', tgtnp.sresultvalue, 1) < =0 then "
						+ " tgtnp.sresultvalue+'.'+dbo.RoundDigits(rp.nroundingdigits) else tgtnp.sresultvalue end)"
						+ " when rp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
						+ " then tgtpp.spredefinedname" + " when rp.nparametertypecode = "
						+ Enumeration.ParameterType.CHARACTER.getparametertype() + " then tgtcp.scharname"
						+ " when rp.nparametertypecode = " + Enumeration.ParameterType.ATTACHEMENT.getparametertype()
						+ " then NULL" + " else NULL end as sfinal, "

						+ " case " + " when rp.nparametertypecode = "
						+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then tgtnp.sresultvalue"
						+ " when rp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
						+ " then tgtpp.spredefinedname when rp.nparametertypecode = "
						+ Enumeration.ParameterType.CHARACTER.getparametertype() + " then tgtcp.scharname"
						+ " when rp.nparametertypecode = " + Enumeration.ParameterType.ATTACHEMENT.getparametertype()
						+ " then NULL else NULL end as sresult, "

						+ " " + Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforcestatus, "
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforceresult, "
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as ncalcultedresult, "
						+ " rp.smina, rp.sminb, rp.smaxb, rp.smaxa, rp.sminlod, rp.smaxlod, rp.sminloq, rp.smaxloq, rp.sdisregard, rp.sresultvalue, "
						+ " N'" + utcDateTime + "' dentereddate, "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nenteredby, " + " ("
						+ "		select nuserrolecode  from treetemplatetransactionrole "
						+ "		where ntemptransrolecode in (" + "			select max(ttr.ntemptransrolecode) "
						+ "			from treetemplatetransactionrole ttr,approvalconfig ac,approvalconfigversion acv "
						+ "			where ac.napprovalconfigcode=ttr.napprovalconfigcode "
						+ "			and ac.napprovalconfigcode = acv.napprovalconfigcode "
						+ "			and ttr.ntreeversiontempcode = acv.ntreeversiontempcode "
						+ "			and ttr.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "			and acv.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "			and ac.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "			and ac.nregtypecode= " + nregtypecode + " 		and ac.nregsubtypecode= "
						+ nregsubtypecode + " 		and acv.napproveconfversioncode =" + approvalConfigVersionCode
						+ " 		group by ttr.ntreeversiontempcode" + "		)" + "	) as nenteredrole, "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ndeputyenteredby, "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ndeputyenteredrole, " + " "
						+ Enumeration.TransactionStatus.ALL.gettransactionstatus()
						+ " nfilesize, NULL ssystemfilename, " + " "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nlinkcode, " + " "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nattachmenttypecode," + " "
						+ userInfo.getNtranssitecode() + " nsitecode,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
						+ " from registrationparameter rp "
						+ " left outer join testgrouptestcharparameter tgtcp on rp.ntestgrouptestparametercode = tgtcp.ntestgrouptestparametercode"
						+ " and tgtcp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " left outer join testgrouptestnumericparameter tgtnp on rp.ntestgrouptestparametercode = tgtnp.ntestgrouptestparametercode"
						+ " and tgtnp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " left outer join testgrouptestpredefparameter tgtpp on rp.ntestgrouptestparametercode = tgtpp.ntestgrouptestparametercode"
						+ " and tgtpp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgtpp.ndefaultstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
						+ " and rp.ntransactiontestcode in (" + addedTestCode + ") and " + " rp.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rp.nsitecode="
						+ userInfo.getNtranssitecode();// + ";");

				jdbcTemplate.execute(query5);
			} else {
				
				addedTestCode = String.join(",",nonAutoCompleteTestCodeList.stream().map(String::valueOf).collect(Collectors.toList()));

				final String query6 = "insert into resultparameter( ntransactionresultcode, npreregno, ntransactiontestcode, ntestgrouptestparametercode, ntestparametercode,"
						+ " nparametertypecode, nresultmandatory, nreportmandatory, ntestgrouptestformulacode, nunitcode, ngradecode, ntransactionstatus,"
						+ "  nenforcestatus, nenforceresult,ncalculatedresult,"
						+ "  nenteredby, nenteredrole, ndeputyenteredby, ndeputyenteredrole, nlinkcode, nattachmenttypecode,jsondata, nsitecode, nstatus)"
						+ " select ntransactionresultcode, rp.npreregno, rp.ntransactiontestcode, rp.ntestgrouptestparametercode, rp.ntestparametercode,"
						+ " rp.nparametertypecode, rp.nresultmandatory, rp.nreportmandatory, rp.ntestgrouptestformulacode, "
						+ " rp.nunitcode,  "
						+ "case when (rp.jsondata->>'ngradecode')::int isnull then -1 else (rp.jsondata->>'ngradecode')::int end  as ngradecode, " 
						+ parameterStatus
						+ " ntransactionstatus," + Enumeration.TransactionStatus.NO.gettransactionstatus()
						+ " as nenforcestatus," + "" + Enumeration.TransactionStatus.NO.gettransactionstatus()
						+ " as nenforceresult, " + Enumeration.TransactionStatus.NO.gettransactionstatus()
						+ " as ncalcultedresult, " + " " + Enumeration.TransactionStatus.NA.gettransactionstatus()
						+ " nenteredby, " + Enumeration.TransactionStatus.NA.gettransactionstatus() + " nenteredrole,"
						+ " " + Enumeration.TransactionStatus.NA.gettransactionstatus() + " ndeputyenteredby, "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ndeputyenteredrole," + " "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nlinkcode, " + " "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nattachmenttypecode, "
						+ "rp.jsondata," + userInfo.getNtranssitecode() + " nsitecode,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
						+ " from registrationparameter rp "
						+ " where rp.ntransactiontestcode in (" + addedTestCode + ") " + " and rp.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rp.nsitecode="
						+ userInfo.getNtranssitecode();
				
				jdbcTemplate.execute(query6);

				final String query8 = "insert into sdmslabsheetmaster(ntransactiontestcode,npreregno,sarno,ntransactionsamplecode,ntestgrouptestcode, "
						+ " ntestcode,nretestno,ntestrepeatcount,ninterfacetypecode,sllinterstatus,nusercode,nuserrolecode,nsitecode,nstatus)"
						+

						" select ntransactiontestcode, rt.npreregno,ra.sarno,rt.ntransactionsamplecode,"
						+ " rt.ntestgrouptestcode,rt.ntestcode,rt.ntestretestno nretestno,rt.ntestrepeatno ntestrepeatcount,"
						+

						"  tm.ninterfacetypecode,'A' sllinterstatus," + userInfo.getNusercode() + " nusercode," + userInfo.getNuserrole()
						+ " nuserrolecode,r.nsitecode,1 nstatus"
						+ " from registration r,registrationarno ra,registrationtest rt,instrumentcategory ic,testmaster tm "
						+ " where "
						+ " rt.ntransactiontestcode in (select rank() over(order by tgt.ntestgrouptestcode)+"
						+ nTransactionTestCode + " ntransactiontestcode "
						+ " from testgrouptest tgt where tgt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgt.ntestgrouptestcode in (" + sntestgrouptestcode + "))"
						+ " and rt.npreregno = r.npreregno and r.npreregno = ra.npreregno and ic.ninstrumentcatcode = rt.ninstrumentcatcode "
						+ " and rt.ninstrumentcatcode > 0 and rt.ntestcode = tm.ntestcode"
//								+ "and ic.ninterfacetypecode = "
//								+ Enumeration.InterFaceType.LOGILAB.getInterFaceType() + " "
						+ " and rt.nstatus = ra.nstatus and ra.nstatus = r.nstatus and r.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				LOGGER.info("Add Test Query-->" + query8);

				final String query9 = "insert into sdmslabsheetdetails (ntransactionresultcode,npreregno,sarno,ntransactionsamplecode,ntransactiontestcode, "
						+ " ntestgrouptestcode,ntestcode,nretestno,ntestrepeatcount,ntestgrouptestparametercode,ntestparametercode,nparametertypecode,"
						+ " nroundingdigits,sresult,sllinterstatus,sfileid,nlinkcode,nattachedlink,nsitecode,nstatus)" +

						" select ntransactionresultcode, rp.npreregno,ra.sarno,rt.ntransactionsamplecode, rt.ntransactiontestcode,"
						+ " rt.ntestgrouptestcode,rt.ntestcode,rt.ntestretestno nretestno,rt.ntestrepeatno ntestrepeatcount,"
						+ " rp.ntestgrouptestparametercode, rp.ntestparametercode, rp.nparametertypecode, rp.nroundingdigits, "
						+ " NULL sresult, 'A' sllinterstatus, NULL sfileid,-1 nlinkcode,-1 nattachedlink,r.nsitecode,1 nstatus"
						+ " from registration r,registrationarno ra,registrationparameter rp,registrationtest rt,instrumentcategory ic"
						+ " where rp.ntransactiontestcode = rt.ntransactiontestcode " +

						" and rt.ntransactiontestcode in (select rank() over(order by tgt.ntestgrouptestcode)+"
						+ nTransactionTestCode + " ntransactiontestcode "
						+ " from testgrouptest tgt where tgt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgt.ntestgrouptestcode in (" + sntestgrouptestcode + "))"
						+ " and r.npreregno = ra.npreregno and ra.npreregno = rp.npreregno and ic.ninstrumentcatcode = rt.ninstrumentcatcode"
						+ " and rt.ninstrumentcatcode > 0 "
//								+ "and ic.ninterfacetypecode = "
//								+ Enumeration.InterFaceType.LOGILAB.getInterFaceType() + "  "
						+ " and r.nstatus = ra.nstatus and ra.nstatus = rp.nstatus and rp.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and r.nsitecode=ra.nsitecode and ra.nsitecode = rp.nsitecode and rp.nsitecode="
						+ userInfo.getNtranssitecode() + ";";

				LOGGER.info("Add Test Query-->" + query9);

			}

			final String query10 = "insert into resultparametercomments (ntransactionresultcode, ntransactiontestcode, npreregno, "
					+ "ntestgrouptestparametercode, ntestparametercode,jsondata, nsitecode, nstatus)"
					+ " select  rp.ntransactionresultcode,"
					+ " rp.ntransactiontestcode, rp.npreregno, rp.ntestgrouptestparametercode, rp.ntestparametercode,rp.jsondata, "
					+ userInfo.getNtranssitecode() + " nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
					+ " from resultparameter rp " + " where rp.ntransactiontestcode in (" + createdTestCode
					+ ") and rp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rp.nsitecode=" + userInfo.getNtranssitecode();
			
			jdbcTemplate.execute(query10);

			if (jobAllocationTestList.size() > 0) {
				addedTestCode = String.join(",",
						jobAllocationTestList.stream().map(String::valueOf).collect(Collectors.toList()));

				approvalConfigVersionCode = approvalConfigAutoApprovals.get(0).getNapprovalconfigversioncode();
				//Modified the insert query on sonia on 5th july 2024, for jira id 4478
				
				 String query12 = "insert into joballocation (njoballocationcode, npreregno, ntransactionsamplecode, ntransactiontestcode, nsectioncode, nuserrolecode,"
						+ " nusercode, nuserperiodcode, ninstrumentcategorycode, ninstrumentcode,ninstrumentnamecode, ninstrumentperiodcode, ntechniquecode, ntimezonecode, ntestrescheduleno, "
						+ " jsondata, jsonuidata, nsitecode, nstatus) "
						+ " select rank() over(order by rt.ntransactiontestcode)+" + nJoballocationCode
						+ " njoballocationcode, rt.npreregno, rt.ntransactionsamplecode,"
						+ " rt.ntransactiontestcode, rt.nsectioncode, " + "	case when ("
						+ "	select nuserrolecode  from treetemplatetransactionrole where ntemptransrolecode" + " in ("
						+ " select max(ttr.ntemptransrolecode) "
						+ "	from treetemplatetransactionrole ttr,approvalconfig ac,approvalconfigversion acv "
						+ "	where ac.napprovalconfigcode=ttr.napprovalconfigcode "
						+ "	and ac.napprovalconfigcode = acv.napprovalconfigcode "
						+ "	and ttr.ntreeversiontempcode = acv.ntreeversiontempcode " + "	and ttr.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and acv.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and ac.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and ac.nregtypecode= "
						+ nregtypecode + " and ac.nregsubtypecode= " + nregsubtypecode
						+ " and acv.napproveconfversioncode =" + approvalConfigVersionCode
						+ " group by ttr.ntreeversiontempcode)" + "	) isnull then -1 else ("
						+ "	select nuserrolecode  from treetemplatetransactionrole where ntemptransrolecode" + " in ("
						+ " select max(ttr.ntemptransrolecode) "
						+ "	from treetemplatetransactionrole ttr,approvalconfig ac,approvalconfigversion acv "
						+ "	where ac.napprovalconfigcode=ttr.napprovalconfigcode "
						+ "	and ac.napprovalconfigcode = acv.napprovalconfigcode "
						+ "	and ttr.ntreeversiontempcode = acv.ntreeversiontempcode " + "	and ttr.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and acv.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and ac.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and ac.nregtypecode= "
						+ nregtypecode + " and ac.nregsubtypecode= " + nregsubtypecode
						+ " and acv.napproveconfversioncode =" + approvalConfigVersionCode
						+ " group by ttr.ntreeversiontempcode))" + " end ,"
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nusercode, "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nuserperiodcode,"
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ninstrumentcategorycode,"
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ninstrumentcode," + " "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ninstrumentnamecode," + " "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ninstrumentperiodcode, "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ntechniquecode, "
						+ userInfo.getNtimezonecode() + " ntimezonecode," + " "
						+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + " ntestrescheduleno, "
						+ " json_build_object('duserblockfromdate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "','duserblocktodate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo),
								"yyyy-MM-dd HH:mm:ss")
						+ "','suserblockfromtime','00:00','suserblocktotime','00:00',"
						+ "'suserholdduration','0','dinstblockfromdate',NULL,'dinstblocktodate',NULL,'sinstblockfromtime',NULL"
						+ ",'sinstblocktotime',NULL,'sinstrumentholdduration',NULL,'scomments','')::jsonb,"
						+ " json_build_object('duserblockfromdate','"
						+ dateUtilityFunction
								.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo), "yyyy-MM-dd HH:mm:ss")
						+ "','duserblocktodate','"
						+ dateUtilityFunction.instantDateToStringWithFormat(dateUtilityFunction.getCurrentDateTime(userInfo),
								"yyyy-MM-dd HH:mm:ss")
						+ "','suserblockfromtime','00:00','suserblocktotime','00:00',"
						+ "'suserholdduration','0','dinstblockfromdate',NULL,'dinstblocktodate',NULL,'sinstblockfromtime',NULL"
						+ ",'sinstblocktotime',NULL,'sinstrumentholdduration',NULL,'scomments','')::jsonb,"
						+ userInfo.getNtranssitecode() + " nsitecode,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
						+ " from registrationtest rt" + " where rt.ntransactiontestcode in (" + addedTestCode
						+ ") and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rt.nsitecode=" + userInfo.getNtranssitecode()+";";
					//ALPD-4649--Vignesh R(16-09-2024)
					query12=query12+"update seqnoregistration set nsequenceno =(select max(njoballocationcode) from joballocation where nstatus=1) "
							+ " where stablename = 'joballocation' and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
					
				jdbcTemplate.execute(query12);
			}
		}

		inputMap.put("ntransactiontestcode", createdTestCode);
		final Map<String, Object> returnmap = new HashMap<String, Object>();

		returnmap.put("ntransactiontestcode", createdTestCode);

		if (directAddTest) {
			if (!strTestHistory.isEmpty()) {
				strTestHistory = "insert into registrationhistory (nreghistorycode, npreregno, ntransactionstatus, dtransactiondate, nusercode, nuserrolecode, "
						+ " ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode, nstatus,noffsetdtransactiondate,ntransdatetimezonecode) values"
						+ strTestHistory.substring(0, strTestHistory.length() - 1) + ";";
			}

			jdbcTemplate.execute(strTestHistory);

			if (inputMap.get("ntransactiontestcode") != null && inputMap.get("ntransactiontestcode") != "") {
				returnmap.putAll(
						(Map<String, Object>) RegistrationDAO.getRegistrationTest(inputMap, userInfo).getBody());

				jsonAuditObject.put("registrationtest", (List<Map<String, Object>>) returnmap.get("selectedTest"));
				auditmap.put("nregtypecode", nregtypecode);
				auditmap.put("nregsubtypecode", nregsubtypecode);
				auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				actionType.put("registrationtest", "IDS_ADDTEST");
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			}
		}

		return new ResponseEntity<Object>(returnmap, HttpStatus.OK);
	}

	private Map<String, Object> createQueryCreateTestParameterHistory(RegistrationSampleHistory objRegistrationSample,														
			String comPleted, String stestStatus, int nTransactionTestCode, int nTesthistoryCode,
			int nRegistrationParameterCode, List<String> replicateTestList, List<String> replicateTestHistoryList,
			List<String> replicateTestParameterList, UserInfo userInfo, String sntestgrouptestcode,
			final Map<String, Object> inputMap, List<Integer> transactionTestCodeList,
			List<Integer> autoCompleteTestCodeList, List<Integer> nonAutoCompleteTestCodeList,
			Set<Integer> jobAllocationTestList, boolean isAutoComplete, boolean needJobAllocation,final boolean nneedtestinitiate) throws Exception {
		
		
		final ObjectMapper objMapper = new ObjectMapper(); 
		String strQuery = "";
		String rulesResultTable1="";
		String rulesResultTable2="";
		List<RegistrationTest> testGroupTestList= new ArrayList();
		boolean isNewSample=inputMap.containsKey("sampleCode")?((List<Integer>)inputMap.get("sampleCode")).contains(objRegistrationSample.getNtransactionsamplecode()):false;
		
		if(!isNewSample) {
		String stestgrouptestcode = " (select  (x.jsondata->'ntestgrouptestcode')::int as ntestgrouptestcode  "
				+ " from  (SELECT Jsonb_array_elements(jsonuidata -> 'testInitiateTests') " + "  AS "
				+ "   jsondata  FROM   testgrouprulesengine "
				+ "   WHERE  ntestgrouprulesenginecode IN (select tgr.ntestgrouprulesenginecode from  "
				+ "registrationsample rs,registrationtest rt,testgrouprulesengine tgr where "
				+ "rs.ntransactionsamplecode=rt.ntransactionsamplecode "
				+ "and rt.ntestgrouptestcode=tgr.ntestgrouptestcode and rs.ntransactionsamplecode in ("
				+ objRegistrationSample.getNtransactionsamplecode() + ") and tgr.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and tgr.ntestgrouprulesenginecode in (" + inputMap.get("ntestgrouprulesenginecode") + ")) "
				+ " union all " + " SELECT Jsonb_array_elements(jsonuidata -> 'testRepeatTests') AS  jsondata "
				+ "  FROM   testgrouprulesengine "
				+ "  WHERE  ntestgrouprulesenginecode IN   (select tgr.ntestgrouprulesenginecode 	from  "
				+ "	registrationsample rs,registrationtest rt,testgrouprulesengine tgr 	where "
				+ "	rs.ntransactionsamplecode=rt.ntransactionsamplecode "
				+ "	and rt.ntestgrouptestcode=tgr.ntestgrouptestcode and rs.ntransactionsamplecode in ("
				+ objRegistrationSample.getNtransactionsamplecode() + ") and tgr.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and tgr.ntestgrouprulesenginecode in (" + inputMap.get("ntestgrouprulesenginecode") + ")"
				+ ") ) x  where (x.jsondata->>'nneedsample')::int=4 )  ";
		
		// Original
//		String rulesquery = "(select a.nrepeatcountno from "
//				+ "	(select sum(z.nrepeatcountno) as nrepeatcountno,z.ntestgrouptestcode  as ntestgrouptestcode from( "
//				+ " select   " + " case when y.nrepeatcountno=0 then 1 else " + "  y.nrepeatcountno  " + " end  "
//				+ "  as nrepeatcountno  " + "	,y.ntestgrouptestcode,y.stestsynonym " + " from  "
//				+ " (select  x.jsondata->>'stestsynonym' as stestsynonym ,  "
//				+ " (x.jsondata->>'nrepeatcountno')::int as nrepeatcountno  "
//				+ " ,(x.jsondata->'ntestgrouptestcode')::int as ntestgrouptestcode  " + " from  "
//				+ " (SELECT Jsonb_array_elements(jsonuidata -> 'testInitiateTests') " + "  AS " + "   jsondata "
//				+ "  FROM   testgrouprulesengine "
//				+ "   WHERE  ntestgrouprulesenginecode IN (select tgr.ntestgrouprulesenginecode " + "from  "
//				+ "registrationsample rs,registrationtest rt,testgrouprulesengine tgr " + "where "
//				+ "rs.ntransactionsamplecode=rt.ntransactionsamplecode "
//				+ "and rt.ntestgrouptestcode=tgr.ntestgrouptestcode " + "and rs.ntransactionsamplecode in ("
//				+ objRegistrationSample.getNtransactionsamplecode() + ") " + "and tgr.ntransactionstatus="
//				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
//				+ " and tgr.ntestgrouprulesenginecode in (" + inputMap.get("ntestgrouprulesenginecode") + ")" + ") "
//				+ " union all " + " SELECT Jsonb_array_elements(jsonuidata -> 'testRepeatTests') AS " + " jsondata "
//				+ "  FROM   testgrouprulesengine "
//				+ "  WHERE  ntestgrouprulesenginecode IN   (select tgr.ntestgrouprulesenginecode " + "	from  "
//				+ "	registrationsample rs,registrationtest rt,testgrouprulesengine tgr " + "	where "
//				+ "	rs.ntransactionsamplecode=rt.ntransactionsamplecode "
//				+ "	and rt.ntestgrouptestcode=tgr.ntestgrouptestcode " + "	and rs.ntransactionsamplecode in ("
//				+ objRegistrationSample.getNtransactionsamplecode() + ") " + "	and tgr.ntransactionstatus="
//				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
//				+ " and tgr.ntestgrouprulesenginecode in (" + inputMap.get("ntestgrouprulesenginecode") + ")"
//				+ ") ) x )y   "
//				+ ") z group by z.ntestgrouptestcode )a where a.ntestgrouptestcode= tgt.ntestgrouptestcode) "
//				+ "  AS nrepeatcountno";
		
		String rulesquery = " y.nparenttestgrouptestcode as nparenttestgrouptestcode ,(select a.nrepeatcountno from "
				+ "	(select sum(z.nrepeatcountno) as nrepeatcountno,z.ntestgrouptestcode  as ntestgrouptestcode,z.nparenttestgrouptestcode from( "
				+ " select   " + " case when y.nrepeatcountno=0 then 1 else " + "  y.nrepeatcountno  " + " end  "
				+ "  as nrepeatcountno  " + "	,y.ntestgrouptestcode,y.stestsynonym,y.nparenttestgrouptestcode "
				+ " from  " 
				+ " (select  x.jsondata->>'stestsynonym' as stestsynonym ,  "
				+ " (x.jsondata->>'nrepeatcountno')::int as nrepeatcountno  "
				+ " ,(x.jsondata->'ntestgrouptestcode')::int as ntestgrouptestcode  "
				+ " ,x.ntestgrouptestcode as nparenttestgrouptestcode  " + " from  "
				+ " (SELECT ntestgrouptestcode,Jsonb_array_elements(jsonuidata -> 'testInitiateTests') " + "  AS "
				+ "   jsondata " + "  FROM   testgrouprulesengine "
				+ "   WHERE  ntestgrouprulesenginecode IN (select tgr.ntestgrouprulesenginecode " + "from  "
				+ "registrationsample rs,registrationtest rt,testgrouprulesengine tgr " + "where "
				+ "rs.ntransactionsamplecode=rt.ntransactionsamplecode "
				+ "and rt.ntestgrouptestcode=tgr.ntestgrouptestcode " + "and rs.ntransactionsamplecode in ("
				+ objRegistrationSample.getNtransactionsamplecode() + ") " + "and tgr.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and tgr.ntestgrouprulesenginecode in (" + inputMap.get("ntestgrouprulesenginecode") + ")" + ") "
				+ " union all "
				+ " SELECT  ntestgrouptestcode,Jsonb_array_elements(jsonuidata -> 'testRepeatTests') AS " + " jsondata "
				+ "  FROM   testgrouprulesengine "
				+ "  WHERE  ntestgrouprulesenginecode IN   (select tgr.ntestgrouprulesenginecode " + "	from  "
				+ "	registrationsample rs,registrationtest rt,testgrouprulesengine tgr where "
				+ "	rs.ntransactionsamplecode=rt.ntransactionsamplecode "
				+ "	and rt.ntestgrouptestcode=tgr.ntestgrouptestcode and rs.ntransactionsamplecode in ("
				+ objRegistrationSample.getNtransactionsamplecode() + ") 	and tgr.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and tgr.ntestgrouprulesenginecode in (" + inputMap.get("ntestgrouprulesenginecode") + ")"
				+ ") ) x )y   "
				+ ") z group by z.ntestgrouptestcode,z.nparenttestgrouptestcode )a where a.ntestgrouptestcode= tgt.ntestgrouptestcode"
				+ " and a.nparenttestgrouptestcode=y.nparenttestgrouptestcode ) " + "  AS nrepeatcountno";

		final String testGroupQuery = " select  " + rulesquery
				+ ", tgt.ntestgrouptestcode, tgt.ntestcode,tgt.stestsynonym, tgt.nsectioncode,"
				+ " s.ssectionname, tgt.nmethodcode, m.smethodname, tgt.ninstrumentcatcode, tm.nchecklistversioncode,"
				+ " coalesce((" + " select max(ntestrepeatno) + 1 from registrationtest "
				+ " where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode() + ")"
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + " ),1) ntestrepeatno,"
				+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + " ntestretestno, " + " tgt.ncost,"
				+ " case when " + needJobAllocation + " = false then " + stestStatus + " else "
				+ " coalesce((select max(ntransactionstatus) from registrationsectionhistory " + " where npreregno="
				+ objRegistrationSample.getNpreregno() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " and nsectioncode = tgt.nsectioncode), " + stestStatus
				+ " ) end ntransactionstatus,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
				+ " from testgrouptest tgt, testmaster tm, transactionstatus ts, section s,method m , "
				+ " (select  x.jsondata->>'stestsynonym' as stestsynonym ,  "
				+ " (x.jsondata->>'nrepeatcountno')::int as nrepeatcountno  "
				+ " ,(x.jsondata->'ntestgrouptestcode')::int as ntestgrouptestcode  "
				+ " ,x.ntestgrouptestcode as nparenttestgrouptestcode,x.jsondata  " + " from  "
				+ " (SELECT ntestgrouptestcode,Jsonb_array_elements(jsonuidata -> 'testInitiateTests') " + "  AS "
				+ "   jsondata " + "  FROM   testgrouprulesengine "
				+ "   WHERE  ntestgrouprulesenginecode IN (select tgr.ntestgrouprulesenginecode " + "from  "
				+ "registrationsample rs,registrationtest rt,testgrouprulesengine tgr " + "where "
				+ "rs.ntransactionsamplecode=rt.ntransactionsamplecode "
				+ "and rt.ntestgrouptestcode=tgr.ntestgrouptestcode " + "and rs.ntransactionsamplecode in ("
				+ objRegistrationSample.getNtransactionsamplecode() + ") " + "and tgr.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and tgr.ntestgrouprulesenginecode in (" + inputMap.get("ntestgrouprulesenginecode") + ")" + ") "
				+ " union all "
				+ " SELECT  ntestgrouptestcode,Jsonb_array_elements(jsonuidata -> 'testRepeatTests') AS " + " jsondata "
				+ "  FROM   testgrouprulesengine "
				+ "  WHERE  ntestgrouprulesenginecode IN   (select tgr.ntestgrouprulesenginecode " + "	from  "
				+ "	registrationsample rs,registrationtest rt,testgrouprulesengine tgr " + "	where "
				+ "	rs.ntransactionsamplecode=rt.ntransactionsamplecode "
				+ "	and rt.ntestgrouptestcode=tgr.ntestgrouptestcode " + "	and rs.ntransactionsamplecode in ("
				+ objRegistrationSample.getNtransactionsamplecode() + ") " + "	and tgr.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and tgr.ntestgrouprulesenginecode in (" + inputMap.get("ntestgrouprulesenginecode") + ")"
				+ ") ) x where (x.jsondata->>'nneedsample')::int=4 )y   "
				+ " where tm.ntestcode = tgt.ntestcode"
				+"    AND tgt.ntestgrouptestcode =y.ntestgrouptestcode "
				+ " and tgt.nstatus = tm.nstatus" 
				+ " and tgt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode in (" + stestStatus
				+ ") and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgt.nsectioncode = s.nsectioncode and tgt.nmethodcode=m.nmethodcode "
				+ " and s.nstatus= m.nstatus and m.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		 testGroupTestList = jdbcTemplate.query(testGroupQuery, new RegistrationTest());

		 rulesResultTable1="	   	inner join  "
				+ "       (SELECT x.jsondata ->> 'stestsynonym'                 AS stestsynonym, "
				+ "               ( x.jsondata ->> 'nrepeatcountno' ) :: INT    AS nrepeatcountno, "
				+ "               ( x.jsondata -> 'ntestgrouptestcode' ) :: INT AS "
				+ "               ntestgrouptestcode, "
				+ "               x.ntestgrouptestcode                          AS "
				+ "               nparenttestgrouptestcode "
				+ "        FROM   (SELECT ntestgrouptestcode, "
				+ "                       Jsonb_array_elements(jsonuidata -> 'testInitiateTests') "
				+ "                       AS "
				+ "                       jsondata "
				+ "                FROM   testgrouprulesengine "
				+ "                WHERE  ntestgrouprulesenginecode IN "
				+ "                       (SELECT tgr.ntestgrouprulesenginecode "
				+ "                        FROM   registrationsample rs, "
				+ "                               registrationtest rt, "
				+ "                               testgrouprulesengine tgr "
				+ "                        WHERE "
				+ "                rs.ntransactionsamplecode = rt.ntransactionsamplecode "
				+ "                AND rt.ntestgrouptestcode = tgr.ntestgrouptestcode "
				+ "                AND rs.ntransactionsamplecode IN ( "+objRegistrationSample.getNtransactionsamplecode()+") "
				+ "                AND tgr.ntransactionstatus =   "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ "                AND tgr.ntestgrouprulesenginecode IN ( "+inputMap.get("ntestgrouprulesenginecode")+")) "
				+ "                UNION ALL "
				+ "                SELECT ntestgrouptestcode, "
				+ "                       Jsonb_array_elements(jsonuidata -> 'testRepeatTests') AS "
				+ "                       jsondata "
				+ "                FROM   testgrouprulesengine "
				+ "                WHERE  ntestgrouprulesenginecode IN "
				+ "                       (SELECT tgr.ntestgrouprulesenginecode "
				+ "                        FROM   registrationsample rs, "
				+ "                               registrationtest rt, "
				+ "                               testgrouprulesengine tgr "
				+ "                        WHERE "
				+ "                rs.ntransactionsamplecode = rt.ntransactionsamplecode "
				+ "                AND rt.ntestgrouptestcode = tgr.ntestgrouptestcode "
				+ "                AND rs.ntransactionsamplecode IN ( "+objRegistrationSample.getNtransactionsamplecode()+" ) "
				+ "                AND tgr.ntransactionstatus =  "+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ "                AND tgr.ntestgrouprulesenginecode IN ("+inputMap.get("ntestgrouprulesenginecode")+"))) x)z	 "
				+ "			ON tgt.ntestgrouptestcode = z.ntestgrouptestcode  ";
		
		 
		 rulesResultTable2 = " left outer join  (SELECT  "
				+ "                          x.jsondata ->> 'salertmessage' as  salertmessage,  "
				+ "                          x.jsondata ->> 'additionalInfo' as  additionalInfo,  "
				+ "                                  x.jsondata ->> 'additionalInfoUidata' as additionalInfoUidata ,  "
				+ "                          x.jsondata ->> 'ntestgrouptestpredefcode' as ntestgrouptestpredefcode ,  "
				+ "                                          x.jsondata ->> 'sresultcomment'   as sresultcomment "
				+ "                          , " + "                          x.jsondata ->> 'sresult' "
				+ "                          AS " + "                          sresult, "
				+ "  x.jsondata ->> 'sfinal'    AS sfinal,"
				+ "( x.jsondata ->> 'ntestgrouptestparametercode' ) :: INT   AS  ntestgrouptestparametercode , "
				+ "    ( x.nparenttestgrouptestcode )  "
				+ "     AS "
				+ "     nparenttestgrouptestcode,(x.jsondata->>'ngradecode') :: INT  as ngradecode "
				+ "FROM   (SELECT Jsonb_array_elements(( " + "                        Jsonb_array_elements( "
				+ "     jsonuidata -> 'testInitiateTests')->> 'ParameterRulesEngine' ) ::  jsonb) "
				+ "                AS  jsondata,ntestgrouptestcode     AS  nparenttestgrouptestcode FROM   testgrouprulesengine "
				+ " WHERE  ntestgrouprulesenginecode IN ( (select tgr.ntestgrouprulesenginecode from  "
				+ "				registrationsample rs,registrationtest rt,testgrouprulesengine tgr 	where "
				+ "			rs.ntransactionsamplecode=rt.ntransactionsamplecode "
				+ "				and rt.ntestgrouptestcode=tgr.ntestgrouptestcode 	and rs.ntransactionsamplecode in ("
				+ 	objRegistrationSample.getNtransactionsamplecode() + ") 	and tgr.ntransactionstatus="
				+ 	Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and tgr.ntestgrouprulesenginecode in (" + inputMap.get("ntestgrouprulesenginecode") + ")"
				+ ") ) "
				+ "        AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")x)y	 "
				+ "	  ON y.ntestgrouptestparametercode = "
				+ "      tgtp.ntestgrouptestparametercode "
				+ "  and y.nparenttestgrouptestcode=z.nparenttestgrouptestcode ";
		}else {
			String testCode=(String) inputMap.get(String.valueOf(objRegistrationSample.getNtransactionsamplecode())).toString();
			final String testGroupQuery = " select  tgt.nrepeatcountno, tgt.ntestgrouptestcode, tgt.ntestcode,tgt.stestsynonym, tgt.nsectioncode,"
					+ " s.ssectionname, tgt.nmethodcode, m.smethodname, tgt.ninstrumentcatcode, tm.nchecklistversioncode,"
					+ " coalesce((" + " select max(ntestrepeatno) + 1 from registrationtest "
					+ " where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode() + ")"
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + " ),1) ntestrepeatno,"
					+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + " ntestretestno, " + " tgt.ncost,"
					+ " case when " + needJobAllocation + " = false then " + stestStatus + " else "
					+ " coalesce((select max(ntransactionstatus) from registrationsectionhistory " + " where npreregno="
					+ objRegistrationSample.getNpreregno() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nsectioncode = tgt.nsectioncode), " + stestStatus
					+ " ) end ntransactionstatus,"+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
					+ " from testgrouptest tgt, testmaster tm, transactionstatus ts, section s,method m  "
					+ " where tm.ntestcode = tgt.ntestcode" + " and tgt.ntestgrouptestcode in (" + testCode
					+ ") and tgt.nstatus = tm.nstatus" + " and tgt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode in (" + stestStatus
					+ ") and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tgt.nsectioncode = s.nsectioncode and tgt.nmethodcode=m.nmethodcode "
					+ " and s.nstatus= m.nstatus and m.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			
			 testGroupTestList = jdbcTemplate.query(testGroupQuery, new RegistrationTest());
		}
		if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()) {
			final int preregno = objRegistrationSample.getNpreregno();
			Optional<Map<String, Object>> age = ((List<Map<String, Object>>) inputMap.get("ageData")).stream()
					.filter(x -> ((Integer) x.get("npreregno")) == preregno).findAny();
			int nage = (int) age.get().get("nage");
			int ngendercode = (int) age.get().get("ngendercode");
			
			if(!isNewSample) {
			strQuery = " case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype()
					+ " then case when  " + " tgtcs.ngendercode=" + ngendercode + " and " + nage
					+ " between tgtcs.nfromage and tgtcs.ntoage then "
					+ " jsonb_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,"
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',y.sresult,'sfinal',y.sfinal"
					+ " ,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
					+ " coalesce((select max(ntestrepeatno)  from registrationtest "
					+ "	where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
					+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  and nsitecode=" + userInfo.getNtranssitecode()
					+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
					+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym,"

					+ " 'salertmessage', y.salertmessage,'additionalInfo', y.additionalInfo,'additionalInfoUidata',y.additionalInfoUidata"
					+ " ,'ntestgrouptestpredefcode',y.ntestgrouptestpredefcode,'sresultcomment',y.sresultcomment,'ngradecode',y.ngradecode)::jsonb "

					+ " else "
					+ " jsonb_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,"
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult', y.sresult,'sfinal',y.sfinal"
					+ ",'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
					+ " coalesce((select max(ntestrepeatno)  from registrationtest "
					+ "	where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
					+ ") and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
					+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym"
					+ ", 'salertmessage', y.salertmessage,'additionalInfo', y.additionalInfo,'additionalInfoUidata',y.additionalInfoUidata"
					+ " ,'ntestgrouptestpredefcode',y.ntestgrouptestpredefcode,'sresultcomment',y.sresultcomment,'ngradecode',y.ngradecode)::jsonb "
					+ " end else " + " jsonb_build_object("
					+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
					+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
					+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
					+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
					+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
					+ "'nroundingdigits',tgtp.nroundingdigits,'sresult', y.sresult,'sfinal',y.sfinal,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
					+ " coalesce((select max(ntestrepeatno)  from registrationtest "
					+ "	where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
					+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = " + userInfo.getNtranssitecode()
					+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
					+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym"

					+ " ,'salertmessage', y.salertmessage,'additionalInfo', y.additionalInfo,'additionalInfoUidata',y.additionalInfoUidata"
					+ " ,'ntestgrouptestpredefcode',y.ntestgrouptestpredefcode,'sresultcomment',y.sresultcomment,'ngradecode',y.ngradecode)::jsonb "

					+ " end jsondata," + userInfo.getNtranssitecode() + " nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ " from testmaster tm,testgrouptest tgt inner join "
					+ " testgrouptestparameter tgtp  on tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode "
					+ "	left outer join testgrouptestclinicalspec tgtcs on tgtcs.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
					+ "	and tgtcs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and tgtcs.ngendercode=" + ngendercode + " and " + nage
					+ " between tgtcs.nfromage and tgtcs.ntoage ";
			}else {
				strQuery = " case when tgtp.nparametertypecode=" + Enumeration.ParameterType.NUMERIC.getparametertype()
				+ " then case when  " + " tgtcs.ngendercode=" + ngendercode + " and " + nage
				+ " between tgtcs.nfromage and tgtcs.ntoage then "
				+ " json_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,'ngradecode',tgtcs.ngradecode,"
				+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
				+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
				+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
				+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
				+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
				+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
				+ " coalesce((select max(ntestrepeatno)  from registrationtest "
				+ "	where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
				+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and nsitecode=" + userInfo.getNtranssitecode()
				+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
				+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym)::jsonb " + " else "
				+ " json_build_object('nfromage',tgtcs.nfromage,'ntoage',tgtcs.ntoage,'ngendercode',tgtcs.ngendercode,'ngradecode',tgtcs.ngradecode,"
				+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
				+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
				+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
				+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
				+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
				+ " 'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
				+ " coalesce((select max(ntestrepeatno)  from registrationtest "
				+ "	where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
				+ ") and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
				+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym)::jsonb "
				+ " end else " + " json_build_object("
				+ " 'sminb',case when tgtcs.slowb='null' then NULL else tgtcs.slowb end,"
				+ " 'smina',case when tgtcs.slowa='null' then NULL else tgtcs.slowa end,"
				+ " 'smaxa',case when tgtcs.shigha='null' then NULL else tgtcs.shigha end,"
				+ " 'smaxb',case when tgtcs.shighb='null' then NULL else tgtcs.shighb end,"
				+ " 'sresultvalue',case when tgtcs.sresultvalue='null' then NULL else tgtcs.sresultvalue end,"
				+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
				+ " coalesce((select max(ntestrepeatno)  from registrationtest "
				+ "	where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
				+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNtranssitecode()
				+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
				+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym)::jsonb "
				+ " end jsondata," + userInfo.getNtranssitecode() + " nsitecode,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
				+ " from testmaster tm,testgrouptest tgt inner join "
				+ " testgrouptestparameter tgtp  on tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode "
				+ "	left outer join testgrouptestclinicalspec tgtcs on tgtcs.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
				+ "	and tgtcs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and tgtcs.ngendercode=" + ngendercode + " and " + nage
				+ " between tgtcs.nfromage and tgtcs.ntoage ";
			}
		} else {
			if(!isNewSample) {
			strQuery = "jsonb_build_object('sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,"
					+ "'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,"
					+ "'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,"
					+ "'nroundingdigits',tgtp.nroundingdigits,'sresult', y.sresult,'sfinal',y.sfinal "
					+ ",'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
					+ " coalesce((select max(ntestrepeatno)  from registrationtest "
					+ "	where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
					+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
					+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym"
					+ " ,'salertmessage', y.salertmessage,'additionalInfo', y.additionalInfo,'additionalInfoUidata',y.additionalInfoUidata"
					+ " ,'ntestgrouptestpredefcode',y.ntestgrouptestpredefcode,'sresultcomment',y.sresultcomment,'ngradecode',y.ngradecode)::jsonb "
					+ " jsondata" + " ," + userInfo.getNtranssitecode() + " nsitecode,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
					+ "from testmaster tm,testgrouptest tgt,testgrouptestparameter tgtp";
			}else {
				strQuery = "json_build_object('sminlod',tgtnp.sminlod,'smaxlod',tgtnp.smaxlod,'sminb',tgtnp.sminb,'smina',tgtnp.smina,"
						+ "'smaxa',tgtnp.smaxa,'smaxb',tgtnp.smaxb,'sminloq',tgtnp.sminloq,'smaxloq',tgtnp.smaxloq,'ngradecode',tgtnp.ngradecode,"
						+ "'sdisregard',tgtnp.sdisregard,'sresultvalue',tgtnp.sresultvalue,"
						+ "'nroundingdigits',tgtp.nroundingdigits,'sresult',NULL,'sfinal',NULL,'stestsynonym',concat(tgt.stestsynonym,'[',cast(  "
						+ " coalesce((select max(ntestrepeatno)  from registrationtest "
						+ "	where ntransactionsamplecode in (" + objRegistrationSample.getNtransactionsamplecode()
						+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode()
						+ " and ntestgrouptestcode = tgt.ntestgrouptestcode" + "	),1)"
						+ " as character varying),']','[0]'),'sparametersynonym',tgtp.sparametersynonym)::jsonb jsondata"
						+ " ," + userInfo.getNtranssitecode() + " nsitecode,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus "
						+ "from testmaster tm,testgrouptest tgt,testgrouptestparameter tgtp";
			}
		}
		
     String queryValue= !isNewSample?"z.nparenttestgrouptestcode as nparenttestgrouptestcode,":""; 
     String queryValue2= !isNewSample?comPleted:(String) inputMap.get(String.valueOf(objRegistrationSample.getNtransactionsamplecode())).toString();
     
     String	  query4 = " select  "+queryValue+" tgt.ntestgrouptestcode, tgtp.ntestgrouptestparametercode, tgtp.ntestparametercode,"
				+ " tgtp.nparametertypecode, coalesce(tgtf.ntestgrouptestformulacode, "
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ")"
				+ " ntestgrouptestformulacode, tgtp.nunitcode, tgtp.nresultmandatory, tgtp.nreportmandatory," + strQuery
				+ " left outer join testgrouptestformula tgtf"
				+ " on tgtp.ntestgrouptestparametercode = tgtf.ntestgrouptestparametercode and tgtf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgtf.ntransactionstatus = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " left outer join testgrouptestnumericparameter tgtnp on tgtp.ntestgrouptestparametercode = tgtnp.ntestgrouptestparametercode"
				+ " and tgtnp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ rulesResultTable1
				+ rulesResultTable2
				+ " where tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode and tm.ntestcode = tgt.ntestcode and tgt.nstatus = tm.nstatus"
				+ " and tgt.nstatus = tgtp.nstatus and tgt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgt.ntestgrouptestcode in ("+ queryValue2 + ")";

		final List<TestGroupTestParameter> parameterList = jdbcTemplate.query(query4, new TestGroupTestParameter());

		for (RegistrationTest regTestGroupTest : testGroupTestList) {
			if (regTestGroupTest.getNrepeatcountno() > 1) {
				for (int repeatNo = regTestGroupTest.getNtestrepeatno(); repeatNo < (regTestGroupTest.getNtestrepeatno()
						+ regTestGroupTest.getNrepeatcountno()); repeatNo++)
				{
					int nttestcode = nTransactionTestCode++;
					transactionTestCodeList.add(nttestcode);

					if (objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
							.gettransactionstatus()
							&& objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
									.gettransactionstatus()) {

						if (isAutoComplete) {
							autoCompleteTestCodeList.add(nttestcode);
						} else {
							nonAutoCompleteTestCodeList.add(nttestcode);
						}
						if (!needJobAllocation) {
							jobAllocationTestList.add(nttestcode);
						}

					}

					replicateTestList.add("(" + nttestcode + "," + objRegistrationSample.getNtransactionsamplecode()
							+ "," + objRegistrationSample.getNpreregno() + ","
							+ regTestGroupTest.getNtestgrouptestcode() + "," + regTestGroupTest.getNinstrumentcatcode()
							+ ",-1," + repeatNo + ",0," + " json_build_object('ntransactiontestcode', " + nttestcode
							+ ",'npreregno'," + objRegistrationSample.getNpreregno() + ",'ntransactionsamplecode',"
							+ objRegistrationSample.getNtransactionsamplecode() + ",'ssectionname','"
							+ regTestGroupTest.getSsectionname() + "','smethodname','"
							+ regTestGroupTest.getSmethodname() + "','ncost'," + regTestGroupTest.getNcost() + ","
							+ "'stestname','" + regTestGroupTest.getStestsynonym() + "'," + "'stestsynonym',concat('"
							+ regTestGroupTest.getStestsynonym() + "','[" + repeatNo + "]["
							+ regTestGroupTest.getNtestretestno() + "]'))::jsonb," + userInfo.getNtranssitecode() + ",'"
							+dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ regTestGroupTest.getNtestcode() + "," + regTestGroupTest.getNsectioncode() + ","
							+ regTestGroupTest.getNmethodcode() + ")");

					boolean gradevalidation=false;
					for (TestGroupTestParameter registrationParameter : parameterList) {
						if (regTestGroupTest.getNtestgrouptestcode() == registrationParameter.getNtestgrouptestcode()) {
							if (regTestGroupTest.getNparenttestgrouptestcode() == registrationParameter.getNparenttestgrouptestcode()) {
							int nttestparametercode = nRegistrationParameterCode++;

							final Map<String, Object> mapObject = registrationParameter.getJsondata();
							mapObject.put("ntransactionresultcode", nttestparametercode);
							mapObject.put("ntransactiontestcode", nttestcode);
							mapObject.put("stestsynonym", regTestGroupTest.getStestsynonym() + "[" + repeatNo + "]["
									+ regTestGroupTest.getNtestretestno() + "]");
							
							if (registrationParameter.getNparametertypecode() == Enumeration.ParameterType.NUMERIC
									.getparametertype()&&mapObject.get("sfinal")!=null) {
								int ngradecode = findGrade(mapObject);
								mapObject.put("ngradecode", ngradecode);
							}  
							
							replicateTestParameterList.add("(" + nttestparametercode + ","
									+ objRegistrationSample.getNpreregno() + "," + nttestcode + ","
									+ registrationParameter.getNtestgrouptestparametercode() + ","
									+ registrationParameter.getNtestparametercode() + ","
									+ registrationParameter.getNparametertypecode() + ","
									+ registrationParameter.getNtestgrouptestformulacode() + ","
									+ registrationParameter.getNunitcode() + ","
									+ registrationParameter.getNresultmandatory() + ","
									+ registrationParameter.getNreportmandatory() + "," + "'"
									+ objMapper.writeValueAsString(mapObject) + "'," + userInfo.getNtranssitecode()
									+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
							}
						}
					}
					int transactionstatus=regTestGroupTest.getNtransactionstatus();
					int usercode=-1;
					int userrolecode=-1;
					//ALPD-4649--Vignesh R(16-09-2024)
					if(gradevalidation && nneedtestinitiate )
					{
						 transactionstatus=Enumeration.TransactionStatus.INITIATED.gettransactionstatus();
					}
					
					int nttesthistorycode = nTesthistoryCode++;
					replicateTestHistoryList.add("(" + nttesthistorycode + "," + nttestcode + ","
							+ objRegistrationSample.getNtransactionsamplecode() + ","
							+ objRegistrationSample.getNpreregno() + "," + userInfo.getNformcode() + ","
							+ transactionstatus + "," + usercode + ","
							+ userrolecode + "," + userInfo.getNdeputyusercode() + ","
							+ userInfo.getNdeputyuserrole() + ",'" +stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','"
							+dateUtilityFunction.getCurrentDateTime(userInfo) + "',-1," + userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
							+ ")");
				}
			} else {
				// no replicate mentioned in test group
				int nttestcode = nTransactionTestCode++;
				transactionTestCodeList.add(nttestcode);
				if (objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
						&& objRegistrationSample.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()) {
					if (isAutoComplete) {
						autoCompleteTestCodeList.add(nttestcode);
					} else {
						nonAutoCompleteTestCodeList.add(nttestcode);
					}
					if (!needJobAllocation) {
						jobAllocationTestList.add(nttestcode);
					}
				}
				replicateTestList.add("(" + nttestcode + "," + objRegistrationSample.getNtransactionsamplecode() + ","
						+ objRegistrationSample.getNpreregno() + "," + regTestGroupTest.getNtestgrouptestcode() + ","
						+ regTestGroupTest.getNinstrumentcatcode() + ",-1," + (regTestGroupTest.getNtestrepeatno())
						+ ",0," + " json_build_object('ntransactiontestcode', " + nttestcode + ",'npreregno',"
						+ objRegistrationSample.getNpreregno() + ",'ntransactionsamplecode',"
						+ objRegistrationSample.getNtransactionsamplecode() + ",'ssectionname','"
						+ regTestGroupTest.getSsectionname() + "','smethodname','" + regTestGroupTest.getSmethodname()
						+ "','ncost'," + regTestGroupTest.getNcost() + "," + "'stestname','"
						+ regTestGroupTest.getStestsynonym() + "'," + "'stestsynonym',concat('"
						+ regTestGroupTest.getStestsynonym() + "','[" + (regTestGroupTest.getNtestrepeatno())
						+ "][0]'))::jsonb," + userInfo.getNtranssitecode() + ",'" +dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ regTestGroupTest.getNtestcode() + "," + regTestGroupTest.getNsectioncode() + ","
						+ regTestGroupTest.getNmethodcode() + ")");

				boolean gradevalidation=false;
				for (TestGroupTestParameter testGroupTestParameter : parameterList) {
					//to Add Parameters based on Parent Test Group Test Codes
					if (regTestGroupTest.getNtestgrouptestcode() == testGroupTestParameter.getNtestgrouptestcode()) {
						if (regTestGroupTest.getNparenttestgrouptestcode() == testGroupTestParameter.getNparenttestgrouptestcode()) {
						int nttestparametercode = nRegistrationParameterCode++;

						final Map<String, Object> mapObject = testGroupTestParameter.getJsondata();
						mapObject.put("ntransactionresultcode", nttestparametercode);
						mapObject.put("ntransactiontestcode", nttestcode);
						
						//Added by sonia on  24-07-2024 for Jira ID:ALPD-4586
						final String currentDateTime =dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z","");
						
						mapObject.put("dentereddate", currentDateTime);
						mapObject.put("dentereddatetimezonecode", userInfo.getNtimezonecode());
						mapObject.put("noffsetdentereddate", dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));
						
						
						if (testGroupTestParameter.getNparametertypecode() == Enumeration.ParameterType.NUMERIC.getparametertype()&&mapObject.get("sfinal")!=null) {
							int ngradecode = findGrade(mapObject);
							mapObject.put("ngradecode", ngradecode);
						}  
						if(mapObject.get("sfinal")!=null)
						{
							gradevalidation=true;
						}
						//ALPD-4649--Vignesh R(16-09-2024)
						if (needJobAllocation&&mapObject.get("sfinal")!=null) {
							jobAllocationTestList.add(nttestcode);
						}
						replicateTestParameterList
								.add("(" + nttestparametercode + "," + objRegistrationSample.getNpreregno() + ","
										+ nttestcode + "," + testGroupTestParameter.getNtestgrouptestparametercode()
										+ "," + testGroupTestParameter.getNtestparametercode() + ","
										+ testGroupTestParameter.getNparametertypecode() + ","
										+ testGroupTestParameter.getNtestgrouptestformulacode() + ","
										+ testGroupTestParameter.getNunitcode() + ","
										+ testGroupTestParameter.getNresultmandatory() + ","
										+ testGroupTestParameter.getNreportmandatory() + ","
										+ "'" + objMapper.writeValueAsString(mapObject) + "',"
										+ userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")");
						}
					}
				}
				
				int transactionstatus=regTestGroupTest.getNtransactionstatus();
				int usercode=userInfo.getNusercode();
				int userrolecode=userInfo.getNuserrole();
				//ALPD-4649--Vignesh R(16-09-2024)
				if(gradevalidation&&nneedtestinitiate)
				{
					 transactionstatus=Enumeration.TransactionStatus.INITIATED.gettransactionstatus();
					 usercode=-1;
					 userrolecode=-1;
				}
				
				int nttesthistorycode = nTesthistoryCode++;
				replicateTestHistoryList.add("(" + nttesthistorycode + "," + nttestcode + ","
						+ objRegistrationSample.getNtransactionsamplecode() + "," + objRegistrationSample.getNpreregno()
						+ "," + userInfo.getNformcode() + ","
						+ transactionstatus + "," + usercode + ","
						+ userrolecode + "," + userInfo.getNdeputyusercode() + ","
						+ userInfo.getNdeputyuserrole() + ",'" +stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','"
						+dateUtilityFunction.getCurrentDateTime(userInfo) + "',-1," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
						+ ")");

			}
		}

		final Map<String, Object> returnMap = new HashMap<String, Object>() {
		};

		returnMap.put("replicateTestHistoryList", replicateTestHistoryList);
		returnMap.put("replicateTestList", replicateTestList);
		returnMap.put("replicateTestParameterList", replicateTestParameterList);
		returnMap.put("nTransactionTestCode", nTransactionTestCode);
		returnMap.put("nTesthistoryCode", nTesthistoryCode);
		returnMap.put("nRegistrationParameterCode", nRegistrationParameterCode);
		returnMap.put("autoCompleteTestCodeList", autoCompleteTestCodeList);
		returnMap.put("nonAutoCompleteTestCodeList", nonAutoCompleteTestCodeList);
		returnMap.put("transactionTestCodeList", transactionTestCodeList);
		returnMap.put("jobAllocationTestList", jobAllocationTestList);

		return returnMap;

	}

	private String validateTestAutoComplete(List<String> listSample, List<TestGroupTest> listTest,
			final UserInfo userInfo) throws Exception {
		final String sQuery = "select case when tgtp.nparametertypecode="
				+ Enumeration.ParameterType.NUMERIC.getparametertype()
				+ " then tgtnp.sresultvalue else tgtcp.scharname end sresultvalue,"
				+ " tgtp.nparametertypecode, tgtp.ntestgrouptestparametercode from registrationtest rt "
				+ " inner join testgrouptest tgt on tgt.ntestgrouptestcode = rt.ntestgrouptestcode"
				+ " and tgt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
				+ " inner join testgrouptestparameter tgtp on tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode"
				+ " and tgtp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestcharparameter tgtcp on tgtcp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
				+ " and tgtcp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode"
				+ " and tgtnp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " where rt.ntransactionsamplecode in (" + String.join(",", listSample) + ")"
				+ " and tgt.ntestgrouptestcode in (" +stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode") + ")"
				+ " and tgtp.nparametertypecode in(" + Enumeration.ParameterType.NUMERIC.getparametertype() + ","
				+ Enumeration.ParameterType.CHARACTER.getparametertype() + ") "
				+ " and case when tgtp.nparametertypecode = " + Enumeration.ParameterType.NUMERIC.getparametertype()
				+ " then tgtnp.sresultvalue else tgtcp.scharname end is null and tgtp.nresultmandatory = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ";";
// @SuppressWarnings("unchecked")
		List<TestGroupTestNumericParameter> validateList = jdbcTemplate.query(sQuery,
				new TestGroupTestNumericParameter());
		if (validateList.isEmpty()) {
			return Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		} else {
			return "IDS_DEFAULTRESULTVALUEISNOTAVAILABLE";
		}
	}
	private boolean  isNaN(String st) {
		//if(st=="" ||st==null) {  //Commented by Sonia on 06th Sep 2024 for JIRA ID:ALPD-4586
		if(st.trim().equals("") || st==null) { //Modified the if condition by Sonia on 06th Sep 2024 for JIRA ID:ALPD-4586
			return true;
		}else
		{
			return false;
		}
	}
	private int findGrade(Map<String, Object> paramData) {  
		
		String minB =paramData.get("sminb")==null?"":paramData.get("sminb").toString();
		String minA =paramData.get("smina")==null?"":paramData.get("smina").toString();
		String maxA =paramData.get("smaxa")==null?"":paramData.get("smaxa").toString();
		String maxB =paramData.get("smaxb")==null?"":paramData.get("smaxb").toString(); 
		Float result = Float.valueOf(paramData.get("sfinal").toString());
		
		int ngradecode=Enumeration.Grade.FIO.getGrade();
		
		  if (isNaN(minA) && isNaN(minB) && isNaN(maxA) && isNaN(maxB)) {
			   //ALPD-4502 - when spec limits are not provided in test group
			  ngradecode= Enumeration.Grade.PASS.getGrade();
		    } else if (isNaN(minA) && !isNaN(minB) && isNaN(maxA) && !isNaN(maxB)) {
		        if (result >= Float.valueOf(minB) && result <= Float.valueOf(maxB))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else if (result < Float.valueOf(minB) || Float.valueOf(maxB) < result)
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		    } else if (!isNaN(minA) && isNaN(minB) && !isNaN(maxA) && isNaN(maxB)) {
		        if (result >= Float.valueOf(minA) && result <= Float.valueOf(maxA))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		    } else if (isNaN(minA) && isNaN(minB) && isNaN(maxA) && !isNaN(maxB)) {
		        if (result <= Float.valueOf(maxB)) {
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        } else if (result > Float.valueOf(maxB)) {
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        }
		    } else if (isNaN(minA) && isNaN(minB) && !isNaN(maxA) && isNaN(maxB)) {
		        if (result <= Float.valueOf(maxA)) {
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        } else if (result > Float.valueOf(maxA)) {
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        }
		    } else if (isNaN(minA) && isNaN(minB) && !isNaN(maxA) && !isNaN(maxB)) {
		        if (result > Float.valueOf(maxA) && result <= Float.valueOf(maxB))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else if (result > Float.valueOf(maxB))
		        	ngradecode= Enumeration.Grade.OOT.getGrade();
		        else if (result < Float.valueOf(maxA))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		    } else if (isNaN(minA) && !isNaN(minB) && isNaN(maxA) && isNaN(maxB)) {
		        if (result >= Float.valueOf(minB))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else if (result < Float.valueOf(minB))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		    } else if (isNaN(minA) && !isNaN(minB) && isNaN(maxA) && !isNaN(maxB)) {
		        if (result >= Float.valueOf(minA) && result <= Float.valueOf(maxB))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else if (result < Float.valueOf(minA))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else if (result > Float.valueOf(maxB))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		    } else if (isNaN(minA) && !isNaN(minB) && !isNaN(maxA) && !isNaN(maxB)) {
		        if (result >= Float.valueOf(minB))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else if (result > Float.valueOf(maxA) && result <= Float.valueOf(maxB))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else if (result > Float.valueOf(maxB))
		        	ngradecode= Enumeration.Grade.OOT.getGrade();
		        else if (result < Float.valueOf(minB))
		        	ngradecode= Enumeration.Grade.OOT.getGrade();
		    } else if (!isNaN(minA) && isNaN(minB) && isNaN(maxA) && isNaN(maxB)) {
		        if (result >= Float.valueOf(minA))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else if (result < Float.valueOf(minA))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		    } else if (!isNaN(minA) && isNaN(minB) && !isNaN(maxA) && !isNaN(maxB)) {
		        if (result >= Float.valueOf(minA) && result <= Float.valueOf(maxA))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else if (result >= Float.valueOf(minA) && result <= Float.valueOf(maxB))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else
		        	ngradecode= Enumeration.Grade.OOT.getGrade();
		    } else if (isNaN(minA) && !isNaN(minB) && !isNaN(maxA) && isNaN(maxB)) {
		        if (result >= Float.valueOf(minB) && result <= Float.valueOf(maxA))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else if (result < Float.valueOf(minB))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else if (result > Float.valueOf(maxA))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		    } else if (isNaN(minA) && !isNaN(minB) && !isNaN(maxA) && isNaN(maxB)) {
		        if (result >= Float.valueOf(minB) && result <= Float.valueOf(maxA))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else if (result > Float.valueOf(maxA) && result <= Float.valueOf(maxB))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else if (result > Float.valueOf(maxB) || result < Float.valueOf(minB))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		    } else if (!isNaN(minA) && !isNaN(minB) && isNaN(maxA) && !isNaN(maxB)) {
		        if (result >= Float.valueOf(minB) && result < Float.valueOf(minA))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else if (result < Float.valueOf(minB))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else if (result >= Float.valueOf(minA))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		    } else if (isNaN(minA) && !isNaN(minB) && isNaN(maxA) && !isNaN(maxB)) {
		        if (result >= Float.valueOf(minB) && result < Float.valueOf(minA))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else if (result >= Float.valueOf(minA) && result <= Float.valueOf(maxB))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else if (result > Float.valueOf(maxB) || result < Float.valueOf(minB))
		        	ngradecode= Enumeration.Grade.OOT.getGrade();
		    } else if (!isNaN(minA) && !isNaN(minB) && !isNaN(maxA) && isNaN(maxB)) {
		        if (result >= Float.valueOf(minB) && result < Float.valueOf(minA))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else if (result >= Float.valueOf(minA) && result <= Float.valueOf(maxA))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else if (result < Float.valueOf(minB))
		        	ngradecode= Enumeration.Grade.OOT.getGrade();
		        else if (result > Float.valueOf(maxA))
		        	ngradecode= Enumeration.Grade.OOT.getGrade();
		    } else if (!isNaN(minA) && !isNaN(minB) && !isNaN(maxA) && !isNaN(maxB)) {
		        if (result >= Float.valueOf(minA) && result <= Float.valueOf(maxA))
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		        else if (result >= Float.valueOf(minB) && result <= Float.valueOf(maxB))
		        	ngradecode= Enumeration.Grade.OOT.getGrade();
		        else if ((result < Float.valueOf(minB) || Float.valueOf(maxB) < result) && (Float.valueOf(minB) != 0 && Float.valueOf(maxB) != 0))
		        	ngradecode= Enumeration.Grade.OOS.getGrade();
		        else
		        	ngradecode= Enumeration.Grade.PASS.getGrade();
		    }
		  
		  return ngradecode;
	}
	

	
	public Map<String, Object> validateSeqnoSubSampleNo(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		final ObjectMapper objmap = new ObjectMapper();

		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final List<TestGroupTest> listTest = objmap.convertValue(inputMap.get("testgrouptest"),
				new TypeReference<List<TestGroupTest>>() {
				});

		Boolean skipMethodValidity = null;
		if (inputMap.containsKey("skipmethodvalidity")) {
			skipMethodValidity = (Boolean) inputMap.get("skipmethodvalidity");
		}

		final String sntestgrouptestcode =stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode");

		List<TestGroupTest> expiredMethodTestList = new ArrayList();

		if (expiredMethodTestList.isEmpty()) {
			String sQuery = " lock  table lockpreregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			String npreregno = (String) inputMap.get("npreregno");

			boolean needJobAllocation = (boolean) inputMap.get("nneedjoballocation");

			/**
			 * to get current status of selected samples
			 */
			final String squery = "select npreregno, ntransactionstatus from registrationhistory where"
					+ " nreghistorycode in (select max(nreghistorycode) from registrationhistory r"
					+ " where r.npreregno in (" + npreregno + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " group by r.npreregno)";
			List<RegistrationHistory> lstStatus = jdbcTemplate.query(squery, new RegistrationHistory());

			final List<RegistrationHistory> uncancelledUnRejectedList = lstStatus.stream().filter(
					x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
							&& x.getNtransactionstatus() != Enumeration.TransactionStatus.REJECTED
									.gettransactionstatus()
							&& x.getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED
									.gettransactionstatus())
					.collect(Collectors.toList());

			if (uncancelledUnRejectedList.isEmpty()) {
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						"IDS_SAMPLECANCELLEDREJECTEDRELEASED");
			} else {
				npreregno = stringUtilityFunction.fnDynamicListToString(uncancelledUnRejectedList, "getNpreregno");
				
				/**
				 * Commented below validation as there is requirement to add subsample after
				 * registration
				 */
				/**
				 * validation for sample at preregister status to add subsample
				 */
				
				List<TestGroupTest> TestGroupTests = objmap.convertValue(inputMap.get("testgrouptest"),
						new TypeReference<List<TestGroupTest>>() {});

				int testCount = TestGroupTests.stream().mapToInt(
						testgrouptest -> testgrouptest.getNrepeatcountno() == 0 ? 1 : testgrouptest.getNrepeatcountno())
						.sum();

				int paramterCount = TestGroupTests.stream()
						.mapToInt(testgrouptest -> ((testgrouptest.getNrepeatcountno() == 0 ? 1
								: testgrouptest.getNrepeatcountno()) * testgrouptest.getNparametercount()))
						.sum();

				boolean isSampleNotPreRegStatus = uncancelledUnRejectedList.stream().anyMatch(x -> x
						.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
				String registeredSampleGetSeq = "";

				if (isSampleNotPreRegStatus) {
					registeredSampleGetSeq = ",'registrationhistory', 'registrationsection', 'registrationsectionhistory',"
							+ "'joballocation'";
				}

				int nsampletypecode = (int) inputMap.get("nsampletypecode");
				int orderType = -1;
				String s = "select * from registration where npreregno in (" + npreregno + ")";
				List<Registration> lstReg = jdbcTemplate.query(s, new Registration());

				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
					if (!lstReg.isEmpty()) {
						if (lstReg.get(0).getJsondata().containsKey("Order Type")) {
							Registration reg = lstReg.get(0);
							JSONObject json = new JSONObject(reg.getJsondata());
							orderType = (int) ((JSONObject) json.get("Order Type")).getInt("value");
							if (orderType == 1 || orderType == -1) {
								registeredSampleGetSeq = registeredSampleGetSeq
										+ ",'externalordertest','externalordersample'";
							}

						}
					}
				}				

				final String strSelectSeqno = "select stablename,nsequenceno from seqnoregistration  where stablename "
						+ "in ('registrationparameter','registrationsample',"
						+ "'registrationsamplehistory','registrationtest','registrationtesthistory','externalordersample','externalordertest' "
						+ registeredSampleGetSeq + ");";

				final List<?> lstMultiSeqNo = projectDAOSupport.getMultipleEntitiesResultSetInList(strSelectSeqno,jdbcTemplate, SeqNoRegistration.class);

				final List<SeqNoRegistration> lstSeqNoReg = (List<SeqNoRegistration>) lstMultiSeqNo.get(0);

				returnMap = lstSeqNoReg.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
						SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

				final List<String> listPreregNo = new ArrayList<String>(Arrays.asList(npreregno.split(",")));

				String strSeqnoUpdate = "Update seqnoregistration set nsequenceno = "
						+ ((int) returnMap.get("registrationparameter") + (paramterCount))
						+ " where stablename = 'registrationparameter';" + "Update seqnoregistration set nsequenceno = "
						+ ((int) returnMap.get("registrationsample")
						+listTest.size())
						+ " where stablename = 'registrationsample';" + "Update seqnoregistration set nsequenceno = "
						+ ((int) returnMap.get("registrationsamplehistory") //+ listPreregNo.size()
						+listTest.size())
						+ " where stablename = 'registrationsamplehistory';";


				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()
						&& (orderType == 1 || orderType == -1)) {
					if (!lstReg.isEmpty()) {
						if (lstReg.get(0).getJsondata().containsKey("Order Type")) {
							strSeqnoUpdate = strSeqnoUpdate + "Update seqnoregistration set nsequenceno = "+ ((int) returnMap.get("externalordersample")
							+listTest.size())+ " where stablename = 'externalordersample';";

						}
					}
				}
				
				if (isSampleNotPreRegStatus) {
					/**
					 * sequence no. of registrationhistory has to be updated if the samples are not
					 * in quarantine/preregistered/ registered/partial
					 */

					final List<RegistrationHistory> sampleHistoryUpdateList = uncancelledUnRejectedList.stream()
							.filter(x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
									.gettransactionstatus()
									&& x.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
											.gettransactionstatus()
									&& x.getNtransactionstatus() != Enumeration.TransactionStatus.REGISTERED
											.gettransactionstatus()
									&& x.getNtransactionstatus() != Enumeration.TransactionStatus.PARTIALLY
											.gettransactionstatus())
							.collect(Collectors.toList());

					strSeqnoUpdate = strSeqnoUpdate + ";Update seqnoregistration set nsequenceno = "
							+ ((int) returnMap.get("registrationhistory") + sampleHistoryUpdateList.size())
							+ " where stablename = 'registrationhistory';";
					final List<RegistrationHistory> sampleWithoutPreregQuarantineStatus = uncancelledUnRejectedList
							.stream()
							.filter(x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
									.gettransactionstatus()
									&& x.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
											.gettransactionstatus())
							.collect(Collectors.toList());

					if (!sampleWithoutPreregQuarantineStatus.isEmpty()) {

						// Samples are available btw registered and final level of approval

						String preRegNoString = stringUtilityFunction.fnDynamicListToString(sampleWithoutPreregQuarantineStatus,
								"getNpreregno");
						final String sApprovalConfigQuery = "select acap.*, ra.npreregno, ra.sarno from registrationarno ra, approvalconfigautoapproval acap"
								+ " where acap.napprovalconfigversioncode = ra.napprovalversioncode and acap.nstatus = ra.nstatus"
								+ " and ra.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ra.npreregno in (" + preRegNoString + ")";
						
						final List<ApprovalConfigAutoapproval> approvalConfigAutoApprovals = jdbcTemplate.query(sApprovalConfigQuery, new ApprovalConfigAutoapproval());

						for (ApprovalConfigAutoapproval objApprovalConfigAutoapproval : approvalConfigAutoApprovals) {
							listPreregNo.add(String.valueOf(objApprovalConfigAutoapproval.getNpreregno()));
						}

						returnMap.put("approvalconfigautoapproval", approvalConfigAutoApprovals);

						final String subSampleQueryString = "select sarno.npreregno, count(sarno.ntransactionsamplecode) ntransactionsamplecode, "
								+ " rarno.sarno from registrationsamplearno sarno, registrationarno rarno,registrationsamplehistory rsh "
								+ " where rarno.npreregno in ( " + preRegNoString + ") "
								+ "  and rarno.npreregno = sarno.npreregno "
								+ " and rsh.ntransactionsamplecode = sarno.ntransactionsamplecode "
								+ " and rsh.ntransactionstatus != "
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
								+ " and rsh.nsamplehistorycode =any (select max(nsamplehistorycode) from registrationsamplehistory "
								+ " where nsitecode=" + userInfo.getNtranssitecode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and npreregno in("
								+ preRegNoString + ")  group by ntransactionsamplecode) "
								+ " and rarno.nsitecode = sarno.nsitecode and sarno.nsitecode = "
								+ userInfo.getNtranssitecode()
								+ " and rarno.nstatus = sarno.nstatus and sarno.nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " group by sarno.npreregno, rarno.sarno ";

						List<RegistrationSample> sampleCountList = (List<RegistrationSample>) jdbcTemplate.query(subSampleQueryString, new RegistrationSample());
						
						returnMap.put("SubSampleCountList", sampleCountList);

						if (!listPreregNo.isEmpty() && !needJobAllocation) {
							strSeqnoUpdate = strSeqnoUpdate + "update seqnoregistration set nsequenceno = "+ ((int) returnMap.get("joballocation")
											+ (sampleWithoutPreregQuarantineStatus.size() * testCount))+ " where stablename = 'joballocation';";
						}

						strSeqnoUpdate = strSeqnoUpdate + "update seqnoregistration set nsequenceno = "
								+ (int) returnMap.get("registrationparameter") + (sampleWithoutPreregQuarantineStatus.size() * paramterCount)
								+ " where stablename = 'resultparametercomments';";

						if (!uncancelledUnRejectedList.isEmpty()) {
							Set<String> listSectionCode = TestGroupTests.stream()
									.map(obj -> String.valueOf(obj.getNsectioncode())).collect(Collectors.toSet());

							final String ssectionCountQuery = " select npreregno, case when count(nsectioncode) = "
									+ listSectionCode.size() + " then 0 else " + listSectionCode.size()
									+ " - count(nsectioncode) end nsectioncode "
									+ " from registrationsection rs where npreregno in (" + npreregno + ")"
									+ " and nsectioncode in(" + String.join(",", listSectionCode) + ")"
									+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus= "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " group by npreregno";

							final List<RegistrationSection> preregSectionList = jdbcTemplate.query(ssectionCountQuery,new RegistrationSection());

							// Sum of new sections count.
							final int sectionPreregCount = preregSectionList.stream().mapToInt(regSection -> regSection.getNsectioncode()).sum();

							// Sample count for which the new sections are not available
							long unavailPreregSectionCount = listPreregNo.stream().filter(item -> preregSectionList.stream()
											.noneMatch(item1 -> item1.getNpreregno() == Integer.parseInt(item))).count();

							// Sum of both the above counts to be updated in sequence table for
							int preregCountToAdd = sectionPreregCount + (int) unavailPreregSectionCount;
							if (preregCountToAdd > 0) {
								strSeqnoUpdate = strSeqnoUpdate + "update seqnoregistration set nsequenceno = "
										+ (preregCountToAdd + (int) returnMap.get("registrationsection"))
										+ " where stablename = 'registrationsection';"
										+ "update seqnoregistration set nsequenceno = "
										+ (preregCountToAdd + (int) returnMap.get("registrationsectionhistory"))
										+ " where stablename = 'registrationsectionhistory';";
							}
						}
					}
				}
				
				jdbcTemplate.execute(strSeqnoUpdate);
				
				returnMap.put("ordertypecode", orderType);
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			}
		} else {
			final String expiredMethod =stringUtilityFunction.fnDynamicListToString(expiredMethodTestList, "getSmethodname");
			returnMap.put("NeedConfirmAlert", true);
			final String message = commonFunction.getMultilingualMessage("IDS_TESTWITHEXPIREDMETHODCONFIRM",
					userInfo.getSlanguagefilename());
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), message.concat(" " + expiredMethod));
		}
		return returnMap;
	}
	
	@SuppressWarnings({ "unchecked" })
	public Map<String, Object> createSubSamples(Map<String, Object> inputMap) throws Exception {

		Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
        List<Integer> ntransactionSampleCode= new ArrayList();
        Map<String, Object> sampleMapping = new HashMap<String, Object>();
		ObjectMapper objmap = new ObjectMapper();
		objmap.registerModule(new JavaTimeModule());
		int nsampletypecode = (int) inputMap.get("nsampletypecode");

		final UserInfo userInfo = objmap.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		String npreregno = (String) inputMap.get("npreregno");
		final String squery = "select npreregno, ntransactionstatus from registrationhistory where"
				+ " nreghistorycode in (select max(nreghistorycode) from registrationhistory r"
				+ " where r.npreregno in (" + npreregno + ") and nsitecode=" + userInfo.getNtranssitecode()
				+ " group by r.npreregno)";
		List<RegistrationHistory> lstStatus = jdbcTemplate.query(squery, new RegistrationHistory());

		final List<RegistrationHistory> uncancelledUnRejectedList = lstStatus.stream()
				.filter(x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
						&& x.getNtransactionstatus() != Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
						&& x.getNtransactionstatus() != Enumeration.TransactionStatus.RELEASED.gettransactionstatus())
				.collect(Collectors.toList());

		Map<String, Object> returnMap = new HashMap<String, Object>();
		if (uncancelledUnRejectedList.isEmpty()) {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					"IDS_SAMPLECANCELLEDREJECTEDRELEASED");
			return returnMap;
		} else {
			npreregno =stringUtilityFunction.fnDynamicListToString(uncancelledUnRejectedList, "getNpreregno");

			List<String> lstPreregno1 = new ArrayList<String>(Arrays.asList(npreregno.split(",")));
			List<Integer> lstPreregno = lstPreregno1.stream().map(x -> Integer.parseInt(x))
					.collect(Collectors.toList());
			 String samplecode = ((List<String>) inputMap.get("RegistrationSample")).stream().map(x -> String.valueOf(x)).collect(Collectors.joining(","));
			String query="select * from registrationsample where ntransactionsamplecode in ("+samplecode+") and nstatus=1";
			List<RegistrationSample> registrationSample=jdbcTemplate.query(query, new RegistrationSample());
		

			List<TestGroupTest> testGroupTestList = objmap.convertValue(inputMap.get("testgrouptest"),new TypeReference<List<TestGroupTest>>() {});

			List<String> SubSampledateList = objmap.convertValue(inputMap.get("subsampleDateList"),	new TypeReference<List<String>>() {});
			
			 List<RegistrationSample> subSampleDetail1 = objmap.convertValue(inputMap.get("SubSampleCountList"),new TypeReference<List<RegistrationSample>>() {});

			int nregsamplecode = (int) inputMap.get("registrationsample");
			int nregsamplehistorycode = (int) inputMap.get("registrationsamplehistory");
			int seqordersample = -1;


			inputMap.put("nsitecode", userInfo.getNtranssitecode());

			boolean isSampleNotPreRegStatus = uncancelledUnRejectedList.stream().anyMatch(
					x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());

			if (isSampleNotPreRegStatus) {
				int nreghistorycode = (int) inputMap.get("registrationhistory");
				/**
				 * To insert main sample status
				 */
				final List<RegistrationHistory> sampleHistoryUpdateList = uncancelledUnRejectedList.stream()
						.filter(x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
								.gettransactionstatus()
								&& x.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
										.gettransactionstatus()
								&& x.getNtransactionstatus() != Enumeration.TransactionStatus.REGISTERED
										.gettransactionstatus()
								&& x.getNtransactionstatus() != Enumeration.TransactionStatus.PARTIALLY
										.gettransactionstatus())
						.collect(Collectors.toList());
				List<String> registrationHistoryList = new ArrayList<String>();

				for (RegistrationHistory history : sampleHistoryUpdateList) {
					nreghistorycode++;
					registrationHistoryList.add("(" + nreghistorycode + "," + history.getNpreregno() + ","
							+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus() + ", '"
							+dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + ","
							+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + ","
							+ userInfo.getNdeputyuserrole() + ",'" +stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
							+ userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
							+ ")");

				}

				if (registrationHistoryList.size() > 0) {
					String strRegistrationHistory = " insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,"
							+ " dtransactiondate,nusercode,nuserrolecode "
							+ " ,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,"
							+ " ntransdatetimezonecode) values";
					jdbcTemplate.execute(strRegistrationHistory + String.join(",", registrationHistoryList));

				}
			}

			String strRegistrationSample = "";
			String strRegistrationSampleHistory = "";
			String strRegistrationSampleArno = "";
			String externalOrderSampleQuery = "";
			String externalOrderTestQuery = "";

			String ntransactionsamplecode = "";
			int regsampleCount = testGroupTestList.size();
			int sampleCount = lstPreregno.size();


			for (int i = 0; i < registrationSample.size(); i++) {
			if (inputMap.containsKey("specBasedComponent")) {
				if ((boolean) inputMap.get("specBasedComponent") == false) {
					 String querys12 = "select nspecsampletypecode from testgroupspecsampletype where nallottedspeccode="
							+ registrationSample.get(i).getNallottedspeccode();

					Integer nspecsampletypecode = jdbcTemplate.queryForObject(querys12, Integer.class);

					registrationSample.get(i).setNspecsampletypecode(nspecsampletypecode);
				}
			}
			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
				if (registrationSample.get(i).getJsondata().containsKey("sampleorderid")) {
					int orderType = (int) inputMap.get("ordertypecode");
					if (orderType == 1 || orderType == -1) {
						seqordersample = (int) inputMap.get("externalordersample");
					}
				}
			}
			}
			/**
			 * Start of sample iteration
			 */
			for (int i = 0; i < registrationSample.size(); i++) {
				nregsamplecode++;
				nregsamplehistorycode++;
				seqordersample++;
				
				String detailsQuery="Select r.* from registrationsample rs,registration r where" // r.npreregno="+preregno+" and "
				+ " rs.npreregno=r.npreregno  and  rs.ntransactionsamplecode in ("+registrationSample.get(i).getNtransactionsamplecode()+") and rs.nstatus=1 and r.nstatus=1 ";
				List<Registration> registration=jdbcTemplate.query(detailsQuery, new Registration());
				
				int preregno=registration.get(0).getNpreregno();					

				JSONObject jsoneditObj = new JSONObject(registrationSample.get(i).getJsondata());
				JSONObject jsonuiObj = new JSONObject(registrationSample.get(i).getJsonuidata());

				jsonuiObj.put("ntransactionsamplecode", nregsamplecode);
				jsonuiObj.put("npreregno", preregno);

				strRegistrationSample = strRegistrationSample + " (" + nregsamplecode + "," + preregno + ",'"
						+ registrationSample.get(i).getNspecsampletypecode() + "'," + registrationSample.get(i).getNcomponentcode()
						+ ",'" +stringUtilityFunction.replaceQuote(jsoneditObj.toString()) + "'::JSONB,'"
						+ stringUtilityFunction.replaceQuote(jsonuiObj.toString()) + "'::JSONB," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

				if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
					if (registrationSample.get(i).getJsondata().containsKey("sampleorderid")
							&& registration.get(0).getJsondata().containsKey("OrderIdData")) {
						int orderType = (int) inputMap.get("ordertypecode");

						if (orderType == 1 || orderType == -1) {

							int externalordercode = Integer.parseInt(registration.get(0).getJsondata().get("OrderIdData").toString());
							int nsampleAppearanceCode = -1;
							if(jsonuiObj.has("nsampleappearancecode")) {
								nsampleAppearanceCode = (int) jsonuiObj.get("nsampleappearancecode");
							}
							externalOrderSampleQuery += " (" + seqordersample + "," + externalordercode + ","
									+ registrationSample.get(i).getNcomponentcode() + "," + 0 + "," + -1 + ",'"
									+ stringUtilityFunction.replaceQuote(jsonuiObj.get("sampleorderid").toString()) + "','"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "" + userInfo.getNtranssitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ userInfo.getNtranssitecode() + "," + nregsamplecode + ", '"+stringUtilityFunction.replaceQuote(jsonuiObj.get("subsamplecollectiondatetime").toString())
									+"', "+ nsampleAppearanceCode+"),";
						}

					}
				}

				final int mainSampleNo = preregno;
				int mainSampleStatus = uncancelledUnRejectedList.stream()
						.filter(item -> item.getNpreregno() == mainSampleNo).collect(Collectors.toList()).get(0)
						.getNtransactionstatus();

				int subSampleStatus = mainSampleStatus;
				// ALPD-1127
				if (mainSampleStatus == Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()) {
					subSampleStatus = Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus();
				}
				// ALPD-1127
				if (mainSampleStatus != Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
						&& mainSampleStatus != Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()
						&& mainSampleStatus != Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()) {
					mainSampleStatus = Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus();
					subSampleStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
				}
				strRegistrationSampleHistory = strRegistrationSampleHistory + "(" + nregsamplehistorycode + ","
						+ nregsamplecode + "," + preregno + "," + subSampleStatus + ",'"
						+dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
						+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
						+stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

				String subSampleARNO = "-";
				if (mainSampleStatus != Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
						&& mainSampleStatus != Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()) {
					 RegistrationSample subSampleDetail = new RegistrationSample();
					 
					 for (int j = 0; j < subSampleDetail1.size(); j++) {
					
						if(subSampleDetail1.get(j).getNpreregno()==mainSampleNo) {
							subSampleDetail=subSampleDetail1.get(j);
							subSampleDetail.setNtransactionsamplecode(subSampleDetail.getNtransactionsamplecode() + 1);
							subSampleDetail1.get(j).setNtransactionsamplecode(subSampleDetail.getNtransactionsamplecode());	
						}
					 }
					

					String formatted = String.format("%02d", subSampleDetail.getNtransactionsamplecode()  );
					subSampleARNO = "" + subSampleDetail.getSarno() + "-" + formatted;

				}
				strRegistrationSampleArno = strRegistrationSampleArno + "(" + nregsamplecode + "," + preregno
						+ ",'" + subSampleARNO + "'," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				// commented from below
//					final String stestcode = testGroupTestList.stream().map(x -> String.valueOf(x.getNtestgrouptestcode()))
//												.distinct().collect(Collectors.joining(","));					
//				
//					final int preregno= lstPreregno.get(i);		
//				
//					sQuery = parameterQuery(inputMap, preregno, userInfo);		
//					
//					/***
//					 * registrationsection and history insert
//					 */					
//					if (mainSampleStatus != Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
//							&& mainSampleStatus != Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()) 
//					{
//						final Map<String, Object> sectionSeqMap =  insertRegistrationSection(userInfo, preregno, stestcode, 
//								testGroupTestList, nregSectionCode, nSectionHistoryCode);
//						
//						nregSectionCode = (int) sectionSeqMap.get("registrationsection");
//						nSectionHistoryCode = (int) sectionSeqMap.get("registrationsectionhistory");
//					}
//		
//					if (!testGroupTestList.isEmpty()) {
//						
//						//FRS-00410- To add test based on replicate count defined in testgroup test
//						
//						final Map<String, Object> testHistoryParameterMap = insertTestHistoryParameter(stestcode, sQuery, 
//																				userInfo, lstPreregno.get(i), nregsamplecode, ntransactiontestcode, 
//																				ntesthistorycode, nregistrationparametercode, transactionCodeList);
//						ntransactiontestcode = (int)testHistoryParameterMap.get("ntransactiontestcode");
//						ntesthistorycode = (int)testHistoryParameterMap.get("ntesthistorycode");
//						nregistrationparametercode = (int)testHistoryParameterMap.get("ntransactionresultcode");
//						transactionCodeList = (ArrayList<Integer>) testHistoryParameterMap.get("transactionCodeList");
//						// End- FRS-00410		
//					}
				/// commented till above
				StringJoiner joinerSampleMain = new StringJoiner(",");
				joinerSampleMain.add(String.valueOf(nregsamplecode));

				ntransactionsamplecode = ntransactionsamplecode + "," + String.valueOf(nregsamplecode);
				ntransactionSampleCode.add(nregsamplecode);
				sampleMapping.put(String.valueOf(nregsamplecode),
						testGroupTestList.stream().map(lst -> String.valueOf(lst.getNtestgrouptestcode())).collect(Collectors.joining(",")));
			}
			// end of sample iteration

			strRegistrationSample = "insert into registrationsample(ntransactionsamplecode,npreregno,nspecsampletypecode,ncomponentcode,jsondata,jsonuidata, "
					+ "nsitecode,nstatus) values "
					+ strRegistrationSample.substring(0, strRegistrationSample.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationSample);

			strRegistrationSampleHistory = "Insert into registrationsamplehistory(nsamplehistorycode, ntransactionsamplecode, npreregno, ntransactionstatus, "
					+ "dtransactiondate, nusercode, nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments, nsitecode,nstatus ) values "
					+ strRegistrationSampleHistory.substring(0, strRegistrationSampleHistory.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationSampleHistory);

			strRegistrationSampleArno = "Insert into registrationsamplearno (ntransactionsamplecode,npreregno,ssamplearno,nsitecode,nstatus) values "
					+ strRegistrationSampleArno.substring(0, strRegistrationSampleArno.length() - 1) + ";";
			jdbcTemplate.execute(strRegistrationSampleArno);

			if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
				//for (int i = 0; i < registrationSample.size(); i++) {
				if (registrationSample.get(0).getJsondata().containsKey("sampleorderid")) {
					int orderType = (int) inputMap.get("ordertypecode");

					if (orderType == 1 || orderType == -1 && externalOrderSampleQuery!="") {
						externalOrderSampleQuery = "INSERT INTO externalordersample (nexternalordersamplecode,nexternalordercode,ncomponentcode,nsampleqty,nunitcode,sexternalsampleid,dmodifieddate,nsitecode,nstatus, nparentsitecode,ntransactionsamplecode, dsamplecollectiondatetime, nsampleappearancecode) values "
						+ externalOrderSampleQuery.substring(0, externalOrderSampleQuery.length() - 1) + ";";
						jdbcTemplate.execute(externalOrderSampleQuery);
//						externalOrderTestQuery = "INSERT INTO externalordertest (nexternalordertestcode,nexternalordersamplecode,nexternalordercode,ntestpackagecode,ncontainertypecode,ntestcode,dmodifieddate,nsitecode,nstatus, nparentsitecode) values "
//								+ externalOrderTestQuery.substring(0, externalOrderTestQuery.length() - 1) + ";";
//
//						getJdbcTemplate().execute(externalOrderTestQuery);
					}

				}
			//}
			}

			// Add Test code start

			final int nregTypeCode = (int) inputMap.get("nregtypecode");
			final int nregSubTypeCode = (int) inputMap.get("nregsubtypecode");

			final String sFindSubSampleQuery = "select npreregno, ntransactionstatus, ntransactionsamplecode from registrationsamplehistory"
					+ " where nsamplehistorycode = any (select max(nsamplehistorycode) from registrationsamplehistory where ntransactionsamplecode"
					+ " in (" + ntransactionsamplecode.substring(1) + ")  and nsitecode=" + userInfo.getNtranssitecode()
					+ " group by ntransactionsamplecode) and ntransactionstatus not in ("
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ") and nsitecode="
					+ userInfo.getNtranssitecode() + ";";

			List<RegistrationSampleHistory> listAvailableSample = jdbcTemplate.query(sFindSubSampleQuery,
					new RegistrationSampleHistory());

			final String sFindSubSampleQuerys = "select * from registrationsample"
					+ " where ntransactionsamplecode "
					+ " in (" + ntransactionsamplecode.substring(1) + ") "
					+  "  and nsitecode="
					+ userInfo.getNtranssitecode() + ";";
			List<RegistrationSample> listAvailableSamples = jdbcTemplate.query(sFindSubSampleQuerys,
					new RegistrationSample());
			listAvailableSamples.stream().map(x->{x.setIsneedsample(true); return x;}).collect(Collectors.toList());
			listAvailableSample.stream().map(x->{x.setIsneedsample(true); return x;}).collect(Collectors.toList());
			inputMap.put("directAddTest", false);
			returnMap.put("AvailableSampleHistory", listAvailableSample);
			returnMap.put("AvailableSample", listAvailableSamples);
			returnMap.put("ntransactionsamplecode", ntransactionSampleCode);
			ntransactionsamplecode = ntransactionsamplecode.substring(1);
			returnMap.putAll(sampleMapping);
			
			final String query1 = "select * from fn_registrationsubsampleget('" + inputMap.get("npreregno") + "'::text,"
					+ "'" + ntransactionsamplecode + "'::text" + ", 5 ,"
					+ userInfo.getNtranssitecode() + ",'" + userInfo.getSlanguagetypecode() + "','"
					+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "'" + ",'"
					+ commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename()) + "')";
			LOGGER.info("sub sample query:" + query1);
			final String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
			
			List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					lstData1, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "subsample");

			jsonAuditObject.put("registrationsample", (List<Map<String, Object>>) lstData);
			auditmap.put("nregtypecode", inputMap.get("nregtypecode"));
			auditmap.put("nregsubtypecode",inputMap.get("nregsubtypecode"));
			auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
			actionType.put("registrationsample", "IDS_ADDSUBSAMPLE");
		
			userInfo.setNformcode((short) 43);	
			userInfo.setSformname(commonFunction.getMultilingualMessage("IDS_SAMPLEREGISTRATION", userInfo.getSlanguagefilename()));
			userInfo.setSmodulename(commonFunction.getMultilingualMessage("IDS_REGISTRATION", userInfo.getSlanguagefilename()));
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, userInfo);
			return returnMap;
		}

	}
	
	
}
