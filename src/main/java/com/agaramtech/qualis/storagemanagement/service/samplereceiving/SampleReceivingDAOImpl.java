package com.agaramtech.qualis.storagemanagement.service.samplereceiving;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
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
import com.agaramtech.qualis.storagemanagement.model.SampleReceiving;
import com.agaramtech.qualis.storagemanagement.service.samplestoragetransaction.SampleStorageTransactionDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;



@AllArgsConstructor
@Repository
public class SampleReceivingDAOImpl implements SampleReceivingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleReceivingDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final SampleStorageTransactionDAO sampleStorageTransactionDAO;
	/**
	 * This method is used to retrieve list of all active samplereceiving for the
	 * specified site.
	 *
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 @param nprojecttypecode [int] primary key of samplereceiving object
	 * @return response entity object holding response status and list of all active
	 *         samplereceiving
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getSampleReceiving(String fromDate, String toDate, String currentUIDate,
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
		final String strQuery = "select * from projecttype where nprojecttypecode>0 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 DESC";
		List<ProjectType> list = jdbcTemplate.query(strQuery, new ProjectType());
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
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and nsitecode="+userInfo.getNmastersitecode()+" order by 1 DESC ";
				final ProjectType lstProjectType = jdbcTemplate.queryForObject(strQuery1,
						new ProjectType());

				defaulrProjectType.put("label", lstProjectType.getSprojecttypename());
				defaulrProjectType.put("value", lstProjectType.getNprojecttypecode());
				defaulrProjectType.put("item", lstProjectType);
			}
		}

		final String query = "select u.sunitname,sc.nsampleqty,ssr.scomments,ssr.nsitecode,sc.nprojecttypecode, pt.sprojecttypename, "
				+ " ssr.nstoragesamplereceivingcode, ssr.sbarcodeid,COALESCE(TO_CHAR(ssr.dcollectiondate,'dd/MM/yyyy HH24:mi:ss'),'') as scollectiondate,  "
				+ " ssr.jsondata from storagesamplereceiving ssr,storagesamplecollection sc ,unit u ,projecttype pt  "
				+ " where ssr.sbarcodeid=sc.sbarcodeid   and sc.nprojecttypecode=pt.nprojecttypecode "
				+ " and sc.nunitcode=u.nunitcode   " + " and pt.nsitecode="+userInfo.getNmastersitecode()+" and ssr.nsitecode="
				+ userInfo.getNtranssitecode() + " " + " and ssr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and sc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + "and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and  sc.nprojecttypecode="
				+ nprojecttypecode + " and  " + "ssr.dcollectiondate between '" + fromDate + "' and '" + toDate
				+ "' order by ssr.nstoragesamplereceivingcode DESC";

		List<SampleReceiving> lstSampleReceiving = jdbcTemplate.query(query, new SampleReceiving());

		final String barcodequery = "SELECT  nsorter,jsondata->>'sfieldname' as sfieldname FROM  projecttype pt , bulkbarcodeconfigversion bcv,"
				+ "  bulkbarcodeconfig bc,bulkbarcodeconfigdetails bcd "
				+ "  where bcd.nbulkbarcodeconfigcode = bc.nbulkbarcodeconfigcode and bcd.nprojecttypecode = bc.nprojecttypecode "
				+ "  and  bc.nprojecttypecode = " + nprojecttypecode + "" + "  AND bcv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ""
				+ "   AND  bc.nbulkbarcodeconfigcode = bcv.nbulkbarcodeconfigcode "
				+ "   AND  pt.nprojecttypecode = bc.nprojecttypecode " + "  AND pt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND bc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND bcv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND bcd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND bc.nsitecode = "
				+ userInfo.getNmastersitecode() + " order by nsorter;";

		final int nbarcodelength = getBarcodeLengthByProjecType(nprojecttypecode, userInfo);

		outputMap.put("nbarcodelength", nbarcodelength);
		final List<Map<String, Object>> projectBarcodeConfig = jdbcTemplate.queryForList(barcodequery);
		final Map<String, Object> map = new LinkedHashMap<>();
		final Map<String, Object> map1 = new LinkedHashMap<>();
		map.put("sfieldname", "Unit");
		map.put("nsorter", "15");
		map1.put("sfieldname", "Quantity");
		map1.put("nsorter", "16");
		projectBarcodeConfig.add(map);
		projectBarcodeConfig.add(map1);
		outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);
		outputMap.put("selectedProjectType", defaulrProjectType);
		outputMap.put("SampleReceiving", lstSampleReceiving);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active Barcode data for the
	 * specified project type and site.
	 *
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Barcode data specified project type and site.
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBarcodeConfigData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		final String barcodeCheck = "select *  from storagesamplecollection where  " + " sbarcodeid='"
				+ inputMap.get("spositionvalue") + "' and nsitecode=" + userInfo.getNtranssitecode() + ""
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		List<Map<String, Object>> projectList = jdbcTemplate.queryForList(barcodeCheck);

		if (projectList.isEmpty()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHOOSENBARCODENOTINTHISPROJECT",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			Map<String, Object> barcodeDetails = sampleStorageTransactionDAO.readBarcode(inputMap, userInfo);
			System.out.println("Barcode Details: " + barcodeDetails);

			final String barcodequery = "SELECT  nsorter,jsondata->>'sfieldname' as sfieldname FROM  projecttype pt , bulkbarcodeconfigversion bcv,"
					+ "  bulkbarcodeconfig bc,bulkbarcodeconfigdetails bcd "
					+ "  where bcd.nbulkbarcodeconfigcode = bc.nbulkbarcodeconfigcode and bcd.nprojecttypecode = bc.nprojecttypecode "
					+ "  and  bc.nprojecttypecode = " + (int) inputMap.get("nprojecttypecode") + ""
					+ "  AND bcv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ "" + "   AND  bc.nbulkbarcodeconfigcode = bcv.nbulkbarcodeconfigcode "
					+ "   AND  pt.nprojecttypecode = bc.nprojecttypecode " + "  AND pt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bcv.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bcd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bc.nsitecode = "
					+ userInfo.getNmastersitecode() + " and pt.nsitecode="+userInfo.getNmastersitecode()+" "
					+ " and bcv.nsitecode="+userInfo.getNmastersitecode()+" and bcd.nsitecode="+userInfo.getNmastersitecode()+" order by nsorter;";

			final List<Map<String, Object>> projectBarcodeConfig = jdbcTemplate.queryForList(barcodequery);
			final Map<String, Object> map = new LinkedHashMap<>();
			final Map<String, Object> map1 = new LinkedHashMap<>();
			map.put("sfieldname", "Unit");
			map.put("nsorter", "15");
			map1.put("sfieldname", "Quantity");
			map1.put("nsorter", "16");
			projectBarcodeConfig.add(map);
			projectBarcodeConfig.add(map1);
			outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);

			if (barcodeDetails != null && barcodeDetails.containsKey("jsonValue")) {

				final String addFields = "select u.sunitname,sc.nsampleqty from storagesamplecollection sc,unit u "
						+ " where  u.nunitcode=sc.nunitcode  and sc.nsitecode=" + userInfo.getNtranssitecode()
						+ " and u.nsitecode=" + userInfo.getNmastersitecode() + " " + " and sc.sbarcodeid='"
						+ inputMap.get("spositionvalue") + "' and u.nsitecode="+userInfo.getNmastersitecode()+""
						+ " and sc.nsitecode ="+userInfo.getNtranssitecode()+" and  sc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";
				final List<Map<String, Object>> listDataList = jdbcTemplate.queryForList(addFields);

				final Map<String, Object> jsondataBarcodeData = mapper
						.readValue(barcodeDetails.get("jsonValue").toString(), Map.class);
				final Map<String, Object> jsonDataValue = mapper
						.readValue(barcodeDetails.get("objJsonPrimaryKeyValue").toString(), Map.class);
				jsondataBarcodeData.put("nbulkbarcodeconfigcode", jsonDataValue.get("nbulkbarcodeconfigcode"));
				if (listDataList != null) {
					jsondataBarcodeData.put("Quantity", listDataList.get(0).get("nsampleqty"));
					jsondataBarcodeData.put("Unit", listDataList.get(0).get("sunitname"));
				} else {
					jsondataBarcodeData.put("Quantity", "-");
					jsondataBarcodeData.put("Unit", "-");
				}
				outputMap.put("addValue", listDataList);
				outputMap.put("jsondataBarcodeData", jsondataBarcodeData);
			} else {
				LOGGER.error("Expected json data not found in barcodeDetails" + outputMap);
			}
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to storagesamplereceiving table. Need to
	 * check for duplicate entry of samplereceiving name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default samplereceiving for a site
	 *
	 * @param sampleReceivingObj [SampleReceiving] object holding details to be added in samplereceiving
	 *                table
	 * @return inserted SampleReceiving object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createSampleReceiving(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final List<Object> savedSampleReceiving = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String fromDate = "";
		String toDate = "";
		final Map<String, Object> sampleReceivingObj = mapper.convertValue(inputMap.get("SampleReceiving"), Map.class);
		//JSONObject actionSampleReceing = new JSONObject();

		final String barcodeCheck = "select *  from storagesamplecollection where  " + "  sbarcodeid='"
				+ stringUtilityFunction.replaceQuote(sampleReceivingObj.get("sbarcodeid").toString()) + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and nsitecode = "+userInfo.getNtranssitecode()+"";

		List<Map<String, Object>> projectList = jdbcTemplate.queryForList(barcodeCheck);
		if (projectList.isEmpty()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CHOOSENBARCODENOTINTHISPROJECT",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			final String existsQuery = getBarcodeId(sampleReceivingObj.get("sbarcodeid").toString().trim(), userInfo);
			final SampleReceiving existsSampleReceivingObj = (SampleReceiving) jdbcUtilityFunction
					.queryForObject(existsQuery, SampleReceiving.class, jdbcTemplate);
			if (existsSampleReceivingObj == null) {
				String sQuery = " lock  table storagesamplereceiving "
						+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + ";";
				jdbcTemplate.execute(sQuery);
				if (inputMap.get("fromDate") != null) {
					fromDate = (String) inputMap.get("fromDate");
				}
				if (inputMap.get("toDate") != null) {
					toDate = (String) inputMap.get("toDate");
				}

				if (sampleReceivingObj.get("dcollectiondate") != null) {
					sampleReceivingObj.put("dcollectiondate",
							sampleReceivingObj.get("dcollectiondate").toString().replace("T", " ").replace("Z", ""));
				}
				final String seqNo = "select nsequenceno from seqnostoragemanagement where stablename='storagesamplereceiving' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				int nsequenceno = jdbcTemplate.queryForObject(seqNo, Integer.class);
				nsequenceno++;
				final String updatequery = "update seqnostoragemanagement set nsequenceno =" + nsequenceno
						+ " where stablename='storagesamplereceiving';";
				jdbcTemplate.execute(updatequery);

				String insertQuery = "INSERT INTO public.storagesamplereceiving(  "
						+ "	nstoragesamplereceivingcode, sbarcodeid, jsondata, dcollectiondate, ntzcollectiondatetime,   "
						+ "	noffsetdcollectiondatetime, scomments, dmodifieddate, nsitecode, nstatus)  "
						+ "	VALUES (" + nsequenceno + ",N'"
						+ stringUtilityFunction.replaceQuote(sampleReceivingObj.get("sbarcodeid").toString()) + "', "
						+ " '" + sampleReceivingObj.get("jsondata") + "' , '"
						+ sampleReceivingObj.get("dcollectiondate") + "',"
						+ sampleReceivingObj.get("ntzcollectiondatetime") + "," + "'"
						+ sampleReceivingObj.get("noffsetdcollectiondatetime") + "',N'"
						+ stringUtilityFunction.replaceQuote(sampleReceivingObj.get("scomments").toString()) + "','"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				jdbcTemplate.execute(insertQuery);

				multilingualIDList.add("IDS_ADDSAMPLERECEIVING");
				final SampleReceiving objAuditSampleReceiving = new SampleReceiving();
				final String sComments = sampleReceivingObj.get("scomments").toString();
				objAuditSampleReceiving.setScomments(sComments.isEmpty() ? "-" : sComments);
				objAuditSampleReceiving.setScollectiondate((String) sampleReceivingObj.get("dcollectiondate"));
				objAuditSampleReceiving.setSbarcodeid(
						stringUtilityFunction.replaceQuote(sampleReceivingObj.get("sbarcodeid").toString()));
				savedSampleReceiving.add(objAuditSampleReceiving);
				auditUtilityFunction.fnInsertAuditAction(savedSampleReceiving, 1, null, multilingualIDList, userInfo);
				return getSampleReceiving(fromDate, toDate, null, userInfo,
						(int) sampleReceivingObj.get("nprojecttypecode"));
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_BARCODEIDALREADYEXISTS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

	}

	/**
	 * This method is used to update entry in samplereceiving table. Need to
	 * validate that the samplereceiving object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * samplereceiving name for the specified site before saving into database. Need
	 * to check that there should be only one default samplereceiving for a site
	 *
	 * @param objSampleReceiving [SampleReceiving] object holding details to be updated in samplereceiving
	 *                table
	 * @return response entity object holding response status and data of updated
	 *         samplereceiving object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateSampleReceiving(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> beforeSampleReceiving = new ArrayList<>();
		final List<Object> afterSampleReceiving = new ArrayList<>();
		mapper.registerModule(new JavaTimeModule());

		final SampleReceiving objSampleReceiving = mapper.convertValue(inputMap.get("SampleReceiving"),
				new TypeReference<SampleReceiving>() {
		});
		final String activeQuery = getActiveSampleReceiving(objSampleReceiving.getNstoragesamplereceivingcode(),
				userInfo);
		final SampleReceiving activeSampleReceiving = (SampleReceiving) jdbcUtilityFunction.queryForObject(activeQuery,
				SampleReceiving.class, jdbcTemplate);
		if (activeSampleReceiving != null) {
			final String auditQueryBefore = getActiveSampleReceiving(
					objSampleReceiving.getNstoragesamplereceivingcode(), userInfo);
			final SampleReceiving objSampleReceivingAuditBefore = (SampleReceiving) jdbcUtilityFunction
					.queryForObject(auditQueryBefore, SampleReceiving.class, jdbcTemplate);
			if (inputMap.get("fromDate") != null) {
				fromDate = (String) inputMap.get("fromDate");
			}
			if (inputMap.get("toDate") != null) {
				toDate = (String) inputMap.get("toDate");
			}
			if (objSampleReceiving.getDcollectiondate() != null) {
				objSampleReceiving
				.setScollectiondate(dateUtilityFunction.instantDateToString(objSampleReceiving.getDcollectiondate())
						.replace("T", " ").replace("Z", ""));
			}
			final String updateQuery = "update storagesamplereceiving set dcollectiondate='"
					+ objSampleReceiving.getScollectiondate() + "',ntzcollectiondatetime="
					+ objSampleReceiving.getNtzcollectiondatetime() + "," + "noffsetdcollectiondatetime="
					+ objSampleReceiving.getNoffsetdcollectiondatetime() + ",scomments=N'"
					+ stringUtilityFunction.replaceQuote(objSampleReceiving.getScomments()) + "',dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nsitecode=" + userInfo.getNtranssitecode()
					+ " " + "where nstoragesamplereceivingcode=" + objSampleReceiving.getNstoragesamplereceivingcode()
					+ "";
			jdbcTemplate.execute(updateQuery);
			String auditQueryAfter = getActiveSampleReceiving(objSampleReceiving.getNstoragesamplereceivingcode(),
					userInfo);
			final SampleReceiving objSampleReceivingAuditAfter = (SampleReceiving) jdbcUtilityFunction
					.queryForObject(auditQueryAfter, SampleReceiving.class, jdbcTemplate);
			multilingualIDList.add("IDS_EDITSAMPLERECEIVING");
			beforeSampleReceiving.add(objSampleReceivingAuditBefore);
			objSampleReceivingAuditAfter.setSbarcodeid("-");
			afterSampleReceiving.add(objSampleReceivingAuditAfter);
			auditUtilityFunction.fnInsertAuditAction(afterSampleReceiving, 2, beforeSampleReceiving, multilingualIDList,
					userInfo);
			return getSampleReceiving(fromDate, toDate, null, userInfo, objSampleReceiving.getNprojecttypecode());
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to retrieve active samplereceiving object based on the
	 * specified nunitCode.
	 *
	 * @param nstoragesamplereceivingcode [int] primary key of samplereceiving object
	 * @return response entity object holding response status and data of samplereceiving
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleReceivingById(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int nstoragesamplereceivingcode = (int) inputMap.get("nstoragesamplereceivingcode");
		final String activeQuery = getActiveSampleReceiving(nstoragesamplereceivingcode, userInfo);
		final SampleReceiving activeSampleReceiving = (SampleReceiving) jdbcUtilityFunction.queryForObject(activeQuery,
				SampleReceiving.class, jdbcTemplate);
		if (activeSampleReceiving != null) {
			Map<String, Object> outputMap = new HashMap<>();
			String query = "SELECT nstoragesamplereceivingcode, sbarcodeid, jsondata,   "
					+ "COALESCE(TO_CHAR(dcollectiondate,'" + userInfo.getSpgsitedatetime() + "'),'') as "
					+ "scollectiondate,scomments, nsitecode FROM public.storagesamplereceiving "
					+ "where nstoragesamplereceivingcode=" + nstoragesamplereceivingcode + " and  " + "nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ";
			final SampleReceiving lstSampleReceiving = jdbcTemplate.queryForObject(query,
					new SampleReceiving());
			final Map<String, Object> jsondataValue = lstSampleReceiving.getJsondata();
			final String barcodequery = "SELECT  nsorter,jsondata->>'sfieldname' as sfieldname FROM  projecttype pt , bulkbarcodeconfigversion bcv,"
					+ "  bulkbarcodeconfig bc,bulkbarcodeconfigdetails bcd "
					+ "  where bcd.nbulkbarcodeconfigcode = bc.nbulkbarcodeconfigcode and bcd.nprojecttypecode = bc.nprojecttypecode "
					+ "  and  bc.nprojecttypecode = " + (int) inputMap.get("nprojecttypecode") + ""
					+ "  AND  bcd.nbulkbarcodeconfigcode = " + jsondataValue.get("nbulkbarcodeconfigcode") + ""
					+ "  AND  bc.nbulkbarcodeconfigcode = bcv.nbulkbarcodeconfigcode "
					+ "  AND  pt.nprojecttypecode = bc.nprojecttypecode " + "  AND pt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bcv.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bcd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "  AND bc.nsitecode = "
					+ userInfo.getNmastersitecode() + " and pt.nsitecode="+userInfo.getNmastersitecode()+" "
					+ " and bcv.nsitecode="+userInfo.getNmastersitecode()+" and bcd.nsitecode="+userInfo.getNmastersitecode()+" "
					+ " order by nsorter;";

			final List<Map<String, Object>> projectBarcodeConfig = jdbcTemplate.queryForList(barcodequery);
			outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);
			final Map<String, Object> map = new LinkedHashMap<>();
			final Map<String, Object> map1 = new LinkedHashMap<>();
			map.put("sfieldname", "Unit");
			map.put("nsorter", "15");
			map1.put("sfieldname", "Quantity");
			map1.put("nsorter", "16");
			projectBarcodeConfig.add(map);
			projectBarcodeConfig.add(map1);
			outputMap.put("activeSampleColletionByID", lstSampleReceiving);
			final String addFields = "select u.sunitname,sc.nsampleqty from storagesamplecollection sc,unit u    "
					+ " where  u.nunitcode=sc.nunitcode  " + " and sc.sbarcodeid='" + inputMap.get("spositionvalue")
					+ "' and sc.nsitecode="+userInfo.getNtranssitecode()+" and u.nsitecode="+userInfo.getNmastersitecode()+" "
					+ "  and sc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ "  and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by 1 desc";
			List<Map<String, Object>> listDataList = jdbcTemplate.queryForList(addFields);
			outputMap.put("addValue", listDataList);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method id used to delete an entry in samplereceiving table Need to check
	 * the record is already deleted or not
	 *
	 * @param objsamplereceiving [samplereceiving] an Object holds the record to be
	 *                           deleted
	 * @return a response entity with corresponding HTTP status and an unit object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteSampleReceiving(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final List<Object> deleteSampleReceiving = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String activeQuery = getActiveSampleReceiving((int) inputMap.get("nstoragesamplereceivingcode"),
				userInfo);
		final SampleReceiving activeSampleReceiving = (SampleReceiving) jdbcUtilityFunction.queryForObject(activeQuery,
				SampleReceiving.class, jdbcTemplate);
		if (activeSampleReceiving != null) {
			final List<SampleReceiving> objSampleReceiving = getSampleReceivingExist(inputMap, userInfo,
					activeSampleReceiving.getSbarcodeid());
			if (objSampleReceiving.get(0).isIsprocessing() == false
					&& objSampleReceiving.get(0).isIstemporarystorage() == false) {
				activeSampleReceiving.setScomments((String) inputMap.get("scomments"));
				activeSampleReceiving.setScollectiondate((String) inputMap.get("scollectiondate"));
				deleteSampleReceiving.add(activeSampleReceiving);
				multilingualIDList.add("IDS_DELETESAMPLERECEIVING");
				auditUtilityFunction.fnInsertAuditAction(deleteSampleReceiving, 1, null, multilingualIDList, userInfo);
				String deleteQuery = "update storagesamplereceiving set nstatus="
						+ Enumeration.TransactionStatus.NA.gettransactionstatus()
						+ " where nstoragesamplereceivingcode=" + (int) inputMap.get("nstoragesamplereceivingcode")
						+ "";
				jdbcTemplate.execute(deleteQuery);
				return getSampleReceiving(fromDate, toDate, null, userInfo, (int) inputMap.get("nprojecttypecode"));
			} else {
				String alert = "";
				final boolean isIsprocessing = objSampleReceiving.get(0).isIsprocessing();
				final boolean isIstemporarystorage = objSampleReceiving.get(0).isIstemporarystorage();
				final String sthisrecordisused = commonFunction.getMultilingualMessage("IDS_THISISUSEDIN",
						userInfo.getSlanguagefilename());
				if (isIsprocessing && isIstemporarystorage) {
					alert = commonFunction.getMultilingualMessage("IDS_STORAGESAMPLEPROCESSING",
							userInfo.getSlanguagefilename()) + ","
							+ commonFunction.getMultilingualMessage("IDS_TEMPORARYSTORAGE",
									userInfo.getSlanguagefilename());
				} else if (isIsprocessing) {
					alert = commonFunction.getMultilingualMessage("IDS_STORAGESAMPLEPROCESSING",
							userInfo.getSlanguagefilename());
				} else if (isIstemporarystorage) {
					alert = commonFunction.getMultilingualMessage("IDS_TEMPORARYSTORAGE",
							userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(sthisrecordisused + " " + alert, HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	private List<SampleReceiving> getSampleReceivingExist(Map<String, Object> inputMap, UserInfo userInfo,
			String activeSbarcodeid) {
		String sbarcode = activeSbarcodeid.trim();
		String processingBarcode = null;
		if (sbarcode.length() > 1) {
			processingBarcode = sbarcode.substring(0, 1) + '2' + sbarcode.substring(2);
		}
		final String strQuery = "select CASE WHEN EXISTS(select sbarcodeid from storagesampleprocessing "
				+ " where sbarcodeid ='" + processingBarcode + "' and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nprojecttypecode="
				+ inputMap.get("nprojecttypecode") + " and nsitecode=" + userInfo.getNtranssitecode() + ")  "
				+ "THEN TRUE ELSE FALSE  End AS isprocessing,"
				+ "CASE when exists (select sbarcodeid from temporarystorage where sbarcodeid in ('" + activeSbarcodeid
				+ "','" + processingBarcode + "' )  " + " and nprojecttypecode=" + inputMap.get("nprojecttypecode")
				+ "  and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + ")" + "THEN TRUE ELSE FALSE  End AS istemporarystorage;";
		return jdbcTemplate.query(strQuery, new SampleReceiving());
	}

	/**
	 * This method is used to fetch the active samplereceiving objects for the
	 * specified samplereceiving name and site.
	 *
	 * @param sbarcodeid [String] name of the samplereceiving
	 * @param nmasterSiteCode  [int] site code of the samplereceiving
	 * @return list of active samplereceiving code(s) based on the specified
	 *         samplereceiving name and site
	 * @throws Exception
	 */

	private String getBarcodeId(final String sbarcodeid, UserInfo userInfo) {
		String existsQuery = "select * from storagesamplereceiving where " + " sbarcodeid=N'"
				+ stringUtilityFunction.replaceQuote(sbarcodeid) + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nsitecode="
				+ userInfo.getNtranssitecode() + "";
		return existsQuery;
	}

	/**
	 * This method is used to fetch the active samplereceiving objects for the
	 * specified samplereceiving name and site.
	 *
	 * @param ssamplereceivingname [String] name of the samplereceiving
	 * @param nmasterSiteCode      [int] site code of the samplereceiving
	 * @return list of active samplereceiving code(s) based on the specified
	 *         samplereceiving name and site
	 * @throws Exception
	 */

	private String getActiveSampleReceiving(final int storagesamplereceivingcode, final UserInfo userInfo) {
		final String activeQuery = "select  *,dcollectiondate as scollectiondate from storagesamplereceiving where nstoragesamplereceivingcode="
				+ storagesamplereceivingcode + " and nsitecode=" + userInfo.getNtranssitecode() + " and " + "nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return activeQuery;
	}
	
	/**
	 * This method is used to fetch the active samplereceiving objects for the
	 * specified samplereceiving name and site.
	 *
	 * @param nprojecttypecode [int] name of the samplereceiving
	 * @param nmasterSiteCode      [int] site code of the samplereceiving
	 * @return list of active samplereceiving code(s) based on the specified
	 *         samplereceiving name and site
	 * @throws Exception
	 */

	public int getBarcodeLengthByProjecType(final int nprojecttypecode, final UserInfo userinfo) {
		int nbarcodelength = 0;
		final String stQuery = "select bc.nbarcodelength as nbarcodelength from bulkbarcodeconfig bc,bulkbarcodeconfigversion bcv,"
				+ " projecttype pt where bc.nbulkbarcodeconfigcode=bcv.nbulkbarcodeconfigcode"
				+ "	and  bc.nprojecttypecode=" + nprojecttypecode
				+ " and pt.nprojecttypecode=bc.nprojecttypecode and bcv.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " 	and bcv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bc.nsitecode="
				+ userinfo.getNmastersitecode() + " and bcv.nsitecode="+userinfo.getNmastersitecode()+" and pt.nsitecode="+userinfo.getNmastersitecode()+"";
		List<Map<String, Object>> listBulkBarcodeConfig = jdbcTemplate.queryForList(stQuery);
		if (!listBulkBarcodeConfig.isEmpty()) {
			nbarcodelength = Integer.parseInt(listBulkBarcodeConfig.get(0).get("nbarcodelength").toString());
		} else {
			nbarcodelength = 0;
		}
		LOGGER.info("getBarcodeLengthByProjecType:" + stQuery);
		return nbarcodelength;
	}

}
