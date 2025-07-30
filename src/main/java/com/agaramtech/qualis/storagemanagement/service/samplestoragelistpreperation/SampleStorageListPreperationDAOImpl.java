package com.agaramtech.qualis.storagemanagement.service.samplestoragelistpreperation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform only read operation on "samplestoragelocation" table by
 * implementing methods from its interface.
 *
 * @author ATE236
 * @version 10.0.0.2
 */
@AllArgsConstructor
@Repository
public class SampleStorageListPreperationDAOImpl implements SampleStorageListPreperationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleStorageListPreperationDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;

	/**
	 * This method is used to retrieve list of all available sample repository(s) with respect to site
	 * @param inputMap [Map] map object holding details to be fetched data from samplestoragelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleRepositories records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getsamplestoragelistpreperation(final Map<String, Object> inputMap,
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
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssl.nsitecode="
		+ userInfo.getNtranssitecode() + " and ssl.nsamplestoragelocationcode in "
		+ "(select nsamplestoragelocationcode from samplestorageversion where nstatus="
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalstatus="
		+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nsitecode="
		+ userInfo.getNtranssitecode() + ") and ts.ntranscode=ssl.nmappingtranscode " + conditonstr
		+ "  order by ssl.nsamplestoragelocationcode desc ";
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
		outputMap.put("ProjectType", list);

		strQuery = "select * from visitnumber where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("VisitNumber", list);

		strQuery = "select p.sprojecttypename,p.nprojecttypecode from projecttype p where p.nstatus=  "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();

		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("selectedProjectType", list.get(0));
		outputMap.put("projectbarcodeconfig", list);
		outputMap.put("nprojecttypecode", list.get(0).get("nprojecttypecode"));
		outputMap.putAll(getProjectbarcodeconfig(outputMap, userInfo).getBody());
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve list of all available sample repository(s) with respect to site and filter credentials
	 * @param inputMap [Map] map object holding details to read in bulkbarcodeconfigdetails table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleRepositories records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		List<Map<String, Object>> lst = new ArrayList<>();
		final String str = "SELECT p.sprojecttypename, " + "       pbc.nprojecttypecode, "
				+ "	   pbc.jsondata->>'sfieldname' as sfieldname " + "FROM   bulkbarcodeconfigdetails pbc, "
				+ "       projecttype p " + "WHERE  p.nprojecttypecode = pbc.nprojecttypecode "
				+ "       AND pbc.nprojecttypecode in (  " + inputMap.get("nprojecttypecode")
				+ "  )     AND p.nstatus =   " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "       AND pbc.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND p.nsitecode=" + userInfo.getNmastersitecode()
				+ " GROUP  BY  p.sprojecttypename,pbc.nprojecttypecode, " + "	   pbc.jsondata->>'sfieldname'  ";
		try {
			lst = jdbcTemplate.queryForList(str);
		} catch (final Exception e) {
			lst = new ArrayList<>();
		}
		if (lst.size() == 0) {
			inputMap.put("view_nprojecttypecode", 0);
		} else {
			// ALPD-4774-Vignesh R(31-08-2024)
			inputMap.put("view_nprojecttypecode",
					(int) inputMap.get("nprojecttypecode") > 3 ? 0 : inputMap.get("nprojecttypecode"));
		}
		if ((int) inputMap.get("nprojecttypecode") == -1) {
			inputMap.put("view_nprojecttypecode", 0);
		}

		System.out.println("JVM Bit size: " + System.getProperty("sun.arch.data.model"));
		String tableName = "";
		String getJSONKeysQuery = "";
		final Map<String, Object> returnObject = new HashMap<>();
		final String sourceName = (String) inputMap.get("source") + ((int) inputMap.get("view_nprojecttypecode"));
		String conditionString = inputMap.containsKey("conditionstring") ? (String) inputMap.get("conditionstring")
				: "";
		if (conditionString.isEmpty()) {
			conditionString = inputMap.containsKey("filterquery") ? "and " + (String) inputMap.get("filterquery") : "";
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
		if (fields!=null && fields.contains(inputMap.get("valuemember").toString())) {
			getJSONKeysQuery = "select " + tableName + ".* from " + tableName + " where nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and \""
					+ inputMap.get("valuemember") + "\" > '0' " + conditionString + " ;";
		} else {
			getJSONKeysQuery = "select  " + tableName + ".* from " + tableName + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + conditionString + " ;";
		}

		final List<Map<String, Object>> data = jdbcTemplate.queryForList(getJSONKeysQuery);
		returnObject.put((String) inputMap.get("label"), data);
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active sampleRepository Barcode object, based on the specified nprojecttypecode and spositionvalue.
	 * @param inputMap [Map] map object holding details to read in view_sampleretrieval_0 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity object holding response status and data of sampleRepository Barcode
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSelectedBarcodeData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnObject = new HashMap<>();
		Map<String, Object> object = new HashMap<>();

		// ALPD-4774-Vignesh R(31-08-2024)
		final String str = "select * from view_sampleretrieval_"
				+ (((int) inputMap.get("nprojecttypecode") == -1 || (int) inputMap.get("nprojecttypecode") > 3 ? 0
						: (int) inputMap.get("nprojecttypecode")))
				+ " where spositionvalue='" + inputMap.get("spositionvalue") + "';";
		try {
			object = jdbcTemplate.queryForMap(str);
		} catch (final Exception e) {
			object = new HashMap<>();
		}
		returnObject.put("selectedBarcodeValue", object);
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	/**
	 * This method is used to get all the available sampleRepositories for the specified nprojecttypecode and site.
	 * @param inputMap [Map] map object holding details to read in bulkbarcodeconfigdetails table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleRepositories records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getProjectbarcodeconfig(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnObject = new HashMap<>();
		List<Map<String, Object>> lst = new ArrayList<>();

		final String str = " select bbcd.nprojecttypecode,pt.sprojecttypename,bbcd.jsondata->>'sfieldname' as sfieldname from bulkbarcodeconfig bbc, bulkbarcodeconfigversion bbcv,"
				+ " bulkbarcodeconfigdetails bbcd ,projecttype pt where bbc.nbulkbarcodeconfigcode=bbcv.nbulkbarcodeconfigcode and "
				+ " bbcd.nbulkbarcodeconfigcode=bbc.nbulkbarcodeconfigcode and pt.nprojecttypecode = bbc.nprojecttypecode and bbcd.nprojecttypecode in ("
				+ inputMap.get("nprojecttypecode") + ")" + " and bbcv.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and bbc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbcv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbcd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbc.nsitecode="
				+ userInfo.getNmastersitecode() + " and bbcv.nsitecode=" + userInfo.getNmastersitecode()
				+ " and bbcd.nsitecode=" + userInfo.getNmastersitecode() + " and pt.nsitecode="
				+ userInfo.getNmastersitecode() + " group by bbcd.nprojecttypecode,pt.sprojecttypename,"
				+ " bbcd.jsondata->>'sfieldname',bbcd.nsorter order by bbcd.nsorter desc ";
		try {
			lst = jdbcTemplate.queryForList(str);
		} catch (final Exception e) {
			lst = new ArrayList<>();
		}
		returnObject.put("selectedProjectTypeList", lst);
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve sample storage details based on imported
	 * excel. Here the imported excel sheet is iterated to get the sampleIDs as
	 * String ("filterquery") with conditions added to it and is put into inputMap
	 * [Map] map with keys
	 * "source","label","valuemember","nprojecttypecode" and "filterquery". 
	 * This inputMap and userInfo is then sent to getDynamicFilterExecuteData() in the
	 * same DAOImpl class to get the stored sample details.
	 *
	 * @param request [MultipartHttpServletRequest] multipart request with
	 *                parameters:
	 *                ImportFile,nformcode,userinfo,nprojecttypecode,source,fieldName,label,valuemember
	 * @return response object with list of sample storage details based on imported
	 *         excel.
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getImportSampleIDData(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> inputMap = new HashMap<>();
		final String source = StringEscapeUtils.unescapeJava(request.getParameter("source")).toString();
		final String label = StringEscapeUtils.unescapeJava(request.getParameter("label")).toString();
		final String valuemember = StringEscapeUtils.unescapeJava(request.getParameter("valuemember")).toString();
		final int nprojecttypecode = Integer.parseInt(request.getParameter("nprojecttypecode"));
		inputMap.put("source", source);
		inputMap.put("label", label);
		inputMap.put("valuemember", valuemember);
		inputMap.put("nprojecttypecode", nprojecttypecode);

		final MultipartFile multipartFile = request.getFile("ImportFile");
		final String fieldName = StringEscapeUtils.unescapeJava(request.getParameter("fieldName")).toString();
		final InputStream ins = multipartFile.getInputStream();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024];
		int len;
		while ((len = ins.read(buffer)) > -1) {
			baos.write(buffer, 0, len);
		}
		baos.flush();
		try {
			final InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
			final Workbook workbook = WorkbookFactory.create(is1);
			final Sheet sheet = workbook.getSheetAt(0);

			int rowIndex = 0;
			final List<String> lstHeader = new ArrayList<>();
			final StringJoiner positionValue = new StringJoiner(",");
			int cellIndex = 0;
			for (final Row row : sheet) {
				if (rowIndex > 0) {
					if (!lstHeader.isEmpty()) {
						for (int i = 0; i < lstHeader.size(); i++) {
							if (row.getCell(cellIndex) != null) {
								LOGGER.info(row.getCell(cellIndex).toString().trim());
								final Cell cell = row.getCell(cellIndex);
								positionValue.add("'" + cell.toString().trim() + "'");
							} else {
								positionValue.add("''");
							}
						}
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVALIDTEMPLATEHEADERS",
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				} else {

					for (final Cell cell : row) // iteration over cell using for each loop
					{
						final String header = cell.getStringCellValue();
						if (header.trim().equals(fieldName.trim())) {
							lstHeader.add(header);
							break;
						}
						cellIndex++;
					}
				}
				rowIndex++;
			}
			final String spositionvalue = positionValue.toString();
			final String filterquery = " nprojecttypecode=" + nprojecttypecode + " and spositionvalue in ("
					+ spositionvalue + ")";
			inputMap.put("filterquery", filterquery);

			final Map<String, Object> returnObject = getDynamicFilterExecuteData(inputMap, userInfo).getBody();

			final List<Map<String, Object>> data = (List<Map<String, Object>>) returnObject.get(inputMap.get("label"));
			if (!data.isEmpty()) {
				return new ResponseEntity<>(returnObject, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						"IDS_NORECORDSFOUNDFORGIVENPROJECTTYPE", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}

		} catch (final Exception e) {
			e.printStackTrace();
			LOGGER.error("Error: upload file not proper file." + e);

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_INVALIDFILE", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

}
