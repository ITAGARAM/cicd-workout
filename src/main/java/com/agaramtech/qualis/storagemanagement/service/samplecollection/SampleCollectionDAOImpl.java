package com.agaramtech.qualis.storagemanagement.service.samplecollection;

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
import com.agaramtech.qualis.basemaster.model.BulkBarcodeConfigDetails;
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
import com.agaramtech.qualis.storagemanagement.service.samplestoragetransaction.SampleStorageTransactionDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "storagesamplecollection" table by 
 * implementing methods from its interface. 
 */
@RequiredArgsConstructor
@Repository
public class SampleCollectionDAOImpl implements SampleCollectionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleCollectionDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final SampleStorageTransactionDAO sampleStorageTransactionDAO;
	
	/**
	 * This method is used to retrieve list of all available sampleCollections for the specified site.
	 * @param fromDate [String] holding from-date which list the sample collection from the from-date.
	 * @param toDate [String] holding to-date which list the sample collection till the to-date.
	 * @param currentUIDate [String] holding the current UI date.
	 * @param nprojecttypecode [int] holding the primary key of the project type of the sample collections.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active sampleCollections
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleCollection(String fromDate, String toDate, String currentUIDate,
			final UserInfo userInfo, int nprojecttypecode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo, currentUIDate, "datetime",
					"FromDate");
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
			toDate = dateUtilityFunction.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}
		final String strQuery = "select * from projecttype where nprojecttypecode>0 and nsitecode="+userInfo.getNmastersitecode()+" and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<ProjectType> list = jdbcTemplate.query(strQuery, new ProjectType());
		outputMap.put("projectType", list);
		final Map<String, Object> defaulrProjectType = new LinkedHashMap<>();
		if (!list.isEmpty()) {
			if (nprojecttypecode == -1) {
				defaulrProjectType.put("label", list.get(list.size() - 1).getSprojecttypename());
				defaulrProjectType.put("value", list.get(list.size() - 1).getNprojecttypecode());
				defaulrProjectType.put("item", list.get(list.size() - 1));
				nprojecttypecode = list.get(list.size() - 1).getNprojecttypecode();
			} else {
				final String strQuery1 = "select * from projecttype where nprojecttypecode>0 and "
						+ " nprojecttypecode=" + nprojecttypecode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final ProjectType lstProjectType = jdbcTemplate.queryForObject(strQuery1,
						new ProjectType());
				defaulrProjectType.put("label", lstProjectType.getSprojecttypename());
				defaulrProjectType.put("value", lstProjectType.getNprojecttypecode());
				defaulrProjectType.put("item", lstProjectType);
			}
		}
		final Map<String, Object> mapBarcodeDetails = getBarcodeLengthByProjecType(nprojecttypecode, userInfo);
		final String query = "select sc.scomments,sc.nsamplecollectioncode,u.sunitname,sc.sbarcodeid,sc.jsondata,COALESCE(TO_CHAR(sc.dcollectiondate,'"
				+ "" + userInfo.getSpgsitedatetime()
				+ "'),'') as scollectiondate,sc.nsampleqty,sc.scomments,sc.nprojecttypecode from storagesamplecollection sc,unit u where u.nunitcode=sc.nunitcode and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  sc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and sc.nsitecode="
				+ userInfo.getNtranssitecode() + " and " + " sc.dcollectiondate between '" + fromDate + "' and '"
				+ toDate + "' and nprojecttypecode=" + nprojecttypecode + " order by sc.nsamplecollectioncode desc";
		List<StorageSampleCollection> lstSampleCollection = jdbcTemplate.query(query,
				new StorageSampleCollection());
		String barcodequery = getBarcodeFieldsByProjectType(nprojecttypecode, userInfo);
		final List<BulkBarcodeConfigDetails> projectBarcodeConfig = jdbcTemplate.query(barcodequery,
				new BulkBarcodeConfigDetails());
		outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);
		outputMap.put("selectedProjectType", defaulrProjectType);
		outputMap.put("nbarcodelength", (int) mapBarcodeDetails.get("nbarcodelength"));
		outputMap.put("SampleCollection", lstSampleCollection);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all available barcodes for the specified site.
	 * @param inputMap [Map] map object with nprojecttypecode [int] which hold the primary key of the project type of the sample collection and
	 * 				userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for which the list is to be fetched
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response status and list of all active barcodes
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBarcodeConfigData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> barcodeDetails = sampleStorageTransactionDAO.readBarcode(inputMap, userInfo);
		final String barcodequery = getBarcodeFieldsByProjectType((int) inputMap.get("nprojecttypecode"), userInfo);
		List<BulkBarcodeConfigDetails> projectBarcodeConfig = jdbcTemplate.query(barcodequery,
				new BulkBarcodeConfigDetails());
		outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);
		if (barcodeDetails != null && barcodeDetails.containsKey("jsonValue")) {
			final Map<String, Object> jsonData = mapper.readValue(barcodeDetails.get("jsonValue").toString(),
					Map.class);
			final Map<String, Object> jsonDataValue = mapper
					.readValue(barcodeDetails.get("objJsonPrimaryKeyValue").toString(), Map.class);
			jsonData.put("nbulkbarcodeconfigcode", jsonDataValue.get("nbulkbarcodeconfigcode"));
			outputMap.put("jsondataBarcodeData", jsonData);
			outputMap.put("objJsonPrimaryKeyValue", jsonDataValue);
		}
		System.out.println("Output Map: " + outputMap);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to storagesamplecollection table.
	 * Sample Collection Barcode ID for each Project Type is unique across the database. 
	 * Need to check for duplicate entry of sbarcodeid for the specified site and projectType before saving into database.
	 * @param inputMap [Map] map object holding details to be added in storagesamplecollection table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return saved sampleCollection object with status code 200 if saved successfully else if the sampleCollection already exists, 
	 * 			response will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSampleCollection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table storagesamplecollection "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final ObjectMapper mapper = new ObjectMapper();
		final List<Object> savedSampleCollection = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String fromDate = "";
		String toDate = "";
		Map<String, Object> sampleCollectionObj = mapper.convertValue(inputMap.get("storagesamplecollection"),
				Map.class);
		String existsQuery = getBarcodeId(sampleCollectionObj.get("sbarcodeid").toString().trim(), userInfo);
		final StorageSampleCollection existsSampleCollectionObj = (StorageSampleCollection) jdbcUtilityFunction
				.queryForObject(existsQuery, StorageSampleCollection.class, jdbcTemplate);
		if (existsSampleCollectionObj == null) {
			if (inputMap.get("fromDate") != null) {
				fromDate = (String) inputMap.get("fromDate");
			}
			if (inputMap.get("toDate") != null) {
				toDate = (String) inputMap.get("toDate");
			}
			if (sampleCollectionObj.get("dcollectiondate") != null) {
				sampleCollectionObj.put("dcollectiondate",
						sampleCollectionObj.get("dcollectiondate").toString().replace("T", " ").replace("Z", ""));
			}
			final String seqNo = "select nsequenceno from seqnostoragemanagement where stablename='storagesamplecollection' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int nsequenceno = jdbcTemplate.queryForObject(seqNo, Integer.class);
			nsequenceno++;
			final String updatequery = "update seqnostoragemanagement set nsequenceno =" + nsequenceno
					+ " where stablename='storagesamplecollection'";
			jdbcTemplate.execute(updatequery);
			final String insertQuery = "insert into storagesamplecollection(nsamplecollectioncode,nprojecttypecode,nunitcode,sbarcodeid,"
					+ "jsondata,nsampleqty,dcollectiondate,ntzcollectiondatetime,noffsetdcollectiondatetime,"
					+ "scomments,dmodifieddate,nsitecode,nstatus) values(" + nsequenceno + ","
					+ sampleCollectionObj.get("nprojecttypecode") + "," + sampleCollectionObj.get("nunitcode") + ","
					+ "N'" + stringUtilityFunction.replaceQuote(sampleCollectionObj.get("sbarcodeid").toString())
					+ "','" + sampleCollectionObj.get("jsondata") + "'," + sampleCollectionObj.get("nsampleqty") + ",'"
					+ sampleCollectionObj.get("dcollectiondate") + "',"
					+ sampleCollectionObj.get("ntzcollectiondatetime") + "," + "'"
					+ sampleCollectionObj.get("noffsetdcollectiondatetime") + "',N'"
					+ stringUtilityFunction.replaceQuote(sampleCollectionObj.get("scomments").toString()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			jdbcTemplate.execute(insertQuery);
			String auditQuery = sampleCollectionAudit(nsequenceno, userInfo);
			multilingualIDList.add("IDS_ADDSAMPLECOLLECTION");
			final StorageSampleCollection objSampleCollection = (StorageSampleCollection) jdbcUtilityFunction
					.queryForObject(auditQuery, StorageSampleCollection.class, jdbcTemplate);
			savedSampleCollection.add(objSampleCollection);
			auditUtilityFunction.fnInsertAuditAction(savedSampleCollection, 1, null, multilingualIDList, userInfo);
			return getSampleCollection(fromDate, toDate, null, userInfo,
					(int) sampleCollectionObj.get("nprojecttypecode"));
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_BARCODEIDALREADYEXISTS",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to update entry in storagesamplecollection table.Need to
	 * validate that the storagesamplecollection object to be updated is active
	 * before updating details in database.
	 * Need to check for duplicate entry of sbarcodeid for the specified site and projectType before saving into database.
	 * @param inputMap [Map] map object holding details to be updated in storagesamplecollection table.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return saved sampleCollection object with status code 200 if saved successfully 
	 * 			else if the sampleCollection already exists, response will be returned as 'Already Exists' with status code 409
	 *          else if the sampleCollection to be updated is not available, response will be returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSampleCollection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> beforeSampleCollection = new ArrayList<>();
		final List<Object> afterSampleCollection = new ArrayList<>();
		mapper.registerModule(new JavaTimeModule());
		final StorageSampleCollection objSampleCollection = mapper.convertValue(inputMap.get("storagesamplecollection"),
				new TypeReference<StorageSampleCollection>() {
		});
		final String activeQuery = getActiveSampleCollection(objSampleCollection.getNsamplecollectioncode(), userInfo);
		final StorageSampleCollection activeSampleCollection = (StorageSampleCollection) jdbcUtilityFunction
				.queryForObject(activeQuery, StorageSampleCollection.class, jdbcTemplate);
		if (activeSampleCollection != null) {
			String auditQuery = sampleCollectionAudit(objSampleCollection.getNsamplecollectioncode(), userInfo);
			multilingualIDList.add("IDS_EDITSAMPLECOLLECTION");
			final StorageSampleCollection objSampleCollectionAuditBefore = (StorageSampleCollection) jdbcUtilityFunction
					.queryForObject(auditQuery, StorageSampleCollection.class, jdbcTemplate);
			if (inputMap.get("fromDate") != null) {
				fromDate = (String) inputMap.get("fromDate");
			}
			if (inputMap.get("toDate") != null) {
				toDate = (String) inputMap.get("toDate");
			}
			if (objSampleCollection.getDcollectiondate() != null) {
				objSampleCollection
				.setScollectiondate(dateUtilityFunction.instantDateToString(objSampleCollection.getDcollectiondate())
						.replace("T", " ").replace("Z", ""));
			}
			final String updateQuery = "update storagesamplecollection set dcollectiondate='"
					+ objSampleCollection.getScollectiondate() + "',ntzcollectiondatetime="
					+ objSampleCollection.getNtzcollectiondatetime() + "," + "noffsetdcollectiondatetime="
					+ objSampleCollection.getNoffsetdcollectiondatetime() + ",scomments=N'"
					+ stringUtilityFunction.replaceQuote(objSampleCollection.getScomments()) + "',dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nsitecode=" + userInfo.getNtranssitecode()
					+ " " + "where nsamplecollectioncode=" + objSampleCollection.getNsamplecollectioncode() + "";
			jdbcTemplate.execute(updateQuery);
			auditQuery = sampleCollectionAudit(objSampleCollection.getNsamplecollectioncode(), userInfo);
			final StorageSampleCollection objSampleCollectionAuditAfter = (StorageSampleCollection) jdbcUtilityFunction
					.queryForObject(auditQuery, StorageSampleCollection.class, jdbcTemplate);
			beforeSampleCollection.add(objSampleCollectionAuditBefore);
			afterSampleCollection.add(objSampleCollectionAuditAfter);
			auditUtilityFunction.fnInsertAuditAction(afterSampleCollection, 2, beforeSampleCollection,
					multilingualIDList, userInfo);
			return getSampleCollection(fromDate, toDate, null, userInfo, objSampleCollection.getNprojecttypecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to retrieve active sampleCollection object based on the specified nsamplecollectioncode.
	 * @param inputMap [Map] map object holding primary key of sample collection and the projecttype primary key.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of sampleCollection object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getActiveSampleCollectionById(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int nsamplecollectioncode = (int) inputMap.get("nsamplecollectioncode");
		final String activeQuery = getActiveSampleCollection(nsamplecollectioncode, userInfo);
		final StorageSampleCollection activeSampleCollection = (StorageSampleCollection) jdbcUtilityFunction
				.queryForObject(activeQuery, StorageSampleCollection.class, jdbcTemplate);
		if (activeSampleCollection != null) {
			Map<String, Object> outputMap = new HashMap<>();
			String query = "select sc.scomments,sc.nsamplecollectioncode,u.sunitname,sc.sbarcodeid,sc.jsondata,COALESCE(TO_CHAR(sc.dcollectiondate,'"
					+ "" + userInfo.getSpgsitedatetime()
					+ "'),'') as scollectiondate,sc.nsampleqty,sc.scomments,sc.nprojecttypecode from storagesamplecollection sc,unit u where u.nunitcode=sc.nunitcode and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  sc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and sc.nsitecode="
					+ userInfo.getNtranssitecode() + " and " + " nsamplecollectioncode=" + nsamplecollectioncode
					+ " and sc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			final StorageSampleCollection lstSampleCollection = jdbcTemplate.queryForObject(query,
					new StorageSampleCollection());
			final String barcodequery = getBarcodeFieldsByProjectType((int) inputMap.get("nprojecttypecode"), userInfo);
			List<BulkBarcodeConfigDetails> projectBarcodeConfig = jdbcTemplate.query(barcodequery,
					new BulkBarcodeConfigDetails());
			outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);
			outputMap.put("activeSampleColletionByID", lstSampleCollection);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to delete an entry in storagesamplecollection table
	 * Need to check the record is already deleted or not
	 * @param inputMap [Map] map object holding detail to be deleted from storagesamplecollection table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available sampleCollection object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteSampleCollection(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final List<Object> deleteSampleCollection = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String activeQuery = getActiveSampleCollection((int) inputMap.get("nsamplecollectioncode"), userInfo);
		final StorageSampleCollection activeSampleCollection = (StorageSampleCollection) jdbcUtilityFunction
				.queryForObject(activeQuery, StorageSampleCollection.class, jdbcTemplate);
		if (activeSampleCollection != null) {
			final String recordInCollection = "select * from storagesamplereceiving where " + " sbarcodeid='"
					+ activeSampleCollection.getSbarcodeid() + "'  " + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode="
					+ userInfo.getNtranssitecode() + "";
			List<Map<String, Object>> objSampleReceiving = jdbcTemplate.queryForList(recordInCollection);
			if (objSampleReceiving.isEmpty()) {
				final String auditQuery = sampleCollectionAudit((int) inputMap.get("nsamplecollectioncode"), userInfo);
				final StorageSampleCollection objSampleCollectionAuditAfter = (StorageSampleCollection) jdbcUtilityFunction
						.queryForObject(auditQuery, StorageSampleCollection.class, jdbcTemplate);
				String deleteQuery = "update storagesamplecollection set nstatus="
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " where nsamplecollectioncode="
						+ (int) inputMap.get("nsamplecollectioncode") + "";
				jdbcTemplate.execute(deleteQuery);
				deleteSampleCollection.add(objSampleCollectionAuditAfter);
				multilingualIDList.add("IDS_DELETESAMPLECOLLECTION");
				auditUtilityFunction.fnInsertAuditAction(deleteSampleCollection, 1, null, multilingualIDList, userInfo);
				return getSampleCollection(fromDate, toDate, null, userInfo, (int) inputMap.get("nprojecttypecode"));
			} else {
				String alert;
				alert = commonFunction.getMultilingualMessage("IDS_THISISUSEDIN", userInfo.getSlanguagefilename()) + " "
						+ commonFunction.getMultilingualMessage("IDS_STORAGESAMPLERECEIVING",
								userInfo.getSlanguagefilename());
				return new ResponseEntity<>(
						alert + " " + commonFunction.getMultilingualMessage("", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
	}

	/**
	 * This method is used to fetch the sampleCollection for the specified barcodeid and site.
	 * @param sbarcodeid [String] barcode id of the sampleCollection
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return sampleCollection get query [String] created by specified sbarcodeid and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private String getBarcodeId(final String sbarcodeid, final UserInfo userInfo) {
		return "select * from storagesamplecollection where " + " sbarcodeid=N'"
				+ stringUtilityFunction.replaceQuote(sbarcodeid) + "' " + "and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nsitecode="
				+ userInfo.getNtranssitecode() + "";
	}

	/**
	 * This method is used to fetch the sampleCollection for the specified nsamplecollectioncode and site.
	 * @param samplecollectioncode [int] primary key of the sampleCollection
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return sampleCollection get query [String] created by specified nsamplecollectioncode and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private String getActiveSampleCollection(final int samplecollectioncode, final UserInfo userInfo) {
		return "select * from storagesamplecollection where nsamplecollectioncode=" + samplecollectioncode
				+ " and  nsitecode=" + userInfo.getNtranssitecode() + " and " + "nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
	}
	
	/**
	 * This method is used to fetch the sampleCollection for the specified nsamplecollectioncode and site.
	 * @param samplecollectioncode [int] primary key of the sampleCollection
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return sampleCollection get query [String] created by specified nsamplecollectioncode and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private String sampleCollectionAudit(final int nsamplecollectioncode, final UserInfo userinfo) {
		return "select sc.scomments,sc.nsamplecollectioncode,u.sunitname,sc.sbarcodeid,sc.jsondata,COALESCE(TO_CHAR(sc.dcollectiondate,'"
				+ "" + userinfo.getSpgsitedatetime()
				+ "'),'') as scollectiondate,sc.nsampleqty,sc.scomments,sc.nprojecttypecode,p.sprojecttypename from storagesamplecollection sc,unit u,projecttype p where u.nunitcode=sc.nunitcode and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  p.nprojecttypecode=sc.nprojecttypecode and " + "  sc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and sc.nsitecode="
				+ userinfo.getNtranssitecode() + " and " + "sc.nsamplecollectioncode=" + nsamplecollectioncode
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
	}

	/**
	 * This method is used to fetch list of all barcode fields and its length map for the specified nprojecttypecode and site.
	 * @param nprojecttypecode [int] primary key of the project type
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return map object based on the specified nprojecttypecode and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	public Map<String, Object> getBarcodeLengthByProjecType(final int nprojecttypecode, final UserInfo userinfo) {
		Map<String, Object> map = new HashMap<String, Object>();
		int nbarcodelength = 0;
		final String stQuery = "SELECT  bc.nbarcodelength AS nbarcodelength,"
				+ "  bcd.nfieldstartposition as samplecyclestartpostion,"
				+ "  bcd.nfieldlength as samplecyclenfieldlength FROM  projecttype pt , bulkbarcodeconfigversion bcv,"
				+ "  bulkbarcodeconfig bc LEFT JOIN "
				+ "  bulkbarcodeconfigdetails bcd ON bcd.nbulkbarcodeconfigcode = bc.nbulkbarcodeconfigcode "
				+ "  AND bcd.nprojecttypecode = bc.nprojecttypecode " + "  AND bcd.stablename = 'samplecycle' "
				+ "  AND bcd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " WHERE  bc.nprojecttypecode = " + nprojecttypecode + "" + "  AND bcv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ""
				+ "   AND  bc.nbulkbarcodeconfigcode = bcv.nbulkbarcodeconfigcode "
				+ "   AND  pt.nprojecttypecode = bc.nprojecttypecode " + "  AND pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bcv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bc.nsitecode = "
				+ userinfo.getNmastersitecode() + ";";
		List<Map<String, Object>> listBulkBarcodeConfig = jdbcTemplate.queryForList(stQuery);
		if (!listBulkBarcodeConfig.isEmpty()) {
			nbarcodelength = Integer.parseInt(listBulkBarcodeConfig.get(0).get("nbarcodelength").toString());
			map.put("nbarcodelength", nbarcodelength);
		} else {
			map.put("nbarcodelength", 0);
		}
		LOGGER.info("getBarcodeLengthByProjecType:" + stQuery);
		return map;
	}

	/**
	 * This method is used to fetch list of all barcode fields map for the specified nprojecttypecode and site.
	 * @param nprojecttypecode [int] primary key of the project type
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return barcodeConfig get query [String] based on the specified nprojecttypecode and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	public String getBarcodeFieldsByProjectType(final int nprojecttypecode, final UserInfo userinfo) {
		return "SELECT  nsorter,jsondata->>'sfieldname' as sfieldname FROM  projecttype pt , bulkbarcodeconfigversion bcv,"
				+ "  bulkbarcodeconfig bc,bulkbarcodeconfigdetails bcd "
				+ "  where bcd.nbulkbarcodeconfigcode = bc.nbulkbarcodeconfigcode and bcd.nprojecttypecode = bc.nprojecttypecode "
				+ "  and  bc.nprojecttypecode = " + nprojecttypecode + "" + "  AND bcv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ""
				+ "  AND  bc.nbulkbarcodeconfigcode = bcv.nbulkbarcodeconfigcode "
				+ "  AND  pt.nprojecttypecode = bc.nprojecttypecode " + "  AND pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bcv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bcd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bc.nsitecode = "
				+ userinfo.getNmastersitecode() + " order by nsorter;";
	}
}
