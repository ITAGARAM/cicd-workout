package com.agaramtech.qualis.storagemanagement.service.sampleprocessing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.agaramtech.qualis.storagemanagement.model.SampleProcessing;
import com.agaramtech.qualis.storagemanagement.model.TemporaryStorage;
import com.agaramtech.qualis.storagemanagement.service.samplestoragetransaction.SampleStorageTransactionDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "storagesampleprocessing" table by 
 * implementing methods from its interface. 
 */
@RequiredArgsConstructor
@Repository
public class SampleProcessingDAOImpl implements SampleProcessingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleProcessingDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final SampleStorageTransactionDAO sampleStorageTransactionDAO;
	
	
	/**
	 * This method is used to get all the available storagesampleprocessing with respect to site
	 * @param fromDate [String] From Date.
	 * @param toDate [String]To Date.
	 * @param currentUIDate [String] Current Date.
	 * @param nprojecttypecode [int] Project Type Code.
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of storagesampleprocessing records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleProcessing(String fromDate, String toDate, String currentUIDate,
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
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode="+userInfo.getNmastersitecode();
		final List<ProjectType> list = jdbcTemplate.query(strQuery, new ProjectType());
		outputMap.put("projectType", list);

		final Map<String, Object> defaultProjectType = new LinkedHashMap<String, Object>();
		if (!list.isEmpty()) {
			if (nprojecttypecode == -1) {
				defaultProjectType.put("label", list.get(list.size() - 1).getSprojecttypename());
				defaultProjectType.put("value", list.get(list.size() - 1).getNprojecttypecode());
				defaultProjectType.put("item", list.get(list.size() - 1));
				nprojecttypecode = list.get(list.size() - 1).getNprojecttypecode();
			} else {
				final String strQuery1 = "select * from projecttype where "
						+ " nprojecttypecode=" + nprojecttypecode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode="+userInfo.getNmastersitecode();
				final ProjectType lstProjectType = jdbcTemplate.queryForObject(strQuery1,
						new ProjectType());
				defaultProjectType.put("label", lstProjectType.getSprojecttypename());
				defaultProjectType.put("value", lstProjectType.getNprojecttypecode());
				defaultProjectType.put("item", lstProjectType);
			}
		}
		final int nbarcodelength = getBarcodeLengthByProjecType(nprojecttypecode, userInfo);

		final String query = "select CONCAT(spt.nprocesstime ,' ', coalesce(per.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " per.jsondata->'speriodname'->>'en-US')) as sprocessduration,CONCAT(spt.ngracetime ,' ', coalesce(per1.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " per1.jsondata->'speriodname'->>'en-US')) as sgraceduration,protype.nprocesstypecode,sp.ncollectiontubetypecode,protype.sprocesstypename,p.sproductname,case when sp.scomments='' then '-' else sp.scomments end as scomments,sp.sdeviationcomments,sp.nsampleprocessingcode,ct.stubename,pt.sprojecttypename,sp.sbarcodeid,sp.jsondata,COALESCE(TO_CHAR(sp.dprocessstartdate,'"
				+ "" + userInfo.getSpgsitedatetime()
				+ "'),'') as sprocessstartdate,COALESCE(TO_CHAR(sp.dprocessenddate,'" + ""
				+ userInfo.getSpgsitedatetime() + ""
				+ "'),'-') as sprocessenddate,case when sp.scomments='' then '-' else sp.scomments end as scomments ,"
				+ " case when sp.sdeviationcomments='' then '-' else sp.sdeviationcomments end as sdeviationcomments,"
				+ " sp.nprojecttypecode from storagesampleprocessing sp,sampleprocesstype spt,collectiontubetype ct,"
				+ " projecttype pt,processtype protype,product p,samplecollectiontype sct,period per,period per1 "
				+ " where spt.nsampleprocesstypecode=sp.nsampleprocesstypecode "
				+ " and spt.ncollectiontubetypecode=sp.ncollectiontubetypecode "
				+ " and spt.nsamplecollectiontypecode =sp.nsamplecollectiontypecode and sct.nproductcode=p.nproductcode and "
				+ " protype.nprocesstypecode=spt.nprocesstypecode  and sp.ncollectiontubetypecode=spt.ncollectiontubetypecode "
				+ " and sp.ncollectiontubetypecode=spt.ncollectiontubetypecode "
				+ " and sp.ncollectiontubetypecode=ct.ncollectiontubetypecode and per1.nperiodcode=spt.ngraceperiodtime "
				+ " and per.nperiodcode=spt.nprocessperiodtime and sp.nsamplecollectiontypecode=sct.nsamplecollectiontypecode"
				+ " and sp.nprojecttypecode=pt.nprojecttypecode and spt.nprojecttypecode=pt.nprojecttypecode "
				+ " and ct.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and spt.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and protype.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sct.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and per.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and per1.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sp.nsitecode="	+ userInfo.getNtranssitecode()
				+ " and spt.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and ct.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and pt.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and protype.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and p.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and sct.nsitecode="	+ userInfo.getNmastersitecode()
				//+ " and per.nsitecode="	+ userInfo.getNmastersitecode()
				//+ " and per1.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and  sp.dprocessstartdate between '"
				+ fromDate + "' and '" + toDate + "' and sp.nprojecttypecode=" + nprojecttypecode
				+ " order by sp.nsampleprocessingcode desc";

		final List<SampleProcessing> lstSampleProcessing = jdbcTemplate.query(query, new SampleProcessing());

		final String barcodequery = getBarcodeFieldsByProjectType(nprojecttypecode, userInfo);
		final List<Map<String, Object>> projectBarcodeConfig = jdbcTemplate.queryForList(barcodequery);

		Map<String, Object> map = new LinkedHashMap<>();
		Map<String, Object> map1 = new LinkedHashMap<>();
		map.put("sfieldname", "Quantity");
		map1.put("sfieldname", "Unit");
		map.put("nsorter", "11");
		map1.put("nsorter", "12");
		projectBarcodeConfig.add(map);
		projectBarcodeConfig.add(map1);

		final String query1 = " select bbd.* from bulkbarcodeconfigdetails bbd,bulkbarcodeconfig bb,"
				+ " bulkbarcodeconfigversion bbv where "
				+ " bbd.nprojecttypecode= " + nprojecttypecode
				+ " and bbd.nbulkbarcodeconfigcode=bb.nbulkbarcodeconfigcode and "
				+ " bb.nbulkbarcodeconfigcode=bbv.nbulkbarcodeconfigcode and bbv.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and bbd.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bb.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bbv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bbd.nsitecode="+ userInfo.getNmastersitecode()
				+ " and bb.nsitecode="+ userInfo.getNmastersitecode()
				+ " and bbv.nsitecode="+ userInfo.getNmastersitecode()
				+ "   order by nsorter ";
		final List<BulkBarcodeConfigDetails> barcodelist = jdbcTemplate.query(query1, new BulkBarcodeConfigDetails());
		boolean iscollectiontubetype = false;
		iscollectiontubetype = barcodelist.stream().anyMatch(item -> item.getStablename().equals("collectiontubetype"));

		boolean issamplecollectiontype = barcodelist.stream()
				.anyMatch(item -> item.getStablename().equals("collectiontubetype"));
		if (iscollectiontubetype) {
			final List<BulkBarcodeConfigDetails> filteredList = barcodelist.stream()
					.filter(item -> item.getStablename().equals("collectiontubetype")).collect(Collectors.toList());
			final String sfieldnameCollection = filteredList.get(0).getJsondata().get("sfieldname").toString();
			outputMap.put("sfieldnameCollection", sfieldnameCollection);
		}
		if (issamplecollectiontype) {
			List<BulkBarcodeConfigDetails> filteredListSampleCollection = barcodelist.stream()
					.filter(item -> item.getStablename().equals("samplecollectiontype")).collect(Collectors.toList());
			String sfieldnameSampleType = filteredListSampleCollection.get(0).getJsondata().get("sfieldname")
					.toString();
			outputMap.put("sfieldnameSampleType", sfieldnameSampleType);
		}
		outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);
		outputMap.put("selectedProjectType", defaultProjectType);
		outputMap.put("nbarcodelength", nbarcodelength);
		outputMap.put("SampleProcessing", lstSampleProcessing);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 *  This method is used to retrieve list of all active Barcode data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nbarcodeLength","toDate","spositionvalue","nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all active Barcode data specified project type and site.
	 * @throws Exception that are thrown from this DAO layer.
	 * @jira ALPD-4627 Displaying barcode info while clicking enter key in Sample
	 *       collection, Sample receiving, Sample Processing, Temporary Storage.
	 *       Storage.
	 */

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getBarcodeConfigData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		String sbarcode = inputMap.get("spositionvalue").toString();
		String newBarcode = null;
		if (sbarcode.length() > 1) {
			newBarcode = sbarcode.substring(0, 1) + '1' + sbarcode.substring(2);
		}
		final String barcodeCheck = "select * from storagesamplereceiving where sbarcodeid='" + newBarcode
				+ "' and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		List<Map<String, Object>> objSampleReceiving = jdbcTemplate.queryForList(barcodeCheck);
		if (objSampleReceiving.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SAMPLENOTRECEIVED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			Map<String, Object> barcodeDetails = sampleStorageTransactionDAO.readBarcode(inputMap, userInfo);
			final String query = getProcessDurationTime(inputMap, userInfo);
			final SampleProcessing objSampleProcessing = (SampleProcessing) jdbcUtilityFunction.queryForObject(query,
					SampleProcessing.class, jdbcTemplate);
			boolean isSecondtime = false;
			if (objSampleProcessing != null) {
				isSecondtime = true;
				outputMap.put("sprocessstartdatesecondtime", isSecondtime);
				if (objSampleProcessing.getSprocessstartdate() != null) {
					outputMap.put("sprocessstartdate", objSampleProcessing.getSprocessstartdate());
				}
				if (!objSampleProcessing.getSprocessenddate().isEmpty()) {
					outputMap.put("sprocessenddate", objSampleProcessing.getSprocessenddate());
				}
				if (!objSampleProcessing.getScomments().isEmpty()) {
					outputMap.put("scomments", objSampleProcessing.getScomments());
				}
				if (!objSampleProcessing.getSdeviationcomments().isEmpty()) {
					outputMap.put("sdeviationcomments", objSampleProcessing.getSdeviationcomments());
				}
			}
			final String barcodequery = getBarcodeFieldsByProjectType((int) inputMap.get("nprojecttypecode"), userInfo);

			List<Map<String, Object>> projectBarcodeConfig = jdbcTemplate.queryForList(barcodequery);

			final String addFields = "select u.sunitname,sc.nsampleqty from storagesamplecollection sc,unit u    "
					+ "where  u.nunitcode=sc.nunitcode  " + "and sc.sbarcodeid='" + newBarcode + "' and  sc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";
			final List<Map<String, Object>> listDataList = jdbcTemplate.queryForList(addFields);

			final Map<String, Object> jsonDataValue = mapper
					.readValue(barcodeDetails.get("objJsonPrimaryKeyValue").toString(), Map.class);
			final Map<String, Object> jsonData = mapper.readValue(barcodeDetails.get("jsonValue").toString(),
					Map.class);

			Map<String, Object> map = new LinkedHashMap<>();
			Map<String, Object> map1 = new LinkedHashMap<>();
			map.put("sfieldname", "Quantity");
			map1.put("sfieldname", "Unit");
			map.put("nsorter", "11");
			map1.put("nsorter", "12");
			projectBarcodeConfig.add(map);
			projectBarcodeConfig.add(map1);
			jsonData.put("Quantity", listDataList.get(0).get("nsampleqty"));
			jsonData.put("Unit", listDataList.get(0).get("sunitname"));
			jsonData.put("nbulkbarcodeconfigcode", jsonDataValue.get("nbulkbarcodeconfigcode"));
			outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);

			if (barcodeDetails != null && barcodeDetails.containsKey("jsonValue")) {
				outputMap.put("jsondataBarcodeData", jsonData);
				outputMap.put("jsonPrimaryKeyValue", jsonDataValue);
			}
			System.out.println("Output Map: " + outputMap);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve list of all active Sample Type data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all active Sample Type data for the specified project type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final String strQuery = "select p.sproductname,p.nproductcode,sct.nsamplecollectiontypecode "
				+ " from samplecollectiontype sct,projecttype pt,product p  "
				+ " where sct.nprojecttypecode=pt.nprojecttypecode  "
				+ "and sct.nproductcode=p.nproductcode  "
				+ " and pt.nsitecode = " 	+ userInfo.getNmastersitecode()
				+ " and sct.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and p.nsitecode=" + userInfo.getNmastersitecode()
				+ " and sct.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sct.nsitecode="	+ userInfo.getNmastersitecode()
				+ " " + "and pt.nprojecttypecode=" + inputMap.get("nprojecttypecode")
				+ "; ";
		final List<Map<String, Object>> sampletypeList = jdbcTemplate.queryForList(strQuery);
		final Map<String, Object> map = new HashMap<>();
		map.put("sampletypeList", sampletypeList);
		LOGGER.info("getSampleType:" + strQuery);
		return new ResponseEntity<>(sampletypeList, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active Sample Process Type data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all active Sample Process Type data for the specified project type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleProcessType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final String strQuery = "select sp.nprocesstypecode,sprocesstypename,sp.nsampleprocesstypecode "
				+ " from processtype pt,sampleprocesstype sp,collectiontubetype ct,"
				+ " samplecollectiontype sct where sp.nprocesstypecode in(select pt.nprocesstypecode "
				+ " from  sampleprocesstype sp,processtype pt,projecttype p  where "
				+ "  sp.nprojecttypecode=" + (int) inputMap.get("nprojecttypecode") + ""
				+ "  and  sp.nprojecttypecode=p.nprojecttypecode and  pt.nprocesstypecode=sp.nprocesstypecode and  pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  sp.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sp.nsitecode = "
				+ userInfo.getNmastersitecode()
				+ ") and sct.nsamplecollectiontypecode=sp.nsamplecollectiontypecode and ct.ncollectiontubetypecode=sp.ncollectiontubetypecode and  sct.nsamplecollectiontypecode ="
				+ (int) inputMap.get("nsamplecollectiontypecode") + " and   sp.ncollectiontubetypecode="
				+ (int) inputMap.get("ncollectiontubetypecode")
				+ " and sp.nprocesstypecode=pt.nprocesstypecode "
				+ " and pt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sct.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ct.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and sct.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and ct.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and sp.nsitecode="	+ userInfo.getNmastersitecode()
				+ " order by sp.nexecutionorder;";
		final List<Map<String, Object>> collectionSampleProcessType = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(collectionSampleProcessType, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active Process Duration data for the specified project type and Sample Type and Collection tube type and site.
	 * @param inputMap  [inputMap] map object with "ncollectiontubetypecode","nsamplecollectiontypecode","nprocesstypecode","nprojecttypecode" and  "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all active Process Duration data for the specified project type and Sample Type and Collection tube type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProcessduration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final String strQuery = "select CONCAT(sp.nprocesstime ,' ', coalesce(p.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " p.jsondata->'speriodname'->>'en-US')) as sprocessduration,CONCAT(sp.ngracetime ,' ', coalesce(p.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " p.jsondata->'speriodname'->>'en-US')) as sgraceduration,pt.sprocesstypename,"
				+ " sp.nsampleprocesstypecode,sp.nprocesstime,sp.ngracetime "
				+ " from  sampleprocesstype sp,processtype pt,period p"
				+ " where sp.ncollectiontubetypecode=" + (int) inputMap.get("ncollectiontubetypecode")
				+ " and sp.nsamplecollectiontypecode =" + (int) inputMap.get("nsamplecollectiontypecode") + " "
				+ " and  sp.nprocesstypecode=" + (int) inputMap.get("nprocesstypecode") + "  and sp.nprojecttypecode="
				+ (int) inputMap.get("nprojecttypecode") + ""
				+ " and sp.nprocessperiodtime=p.nperiodcode and pt.nprocesstypecode=sp.nprocesstypecode "
				+ " and  pt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  p.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sp.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sp.nsitecode = "+ userInfo.getNmastersitecode()
				+ " and pt.nsitecode = "+ userInfo.getNmastersitecode()
				+ " ;";
		final List<Map<String, Object>> collectionSampleProcessType = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(collectionSampleProcessType, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all active Collection Tube type data for the specified project type and site.
	 * @param inputMap  [inputMap] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched	
	 * @return response entity object holding response status and list of all active Collection Tube type data for the specified project type and site. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCollectionTubeType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final String strQuery = "select ncollectiontubetypecode,stubename from collectiontubetype "
				+ " where ncollectiontubetypecode>0 and nprojecttypecode=" + (int) inputMap.get("nprojecttypecode")
				+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = "+ userInfo.getNmastersitecode() + " ;";
		final List<Map<String, Object>> collectiontubeList = jdbcTemplate.queryForList(strQuery);
		return new ResponseEntity<>(collectiontubeList, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to storagesampleprocessing  table.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details
	 * @return ResponseEntity with string message as 'The Barcode ID scanned already' if the barcode id already scanned / 
	 * 			list of storagesampleprocessing along with the newly added storagesampleprocessing .
	 * @throws Exception exception
	 */		@Override
	public ResponseEntity<Object> createSampleProcessing(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table storagesampleprocessing "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final ObjectMapper mapper = new ObjectMapper();
		final List<Object> savedSampleProcessing = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String fromDate = "";
		String toDate = "";
		mapper.registerModule(new JavaTimeModule());
		Map<String, Object> sampleProcessingObj = mapper.convertValue(inputMap.get("sampleprocessing"), Map.class);
		String sbarcode = sampleProcessingObj.get("sbarcodeid").toString().trim();
		String newBarcode = null;
		if (sbarcode.length() > 1) {
			newBarcode = sbarcode.substring(0, 1) + '1' + sbarcode.substring(2);
		}
		final String barcodeCheck = "select * from storagesamplereceiving where sbarcodeid='" + newBarcode
				+ "' and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
		final List<Map<String, Object>> objSampleReceiving = jdbcTemplate.queryForList(barcodeCheck);
		if (objSampleReceiving.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SAMPLENOTRECEIVED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String existsQuery = getBarcodeId(sampleProcessingObj.get("sbarcodeid").toString().trim(),
					(int) sampleProcessingObj.get("nsampleprocesstypecode"), userInfo);

			final SampleProcessing existsSampleProcessingObj = (SampleProcessing) jdbcUtilityFunction
					.queryForObject(existsQuery, SampleProcessing.class, jdbcTemplate);

			final String validation = "select * from storagesampleprocessing where  sbarcodeid='"
					+ sampleProcessingObj.get("sbarcodeid").toString().trim() + "' "
					+ "and nprojecttypecode<>"
					+ sampleProcessingObj.get("nprojecttypecode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and nsitecode=" + userInfo.getNtranssitecode();
			final List<Map<String, Object>> countList = jdbcTemplate.queryForList(validation);
			if (!countList.isEmpty()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYEXISTS", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			final LocalDateTime processStartDate = LocalDateTime.parse(
					sampleProcessingObj.get("dprocessstartdate").toString().replace("T", " ").replace("Z", ""),
					formatter);
			LocalDateTime processEndDate = null;
			if (sampleProcessingObj.get("dprocessenddate") != null) {
				processEndDate = LocalDateTime.parse(
						sampleProcessingObj.get("dprocessenddate").toString().replace("T", " ").replace("Z", ""),
						formatter);
			}
			if (sampleProcessingObj.get("dprocessenddate") == null
					|| processEndDate.isBefore(processStartDate) != true) {
				if (inputMap.get("fromDate") != null) {
					fromDate = (String) inputMap.get("fromDate");
				}
				if (inputMap.get("toDate") != null) {
					toDate = (String) inputMap.get("toDate");
				}
				if (sampleProcessingObj.get("dprocessstartdate") != null) {
					sampleProcessingObj.put("dprocessstartdate",
							sampleProcessingObj.get("dprocessstartdate").toString().replace("T", " ").replace("Z", ""));
				}
				String dprocessenddate;
				if (sampleProcessingObj.get("dprocessenddate") != null) {
					dprocessenddate = "'"
							+ sampleProcessingObj.get("dprocessenddate").toString().replace("T", " ").replace("Z", "")
							+ "'";
				} else {
					dprocessenddate = null;
				}

				final Map<String, Object> lstprocesstype = getActiceProcessTypeByBarcodeId(inputMap,
						sampleProcessingObj.get("sbarcodeid").toString().trim(), userInfo);

				final String strstatus = (String) lstprocesstype
						.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
				if (!Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(strstatus)) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(strstatus, userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					if (existsSampleProcessingObj == null) {
						final String seqNo = "select nsequenceno from seqnostoragemanagement where stablename='storagesampleprocessing' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "";
						int nsequenceno = jdbcTemplate.queryForObject(seqNo, Integer.class);
						nsequenceno++;
						final String updatequery = "update seqnostoragemanagement set nsequenceno =" + nsequenceno
								+ " where stablename='storagesampleprocessing'";
						jdbcTemplate.execute(updatequery);

						final String insertQuery = "insert into storagesampleprocessing(nsampleprocessingcode,nprojecttypecode,nsamplecollectiontypecode,ncollectiontubetypecode,nsampleprocesstypecode,"
								+ "sbarcodeid,jsondata,dprocessstartdate,ntzprocessstartdate,noffsetdprocessstartdate,dprocessenddate,ntzprocessenddate,noffsetdprocessenddate,"
								+ "scomments,sdeviationcomments,dmodifieddate,nsitecode,nstatus) values(" + nsequenceno
								+ "," + sampleProcessingObj.get("nprojecttypecode") + ","
								+ sampleProcessingObj.get("nsamplecollectiontypecode") + ","
								+ sampleProcessingObj.get("ncollectiontubetypecode") + "," + " "
								+ sampleProcessingObj.get("nsampleprocesstypecode") + "," + "N'"
								+ stringUtilityFunction.replaceQuote(sampleProcessingObj.get("sbarcodeid").toString())
								+ "','" + sampleProcessingObj.get("jsondata") + "','"
								+ sampleProcessingObj.get("dprocessstartdate") + "',"
								+ sampleProcessingObj.get("ntzprocessstartdate") + "," + "'"
								+ sampleProcessingObj.get("noffsetdprocessstartdate") + "'," + dprocessenddate + ","
								+ "" + sampleProcessingObj.get("ntzprocessenddate") + ","
								+ sampleProcessingObj.get("noffsetdprocessenddate") + ",N'"
								+ stringUtilityFunction.replaceQuote(sampleProcessingObj.get("scomments").toString())
								+ "',N'"
								+ stringUtilityFunction
								.replaceQuote(sampleProcessingObj.get("sdeviationcomments").toString())
								+ "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
						jdbcTemplate.execute(insertQuery);

						final String auditQuery = sampleProcessignAudit(nsequenceno, userInfo);
						multilingualIDList.add("IDS_ADDSAMPLEPROCESSING");

						final SampleProcessing objSampleProcessing = (SampleProcessing) jdbcUtilityFunction
								.queryForObject(auditQuery, SampleProcessing.class, jdbcTemplate);
						savedSampleProcessing.add(objSampleProcessing);

						auditUtilityFunction.fnInsertAuditAction(savedSampleProcessing, 1, null, multilingualIDList,
								userInfo);
					} else {
						final List<Object> beforeSampleProcessing = new ArrayList<>();
						final List<Object> afterSampleProcessing = new ArrayList<>();

						String auditQuery = sampleProcessignAudit(existsSampleProcessingObj.getNsampleprocessingcode(),
								userInfo);
						multilingualIDList.add("IDS_EDITSAMPLEPROCESSING");

						final SampleProcessing objSampleProcessingBeforeAudit = (SampleProcessing) jdbcUtilityFunction
								.queryForObject(auditQuery, SampleProcessing.class, jdbcTemplate);
						final String updateQuery = "update storagesampleprocessing set nsamplecollectiontypecode="
								+ sampleProcessingObj.get("nsamplecollectiontypecode") + ","
								+ "ncollectiontubetypecode=" + sampleProcessingObj.get("ncollectiontubetypecode")
								+ ",nsampleprocesstypecode=" + sampleProcessingObj.get("nsampleprocesstypecode")
								+ ",dprocessstartdate='" + sampleProcessingObj.get("dprocessstartdate")
								+ "',ntzprocessstartdate=" + sampleProcessingObj.get("ntzprocessstartdate") + ","
								+ "noffsetdprocessstartdate=" + sampleProcessingObj.get("noffsetdprocessstartdate")
								+ ",dprocessenddate=" + dprocessenddate + "," + " ntzprocessenddate="
								+ sampleProcessingObj.get("ntzprocessenddate") + ",noffsetdprocessenddate="
								+ sampleProcessingObj.get("noffsetdprocessenddate") + "," + "" + "scomments=N'"
								+ stringUtilityFunction.replaceQuote(sampleProcessingObj.get("scomments").toString())
								+ "',sdeviationcomments=N'"
								+ stringUtilityFunction
								.replaceQuote(sampleProcessingObj.get("sdeviationcomments").toString())
								+ "'," + " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
								+ "',nsitecode=" + userInfo.getNtranssitecode() + " " + "where nsampleprocessingcode="
								+ existsSampleProcessingObj.getNsampleprocessingcode() + "";
						jdbcTemplate.execute(updateQuery);

						auditQuery = sampleProcessignAudit(existsSampleProcessingObj.getNsampleprocessingcode(),
								userInfo);

						final SampleProcessing objSampleProcessingAuditAfter = (SampleProcessing) jdbcUtilityFunction
								.queryForObject(auditQuery, SampleProcessing.class, jdbcTemplate);
						beforeSampleProcessing.add(objSampleProcessingBeforeAudit);
						afterSampleProcessing.add(objSampleProcessingAuditAfter);

						auditUtilityFunction.fnInsertAuditAction(afterSampleProcessing, 2, beforeSampleProcessing,
								multilingualIDList, userInfo);
					}
					return getSampleProcessing(fromDate, toDate, null, userInfo,
							(int) sampleProcessingObj.get("nprojecttypecode"));
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ENDATEAFTERSTARTDATE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
		 * This method is used to fetch the storagesampleprocessing object for the specified barcode id and site.
		 * @param sbarcodeid [String] name of the Barcode ID
		 * @param nsampleprocesstypecode [int] name of the Sample Process Type code.
		 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched	
		 * @return storagesampleprocessing object based on the specified barcode id and site
		 * @throws Exception that are thrown from this DAO layer
		 */
	private String getBarcodeId(final String sbarcodeid, final int nsampleprocesstypecode, UserInfo userInfo) {
		return "select nsampleprocessingcode,scomments,sdeviationcomments,COALESCE(TO_CHAR(dprocessstartdate,'"
				+ userInfo.getSpgsitedatetime()
				+ "'),'') as sprocessstartdate,dprocessstartdate,dprocessenddate,COALESCE(TO_CHAR(dprocessenddate,'"
				+ userInfo.getSpgsitedatetime() + "'),'') as sprocessenddate from storagesampleprocessing where "
				+ " sbarcodeid=N'" + stringUtilityFunction.replaceQuote(sbarcodeid) + "' and nsampleprocesstypecode="
				+ nsampleprocesstypecode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " " + "and nsitecode=" + userInfo.getNtranssitecode() + "";
	}

	/**
	 * This method is used to update entry in storagesampleprocessing details.
	 * @param inputMap  [inputMap] map object with "fromDate","toDate","currentdate","nprojecttypecode","temporarystorage" and "userinfo" as keys for which the data is to be fetched
	 * 								temporarystorage [TemporaryStorage]  object holding details to be added in temporarystorage table,
	 * 								userinfo [UserInfo] holding logged in user details
	 * @return ResponseEntity with storagesampleprocessing object for the specified primary key / with string message as
	 * 						'Already Deleted' if the storagesampleprocessing record is not available
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> updateSampleProcessing(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> beforeSampleProcessing = new ArrayList<>();
		final List<Object> afterSampleProcessing = new ArrayList<>();
		mapper.registerModule(new JavaTimeModule());

		final SampleProcessing objSampleProcessing = mapper.convertValue(inputMap.get("sampleprocessing"),
				new TypeReference<SampleProcessing>() {
		});
		final String activeQuery = getActiveSampleProcessing(objSampleProcessing.getNsampleprocessingcode(), userInfo);
		final SampleProcessing activeSampleProcessing = (SampleProcessing) jdbcUtilityFunction
				.queryForObject(activeQuery, SampleProcessing.class, jdbcTemplate);
		if (activeSampleProcessing != null) {
			String auditQuery = sampleProcessignAudit(objSampleProcessing.getNsampleprocessingcode(), userInfo);
			multilingualIDList.add("IDS_EDITSAMPLEPROCESSING");
			final SampleProcessing objSampleProcessingAuditBefore = (SampleProcessing) jdbcUtilityFunction
					.queryForObject(auditQuery, SampleProcessing.class, jdbcTemplate);
			if (objSampleProcessing.getDprocessenddate() == null || objSampleProcessing.getDprocessenddate()
					.isBefore(objSampleProcessing.getDprocessstartdate()) != true) {
				if (inputMap.get("fromDate") != null) {
					fromDate = (String) inputMap.get("fromDate");
				}
				if (inputMap.get("toDate") != null) {
					toDate = (String) inputMap.get("toDate");
				}
				if (objSampleProcessing.getDprocessstartdate() != null) {
					objSampleProcessing.setSprocessstartdate(
							dateUtilityFunction.instantDateToString(objSampleProcessing.getDprocessstartdate())
							.replace("T", " ").replace("Z", ""));
				}
				String dprocessenddate;
				if (objSampleProcessing.getDprocessenddate() != null) {
					objSampleProcessing.setSprocessenddate(
							dateUtilityFunction.instantDateToString(objSampleProcessing.getDprocessenddate())
							.replace("T", " ").replace("Z", ""));
					dprocessenddate = "'" + objSampleProcessing.getSprocessenddate() + "'";
				} else {
					dprocessenddate = null;
				}
				final String updateQuery = "update storagesampleprocessing set dprocessstartdate='"
						+ objSampleProcessing.getSprocessstartdate() + "',ntzprocessstartdate="
						+ objSampleProcessing.getNtzprocessstartdate() + "," + "noffsetdprocessstartdate="
						+ objSampleProcessing.getNoffsetdprocessstartdate() + ",dprocessenddate=" + dprocessenddate
						+ "," + " ntzprocessenddate=" + objSampleProcessing.getNtzprocessenddate()
						+ ",noffsetdprocessenddate=" + objSampleProcessing.getNoffsetdprocessenddate() + "," + ""
						+ "scomments=N'" + stringUtilityFunction.replaceQuote(objSampleProcessing.getScomments())
						+ "',sdeviationcomments=N'"
						+ stringUtilityFunction.replaceQuote(objSampleProcessing.getSdeviationcomments()) + "',"
						+ " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',nsitecode="
						+ userInfo.getNtranssitecode() + " " + "where nsampleprocessingcode="
						+ objSampleProcessing.getNsampleprocessingcode() + "";
				jdbcTemplate.execute(updateQuery);

				auditQuery = sampleProcessignAudit(objSampleProcessing.getNsampleprocessingcode(), userInfo);

				final SampleProcessing objSampleProcessingAuditAfter = (SampleProcessing) jdbcUtilityFunction
						.queryForObject(auditQuery, SampleProcessing.class, jdbcTemplate);
				beforeSampleProcessing.add(objSampleProcessingAuditBefore);
				afterSampleProcessing.add(objSampleProcessingAuditAfter);

				auditUtilityFunction.fnInsertAuditAction(afterSampleProcessing, 2, beforeSampleProcessing,
						multilingualIDList, userInfo);
				return getSampleProcessing(fromDate, toDate, null, userInfo, objSampleProcessing.getNprojecttypecode());
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ENDATEAFTERSTARTDATE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * This method is used to retrieve the active storagesampleprocessing object for the selected sample processing and site.
	 * @param nsampleprocesstypecode [int] name of the Sample Process Type code.
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched	
	 * @return storagesampleprocessing object for the selected sample processing and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private String getActiveSampleProcessing(final int nsampleprocessingcode, final UserInfo userInfo) {

		return "select nsampleprocessingcode,nprojecttypecode,nsamplecollectiontypecode ,COALESCE(TO_CHAR(dprocessenddate,'"
				+ userInfo.getSpgsitedatetime() + ""
				+ "'),'') as sprocessenddate from storagesampleprocessing where nsampleprocessingcode="
				+ nsampleprocessingcode + " and nsitecode=" + userInfo.getNtranssitecode() + " and " + "nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
	}

	/**
	 * This method is used to retrieve a specific storagesampleprocessing record.
	 * @param inputMap  [Map] map object with "nprojecttypecode","nsampleprocessingcode" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with Temporary Storage object for the specified primary key / with string message as
	 * 						'Already Deleted' if the storagesampleprocessing record is not available
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleProcessingById(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int nsampleprocessingcode = (int) inputMap.get("nsampleprocessingcode");
		final String activeQuery = getActiveSampleProcessing(nsampleprocessingcode, userInfo);
		final SampleProcessing activeSampleProcessing = (SampleProcessing) jdbcUtilityFunction
				.queryForObject(activeQuery, SampleProcessing.class, jdbcTemplate);

		if (activeSampleProcessing != null) {
			Map<String, Object> outputMap = new HashMap<>();
			outputMap.put("iscommentsrequired", false);
			outputMap.put("isdevaiationrequired", false);
			final String query = "select protype.nprocesstypecode,sp.nsampleprocesstypecode,protype.sprocesstypename,p.sproductname,sp.scomments,sp.sdeviationcomments,sp.nsampleprocessingcode,ct.stubename,pt.sprojecttypename,sp.sbarcodeid,sp.jsondata,COALESCE(TO_CHAR(sp.dprocessstartdate,'"
					+ "" + userInfo.getSpgsitedatetime()
					+ "'),'') as sprocessstartdate,COALESCE(TO_CHAR(sp.dprocessenddate,'" + ""
					+ userInfo.getSpgsitedatetime() + ""
					+ "'),'') as sprocessenddate,sp.nprojecttypecode,p.nproductcode,ct.ncollectiontubetypecode,CONCAT(spt.nprocesstime,' ', coalesce(per.jsondata->'speriodname'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ " per.jsondata->'speriodname'->>'en-US')) as sprocessduration,CONCAT(spt.ngracetime,' ', coalesce(per1.jsondata->'speriodname'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ " per1.jsondata->'speriodname'->>'en-US')) as sgraceduration, spt.nprocesstime,spt.ngracetime "
					+ " from storagesampleprocessing sp,samplecollectiontype sct,sampleprocesstype spt,collectiontubetype ct,"
					+ " projecttype pt,processtype protype,product p,period per,period per1 "
					+ "  where spt.nsampleprocesstypecode=sp.nsampleprocesstypecode and spt.ncollectiontubetypecode=sp.ncollectiontubetypecode and spt.nsamplecollectiontypecode=sp.nsamplecollectiontypecode and spt.nprocessperiodtime=per.nperiodcode and per1.nperiodcode=spt.ngraceperiodtime and sp.nsamplecollectiontypecode=sct.nsamplecollectiontypecode and "
					+ " spt.nsamplecollectiontypecode=sct.nsamplecollectiontypecode   and protype.nprocesstypecode=spt.nprocesstypecode  and sp.ncollectiontubetypecode=spt.ncollectiontubetypecode and  sp.ncollectiontubetypecode=spt.ncollectiontubetypecode "
					+ " and sp.ncollectiontubetypecode=ct.ncollectiontubetypecode and sct.nproductcode=p.nproductcode and sp.nprojecttypecode=pt.nprojecttypecode and spt.nprojecttypecode=pt.nprojecttypecode "

								+ " and ct.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and sp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and spt.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and protype.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and pt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and p.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and sct.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and per.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and per1.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and sp.nsitecode="	+ userInfo.getNtranssitecode()
								+ " and spt.nsitecode="	+ userInfo.getNmastersitecode()
								+ " and ct.nsitecode="	+ userInfo.getNmastersitecode()
								+ " and pt.nsitecode="	+ userInfo.getNmastersitecode()
								+ " and protype.nsitecode="	+ userInfo.getNmastersitecode()
								+ " and p.nsitecode="	+ userInfo.getNmastersitecode()
								+ " and sct.nsitecode="	+ userInfo.getNmastersitecode()
								//+ " and per.nsitecode="	+ userInfo.getNmastersitecode()
								//+ " and per1.nsitecode="	+ userInfo.getNmastersitecode()
								+ " and  nsampleprocessingcode="+ nsampleprocessingcode + "";

			final SampleProcessing lstSampleProcessing = jdbcTemplate.queryForObject(query, new SampleProcessing());

			final String barcodequery = getBarcodeFieldsByProjectType((int) inputMap.get("nprojecttypecode"), userInfo);

			List<Map<String, Object>> projectBarcodeConfig = jdbcTemplate.queryForList(barcodequery);
			Map<String, Object> map = new LinkedHashMap<>();
			Map<String, Object> map1 = new LinkedHashMap<>();
			map.put("sfieldname", "Quantity");
			map1.put("sfieldname", "Unit");
			map.put("nsorter", "11");
			map1.put("nsorter", "12");
			projectBarcodeConfig.add(map);
			projectBarcodeConfig.add(map1);
			outputMap.put("jsondataBarcodeFields", projectBarcodeConfig);
			outputMap.put("activeSampleProcessingByID", lstSampleProcessing);
			outputMap.put("sprocessstartdatesecondtime", false);

			if (lstSampleProcessing.getSprocessenddate() != null
					&& !lstSampleProcessing.getSprocessenddate().isEmpty()) {
				outputMap.put("sprocessstartdatesecondtime", true);
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
	 * This method is used to delete an entry in storagesampleprocessing table
	 * @param inputMap [Map] object with key of UserInfo object.
	 * @return ResponseEntity with string message as 'Already deleted' if the storagesampleprocessing record is not available/ 
	 * 			list of all storagesampleprocessing excluding the deleted record 
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> deleteSampleProcessing(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final List<Object> deleteSampleProcessing = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String activeQuery = getActiveSampleProcessing((int) inputMap.get("nsampleprocessingcode"), userInfo);
		final SampleProcessing activeSampleProcessing = (SampleProcessing) jdbcUtilityFunction
				.queryForObject(activeQuery, SampleProcessing.class, jdbcTemplate);
		if (activeSampleProcessing != null) {
			if (activeSampleProcessing.getSprocessenddate().isEmpty()) {

				TemporaryStorage objtemporaryStorage = getActiveTemporaryStoragebyProjectType(
						inputMap.get("sbarcodeid").toString(), (int) inputMap.get("nprojecttypecode"), userInfo);

				if (objtemporaryStorage == null) {
					final String auditQuery = sampleProcessignAudit((int) inputMap.get("nsampleprocessingcode"),
							userInfo);
					final SampleProcessing objSampleProcessingAuditAfter = (SampleProcessing) jdbcUtilityFunction
							.queryForObject(auditQuery, SampleProcessing.class, jdbcTemplate);
					deleteSampleProcessing.add(objSampleProcessingAuditAfter);
					multilingualIDList.add("IDS_DELETESAMPLEPROCESSING");

					auditUtilityFunction.fnInsertAuditAction(deleteSampleProcessing, 1, null, multilingualIDList,
							userInfo);

					final String deleteQuery = "update storagesampleprocessing set nstatus="
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " where nsampleprocessingcode="
							+ (int) inputMap.get("nsampleprocessingcode") + " and nsitecode="
							+ userInfo.getNtranssitecode() + "";
					jdbcTemplate.execute(deleteQuery);

					return getSampleProcessing(fromDate, toDate, null, userInfo,
							(int) inputMap.get("nprojecttypecode"));
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THISRECORDTEMPORARYSTORAGE",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_PROCESSCOMPLETED", userInfo.getSlanguagefilename()),
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
	 * This method is used to retrieve the list of all active process duration times for the specified barcode ID, site, and selected sample process type.
	 * @param inputMap  [Map] map object with "spositionvalue","nsampleprocesstypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all active process duration times for the specified barcode ID, site, and selected sample process type
	 * @throws Exception that are thrown from this DAO layer
	 */
	public String getProcessDurationTime(final Map<String, Object> inputMap, final UserInfo userinfo) {
		return "select sp.nprocesstime,sp.ngracetime, sp.nprocessperiodtime, ssp.nsampleprocessingcode, ssp.scomments, ssp.sdeviationcomments, "
				+ "COALESCE(TO_CHAR(ssp.dprocessstartdate, '" + userinfo.getSpgsitedatetime()
				+ "'), '') as sprocessstartdate, ssp.dprocessstartdate, "
				+ "ssp.dprocessenddate, COALESCE(TO_CHAR(ssp.dprocessenddate, '" + userinfo.getSpgsitedatetime()
				+ "'), '') as sprocessenddate from storagesampleprocessing ssp, sampleprocesstype sp "
				+ "where ssp.sbarcodeid = N'"
				+ stringUtilityFunction.replaceQuote(inputMap.get("spositionvalue").toString()) + "' "
				+ "and sp.nsampleprocesstypecode = ssp.nsampleprocesstypecode " + "and ssp.nsampleprocesstypecode = "
				+ inputMap.get("nsampleprocesstypecode") + " "
				+ " and ssp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ssp.nsitecode = "+ userinfo.getNtranssitecode()
				+ " and sp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sp.nsitecode = "+ userinfo.getNmastersitecode();
	}

	/**
	 * This method is used to retrieve the list of sample processing entries for the logged in site, and selected sample process type.
	 * @param inputMap  [Map] map object with "nsampleprocessingcode" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list  of sample processing entries for the logged in site, and selected sample process type
	 * @throws Exception that are thrown from this DAO layer
	 */
	private String sampleProcessignAudit(final int nsampleprocessingcode, final UserInfo userInfo) {
		return "select protype.nprocesstypecode,sp.nsampleprocesstypecode,protype.sprocesstypename,p.sproductname,sp.scomments,sp.sdeviationcomments,sp.nsampleprocessingcode,ct.stubename,pt.sprojecttypename,"
				+ " sp.sbarcodeid,sp.jsondata,COALESCE(TO_CHAR(sp.dprocessstartdate,'" + userInfo.getSpgsitedatetime()
				+ "'),'') as sprocessstartdate,COALESCE(TO_CHAR(sp.dprocessenddate,'" + ""
				+ userInfo.getSpgsitedatetime() + ""
				+ "'),'') as sprocessenddate,sp.nprojecttypecode,ct.ncollectiontubetypecode,CONCAT(spt.nprocesstime ,' ', coalesce(per.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',per.jsondata->'speriodname'->>'en-US')) as sprocessduration,spt.nprocesstime,CONCAT(spt.ngracetime ,' ', coalesce(per1.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "	per1.jsondata->'speriodname'->>'en-US')) as sgraceduration,spt.ngracetime "
				+ " from storagesampleprocessing sp,sampleprocesstype spt,collectiontubetype ct,projecttype pt,"
				+ " processtype protype,product p,period per,period per1,samplecollectiontype sct "
				+ " where spt.nsampleprocesstypecode=sp.nsampleprocesstypecode and per1.nperiodcode=spt.ngraceperiodtime and spt.ncollectiontubetypecode=sp.ncollectiontubetypecode and spt.nsamplecollectiontypecode=sp.nsamplecollectiontypecode and spt.nprocessperiodtime=per.nperiodcode and p.nproductcode=sct.nproductcode and sp.nsamplecollectiontypecode=sct.nsamplecollectiontypecode and "
				+ " spt.nsamplecollectiontypecode=sct.nsamplecollectiontypecode and protype.nprocesstypecode=spt.nprocesstypecode  and sp.ncollectiontubetypecode=spt.ncollectiontubetypecode and  sp.ncollectiontubetypecode=spt.ncollectiontubetypecode "
				+ " and sp.ncollectiontubetypecode=ct.ncollectiontubetypecode and  sp.nprojecttypecode=pt.nprojecttypecode and spt.nprojecttypecode=pt.nprojecttypecode  "
				+ " and  per.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ct.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and spt.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and protype.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sct.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and per.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and per1.nstatus="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sp.nsitecode="	+ userInfo.getNtranssitecode()
				+ " and spt.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and ct.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and pt.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and protype.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and p.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and sct.nsitecode="	+ userInfo.getNmastersitecode()
				//+ " and per.nsitecode="	+ userInfo.getNmastersitecode()
				//+ " and per1.nsitecode="	+ userInfo.getNmastersitecode()
				+ " and   nsampleprocessingcode="+ nsampleprocessingcode;
	}

	/**
	 * This method is used to retrieve the Barcode Length entries for the logged in site, and selected project type.
	 * @param inputMap  [Map] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list  of Barcode Length entries for the logged in site, and selected project type.
	 * @throws Exception that are thrown from this DAO layer
	 */
	public int getBarcodeLengthByProjecType(final int nprojecttypecode, final UserInfo userinfo) {
		int nbarcodelength = 0;
		final String stQuery = "select bc.nbarcodelength as nbarcodelength from bulkbarcodeconfig bc,"
				+ " bulkbarcodeconfigversion bcv,projecttype pt where bc.nbulkbarcodeconfigcode=bcv.nbulkbarcodeconfigcode"
				+ "	and  bc.nprojecttypecode=" + nprojecttypecode
				+ " and pt.nprojecttypecode=bc.nprojecttypecode and bcv.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and pt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bcv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bc.nsitecode="+ userinfo.getNmastersitecode()
				+ " and bcv.nsitecode="+ userinfo.getNmastersitecode()
				+ " and pt.nsitecode="+ userinfo.getNmastersitecode()+ "";
		final List<Map<String, Object>> listBulkBarcodeConfig = jdbcTemplate.queryForList(stQuery);

		if (!listBulkBarcodeConfig.isEmpty()) {
			nbarcodelength = Integer.parseInt(listBulkBarcodeConfig.get(0).get("nbarcodelength").toString());
		} else {
			nbarcodelength = 0;
		}
		return nbarcodelength;
	}
	/**
	 * This method is used to retrieve the Barcode Fields entries for the logged in site, and selected project type.
	 * @param inputMap  [Map] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list  of Barcode Fields entries for the logged in site, and selected project type.
	 * @throws Exception that are thrown from this DAO layer
	 */
	public String getBarcodeFieldsByProjectType(final int nprojecttypecode, final UserInfo userinfo) {
		return "SELECT  nsorter,jsondata->>'sfieldname' as sfieldname FROM  projecttype pt , bulkbarcodeconfigversion bcv,"
				+ "  bulkbarcodeconfig bc,bulkbarcodeconfigdetails bcd "
				+ "  where bcd.nbulkbarcodeconfigcode = bc.nbulkbarcodeconfigcode and bcd.nprojecttypecode = bc.nprojecttypecode "
				+ "  and  bc.nprojecttypecode = " + nprojecttypecode + "" + "  AND bcv.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ""
				+ "   AND  bc.nbulkbarcodeconfigcode = bcv.nbulkbarcodeconfigcode "
				+ "   AND  pt.nprojecttypecode = bc.nprojecttypecode "
				+ " and pt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bcv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bcd.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bc.nsitecode="+ userinfo.getNmastersitecode()
				+ " and bcv.nsitecode="+ userinfo.getNmastersitecode()
				+ " and pt.nsitecode="+ userinfo.getNmastersitecode()
				+ " and bcd.nsitecode="+ userinfo.getNmastersitecode()
				+ " order by nsorter;";
	}

	/**
	 * This method is used to retrieve the list of active process type entries  for the logged in site, and selected barcode id.
	 * @param inputMap  [Map] map object with "nprocesstypelength","listOfnsampleprocesstypecode","nsampleprocesstypecode","sbarcodeid" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list  of active process type entries  for the logged in site, and selected barcode id.
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Map<String, Object> getActiceProcessTypeByBarcodeId(final Map<String, Object> inputMap,
			final String sbarcodeid, UserInfo userInfo) {

		Map<String, Object> map = new HashMap<>();
		map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		final String listOfnsampleprocesstypecode = inputMap.get("listOfnsampleprocesstypecode").toString();
		final int nprocesstypelength = (int) inputMap.get("nprocesstypelength");

		String existsQuery = "select nsampleprocessingcode,scomments,sdeviationcomments,COALESCE(TO_CHAR(dprocessstartdate,'"
				+ userInfo.getSpgsitedatetime()
				+ "'),'') as sprocessstartdate,dprocessstartdate,dprocessenddate,COALESCE(TO_CHAR(dprocessenddate,'"
				+ userInfo.getSpgsitedatetime() + "'),'') as sprocessenddate from storagesampleprocessing where "
				+ " sbarcodeid=N'" + stringUtilityFunction.replaceQuote(sbarcodeid) + "' and nsampleprocesstypecode in("
				+ listOfnsampleprocesstypecode + ") " + " and dprocessenddate is not null and  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nsitecode="
				+ userInfo.getNtranssitecode() + "";
		final List<Map<String, Object>> existingProcesstype = jdbcTemplate.queryForList(existsQuery);

		if (nprocesstypelength != existingProcesstype.size() && !inputMap.containsKey("isSingleProcesstype")) {
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_COMPLETEPREVIOUSPROCESSTYPE");
		} else {
			final String nsampleprocesstypecode = inputMap.get("nsampleprocesstypecode").toString();
			existsQuery = "select nsampleprocessingcode,scomments,sdeviationcomments,COALESCE(TO_CHAR(dprocessstartdate,'"
					+ userInfo.getSpgsitedatetime()
					+ "'),'') as sprocessstartdate,dprocessstartdate,dprocessenddate,COALESCE(TO_CHAR(dprocessenddate,'"
					+ userInfo.getSpgsitedatetime() + "'),'') as sprocessenddate from storagesampleprocessing where "
					+ " sbarcodeid=N'" + stringUtilityFunction.replaceQuote(sbarcodeid)
					+ "' and nsampleprocesstypecode in(" + nsampleprocesstypecode + ") "
					+ " and dprocessenddate is not null and  nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nsitecode="
					+ userInfo.getNtranssitecode() + "";
			List<Map<String, Object>> existingProcesstype1 = jdbcTemplate.queryForList(existsQuery);
			if (existingProcesstype1.size() == 1) {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_BARCODEIDALREADYEXISTS");
			}
		}
		return map;
	}

	/**
	 * This method is used to retrieve the list of active temporary storage entries  for the logged in site, and selected project type and barcode id.
	 * @param inputMap  [Map] map object with "nprojecttypecode","sbarcodeid" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list  of active temporary storage entries  for the logged in site, and selected project type and barcode id.
	 * @throws Exception that are thrown from this DAO layer
	 */
	private TemporaryStorage getActiveTemporaryStoragebyProjectType(final String sbarcodeid, final int nprojecttypecode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = "select * from temporarystorage where sbarcodeid=N'" + sbarcodeid
				+ "' and nprojecttypecode=" + nprojecttypecode
				+ " and nsitecode=" + userInfo.getNtranssitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return (TemporaryStorage) jdbcUtilityFunction.queryForObject(strQuery, TemporaryStorage.class, jdbcTemplate);
	}
}
