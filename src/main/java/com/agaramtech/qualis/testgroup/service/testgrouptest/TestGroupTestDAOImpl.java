package com.agaramtech.qualis.testgroup.service.testgrouptest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.transform.Source;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.ApprovalRoleActionDetail;
import com.agaramtech.qualis.configuration.model.TreeVersionTemplate;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TestGroupCommonFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Component;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestCharParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFile;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFormula;
import com.agaramtech.qualis.testgroup.model.TestGroupTestMaterial;
import com.agaramtech.qualis.testgroup.model.TestGroupTestNumericParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testmanagement.model.TestCategory;
import com.agaramtech.qualis.testmanagement.model.TestContainerType;
import com.agaramtech.qualis.testmanagement.model.TestFile;
import com.agaramtech.qualis.testmanagement.model.TestFormula;
import com.agaramtech.qualis.testmanagement.model.TestInstrumentCategory;
import com.agaramtech.qualis.testmanagement.model.TestMasterClinicalSpec;
import com.agaramtech.qualis.testmanagement.model.TestMethod;
import com.agaramtech.qualis.testmanagement.model.TestPackageTest;
import com.agaramtech.qualis.testmanagement.model.TestParameter;
import com.agaramtech.qualis.testmanagement.model.TestParameterNumeric;
import com.agaramtech.qualis.testmanagement.model.TestPredefinedParameter;
import com.agaramtech.qualis.testmanagement.model.TestSection;
import com.agaramtech.qualis.testmanagement.service.testcategory.TestCategoryDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TestGroupTestDAOImpl implements TestGroupTestDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestGroupTestDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private TestGroupCommonFunction testGroupCommonFunction;
	private FTPUtilityFunction ftpUtilityFunction;
	private TestCategoryDAO testCategoryDAO;

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getCreateComponentTestSeqNo(final boolean isTestAction, final UserInfo objUserInfo,
			final TestGroupSpecification objSpecification, final List<TestGroupSpecSampleType> lstTestGroupComponent,
			final List<TestGroupTest> listTest) throws Exception {
		final TestGroupSpecification objTestGroupSpecification = testGroupCommonFunction
				.getActiveSpecification(objSpecification.getNallottedspeccode(), objUserInfo);
		final Map<String, Object> outputMap = new HashMap<>();
		if (objTestGroupSpecification != null) {
			if (objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()
					|| objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
							.gettransactionstatus()) {
				StringBuilder sb = new StringBuilder();
				List<TestGroupTest> filteredList = new ArrayList<TestGroupTest>();
				if (isTestAction) {
					String sTestQry = "select ntestcode from testgrouptest where nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nspecsampletypecode="
							+ lstTestGroupComponent.get(0).getNspecsampletypecode() + " and nsitecode="
							+ objUserInfo.getNmastersitecode();
					List<TestGroupTest> lstAvailTGT = (List<TestGroupTest>) jdbcTemplate.query(sTestQry,
							new TestGroupTest());
					filteredList = listTest.stream()
							.filter(source -> lstAvailTGT.stream()
									.noneMatch(check -> source.getNtestcode() == check.getNtestcode()))
							.collect(Collectors.toList());
				} else {
					filteredList = listTest;
				}

				String stestcode = stringUtilityFunction.fnDynamicListToString(filteredList, "getNtestcode");
				if (stestcode.isEmpty()) {
					stestcode = "0";
				}
				sb.append("select nparametertypecode from testparameter where ntestcode in (" + stestcode
						+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";");
				sb.append("select count(ntestfilecode) as ntestfilecode from testfile where ndefaultstatus= "
						+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode in(" + stestcode
						+ ") and nsitecode=" + objUserInfo.getNmastersitecode() + ";");
				sb.append("select count(ntestformulacode) as ntestformulacode from testformula where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode in ("
						+ stestcode + ") and nsitecode=" + objUserInfo.getNmastersitecode() + ";");
				String StrQuery = "select nparametertypecode from testparameter where ntestcode in (" + stestcode
						+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + objUserInfo.getNmastersitecode() + "";
				LOGGER.info(StrQuery);
				List<TestParameter> lstTestParameter = jdbcTemplate.query(StrQuery, new TestParameter());

				StrQuery = "select ntestfilecode from testfile where ntestcode in (" + stestcode + ") and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ objUserInfo.getNmastersitecode();
				List<TestFile> lstTestFile = jdbcTemplate.query(StrQuery, new TestFile());

				StrQuery = "select ntestformulacode,ndefaultstatus from testformula where ntestcode in (" + stestcode
						+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + objUserInfo.getNmastersitecode() + "";
				List<TestFormula> lstTestFormula = jdbcTemplate.query(StrQuery, new TestFormula());

				StrQuery = "select  tsc.*,tm.ntestcode from testmaster tm ,testparameter tp,testmasterclinicalspec tsc where tm.ntestcode=tp.ntestcode "
						+ " and tp.ntestparametercode=tsc.ntestparametercode  and tm.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tp.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tsc.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tm.ntestcode in ("
						+ stestcode + ") and tp.nsitecode=" + objUserInfo.getNmastersitecode() + " and tsc.nsitecode="
						+ objUserInfo.getNmastersitecode();

				List<TestMasterClinicalSpec> lstClinialSpec = jdbcTemplate.query(StrQuery,
						new TestMasterClinicalSpec());

				final Map<String, Object> mapLists = new HashMap<String, Object>();
				mapLists.put("TestParameter", lstTestParameter);
				mapLists.put("TestFile", lstTestFile);
				mapLists.put("TestFormula", lstTestFormula);
				mapLists.put("TestMasterClinicalSpec", lstClinialSpec);
				final List<TestParameter> lstTP = (List<TestParameter>) mapLists
						.get(TestParameter.class.getSimpleName());
				final List<TestFile> lstTF = (List<TestFile>) mapLists.get(TestFile.class.getSimpleName());
				List<TestFormula> lstTForm = (List<TestFormula>) mapLists.get(TestFormula.class.getSimpleName());
				List<TestMasterClinicalSpec> lstParametrClinialSpec = (List<TestMasterClinicalSpec>) mapLists
						.get(TestMasterClinicalSpec.class.getSimpleName());
				outputMap.put("nclinicalspeccount",
						(lstParametrClinialSpec != null && !lstParametrClinialSpec.isEmpty())
								? lstParametrClinialSpec.size()
								: 0);
				sb.delete(0, sb.length());
				sb.append("select count(tpp.ntestpredefinedcode) "
						+ "from testpredefinedparameter tpp,testparameter tp where tp.ntestparametercode = tpp.ntestparametercode "
						+ "and tpp.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tp.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tp.nparametertypecode=" + Enumeration.ParameterType.PREDEFINED.getparametertype()
						+ " and tp.ntestcode in(" + stestcode + ") and tpp.nsitecode="
						+ objUserInfo.getNmastersitecode() + " and tp.nsitecode=" + objUserInfo.getNmastersitecode()
						+ ";");

				outputMap.put("ncomponentcount",
						(lstTestGroupComponent != null && !lstTestGroupComponent.isEmpty()) ? lstTP.size() : 0);
				outputMap.put("ntestcount",
						(filteredList != null && !filteredList.isEmpty()) ? filteredList.size() : 0);
				outputMap.put("nparametercount", (lstTP != null && !lstTP.isEmpty()) ? lstTP.size() : 0);
				outputMap.put("npredefparamcount", jdbcTemplate.queryForObject(sb.toString(), Integer.class));
				final int ncharparamcount = (int) lstTP.stream().filter(objTP -> objTP
						.getNparametertypecode() == Enumeration.ParameterType.CHARACTER.getparametertype()).count();
				final int nnumericcount = (int) lstTP.stream().filter(
						objTP -> objTP.getNparametertypecode() == Enumeration.ParameterType.NUMERIC.getparametertype())
						.count();
				outputMap.put("ncharparamcount", ncharparamcount);
				outputMap.put("nnumericcount", nnumericcount);
				outputMap.put("ntestfilecount",
						(lstTF != null && !lstTF.isEmpty()) ? lstTF.get(0).getNtestfilecode() : 0);
				// modified
				lstTForm = lstTForm.stream()
						.filter(x -> x.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
						.collect(Collectors.toList());
				outputMap.put("ntestFormcount", (lstTForm != null && !lstTForm.isEmpty()) ? lstTForm.size() : 0);
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			} else {
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()));
			}
		} else {
			outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction
					.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()));
		}
		return outputMap;
	}

	@Override
	public Map<String, Object> updateCreateComponentTestSeqNo(final boolean isTestAction,
			final Map<String, Object> objMap) throws Exception {

		final String Query = " lock  table locktestgrouptest " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(Query);

		Map<String, Object> returnMap = new HashMap<>();
		final String sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename in (N'testgroupspecsampletype',N'testgrouptest',"
				+ "N'testgrouptestcharparameter',N'testgrouptestfile',N'testgrouptestformula',N'testgrouptestnumericparameter',N'testgrouptestparameter',"
				+ "N'testgrouptestpredefparameter',N'testgrouptestclinicalspec') and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by stablename";
		final List<Integer> lstCount = jdbcTemplate.queryForList(sQuery, Integer.class);
		final StringBuilder sb = new StringBuilder();
		if (!isTestAction) {
			sb.append("update seqnotestgroupmanagement set nsequenceno = "
					+ (lstCount.get(0) + (int) objMap.get("ncomponentcount"))
					+ "  where stablename=N'testgroupspecsampletype' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		}
		sb.append("update seqnotestgroupmanagement set nsequenceno="
				+ (lstCount.get(1) + (int) objMap.get("ntestcount")) + "  where stablename=N'testgrouptest' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		sb.append("update seqnotestgroupmanagement set nsequenceno="
				+ (lstCount.get(2) + (int) objMap.get("ncharparamcount"))
				+ "  where stablename=N'testgrouptestcharparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		sb.append("update seqnotestgroupmanagement set nsequenceno="
				+ (lstCount.get(3) + (int) objMap.get("nclinicalspeccount"))
				+ "  where stablename=N'testgrouptestclinicalspec' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		sb.append("update seqnotestgroupmanagement set nsequenceno="
				+ (lstCount.get(4) + (int) objMap.get("ntestcount")) + "  where stablename=N'testgrouptestfile' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		sb.append("update seqnotestgroupmanagement set nsequenceno="
				+ (lstCount.get(5) + (int) objMap.get("ntestFormcount"))
				+ "  where stablename=N'testgrouptestformula' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		sb.append("update seqnotestgroupmanagement set nsequenceno="
				+ (lstCount.get(6) + (int) objMap.get("nnumericcount"))
				+ "  where stablename=N'testgrouptestnumericparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		sb.append("update seqnotestgroupmanagement set nsequenceno="
				+ (lstCount.get(7) + (int) objMap.get("nparametercount"))
				+ "  where stablename=N'testgrouptestparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
		sb.append("update seqnotestgroupmanagement set nsequenceno="
				+ (lstCount.get(8) + (int) objMap.get("npredefparamcount"))
				+ "  where stablename=N'testgrouptestpredefparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");

		jdbcTemplate.execute(sb.toString());
		returnMap.put("ntgtcomponentcount", lstCount.get(0));
		returnMap.put("ntgtcount", lstCount.get(1));
		returnMap.put("ntgtcpcount", lstCount.get(2));
		returnMap.put("ntgtcscount", lstCount.get(3));
		returnMap.put("ntgttfcount", lstCount.get(4));
		returnMap.put("ntgttformcount", lstCount.get(5));
		returnMap.put("ntgtnpcount", lstCount.get(6));
		returnMap.put("ntgttpcount", lstCount.get(7));
		returnMap.put("ntgtppcount", lstCount.get(8));
		return returnMap;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ResponseEntity<Object> createComponent(Map<String, Object> inputMap, UserInfo objUserInfo,
			TestGroupSpecification objTestGroupSpec, List<TestGroupSpecSampleType> listTestGroupComponent,
			final List<TestGroupTest> listTestGroupTest, final Map<String, Object> objMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		String sAuditQuery = "";
		final String sComponentCode = stringUtilityFunction.fnDynamicListToString(listTestGroupComponent,
				"getNcomponentcode");
		String sComponentQry = "select ncomponentcode, scomponentname, sdescription, ndefaultstatus, nsitecode, nstatus from component where ncomponentcode in ("
				+ sComponentCode + ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ";";
		final Component objComponent = (Component) jdbcUtilityFunction.queryForObject(sComponentQry, Component.class,jdbcTemplate);
		if (objComponent != null) {
			sComponentQry = "select nspecsampletypecode, nallottedspeccode, ncomponentcode, nsitecode, nstatus from testgroupspecsampletype where ncomponentcode = "
					+ listTestGroupComponent.get(0).getNcomponentcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nallottedspeccode = "
					+ objTestGroupSpec.getNallottedspeccode() + " and nsitecode=" + objUserInfo.getNmastersitecode()
					+ ";";
			final TestGroupSpecSampleType objTGSST = (TestGroupSpecSampleType) jdbcUtilityFunction
					.queryForObject(sComponentQry, TestGroupSpecSampleType.class, jdbcTemplate);
			if (objTGSST == null) {
				final int ntgtcomponentcount = (int) objMap.get("ntgtcomponentcount") + 1;
				String sInsertQuery = "insert into testgroupspecsampletype (nspecsampletypecode, nallottedspeccode, ncomponentcode,dmodifieddate, nstatus,nsitecode)"
						+ " select " + ntgtcomponentcount + ", " + objTestGroupSpec.getNallottedspeccode()
						+ ", ncomponentcode,'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ objUserInfo.getNmastersitecode() + " from component where ncomponentcode in ("
						+ sComponentCode + ");";
				listTestGroupComponent.get(0).setNspecsampletypecode(ntgtcomponentcount);
				final Map<String, Object> tempMap = insertTest(objUserInfo, objTestGroupSpec, listTestGroupComponent,
						listTestGroupTest, objMap);
				String insertQueryString = "";
				insertQueryString += sInsertQuery;
				List<String> lstInsertQry = (List<String>) tempMap.get("insertqry");
				insertQueryString += lstInsertQry.stream().map(str -> str).collect(Collectors.joining(" "));
				jdbcTemplate.execute(insertQueryString);

				List<String> listString = (List<String>) tempMap.get("stestgrouptestcode");
				String stestgrouptestcode = String.join(",", listString);
				sAuditQuery = "select nspecsampletypecode, nallottedspeccode, ncomponentcode, nsitecode, nstatus from testgroupspecsampletype where nspecsampletypecode = "
						+ ntgtcomponentcount + " ";
				List<TestGroupSpecSampleType> lstTestGroupSpecSampleType = jdbcTemplate.query(sAuditQuery,
						new TestGroupSpecSampleType());

				sAuditQuery = "select ntestgrouptestcode, nspecsampletypecode, ntestcode, nsectioncode, nmethodcode, ninstrumentcatcode, ncontainertypecode, stestsynonym, nrepeatcountno, ncost, nsorter, nisadhoctest, nisvisible, nsitecode, nstatus from testgrouptest where ntestgrouptestcode in ("
						+ stestgrouptestcode + ")";
				List<TestGroupTest> lstTestGroupTest = jdbcTemplate.query(sAuditQuery, new TestGroupTest());

				sAuditQuery = "select tgtp.ntestgrouptestparametercode, tgtp.ntestgrouptestcode, tgtp.ntestparametercode, tgtp.nparametertypecode, tgtp.nunitcode, tgtp.sparametersynonym, tgtp.nroundingdigits, tgtp.nresultmandatory, tgtp.nreportmandatory, tgtp.ngradingmandatory, tgtp.nchecklistversioncode, tgtp.sspecdesc, tgtp.nsorter, tgtp.nisadhocparameter, tgtp.nisvisible, tgtp.nsitecode, tgtp.nstatus, pt.sdisplaystatus,tgtp.nresultaccuracycode from testgrouptestparameter tgtp, parametertype pt where ntestgrouptestcode in ("
						+ stestgrouptestcode + ") and tgtp.nparametertypecode=pt.nparametertypecode and tgtp.nsitecode="
						+ objUserInfo.getNmastersitecode() + " and pt.nsitecode=" + objUserInfo.getNmastersitecode();
				List<TestGroupTestParameter> lstTestGroupTestParameter = objMapper.convertValue(
						commonFunction.getMultilingualMessageList(
								jdbcTemplate.query(sAuditQuery, new TestGroupTestParameter()),
								Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()),
						new TypeReference<List<TestGroupTestParameter>>() {
						});

				sAuditQuery = "select ntestgrouptestformulacode, ntestgrouptestcode, ntestgrouptestparametercode, ntestformulacode, sformulacalculationcode, sformulacalculationdetail, ntransactionstatus, nsitecode, nstatus from testgrouptestformula where ntestgrouptestcode in ("
						+ stestgrouptestcode + ") and nsitecode=" + objUserInfo.getNmastersitecode();
				List<TestGroupTestFormula> lstTestGroupTestFormula = jdbcTemplate.query(sAuditQuery,
						new TestGroupTestFormula());

				sAuditQuery = "select * from testgrouptestfile where ntestgrouptestcode in (" + stestgrouptestcode
						+ ") and nsitecode=" + objUserInfo.getNmastersitecode();
				List<TestGroupTestFile> lstTestGroupTestFile = jdbcTemplate.query(sAuditQuery, new TestGroupTestFile());

				sAuditQuery = "select tgtpp.ntestgrouptestpredefcode, tgtpp.ntestgrouptestparametercode, tgtpp.ngradecode, tgtpp.spredefinedname, tgtpp.spredefinedsynonym, tgtpp.spredefinedcomments, tgtpp.salertmessage, tgtpp.nneedresultentryalert, tgtpp.nneedsubcodedresult, tgtpp.ndefaultstatus, tgtpp.nsitecode, tgtpp.nstatus, "
						+ "tgtp.ntestgrouptestparametercode, tgtp.ntestgrouptestcode, tgtp.ntestparametercode, tgtp.nparametertypecode, tgtp.nunitcode, tgtp.sparametersynonym, tgtp.nroundingdigits, tgtp.nresultmandatory, tgtp.nreportmandatory, tgtp.ngradingmandatory, tgtp.nchecklistversioncode, tgtp.sspecdesc, tgtp.nsorter, tgtp.nisadhocparameter, tgtp.nisvisible, tgtp.nsitecode, tgtp.nstatus,tgtp.nresultaccuracycode from testgrouptestpredefparameter tgtpp,testgrouptestparameter tgtp where tgtp.ntestgrouptestparametercode = tgtpp.ntestgrouptestparametercode "
						+ "and tgtp.ntestgrouptestcode in (" + stestgrouptestcode + ") and tgtpp.nsitecode="
						+ objUserInfo.getNmastersitecode() + " and tgtp.nsitecode=" + objUserInfo.getNmastersitecode();
				List<TestGroupTestPredefinedParameter> lstTestGroupTestPredefinedParameter = jdbcTemplate
						.query(sAuditQuery, new TestGroupTestPredefinedParameter());

				List<Object> lstnewobject = new ArrayList<Object>();
				lstnewobject.add(lstTestGroupSpecSampleType);
				lstnewobject.add(lstTestGroupTest);
				lstnewobject.add(lstTestGroupTestParameter);
				lstnewobject.add(lstTestGroupTestFormula);
				lstnewobject.add(lstTestGroupTestFile);
				lstnewobject.add(lstTestGroupTestPredefinedParameter);

				List<String> lstAction = new ArrayList<String>();

				Map obj = (Map) ((Map) ((Map) ((Map) inputMap.get("genericlabel")).get("AddComponent")).get("jsondata"))
						.get("sdisplayname");
				String addcomponentlabel = (String) obj.get(objUserInfo.getSlanguagetypecode());

				lstAction.add(addcomponentlabel);
				lstAction.add("IDS_ADDTEST");
				lstAction.add("IDS_ADDTESTPARAMETER");
				lstAction.add("IDS_ADDTESTFORMULA");
				lstAction.add("IDS_ADDTESTFILE");
				lstAction.add("IDS_ADDPREDEFINEDPARAMETER");
				lstAction.add("IDS_ADDSPECLIMIT");
				auditUtilityFunction.fnInsertListAuditAction(lstnewobject, 1, null, lstAction, objUserInfo);
				return new ResponseEntity<Object>(
						testGroupCommonFunction.getComponentDetails(objTestGroupSpec, objUserInfo), HttpStatus.OK);
			} else {
				return new ResponseEntity<Object>(commonFunction
						.getMultilingualMessage("IDS_SPECALREADYHASTHISCOMPONENT", objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			Map obj1 = (Map) ((Map) ((Map) ((Map) inputMap.get("genericlabel")).get("AlreadyComponentDeleted"))
					.get("jsondata")).get("sdisplayname");
			String msglabel = (String) obj1.get(objUserInfo.getSlanguagetypecode());
			return new ResponseEntity<Object>(msglabel, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public ResponseEntity<Object> deleteTestGroupComponent(Map<String, Object> inputMap, UserInfo objUserInfo,
			TestGroupSpecification objTestGroupSpec, List<TestGroupSpecSampleType> listTestGroupComponent)
			throws Exception {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		List<Object> mapLists = new ArrayList<>();
		final String snspecsampletypecode = stringUtilityFunction.fnDynamicListToString(listTestGroupComponent,
				"getNspecsampletypecode");
		final String componentValidation = "select count(nspecsampletypecode) from testgroupspecsampletype where nspecsampletypecode in ("
				+ snspecsampletypecode + ") " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		int count = jdbcTemplate.queryForObject(componentValidation, Integer.class);

		if (count > 0) {
			final TestGroupSpecification testGroupSpecification = testGroupCommonFunction
					.getActiveSpecification(objTestGroupSpec.getNallottedspeccode(), objUserInfo);

			if (testGroupSpecification != null) {

				if (testGroupSpecification.getNprojectmastercode() != -1) {
					map = (Map<String, Object>) testGroupCommonFunction
							.getProjectMasterStatusCode(testGroupSpecification.getNprojectmastercode(), objUserInfo);
					strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				} else {

					String sQuery = "";
					if (testGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT
							.gettransactionstatus()
							|| testGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
									.gettransactionstatus()) {

						final String sTestQry = "select * from testgrouptest where nspecsampletypecode in ("
								+ snspecsampletypecode + ") and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
								+ objUserInfo.getNmastersitecode() + ";";
						List<TestGroupTest> listTest = (List<TestGroupTest>) jdbcTemplate.query(sTestQry,
								new TestGroupTest());
						final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest,
								"getNtestgrouptestcode");
						sQuery = "select * from testgroupspecsampletype where nspecsampletypecode in ("
								+ snspecsampletypecode + ") and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
						List<TestGroupSpecSampleType> lstTestGroupSpecSampleType = jdbcTemplate.query(sQuery,
								new TestGroupSpecSampleType());
						mapLists.add(lstTestGroupSpecSampleType);
						String sDeleteQuery = "update testgroupspecsampletype set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where nspecsampletypecode in (" + snspecsampletypecode + ");";
						List<String> lstActionType = new ArrayList<String>();
						if (!sntestgrouptestcode.isEmpty()) {
							sDeleteQuery = sDeleteQuery + "update testgrouptest set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
									+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
									+ " where ntestgrouptestcode in (" + sntestgrouptestcode + ");";
							sDeleteQuery = sDeleteQuery + "update testgrouptestparameter set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
									+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
									+ " where ntestgrouptestcode in ( " + sntestgrouptestcode + ");";

							sDeleteQuery = sDeleteQuery + "update testgrouptestpredefparameter set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
									+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
									+ " where ntestgrouptestparametercode in (select tgtp.ntestgrouptestparametercode from testgrouptest tgt, testgrouptestparameter tgtp"
									+ " where tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode and tgt.ntestgrouptestcode in ( "
									+ sntestgrouptestcode + ")) and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

							sDeleteQuery = sDeleteQuery + "update testgrouptestcharparameter set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
									+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
									+ " where ntestgrouptestparametercode in (select tgtp.ntestgrouptestparametercode from testgrouptest tgt, testgrouptestparameter tgtp "
									+ " where tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode and  tgt.ntestgrouptestcode in ( "
									+ sntestgrouptestcode + "));";

							sDeleteQuery = sDeleteQuery + "update testgrouptestnumericparameter set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
									+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
									+ " where ntestgrouptestparametercode in (select tgtp.ntestgrouptestparametercode from testgrouptest tgt, testgrouptestparameter tgtp "
									+ " where tgt.ntestgrouptestcode = tgtp.ntestgrouptestcode and tgt.ntestgrouptestcode in ( "
									+ sntestgrouptestcode + "));";

							sDeleteQuery = sDeleteQuery + "update testgrouptestformula set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
									+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
									+ " where ntestgrouptestcode in ( " + sntestgrouptestcode + ");";
							sDeleteQuery = sDeleteQuery + "update testgrouptestfile set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
									+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
									+ " where ntestgrouptestcode in ( " + sntestgrouptestcode + ");";
							sDeleteQuery = sDeleteQuery + "update testgrouptestmaterial set nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
									+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
									+ " where ntestgrouptestcode in ( " + sntestgrouptestcode + ");";

							Map obj = (Map) ((Map) ((Map) ((Map) inputMap.get("genericlabel")).get("DeleteComponent"))
									.get("jsondata")).get("sdisplayname");
							String deletecomponentlabel = (String) obj.get(objUserInfo.getSlanguagetypecode());

							lstActionType.addAll(Arrays.asList(deletecomponentlabel, "IDS_DELETETEST",
									"IDS_DELETETESTPARAMETER", "IDS_DELETEPREDEFINEDPARAMETER", "IDS_DELETETESTFORMULA",
									"IDS_DELETETESTFILE", "IDS_DELETETESTMATERIAL"));
						} else {
							lstActionType.addAll(Arrays.asList("IDS_DELETETESTGROUPCOMPONENT"));
						}
						jdbcTemplate.execute(sDeleteQuery);
						auditUtilityFunction.fnInsertListAuditAction(mapLists, 1, null, lstActionType, objUserInfo);
						return new ResponseEntity<>(
								testGroupCommonFunction.getComponentDetails(objTestGroupSpec, objUserInfo),
								HttpStatus.OK);
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}

				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getAvailableComponent(UserInfo objUserInfo, TestGroupSpecification objTestGroupSpec)
			throws Exception {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

		final TestGroupSpecification objTestGroupSpecification = testGroupCommonFunction
				.getActiveSpecification(objTestGroupSpec.getNallottedspeccode(), objUserInfo);
		if (objTestGroupSpecification != null) {

			if (objTestGroupSpec.getNprojectmastercode() != -1) {
				map = (Map<String, Object>) testGroupCommonFunction
						.getProjectMasterStatusCode(objTestGroupSpec.getNprojectmastercode(), objUserInfo);
				strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
			}
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}

			else {

				if (objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT
						.gettransactionstatus()
						|| objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
								.gettransactionstatus()) {
					String sQuery = "";
					final List<TestCategory> lstTestCategory = (List<TestCategory>) testCategoryDAO
							.getTestCategoryForTestGroup(objUserInfo, objTestGroupSpec.getNclinicaltyperequired())
							.getBody();
					if (objTestGroupSpecification.getNcomponentrequired() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						sQuery = "select c.ncomponentcode, c.scomponentname from component c where c.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and c.ncomponentcode>0"
								+ " and not exists (select 1 from testgroupspecsampletype tgsst"
								+ " where tgsst.nallottedspeccode = " + objTestGroupSpecification.getNallottedspeccode()
								+ " and c.ncomponentcode = tgsst.ncomponentcode" + " and tgsst.nstatus = c.nstatus);";
					}
					final List<TestGroupSpecSampleType> listTestGroupSpecSampleType = (List<TestGroupSpecSampleType>) jdbcTemplate
							.query(sQuery, new TestGroupSpecSampleType());
					if (lstTestCategory != null && !lstTestCategory.isEmpty()
							&& !listTestGroupSpecSampleType.isEmpty()) {
						int ntestcategorycode = 0;
						List<TestCategory> defaultTestCategory = lstTestCategory.stream().filter(
								x -> x.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
								.collect(Collectors.toList());
						if (!defaultTestCategory.isEmpty()) {
							ntestcategorycode = defaultTestCategory.get(0).getNtestcategorycode();
						} else {
							ntestcategorycode = lstTestCategory.get(0).getNtestcategorycode();
						}
						sQuery = getTestMasterQuery(ntestcategorycode,
								listTestGroupSpecSampleType.get(0).getNcomponentcode(),
								objTestGroupSpec.getNallottedspeccode(), objTestGroupSpec.getNclinicaltyperequired());
					}
					final List<TestGroupTest> listTestGroupTest = (List<TestGroupTest>) jdbcTemplate.query(sQuery,
							new TestGroupTest());
					final Map<String, Object> outputMap = new HashMap<String, Object>();
					outputMap.put("TestCategory", lstTestCategory);
					outputMap.put("TestGroupSpecSampleType", listTestGroupSpecSampleType);
					outputMap.put("TestGroupTest", listTestGroupTest);
					return new ResponseEntity<>(outputMap, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
									objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}

			}

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getAvailableTest(UserInfo objUserInfo, TestGroupSpecSampleType objTestGroupComponent,
			int ntreeversiontempcode, int nprojectmastercode, int nclinicaltyperequired) throws Exception {

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		final TestGroupSpecification objTestGroupSpecification = testGroupCommonFunction
				.getActiveSpecification(objTestGroupComponent.getNallottedspeccode(), objUserInfo);

		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);
		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			if (objTestGroupSpecification != null) {

				if (nprojectmastercode != -1) {
					map = (Map<String, Object>) testGroupCommonFunction
							.getProjectMasterStatusCode(nprojectmastercode, objUserInfo);
					strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				}

				else {

					if (objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT
							.gettransactionstatus()
							|| objTestGroupSpecification
									.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
											.gettransactionstatus()) {
						final List<TestCategory> lstTestCategory = (List<TestCategory>) testCategoryDAO
								.getTestCategoryForTestGroup(objUserInfo, nclinicaltyperequired).getBody();
						TestCategory selectedTestCategory = null;
						List<TestGroupTest> listTestGroupTest = new ArrayList<TestGroupTest>();
						if (!lstTestCategory.isEmpty()) {
							List<TestCategory> defaultTestCategory = lstTestCategory.stream().filter(x -> x
									.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
									.collect(Collectors.toList());
							if (!defaultTestCategory.isEmpty()) {
								selectedTestCategory = defaultTestCategory.get(0);// .getNtestcategorycode();
							} else {
								selectedTestCategory = lstTestCategory.get(0);// lstTestCategory.get(lstTestCategory.size()
																				// -
																				// 1);// .getNtestcategorycode();
							}
							final String sQuery = getTestMasterQuery(selectedTestCategory.getNtestcategorycode(),
									objTestGroupComponent.getNcomponentcode(),
									objTestGroupComponent.getNallottedspeccode(), nclinicaltyperequired);
							listTestGroupTest = (List<TestGroupTest>) jdbcTemplate.query(sQuery, new TestGroupTest());
						}
						final Map<String, Object> outputMap = new HashMap<String, Object>();
						outputMap.put("TestCategory", lstTestCategory);
						outputMap.put("SelectedTestCategory", selectedTestCategory);
						outputMap.put("TestGroupTest", listTestGroupTest);
						return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> createTest(final UserInfo objUserInfo, final TestGroupSpecification objSpecification,
			final List<TestGroupSpecSampleType> lstTestGroupComponent, final List<TestGroupTest> listTestGroupTest,
			final Map<String, Object> objMap) throws Exception {

		// ALPD-3618
		final String ntestcodeList = listTestGroupTest.stream().map(testItem -> String.valueOf(testItem.getNtestcode()))
				.collect(Collectors.joining(","));

		final String sQuery = "select tgt.ntestcode,tm.stestname,tgt.nrepeatcountno from testgrouptest tgt,testmaster tm where  tgt.ntestcode in("
				+ ntestcodeList + ") and tm.ntestcode=tgt.ntestcode and tgt.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  exists (select * from testgroupspecsampletype tst " + " where tgt.nspecsampletypecode="
				+ lstTestGroupComponent.get(0).getNspecsampletypecode() + " and "
				+ " tgt.nspecsampletypecode=tst.nspecsampletypecode  and   tst.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

		final List<TestGroupTest> listoldTestGroupTest = jdbcTemplate.query(sQuery, new TestGroupTest());

		final List<TestGroupTest> newlistTestGroupTest = listTestGroupTest.stream()
				.filter(listAvalData -> listoldTestGroupTest.stream()
						.noneMatch(lstTestCode -> lstTestCode.getNtestcode() == listAvalData.getNtestcode()))
				.collect(Collectors.toList());

		if (!newlistTestGroupTest.isEmpty()) {

			final Map<String, Object> tempMap = insertTest(objUserInfo, objSpecification, lstTestGroupComponent,
					newlistTestGroupTest, objMap);
			final ObjectMapper objMapper = new ObjectMapper();

			String insertQueryString = "";
			List<String> lstInsertString = (List<String>) tempMap.get("insertqry");
			insertQueryString += lstInsertString.stream().map(str -> str).collect(Collectors.joining(" "));
			jdbcTemplate.execute(insertQueryString);

			List<String> listString = (List<String>) tempMap.get("stestgrouptestcode");
			String stestgrouptestcode = String.join(",", listString);
			String sAuditQuery = "";
			sAuditQuery = "select * from testgrouptest where ntestgrouptestcode in (" + stestgrouptestcode + ");";
			List<TestGroupTest> lstTestGroupTest = jdbcTemplate.query(sAuditQuery, new TestGroupTest());

			sAuditQuery = "select tgtp.ntestgrouptestparametercode, tgtp.ntestgrouptestcode, tgtp.ntestparametercode,"
					+ " tgtp.nparametertypecode, tgtp.nunitcode, tgtp.sparametersynonym, tgtp.nroundingdigits, tgtp.nresultmandatory,"
					+ " tgtp.nreportmandatory, tgtp.ngradingmandatory, tgtp.nchecklistversioncode, tgtp.sspecdesc, tgtp.nsorter,"
					+ " tgtp.nisadhocparameter, tgtp.nisvisible, tgtp.nsitecode, tgtp.nstatus, pt.sdisplaystatus,tgtp.nresultaccuracycode from testgrouptestparameter tgtp,"
					+ " parametertype pt where ntestgrouptestcode in (" + stestgrouptestcode + ") and"
					+ " tgtp.nparametertypecode=pt.nparametertypecode and tgtp.nsitecode="
					+ objUserInfo.getNmastersitecode() + " and pt.nsitecode=" + objUserInfo.getNmastersitecode() + ";";
			List<TestGroupTestParameter> lstTestGroupTestParameter = objMapper.convertValue(
					commonFunction.getMultilingualMessageList(
							jdbcTemplate.query(sAuditQuery, new TestGroupTestParameter()),
							Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()),
					new TypeReference<List<TestGroupTestParameter>>() {
					});

			sAuditQuery = "select * from testgrouptestformula where ntestgrouptestcode in (" + stestgrouptestcode
					+ ") and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
			List<TestGroupTestFormula> lstTestGroupTestFormula = jdbcTemplate.query(sAuditQuery,
					new TestGroupTestFormula());

			sAuditQuery = "select * from testgrouptestfile where ntestgrouptestcode in (" + stestgrouptestcode + ")"
					+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
			List<TestGroupTestFile> lstTestGroupTestFile = jdbcTemplate.query(sAuditQuery, new TestGroupTestFile());

			sAuditQuery = "select * from testgrouptestpredefparameter tgtpp,testgrouptestparameter tgtp where tgtp.ntestgrouptestparametercode = tgtpp.ntestgrouptestparametercode and tgtp.ntestgrouptestcode in ("
					+ stestgrouptestcode + ") and tgtpp.nsitecode=" + objUserInfo.getNmastersitecode()
					+ " and tgtp.nsitecode=" + objUserInfo.getNmastersitecode() + ";";
			List<TestGroupTestPredefinedParameter> lstTestGroupTestPredefinedParameter = jdbcTemplate.query(sAuditQuery,
					new TestGroupTestPredefinedParameter());

			List<Object> lstnewobject = new ArrayList<>();
			lstnewobject.add(lstTestGroupTest);

			List<String> lstAction = new ArrayList<String>();
			lstAction.add("IDS_ADDTEST");
			auditUtilityFunction.fnInsertListAuditAction(lstnewobject, 1, null, lstAction, objUserInfo);
		}
		return new ResponseEntity<Object>(testGroupCommonFunction.getTestDetails(
				lstTestGroupComponent.get(0).getNspecsampletypecode(), 0, objUserInfo, false), HttpStatus.OK);
	}

	@SuppressWarnings({ "unused" })
	@Override
	public ResponseEntity<Object> deleteTest(UserInfo objUserInfo, TestGroupSpecSampleType objTestGroupComponent,
			TestGroupTest objTest, int ntreeversiontempcode, int nprojectmastercode) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecByTestId(objTest.getNtestgrouptestcode());

		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);
		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			if (objTGS != null) {
				Map<String, Object> map = new LinkedHashMap<String, Object>();
				String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (nprojectmastercode != -1) {
					map = (Map<String, Object>) testGroupCommonFunction
							.getProjectMasterStatusCode(nprojectmastercode, objUserInfo);
					strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				} else {

					if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
							|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
									.gettransactionstatus()) {
						final int ntestgrouptestcode = objTest.getNtestgrouptestcode();
						String sQuery = "";
						sQuery = " select * from testgrouptest where ntestgrouptestcode = " + ntestgrouptestcode
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
						List<TestGroupTest> lstTestGroupTest = jdbcTemplate.query(sQuery, new TestGroupTest());

						sQuery = " select tgtp.ntestgrouptestparametercode, tgtp.ntestgrouptestcode, tgtp.ntestparametercode,"
								+ " tgtp.nparametertypecode, tgtp.nunitcode, tgtp.sparametersynonym, tgtp.nroundingdigits,"
								+ " tgtp.nresultmandatory, tgtp.nreportmandatory, tgtp.ngradingmandatory, tgtp.nchecklistversioncode,"
								+ " tgtp.sspecdesc, tgtp.nsorter, tgtp.nisadhocparameter, tgtp.nisvisible, tgtp.nsitecode, tgtp.nstatus,"
								+ " pt.sdisplaystatus,tgtp.nresultaccuracycode from testgrouptestparameter tgtp, parametertype pt where tgtp.ntestgrouptestcode = "
								+ ntestgrouptestcode + " and tgtp.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgtp.nparametertypecode=pt.nparametertypecode and tgtp.nsitecode="
								+ objUserInfo.getNmastersitecode() + " and pt.nsitecode="
								+ objUserInfo.getNmastersitecode() + ";";
						List<TestGroupTestParameter> lstTestGroupTestParameter = objMapper.convertValue(
								commonFunction.getMultilingualMessageList(
										jdbcTemplate.query(sQuery, new TestGroupTestParameter()),
										Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()),
								new TypeReference<List<TestGroupTestParameter>>() {
								});

						sQuery = " select * from testgrouptestpredefparameter tgtp, testgrouptestparameter tgp "
								+ " where tgp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode and tgp.nstatus = tgtp.nstatus"
								+ " and tgp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgp.ntestgrouptestcode =  " + ntestgrouptestcode + " and tgtp.nsitecode="
								+ objUserInfo.getNmastersitecode() + " and tgp.nsitecode="
								+ objUserInfo.getNmastersitecode() + ";";
						List<TestGroupTestPredefinedParameter> lstTestGroupTestPredefinedParameter = jdbcTemplate
								.query(sQuery, new TestGroupTestPredefinedParameter());

						sQuery = " select * from testgrouptestcharparameter tgcp, testgrouptestparameter tgp where tgp.ntestgrouptestparametercode = tgcp.ntestgrouptestparametercode"
								+ " and tgp.nstatus = tgcp.nstatus and tgp.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgp.ntestgrouptestcode = " + ntestgrouptestcode + " and tgcp.nsitecode="
								+ objUserInfo.getNmastersitecode() + " and tgp.nsitecode="
								+ objUserInfo.getNmastersitecode() + ";";
						List<TestGroupTestCharParameter> lstTestGroupTestCharParameter = jdbcTemplate.query(sQuery,
								new TestGroupTestCharParameter());

						sQuery = " select * from testgrouptestformula where ntestgrouptestcode = " + ntestgrouptestcode
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
						List<TestGroupTestFormula> lstTestGroupTestFormula = jdbcTemplate.query(sQuery,
								new TestGroupTestFormula());

						sQuery = " select * from testgrouptestfile where ntestgrouptestcode = " + ntestgrouptestcode
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
						List<TestGroupTestFile> lstTestGroupTestFile = jdbcTemplate.query(sQuery,
								new TestGroupTestFile());

						sQuery = " select * from testgrouptestmaterial where ntestgrouptestcode = " + ntestgrouptestcode
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
						List<TestGroupTestMaterial> lstTestGroupTestMaterial = jdbcTemplate.query(sQuery,
								new TestGroupTestMaterial());

						List<Object> mapLists = new ArrayList<>();
						mapLists.add(lstTestGroupTest);
						mapLists.add(lstTestGroupTestParameter);
						mapLists.add(lstTestGroupTestPredefinedParameter);
						mapLists.add(lstTestGroupTestFormula);
						mapLists.add(lstTestGroupTestFile);
						mapLists.add(lstTestGroupTestMaterial);

						String sDeleteQuery = "update testgrouptest set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where ntestgrouptestcode = " + ntestgrouptestcode + ";";
						sDeleteQuery = sDeleteQuery + "update testgrouptestparameter set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", "
								+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where ntestgrouptestcode = " + ntestgrouptestcode + ";";
						sDeleteQuery = sDeleteQuery + "update testgrouptestpredefparameter set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where ntestgrouptestparametercode in (select ntestgrouptestparametercode from testgrouptestparameter where ntestgrouptestcode = "
								+ ntestgrouptestcode + ") and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
						sDeleteQuery = sDeleteQuery + "update testgrouptestcharparameter set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where ntestgrouptestparametercode in (select ntestgrouptestparametercode from testgrouptestparameter where ntestgrouptestcode = "
								+ ntestgrouptestcode + ");";
						sDeleteQuery = sDeleteQuery + "update testgrouptestnumericparameter set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where ntestgrouptestparametercode in (select ntestgrouptestparametercode from testgrouptestparameter where ntestgrouptestcode = "
								+ ntestgrouptestcode + ");";
						sDeleteQuery = sDeleteQuery + "update testgrouptestformula set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", "
								+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where ntestgrouptestcode = " + ntestgrouptestcode + ";";
						sDeleteQuery = sDeleteQuery + "update testgrouptestfile set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", "
								+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where ntestgrouptestcode = " + ntestgrouptestcode + ";";

						sDeleteQuery = sDeleteQuery + "update testgrouptestmaterial set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where ntestgrouptestcode = " + ntestgrouptestcode + ";";

						jdbcTemplate.execute(sDeleteQuery);

						List<String> lstActionType = Arrays.asList("IDS_DELETETEST", "IDS_DELETETESTPARAMETER",
								"IDS_DELETEPREDEFINEDPARAMETER", "IDS_DELETETESTFORMULA", "IDS_DELETETESTFILE",
								"IDS_DELETETESTMATERIAL");
						auditUtilityFunction.fnInsertListAuditAction(mapLists, 1, null, lstActionType, objUserInfo);
						return new ResponseEntity<>(testGroupCommonFunction.getTestDetails(
								objTest.getNspecsampletypecode(), 0, objUserInfo, false), HttpStatus.OK);
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}

				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateTest(UserInfo objUserInfo, TestGroupTest objTest,
			final TestGroupSpecification objGroupSpecification, final TestGroupTestFile objTestFile) throws Exception {
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objGroupSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
				final TestGroupTest objTestGroupTest = testGroupCommonFunction
						.getActiveTestGroupTest(objTest.getNtestgrouptestcode());
				if (objTestGroupTest != null) {
					String insertQueryString = "";
					String sUpdateQuery = "update testgrouptest set nsectioncode = " + objTest.getNsectioncode()
							+ ", nmethodcode = " + objTest.getNmethodcode() + ", nrepeatcountno = "
							+ objTest.getNrepeatcountno() + ", ncontainertypecode=" + objTest.getNcontainertypecode()
							+ ",ninstrumentcatcode = " + objTest.getNinstrumentcatcode() + ",ntestpackagecode="
							+ objTest.getNtestpackagecode() + ", ncost = " + objTest.getNcost() + ", nsorter = "
							+ objTest.getNsorter() + ", stestsynonym = N'"
							+ stringUtilityFunction.replaceQuote(objTest.getStestsynonym()) + "'," + "dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + " where ntestgrouptestcode = "
							+ objTest.getNtestgrouptestcode() + ";";
					insertQueryString += sUpdateQuery;

					final String queryString = "select * from testgrouptestfile where nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestgrouptestcode="
							+ objTestGroupTest.getNtestgrouptestcode() + " and nsitecode="
							+ objUserInfo.getNmastersitecode();
					final TestGroupTestFile testFile = (TestGroupTestFile) jdbcUtilityFunction
							.queryForObject(queryString, TestGroupTestFile.class, jdbcTemplate);

					if (objTestFile == null) {
						sUpdateQuery = "update testgrouptestfile set nstatus="
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where ntestgrouptestcode = " + objTestGroupTest.getNtestgrouptestcode() + ";";
						insertQueryString += sUpdateQuery;
					} else if (objTestFile != null && testFile == null) {

						// NIBSCRT-2257
//						String nseqnoNo = "select ntestgroupfilecode = (SELECT CASE "
//						        + " WHEN  (select max(ntestgroupfilecode)+1 from testgrouptestfile) IS NULL THEN"
//						        + " 1 "
//						        + " ELSE (select max(ntestgroupfilecode)+1 from testgrouptestfile) "
//						        + " END )";

						String nseqnoNo = "select  (SELECT CASE "
								+ " WHEN  (select max(ntestgroupfilecode)+1 from testgrouptestfile) IS NULL THEN"
								+ " 1 " + " ELSE (select max(ntestgroupfilecode)+1 from testgrouptestfile) "
								+ " END ) ntestgroupfilecode";
						Integer nseqnoTestFileNo = jdbcTemplate.queryForObject(nseqnoNo, Integer.class);
						sUpdateQuery = "update seqnotestgroupmanagement set nsequenceno = " + nseqnoTestFileNo + ""
								+ " where stablename='testgrouptestfile' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

						final int offset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
						sUpdateQuery = "insert into testgrouptestfile(ntestgroupfilecode,ntestgrouptestcode,nlinkcode,"
								+ " nattachmenttypecode,sfilename,sdescription,ssystemfilename,nfilesize,"
								+ " dcreateddate,noffsetdcreateddate,ntzcreateddate,dmodifieddate,nstatus,nsitecode)values("
								+ nseqnoTestFileNo + "," + objTest.getNtestgrouptestcode() + " ," + " "
								+ objTestFile.getNlinkcode() + "," + Enumeration.AttachmentType.FTP.gettype() + ",'"
								+ objTestFile.getSfilename() + "','" + objTestFile.getSdescription() + "'," + " '"
								+ objTestFile.getSsystemfilename() + "'," + objTestFile.getNfilesize() + ",'"
								+ objTestFile.getDcreateddate() + "'," + offset + "," + objUserInfo.getNtimezonecode()
								+ "," + "'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
								+ objTestFile.getNstatus() + "," + objUserInfo.getNmastersitecode() + ")";
						insertQueryString += sUpdateQuery;
					} else {
						sUpdateQuery = "update testgrouptestfile set nlinkcode = " + objTestFile.getNlinkcode() + ","
								+ " nattachmenttypecode = " + objTestFile.getNattachmenttypecode() + ", sfilename = N'"
								+ stringUtilityFunction.replaceQuote(objTestFile.getSfilename()) + "',"
								+ " sdescription = N'"
								+ stringUtilityFunction.replaceQuote(objTestFile.getSdescription())
								+ "', ssystemfilename = N'" + objTestFile.getSsystemfilename() + "'," + " nfilesize ="
								+ objTestFile.getNfilesize() + ", dcreateddate = '" + objTestFile.getDcreateddate()
								+ "'," + " noffsetdcreateddate="
								+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
								+ ",ntzcreateddate=" + objUserInfo.getNtimezonecode() + "," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where ntestgroupfilecode = " + objTestFile.getNtestgroupfilecode() + ";";
						insertQueryString += sUpdateQuery;
					}

					jdbcTemplate.execute(insertQueryString);

					List<Object> lstnewobject = new ArrayList<>();
					lstnewobject.add(objTest);
					List<Object> lstoldobject = new ArrayList<>();
					lstoldobject.add(objTestGroupTest);
					auditUtilityFunction.fnInsertAuditAction(lstnewobject, 2, lstoldobject,
							Arrays.asList("IDS_EDITTEST"), objUserInfo);

					if (testFile == null && objTestFile != null) {
						// insert
						auditUtilityFunction.fnInsertAuditAction(Arrays.asList(objTestFile), 1, null,
								Arrays.asList("IDS_ADDTESTGROUPTESTFILE"), objUserInfo);
					}
					if (testFile != null) {
						if (objTestFile == null) {
							// delete
							auditUtilityFunction.fnInsertAuditAction(Arrays.asList(testFile), 1, null,
									Arrays.asList("IDS_DELETETESTGROUPTESTFILE"), objUserInfo);
						} else {
							// update
							auditUtilityFunction.fnInsertAuditAction(Arrays.asList(objTestFile), 2,
									Arrays.asList(testFile), Arrays.asList("IDS_EDITTESTGROUPTESTFILE"), objUserInfo);
						}
					}

					return new ResponseEntity<>(testGroupCommonFunction.getTestDetails(objTest.getNspecsampletypecode(),
							objTest.getNtestgrouptestcode(), objUserInfo, true), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveTestById(final UserInfo objUserInfo, final TestGroupTest objTest,
			final int ntreeversiontempcode, final int nprojectmastercode) throws Exception {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecByTestId(objTest.getNtestgrouptestcode());
		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);

		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			if (objTGS != null) {
				// Integer nprojectStatuscode=-1;
				if (nprojectmastercode != -1) {
					map = (Map<String, Object>) testGroupCommonFunction
							.getProjectMasterStatusCode(nprojectmastercode, objUserInfo);
					strstatus = (String) map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() != strstatus) {

					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(strstatus, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				} else {

					if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
							|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
									.gettransactionstatus()) {
						final Map<String, Object> outputMap = new HashMap<String, Object>();
						final String sQuery = "select tgt.ntestgrouptestcode, tgt.nspecsampletypecode, tgt.ntestcode, tgt.stestsynonym, tgt.ncost, tgt.nsorter,tgt.nrepeatcountno,"
//						+ " tgt.nsourcecode, s.ssourcename,"
								+ " tgt.nsectioncode, sec.ssectionname, tgt.nmethodcode, m.smethodname, tgt.ninstrumentcatcode, ic.sinstrumentcatname,tgt.ncontainertypecode,ct.scontainertype,"
								+ " tp.ntestpackagecode,tp.stestpackagename from testgrouptest tgt,"// +" source s,"
								+ " method m, section sec, instrumentcategory ic,containertype ct,testpackage tp "
								+ " where " + " m.nmethodcode = tgt.nmethodcode and sec.nsectioncode = tgt.nsectioncode"
								+ " and ic.ninstrumentcatcode = tgt.ninstrumentcatcode"
								+ " and ct.ncontainertypecode=tgt.ncontainertypecode "
								+ " and tgt.ntestpackagecode=tp.ntestpackagecode" + " and tp.nstatus=tgt.nstatus "
								+ " and m.nstatus = tgt.nstatus"
								+ " and sec.nstatus = tgt.nstatus and ic.nstatus = tgt.nstatus and tgt.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgt.ntestgrouptestcode = " + objTest.getNtestgrouptestcode()
								+ " and m.nsitecode=" + objUserInfo.getNmastersitecode() + " and sec.nsitecode="
								+ objUserInfo.getNmastersitecode() + " and ic.nsitecode="
								+ objUserInfo.getNmastersitecode() + " and ct.nsitecode="
								+ objUserInfo.getNmastersitecode() + " and tp.nsitecode="
								+ objUserInfo.getNmastersitecode();
						final TestGroupTest objTestGroupTest = (TestGroupTest) jdbcUtilityFunction
								.queryForObject(sQuery, TestGroupTest.class, jdbcTemplate);
						if (objTestGroupTest != null) {

							outputMap.put("SelectedTest", objTestGroupTest);
							final String sTestGroupFileQry = "select * from testgrouptestfile where ntestgrouptestcode = "
									+ objTest.getNtestgrouptestcode() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							outputMap.put("TestGroupTestFile", jdbcUtilityFunction.queryForObject(sTestGroupFileQry,
									TestGroupTestFile.class, jdbcTemplate));
							outputMap.put("SelectedTestGroupTest", objTestGroupTest);
							return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
						} else {
							return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
									objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}

				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getTestByComponentId(final UserInfo objUserInfo,
			final TestGroupSpecSampleType objTestGroupSpecSampleType) throws Exception {
		final String componentValidation = "select count(nspecsampletypecode) from testgroupspecsampletype where nspecsampletypecode = "
				+ objTestGroupSpecSampleType.getNspecsampletypecode() + " " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		int count = jdbcTemplate.queryForObject(componentValidation, Integer.class);
		if (count > 0) {
			return new ResponseEntity<>(testGroupCommonFunction.getTestDetails(
					objTestGroupSpecSampleType.getNspecsampletypecode(), 0, objUserInfo, false), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getTestMasterByCategory(final UserInfo objUserInfo,
			final TestCategory objTestCategory, final TestGroupSpecSampleType objComponent, final int nallottedspeccode)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String sQuery = getTestMasterQuery(objTestCategory.getNtestcategorycode(),
				objComponent.getNcomponentcode(), nallottedspeccode, objTestCategory.getNclinicaltyperequired());
		outputMap.put("TestGroupTest", jdbcTemplate.query(sQuery, new TestGroupTest()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private String getTestMasterQuery(final int ntestcategorycode, final int ncomponentcode,
			final int nallottedspeccode, int nclinicaltyperequired) {
		String sSubQry = "";
		if (ntestcategorycode > 0) {
			sSubQry = " and tm.ntestcategorycode = " + ntestcategorycode;
		}
		//// ALPD-5178 L.Subashini = 26122024 - 'tm.stestsynonym' in the select query
		return "select tm.ntestcode, tm.stestname, tm.stestsynonym, tm.ncost, 1 nrepeatcountno "
				+ " from testmaster tm,testcategory tc where exists (select 1 from testparameter tp where tm.ntestcode = tp.ntestcode and tp.nstatus = tm.nstatus)"
				+ " and not exists (select 1 from testgrouptest tgt, testgroupspecsampletype tgsst where tgt.nspecsampletypecode = tgsst.nspecsampletypecode"
				+ " and tm.ntestcode = tgt.ntestcode and tm.nstatus = tgt.nstatus and tm.nstatus = tgsst.nstatus and tgsst.ncomponentcode = "
				+ ncomponentcode + " and tgsst.nallottedspeccode = " + nallottedspeccode + ") " + sSubQry
				+ " and tm.ntransactionstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tc.ntestcategorycode=tm.ntestcategorycode and tc.nclinicaltyperequired=" + nclinicaltyperequired
				+ " and tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> insertTest(final UserInfo objUserInfo, final TestGroupSpecification objSpecification,
			final List<TestGroupSpecSampleType> lstTestGroupComponent, final List<TestGroupTest> listTest,
			final Map<String, Object> objMap) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		List<String> listTestGroupTestPK = new ArrayList<>();
		List<String> insertQueryArray = new ArrayList<>();
		int ntgtseq = (int) objMap.get("ntgtcount");
		int ntgtcpseq = (int) objMap.get("ntgtcpcount");
		int ntgttfseq = (int) objMap.get("ntgttfcount");
		int ntgttformseq = (int) objMap.get("ntgttformcount");
		int ntgtnpseq = (int) objMap.get("ntgtnpcount");
		int ntgttpseq = (int) objMap.get("ntgttpcount");
		int ntgtppseq = (int) objMap.get("ntgtppcount");
		int ntgtscseq = (int) objMap.get("ntgtcscount");
		String stestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNtestcode");
		Map<String, Object> mapLists = new HashMap<String, Object>();
		String sQuery = "";
		sQuery = "select nsectioncode,ntestcode from testsection where ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode in(" + stestcode + ")"
				+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
		List<TestSection> lstTS = jdbcTemplate.query(sQuery, new TestSection());

		sQuery = "select nmethodcode,ntestcode from testmethod where ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode in(" + stestcode + ")"
				+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
		List<TestMethod> lstTM = jdbcTemplate.query(sQuery, new TestMethod());

		sQuery = "select ninstrumentcatcode,ntestcode from testinstrumentcategory where ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode in(" + stestcode + ")"
				+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
		List<TestInstrumentCategory> lstTestInstrumentCategory = jdbcTemplate.query(sQuery,
				new TestInstrumentCategory());

		sQuery = "select * from testfile where ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode in(" + stestcode + ")"
				+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
		List<TestFile> lstTF1 = jdbcTemplate.query(sQuery, new TestFile());

		sQuery = "select ntestparametercode,ntestcode,nparametertypecode,nroundingdigits,nunitcode,sparametername,sparametersynonym,nresultaccuracycode "
				+ " from testparameter where nstatus=1 and ntestcode in (" + stestcode + ") and nsitecode="
				+ objUserInfo.getNmastersitecode() + ";";
		List<TestParameter> lstTestParameter = jdbcTemplate.query(sQuery, new TestParameter());

		sQuery = "select tpp.ntestpredefinedcode,tpp.ngradecode,tpp.ntestparametercode,tpp.spredefinedname,tpp.spredefinedsynonym,tpp.ndefaultstatus"
				+ " from testpredefinedparameter tpp,testparameter tp where tp.ntestparametercode = tpp.ntestparametercode"
				+ " and tpp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nparametertypecode="
				+ Enumeration.ParameterType.PREDEFINED.getparametertype()
				+ " and tpp.spredefinedname is not null and tp.ntestcode in(" + stestcode + ") and tpp.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and tp.nsitecode=" + objUserInfo.getNmastersitecode() + ";";
		List<TestPredefinedParameter> lstTestPredefinedParameter = jdbcTemplate.query(sQuery,
				new TestPredefinedParameter());

		sQuery = "select * from source where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ndefaultstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus() + ";";
		List<Source> lstSource = null;// getJdbcTemplate().query(sQuery,new Source());
		sQuery = "select ncontainertypecode,ntestcode from testcontainertype where ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode in(" + stestcode + ")"
				+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
		List<TestContainerType> lstTestContainerType = jdbcTemplate.query(sQuery, new TestContainerType());

		sQuery = "select ntestpackagecode,ntestcode from testpackagetest where   nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestcode in(" + stestcode + ")"
				+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
		List<TestPackageTest> lstTestPackage = jdbcTemplate.query(sQuery, new TestPackageTest());

		mapLists.put("TestSection", lstTS);
		mapLists.put("TestMethod", lstTM);
		mapLists.put("TestInstrumentCategory", lstTestInstrumentCategory);
		mapLists.put("TestFile", lstTF1);
		mapLists.put("TestParameter", lstTestParameter);
		mapLists.put("TestPredefinedParameter", lstTestPredefinedParameter);
		mapLists.put("Source", lstSource);
		mapLists.put("TestContainerType", lstTestContainerType);
		mapLists.put("TestPackageTest", lstTestPackage);

		List<TestParameter> lstTestParam = (List<TestParameter>) mapLists.get(TestParameter.class.getSimpleName());

		List<TestParameter> lstCharParam = lstTestParam.stream().filter(
				objParam -> objParam.getNparametertypecode() == Enumeration.ParameterType.CHARACTER.getparametertype())
				.collect(Collectors.toList());

		List<TestParameter> lstNumericParam = lstTestParam.stream().filter(
				objParam -> objParam.getNparametertypecode() == Enumeration.ParameterType.NUMERIC.getparametertype())
				.collect(Collectors.toList());

		List<TestPredefinedParameter> lstPredefParam = (List<TestPredefinedParameter>) mapLists
				.get(TestPredefinedParameter.class.getSimpleName());

		List<TestFile> lstTestFile = (List<TestFile>) mapLists.get(TestFile.class.getSimpleName());

		List<TestSection> lstTestSection = (List<TestSection>) mapLists.get(TestSection.class.getSimpleName());

		List<TestMethod> lstTestMethod = (List<TestMethod>) mapLists.get(TestMethod.class.getSimpleName());

		List<TestInstrumentCategory> lstTestIC = (List<TestInstrumentCategory>) mapLists
				.get(TestInstrumentCategory.class.getSimpleName());

		List<TestContainerType> lstTestCT = (List<TestContainerType>) mapLists
				.get(TestContainerType.class.getSimpleName());

		List<TestPackageTest> lstTestTB = (List<TestPackageTest>) mapLists.get(TestPackageTest.class.getSimpleName());

		if (lstNumericParam != null && !lstNumericParam.isEmpty()) {
			String sparametercode = stringUtilityFunction.fnDynamicListToString(lstNumericParam,
					"getNtestparametercode");
			sQuery = "select ntestformulacode, ntestcode, ntestparametercode, sformulaname, sformulacalculationcode, sformulacalculationdetail, ndefaultstatus, nsitecode, nstatus from testformula where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and ntestparametercode in("
					+ sparametercode + ") and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
			List<TestFormula> lsttf = jdbcTemplate.query(sQuery, new TestFormula());

			sQuery = "select ntestparamnumericcode, ntestparametercode, smina, sminb, smaxb, smaxa, sminlod, smaxlod, sminloq, smaxloq, sdisregard, sresultvalue, nsitecode, nstatus,ngradecode from testparameternumeric where ntestparametercode in ("
					+ sparametercode + ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
			List<TestParameterNumeric> lsttpn = jdbcTemplate.query(sQuery, new TestParameterNumeric());

			sQuery = "select * from testmasterclinicalspec where ntestparametercode in (" + sparametercode
					+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";
			List<TestMasterClinicalSpec> lsttpnSpec = jdbcTemplate.query(sQuery, new TestMasterClinicalSpec());

			mapLists.put("TestFormula", lsttf);
			mapLists.put("TestParameterNumeric", lsttpn);
			mapLists.put("TestMasterClinicalSpec", lsttpnSpec);

		}

		int nsorter = 0;
		final String queryString = "select max(nsorter) from testgrouptest where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nspecsampletypecode="
				+ lstTestGroupComponent.get(0).getNspecsampletypecode() + " and nsitecode="
				+ objUserInfo.getNmastersitecode();
		Integer testSorter = jdbcTemplate.queryForObject(queryString, Integer.class);
		if (testSorter != null) {
			nsorter = testSorter;
		}

		List<TestFormula> lstTestFormula = (List<TestFormula>) mapLists.get(TestFormula.class.getSimpleName());
		List<TestParameterNumeric> lsttestparameternum = (List<TestParameterNumeric>) mapLists
				.get(TestParameterNumeric.class.getSimpleName());
		List<TestMasterClinicalSpec> lstclinialspecnum = (List<TestMasterClinicalSpec>) mapLists
				.get(TestMasterClinicalSpec.class.getSimpleName());
		int nsectioncode = -1;
		int nmethodcode = -1;
		int ninstcatcode = -1;
		int ncontainertypecode = -1;
		int ntestpackagecode = -1;
		for (TestGroupTest objTestGroupTest : listTest) {
			nsorter++;
			List<TestSection> lstSection = lstTestSection.stream()
					.filter(source -> source.getNtestcode() == objTestGroupTest.getNtestcode())
					.collect(Collectors.toList());
			if (lstSection != null && !lstSection.isEmpty()) {
				nsectioncode = lstSection.get(0).getNsectioncode();
			} else {
				nsectioncode = Enumeration.TransactionStatus.NA.gettransactionstatus();
			}
			List<TestMethod> lstMethod = lstTestMethod.stream()
					.filter(source -> source.getNtestcode() == objTestGroupTest.getNtestcode())
					.collect(Collectors.toList());
			if (lstMethod != null && !lstMethod.isEmpty()) {
				nmethodcode = lstMethod.get(0).getNmethodcode();
			} else {
				nmethodcode = Enumeration.TransactionStatus.NA.gettransactionstatus();
			}
			List<TestInstrumentCategory> lstInstCateg = lstTestIC.stream()
					.filter(source -> source.getNtestcode() == objTestGroupTest.getNtestcode())
					.collect(Collectors.toList());
			if (lstInstCateg != null && !lstInstCateg.isEmpty()) {
				ninstcatcode = lstInstCateg.get(0).getNinstrumentcatcode();
			} else {
				ninstcatcode = Enumeration.TransactionStatus.NA.gettransactionstatus();
			}
			List<TestContainerType> lstContainerType = lstTestCT.stream()
					.filter(source -> source.getNtestcode() == objTestGroupTest.getNtestcode())
					.collect(Collectors.toList());
			if (lstContainerType != null && !lstContainerType.isEmpty()) {
				ncontainertypecode = lstContainerType.get(0).getNcontainertypecode();
			} else {
				ncontainertypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();
			}

			List<TestPackageTest> lstTestPackageTest = lstTestTB.stream()
					.filter(source -> source.getNtestcode() == objTestGroupTest.getNtestcode())
					.collect(Collectors.toList());
			if (lstTestPackageTest != null && !lstTestPackageTest.isEmpty()) {
				ntestpackagecode = lstTestPackageTest.get(0).getNtestpackagecode();
			} else {
				ntestpackagecode = Enumeration.TransactionStatus.NA.gettransactionstatus();
			}
			ntgtseq = ntgtseq + 1;
			listTestGroupTestPK.add(String.valueOf(ntgtseq));

			String sinsQuery = "insert into testgrouptest (ntestgrouptestcode, nspecsampletypecode, ntestcode, nsectioncode, nmethodcode, ninstrumentcatcode, "
					+ "ncontainertypecode,ntestpackagecode,stestsynonym, nrepeatcountno, ncost, nsorter, nisadhoctest, nisvisible, dmodifieddate,nsitecode,nstatus) values ("
					+ ntgtseq + "," + lstTestGroupComponent.get(0).getNspecsampletypecode() + ","
					+ objTestGroupTest.getNtestcode() + "," + nsectioncode + "," + nmethodcode + "," + ninstcatcode
					+ "," + ncontainertypecode + "," + ntestpackagecode + ",N'"
					+ stringUtilityFunction.replaceQuote(objTestGroupTest.getStestsynonym()) + "', "
					+ objTestGroupTest.getNrepeatcountno() + ", " + objTestGroupTest.getNcost() + "," + nsorter + ","
					+ objTestGroupTest.getNisadhoctest() + "," + objTestGroupTest.getNisvisible() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + objUserInfo.getNmastersitecode()
					+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			insertQueryArray.add(sinsQuery);
			List<TestFile> lstTF = lstTestFile.stream()
					.filter(objTestFile -> objTestFile.getNtestcode() == objTestGroupTest.getNtestcode())
					.collect(Collectors.toList());
			if (lstTF != null && lstTF.size() > 0) {
				ntgttfseq = ntgttfseq + 1;
				sinsQuery = "insert into testgrouptestfile (ntestgroupfilecode, ntestgrouptestcode, nlinkcode, nattachmenttypecode, sfilename, sdescription, ssystemfilename, nfilesize, dcreateddate,noffsetdcreateddate,ntzcreateddate, dmodifieddate,nstatus,nsitecode)"
						+ " values (" + ntgttfseq + "," + ntgtseq + "," + lstTF.get(0).getNlinkcode() + ","
						+ lstTF.get(0).getNattachmenttypecode() + ",N'"
						+ stringUtilityFunction.replaceQuote(lstTF.get(0).getSfilename()) + "',N'"
						+ stringUtilityFunction.replaceQuote(lstTF.get(0).getSdescription()) + "',N'"
						+ lstTF.get(0).getSsystemfilename() + "', " + lstTF.get(0).getNfilesize() + ", case when N'"
						+ lstTF.get(0).getDcreateddate() + "'='null' then NULL else N'" + lstTF.get(0).getDcreateddate()
						+ "'::timestamp end," + "" + lstTF.get(0).getNoffsetdcreateddate() + " ,"
						+ objUserInfo.getNtimezonecode() + ",'" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
						+ "'," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ objUserInfo.getNmastersitecode() + ");";
				insertQueryArray.add(sinsQuery);
			}
			int testParameterSorter = 0;
			for (TestParameter objTestParameter : lstTestParam) {
				if (objTestGroupTest.getNtestcode() == objTestParameter.getNtestcode()) {
					testParameterSorter++;
					ntgttpseq = ntgttpseq + 1;

					sinsQuery = "insert into testgrouptestparameter (ntestgrouptestparametercode, ntestgrouptestcode, ntestparametercode, nparametertypecode, nunitcode, sparametersynonym,"
							+ "nroundingdigits, nresultmandatory, nreportmandatory, ngradingmandatory, nchecklistversioncode, sspecdesc, nsorter, nisadhocparameter, nisvisible,dmodifieddate, nstatus,nsitecode,nresultaccuracycode)"
							+ " values (" + ntgttpseq + "," + ntgtseq + "," + objTestParameter.getNtestparametercode()
							+ "," + objTestParameter.getNparametertypecode() + "," + objTestParameter.getNunitcode()
							+ ",N'" + stringUtilityFunction.replaceQuote(objTestParameter.getSparametersynonym()) + "',"
							+ objTestParameter.getNroundingdigits() + ","
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "," + " -1, NULL,"
							+ testParameterSorter + "," + Enumeration.TransactionStatus.NO.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ objUserInfo.getNmastersitecode() + "," + objTestParameter.getNresultaccuracycode() + ");";
					insertQueryArray.add(sinsQuery);
					for (TestParameter objCharTestParam : lstCharParam) {
						if (objTestParameter.getNtestparametercode() == objCharTestParam.getNtestparametercode()) {
							ntgtcpseq = ntgtcpseq + 1;
							sinsQuery = "insert into testgrouptestcharparameter (ntestgrouptestcharcode, ntestgrouptestparametercode, scharname,dmodifieddate,nstatus,nsitecode)"
									+ " values (" + ntgtcpseq + "," + ntgttpseq + ",NULL,'"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ objUserInfo.getNmastersitecode() + ");";
							insertQueryArray.add(sinsQuery);
						}
					}
					for (TestPredefinedParameter objPredefParam : lstPredefParam) {
						if (objTestParameter.getNtestparametercode() == objPredefParam.getNtestparametercode()) {
							ntgtppseq = ntgtppseq + 1;

							sinsQuery = "insert into testgrouptestpredefparameter (ntestgrouptestpredefcode, ntestgrouptestparametercode, ngradecode, spredefinedname,spredefinedsynonym,"
									+ " ndefaultstatus,dmodifieddate,nstatus,nsitecode) values (" + (ntgtppseq) + ","
									+ ntgttpseq + "," + objPredefParam.getNgradecode() + "," + "N'"
									+ stringUtilityFunction.replaceQuote(objPredefParam.getSpredefinedname()) + "',N'"
									+ stringUtilityFunction.replaceQuote(objPredefParam.getSpredefinedsynonym()) + "',"
									+ objPredefParam.getNdefaultstatus() + ",'"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ objUserInfo.getNmastersitecode() + ");";

							insertQueryArray.add(sinsQuery);
						}
					}
					for (TestParameter objNumericParam : lstNumericParam) {
						if (objTestParameter.getNtestparametercode() == objNumericParam.getNtestparametercode()) {
							ntgtnpseq = ntgtnpseq + 1;
							List<TestParameterNumeric> lsttestparamnum = lsttestparameternum.stream()
									.filter(x -> x.getNtestparametercode() == objTestParameter.getNtestparametercode())
									.collect(Collectors.toList());
							if (lsttestparamnum != null && !lsttestparamnum.isEmpty()) {// Need to include clinical demo
								// ntgtnpseq=ntgtnpseq+1; // fields in Future
								sinsQuery = "insert into testgrouptestnumericparameter (ntestgrouptestnumericcode, ntestgrouptestparametercode, sminlod, smaxlod, sminb,"
										+ " smina, smaxa, smaxb, sminloq, smaxloq, sdisregard, sresultvalue,dmodifieddate,nstatus,nsitecode,ngradecode)"
										+ " values (" + ntgtnpseq + "," + ntgttpseq + "," + " case when N'"
										+ lsttestparamnum.get(0).getSminlod() + "'='null' then  NULL else N'"
										+ lsttestparamnum.get(0).getSminlod() + "' end," + " case when N'"
										+ lsttestparamnum.get(0).getSmaxlod() + "'='null' then  NULL else N'"
										+ lsttestparamnum.get(0).getSmaxlod() + "' end ," + " case when N'"
										+ lsttestparamnum.get(0).getSminb() + "'='null' then NULL else N'"
										+ lsttestparamnum.get(0).getSminb() + "' end ," + " case when N'"
										+ lsttestparamnum.get(0).getSmina() + "'='null' then  NULL else N'"
										+ lsttestparamnum.get(0).getSmina() + "' end ," + " case when N'"
										+ lsttestparamnum.get(0).getSmaxa() + "'='null' then NULL else  N'"
										+ lsttestparamnum.get(0).getSmaxa() + "' end ," + " case when N'"
										+ lsttestparamnum.get(0).getSmaxb() + "'='null' then NULL else  N'"
										+ lsttestparamnum.get(0).getSmaxb() + "' end ," + " case when N'"
										+ lsttestparamnum.get(0).getSminloq() + "'='null' then NULL else  N'"
										+ lsttestparamnum.get(0).getSminloq() + "' end ," + " case when N'"
										+ lsttestparamnum.get(0).getSmaxloq() + "'='null' then NULL else  N'"
										+ lsttestparamnum.get(0).getSmaxloq() + "' end ," + " case when N'"
										+ lsttestparamnum.get(0).getSdisregard() + "'='null' then NULL else  N'"
										+ lsttestparamnum.get(0).getSdisregard() + "' end ," + " case when N'"
										+ lsttestparamnum.get(0).getSresultvalue() + "'='null' then NULL else  N'"
										+ lsttestparamnum.get(0).getSresultvalue() + "' end ," + " " + "'"
										+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ objUserInfo.getNmastersitecode() + ","
										+ lsttestparamnum.get(0).getNgradecode() + ");";
							} else {
								sinsQuery = "insert into testgrouptestnumericparameter (ntestgrouptestnumericcode, ntestgrouptestparametercode, sminlod, smaxlod, sminb,"
										+ " smina, smaxa, smaxb, sminloq, smaxloq, sdisregard, sresultvalue,dmodifieddate, nstatus,nsitecode,ngradecode)"
										+ " values (" + ntgtnpseq + ", " + ntgttpseq
										+ ", NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL," + "'"
										+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ objUserInfo.getNmastersitecode() + ",-1);";
							}
							insertQueryArray.add(sinsQuery);

							List<TestFormula> lstTestForm = lstTestFormula
									.stream().filter(objFormula -> objFormula
											.getNtestparametercode() == objTestParameter.getNtestparametercode())
									.collect(Collectors.toList());
							if (lstTestForm != null && !lstTestForm.isEmpty()) {
								ntgttformseq = ntgttformseq + 1;
								sinsQuery = "insert into testgrouptestformula (ntestgrouptestformulacode, ntestgrouptestcode, ntestgrouptestparametercode, ntestformulacode,"
										+ " sformulacalculationcode, sformulacalculationdetail, ntransactionstatus, dmodifieddate,nstatus,nsitecode)"
										+ " values (" + ntgttformseq + "," + ntgtseq + "," + ntgttpseq + ","
										+ lstTestForm.get(0).getNtestformulacode() + "," + "N'"
										+ lstTestForm.get(0).getSformulacalculationcode() + "'" + ",N'"
										+ stringUtilityFunction
												.replaceQuote(lstTestForm.get(0).getSformulacalculationdetail())
										+ "'," + lstTestForm.get(0).getNdefaultstatus() + "," + "'"
										+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ objUserInfo.getNmastersitecode() + ");";
								insertQueryArray.add(sinsQuery);
							}
						}
					}

					if (lstclinialspecnum != null && lstclinialspecnum.size() > 0) {
						List<TestMasterClinicalSpec> lsttestparamclinialspecs = new ArrayList<TestMasterClinicalSpec>();
						lsttestparamclinialspecs = lstclinialspecnum.stream()
								.filter(x -> x.getNtestparametercode() == objTestParameter.getNtestparametercode())
								.collect(Collectors.toList());
						for (TestMasterClinicalSpec lsttestparamclinialspec : lsttestparamclinialspecs) {
							if (lsttestparamclinialspec != null) {// Need to include clinical demo
								ntgtscseq = ntgtscseq + 1; // fields in Future
								// ALPD-5054 added by Dhanushya RI,To insert fromage period and toage period
								sinsQuery = "insert into testgrouptestclinicalspec (ntestgrouptestclinicspeccode, ntestgrouptestparametercode, ngendercode, nfromage, ntoage, shigha, shighb, slowa, slowb, sresultvalue,"
										+ " sminlod, smaxlod, sminloq, smaxloq, sdisregard, ntzmodifieddate, noffsetdmodifieddate,dmodifieddate, nstatus,nsitecode, ngradecode,nfromageperiod,ntoageperiod)"
										+ " values (" + ntgtscseq + "," + ntgttpseq + ","
										+ lsttestparamclinialspec.getNgendercode() + ","
										+ lsttestparamclinialspec.getNfromage() + ","
										+ lsttestparamclinialspec.getNtoage() + "," + "case when N'"
										+ lsttestparamclinialspec.getShigha() + "'='null' then  NULL else N'"
										+ lsttestparamclinialspec.getShigha() + "' end," + "case when N'"
										+ lsttestparamclinialspec.getShighb() + "'='null' then  NULL else N'"
										+ lsttestparamclinialspec.getShighb() + "' end," + "case when N'"
										+ lsttestparamclinialspec.getSlowa() + "'='null' then  NULL else N'"
										+ lsttestparamclinialspec.getSlowa() + "' end," + "case when N'"
										+ lsttestparamclinialspec.getSlowb() + "'='null' then  NULL else N'"
										+ lsttestparamclinialspec.getSlowb() + "' end," + "case when N'"
										+ lsttestparamclinialspec.getSresultvalue() + "'='null' then  NULL else N'"
										+ lsttestparamclinialspec.getSresultvalue() + "' end," + "case when N'"
										+ lsttestparamclinialspec.getSminlod() + "'='null' then  NULL else N'"
										+ lsttestparamclinialspec.getSminlod() + "' end," + "case when N'"
										+ lsttestparamclinialspec.getSmaxlod() + "'='null' then  NULL else N'"
										+ lsttestparamclinialspec.getSmaxlod() + "' end," + "case when N'"
										+ lsttestparamclinialspec.getSminloq() + "'='null' then  NULL else N'"
										+ lsttestparamclinialspec.getSminloq() + "' end," + "case when N'"
										+ lsttestparamclinialspec.getSmaxloq() + "'='null' then  NULL else N'"
										+ lsttestparamclinialspec.getSmaxloq() + "' end," + "case when N'"
										+ lsttestparamclinialspec.getSdisregard() + "'='null' then  NULL else N'"
										+ lsttestparamclinialspec.getSdisregard() + "' end,"
										+ objUserInfo.getNtimezonecode() + ","
										+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
										+ ",'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ objUserInfo.getNmastersitecode() + ","
										+ lsttestparamclinialspec.getNgradecode() + "," + " "
										+ lsttestparamclinialspec.getNfromageperiod() + ","
										+ lsttestparamclinialspec.getNtoageperiod() + ");";
								insertQueryArray.add(sinsQuery);
							}
						}
					}
				}
			}
		}
		outputMap.put("insertqry", insertQueryArray);
		outputMap.put("stestgrouptestcode", listTestGroupTestPK);
		return outputMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getTestGroupComponent(UserInfo objUserInfo, TestGroupSpecification objTestGroupSpec)
			throws Exception {
		Map<String, Object> outputMap = null;
		List<ApprovalRoleActionDetail> listActions = testGroupCommonFunction.getApprovalConfigAction(objTestGroupSpec,
				objUserInfo);
		outputMap = testGroupCommonFunction.getComponentDetails(objTestGroupSpec, objUserInfo);
		outputMap.put("ApprovalRoleActionDetail", listActions);
		outputMap.putAll((Map<String, Object>) testGroupCommonFunction
				.getSpecificationFile(objUserInfo, objTestGroupSpec).getBody());
		outputMap.putAll((Map<String, Object>) testGroupCommonFunction
				.getSpecificationHistory(objUserInfo, objTestGroupSpec).getBody());
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTestGroupTest(final UserInfo objUserInfo, final int testgrouptestcode)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<>();
		TestGroupTest objTestGroupTest = (TestGroupTest) jdbcUtilityFunction.queryForObject(
				"select nspecsampletypecode from testgrouptest" + " where ntestgrouptestcode = " + testgrouptestcode
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				TestGroupTest.class, jdbcTemplate);

		if (objTestGroupTest != null) {
			final List<TestGroupTest> listTestGroupTest = testGroupCommonFunction
					.getTestGroupTest(objTestGroupTest.getNspecsampletypecode(), testgrouptestcode, objUserInfo);
			if (!listTestGroupTest.isEmpty()) {
				outputMap.put("SelectedTest", listTestGroupTest.get(0));
				List<TestGroupTestParameter> listTGTP = testGroupCommonFunction
						.getTestGroupTestParameter(listTestGroupTest.get(0).getNtestgrouptestcode(), 0, objUserInfo);
				outputMap.put("TestGroupTestParameter", listTGTP);
				List<TestGroupTestMaterial> listTGTM = testGroupCommonFunction
						.getTestGroupTestMaterial(listTestGroupTest.get(0).getNtestgrouptestcode(), 0, objUserInfo);
				outputMap.put("TestGroupTestMaterial", listTGTM);
				if (!listTGTM.isEmpty()) {
					outputMap.put("selectedMaterial",
							commonFunction.getMultilingualMessageList(listTGTM, Arrays.asList("sdisplaystatus"),
									objUserInfo.getSlanguagefilename()).get(listTGTM.size() - 1));
				}
				if (!listTGTP.isEmpty()) {
					outputMap
							.putAll(testGroupCommonFunction.getTestGroupParameterDetails(listTGTP.get(0), objUserInfo));
				}
				outputMap.put("ntestgrouptestcode", testgrouptestcode);
				outputMap.putAll(
						(Map<String, Object>) testGroupCommonFunction.getTestGroupRulesEngine(outputMap, objUserInfo));
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getTestGroupTestParameter(UserInfo objUserInfo, TestGroupTestParameter objParameter)
			throws Exception {
		return testGroupCommonFunction.getTestGroupParameter(objUserInfo, objParameter, false);
	}

	@Override
	public ResponseEntity<Object> getTestGroupTestMaterial(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testGroupCommonFunction.getTestGroupMaterial(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestGroupFormula(final TestGroupTestParameter objParameter,
			final UserInfo objUserInfo, final TestGroupSpecification objSpecification) throws Exception {
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
				final String sQuery = "select * from testformula where ntestparametercode="
						+ objParameter.getNtestparametercode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ntestformulacode not in (select ntestformulacode from testgrouptestformula "
						+ " where ntestgrouptestparametercode=" + objParameter.getNtestgrouptestparametercode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
						+ " and nsitecode=" + objUserInfo.getNmastersitecode();
				final List<TestFormula> listFormula = (List<TestFormula>) jdbcTemplate.query(sQuery, new TestFormula());
				if (listFormula.isEmpty()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FORMULANOTAVAILABLE",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				} else {
					return new ResponseEntity<>(listFormula, HttpStatus.OK);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> createTestGroupFormula(UserInfo objUserInfo, List<TestGroupTestFormula> listFormula,
			final TestGroupSpecification objGroupSpecification) throws Exception {

		final String Query = " lock  table locktestgroupformula " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(Query);

		final String lockQuery = "LOCK TABLE locktestgroup;";
		jdbcTemplate.execute(lockQuery);
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objGroupSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
				String sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename = 'testgrouptestformula' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				int nSeqNo = jdbcTemplate.queryForObject(sQuery, Integer.class);
				sQuery = "select ntestgrouptestformulacode,ntransactionstatus, ntestformulacode from testgrouptestformula"
						+ " where ntestgrouptestparametercode = " + listFormula.get(0).getNtestgrouptestparametercode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + objUserInfo.getNmastersitecode();
				List<TestGroupTestFormula> lstOldFormula = (List<TestGroupTestFormula>) jdbcTemplate.query(sQuery,
						new TestGroupTestFormula());

				List<TestGroupTestFormula> filteredList = listFormula.stream()
						.filter(source -> lstOldFormula.stream()
								.noneMatch(check -> source.getNtestformulacode() == check.getNtestformulacode()))
						.collect(Collectors.toList());
				final long lstTF = lstOldFormula.stream().filter(
						y -> y.getNtransactionstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
						.count();
				int count = 0;
				String insertQueryString = "";
				for (TestGroupTestFormula objTGTF : filteredList) {
					if (count == 0) {
						if (lstTF == 0) {
							objTGTF.setNtransactionstatus(
									(short) Enumeration.TransactionStatus.YES.gettransactionstatus());
						}
					} else {
						objTGTF.setNtransactionstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					}
					count++;
					nSeqNo++;
					sQuery = "insert into testgrouptestformula (ntestgrouptestformulacode, ntestgrouptestcode, ntestgrouptestparametercode, ntestformulacode, "
							+ "sformulacalculationcode, sformulacalculationdetail, ntransactionstatus,dmodifieddate, nstatus,nsitecode) "
							+ " values (" + nSeqNo + "," + objTGTF.getNtestgrouptestcode() + ","
							+ objTGTF.getNtestgrouptestparametercode() + "," + objTGTF.getNtestformulacode() + "," + "'"
							+ stringUtilityFunction.replaceQuote(objTGTF.getSformulacalculationcode()) + "','"
							+ stringUtilityFunction.replaceQuote(objTGTF.getSformulacalculationdetail()) + "',"
							+ objTGTF.getNtransactionstatus() + ", " + "'"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ objUserInfo.getNmastersitecode() + ");";
					insertQueryString += sQuery;
					objTGTF.setNtestgrouptestformulacode(nSeqNo);
				}
				sQuery = "update seqnotestgroupmanagement set nsequenceno = " + nSeqNo
						+ " where stablename = 'testgrouptestformula' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
				insertQueryString += sQuery;
				jdbcTemplate.execute(insertQueryString);

				List<String> lstactionType = new ArrayList<String>();
				lstactionType.add("IDS_ADDFORMULA");
				auditUtilityFunction.fnInsertAuditAction(filteredList, 1, null, lstactionType, objUserInfo);
				return new ResponseEntity<>(testGroupCommonFunction.getTestGroupTestFormula(
						listFormula.get(0).getNtestgrouptestparametercode(), objUserInfo), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteTestGroupFormula(UserInfo objUserInfo, TestGroupTestFormula objFormula,
			TestGroupSpecification objGroupSpecification) throws Exception {
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objGroupSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
				final String sQuery = "select * from testgrouptestformula where ntestgrouptestformulacode = "
						+ objFormula.getNtestgrouptestformulacode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				TestGroupTestFormula objTGTF = (TestGroupTestFormula) jdbcUtilityFunction.queryForObject(sQuery,
						TestGroupTestFormula.class, jdbcTemplate);
				if (objTGTF != null) {
					final String sDeleteQry = "delete from testgrouptestformula where ntestgrouptestformulacode = "
							+ objFormula.getNtestgrouptestformulacode() + ";";
					jdbcTemplate.execute(sDeleteQry);
					List<String> lstactionType = new ArrayList<>();
					lstactionType.add("IDS_DELETEFORMULA");
					List<TestGroupTestFormula> lstnewobject = new ArrayList<>();
					lstnewobject.add(objTGTF);
					auditUtilityFunction.fnInsertAuditAction(lstnewobject, 1, null, lstactionType, objUserInfo);
					return new ResponseEntity<>(testGroupCommonFunction.getTestGroupTestFormula(
							objFormula.getNtestgrouptestparametercode(), objUserInfo), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> defaultTestGroupFormula(final UserInfo objUserInfo,
			final TestGroupTestFormula objFormula, final TestGroupSpecification objGroupSpecification)
			throws Exception {
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objGroupSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
				final String sQuery = "select * from testgrouptestformula where ntestgrouptestformulacode = "
						+ objFormula.getNtestgrouptestformulacode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				TestGroupTestFormula objOldFormula = (TestGroupTestFormula) jdbcUtilityFunction.queryForObject(sQuery,
						TestGroupTestFormula.class, jdbcTemplate);
				if (objOldFormula != null) {
					if (objOldFormula.getNtransactionstatus() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDEFAULT.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					} else {
						final String sDefaultQry = "select * from testgrouptestformula where nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and ntransactionstatus = "
								+ Enumeration.TransactionStatus.YES.gettransactionstatus()
								+ " and ntestgrouptestformulacode <> " + objOldFormula.getNtestgrouptestformulacode()
								+ " and nsitecode=" + objUserInfo.getNmastersitecode();
						TestGroupTestFormula objDefaultFormula = (TestGroupTestFormula) jdbcUtilityFunction
								.queryForObject(sDefaultQry, TestGroupTestFormula.class, jdbcTemplate);
						String sUpdateQuery = "";
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<>();
						final List<Object> lstNewObject = new ArrayList<>();
						if (objDefaultFormula != null) {
							sUpdateQuery = sUpdateQuery + "update testgrouptestformula set ntransactionstatus = "
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "," + "dmodifieddate='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
									+ " where ntestgrouptestformulacode = "
									+ objDefaultFormula.getNtestgrouptestformulacode() + ";";
							lstOldObject.add(objDefaultFormula);
							TestGroupTestFormula objNewTestFormula = SerializationUtils.clone(objDefaultFormula);
							objNewTestFormula.setNtransactionstatus(
									(short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							lstNewObject.add(objNewTestFormula);
							multilingualIDList.add("IDS_EDITTESTFORMULA");
						}
						lstOldObject.add(objOldFormula);
						multilingualIDList.add("IDS_EDITTESTFORMULA");
						TestGroupTestFormula objNewTestFormula = SerializationUtils.clone(objOldFormula);
						objNewTestFormula.setNtransactionstatus(
								(short) Enumeration.TransactionStatus.YES.gettransactionstatus());
						lstNewObject.add(objNewTestFormula);
						sUpdateQuery = sUpdateQuery + "update testgrouptestformula set ntransactionstatus = "
								+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
								+ " where ntestgrouptestformulacode = " + objFormula.getNtestgrouptestformulacode()
								+ ";";

						jdbcTemplate.execute(sUpdateQuery);

						auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList,
								objUserInfo);
						return new ResponseEntity<>(testGroupCommonFunction.getTestGroupTestFormula(
								objFormula.getNtestgrouptestparametercode(), objUserInfo), HttpStatus.OK);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> updateTestGroupParameter(UserInfo objUserInfo,
			TestGroupTestNumericParameter objNumericParameter, TestGroupTestCharParameter objCharParameter,
			TestGroupTestPredefinedParameter objPredefinedParameter, TestGroupTestParameter objParameter,
			final TestGroupTestFormula testGroupTestFormula) throws Exception {
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecByTestId(objParameter.getNtestgrouptestcode());
		String totalcount = "select count(ntestgrouptestparametercode) from testgrouptestparameter  where nstatus=1 and nresultmandatory=4 and ntestgrouptestcode="
				+ objParameter.getNtestgrouptestcode() + " and ntestgrouptestparametercode!="
				+ objParameter.getNtestgrouptestparametercode() + " and nsitecode=" + objUserInfo.getNmastersitecode();
		int ntotalcount = jdbcTemplate.queryForObject(totalcount, Integer.class);
		String adhoctotalcount = "select count(ntestgrouptestparametercode) from testgrouptestparameter  where  testgrouptestparameter.nisadhocparameter=3 and nresultmandatory=4 and testgrouptestparameter.nstatus=1 and  ntestgrouptestcode="
				+ objParameter.getNtestgrouptestcode() + " and ntestgrouptestparametercode!="
				+ objParameter.getNtestgrouptestparametercode() + " and nsitecode=" + objUserInfo.getNmastersitecode();
		int nadhoctotalcount = jdbcTemplate.queryForObject(adhoctotalcount, Integer.class);
		String totalmandatorycount = "select count(ntestgrouptestparametercode) from testgrouptestparameter  where nstatus=1 and nresultmandatory=3 and ntestgrouptestcode="
				+ objParameter.getNtestgrouptestcode() + " and ntestgrouptestparametercode!="
				+ objParameter.getNtestgrouptestparametercode() + " and nsitecode=" + objUserInfo.getNmastersitecode();
		int ntotalmandatorycount = jdbcTemplate.queryForObject(totalmandatorycount, Integer.class);
		if ((ntotalcount == nadhoctotalcount && objParameter.getNisadhocparameter() != 4
				&& ntotalmandatorycount == 0)) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ATLEASTONEPARAMETERISNOTADHOC",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			if (objTGS != null) {
				if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus() || objTGS
						.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
					final TestGroupTest objTestGroupTest = testGroupCommonFunction
							.getActiveTest(objParameter.getNtestgrouptestcode());
					if (objTestGroupTest != null) {
						final String sCheckParameterQry = "select tgtp.ntestgrouptestparametercode, tgtp.ntestgrouptestcode,"
								+ " tgtp.ntestparametercode, tgtp.nparametertypecode, tgtp.nunitcode, tgtp.sparametersynonym,"
								+ " tgtp.nroundingdigits, tgtp.nresultmandatory, tgtp.nreportmandatory, tgtp.ngradingmandatory,"
								+ " tgtp.nchecklistversioncode, tgtp.sspecdesc, tgtp.nsorter, tgtp.nisadhocparameter, tgtp.nisvisible,"
								+ " tgtp.nsitecode, tgtp.nstatus, pt.sdisplaystatus,tgtp.nresultaccuracycode from testgrouptestparameter tgtp, parametertype pt"
								+ " where tgtp.ntestgrouptestparametercode = "
								+ objParameter.getNtestgrouptestparametercode() + " and tgtp.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgtp.nparametertypecode=pt.nparametertypecode" + " and pt.nsitecode="
								+ objUserInfo.getNmastersitecode();
						final TestGroupTestParameter objCheckParameter = (TestGroupTestParameter) jdbcUtilityFunction
								.queryForObject(sCheckParameterQry, TestGroupTestParameter.class, jdbcTemplate);
						objCheckParameter.setSdisplaystatus(commonFunction.getMultilingualMessage(
								objCheckParameter.getSdisplaystatus(), objUserInfo.getSlanguagefilename()));
						if (objCheckParameter != null) {
							List<Object> lstNewObject = new ArrayList<>();
							List<Object> lstOldObject = new ArrayList<>();
							List<String> multilingualIDList = new ArrayList<>();

							List<Object> lstDefaultResultObject = new ArrayList<>();
							List<String> defaultResultMultilingualId = new ArrayList<>();

							List<String> insertQueryArray = new ArrayList<>();
							String insertQueryString = "";
//						final StringBuilder sb = new StringBuilder();
							final String sInsertQuery = "update testgrouptestparameter set nparametertypecode = "
									+ objParameter.getNparametertypecode() + ", nunitcode = "
									+ objParameter.getNunitcode() + ", sparametersynonym = N'"
									+ stringUtilityFunction.replaceQuote(objParameter.getSparametersynonym()) + "',"
									+ " nroundingdigits = " + objParameter.getNroundingdigits()
									+ ", nresultmandatory = " + objParameter.getNresultmandatory()
									+ ",nisadhocparameter=" + objParameter.getNisadhocparameter()
									+ ",nreportmandatory = " + objParameter.getNreportmandatory()
									+ ", nchecklistversioncode = " + objParameter.getNchecklistversioncode() + ","
									+ " sspecdesc =  case when N'"
									+ stringUtilityFunction.replaceQuote(objParameter.getSspecdesc())
									+ "'='null' then NULL else N'"
									+ stringUtilityFunction.replaceQuote(objParameter.getSspecdesc())
									+ "' end , nsorter = " + objParameter.getNsorter() + "," + " dmodifieddate='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
									+ " nresultaccuracycode=" + objParameter.getNresultaccuracycode()
									+ " where ntestgrouptestparametercode = "
									+ objParameter.getNtestgrouptestparametercode() + ";";
							insertQueryArray.add(sInsertQuery);
							insertQueryString += sInsertQuery;

							lstNewObject.add(objParameter);
							lstOldObject.add(objCheckParameter);
							multilingualIDList.add("IDS_EDITPARAMETER");
							StringBuilder sb1 = new StringBuilder();
							final String sNUmericParameterCheck = "select *"
									+ " from testgrouptestnumericparameter where ntestgrouptestparametercode = "
									+ objParameter.getNtestgrouptestparametercode() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
									+ objUserInfo.getNmastersitecode() + ";";
							List<TestGroupTestNumericParameter> lstTestGroupTestNumericParameter = jdbcTemplate
									.query(sNUmericParameterCheck, new TestGroupTestNumericParameter());

							final String sCharParameterCheck = "select * from testgrouptestcharparameter"
									+ " where ntestgrouptestparametercode = "
									+ objParameter.getNtestgrouptestparametercode() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
									+ objUserInfo.getNmastersitecode() + ";";
							List<TestGroupTestCharParameter> lstTestGroupTestCharParameter = jdbcTemplate
									.query(sCharParameterCheck, new TestGroupTestCharParameter());

							final String sPredefParameterCheck = "select *"
									+ " from testgrouptestpredefparameter where ntestgrouptestparametercode = "
									+ objParameter.getNtestgrouptestparametercode() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
									+ objUserInfo.getNmastersitecode() + ";";
							List<TestGroupTestPredefinedParameter> lstTestGroupTestPredefinedParameter = jdbcTemplate
									.query(sPredefParameterCheck, new TestGroupTestPredefinedParameter());

							final String sFormulaCheck = "select * from testgrouptestformula where ntestgrouptestparametercode = "
									+ objParameter.getNtestgrouptestparametercode() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
									+ objUserInfo.getNmastersitecode() + ";";
							List<TestGroupTestFormula> lstTestGroupTestFormula = jdbcTemplate.query(sFormulaCheck,
									new TestGroupTestFormula());
							Map<String, Object> mapParameterDetails = new HashMap<String, Object>();
							mapParameterDetails.put("TestGroupTestNumericParameter", lstTestGroupTestNumericParameter);
							mapParameterDetails.put("TestGroupTestCharParameter", lstTestGroupTestCharParameter);
							mapParameterDetails.put("TestGroupTestPredefinedParameter",
									lstTestGroupTestPredefinedParameter);
							mapParameterDetails.put("TestGroupTestFormula", lstTestGroupTestFormula);

							if (objCheckParameter.getNparametertypecode() != objParameter.getNparametertypecode()) {
								if (objCheckParameter.getNparametertypecode() == Enumeration.ParameterType.CHARACTER
										.getparametertype()) {
									final String sDeleteQry = "update testgrouptestcharparameter set nstatus = "
											+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
											+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
											+ "'" + " where ntestgrouptestparametercode = "
											+ objParameter.getNtestgrouptestparametercode() + " and nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
									insertQueryArray.add(sDeleteQry);
									insertQueryString += sDeleteQry;
									lstDefaultResultObject.add(objParameter);
									defaultResultMultilingualId.add("IDS_DELETECHARACTERRESULT");
								} else if (objCheckParameter
										.getNparametertypecode() == Enumeration.ParameterType.NUMERIC
												.getparametertype()) {
									String sDeleteQry = "update testgrouptestnumericparameter set nstatus = "
											+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
											+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
											+ "'" + " where ntestgrouptestparametercode = "
											+ objParameter.getNtestgrouptestparametercode() + " and nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
									insertQueryArray.add(sDeleteQry);
									insertQueryString += sDeleteQry;
									lstDefaultResultObject.add(objParameter);
									defaultResultMultilingualId.add("IDS_DELETESPECLIMT");
									final List<TestGroupTestFormula> listFormula = (List<TestGroupTestFormula>) mapParameterDetails
											.get("TestGroupTestFormula");
									if (!listFormula.isEmpty()) {
										sDeleteQry = "update testgrouptestformula set nstatus = "
												+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
												+ "dmodifieddate='"
												+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
												+ " where ntestgrouptestformulacode = "
												+ listFormula.get(0).getNtestgrouptestformulacode();
										insertQueryArray.add(sDeleteQry);
										insertQueryString += sDeleteQry;
										lstDefaultResultObject.add(listFormula.get(0));
										defaultResultMultilingualId.add("IDS_DELETEFORMULA");
									}
								} else if (objCheckParameter
										.getNparametertypecode() == Enumeration.ParameterType.PREDEFINED
												.getparametertype()) {
									final String sDeleteQry = "update testgrouptestpredefparameter set nstatus = "
											+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
											+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
											+ "'" + " where ntestgrouptestparametercode = "
											+ objParameter.getNtestgrouptestparametercode() + " and nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
									insertQueryArray.add(sDeleteQry);
									insertQueryString += sDeleteQry;
									lstDefaultResultObject.add(objParameter);
									defaultResultMultilingualId.add("IDS_DELETECODEDRESULT");
								} else if (objCheckParameter
										.getNparametertypecode() == Enumeration.ParameterType.ATTACHEMENT
												.getparametertype()) {
									// Need qry and table for attachment
								}
							}

							final String sSeqQry = jdbcTemplate.queryForObject(
									"select jsonb_object_agg(stablename,nsequenceno) from seqnotestgroupmanagement where stablename "
											+ " in ('testgrouptestcharparameter', 'testgrouptestnumericparameter', 'testgrouptestpredefparameter', 'testgrouptestformula')"
											+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";",
									String.class);
							JSONObject listSeqNo = new JSONObject(sSeqQry);
							// Insert/Update of Numeric Parameter default result
							if (objNumericParameter != null) {
								final List<TestGroupTestNumericParameter> listCheckNumeric = (List<TestGroupTestNumericParameter>) mapParameterDetails
										.get("TestGroupTestNumericParameter");
								if (listCheckNumeric.isEmpty()) {
									objNumericParameter.setNtestgrouptestnumericcode(
											(int) listSeqNo.get("testgrouptestnumericparameter") + 1);
									final String sNumericInsertQuery = "insert into testgrouptestnumericparameter (ntestgrouptestnumericcode, ntestgrouptestparametercode, sminlod, smaxlod, sminb,"
											+ " smina, smaxa, smaxb, sminloq, smaxloq, sdisregard, sresultvalue,dmodifieddate, nstatus,nsitecode,ngradecode)"
											+ " values (" + objNumericParameter.getNtestgrouptestnumericcode() + ","
											+ objParameter.getNtestgrouptestparametercode() + "," + " case when N'"
											+ objNumericParameter.getSminlod() + "'='null' then NULL else N'"
											+ objNumericParameter.getSminlod() + "' end, case when N'"
											+ objNumericParameter.getSmaxlod() + "'='null' then NULL else N'"
											+ objNumericParameter.getSmaxlod() + "' end, case when N'"
											+ objNumericParameter.getSminb() + "'='null' then NULL else N'"
											+ objNumericParameter.getSminb() + "' end, case when N'"
											+ objNumericParameter.getSmina() + "'='null' then NULL else N'"
											+ objNumericParameter.getSmina() + "' end, case when N'"
											+ objNumericParameter.getSmaxa() + "'='null' then NULL else N'"
											+ objNumericParameter.getSmaxa() + "' end, case when N'"
											+ objNumericParameter.getSmaxb() + "'='null' then NULL else N'"
											+ objNumericParameter.getSmaxb() + "' end, case when N'"
											+ objNumericParameter.getSminloq() + "'='null' then NULL else N'"
											+ objNumericParameter.getSminloq() + "' end, case when N'"
											+ objNumericParameter.getSmaxloq() + "'='null' then NULL else N'"
											+ objNumericParameter.getSmaxloq() + "' end, case when N'"
											+ objNumericParameter.getSdisregard() + "'='null' then NULL else N'"
											+ objNumericParameter.getSdisregard() + "' end, case when N'"
											+ objNumericParameter.getSresultvalue() + "'='null' then NULL else N'"
											+ objNumericParameter.getSresultvalue() + "' end, " + "'"
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
											+ objUserInfo.getNmastersitecode() + ","
											+ objNumericParameter.getNgradecode() + ");";
									insertQueryArray.add(sNumericInsertQuery);
									insertQueryString += sNumericInsertQuery;
									insertQueryArray.add("update seqnotestgroupmanagement set nsequenceno = "
											+ ((int) listSeqNo.get("testgrouptestnumericparameter") + 1)
											+ " where stablename = 'testgrouptestnumericparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
									insertQueryString += "update seqnotestgroupmanagement set nsequenceno = "
											+ ((int) listSeqNo.get("testgrouptestnumericparameter") + 1)
											+ " where stablename = 'testgrouptestnumericparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
									lstDefaultResultObject.add(objNumericParameter);
									defaultResultMultilingualId.add("IDS_DEFALTSPECLIMIT");
								} else {
									final String sUpdateQry = "update testgrouptestnumericparameter set"
											+ " sminlod = case when N'" + objNumericParameter.getSminlod()
											+ "'='null' then NULL else N'" + objNumericParameter.getSminlod() + "' end,"
											+ " smaxlod = case when N'" + objNumericParameter.getSmaxlod()
											+ "'='null' then NULL else N'" + objNumericParameter.getSmaxlod() + "' end,"
											+ " sminb = case when N'" + objNumericParameter.getSminb()
											+ "'='null' then NULL else N'" + objNumericParameter.getSminb() + "' end,"
											+ " smina = case when N'" + objNumericParameter.getSmina()
											+ "'='null' then NULL else N'" + objNumericParameter.getSmina() + "' end,"
											+ " smaxa = case when N'" + objNumericParameter.getSmaxa()
											+ "'='null' then NULL else N'" + objNumericParameter.getSmaxa() + "' end,"
											+ " smaxb = case when N'" + objNumericParameter.getSmaxb()
											+ "'='null' then NULL else N'" + objNumericParameter.getSmaxb() + "' end,"
											+ " sminloq = case when N'" + objNumericParameter.getSminloq()
											+ "'='null' then NULL else N'" + objNumericParameter.getSminloq() + "' end,"
											+ " smaxloq = case when N'" + objNumericParameter.getSmaxloq()
											+ "'='null' then NULL else N'" + objNumericParameter.getSmaxloq() + "' end,"
											+ " sdisregard = case when N'" + objNumericParameter.getSdisregard()
											+ "'='null' then NULL else N'" + objNumericParameter.getSdisregard()
											+ "' end," + " sresultvalue = case when N'"
											+ objNumericParameter.getSresultvalue() + "'='null' then NULL else N'"
											+ objNumericParameter.getSresultvalue() + "' end," + "ngradecode="
											+ objNumericParameter.getNgradecode() + ", dmodifieddate='"
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
											+ " where ntestgrouptestnumericcode = "
											+ listCheckNumeric.get(0).getNtestgrouptestnumericcode() + ";";
									insertQueryArray.add(sUpdateQry);
									insertQueryString += sUpdateQry;
									lstNewObject.add(objNumericParameter);
									lstOldObject.add(listCheckNumeric.get(0));
									multilingualIDList.add("IDS_DEFALTSPECLIMIT");
								}
							}

							if (objParameter.getNparametertypecode() == Enumeration.ParameterType.NUMERIC
									.getparametertype()) {
								final List<TestGroupTestFormula> listFormula = (List<TestGroupTestFormula>) mapParameterDetails
										.get("TestGroupTestFormula");
								if (testGroupTestFormula != null) {
									if (!listFormula.isEmpty()) {
										final String sUpdateQuery = "update testgrouptestformula set ntestformulacode = "
												+ testGroupTestFormula.getNtestformulacode() + ","
												+ " sformulacalculationcode = N'"
												+ stringUtilityFunction
														.replaceQuote(testGroupTestFormula.getSformulacalculationcode())
												+ "'," + " sformulacalculationdetail = N'"
												+ stringUtilityFunction.replaceQuote(
														testGroupTestFormula.getSformulacalculationdetail())
												+ "'," + "dmodifieddate='"
												+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
												+ " where ntestgrouptestformulacode = "
												+ listFormula.get(0).getNtestgrouptestformulacode();
										insertQueryArray.add(sUpdateQuery);
										insertQueryString += sUpdateQuery;
										lstNewObject.add(testGroupTestFormula);
										lstOldObject.add(listFormula.get(0));
										multilingualIDList.add("IDS_EDITFORMULA");
									} else {
										testGroupTestFormula.setNtestgrouptestformulacode(
												(int) listSeqNo.get("testgrouptestformula") + 1);
										// ALPD-4070---------NASRIN---------modified-----08-MAY-2025------nmodifieddate---to---dmodifieddate-----in
										// the below query
										final String sInsertQry = "insert into testgrouptestformula (ntestgrouptestformulacode, ntestgrouptestcode, ntestgrouptestparametercode,"
												+ " ntestformulacode, sformulacalculationcode, sformulacalculationdetail, ntransactionstatus,dmodifieddate, nstatus,nsitecode)"
												+ " values (" + testGroupTestFormula.getNtestgrouptestformulacode()
												+ ", " + objParameter.getNtestgrouptestcode() + ", "
												+ objParameter.getNtestgrouptestparametercode() + ", "
												+ testGroupTestFormula.getNtestformulacode() + ", N'"
												+ stringUtilityFunction
														.replaceQuote(testGroupTestFormula.getSformulacalculationcode())
												+ "'," + " N'" + testGroupTestFormula.getSformulacalculationdetail()
												+ "', " + Enumeration.TransactionStatus.YES.gettransactionstatus()
												+ ", " + "'" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
												+ "'," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ "," + objUserInfo.getNmastersitecode() + ");";
										insertQueryArray.add(sInsertQry);
										insertQueryString += sInsertQry;
										insertQueryArray.add("update seqnotestgroupmanagement set nsequenceno = "
												+ ((int) listSeqNo.get("testgrouptestformula") + 1)
												+ " where stablename = 'testgrouptestformula' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
										insertQueryString += "update seqnotestgroupmanagement set nsequenceno = "
												+ ((int) listSeqNo.get("testgrouptestformula") + 1)
												+ " where stablename = 'testgrouptestformula' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
									}
								} else {
									if (!listFormula.isEmpty()) {
										final String sDeleteQry = "update testgrouptestformula set nstatus = "
												+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
												+ "dmodifieddate='"
												+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
												+ " where ntestgrouptestformulacode = "
												+ listFormula.get(0).getNtestgrouptestformulacode();
										insertQueryArray.add(sDeleteQry);
										insertQueryString += sDeleteQry;
										lstDefaultResultObject.add(listFormula.get(0));
										defaultResultMultilingualId.add("IDS_DELETEFORMULA");
									}
								}
							}

							// Insert/Update of Char Parameter default result
							if (objCharParameter != null) {
								final List<TestGroupTestCharParameter> listCheckChar = (List<TestGroupTestCharParameter>) mapParameterDetails
										.get("TestGroupTestCharParameter");
								if (listCheckChar.isEmpty()) {
									objCharParameter.setNtestgrouptestcharcode(
											(int) listSeqNo.get("testgrouptestcharparameter") + 1);
									if (objCharParameter.getScharname() == null) {
										objCharParameter.setScharname("");
									}
									final String sCharInsertQuery = "insert into testgrouptestcharparameter (ntestgrouptestcharcode, ntestgrouptestparametercode, scharname,dmodifieddate, nstatus,nsitecode)"
											+ " values (" + ((int) listSeqNo.get("testgrouptestcharparameter") + 1)
											+ "," + objCharParameter.getNtestgrouptestparametercode() + ",N'"
											+ stringUtilityFunction.replaceQuote(objCharParameter.getScharname()) + "',"
											+ "'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
											+ objUserInfo.getNmastersitecode() + ");";
									insertQueryArray.add(sCharInsertQuery);
									insertQueryString += sCharInsertQuery;
									insertQueryArray.add("update seqnotestgroupmanagement set nsequenceno = "
											+ ((int) listSeqNo.get("testgrouptestcharparameter") + 1)
											+ " where stablename = 'testgrouptestcharparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
									insertQueryString += "update seqnotestgroupmanagement set nsequenceno = "
											+ ((int) listSeqNo.get("testgrouptestcharparameter") + 1)
											+ " where stablename = 'testgrouptestcharparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
									lstDefaultResultObject.add(objCharParameter);
									defaultResultMultilingualId.add("IDS_DEFAULTCHARACTERRESULT");
								} else {
									final String sUpdateQry = "update testgrouptestcharparameter set scharname = N'"
											+ stringUtilityFunction.replaceQuote(objCharParameter.getScharname()) + "',"
											+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
											+ "'" + " where ntestgrouptestcharcode = "
											+ listCheckChar.get(0).getNtestgrouptestcharcode() + ";";
									insertQueryArray.add(sUpdateQry);
									insertQueryString += sUpdateQry;
									lstNewObject.add(objCharParameter);
									lstOldObject.add(listCheckChar.get(0));
									multilingualIDList.add("IDS_DEFAULTCHARACTERRESULT");
								}
							}

							// Insert/Update of Predefined Parameter default result
							if (objPredefinedParameter != null) {
								final List<TestGroupTestPredefinedParameter> listCheckPredefine = (List<TestGroupTestPredefinedParameter>) mapParameterDetails
										.get("TestGroupTestPredefinedParameter");
								if (listCheckPredefine.isEmpty()) {
									final TestGroupTestPredefinedParameter objPredefParameter = testGroupCommonFunction
											.getPredefinedParameterByName(objPredefinedParameter);
									if (objPredefParameter == null) {
										final String sDefaultQry = "select ntestgrouptestpredefcode from testgrouptestpredefparameter"
												+ " where ntestgrouptestparametercode = "
												+ objPredefinedParameter.getNtestgrouptestparametercode()
												+ " and ndefaultstatus = "
												+ Enumeration.TransactionStatus.YES.gettransactionstatus()
												+ " and nstatus = "
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and nsitecode=" + objUserInfo.getNmastersitecode();
										final TestGroupTestPredefinedParameter objDefaultPredefined = (TestGroupTestPredefinedParameter) jdbcUtilityFunction
												.queryForObject(sDefaultQry, TestGroupTestPredefinedParameter.class,
														jdbcTemplate);
										if (objDefaultPredefined == null) {
											objPredefinedParameter.setNdefaultstatus(
													(short) Enumeration.TransactionStatus.YES.gettransactionstatus());
										}
										objPredefinedParameter.setNtestgrouptestpredefcode(
												(int) listSeqNo.get("testgrouptestpredefparameter") + 1);
										final String sPredefInsertQuery = testGroupCommonFunction
												.insertPredefinedParameterQuery(objPredefinedParameter, objUserInfo);
										insertQueryArray.add(sPredefInsertQuery);
										insertQueryString += sPredefInsertQuery;
										insertQueryArray.add("update seqnotestgroupmanagement set nsequenceno = "
												+ ((int) listSeqNo.get("testgrouptestpredefparameter") + 1)
												+ " where stablename = 'testgrouptestpredefparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";");
										insertQueryString += "update seqnotestgroupmanagement set nsequenceno = "
												+ ((int) listSeqNo.get("testgrouptestpredefparameter") + 1)
												+ " where stablename = 'testgrouptestpredefparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
										lstDefaultResultObject.add(objPredefinedParameter);
										defaultResultMultilingualId.add("IDS_DEFAULTCODEDRESULT");
									} else {
										return new ResponseEntity<>(commonFunction.getMultilingualMessage(
												Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
												objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
									}
								} else {
									final TestGroupTestPredefinedParameter objPredefParameter = testGroupCommonFunction
											.getPredefinedObjectByName(objPredefinedParameter);
									if (objPredefParameter == null) {
										insertQueryArray.add(testGroupCommonFunction
												.updatePredefinedParameterQuery(objPredefinedParameter, objUserInfo));
										insertQueryString += testGroupCommonFunction
												.updatePredefinedParameterQuery(objPredefinedParameter, objUserInfo);
										lstNewObject.add(objPredefinedParameter);
										lstOldObject.add(listCheckPredefine.get(0));
										multilingualIDList.add("IDS_DEFAULTCODEDRESULT");
									} else {
										return new ResponseEntity<>(commonFunction.getMultilingualMessage(
												Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
												objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
									}
								}
							}
							jdbcTemplate.execute(insertQueryString);

							auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList,
									objUserInfo);
							if (!lstDefaultResultObject.isEmpty()) {
								auditUtilityFunction.fnInsertAuditAction(lstDefaultResultObject, 1, null,
										defaultResultMultilingualId, objUserInfo);
							}

							return testGroupCommonFunction.getTestGroupParameter(objUserInfo, objParameter, false);
						} else {
							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_PARAMETERALREADYDELETED",
											objUserInfo.getSlanguagefilename()),
									HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED",
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
									objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		}
	}

	private TestGroupTest checkTestIsPresent(final int ntestgrouptestcode) throws Exception {
		final String sQuery = "select * from testgrouptest where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestgrouptestcode = "
				+ ntestgrouptestcode;
		return (TestGroupTest) jdbcUtilityFunction.queryForObject(sQuery, TestGroupTest.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> viewTestGroupTestFile(final int ntestgroupfilecode, final UserInfo objUserInfo,
			final TestGroupSpecification objSpecification, final int ntestgrouptestcode) throws Exception {
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
				Map<String, Object> map = new HashMap<String, Object>();
				final TestGroupTest objTestGroupTest = checkTestIsPresent(ntestgrouptestcode);
				// ntestgroupfilecode);
				if (objTestGroupTest != null) {
					String sQuery = "select * from testgrouptestfile where nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestgroupfilecode = "
							+ ntestgroupfilecode;
					final TestGroupTestFile objTGTF = (TestGroupTestFile) jdbcUtilityFunction.queryForObject(sQuery,
							TestGroupTestFile.class, jdbcTemplate);
					if (objTGTF != null) {
						if (objTGTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							UserInfo userInfo = new UserInfo(objUserInfo);
							userInfo.setNformcode((short) Enumeration.FormCode.TESTMASTER.getFormCode());
							map = ftpUtilityFunction.FileViewUsingFtp(objTGTF.getSsystemfilename(), -1, userInfo, "",
									"");
							// Folder Name - test master
						} else {

							sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
									+ objTGTF.getNlinkcode() + " and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery,
									LinkMaster.class, jdbcTemplate);
							map.put("AttachLink", objlinkmaster.getSlinkname() + objTGTF.getSfilename());
						}
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstObject = new ArrayList<>();
						multilingualIDList
								.add(objTGTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
										? "IDS_VIEWTESTGROUPFILE"
										: "IDS_VIEWTESTGROUPLINK");
						lstObject.add(objTGTF);
						auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
						return new ResponseEntity<>(map, HttpStatus.OK);
					} else {
						// status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

}
