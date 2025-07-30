package com.agaramtech.qualis.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.service.controlmaster.ControlMasterDAO;
import com.agaramtech.qualis.reports.CreateVisibleSignature2;
import com.agaramtech.qualis.reports.model.ReportDetails;
import com.agaramtech.qualis.reports.model.ReportImages;
import com.agaramtech.qualis.reports.model.SubReportDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ReportDAOSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportDAOSupport.class);

	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final CommonFunction commonFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final ControlMasterDAO controlMasterDAO;

	@Value("${spring.datasource.password}")
	private String password;
	@Value("${spring.datasource.username}")
	private String username;
	@Value("${spring.datasource.url}")
	private String url;

	@SuppressWarnings("unused")
	public Map<String, Object> validationCheckForNodeServer(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		int nreporttypecode = -1;
		int nregTypeCode = -1;
		int nregSubTypeCode = -1;
		int ncoareporttypecode = -1;
		int decisionStatus = -1;
		int certificateTypeCode = -1;
		int nsectionCode = -1;
		int ncontrolCode = -1;
		int napproveconfversioncode = -1;
		String conditionString = "";
		String stable = "";
		String scondition = "";
		String limitCondition = "";

		if (inputMap.containsKey("nreporttypecode")) {
			nreporttypecode = (int) inputMap.get("nreporttypecode");
		}
		if (inputMap.containsKey("ncoareporttypecode")) {
			ncoareporttypecode = (int) inputMap.get("ncoareporttypecode");
		}
		if (inputMap.containsKey("nregtypecode")) {
			nregTypeCode = (int) inputMap.get("nregtypecode");
		}
		if (inputMap.containsKey("nregsubtypecode")) {
			nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
		}
		if (inputMap.containsKey("nsectioncode")) {
			nsectionCode = (int) inputMap.get("nsectioncode");
		}
		if (inputMap.containsKey("ncertificatetypecode")) {
			certificateTypeCode = (int) inputMap.get("ncertificatetypecode");
		}
		if (inputMap.containsKey("ndecisionstatus")) {
			decisionStatus = (int) inputMap.get("ndecisionstatus");
		}
		if (inputMap.containsKey("ncontrolcode")) {
			ncontrolCode = inputMap.containsKey("nreporttypecode")
					&& (int) inputMap.get("nreporttypecode") == Enumeration.ReportType.CONTROLBASED.getReporttype()
							? (int) inputMap.get("ncontrolcode")
							: -1;
		}

		if (inputMap.containsKey("napproveconfversioncode")) {
			napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		}

		if (inputMap.containsKey("nreportdetailcode")) {
			conditionString = " and rd.nreportdetailcode=" + inputMap.get("nreportdetailcode");
		} else {
			conditionString = " and coa.ncoareporttypecode=" + ncoareporttypecode + " and rm.nregtypecode="
					+ nregTypeCode + " and rm.nregsubtypecode=" + nregSubTypeCode + " and rm.napproveconfversioncode="
					+ napproveconfversioncode + "" + " and rd.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rm.nreportdecisiontypecode="
					+ decisionStatus + " and rt.nreporttypecode=" + nreporttypecode + " and rm.ncertificatetypecode = "
					+ certificateTypeCode + " and rm.ncontrolcode = " + ncontrolCode + " and rm.nsectioncode = "
					+ nsectionCode;
		}

		if (userInfo.getNformcode() == Enumeration.QualisForms.RELEASE.getqualisforms()) {
			if ((int) inputMap.get("nsampletypecode") != Enumeration.SampleType.CLINICALSPEC.getType()
					&& (int) inputMap.get("isSMTLFlow") == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
				stable += ",coaparent cp";
				scondition += " and cp.nreporttemplatecode=rm.nreporttemplatecode and cp.ncoaparentcode in ("
						+ inputMap.get("ncoaparentcode") + ") and cp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				limitCondition = " limit 1 ";
			}
			if ((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.PROJECTWISE.getcoaReportType()
					&& (int) inputMap.get("isSMTLFlow") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				stable = ",reportinfoproject rip";
				scondition = " and rip.nreporttemplatecode=rm.nreporttemplatecode and " + " rip.nprojectmastercode="
						+ (int) inputMap.get("nprojectcode") + " and rip.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			}
		}

		final String getMainReports = " select * from reportmaster rm,reportdetails rd,reporttype rt ,coareporttype coa "
				+ stable + " " + " where coa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nreporttypecode=rm.nreporttypecode " + " and coa.ncoareporttypecode=rm.ncoareporttypecode"
				+ " and rm.nreportcode=rd.nreportcode " + " and rm.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + conditionString + scondition
				+ limitCondition;

		final ReportDetails mainReport = (ReportDetails) jdbcUtilityFunction.queryForObject(getMainReports,
				ReportDetails.class, jdbcTemplate);
		map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		if (mainReport != null) {
			final String sFile = FilenameUtils.getExtension(mainReport.getSsystemfilename().toString());

			if ("mrt".equals("" + sFile + "")) {
				final String getReportPaths = "select ssettingvalue from reportsettings where nreportsettingcode ="
						+ Enumeration.ReportSettings.REPORTINGTOOL_URL.getNreportsettingcode() + " " + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				final String reportPaths = jdbcTemplate.queryForObject(getReportPaths, String.class);

				try {
					final String sURL = "http://localhost:8888/getReportNodeServer";
					RestTemplate restTemplate = new RestTemplate();
					LOGGER.info("COAParentCode====>>" + sURL);
					final String sResult = restTemplate.postForObject(sURL, "", String.class);
					LOGGER.info("ReleaseReport Compiled Successfully====>>");
					map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), sResult);

				} catch (Exception e) {
					LOGGER.info("Node server for report exception====>>" + e.getMessage());
					map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.FAILED.getreturnstatus());
				}
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> compileAndPrintReport(Map<String, Object> jasperParameters, File JRXMLFile, int qType,
			String pdfPath, String customFileName, UserInfo userInfo, final String systemFileName, int ncontrolCode,
			boolean ftpSaveNeeded, String ssignfilename, String CertificateFileName, String SecurityKey)
			throws Exception {
		Map<String, Object> fileMap = new HashMap<String, Object>();

		final String currentDateTime = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ")
				.replace("Z", "");
		final String concat = String.valueOf(userInfo.getNtranssitecode()) + currentDateTime;
		final String parameterString = jasperParameters.containsKey("sprimarykeyname")
				? "_" + jasperParameters.get("sprimarykeyname")
				: "";

		final String sexporttypeQuery = "select rd.sreportformatdetail from reportmaster rm,reportdetails rd "
				+ "where rm.nreportcode=rd.nreportcode and rm.ncontrolcode=" + ncontrolCode + " "
				+ "and rm.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ "and rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.nreportdetailcode=" + jasperParameters.get("nreportdetailcode") + " " + " and rd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		String sexporttype = jdbcTemplate.queryForObject(sexporttypeQuery, String.class);

		String sFileType = FilenameUtils.getExtension(JRXMLFile.toString());

		if (sFileType.equalsIgnoreCase("jrxml")) {

			sexporttype = "pdf";
		} else {
			sexporttype = sexporttype;
		}

		String outputFileName = systemFileName.isEmpty()
				? UUID.nameUUIDFromBytes(concat.getBytes()).toString() + parameterString + "." + sexporttype + ""
				: systemFileName;

		File filePath = new File(pdfPath);
		if (!filePath.exists()) {
			filePath.mkdir();
		}
		String sreportingtoolURL = "";
		if (jasperParameters.containsKey("sreportingtoolURL")) {
			sreportingtoolURL = jasperParameters.get("sreportingtoolURL").toString();
			jasperParameters.remove("sreportingtoolURL");
		}
		try {
			final ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(),
					false);

			jasperParameters.put("REPORT_RESOURCE_BUNDLE", resourcebundle);

			final Connection connection = jdbcTemplate.getDataSource().getConnection();
			if (qType == Enumeration.ReportQueryFormat.PLSQL.getReportQueryFormat()) {
				// need to discuss,check and change for below JRProperties
				// ,JRQueryExecuterFactory
//				JRProperties.setProperty(JRQueryExecuterFactory.QUERY_EXECUTER_FACTORY_PREFIX + "plsql",
//						"com.jaspersoft.jrx.query.PlSqlQueryExecuterFactory");
			}

			if (sFileType.equals("jrxml")) {
				// need to discuss,check and change for below
				// jasperDesign,JasperPrint,JasperReport
				// ,JasperExportManager,JasperCompileManager,JasperFillManager,JRXmlLoader
//				final JasperDesign jasperDesign = JRXmlLoader.load(JRXMLFile);
//				jasperDesign.setLanguage("java");
//				final JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
//				final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jasperParameters,
//						connection);
//				JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath + File.separatorChar + outputFileName);

				fileMap.put("filetype", "jrxml");
				LOGGER.info("1/2 : jrxml to pdf created");
			} else {
				if (sreportingtoolURL != "") {
					RestTemplate restTemplate = new RestTemplate();

					String squery = "select ssettingvalue from reportsettings where nreportsettingcode =17;";
					String sJSONFileName = jdbcTemplate.queryForObject(squery, String.class);
					sJSONFileName = sJSONFileName.replace("\\", "\\\\");

					squery = "select ssettingvalue from settings where nsettingcode = 29 and nstatus = 1;";
					String sSMTLrpt = jdbcTemplate.queryForObject(squery, String.class);
					String filein = JRXMLFile.toString().replace("\\", "\\\\\\\\");
					String fileout = pdfPath.replace("\\", "\\\\") + "\\\\" + outputFileName;
					String sreportingtoolfilename = userInfo.getSreportingtoolfilename();

					String suser = username;
					String spsw = password;
					String surl = url;
					String sDB = surl.substring(surl.lastIndexOf("/") + 1);
					String sserver = surl.substring(surl.indexOf("//") + 2, surl.lastIndexOf(":"));
					String sport = surl.substring(surl.lastIndexOf(":") + 1, surl.lastIndexOf("/"));
					String sConnString = "Server=" + sserver + ";Port=" + sport + ";Database=" + sDB + ";User=" + suser
							+ ";Pwd=" + spsw + ";";

					String sImageURL = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
					sImageURL = sImageURL.substring(0, sImageURL.lastIndexOf("/"));

					String param;
					if (jasperParameters.get("nreporttypecode")
							.equals(Enumeration.ReportType.CONTROLBASED.getReporttype())) {
						param = "{\"name\":\"" + sreportingtoolfilename + "\",\"filein\":\"" + filein
								+ "\",\"fileout\":\"" + fileout + "\",\"jsonfilename\":\"" + sJSONFileName
								+ "\",\"smtlreport\":\"" + sSMTLrpt + "\",\"sexporttype\":\"" + sexporttype
								+ "\",\"reporttype\":\"" + jasperParameters.get("nreporttypecode")
								+ "\",\"sconnectionstring\":\"" + sConnString + "\",\"parameter\":"
								+ jasperParameters.get("parameterlist") + "}";

					} else {

						param = "{\"name\":\"" + sreportingtoolfilename + "\",\"filein\":\"" + filein
								+ "\",\"fileout\":\"" + fileout + "\",\"jsonfilename\":\"" + sJSONFileName
								+ "\",\"smtlreport\":\"" + sSMTLrpt + "\",\"sexporttype\":\"" + sexporttype
								+ "\",\"reporttype\":\"" + jasperParameters.get("nreporttypecode")
								+ "\",\"sconnectionstring\":\"" + sConnString + "\",\"parameter\":{\"npreregno\":\" "
								+ jasperParameters.get("npreregno") + " \",\"ncoaparentcode\":"
								+ jasperParameters.get("ncoaparentcode") + ",\"sreportimgurl\":\" " + sImageURL + "\" "
								+ ",\"nsitecode\":" + jasperParameters.get("nsitecode") + "}}";
					}

					String url = sreportingtoolURL;
					restTemplate.postForObject(url, param, Void.class);
					LOGGER.info("COAParentCode====>>" + parameterString);
					LOGGER.info("Release Report Parameters====>>" + param);
					LOGGER.info("ReleaseReport Compiled Successfully====>>");
					final String sReportQuery = "select ssettingvalue from settings where nsettingcode = 33 and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final Settings objReportsettings = (Settings) jdbcUtilityFunction.queryForObject(sReportQuery,
							Settings.class, jdbcTemplate);
					if (objReportsettings.getSsettingvalue()
							.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))) {
						if (ssignfilename != null && ssignfilename.length() > 0 && CertificateFileName != null
								&& CertificateFileName.length() > 0) {
							File ofilesign = new File(ssignfilename);
							File ofilecertificate = new File(CertificateFileName);
							String scustompath = "";
							if (ofilesign.getName() != null && ofilesign.getName().length() > 0
									&& ofilecertificate.getName() != null && ofilecertificate.getName().length() > 0) {
								Map<String, Object> map = new HashMap<>();
								short screenFormcode = userInfo.getNformcode();
								userInfo.setNformcode((short) 3);
								final Map<String, Object> imageMap = new HashMap<String, Object>();
								final List<ControlMaster> controlList = (List<ControlMaster>) controlMasterDAO
										.getUploadControlsByFormCode(userInfo).getBody();

								scustompath = ofilesign.getPath().substring(0, ofilesign.getPath().lastIndexOf("\\"))
										+ "\\";

								imageMap.put("SignImage_fileName", ofilesign.getName());
								imageMap.put("SignImage_customName", "");
								imageMap.put("SignImage_path", scustompath);

								scustompath = ofilecertificate.getPath().substring(0,
										ofilecertificate.getPath().lastIndexOf("\\")) + "\\";
								imageMap.put("UserImage_fileName", ofilecertificate.getName());
								imageMap.put("UserImage_customName", "");
								imageMap.put("UserImage_path", scustompath);

								map = ftpUtilityFunction.multiPathMultiFileDownloadUsingFtp(imageMap, controlList,
										userInfo, "");

								if (!map.keySet().isEmpty() && !map.get("518").equals("false")
										&& !map.get("519").equals("false")) {
									digitalsign(fileout, ssignfilename, CertificateFileName, SecurityKey);
								}

								userInfo.setNformcode(screenFormcode);
							}
						}
					}
				}

				fileMap.put("filetype", "mrt");
				fileMap.put("sourcefile", JRXMLFile.toString());

				Map<String, Object> second_Map = copyMap(jasperParameters);

				String json = new ObjectMapper().writeValueAsString(second_Map);
				fileMap.put("sourceparameter", json);
			}

			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			LOGGER.info("2/2 : filemap " + fileMap);
			//
			String sQuery = "select ssettingvalue from reportsettings " + " where nreportsettingcode = "
					+ Enumeration.ReportSettings.REPORT_DOWNLOAD_URL.getNreportsettingcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			String fileDownloadURL = jdbcTemplate.queryForObject(sQuery, String.class);
			fileMap.put("filepath", fileDownloadURL + outputFileName);

			//
			if (ftpSaveNeeded) {
				Thread.sleep(3000); // ALPD-4224
				File f = new File(pdfPath + outputFileName);
				Boolean fileFlag = f.exists();
				LOGGER.info("file     : " + f);
				LOGGER.info("fileFlag : " + fileFlag);
				if (fileFlag) {
					fileMap = ftpUtilityFunction.FileUploadaAndDownloadinFtp(pdfPath, outputFileName, customFileName,
							ncontrolCode, userInfo);
				}

			}
			final String downloadPath = getReportDownloadPathName();
			fileMap.put("reportViewURL", downloadPath + outputFileName);
			fileMap.put("outputFileName", outputFileName);

			return fileMap;
		} catch (ResourceAccessException e) {
			LOGGER.info(e.getMessage());
			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "ConnectionRefused");
			fileMap.put("Exception", "ConnectionRefused");
			return fileMap;

		} catch (Exception e) {
			LOGGER.info(e.getMessage());
			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), e.getMessage());
			return fileMap;
		}
	}

	public Map<String, Object> getDynamicReports(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		String folderName = "";
		String sJRXMLname;
		String filePath;
		int nreporttypecode = -1;
		int nregTypeCode = -1;
		int nregSubTypeCode = -1;
		int ncoareporttypecode = -1;
		int decisionStatus = -1;
		int certificateTypeCode = -1;
		int nsectionCode = -1;
		int ncontrolCode = -1;
		int napproveconfversioncode = -1;
		String conditionString = "";

		final String getfilePath = "select ssettingvalue from reportsettings " + " where nreportsettingcode = "
				+ Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		filePath = jdbcTemplate.queryForObject(getfilePath, String.class);

		if (inputMap.containsKey("nreporttypecode")) {
			nreporttypecode = (int) inputMap.get("nreporttypecode");
		}
		if (inputMap.containsKey("ncoareporttypecode")) {
			ncoareporttypecode = (int) inputMap.get("ncoareporttypecode");
		}
		if (inputMap.containsKey("nregtypecode")) {
			nregTypeCode = (int) inputMap.get("nregtypecode");
		}
		if (inputMap.containsKey("nregsubtypecode")) {
			nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
		}
		if (inputMap.containsKey("nsectioncode")) {
			nsectionCode = (int) inputMap.get("nsectioncode");
		}
		if (inputMap.containsKey("ncertificatetypecode")) {
			certificateTypeCode = (int) inputMap.get("ncertificatetypecode");
		}
		if (inputMap.containsKey("ndecisionstatus")) {
			decisionStatus = (int) inputMap.get("ndecisionstatus");
		}
		if (inputMap.containsKey("ncontrolcode")) {
			ncontrolCode = (int) inputMap.get("ncontrolcode");
		}

		if (inputMap.containsKey("napproveconfversioncode")) {
			napproveconfversioncode = (int) inputMap.get("napproveconfversioncode");
		}

		if (inputMap.containsKey("nreportdetailcode")) {
			conditionString = " and rd.nreportdetailcode=" + inputMap.get("nreportdetailcode");
		} else {
			conditionString = " and coa.ncoareporttypecode=" + ncoareporttypecode + " and rm.nregtypecode="
					+ nregTypeCode + " and rm.nregsubtypecode=" + nregSubTypeCode + " and rm.napproveconfversioncode="
					+ napproveconfversioncode + "" + " and rd.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and rm.nreportdecisiontypecode="
					+ decisionStatus + " and rt.nreporttypecode=" + nreporttypecode + " and rm.ncertificatetypecode = "
					+ certificateTypeCode + " and rm.ncontrolcode = " + ncontrolCode + " and rm.nsectioncode = "
					+ nsectionCode;
		}

		String stable = "";
		String scondition = "";
		String limitCondition = "";
		if (userInfo.getNformcode() == Enumeration.QualisForms.RELEASE.getqualisforms()) {
			if ((int) inputMap.get("nsampletypecode") != Enumeration.SampleType.CLINICALSPEC.getType()
					&& (int) inputMap.get("isSMTLFlow") == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
				stable += ",coaparent cp";
				scondition += " and cp.nreporttemplatecode=rm.nreporttemplatecode and cp.ncoaparentcode in ("
						+ inputMap.get("ncoaparentcode") + ") and cp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				limitCondition = " limit 1 ";
			}
			if ((int) inputMap.get("ncoareporttypecode") == Enumeration.COAReportType.PROJECTWISE.getcoaReportType()
					&& (int) inputMap.get("isSMTLFlow") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				stable = ",reportinfoproject rip";
				scondition = " and rip.nreporttemplatecode=rm.nreporttemplatecode and " + " rip.nprojectmastercode="
						+ (int) inputMap.get("nprojectcode") + " and rip.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			}
		}

		final String getMainReports = " select * from reportmaster rm,reportdetails rd,reporttype rt ,coareporttype coa "
				+ stable + " " + " where coa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nreporttypecode=rm.nreporttypecode " + " and coa.ncoareporttypecode=rm.ncoareporttypecode"
				+ " and rm.nreportcode=rd.nreportcode " + " and rm.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + conditionString + scondition
				+ limitCondition;

		ReportDetails mainReport = (ReportDetails) jdbcUtilityFunction.queryForObject(getMainReports,
				ReportDetails.class, jdbcTemplate);

		if (mainReport != null) {
			if (!mainReport.getSsystemfilename().isEmpty()) {
				sJRXMLname = mainReport.getSsystemfilename();
				int qType = mainReport.getNisplsqlquery();
				LOGGER.info("JRXML File From FTP======================>" + sJRXMLname);
				LOGGER.info("JRXML File Query Type====================>" + qType);
				final String getSubReports = " select * from subreportdetails " + " where nreportdetailcode="
						+ mainReport.getNreportdetailcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				List<SubReportDetails> subReports = jdbcTemplate.query(getSubReports, new SubReportDetails());

				final String getReportImages = " select * from reportimages" + " where nreportdetailcode="
						+ mainReport.getNreportdetailcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				List<ReportImages> reportImages = jdbcTemplate.query(getReportImages, new ReportImages());
				List<String> originalFileNames = new ArrayList<String>();
				List<String> systemFileNames = new ArrayList<String>();
				folderName = mainReport.getSreportname() + File.separatorChar;
				if (subReports.size() > 0) {
					for (int i = 0; i < subReports.size(); i++) {
						File file = new File(filePath + folderName + subReports.get(i).getSfilename());
						if (!file.exists()) {
							originalFileNames.add(subReports.get(i).getSfilename());
							systemFileNames.add(subReports.get(i).getSsystemfilename());
						}
					}
				}
				if (reportImages.size() > 0) {
					for (int i = 0; i < reportImages.size(); i++) {
						File file = new File(filePath + folderName + reportImages.get(i).getSfilename());
						if (!file.exists()) {
							originalFileNames.add(reportImages.get(i).getSfilename());
							systemFileNames.add(reportImages.get(i).getSsystemfilename());
						}
					}
				}
				String sfilesharedfolder = filePath + folderName + sJRXMLname;
				File JRXMLFile = new File(sfilesharedfolder);
				if (!JRXMLFile.exists()) {
					originalFileNames.add(sJRXMLname);
					systemFileNames.add(sJRXMLname);
				}
				if (originalFileNames.size() > 0) {
					if(JRXMLFile.exists()) {
						originalFileNames.add(sJRXMLname);
						systemFileNames.add(sJRXMLname);
					}
					ftpUtilityFunction.multiFileDownloadFromFTP(systemFileNames, -1, userInfo, filePath + folderName,
							originalFileNames);

				}
				if (subReports.size() > 0) {
					for (SubReportDetails subreport : subReports) {
						String JRXMLName = filePath + folderName + subreport.getSfilename();
						String jasperName = filePath + folderName
								+ subreport.getSfilename().substring(0, subreport.getSfilename().lastIndexOf("."))
								+ ".jasper";
						// need to discuss,check and change for below JasperCompileManager
						// JasperCompileManager.compileReportToFile(JRXMLName, jasperName);
					}
				}
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				returnMap.put("JRXMLname", sJRXMLname);
				returnMap.put("folderName", folderName);
				returnMap.put("qType", qType);
				returnMap.put("nreportdetailcode", mainReport.getNreportdetailcode());

			} else {
				returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						commonFunction.getMultilingualMessage("IDS_NOREPORTFOUND", userInfo.getSlanguagefilename()));
			}

		} else {
			returnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					commonFunction.getMultilingualMessage("IDS_CONFIGUREREPORT", userInfo.getSlanguagefilename()));
		}
		return returnMap;
	}

	// need to change exception DocumentException
	@SuppressWarnings("unused")
	public void digitalsign(String docFile, String ssignfilename, String CertificateFileName, String SecurityKey)
			throws FileNotFoundException, IOException, KeyStoreException, NoSuchAlgorithmException,
			UnrecoverableKeyException, CertificateException {
//			throws FileNotFoundException, IOException, DocumentException, KeyStoreException, NoSuchAlgorithmException,
//			CertificateException, UnrecoverableKeyException {

		LOGGER.info("digitalsign:" + docFile + ":" + ssignfilename + ":" + CertificateFileName);
		String sFileout = docFile;
		File signedDocumentFile;
		String substring = docFile.substring(0, docFile.lastIndexOf('.'));
		String sPDFFilename = substring + "_Temp.pdf";

		String[] args = new String[4];
		String curdir = System.getProperty("user.dir");

		args[0] = CertificateFileName;

		args[1] = SecurityKey;

		args[2] = docFile;

		args[3] = ssignfilename;
		File ksFile = new File(args[0]);
		KeyStore keystore = KeyStore.getInstance("PKCS12");
		char[] pin = args[1].toCharArray();
		try (InputStream is = new FileInputStream(ksFile)) {
			keystore.load(is, pin);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return;
		}

		File documentFile = new File(args[2]);

		CreateVisibleSignature2 signing = new CreateVisibleSignature2(keystore, pin.clone());

		LOGGER.info("args[0]:" + args[0]);
		LOGGER.info("args[1]:" + args[0]);
		LOGGER.info("args[2]:" + args[0]);
		LOGGER.info("args[3]:" + args[0]);
		LOGGER.info("sFileout:" + sFileout);

		signing.DigitalSignature(args[0], args[1], args[2], args[3], sFileout);

	}

	public String getReportDownloadPathName() throws Exception {
		final String downloadPath = "select * from settings where nsettingcode="
				+ Enumeration.Settings.COAREPORTVIEW_URL.getNsettingcode() + " " + "and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final Settings surl = (Settings) jdbcUtilityFunction.queryForObject(downloadPath, Settings.class, jdbcTemplate);
		return surl.getSsettingvalue();
	}

	public Map<String, Object> copyMap(Map<String, Object> original) {

		Map<String, Object> second_Map = new HashMap<>();

		for (Map.Entry<String, Object> entry : original.entrySet()) {

			if (!entry.getKey().equals("REPORT_RESOURCE_BUNDLE") // &&
			)
				second_Map.put(entry.getKey().trim(), entry.getValue());
		}

		return second_Map;
	}

	// ALPD-5116
	// Release screen - report view using stimulsoft viewer.
	// -Janakumar, Subashini
	public Map<String, Object> compileAndPrintReportForSchedular(Map<String, Object> jasperParameters, File JRXMLFile,
			int qType, String pdfPath, String customFileName, UserInfo userInfo, final String systemFileName,
			int ncontrolCode, boolean ftpSaveNeeded, String ssignfilename, String CertificateFileName,
			String SecurityKey) throws Exception {
		Map<String, Object> fileMap = new HashMap<String, Object>();

		final String currentDateTime = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ")
				.replace("Z", "");
		final String concat = String.valueOf(userInfo.getNtranssitecode()) + currentDateTime;
		final String parameterString = jasperParameters.containsKey("sprimarykeyname")
				? "_" + jasperParameters.get("sprimarykeyname")
				: "";

		final String sexporttypeQuery = "select rd.sreportformatdetail from reportmaster rm,reportdetails rd "
				+ "where rm.nreportcode=rd.nreportcode and rm.ncontrolcode=" + ncontrolCode + " "
				+ "and rm.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ "and rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.nreportdetailcode=" + jasperParameters.get("nreportdetailcode") + " " + " and rm.nsitecode = "
				+ userInfo.getNmastersitecode() + " and rd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		String sexporttype = (String) jdbcUtilityFunction.queryForObject(sexporttypeQuery, String.class, jdbcTemplate);

		String sFileType = FilenameUtils.getExtension(JRXMLFile.toString());

		if (sFileType.equalsIgnoreCase("jrxml")) { // ALPD-4316 janakumar sFile change to sFileType

			sexporttype = "pdf";
		}

		String outputFileName = systemFileName.isEmpty()
				? UUID.nameUUIDFromBytes(concat.getBytes()).toString() + parameterString + "." + sexporttype + ""
				: systemFileName;

		File filePath = new File(pdfPath);
		if (!filePath.exists()) {
			filePath.mkdir();
		}
		String sreportingtoolURL = "";
		if (jasperParameters.containsKey("sreportingtoolURL")) {
			sreportingtoolURL = jasperParameters.get("sreportingtoolURL").toString();
			jasperParameters.remove("sreportingtoolURL");
		}
		try {
			final ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(),
					false);

			jasperParameters.put("REPORT_RESOURCE_BUNDLE", resourcebundle);

//					final Connection connection = jdbcTemplate.getDataSource().getConnection();
			if (qType == Enumeration.ReportQueryFormat.PLSQL.getReportQueryFormat()) {
//						JRProperties.setProperty(JRQueryExecuterFactory.QUERY_EXECUTER_FACTORY_PREFIX + "plsql",
//								"com.jaspersoft.jrx.query.PlSqlQueryExecuterFactory");
			}

			if (sFileType.equals("jrxml")) {
//						final JasperDesign jasperDesign = JRXmlLoader.load(JRXMLFile);
//						jasperDesign.setLanguage("java");
//						final JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
//						final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jasperParameters, connection);
//						JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath +  File.separatorChar + outputFileName);
				fileMap.put("filetype", "jrxml");
				LOGGER.info("1/2 : jrxml to pdf created");
			} else {
				if (sreportingtoolURL != "") {
					RestTemplate restTemplate = new RestTemplate();

					String squery = "select ssettingvalue from reportsettings where nreportsettingcode =17;";
					String sJSONFileName = (String) jdbcUtilityFunction.queryForObject(squery, String.class,
							jdbcTemplate);
					sJSONFileName = sJSONFileName.replace("\\", "\\\\");

					squery = "select ssettingvalue from settings where nsettingcode = 29 and nstatus = 1;";
					String sSMTLrpt = (String) jdbcUtilityFunction.queryForObject(squery, String.class, jdbcTemplate);
					String filein = JRXMLFile.toString().replace("\\", "\\\\\\\\");
					String fileout = pdfPath.replace("\\", "\\\\") + "\\\\" + outputFileName;
					String sreportingtoolfilename = userInfo.getSreportingtoolfilename();

					String suser = username;
					String spsw = password;
					String surl = url;
					String sDB = surl.substring(surl.lastIndexOf("/") + 1);
					String sserver = surl.substring(surl.indexOf("//") + 2, surl.lastIndexOf(":"));
					String sport = surl.substring(surl.lastIndexOf(":") + 1, surl.lastIndexOf("/"));
					String sConnString = "Server=" + sserver + ";Port=" + sport + ";Database=" + sDB + ";User=" + suser
							+ ";Pwd=" + spsw + ";";

					String sImageURL = "";

					String param;
					if (jasperParameters.get("nreporttypecode")
							.equals(Enumeration.ReportType.CONTROLBASED.getReporttype())) {
						param = "{\"name\":\"" + sreportingtoolfilename + "\",\"filein\":\"" + filein
								+ "\",\"fileout\":\"" + fileout + "\",\"jsonfilename\":\"" + sJSONFileName
								+ "\",\"smtlreport\":\"" + sSMTLrpt + "\",\"sexporttype\":\"" + sexporttype
								+ "\",\"reporttype\":\"" + jasperParameters.get("nreporttypecode")
								+ "\",\"sconnectionstring\":\"" + sConnString + "\",\"parameter\":"
								+ jasperParameters.get("parameterlist") + "}";

					} else {

						param = "{\"name\":\"" + sreportingtoolfilename + "\",\"filein\":\"" + filein
								+ "\",\"fileout\":\"" + fileout + "\",\"jsonfilename\":\"" + sJSONFileName
								+ "\",\"smtlreport\":\"" + sSMTLrpt + "\",\"sexporttype\":\"" + sexporttype
								+ "\",\"reporttype\":\"" + jasperParameters.get("nreporttypecode")
								+ "\",\"sconnectionstring\":\"" + sConnString + "\",\"parameter\":{\"npreregno\":\" "
								+ jasperParameters.get("npreregno") + " \",\"ncoaparentcode\":"
								+ jasperParameters.get("ncoaparentcode") + ",\"sreportimgurl\":\" " + sImageURL + "\" "
								+ ",\"nsitecode\":" + jasperParameters.get("nsitecode") + "}}";
					}

					String url = sreportingtoolURL;

					restTemplate.postForObject(url, param, Void.class);
					LOGGER.info("COAParentCode====>>" + parameterString);
					LOGGER.info("Release Report Parameters====>>" + param);
					LOGGER.info("ReleaseReport Compiled Successfully====>>");

					final String sReportQuery = "select ssettingvalue from settings where nsettingcode = 33 and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final Settings objReportsettings = (Settings) jdbcUtilityFunction.queryForObject(sReportQuery,
							Settings.class, jdbcTemplate);
					if (objReportsettings.getSsettingvalue()
							.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))) {
						if (ssignfilename != null && ssignfilename.length() > 0 && CertificateFileName != null
								&& CertificateFileName.length() > 0) {
							File ofilesign = new File(ssignfilename);
							File ofilecertificate = new File(CertificateFileName);
							String scustompath = "";
							if (ofilesign.getName() != null && ofilesign.getName().length() > 0
									&& ofilecertificate.getName() != null && ofilecertificate.getName().length() > 0) {
								Map<String, Object> map = new HashMap<>();
								short screenFormcode = userInfo.getNformcode();
								userInfo.setNformcode((short) 3);
								final Map<String, Object> imageMap = new HashMap<String, Object>();
								final List<ControlMaster> controlList = (List<ControlMaster>) controlMasterDAO
										.getUploadControlsByFormCode(userInfo).getBody();

								scustompath = ofilesign.getPath().substring(0, ofilesign.getPath().lastIndexOf("\\"))
										+ "\\";

								imageMap.put("SignImage_fileName", ofilesign.getName());
								imageMap.put("SignImage_customName", "");
								imageMap.put("SignImage_path", scustompath);

								scustompath = ofilecertificate.getPath().substring(0,
										ofilecertificate.getPath().lastIndexOf("\\")) + "\\";
								imageMap.put("UserImage_fileName", ofilecertificate.getName());
								imageMap.put("UserImage_customName", "");
								imageMap.put("UserImage_path", scustompath);

								map = ftpUtilityFunction.multiPathMultiFileDownloadUsingFtp(imageMap, controlList,
										userInfo, "");

								if (!map.keySet().isEmpty() && !map.get("518").equals("false")
										&& !map.get("519").equals("false")) {
									digitalsign(fileout, ssignfilename, CertificateFileName, SecurityKey);
								}

								userInfo.setNformcode(screenFormcode);
							}
						}
					}
				}

				fileMap.put("filetype", "mrt");
				fileMap.put("sourcefile", JRXMLFile.toString());

				Map<String, Object> second_Map = copyMap(jasperParameters);

				String json = new ObjectMapper().writeValueAsString(second_Map);
				fileMap.put("sourceparameter", json);

			}

			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			LOGGER.info("2/2 : filemap " + fileMap);
			String sQuery = "select ssettingvalue from reportsettings " + " where nreportsettingcode = "
					+ Enumeration.ReportSettings.REPORT_DOWNLOAD_URL.getNreportsettingcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			String fileDownloadURL = (String) jdbcUtilityFunction.queryForObject(sQuery, String.class, jdbcTemplate);
			fileMap.put("filepath", fileDownloadURL + outputFileName);

			if (ftpSaveNeeded) {
				Thread.sleep(5000); // ALPD-4224
				File f = new File(pdfPath + outputFileName);
				Boolean fileFlag = f.exists();
				LOGGER.info("file     : " + f);
				LOGGER.info("fileFlag : " + fileFlag);
				if (fileFlag) {
					fileMap = ftpUtilityFunction.FileUploadaAndDownloadinFtp(pdfPath, outputFileName, customFileName,
							ncontrolCode, userInfo);
				}

			}

			// Added by sonia for ALPD-4360 on 20-Jun-2024 Auto Download Reports
			final String downloadPath = getReportDownloadPathName();
			fileMap.put("reportViewURL", downloadPath + outputFileName);
			fileMap.put("outputFileName", outputFileName);

			return fileMap;
		} catch (ResourceAccessException e) {
			LOGGER.info(e.getMessage());
			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "ConnectionRefused");
			fileMap.put("Exception", "ConnectionRefused");
			return fileMap;

		} catch (Exception e) {
			LOGGER.info(e.getMessage());
			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), e.getMessage());
			return fileMap;
		}
	}

	// Added by L.Subashini for stimulsoft viewer display of reports -07-12-2024
	public Map<String, Object> compileAndPrintNonJasperReport(Map<String, Object> jasperParameters, File JRXMLFile,
			String pdfPath, String customFileName, UserInfo userInfo, String systemFileName, int ncontrolCode,
			boolean ftpSaveNeeded, String ssignfilename, String CertificateFileName, String SecurityKey)
			throws Exception {

		Map<String, Object> fileMap = new HashMap<String, Object>();

		final String sexporttypeQuery = "select rd.sreportformatdetail from reportmaster rm,reportdetails rd "
				+ "where rm.nreportcode=rd.nreportcode and rm.ncontrolcode=" + ncontrolCode + " "
				+ "and rm.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ "and rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.nreportdetailcode=" + jasperParameters.get("nreportdetailcode") + " " + " and rd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		String sexporttype = jdbcTemplate.queryForObject(sexporttypeQuery, String.class);

		final String currentDateTime = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ")
				.replace("Z", "");
		final String concat = String.valueOf(userInfo.getNtranssitecode()) + currentDateTime;
		final String parameterString = jasperParameters.containsKey("sprimarykeyname")
				? "_" + jasperParameters.get("sprimarykeyname")
				: "";

		String outputFileName = systemFileName.isEmpty()
				? UUID.nameUUIDFromBytes(concat.getBytes()).toString() + parameterString + "." + sexporttype + ""
				: systemFileName;

		final File filePath = new File(pdfPath);
		if (!filePath.exists()) {
			filePath.mkdir();
		}

		try {
			final ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(),
					false);

			jasperParameters.put("REPORT_RESOURCE_BUNDLE", resourcebundle);

			final String sreportingtoolURL = jasperParameters.get("sreportingtoolURL").toString();

			if (sreportingtoolURL != "") {
				RestTemplate restTemplate = new RestTemplate();

				String squery = "select ssettingvalue from reportsettings where nreportsettingcode =17;";
				String sJSONFileName = jdbcTemplate.queryForObject(squery, String.class);
				sJSONFileName = sJSONFileName.replace("\\", "\\\\");

				String filein = JRXMLFile.toString().replace("\\", "\\\\\\\\");
				String fileout = pdfPath.replace("\\", "\\\\") + "\\\\" + outputFileName;
				String sreportingtoolfilename = userInfo.getSreportingtoolfilename();

				String suser = username;
				String spsw = password;
				String surl = url;
				String sDB = surl.substring(surl.lastIndexOf("/") + 1);
				String sserver = surl.substring(surl.indexOf("//") + 2, surl.lastIndexOf(":"));
				String sport = surl.substring(surl.lastIndexOf(":") + 1, surl.lastIndexOf("/"));
				String sConnString = "Server=" + sserver + ";Port=" + sport + ";Database=" + sDB + ";User=" + suser
						+ ";Pwd=" + spsw + ";";

				String sImageURL = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
				sImageURL = sImageURL.substring(0, sImageURL.lastIndexOf("/"));

				squery = "select ssettingvalue from settings where nsettingcode = 29 and nstatus = 1;";
				String sSMTLrpt = jdbcTemplate.queryForObject(squery, String.class);

				String param;
				if (jasperParameters.get("nreporttypecode")
						.equals(Enumeration.ReportType.CONTROLBASED.getReporttype())) {
					param = "{\"name\":\"" + sreportingtoolfilename + "\",\"filein\":\"" + filein + "\",\"fileout\":\""
							+ fileout + "\",\"jsonfilename\":\"" + sJSONFileName + "\",\"smtlreport\":\"" + sSMTLrpt
							+ "\",\"sexporttype\":\"" + sexporttype + "\",\"reporttype\":\""
							+ jasperParameters.get("nreporttypecode") + "\",\"sconnectionstring\":\"" + sConnString
							+ "\",\"parameter\":" + jasperParameters.get("parameterlist") + "}";

				} else {

					param = "{\"name\":\"" + sreportingtoolfilename + "\",\"filein\":\"" + filein + "\",\"fileout\":\""
							+ fileout + "\",\"jsonfilename\":\"" + sJSONFileName + "\",\"smtlreport\":\"" + sSMTLrpt
							+ "\",\"sexporttype\":\"" + sexporttype + "\",\"reporttype\":\""
							+ jasperParameters.get("nreporttypecode") + "\",\"sconnectionstring\":\"" + sConnString
							+ "\",\"parameter\":{\"npreregno\":\" " + jasperParameters.get("npreregno")
							+ " \",\"ncoaparentcode\":" + jasperParameters.get("ncoaparentcode")
							+ ",\"sreportimgurl\":\" " + sImageURL + "\" " + ",\"nsitecode\":"
							+ jasperParameters.get("nsitecode") + "}}";
				}

				String url = sreportingtoolURL;

				restTemplate.postForObject(url, param, Void.class);

				LOGGER.info("Release Report Parameters====>>" + param);
				LOGGER.info("ReleaseReport Compiled Successfully====>>");

				final String sReportQuery = "select ssettingvalue from settings where nsettingcode = 33 and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Settings objReportsettings = (Settings) jdbcTemplate.queryForObject(sReportQuery, new Settings());

				if (objReportsettings.getSsettingvalue()
						.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))) {
					// Digital Signature
					if (ssignfilename != null && ssignfilename.length() > 0 && CertificateFileName != null
							&& CertificateFileName.length() > 0) {
						File ofilesign = new File(ssignfilename);
						File ofilecertificate = new File(CertificateFileName);
						String scustompath = "";

						if (ofilesign.getName() != null && ofilesign.getName().length() > 0
								&& ofilecertificate.getName() != null && ofilecertificate.getName().length() > 0) {
							Map<String, Object> map = new HashMap<>();
							short screenFormcode = userInfo.getNformcode();
							userInfo.setNformcode((short) 3);
							final Map<String, Object> imageMap = new HashMap<String, Object>();
							final List<ControlMaster> controlList = (List<ControlMaster>) controlMasterDAO
									.getUploadControlsByFormCode(userInfo).getBody();

							scustompath = ofilesign.getPath().substring(0, ofilesign.getPath().lastIndexOf("\\"))
									+ "\\";

							imageMap.put("SignImage_fileName", ofilesign.getName());
							imageMap.put("SignImage_customName", "");
							imageMap.put("SignImage_path", scustompath);

							scustompath = ofilecertificate.getPath().substring(0,
									ofilecertificate.getPath().lastIndexOf("\\")) + "\\";

							imageMap.put("UserImage_fileName", ofilecertificate.getName());
							imageMap.put("UserImage_customName", "");
							imageMap.put("UserImage_path", scustompath);

							map = ftpUtilityFunction.multiPathMultiFileDownloadUsingFtp(imageMap, controlList, userInfo,
									"");

							if (!map.keySet().isEmpty() && !map.get("518").equals("false")
									&& !map.get("519").equals("false")) {
								digitalsign(fileout, ssignfilename, CertificateFileName, SecurityKey);
							}

							userInfo.setNformcode(screenFormcode);
						}
					}
				}
			}

			fileMap.put("filetype", "mrt");
			fileMap.put("sourcefile", JRXMLFile.toString());

			Map<String, Object> second_Map = copyMap(jasperParameters);

			final String json = new ObjectMapper().writeValueAsString(second_Map);
			fileMap.put("sourceparameter", json);

			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			LOGGER.info("2/2 : filemap " + fileMap);

			final String sQuery = "select ssettingvalue from reportsettings " + " where nreportsettingcode = "
					+ Enumeration.ReportSettings.REPORT_DOWNLOAD_URL.getNreportsettingcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final String fileDownloadURL = jdbcTemplate.queryForObject(sQuery, String.class);
			fileMap.put("filepath", fileDownloadURL + outputFileName);

			if (ftpSaveNeeded) {
				Thread.sleep(5000); // ALPD-4224
				File f = new File(pdfPath + outputFileName);
				Boolean fileFlag = f.exists();
				LOGGER.info("file     : " + f);
				LOGGER.info("fileFlag : " + fileFlag);
				if (fileFlag) {
					fileMap = ftpUtilityFunction.FileUploadaAndDownloadinFtp(pdfPath, outputFileName, customFileName,
							ncontrolCode, userInfo);
				}

			}

			// Added by sonia for ALPD-4360 on 20-Jun-2024 Auto Download Reports
			final String downloadPath = getReportDownloadPathName();
			fileMap.put("reportViewURL", downloadPath + outputFileName);
			fileMap.put("outputFileName", outputFileName);

			return fileMap;
		} catch (ResourceAccessException e) {
			LOGGER.info(e.getMessage());
			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "ConnectionRefused");
			fileMap.put("Exception", "ConnectionRefused");
			return fileMap;

		} catch (Exception e) {
			LOGGER.info(e.getMessage());
			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), e.getMessage());
			return fileMap;
		}
	}

	// Added by L.Subashini for stimulsoft viewer display of reports -07-12-2024
	public Map<String, Object> compileAndPrintJasperReport(Map<String, Object> jasperParameters, File JRXMLFile,
			int qType, String pdfPath, String customFileName, UserInfo userInfo, final String systemFileName,
			int ncontrolCode, boolean ftpSaveNeeded) throws Exception {

		Map<String, Object> fileMap = new HashMap<String, Object>();

		final String currentDateTime = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ")
				.replace("Z", "");
		final String concat = String.valueOf(userInfo.getNtranssitecode()) + currentDateTime;
		final String parameterString = jasperParameters.containsKey("sprimarykeyname")
				? "_" + jasperParameters.get("sprimarykeyname")
				: "";

		final String outputFileName = systemFileName.isEmpty()
				? UUID.nameUUIDFromBytes(concat.getBytes()).toString() + parameterString + ".pdf"
				: systemFileName;

		final File filePath = new File(pdfPath);
		if (!filePath.exists()) {
			filePath.mkdir();
		}

		if (jasperParameters.containsKey("sreportingtoolURL")) {
			jasperParameters.remove("sreportingtoolURL");
		}

		try {
			final ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(),
					false);
			jasperParameters.put("REPORT_RESOURCE_BUNDLE", resourcebundle);

			final Connection connection = jdbcTemplate.getDataSource().getConnection();
			if (qType == Enumeration.ReportQueryFormat.PLSQL.getReportQueryFormat()) {
//				JRProperties.setProperty(JRQueryExecuterFactory.QUERY_EXECUTER_FACTORY_PREFIX + "plsql",
//						"com.jaspersoft.jrx.query.PlSqlQueryExecuterFactory");
			}

			final String sFileType = FilenameUtils.getExtension(JRXMLFile.toString());

			if (sFileType.equals("jrxml")) {
//				final JasperDesign jasperDesign = JRXmlLoader.load(JRXMLFile);
//				jasperDesign.setLanguage("java");
//			
//				final JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
//				final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jasperParameters, connection);
//				
//				JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath +  File.separatorChar + outputFileName);
				fileMap.put("filetype", "jrxml");

				LOGGER.info("PDF created for JRXML");
			}

			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			LOGGER.info("2/2 : filemap " + fileMap);

			String sQuery = "select ssettingvalue from reportsettings " + " where nreportsettingcode = "
					+ Enumeration.ReportSettings.REPORT_DOWNLOAD_URL.getNreportsettingcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			String fileDownloadURL = jdbcTemplate.queryForObject(sQuery, String.class);
			fileMap.put("filepath", fileDownloadURL + outputFileName);

			if (ftpSaveNeeded) {
				Thread.sleep(5000);

				// ALPD-4224
				File f = new File(pdfPath + outputFileName);
				Boolean fileFlag = f.exists();
				LOGGER.info("file     : " + f);
				LOGGER.info("fileFlag : " + fileFlag);
				if (fileFlag) {
					fileMap = ftpUtilityFunction.FileUploadaAndDownloadinFtp(pdfPath, outputFileName, customFileName,
							ncontrolCode, userInfo);
				}

			}

			// Added by sonia for ALPD-4360 on 20-Jun-2024 Auto Download Reports
			final String downloadPath = getReportDownloadPathName();
			fileMap.put("reportViewURL", downloadPath + outputFileName);
			fileMap.put("outputFileName", outputFileName);

			return fileMap;
		} catch (ResourceAccessException e) {
			LOGGER.info(e.getMessage());
			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "ConnectionRefused");
			fileMap.put("Exception", "ConnectionRefused");
			return fileMap;

		} catch (Exception e) {
			LOGGER.info(e.getMessage());
			fileMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), e.getMessage());
			return fileMap;
		}
	}

}
