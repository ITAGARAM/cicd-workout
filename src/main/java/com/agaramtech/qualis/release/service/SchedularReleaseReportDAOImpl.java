
package com.agaramtech.qualis.release.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ReportDAOSupport;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SchedularReleaseReportDAOImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final ReportDAOSupport reportDAOSupport;

	public ResponseEntity<Object> reportGenerationBySchedular(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		int nregTypeCode = -1;
		int nregSubTypeCode = -1;
		int nsectionCode = -1;
		int naccredit = Enumeration.TransactionStatus.NO.gettransactionstatus();
		String sreportComments = "";

		int qType = 1;
		int ncontrolCode = -1;
		int nreportDetailCode = -1;
		int ncoaparentcode = 0;

		String systemFileName = "";
		String reportPath = "";
		String folderName = "";
		String subReportPath = "";
		String imagePath = "";
		String pdfPath = "";
		String sfileName = "";

		String sJRXMLname = "";
		String sreportingtoolURL = "";
		String ssignfilename = "";
		String scertfilename = "";
		String ssecuritykey = "";

		returnMap.putAll(inputMap);
		returnMap.remove(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

		if (!userInfo.getSreason().isEmpty()) {
			sreportComments = userInfo.getSreason();
		}
		if (inputMap.containsKey("nregtypecode")) {
			nregTypeCode = (int) inputMap.get("nregtypecode");
		}
		if (inputMap.containsKey("ncontrolcode")) {
			ncontrolCode = (int) inputMap.get("ncontrolcode");
		}
		if (inputMap.containsKey("nregsubtypecode")) {
			nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
		}

		if (inputMap.containsKey("ncoaparentcode")) {
			ncoaparentcode = Integer.parseInt(inputMap.get("ncoaparentcode").toString());
			inputMap.put("ncoaparentcode", ncoaparentcode);
		}
		if (inputMap.containsKey("reportSectionCode")) {
			nsectionCode = (int) inputMap.get("reportSectionCode");
		}
		if (inputMap.containsKey("systemFileName")) {
			systemFileName = (String) inputMap.get("systemFileName");
		}

		final String sReportQuery = "select ssettingvalue from settings where nsettingcode = 29 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings objReportsettings = (Settings) jdbcUtilityFunction.queryForObject(sReportQuery, Settings.class,
				jdbcTemplate);

		if (objReportsettings.getSsettingvalue()
				.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))) {

			ssignfilename = (String) returnMap.get("ssignfilename");
			scertfilename = (String) returnMap.get("scertfilename");
			ssecuritykey = (String) returnMap.get("ssecuritykey");

		} else {

			final String sRootDir = ftpUtilityFunction.getFTPFileWritingPath("", null);

			final String testAttachmentQuery = "select coalesce(jsondata->>'ssystemfilename','') ssystemfilename from releasetestattachment where ncoaparentcode = "
					+ inputMap.get("ncoaparentcode") + " and (jsondata->'nneedreport'):: INT = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode = "
					+ userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			final List<Map<String, Object>> lstSysFile = jdbcTemplate.queryForList(testAttachmentQuery);

			File oSysFile = null;
			String sSysFile = "";

			for (int nlen = 0; nlen < lstSysFile.size(); nlen++) {
				sSysFile = (String) lstSysFile.get(nlen).get("ssystemfilename");
				oSysFile = new File(sRootDir + sSysFile);
				if (!oSysFile.exists()) {
					ftpUtilityFunction.FileViewUsingFtp(oSysFile.getName(), 0, userInfo, sRootDir, "");
				}
			}
		}

		final String getReportPaths = "select ssettingvalue from reportsettings where nreportsettingcode in ("
				+ Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode() + ","
				+ Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode() + ","
				+ Enumeration.ReportSettings.REPORTINGTOOL_URL.getNreportsettingcode() + ")" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " order by nreportsettingcode ";

		final List<String> reportPaths = jdbcTemplate.queryForList(getReportPaths, String.class);
		reportPath = reportPaths.get(0);
		subReportPath = reportPaths.get(0);
		imagePath = reportPaths.get(0);
		pdfPath = reportPaths.get(1);

		sreportingtoolURL = reportPaths.get(2);
		inputMap.put("subReportPath", subReportPath);
		inputMap.put("folderName", folderName);
		inputMap.put("imagePath", imagePath);
		inputMap.put("naccredit", naccredit);
		inputMap.put("nregTypeCode", nregTypeCode);
		inputMap.put("nregSubTypeCode", nregSubTypeCode);
		inputMap.put("nsectioncode", nsectionCode);
		inputMap.put("sreportComments", sreportComments);
		inputMap.put("sreportingtoolURL", sreportingtoolURL);
		inputMap.put("ncontrolcode",
				(int) inputMap.get("nreporttypecode") == Enumeration.ReportType.CONTROLBASED.getReporttype()
						? ncontrolCode
						: -1);
		final UserInfo userInfoWithReportFormCode = new UserInfo(userInfo);
		inputMap.put("napproveconfversioncode", (int) inputMap.get("napprovalversioncode"));

		final Map<String, Object> dynamicReport = reportDAOSupport.getDynamicReports(inputMap,
				userInfoWithReportFormCode);

		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals((String) dynamicReport.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {

			sJRXMLname = (String) dynamicReport.get("JRXMLname");
			nreportDetailCode = (int) dynamicReport.get("nreportdetailcode");
			folderName = (String) dynamicReport.get("folderName");

			qType = (int) dynamicReport.get("qType");
			reportPath = reportPath + folderName;

			inputMap.put("qType", qType);
			inputMap.put("folderName", folderName);
			inputMap.put("nreportDetailCode", nreportDetailCode);
			inputMap.put("sJRXMLname", sJRXMLname);

			// ATE234 janakumar ALPD-5116 Release&Report ->View the report in the new tab
			final String getFileuploadpath = "select ssettingvalue from reportsettings where"
					+ " nreportsettingcode in (" + Enumeration.ReportSettings.REPORTLINKPATH.getNreportsettingcode()
					+ "," + Enumeration.ReportSettings.MRT_REPORT_JSON_PATH.getNreportsettingcode()
					+ ") order by nreportsettingcode ";

			final List<String> reportPathsValues = jdbcTemplate.queryForList(getFileuploadpath, String.class);

			returnMap.put("sreportlink", reportPathsValues.get(0));
			returnMap.put("smrttemplatelink", reportPathsValues.get(1));
			returnMap.put("FILEName", dynamicReport.get("JRXMLname"));
			returnMap.put("folderName", folderName.replace("\\", ""));

		}
		reportPath = reportPath + sJRXMLname;
		final File JRXMLFile = new File(reportPath);

		if (sJRXMLname != null && !sJRXMLname.equals("") && JRXMLFile.exists()) {

			inputMap.put("systemFileName", systemFileName);
			inputMap.put("JRXMLFile", JRXMLFile);
			inputMap.put("qType", qType);
			inputMap.put("pdfPath", pdfPath);
			inputMap.put("sfileName", sfileName);
			inputMap.put("ssignfilename", ssignfilename);
			inputMap.put("scertfilename", scertfilename);
			inputMap.put("ssecuritykey", ssecuritykey);
			inputMap.put("nreportdetailcode", dynamicReport.get("nreportdetailcode"));

			returnMap.putAll(getPdfReportForMrtForSchedular(inputMap, userInfo));

			final String draftToReleased = "UPDATE coareporthistorygeneration SET " + "nreportstatus = "
					+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " "
					+ "WHERE ncoareporthistorycode = " + inputMap.get("ncoareporthistorycode") + " " // coa history code
					+ "AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+" AND nsitecode = "+userInfo.getNtranssitecode()+ " ;";

			jdbcTemplate.execute(draftToReleased);

			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		} else {

			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REPORTTEMPLATEISNOTFOUND",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

		}
	}

	public Map<String, Object> getPdfReportForMrtForSchedular(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> jasperParameter = new HashMap<String, Object>();
		File JRXMLFile = (File) inputMap.get("JRXMLFile");

		jasperParameter.put("npreregno", inputMap.get("npreregno"));
		jasperParameter.put("nsectioncode", inputMap.get("nsectioncode"));
		jasperParameter.put("ncoaparentcode", inputMap.get("ncoaparentcode"));
		jasperParameter.put("ssubreportpath",
				inputMap.get("subReportPath").toString() + inputMap.get("folderName").toString());
		jasperParameter.put("simagepath", inputMap.get("imagePath").toString() + inputMap.get("folderName").toString());
		jasperParameter.put("sarno", inputMap.get("sarno"));
		jasperParameter.put("needlogo", inputMap.get("naccredit"));
		jasperParameter.put("nregtypecode", inputMap.get("nregTypeCode"));
		jasperParameter.put("nregsubtypecode", inputMap.get("nregSubTypeCode"));
		jasperParameter.put("sreportcomments", inputMap.get("sreportComments"));
		jasperParameter.put("sprimarykeyname", inputMap.get("ncoaparentcode"));
		jasperParameter.put("nlanguagecode", userInfo.getSlanguagetypecode());
		jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));// janakumar
		jasperParameter.put("nsitecode", (int) userInfo.getNtranssitecode());
		jasperParameter.put("nreportdetailcode", inputMap.get("nreportdetailcode"));
		jasperParameter.put("sreportingtoolURL", inputMap.get("sreportingtoolURL"));

		returnMap.putAll(reportDAOSupport.compileAndPrintReportForSchedular(jasperParameter, JRXMLFile,
				(int) inputMap.get("qType"), inputMap.get("pdfPath").toString(), inputMap.get("sfileName").toString(),
				userInfo, inputMap.get("systemFileName").toString(), (int) inputMap.get("ncontrolcode"), true,
				inputMap.get("ssignfilename").toString(), inputMap.get("scertfilename").toString(),
				inputMap.get("ssecuritykey").toString()));

		String uploadStatus = (String) returnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
		String sFile = FilenameUtils.getExtension(JRXMLFile.toString());

		if (sFile.equals("jrxml")) {
			returnMap.put("filetype", "jrxml");
		}
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)) {

			final String draftToReleased = "UPDATE coareporthistorygeneration SET " + "nreportstatus = "
					+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " "
					+ "WHERE ncoareporthistorycode = " + inputMap.get("ncoareporthistorycode") + " " + "AND nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and nsitecode = "+ userInfo.getNtranssitecode()+ " ;";

			jdbcTemplate.execute(draftToReleased);

			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			returnMap.put("ReportPDFFile", returnMap.get("outputFilename"));

		}
		LOGGER.info("At getPdfReportForMrtForSchedular()");
		return returnMap;

	}

}
