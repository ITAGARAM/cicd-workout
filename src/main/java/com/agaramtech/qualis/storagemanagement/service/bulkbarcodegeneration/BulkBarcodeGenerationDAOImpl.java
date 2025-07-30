package com.agaramtech.qualis.storagemanagement.service.bulkbarcodegeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.basemaster.model.Barcode;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectType;
import com.agaramtech.qualis.storagemanagement.model.BulkBarcodeGeneration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BulkBarcodeGenerationDAOImpl implements BulkBarcodeGenerationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkBarcodeGenerationDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all active bulkbarcodegeneration for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         bulkbarcodegeneration
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBulkBarcodeGeneration(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final ObjectMapper mapper = new ObjectMapper();
		final String currentUIDate = (String) inputMap.get("currentdate");
		String fromDate = "";
		String toDate = "";
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			outputMap.put("FromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("ToDate", mapObject.get("ToDateWOUTC"));
			outputMap.put("realFromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("realToDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(inputMap.get("fromDate").toString(), dbPattern)
					.format(uiPattern);
			final String toDateUI = LocalDateTime.parse(inputMap.get("toDate").toString(), dbPattern).format(uiPattern);
			outputMap.put("FromDate", fromDateUI);
			outputMap.put("ToDate", toDateUI);
			outputMap.put("realFromDate", fromDateUI);
			outputMap.put("realToDate", toDateUI);
			fromDate = dateUtilityFunction.instantDateToString(
					dateUtilityFunction.convertStringDateToUTC(inputMap.get("fromDate").toString(), userInfo, true));
			toDate = dateUtilityFunction.instantDateToString(
					dateUtilityFunction.convertStringDateToUTC(inputMap.get("toDate").toString(), userInfo, true));
		}
		final String strQuery = "select * from projecttype where nprojecttypecode>0 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();

		final List<ProjectType> list = jdbcTemplate.query(strQuery, new ProjectType());
		outputMap.put("projectType", list);

		final Map<String, Object> defaultProjectType = new LinkedHashMap<>();
		int nprojecttypecode = -1;
		if (inputMap.containsKey("nprojecttypecode")) {
			nprojecttypecode = (int) inputMap.get("nprojecttypecode");
		}
		if (!list.isEmpty()) {
			if (nprojecttypecode == -1) {
				defaultProjectType.put("label", list.get(list.size() - 1).getSprojecttypename());
				defaultProjectType.put("value", list.get(list.size() - 1).getNprojecttypecode());
				defaultProjectType.put("item", list.get(list.size() - 1));

				nprojecttypecode = list.get(list.size() - 1).getNprojecttypecode();
			} else {
				final String strQuery1 = "select * from projecttype where nprojecttypecode>0 and "
						+ " nprojecttypecode=" + nprojecttypecode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final ProjectType lstProjectType = jdbcTemplate.queryForObject(strQuery1, new ProjectType());
				if (lstProjectType != null) {
					defaultProjectType.put("label", lstProjectType.getSprojecttypename());
					defaultProjectType.put("value", lstProjectType.getNprojecttypecode());
					defaultProjectType.put("item", lstProjectType);
				}
			}
		}
		outputMap.put("defaultProjectType", defaultProjectType);
		outputMap.put("realProjectType", defaultProjectType);

		int nbulkbarcodeconfigcode = -1;
		if (inputMap.containsKey("nbulkbarcodeconfigcode")) {
			nbulkbarcodeconfigcode = (int) inputMap.get("nbulkbarcodeconfigcode");
		}
		outputMap.putAll(getProjectBarcodceConfig((int) defaultProjectType.get("value"), userInfo).getBody());

		final Map<String, Object> defaultBarcodeConfigMap = (Map<String, Object>) outputMap.get("defaultBarcodeConfig");
		if (!defaultBarcodeConfigMap.isEmpty()) {
			nbulkbarcodeconfigcode = (int) defaultBarcodeConfigMap.get("value");
		}

		final String barcodequery = "select nsorter,jsondata->>'sfieldname' as sfieldname from bulkbarcodeconfigdetails where nprojecttypecode= "
				+ nprojecttypecode + " and  nbulkbarcodeconfigcode=" + nbulkbarcodeconfigcode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter   ";

		final List<Map<String, Object>> projectBarcodeConfig = jdbcTemplate.queryForList(barcodequery);
		final Map<String, Object> map = new LinkedHashMap<>();
		map.put("sfieldname", "Barcode Id");
		map.put("nsorter", 0);
		projectBarcodeConfig.add(0, map);

		final String query = "select bcg.nbulkbarcodegenerationcode,bcg.sfilename,bcg.sdescription,"
				+ "bcg.ssystemfilename,bcg.nprojecttypecode,p.sprojecttypename,bc.sconfigname,bcg.nbulkbarcodeconfigcode   "
				+ "from bulkbarcodegeneration bcg,bulkbarcodeconfig bc,projecttype p where "
				+ " p.nprojecttypecode=bcg.nprojecttypecode and  " + "  bcg.nprojecttypecode=bc.nprojecttypecode "
				+ " and bcg.nbulkbarcodeconfigcode=bc.nbulkbarcodeconfigcode and  bcg.dmodifieddate between '"
				+ fromDate + "' and '" + toDate + "' and bcg.nbulkbarcodeconfigcode=" + nbulkbarcodeconfigcode + " "
				+ "and bc.nprojecttypecode=" + nprojecttypecode + " " + "and bcg.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bcg.nsitecode=bc.nsitecode "
				+ "and bc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bcg.nsitecode=p.nsitecode " + "and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bcg.nsitecode="
				+ userInfo.getNmastersitecode() + " " + "order by bcg.nbulkbarcodegenerationcode desc";

		final List<BulkBarcodeGeneration> lstBulkBarcodeGeneration = jdbcTemplate.query(query,
				new BulkBarcodeGeneration());

		outputMap.put("BulkBarcodeGeneration", lstBulkBarcodeGeneration);
		if (!lstBulkBarcodeGeneration.isEmpty()) {
			inputMap.put("nbulkbarcodeconfigcode", nbulkbarcodeconfigcode);

			if (inputMap.containsKey("nbulkbarcodegenerationcode") && !inputMap.containsKey("isDelete")) {
				outputMap
						.putAll((Map<String, Object>) getSelectedBarcodeGenerationDetail(inputMap, userInfo).getBody());
			} else {
				inputMap.put("nprojecttypecode", nprojecttypecode);
				inputMap.put("nbulkbarcodegenerationcode",
						lstBulkBarcodeGeneration.get(0).getNbulkbarcodegenerationcode());
				inputMap.put("fromDate", fromDate);
				inputMap.put("toDate", toDate);
				outputMap
						.putAll((Map<String, Object>) getSelectedBarcodeGenerationDetail(inputMap, userInfo).getBody());
			}

			if (inputMap.containsKey("bulkbarcodedatagen")) {
				final List<Map<String, Object>> bulkbarcodedatagen = mapper.readValue(
						inputMap.get("bulkbarcodedatagen").toString(), new TypeReference<List<Map<String, Object>>>() {
						});
				outputMap.put("bulkbarcodedatagen", bulkbarcodedatagen);
			}

		} else {
			outputMap.put("selectedBulkBarcodeGeneration", null);
			outputMap.put("bulkbarcodedatagen", null);

		}

		outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active barcodeconfig for
	 * the specified project type.
	 * @param nprojecttype code [int] for which the list is to be fetched
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         barcode config
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getProjectBarcodceConfig(final int nprojecttypecode,
			final UserInfo userinfo) {
		List<Map<String, Object>> listBulkBarcodeConfig = new ArrayList<>();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final Map<String, Object> defaultbarcodeconfig = new HashMap<>();

		final String stQuery = "select * from bulkbarcodeconfig bc,bulkbarcodeconfigversion bcv,projecttype pt where bc.nbulkbarcodeconfigcode=bcv.nbulkbarcodeconfigcode"
				+ "	AND bc.nprojecttypecode=" + nprojecttypecode
				+ " and pt.nprojecttypecode=bc.nprojecttypecode and bcv.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bcv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bc.nsitecode="
				+ userinfo.getNmastersitecode() + "";
		listBulkBarcodeConfig = jdbcTemplate.queryForList(stQuery);
		outputMap.put("bulkBarcodeConfig", listBulkBarcodeConfig);

		if (!listBulkBarcodeConfig.isEmpty()) {
			final Map<String, Object> lastConfig = listBulkBarcodeConfig.get(listBulkBarcodeConfig.size() - 1);
			final String sconfigname = lastConfig.get("sconfigname").toString();
			final int nbulkbarcodeconfigcode = (int) lastConfig.get("nbulkbarcodeconfigcode");
			defaultbarcodeconfig.put("label", sconfigname);
			defaultbarcodeconfig.put("value", nbulkbarcodeconfigcode);
			defaultbarcodeconfig.put("item", lastConfig);
		}
		outputMap.put("defaultBarcodeConfig", defaultbarcodeconfig);
		outputMap.put("realBarcodeConfig", defaultbarcodeconfig);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method is used to import list of all Excel data .
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched and excel sheet with the
	 *                 data.
	 * @return response entity object holding response status and list of all
	 *         active bulkbarcode generation.
	 * @throws Exception that are thrown from this DAO layer.
	 * @jira ALPD-4585 Bulk Barcode Generation -> Added the field length validation.
	 */

	@Override
	public ResponseEntity<Object> importBulkBarcodeGeneration(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> outMap = new HashMap<>();
		final List<BulkBarcodeGeneration> lstBulkBarcodeGeneration = objMapper.readValue(
				request.getParameter("importBulkBarcodeGen"), new TypeReference<List<BulkBarcodeGeneration>>() {
				});
		final MultipartFile multipartFile = request.getFile("ImportFile");
		final String bulkbarcodeFields = request.getParameter("bulkbarcodeFields");
		final List<String> bulkbarcodeFieldsList = new ArrayList<>(Arrays.asList(bulkbarcodeFields.split(",")));
		for (int i = 0; i < bulkbarcodeFieldsList.size(); i++) {
			bulkbarcodeFieldsList.set(i, bulkbarcodeFieldsList.get(i).trim());
		}

		final String query = "select nprojecttypecode,nfieldlength,nbulkbarcodeconfigcode,jsondata->>'sfieldname' as sfieldname,jsondata->>'sformname' as sformname ,stablename,stablecolumnname from bulkbarcodeconfigdetails where nbulkbarcodeconfigcode="
				+ lstBulkBarcodeGeneration.get(0).getNbulkbarcodeconfigcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter";
		final List<Map<String, Object>> lstBarcodeLength = jdbcTemplate.queryForList(query);

		String fromDate = "";
		String toDate = "";

		if (request.getParameter("fromDate") != null) {
			fromDate = request.getParameter("fromDate");
		}
		if (request.getParameter("toDate") != null) {
			toDate = request.getParameter("toDate");
		}
		final BulkBarcodeGeneration objBulkBarcodeGeneration = lstBulkBarcodeGeneration.get(0);
		String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		final int nControlCode = -1;
		sReturnString = ftpUtilityFunction.getFileFTPUpload(request, nControlCode, userInfo);
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
			final String sequencequery = "select nsequenceno from seqnostoragemanagement where stablename ='bulkbarcodegeneration' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;
			final Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo)
					.truncatedTo(ChronoUnit.SECONDS);
			final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
			final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());
			lstBulkBarcodeGeneration.forEach(objtf -> {
				objtf.setDcreateddate(instantDate);
				objtf.setDcreateddate(instantDate);
				objtf.setNoffsetdcreateddate(noffset);
				objtf.setScreateddate(sattachmentDate.replace("T", " ").replace("Z", ""));
			});
			final InputStream ins = multipartFile.getInputStream();
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			int len;
			while ((len = ins.read(buffer)) > -1) {
				baos.write(buffer, 0, len);
			}
			baos.flush();
			final InputStream is1 = new ByteArrayInputStream(baos.toByteArray());

			final Workbook workbook = WorkbookFactory.create(is1);
			final Sheet sheet = workbook.getSheetAt(0);
			int rowIndex = 0;
			final List<String> lstHeader = new ArrayList<>();
			int cellIndex = 0;
			final Map<String, Object> barcodeItem = new HashMap<>();
			final JSONArray bulkbarcodedatagen = new JSONArray();

			StringJoiner positionValue = new StringJoiner("");
			for (final Row row : sheet) {

				if (rowIndex > 0) {
					if (!lstHeader.isEmpty()) {
						cellIndex = 0;
						for (final String field : lstHeader) {
							if (row.getCell(cellIndex) != null) {
								final Cell cell = row.getCell(cellIndex);
								final String sbarcodeddata = cell.toString().replaceAll("\\.0", "").replaceAll("'", "");
								if (!sbarcodeddata.isEmpty() && sbarcodeddata.length() == Integer
										.parseInt(lstBarcodeLength.get(cellIndex).get("nfieldlength").toString())) {
									positionValue.add(sbarcodeddata);
									barcodeItem.put(field, sbarcodeddata);
								} else {
									rowIndex = rowIndex + 1;
									return new ResponseEntity<Object>(
											commonFunction.getMultilingualMessage("IDS_INVALIDDATAFOUNDROW",
													userInfo.getSlanguagefilename())
													+ ": " + rowIndex + ", "
													+ commonFunction.getMultilingualMessage("IDS_COLUMN",
															userInfo.getSlanguagefilename())
													+ ": " + field.toString(),
											HttpStatus.EXPECTATION_FAILED);
								}

							} else {
								rowIndex = rowIndex + 1;
								return new ResponseEntity<Object>(
										commonFunction.getMultilingualMessage("IDS_INVALIDDATAFOUNDROW",
												userInfo.getSlanguagefilename())
												+ ": " + rowIndex + ", "
												+ commonFunction.getMultilingualMessage("IDS_COLUMN",
														userInfo.getSlanguagefilename())
												+ ": " + field.toString(),
										HttpStatus.EXPECTATION_FAILED);
							}
							cellIndex++;
						}
						barcodeItem.put("Barcode Id", positionValue.toString());
						positionValue = new StringJoiner("");
						barcodeItem.put("selected", false);
						bulkbarcodedatagen.put(new JSONObject(barcodeItem));
						barcodeItem.clear();
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVALIDTEMPLATEHEADERS",
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				} else {
					for (final Cell cell : row) {
						final String header = cell.getStringCellValue();

						for (final String field : bulkbarcodeFieldsList) {
							if (field.equals(header)) {
								lstHeader.add(header);
								bulkbarcodeFieldsList.remove(field);
							} else {
								return new ResponseEntity<>(
										header + " " + commonFunction.getMultilingualMessage(
												"IDS_INVALIDTEMPLATEHEADERS", userInfo.getSlanguagefilename()),
										HttpStatus.CONFLICT);
							}
							break;
						}
						cellIndex++;
					}
				}
				rowIndex++;
			}

			if (!bulkbarcodedatagen.isEmpty()) {
				outMap.put("bulkbarcodedatagen", bulkbarcodedatagen);
				final String insertquery = "Insert into bulkbarcodegeneration(nbulkbarcodegenerationcode,nprojecttypecode,nbulkbarcodeconfigcode,jsondata,sfilename,sdescription,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nsitecode,nstatus)"
						+ "values (" + nsequenceno + "," + objBulkBarcodeGeneration.getNprojecttypecode() + ","
						+ objBulkBarcodeGeneration.getNbulkbarcodeconfigcode() + ",'" + bulkbarcodedatagen + "',N'"
						+ stringUtilityFunction.replaceQuote(objBulkBarcodeGeneration.getSfilename()) + "',N'"
						+ stringUtilityFunction.replaceQuote(objBulkBarcodeGeneration.getSdescription()) + "','"
						+ objBulkBarcodeGeneration.getDcreateddate() + "',"
						+ objBulkBarcodeGeneration.getNoffsetdcreateddate() + "," + userInfo.getNtimezonecode() + ",N'"
						+ objBulkBarcodeGeneration.getSsystemfilename() + "','"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);

				final String updatequery = "update seqnostoragemanagement set nsequenceno =" + nsequenceno
						+ " where stablename ='bulkbarcodegeneration'";
				jdbcTemplate.execute(updatequery);
				outMap.put("nprojecttypecode", objBulkBarcodeGeneration.getNprojecttypecode());
				outMap.put("nbulkbarcodeconfigcode", objBulkBarcodeGeneration.getNbulkbarcodeconfigcode());
				outMap.put("fromDate", fromDate);
				outMap.put("toDate", toDate);

				final List<Object> savedBulkBarcodeConfig = new ArrayList<>();
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_IMPORTBULKBARCODEGEN");
				savedBulkBarcodeConfig.add(objBulkBarcodeGeneration);
				auditUtilityFunction.fnInsertAuditAction(savedBulkBarcodeConfig, 1, null, multilingualIDList, userInfo);
			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_RECORDNOTFOUND", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
			return getBulkBarcodeGeneration(outMap, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to retrieve list of all active bulkbarcodegeneration detail records for
	 * the specified site.
	 * 
	 * @param Map object holding nbulkbarcodeconfigcode, nbulkbarcodegenerationcode, fromDate, toDate, nprojecttypecode
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         bulkbarcodegeneration detail records
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<? extends Object> getSelectedBarcodeGenerationDetail(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final int nbulkbarcodeconfigcode = (int) inputMap.get("nbulkbarcodeconfigcode");
		final int nbulkbarcodegenerationcode = Integer.parseInt(inputMap.get("nbulkbarcodegenerationcode").toString());
		final String activeQuery = getActiveBulkBarcodeGeneration(nbulkbarcodegenerationcode, userInfo);
		final BulkBarcodeGeneration activeBulkBarcodeGeneration = (BulkBarcodeGeneration) jdbcUtilityFunction
				.queryForObject(activeQuery, BulkBarcodeGeneration.class, jdbcTemplate);

		if (activeBulkBarcodeGeneration != null) {
			final Map<String, Object> outputMap = new HashMap<>();
			String fromDate = "";
			String toDate = "";
			if (inputMap.get("fromDate") != null) {
				fromDate = (String) inputMap.get("fromDate");
			}
			if (inputMap.get("toDate") != null) {
				toDate = (String) inputMap.get("toDate");
			}
			final int nprojecttypecode = (int) inputMap.get("nprojecttypecode");

			final String query = "select bcg.nbulkbarcodegenerationcode,bcg.sfilename,bcg.jsondata,bcg.ssystemfilename,bcg.sdescription,bcg.nprojecttypecode,p.sprojecttypename,bc.sconfigname,bcg.nbulkbarcodeconfigcode   from bulkbarcodegeneration bcg,bulkbarcodeconfig bc,projecttype p where "
					+ " p.nprojecttypecode=bcg.nprojecttypecode and   bcg.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and bcg.nprojecttypecode=bc.nprojecttypecode and bcg.nsitecode=" + userInfo.getNmastersitecode()
					+ " and bcg.nbulkbarcodeconfigcode=bc.nbulkbarcodeconfigcode and  bcg.dcreateddate between '"
					+ fromDate + "' and '" + toDate + "' and bcg.nbulkbarcodeconfigcode=" + nbulkbarcodeconfigcode
					+ "  and bcg.nprojecttypecode=" + nprojecttypecode + " and nbulkbarcodegenerationcode="
					+ nbulkbarcodegenerationcode + " order by bcg.dmodifieddate desc";
			final BulkBarcodeGeneration objBulkBarcodeGeneration = (BulkBarcodeGeneration) jdbcUtilityFunction
					.queryForObject(query, BulkBarcodeGeneration.class, jdbcTemplate);
			if (objBulkBarcodeGeneration != null) {
				outputMap.put("selectedBulkBarcodeGeneration", objBulkBarcodeGeneration);

				outputMap.put("bulkbarcodedatagen", objBulkBarcodeGeneration.getJsondata());
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	
	/**
	 * This method id used to delete an entry in bulkbarcodegeneration table.
	 * Need to check the record is already deleted or not
	 * @param Map object holds the nbulkbarcodegenerationcode  to be
	 *                           deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with corresponding HTTP status and an
	 *         bulkbarcodegeneration object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteBulkBarcodeGeneration(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final String activeQuery = getActiveBulkBarcodeGeneration((int) inputMap.get("nbulkbarcodegenerationcode"),
				userInfo);
		final BulkBarcodeGeneration activeBulkBarcodeGeneration = (BulkBarcodeGeneration) jdbcUtilityFunction
				.queryForObject(activeQuery, BulkBarcodeGeneration.class, jdbcTemplate);
		if (activeBulkBarcodeGeneration != null) {
			final String deleteQuery = "update bulkbarcodegeneration set nstatus="
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " where nbulkbarcodegenerationcode="
					+ (int) inputMap.get("nbulkbarcodegenerationcode") + "";
			jdbcTemplate.execute(deleteQuery);
			final List<Object> deleteBulkBarcodeGeneration = new ArrayList<>();
			final List<String> multilingualIDList = new ArrayList<>();
			deleteBulkBarcodeGeneration.add(activeBulkBarcodeGeneration);
			multilingualIDList.add("IDS_DELETEBULKBARCODEGENERATION");
			auditUtilityFunction.fnInsertAuditAction(deleteBulkBarcodeGeneration, 1, null, multilingualIDList,
					userInfo);

			return getBulkBarcodeGeneration(inputMap, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method id used to delete an entry in bulkbarcodegeneration table.
	 * Need to check the record is already deleted or not
	 * @param Map object holds the nbulkbarcodegenerationcode  to be
	 *                           deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with corresponding HTTP status and an
	 *         bulkbarcodegeneration detail object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<? extends Object> deleteBarcodeData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final String activeQuery = getActiveBulkBarcodeGeneration((int) inputMap.get("nbulkbarcodegenerationcode"),
				userInfo);
		final BulkBarcodeGeneration activeBulkBarcodeGeneration = (BulkBarcodeGeneration) jdbcUtilityFunction
				.queryForObject(activeQuery, BulkBarcodeGeneration.class, jdbcTemplate);
		if (activeBulkBarcodeGeneration != null) {
			final String deleteQuery = "update bulkbarcodegeneration set jsondata='" + inputMap.get("updateBarcodeData")
					+ "' where nbulkbarcodegenerationcode=" + (int) inputMap.get("nbulkbarcodegenerationcode") + "";
			jdbcTemplate.execute(deleteQuery);
			return getSelectedBarcodeGenerationDetail(inputMap, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to make query string based on the
	 * specified nbulkbarcodegenerationcode.
	 * 
	 * @param nbulkbarcodegenerationcode [int] primary key of bulkbarcodegeneration object
	 * @return response entity object holding Query string
	 * @throws Exception that are thrown from this DAO layer
	 */
	private String getActiveBulkBarcodeGeneration(final int nbulkbarcodegenerationcode, final UserInfo userInfo) {
		return "select * from bulkbarcodegeneration where nbulkbarcodegenerationcode=" + nbulkbarcodegenerationcode
				+ " and nsitecode=" + userInfo.getNmastersitecode() + " and " + "nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
	}

	/*
	 * This method is used for printing single or multiple barcode IDs. The logic is
	 * query-based and includes an audit trail.
	 * @param Map object holds the userinfo object, printer name, sbarcodename, ncontrolcode
	 * validate PRN file avaiable in path or not
	 * Validate Barcode file download from ftp or not
	 * Validate Barcode file upload or not
	 * Validate Barcode configure or not
	 * @return response entity print barcode file.
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> printBarcode(final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String sprintername = (String) inputMap.get("sprintername");
		FileReader fr = null;
		BufferedWriter bufferedWriter = null;
		BufferedReader br = null;
		final String str = "select * from barcode where ncontrolcode =" + inputMap.get("ncontrolcode")
				+ " and sbarcodename='" + inputMap.get("sbarcodename") + "' and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ objUserInfo.getNmastersitecode();
		final List<Barcode> lst = jdbcTemplate.query(str, new Barcode());
		if (!lst.isEmpty()) {
			FileInputStream psStream = null;
			final String getBarcodePath = " select ssettingvalue from settings where nsettingcode = 9 "
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final String filePath = jdbcTemplate.queryForObject(getBarcodePath, String.class);

			if (!lst.get(0).getSsystemfilename().isEmpty()) {
				final String prnFile = filePath + lst.get(0).getSsystemfilename();

				String squery = "";
				squery = jdbcTemplate.queryForObject("Select ssqlquery from SqlQuery where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsqlquerycode = "
						+ lst.get(0).getNquerycode() + "", String.class);
				String tempQry = "";
				tempQry = tempQry + squery;
				if (squery.contains("<@")) {
					final int aa = squery.indexOf("<@");
					final int bb = (squery.indexOf("@>") + 2);
					final int b = (squery.indexOf("@>"));
					tempQry = tempQry.replace(squery.substring(aa, (bb)),
							inputMap.get(squery.substring(aa + 2, b).trim()) + "");
				}

				File fileSharedFolder = new File(prnFile);

				if (!fileSharedFolder.exists()) {
					LOGGER.info("PRN File Not Found in Path->" + prnFile);
					LOGGER.info("Downloading from FTP");
					final UserInfo barcodeUserInfo = new UserInfo(objUserInfo);
					barcodeUserInfo.setNformcode((short) Enumeration.FormCode.BARCODE.getFormCode());
					final Map<String, Object> objmap = ftpUtilityFunction
							.FileViewUsingFtp(lst.get(0).getSsystemfilename(), -1, barcodeUserInfo, filePath, "");
					if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == objmap.get("rtn")) {

						LOGGER.info("File Downloaded from FTP");
						fileSharedFolder = new File(prnFile);
					} else {
						LOGGER.info("Error in downloading file from FTP");
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PRNFILEDOESNOTEXIST",
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				}
				LOGGER.info("PRN File Found in Path->" + prnFile);
				final String homePath = ftpUtilityFunction.getFileAbsolutePath();
				final String fileNamePath1 = System.getenv(homePath) + Enumeration.FTP.UPLOAD_PATH.getFTP()
						+ UUID.randomUUID().toString().trim() + ".prn";

				final Path path = Paths.get(fileNamePath1);

				LOGGER.info("New PRN File Created in Path->" + fileNamePath1);

				bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

				String line;
				final List<Map<String, Object>> lstquery = jdbcTemplate.queryForList(tempQry);
				for (int i = 0; i < lstquery.size(); i++) {
					try {
						fr = new FileReader(prnFile);
						br = new BufferedReader(fr);
						final Map<String, Object> map = lstquery.get(i);

						bufferedWriter = new BufferedWriter(new FileWriter(fileNamePath1));

						while ((line = br.readLine()) != null) {
							if (line.contains("$")) {
								final int start = line.indexOf("$");
								final int end = line.lastIndexOf("$");
								final String valueSubstitutedToFilter1 = line.substring(start + 1, end);
								final String keyToReplace = "\\$" + valueSubstitutedToFilter1 + "\\$";
								line = line.replaceAll(keyToReplace, map.get(valueSubstitutedToFilter1).toString());
								LOGGER.info("Value Replaced " + keyToReplace + " -> "
										+ map.get(valueSubstitutedToFilter1).toString());
							}
							bufferedWriter.write(line);
							bufferedWriter.newLine();
							bufferedWriter.flush();
						}

						if (br != null)
							br.close();
						if (bufferedWriter != null)
							bufferedWriter.close();

						psStream = new FileInputStream(new File(fileNamePath1));
						final String printerPath = "";
						final DocFlavor psInFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
						final Doc myDoc = new SimpleDoc(psStream, psInFormat, null);
						final PrintServiceAttributeSet aset = new HashPrintServiceAttributeSet();
						aset.add(new PrinterName(printerPath, null));

						final PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
						for (final PrintService printer : services) {
							if (printer.getName().equalsIgnoreCase(sprintername)) {
								final DocPrintJob job = printer.createPrintJob();
								job.print(myDoc, null);
								LOGGER.info("Printer Name => " + printer.getName());
								LOGGER.info("Barcode Printed Successfully");
							}
						}
					} catch (final Exception e) {
						LOGGER.error("Error: " + e.getMessage(), e);
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SELECTVALIDRECORDTOPRINT",
										objUserInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					} finally {
						try {
							if (br != null)
								br.close();
							if (fr != null)
								fr.close();
							if (bufferedWriter != null)
								bufferedWriter.close();
							if (psStream != null)
								psStream.close();
						} catch (final IOException e) {
							LOGGER.error("Error closing resources: " + e.getMessage(), e);
						}
					}
				}
				final Map<String, Object> outputMap = new HashMap<String, Object>();
				outputMap.put("sprimarykeyvalue", -1);
				auditUtilityFunction.insertAuditAction(objUserInfo, "IDS_PRINTBARCODE",
						commonFunction.getMultilingualMessage("IDS_PRINTBARCODE", objUserInfo.getSlanguagefilename()),
						outputMap);
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SUCCESS", objUserInfo.getSlanguagefilename()),
						HttpStatus.OK);
			} else {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FILENOTUPLOADEDINBARCODESCREEN",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOBARCODECONFIGURATION",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

}
