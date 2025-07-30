package com.agaramtech.qualis.storagemanagement.service.temporarystorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectType;
import com.agaramtech.qualis.storagemanagement.model.StorageSampleCollection;
import com.agaramtech.qualis.storagemanagement.model.TemporaryStorage;
import com.agaramtech.qualis.storagemanagement.service.samplestoragetransaction.SampleStorageTransactionDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "temporarystorage" table by 
 * implementing methods from its interface. 
 */
@AllArgsConstructor
@Repository
public class TemporaryStorageDAOImpl implements TemporaryStorageDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryStorageDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final SampleStorageTransactionDAO sampleStorageTransactionDAO;

	/**
	 * This method is used to retrieve list of all active temporarystorage for the specified site.
	 * @param fromDate [String] From Date.
	 * @param toDate [String]To Date.
	 * @param currentUIDate [String] Current Date.
	 * @param nprojecttypecode [int] Project Type Code.
	 * @param userInfo [UserInfo] ntranssitecode [int] primary key of site object for which the list is to be fetched
	 * @return response entity object holding response status and list of all active temporarystorage
	 * @throws Exception that are thrown from this DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getTemporaryStorage(String fromDate, String toDate, String currentUIDate,
			final UserInfo userInfo, int nprojecttypecode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			outputMap.put("FromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("ToDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			outputMap.put("FromDate", fromDateUI);
			outputMap.put("ToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}
		final String strQuery = "select * from projecttype where nprojecttypecode>0 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<ProjectType> list = jdbcTemplate.query(strQuery, new ProjectType());
		outputMap.put("projectType", list);

		final Map<String, Object> defaulrProjectType = new LinkedHashMap<String, Object>();
		if (!list.isEmpty()) {
			if (nprojecttypecode == -1) {
				defaulrProjectType.put("label", list.get(list.size() - 1).getSprojecttypename());
				defaulrProjectType.put("value", list.get(list.size() - 1).getNprojecttypecode());
				defaulrProjectType.put("item", list.get(list.size() - 1));

				nprojecttypecode = list.get(list.size() - 1).getNprojecttypecode();
			} else {
				final String strQuery1 = "select * from projecttype where nprojecttypecode>0 and "
						+ " nprojecttypecode=" + nprojecttypecode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode();
				final ProjectType lstProjectType = jdbcTemplate.queryForObject(strQuery1, new ProjectType());
				defaulrProjectType.put("label", lstProjectType.getSprojecttypename());
				defaulrProjectType.put("value", lstProjectType.getNprojecttypecode());
				defaulrProjectType.put("item", lstProjectType);
			}

		}

		int nbarcodelength = getBarcodeLengthByProjecType(nprojecttypecode, userInfo);

		final String query = "select sc.scomments,sc.ntemporarystoragecode,sc.sbarcodeid,sc.jsondata,COALESCE(TO_CHAR(sc.dstoragedatetime,' "
				+ "" + userInfo.getSpgsitedatetime()
				+ "'),'') as sstoragedatetime,case when sc.scomments='' then '-' else sc.scomments end as scomments,sc.nprojecttypecode "
				+ " from temporarystorage sc,projecttype pt where  " + " sc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sc.nsitecode="
				+ userInfo.getNtranssitecode() + " and pt.nsitecode=" + userInfo.getNmastersitecode()
				+ " and sc.dstoragedatetime between '" + fromDate + "' and '" + toDate
				+ "' and pt.nprojecttypecode=sc.nprojecttypecode  and pt.nprojecttypecode=" + nprojecttypecode
				+ " order by sc.ntemporarystoragecode desc ";

		final List<TemporaryStorage> lstTemporaryStorage = jdbcTemplate.query(query, new TemporaryStorage());
		if (!lstTemporaryStorage.isEmpty()) {
			outputMap.put("TemporaryStorage", lstTemporaryStorage);

		} else {
			outputMap.put("TemporaryStorage", new ArrayList<>());

		}

		final String barcodequery = getBarcodeFieldsByProjectType(nprojecttypecode, userInfo);

		final List<Map<String, Object>> projectBarcodeConfig = jdbcTemplate.queryForList(barcodequery);

		Map<String, Object> map = new LinkedHashMap<>();
		Map<String, Object> map1 = new LinkedHashMap<>();
		Map<String, Object> map2 = new LinkedHashMap<>();

		// ALPD-4694 Temporary Storage-->While Try to Delete the Records the Fields are
		// Align Wrongly

		map.put("sfieldname", "Unit");
		map.put("nsorter", "15");
		map1.put("sfieldname", "Quantity");
		map1.put("nsorter", "16");
		map2.put("sfieldname", "Process Duration");
		map2.put("nsorter", "17");

		projectBarcodeConfig.add(map);
		projectBarcodeConfig.add(map1);
		projectBarcodeConfig.add(map2);

		outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);
		outputMap.put("nbarcodelength", nbarcodelength);
		outputMap.put("selectedProjectType", defaulrProjectType);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	/**
	 *  This method is used to retrieve list of all active Barcode data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nbarcodeLength","toDate","spositionvalue","nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all active Barcode data specified project type and site.
	 * @throws Exception that are thrown from this DAO layer.
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public ResponseEntity<Object> getBarcodeConfigData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<>();
		final ObjectMapper mapper = new ObjectMapper();

		String sbarcode = inputMap.get("spositionvalue").toString();
		String newBarcode = null;

		// ALPD-5067 Ate234 janakumar Temporary Storage -> Any barcode id can store in
		// storage - removed all validation

		// if (sbarcode.length() > 1 ) {
		//
		// newBarcode = sbarcode.substring(0, 1) + '1' + sbarcode.substring(2);
		//
		// }
		//
		// final String barcodeCheck="select * from storagesamplereceiving where
		// sbarcodeid='"+newBarcode+"' and nsitecode="+userInfo.getNtranssitecode()+"
		// and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"
		// ";
		//
		// List<Map<String, Object>> objSampleReceiving =
		// jdbcTemplate.queryForList(barcodeCheck);
		//
		// if(objSampleReceiving.isEmpty()) {
		//
		// return new
		// ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLENOTRECEIVED",
		// userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		//
		// }else {

		// Retrieve barcode details
		final Map<String, Object> barcodeDetails = sampleStorageTransactionDAO.readBarcode(inputMap, userInfo);

		if ((barcodeDetails.containsKey("rtn") && barcodeDetails.get("rtn") != null
				&& barcodeDetails.get("rtn").equals(false))) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_INVAILDBARCODEID", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		System.out.println("Barcode Details: " + barcodeDetails);

		final String barcodeQuery = getBarcodeFieldsByProjectType((int) inputMap.get("nprojecttypecode"), userInfo);
		List<Map<String, Object>> projectBarcodeConfig = jdbcTemplate.queryForList(barcodeQuery);

		Map<String, Object> map = new LinkedHashMap<>();
		Map<String, Object> map1 = new LinkedHashMap<>();
		Map<String, Object> map2 = new LinkedHashMap<>();
		map.put("sfieldname", "Unit");
		map.put("nsorter", "15");
		map1.put("sfieldname", "Quantity");
		map1.put("nsorter", "16");
		map2.put("sfieldname", "Process Duration");
		map2.put("nsorter", "17");
		projectBarcodeConfig.add(map);
		projectBarcodeConfig.add(map1);
		projectBarcodeConfig.add(map2);
		outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);

		if (barcodeDetails != null && barcodeDetails.containsKey("jsonValue")) {
			final Map<String, Object> jsondataBarcodeData = mapper.readValue(barcodeDetails.get("jsonValue").toString(),
					Map.class);
			final Map<String, Object> objJsonPrimaryKeyValue = mapper
					.readValue(barcodeDetails.get("objJsonPrimaryKeyValue").toString(), Map.class);
			jsondataBarcodeData.put("nbulkbarcodeconfigcode", objJsonPrimaryKeyValue.get("nbulkbarcodeconfigcode"));

			if (objJsonPrimaryKeyValue.containsKey("ncollectiontubetypecode")
					&& !objJsonPrimaryKeyValue.get("ncollectiontubetypecode").equals("-")
					&& objJsonPrimaryKeyValue.containsKey("nvisitnumbercode")
					&& !objJsonPrimaryKeyValue.get("nvisitnumbercode").equals("-")) {

				final String aliquotNo = "select * from samplepunchsite where nprojecttypecode="
						+ inputMap.get("nprojecttypecode") + " " + "and nsamplepunchsitecode ="
						+ objJsonPrimaryKeyValue.get("nsamplepunchsitecode") + " and nsamplecollectiontypecode="
						+ objJsonPrimaryKeyValue.get("nsamplecollectiontypecode") + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
						+ userInfo.getNmastersitecode();
				final List<Map<String, Object>> lstquery = jdbcTemplate.queryForList(aliquotNo);
				String nAliquotNo = "";
				if (!lstquery.isEmpty()) {
					nAliquotNo = sbarcode.substring(sbarcode.length() - 1);
				} else {
					nAliquotNo = sbarcode.substring(sbarcode.length() - 2);
				}
				int npatientcatcode, nsampledonorcode;
				if (objJsonPrimaryKeyValue.get("npatientcatcode") == null) {
					npatientcatcode = Enumeration.TransactionStatus.NA.gettransactionstatus();
				} else {
					npatientcatcode = (int) objJsonPrimaryKeyValue.get("npatientcatcode");
				}
				if (objJsonPrimaryKeyValue.get("nsampledonorcode") == null) {
					nsampledonorcode = Enumeration.TransactionStatus.NA.gettransactionstatus();
				} else {
					nsampledonorcode = (int) objJsonPrimaryKeyValue.get("nsampledonorcode");
				}
				final String query = " select ap.squantity as squantity , u.sunitname as sunitname from aliquotplan ap,unit u where  "
						+ " ap.nunitcode = u.nunitcode " + " and u.nsitecode =" + userInfo.getNmastersitecode()
						+ " and ap.nsitecode=" + userInfo.getNmastersitecode() + " and ap.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ap.nprojecttypecode="
						+ (int) inputMap.get("nprojecttypecode") + "  and " + " ap.npatientcatcode=" + npatientcatcode
						+ " and ap.nsampledonorcode=" + nsampledonorcode + " " + "and ap.nvisitnumbercode="
						+ objJsonPrimaryKeyValue.get("nvisitnumbercode") + "" + " and ap.ncollectiontubetypecode="
						+ objJsonPrimaryKeyValue.get("ncollectiontubetypecode") + " "
						+ " and ap.nsamplecollectiontypecode=" + objJsonPrimaryKeyValue.get("nsamplecollectiontypecode")
						+ " " + " and ap.saliquotno='" + nAliquotNo + "' ;";
				final TemporaryStorage objTemporaryStorage = (TemporaryStorage) jdbcUtilityFunction
						.queryForObject(query, TemporaryStorage.class, jdbcTemplate);
				if (objTemporaryStorage != null) {
					jsondataBarcodeData.put("Quantity", objTemporaryStorage.getSquantity());
					jsondataBarcodeData.put("Unit", objTemporaryStorage.getSunitname());
				} else {
					String unitAndQty = "select u.sunitname as sunitname,ssc.nsampleqty as squantity "
							+ " from storagesamplecollection ssc,storagesamplereceiving ssr,unit u "
							+ " where ssc.sbarcodeid=ssr.sbarcodeid and ssc.nunitcode=u.nunitcode and ssc.nsitecode=ssr.nsitecode "
							+ " and ssr.nsitecode=" + userInfo.getNtranssitecode() + " and ssc.nsitecode="
							+ userInfo.getNtranssitecode() + " and u.nsitecode=" + userInfo.getNmastersitecode()
							+ " and ssc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ssr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ssr.sbarcodeid='" + newBarcode + "' ;";
					final TemporaryStorage objTemporaryStorageunitQTY = (TemporaryStorage) jdbcUtilityFunction
							.queryForObject(unitAndQty, TemporaryStorage.class, jdbcTemplate);
					if (objTemporaryStorageunitQTY != null) {
						jsondataBarcodeData.put("Quantity", objTemporaryStorageunitQTY.getSquantity());
						jsondataBarcodeData.put("Unit", objTemporaryStorageunitQTY.getSunitname());
					} else {
						jsondataBarcodeData.put("Quantity", "-");
						jsondataBarcodeData.put("Unit", "-");
					}
				}
			}
			String conditionStr = "";
			List<String> visitNumberFieldName = (List<String>) barcodeDetails.get("visitNumberField");
			String visitNumStr = visitNumberFieldName.size() > 0 ? visitNumberFieldName.get(0).toString()
					: "Visit Number";
			List<String> barcodeDetailsFields = (List<String>) barcodeDetails.get("fieldsToCalculate");
			if (barcodeDetailsFields.size() > 0) {
				for (String fieldName : barcodeDetailsFields) {
					conditionStr += " and ssp.jsondata->>'" + fieldName + "' = '" + jsondataBarcodeData.get(fieldName)
					+ "' ";
				}
			}
			if (!conditionStr.isEmpty()) {
				final String processTimeQuery = "SELECT SUM(sp.nprocesstime) || ' ' ||(pd.jsondata->'speriodname'->>'"
						+ userInfo.getSlanguagetypecode() + "') AS nprocesstimeresult "
						+ "FROM storagesampleprocessing ssp " + "JOIN visitnumber vn ON ssp.jsondata->>'" + visitNumStr
						+ "' = vn.svisitnumber AND ssp.nprojecttypecode=vn.nprojecttypecode "
						+ "JOIN sampleprocesstype sp ON sp.nsampleprocesstypecode = ssp.nsampleprocesstypecode AND sp.nprojecttypecode=ssp.nprojecttypecode "
						+ "JOIN period pd ON sp.nprocessperiodtime=pd.nperiodcode " + "WHERE ssp.nsitecode="
						+ userInfo.getNtranssitecode() + " " + "AND vn.nsitecode=" + userInfo.getNmastersitecode() + " "
						+ "AND sp.nsitecode=" + userInfo.getNmastersitecode() + " " + "AND pd.nsitecode="
						+ userInfo.getNmastersitecode() + " " + "AND ssp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND vn.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND sp.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND pd.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "AND ssp.nprojecttypecode=" + (int) inputMap.get("nprojecttypecode") + " " + conditionStr
						+ " GROUP BY pd.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode() + "';";
				final List<Map<String, Object>> processTime = jdbcTemplate.queryForList(processTimeQuery);
				if (processTime.isEmpty()) {
					jsondataBarcodeData.put("Process Duration", "-");
				} else {
					jsondataBarcodeData.put("Process Duration", processTime.get(0).get("nprocesstimeresult"));
				}
			} else {
				jsondataBarcodeData.put("Process Duration", "-");
			}
			outputMap.put("jsondataBarcodeData", jsondataBarcodeData);
			outputMap.put("objJsonPrimaryKeyValue", objJsonPrimaryKeyValue);
			System.out.println("Output Map: " + outputMap);
			System.out.println("jsondataBarcodeData1: " + jsondataBarcodeData);
			System.out.println("objJsonPrimaryKeyValue: " + objJsonPrimaryKeyValue);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to temporarystorage table.
	 * Need to check for duplicate entry of sbarcodeid the specified site.	 
	 *  @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of added temporarystorage object
	 * @throws Exception that are thrown from this DAO layer
	 * @jira ALPD-4627 Displaying barcode info while clicking enter key in Sample
	 *       collection, Sample receiving, Sample Processing, Temporary Storage.
	 *       Storage.
	 */
	@Override
	public ResponseEntity<Object> createTemporaryStorage(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table temporarystorage "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final ObjectMapper mapper = new ObjectMapper();
		final List<Object> savedTemporaryStorage = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String fromDate = "";
		String toDate = "";
		Map<String, Object> temporaryStorageObj = mapper.convertValue(inputMap.get("temporarystorage"), Map.class);
		String existsQuery = getBarcodeId(temporaryStorageObj.get("sbarcodeid").toString().trim(), userInfo);
		final TemporaryStorage existsSampleCollectionObj = (TemporaryStorage) jdbcUtilityFunction
				.queryForObject(existsQuery, TemporaryStorage.class, jdbcTemplate);
		if (existsSampleCollectionObj == null) {
			if (inputMap.get("fromDate") != null) {
				fromDate = (String) inputMap.get("fromDate");
			}
			if (inputMap.get("toDate") != null) {
				toDate = (String) inputMap.get("toDate");
			}
			if (temporaryStorageObj.get("dstoragedatetime") != null) {
				temporaryStorageObj.put("dstoragedatetime",
						temporaryStorageObj.get("dstoragedatetime").toString().replace("T", " ").replace("Z", ""));
			}
			final String seqNo = "select nsequenceno from seqnostoragemanagement where stablename='temporarystorage' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int nsequenceno = jdbcTemplate.queryForObject(seqNo, Integer.class);
			nsequenceno++;

			final String updatequery = "update seqnostoragemanagement set nsequenceno =" + nsequenceno
					+ " where stablename='temporarystorage'";
			jdbcTemplate.execute(updatequery);

			final String insertQuery = "insert into temporarystorage(ntemporarystoragecode,nprojecttypecode,sbarcodeid,jsondata,dstoragedatetime,ntzstoragedatetime,noffsetstoragedatetime,"
					+ "scomments,dmodifieddate,nsitecode,nstatus) values(" + nsequenceno + ","
					+ temporaryStorageObj.get("nprojecttypecode") + "," + "N'"
					+ stringUtilityFunction.replaceQuote(temporaryStorageObj.get("sbarcodeid").toString()) + "','"
					+ temporaryStorageObj.get("jsondata") + "','" + temporaryStorageObj.get("dstoragedatetime") + "',"
					+ temporaryStorageObj.get("ntzstoragedatetime") + "," + "'"
					+ temporaryStorageObj.get("noffsetstoragedatetime") + "',N'"
					+ stringUtilityFunction.replaceQuote(temporaryStorageObj.get("scomments").toString()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			jdbcTemplate.execute(insertQuery);

			final String auditQuery = getTemporaryStorageAudit(nsequenceno, userInfo);
			multilingualIDList.add("IDS_ADDTEMPORARYSTORAGE");

			final TemporaryStorage objTemporaryStorage = (TemporaryStorage) jdbcUtilityFunction
					.queryForObject(auditQuery, TemporaryStorage.class, jdbcTemplate);
			savedTemporaryStorage.add(objTemporaryStorage);

			auditUtilityFunction.fnInsertAuditAction(savedTemporaryStorage, 1, null, multilingualIDList, userInfo);

			return getTemporaryStorage(fromDate, toDate, null, userInfo,
					(int) temporaryStorageObj.get("nprojecttypecode"));
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_BARCODEIDALREADYEXISTS",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to update entry in temporarystorage table.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of updated temporarystorage object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateTemporaryStorage(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> beforeTemporaryStorage = new ArrayList<>();
		final List<Object> afterTemporaryStorage = new ArrayList<>();
		mapper.registerModule(new JavaTimeModule());
		final TemporaryStorage objTemporaryStorage = mapper.convertValue(inputMap.get("temporarystorage"),
				new TypeReference<TemporaryStorage>() {
		});
		final String activeQuery = getActiveTemporaryStorage(objTemporaryStorage.getNtemporarystoragecode(), userInfo);
		final TemporaryStorage activeSampleCollection = (TemporaryStorage) jdbcUtilityFunction
				.queryForObject(activeQuery, TemporaryStorage.class, jdbcTemplate);
		if (activeSampleCollection != null) {
			String auditQuery = getTemporaryStorageAudit(objTemporaryStorage.getNtemporarystoragecode(), userInfo);
			multilingualIDList.add("IDS_EDITTEMPORARYSTORAGE");
			final TemporaryStorage objTemporaryStorageAuditBefore = (TemporaryStorage) jdbcUtilityFunction
					.queryForObject(auditQuery, TemporaryStorage.class, jdbcTemplate);
			if (inputMap.get("fromDate") != null) {
				fromDate = (String) inputMap.get("fromDate");
			}
			if (inputMap.get("toDate") != null) {
				toDate = (String) inputMap.get("toDate");
			}
			if (objTemporaryStorage.getDstoragedatetime() != null) {
				objTemporaryStorage.setSstoragedatetime(
						dateUtilityFunction.instantDateToString(objTemporaryStorage.getDstoragedatetime())
						.replace("T", " ").replace("Z", ""));
			}
			final String updateQuery = "update temporarystorage set dstoragedatetime='"
					+ objTemporaryStorage.getSstoragedatetime() + "',ntzstoragedatetime="
					+ objTemporaryStorage.getNtzstoragedatetime() + "," + "noffsetstoragedatetime="
					+ objTemporaryStorage.getNoffsetstoragedatetime() + ",scomments=N'"
					+ stringUtilityFunction.replaceQuote(objTemporaryStorage.getScomments()) + "',dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nsitecode=" + userInfo.getNtranssitecode()
					+ " where ntemporarystoragecode=" + objTemporaryStorage.getNtemporarystoragecode()
					+ " and nsitecode=" + userInfo.getNtranssitecode();
			jdbcTemplate.execute(updateQuery);

			auditQuery = getTemporaryStorageAudit(objTemporaryStorage.getNtemporarystoragecode(), userInfo);

			final TemporaryStorage objTemporaryStorageAuditAfter = (TemporaryStorage) jdbcUtilityFunction
					.queryForObject(auditQuery, TemporaryStorage.class, jdbcTemplate);
			beforeTemporaryStorage.add(objTemporaryStorageAuditBefore);
			afterTemporaryStorage.add(objTemporaryStorageAuditAfter);

			auditUtilityFunction.fnInsertAuditAction(afterTemporaryStorage, 2, beforeTemporaryStorage,
					multilingualIDList, userInfo);
			return getTemporaryStorage(fromDate, toDate, null, userInfo, objTemporaryStorage.getNprojecttypecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to retrieve active temporarystorage object 
	 * @param inputMap  [Map] map object with "nprojecttypecode","ntemporarystoragecode" and "userInfo" as keys for which the data is to be fetched
	 * @return response entity  object holding response status and data of temporarystorage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveTemporaryStorageById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final int ntemporarystoragecode = (int) inputMap.get("ntemporarystoragecode");
		final String activeQuery = getActiveTemporaryStorage(ntemporarystoragecode, userInfo);

		final StorageSampleCollection activeSampleCollection = (StorageSampleCollection) jdbcUtilityFunction
				.queryForObject(activeQuery, StorageSampleCollection.class, jdbcTemplate);

		if (activeSampleCollection != null) {
			Map<String, Object> outputMap = new HashMap<>();

			String query = "select sc.scomments,sc.ntemporarystoragecode,sc.sbarcodeid,sc.jsondata,COALESCE(TO_CHAR(sc.dstoragedatetime,'"
					+ "" + userInfo.getSpgsitedatetime()
					+ "'),'') as sstoragedatetime,sc.scomments,sc.nprojecttypecode from temporarystorage sc where  "
					+ " sc.nsitecode=" + userInfo.getNtranssitecode() + " and  ntemporarystoragecode="
					+ ntemporarystoragecode + " and sc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			final TemporaryStorage lstTemporaryStorage = jdbcTemplate.queryForObject(query, new TemporaryStorage());

			final String barcodequery = getBarcodeFieldsByProjectType((int) inputMap.get("nprojecttypecode"), userInfo);

			final List<Map<String, Object>> projectBarcodeConfig = jdbcTemplate.queryForList(barcodequery);
			Map<String, Object> map = new LinkedHashMap<>();
			Map<String, Object> map1 = new LinkedHashMap<>();
			Map<String, Object> map2 = new LinkedHashMap<>();
			map.put("sfieldname", "Unit");
			map.put("nsorter", "15");
			map1.put("sfieldname", "Quantity");
			map1.put("nsorter", "16");
			map2.put("sfieldname", "Process Duration");
			map2.put("nsorter", "17");
			projectBarcodeConfig.add(map);
			projectBarcodeConfig.add(map1);
			projectBarcodeConfig.add(map2);
			outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);
			outputMap.put("activeTemporaryStorageByID", lstTemporaryStorage);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to delete an entry in temporarystorage  table.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of deleted temporarystorage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteTemporaryStorage(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final List<Object> deleteTemporaryStorage = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String activeQuery = getActiveTemporaryStorage((int) inputMap.get("ntemporarystoragecode"), userInfo);
		final TemporaryStorage activeTemporaryStorage = (TemporaryStorage) jdbcUtilityFunction
				.queryForObject(activeQuery, TemporaryStorage.class, jdbcTemplate);
		if (activeTemporaryStorage != null) {
			final String auditQuery = getTemporaryStorageAudit((int) inputMap.get("ntemporarystoragecode"), userInfo);
			final TemporaryStorage objTemporaryStorageAuditAfter = (TemporaryStorage) jdbcUtilityFunction
					.queryForObject(auditQuery, TemporaryStorage.class, jdbcTemplate);
			deleteTemporaryStorage.add(objTemporaryStorageAuditAfter);
			multilingualIDList.add("IDS_DELETETEMPORARYSTORAGE");
			auditUtilityFunction.fnInsertAuditAction(deleteTemporaryStorage, 1, null, multilingualIDList, userInfo);

			final String deleteQuery = "update temporarystorage set nstatus="
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " where ntemporarystoragecode="
					+ (int) inputMap.get("ntemporarystoragecode") + " and nsitecode=" + userInfo.getNtranssitecode();
			jdbcTemplate.execute(deleteQuery);
			return getTemporaryStorage(fromDate, toDate, null, userInfo, (int) inputMap.get("nprojecttypecode"));
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}

	/**
	 * This method is used to fetch the TemporaryStorage object for the specified barcode id and site.
	 * @param sbarcodeid [String] name of the Barcode ID
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched	
	 * @return temporarystorage object based on the specified barcode id and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private String getBarcodeId(final String sbarcodeid, UserInfo userInfo) {
		return "select * from temporarystorage where " + " sbarcodeid=N'"
				+ stringUtilityFunction.replaceQuote(sbarcodeid) + "'" + "  and  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  and nsitecode="
				+ userInfo.getNtranssitecode() + "";
	}
	
	/**
	 * This method is used to retrieve active temporarystorage object based on the specified ntemporarystoragecode.
	 * @param ntemporarystoragecode [int] primary key of temporarystorage object
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity  object holding response status and data of temporarystorage object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	private String getActiveTemporaryStorage(final int ntemporarystoragecode, final UserInfo userInfo) {

		return "select * from temporarystorage where ntemporarystoragecode=" + ntemporarystoragecode + " and nsitecode="
				+ userInfo.getNtranssitecode() + " and " + "nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
	}

	/**
	 * This method is used to retrieve active temporarystorage object based on the specified ntemporarystoragecode.
	 * @param ntemporarystoragecode [int] primary key of temporarystorage object
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity  object holding response status and data of temporarystorage object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	private String getTemporaryStorageAudit(final int ntemporarystoragecode, final UserInfo userinfo) {
		return "select sc.scomments,sc.ntemporarystoragecode,sc.sbarcodeid,sc.jsondata,COALESCE(TO_CHAR(sc.dstoragedatetime,'"
				+ "" + userinfo.getSpgsitedatetime()
				+ "'),'') as sstoragedatetime,sc.nprojecttypecode,p.sprojecttypename from temporarystorage sc,projecttype p where "
				+ "   p.nprojecttypecode=sc.nprojecttypecode and " + "  sc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and sc.nsitecode="
				+ userinfo.getNtranssitecode() + " and " + "sc.ntemporarystoragecode=" + ntemporarystoragecode
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
	}

	/**
	 * This method is used to retrieve active Bulk Barcode Config object based on the specified nprojecttypecode.
	 * @param nprojecttypecode [int] Project Type Code.
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity  object holding response status and data of Bulk Barcode Config object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	public int getBarcodeLengthByProjecType(final int nprojecttypecode, final UserInfo userinfo) {
		int nbarcodelength = 0;
		final String stQuery = "select bc.nbarcodelength as nbarcodelength "
				+ " from bulkbarcodeconfig bc,bulkbarcodeconfigversion bcv,projecttype pt "
				+ " where bc.nbulkbarcodeconfigcode=bcv.nbulkbarcodeconfigcode" + "	and  bc.nprojecttypecode="
				+ nprojecttypecode + " and pt.nprojecttypecode=bc.nprojecttypecode and bcv.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " 	and bcv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bc.nsitecode="
				+ userinfo.getNmastersitecode() + " and bcv.nsitecode=" + userinfo.getNmastersitecode()
				+ " and pt.nsitecode=" + userinfo.getNmastersitecode() + "";
		List<Map<String, Object>> listBulkBarcodeConfig = jdbcTemplate.queryForList(stQuery);
		if (!listBulkBarcodeConfig.isEmpty()) {
			nbarcodelength = Integer.parseInt(listBulkBarcodeConfig.get(0).get("nbarcodelength").toString());
		} else {
			nbarcodelength = 0;
		}
		LOGGER.info("getBarcodeLengthByProjecType:" + stQuery);
		return nbarcodelength;
	}

	/**
	 * This method is used to retrieve active Bulk Barcode Config object based on the specified nprojecttypecode.
	 * @param nprojecttypecode [int] Project Type Code.
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched.
	 * @return response entity  object holding response status and data of Bulk Barcode Config object.
	 * @throws Exception that are thrown from this DAO layer
	 */
	public String getBarcodeFieldsByProjectType(final int nprojecttypecode, final UserInfo userinfo) {
		return "SELECT  nsorter,jsondata->>'sfieldname' as sfieldname FROM  projecttype pt,"
				+ " bulkbarcodeconfigversion bcv," + " bulkbarcodeconfig bc,bulkbarcodeconfigdetails bcd "
				+ "  where bcd.nbulkbarcodeconfigcode = bc.nbulkbarcodeconfigcode and bcd.nprojecttypecode = bc.nprojecttypecode "
				+ "  and  bc.nprojecttypecode = " + nprojecttypecode + "" + "  AND bcv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ""
				+ "   AND  bc.nbulkbarcodeconfigcode = bcv.nbulkbarcodeconfigcode "
				+ "   AND  pt.nprojecttypecode = bc.nprojecttypecode " + "  AND pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bcv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND bcd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND pt.nsitecode = "
				+ userinfo.getNmastersitecode() + "  AND bcv.nsitecode = " + userinfo.getNmastersitecode()
				+ "  AND bc.nsitecode = " + userinfo.getNmastersitecode() + "  AND bcd.nsitecode = "
				+ userinfo.getNmastersitecode() + " order by nsorter;";
	}

}
