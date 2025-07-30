package com.agaramtech.qualis.dynamicmaster.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.credential.model.QualisForms;
import com.agaramtech.qualis.dynamicmaster.model.DynamicMaster;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class DynamicMasterDAOImpl implements DynamicMasterDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicMasterDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final FTPUtilityFunction ftpUtilityFunction;

	@Override
	public ResponseEntity<Object> getDynamicMaster(final UserInfo userInfo) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final Map<String, Object> returnMap = new HashMap<String, Object>();
		final ReactRegistrationTemplate templateDesign = (ReactRegistrationTemplate) getMasterDesign(userInfo)
				.getBody();
		returnMap.put("DynamicMasterDesign", templateDesign);

		if (templateDesign != null) {
			final String getDesignQuery = "select json_agg(a.jsonuidata) from (select "
					+ " dm.jsonuidata || json_build_object('ndynamicmastercode',dm.ndynamicmastercode,'nformcode',dm.nformcode,"
					+ "'ndesigntemplatemappingcode',dm.ndesigntemplatemappingcode)::jsonb  jsonuidata "
					+ " from designtemplatemapping rrt, dynamicmaster dm  where rrt.nsitecode="
					+ userInfo.getNmastersitecode() + " and dm.nsitecode=" + userInfo.getNmastersitecode() + " "
					+ "and dm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and rrt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rrt.ndesigntemplatemappingcode = dm.ndesigntemplatemappingcode" + " and dm.nformcode = "
					+ userInfo.getNformcode() + " and rrt.ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ")a";

			LOGGER.info("Get Quey -->" + getDesignQuery);

			final String dataList = jdbcTemplate.queryForObject(getDesignQuery, String.class);

			if (dataList == null) {
				returnMap.put("DynamicMasterData", new ArrayList<>());
			} else {
				final UserInfo dateConversionUserInfo = new UserInfo(userInfo);
				dateConversionUserInfo.setNformcode((short) -1);
				final List<Map<String, Object>> modifiedDataList = objectMapper.convertValue(
						projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(dataList, dateConversionUserInfo,
								true, templateDesign.getNdesigntemplatemappingcode(), "master"),
						new TypeReference<List<Map<String, Object>>>() {
						});
				returnMap.put("DynamicMasterData", modifiedDataList);
			}
		} else {
			returnMap.put("DynamicMasterData", new ArrayList<>());
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMasterDesign(final UserInfo userInfo) throws Exception {
		final String getDesignQuery = "select mp.jsondata screendesign,temp.jsondata as slideoutdesign, tm.ndesigntemplatemappingcode "
				+ " from reactregistrationtemplate temp,designtemplatemapping tm,mappedtemplatefieldprops mp"
				+ " where mp.nsitecode = " + userInfo.getNmastersitecode() + " and tm.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + " and temp.nsitecode = " + userInfo.getNmastersitecode()
				+ " and mp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and temp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tm.ndesigntemplatemappingcode = mp.ndesigntemplatemappingcode "
				+ " and temp.nreactregtemplatecode = tm.nreactregtemplatecode" + " and tm.nformcode = "
				+ userInfo.getNformcode() + " and tm.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
		final ReactRegistrationTemplate templateDesign = (ReactRegistrationTemplate) jdbcUtilityFunction
				.queryForObject(getDesignQuery, ReactRegistrationTemplate.class, jdbcTemplate);
		return new ResponseEntity<>(templateDesign, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveDynamicMasterById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final String getDesignQuery = "select json_agg(a.jsondata) from (select "
				+ " dm.jsondata || json_build_object('ndynamicmastercode',dm.ndynamicmastercode)::jsonb "
				+ " || json_build_object('nformcode',dm.nformcode)::jsonb "
				+ " || json_build_object('ndesigntemplatemappingcode',dm.ndesigntemplatemappingcode)::jsonb jsondata"
				+ " from designtemplatemapping rrt ,dynamicmaster dm where dm.nsitecode ="
				+ userInfo.getNmastersitecode() + " and rrt.nsitecode=" + userInfo.getNmastersitecode()
				+ " and dm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rrt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and dm.ndynamicmastercode=" + inputMap.get("ndynamicmastercode") + ")a";
		final String dataResult = jdbcTemplate.queryForObject(getDesignQuery, String.class);
		if (dataResult == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_RECORDALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final List<Map<String, Object>> listMap = objectMapper.readValue(dataResult.toString(),
				new TypeReference<List<Map<String, Object>>>() {
				});
		final Map<String, Object> objMap = new HashMap<String, Object>();
		objMap.put("EditData", listMap.get(0));
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createDynamicMaster(final MultipartHttpServletRequest inputMap,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final Object reg = objectMapper.readValue(inputMap.getParameter("Map"), new TypeReference<Object>() {
		});
		final JSONObject objJson = new JSONObject(inputMap.getParameter("Map"));
		if ((boolean) objJson.get("isFileupload")) {
			final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(inputMap, -1, userInfo);
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)) {
				return new ResponseEntity<>(createDynamicMasterWithFile((Map<String, Object>) reg, userInfo).getBody(),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			final ResponseEntity<Object> str = createDynamicMasterWithFile((Map<String, Object>) reg, userInfo);
			if (str.getStatusCode().equals(HttpStatus.CONFLICT)) {
				return new ResponseEntity<>(str.getBody(), HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<>(str.getBody(), HttpStatus.OK);
			}
		}
	}

	public ResponseEntity<Object> createDynamicMasterWithFile(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstdynamicdata = new ArrayList<>();
		final List<Map<String, Object>> lstaudit = new ArrayList<>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditObject = new JSONObject();
		final DynamicMaster dynamicMaster = objectMapper.convertValue(inputMap.get("dynamicmaster"),
				DynamicMaster.class);

		final List<Map<String, Object>> masterDateConstraints = objectMapper
				.convertValue(inputMap.get("masterdateconstraints"), new TypeReference<List<Map<String, Object>>>() {
				});

		final List<Map<String, Object>> masterUniqueValidation = objectMapper
				.convertValue(inputMap.get("mastercombinationunique"), new TypeReference<List<Map<String, Object>>>() {
				});
		final List<String> dateList = objectMapper.convertValue(inputMap.get("masterdatelist"),
				new TypeReference<List<String>>() {
				});

		JSONObject jsonData = new JSONObject(dynamicMaster.getJsondata());
		JSONObject jsonuiData = new JSONObject(dynamicMaster.getJsonuidata());

		jsonData = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonData, dateList, false,
				userInfo);
		jsonuiData = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuiData, dateList, false,
				userInfo);

		final Map<String, Object> dateValidateMap = commonFunction.validateDynamicDateContraints(jsonData,
				masterDateConstraints, userInfo);
		if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(dateValidateMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			return new ResponseEntity<>(dateValidateMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
					HttpStatus.CONFLICT);

		}

		final Map<String, Object> map = projectDAOSupport.validateUniqueConstraint(masterUniqueValidation,
				(Map<String, Object>) inputMap.get("dynamicmaster"), userInfo, "create", DynamicMaster.class,
				"ndynamicmastercode", true);

		if (!Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
				.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			return new ResponseEntity<>(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
					HttpStatus.CONFLICT);
		}

		final String getSequenceNo = "select nsequenceno+1 from seqnobasemaster where stablename ='dynamicmaster'";
		final int seqNo = jdbcTemplate.queryForObject(getSequenceNo, Integer.class);

		final String insertQuery = " insert into dynamicmaster (ndynamicmastercode,nformcode,"
				+ " ndesigntemplatemappingcode, jsondata, jsonuidata, dmodifieddate, nsitecode, nstatus) values("
				+ seqNo + "," + dynamicMaster.getNformcode() + "," + dynamicMaster.getNdesigntemplatemappingcode()
				+ ",'" + jsonData.toString() + "'::jsonb" + ",'" + jsonuiData.toString() + "'::jsonb, '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

		jdbcTemplate.execute(insertQuery + "; update seqnobasemaster set nsequenceno = " + seqNo
				+ " where stablename='dynamicmaster'");
		final String sformname = jdbcTemplate.queryForObject("select jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "' from qualisforms where nformcode=" + userInfo.getNformcode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), String.class);
		lstdynamicdata = ((List<Map<String, Object>>) ((Map<String, Object>) getDynamicMaster(userInfo).getBody())
				.get("DynamicMasterData"));
		lstaudit.add(0, lstdynamicdata.get(lstdynamicdata.size() - 1));
		jsonAuditObject.put("dynamicmaster", lstaudit);
		actionType.put("dynamicmaster",
				commonFunction.getMultilingualMessage("IDS_ADD", userInfo.getSlanguagefilename()) + " " + sformname);
		objmap.put("nregtypecode", -1);
		objmap.put("nregsubtypecode", -1);
		objmap.put("ndesigntemplatemappingcode", lstaudit.get(0).get("ndesigntemplatemappingcode"));
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, objmap, false, userInfo);
		return getDynamicMaster(userInfo);

	}

	private boolean validateUniqueConstraint(final List<Map<String, Object>> masterUniqueValidation,
			final DynamicMaster dynamicMaster, final UserInfo userInfo, final String task) throws Exception {
		final JSONObject jsonUIData = new JSONObject(dynamicMaster.getJsonuidata());

		final StringBuffer buffer = new StringBuffer();
		for (final Map<String, Object> constraintMap : masterUniqueValidation) {
			final String constraint = (String) constraintMap.get("1");
			final String data = (String) jsonUIData.get(constraint);
			buffer.append(" and  jsonuidata->> '" + constraint + "' = '" + data + "'");
		}

		boolean valid = true;
		if (buffer.toString().length() > 1) {
			if (task.equalsIgnoreCase("update")) {
				buffer.append(" and ndynamicmastercode <>" + dynamicMaster.getNdynamicmastercode());
			}
			final String validationQuery = "select dm.ndynamicmastercode, dm.nformcode, dm.ndesigntemplatemappingcode, dm.jsondata,"
					+ " dm.jsonuidata, dm.nsitecode, dm.nstatus from dynamicmaster dm where nformcode = "
					+ userInfo.getNformcode() + " and nsitecode=" + userInfo.getNmastersitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + buffer.toString() + ";";

			final List<DynamicMaster> masterList = jdbcTemplate.query(validationQuery, new DynamicMaster());
			if (!masterList.isEmpty()) {
				valid = false;
			}
		}
		return valid;
	}

	@Override
	public ResponseEntity<Object> updateDynamicMaster(final MultipartHttpServletRequest inputMap,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final Object reg = objectMapper.readValue(inputMap.getParameter("Map"), new TypeReference<Object>() {
		});
		final JSONObject objJson = new JSONObject(inputMap.getParameter("Map"));
		if ((boolean) objJson.get("isFileupload")) {
			final String uploadStatus = ftpUtilityFunction.getFileFTPUpload(inputMap, -1, userInfo);

			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(uploadStatus)) {
				return new ResponseEntity<>(updateDynamicMasterWithFile((Map<String, Object>) reg, userInfo).getBody(),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(uploadStatus, userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			final ResponseEntity<Object> str = updateDynamicMasterWithFile((Map<String, Object>) reg, userInfo);
			if (str.getStatusCode().equals(HttpStatus.CONFLICT)) {
				return new ResponseEntity<>(str.getBody(), HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<>(str.getBody(), HttpStatus.OK);
			}
		}
	}

	public ResponseEntity<Object> updateDynamicMasterWithFile(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstdynamicdata = new ArrayList<>();
		final List<Map<String, Object>> lstaudit = new ArrayList<>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditObjectold = new JSONObject();
		final JSONObject jsonAuditObjectnew = new JSONObject();
		final DynamicMaster dynamicMaster = objectMapper.convertValue(inputMap.get("dynamicmaster"),
				DynamicMaster.class);

		final String activeQuery = "select ndynamicmastercode, nformcode, ndesigntemplatemappingcode, jsondata, jsonuidata, nsitecode, nstatus from dynamicmaster where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndynamicmastercode = "
				+ dynamicMaster.getNdynamicmastercode();
		final DynamicMaster masterById = jdbcTemplate.queryForObject(activeQuery, new DynamicMaster());

		if (masterById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_RECORDALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<Map<String, Object>> masterDateConstraints = objectMapper.convertValue(
					inputMap.get("masterdateconstraints"), new TypeReference<List<Map<String, Object>>>() {
					});
			final List<Map<String, Object>> masterUniqueValidation = objectMapper.convertValue(
					inputMap.get("mastercombinationunique"), new TypeReference<List<Map<String, Object>>>() {
					});
			final List<String> dateList = objectMapper.convertValue(inputMap.get("masterdatelist"),
					new TypeReference<List<String>>() {
					});

			JSONObject jsonData = new JSONObject(dynamicMaster.getJsondata());

			jsonData = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonData, dateList, false,
					userInfo);

			final Map<String, Object> dateValidateMap = commonFunction.validateDynamicDateContraints(jsonData,
					masterDateConstraints, userInfo);
			if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals(dateValidateMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
				return new ResponseEntity<>(
						dateValidateMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
						HttpStatus.CONFLICT);

			}

			final Map<String, Object> map = projectDAOSupport.validateUniqueConstraint(masterUniqueValidation,
					(Map<String, Object>) inputMap.get("dynamicmaster"), userInfo, "update", DynamicMaster.class,
					"ndynamicmastercode", true);

			if (!Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
					.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
				return new ResponseEntity<>(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
						HttpStatus.CONFLICT);
			}

			lstdynamicdata = ((List<Map<String, Object>>) ((Map<String, Object>) getDynamicMaster(userInfo).getBody())
					.get("DynamicMasterData"));
			lstaudit.add(0,
					lstdynamicdata.stream()
							.filter(t -> t.get("ndynamicmastercode").equals(dynamicMaster.getNdynamicmastercode()))
							.findAny().get());
			jsonAuditObjectold.put("dynamicmaster", lstaudit);
			lstaudit.clear();

			final String updateQuery = " update dynamicmaster set jsondata ='" + dynamicMaster.getJsonstring()
					+ "'::jsonb " + " ,jsonuidata =' " + dynamicMaster.getJsonuistring() + "'::jsonb, dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ndynamicmastercode="
					+ dynamicMaster.getNdynamicmastercode();

			jdbcTemplate.execute(updateQuery);

			final String sformname = jdbcTemplate.queryForObject("select jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode() + "' from qualisforms where nformcode=" + userInfo.getNformcode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), String.class);
			lstdynamicdata = ((List<Map<String, Object>>) ((Map<String, Object>) getDynamicMaster(userInfo).getBody())
					.get("DynamicMasterData"));
			lstaudit.add(0,
					lstdynamicdata.stream()
							.filter(t -> t.get("ndynamicmastercode").equals(dynamicMaster.getNdynamicmastercode()))
							.findAny().get());
			jsonAuditObjectnew.put("dynamicmaster", lstaudit);
			actionType.put("dynamicmaster",
					commonFunction.getMultilingualMessage("IDS_EDIT", userInfo.getSlanguagefilename()) + " "
							+ sformname);
			objmap.put("nregtypecode", -1);
			objmap.put("nregsubtypecode", -1);
			objmap.put("ndesigntemplatemappingcode", lstaudit.get(0).get("ndesigntemplatemappingcode"));
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectold, jsonAuditObjectnew, actionType, objmap, false,
					userInfo);

			return getDynamicMaster(userInfo);
		}
	}

	@Override
	public ResponseEntity<Object> deleteDynamicMaster(final DynamicMaster dynamicMaster, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstdynamicdata = new ArrayList<>();
		final List<Map<String, Object>> lstaudit = new ArrayList<>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditObjectold = new JSONObject();
		final String activeQuery = "select ndynamicmastercode, nformcode, ndesigntemplatemappingcode, jsondata, jsonuidata, nsitecode, nstatus from dynamicmaster where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndynamicmastercode = "
				+ dynamicMaster.getNdynamicmastercode();
		final DynamicMaster masterById = (DynamicMaster) jdbcUtilityFunction.queryForObject(activeQuery,
				DynamicMaster.class, jdbcTemplate);

		if (masterById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_RECORDALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			boolean validRecord = false;

			final ValidatorDel objDeleteValidation = projectDAOSupport
					.validateDeleteRecord(Integer.toString(dynamicMaster.getNdynamicmastercode()), userInfo);
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
			} else {
				validRecord = false;
			}

			if (validRecord) {
				lstdynamicdata = ((List<Map<String, Object>>) ((Map<String, Object>) getDynamicMaster(userInfo)
						.getBody()).get("DynamicMasterData"));
				lstaudit.add(0,
						lstdynamicdata.stream()
								.filter(t -> t.get("ndynamicmastercode").equals(dynamicMaster.getNdynamicmastercode()))
								.findAny().get());
				jsonAuditObjectold.put("dynamicmaster", lstaudit);

				final String deleteQuery = " update dynamicmaster set nstatus ="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ndynamicmastercode="
						+ dynamicMaster.getNdynamicmastercode();

				jdbcTemplate.execute(deleteQuery);

				final String sformname = jdbcTemplate
						.queryForObject(
								"select jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
										+ "' from qualisforms where nformcode=" + userInfo.getNformcode()
										+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								String.class);

				actionType.put("dynamicmaster",
						commonFunction.getMultilingualMessage("IDS_DELETE", userInfo.getSlanguagefilename()) + " "
								+ sformname);
				objmap.put("nregtypecode", -1);
				objmap.put("nregsubtypecode", -1);
				objmap.put("ndesigntemplatemappingcode", lstaudit.get(0).get("ndesigntemplatemappingcode"));
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectold, null, actionType, objmap, false, userInfo);
				return getDynamicMaster(userInfo);
			} else {
				return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> importDynamicMaster(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final MultipartFile objmultipart = request.getFile("uploadedFile");
		final int ndesigntemplatemappingcode = Integer.parseInt(request.getParameter("ndesigntemplatemappingcode"));
		final InputStream objinputstream = objmultipart.getInputStream();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024];
		int len;
		while ((len = objinputstream.read(buffer)) > -1) {
			baos.write(buffer, 0, len);
		}
		baos.flush();
		final InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
		final Workbook workbook = WorkbookFactory.create(is1);
		final Sheet sheet = workbook.getSheetAt(0);
		int rowIndex = 0;
		String query = "insert into dynamicmaster(ndynamicmastercode,nformcode,ndesigntemplatemappingcode,jsondata,jsonuidata,dmodifieddate,nsitecode,nstatus) values ";
		final List<String> lstHeader = new ArrayList<>();

		final JSONArray masterDateConstraints = new JSONArray(request.getParameter("masterdateconstraints"));
		final JSONArray masterUniqueValidation = new JSONArray(request.getParameter("mastercombinationunique"));
		final JSONArray dateList = new JSONArray(request.getParameter("masterdatelist"));
		final JSONObject mandatoryFields = new JSONObject(request.getParameter("mandatoryFields"));
		final JSONArray comboComponents = new JSONArray(request.getParameter("comboComponents"));
		final List<Map<String, Object>> comboComponents1 = objectMapper.readValue(comboComponents.toString(),
				new TypeReference<List<Map<String, Object>>>() {
				});
		final String getSequenceNo = "select nsequenceno from seqnobasemaster where stablename ='dynamicmaster'";
		int seqNo = jdbcTemplate.queryForObject(getSequenceNo, Integer.class);

		for (final Row row : sheet) {
			System.out.println("A" + row.getRowNum());
			if (rowIndex > 0) {
				int cellIndex = 0;
				JSONObject objJsonUiData = new JSONObject();
				JSONObject objJsonData = new JSONObject();
				for (final String field : lstHeader) // iteration over cell using for each loop
				{
					if (row.getCell(cellIndex) != null && row.getCell(cellIndex).getCellType() != CellType.BLANK) {
						System.out.println(row.getCell(cellIndex));
						final Cell cell = row.getCell(cellIndex);
						if (cell.getCellType() == CellType.STRING) {
							objJsonUiData.put(field, cell.getStringCellValue());
						} else if (cell.getCellType() == CellType.NUMERIC) {
							if (DateUtil.isCellDateFormatted(cell)) {
								final SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								objJsonUiData.put(field, sourceFormat.format(cell.getDateCellValue()));
							} else {
								objJsonUiData.put(field, cell.getNumericCellValue());

							}
						} else if (DateUtil.isCellDateFormatted(cell)) {
							if (cell.getDateCellValue() != null) {
								final SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								objJsonUiData.put(field, sourceFormat.format(cell.getDateCellValue()));
							} else {
								objJsonUiData.put(field, "");
							}
						}
						objJsonData.put(field, objJsonUiData.get(field));
					} else {
						final Cell cell1 = sheet.getRow(0).getCell(cellIndex);
						final String fieldString = String.valueOf(cell1.getStringCellValue());
						if (fieldString.contains("(dd-mm-yyy)")) {
							objJsonUiData.put(field, "");
							objJsonData.put(field, "");
						} else {
							if (mandatoryFields.has(field) && (boolean) mandatoryFields.get(field)) {
								return new ResponseEntity<>(
										commonFunction.getMultilingualMessage("IDS_ENTER" + ' ' + field,
												userInfo.getSlanguagefilename()),
										HttpStatus.EXPECTATION_FAILED);
							}
							if (cell1.getCellType() == CellType.NUMERIC) {
								objJsonUiData.put(field, 0);
								objJsonData.put(field, 0);
							} else if (cell1.getCellType() == CellType.STRING) {
								objJsonUiData.put(field, "");
								objJsonData.put(field, "");
							}
						}
					}

					final List<Map<String, Object>> lst = comboComponents1.stream()
							.filter(x -> ((String) x.get("label")).equals(field)).collect(Collectors.toList());

					if (!lst.isEmpty()) {

						boolean multiLingual = false;
						if (lst.get(0).containsKey("isMultiLingual")) {
							multiLingual = (boolean) lst.get(0).get("isMultiLingual");

						}
						String masterQuery = "";

						if (multiLingual) {
							masterQuery = "select * from " + lst.get(0).get("source") + " where jsondata -> '"
									+ lst.get(0).get("displaymember") + "'->>'" + userInfo.getSlanguagetypecode()
									+ "'='" + objJsonUiData.get(field) + "'" + "and nstatus ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
						} else {
							masterQuery = "select * from " + lst.get(0).get("source") + " where \""
									+ lst.get(0).get("displaymember") + "\"='" + objJsonUiData.get(field)
									+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						}

						final List<?> lstData = jdbcTemplate.queryForList(masterQuery);
						if (!lstData.isEmpty()) {
							final Map<String, Object> mapData = (Map<String, Object>) lstData.get(0);
							final JSONObject newData = new JSONObject();
							newData.put("pkey", lst.get(0).get("valuemember"));

							if (multiLingual) {
								final PGobject pgObject = (PGobject) mapData.get("jsondata");
								final Map<String, Object> list = new JSONObject((pgObject).getValue()).toMap();
								final Map<String, Object> list1 = (Map<String, Object>) (list
										.get(lst.get(0).get("displaymember")));
								final String list2 = (String) (list1.get(userInfo.getSlanguagetypecode()));
								newData.put("label", list2);
							} else {
								newData.put("label", mapData.get(lst.get(0).get("displaymember")));
							}
							newData.put("value", mapData.get(lst.get(0).get("valuemember")));
							newData.put("source", lst.get(0).get("source"));
							newData.put("nquerybuildertablecode", lst.get(0).get("nquerybuildertablecode"));
							if (multiLingual) {
								final PGobject pgObject = (PGobject) mapData.get("jsondata");
								final Map<String, Object> list = new JSONObject((pgObject).getValue()).toMap();
								final Map<String, Object> list1 = (Map<String, Object>) (list
										.get(lst.get(0).get("displaymember")));
								final String list2 = (String) (list1.get(userInfo.getSlanguagetypecode()));
								objJsonUiData.put(field, list2);
							} else {
								objJsonUiData.put(field, mapData.get(lst.get(0).get("displaymember")));
							}
							objJsonData.put(field, newData);
						} else {
							return new ResponseEntity<>(objJsonUiData.get(field) + " "
									+ commonFunction.getMultilingualMessage("IDS_VALUENOTPRESENTINPARENT",
											userInfo.getSlanguagefilename())
									+ " (" + lst.get(0).get("label") + ")", HttpStatus.CONFLICT);
						}
					}
					cellIndex++;
				}

				final List<String> dateList1 = objectMapper.readValue(dateList.toString(),
						new TypeReference<List<String>>() {
						});

				objJsonUiData = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(objJsonUiData,
						dateList1, false, userInfo);
				objJsonData = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(objJsonData, dateList1,
						false, userInfo);

				final JSONObject objJson1 = new JSONObject(objJsonUiData.toString());
				final List<Map<String, Object>> masterDateConstraints1 = objectMapper
						.readValue(masterDateConstraints.toString(), new TypeReference<List<Map<String, Object>>>() {
						});
				final Map<String, Object> dateValidateMap = commonFunction.validateDynamicDateContraints(objJsonUiData,
						masterDateConstraints1, userInfo);
				if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals(dateValidateMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					return new ResponseEntity<>(
							dateValidateMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
							HttpStatus.CONFLICT);
				}

				final Map<String, Object> map1 = new HashMap<>();
				final Map<String, Object> map2 = objJson1.toMap();
//				final Map<String, Object> map2 = objectMapper.readValue(objJson1.toString(), Map.class);
				map1.put("jsonuidata", map2);
				final List<Map<String, Object>> masterUniqueValidation1 = objectMapper
						.readValue(masterUniqueValidation.toString(), new TypeReference<List<Map<String, Object>>>() {
						});
				final Map<String, Object> map = projectDAOSupport.validateUniqueConstraint(masterUniqueValidation1,
						map1, userInfo, "create", DynamicMaster.class, "ndynamicmastercode", true);

				if (!Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
						.equals(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
					return new ResponseEntity<>(map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
							HttpStatus.CONFLICT);
				}
				seqNo++;
				query = query + (rowIndex == 1 ? "(" : ",(") + (seqNo) + "," + userInfo.getNformcode() + ","
						+ ndesigntemplatemappingcode + ",'" + objJsonData.toString() + "'::jsonb,'"
						+ objJsonUiData.toString() + "'::jsonb,'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "'," + userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			} else {
//				int cellIndex = 0;
				for (final Cell cell : row) // iteration over cell using for each loop
				{
					String header = cell.getStringCellValue();
					header = header.substring(header.indexOf('(') + 1, header.indexOf(')'));
					lstHeader.add(header);
//					cellIndex++;
				}
			}

			rowIndex++;
		}
		if (sheet.getLastRowNum() == 0 && sheet.getRow(rowIndex) == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NORECORDINTHEEXCELSHEET",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

		jdbcTemplate.execute(
				query + "; update seqnobasemaster set nsequenceno = " + seqNo + " where stablename='dynamicmaster'");
		return getDynamicMaster(userInfo);
	}

	/**
	 * This method is used to get the list of active entries available in the
	 * specified form/screen based on the provided screen name.
	 * 
	 * @param formName [String] name of the screen/form
	 * @return Returns the entries list available in the specified screen
	 * @throws Exception when valid input is not provided or some error thrown in
	 *                   query execution
	 */
	@Override
	public ResponseEntity<Object> getDynamicMasterByForm(final String formName) throws Exception {
		final String queryString = " select * from qualisforms where " + " jsondata->'sdisplayname'->>'en-US' = N'"
				+ stringUtilityFunction.replaceQuote(formName) + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final QualisForms qualisForm = jdbcTemplate.queryForObject(queryString, QualisForms.class);

		final UserInfo userInfo = new UserInfo();
		userInfo.setNformcode(qualisForm.getNformcode());
		userInfo.setSsitedatetime("dd/MM/yyyy HH:mm:ss");
		userInfo.setSsitedate("dd/MM/yyyy");
		return new ResponseEntity<>(getDynamicMaster(userInfo).getBody(), HttpStatus.OK);
	}

	public DynamicMaster checKDynamicMasterIsPresent(final int ndesigntemplatemappingcode, final int ndynamicmastercode)
			throws Exception {
		final String strQuery = "select ndynamicmastercode ,ndesigntemplatemappingcode from dynamicmaster where ndynamicmastercode = "
				+ ndynamicmastercode + " and  ndesigntemplatemappingcode=" + ndesigntemplatemappingcode
				+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DynamicMaster objTest = jdbcTemplate.queryForObject(strQuery, DynamicMaster.class);
		return objTest;
	}

	@Override
	public Map<String, Object> viewDynamicMaster(final DynamicMaster objFile, final UserInfo objUserInfo,
			final Map<String, Object> inputMap) throws Exception {
		Map<String, Object> map = new HashMap<>();
		final Map<String, Object> value = new LinkedHashMap<String, Object>();
		final String strQuery = "select * from dynamicmaster where ndynamicmastercode = "
				+ objFile.getNdynamicmastercode() + " and  ndesigntemplatemappingcode="
				+ objFile.getNdesigntemplatemappingcode() + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DynamicMaster objDynamicMaster = jdbcTemplate.queryForObject(strQuery, DynamicMaster.class);
		if (objDynamicMaster != null) {
			map = ftpUtilityFunction.FileViewUsingFtp(objFile.getSsystemfilename(), -1, objUserInfo, "", "");
			final Map<String, Object> auditmap = new LinkedHashMap<String, Object>();
			final JSONObject actionType = new JSONObject();
			final JSONObject jsonAuditObject = new JSONObject();
			value.putAll((Map<String, Object>) inputMap.get("viewFile"));
			final List<Map<String, Object>> ins = new ArrayList<>();
			ins.add(value);
			jsonAuditObject.put("dynamicmaster", ins);
			auditmap.put("nregtypecode", -1);
			auditmap.put("nregsubtypecode", -1);
			auditmap.put("ndesigntemplatemappingcode", 1);
			actionType.put("dynamicmaster", "IDS_DOWNLOADFILE");
			objUserInfo.setNformcode((short) Enumeration.QualisForms.SAMPLEREGISTRATION.getqualisforms());
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, objUserInfo);
		}
		return map;
	}
}