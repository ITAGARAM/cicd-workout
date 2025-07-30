package com.agaramtech.qualis.testgroup.service.testgrouptestparameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.TreeVersionTemplate;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TestGroupCommonFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestCharParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestClinicalSpec;
import com.agaramtech.qualis.testgroup.model.TestGroupTestNumericParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedSubCode;
import com.agaramtech.qualis.testmanagement.model.TestFormula;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TestGroupParameterDAOImpl implements TestGroupParameterDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestGroupParameterDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private TestGroupCommonFunction testGroupCommonFunction;

	@Override
	public ResponseEntity<Object> getActiveTestParameterById(final UserInfo objUserInfo,
			final TestGroupTestParameter objTestParameter) throws Exception {
		final String sQuery = "select tgtp.nparametertypecode, pt.sdisplaystatus, tgtp.nresultmandatory, ts1.stransdisplaystatus as sresultmandatory,"
				+ " tgtp.nreportmandatory, ts2.stransdisplaystatus as sreportmandatory, tgtp.nunitcode, u.sunitname, tgtp.nchecklistversioncode, cv.schecklistversionname,"
				+ " tgtp.ntestgrouptestcode, tgtp.ntestgrouptestparametercode, tgtp.ntestparametercode, tgtp.sparametersynonym, tgtp.sspecdesc,tgtp.nresultaccuracycode,ra.sresultaccuracyname "
				+ " from testgrouptestparameter tgtp, checklist cl, checklistversion cv, unit u, transactionstatus ts1, transactionstatus ts2, parametertype pt,resultaccuracy ra "
				+ " where cl.nchecklistcode = cv.nchecklistcode and cv.nchecklistversioncode = tgtp.nchecklistversioncode and u.nunitcode = tgtp.nunitcode"
				+ " and ts1.ntranscode = tgtp.nresultmandatory and ts2.ntranscode = tgtp.nreportmandatory and pt.nparametertypecode = tgtp.nparametertypecode"
				+ " and ra.nresultaccuracycode=tgp.nresultaccuracycode and tgtp.nstatus = cl.nstatus and tgtp.nstatus = cv.nstatus and tgtp.nstatus = u.nstatus and tgtp.nstatus = ts1.nstatus "
				+ " and tgtp.nstatus = ts2.nstatus and ra.nstatus=tgp.nstatus "
				+ " and tgtp.nstatus = pt.nstatus and tgtp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tgtp.ntestgrouptestparametercode = " + objTestParameter.getNtestgrouptestparametercode()
				+ " and cl.nsitecode=" + objUserInfo.getNmastersitecode() + " and cv.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and u.nsitecode=" + objUserInfo.getNmastersitecode()
				+ " and pt.nsitecode=" + objUserInfo.getNmastersitecode() + " and ra.nsitecode="
				+ objUserInfo.getNmastersitecode();
		LOGGER.info(sQuery);
		final TestGroupTestParameter objTGTP = (TestGroupTestParameter) jdbcUtilityFunction.queryForObject(sQuery,
				TestGroupTestParameter.class, jdbcTemplate);
		if (objTGTP != null) {
			return new ResponseEntity<Object>(objTGTP, HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public Map<String, Object> getUpdateSequenceParameter(UserInfo objUserInfo, TestGroupTestParameter objTestParameter,
			TestGroupSpecification objTestGroupSpec) throws Exception {
		final String Query = " lock  table locktestgroup " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(Query);
		final TestGroupSpecification objTestGroupSpecification = testGroupCommonFunction
				.getActiveSpecification(objTestGroupSpec.getNallottedspeccode(), objUserInfo);
		String returnStr = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		if (objTestGroupSpecification != null) {
			if (objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()
					|| objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
							.gettransactionstatus()) {
				String sQuery = "select * from testgrouptestparameter where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ntestgrouptestparametercode = " + objTestParameter.getNtestgrouptestparametercode();
				TestGroupTestParameter objTestGroupParameter = (TestGroupTestParameter) jdbcUtilityFunction
						.queryForObject(sQuery, TestGroupTestParameter.class, jdbcTemplate);
				if (objTestGroupParameter != null) {
					if (objTestGroupParameter.getNparametertypecode() != objTestParameter.getNparametertypecode()) {
						String stablename = "";
						if (objTestParameter.getNparametertypecode() == Enumeration.ParameterType.CHARACTER
								.getparametertype()) {
							stablename = "testgrouptestcharparameter";
						} else if (objTestParameter.getNparametertypecode() == Enumeration.ParameterType.NUMERIC
								.getparametertype()) {
							stablename = "testgrouptestnumericparameter";
						} else if (objTestParameter.getNparametertypecode() == Enumeration.ParameterType.ATTACHEMENT
								.getparametertype()) {
							stablename = "testgrouptestattchmentparameter";
						}
						if (stablename.isEmpty()) {
							sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename = '"
									+ stablename + "') and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							Integer seqNo = jdbcTemplate.queryForObject(sQuery, Integer.class);
							outputMap.put("SeqNo", seqNo);
							sQuery = "update seqnotestgroupmanagement set nsequenceno = " + (seqNo + 1)
									+ " where stablename = '" + stablename + "' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
						}
					}
				} else {
					returnStr = commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename());
				}
			} else {
				returnStr = commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
						objUserInfo.getSlanguagefilename());
			}
		} else {
			returnStr = commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED",
					objUserInfo.getSlanguagefilename());
		}
		outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), returnStr);
		return outputMap;
	}

	@Override
	public ResponseEntity<Object> updateParameter(UserInfo objUserInfo, TestGroupTestParameter objTestParameter,
			final TestGroupSpecification objTestGroupSpec, Map<String, Object> inputMap) throws Exception {
		final String sQuery = "select * from testgrouptestparameter where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestgrouptestparametercode = "
				+ objTestParameter.getNtestgrouptestparametercode();
		TestGroupTestParameter objTestGroupParameter = (TestGroupTestParameter) jdbcUtilityFunction
				.queryForObject(sQuery, TestGroupTestParameter.class, jdbcTemplate);
		if (objTestGroupParameter != null) {
			final StringBuilder sb = new StringBuilder();
			final String sInsertQuery = "update testgrouptestparameter set nparametertypecode = "
					+ objTestParameter.getNparametertypecode() + ", nunitcode = " + objTestParameter.getNunitcode()
					+ ", sparametersynonym = N'"
					+ stringUtilityFunction.replaceQuote(objTestParameter.getSparametersynonym()) + "',"
					+ " nroundingdigits = " + objTestParameter.getNroundingdigits() + ", nresultmandatory = "
					+ objTestParameter.getNresultmandatory() + "," + "nreportmandatory = "
					+ objTestParameter.getNreportmandatory() + ", nchecklistversioncode = "
					+ objTestParameter.getNchecklistversioncode() + ", sspecdesc = '"
					+ stringUtilityFunction.replaceQuote(objTestParameter.getSspecdesc()) + "', nsorter = "
					+ objTestParameter.getNsorter() + ",dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',nresultaccuracycode= "
					+ objTestParameter.getNresultaccuracycode() + " where ntestgrouptestparametercode = "
					+ objTestParameter.getNtestgrouptestparametercode() + ";";
			sb.append(sInsertQuery);
			if (objTestGroupParameter.getNparametertypecode() != objTestParameter.getNparametertypecode()) {
				if (objTestParameter.getNparametertypecode() == Enumeration.ParameterType.CHARACTER
						.getparametertype()) {
					sb.append(
							"insert into testgrouptestcharparameter (ntestgrouptestcharcode, ntestgrouptestparametercode, scharname,dmodifieddate, nstatus,nsitecode)"
									+ " values (" + ((int) inputMap.get("SeqNo") + 1) + ", "
									+ objTestParameter.getNtestgrouptestparametercode() + ", NULL,'"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ objUserInfo.getNmastersitecode() + ");");
				} else if (objTestParameter.getNparametertypecode() == Enumeration.ParameterType.NUMERIC
						.getparametertype()) {
					sb.append(
							"insert into testgrouptestnumericparameter (ntestgrouptestnumericcode, ntestgrouptestparametercode, sminlod, smaxlod,"
									+ "sminb, smina, smaxa, smaxb, sminloq, smaxloq, sdisregard, sresultvalue,dmodifieddate, nstatus,nsitecode) "
									+ "values (" + ((int) inputMap.get("SeqNo") + 1) + ", "
									+ objTestParameter.getNtestgrouptestparametercode() + ", NULL, NULL, NULL, NULL"
									+ ", NULL, NULL, NULL, NULL, NULL, NULL,'"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
									+ objUserInfo.getNmastersitecode() + ");");
				} else if (objTestParameter.getNparametertypecode() == Enumeration.ParameterType.ATTACHEMENT
						.getparametertype()) {
					// Insert Query of testgrouptest attchment
				}
				// To update the oldparameter type status as -1
				if (objTestGroupParameter.getNparametertypecode() == Enumeration.ParameterType.NUMERIC
						.getparametertype()) {
					sb.append("update testgrouptestnumericparameter set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
							+ " where ntestgrouptestparametercode = "
							+ objTestParameter.getNtestgrouptestparametercode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");
				} else if (objTestGroupParameter.getNparametertypecode() == Enumeration.ParameterType.PREDEFINED
						.getparametertype()) {
					sb.append("update testgrouptestpredefinedparameter set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
							+ " where ntestgrouptestparametercode = "
							+ objTestParameter.getNtestgrouptestparametercode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");
				} else if (objTestGroupParameter.getNparametertypecode() == Enumeration.ParameterType.CHARACTER
						.getparametertype()) {
					sb.append("update testgrouptestcharparameter set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
							+ " where ntestgrouptestparametercode = "
							+ objTestParameter.getNtestgrouptestparametercode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");
				} else if (objTestGroupParameter.getNparametertypecode() == Enumeration.ParameterType.ATTACHEMENT
						.getparametertype()) {
					sb.append("update testgrouptestattachmentparameter set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
							+ " where ntestgrouptestparametercode = "
							+ objTestParameter.getNtestgrouptestparametercode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");
				}
			}
			jdbcTemplate.execute(sb.toString());
			List<Object> lstnewobject = new ArrayList<Object>();
			List<Object> lstoldobject = new ArrayList<Object>();
			lstnewobject.add(objTestParameter);
			lstoldobject.add(objTestGroupParameter);
			auditUtilityFunction.fnInsertAuditAction(lstnewobject, 2, lstoldobject,
					Arrays.asList("IDS_EDITTESTPARAMETER"), objUserInfo);
			return new ResponseEntity<Object>(
					getParameterByTestId(objUserInfo, objTestParameter.getNtestgrouptestcode()).getBody(),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getParameterByTestId(final UserInfo objUserInfo, final int ntestgrouptestcode)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<TestGroupTestParameter> listTestParameter = testGroupCommonFunction
				.getTestGroupTestParameter(ntestgrouptestcode, 0, objUserInfo);
		if (!listTestParameter.isEmpty()) {
			outputMap.putAll(
					testGroupCommonFunction.getTestGroupParameterDetails(listTestParameter.get(0), objUserInfo));
		}
		outputMap.put("TestGroupTestParameter", listTestParameter);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActivePredefinedParameterById(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefinedParameter,
			final TestGroupSpecification objSpecification) throws Exception {
		final TestGroupSpecification objTestGroupSpecification = testGroupCommonFunction
				.getActiveSpecification(objSpecification.getNallottedspeccode(), objUserInfo);
		if (objTestGroupSpecification != null) {
			if (objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()
					|| objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
							.gettransactionstatus()) {
				final String sPredefQry = "select tgtpp.spredefinedname,tgtpp.spredefinedsynonym,case when  tgtpp.spredefinedcomments='null' then '' else tgtpp.spredefinedcomments end spredefinedcomments ,tgtpp.ntestgrouptestparametercode, tgtpp.ntestgrouptestpredefcode,tgtpp.nneedresultentryalert,tgtpp.nneedsubcodedresult,case when  tgtpp.salertmessage='null' then '' else tgtpp.salertmessage end salertmessage, "
						+ " coalesce(g.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode()
						+ "',g.jsondata->'sdisplayname'->>'en-US') sdisplaystatus,tgtpp.ngradecode"
						+ " from testgrouptestpredefparameter tgtpp, grade g where g.ngradecode = tgtpp.ngradecode and g.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgtpp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ntestgrouptestpredefcode = " + objPredefinedParameter.getNtestgrouptestpredefcode();
				final List<TestGroupTestPredefinedParameter> listNumericParameter = (List<TestGroupTestPredefinedParameter>) jdbcTemplate
						.query(sPredefQry, new TestGroupTestPredefinedParameter());
				if (!listNumericParameter.isEmpty()) {
					return new ResponseEntity<Object>(
							commonFunction.getMultilingualMessageList(listNumericParameter,
									Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()).get(0),
							HttpStatus.OK);
				} else {
					return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<Object>(
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

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getActiveParameterById(final UserInfo objUserInfo,
			final TestGroupTestParameter objParameter, final int ntreeversiontempcode) throws Exception {

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String strstatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

		final TestGroupSpecification objTestGroupSpecification = testGroupCommonFunction
				.getActiveSpecByTestId(objParameter.getNtestgrouptestcode());

		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);
		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			if (objTestGroupSpecification != null) {

				if (objParameter.getNprojectmastercode() != -1) {
					map = (Map<String, Object>) testGroupCommonFunction
							.getProjectMasterStatusCode(objParameter.getNprojectmastercode(), objUserInfo);
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
						final TestGroupTest objTestGroupTest = testGroupCommonFunction
								.getActiveTest(objParameter.getNtestgrouptestcode());
						if (objTestGroupTest != null) {
							final Map<String, Object> outputMap = new HashMap<String, Object>();
							final String sUpdateQry = "select tgtp.nisadhocparameter,tgtp.ntestgrouptestcode, tgtp.sparametersynonym, tgtp.nroundingdigits, tgtp.nresultmandatory, tgtp.nreportmandatory,"
									+ " tgtp.sspecdesc, tgtp.nsorter, tgtp.nunitcode, u.sunitname, pt.sdisplaystatus, tgtp.nparametertypecode,"
									+ " cl.schecklistname, cv.schecklistversionname, cv.nchecklistversioncode, tgtp.ntestgrouptestparametercode,"
									+ " tgtf.sformulacalculationdetail, tgtf.ntestformulacode, tgtf.sformulacalculationcode,tf.sformulaname,tgtp.nresultaccuracycode,ra.sresultaccuracyname "
									+ " from testgrouptestparameter tgtp left outer join testgrouptestformula tgtf"
									+ " on tgtp.ntestgrouptestparametercode = tgtf.ntestgrouptestparametercode"
									+ " and tgtf.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgtf.nsitecode=" + objUserInfo.getNmastersitecode()
									+ " left outer join testformula tf on tf.ntestformulacode=tgtf.ntestformulacode and tf.nstatus="
									+ " " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tf.nsitecode=" + objUserInfo.getNmastersitecode() + " and tgtf.nstatus="
									+ " " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ ",checklist cl,"
									+ " checklistversion cv, unit u, parametertype pt,resultaccuracy ra "
									+ " where u.nunitcode = tgtp.nunitcode and pt.nparametertypecode = tgtp.nparametertypecode and ra.nresultaccuracycode=tgtp.nresultaccuracycode "
									+ " and cl.nchecklistcode = cv.nchecklistcode and cv.nchecklistversioncode = tgtp.nchecklistversioncode"
									+ " and cl.nstatus = tgtp.nstatus and cv.nstatus = tgtp.nstatus and u.nstatus = tgtp.nstatus and ra.nstatus=tgtp.nstatus "
									+ " and pt.nstatus = tgtp.nstatus and tgtp.ntestgrouptestparametercode = "
									+ objParameter.getNtestgrouptestparametercode() + " and tgtp.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cl.nsitecode="
									+ objUserInfo.getNmastersitecode() + " and u.nsitecode="
									+ objUserInfo.getNmastersitecode() + " and pt.nsitecode="
									+ objUserInfo.getNmastersitecode() + " and ra.nsitecode="
									+ objUserInfo.getNmastersitecode();
							final List<TestGroupTestParameter> listTestParameter = (List<TestGroupTestParameter>) jdbcTemplate
									.query(sUpdateQry, new TestGroupTestParameter());
							if (!listTestParameter.isEmpty()) {
								if (listTestParameter.get(0)
										.getNparametertypecode() == Enumeration.ParameterType.PREDEFINED
												.getparametertype()) {
									final String sPredefQry = "select tgtpp.spredefinedname,tgtpp.spredefinedsynonym, tgtpp.ndefaultstatus, tgtpp.ntestgrouptestparametercode, tgtpp.ntestgrouptestpredefcode, "
											+ " coalesce(g.jsondata->'sdisplayname'->>'"
											+ objUserInfo.getSlanguagetypecode()
											+ "',g.jsondata->'sdisplayname'->>'en-US') sdisplaystatus, tgtpp.ngradecode,tgtpp.nneedresultentryalert,tgtpp.nneedsubcodedresult "
											+ " from testgrouptestpredefparameter tgtpp, grade g where g.ngradecode = tgtpp.ngradecode "
											+ " and g.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and tgtpp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and tgtpp.ndefaultstatus = "
											+ Enumeration.TransactionStatus.YES.gettransactionstatus()
											+ " and tgtpp.ntestgrouptestparametercode = "
											+ objParameter.getNtestgrouptestparametercode() + " and tgtpp.nsitecode="
											+ objUserInfo.getNmastersitecode();

									final List<TestGroupTestPredefinedParameter> listNumericParameter = (List<TestGroupTestPredefinedParameter>) commonFunction
											.getMultilingualMessageList(
													jdbcTemplate.query(sPredefQry,
															new TestGroupTestPredefinedParameter()),
													Arrays.asList("sdisplaystatus"),
													objUserInfo.getSlanguagefilename());
									if (!listNumericParameter.isEmpty()) {
										outputMap.put("TestGroupTestPredefinedParameter", listNumericParameter.get(0));
									}
								} else if (listTestParameter.get(0)
										.getNparametertypecode() == Enumeration.ParameterType.NUMERIC
												.getparametertype()) {

									final String sNumericQry = "select tgtnp.*,coalesce(g.jsondata->'sdisplayname'->>'"
											+ objUserInfo.getSlanguagetypecode()
											+ "',g.jsondata->'sdisplayname'->>'en-US') sgradename from testgrouptestnumericparameter tgtnp,grade g where tgtnp.ntestgrouptestparametercode = "
											+ objParameter.getNtestgrouptestparametercode()
											+ " and tgtnp.ngradecode=g.ngradecode and g.nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and tgtnp.nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and tgtnp.nsitecode=" + objUserInfo.getNmastersitecode()+ ";";
									List<TestGroupTestNumericParameter> lstTestGroupTestNumericParameter = jdbcTemplate
											.query(sNumericQry, new TestGroupTestNumericParameter());

									final String sFormulaQry = "select * from testformula where ntestparametercode = "
											+ objParameter.getNtestparametercode() + " and nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and nsitecode=" + objUserInfo.getNmastersitecode() + ";";

									List<TestFormula> lstTestFormula = jdbcTemplate.query(sFormulaQry,
											new TestFormula());
									Map<String, Object> mapList = new HashMap<String, Object>();
									mapList.put("TestGroupTestNumericParameter", lstTestGroupTestNumericParameter);
									mapList.put("TestFormula", lstTestFormula);

									outputMap.putAll(mapList);
								} else if (listTestParameter.get(0)
										.getNparametertypecode() == Enumeration.ParameterType.CHARACTER
												.getparametertype()) {
									final String sCharQry = "select * from testgrouptestcharparameter where ntestgrouptestparametercode = "
											+ objParameter.getNtestgrouptestparametercode() + " and nstatus = "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and nsitecode=" + objUserInfo.getNmastersitecode();
									final TestGroupTestCharParameter objCharParameter = (TestGroupTestCharParameter) jdbcUtilityFunction
											.queryForObject(sCharQry, TestGroupTestCharParameter.class, jdbcTemplate);
									outputMap.put("TestGroupTestCharParameter", objCharParameter);
								} else if (listTestParameter.get(0)
										.getNparametertypecode() == Enumeration.ParameterType.ATTACHEMENT
												.getparametertype()) {
									// Need to write code for attachment
								}
								outputMap.put("TestGroupTestParameter",
										commonFunction.getMultilingualMessageList(listTestParameter,
												Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()));
								return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
							} else {
								return new ResponseEntity<Object>(
										commonFunction.getMultilingualMessage("IDS_PARAMETERALREADYDELETED",
												objUserInfo.getSlanguagefilename()),
										HttpStatus.EXPECTATION_FAILED);
							}

						} else {
							return new ResponseEntity<Object>(
									commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED",
											objUserInfo.getSlanguagefilename()),
									HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						return new ResponseEntity<Object>(
								commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}

				}

			} else {
				return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> createTestGroupPredefParameter(UserInfo objUserInfo,
			TestGroupTestPredefinedParameter objPredefParameter, TestGroupSpecification objGroupSpecification,
			List<TestGroupTestPredefinedSubCode> lstCodedResult) throws Exception {
		List<TestGroupTestPredefinedSubCode> lstCodedResultAudit = new ArrayList<>();
		final String Query = " lock  table locktestgroup " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(Query);
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objGroupSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
				final TestGroupTestPredefinedParameter objPredefinedParameter = testGroupCommonFunction
						.getPredefinedParameterByName(objPredefParameter);
				if (objPredefinedParameter == null) {
					final String sDefaultQry = "select ntestgrouptestpredefcode from testgrouptestpredefparameter"
							+ " where ntestgrouptestparametercode = "
							+ objPredefParameter.getNtestgrouptestparametercode() + " and ndefaultstatus = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ objUserInfo.getNmastersitecode();
					final TestGroupTestPredefinedParameter objDefaultPredefined = (TestGroupTestPredefinedParameter) jdbcUtilityFunction
							.queryForObject(sDefaultQry, TestGroupTestPredefinedParameter.class, jdbcTemplate);
					if (objDefaultPredefined == null) {
						objPredefParameter
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					}
					final String sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename = 'testgrouptestpredefparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
					final int nSeqNo = jdbcTemplate.queryForObject(sQuery, Integer.class) + 1;
					objPredefParameter.setNtestgrouptestpredefcode(nSeqNo);
					String sInsertQuery = testGroupCommonFunction.insertPredefinedParameterQuery(objPredefParameter,
							objUserInfo);
					sInsertQuery = sInsertQuery + "update seqnotestgroupmanagement set nsequenceno = " + nSeqNo
							+ " where stablename = 'testgrouptestpredefparameter' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
					jdbcTemplate.execute(sInsertQuery);

					if (lstCodedResult.size() > 0) {

						final String subQuery = "select nsequenceno from seqnotestgroupmanagement where stablename = 'testgrouptestpredefsubresult' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
						int nSeqNosub = jdbcTemplate.queryForObject(subQuery, Integer.class);

						String partQuery = "";
						String queryString = "insert into testgrouptestpredefsubresult (ntestgrouptestpredefsubcode,ntestgrouptestpredefcode,ssubcodedresult,dmodifieddate "
								+ ",nsitecode,nstatus) " + " VALUES ";

						for (TestGroupTestPredefinedSubCode SubcodedResult : lstCodedResult) {
							SubcodedResult.setNtestgrouptestpredefsubcode(nSeqNosub + 1);
							partQuery = partQuery + "(" + SubcodedResult.getNtestgrouptestpredefsubcode() + ","
									+ objPredefParameter.getNtestgrouptestpredefcode() + ",'"
									+ SubcodedResult.getSsubcodedresult() + "','"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
									+ objUserInfo.getNmastersitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
							nSeqNosub = nSeqNosub + 1;
							SubcodedResult
									.setNtestgrouptestpredefcode(objPredefParameter.getNtestgrouptestpredefcode());
							lstCodedResultAudit.add(SubcodedResult);
						}
						jdbcTemplate.execute(queryString + partQuery.substring(0, partQuery.length() - 1));
						String UpQry = "update seqnotestgroupmanagement set nsequenceno=" + nSeqNosub
								+ " where stablename='testgrouptestpredefsubresult' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
						jdbcTemplate.execute(UpQry);
					}

					List<TestGroupTestPredefinedParameter> ins = new ArrayList<TestGroupTestPredefinedParameter>();
					ins.add(objPredefParameter);
					List<String> lstactionType = new ArrayList<String>();
					lstactionType.add("IDS_ADDTESTPREDEFINEDPARAMETER");
					List<List<?>> lstnewobject = new ArrayList<>();
					lstnewobject.add(ins);
					lstnewobject.add(lstCodedResultAudit);
					auditUtilityFunction.fnInsertListAuditAction(lstnewobject, 1, null,
							Arrays.asList("IDS_ADDTESTPREDEFINEDPARAMETER", "IDS_ADDSUBCODERESULT"), objUserInfo);

					return new ResponseEntity<Object>(testGroupCommonFunction.getTestGroupTestPredefinedParameter(
							objPredefParameter.getNtestgrouptestparametercode(), objUserInfo.getSlanguagefilename()),
							HttpStatus.OK);
				} else {
					return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateTestGroupPredefParameter(UserInfo objUserInfo,
			TestGroupTestPredefinedParameter objPredefParameter, TestGroupSpecification objGroupSpecification,
			List<TestGroupTestPredefinedSubCode> lstCodedResult, List<TestGroupTestPredefinedSubCode> lstaddCodedResult)
			throws Exception {
		List<TestGroupTestPredefinedSubCode> lstCodedResultAddAudit = new ArrayList<TestGroupTestPredefinedSubCode>();
		List<TestGroupTestPredefinedSubCode> lstCodedResultDeleteAudit = new ArrayList<TestGroupTestPredefinedSubCode>();
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objGroupSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
				final String sQuery = "select * from testgrouptestpredefparameter where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ntestgrouptestpredefcode = " + objPredefParameter.getNtestgrouptestpredefcode() + ";";
				final TestGroupTestPredefinedParameter objOldPredefParameter = (TestGroupTestPredefinedParameter) jdbcUtilityFunction
						.queryForObject(sQuery, TestGroupTestPredefinedParameter.class, jdbcTemplate);
				if (objOldPredefParameter != null) {
					final TestGroupTestPredefinedParameter objPredefinedParameter = testGroupCommonFunction
							.getPredefinedObjectByName(objPredefParameter);
					if (objPredefinedParameter == null) {
						jdbcTemplate.execute(testGroupCommonFunction
								.updatePredefinedParameterQuery(objPredefParameter, objUserInfo));
						List<String> lstactionType = new ArrayList<String>();
						lstactionType.add("IDS_EDITTESTPREDEFINEDPARAMETER");
						List<Object> lstNewObject = new ArrayList<Object>();
						List<Object> lstOldObject = new ArrayList<Object>();
						objPredefParameter.setNdefaultstatus(objOldPredefParameter.getNdefaultstatus());
						lstNewObject.add(objPredefParameter);
						lstOldObject.add(objOldPredefParameter);
						auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, lstactionType,
								objUserInfo);

						if (lstCodedResult.size() > 0 && lstCodedResult != null) {
							String ntestgrouptestpredefsubcode = "";
							String deletesubcode = "";

							for (TestGroupTestPredefinedSubCode SubcodedResult : lstCodedResult) {
								ntestgrouptestpredefsubcode = ntestgrouptestpredefsubcode
										+ SubcodedResult.getNtestgrouptestpredefsubcode() + ",";
								SubcodedResult
										.setNtestgrouptestpredefcode(objPredefParameter.getNtestgrouptestpredefcode());
								lstCodedResultDeleteAudit.add(SubcodedResult);
							}
							deletesubcode = "update testgrouptestpredefsubresult set nstatus="
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									+ " where  ntestgrouptestpredefsubcode in ("
									+ ntestgrouptestpredefsubcode.substring(0, ntestgrouptestpredefsubcode.length() - 1)
									+ ")";
							jdbcTemplate.execute(deletesubcode);
							List<String> lstactionTypeactiondelete = new ArrayList<String>();
							lstactionTypeactiondelete.add("IDS_DELETESUBCODERESULT");
							List<List<?>> lstnewobject = new ArrayList<>();
							lstnewobject.add(lstCodedResultDeleteAudit);
							auditUtilityFunction.fnInsertListAuditAction(lstnewobject, 1, null,
									lstactionTypeactiondelete, objUserInfo);
						}

						if (lstaddCodedResult.size() > 0) {

							final String subQuery = "select nsequenceno from seqnotestgroupmanagement where stablename = 'testgrouptestpredefsubresult' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
							int nSeqNosub = jdbcTemplate.queryForObject(subQuery, Integer.class);

							String partQuery = "";
							String queryString = "insert into testgrouptestpredefsubresult (ntestgrouptestpredefsubcode,ntestgrouptestpredefcode,ssubcodedresult,dmodifieddate "
									+ ",nsitecode,nstatus) " + " VALUES ";

							for (TestGroupTestPredefinedSubCode SubcodedResult : lstaddCodedResult) {
								SubcodedResult.setNtestgrouptestpredefsubcode(nSeqNosub + 1);
								partQuery = partQuery + "(" + SubcodedResult.getNtestgrouptestpredefsubcode() + ","
										+ objPredefParameter.getNtestgrouptestpredefcode() + ",'"
										+ SubcodedResult.getSsubcodedresult() + "','"
										+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
										+ objUserInfo.getNmastersitecode() + ", "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
								nSeqNosub = nSeqNosub + 1;
								SubcodedResult
										.setNtestgrouptestpredefcode(objPredefParameter.getNtestgrouptestpredefcode());
								lstCodedResultAddAudit.add(SubcodedResult);
							}
							jdbcTemplate.execute(queryString + partQuery.substring(0, partQuery.length() - 1));
							String UpQry = "update seqnotestgroupmanagement set nsequenceno=" + nSeqNosub
									+ " where stablename='testgrouptestpredefsubresult' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
							jdbcTemplate.execute(UpQry);
							List<String> lstactionTypeaction = new ArrayList<String>();
							lstactionTypeaction.add("IDS_ADDSUBCODERESULT");
							List<List<?>> lstnewobject = new ArrayList<>();
							lstnewobject.add(lstCodedResultAddAudit);
							auditUtilityFunction.fnInsertListAuditAction(lstnewobject, 1, null, lstactionTypeaction,
									objUserInfo);
						}
						return new ResponseEntity<Object>(
								testGroupCommonFunction.getTestGroupTestPredefinedParameter(
										objPredefParameter.getNtestgrouptestparametercode(),
										objUserInfo.getSlanguagefilename()),
								HttpStatus.OK);
					} else {
						return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				} else {
					return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteTestGroupPredefParameter(UserInfo objUserInfo,
			TestGroupTestPredefinedParameter objPredefParameter, TestGroupSpecification objGroupSpecification,
			List<TestGroupTestPredefinedSubCode> lstCodedResult) throws Exception {
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objGroupSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
				final String sQuery = "select * from testgrouptestpredefparameter where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ntestgrouptestpredefcode = " + objPredefParameter.getNtestgrouptestpredefcode() + ";";
				final TestGroupTestPredefinedParameter objOldPredefParameter = (TestGroupTestPredefinedParameter) jdbcUtilityFunction
						.queryForObject(sQuery, TestGroupTestPredefinedParameter.class, jdbcTemplate);
				if (objOldPredefParameter != null) {
					if (objOldPredefParameter.getNdefaultstatus() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					} else {
						final String sUpdateQuery = "update testgrouptestpredefparameter set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
								+ " where ntestgrouptestpredefcode = "
								+ objPredefParameter.getNtestgrouptestpredefcode() + ";";
						final String UpdateQuery = "update testgrouptestpredefsubresult set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
								+ " where ntestgrouptestpredefcode = "
								+ objPredefParameter.getNtestgrouptestpredefcode() + ";";
						jdbcTemplate.execute(UpdateQuery);
						jdbcTemplate.execute(sUpdateQuery);

						List<TestGroupTestPredefinedParameter> ins = new ArrayList<TestGroupTestPredefinedParameter>();
						ins.add(objPredefParameter);
						List<List<?>> lstnewobject = new ArrayList<>();
						lstnewobject.add(ins);
						lstnewobject.add(lstCodedResult);
						auditUtilityFunction.fnInsertListAuditAction(lstnewobject, 1, null,
								Arrays.asList("IDS_DELETETESTPREDEFINEDPARAMETER", "IDS_DELETESUBCODERESULT"),
								objUserInfo);
						return new ResponseEntity<Object>(
								testGroupCommonFunction.getTestGroupTestPredefinedParameter(
										objPredefParameter.getNtestgrouptestparametercode(),
										objUserInfo.getSlanguagefilename()),
								HttpStatus.OK);
					}
				} else {
					return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> defaultTestGroupPredefParameter(UserInfo objUserInfo,
			TestGroupTestPredefinedParameter objPredefParameter, TestGroupSpecification objGroupSpecification)
			throws Exception {
		String sQuery = "select * from testgrouptestpredefparameter where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestgrouptestpredefcode = "
				+ objPredefParameter.getNtestgrouptestpredefcode();
		final TestGroupTestPredefinedParameter objTestPredefined = (TestGroupTestPredefinedParameter) jdbcUtilityFunction
				.queryForObject(sQuery, TestGroupTestPredefinedParameter.class, jdbcTemplate);
		if (objTestPredefined != null) {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstOldObject = new ArrayList<>();
			final List<Object> lstNewObject = new ArrayList<>();
			sQuery = "select * from testgrouptestpredefparameter where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and ntestgrouptestparametercode = "
					+ objPredefParameter.getNtestgrouptestparametercode() + " and ntestgrouptestpredefcode <> "
					+ objPredefParameter.getNtestgrouptestpredefcode() + " and nsitecode="
					+ objUserInfo.getNmastersitecode();
			List<TestGroupTestPredefinedParameter> lstPredef = (List<TestGroupTestPredefinedParameter>) jdbcTemplate
					.query(sQuery, new TestGroupTestPredefinedParameter());
			String sUpdateQuery = "";
			if (lstPredef != null && lstPredef.size() > 0) {
				lstOldObject.add(SerializationUtils.clone(lstPredef.get(0)));
				TestGroupTestPredefinedParameter objNewPredefined = SerializationUtils.clone(lstPredef.get(0));
				sUpdateQuery = "update testgrouptestpredefparameter set ndefaultstatus = "
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
						+ " where ntestgrouptestpredefcode = " + objNewPredefined.getNtestgrouptestpredefcode() + ";";
				objNewPredefined.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
				lstNewObject.add(objNewPredefined);
				multilingualIDList.add("IDS_EDITTESTPREDEFINEDPARAMETER");
			}

			sUpdateQuery = sUpdateQuery + "update testgrouptestpredefparameter set ndefaultstatus = "
					+ objPredefParameter.getNdefaultstatus() + "" + ",dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' " + " where ntestgrouptestpredefcode = "
					+ objPredefParameter.getNtestgrouptestpredefcode() + ";";

			jdbcTemplate.execute(sUpdateQuery);
			multilingualIDList.add("IDS_EDITTESTPREDEFINEDPARAMETER");
			lstNewObject.add(objPredefParameter);
			lstOldObject.add(objTestPredefined);
			auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList, objUserInfo);
			return new ResponseEntity<Object>(
					testGroupCommonFunction.getTestGroupTestPredefinedParameter(
							objPredefParameter.getNtestgrouptestparametercode(), objUserInfo.getSlanguagefilename()),
					HttpStatus.OK);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> createTestGroupAddSpecification(UserInfo objUserInfo,
			TestGroupTestClinicalSpec objClinicalSpec, TestGroupSpecification objGroupSpecification) throws Exception {
		final String Query = " lock  table locktestgroup " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(Query);
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objGroupSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {

				if (objClinicalSpec.getNfromage() < objClinicalSpec.getNtoage()) {

					final List<Integer> objPredefinedParameter = testGroupCommonFunction
							.clinicalSpecValidation(objClinicalSpec);
					if (objPredefinedParameter.size() == 0) {
						final String sQuery = "select nsequenceno from seqnotestgroupmanagement where stablename = 'testgrouptestclinicalspec' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
						final int nSeqNo = jdbcTemplate.queryForObject(sQuery, Integer.class) + 1;
						objClinicalSpec.setNtestgrouptestclinicspeccode(nSeqNo);
						String sInsertQuery = testGroupCommonFunction.insertClinicalSpecQuery(objClinicalSpec,
								objUserInfo);
						sInsertQuery = sInsertQuery + "update seqnotestgroupmanagement set nsequenceno = " + nSeqNo
								+ " where stablename = 'testgrouptestclinicalspec' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
						jdbcTemplate.execute(sInsertQuery);
						List<String> lstactionType = new ArrayList<String>();
						lstactionType.add("IDS_ADDCLINICALSPEC");
						List<Object> lstnewobject = new ArrayList<Object>();
						lstnewobject.add(objClinicalSpec);
						auditUtilityFunction.fnInsertAuditAction(lstnewobject, 1, null, lstactionType, objUserInfo);
						return new ResponseEntity<Object>(testGroupCommonFunction.getTestGroupTestClinicalSpec(
								objClinicalSpec.getNtestgrouptestparametercode(), objUserInfo), HttpStatus.OK);
					} else {
						return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.CLINICALSPECALREADYEXISTS.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_TOAGEMUSTMORETHANFROM",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveClinicalSpecById(final UserInfo objUserInfo,
			final TestGroupTestClinicalSpec objClinicalSpec, final TestGroupSpecification objSpecification)
			throws Exception {
		final TestGroupSpecification objTestGroupSpecification = testGroupCommonFunction
				.getActiveSpecification(objSpecification.getNallottedspeccode(), objUserInfo);
		if (objTestGroupSpecification != null) {
			if (objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT
					.gettransactionstatus()
					|| objTestGroupSpecification.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
							.gettransactionstatus()) {
				final String sPredefQry = "select tgtpp.ntestgrouptestclinicspeccode,tgtpp.nfromageperiod,tgtpp.ntoageperiod, tgtpp.ntestgrouptestparametercode, tgtpp.nfromage,tgtpp.ntoage,tgtpp.shigha,"
						+ " tgtpp.shighb,tgtpp.slowa,tgtpp.slowb,tgtpp.sminlod,tgtpp.smaxlod,tgtpp.sminloq ,tgtpp.smaxloq,tgtpp.sdisregard,tgtpp.ngendercode,tgtpp.sresultvalue,  "
						+ " coalesce(g.jsondata->'sgendername'->>'" + objUserInfo.getSlanguagetypecode()
						+ "',g.jsondata->'sgendername'->>'en-US') as sgendername,"
						+ " COALESCE(p1.jsondata->'speriodname'->>'" + objUserInfo.getSlanguagetypecode()
						+ "', p1.jsondata->'speriodname'->>'en-US')  AS sfromageperiod, "
						+ "  COALESCE(p2.jsondata->'speriodname'->>'" + objUserInfo.getSlanguagetypecode()
						+ "', p2.jsondata->'speriodname'->>'en-US')  AS stoageperiod, "
						+ " coalesce(gd.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode()
						+ "',gd.jsondata->'sdisplayname'->>'en-US') as sgradename,gd.ngradecode "
						+ " from testgrouptestclinicalspec tgtpp, gender g,grade gd,period p1,period p2 where g.ngendercode = tgtpp.ngendercode "
						+ " and p1.nperiodcode=tgtpp.nfromageperiod and p2.nperiodcode=tgtpp.ntoageperiod and tgtpp.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and gd.ngradecode="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and  g.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and p1.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and p2.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgtpp.ntestgrouptestclinicspeccode = "
						+ objClinicalSpec.getNtestgrouptestclinicspeccode();
				final List<TestGroupTestClinicalSpec> listNumericParameter = (List<TestGroupTestClinicalSpec>) jdbcTemplate
						.query(sPredefQry, new TestGroupTestClinicalSpec());
				if (!listNumericParameter.isEmpty()) {
					return new ResponseEntity<Object>(
							commonFunction.getMultilingualMessageList(listNumericParameter,
									Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()).get(0),
							HttpStatus.OK);
				} else {
					return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<Object>(
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
	public ResponseEntity<Object> deleteTestGroupAddSpecification(UserInfo objUserInfo,
			TestGroupTestClinicalSpec objClinicalSpec, TestGroupSpecification objGroupSpecification) throws Exception {
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objGroupSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
				final String sQuery = "select * from testgrouptestclinicalspec where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ntestgrouptestclinicspeccode = " + objClinicalSpec.getNtestgrouptestclinicspeccode()
						+ ";";
				final TestGroupTestPredefinedParameter objOldPredefParameter = (TestGroupTestPredefinedParameter) jdbcUtilityFunction
						.queryForObject(sQuery, TestGroupTestPredefinedParameter.class, jdbcTemplate);

				if (objOldPredefParameter != null) {
					final String sUpdateQuery = "update testgrouptestclinicalspec set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',ntzmodifieddate= "
							+ objUserInfo.getNtimezonecode() + ",noffsetdmodifieddate= "
							+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
							+ " where ntestgrouptestclinicspeccode = "
							+ objClinicalSpec.getNtestgrouptestclinicspeccode() + ";";
					jdbcTemplate.execute(sUpdateQuery);
					List<String> lstactionType = new ArrayList<String>();
					lstactionType.add("IDS_DELETECLINICALSPEC");
					List<Object> lstNewObject = new ArrayList<Object>();
					lstNewObject.add(objClinicalSpec);
					auditUtilityFunction.fnInsertAuditAction(lstNewObject, 1, null, lstactionType, objUserInfo);
					return new ResponseEntity<Object>(testGroupCommonFunction.getTestGroupTestClinicalSpec(
							objClinicalSpec.getNtestgrouptestparametercode(), objUserInfo), HttpStatus.OK);
					// }
				} else {
					return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateTestGroupAddSpecification(UserInfo objUserInfo,
			TestGroupTestClinicalSpec objClinicalSpec, TestGroupSpecification objGroupSpecification) throws Exception {
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objGroupSpecification.getNallottedspeccode(), objUserInfo);
		if (objTGS != null) {
			if (objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| objTGS.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION.gettransactionstatus()) {
				final String sQuery = "select * from testgrouptestclinicalspec where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ntestgrouptestclinicspeccode = " + objClinicalSpec.getNtestgrouptestclinicspeccode()
						+ ";";
				final TestGroupTestClinicalSpec objOldPredefParameter = (TestGroupTestClinicalSpec) jdbcUtilityFunction
						.queryForObject(sQuery, TestGroupTestClinicalSpec.class, jdbcTemplate);
				if (objClinicalSpec.getNfromage() < objClinicalSpec.getNtoage()) {
					if (objOldPredefParameter != null) {
						final List<Integer> objPredefinedParameter = testGroupCommonFunction
								.clinicalSpecObjectByGender(objClinicalSpec);
						if (objPredefinedParameter.size() == 0) {
							jdbcTemplate.execute(
									testGroupCommonFunction.updateClinicalSpecQuery(objClinicalSpec, objUserInfo));
							List<String> lstactionType = new ArrayList<String>();
							lstactionType.add("IDS_EDITCLINICALSPEC");
							List<Object> lstNewObject = new ArrayList<Object>();
							List<Object> lstOldObject = new ArrayList<Object>();
							lstNewObject.add(objClinicalSpec);
							lstOldObject.add(objOldPredefParameter);
							auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, lstactionType,
									objUserInfo);
							return new ResponseEntity<Object>(
									testGroupCommonFunction.getTestGroupTestClinicalSpec(
											objClinicalSpec.getNtestgrouptestparametercode(), objUserInfo),
									HttpStatus.OK);
						} else {
							return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.CLINICALSPECALREADYEXISTS.getreturnstatus(),
									objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
						}
					} else {
						return new ResponseEntity<Object>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}

				} else {
					return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_TOAGEMUSTMORETHANFROM",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getActivePredefinedParameterSubCodedById(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefinedParameter,
			final TestGroupSpecification objSpecification) throws Exception {
		String sPredefQry = "	Select ntestgrouptestpredefsubcode,ntestgrouptestpredefcode,ssubcodedresult from testgrouptestpredefsubresult where nstatus=1 and ntestgrouptestpredefcode="
				+ objPredefinedParameter.getNtestgrouptestpredefcode() + " and nsitecode="
				+ objUserInfo.getNmastersitecode();

		return new ResponseEntity<Object>((List<TestGroupTestPredefinedSubCode>) jdbcTemplate.query(sPredefQry,
				new TestGroupTestPredefinedSubCode()), HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getActivePredefinedParameterSubCoded(final UserInfo objUserInfo,
			final TestGroupTestPredefinedParameter objPredefinedParameter) throws Exception {
		String sPredefQry = "	Select ntestgrouptestpredefsubcode,ntestgrouptestpredefcode,ssubcodedresult from testgrouptestpredefsubresult where nstatus=1 and ntestgrouptestpredefcode="
				+ objPredefinedParameter.getNtestgrouptestpredefcode() + " and nsitecode="
				+ objUserInfo.getNmastersitecode();

		return new ResponseEntity<Object>((List<TestGroupTestPredefinedSubCode>) jdbcTemplate.query(sPredefQry,
				new TestGroupTestPredefinedSubCode()), HttpStatus.OK);

	}

}
