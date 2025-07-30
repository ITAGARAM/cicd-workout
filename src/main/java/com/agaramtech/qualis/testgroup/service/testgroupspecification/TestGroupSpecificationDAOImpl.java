package com.agaramtech.qualis.testgroup.service.testgroupspecification;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.ApprovalRoleActionDetail;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.TestGroupCommonFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.SeqNoTestGroupmanagement;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TestGroupSpecificationDAOImpl implements TestGroupSpecificationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestGroupSpecificationDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private TestGroupCommonFunction testGroupCommonFunction;

	@Override
	public ResponseEntity<Object> getSpecificationHistory(final UserInfo userInfo,
			final TestGroupSpecification objSpecification) throws Exception {
		return testGroupCommonFunction.getSpecificationHistory(userInfo, objSpecification);
	}

	@Override
	public ResponseEntity<Object> specReportGenerate(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		final TestGroupSpecSampleType testgroupsampletype = mapper.convertValue(inputMap.get("selectedComponent"),
				TestGroupSpecSampleType.class);
		final TestGroupTest testgrouptest = mapper.convertValue(inputMap.get("selectedTest"), TestGroupTest.class);

		if (testgroupsampletype != null && testgrouptest != null) {
			final String getSpecDetatils = "select sspecname,sversion from testgroupspecification "
					+ " where nallottedspeccode = " + inputMap.get("nallottedspeccode") + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			TestGroupSpecification testGroupSpecObj = (TestGroupSpecification) jdbcUtilityFunction
					.queryForObject(getSpecDetatils, TestGroupSpecification.class, jdbcTemplate);
			if (testGroupSpecObj != null) {

				Map<String, Object> returnMap = new HashMap<>();
				String sfileName = "";
				String sJRXMLname = "";
				int qType = 1;
				int ncontrolCode = -1;
				String sfilesharedfolder = "";
				String fileuploadpath = "";
				String subReportPath = "";
				String imagePath = "";
				String pdfPath = "";
				String sreportingtoolURL = "";
				final String getFileuploadpath = "select ssettingvalue from reportsettings where nreportsettingcode in ("
						+ Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode() + ","
						+ Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode() + ","
						+ Enumeration.ReportSettings.REPORTINGTOOL_URL.getNreportsettingcode() + ") "
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nreportsettingcode";
				LOGGER.info(getFileuploadpath);
				List<String> reportPaths = jdbcTemplate.queryForList(getFileuploadpath, String.class);
				fileuploadpath = reportPaths.get(0);
				subReportPath = reportPaths.get(0);
				imagePath = reportPaths.get(0);
				pdfPath = reportPaths.get(1);
				sreportingtoolURL = reportPaths.get(2);

				if (inputMap.containsKey("ncontrolcode")) {
					ncontrolCode = (int) inputMap.get("ncontrolcode");
				}

				sJRXMLname = "SpecReport.jrxml";
				sfileName = "SpecReport_" + inputMap.get("nallottedspeccode");
				;
				inputMap.put("ncontrolcode",
						(int) inputMap.get("nreporttypecode") == Enumeration.ReportType.CONTROLBASED.getReporttype()
								? inputMap.get("ncontrolcode")
								: ncontrolCode);

				UserInfo userInfoWithReportFormCode = new UserInfo(userInfo);
				userInfoWithReportFormCode.setNformcode((short) Enumeration.FormCode.REPORTCONFIG.getFormCode());
				Map<String, Object> dynamicReport = new HashMap<>();
						//getDynamicReports(inputMap, userInfoWithReportFormCode);
				String folderName = "";

				if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals((String) dynamicReport.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					sJRXMLname = (String) dynamicReport.get("JRXMLname");
					folderName = (String) dynamicReport.get("folderName");
					fileuploadpath = fileuploadpath + folderName;
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOREPORTFOUNDFORSPEC",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				sfilesharedfolder = fileuploadpath + sJRXMLname;
				File JRXMLFile = new File(sfilesharedfolder);
				if (sJRXMLname != null && !sJRXMLname.equals("")) {

					Map<String, Object> jasperParameter = new HashMap<>();
					jasperParameter.put("ssubreportpath", subReportPath + folderName);
					jasperParameter.put("simagepath", imagePath + folderName);
					jasperParameter.put("sreportingtoolURL", sreportingtoolURL);
					jasperParameter.put("language", userInfo.getSlanguagetypecode());
					jasperParameter.put((String) inputMap.get("sprimarykeyname"),
							(int) inputMap.get("nallottedspeccode"));
					jasperParameter.put("sprimarykeyname", (int) inputMap.get("nallottedspeccode"));
					jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));
					jasperParameter.put("nreportdetailcode", dynamicReport.get("nreportdetailcode"));

					//returnMap.putAll(compileAndPrintReport(jasperParameter, JRXMLFile, qType, pdfPath, sfileName,
						//	userInfo, "", ncontrolCode, false, "", "", ""));
					String uploadStatus = (String) returnMap
							.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
					if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(uploadStatus)) {

						returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
						String auditAction = "IDS_SPECREPORT";
						String comments = commonFunction.getMultilingualMessage("IDS_ALLOTTEDSPECCODE",
								userInfo.getSlanguagefilename()) + ": " + inputMap.get("nallottedspeccode") + "; ";

						comments += commonFunction.getMultilingualMessage("IDS_SPECIFICATION",
								userInfo.getSlanguagefilename()) + ": " + testGroupSpecObj.getSspecname() + "; ";

						comments += commonFunction.getMultilingualMessage("IDS_VERSION",
								userInfo.getSlanguagefilename()) + ": "
								+ (testGroupSpecObj.getSversion().isEmpty() ? "-" : testGroupSpecObj.getSversion())
								+ "; ";

						comments = comments
								+ commonFunction.getMultilingualMessage("IDS_FILENAME", userInfo.getSlanguagefilename())
								+ ": " + returnMap.get("outputFilename") + "; ";

						Map<String, Object> outputMap = new HashMap<>();
						outputMap.put("stablename", "testgroupspecification");
						outputMap.put("sprimarykeyvalue", inputMap.get("nallottedspeccode"));

						auditUtilityFunction.insertAuditAction(userInfo, auditAction, comments, outputMap);
					}
				} else {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOREPORTFOUNDFORSPEC",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

				}
				return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_REPORTCANNOTGENERATEFORCOMPWITHOUTTEST",
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> retireSpec(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		final String Query = " lock  table locktestgrpspchistory " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(Query);
		ObjectMapper objMapper = new ObjectMapper();
		List<Object> lstnewobject = new ArrayList<Object>();
		List<Object> lstoldobject = new ArrayList<Object>();
		String retrQuery = "";
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final TestGroupSpecification objSpeccode = objMapper.convertValue(inputMap.get("nallottedspeccode"),
				TestGroupSpecification.class);
		final TreeTemplateManipulation objTree = objMapper.convertValue(inputMap.get("treetemplatemanipulation"),
				TreeTemplateManipulation.class);
		final ApprovalRoleActionDetail objApproveaction = objMapper
				.convertValue(inputMap.get("approvalRoleActionDetail"), ApprovalRoleActionDetail.class);
		if (objApproveaction.getNlevelno() == Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()) {

			String getQuery = "select tgs.*,ts.stransdisplaystatus sapprovalstatus from testgroupspecification tgs,transactionstatus ts"
					+ " where tgs.nallottedspeccode =" + objSpeccode.getNallottedspeccode() + " and " + " tgs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and ts.ntranscode=tgs.napprovalstatus and " + " ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

			TestGroupSpecification objSpec = (TestGroupSpecification) jdbcUtilityFunction.queryForObject(getQuery,
					TestGroupSpecification.class, jdbcTemplate);

			String getTreeQuery = "select * from treetemplatemanipulation ttm"
					+ " where ttm.ntemplatemanipulationcode =" + objTree.getNtemplatemanipulationcode() + " and "
					+ " ttm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

			TreeTemplateManipulation objTreeTemp = (TreeTemplateManipulation) jdbcUtilityFunction
					.queryForObject(getTreeQuery, TreeTemplateManipulation.class, jdbcTemplate);

			if (objSpec.getNapprovalstatus() != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				retrQuery = " update testgroupspecification set napprovalstatus=" + " "
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + "," + "dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nallottedspeccode ="
						+ objSpeccode.getNallottedspeccode() + "";

				String seqQuery = "select * from seqnotestgroupmanagement where stablename='testgroupspecificationhistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				SeqNoTestGroupmanagement objSeq = (SeqNoTestGroupmanagement) jdbcUtilityFunction
						.queryForObject(seqQuery, SeqNoTestGroupmanagement.class, jdbcTemplate);
				int seqNo = objSeq.getNsequenceno() + 1;

				retrQuery = retrQuery + ";insert into testgroupspecificationhistory(nspecificationhistorycode,"
						+ " nallottedspeccode,ntransactionstatus,nusercode,nuserrolecode,"
						+ " dtransactiondate,noffsettransactiondate,ntztransactiondate,scomments,dmodifieddate,nstatus,nsitecode)values("
						+ seqNo + "," + objSpeccode.getNallottedspeccode() + "," + " "
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + "," + " "
						+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
						+ userInfo.getNtimezonecode() + " ,'" + userInfo.getSreason() + "'," + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ userInfo.getNmastersitecode() + ")";

				retrQuery = retrQuery + "; update seqnotestgroupmanagement set nsequenceno=" + seqNo
						+ " where stablename='testgroupspecificationhistory' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

				jdbcTemplate.execute(retrQuery);
				TestGroupSpecification objNewSpec = (TestGroupSpecification) jdbcUtilityFunction
						.queryForObject(getQuery, TestGroupSpecification.class, jdbcTemplate);
				lstnewobject.add(objNewSpec);
				lstnewobject.add(objTree);
				lstoldobject.add(objSpec);
				lstoldobject.add(objTreeTemp);

				auditUtilityFunction.fnInsertAuditAction(lstnewobject, 2, lstoldobject,
						Arrays.asList("IDS_RETIRESPECIFICATION"), userInfo);

				outputMap.putAll((Map<String, Object>) testGroupCommonFunction
						.getTestGroupSpecification(userInfo, objTree, true, objSpeccode).getBody());
				return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYRETIRED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FINALLEVELAPPROVE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

}
