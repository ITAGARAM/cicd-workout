package com.agaramtech.qualis.storagemanagement.service.samplestoragetransaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.basemaster.model.BulkBarcodeConfigDetails;
import com.agaramtech.qualis.basemaster.model.ContainerType;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.SampleStorageCommons;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageMapping;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageTransaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "samplestoragetransaction" table by
 * implementing methods from its interface.
 *
 * @author ATE236
 * @version 10.0.0.2
 */
@AllArgsConstructor
@Repository
public class SampleStorageTransactionDAOImpl implements SampleStorageTransactionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleStorageTransactionDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final SampleStorageCommons sampleStorageCommons;

	/**
	 * This method is used to retrieve the list of available Sample Storage Structures, Sample Types, 
	 * Container Types, Container Structures, Project Types, and Selected Project Types.  
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}	
	 * 			
	 * @return response entity  object holding response status and list of available Sample Storage Structures, Sample Types, 
	 * Container Types, Container Structures, Project Types, and Selected Project Types.
	 * 
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getsamplestoragetransaction(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final String str = "select ssettingvalue from settings where nsettingcode=40 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(str, Settings.class, jdbcTemplate);
		String conditonstr = "";
		if (Integer.parseInt(objSettings.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			conditonstr = "  AND  ssl.nmappingtranscode="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
		}

		String strQuery = " select ssl.*,ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
		+ "'  as stransdisplaystatus from samplestoragelocation ssl,transactionstatus ts where ssl.nstatus = "
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and ssl.nsamplestoragelocationcode in (select nsamplestoragelocationcode from"
		+ " samplestorageversion where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and napprovalstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
		+ " and nsitecode=" + userInfo.getNtranssitecode() + ") and ts.ntranscode=ssl.nmappingtranscode "
		+ conditonstr + "   order by ssl.nsamplestoragelocationcode desc ";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("sampleStorageLocation", list);

		strQuery = "select * from product where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		+ " and nsitecode=" + userInfo.getNmastersitecode();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("sampleType", list);

		strQuery = "select * from containertype where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerType", list);

		strQuery = "select * from containerstructure where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerStructure", list);

		strQuery = "select * from projecttype where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("projectType", list);
		outputMap.put("selectedProjectType", list.get(0));
		return new ResponseEntity<Map<String, Object>>(outputMap, HttpStatus.OK);

	}

	/*
	@Override
	public ResponseEntity<Object> addSampleStorageMapping(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final int nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");

		final String strQuery = "select   jsondata->>'scontainerpath' as scontainerpath,* from   samplestoragecontainerpath  where    nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestoragelocationcode="
				+ nsamplestoragelocationcode + " and nsitecode=" + userInfo.getNmastersitecode()
				+ " and nsamplestorageversioncode in (select nsamplestorageversioncode from samplestorageversion"
				+ " where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and napprovalstatus  = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nsamplestoragelocationcode=" + nsamplestoragelocationcode + ") "
				+ " and nsamplestoragecontainerpathcode not in (select nsamplestoragecontainerpathcode from samplestoragemapping where"
				+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + ")";
		final List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("samplestoragecontainerpath", list);

		final String containertypeQuery = "select c.* from containertype c , containerstructure cts where "
				+ " c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nsitecode = "
				+ userInfo.getNmastersitecode() + " and c.nsitecode=cts.nsitecode "
				+ " and cts.ncontainertypecode=c.ncontainertypecode  group by c.ncontainertypecode order by c.ncontainertypecode";
		final List<ContainerType> lstcontainerType = jdbcTemplate.query(containertypeQuery, new ContainerType());

		outputMap.put("containerType", lstcontainerType);

		final String directionmasterQuery = "select jsondata->'sdirection'->>'" + userInfo.getSlanguagetypecode()
		+ "' as sdirection,* from directionmaster where nsitecode=" + userInfo.getNmastersitecode()
		+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<Map<String, Object>> directionmasterlist = jdbcTemplate.queryForList(directionmasterQuery);
		outputMap.put("directionmaster", directionmasterlist);

		if (lstcontainerType.size() > 0) {
			outputMap.put("ncontainertypecode", lstcontainerType.get(0).getNcontainertypecode());
			outputMap.putAll(getContainerStructure(outputMap, userInfo).getBody());
		} else {
			outputMap.put("ncontainertypecode", new HashMap<String, Object>());
			outputMap.put("containerStructure", new ArrayList<>());
		}

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getEditSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final int nsamplestoragemappingcode = (int) inputMap.get("nsamplestoragemappingcode");

		final String strQuery = "  select dm.jsondata->'sdirection'->>'" + userInfo.getSlanguagetypecode()
		+ "' as sdirection, ssc.jsondata->>'scontainerpath' as scontainerpath, ,ct.ncontainertypecode ,ct.scontainertype,cs.nrow,cs.ncolumn, "
		+ " cs.scontainerstructurename, " + " p.sproductname,  " + " ts.jsondata->'stransdisplaystatus'->>'"
		+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,   ssm.*   from  "
		+ " samplestoragemapping ssm,  samplestoragecontainerpath ssc,  containertype ct, "
		+ " containerstructure cs,  product p,   transactionstatus ts,directionmaster dm   where "
		+ " ssm.nsamplestoragecontainerpathcode=ssc.nsamplestoragecontainerpathcode and "
		+ " ssm.ncontainertypecode=ct.ncontainertypecode and "
		+ " dm.ndirectionmastercode=ssm.ndirectionmastercode and "
		+ " ssm.ncontainerstructurecode=cs.ncontainerstructurecode and  "
		+ " ssm.nproductcode=p.nproductcode and  ssm.nneedposition=ts.ntranscode and "
		+ " ssm.nsamplestoragemappingcode=" + nsamplestoragemappingcode + " and ssm.nstatus="
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssc.nstatus="
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ct.nstatus="
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cs.nstatus="
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dm.nstatus="
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final Map<String, Object> object = jdbcTemplate.queryForMap(strQuery);
		outputMap.put("editsampleStorageMapping", object);
		outputMap.put("ncontainertypecode", object.get("ncontainertypecode"));
		outputMap.putAll(getContainerStructure(outputMap, userInfo).getBody());
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getContainerStructure(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String strQuery = " select cts.*,ct.* from   containerstructure cts, containertype ct where  ct.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cts.ncontainertypecode= ct.ncontainertypecode and  ct.ncontainertypecode="
				+ inputMap.get("ncontainertypecode");
		final List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerStructure", list);
		return new ResponseEntity<Map<String, Object>>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveSampleStorageMappingById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final int nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");
		outputMap.putAll((Map<String, Object>) sampleStorageCommons
				.getSelectedSampleStorageStructure(nsamplestoragelocationcode, 0, userInfo).getBody());
		outputMap.put("nsamplestoragelocationcode", nsamplestoragelocationcode);
		outputMap.putAll(getsamplestoragetransaction(outputMap, userInfo).getBody());

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
*/
	
	/**
	 * This method is used to retrieve the available quantity and unit name based on the provided  information.
	 * 
	 *@param jsonobj  [Map] object with nprojecttypecode:Defines the value of Project Type Code,jsonValue:It has the barcode information, 
	 *objJsonPrimaryKeyValue:It contains the barcode master primary key, which uniquely identifies each barcode record ,userInfo:Login user information.
	 * 			
	 * Input : {"nprojecttypecode":1,"userInfo":{"nmastersitecode":-1}}
	 * 			
	 * @return return the Quantity and Unit Name.
	 * 
	 * @throws Exception exception
	 */
	
	private JSONObject fetchQuantity(final Map<String, Object> jsonobj, final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final JSONObject item = new JSONObject(jsonobj);
		final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> jsonData = objMapper.readValue(item.get("jsonValue").toString(), Map.class);
		final Map<String, Object> jsonDataValue = objMapper.readValue(item.get("objJsonPrimaryKeyValue").toString(),
				Map.class);
		// ALPD-5095 Sample Storage -->In Sample Info the Quantity and Unit is not
		// Captured. While Using Daughter Aliquot in Sample storage
		// ALPD-4993 Added by rukshana it was hard-coded before with field name given in
		// UI so record was not loaded for aliquot now changed in to table columnname
		// for checking
		if (jsonDataValue.containsKey("ncollectiontubetypecode")
				&& !jsonDataValue.get("ncollectiontubetypecode").equals("-")
				&& jsonDataValue.containsKey("nvisitnumbercode")
				&& !jsonDataValue.get("nvisitnumbercode").equals("-")) {
			// ATE234 janakumar ALPD-5066 Temporary Storage-->500 error occurs for specific
			// scenario
			// ALPD-5078 Temporary storage & Sample storage-->while enter the barcode 500
			// error occurs for PTB Project.
			// ALPD-5095 Sample Storage -->In Sample Info the Quantity and Unit is not
			// Captured. While Using Daughter Aliquot in Sample storage
			final String sbarcode = (String) jsonobj.get("daugtherAliquotFilterBarcode");
			final String aliquotNo = "select * from samplepunchsite where nprojecttypecode="
					+ inputMap.get("nprojecttypecode") + " " + "and nsamplepunchsitecode ="
					+ jsonDataValue.get("nsamplepunchsitecode") + " and nsamplecollectiontypecode="
					+ jsonDataValue.get("nsamplecollectiontypecode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			final List<Map<String, Object>> lstquery = jdbcTemplate.queryForList(aliquotNo);
			String nAliquotNo = "";
			if (!lstquery.isEmpty()) {
				nAliquotNo = sbarcode.substring(sbarcode.length() - 1);
			} else {
				nAliquotNo = sbarcode.substring(sbarcode.length() - 2);
			}

			int npatientcatcode, nsampledonorcode;
			if (jsonDataValue.get("npatientcatcode") == null) {
				npatientcatcode = Enumeration.TransactionStatus.NA.gettransactionstatus();
			} else {
				npatientcatcode = (int) jsonDataValue.get("npatientcatcode");
			}
			// ATE234 Janakumar ALPD-5577 Sample Storage-->while download the pdf, screen
			// getting freezed
			if (jsonDataValue.get("nsampledonorcode") == null) {
				nsampledonorcode = Enumeration.TransactionStatus.NA.gettransactionstatus();
			} else {
				nsampledonorcode = (int) jsonDataValue.get("nsampledonorcode");
			}
			// ATE234 janakumar ALPD-5066 and ap.saliquotno='"+nAliquotNo+"' added the field
			// ALPD-5095 Quantity is fetching for sample storage has been included along
			// with collection tube type and sample collectiontype
			final String aliquotQty = "select ap.squantity as squantity , u.sunitname as sunitname from "
					+ "aliquotplan ap,unit u where   " + "ap.nunitcode = u.nunitcode  and ap.nsitecode=u.nsitecode  "
					+ "and ap.nsitecode=" + userInfo.getNmastersitecode() + " " + "and ap.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + "and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + "and ap.nprojecttypecode="
					+ inputMap.get("nprojecttypecode") + "  " + "and ap.npatientcatcode=" + npatientcatcode + " "
					+ "and ap.nsampledonorcode=" + nsampledonorcode + " " + "and ap.nvisitnumbercode="
					+ jsonDataValue.get("nvisitnumbercode") + " " + "and ap.ncollectiontubetypecode="
					+ jsonDataValue.get("ncollectiontubetypecode") + " " + "and ap.nsamplecollectiontypecode="
					+ jsonDataValue.get("nsamplecollectiontypecode") + " " + "and ap.saliquotno='" + nAliquotNo + "' ;";

			final SampleStorageMapping sampleStoragealiquotQty = (SampleStorageMapping) jdbcUtilityFunction
					.queryForObject(aliquotQty, SampleStorageMapping.class, jdbcTemplate);

			if (sampleStoragealiquotQty != null) {

				jsonData.put("IDS_QUANTITY", sampleStoragealiquotQty.getSquantity());
				jsonData.put("IDS_UNITNAME", sampleStoragealiquotQty.getSunitname());

			} else {
				// ALPD-5074 Sample Storage -> THIST - Daughter Aliquot count of barcode ID.
				final String mappingQty = "select ssm.nquantity as squantity,u.sunitname as sunitname from samplestoragemapping ssm,unit u where "
						+ "ssm.nunitcode=u.nunitcode and ssm.nsamplestoragemappingcode="
						+ inputMap.get("nsamplestoragemappingcode") + " " + "and ssm.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and u.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nsitecode="
						+ userInfo.getNmastersitecode() + " ";

				final SampleStorageMapping sampleStorageMappingQty = (SampleStorageMapping) jdbcUtilityFunction
						.queryForObject(mappingQty, SampleStorageMapping.class, jdbcTemplate);

				final String mappingQtypush = sampleStorageMappingQty.getSquantity();
				final String mappingUnitpush = sampleStorageMappingQty.getSunitname();

				jsonData.put("IDS_QUANTITY", mappingQtypush.equals("0") ? "-" : mappingQtypush);
				jsonData.put("IDS_UNITNAME", mappingUnitpush.equals("0") ? "NA" : mappingUnitpush);
			}

		} else {

			jsonData.put("IDS_QUANTITY", "-");
			jsonData.put("IDS_UNITNAME", "NA");
		}

		final JSONObject objJsonDataList = new JSONObject(jsonData);

		return objJsonDataList;

	}
	
	/**
	 * This method is used to retrieve the list of available Sample Storage Structures, Sample Types, 
	 * Container Types, Container Structures, Project Types, and Selected Project Types.  
	 * 
	 * @param The inputMap is a Map object that contains multiple keys required for processing the sample storage transaction.
	 *  It includes userInfo, which holds the logged-in user details, and nmasterSiteCode, the primary key representing the site for which the 
	 *  list is to be fetched. The key nsamplestoragelocationcode represents the primary key of the cell position, 
	 *  while nsamplestoragemappingcode refers to the primary key of the sample storage mapping. The nprojecttypecode 
	 *  holds the primary code of the Project Type. Additionally, sposition is used to specify the position of the cell,
	 *  and spositionvalue holds the value associated with that cell position.	
	 *  
	 * 					Input : {"userinfo":{nmastersitecode": -1},"nsamplestoragelocationcode":1,"nsamplestoragemappingcode":1,
	 * "nprojecttypecode":1,"sposition":A3,"spositionvalue":P103}	
	 * 			
	 * @return response entity  object holding response status and list of available Cell Data and Available spaces.
	 * 
	 * @throws Exception exception
	 */

	@Override
	public ResponseEntity<Object> createSampleStorageTransaction(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table locksamplestoragetransaction "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		String Query = "";
		String updateQuery = "";
		Map<String, Object> barcodeDetails = new HashMap<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTestList = new ArrayList<>();
		final List<Object> beforeSavedTestList = new ArrayList<>();
		JSONObject barcodeDetailsQty = new JSONObject();

		List<SampleStorageTransaction> lstCheckValue = new ArrayList<>();
		boolean checkValue = false;
		// ALPD-5074 Sample Storage -> THIST - Daughter Aliquot count of barcode ID.
		boolean isBarcodeID = false;

		final String barcodeLength = inputMap.get("spositionvalue").toString().replaceAll("\\s", "");
		final String query = " select bb.nbarcodelength as nfieldlength from bulkbarcodeconfig bb,bulkbarcodeconfigversion bbv where "
				+ "bb.nprojecttypecode= " + inputMap.get("nprojecttypecode") + " and bb.nsitecode=bbv.nsitecode"
				+ " and bb.nbulkbarcodeconfigcode=bbv.nbulkbarcodeconfigcode and bbv.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and bb.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bb.nsitecode="
				+ userInfo.getNmastersitecode();

		final List<BulkBarcodeConfigDetails> barcodelistLength = jdbcTemplate.query(query,
				new BulkBarcodeConfigDetails());

		if (!barcodelistLength.isEmpty()) {
			final int nbarcodeLength = barcodeLength.length();
			int formattingLength = barcodelistLength.get(0).getNfieldlength();

			if (nbarcodeLength < formattingLength) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_INVAILDBARCODEID", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			} else if (nbarcodeLength > formattingLength) {
				final String daugtherAliquotPlan = "select ssettingvalue from settings where nsettingcode="
						+ Enumeration.Settings.DAUGHTERALIQUOTPLAN.getNsettingcode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(daugtherAliquotPlan,
						Settings.class, jdbcTemplate);

				if (objSettings != null) {
					final int daugtherAliquotPlanLength = Integer.parseInt(objSettings.getSsettingvalue());
					formattingLength = formattingLength + daugtherAliquotPlanLength;
					if (nbarcodeLength == formattingLength) {
						isBarcodeID = true;
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVAILDBARCODEID",
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

					}
				}
			} else {
				isBarcodeID = true;
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PROJECTCONFIGINFORMATTINGSCREEN",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
		// ALPD-5074 Sample Storage -> THIST - Daughter Aliquot count of barcode ID.
		if (isBarcodeID) {

			if ((int) inputMap.get("nbarcodedescription") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				barcodeDetails = readBarcode(inputMap, userInfo);
				if ((barcodeDetails.get("daugtherAliquotPlanLength").equals(false)
						&& barcodeDetails.get("daugtherAliquotPlanLength") != null)
						|| (barcodeDetails.containsKey("rtn") && barcodeDetails.get("rtn") != null
						&& barcodeDetails.get("rtn").equals(false))) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVAILDBARCODEID",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

				}

				barcodeDetailsQty = fetchQuantity(barcodeDetails, inputMap, userInfo);
				inputMap.put("jsondata", barcodeDetailsQty);

			}
			final String strCheckPosition = "select * from samplestoragetransaction where "
					+ "nsamplestoragelocationcode=" + inputMap.get("nsamplestoragelocationcode")
					+ " and nsamplestoragemappingcode=" + inputMap.get("nsamplestoragemappingcode") + " and sposition='"
					+ inputMap.get("sposition") + "' and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			lstCheckValue = jdbcTemplate.query(strCheckPosition, new SampleStorageTransaction());

			final SampleStorageTransaction samplestoragetransaction = new SampleStorageTransaction();
			samplestoragetransaction.setNsamplestoragemappingcode((int) inputMap.get("nsamplestoragemappingcode"));
			samplestoragetransaction.setSposition(inputMap.get("sposition").toString());
			samplestoragetransaction.setSpositionvalue(inputMap.get("spositionvalue").toString());
			samplestoragetransaction.setDmodifieddate(dateUtilityFunction.getCurrentDateTime(userInfo));
			samplestoragetransaction.setNsitecode(userInfo.getNtranssitecode());
			samplestoragetransaction.setNstatus((short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
			if (!(boolean) inputMap.get("multiUserCheck") && !lstCheckValue.isEmpty()) {
				checkValue = false;
			} else {
				checkValue = true;
			}
			if (checkValue) {
				String strCheckValue = "select * from samplestoragetransaction where " + " spositionvalue='"
						+ inputMap.get("spositionvalue") + "' and nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				if (!lstCheckValue.isEmpty() && lstCheckValue.get(0)
						.getNpositionfilled() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					beforeSavedTestList.add(lstCheckValue.get(0));
					strCheckValue = strCheckValue + " and nsamplestoragetransactioncode <> "
							+ lstCheckValue.get(0).getNsamplestoragetransactioncode();
					final List<Map<String, Object>> lstCheck = jdbcTemplate.queryForList(strCheckValue);
					if (lstCheck.isEmpty()) { // ALPD-4480 janakumar Sample Storage-->cannot be able to edit the Sample
						// storage Box. When the popup is Open.500 Error Occurs
						final String strUpdateQuery = "update samplestoragetransaction set spositionvalue='"
								+ stringUtilityFunction.replaceQuote(inputMap.get("spositionvalue").toString())
								+ "', jsondata='"
								+ stringUtilityFunction.replaceQuote(inputMap.get("jsondata").toString())
								+ "', dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "', nprojecttypecode=" + inputMap.get("nprojecttypecode")
								+ ", nsamplestoragemappingcode=" + inputMap.get("nsamplestoragemappingcode")
								+ ", nsamplestoragelocationcode=" + inputMap.get("nsamplestoragelocationcode")
								+ ", nsitecode=" + userInfo.getNtranssitecode()
								+ " where nsamplestoragetransactioncode="
								+ lstCheckValue.get(0).getNsamplestoragetransactioncode() + " "
								+ " and nsamplestoragelocationcode=" + inputMap.get("nsamplestoragelocationcode") + ""
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

						jdbcTemplate.execute(strUpdateQuery);

						samplestoragetransaction
						.setNsamplestoragelocationcode(lstCheckValue.get(0).getNsamplestoragelocationcode());
						samplestoragetransaction.setNsamplestoragetransactioncode(
								lstCheckValue.get(0).getNsamplestoragetransactioncode());

						savedTestList.add(samplestoragetransaction);

						multilingualIDList.add("IDS_EDITSAMPLESTORAGE");
						auditUtilityFunction.fnInsertAuditAction(savedTestList, 2, beforeSavedTestList,
								multilingualIDList, userInfo);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYEXISTS",
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}

				} else {
					final List<Map<String, Object>> lstCheck = jdbcTemplate.queryForList(strCheckValue);
					if (lstCheck.isEmpty()) {

						final String sectSeq = "select nsequenceno from seqnobasemaster where stablename='samplestoragetransaction' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final Integer objseq = jdbcTemplate.queryForObject(sectSeq, Integer.class);

						final int nsamplestoragetransactioncode = objseq + 1;
						Query = " insert into samplestoragetransaction (nsamplestoragetransactioncode, nsamplestoragelocationcode, nsamplestoragemappingcode, nprojecttypecode, sposition,"
								+ "	spositionvalue, jsondata, npositionfilled, dmodifieddate, nsitecode, nstatus)  values ( "
								+ nsamplestoragetransactioncode + "," + inputMap.get("nsamplestoragelocationcode") + ","
								+ inputMap.get("nsamplestoragemappingcode") + "," + inputMap.get("nprojecttypecode")
								+ " ,'" + stringUtilityFunction.replaceQuote(inputMap.get("sposition").toString())
								+ "','" + stringUtilityFunction.replaceQuote(inputMap.get("spositionvalue").toString())
								+ "', '" + stringUtilityFunction.replaceQuote(inputMap.get("jsondata").toString())
								+ "'," + Enumeration.TransactionStatus.YES.gettransactionstatus() + " ,'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode()
								+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") ;";
						updateQuery = " update seqnobasemaster set nsequenceno= " + nsamplestoragetransactioncode
								+ " where stablename='samplestoragetransaction';";
						jdbcTemplate.execute(Query + updateQuery);

						final Integer nsamplestoragelocationcodeint = (Integer) inputMap
								.get("nsamplestoragelocationcode");
						final Short nsamplestoragelocationcodeintshort = nsamplestoragelocationcodeint.shortValue();

						samplestoragetransaction.setNsamplestoragelocationcode(nsamplestoragelocationcodeintshort);
						samplestoragetransaction.setNsamplestoragetransactioncode(nsamplestoragetransactioncode);

						savedTestList.add(samplestoragetransaction);
						multilingualIDList.add("IDS_ADDSAMPLESTORAGE");
						auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYEXISTS",
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				}
			} else {
				inputMap.put("isAlreadyExists", true);
			}

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_INVAILDBARCODEID", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		return getSheetData(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve the list of available Sheet data.
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 	 which the list is to be fetched,nsamplestoragelocationcode:defines the storage structure code,nsamplestoragemappingcode:Defines the sample storage mapping code,
	 * sposition:Defines the barcode id respective project type.
	 * 					Input : {"userinfo":{nmastersitecode": -1},"sposition":"P12345678911111","nsamplestoragelocationcode":1,"nsamplestoragemappingcode":1}	
	 * 			
	 * @return response entity  object holding response status and list of available Sheet Data.
	 * 
	 * @throws Exception exception
	 */
	
	public ResponseEntity<Object> getSheetData(final Map<String, Object> inputMap, final UserInfo userInfo) {
		final Map<String, Object> outputMap = new HashMap<>();
		Map<String, Object> map = new HashMap<>();
		String strQuery = "select *, jsondata  as \"additionalInfo\" from samplestoragetransaction where nsamplestoragelocationcode="
				+ inputMap.get("nsamplestoragelocationcode") + " and nsamplestoragemappingcode="
				+ inputMap.get("nsamplestoragemappingcode") + " and sposition='" + inputMap.get("sposition")
				+ "' and npositionfilled=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNtranssitecode();

		try {
			map = jdbcTemplate.queryForMap(strQuery);
		} catch (final Exception e) {
			map = new HashMap<>();
		}
		outputMap.put("cellData", map);

		strQuery = "(select ssm.nnoofcontainer-count(st.sposition) as navailablespace from samplestoragetransaction st ,samplestoragemapping ssm "
				+ "		 where   ssm.nsamplestoragemappingcode=st.nsamplestoragemappingcode "
				+ " and st.nsamplestoragemappingcode= " + inputMap.get("nsamplestoragemappingcode")
				+ "		and st.spositionvalue!='' " + " group by ssm.nnoofcontainer " + "		)";

		try {
			map = jdbcTemplate.queryForMap(strQuery);
		} catch (final Exception e) {
			map = new HashMap<>();
		}
		outputMap.put("navailablespace", map);

		if (inputMap.containsKey("isAlreadyExists")) {
			outputMap.put("isAlreadyExists", inputMap.get("isAlreadyExists"));
		}

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method is used to modify the samplestoragemapping.  
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 	which the list is to be fetched ,nsamplestoragemappingcode:Define the value of sample storage mapping code,nsamplestoragelocationcode:Define the value of storage structure code,
	 * sboxid:Define the Container id of storage.	
	 * 					Input : {"userinfo":{nmastersitecode": -1},"nsamplestoragemappingcode":1,"nsamplestoragelocationcode":1,"sboxid":"C1"}	
	 * 			
	 * @return response entity  object holding response status and list of available container id.
	 * 
	 * @throws Exception exception
	 */
	
	@Override
	public ResponseEntity<Object> updateSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		String updateQuery = "";
		final Map<String, Object> mapObj = new HashMap<String, Object>();
		final String strcheck = "select count(nsamplestoragemappingcode) from samplestoragemapping ssm,samplestoragecontainerpath ssc "
				+ "where ssm.sboxid=N'" + stringUtilityFunction.replaceQuote(inputMap.get("sboxid").toString()) + "'  "
				+ " and ssm.nsamplestoragemappingcode <> " + inputMap.get("nsamplestoragemappingcode")
				+ " and ssc.nsamplestoragecontainerpathcode=ssm.nsamplestoragecontainerpathcode "
				+ " and ssc.nsamplestoragelocationcode=" + inputMap.get("nsamplestoragelocationcode")
				+ " and ssm.nsitecode=" + userInfo.getNmastersitecode() + " and ssc.nsitecode="
				+ userInfo.getNmastersitecode();

		Integer check = null;
		boolean isExist = false;
		try {
			check = jdbcTemplate.queryForObject(strcheck, Integer.class);
		} catch (final Exception e) {
			check = null;
		}
		if (check == 0) {
			final int nsamplestoragemappingcode = (int) inputMap.get("nsamplestoragemappingcode");
			updateQuery = " UPDATE public.samplestoragemapping SET   sboxid='"
					+ stringUtilityFunction.replaceQuote(inputMap.get("sboxid").toString())
					+ "'	WHERE nsamplestoragemappingcode=" + nsamplestoragemappingcode + ";";

			jdbcTemplate.execute(updateQuery);
			isExist = false;
			mapObj.put("sboxid", inputMap.get("sboxid").toString());
		} else {
			final String strGetQuery = "select sboxid from samplestoragemapping where nsamplestoragemappingcode="
					+ inputMap.get("nsamplestoragemappingcode") + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final String boxValue = jdbcTemplate.queryForObject(strGetQuery, String.class);
			mapObj.put("sboxid", boxValue);
			isExist = true;
		}
		mapObj.put("existCheck", isExist);
		return new ResponseEntity<>(mapObj, HttpStatus.OK);
	}
/*
	@Override
	public ResponseEntity<Map<String, Object>> deleteSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final int nsamplestoragemappingcode = (int) inputMap.get("nsamplestoragemappingcode");
		final String updateQuery = "  delete from samplestoragemapping WHERE nsamplestoragemappingcode="
				+ nsamplestoragemappingcode + ";";
		jdbcTemplate.execute(updateQuery);
		return getsamplestoragetransaction(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> approveSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final int nsamplestoragelocationcode = (int) inputMap.get("nsamplestoragelocationcode");
		String updateQuery = "";
		final int nmappingtranscode = jdbcTemplate
				.queryForObject("select nmappingtranscode from samplestoragelocation where nsamplestoragelocationcode="
						+ nsamplestoragelocationcode, Short.class);

		if (nmappingtranscode == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYAPPROVED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			updateQuery = "  update   samplestoragelocation set nmappingtranscode="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " where  nsamplestoragelocationcode=" + nsamplestoragelocationcode + ";";

			jdbcTemplate.execute(updateQuery);

			return getActiveSampleStorageMappingById(inputMap, userInfo);
		}
	}
	*/
	/**
	 *This method is used to retrieve the list of available data by determining the cell position. 
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched ,isMultiSampleAdd:This method is used to determine whether the container should allow multiple showing or single showing,
	 * nsamplestoragemappingcode: Indicates the primary key or value field used for identifying each record in the sample storage mapping.
	 * 		
	 * Input : {"userinfo":{nmastersitecode": -1},"isMultiSampleAdd":false,"nsamplestoragemappingcode":1}	
	 * 			
	 * @return response entity  object holding response status and list of available Container.
	 * 
	 * @throws Exception exception
	 */

	@Override
	public ResponseEntity<Object> getsamplestoragemappingSheetData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		Map<String, Object> map = new HashMap<>();
		final String nsamplestoragemappingcode = (String) inputMap.get("nsamplestoragemappingcode");

		String strQuery = "";

		if ((boolean) inputMap.get("isMultiSampleAdd")) {
			strQuery = "select jsonb_object_agg(x.nsamplestoragemappingcode,x.jsondata)   from "
					+ "(select nsamplestoragemappingcode,jsonb_object_agg( sposition,jsonb_build_object('spositionvalue',spositionvalue, "
					+ "	  'additionalInfo', jsondata " + " ) )  as jsondata  "
					+ "from  samplestoragetransaction where nsamplestoragemappingcode in (" + nsamplestoragemappingcode
					+ ")  and npositionfilled= " + Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " group by nsamplestoragemappingcode)x";
		} else {
			strQuery = "select jsonb_object_agg( sposition,jsonb_build_object('spositionvalue',spositionvalue, "
					+ " 'additionalInfo'," + " jsondata ) ) as jsondata "
					+ " from samplestoragetransaction where nsamplestoragemappingcode=" + nsamplestoragemappingcode
					+ " and npositionfilled= " + Enumeration.TransactionStatus.YES.gettransactionstatus();
		}
		System.out.println(strQuery);
		String strMaterial = jdbcTemplate.queryForObject(strQuery, String.class);
		outputMap.put("sheetData", strMaterial);
		strQuery = " SELECT Jsonb_object_agg(x.nsamplestoragemappingcode, x.sboxid) "
				+ "FROM   (SELECT nsamplestoragemappingcode,sboxid " + "        FROM   samplestoragemapping "
				+ "        WHERE  nsamplestoragemappingcode IN ( " + nsamplestoragemappingcode + " )  "
				+ "        GROUP  BY nsamplestoragemappingcode, sboxid)x ";
		strMaterial = jdbcTemplate.queryForObject(strQuery, String.class);
		outputMap.put("AdditionalFieldsComponentData", strMaterial);

		strQuery = "(select ssm.nnoofcontainer-count(st.sposition) as navailablespace from samplestoragetransaction st ,samplestoragemapping ssm "
				+ "		 where   ssm.nsamplestoragemappingcode=st.nsamplestoragemappingcode "
				+ " and st.nsamplestoragemappingcode= " + nsamplestoragemappingcode + "		and st.spositionvalue!='' "
				+ " group by ssm.nnoofcontainer " + "		)";

		try {
			map = jdbcTemplate.queryForMap(strQuery);
		} catch (final Exception e) {
			map = new HashMap<>();
		}
		outputMap.put("navailablespace", map);

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve the list of available data based on the provided filter criteria.
	 * 
	 *@param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched ,label: Specifies the field to be used as the display label in the result set,valuemember: Indicates the primary key or value field used for identifying each record in the sample storage mapping,
	 * filterquery: Defines the condition or filter to be applied while retrieving the data,source: Specifies the table from which the data needs to be read.	
	 * 					
	 * Input : {"userinfo":{nmastersitecode": -1},"label":"sampleStoragetransaction",valuemember:"nsamplestoragemappingcode",
	 * "filterquery":"nproductcode = 6 and nprojecttypecode=1","source":"view_samplestoragelocation"}	
	 * 			
	 * @return response entity  object holding response status and list of available Sample Storage.
	 * 
	 * @throws Exception exception
	 */
	
	@Override
	public ResponseEntity<Object> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		String tableName = "";
		String getJSONKeysQuery = "";
		final Map<String, Object> returnObject = new HashMap<>();
		final String sourceName = (String) inputMap.get("source");
		String conditionString = inputMap.containsKey("conditionstring") ? (String) inputMap.get("conditionstring")
				: "";
		if (conditionString.isEmpty()) {
			conditionString = inputMap.containsKey("filterquery") ? "and " + (String) inputMap.get("filterquery") : "";
		}
		final String str = "select ssettingvalue from settings where nsettingcode=40 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(str, Settings.class, jdbcTemplate);
		String conditionSettingStr = "";
		if (Integer.parseInt(objSettings.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			conditionSettingStr = "  AND  nmappingtranscode="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
		}
		final String scollate = "collate \"default\"";
		if (conditionString.contains("LIKE")) {

			while (conditionString.contains("LIKE")) {
				final String sb = conditionString;
				String sQuery = conditionString;
				final int colanindex = sb.indexOf("LIKE '");
				final String str1 = sQuery.substring(0, colanindex + 6);
				sQuery = sQuery.substring(colanindex + 6);
				final StringBuilder sb3 = new StringBuilder(str1);
				final StringBuilder sb4 = new StringBuilder(sQuery);
				sb3.replace(colanindex, colanindex + 4, "ilike");
				System.out.println(sQuery);
				final int indexofsv = sQuery.indexOf("'");

				sb4.replace(indexofsv, indexofsv + 1, "'" + scollate + " ");
				conditionString = sb3.toString() + sb4.toString();
			}

		}

		tableName = sourceName.toLowerCase();

		final String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "' and data_type = 'jsonb'";
		String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
		jsonField = jsonField != null ? "||" + jsonField : "";
		final String getFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "'";
		final String fields = jdbcTemplate.queryForObject(getFieldQuery, String.class);
		if (fields.contains(inputMap.get("valuemember").toString())) {
			getJSONKeysQuery = "select " + tableName + ".* from " + tableName + " where nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and \""
					+ inputMap.get("valuemember") + "\" > '0' " + conditionString + "" + conditionSettingStr + " ;";
		} else {
			getJSONKeysQuery = "select  " + tableName + ".* from " + tableName + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + conditionString + ""
					+ conditionSettingStr + " ;";
		}
		final List<Map<String, Object>> data = jdbcTemplate.queryForList(getJSONKeysQuery);
		returnObject.put((String) inputMap.get("label"), data);
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	
	/**
	 * This method is used to retrieve the list of available barcode information to using barcode master screen tables.
	 * 
	 *@param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched ,spositionvalue: Barcode id of the project type,nprojecttypecode: Indicates the primary key or value field used for identifying each record in the Project Type,
	 * 					
	 * Input : {"userinfo":{nmastersitecode": -1},"spositionvalue":"P1234567890234",
	 * nprojecttypecode=1}	
	 * 			
	 * @return response entity  object holding response status and list of available Sample Storage.
	 * 
	 * @throws Exception exception
	 */
	
	@Override
	public Map<String, Object> readBarcode(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final String barcode = inputMap.get("spositionvalue").toString().replaceAll("\\s", "");
		final int nprojectcode = (int) inputMap.get("nprojecttypecode");
		int nbarcodeLength = barcode.length();
		final Map<String, Object> returnMap = new HashMap<>();
		final JSONObject objJsonDataList = new JSONObject(inputMap.get("jsondata").toString());
		final JSONObject objJsonPrimaryKeyValue = new JSONObject();

		List<Map<String, Object>> value = null;
		String subBarcode = "";
		final String query = " select bbd.* from bulkbarcodeconfigdetails bbd,bulkbarcodeconfig bb,bulkbarcodeconfigversion bbv where "
				+ " bbd.nprojecttypecode= " + nprojectcode
				+ " and bbd.nbulkbarcodeconfigcode=bb.nbulkbarcodeconfigcode and "
				+ " bb.nbulkbarcodeconfigcode=bbv.nbulkbarcodeconfigcode and bbv.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and bbd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bb.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbd.nsitecode="
				+ userInfo.getNmastersitecode() + " and bb.nsitecode=" + userInfo.getNmastersitecode()
				+ " and bbv.nsitecode=" + userInfo.getNmastersitecode() + " order by nsorter ";

		final List<BulkBarcodeConfigDetails> barcodelist = jdbcTemplate.query(query, new BulkBarcodeConfigDetails());

		// objJsonDataList.put("nbulkbarcodeconfigcode",
		// barcodelist.get(0).getNbulkbarcodeconfigcode());
		// ALPD-5082 Added fieldsToCalculate and visitNumberField to store field names
		// by VISHAKH
		final List<String> fieldsToCalculate = new ArrayList<>();
		final List<String> visitNumberField = new ArrayList<>();
		int fieldLenth = 0;
		int daugtherAliquotPlanLength = 0;
		boolean isDaugtherAliquotPlan = false;
		if (!barcodelist.isEmpty()) {
			fieldLenth = jdbcTemplate.queryForObject(
					"select COALESCE( sum(nfieldlength)::int,0) from bulkbarcodeconfigdetails where nbulkbarcodeconfigcode="
							+ barcodelist.get(0).getNbulkbarcodeconfigcode() + " and nprojecttypecode="
							+ barcodelist.get(0).getNprojecttypecode() + " and nstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNmastersitecode(),
							Integer.class);
			// ATE234 janakumar -> ALPD-5074 Sample Storage -> THIST - Daughter Aliquot
			// count of barcode ID.

			if ((nbarcodeLength != fieldLenth)) {
				final String daugtherAliquotPlan = "select ssettingvalue from settings where nsettingcode="
						+ Enumeration.Settings.DAUGHTERALIQUOTPLAN.getNsettingcode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(daugtherAliquotPlan,
						Settings.class, jdbcTemplate);
				if (objSettings != null) {
					final String lengthValidation;
					daugtherAliquotPlanLength = Integer.parseInt(objSettings.getSsettingvalue());
					lengthValidation = barcode.substring(fieldLenth);
					if (lengthValidation.length() == daugtherAliquotPlanLength) {
						nbarcodeLength = nbarcodeLength - daugtherAliquotPlanLength;
						isDaugtherAliquotPlan = true;
						final String strBarcode = (String) barcode.subSequence(0, fieldLenth);
						returnMap.put("daugtherAliquotPlanLength", isDaugtherAliquotPlan);
						returnMap.put("daugtherAliquotFilterBarcode", strBarcode);

					} else {
						returnMap.put("daugtherAliquotPlanLength", isDaugtherAliquotPlan);
						isDaugtherAliquotPlan = false;
					}

				}
			} else {
				daugtherAliquotPlanLength = 0;
				isDaugtherAliquotPlan = true;
				returnMap.put("daugtherAliquotPlanLength", isDaugtherAliquotPlan);
				returnMap.put("daugtherAliquotFilterBarcode", barcode);

			}
		}
		int lastIndex = 0;
		try {
			if (!barcodelist.isEmpty() && (nbarcodeLength == fieldLenth) && isDaugtherAliquotPlan) {
				for (int i = 0; i < barcodelist.size(); i++) {
					subBarcode = "";
					if (barcodelist.get(i).getJsondata().containsKey("isneedparent")
							&& (int) barcodelist.get(i).getJsondata()
							.get("isneedparent") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

						final Map<String, Object> parentValueCode = (Map<String, Object>) barcodelist.get(i)
								.getJsondata().get("parentData");

						final String strQuery = "select * from bulkbarcodeconfigdetails "
								+ " where nbulkbarcodeconfigdetailscode="+ parentValueCode.get("nbulkbarcodeconfigdetailscode")+" "
								+ " and nsitecode = "+userInfo.getNmastersitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

						final BulkBarcodeConfigDetails objBulkBarcodeConfigDetails = (BulkBarcodeConfigDetails) jdbcUtilityFunction
								.queryForObject(strQuery, BulkBarcodeConfigDetails.class, jdbcTemplate);

						final int nfieldstartpostition = objBulkBarcodeConfigDetails.getNfieldstartposition();
						final int nfieldlength = objBulkBarcodeConfigDetails.getNfieldlength();
						subBarcode = barcode.substring(nfieldstartpostition, nfieldstartpostition + nfieldlength);
						final String subBarCodePunchSite = barcode.substring(
								(barcodelist.get(i).getNfieldstartposition() - lastIndex),
								barcodelist.get(i).getNfieldstartposition() + barcodelist.get(i).getNfieldlength());

						final String querys = "select b." + barcodelist.get(i).getJsondata().get("sprimarykeyfieldname")
								+ "  from  " + objBulkBarcodeConfigDetails.getStablename() + " a ,"
								+ barcodelist.get(i).getStablename() + "  b" + " where a."
								+ objBulkBarcodeConfigDetails.getStablecolumnname() + " ='" + subBarcode + "' and a."
								+ objBulkBarcodeConfigDetails.getJsondata().get("sprimarykeyfieldname") + "=b."
								+ objBulkBarcodeConfigDetails.getJsondata().get("sprimarykeyfieldname") + " "
								+ "and b.nprojecttypecode=" + inputMap.get("nprojecttypecode") + " and b.ncode="
								+ subBarCodePunchSite + " and b.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

						value = jdbcTemplate.queryForList(querys);
						if (value.isEmpty()) {
							subBarcode = "";
							lastIndex = 1;
						} else {
							subBarcode = barcode.substring((barcodelist.get(i).getNfieldstartposition() - lastIndex),
									barcodelist.get(i).getNfieldstartposition() + barcodelist.get(i).getNfieldlength());
							lastIndex = 0;
						}
					} else {

						subBarcode = barcode.substring((barcodelist.get(i).getNfieldstartposition() - lastIndex),
								barcodelist.get(i).getNfieldstartposition() + barcodelist.get(i).getNfieldlength());
						lastIndex = 0;
						System.out.println("Barcode Values -->" + subBarcode);

					}
					final String skeyname = barcodelist.get(i).getJsondata().get("sfieldname").toString();
					// ALPD-5082 Added fieldsToCalculate and visitNumberField to store field names
					// by VISHAKH - START
					if (barcodelist.get(i).getJsondata().containsKey("isvalidationrequired") && (int) barcodelist.get(i)
							.getJsondata().get("isvalidationrequired") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						fieldsToCalculate.add(skeyname);
					}
					if (barcodelist.get(i).getStablename() != null
							&& barcodelist.get(i).getStablename().equals("visitnumber")) {
						visitNumberField.add(skeyname);
					}
					// ALPD-5082 Added fieldsToCalculate and visitNumberField to store field names
					// by VISHAKH - END
					String querys = "";
					if (!subBarcode.isEmpty()) {
						if (barcodelist.get(i).getNneedmaster() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
							final String stablename = barcodelist.get(i).getStablename();
							final String stablecoumnname = barcodelist.get(i).getStablecolumnname();
							final String stablefieldname = barcodelist.get(i).getJsondata().get("stablefieldname")
									.toString();

							final String sprimarykeyfieldname = barcodelist.get(i).getJsondata()
									.get("sprimarykeyfieldname").toString();

							if (barcodelist.get(i).getNqueryneed() == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {

								String squery = barcodelist.get(i).getSquery();
								final StringBuilder sbuilder1 = new StringBuilder();
								if (squery != null) {
									sbuilder1.append(squery);
									while (squery.contains("<#")) {
										final int nStart = squery.indexOf("<#");
										final int nEnd = squery.indexOf("#>");
										sbuilder1.replace(nStart, nEnd + 2, subBarcode);
										squery = sbuilder1.toString();
									}
									querys = squery + " and pt.nprojecttypecode=" + inputMap.get("nprojecttypecode")
									+ ";";
								}
							} else {
								// ATE-234 Janakumar ALPD-5092 Sample Collection-->Based on Sample Type and Code
								// the Punch Description is loaded wrongly in Sample Collection,Sample Receiving
								// ,Sample Processing and Temporary Storage
								if (barcodelist.get(i).getJsondata().containsKey("isneedparent") && (int) barcodelist
										.get(i).getJsondata().get("isneedparent") == Enumeration.TransactionStatus.YES
										.gettransactionstatus()) {

									querys = "select " + stablefieldname + "," + sprimarykeyfieldname + "  from  "
											+ stablename + " where " + sprimarykeyfieldname + "="
											+ value.get(0).get(sprimarykeyfieldname) + "  and nprojecttypecode="
											+ inputMap.get("nprojecttypecode") + " and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " order by 1 desc;";

								} else {
									querys = "select " + stablefieldname + "," + sprimarykeyfieldname + "  from  " + stablename
											+ " where " + stablecoumnname + "='" + subBarcode + "'  and nprojecttypecode="
											+ inputMap.get("nprojecttypecode") + " and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc;";

								}
							}
							try {
								value = jdbcTemplate.queryForList(querys);
							} catch (final Exception e) {
								((JSONObject) value).put(stablefieldname, "-");

							}
							if (!value.isEmpty()) {
								objJsonDataList.put(skeyname, value.get(0).get(stablefieldname));
								objJsonPrimaryKeyValue.put(sprimarykeyfieldname,
										value.get(0).get(sprimarykeyfieldname));
							} else {
								objJsonDataList.put(skeyname, "-");
								objJsonPrimaryKeyValue.put(sprimarykeyfieldname, -1);
							}

						} else {
							objJsonDataList.put(skeyname, subBarcode);
						}
					} else {
						objJsonDataList.put(skeyname, "-");
					}

				}
			}

			if (!barcodelist.isEmpty()) {
				objJsonPrimaryKeyValue.put("nbulkbarcodeconfigcode", barcodelist.get(0).getNbulkbarcodeconfigcode());
			}

			returnMap.put("jsonValue", objJsonDataList);
			returnMap.put("objJsonPrimaryKeyValue", objJsonPrimaryKeyValue);
			// ALPD-5082 Added fieldsToCalculate and visitNumberField to store field names
			// by VISHAKH
			returnMap.put("fieldsToCalculate", fieldsToCalculate);
			returnMap.put("visitNumberField", visitNumberField);

		} catch (final Exception e) {
			// ((JSONObject) value).put(stablefieldname, "-");
			// ALPD-5094 Sample Storage -->500 error when enter daughter aliquot in barcode
			returnMap.put("rtn", false);

		}
		return returnMap;
	}

	/**
	 * This method is used to retrieve the list of available sample storage related data.
	 * 
	* @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched ,nquantity: Specifies the field to be used as the display label in the result set,nprojecttypecode: Indicates the primary key or value field used for identifying project type,
	 * ncolumn: Defines the container of column,nrow: Defines the container of row,nsamplestoragelocationcode:Defines the storage location code,
	 * nsamplestoragemappingcode:Define the storage structure mapping code,scomments: Define the comments of storage structure,"scontainerpath":Define the storage structure path,"sprojecttypename":Define the structure of the project,"ssampleid":Define the structure of the sample id,
	 * "ssamplestoragelocationname": Define the storage location name,"sunitname":Define the storage unit name,"nquantity":Define the quantity of storage structure
	 * 					
	 * Input : {"userinfo":{nmastersitecode": -1},"ncolumn":10,"nrow":10,"nquantity":0,
	 * "nsamplestoragelocationcode":2,"nsamplestoragemappingcode":3,"scomments":"comment","scontainerpath","FZ > Rack > Tray > Cell Position":,"sunitname":"ml"}	
	 * 			
	 * @return response entity  object holding response status and list of available sample storage related data.
	 * 
	 * @throws Exception exception
	 */
	
	@Override
	public ResponseEntity<Object> getSingleExport(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final int nsamplestoragemappingcode = (int) inputMap.get("nsamplestoragemappingcode");
		final String ssamplestoragelocationname = (String) inputMap.get("ssamplestoragelocationname");

		final String strQuery = "select sposition from samplestoragetransaction where nsamplestoragemappingcode="
				+ nsamplestoragemappingcode + " and npositionfilled= "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus();

		final List<String> strMaterial = jdbcTemplate.queryForList(strQuery, String.class);
		outputMap.put("sheetData", strMaterial);

		final List<String> cellsValues = exportCellsValues(strMaterial, inputMap, userInfo);

		final List<Map<String, Object>> headercolumn = getexportheadercolumn(userInfo);

		if (!cellsValues.isEmpty()) {

			final XSSFWorkbook workbook = new XSSFWorkbook();
			final XSSFSheet spreadsheet = workbook.createSheet("Excelvalues");
			final XSSFFont font = workbook.createFont();
			font.setBold(true);
			font.setFontHeightInPoints((short) 10);

			final CellStyle style = workbook.createCellStyle();

			final CellStyle lockedCellStyle = workbook.createCellStyle();
			lockedCellStyle.setLocked(true);
			lockedCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			// lockedCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			lockedCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			final CellStyle unlockedStyle = workbook.createCellStyle();
			unlockedStyle.setLocked(false); // Unlock the cells

			final CellStyle greyStyle = workbook.createCellStyle();
			greyStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			greyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			// Create header row
			final XSSFRow headerRow = spreadsheet.createRow(0);
			for (int i = 0; i < headercolumn.size(); i++) {
				final String headerValue = (String) headercolumn.get(i).get("sexpheadername");
				final Cell cell = headerRow.createCell(i);
				cell.setCellStyle(greyStyle);
				cell.setCellValue(commonFunction.getMultilingualMessage(headerValue, userInfo.getSlanguagefilename()));
				style.setBorderTop(BorderStyle.THIN);
			}
			final String unitValidation = "select string_agg(sunitname ,',') from unit where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			final String strUnit = jdbcTemplate.queryForObject(unitValidation, String.class);
			String[] dropDownValues;
			if (strUnit != null) {
				dropDownValues = strUnit.split(",");
			} else {
				dropDownValues = new String[] { "NA" };
			}

			final DataValidationHelper validationHelper = spreadsheet.getDataValidationHelper();

			final DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(dropDownValues);

			// Fill data rows
			int rowIndex = 1;
			int count = 0;
			for (int i = 0; i < cellsValues.size(); i++) {
				final XSSFRow row = spreadsheet.createRow(rowIndex++);
				for (int j = 0; j < headercolumn.size(); j++) {
					final Cell cell = row.createCell(j);

					final String sexpheadername = (String) headercolumn.get(j).get("sexpheadername");
					final String sexpcolumnname = (String) headercolumn.get(j).get("sexpcolumnname");
					final int nisvisible = (int) headercolumn.get(j).get("nisvisible");
					final int niseditable = (int) headercolumn.get(j).get("niseditable");

					final Object cellnisvisibleObj = inputMap.get(sexpcolumnname);
					final String cellValues = cellnisvisibleObj != null ? cellnisvisibleObj.toString() : " ";

					if ("IDS_SAMPLEPOSITION".equals(sexpheadername)) {
						cell.setCellValue(cellsValues.get(count));
						count++;

						cell.setCellStyle(lockedCellStyle);
					} else if ("IDS_FROMUNIT".equals(sexpheadername)) {

						final CellRangeAddressList addressList = new CellRangeAddressList(i, i, j, j);

						final DataValidation dataValidation = validationHelper.createValidation(constraint,
								addressList);

						dataValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);

						dataValidation.setShowErrorBox(true);

						spreadsheet.addValidationData(dataValidation);

						cell.setCellValue(cellValues);

						cell.setCellStyle(unlockedStyle);
					} else if ("IDS_QUANTITY".equals(sexpheadername)) {
						final int value = Integer.parseInt(cellValues);
						cell.setCellValue(value);
						cell.setCellStyle(unlockedStyle);
					} else {

						if (nisvisible == Enumeration.ExportCell.VISIBLEHIDE.getExportCell()) {

							cell.setCellStyle(lockedCellStyle);

							spreadsheet.setColumnHidden(j, true);
						}

						if (niseditable == Enumeration.ExportCell.EDIT.getExportCell()) {

							cell.setCellStyle(unlockedStyle);
							final String trimmedValue = cell.getStringCellValue().trim();
							cell.setCellValue(trimmedValue);

						} else {

							cell.setCellStyle(lockedCellStyle);
						}

						cell.setCellValue(cellValues);
					}
				}
			}

			style.setFont(font);

			for (int i = 0; i < headercolumn.size(); i++) {
				final String sexpheadername = (String) headercolumn.get(i).get("sexpheadername");
				if ("IDS_SAMPLEID".equals(sexpheadername) || "IDS_COMMENTS".equals(sexpheadername)) {
					spreadsheet.setColumnWidth(i, 8000);
				} else if ("IDS_FROMUNIT".equals(sexpheadername)) {
					spreadsheet.setColumnWidth(i, 3000);
				} else {
					spreadsheet.autoSizeColumn(i);
				}
			}

			spreadsheet.protectSheet("password");

			try {
				final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
				final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH-mm-ss");
				final Date date = new Date();
				final String currentDate = dateFormatter.format(date);
				final String currentTime = timeFormatter.format(date);

				final String outputFileName = ssamplestoragelocationname + "_" + currentDate + " " + currentTime
						+ ".xls";
				LOGGER.error("Error:Output Export File name " + outputFileName + currentTime);

				// Get path value from system's environment variables instead of absolute path
				final String homePath = ftpUtilityFunction.getFileAbsolutePath();
				
				//commented by Rajesh 20-JUN-2025
				//final String targetPath = System.getenv(homePath) + Enumeration.FTP.UPLOAD_PATH.getFTP()
				//+ outputFileName;
				
				//Added by Rajesh 20-JUN-2025 for AWS
				String targetPath = "";

				if (FTPUtilityFunction.IS_UNIX && homePath.equals("JBOSS_HOME")) {
					targetPath = System.getenv(homePath) + Enumeration.FTP.JBOSS_EAP_7_2_0_UPLOAD_PATH.getFTP() + outputFileName;
				} else if (FTPUtilityFunction.IS_UNIX && homePath.equals("TOMCAT_HOME")) {
					targetPath = System.getenv(homePath) + Enumeration.FTP.UBUNTU_TOMCAT_UPLOAD_PATH.getFTP() + outputFileName;
				} else if (FTPUtilityFunction.IS_WINDOWS && homePath.equals("TOMCAT_HOME")) {
					targetPath = System.getenv(homePath) + Enumeration.FTP.UPLOAD_PATH.getFTP() + outputFileName;
				}
				

				LOGGER.error("Error:Output Export File name " + targetPath);

				// Write to file
				final FileOutputStream out = new FileOutputStream(new File(targetPath));

				final String sDownloadPathName = getExportDownloadPathName();
				if (sDownloadPathName != null) {
					outputMap.put("exportFileViewUrl", sDownloadPathName + outputFileName);
					outputMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
				} else {
					outputMap.put("rtn", Enumeration.ReturnStatus.FAILED.getreturnstatus().trim().toString());
				}
				workbook.write(out);
				out.close();

				LOGGER.error("Writesheet.xlsx written successfully");

			} catch (final Exception e) {
				e.printStackTrace();
			}

		} else {

			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTTHEAVAILBLESPACEEMPTY",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	
	/**
	 * This method is used to retrieve the list of available export columns in the exportconfiguration.
	 * 
	* @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched,nformcode:which hold the form code of the sample storage screen.
	 * Input : {"userinfo":{nmastersitecode": -1},"nformcode":199}}
	 * 
	 * @return response entity  object holding response status and list of available sample storage related data.
	 * 
	 * @throws Exception exception
	 */
	private List<Map<String, Object>> getexportheadercolumn(final UserInfo userInfo) throws Exception {
		final String query = "select sexpheadername,ncolumndatatype, sexpcolumnname, nsortorder, nisvisible, niseditable from exportconfiguration where nformcode="
				+ userInfo.getNformcode() + " and nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsortorder asc ;";
		return jdbcTemplate.queryForList(query);
	}
	
	/**
	 * This method is used to retrieve the list of available export cell values.
	 * 
	* @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched,nrow:which hold the row of the storage structure,ncolumn:which hold the row of the column structure
	 * Input : {"userinfo":{mastersitecode": -1},"ncolumn":10,"nrow":10}
	 * 
	 * @return list of string  holding the response of available cell values.
	 * 
	 * @throws Exception exception
	 */
	private List<String> exportCellsValues(final List<String> strMaterial, final Map<String, Object> inputMap,
			final UserInfo userInfo) {

		final String[] alphabet = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
				"R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

		String key;
		String value = null;
		final Map<String, String> valueMap = new HashMap<>();
		final List<String> keysList = new ArrayList<>();

		// ATE234 Janakumar ALPD-6059 : SUP-1148 > ICGEB > Not able to view all samples
		// stored in Plate 9 (Location- DNA Plates > FZ01 > Shelf-1 > Plate-9)-L4, check
		// description
		final int ndirectionmastercode=(int) inputMap.get("ndirectionmastercode");
		final int nrow= (int) inputMap.get("nrow");
		final int ncolumn= (int) inputMap.get("ncolumn");

		final int rows = ndirectionmastercode == Enumeration.DireactionMaster.LEFTTORIGHT
				.getDireactionMaster() ? nrow : ncolumn;
		final int columns = ndirectionmastercode == Enumeration.DireactionMaster.LEFTTORIGHT
				.getDireactionMaster() ? ncolumn : nrow;

		
			for (int i = 0; i < rows; i++) {
				for (int j = 1; j <= columns; j++) {
					key = "spositionvalue";
					value = alphabet[i] + j;
					int count = 0;
					for (String cellExist : strMaterial) {

						if (cellExist.equals(value)) {
							count++;
							break;
						} else {

						}
					}
					if (count == 0) {
						valueMap.put(value, key);
						keysList.add(value);
					}

				}
			}
	

		keysList.sort(new Comparator<String>() {
			@Override
			public int compare(final String o1, final String o2) {
				// Extract the letter and number parts
				final String letterPart1 = o1.replaceAll("\\d", "");
				final String numberPart1 = o1.replaceAll("\\D", "");
				final String letterPart2 = o2.replaceAll("\\d", "");
				final String numberPart2 = o2.replaceAll("\\D", "");

				// First, compare by the letter part
				final int letterComparison = letterPart1.compareTo(letterPart2);
				if (letterComparison != 0) {
					return letterComparison;
				}

				// If the letter parts are the same, compare by the number part
				return Integer.compare(Integer.parseInt(numberPart1), Integer.parseInt(numberPart2));
			}
		});

		LOGGER.error("Error:Export Cell Value " + keysList);
		return keysList;
	}

	/**
	 * This method is used to find the Excel file download path. 
	 * @return  string  holding the response of Excel file download path.
	 * 
	 * @throws Exception exception
	 */
	public String getExportDownloadPathName() throws Exception {
		final String downloadPath = "select * from settings where nsettingcode="
				+ Enumeration.Settings.EXPORTVIEW_URL.getNsettingcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final Settings surl = (Settings) jdbcUtilityFunction.queryForObject(downloadPath, Settings.class, jdbcTemplate);
		return surl.getSsettingvalue();
	}

	/**
	 * This method is make the new entry of samplestoragetransaction.
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched ,nprojecttypecode: Indicates the primary key or value field used for identifying project type,
	 * filecount: Define the no. of file count,
	 * ImportFile:Define the sample storage related data.
	 * 			
	 * Input : {"userinfo":{nmastersitecode": -1},"nprojecttypecode":10,"filecount":10
	 * 			
	 * @return response entity  object holding response status and list of available sample storage related data.
	 * 
	 * @throws Exception exception
	 */
	// janakumar ALPD-4662 Sample Storage -> Export & Import
	@Override
	public ResponseEntity<Object> getImportData(final MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper mapper = new ObjectMapper();

		final Map<String, String> valueFirstRow = new HashMap<>();

		final StringJoiner positionValue = new StringJoiner(",");

		final MultipartFile multipartFile = request.getFile("ImportFile");

		final String nprojecttypecode = request.getParameter("nprojecttypecode");
		final StringJoiner duplicateValues = new StringJoiner(",");

		final String barcodequery = "SELECT  bc.nbarcodelength,jsondata ->> 'sfieldname' as sfieldname  FROM  projecttype pt , bulkbarcodeconfigversion bcv, "
				+ "bulkbarcodeconfig bc,bulkbarcodeconfigdetails bcd  "
				+ "where bcd.nbulkbarcodeconfigcode = bc.nbulkbarcodeconfigcode and bcd.nprojecttypecode = bc.nprojecttypecode "
				+ "and  bc.nprojecttypecode = " + nprojecttypecode + " " + "AND bcv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ "AND  bc.nbulkbarcodeconfigcode = bcv.nbulkbarcodeconfigcode "
				+ "AND  pt.nprojecttypecode = bc.nprojecttypecode " + "AND pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND bc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND bcv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND bcd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND bc.nsitecode = "
				+ userInfo.getNmastersitecode() + " order by nsorter; ";

		final List<Map<String, Object>> projectBarcodeConfig = jdbcTemplate.queryForList(barcodequery);

		if (!projectBarcodeConfig.isEmpty()) {

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
			final List<Map<String, String>> samplePositionanid = new ArrayList<>();
			final List<String> lstHeader = new ArrayList<>();

			final List<String> sampleID = new ArrayList<>();

			final List<Map<String, Object>> headercolumn = getexportheadercolumn(userInfo);

			final Map<String, Object> headerValueDatatype = new HashMap<>();

			final String daugtherAliquotPlan = "select ssettingvalue from settings where nsettingcode="
					+ Enumeration.Settings.DAUGHTERALIQUOTPLAN.getNsettingcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(daugtherAliquotPlan,
					Settings.class, jdbcTemplate);
			int actualLengthOfdaugtherAliquot = 0;
			int approvedFormatLength = 0;
			if (objSettings != null) {

				final int daugtherAliquotPlanLength = Integer.parseInt(objSettings.getSsettingvalue());
				approvedFormatLength = (int) projectBarcodeConfig.get(0).get("nbarcodelength");
				actualLengthOfdaugtherAliquot = daugtherAliquotPlanLength + approvedFormatLength;
			}

			for (int i = 0; i < headercolumn.size(); i++) {
				final String headerValue = (String) headercolumn.get(i).get("sexpheadername");
				final int headerDataType = (int) headercolumn.get(i).get("ncolumndatatype");

				headerValueDatatype.put(
						commonFunction.getMultilingualMessage(headerValue, userInfo.getSlanguagefilename()),
						headerDataType);
			}

			int rowIndex = 0;

			for (final Row row : sheet) {
				final Map<String, String> rowSheetValues = new HashMap<>();

				if (rowIndex > 0) {
					boolean crtDataType = false;
					String sampleIDAlert = null;
					for (int i = 0; i < lstHeader.size(); i++) {

						final Cell cell = row.getCell(i);
						final int dataType = (int) headerValueDatatype.get(lstHeader.get(i));
						final CellType expectedType = getCellTypeValue(dataType);
						// CellType. values()[dataType]; // Convert int to CellType enum
						String value = "";
						// int j=cell.getCellType()

						if (cell != null) {
							if (cell.getCellType() == CellType.NUMERIC && cell.getCellType() == expectedType) {
								if (DateUtil.isCellDateFormatted(cell)) {
									value = cell.getDateCellValue().toString(); // Date formatted cell
									crtDataType = true;
								} else {
									value = String.valueOf(cell.getNumericCellValue()); // Numeric value as String
									crtDataType = true;
								}
							} else if (cell.getCellType() == CellType.STRING && cell.getCellType() == expectedType) {
								value = cell.getStringCellValue();
								crtDataType = true;
							} else if (cell.getCellType() == CellType.BOOLEAN && cell.getCellType() == expectedType) {
								value = String.valueOf(cell.getBooleanCellValue());
								crtDataType = true;
							} else if (cell.getCellType() == CellType.FORMULA && cell.getCellType() == expectedType) {
								value = cell.getCellFormula();
								crtDataType = true;
							} else if (cell.getCellType() == CellType.BLANK && cell.getCellType() == expectedType) {
								value = cell.getCellFormula();
								crtDataType = true;
							} else if (cell.getCellType() == CellType.ERROR && cell.getCellType() == expectedType) {
								value = cell.getCellFormula();
								crtDataType = true;
							} else {
								if (cell.getCellType() == CellType.BLANK) {
									value = cell.getStringCellValue();
									crtDataType = true;
								} else {
									value = ""; // Handle BLANK and other types
									crtDataType = false;
								}
							}
						} else {
							value = ""; // Cell is null
						}

						if (lstHeader.get(i).equals("Sample Position")) {

							positionValue.add("'" + value + "'");
							sampleIDAlert = value;
						}

						if (crtDataType) {

							rowSheetValues.put(lstHeader.get(i), value);

							if (lstHeader.get(i).equals("Sample Id")) {
								final String barcodeId = String.valueOf(value.trim());
								if (approvedFormatLength == barcodeId.length()) {
									sampleID.add(barcodeId);
								} else if (actualLengthOfdaugtherAliquot == barcodeId.length()) {
									sampleID.add(barcodeId);
								} else if (barcodeId.length() == 0) {
								} else {
									return new ResponseEntity<>(
											commonFunction.getMultilingualMessage("IDS_INVAILDBARCODEID",
													userInfo.getSlanguagefilename()) + " " + sampleIDAlert,
											HttpStatus.EXPECTATION_FAILED);
								}
							}

						} else {
							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_INVALIDDATAFOUNDROW",
											userInfo.getSlanguagefilename()) + " " + sampleIDAlert,
									HttpStatus.EXPECTATION_FAILED);
						}

					}

					samplePositionanid.add(rowSheetValues);

				} else {
					for (final Cell cell : row) {
						final String header = cell.getStringCellValue();
						lstHeader.add(header);
					}
				}
				rowIndex++;
			}

			final Row firstDataRow = sheet.getRow(1); // Get the second row (index 1)

			final int headerSize = lstHeader.size();
			for (int i = 0; i < headerSize; i++) {

				final Cell cell = firstDataRow.getCell(i);
				String value = "";

				if (cell != null) {
					if (cell.getCellType() == CellType.NUMERIC) {
						if (DateUtil.isCellDateFormatted(cell)) {
							value = cell.getDateCellValue().toString(); // Date formatted cell
						} else {
							value = cell != null ? String.valueOf(cell.getNumericCellValue()) : "0";// Numeric value as
							// String
							if (value.isEmpty()) {
								value = "0";
							}
						}
					} else if (cell.getCellType() == CellType.STRING) {
						value = cell != null ? cell.getStringCellValue() : "-";
						if (value.isEmpty()) {
							value = "-";
						}
					} else if (cell.getCellType() == CellType.BOOLEAN) {
						value = String.valueOf(cell.getBooleanCellValue());
					} else if (cell.getCellType() == CellType.FORMULA) {
						value = cell.getCellFormula();
					} else {
						value = ""; // Handle BLANK and other types
					}
				} else {
					value = ""; // Cell is null
				}

				valueFirstRow.put(lstHeader.get(i), value);
			}

			if (!nprojecttypecode.equals(valueFirstRow.get("nprojecttypecode"))) {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECEDPROJECTANDIMPORTPROJECTMISMATCH",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			} else {

				final Set<String> duplicates = new HashSet<>();

				final Set<String> seen = new HashSet<>();

				for (final String id : sampleID) {
					if (!id.isEmpty()) {
						if (!seen.add(id)) {
							duplicates.add(id);
						} else {
							duplicateValues.add("'" + id + "'");
						}
					}
				}
				LOGGER.error("Sucess: Print the Duplicate List " + duplicates);
				if (duplicates.isEmpty()) {

					if (duplicateValues.length() <= 0) {

						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NORECORDINTHEEXCELSHEET",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

					} else {
						final String duplicateCheck = "select spositionvalue from samplestoragetransaction "
								+ " where spositionvalue in (" + duplicateValues + ") and nprojecttypecode = "
								+ nprojecttypecode + "" + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and nsitecode="
								+ userInfo.getNtranssitecode() + "";

						final List<SampleStorageTransaction> duplicateValidation = jdbcTemplate.query(duplicateCheck,
								new SampleStorageTransaction());

						if (!duplicateValidation.isEmpty()) {

							final String sPositionValue = duplicateValidation.stream()
									.map(x -> String.valueOf(x.getSpositionvalue())).distinct()
									.collect(Collectors.joining(","));

							return new ResponseEntity<>(
									commonFunction.getMultilingualMessage("IDS_DUPLICATERECORD",
											userInfo.getSlanguagefilename()) + "" + sPositionValue,
									HttpStatus.EXPECTATION_FAILED);

						} else {

							final List<Map<String, Object>> masterScreen = masterScreenValidation(valueFirstRow,
									userInfo);

							final Map<String, Object> postionExist = postionValidation(positionValue, valueFirstRow,
									samplePositionanid, userInfo);

							if (masterScreen.get(0).get("issamplestoragelocation").equals(true)
									&& masterScreen.get(0).get("isprojecttype").equals(true)) {
								String sampleid = null;
								if (postionExist.get("rtn").equals("removedDuplicate")) {

									final int insertSize = samplePositionanid.size();
									final List<String> queries = new ArrayList<>();

									final StringBuilder queryBuilder = new StringBuilder();
									queryBuilder.append(
											"INSERT INTO samplestoragetransaction (nsamplestoragetransactioncode, nsamplestoragelocationcode, nsamplestoragemappingcode, nprojecttypecode, sposition, spositionvalue, jsondata, npositionfilled, dmodifieddate, nsitecode, nstatus) VALUES ");

									final String sectSeq = "select nsequenceno from seqnobasemaster where stablename='samplestoragetransaction' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
									final Integer objseq = jdbcTemplate.queryForObject(sectSeq, Integer.class);
									int nsamplestoragetransactioncode = objseq;

									for (int i = 0; i < insertSize; i++) {
										final Map<String, Object> inputMap = new HashMap<>();

										final String barcode = samplePositionanid.get(i).get("Sample Id").trim();
										final Object nprojectcodeObj = valueFirstRow.get("nprojecttypecode");
										final String projectCodeString = nprojectcodeObj != null
												? nprojectcodeObj.toString()
														: null;

										final int projectCodeInt = (projectCodeString != null)
												? Integer.parseInt(projectCodeString)
														: 0;
										final JSONObject jsondata = new JSONObject();
										inputMap.put("spositionvalue", barcode);
										inputMap.put("nprojecttypecode", projectCodeInt);
										inputMap.put("jsondata", jsondata);
										inputMap.put("nsamplestoragemappingcode",
												valueFirstRow.get("nsamplestoragemappingcode"));

										JSONObject barcodeDetailsQty = new JSONObject();

										final Map<String, Object> barcodeDetails = readBarcode(inputMap, userInfo);

										final Map<String, Object> jsonData = mapper
												.readValue(barcodeDetails.get("jsonValue").toString(), Map.class);

										final boolean isMatchBarcode = projectBarcodeConfig.stream()
												.allMatch(config -> {
													final String sfieldname = (String) config.get("sfieldname");
													return jsonData.containsKey(sfieldname);
												});
										sampleid = samplePositionanid.get(i).get("Sample Position");
										if (isMatchBarcode) {
											// ATE234 Janakumar ALPD-5138 Sample storage-->While doing export and import
											// sample id and position not showing in container box
											// ATE234 -> ALPD-5072 Sample Storage-During Import Qty and unit should be
											// based aliquot plan
											// ALPD-5520 Sample storage ->Import excels based on setting sheet data or
											// aliquot plan quantity & unit.
											final String getQtySheet = " select ssettingvalue from settings  where nsettingcode =79 "
													+ " and nstatus = "
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
											final Integer valueQtySheet = jdbcTemplate.queryForObject(getQtySheet,
													Integer.class);
											JSONObject objJsonDataList = null;
											if (valueQtySheet == Enumeration.TransactionStatus.NO
													.gettransactionstatus()) {

												barcodeDetailsQty = fetchQuantity(barcodeDetails, inputMap, userInfo);
												barcodeDetailsQty.put("IDS_COMMENTS",
														samplePositionanid.get(i).get("Comments"));
												barcodeDetailsQty.put("IDS_SAMPLEID",
														samplePositionanid.get(i).get("Sample Id").trim());
												barcodeDetailsQty.put("IDS_POSITION",
														samplePositionanid.get(i).get("Sample Position"));

											} else {

												jsonData.put("IDS_UNITNAME", samplePositionanid.get(i).get("Unit"));
												jsonData.put("IDS_QUANTITY", samplePositionanid.get(i).get("Quantity"));
												jsonData.put("IDS_COMMENTS", samplePositionanid.get(i).get("Comments"));
												jsonData.put("IDS_SAMPLEID",
														samplePositionanid.get(i).get("Sample Id").trim());
												jsonData.put("IDS_POSITION",
														samplePositionanid.get(i).get("Sample Position"));
												final JSONObject objJsonDataListQty = new JSONObject(jsonData);
												objJsonDataList = objJsonDataListQty;
											}
											String sbarcodevalue=samplePositionanid.get(i).get("Sample Id").trim();
											sbarcodevalue=sbarcodevalue.replace(" ", "");
											nsamplestoragetransactioncode++;

											// Build the SQL insert statement for each record
											queryBuilder.append("(").append(nsamplestoragetransactioncode).append(", ")
											.append(valueFirstRow.get("nsamplestoragelocationcode"))
											.append(", ").append(valueFirstRow.get("nsamplestoragemappingcode"))
											.append(", ").append(valueFirstRow.get("nprojecttypecode"))
											.append(", '")
											.append(samplePositionanid.get(i).get("Sample Position"))
											.append("', '")
											.append(sbarcodevalue)
											.append("', '")
											.append(valueQtySheet == Enumeration.TransactionStatus.NO
											.gettransactionstatus() ? barcodeDetailsQty
													: objJsonDataList)
											.append("', ")
											.append(Enumeration.TransactionStatus.YES.gettransactionstatus())
											.append(", '")
											.append(dateUtilityFunction.getCurrentDateTime(userInfo))
											.append("', ").append(userInfo.getNtranssitecode()).append(", ")
											.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
											.append("),");

											// Execute in batches
											if ((i + 1) % 1000 == 0 || i + 1 == insertSize) { // Adjust batch size as
												// needed
												queryBuilder.setLength(queryBuilder.length() - 1); // Remove the last
												// comma
												queries.add(queryBuilder.toString());

												// Reset the StringBuilder for the next batch
												queryBuilder.setLength(0);
												queryBuilder.append(
														"INSERT INTO samplestoragetransaction (nsamplestoragetransactioncode, nsamplestoragelocationcode, nsamplestoragemappingcode, nprojecttypecode, sposition, spositionvalue, jsondata, npositionfilled, dmodifieddate, nsitecode, nstatus) VALUES ");
											}
										} else {
											return new ResponseEntity<>(commonFunction.getMultilingualMessage(
													"IDS_IMPORTEBARCODEFIELDWASNOTINMASTERSCREEN",
													userInfo.getSlanguagefilename()) + " " + sampleid + " " + jsonData,
													HttpStatus.EXPECTATION_FAILED);
										}
									}

									// Execute the accumulated queries
									for (final String sqlQuery : queries) {
										jdbcTemplate.execute(sqlQuery);
									}
									LOGGER.error("value: Excuteing Query" + queries);

									// Update the sequence number in the database
									final String updateQuery = "UPDATE seqnobasemaster SET nsequenceno = (SELECT MAX(nsamplestoragetransactioncode) "
											+ "FROM samplestoragetransaction) WHERE stablename = 'samplestoragetransaction'";
									jdbcTemplate.execute(updateQuery);

								} else {
									return new ResponseEntity<>(
											commonFunction.getMultilingualMessage("IDS_IMPORTEDSHEETALREADYFILLED",
													userInfo.getSlanguagefilename()),
											HttpStatus.EXPECTATION_FAILED);
								}
							} else {
								if (masterScreen.get(0).get("issamplestoragelocation").equals(true)
										&& masterScreen.get(0).get("isprojecttype").equals(true)
										&& masterScreen.get(0).get("isunit").equals(true)) {

									// Handle the response entity
								}
							}
						}
					}
				} else {
					final String result = String.join(", ", duplicates);
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DUPLICATERECORD",
							userInfo.getSlanguagefilename()) + " " + result, HttpStatus.EXPECTATION_FAILED);
				}
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PROJECTCONFIGINFORMATTINGSCREEN",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		return getImportSheetData(valueFirstRow, positionValue, userInfo);

	}

	/**
	 * This method is used to return the Type of the cell.
	 * 
	 *@param inputMap  dataType which holds the Type of the cell .
	 * 			
	 * Input : {"dataType":1}
	 * 			
	 * @return Cell Type integer hold the type of the cell.
	 * 
	 * @throws Exception exception
	 */
	private final CellType getCellTypeValue(int dataType) {

		CellType cellTypeValue = null;

		// Check if the dataType is NUMERIC (assuming -1 represents NUMERIC)
		if (dataType == Enumeration.CellType._NONE.getNcellTypet() ) {
			cellTypeValue = CellType._NONE;
		}

		// Check if the dataType is NUMERIC (assuming 0 represents NUMERIC)
		if (dataType == Enumeration.CellType.NUMERIC.getNcellTypet()  ) {
			cellTypeValue = CellType.NUMERIC;
		}

		// Check if the dataType is STRING (assuming 1 represents STRING)
		else if (dataType == Enumeration.CellType.STRING.getNcellTypet() ) {
			cellTypeValue = CellType.STRING;
		}

		// Check if the dataType is BOOLEAN (assuming 2 represents BOOLEAN)
		else if (dataType == Enumeration.CellType.BOOLEAN.getNcellTypet() ) {
			cellTypeValue = CellType.BOOLEAN;
		}

		// Check if the dataType is FORMULA (assuming 3 represents FORMULA)
		else if (dataType == Enumeration.CellType.FORMULA.getNcellTypet() ) {
			cellTypeValue = CellType.FORMULA;
		}

		// Check if the dataType is BLANK (assuming 4 represents BLANK)
		else if (dataType == Enumeration.CellType.BLANK.getNcellTypet() ) {
			cellTypeValue = CellType.BLANK;
		}

		// Check if the dataType is ERROR (assuming 5 represents ERROR)
		else if (dataType == Enumeration.CellType.ERROR.getNcellTypet() ) {
			cellTypeValue = CellType.ERROR;
		}

		// If no matching data type, return null
		return cellTypeValue;
	}

	/**
	 * This method is used to return the sheet of the data.
	 * 
	 *@param valueFirstRow[Map] object with nsamplestoragelocationcode which hold the value of storage structure code,nsamplestoragemappingcode:Identifying the value of the sample storage mapping code,
	 *sposition:Defines the cell barcode id based on the projecttype.
	 * 			
	 * Input : {"nsamplestoragelocationcode":1,"nsamplestoragemappingcode":1,"sposition":"P1112345611202"}
	 * 			
	 * @return Cell Type integer hold the  sheet of the data.
	 * 
	 * @throws Exception exception
	 */
	private ResponseEntity<Object> getImportSheetData(final Map<String, String> valueFirstRow,
			final StringJoiner positionValue, final UserInfo userInfo) {

		final Map<String, Object> outputMap = new HashMap<>();
		Map<String, Object> map = new HashMap<>();
		String strQuery = "select *, jsondata  as \"additionalInfo\" from samplestoragetransaction where nsamplestoragelocationcode="
				+ valueFirstRow.get("nsamplestoragelocationcode") + " and nsamplestoragemappingcode="
				+ valueFirstRow.get("nsamplestoragemappingcode") + " and sposition in (" + positionValue
				+ ") and npositionfilled=" + Enumeration.TransactionStatus.YES.gettransactionstatus() + ";";

		try {
			map = jdbcTemplate.queryForMap(strQuery);
		} catch (final Exception e) {
			map = new HashMap<>();
		}
		outputMap.put("cellData", map);

		strQuery = "(select ssm.nnoofcontainer-count(st.sposition) as navailablespace from samplestoragetransaction st ,samplestoragemapping ssm "
				+ "		 where   ssm.nsamplestoragemappingcode=st.nsamplestoragemappingcode "
				+ " and st.nsamplestoragemappingcode= " + valueFirstRow.get("nsamplestoragemappingcode")
				+ "		and st.spositionvalue!='' " + " group by ssm.nnoofcontainer " + "		)";

		try {
			map = jdbcTemplate.queryForMap(strQuery);
		} catch (final Exception e) {
			map = new HashMap<>();
		}
		outputMap.put("navailablespace", map);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to checking the position validation.
	 * 
	 *@param inputMap [Map] map object with nprojecttypecode which holds the value of project type code,
	 *nsamplestoragemappingcode:which holds the value of sample process mapping code
	 * 			
	 * Input : {"nprojecttypecode":1,"nsamplestoragemappingcode":1}
	 * 			
	 * @return Map of object return the sample position validation.
	 * 
	 * @throws Exception exception
	 */
	
	private Map<String, Object> postionValidation(final StringJoiner positionValue,
			final Map<String, String> valueFirstRow, final List<Map<String, String>> samplePositionanid,
			final UserInfo userInfo) {

		final Map<String, Object> outputMap = new LinkedHashMap<>();

		final String strQuery = "select sposition from samplestoragetransaction where  nprojecttypecode="
				+ valueFirstRow.get("nprojecttypecode") + "  and nsamplestoragemappingcode="
				+ valueFirstRow.get("nsamplestoragemappingcode") + " and sposition in(" + positionValue
				+ ")  and npositionfilled= " + Enumeration.TransactionStatus.YES.gettransactionstatus();

		final List<String> duplicatePosition = jdbcTemplate.queryForList(strQuery, String.class);

		final Set<String> seenValues = new HashSet<>();

		final List<Map<String, String>> filteredList = samplePositionanid.stream().filter(entry -> {
			final String value7 = entry.get("Sample Position");
			final String value8 = entry.get("Sample Id");

			// Skip this entry if Value7 is in duplicatePosition and is non-empty
			if (value7 != null && !value7.isEmpty() && duplicatePosition.contains(value7)) {
				return false;
			}

			// Skip this entry if Value8 is empty or null
			if (value8 == null || value8.isEmpty()) {
				return false;
			}

			// Add Value8 to the seen set, return false if it was already seen
			return seenValues.add(value8);
		}).collect(Collectors.toList());

		// Replace the original list with the filtered list
		samplePositionanid.clear();
		samplePositionanid.addAll(filteredList);

		LOGGER.error("Error: Postion Validation " + samplePositionanid);

		outputMap.put("rtn", !filteredList.isEmpty() ? "removedDuplicate" : "noDuplicate");

		return outputMap;
	}

	/**
	 * This method is used to check whether the master record exists or not. .
	 * 
	 *@param inputMap  [Map] map object with nprojecttypecode which holds the value of the project type ,
	 *nsamplestoragelocationcode:Identifying the storage structure code
	 * 			
	 * Input : {"nsamplestoragelocationcode":1,"nprojecttypecode":1}
	 * 			
	 * @return list holding the master data record.
	 * 
	 * @throws Exception exception
	 */
	private List<Map<String, Object>> masterScreenValidation(final Map<String, String> valueFirstRow,
			final UserInfo userInfo) {

		final String screenValidate = "select CASE WHEN EXISTS(select ssamplestoragelocationname from samplestoragelocation    "
				+ "where nsamplestoragelocationcode =" + valueFirstRow.get("nsamplestoragelocationcode")
				+ "  and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  )   "
				+ "THEN TRUE ELSE FALSE  End AS issamplestoragelocation,   "
				+ "CASE when exists (select sprojecttypename from projecttype where nprojecttypecode ="
				+ valueFirstRow.get("nprojecttypecode") + " and nsitecode=" + userInfo.getNmastersitecode()
				+ "   and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  )   "
				+ "THEN TRUE ELSE FALSE  End AS isprojecttype ;";
		return jdbcTemplate.queryForList(screenValidate);
	}

}
