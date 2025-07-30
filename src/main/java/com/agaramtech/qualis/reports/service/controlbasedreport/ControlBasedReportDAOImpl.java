package com.agaramtech.qualis.reports.service.controlbasedreport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
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
import com.agaramtech.qualis.global.ReportDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.model.ControlbasedReportvalidation;
import com.agaramtech.qualis.reports.model.ParameterConfiguration;
import com.agaramtech.qualis.reports.model.ReportDetails;
import com.agaramtech.qualis.reports.model.ReportMaster;
import com.agaramtech.qualis.reports.model.ReportParameter;
import com.agaramtech.qualis.reports.service.reportconfig.ReportMasterDAO;
import com.google.gson.Gson;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ControlBasedReportDAOImpl implements ControlBasedReportDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControlBasedReportDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ReportMasterDAO reportMasterDAO;
	private final ReportDAOSupport reportDAOSupport;

	@SuppressWarnings({ "unchecked" })
	public ResponseEntity<Object> generateControlBasedReport(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> sNodeServerStart = null;
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> controlRtn;

		sNodeServerStart = reportDAOSupport.validationCheckForNodeServer(inputMap, userInfo);

		if (sNodeServerStart.get("rtn").equals("Failed")) {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_STARTNODESERVER", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String controlBasedQuery = "select rm.*,rd.sreportformatdetail from reportmaster rm,reportdetails rd "
					+ "where rm.nreportcode=rd.nreportcode and rm.ncontrolcode=" + inputMap.get("ncontrolcode") + " "
					+ "and rm.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and rd.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
					+ "and rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and rd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and rd.nsitecode=" + userInfo.getNmastersitecode() + " and rm.nsitecode="
					+ userInfo.getNmastersitecode() + " " + ";";

			final ReportMaster reportMaster = (ReportMaster) jdbcUtilityFunction.queryForObject(controlBasedQuery,
					ReportMaster.class, jdbcTemplate);

			if (reportMaster == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDREPORTDESIGNCONFIG", // ids

						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else if (reportMaster.getSreportformatdetail().equalsIgnoreCase("viewer")) {

				inputMap.put("nreporttypecode", Enumeration.ReportType.CONTROLBASED.getReporttype()); // this for which
																										// type report
																										// generation
				returnMap.putAll(
						(Map<String, Object>) viewReportWithParameters(reportMaster, inputMap, userInfo).getBody());
				returnMap.remove("nreportdetailcode");

				return new ResponseEntity<Object>(returnMap, HttpStatus.OK);

			} else {

				inputMap.put("nreporttypecode", Enumeration.ReportType.CONTROLBASED.getReporttype()); // this for which
																										// type report
																										// generation

				final String controlBasedQueryString = "select cbr.ntranscode,ts.stransstatus  as stransactionstatus from controlbasedreportvalidation cbr,reportmaster rm,  reportdetails rd,transactionstatus ts  where "
						+ "cbr.nreportdetailcode=rd.nreportdetailcode  and   rm.nreportcode = rd.nreportcode  and ts.ntranscode=cbr.ntranscode   "
						+ "and rm.ntransactionstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " " + "and cbr.ncontrolcode=" + inputMap.get("ncontrolcode")
						// + " and cbr.ntranscode="+ inputMap.get("ntranscode")
						+ " " + "and cbr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and rm.nstatus=  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and rd.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " and rd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and rd.nsitecode =" + userInfo.getNmastersitecode() + " " + "and cbr.nsitecode = "
						+ userInfo.getNmastersitecode() + " " + "and rm.nsitecode =" + userInfo.getNmastersitecode()
						+ " " + "and  ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

				final List<ControlbasedReportvalidation> controlbasedreportvalidation = (List<ControlbasedReportvalidation>) jdbcTemplate
						.query(controlBasedQueryString, new ControlbasedReportvalidation());

				boolean isValidControl = false;
				if (controlbasedreportvalidation.isEmpty()) {
					isValidControl = true;
				} else {

					isValidControl = controlbasedreportvalidation.stream()
							.anyMatch(item -> item.getNtranscode() == (int) inputMap.get("ntranscode"));
				}

				if (isValidControl) {

					controlRtn = controlBasedReportGeneration(reportMaster, inputMap, userInfo);

				} else {
					final String ntransname = controlbasedreportvalidation.stream()
							.map(objreport -> String.valueOf(objreport.getStransactionstatus()))
							.collect(Collectors.joining(","));

					return new ResponseEntity<Object>(
							commonFunction.getMultilingualMessage("IDS_SELECT", userInfo.getSlanguagefilename()) + " "
									+ ntransname,
							HttpStatus.EXPECTATION_FAILED);

				}

			}
			return new ResponseEntity<Object>(controlRtn, HttpStatus.OK);
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	public Map<String, Object> controlBasedReportGeneration(ReportMaster reportMaster, Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
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
				+ Enumeration.ReportSettings.REPORTINGTOOL_URL.getNreportsettingcode() + ") " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nreportsettingcode";
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

		inputMap.put("ncontrolcode",
				(int) inputMap.get("nreporttypecode") == Enumeration.ReportType.CONTROLBASED.getReporttype()
						? inputMap.get("ncontrolcode")
						: ncontrolCode);

		UserInfo userInfoWithReportFormCode = new UserInfo(userInfo);
		userInfoWithReportFormCode.setNformcode((short) Enumeration.FormCode.REPORTCONFIG.getFormCode());
		Map<String, Object> dynamicReport = reportDAOSupport.getDynamicReports(inputMap, userInfoWithReportFormCode);
		String folderName = "";

		sJRXMLname = (String) dynamicReport.get("JRXMLname");

		final String sFileType = FilenameUtils.getExtension(dynamicReport.get("JRXMLname").toString());

		if (sFileType.equalsIgnoreCase("jrxml")) {

			returnMap
					.putAll((Map<String, Object>) viewReportWithParameters(reportMaster, inputMap, userInfo).getBody());

			returnMap.remove("nreportdetailcode");

		} else {

			folderName = (String) dynamicReport.get("folderName");
			fileuploadpath = fileuploadpath + folderName;

			sfilesharedfolder = fileuploadpath + sJRXMLname;
			File JRXMLFile = new File(sfilesharedfolder);

			final String parameterQuery = "select * from parameterconfiguration " // nsitecode
					+ "where nreportdetailcode=" + dynamicReport.get("nreportdetailcode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + ";";

			final List<ParameterConfiguration> parameterValues = jdbcTemplate.query(parameterQuery,
					new ParameterConfiguration());

			final String mappedValues = parameterValues.stream()
					.map(objreport -> String.valueOf(objreport.getSmappedcolumnname()))
					.collect(Collectors.joining(","));

			final Map<String, Object> data = (Map<String, Object>) inputMap.get("selectedRecord");

			final String[] pairsList = mappedValues.split(",");

			Map<String, String> valueMap = new HashMap<>();

			for (int i = 0; i < pairsList.length; i++) {

				String value = pairsList[i].toString();
				if (data.containsKey(value)) {

					value = (String) data.get(value).toString();
				}

				String key = pairsList[i].toString();
				valueMap.put(key, value);

			}
			String jsonDataList = null;
			if (valueMap.isEmpty() || valueMap == null) {
			} else {
				Gson gson = new Gson();
				jsonDataList = gson.toJson(valueMap);
			}

			if (sJRXMLname != null && !sJRXMLname.equals("")) {

				Map<String, Object> jasperParameter = new HashMap<>();
				jasperParameter.put("ssubreportpath", subReportPath + folderName);
				jasperParameter.put("simagepath", imagePath + folderName);
				jasperParameter.put("sreportingtoolURL", sreportingtoolURL);
				jasperParameter.put("language", userInfo.getSlanguagetypecode());
				jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));
				jasperParameter.put("nreportdetailcode", dynamicReport.get("nreportdetailcode"));
				jasperParameter.put("parameterlist", jsonDataList);

				returnMap.putAll(reportDAOSupport.compileAndPrintReport(jasperParameter, JRXMLFile, qType, pdfPath,
						sfileName, userInfo, "", ncontrolCode, false, "", "", ""));

				String uploadStatus = (String) returnMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)) {
					returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

					final String auditFormCode = "select nformcode from qualisforms  where sformname ='Report Designer' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					int count = jdbcTemplate.queryForObject(auditFormCode, Integer.class);

					final String auditFormName = "select sformname from qualisforms  where nformcode="
							+ inputMap.get("nformcode") + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					final ParameterConfiguration controlBasedList = (ParameterConfiguration) jdbcUtilityFunction
							.queryForObject(auditFormName, ParameterConfiguration.class, jdbcTemplate);
					final List<Object> savedTestList = new ArrayList<>();
					final List<String> multilingualIDList = new ArrayList<>();

					controlBasedList.setSreportgenerated("Repport Generated");
					savedTestList.add(controlBasedList);
					multilingualIDList.add("IDS_REPORTGENERATION");
					auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

				}
			}

		}

		return returnMap;
	}

	@SuppressWarnings("unused")
	private List<ControlbasedReportvalidation> getTransactionValidation(Object ncontrolcode, UserInfo userInfo) {

		final String getValidationStatus = "select ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "' as stransactionstatus from controlbasedreportvalidation cbr,reportmaster rm,  reportdetails rd ,transactionstatus ts  where "
				+ "cbr.nreportdetailcode=rd.nreportdetailcode  " + "and cbr.ntranscode=ts.ntranscode "
				+ "and  rm.nreportcode = rd.nreportcode  " + "and rm.ntransactionstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and cbr.nformcode="
				+ ncontrolcode + " " + "and cbr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ "and rd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "order by 1 desc";

		return (List<ControlbasedReportvalidation>) jdbcTemplate.query(getValidationStatus,
				new ControlbasedReportvalidation());
	}

	@SuppressWarnings({ "unchecked", "unused" })
	public ResponseEntity<Object> viewReportWithParameters(final ReportMaster reportMaster,
			final Map<String, Object> inputFieldData, final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		String query = "select rd.*, rm.sreportname from " + " reportmaster rm, reportdetails rd "
				+ " where rd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rm.nreportcode=rd.nreportcode " + " and rd.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rm.nreportcode="
				+ reportMaster.getNreportcode();

		final ReportDetails reportDetails = jdbcTemplate.queryForObject(query, new ReportDetails());
		outputMap.put("SelectedReportDetails", reportDetails);
		outputMap.put("SelectedViewReportMaster", reportMaster);

		final String sFile = FilenameUtils.getExtension(reportDetails.getSsystemfilename());

		if (reportDetails != null) {
			final Map<String, Object> designMap = (Map<String, Object>) getReportParameter(
					reportDetails.getNreportdetailcode(), userInfo).getBody();
			final List<ReportParameter> parameterList = (List<ReportParameter>) designMap.get("ReportParameter");

			Map<String, Object> inputMap = new HashMap<String, Object>();

			int nregTypeCode = reportMaster.getNregtypecode();
			int nregSubTypeCode = reportMaster.getNregsubtypecode();
			int nreportDetailCode = reportDetails.getNreportdetailcode();
			int ncoareporttypecode = reportMaster.getNcoareporttypecode();

			inputMap.put("nreporttypecode", reportMaster.getNreporttypecode());
			inputMap.put("ncoareporttypecode", ncoareporttypecode);
			inputMap.put("nregtypecode", nregTypeCode);
			inputMap.put("nregsubtypecode", nregSubTypeCode);
			inputMap.put("nsectioncode", reportMaster.getNsectioncode());
			inputMap.put("ncertificatetypecode", reportMaster.getNcertificatetypecode());
			inputMap.put("ndecisionstatus", reportMaster.getNreportdecisiontypecode());
			inputMap.put("ncontrolcode", reportMaster.getNcontrolcode());
			inputMap.put("nreportdetailcode", nreportDetailCode);
			inputMap.put("nprojectcode", inputFieldData.get("nprojectcode"));

			Map<String, Object> jasperParameter = new HashMap<String, Object>();
			for (Map.Entry<String, Object> fieldMap : ((Map<String, Object>) inputFieldData.get("selectedRecord"))
					.entrySet()) {
				if (!fieldMap.getKey().endsWith("_componentcode") && !fieldMap.getKey().endsWith("_componentname")) {
					if (((Map<String, Object>) inputFieldData.get("selectedRecord"))
							.get(fieldMap.getKey().concat("_componentcode")) != null) {
						if ((Integer) ((Map<String, Object>) inputFieldData.get("selectedRecord")).get(
								fieldMap.getKey().concat("_componentcode")) == Enumeration.DesignComponent.DATEFEILD
										.gettype()) {

							final String dateValue = dateUtilityFunction.instantDateToString(dateUtilityFunction
									.convertStringDateToUTC((String) fieldMap.getValue(), userInfo, false));
							jasperParameter.put(fieldMap.getKey(), dateValue.replace("T", " "));

						} else {
							if (fieldMap.getKey().startsWith("n") && !(fieldMap.getValue() instanceof Integer)) {
								fieldMap.setValue(Integer.parseInt((String) fieldMap.getValue()));
							}

							jasperParameter.put(fieldMap.getKey(), fieldMap.getValue());
						}
					}
					jasperParameter.put(fieldMap.getKey(), fieldMap.getValue());
				}
			}

			String sfileName = "";
			String sJRXMLname = "";
			int naccredit = Enumeration.TransactionStatus.NO.gettransactionstatus();
			int ncertificateTypeCode = -1;
			String sreportComments = "";
			int qType = 1;
			int ndecisionstatus = -1;
			int ncontrolCode = -1;
			String systemFileName = "";
			String sfilesharedfolder = "";
			String fileuploadpath = "";
			String subReportPath = "";
			String imagePath = "";
			String pdfPath = "";
			int nreporttypecode = 2;

			final String getFileuploadpath = "select ssettingvalue from reportsettings where nreportsettingcode in ("
					+ Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode() + ","
					+ Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode()
					+ ") order by nreportsettingcode ";

			final List<String> reportPaths = jdbcTemplate.queryForList(getFileuploadpath, String.class);
			fileuploadpath = reportPaths.get(0);
			subReportPath = reportPaths.get(0);
			imagePath = reportPaths.get(0);
			pdfPath = reportPaths.get(1);
			UserInfo userInfoWithReportFormCode = new UserInfo(userInfo);
			userInfoWithReportFormCode.setNformcode((short) Enumeration.FormCode.REPORTCONFIG.getFormCode());
			final Map<String, Object> dynamicReport = reportDAOSupport.getDynamicReports(inputMap,
					userInfoWithReportFormCode);
			String folderName = "";
			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals((String) dynamicReport.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {

				sJRXMLname = (String) dynamicReport.get("JRXMLname");
				nreportDetailCode = (int) dynamicReport.get("nreportdetailcode");
				folderName = (String) dynamicReport.get("folderName");
				imagePath = subReportPath;
				fileuploadpath = fileuploadpath + folderName;
			}

			sfilesharedfolder = fileuploadpath + sJRXMLname;
			File JRXMLFile = new File(sfilesharedfolder);

			if (sJRXMLname != null && !sJRXMLname.equals("")) {

				jasperParameter.put("ssubreportpath", subReportPath + folderName);
				jasperParameter.put("simagepath", imagePath + folderName);
				jasperParameter.put("language", userInfo.getSlanguagetypecode());
				jasperParameter.put("certificatetypecode", ncertificateTypeCode);
				jasperParameter.put("needlogo", naccredit);
				jasperParameter.put("ndecisionstatus", ndecisionstatus);
				jasperParameter.put("sreportcomments", sreportComments);
				jasperParameter.put("nreporttypecode", nreporttypecode);
				jasperParameter.put("nreportdetailcode", reportDetails.getNreportdetailcode());

				if (sFile.equalsIgnoreCase("mrt") || sFile.equalsIgnoreCase("jrxml")) {

					outputMap.put("reportType", sFile);
					outputMap.putAll(reportDAOSupport.compileAndPrintReport(jasperParameter, JRXMLFile, qType, pdfPath,
							sfileName, userInfo, systemFileName, (int) inputFieldData.get("ncontrolcode"), false, "",
							"", ""));

				} else {
					outputMap.put("ErrorType", sFile);
					return new ResponseEntity<>(outputMap, HttpStatus.CONFLICT);
				}

				String uploadStatus = (String) outputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)) {
					outputMap.put("ReportPDFPath", pdfPath);
					outputMap.put("ReportPDFFile", outputMap.get("outputFilename"));
				}
				outputMap.put("ssystemfilename", sJRXMLname);
				outputMap.put("sreportname", folderName);

			}
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getReportParameter(final int nreportDetailCode, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final String queryString = "select rp.*,rp.nreportparametercode as ncontrolBasedparameter from reportparameter rp where rp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rp.nreportdetailcode="
				+ nreportDetailCode;

		final List<ReportParameter> parameterList = jdbcTemplate.query(queryString, new ReportParameter());
		for (ReportParameter ParameterList : parameterList) {
			ParameterList.setSdisplaydatatype(commonFunction.getMultilingualMessage(ParameterList.getSdatatype(),
					userInfo.getSlanguagefilename()));
		}
		outputMap.put("ReportParameter", parameterList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> controlBasedReportParameter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final String queryString = "select sreportparametername as scontrolBasedparameter,nreportparametercode as ncontrolBasedparameter,* from reportparameter where nreportdetailcode="
				+ inputMap.get("nreportdetailcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		List<Map<String, Object>> reportTemplateList = jdbcTemplate.queryForList(queryString);

		final List<ParameterConfiguration> controlbasedreportParameterMapping = (List<ParameterConfiguration>) getControlbasedReportParameter(
				(int) inputMap.get("nreportdetailcode"), userInfo);
		outputMap.put("ParameterMappingDatagrid", controlbasedreportParameterMapping);
		outputMap.put("reportTemplateList", reportTemplateList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> controlBasedReportparametretable(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final String queryString = "select ROW_NUMBER()OVER(ORDER BY nreportparametercolumncode)AS ncolumnfield,"
				+ "arr->>'columnname' as stablecolumn "
				+ "from reportparametercolumn,jsonb_array_elements(jsondata)  arr where  " + "nformcode="
				+ inputMap.get("nformcode") + " and arr ->>'columndatatype'='" + inputMap.get("sdatatype") + "' "
				+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		List<Map<String, Object>> reportTemplateList = jdbcTemplate.queryForList(queryString);
		return new ResponseEntity<>(reportTemplateList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> controlBasedReportparametretablecolumn(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {

		String dataType = (String) inputMap.get("reportdatatype");

		dataType = dataType.replace(".", "_");

		String dataTypeString = null;

		final String queryString = "select ROW_NUMBER()OVER(ORDER BY nquerybuildertablecode)AS ncolumnfield,arr->>'columnname' as stablecolumn,nquerybuildertablecode as nquerybuildertablecodecolumn,nquerybuildertablecode from querybuildertablecolumns, "
				+ " jsonb_array_elements(jstaticcolumns)  arr " + " where nformcode=" + inputMap.get("nformcode") + ""
				+ " and arr ->>'filterinputtype'='" + dataTypeString + "' " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		List<Map<String, Object>> reportTemplateList = jdbcTemplate.queryForList(queryString);
		return new ResponseEntity<>(reportTemplateList, HttpStatus.OK);
	}

	@SuppressWarnings({ "unused" })
	@Override
	public ResponseEntity<Object> downloadControlBasedReportparametreInsert(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		List<Map<String, Object>> objlstMap = new ArrayList<Map<String, Object>>();
		final List<Object> savedTestList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTestList1 = new ArrayList<>();

		String str = "reportparameter where nreportparametercode in(" + inputMap.get("ncontrolbasedparameter") + ")";
		String str1 = "querybuildertablecolumns where nquerybuildertablecode in("
				+ inputMap.get("nquerybuildertablecode") + ")";
		final String[] columnList = inputMap.get("squerybuildertablecodecolumn").toString().split(",");
		final String[] paramList = inputMap.get("ncontrolbasedparameter").toString().split(",");

		String strAudit = "SELECT STRING_AGG(rp.sreportparametername, ', ') AS parameterlist "
				+ "FROM reportparameter rp where rp.nreportdetailcode in (" + inputMap.get("nreportdetailcode") + ") "
				+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

		ParameterConfiguration paramerterconfiguration = new ParameterConfiguration();
		String auditColumns = jdbcTemplate.queryForObject(strAudit, String.class);

		paramerterconfiguration.setNreportdetailcode((int) inputMap.get("nreportdetailcode"));
		paramerterconfiguration.setParameterlist(auditColumns);
		paramerterconfiguration.setSmappedcolumnname((String) inputMap.get("squerybuildertablecodecolumn"));

		savedTestList.add(paramerterconfiguration);
		multilingualIDList.add("IDS_ADDPARAMETETMAPPING");

		String queryString1 = "";
		String queryString = "";

		if (inputMap.get("nreportparameterconfigurationcode") == null) {

			String sequencequery = "select nsequenceno from seqnoreportmanagement where stablename ='parameterconfiguration'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);

			for (int i = 0; i < columnList.length; i++) {
				queryString1 = "INSERT INTO public.parameterconfiguration(nreportparameterconfigurationcode, nreportmastercode, nreportdetailcode, nreportparametercode,smappedcolumnname, dmodifieddate, nsitecode, nstatus) "
						+ "Select " + (nsequenceno + i + 1) + "  as nreportparameterconfigurationcode,"
						+ inputMap.get("nreportcode") + "," + inputMap.get("nreportdetailcode")
						+ ",nreportparametercode," + columnList[i] + ",'" + ""
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from reportparameter  where "
						+ "nreportparametercode in(" + paramList[i] + ") ;";

				queryString = queryString1
						+ "update seqnoreportmanagement set nsequenceno =(select max(nreportparameterconfigurationcode) from parameterconfiguration) where stablename='parameterconfiguration'";

				jdbcTemplate.execute(queryString);

			}

			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

			return reportMasterDAO.getControlbasedReportParameter((int) inputMap.get("nreportdetailcode"), userInfo);

		} else {

			final String delQuery = "delete from parameterconfiguration  where"
					+ " nreportparameterconfigurationcode in (" + inputMap.get("nreportparameterconfigurationcode")
					+ ") and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(delQuery);

			String sequencequery = "select nsequenceno from seqnoreportmanagement where stablename ='parameterconfiguration'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);

			for (int i = 0; i < columnList.length; i++) {
				queryString1 = "INSERT INTO public.parameterconfiguration(nreportparameterconfigurationcode, nreportmastercode, nreportdetailcode, nreportparametercode,smappedcolumnname, dmodifieddate, nsitecode, nstatus) "
						+ "Select " + (nsequenceno + i + 1) + "  as nreportparameterconfigurationcode,"
						+ inputMap.get("nreportcode") + "," + inputMap.get("nreportdetailcode")
						+ ",nreportparametercode," + columnList[i] + ",'" + ""
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from reportparameter  where "
						+ "nreportparametercode in(" + paramList[i] + ") ;";
				jdbcTemplate.execute(queryString1);
			}
			queryString = "update seqnoreportmanagement set nsequenceno =(select max(nreportparameterconfigurationcode) from parameterconfiguration) where stablename='parameterconfiguration'";
			jdbcTemplate.execute(queryString);
			auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

		}

		return reportMasterDAO.getControlbasedReportParameter((int) inputMap.get("nreportdetailcode"), userInfo);

	}

	@SuppressWarnings("unused")
	private List<ParameterConfiguration> parameterMappingbyId(Map<String, Object> inputMap) {

		String queryString = "select * from parameterconfiguration " + "where nreportdetailcode="
				+ inputMap.get("nreportdetailcode") + " and nreportcode=" + inputMap.get("nreportcode") + " "
				+ "and nquerybuildertablecode in (" + inputMap.get("nquerybuildertablecode") + ") "
				+ "and nreportparametercode in (" + inputMap.get("ncontrolbasedparameter") + ")" + "and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";

		return (List<ParameterConfiguration>) jdbcTemplate.query(queryString, new ParameterConfiguration());
	}

	public List<ParameterConfiguration> getControlbasedReportParameter(int nreportvalidationcode, UserInfo userInfo)
			throws Exception {

		final String queryString = "select pc.*,pc.smappedcolumnname as stablecolumn, "
				+ "rp.sreportparametername as scontrolBasedparameter,rp.nreportparametercode as ncontrolBasedparameter  "
				+ "from   parameterconfiguration pc,reportparameter rp,reportdetails rd  "
				+ "where pc.nreportparametercode=rp.nreportparametercode "
				+ "and pc.nreportdetailcode=rd.nreportdetailcode " + "and pc.nreportdetailcode=" + nreportvalidationcode
				+ " " + "and pc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "order by 1 desc;";

		return (List<ParameterConfiguration>) jdbcTemplate.query(queryString, new ParameterConfiguration());

	}

}
