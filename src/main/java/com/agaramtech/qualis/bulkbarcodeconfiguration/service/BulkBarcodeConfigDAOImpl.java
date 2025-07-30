package com.agaramtech.qualis.bulkbarcodeconfiguration.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.BulkBarcodeConfigDetails;
import com.agaramtech.qualis.bulkbarcodeconfiguration.model.BulkBarcodeConfig;
import com.agaramtech.qualis.bulkbarcodeconfiguration.model.BulkBarcodeConfigVersion;
import com.agaramtech.qualis.bulkbarcodeconfiguration.model.BulkBarcodeMaster;
import com.agaramtech.qualis.dashboard.model.QueryBuilderTableColumns;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.model.ProjectType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on 'bulkbarcodeconfig','bulkbarcodeconfigdetails' table by 
 * implementing methods from its interface. 
 */
@AllArgsConstructor
@Repository
public class BulkBarcodeConfigDAOImpl implements BulkBarcodeConfigDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkBarcodeConfigDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to get all the available bulkbarcodeconfig with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of bulkbarcodeconfig records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getBulkBarcodeConfiguration(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final List<ProjectType> projectTypeList = (List<ProjectType>) getProjectType(userInfo).getBody();
		outputMap.put("ProjectType", projectTypeList);
		outputMap.put("SelectedProjectType", projectTypeList.get(projectTypeList.size() - 1));
		LOGGER.info("getBulkBarcodeConfiguration() called");
		if (!projectTypeList.isEmpty()) {
			int nprojecttypecode = 0;
			nprojecttypecode = projectTypeList.get(projectTypeList.size() - 1).getNprojecttypecode();

			final List<BulkBarcodeConfig> lstBulkBarcodeConfig = (List<BulkBarcodeConfig>) getBulkBarcodeConfigbyProjectType(
					nprojecttypecode, userInfo).getBody();

			if (!lstBulkBarcodeConfig.isEmpty()) {
				outputMap.put("BulkBarcodeConfig", lstBulkBarcodeConfig);
				outputMap.put("selectedBulkBarcodeConfig", lstBulkBarcodeConfig.get(lstBulkBarcodeConfig.size() - 1));
				int nbulkbarcodeconfigcode = lstBulkBarcodeConfig.get(lstBulkBarcodeConfig.size() - 1)
						.getNbulkbarcodeconfigcode();

				outputMap.putAll((Map<String, Object>) getBulkBarcodeConfigDetailsbyBulkBarcodeConfig(nprojecttypecode,
						nbulkbarcodeconfigcode, userInfo, 1).getBody());
			} else {
				outputMap.put("BulkBarcodeConfig", lstBulkBarcodeConfig);
				outputMap.put("selectedBulkBarcodeConfig", null);
			}
		} else {
			outputMap.put("selectedBulkBarcodeConfig", null);
			outputMap.put("selectedBulkBarcodeConfigVersion", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to get all the available barcodemaster entries
	 * @param nprojecttypecode [int] Project Type Code.
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of barcodemaster records 
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getBarcodeMaster(final UserInfo userInfo, final int nbulkbarcodeconfigcode)
			throws Exception {

		// ALPD-4702--Vignesh R(21-08-2024)
		final BulkBarcodeConfig isActiveBulkBarcodeConfig = getActiveBulkBarcodeConfigurationById(
				nbulkbarcodeconfigcode, userInfo);

		if (isActiveBulkBarcodeConfig != null) {

			final Map<String, Object> outputMap = new LinkedHashMap<>();

			final String bulkBarcodeMasterQuery = "select  coalesce(q.jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode() + "',q.jsondata->'sdisplayname'->>'en-US') as "
					+ " sformname,bm.*  from barcodemaster bm, qualisforms q where "
					+ " bm.nformcode=q.nformcode and bm.nsitecode=" + userInfo.getNmastersitecode()
					+ " and bm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and q.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			
			final List<BulkBarcodeMaster> bulkBarcodeMasterList = (List<BulkBarcodeMaster>) jdbcTemplate
					.query(bulkBarcodeMasterQuery, new BulkBarcodeMaster());

			outputMap.put("BulkBarcodeMaster", bulkBarcodeMasterList);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to add a new entry to bulkbarcodeconfig  table.
	 * Need to check for duplicate entry of configuration name for the selected project type.	 
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfig [BulkBarcodeConfig]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of added bulkbarcodeconfig object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> createBulkBarcodeConfiguration(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table lockbarcodeformatting "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<Object> savedList = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();
		final BulkBarcodeConfig objBulkBarcodeConfig = objmapper.convertValue(inputMap.get("bulkbarcodeconfig"),
				BulkBarcodeConfig.class);

		final BulkBarcodeConfig bulkBarcodeConfigListByName = getBulkBarcodeConfigByName(
				objBulkBarcodeConfig.getSconfigname(), objBulkBarcodeConfig.getNprojecttypecode(), userInfo);

		if (bulkBarcodeConfigListByName == null) {
			int nSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobarcode where stablename='bulkbarcodeconfig' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			nSeqNo++;

			int nversionSeqNo = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobarcode where stablename='bulkbarcodeconfigversion' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			nversionSeqNo++;

			String barcodeConfigInsert = "insert into bulkbarcodeconfig(nbulkbarcodeconfigcode, nprojecttypecode, sconfigname, "
					+ " nbarcodelength, sdescription,dmodifieddate, nsitecode, nstatus)" + " values(" + nSeqNo + ","
					+ objBulkBarcodeConfig.getNprojecttypecode() + " ,N'"
					+ stringUtilityFunction.replaceQuote(objBulkBarcodeConfig.getSconfigname()) + "',"
					+ objBulkBarcodeConfig.getNbarcodelength() + "," + " N'"
					+ stringUtilityFunction.replaceQuote(objBulkBarcodeConfig.getSdescription()) + "','"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			String barcodeConfigVersionInsert = "insert into bulkbarcodeconfigversion(nbulkbarcodeconfigversioncode, nbulkbarcodeconfigcode,"
					+ " sversionno, ntransactionstatus, dmodifieddate, nsitecode, nstatus)" + " values(" + nversionSeqNo
					+ "," + nSeqNo + ",'-'," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			String updateseqNo = "update seqnobarcode set nsequenceno = " + nSeqNo
					+ " where stablename='bulkbarcodeconfig' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			String updateVersionSeqNo = "update seqnobarcode set nsequenceno = " + nversionSeqNo
					+ " where stablename='bulkbarcodeconfigversion' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(barcodeConfigInsert + barcodeConfigVersionInsert + updateseqNo + updateVersionSeqNo);
			String query = "select * from bulkbarcodeconfigversion where nbulkbarcodeconfigcode=" + nSeqNo;
			BulkBarcodeConfigVersion objBulkBarcodeConfigVersion = (BulkBarcodeConfigVersion) jdbcUtilityFunction
					.queryForObject(query, BulkBarcodeConfigVersion.class, jdbcTemplate);
			objBulkBarcodeConfig.setNbulkbarcodeconfigcode(nSeqNo);
			objBulkBarcodeConfig.setNbulkbarcodeconfigversioncode(nversionSeqNo);
			List<BulkBarcodeConfig> insconig = new ArrayList<BulkBarcodeConfig>();
			insconig.add(objBulkBarcodeConfig);
			List<BulkBarcodeConfigVersion> ins = new ArrayList<BulkBarcodeConfigVersion>();
			ins.add(objBulkBarcodeConfigVersion);
			savedList.add(insconig);
			savedList.add(ins);
			auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null,
					Arrays.asList("IDS_ADDBARCODEFORMATTING", "IDS_ADDBARCODEFORMATTINGVERSION"), userInfo);
			return getBulkBarcodeConfigAfterCreate(objBulkBarcodeConfig.getNprojecttypecode(), userInfo);
		} else {
			// Conflict = 409 - Duplicate entry
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}
	/**
	 *  This method is used to retrieve a list of active  bulkbarcodeconfig record for the selected project type.
	 * @param inputMap  [Map] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with bulkbarcodeconfig object for the selected nbulkbarcodeconfig.
	 * @throws Exception that are thrown from this DAO layer.
	 */
	public ResponseEntity<Object> getBulkBarcodeConfigAfterCreate(final int nprojecttypecode,
			final UserInfo objUserInfo) throws Exception {

		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final String projectTypequery = "select * from projecttype where nprojecttypecode=" + nprojecttypecode
				+ " and nprojecttypecode >0  and nsitecode=" + objUserInfo.getNmastersitecode() + "  and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final ProjectType projectType = jdbcTemplate.queryForObject(projectTypequery, new ProjectType());
		final List<ProjectType> projectTypeList = (List<ProjectType>) getProjectType(objUserInfo).getBody();
		map.put("ProjectType", projectTypeList);
		map.put("SelectedProjectType", projectType);

		final List<BulkBarcodeConfig> lstBulkBarcodeConfig = (List<BulkBarcodeConfig>) getBulkBarcodeConfigbyProjectType(
				nprojecttypecode, objUserInfo).getBody();
		map.put("BulkBarcodeConfig", lstBulkBarcodeConfig);
		if (!lstBulkBarcodeConfig.isEmpty()) {
			map.put("selectedBulkBarcodeConfig", lstBulkBarcodeConfig.get(lstBulkBarcodeConfig.size() - 1));
			map.putAll((Map<String, Object>) getBulkBarcodeConfigDetailsbyBulkBarcodeConfig(nprojecttypecode,
					lstBulkBarcodeConfig.get(lstBulkBarcodeConfig.size() - 1).getNbulkbarcodeconfigcode(), objUserInfo,
					1).getBody());
		} else {
			map.put("selectedBulkBarcodeConfig", null);
			map.put("BarcodeDetails", lstBulkBarcodeConfig);

		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the BulkBarcodeConfig object for the specified configuration name  and project type.
	 * @param sconfigname [String] name of the configuration name
	 * @param nprojecttypecode [int] name of the project type
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched	
	 * @return BulkBarcodeConfig object based on the specified configuration name  and project type.
	 * @throws Exception that are thrown from this DAO layer
	 */
	private BulkBarcodeConfig getBulkBarcodeConfigByName(final String sconfigname, final int nprojecttypecode,
			UserInfo userInfo) throws Exception {
		final String strQuery = "select  sconfigname from bulkbarcodeconfig where sconfigname = N'"
				+ stringUtilityFunction.replaceQuote(sconfigname) + "' and nprojecttypecode=" + nprojecttypecode
				+ " and nsitecode=" + userInfo.getNmastersitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (BulkBarcodeConfig) jdbcUtilityFunction.queryForObject(strQuery, BulkBarcodeConfig.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in bulkbarcodeconfig  table.
	 * Need to check for duplicate entry of configuration name for the selected project type.	 
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfig [BulkBarcodeConfig]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details.

	 * @return response entity object holding response status and data of updated bulkbarcodeconfig object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateBulkBarcodeConfiguration(BulkBarcodeConfig objBulkBarcodeConfig,
			final UserInfo userInfo) throws Exception {

		final BulkBarcodeConfig isActiveBulkBarcodeConfig = getActiveBulkBarcodeConfigurationById(
				objBulkBarcodeConfig.getNbulkbarcodeconfigcode(), userInfo);

		if (isActiveBulkBarcodeConfig != null) {
			// ALPD-4731--Vignesh R(27-08-2024)
			final String queryString = "select nbulkbarcodeconfigcode from bulkbarcodeconfig where sconfigname = N'"
					+ stringUtilityFunction.replaceQuote(objBulkBarcodeConfig.getSconfigname())
					+ "' and nbulkbarcodeconfigcode <> " + objBulkBarcodeConfig.getNbulkbarcodeconfigcode()
					+ " and nprojecttypecode=" + objBulkBarcodeConfig.getNprojecttypecode() + " and  nsitecode="
					+ userInfo.getNmastersitecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<BulkBarcodeConfig> bulkBarcodeConfigListByName = jdbcTemplate.query(queryString,
					new BulkBarcodeConfig());

			if (bulkBarcodeConfigListByName.isEmpty()) {
				final String updateQueryString = "update bulkbarcodeconfig set nprojecttypecode="
						+ objBulkBarcodeConfig.getNprojecttypecode() + ",sconfigname=N'"
						+ stringUtilityFunction.replaceQuote(objBulkBarcodeConfig.getSconfigname()) + "'"
						+ ",nbarcodelength=" + objBulkBarcodeConfig.getNbarcodelength() + ",sdescription='"
						+ stringUtilityFunction.replaceQuote(objBulkBarcodeConfig.getSdescription()) + "'"
						+ " ,dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ " where nbulkbarcodeconfigcode=" + objBulkBarcodeConfig.getNbulkbarcodeconfigcode();
				jdbcTemplate.execute(updateQueryString);

				final List<String> multilingualIDList = new ArrayList<>();

				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(isActiveBulkBarcodeConfig);

				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(objBulkBarcodeConfig);
				multilingualIDList.add("IDS_EDITBARCODEFORMATTING");

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);

				return getBulkBarcodeConfig(objBulkBarcodeConfig.getNbulkbarcodeconfigcode(),
						objBulkBarcodeConfig.getNprojecttypecode(), userInfo);

			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to delete an entry in bulkbarcodeconfig  table.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfig [BulkBarcodeConfig]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of deleted bulkbarcodeconfig object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteBulkBarcodeConfiguration(BulkBarcodeConfig objBulkBarcodeConfig,
			final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> AuditActionList = new ArrayList<>();
		final List<Object> deletedList = new ArrayList<>();

		final BulkBarcodeConfig isActiveBulkBarcodeConfig = getActiveBulkBarcodeConfigurationById(
				objBulkBarcodeConfig.getNbulkbarcodeconfigcode(), userInfo);

		if (isActiveBulkBarcodeConfig != null) {

			String strCategory = "select * from bulkbarcodeconfigdetails where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nprojecttypecode="
					+ objBulkBarcodeConfig.getNprojecttypecode() + " and nsitecode=" + userInfo.getNmastersitecode()
					+ " and " + " nbulkbarcodeconfigcode=" + objBulkBarcodeConfig.getNbulkbarcodeconfigcode();
			List<BulkBarcodeConfigDetails> lstBulkBarcodeConfigDetails = (List<BulkBarcodeConfigDetails>) jdbcTemplate
					.query(strCategory, new BulkBarcodeConfigDetails());
			String updateQuery = "";
			updateQuery = "update bulkbarcodeconfig set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nprojecttypecode="
					+ objBulkBarcodeConfig.getNprojecttypecode() + " and nbulkbarcodeconfigcode="
					+ objBulkBarcodeConfig.getNbulkbarcodeconfigcode() + ";";

			updateQuery = updateQuery + "update bulkbarcodeconfigdetails set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nprojecttypecode="
					+ objBulkBarcodeConfig.getNprojecttypecode() + " and nbulkbarcodeconfigcode="
					+ objBulkBarcodeConfig.getNbulkbarcodeconfigcode() + ";";
			jdbcTemplate.execute(updateQuery);

			objBulkBarcodeConfig.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

			deletedList.add(objBulkBarcodeConfig);
			AuditActionList.add(deletedList);
			multilingualIDList.add("IDS_DELETEBARCODEFORMATTING");
			AuditActionList.add(lstBulkBarcodeConfigDetails);
			multilingualIDList.add("IDS_DELETEBULKBARCODEDETAILS");

			auditUtilityFunction.fnInsertListAuditAction(AuditActionList, 1, null, multilingualIDList, userInfo);
			return getBulkBarcodeConfigAfterCreate(objBulkBarcodeConfig.getNprojecttypecode(), userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve active bulkbarcodeconfig object 
	 * @param inputMap  [Map] map object with "nbulkbarcodeconfigcode" and "userInfo" as keys for which the data is to be fetched
	 * @return response entity  object holding response status and data of bulkbarcodeconfig object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public BulkBarcodeConfig getActiveBulkBarcodeConfigurationById(final int nbulkbarcodeconfigcode,
			final UserInfo userInfo) throws Exception {

		final String strQuery = "select  bb.*,pt.sprojecttypename,bv.nbulkbarcodeconfigversioncode,bv.sversionno,bv.ntransactionstatus,"
				+ " ts.stransstatus as stransdisplaystatus  from bulkbarcodeconfigversion bv,bulkbarcodeconfig bb,transactionstatus ts,projecttype pt "
				+ " where bb.nbulkbarcodeconfigcode=" + nbulkbarcodeconfigcode + " and"
				+ " bb.nprojecttypecode=pt.nprojecttypecode and bv.nbulkbarcodeconfigcode=bb.nbulkbarcodeconfigcode"
				+ "	and ts.ntranscode=bv.ntransactionstatus and bv.nsitecode=" + userInfo.getNmastersitecode()
				+ " and bb.nsitecode=" + userInfo.getNmastersitecode() + " and bv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and bb.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nsitecode="
				+ userInfo.getNmastersitecode();

		return (BulkBarcodeConfig) jdbcUtilityFunction.queryForObject(strQuery, BulkBarcodeConfig.class, jdbcTemplate);
	}

	/**
	 * This method is used to updated the field position in the bulkbarcodeconfigdetails table for the  selected bulkbarcodeconfig.
	 * @param inputMap  [Map] map object with "objProjectBarcodeConfig" and "userInfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with bulkbarcodeconfigdetails object.	 
	 * @throws Exception exception
	 */	
	public String updateFieldPosition(BulkBarcodeConfig objProjectBarcodeConfig, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedbeforeList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final String query = "select * from bulkbarcodeconfigdetails where nprojecttypecode="
				+ objProjectBarcodeConfig.getNprojecttypecode() + " and " + " nbulkbarcodeconfigcode="
				+ objProjectBarcodeConfig.getNbulkbarcodeconfigcode() + " " + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by  nsorter";

		List<BulkBarcodeConfigDetails> barcodeDetailsList = (List<BulkBarcodeConfigDetails>) jdbcTemplate.query(query,
				new BulkBarcodeConfigDetails());
		int nfieldstartposition = 0;
		int prevFieldLength = 0;
		int PrevStartPostion = 0;
		final StringBuilder insertQuery = new StringBuilder();
		if (!barcodeDetailsList.isEmpty()) {
			for (BulkBarcodeConfigDetails objBarcodeConfigdetails : barcodeDetailsList) {
				int nsorter = objBarcodeConfigdetails.getNsorter();
				int nfieldlength = objBarcodeConfigdetails.getNfieldlength();
				if (nsorter != 1) {
					nfieldstartposition = PrevStartPostion + prevFieldLength;
				}
				PrevStartPostion = nfieldstartposition;
				prevFieldLength = nfieldlength;
				String updateQuery = "update bulkbarcodeconfigdetails set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ,nfieldstartposition="
						+ nfieldstartposition + " where " + " nprojecttypecode="
						+ objBarcodeConfigdetails.getNprojecttypecode() + " and nbulkbarcodeconfigcode="
						+ objBarcodeConfigdetails.getNbulkbarcodeconfigcode() + " and nbulkbarcodeconfigdetailscode="
						+ objBarcodeConfigdetails.getNbulkbarcodeconfigdetailscode() + ";";
				insertQuery.append(updateQuery);
			}
			jdbcTemplate.execute(insertQuery.substring(0, insertQuery.length() - 1));
		}

		final String queryAfter = "select * from bulkbarcodeconfigdetails where nprojecttypecode="
				+ objProjectBarcodeConfig.getNprojecttypecode() + " and " + " nbulkbarcodeconfigcode="
				+ objProjectBarcodeConfig.getNbulkbarcodeconfigcode() + " " + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by  nsorter";

		List<BulkBarcodeConfigDetails> barcodeDetailsAfterList = (List<BulkBarcodeConfigDetails>) jdbcTemplate
				.query(queryAfter, new BulkBarcodeConfigDetails());
		multilingualIDList.add("IDS_EDITBULKBARCODEDETAILS");
		savedbeforeList.add(barcodeDetailsList);
		listAfterSave.add(barcodeDetailsAfterList);
		auditUtilityFunction.fnInsertListAuditAction(listAfterSave, 2, savedbeforeList, multilingualIDList, userInfo);
		return Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
	}

	/**
	 * This method is used to update selected bulkbarcodeconfigversion table.
	 * @param inputMap  [Map] map object with "bulkbarcodeconfig" and "userInfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with string message as 'Already Deleted' if the bulkbarcodeconfig record is not available/ 
	 * list of all bulkbarcodeconfig and along with the updated bulkbarcodeconfig and bulkbarcodeconfigversion entries.	 
	 * @throws Exception exception
	 */	
	@Override
	public ResponseEntity<Object> approveBulkBarcodeConfiguration(BulkBarcodeConfig objBulkBarcodeConfig,
			final UserInfo userInfo) throws Exception {

		final BulkBarcodeConfig isActiveBulkBarcodeConfig = getActiveBulkBarcodeConfigurationById(
				objBulkBarcodeConfig.getNbulkbarcodeconfigcode(), userInfo);

		if (isActiveBulkBarcodeConfig != null) {
			int fieldLenth = jdbcTemplate.queryForObject(
					"select COALESCE( sum(nfieldlength)::int,0) from bulkbarcodeconfigdetails "
							+ " where nbulkbarcodeconfigcode=" + objBulkBarcodeConfig.getNbulkbarcodeconfigcode()
							+ " and " + " nprojecttypecode=" + objBulkBarcodeConfig.getNprojecttypecode()
							+ " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			if (objBulkBarcodeConfig.getNbarcodelength() == fieldLenth) {

				// ATE-234 janakumar r ALPD-4560 Barcode Formatting -> While approve
				// project-based barcode length validation.
				List<Map<String, Object>> value = null;
				final String queryCheck = "select * from bulkbarcodeconfigdetails  where" + " nbulkbarcodeconfigcode="
						+ objBulkBarcodeConfig.getNbulkbarcodeconfigcode() + " and stablename!='' " + " and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode() + " order by nsorter ;";

				final List<BulkBarcodeConfigDetails> barcodelist = jdbcTemplate.query(queryCheck,
						new BulkBarcodeConfigDetails());

				final List<String> exceedScreen = new ArrayList<String>();
				final List<String> noRecordScreen = new ArrayList<String>();
				for (int i = 0; i < barcodelist.size(); i++) { // this for loop itrate the barcode entrie fields.

					final String stablename = barcodelist.get(i).getStablename();
					final String stablecoumnname = barcodelist.get(i).getStablecolumnname();
					final int presentFieldLength = barcodelist.get(i).getNfieldlength();
					final String screenName = barcodelist.get(i).getJsondata().get("sformname").toString();

					final String querysValue = "select max(" + stablecoumnname + ") as codeValue from  " + stablename
							+ " where  nprojecttypecode=" + objBulkBarcodeConfig.getNprojecttypecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc;";

					try {
						value = (List<Map<String, Object>>) jdbcTemplate.queryForList(querysValue);

						if (value.get(0).get("codeValue") != null) {
							final String nCodeValueObj = (String) value.get(0).get("codeValue").toString();
							final int screenFieldLength = nCodeValueObj.length();

							if (screenFieldLength == presentFieldLength) {

							} else {
								exceedScreen.add(screenName);
							}
						} else {
							noRecordScreen.add(screenName);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

				if (exceedScreen.isEmpty() && noRecordScreen.isEmpty()) {

					updateFieldPosition(objBulkBarcodeConfig, userInfo);

					int count = 0;

					String versionCount = "select max(sversionno) sversionno from bulkbarcodeconfigversion bv,bulkbarcodeconfig bb where bv.nbulkbarcodeconfigcode=bb.nbulkbarcodeconfigcode "
							+ " and bb.nprojecttypecode=" + objBulkBarcodeConfig.getNprojecttypecode() + " "
							+ " and bb.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and bb.nsitecode=" + userInfo.getNmastersitecode() + " group by bb.nprojecttypecode ";
					
					String versionCountString = jdbcTemplate.queryForObject(versionCount, String.class);
					
					if (versionCountString.equals("-")) {
						count = 1;
					} else {
						count = Integer.valueOf(versionCountString) + 1;
					}
					
					String updateQueryString = "";
					String query = " select * from bulkbarcodeconfig bb,bulkbarcodeconfigversion bv where "
							+ " bb.nbulkbarcodeconfigcode=bv.nbulkbarcodeconfigcode and bb.nprojecttypecode="
							+ objBulkBarcodeConfig.getNprojecttypecode() + " and bv.ntransactionstatus ="

							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and bv.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bv.nsitecode="
							+ userInfo.getNmastersitecode() + " and bb.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bb.nsitecode="
							+ userInfo.getNmastersitecode();

					final List<BulkBarcodeConfig> bulkBarcodeConfigList = (List<BulkBarcodeConfig>) jdbcTemplate
							.query(query, new BulkBarcodeConfig());
					if (!bulkBarcodeConfigList.isEmpty()) {
						updateQueryString = "update bulkbarcodeconfigversion set ntransactionstatus="
								+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where  nbulkbarcodeconfigcode="
								+ bulkBarcodeConfigList.get(0).getNbulkbarcodeconfigcode() + ";";
					}

					updateQueryString = updateQueryString + "update bulkbarcodeconfigversion set sversionno='" + count
							+ "',ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							+ "," + " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "' where nbulkbarcodeconfigcode=" + objBulkBarcodeConfig.getNbulkbarcodeconfigcode()
							+ ";";
					jdbcTemplate.execute(updateQueryString);

					String querys = "select * from bulkbarcodeconfigversion where " + "  nsitecode="
							+ userInfo.getNmastersitecode() + " "
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and  nbulkbarcodeconfigcode="
							+ objBulkBarcodeConfig.getNbulkbarcodeconfigcode();
					BulkBarcodeConfigVersion objBulkBarcodeConfigVersion = (BulkBarcodeConfigVersion) jdbcUtilityFunction
							.queryForObject(querys, BulkBarcodeConfigVersion.class, jdbcTemplate);

					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> savedList = new ArrayList<>();
					savedList.add(objBulkBarcodeConfigVersion);
					multilingualIDList.add("IDS_APPROVEBARCODEFORMATTING");
					auditUtilityFunction.fnInsertAuditAction(savedList, 2, Arrays.asList(isActiveBulkBarcodeConfig),
							multilingualIDList, userInfo);
					return getBulkBarcodeConfig(objBulkBarcodeConfig.getNbulkbarcodeconfigcode(),
							objBulkBarcodeConfig.getNprojecttypecode(), userInfo);

				} else {
					final String alertExceedScreen = exceedScreen.stream().collect(Collectors.joining(","));
					final String alertNoRecordScreen = noRecordScreen.stream().collect(Collectors.joining(","));

					if (!noRecordScreen.isEmpty()) {
						return new ResponseEntity<Object>(
								commonFunction.getMultilingualMessage("IDS_NORECORDIINSCREEEN",
										userInfo.getSlanguagefilename()) + " " + alertNoRecordScreen,
								HttpStatus.EXPECTATION_FAILED);
					} else if (!exceedScreen.isEmpty()) {
						return new ResponseEntity<Object>(
								commonFunction.getMultilingualMessage("IDS_FIELDLENGTHINSCREEN",
										userInfo.getSlanguagefilename()) + " " + alertExceedScreen,
								HttpStatus.EXPECTATION_FAILED);
					}

					return new ResponseEntity<Object>(
							commonFunction.getMultilingualMessage("IDS_SCREENNAME", userInfo.getSlanguagefilename())
									+ " " + alertExceedScreen + alertNoRecordScreen,
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_BARCODEFIELDLENGTHEXCEEDSMAXLENGTH",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve for all list of active bulkbarcodeconfig  entries for the selected project type.
	 * @param inputMap  [inputMap] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * 								nprojecttypecode [int] ,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity  with list of active  bulkbarcodeconfig object for the selected project type.
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<List<BulkBarcodeConfig>> getBulkBarcodeConfigbyProjectType(int nprojecttypecode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = " select bb.*,bv.nbulkbarcodeconfigversioncode,bv.sversionno,bv.ntransactionstatus, "
				+ " ts.stransstatus as stransdisplaystatus "
				+ " from bulkbarcodeconfigversion bv,bulkbarcodeconfig bb,transactionstatus ts "
				+ " where bv.nbulkbarcodeconfigcode=bb.nbulkbarcodeconfigcode"
				+ " and ts.ntranscode=bv.ntransactionstatus" + " and bb.nprojecttypecode=" + nprojecttypecode
				+ " and bb.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bb.nsitecode=" + userInfo.getNmastersitecode() + " " + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bv.nsitecode="
				+ userInfo.getNmastersitecode() + " order by nbulkbarcodeconfigcode ";
		List<BulkBarcodeConfig> lstBulkBarcodeConfig = jdbcTemplate.query(strQuery, new BulkBarcodeConfig());
		return new ResponseEntity<>(lstBulkBarcodeConfig, HttpStatus.OK);
	}
	
	/**
	 * This method is used to retrieve for all list of active project type  entries for the logged in site.
	 * @param inputMap  [inputMap] map object with "userinfo" as keys for which the data is to be fetched
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity  with list of active  project type object for the logged in site.
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<List<ProjectType>> getProjectType(final UserInfo userInfo) throws Exception {
		final String projectTypeQuery = "select * from projecttype where  nprojecttypecode >0 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final List<ProjectType> projectTypeList = (List<ProjectType>) jdbcTemplate.query(projectTypeQuery,
				new ProjectType());
		return new ResponseEntity<>(projectTypeList, HttpStatus.OK);
	}

	/**
	 *  This method is used to retrieve a specific bulkbarcodeconfig record.
	 * @param inputMap  [Map] map object with "nprojecttypecode","nbulkbarcodeconfigcode" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with bulkbarcodeconfig object for the selected bulkbarcodeconfig.
	 * @throws Exception that are thrown from this DAO layer.
	 */
	@Override
	public ResponseEntity<Object> getBulkBarcodeConfig(final int nbulkbarcodeconfigcode, final int nprojecttypecode,
			final UserInfo objUserInfo) throws Exception {
		// ALPD-4722--Vignesh R(21-08-2024)
		final BulkBarcodeConfig isActiveBulkBarcodeConfig = getActiveBulkBarcodeConfigurationById(
				nbulkbarcodeconfigcode, objUserInfo);
		if (isActiveBulkBarcodeConfig != null) {
			final Map<String, Object> map = new LinkedHashMap<String, Object>();
			final String projectTypequery = "select * from projecttype where nprojecttypecode=" + nprojecttypecode
					+ " and nprojecttypecode >0 and nsitecode="+objUserInfo.getNmastersitecode()+" and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final ProjectType projectType = jdbcTemplate.queryForObject(projectTypequery, new ProjectType());
			final List<ProjectType> projectTypeList = (List<ProjectType>) getProjectType(objUserInfo).getBody();
			map.put("ProjectType", projectTypeList);
			map.put("SelectedProjectType", projectType);

			map.put("selectedBulkBarcodeConfig", isActiveBulkBarcodeConfig);
			final List<BulkBarcodeConfig> lstBulkBarcodeConfig = (List<BulkBarcodeConfig>) getBulkBarcodeConfigbyProjectType(
					nprojecttypecode, objUserInfo).getBody();
			map.put("BulkBarcodeConfig", lstBulkBarcodeConfig);
			map.putAll((Map<String, Object>) getBulkBarcodeConfigDetailsbyBulkBarcodeConfig(nprojecttypecode,
					nbulkbarcodeconfigcode, objUserInfo, 1).getBody());
			return new ResponseEntity<Object>(map, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve a list of all active barcodemaster table for the selected project type.
	 * @param inputMap  [Map] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of deleted bulkbarcodeconfig object
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getFilterProjectType(final UserInfo userInfo, final int nprojecttypecode)
			throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final String projectTypequery = "select * from projecttype where nprojecttypecode=" + nprojecttypecode
				+ " and nprojecttypecode >0 and nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final ProjectType projectType = jdbcTemplate.queryForObject(projectTypequery, new ProjectType());
		final List<ProjectType> projectTypeList = (List<ProjectType>) getProjectType(userInfo).getBody();
		map.put("ProjectType", projectTypeList);
		map.put("SelectedProjectType", projectType);
		final List<BulkBarcodeConfig> lstBulkBarcodeConfig = (List<BulkBarcodeConfig>) getBulkBarcodeConfigbyProjectType(
				nprojecttypecode, userInfo).getBody();
		if (lstBulkBarcodeConfig.isEmpty()) {
			map.put("BulkBarcodeConfig", lstBulkBarcodeConfig);
			map.put("selectedBulkBarcodeConfig", null);
		} else {
			map.put("BulkBarcodeConfig", lstBulkBarcodeConfig);
			map.put("selectedBulkBarcodeConfig", lstBulkBarcodeConfig.get(lstBulkBarcodeConfig.size() - 1));
			map.putAll((Map<String, Object>) getBulkBarcodeConfigDetailsbyBulkBarcodeConfig(nprojecttypecode,
					lstBulkBarcodeConfig.get(lstBulkBarcodeConfig.size() - 1).getNbulkbarcodeconfigcode(), userInfo, 1)
					.getBody());
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to bulkbarcodeconfigdetails  table.
	 * Need to check for duplicate entry of Sorter for the selected bulkbarcodeconfig.	
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfigdetails [BulkBarcodeConfigDetails]  object holding details to be added in bulkbarcodeconfigdetails table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of added bulkbarcodeconfigdetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<? extends Object> createBulkBarcodeMaster(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table lockbarcodeformatting "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedList = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();
		final BulkBarcodeConfigDetails objProjectBarcodeConfig = objmapper
				.convertValue(inputMap.get("bulkbarcodeconfigdetails"), BulkBarcodeConfigDetails.class);
		final BulkBarcodeConfig isActiveBulkBarcodeConfig = getActiveBulkBarcodeConfigurationById(
				objProjectBarcodeConfig.getNbulkbarcodeconfigcode(), userInfo);
		if (isActiveBulkBarcodeConfig != null) {
			JSONObject jsonObject = new JSONObject(objProjectBarcodeConfig.getJsondata());

			BulkBarcodeConfigDetails bulkBarcodeFieldName = getBulkBarcodeFieldName(
					objProjectBarcodeConfig.getNprojecttypecode(), objProjectBarcodeConfig.getNbulkbarcodeconfigcode(),
					jsonObject.get("sfieldname").toString(), userInfo);
			BulkBarcodeConfigDetails bulkBarcodeSortOrder = getBulkBarcodeSortOrder(
					objProjectBarcodeConfig.getNprojecttypecode(), objProjectBarcodeConfig.getNbulkbarcodeconfigcode(),
					objProjectBarcodeConfig.getNsorter(), userInfo);
			if (bulkBarcodeFieldName == null) {
				if (bulkBarcodeSortOrder == null) {
					int nSeqNo = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobarcode where stablename='bulkbarcodeconfigdetails' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					nSeqNo++;
					String projectBarcodeConfigInsert = "insert into bulkbarcodeconfigdetails(nbulkbarcodeconfigdetailscode, nbulkbarcodeconfigcode, "
							+ " nprojecttypecode, nneedmaster, nqueryneed, nquerybuildertablecode,stablename, "
							+ " stablecolumnname, nfieldstartposition, nfieldlength, nsorter, jsondata, squery, dmodifieddate, nsitecode, nstatus,sdescription)"
							+ " values(" + nSeqNo + "," + objProjectBarcodeConfig.getNbulkbarcodeconfigcode() + ","
							+ objProjectBarcodeConfig.getNprojecttypecode() + ","
							+ objProjectBarcodeConfig.getNneedmaster() + " ," + objProjectBarcodeConfig.getNqueryneed()
							+ "," + objProjectBarcodeConfig.getNquerybuildertablecode() + "" + " ,N'"
							+ stringUtilityFunction.replaceQuote(objProjectBarcodeConfig.getStablename()) + "','"
							+ stringUtilityFunction.replaceQuote(objProjectBarcodeConfig.getStablecolumnname()) + " ',"
							+ objProjectBarcodeConfig.getNfieldstartposition() + ","
							+ objProjectBarcodeConfig.getNfieldlength() + " ," + objProjectBarcodeConfig.getNsorter()
							+ ",'" + jsonObject + " ',case when '"
							+ stringUtilityFunction.replaceQuote(objProjectBarcodeConfig.getSquery())
							+ "'='null' then null else N'"
							+ stringUtilityFunction.replaceQuote(objProjectBarcodeConfig.getSquery()) + "'end " + " ,'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
							+ objProjectBarcodeConfig.getSdescription() + "');";

					String updateseqNo = "update seqnobarcode set nsequenceno = " + nSeqNo
							+ " where stablename='bulkbarcodeconfigdetails' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(projectBarcodeConfigInsert + updateseqNo);

					objProjectBarcodeConfig.setNbulkbarcodeconfigdetailscode(nSeqNo);
					savedList.add(objProjectBarcodeConfig);
					multilingualIDList.add("IDS_ADDBULKBARCODDETAILS");
					auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);
					return getBulkBarcodeConfigDetailsbyBulkBarcodeConfig(objProjectBarcodeConfig.getNprojecttypecode(),
							objProjectBarcodeConfig.getNbulkbarcodeconfigcode(), userInfo, 2);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the BulkBarcodeConfigDetails object for the specified field name and project type.
	 * @param sfieldname [String] name of the field name
	 * @param nprojecttypecode [int] name of the project type
	 * @param nbulkbarcodeconfigcode [int] name of the BulkBarcodeConfig
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched	
	 * @return BulkBarcodeConfigDetails object based on the specified  field name and project type.
	 * @throws Exception that are thrown from this DAO layer
	 */
	private BulkBarcodeConfigDetails getBulkBarcodeFieldName(final int nprojecttypecode,
			final int nbulkbarcodeconfigcode, final String sfieldname, final UserInfo userInfo) throws Exception {
		final String strQuery = "select  jsondata->>'sfieldname' as displayname from bulkbarcodeconfigdetails where "
				+ "jsondata->>'sfieldname' = N'" + sfieldname + "' and nprojecttypecode=" + nprojecttypecode
				+ " and nbulkbarcodeconfigcode=" + nbulkbarcodeconfigcode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		return (BulkBarcodeConfigDetails) jdbcUtilityFunction.queryForObject(strQuery, BulkBarcodeConfigDetails.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to fetch the BulkBarcodeConfigDetails object for the specified Sorter and project type.
	 * @param nsorter [int] name of the Sorter
	 * @param nprojecttypecode [int] name of the project type
	 * @param nbulkbarcodeconfigcode [int] name of the BulkBarcodeConfig
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched	
	 * @return BulkBarcodeConfigDetails object based on the specified Sorter and project type.
	 * @throws Exception that are thrown from this DAO layer
	 */
	private BulkBarcodeConfigDetails getBulkBarcodeSortOrder(final int nprojecttypecode,
			final int nbulkbarcodeconfigcode, final int nsorter, final UserInfo userInfo) throws Exception {
		final String strQuery = "select  nsorter from bulkbarcodeconfigdetails where " + "nsorter = " + nsorter
				+ " and nprojecttypecode=" + nprojecttypecode + " and nbulkbarcodeconfigcode=" + nbulkbarcodeconfigcode
				+ " and nsitecode="+ userInfo.getNmastersitecode()+" and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (BulkBarcodeConfigDetails) jdbcUtilityFunction.queryForObject(strQuery, BulkBarcodeConfigDetails.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to retrieve active barcodemaster entries for the selected project type. 
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity with Field Length object for the specified nprojecttypecode.
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getFieldLengthService(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final int nprojecttypecode = (int) inputMap.get("nprojecttypecode");
		final String stablename = inputMap.get("stablename").toString();

		final String fieldLengthquery = "select  max(ncodelength) as ncodelength from " + stablename + " where "
				+ " nprojecttypecode=" + nprojecttypecode + " and  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Integer bulkBarcodeMasterList = jdbcTemplate.queryForObject(fieldLengthquery, Integer.class);

		outputMap.put("FieldLength", bulkBarcodeMasterList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active BarcodeDetails entries for the selected project type and bulk barcode config. 
	 * @param nprojecttypecode [int] name of the project type
	 * @param nbulkbarcodeconfigcode [int] name of the BulkBarcodeConfig
	 * @param nflag [int] name of the Flag
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity withBarcodeDetails for the selected project type and bulk barcode config. 
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Map<String, Object>> getBulkBarcodeConfigDetailsbyBulkBarcodeConfig(int nprojecttypecode,
			int nbulkbarcodeconfigcode, final UserInfo userInfo, int nflag) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String barcodeDetails = "select nsorter,jsondata->>'sfieldname' as sdisplayname,* from bulkbarcodeconfigdetails where nprojecttypecode="
				+ nprojecttypecode + " and nbulkbarcodeconfigcode=" + nbulkbarcodeconfigcode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter desc";
		final List<BulkBarcodeConfigDetails> barcodeDetailsList = (List<BulkBarcodeConfigDetails>) jdbcTemplate
				.query(barcodeDetails, new BulkBarcodeConfigDetails());
		outputMap.put("BarcodeDetails", barcodeDetailsList);
		// nflag 2 is get project type for UI
		if (nflag == 2) {
			final String projectTypequery = "select * from projecttype where nprojecttypecode=" + nprojecttypecode
					+ " and nprojecttypecode >0 and nsitecode = "+userInfo.getNmastersitecode()+" and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final ProjectType projectType = jdbcTemplate.queryForObject(projectTypequery, new ProjectType());
			final List<ProjectType> projectTypeList = (List<ProjectType>) getProjectType(userInfo).getBody();
			outputMap.put("ProjectType", projectTypeList);
			outputMap.put("SelectedProjectType", projectType);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	/**
	 * This method is used to retrieve active BarcodeDetails entries for the selected project type and bulk barcode config. 
	 * @param nprojecttypecode [int] name of the project type
	 * @param nbulkbarcodeconfigcode [int] name of the BulkBarcodeConfig
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity withBarcodeDetails for the selected project type and bulk barcode config. 
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getBulkBarcodeConfigDetailsAfterEdit(int nprojecttypecode, int nbulkbarcodeconfigcode,
			int nbulkbarcodeconfigdetailscode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String barcodeDetails = "select jsondata->>'sfieldname' as sdisplayname,* from bulkbarcodeconfigdetails where nprojecttypecode="
				+ nprojecttypecode + " and nbulkbarcodeconfigcode=" + nbulkbarcodeconfigcode
				+ " and nbulkbarcodeconfigdetailscode=" + nbulkbarcodeconfigdetailscode + " "
				+ " and nsitecode = "+userInfo.getNmastersitecode()+" and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		final List<BulkBarcodeConfigDetails> barcodeDetailsList = (List<BulkBarcodeConfigDetails>) jdbcTemplate
				.query(barcodeDetails, new BulkBarcodeConfigDetails());
		outputMap.put("BarcodeDetails", barcodeDetailsList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to update entry in bulkbarcodeconfigdetails  table.
	 * Need to check for duplicate entry of Sorter for the selected bulkbarcodeconfig.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfigdetails [BulkBarcodeConfigDetails]  object holding details to be added in bulkbarcodeconfigdetails table,
	 * 								userinfo [UserInfo] holding logged in user details.

	 * @return response entity object holding response status and data of updated bulkbarcodeconfigdetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<? extends Object> updateBulkBarcodeMaster(
			BulkBarcodeConfigDetails objBulkBarcodeConfigDetails, final UserInfo userInfo) throws Exception {

		final BulkBarcodeConfigDetails isActiveBulkBarcodeConfig = getActiveBulkBarcodeMasterById(
				objBulkBarcodeConfigDetails.getNbulkbarcodeconfigdetailscode(), userInfo);

		if (isActiveBulkBarcodeConfig != null) {
			final String queryString = "select nbulkbarcodeconfigdetailscode from bulkbarcodeconfigdetails where nsorter = "
					+ objBulkBarcodeConfigDetails.getNsorter() + " and nprojecttypecode="
					+ objBulkBarcodeConfigDetails.getNprojecttypecode() + " and nbulkbarcodeconfigdetailscode <> "
					+ objBulkBarcodeConfigDetails.getNbulkbarcodeconfigdetailscode() + " and  nbulkbarcodeconfigcode="
					+ objBulkBarcodeConfigDetails.getNbulkbarcodeconfigcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			final List<BulkBarcodeConfigDetails> bulkBarcodeConfigListByName = (List<BulkBarcodeConfigDetails>) jdbcTemplate
					.query(queryString, new BulkBarcodeConfigDetails());

			if (bulkBarcodeConfigListByName.isEmpty()) {
				final String updateQueryString = "update bulkbarcodeconfigdetails set sdescription='"
						+ objBulkBarcodeConfigDetails.getSdescription() + "', nsorter="
						+ objBulkBarcodeConfigDetails.getNsorter() + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', jsondata=jsonb_set(jsondata, '{isvalidationrequired}', '"
						+ objBulkBarcodeConfigDetails.getIsvalidationrequired() + "') "
						+ " where nbulkbarcodeconfigdetailscode=" // ALPD-5082 Modified query to update processing time
																	// calculation by VISHAKH
						+ objBulkBarcodeConfigDetails.getNbulkbarcodeconfigdetailscode();
				jdbcTemplate.execute(updateQueryString);

				final List<String> multilingualIDList = new ArrayList<>();

				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(isActiveBulkBarcodeConfig);

				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(objBulkBarcodeConfigDetails);

				multilingualIDList.add("IDS_EDITBULKBARCODEDETAILS");

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);

				return getBulkBarcodeConfigDetailsbyBulkBarcodeConfig(objBulkBarcodeConfigDetails.getNprojecttypecode(),
						objBulkBarcodeConfigDetails.getNbulkbarcodeconfigcode(), userInfo, 2);
			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to delete an entry in bulkbarcodeconfigdetails  table.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfigdetails [BulkBarcodeConfigDetails]  object holding details to be added in bulkbarcodeconfigdetails table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of deleted bulkbarcodeconfigdetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<? extends Object> deleteBulkBarcodeMaster(
			BulkBarcodeConfigDetails objBulkBarcodeConfigDetails, final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedList = new ArrayList<>();
		final BulkBarcodeConfigDetails isActiveBulkBarcodeConfig = getActiveBulkBarcodeMasterById(
				objBulkBarcodeConfigDetails.getNbulkbarcodeconfigdetailscode(), userInfo);
		if (isActiveBulkBarcodeConfig != null) {
			final String updateQueryString = "update bulkbarcodeconfigdetails set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " ,dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nbulkbarcodeconfigdetailscode="
					+ objBulkBarcodeConfigDetails.getNbulkbarcodeconfigdetailscode();
			jdbcTemplate.execute(updateQueryString);

			objBulkBarcodeConfigDetails
					.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			savedList.add(objBulkBarcodeConfigDetails);
			multilingualIDList.add("IDS_DELETEBULKBARCODEDETAILS");
			auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);
			return getBulkBarcodeConfigDetailsbyBulkBarcodeConfig(objBulkBarcodeConfigDetails.getNprojecttypecode(),
					objBulkBarcodeConfigDetails.getNbulkbarcodeconfigcode(), userInfo, 2);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	/**
	 * This method is used to retrieve active bulkbarcodeconfigdetails object 
	 * @param inputMap  [Map] map object with "nbulkbarcodeconfigdetailscode" and "userInfo" as keys for which the data is to be fetched
	 * @return response entity  object holding response status and data of bulkbarcodeconfigdetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public BulkBarcodeConfigDetails getActiveBulkBarcodeMasterById(final int nbulkbarcodeconfigdetailscode,
			final UserInfo userInfo) throws Exception {

		String strQuery = "select jsondata->>'sformname' sformname,jsondata->'parentData'->>'sdisplayname' as sparentformname,"
				+ " (jsondata->'parentData'->'nfieldlength')::int as nparentfieldlength,"
				+ " jsondata->>'sfieldname' as sfieldname,(jsondata->>'isneedparent')::int  as isneedparent,"
				+ " (jsondata->>'nbarcodemastercode')::int as nbarcodemastercode,nfieldlength ,"
				+ " (jsondata->'parentData'->'nbulkbarcodeconfigdetailscode')::int as nparentnbarcodemastercode,"
				+ " (jsondata->>'isvalidationrequired') as isvalidationrequired,*  from bulkbarcodeconfigdetails where " // ALPD-5082
																															// Modified // query // for // processing  //time // calculation // by// VISHAKH
				+ "  nbulkbarcodeconfigdetailscode=" + nbulkbarcodeconfigdetailscode + " "
				+ "  and nsitecode ="+userInfo.getNmastersitecode()+" and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (BulkBarcodeConfigDetails) jdbcUtilityFunction.queryForObject(strQuery, BulkBarcodeConfigDetails.class,
				jdbcTemplate);

	}

	/**
	 * This method is used to retrieve active ParentBarcodeDetails object 
	 * @param inputMap  [Map] map object with "nbulkbarcodeconfigcode"[int],"nprojecttypecode"[int] and "userInfo" as keys for which the data is to be fetched
	 * @return response entity  object holding response status and data of ParentBarcodeDetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getParentBulkBarcodeMaster(final int nbulkbarcodeconfigcode,
			final int nprojecttypecode, final UserInfo objUserInfo, Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Map<String, Object> map = new LinkedHashMap<>();

		final BulkBarcodeMaster objBulkBarcodeMaster = objMapper.convertValue(inputMap.get("selectedBarcodeFormatting"),
				BulkBarcodeMaster.class);

		final String query = "select * from  querybuildertablecolumns where nquerybuildertablecode="
				+ objBulkBarcodeMaster.getNquerybuildertablecode()+" "
				+ " and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

		final QueryBuilderTableColumns queryBuilderlist = jdbcTemplate.queryForObject(query,
				new QueryBuilderTableColumns());

		final List<Map<String, Object>> numericColumns = (List<Map<String, Object>>) (List<?>) queryBuilderlist
				.getJnumericcolumns();

		final String queryString = " select  jsondata->>'sfieldname' as sdisplayname,nbulkbarcodeconfigdetailscode, nbulkbarcodeconfigcode,  nprojecttypecode, nneedmaster, nqueryneed,stablename, "
				+ " stablecolumnname, nfieldstartposition, nfieldlength, nsorter, jsondata from bulkbarcodeconfigdetails where nbulkbarcodeconfigcode="
				+ nbulkbarcodeconfigcode + " " + " and nprojecttypecode=" + nprojecttypecode + "  and nsitecode="
				+ objUserInfo.getNmastersitecode() + " and nneedmaster="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and " + " nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<BulkBarcodeConfigDetails> barcodeDetailsList = jdbcTemplate.query(queryString,
				new BulkBarcodeConfigDetails());

		if (barcodeDetailsList.isEmpty()) {
			map.put("ParentBarcodeDetails", barcodeDetailsList);
		} else {
			final List<?> commonValues = numericColumns == null ? new ArrayList<>()
					: barcodeDetailsList.stream()
							.filter(value -> numericColumns.stream()
									.anyMatch(column -> column.containsKey("foriegntablename")
											&& column.get("foriegntablename").equals(value.getStablename())))
							.collect(Collectors.toList());
			map.put("ParentBarcodeDetails", commonValues);
		}

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

}
