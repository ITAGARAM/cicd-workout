package com.agaramtech.qualis.dynamicpreregdesign.service.dynamicpreregdesign;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.dashboard.model.QueryBuilderTableColumns;
import com.agaramtech.qualis.dashboard.model.QueryBuilderTables;
import com.agaramtech.qualis.dynamicpreregdesign.model.Combopojo;
import com.agaramtech.qualis.dynamicpreregdesign.model.DefaultTemplate;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactComponents;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactInputFields;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.Registration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

/**
 * This class is used for Designing dynamic Pre-Register pop-up.
 *
 * @author ATE234 janakumar
 * @since 09-04-2025
 */

@Repository
@AllArgsConstructor
public class DynamicPreRegDesignDAOImpl implements DynamicPreRegDesignDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicPreRegDesignDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to get the available input components such as Text
	 * Box,Combo Box etc...
	 *
	 * @param inputMap contains UserInfo
	 * @return List of input components as JSON format
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getReactComponents(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final String getReactComponents = "select jsondata || json_build_object('componentname',coalesce(jsondata->'componentname'->>'"
				+ userInfo.getSlanguagetypecode() + "',jsondata->'componentname'->>'en-US'))::jsonb as jsondata "
				+ "from reactcomponents  where nsitecode=" + userInfo.getNmastersitecode() + " " + "and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nreactcomponentcode";
		final List<ReactComponents> componentList = jdbcTemplate.query(getReactComponents, new ReactComponents());
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("components", componentList);
		returnMap.put("tables", getComboTables(userInfo));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	/**
	 * This method is used to get the available input fields created before.
	 *
	 * @param inputMap contains UserInfo
	 * @return List of available input fields created before as JSON format
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getReactInputFields(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// added the sdisplayname inside query by neeraj ALPD-5202
		final String getReactInputFields = "select jsondata|| json_build_object('nreactinputfieldcode',nreactinputfieldcode"
				+ " ,'sdisplayname',COALESCE(jsondata->'displayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'displayname'->>'en-US'))::jsonb as jsondata from reactinputfields " + "where nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<ReactInputFields> inputFieldList = jdbcTemplate.query(getReactInputFields, new ReactInputFields());
		return new ResponseEntity<>(inputFieldList, HttpStatus.OK);

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getRegistrationTemplate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		// ObjectMapper objectMapper = new ObjectMapper();
		short sampleType;

		if (inputMap.containsKey("nsampletypecode")) {

			sampleType = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));

		} else {

			final String getSampleTypeQry = "select nsampletypecode,ncategorybasedflowrequired,"
					+ " nportalrequired, coalesce(jsondata->'sampletypename'->>'" + userInfo.getSlanguagetypecode()
					+ "',jsondata->'sampletypename'->>'en-US') as ssampletypename,nsorter "
					+ " from sampletype where nsitecode=" + userInfo.getNmastersitecode() + " " + "and nstatus in (-2 ,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) " + " order by nsorter;";
			List<SampleType> sampleTypes = jdbcTemplate.query(getSampleTypeQry, new SampleType());
			sampleType = sampleTypes.get(0).getNsampletypecode();
			returnMap.put("selectedSampleType", sampleTypes.get(0));
			returnMap.put("SampleTypes", sampleTypes);

		}

		inputMap.put("nsampletypecode", Short.toString(sampleType));
		if (!inputMap.containsKey("nsubsampletypecode")) {
			inputMap.put("SubSample", false);
		}
		returnMap.putAll((Map<String, Object>) getDefaultTemplate(inputMap, userInfo).getBody());
		short defaultTemplate = (short) returnMap.get("ndefaulttemplatecode");
		final String getRegistrationTemplate = "select rt.*,dt.nsubsampletypecode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, "
				+ " coalesce(dt.jtemplatename->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',dt.jtemplatename ->'sdisplayname'->>'en-US') as sdefaulttemplatename,rt.jsondata::text as jsontext,"
				+ " coalesce(st.jsondata->'sampletypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',st.jsondata ->'sampletypename'->>'en-US')  as ssampletypename "
				+ " from reactregistrationtemplate rt,transactionstatus ts, defaulttemplate dt,sampletype st "
				+ " where ts.ntranscode= rt.ntransactionstatus " + " and rt.nsampletypecode = " + sampleType
				+ " and rt.ndefaulttemplatecode = " + defaultTemplate
				+ " and rt.ndefaulttemplatecode = dt.ndefaulttemplatecode  and st.nsampletypecode=rt.nsampletypecode  "
				+ " and dt.nsitecode = " + userInfo.getNmastersitecode() + " and rt.nsitecode = "
				+ userInfo.getNmastersitecode() + " and st.nsitecode = " + userInfo.getNmastersitecode()
				+ " and dt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nreactregtemplatecode;";
		final List<ReactRegistrationTemplate> registrationTemplate = jdbcTemplate.query(getRegistrationTemplate,
				new ReactRegistrationTemplate());

		returnMap.put("RegistrationTemplate", registrationTemplate);

		if (registrationTemplate.isEmpty()) {
			returnMap.put("selectedTemplate", null);
		} else {
			if (inputMap.containsKey("nreactregtemplatecode")) {

				List<ReactRegistrationTemplate> lstSelectedTemplate = registrationTemplate.stream()
						.filter(x -> x.getNreactregtemplatecode() == ((int) inputMap.get("nreactregtemplatecode")))
						.collect(Collectors.toList());
				if (!lstSelectedTemplate.isEmpty()) {
					returnMap.put("selectedTemplate", lstSelectedTemplate.get(0));
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYDELETED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

			} else {
				returnMap.put("selectedTemplate", registrationTemplate.get(registrationTemplate.size() - 1));
			}
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDefaultTemplate(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		short sampleTypeCode = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
		short defaultTemplate = 0;
		String subSampleTypeCode = "";
		if ((boolean) inputMap.get("SubSample")) {
			subSampleTypeCode = " and nsubsampletypecode=" + inputMap.get("nsubsampletypecode");
		}
		if (inputMap.containsKey("ndefaulttemplatecode")) {

			defaultTemplate = Short.parseShort(String.valueOf(inputMap.get("ndefaulttemplatecode")));

		} else {
			final String getTemplateQry = "select ndefaulttemplatecode,nsampletypecode,ndefaultstatus, nsubsampletypecode,"
					+ " coalesce(jtemplatename->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "',jtemplatename->'sdisplayname'->>'en-US') as sdefaulttemplatename,jsondata "
					+ " from defaulttemplate where " + "nsitecode = " + userInfo.getNmastersitecode() + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsampletypecode="
					+ sampleTypeCode + subSampleTypeCode;
			List<DefaultTemplate> templateList = jdbcTemplate.query(getTemplateQry, new DefaultTemplate());

			if (!templateList.isEmpty()) {
				final List<DefaultTemplate> defaultList = templateList.stream()
						.filter(item -> item.getNdefaultstatus() == 3).collect(Collectors.toList());
				if (defaultList.isEmpty()) {
					defaultTemplate = templateList.get(0).getNdefaulttemplatecode();
					returnMap.put("selectedDefaultTemplate", templateList.get(0));
				} else {
					defaultTemplate = defaultList.get(0).getNdefaulttemplatecode();
					returnMap.put("selectedDefaultTemplate", defaultList.get(0));
				}
				returnMap.put("DefaultTemplateList", templateList);
			}
		}

		returnMap.put("ndefaulttemplatecode", defaultTemplate);

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRegistrationTemplateById(int nreactregtemplatecode, UserInfo userInfo)
			throws Exception {

		final String getRegistrationTemplate = "select rt.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, "
				+ " coalesce(dt.jtemplatename->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',dt.jtemplatename ->'sdisplayname'->>'en-US') as sdefaulttemplatename"
				+ " ,rt.jsondata::text as jsontext, " + " coalesce(st.jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',st.jsondata ->'sampletypename'->>'en-US')  as ssampletypename "
				+ " from reactregistrationtemplate rt,transactionstatus ts, defaulttemplate dt,sampletype st "
				+ " where ts.ntranscode= rt.ntransactionstatus and nreactregtemplatecode = " + nreactregtemplatecode
				+ " and rt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and rt.nsitecode = " + userInfo.getNmastersitecode() + " and dt.nsitecode = "
				+ userInfo.getNmastersitecode() + " and st.nsitecode = " + userInfo.getNmastersitecode()
				+ " and rt.ndefaulttemplatecode = dt.ndefaulttemplatecode and st.nsampletypecode=rt.nsampletypecode and"
				+ "  dt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<ReactRegistrationTemplate> registrationTemplate = jdbcTemplate.query(getRegistrationTemplate,
				new ReactRegistrationTemplate());

		if (!registrationTemplate.isEmpty()) {
			return new ResponseEntity<>(registrationTemplate.get(0), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	public ResponseEntity<Object> getApprovedRegistrationTemplate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final String getRegistrationTemplate = "select * from reactregistrationtemplate where nsampletypecode = "
				+ inputMap.get("nsampletypecode") + " and ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " " + " and nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<Map<String, Object>> registrationTemplate = jdbcTemplate.queryForList(getRegistrationTemplate);

		return new ResponseEntity<>(registrationTemplate, HttpStatus.OK);
	}

	public List<ReactRegistrationTemplate> getRegistrationTemplateByName(String templateName, UserInfo userInfo)
			throws Exception {

		final String getRegistrationTemplate = "select * from reactregistrationtemplate " + "where sregtemplatename = '"
				+ stringUtilityFunction.replaceQuote(templateName) + "' " + "and nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.query(getRegistrationTemplate, new ReactRegistrationTemplate());
	}

	@Override
	public ResponseEntity<Object> createRegistrationTemplate(ReactRegistrationTemplate regTemplate, UserInfo userInfo)
			throws Exception {

		List<ReactRegistrationTemplate> validateName = getRegistrationTemplateByName(regTemplate.getSregtemplatename(),
				userInfo);

		if (validateName.isEmpty()) {
			final String getSequenceNo = "select nsequenceno+1 from seqnoregtemplateversion where stablename ='reactregistrationtemplate'";
			final int seqNo = jdbcTemplate.queryForObject(getSequenceNo, Integer.class);

			final String insertRegTemplate = "insert into reactregistrationtemplate (nreactregtemplatecode,nsampletypecode,ntransactionstatus,sregtemplatename,jsondata,nstatus,nsitecode,dmodifieddate, ndefaulttemplatecode,nsubsampletypecode) values ("
					+ seqNo + "," + regTemplate.getNsampletypecode() + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",N'"
					+ stringUtilityFunction.replaceQuote(regTemplate.getSregtemplatename().toString()) + "','"
					+ stringUtilityFunction.replaceQuote(regTemplate.getJsonString()) + "'::jsonb,"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "," + userInfo.getNmastersitecode()
					+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ","
					+ regTemplate.getNdefaulttemplatecode() + "," + regTemplate.getNsubsampletypecode() + ");";

			final String updateSeqNo = "update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename = 'reactregistrationtemplate';";

			jdbcTemplate.execute(insertRegTemplate + updateSeqNo);
			regTemplate.setNreactregtemplatecode(seqNo);
			final List<Object> savedManufacturerList = new ArrayList<>();
			savedManufacturerList.add(regTemplate);
			auditUtilityFunction.fnInsertAuditAction(savedManufacturerList, 1, null,
					Arrays.asList("IDS_ADDTEMPLATEDESIGN"), userInfo);
			Map<String, Object> inputMap = new HashMap<>();
			inputMap.put("nsampletypecode", regTemplate.getNsampletypecode());
			inputMap.put("ndefaulttemplatecode", regTemplate.getNdefaulttemplatecode());

			return getRegistrationTemplate(inputMap, userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> updateRegistrationTemplate(ReactRegistrationTemplate regTemplate, UserInfo userInfo)
			throws Exception {
		ReactRegistrationTemplate oldTemplate = (ReactRegistrationTemplate) getRegistrationTemplateByIdCheck(
				regTemplate.getNreactregtemplatecode(), userInfo).getBody();
		if (oldTemplate == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (oldTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
			List<ReactRegistrationTemplate> validateName = getRegistrationTemplateByName(
					regTemplate.getSregtemplatename(), userInfo);
			if (validateName.isEmpty() || (!validateName.isEmpty()
					&& validateName.get(0).getNreactregtemplatecode() == regTemplate.getNreactregtemplatecode())) {
				final String updateQuery = "update reactregistrationtemplate set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
						// Added by Neeraj--ALPD-5275
						+ " sregtemplatename = '"
						+ stringUtilityFunction.replaceQuote(regTemplate.getSregtemplatename()) + "'," + " jsondata='"
						+ stringUtilityFunction.replaceQuote(regTemplate.getJsonString()) + "'::jsonb "
						+ " where nreactregtemplatecode = " + regTemplate.getNreactregtemplatecode();
				jdbcTemplate.execute(updateQuery);

				final List<Object> savedListAfterSave = new ArrayList<>();
				savedListAfterSave.add(regTemplate);

				final List<Object> savedListBeforeSave = new ArrayList<>();
				savedListBeforeSave.add(oldTemplate);

				auditUtilityFunction.fnInsertAuditAction(savedListAfterSave, 2, savedListBeforeSave,
						Arrays.asList("IDS_EDITTEMPLATEDESIGN"), userInfo);

				Map<String, Object> inputMap = new HashMap<>();
				inputMap.put("nsampletypecode", regTemplate.getNsampletypecode());
				inputMap.put("nreactregtemplatecode", regTemplate.getNreactregtemplatecode());
				inputMap.put("ndefaulttemplatecode", regTemplate.getNdefaulttemplatecode());

				return getRegistrationTemplate(inputMap, userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	public ResponseEntity<Object> getRegistrationTemplateByIdCheck(int nreactregtemplatecode, UserInfo userInfo)
			throws Exception {

		final String getRegistrationTemplate = "select rt.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus,"
				+ " coalesce(dt.jtemplatename->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',dt.jtemplatename ->'sdisplayname'->>'en-US') as sdefaulttemplatename,"
				+ " rt.jsondata::text as jsonString "
				+ " from reactregistrationtemplate rt,transactionstatus ts, defaulttemplate dt"
				+ " where ts.ntranscode= rt.ntransactionstatus  and nreactregtemplatecode = " + nreactregtemplatecode
				+ " and rt.nsitecode = " + userInfo.getNmastersitecode() + " and dt.nsitecode = "
				+ userInfo.getNmastersitecode() + " and rt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and rt.ndefaulttemplatecode = dt.ndefaulttemplatecode " + " and dt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final ReactRegistrationTemplate registrationTemplate = (ReactRegistrationTemplate) jdbcUtilityFunction
				.queryForObject(getRegistrationTemplate, ReactRegistrationTemplate.class, jdbcTemplate);

		return new ResponseEntity<>(registrationTemplate, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> deleteRegistrationTemplate(int nreactRegTemplateCode, UserInfo userInfo)
			throws Exception {

		final ReactRegistrationTemplate oldTemplate = (ReactRegistrationTemplate) getRegistrationTemplateByIdCheck(
				nreactRegTemplateCode, userInfo).getBody();
		if (oldTemplate == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			if (oldTemplate.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				final String updateQuery = "update reactregistrationtemplate set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nreactregtemplatecode = " + nreactRegTemplateCode;
				jdbcTemplate.execute(updateQuery);

				final List<Object> savedAfter = new ArrayList<>();
				oldTemplate.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedAfter.add(oldTemplate);

				auditUtilityFunction.fnInsertAuditAction(savedAfter, 1, null, Arrays.asList("IDS_DELETETEMPLATEDESIGN"),
						userInfo);
				Map<String, Object> inputMap = new HashMap<>();
				inputMap.put("nsampletypecode", oldTemplate.getNsampletypecode());
				inputMap.put("ndefaulttemplatecode", oldTemplate.getNdefaulttemplatecode());

				return getRegistrationTemplate(inputMap, userInfo);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTTEMPLATETODELETE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> approveRegistrationTemplate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final ReactRegistrationTemplate regTemplate = objMapper.convertValue(inputMap.get("registrationtemplate"),
				ReactRegistrationTemplate.class);
		final ReactRegistrationTemplate oldTemplate = (ReactRegistrationTemplate) getRegistrationTemplateByIdCheck(
				regTemplate.getNreactregtemplatecode(), userInfo).getBody();
		if (oldTemplate == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			if (oldTemplate.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				regTemplate.setStemplatetypesname(UUID.randomUUID().toString());
				if (inputMap.containsKey("nsampletypecode")) {
					if ((int) inputMap.get("nsampletypecode") == Enumeration.TransactionStatus.NA
							.gettransactionstatus()) {

						final String query = "update  reactregistrationtemplate set stemplatetypesname='"
								+ regTemplate.getStemplatetypesname() + "' , dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',jsondata='"
								+ stringUtilityFunction.replaceQuote(regTemplate.getJsonString()) + "'::jsonb,"
								+ " ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
								+ " where nreactregtemplatecode=" + regTemplate.getNreactregtemplatecode();
						jdbcTemplate.execute(query);

					} else {

						final String updateQuery = "update reactregistrationtemplate set stemplatetypesname='"
								+ regTemplate.getStemplatetypesname() + "' , dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = "
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
								+ " where nreactregtemplatecode = " + regTemplate.getNreactregtemplatecode();
						jdbcTemplate.execute(updateQuery);
					}

				}

				Map<String, Object> inputMap1 = new HashMap<>();
				inputMap1.put("nsampletypecode", oldTemplate.getNsampletypecode());
				inputMap1.put("nreactregtemplatecode", oldTemplate.getNreactregtemplatecode());
				inputMap1.put("ndefaulttemplatecode", oldTemplate.getNdefaulttemplatecode());
				List<String> lstFields = objMapper.convertValue(inputMap.get("dynamicFields"),
						new TypeReference<List<String>>() {
				});

				if ((int) inputMap.get("nsampletypecode") == Enumeration.SampleType.CLINICALSPEC.getType()) {
					lstFields.add("Patient Permanent Address");
					lstFields.add("Patient Current Address");
					lstFields.add("External Id");
					lstFields.add("Patient Passport No");
					lstFields.add("Patient Phone No");
					lstFields.add("Institution Code");
					lstFields.add("District/City");
					lstFields.add("Submitter Phone No");
				}

				StringJoiner joiner = new StringJoiner(",");
				lstFields.forEach(x -> joiner.add("\"" + x + "\" TEXT"));

				String query = "CREATE TYPE type_"
						+ regTemplate.getStemplatetypesname().replaceAll(" ", "").replaceAll("[^a-zA-Z0-9]", "")
						+ " as (" + joiner.toString() + ");";

				jdbcTemplate.execute(query);

				final List<Object> savedListBeforeSave = new ArrayList<>();
				savedListBeforeSave.add(oldTemplate);

				ReactRegistrationTemplate newReactRegistrationTemplate = SerializationUtils.clone(oldTemplate);
				newReactRegistrationTemplate
				.setNtransactionstatus((short) Enumeration.TransactionStatus.APPROVED.gettransactionstatus());

				final List<Object> savedListAfterSave = new ArrayList<>();
				savedListAfterSave.add(newReactRegistrationTemplate);

				auditUtilityFunction.fnInsertAuditAction(savedListAfterSave, 2, savedListBeforeSave,
						Arrays.asList("IDS_APPROVEDTEMPLATEDESIGN"), userInfo);

				return getRegistrationTemplate(inputMap1, userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTTEMPLATETOAPPROVE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> copyRegistrationTemplate(ReactRegistrationTemplate regTemplate, UserInfo userInfo)
			throws Exception {
		ReactRegistrationTemplate oldTemplate = (ReactRegistrationTemplate) getRegistrationTemplateByIdCheck(
				regTemplate.getNreactregtemplatecode(), userInfo).getBody();
		if (oldTemplate == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			List<ReactRegistrationTemplate> validateName = getRegistrationTemplateByName(
					regTemplate.getSregtemplatename(), userInfo);
			if (validateName.isEmpty()) {
				final String getSequenceNo = "select nsequenceno+1 from seqnoregtemplateversion where stablename ='reactregistrationtemplate'";
				final int seqNo = jdbcTemplate.queryForObject(getSequenceNo, Integer.class);
				String copyRegTemplate = "";
				if (oldTemplate.getNsampletypecode() == -1) {

					copyRegTemplate = "select *  from reactregistrationtemplate where nsitecode="
							+ userInfo.getNmastersitecode() + " and " + "nreactregtemplatecode = "
							+ regTemplate.getNreactregtemplatecode();

					ReactRegistrationTemplate data = (ReactRegistrationTemplate) jdbcUtilityFunction
							.queryForObject(copyRegTemplate, ReactRegistrationTemplate.class, jdbcTemplate);

					final ObjectMapper objMapper = new ObjectMapper();

					copyRegTemplate = "INSERT INTO public.reactregistrationtemplate(nreactregtemplatecode, nsampletypecode, ndefaulttemplatecode,"
							+ " ntransactionstatus, sregtemplatename, jsondata, nstatus,nsitecode,dmodifieddate,nsubsampletypecode)"
							+ " select " + seqNo + " nreactregtemplatecode, nsampletypecode, ndefaulttemplatecode,"
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " ntransactionstatus,N'"
							+ stringUtilityFunction.replaceQuote(regTemplate.getSregtemplatename())
							+ "' sregtemplatename, '"
							+ stringUtilityFunction.replaceQuote(
									objMapper.writeValueAsString(data.getJsondata()).replaceAll("_child", ""))
							+ "'::jsonb, nstatus," + userInfo.getNmastersitecode() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ regTemplate.getNsubsampletypecode()
							+ " from reactregistrationtemplate where nreactregtemplatecode = "
							+ regTemplate.getNreactregtemplatecode();

				} else {
					copyRegTemplate = "INSERT INTO public.reactregistrationtemplate(nreactregtemplatecode, nsampletypecode, ndefaulttemplatecode, ntransactionstatus, sregtemplatename, jsondata, nstatus,nsitecode,dmodifieddate,nsubsampletypecode)"
							+ " select " + seqNo + " nreactregtemplatecode, nsampletypecode, ndefaulttemplatecode,"
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " ntransactionstatus,N'"
							+ stringUtilityFunction.replaceQuote(regTemplate.getSregtemplatename())
							+ "' sregtemplatename, jsondata, nstatus, " + userInfo.getNmastersitecode() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ regTemplate.getNsubsampletypecode()
							+ " from reactregistrationtemplate where nreactregtemplatecode = "
							+ regTemplate.getNreactregtemplatecode();
				}

				final String updateSeqNo = "; update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename = 'reactregistrationtemplate';";
				jdbcTemplate.execute(copyRegTemplate + updateSeqNo);

				regTemplate.setNreactregtemplatecode(seqNo);
				regTemplate.setNtransactionstatus((short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
				regTemplate.setJsonString(oldTemplate.getJsonString());

				final List<Object> savedManufacturerList = new ArrayList<>();
				savedManufacturerList.add(regTemplate);
				auditUtilityFunction.fnInsertAuditAction(savedManufacturerList, 1, null,
						Arrays.asList("IDS_COPYTEMPLATEDESIGN"), userInfo);

				Map<String, Object> inputMap = new HashMap<>();
				inputMap.put("nsampletypecode", oldTemplate.getNsampletypecode());
				inputMap.put("ndefaulttemplatecode", oldTemplate.getNdefaulttemplatecode());

				return getRegistrationTemplate(inputMap, userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getComboValues(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		String tableName = "";
		final ObjectMapper objmapper = new ObjectMapper();
		List<Map<String, Object>> srcData = (List<Map<String, Object>>) inputMap.get("parentcolumnlist");
		Map<String, Object> childData = (Map<String, Object>) inputMap.get("childcolumnlist");
		Map<String, Object> parameters = (Map<String, Object>) inputMap.get("parameters");
		String getJSONKeysQuery = "";

		List<Map<String, Object>> filterQueryComponentsQueries = null;
		if (inputMap.containsKey("filterQueryComponents")) {
			filterQueryComponentsQueries = jdbcTemplate.queryForList("select nsqlquerycode,ssqlquery from "
					+ " sqlquery where nsqlquerycode in (" + inputMap.get("filterQueryComponents") + ")");
		}
		Map<String, Object> returnObject = new HashMap<>();
		for (int i = 0; i < srcData.size(); i++) {
			String sourceName = (String) srcData.get(i).get("source");
			String conditionString = srcData.get(i).containsKey("conditionstring")
					? (String) srcData.get(i).get("conditionstring")
							: "";
			String Keysofparam = "";

			while (conditionString.contains("P$")) {
				StringBuilder sb = new StringBuilder(conditionString);
				int firstindex = sb.indexOf("P$");
				int lastindex = sb.indexOf("$P");
				Keysofparam = sb.substring(firstindex + 2, lastindex);
				if (Keysofparam.contains(".")) {
					int index = Keysofparam.indexOf(".");
					String tablename = Keysofparam.substring(0, index);
					String columnName = Keysofparam.substring(index + 1, Keysofparam.length());
					if (inputMap.containsKey(tablename)) {
						Map<String, Object> userInfoMap = (Map<String, Object>) inputMap.get(tablename);
						if (userInfoMap.containsKey(columnName)) {
							sb.replace(firstindex, lastindex + 2, userInfoMap.get(columnName).toString());
						}
					}
				}
				conditionString = sb.toString();
			}
			final String scollate = "collate \"default\"";
			if (conditionString.contains("LIKE")) {
				while (conditionString.contains("LIKE")) {
					String sb = conditionString;
					String sQuery = conditionString;
					int colanindex = sb.indexOf("LIKE '");
					String str1 = sQuery.substring(0, colanindex + 6);
					sQuery = sQuery.substring(colanindex + 6);
					StringBuilder sb3 = new StringBuilder(str1);
					StringBuilder sb4 = new StringBuilder(sQuery);
					sb3.replace(colanindex, colanindex + 4, "ilike");
					System.out.println(sQuery);
					int indexofsv = sQuery.indexOf("'");

					sb4.replace(indexofsv, indexofsv + 1, "'" + scollate + " ");
					conditionString = sb3.toString() + sb4.toString();
				}
			}

			tableName = sourceName.toLowerCase();
			final String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
					+ tableName + "' and data_type = 'jsonb'";
			String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
			jsonField = jsonField != null ? "||" + jsonField : "";

			final String getDateFieldQuery = "select string_agg(column_name ,',')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
					+ tableName + "' and data_type = 'timestamp without time zone'";
			String jsonDateField = jdbcTemplate.queryForObject(getDateFieldQuery, String.class);
			List<String> lstDateField = null;
			if (jsonDateField != null) {
				lstDateField = new ArrayList<String>(Arrays.asList(jsonDateField.split(",")));
				if (lstDateField.contains("dmodifieddate")) {
					lstDateField.remove("dmodifieddate");
				}
			}

			if (srcData.get(i).containsKey("nsqlquerycode")) {
				final int j = i;
				getJSONKeysQuery = (String) filterQueryComponentsQueries.stream()
						.filter(temp -> temp.get("nsqlquerycode").equals(srcData.get(j).get("nsqlquerycode")))
						.map(temp1 -> temp1.get("ssqlquery")).findFirst().orElse("");
				while (getJSONKeysQuery.contains("P$")) {
					StringBuilder sb = new StringBuilder(getJSONKeysQuery);
					Map<String, Object> userInfoMap = objmapper.convertValue(userInfo,
							new TypeReference<Map<String, Object>>() {
					});
					int firstindex = sb.indexOf("P$");
					int lastindex = sb.indexOf("$P");
					Keysofparam = sb.substring(firstindex + 2, lastindex);
					if (userInfoMap.containsKey(Keysofparam)) {
						sb.replace(firstindex, lastindex + 2, userInfoMap.get(Keysofparam).toString());
					} else if (parameters.containsKey(Keysofparam)) {
						sb.replace(firstindex, lastindex + 2, parameters.get(Keysofparam).toString());
					} else {
						LOGGER.error(
								"PARAMETER NOT FOUND IN THE QUERY ===== Parameter cannot be replaced." + Keysofparam);
					}
					getJSONKeysQuery = sb.toString();
				}

			} else {
				final String getFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
						+ tableName + "'";
				String fields = jdbcTemplate.queryForObject(getFieldQuery, String.class);
				if (fields.contains(srcData.get(i).get("valuemember").toString())) {
					getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb" + jsonField + " as jsondata"
							+ " from " + tableName + " where nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "\""
							+ srcData.get(i).get("valuemember") + "\"" + " > '0' " + conditionString + " ;";
				} else {
					getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb" + jsonField + " as jsondata"
							+ " from " + tableName + " where nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + conditionString
							+ " ;";
				}
			}

			List<Combopojo> data = jdbcTemplate.query(getJSONKeysQuery, new Combopojo());
			System.out.println("Combo Query:" + getJSONKeysQuery);
			String label = (String) srcData.get(i).get("label");
			if (!data.isEmpty() && childData != null && childData.containsKey(label)) {
				List<Combopojo> lstData = data.stream()
						.filter(x -> x.getJsondata().containsKey("ndefaultstatus")
								&& (int) x.getJsondata().get("ndefaultstatus") == Enumeration.TransactionStatus.YES
								.gettransactionstatus())
						.collect(Collectors.toList());
				if (!lstData.isEmpty()) {
					Map<String, Object> childDatas = lstData.get(0).getJsondata();
					Map<String, Object> datas = recursiveChildCombo(childDatas, childData, srcData.get(i), label,
							(String) srcData.get(i).get("valuemember"), tableName, userInfo, inputMap);
					returnObject.putAll(datas);
				}
			}
			final ObjectMapper objectMapper = new ObjectMapper();
			if (lstDateField != null && !lstDateField.isEmpty()) {
				data = objectMapper.convertValue(
						getSiteLocalTimeFromUTCForDynamicSource(data, userInfo, true, lstDateField),
						new TypeReference<List<Combopojo>>() {
						});
			}
			returnObject.put(label, data);
		}
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> recursiveChildCombo(Map<String, Object> childDatas, Map<String, Object> childData,
			Map<String, Object> map, String label, String valueMember, String tableName, UserInfo userInfo,
			Map<String, Object> inputMap) throws Exception {

		final String parentValumember = valueMember;
		final Map<String, Object> objReturn = new HashMap();
		List<Map<String, Object>> childcomboData = (List<Map<String, Object>>) childData.get(label);
		for (int z = 0; z < childcomboData.size(); z++) {
			if (!childcomboData.get(z).containsKey("readonly") || (childcomboData.get(z).containsKey("readonly")
					&& ((boolean) childcomboData.get(z).get("readonly")) == false)) {
				final String sourceName1 = (String) childcomboData.get(z).get("source");
				String labelchild = (String) childcomboData.get(z).get("label");
				List<Map<String, Object>> objChild = ((List<Map<String, Object>>) map.get("child")).stream()
						.filter(x -> x.get("label").equals(labelchild)).collect(Collectors.toList());

				final String valuememberchild = (String) childcomboData.get(z).get("valuemember");
				final String tableNamechild = sourceName1.toLowerCase();
				String Keysofparam = "";
				String conditionString = childcomboData.get(z).containsKey("conditionstring")
						? (String) childcomboData.get(z).get("conditionstring")
								: "";

				while (conditionString.contains("P$")) {
					StringBuilder sb = new StringBuilder(conditionString);
					int firstindex = sb.indexOf("P$");
					int lastindex = sb.indexOf("$P");
					Keysofparam = sb.substring(firstindex + 2, lastindex);
					if (Keysofparam.contains(".")) {
						int index = Keysofparam.indexOf(".");
						String tablename = Keysofparam.substring(0, index);
						String columnName = Keysofparam.substring(index + 1, Keysofparam.length());
						if (inputMap.containsKey(tablename)) {
							Map<String, Object> userInfoMap = (Map<String, Object>) inputMap.get(tablename);
							if (userInfoMap.containsKey(columnName)) {
								sb.replace(firstindex, lastindex + 2, userInfoMap.get(columnName).toString());
							}
						}
					}
					conditionString = sb.toString();

				}
				String valuememberData = valueMember;

				String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
						+ tableNamechild + "' and data_type = 'jsonb'";
				String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);

				final String getDateFieldQuery = "select string_agg(column_name ,',')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
						+ tableNamechild + "' and data_type = 'timestamp without time zone'";
				String jsonDateField = jdbcTemplate.queryForObject(getDateFieldQuery, String.class);
				List<String> lstDateField = null;
				if (jsonDateField != null) {
					lstDateField = new ArrayList<String>(Arrays.asList(jsonDateField.split(",")));
					if (lstDateField.contains("dmodifieddate")) {
						lstDateField.remove("dmodifieddate");
					}
				}

				getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
						+ tableNamechild + "' ";
				String jsonchildField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
				if (!(jsonchildField.contains(valueMember + "||") || jsonchildField.contains("||" + valueMember + "||")
						|| jsonchildField.contains("||" + valueMember))) {
					String tablecolumnname = objChild.isEmpty() ? "" : (String) objChild.get(0).get("tablecolumnname");

					String foreignPk = objChild.isEmpty() ? "" : (String) objChild.get(0).get("foriegntablePK");

					if (!childDatas.containsKey(tablecolumnname)) {
						valueMember = tablecolumnname;
						valuememberData = foreignPk;
					} else {
						valueMember = foreignPk;
						valuememberData = tablecolumnname;
					}
				}

				String defaultvalues = "";
				if (childcomboData.get(z).containsKey("defaultvalues")) {
					List<Map<String, Object>> defaulvalueslst = (List<Map<String, Object>>) childcomboData.get(z)
							.get("defaultvalues");
					for (int j = 0; j < defaulvalueslst.size(); j++) {
						if (defaulvalueslst.get(j).containsKey(
								childDatas.get(childcomboData.get(z).get("parentprimarycode")).toString())) {
							defaultvalues = " in ( " + defaulvalueslst.get(j)
							.get(childDatas.get(childcomboData.get(z).get("parentprimarycode")).toString())
							.toString() + " )";
						}
					}

				} else {
					if (!objChild.isEmpty() && objChild.get(0).containsKey("isDynamicMapping")
							&& (boolean) objChild.get(0).get("isDynamicMapping")) {
						if (childDatas.containsKey("jsondata")) {
							String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
							Map<String, Object> objJsonData = (Map<String, Object>) childDatas.get("jsondata");
							if (objJsonData.containsKey(tablecolumnname)) {
								Map<String, Object> objJsonData1 = (Map<String, Object>) objJsonData
										.get(tablecolumnname);
								defaultvalues = " = '" + objJsonData1.get("value") + "'";

							} else {
								defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
							}
						} else {
							defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
						}

					} else if (parentValumember.equals("ndynamicmastercode") && !objChild.isEmpty()
							&& (boolean) objChild.get(0).get("isDynamicMapping") == false) {
						if (childDatas.containsKey("jsondata")) {
							String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
							Map<String, Object> objJsonData = (Map<String, Object>) childDatas.get("jsondata");
							if (objJsonData.containsKey(tablecolumnname)) {
								Map<String, Object> objJsonData1 = (Map<String, Object>) objJsonData
										.get(tablecolumnname);
								defaultvalues = " = '" + objJsonData1.get("value") + "'";

							} else {
								defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
							}
						} else {
							defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
						}
					} else {

						defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
						if (userInfo.getNformcode() == Enumeration.QualisForms.SCHEDULERCONFIGURATION.getqualisforms()
								&& parentValumember.equals("ninstrumentcatcode")
								&& !inputMap.containsKey("isInstrumentScheduler")) {
							defaultvalues = defaultvalues + " and nregionalsitecode=" + userInfo.getNsitecode()
							+ " and nautocalibration="
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "";
						}
					}
				}
				String name = "";
				if (childcomboData.get(z).containsKey("name")) {
					name = (String) childcomboData.get(z).get("name");
					if ((name.equals("manualorderid") || name.equals("manualsampleid"))
							&& childDatas.containsKey("nordertypecode")
							&& (int) childDatas.get("nordertypecode") == Enumeration.TransactionStatus.ACTIVE
							.gettransactionstatus()) {

						String additionalConditionString = " AND sorderseqno ='-1' ";

						conditionString = conditionString + additionalConditionString + "  limit 1";

					} else if ((name.equals("manualorderid") || name.equals("manualsampleid"))
							&& childDatas.containsKey("nordertypecode")
							&& (int) childDatas.get("nordertypecode") == Enumeration.TransactionStatus.OUTSIDE
							.gettransactionstatus()) {
						if (conditionString.contains("nusercode")) {
							String first = conditionString.substring(0, conditionString.indexOf("nusercode = ") + 12);
							String second = conditionString.substring(conditionString.indexOf("nusercode = ") + 12);
							if (second.contains("and")) {
								String third = second.substring(second.indexOf("and"));
								first = first + "-1 " + third;
								conditionString = first;
							} else {
								first = first + "-1";
								conditionString = first;
							}
						}
					}
				}
				if (!objChild.isEmpty() && objChild.get(0).containsKey("isDynamicMapping")
						&& (boolean) objChild.get(0).get("isDynamicMapping")) {
					String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
					if (!(jsonchildField.contains(tablecolumnname + "||")
							|| jsonchildField.contains("||" + tablecolumnname + "||")
							|| jsonchildField.contains("||" + tablecolumnname))) {
						valueMember = "ndynamicmastercode";
					} else {
						valueMember = "jsondata->" + "'" + tablecolumnname + "'->>'value'";
					}
				} else {
					valueMember = "\"" + valueMember + "\"";
				}
				jsonField = jsonField != null ? "||" + jsonField : "";
				getJSONFieldQuery = "select TO_JSON(" + tableNamechild + ".*)::jsonb" + jsonField + "  as jsondata"
						+ " from " + tableNamechild + " where nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  " + valueMember
						+ defaultvalues + conditionString + ";";

				System.out.println(" query recursiveChildCombo : " + getJSONFieldQuery);
				List<Combopojo> data = jdbcTemplate.query(getJSONFieldQuery, new Combopojo());

				if (!data.isEmpty() && childData != null && childData.containsKey(labelchild)) {
					List<Combopojo> lstData = data.stream()
							.filter(x -> x.getJsondata().containsKey("ndefaultstatus")
									&& (int) x.getJsondata().get("ndefaultstatus") == Enumeration.TransactionStatus.YES
									.gettransactionstatus())
							.collect(Collectors.toList());
					if (!lstData.isEmpty()) {
						Map<String, Object> child = lstData.get(0).getJsondata();
						final Map<String, Object> datas = recursiveChildCombo(child, childData, childcomboData.get(z),
								labelchild, valuememberchild, tableNamechild, userInfo, inputMap);
						objReturn.putAll(datas);
					}
				}

				final ObjectMapper objectMapper = new ObjectMapper();
				if (lstDateField != null && !lstDateField.isEmpty()) {
					data = objectMapper.convertValue(
							getSiteLocalTimeFromUTCForDynamicSource(data, userInfo, true, lstDateField),
							new TypeReference<List<Combopojo>>() {
							});
				}
				objReturn.put(labelchild, data);
			}
		}
		return objReturn;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> recursiveChildComboForOrderType(Map<String, Object> childDatas,
			Map<String, Object> childData, Map<String, Object> map, String label, String valueMember, String tableName,
			UserInfo userInfo, Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> objReturn = new HashMap();
		List<Map<String, Object>> childcomboData = (List<Map<String, Object>>) childData.get(label);

		for (int z = 0; z < childcomboData.size(); z++) {
			final String sourceName1 = (String) childcomboData.get(z).get("source");
			String labelchild = (String) childcomboData.get(z).get("label");
			final String valuememberchild = (String) childcomboData.get(z).get("valuemember");
			final String tableNamechild = sourceName1.toLowerCase();
			String Keysofparam = "";
			String conditionString = childcomboData.get(z).containsKey("conditionstring")
					? (String) childcomboData.get(z).get("conditionstring")
							: "";

			while (conditionString.contains("P$")) {
				StringBuilder sb = new StringBuilder(conditionString);
				int firstindex = sb.indexOf("P$");
				int lastindex = sb.indexOf("$P");
				Keysofparam = sb.substring(firstindex + 2, lastindex);
				if (Keysofparam.contains(".")) {
					int index = Keysofparam.indexOf(".");
					String tablename = Keysofparam.substring(0, index);
					String columnName = Keysofparam.substring(index + 1, Keysofparam.length());
					if (inputMap.containsKey(tablename)) {
						Map<String, Object> userInfoMap = (Map<String, Object>) inputMap.get(tablename);
						if (userInfoMap.containsKey(columnName)) {
							sb.replace(firstindex, lastindex + 2, userInfoMap.get(columnName).toString());
						}
					}
				}
				conditionString = sb.toString();

			}
			String valuememberData = "nordertypecode";

			String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
					+ tableNamechild + "' and data_type = 'jsonb'";

			String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
			final String getDateFieldQuery = "select string_agg(column_name ,',')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
					+ tableNamechild + "' and data_type = 'timestamp without time zone'";

			String jsonDateField = jdbcTemplate.queryForObject(getDateFieldQuery, String.class);

			List<String> lstDateField = null;
			if (jsonDateField != null) {
				lstDateField = new ArrayList<String>(Arrays.asList(jsonDateField.split(",")));
				if (lstDateField.contains("dmodifieddate")) {
					lstDateField.remove("dmodifieddate");
				}
			}

			String defaultvalues = " = '" + childDatas.get(valuememberData) + "'";

			String name = "";
			if (childcomboData.get(z).containsKey("name")) {
				name = (String) childcomboData.get(z).get("name");
				if (name.equals("manualorderid") && childDatas.containsKey("nordertypecode")
						&& (int) childDatas.get("nordertypecode") == 1) {
					String additionalConditionString = " AND sorderseqno ='-1' ";
					conditionString = conditionString + additionalConditionString + "  limit 1";
				} else if (name.equals("manualorderid") && childDatas.containsKey("nordertypecode")
						&& (int) childDatas.get("nordertypecode") == 2) {
					if (conditionString.contains("nusercode")) {
						String first = conditionString.substring(0, conditionString.indexOf("nusercode = ") + 12);
						String second = conditionString.substring(conditionString.indexOf("nusercode = ") + 12);
						if (second.contains("and")) {
							String third = second.substring(second.indexOf("and"));
							first = first + "-1 " + third;
							conditionString = first;
						} else {
							first = first + "-1";
							conditionString = first;
						}
					}
				}
			}

			jsonField = jsonField != null ? "||" + jsonField : "";
			getJSONFieldQuery = "select TO_JSON(" + tableNamechild + ".*)::jsonb" + jsonField + "  as jsondata"
					+ " from " + tableNamechild + " where nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and \"" + valueMember + "\""
					+ defaultvalues + conditionString + ";";
			System.out.println("como query: " + getJSONFieldQuery);

			List<Combopojo> data = jdbcTemplate.query(getJSONFieldQuery, new Combopojo());

			if (!data.isEmpty() && childData != null && childData.containsKey(labelchild)) {
				List<Combopojo> lstData = data.stream()
						.filter(x -> x.getJsondata().containsKey("ndefaultstatus")
								&& (int) x.getJsondata().get("ndefaultstatus") == Enumeration.TransactionStatus.YES
								.gettransactionstatus())
						.collect(Collectors.toList());
				if (!lstData.isEmpty()) {

					Map<String, Object> child = lstData.get(0).getJsondata();

					final Map<String, Object> datas = recursiveChildCombo(child, childData, childcomboData.get(z),
							labelchild, valuememberchild, tableNamechild, userInfo, inputMap);
					objReturn.putAll(datas);
				}
			}
			final ObjectMapper objectMapper = new ObjectMapper();
			if (lstDateField != null && !lstDateField.isEmpty()) {
				data = objectMapper.convertValue(
						getSiteLocalTimeFromUTCForDynamicSource(data, userInfo, true, lstDateField),
						new TypeReference<List<Combopojo>>() {
						});
			}
			objReturn.put(labelchild, data);
		}
		return objReturn;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getChildValues(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		String tableName = "";

		final List<Map<String, Object>> srcData = (List<Map<String, Object>>) inputMap.get("parentcolumnlist");
		Map<String, Object> childData = (Map<String, Object>) inputMap.get("childcolumnlist");
		Map<String, Object> parentData = (Map<String, Object>) inputMap.get("parentdata");
		final Map<String, Object> returnObject = new HashMap<>();
		String valuememberData = "";
		for (int i = 0; i < srcData.size(); i++) {

			if (!srcData.get(i).containsKey("readonly") || (srcData.get(i).containsKey("readonly")
					&& ((boolean) srcData.get(i).get("readonly")) == false)) {
				String valuemember = (String) inputMap.get("valuemember");
				final String sourceName = (String) srcData.get(i).get("source");
				final String valuememberChild = (String) srcData.get(i).get("valuemember");
				final String label = (String) srcData.get(i).get("label");

				List<Map<String, Object>> objChild = ((List<Map<String, Object>>) inputMap.get("child")).stream()
						.filter(x -> x.get("label").equals(label)).collect(Collectors.toList());

				String Keysofparam = "";
				String conditionString = srcData.get(i).containsKey("conditionstring")
						? (String) srcData.get(i).get("conditionstring")
								: "";

				while (conditionString.contains("P$")) {
					StringBuilder sb = new StringBuilder(conditionString);
					int firstindex = sb.indexOf("P$");
					int lastindex = sb.indexOf("$P");
					Keysofparam = sb.substring(firstindex + 2, lastindex);
					if (Keysofparam.contains(".")) {
						int index = Keysofparam.indexOf(".");
						String tablename = Keysofparam.substring(0, index);
						String columnName = Keysofparam.substring(index + 1, Keysofparam.length());
						if (inputMap.containsKey(tablename)) {
							Map<String, Object> userInfoMap = (Map<String, Object>) inputMap.get(tablename);
							if (userInfoMap.containsKey(columnName)) {
								sb.replace(firstindex, lastindex + 2, userInfoMap.get(columnName).toString());
							}
						}
					}
					conditionString = sb.toString();

				}
				tableName = sourceName.toLowerCase();
				valuememberData = valuemember;

				final String getDateFieldQuery = "select string_agg(column_name ,',')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
						+ tableName + "' and data_type = 'timestamp without time zone'";
				String jsonDateField = jdbcTemplate.queryForObject(getDateFieldQuery, String.class);
				List<String> lstDateField = null;
				if (jsonDateField != null) {
					lstDateField = new ArrayList<String>(Arrays.asList(jsonDateField.split(",")));
					if (lstDateField.contains("dmodifieddate")) {
						lstDateField.remove("dmodifieddate");
					}
				}

				String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
						+ tableName + "' and data_type = 'jsonb'";

				String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);

				getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
						+ tableName + "' ";
				String jsonchildField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
				if (!(jsonchildField.contains(valuemember + "||") || jsonchildField.contains("||" + valuemember + "||")
						|| jsonchildField.contains("||" + valuemember))) {
					String tablecolumnname = objChild.isEmpty() ? "" : (String) objChild.get(0).get("tablecolumnname");
					String foreignPk = objChild.isEmpty() ? "" : (String) objChild.get(0).get("foriegntablePK");

					if (!parentData.containsKey(tablecolumnname)) {
						valuemember = tablecolumnname;
						valuememberData = foreignPk;
					} else {

						valuemember = foreignPk;
						valuememberData = tablecolumnname;
					}
				}
				jsonField = jsonField != null ? "||" + jsonField : "";

				String defaultvalues = "";
				if (srcData.get(i).containsKey("defaultvalues")) {
					List<Map<String, Object>> defaulvalueslst = (List<Map<String, Object>>) srcData.get(i)
							.get("defaultvalues");
					for (int j = 0; j < defaulvalueslst.size(); j++) {
						if (defaulvalueslst.get(j)
								.containsKey(parentData.get(srcData.get(i).get("parentprimarycode")).toString())) {
							defaultvalues = " in ( " + defaulvalueslst.get(j)
							.get(parentData.get(srcData.get(i).get("parentprimarycode")).toString()).toString()
							+ " )";
						}
					}

				} else {
					if (!objChild.isEmpty() && objChild.get(0).containsKey("isDynamicMapping")
							&& (boolean) objChild.get(0).get("isDynamicMapping")) {

						if (parentData.containsKey("jsondata")) {
							String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
							Map<String, Object> objJsonData = (Map<String, Object>) parentData.get("jsondata");
							if (objJsonData.containsKey(tablecolumnname)) {

								try {
									Map<String, Object> objJsonData1 = (Map<String, Object>) objJsonData
											.get(tablecolumnname);
									defaultvalues = " = '" + objJsonData1.get("value") + "'";
								} catch (Exception e) {
									defaultvalues = " = '" + parentData.get(valuememberData) + "'";
								}
							} else {
								defaultvalues = " = '" + parentData.get(valuememberData) + "'";
							}
						} else {
							defaultvalues = " = '" + parentData.get(valuememberData) + "'";
						}

					} else if (((String) inputMap.get("valuemember")).equals("ndynamicmastercode")
							&& !objChild.isEmpty() && (boolean) objChild.get(0).get("isDynamicMapping") == false) {

						if (parentData.containsKey("jsondata")) {
							String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
							Map<String, Object> objJsonData = (Map<String, Object>) parentData.get("jsondata");
							if (objJsonData.containsKey(tablecolumnname)) {

								try {
									Map<String, Object> objJsonData1 = (Map<String, Object>) objJsonData
											.get(tablecolumnname);
									defaultvalues = " = '" + objJsonData1.get("value") + "'";
								} catch (Exception e) {
									defaultvalues = " = '" + parentData.get(valuememberData) + "'";
								}

							} else {
								defaultvalues = " = '" + parentData.get(valuememberData) + "'";
							}
						} else {
							defaultvalues = " = '" + parentData.get(valuememberData) + "'";
						}

					}

					else {
						defaultvalues = " = '" + parentData.get(valuememberData) + "'";
					}

				}

				String name = "";
				if (srcData.get(i).containsKey("name")) {
					name = (String) srcData.get(i).get("name");
					if ((name.equals("manualorderid") || name.equals("manualsampleid"))
							&& parentData.containsKey("nordertypecode")
							&& (int) parentData.get("nordertypecode") == Enumeration.TransactionStatus.ACTIVE
							.gettransactionstatus()) {

						String additionalConditionString = " AND sorderseqno ='-1' ";

						conditionString = conditionString + additionalConditionString + "  limit 1";
					} else if ((name.equals("manualorderid") || name.equals("manualsampleid"))
							&& parentData.containsKey("nordertypecode")
							&& (int) parentData.get("nordertypecode") == Enumeration.TransactionStatus.OUTSIDE
							.gettransactionstatus()) {
						if (conditionString.contains("nusercode")) {
							String first = conditionString.substring(0, conditionString.indexOf("nusercode = ") + 12);
							String second = conditionString.substring(conditionString.indexOf("nusercode = ") + 12);
							if (second.contains("and")) {
								String third = second.substring(second.indexOf("and"));
								first = first + "-1 " + third;
								conditionString = first;
							} else {
								first = first + "-1";
								conditionString = first;
							}
						}

					} else if (name.equals("Instrument")) {
						int nistrumnetcatcode = (int) parentData.get("ninstrumentcatcode");
						String instrumentCatName = "  and ninstrumentcatcode = ";
						conditionString = instrumentCatName + nistrumnetcatcode + conditionString;
					} else if (name.equals("Plant Order")) {
						int nplantcode = (int) parentData.get("nplantcode");
						String plantName = "  and nplantcode = ";
						conditionString = plantName + nplantcode + conditionString;
					}
				}
				if (!objChild.isEmpty() && objChild.get(0).containsKey("isDynamicMapping")
						&& (boolean) objChild.get(0).get("isDynamicMapping")) {
					String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
					if (!(jsonchildField.contains(tablecolumnname + "||")
							|| jsonchildField.contains("||" + tablecolumnname + "||")
							|| jsonchildField.contains("||" + tablecolumnname))) {
						valuemember = "ndynamicmastercode";
					} else {
						valuemember = "jsondata->" + "'" + tablecolumnname + "'->>'value'";
					}
				} else if ((inputMap.containsKey("isInstrumentScheduler")
						&& (boolean) inputMap.get("isInstrumentScheduler"))) {

					valuemember = "\"" + valuemember + "\"";
					conditionString = conditionString + " and nautocalibration="
							+ Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ "   and    nregionalsitecode= '" + inputMap.get("nregionalsitecode") + "'";

				}

				else {
					valuemember = "\"" + valuemember + "\"";
				}
				getJSONFieldQuery = "select TO_JSON(" + tableName + ".*)::jsonb" + jsonField + "  as jsondata"
						+ " from " + tableName + " where nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  " + valuemember
						+ defaultvalues + conditionString + ";";

				System.out.println("Combo Query1:" + getJSONFieldQuery);

				List<Combopojo> data = jdbcTemplate.query(getJSONFieldQuery, new Combopojo());

				if (!data.isEmpty() && childData != null && childData.containsKey(label)
						&& (!srcData.get(i).containsKey("name")
								|| !srcData.get(i).get("name").equals("manualsampleid"))) {

					for (int j = 0; j < data.size(); j++) {
						Map<String, Object> child = data.get(j).getJsondata();
						if (child.containsKey("ndefaultstatus")) {
							if ((int) child.get("ndefaultstatus") == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
								Map<String, Object> datas = recursiveChildCombo(child, childData, srcData.get(i), label,
										valuememberChild, tableName, userInfo, inputMap);
								returnObject.putAll(datas);
								break;
							}
						} else {
							break;
						}
					}
				} else if (srcData.get(i).get("name") != null && srcData.get(i).containsKey("name")
						&& srcData.get(i).get("name").equals("manualsampleid")
						&& valuememberData.equals("nordertypecode") && (int) parentData.get(valuememberData) == 2) {

					List<Combopojo> lstData = data.stream()
							.filter(x -> x.getJsondata().containsKey("ndefaultstatus")
									&& (int) x.getJsondata().get("ndefaultstatus") == Enumeration.TransactionStatus.YES
									.gettransactionstatus())
							.collect(Collectors.toList());
					if (!lstData.isEmpty()) {

						Map<String, Object> child = lstData.get(0).getJsondata();
						if (child.containsKey("ndefaultstatus")) {
							if ((int) child.get("ndefaultstatus") == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
								Map<String, Object> datas = recursiveChildCombo(child, childData, srcData.get(i), label,
										valuememberChild, tableName, userInfo, inputMap);
								returnObject.putAll(datas);
								break;
							}
						} else {
							break;
						}
					}

				} else if (childData != null && childData.containsKey(label) && valuememberData.equals("nordertypecode")
						&& (int) parentData.get(valuememberData) == Enumeration.TransactionStatus.ACTIVE
						.gettransactionstatus()) {

					List<Map<String, Object>> lst = (List<Map<String, Object>>) childData.get(label);
					if (lst.stream().anyMatch(x -> x.get("name").equals("manualorderid"))) {
						Map<String, Object> datas = recursiveChildComboForOrderType(parentData, childData,
								srcData.get(i), label, valuememberData, tableName, userInfo, inputMap);
						returnObject.putAll(datas);
					}

				}
				final ObjectMapper objectMapper = new ObjectMapper();
				if (lstDateField != null && !lstDateField.isEmpty()) {
					data = objectMapper.convertValue(
							getSiteLocalTimeFromUTCForDynamicSource(data, userInfo, true, lstDateField),
							new TypeReference<List<Combopojo>>() {
							});
				}
				returnObject.put(label, data);
			}
		}
		LOGGER.info("Combo Change method  End========" + LocalDateTime.now());
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	@Override
	public List<QueryBuilderTables> getComboTables(UserInfo userInfo) throws Exception {

		final String getTableQuery = "select nquerybuildertablecode,nformcode,stablename,coalesce(jsondata->'tablename'->>'"
				+ userInfo.getSlanguagetypecode() + "', jsondata->'tablename'->>'en-US')"
				+ " as sdisplayname,jsondata->>'methodUrl' methodUrl,jsondata->>'classUrl' classUrl,jsondata->>'component' component"
				+ ",cast (jsondata->>'isMasterAdd' as boolean) isMasterAdd,cast(jsondata->>'addControlCode' as integer) addControlCode,cast(jsondata->>'editControlCode' as integer) editControlCode "
				+ " from querybuildertables  where nsitecode=" + userInfo.getNmastersitecode() + " "
				+ "and nismastertable = " + Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return jdbcTemplate.query(getTableQuery, new QueryBuilderTables());
	}

	@Override
	public ResponseEntity<Object> getTableColumns(String tableName, UserInfo userInfo) throws Exception {

		final String getTableColumnQuery = "select * from querybuildertablecolumns where nsitecode="
				+ userInfo.getNmastersitecode() + "  and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nquerybuildertablecode ='"
				+ tableName + "'";

		return new ResponseEntity<>(
				jdbcUtilityFunction.queryForObject(getTableColumnQuery, QueryBuilderTableColumns.class, jdbcTemplate),
				HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getMasterDesign(UserInfo userInfo) {

		final String getDesignQuery = "select mp.jsondata screendesign,temp.jsondata as slideoutdesign"
				+ " from reactregistrationtemplate temp,designtemplatemapping tm,mappedtemplatefieldprops mp"
				+ " where mp.nsitecode = " + userInfo.getNmastersitecode() + " and tm.nsitecode = "
				+ userInfo.getNmastersitecode() + " and temp.nsitecode = " + userInfo.getNmastersitecode()
				+ " and mp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and temp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tm.ndesigntemplatemappingcode = mp.ndesigntemplatemappingcode and temp.nreactregtemplatecode = tm.nreactregtemplatecode"
				+ " and tm.nformcode = " + userInfo.getNformcode() + " and tm.ntransactionstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

		return new ResponseEntity<>(jdbcTemplate.query(getDesignQuery, new ReactRegistrationTemplate()), HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getComboValuesForEdit(Map<String, Object> map, Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		String tableName = "";
		Map<String, Object> returnObject = new HashMap<>();
		final ObjectMapper objmapper = new ObjectMapper();
		List<Map<String, Object>> srcData = (List<Map<String, Object>>) inputMap.get("parentcolumnlist");
		Map<String, Object> childData = (Map<String, Object>) inputMap.get("childcolumnlist");
		Map<String, Object> parameters = (Map<String, Object>) inputMap.get("parameters");
		Map<String, Object> editData = (Map<String, Object>) map.get("EditData");
		String getJSONKeysQuery = "";

		List<Map<String, Object>> filterQueryComponentsQueries = null;

		if (inputMap.containsKey("filterQueryComponents")) {
			filterQueryComponentsQueries = jdbcTemplate.queryForList("select nsqlquerycode,ssqlquery from "
					+ " sqlquery where nsqlquerycode in (" + inputMap.get("filterQueryComponents") + ")");
		}

		for (int i = 0; i < srcData.size(); i++) {
			String sourceName = (String) srcData.get(i).get("source");
			String conditionString = srcData.get(i).containsKey("conditionstring")
					? (String) srcData.get(i).get("conditionstring")
							: "";
			String Keysofparam = "";
			while (conditionString.contains("P$")) {
				StringBuilder sb = new StringBuilder(conditionString);
				int firstindex = sb.indexOf("P$");
				int lastindex = sb.indexOf("$P");
				Keysofparam = sb.substring(firstindex + 2, lastindex);
				if (Keysofparam.contains(".")) {
					int index = Keysofparam.indexOf(".");
					String tablename = Keysofparam.substring(0, index);
					String columnName = Keysofparam.substring(index + 1, Keysofparam.length());
					if (inputMap.containsKey(tablename)) {
						Map<String, Object> userInfoMap = (Map<String, Object>) inputMap.get(tablename);
						if (userInfoMap.containsKey(columnName)) {
							sb.replace(firstindex, lastindex + 2, userInfoMap.get(columnName).toString());
						}
					}
				}
				conditionString = sb.toString();

			}
			tableName = sourceName.toLowerCase();

			if (srcData.get(i).containsKey("nsqlquerycode")) {
				final int j = i;
				getJSONKeysQuery = (String) filterQueryComponentsQueries.stream()
						.filter(temp -> temp.get("nsqlquerycode").equals(srcData.get(j).get("nsqlquerycode")))
						.map(temp1 -> temp1.get("ssqlquery")).findFirst().orElse("");
				while (getJSONKeysQuery.contains("P$")) {
					StringBuilder sb = new StringBuilder(getJSONKeysQuery);
					Map<String, Object> userInfoMap = objmapper.convertValue(userInfo,
							new TypeReference<Map<String, Object>>() {
					});
					int firstindex = sb.indexOf("P$");
					int lastindex = sb.indexOf("$P");
					Keysofparam = sb.substring(firstindex + 2, lastindex);
					if (userInfoMap.containsKey(Keysofparam)) {
						sb.replace(firstindex, lastindex + 2, userInfoMap.get(Keysofparam).toString());
					} else if (parameters.containsKey(Keysofparam)) {
						sb.replace(firstindex, lastindex + 2, parameters.get(Keysofparam).toString());
					} else {
						LOGGER.error(
								"PARAMETER NOT FOUND IN THE QUERY ===== Parameter cannot be replaced." + Keysofparam);
					}

					getJSONKeysQuery = sb.toString();
				}

			} else {
				final String getFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
						+ tableName + "'";
				String fields = jdbcTemplate.queryForObject(getFieldQuery, String.class);
				if (fields.contains(srcData.get(i).get("valuemember").toString())) {
					getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb as jsondata" + " from " + tableName
							+ " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and \"" + srcData.get(i).get("valuemember") + "\" > '0' " + conditionString + " ;";
				} else {
					getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb as jsondata" + " from " + tableName
							+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ conditionString + " ;";
				}
			}
			System.out.println("Combo Query:" + getJSONKeysQuery);
			List<Combopojo> data = jdbcTemplate.query(getJSONKeysQuery, new Combopojo());
			String label = (String) srcData.get(i).get("label");

			if (childData != null && childData.containsKey(label)) {
				Map<String, Object> parentComboObj = null;
				if (editData.get(label) instanceof Map<?, ?>) {
					parentComboObj = (Map<String, Object>) editData.get(label);
					final Map<String, Object> parentComboObj1 = (Map<String, Object>) editData.get(label);
					if (parentComboObj.containsKey("value")) {
						final int k = i;
						List<Combopojo> data1 = data
								.stream().filter(x -> x.getJsondata()
										.get(srcData.get(k).get("valuemember")) == parentComboObj1.get("value"))
								.collect(Collectors.toList());
						if (!data1.isEmpty()) {
							Map<String, Object> child = data1.get(0).getJsondata();
							Map<String, Object> datas = recursiveChildComboForEdit(child, childData, srcData.get(i),
									label, (String) srcData.get(i).get("valuemember"), tableName, inputMap, editData);
							returnObject.putAll(datas);
						}
					}
				}
			}
			returnObject.put(label, data);
		}
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> recursiveChildComboForEdit(Map<String, Object> childDatas,
			Map<String, Object> childData, Map<String, Object> map, String label, String valueMember, String tableName,
			Map<String, Object> inputMap, Map<String, Object> editData) throws Exception {

		final Map<String, Object> objReturn = new HashMap();
		List<Map<String, Object>> childcomboData = (List<Map<String, Object>>) childData.get(label);
		for (int z = 0; z < childcomboData.size(); z++) {
			String labelchild = (String) childcomboData.get(z).get("label");
			List<Map<String, Object>> objChild = ((List<Map<String, Object>>) map.get("child")).stream()
					.filter(x -> x.get("label").equals(labelchild)).collect(Collectors.toList());

			final String sourceName1 = (String) childcomboData.get(z).get("source");

			final String valuememberchild = (String) childcomboData.get(z).get("valuemember");
			final String tableNamechild = sourceName1.toLowerCase();
			final String parentValumember = valueMember;
			String valuememberData = valueMember;
			String conditionString = childcomboData.get(z).containsKey("conditionstring")
					? (String) childcomboData.get(z).get("conditionstring")
							: "";

			String Keysofparam = "";

			while (conditionString.contains("P$")) {
				StringBuilder sb = new StringBuilder(conditionString);
				int firstindex = sb.indexOf("P$");
				int lastindex = sb.indexOf("$P");
				Keysofparam = sb.substring(firstindex + 2, lastindex);
				if (Keysofparam.contains(".")) {
					int index = Keysofparam.indexOf(".");
					String tablename = Keysofparam.substring(0, index);
					String columnName = Keysofparam.substring(index + 1, Keysofparam.length());
					if (inputMap.containsKey(tablename)) {
						Map<String, Object> userInfoMap = (Map<String, Object>) inputMap.get(tablename);
						if (userInfoMap.containsKey(columnName)) {
							sb.replace(firstindex, lastindex + 2, userInfoMap.get(columnName).toString());
						}
					}
				}
				conditionString = sb.toString();
			}

			String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
					+ tableNamechild + "' ";
			final String jsonchildField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
			if (!jsonchildField.contains(valueMember)) {
				;
				String tablecolumnname = objChild.isEmpty() ? "" : (String) objChild.get(0).get("tablecolumnname");

				String foreignPK = objChild.isEmpty() ? "" : (String) objChild.get(0).get("foriegntablePK");

				if (!childDatas.containsKey(tablecolumnname)) {
					valueMember = tablecolumnname;
					valuememberData = foreignPK;
				} else {
					valueMember = foreignPK;
					valuememberData = tablecolumnname;
				}
			}

			String defaultvalues = "";
			if (childcomboData.get(z).containsKey("defaultvalues")) {
				List<Map<String, Object>> defaulvalueslst = (List<Map<String, Object>>) childcomboData.get(z)
						.get("defaultvalues");
				for (int j = 0; j < defaulvalueslst.size(); j++) {
					if (defaulvalueslst.get(j)
							.containsKey(childDatas.get(childcomboData.get(z).get("parentprimarycode")).toString())) {
						defaultvalues = " in ( " + defaulvalueslst.get(j)
						.get(childDatas.get(childcomboData.get(z).get("parentprimarycode")).toString())
						.toString() + " )";
					}
				}
			} else {
				if (!objChild.isEmpty() && objChild.get(0).containsKey("isDynamicMapping")
						&& (boolean) objChild.get(0).get("isDynamicMapping")) {
					if (childDatas.containsKey("jsondata")) {
						String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
						Map<String, Object> objJsonData = (Map<String, Object>) childDatas.get("jsondata");
						if (objJsonData.containsKey(tablecolumnname)) {
							Map<String, Object> objJsonData1 = (Map<String, Object>) objJsonData.get(tablecolumnname);
							defaultvalues = " = '" + objJsonData1.get("value") + "'";

						} else {
							defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
						}
					} else {
						defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
					}

				} else if (parentValumember.equals("ndynamicmastercode") && !objChild.isEmpty()
						&& (boolean) objChild.get(0).get("isDynamicMapping") == false) {
					if (childDatas.containsKey("jsondata")) {
						String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
						Map<String, Object> objJsonData = (Map<String, Object>) childDatas.get("jsondata");
						if (objJsonData.containsKey(tablecolumnname)) {
							Map<String, Object> objJsonData1 = (Map<String, Object>) objJsonData.get(tablecolumnname);
							defaultvalues = " = '" + objJsonData1.get("value") + "'";

						} else {
							defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
						}
					} else {
						defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
					}
				} else {
					defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
				}
			}

			if (!objChild.isEmpty() && objChild.get(0).containsKey("isDynamicMapping")
					&& (boolean) objChild.get(0).get("isDynamicMapping")) {
				String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
				if (!(jsonchildField.contains(tablecolumnname + "||")
						|| jsonchildField.contains("||" + tablecolumnname + "||")
						|| jsonchildField.contains("||" + tablecolumnname))) {
					valueMember = "ndynamicmastercode";
				} else {
					valueMember = "jsondata->" + "'" + tablecolumnname + "'->>'value'";
				}
			} else {
				valueMember = "\"" + valueMember + "\"";
			}

			getJSONFieldQuery = "select TO_JSON(" + tableNamechild + ".*)::jsonb  as jsondata" + " from "
					+ tableNamechild + " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and " + valueMember + defaultvalues + conditionString + ";";
			final List<Combopojo> data = jdbcTemplate.query(getJSONFieldQuery, new Combopojo());
			if (childData != null && childData.containsKey(labelchild)) {
				if (editData.get(labelchild) instanceof Map<?, ?>) {
					Map<String, Object> parentComboObj1 = (Map<String, Object>) editData.get(labelchild);
					List<Combopojo> data2 = data.stream()
							.filter(x -> x.getJsondata().get(valuememberchild) == parentComboObj1.get("value"))
							.collect(Collectors.toList());
					if (!data2.isEmpty()) {
						if (parentComboObj1.containsKey("value")) {
							final Map<String, Object> datas = recursiveChildComboForEdit(data2.get(0).getJsondata(),
									childData, childcomboData.get(z), labelchild, valuememberchild, tableNamechild,
									inputMap, editData);
							objReturn.putAll(datas);
						}
					}

				}
			}
			objReturn.put(labelchild, data);
		}
		return objReturn;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getcustomsearchfilter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		String tableName = "";
		String getJSONKeysQuery = "";
		Map<String, Object> returnObject = new HashMap<>();
		String sourceName = (String) inputMap.get("source");
		String conditionString = inputMap.containsKey("conditionstring") ? (String) inputMap.get("conditionstring")
				: "";
		tableName = sourceName.toLowerCase();
		final String getDateFieldQuery = "select string_agg(column_name ,',')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "' and data_type = 'timestamp without time zone'";
		String jsonDateField = jdbcTemplate.queryForObject(getDateFieldQuery, String.class);
		// String dateconvert = "";
		List<String> lstDateField = null;
		if (jsonDateField != null) {
			lstDateField = new ArrayList<String>(Arrays.asList(jsonDateField.split(",")));
			if (lstDateField.contains("dmodifieddate")) {
				lstDateField.remove("dmodifieddate");
			}
		}

		String getFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "'";
		String fields = jdbcTemplate.queryForObject(getFieldQuery, String.class);
		if (fields.contains(inputMap.get("valuemember").toString())) {
			getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb as jsondata" + " from " + tableName
					+ " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and \""
					+ inputMap.get("valuemember") + "\" > '0' " + conditionString + " ;";
		} else {
			getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb as jsondata" + " from " + tableName
					+ " where nstatus =1 " + conditionString + " ;";
		}
		List<Combopojo> data = jdbcTemplate.query(getJSONKeysQuery, new Combopojo());
		if (lstDateField != null && !lstDateField.isEmpty()) {
			data = objectMapper.convertValue(
					getSiteLocalTimeFromUTCForDynamicSource(data, userInfo, true, lstDateField),
					new TypeReference<List<Combopojo>>() {
					});
		}

		returnObject.put((String) inputMap.get("label"), data);

		returnObject
		.putAll((Map<? extends String, ? extends Object>) getcustomsearchfilterpredefined(inputMap, userInfo)
				.getBody());
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getcustomsearchfilterpredefined(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		String getJSONKeysQuery = "";
		String getJSONFieldQuery = "";
		String jsonField = "";
		Map<String, Object> returnObject = new HashMap<>();
		List<Map<String, Object>> filterfields = (List<Map<String, Object>>) inputMap.get("filterfields");
		for (int i = 0; i < filterfields.size(); i++) {
			if (filterfields.get(i).containsKey("filterinputtype")) {
				final String operator = (String) filterfields.get(i).get("filterinputtype");
				if (operator.equals("predefinednumeric") || operator.equals("predefinedtext")) {
					String conditionStringPredef = "";

					if (filterfields.get(i).containsKey("predefinedconditionalString")) {

						// conditionStringPredef=" and
						// "+filterfields.get(i).get("predefinedvaluemember")+" in
						// ("+filterfields.get(i).get("predefinedconditionalString")+")";
						conditionStringPredef = " and " + filterfields.get(i).get("predefinedconditionalString");
					}
					final String predeftablename = (String) filterfields.get(i).get("predefinedtablename");

					getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
							+ predeftablename + "' and data_type = 'jsonb'";
					jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
					jsonField = jsonField != null ? "||" + jsonField : "";
					// String getFieldQuery = "select string_agg(column_name ,'||')FROM
					// INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
					// + predeftablename + "'";
					// String fields = jdbcTemplate.queryForObject(getFieldQuery, String.class);
					// if(fields.contains(inputMap.get("valuemember").toString())) {
					// getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb" + jsonField +
					// " as jsondata" + " from "
					// + tableName + " where nstatus =1 and " + inputMap.get("valuemember") + " > 0
					// "+conditionString+" ;";
					// }else {
					getJSONKeysQuery = "select TO_JSON(" + predeftablename + ".*)::jsonb" + jsonField + " as jsondata"
							+ " from " + predeftablename + " where nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + conditionStringPredef
							+ " ;";
					// }
					List<Combopojo> data1 = jdbcTemplate.query(getJSONKeysQuery, new Combopojo());
					returnObject.put(predeftablename, data1);
				}

			}

		}
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDynamicFilterExecuteData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		String tableName = "";
		String getJSONKeysQuery = "";
		final ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> returnObject = new HashMap<>();
		String sourceName = (String) inputMap.get("source");
		String conditionString = inputMap.containsKey("conditionstring") ? (String) inputMap.get("conditionstring")
				: "";
		if (conditionString.isEmpty()) {
			conditionString = inputMap.containsKey("filterquery") ? "and " + (String) inputMap.get("filterquery") : "";
		}

		String scollate = "collate \"default\"";
		// String sPatientFilter = "";
		if (conditionString.contains("LIKE")) {

			while (conditionString.contains("LIKE")) {
				String sb = conditionString;
				String sQuery = conditionString;
				int colanindex = sb.indexOf("LIKE '");
				String str1 = sQuery.substring(0, colanindex + 6);
				sQuery = sQuery.substring(colanindex + 6);
				StringBuilder sb3 = new StringBuilder(str1);
				StringBuilder sb4 = new StringBuilder(sQuery);
				sb3.replace(colanindex, colanindex + 4, "ilike");
				int indexofsv = sQuery.indexOf("%'");

				sb4.replace(indexofsv, indexofsv + 2, "%'" + scollate + " ");
				conditionString = sb3.toString() + sb4.toString();
			}

		}

		if (!conditionString.isEmpty() && inputMap.containsKey("filterquery")) {
			String filterByPatientQueryBuilder = new String(conditionString);

			if (filterByPatientQueryBuilder.contains("\\\\")) {
				filterByPatientQueryBuilder = filterByPatientQueryBuilder.replace("\\\\", "#LiMsBaCkSlAsH#");
			}
			if (filterByPatientQueryBuilder.contains("\\'")) {
				filterByPatientQueryBuilder = filterByPatientQueryBuilder.replace("\\'", "''");
			}
			if (filterByPatientQueryBuilder.contains("#LiMsBaCkSlAsH#")) {
				filterByPatientQueryBuilder = filterByPatientQueryBuilder.replace("#LiMsBaCkSlAsH#", "\\\\");
			}
			conditionString = filterByPatientQueryBuilder;
		}

		tableName = sourceName.toLowerCase();

		final String getDateFieldQuery = "select string_agg(column_name ,',')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "' and data_type = 'timestamp without time zone'";
		String jsonDateField = jdbcTemplate.queryForObject(getDateFieldQuery, String.class);
		// String dateconvert = "";
		List<String> lstDateField = null;
		if (jsonDateField != null) {
			lstDateField = new ArrayList<String>(Arrays.asList(jsonDateField.split(",")));
			if (lstDateField.contains("dmodifieddate")) {
				lstDateField.remove("dmodifieddate");
			}

		}

		String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "' and data_type = 'jsonb'";
		String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
		jsonField = jsonField != null ? "||" + jsonField : "";
		String getFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "'";
		String fields = jdbcTemplate.queryForObject(getFieldQuery, String.class);
		if (fields.contains(inputMap.get("valuemember").toString())) {
			getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb" + jsonField + " as jsondata" + " from "
					+ tableName + " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and \"" + inputMap.get("valuemember") + "\" > '0' " + conditionString + " ;";
		} else {
			getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb" + jsonField + " as jsondata" + " from "
					+ tableName + " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " " + conditionString + " ;";
		}
		List<Combopojo> data = jdbcTemplate.query(getJSONKeysQuery, new Combopojo());

		if (lstDateField != null && !lstDateField.isEmpty()) {
			data = objectMapper.convertValue(
					getSiteLocalTimeFromUTCForDynamicSource(data, userInfo, true, lstDateField),
					new TypeReference<List<Combopojo>>() {
					});
		}
		returnObject.put((String) inputMap.get("label"), data);
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<?> getSiteLocalTimeFromUTCForDynamicSource(List<?> sb, final UserInfo userinfo,
			final boolean isdbdateformat, List<String> dateFields) throws Exception {
		List<Map<String, Object>> finalList = new ArrayList();
		if (!sb.isEmpty()) {
			final ObjectMapper Objmapper = new ObjectMapper();
			Objmapper.registerModule(new JavaTimeModule());
			List<Map<String, Object>> lst = Objmapper.convertValue(sb, new TypeReference<List<Map<String, Object>>>() {
			});
			if (dateFields.isEmpty()) {
				return lst;
			}
			if (!dateFields.isEmpty()) {
				DateTimeFormatter destFormat = DateTimeFormatter.ofPattern(userinfo.getSsitedatetime());
				SimpleDateFormat sourceFormat = new SimpleDateFormat(userinfo.getSsitedatetime().replace(" ", "'T'"));
				if (isdbdateformat) {
					sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				}
				for (int i = 0; i < lst.size(); i++) {
					Map<String, Object> data = lst.get(i);
					for (int j = 0; j < dateFields.size(); j++) {

						final String dateFieldName = dateFields.get(j);
						boolean dateOnly = false;
						if (!data.containsKey(dateFieldName)) {
							if (data.containsKey("jsondata")) {
								Map<String, Object> jsonData = (Map<String, Object>) data.get("jsondata");
								if (jsonData.containsKey(dateFieldName)) {
									if ((String) jsonData.get(dateFieldName) != null
											&& !jsonData.get(dateFieldName).equals("")
											&& !jsonData.get(dateFieldName).equals("-")) {
										if (userinfo.getIsutcenabled() == Enumeration.TransactionStatus.YES
												.gettransactionstatus()) {
											if (jsonData.containsKey("ntz" + dateFieldName)) {
												final LocalDateTime ldt = sourceFormat
														.parse((String) jsonData.get(dateFieldName)).toInstant()
														.atOffset(ZoneOffset.ofTotalSeconds(
																(int) jsonData.get("noffset" + dateFieldName)))
														.toLocalDateTime();
												jsonData.put(
														dateFieldName, dateOnly
														? DateTimeFormatter.ofPattern(userinfo.getSsitedate())
																.format(ldt)
																: destFormat.format(ldt));
											} else if (!jsonData.containsKey("noffset" + dateFieldName)) {

											} else {
												if (!jsonData.containsKey("ntz" + dateFieldName)) {
													dateOnly = true;
												}

												final LocalDateTime ldt = sourceFormat
														.parse((String) jsonData.get(dateFieldName)).toInstant()
														.atOffset(ZoneOffset.ofTotalSeconds(
																(int) jsonData.get("noffset" + dateFieldName)))
														.toLocalDateTime();
												jsonData.put(
														dateFieldName, dateOnly
														? DateTimeFormatter.ofPattern(userinfo.getSsitedate())
																.format(ldt)
																: destFormat.format(ldt));

											}
										} else {
											if (!jsonData.containsKey("ntz" + dateFieldName)) {
												dateOnly = true;
											}
											LocalDateTime ldt = sourceFormat.parse((String) jsonData.get(dateFieldName))
													.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

											jsonData.put(dateFieldName, dateOnly
													? DateTimeFormatter.ofPattern(userinfo.getSsitedate()).format(ldt)
															: destFormat.format(ldt));

										}
									} else {
										jsonData.put(dateFieldName, "-");
									}
									data.put("jsondata", jsonData);
								}
							}
						} else {
							if ((String) data.get(dateFieldName) != null && !data.get(dateFieldName).equals("")
									&& !data.get(dateFieldName).equals("-")) {
								if (userinfo.getIsutcenabled() == Enumeration.TransactionStatus.YES
										.gettransactionstatus()) {
									if (data.containsKey("tz" + dateFieldName)) {
										String searchfield = "tz" + dateFieldName;
										Map<String, Object> objMap = (Map<String, Object>) data.get(searchfield);
										final LocalDateTime ldt = sourceFormat.parse((String) data.get(dateFieldName))
												.toInstant()
												.atOffset(ZoneOffset
														.ofTotalSeconds((int) data.get("noffset" + dateFieldName)))
												.toLocalDateTime();
										data.put(dateFieldName, dateOnly
												? DateTimeFormatter.ofPattern(userinfo.getSsitedate()).format(ldt)
														: destFormat.format(ldt));
									} else {
										final LocalDateTime ldt = sourceFormat.parse((String) data.get(dateFieldName))
												.toInstant()
												.atOffset(ZoneOffset
														.ofTotalSeconds((int) data.get("noffset" + dateFieldName)))
												.toLocalDateTime();
										data.put(dateFieldName, dateOnly
												? DateTimeFormatter.ofPattern(userinfo.getSsitedate()).format(ldt)
														: destFormat.format(ldt));
									}
								} else {
									LocalDateTime ldt = sourceFormat.parse((String) data.get(dateFieldName)).toInstant()
											.atZone(ZoneId.systemDefault()).toLocalDateTime();
									data.put(dateFieldName,
											dateOnly ? DateTimeFormatter.ofPattern(userinfo.getSsitedate()).format(ldt)
													: destFormat.format(ldt));
								}
							} else {
								data.put(dateFieldName, "-");
							}

						}

					}
					finalList.add(i, data);
				}

			} else {
				finalList.addAll(lst);
			}

		}
		return finalList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> dateValidation(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		List<Map<String, Object>> lstDate = (List<Map<String, Object>>) inputMap.get("datecolumnlist");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> collectMap = new HashMap<String, Object>();
		if (!lstDate.isEmpty()) {
			for (Map<String, Object> map : lstDate) {
				if (map.containsKey("nperiodcode")) {

					int nperioddata = (int) map.get("nperioddata");
					int windowperiod = Integer.parseInt((String) map.get("windowperiod"));
					Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo);
					Instant startDate = instantDate.plus((nperioddata * windowperiod), ChronoUnit.MINUTES)
							.truncatedTo(ChronoUnit.SECONDS);
					if (map.containsKey("nmonth")) {
						int nmonth = Integer.parseInt((String) map.get("nmonth"));
						startDate = startDate.plus((43200 * nmonth), ChronoUnit.MINUTES)
								.truncatedTo(ChronoUnit.SECONDS);
					}
					if (map.containsKey("nday")) {
						int nday = Integer.parseInt((String) map.get("nday"));
						startDate = startDate.plus((1440 * nday), ChronoUnit.MINUTES).truncatedTo(ChronoUnit.SECONDS);
					}

					collectMap.put("datevalue", dateUtilityFunction.instantDateToString(startDate));
					returnMap.put((String) map.get("label"), collectMap);
				}

			}

		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReactStaticFilterTables(Map<String, Object> inputMap, UserInfo userInfo) {

		final String getReactComponents = "select nquerybuilderstaticfiltercode,jtablename->'displayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "' displayname,TO_JSON(querybuilderstaticfilter.*)::jsonb as jsondata "
				+ "from querybuilderstaticfilter  where  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<?> staticfiltertableList = jdbcTemplate.queryForList(getReactComponents);
		return new ResponseEntity<>(staticfiltertableList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> validatePreview(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		Registration registration = objmapper.convertValue(inputMap.get("Registration"),
				new TypeReference<Registration>() {
		});

		List<String> dateList = objmapper.convertValue(inputMap.get("DateList"), new TypeReference<List<String>>() {
		});

		List<Map<String, Object>> sampleDateConstraint = objmapper.convertValue(inputMap.get("dateconstraints"),
				new TypeReference<List<Map<String, Object>>>() {
		});
		JSONObject jsoneditRegistration = new JSONObject(registration.getJsondata());

		if (!dateList.isEmpty()) {

			jsoneditRegistration = (JSONObject) dateUtilityFunction
					.convertJSONInputDateToUTCByZone(jsoneditRegistration, dateList, false, userInfo);
			final Map<String, Object> obj = commonFunction.validateDynamicDateContraints(jsoneditRegistration,
					sampleDateConstraint, userInfo);
			if (!(Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
					.equals(obj.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
				return new ResponseEntity<>(obj, HttpStatus.OK);
			}
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getExternalportalDetail(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> rtnMap = new HashMap<String, Object>();
		int nquerybuildertablecode = (int) inputMap.get("nquerybuildertablecode");
		if (nquerybuildertablecode == 222) {
			String query = "";
			int externalorder = (int) inputMap.get("nexternalordercode");

			query = "select es.*,c.scomponentname, case when es.nsampleqty = -1 then '-' else  cast(es.nsampleqty  as text) end ||' '|| case when u.sunitname='NA' then '' else  u.sunitname end ssampleqty  from externalordersample es,"
					+ " unit u, component c  where"
					+ " es.nunitcode=u.nunitcode  and es.ncomponentcode=c.ncomponentcode  " + "and u.nsitecode="
					+ userInfo.getNmastersitecode() + " " + "and c.nsitecode=" + userInfo.getNmastersitecode() + " "
					+ "and es.nsitecode=" + userInfo.getNmastersitecode() + " " + "and c.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and es.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nexternalordercode="
					+ externalorder;
			List<?> lstSample = jdbcTemplate.queryForList(query);
			rtnMap.put("Sample", lstSample);

			query = "select et.*,tg.stestsynonym from externalordertest et,testmaster tg "
					+ "where tg.ntestcode=et.ntestcode " + "and et.nsitecode=" + userInfo.getNmastersitecode() + " "
					+ "and tg.nsitecode=" + userInfo.getNmastersitecode() + " " + "and et.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nexternalordercode="
					+ externalorder;

			List<?> lstTest = jdbcTemplate.queryForList(query);

			rtnMap.put("Test", lstTest);
		}

		return new ResponseEntity<>(rtnMap, HttpStatus.OK);

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getDefaultSampleType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		int nsampletypecode = 0;
		final String query = "select  nsampletypecode,coalesce(jsondata->'sampletypename'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',jsondata->'sampletypename'->>'en-US') as ssampletypename from sampletype "
				+ "where nformcode not in (-1," + Enumeration.QualisForms.GOODSIN.getqualisforms() + ") "
				+ "and nsampletypecode <> -1 " + "and nsitecode=" + userInfo.getNmastersitecode() + " " + "and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<SampleType> sampletypeList = jdbcTemplate.query(query, new SampleType());
		outputMap.put("SampleTypeList", sampletypeList);
		outputMap.put("selectedSampleTypeList", sampletypeList.get(0));
		nsampletypecode = sampletypeList.get(0).getNsampletypecode();
		outputMap.put("nsubsampletypecode", nsampletypecode);
		outputMap.put("SubSample", true);
		outputMap.put("nsampletypecode", inputMap.get("nsampletypecode"));
		outputMap.putAll((Map<String, Object>) getDefaultTemplate(outputMap, userInfo).getBody());

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public List<ReactRegistrationTemplate> encryptList(List<ReactRegistrationTemplate> registrationTemplate)
			throws Exception {
		List<ReactRegistrationTemplate> encryptedList = new ArrayList<>();
		final String ALGORITHM = "AES/CBC/PKCS5Padding";
		final String SECRET_KEY = "XMzDdG4D03CKm2IxIWQw7g=="; // 128 bit key
		final String INIT_VECTOR = "encryptionIntVec"; // 16 bytes IV

		IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
		SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");

		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		for (ReactRegistrationTemplate s : registrationTemplate) {
			byte[] encrypted = cipher.doFinal(s.getJsontext().getBytes());
			s.setJsontext(Base64.getEncoder().encodeToString(encrypted));
			encryptedList.add(s);
		}

		return encryptedList;
	}

	public String decryptList(String encryptedList) throws Exception {

		final String SECRET_KEY = "XMzDdG4D03CKm2IxIWQw7g=="; // 128 bit key
		final String INIT_VECTOR = "encryptionIntVec"; // 16 bytes IV
		IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
		SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
		byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedList));

		return new String(original);

	}

	@SuppressWarnings("removal")
	@Override
	public ResponseEntity<Object> createImportTemplate(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {

		final MultipartFile multipartFile = request.getFile("ImportFile");
		final int nsampletypecode = Integer.parseInt(request.getParameter("nsampletypecode"));
		final int ndefaulttemplatecode = Integer.parseInt(request.getParameter("ndefaulttemplatecode"));
		final int nreactregtemplatecode = Integer.parseInt(request.getParameter("nreactregtemplatecode"));
		final String jsondata = StringEscapeUtils.unescapeJava(request.getParameter("jsondata")).toString();
		final String sampletypeName = StringEscapeUtils.unescapeJava(request.getParameter("sampleTypeName")).toString();
		final String templateTypeName = StringEscapeUtils.unescapeJava(request.getParameter("templateTypeName"))
				.toString();
		final String sampleTypeCode = StringEscapeUtils.unescapeJava(request.getParameter("sampleTypeCode")).toString();
		final String defaultTemplateTypeCode = StringEscapeUtils
				.unescapeJava(request.getParameter("defaultTemplateTypeCode")).toString();
		final String selectedSampletype = StringEscapeUtils.unescapeJava(request.getParameter("selectedSampletype"))
				.toString();
		final String selecteddefaulttemplate = StringEscapeUtils
				.unescapeJava(request.getParameter("selecteddefaulttemplate")).toString();
		final InputStream ins = multipartFile.getInputStream();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = ins.read(buffer)) > -1) {
			baos.write(buffer, 0, len);
		}
		baos.flush();
		InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
		final Workbook workbook = WorkbookFactory.create(is1);
		final Sheet sheet = workbook.getSheetAt(0);
		int rowIndex = 0;
		List<String> lstHeader = new ArrayList<>();
		JSONObject objJsonUiData = new JSONObject();
		JSONObject objJsonData = new JSONObject();
		for (Row row : sheet) {
			if (rowIndex > 0) {
				int cellIndex = 0;

				for (String field : lstHeader) // iteration over cell using for each loop
				{
					String headerName = field.toLowerCase();
					if (headerName.equals(jsondata.toLowerCase()) || headerName.equals(sampletypeName.toLowerCase())
							|| headerName.equals(templateTypeName.toLowerCase())
							|| headerName.equals(sampleTypeCode.toLowerCase())
							|| headerName.equals(defaultTemplateTypeCode.toLowerCase())) {
						if (row.getCell(cellIndex) != null) {
							// LOGGER.info(row.getCell(cellIndex));
							Cell cell = row.getCell(cellIndex);
							if (cell.getCellType() == CellType.STRING) {
								objJsonUiData.put(field, cell.getStringCellValue());

							} else if (cell.getCellType() == CellType.NUMERIC) {
								objJsonUiData.put(field, cell.getNumericCellValue());
							}
							objJsonData.put(field, objJsonUiData.get(field));
						} else {
							objJsonData.put(field, "");
						}
						cellIndex++;
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVALIDTEMPLATEHEADERS",
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				}
			} else {
				int cellIndex = 0;
				for (Cell cell : row) // iteration over cell using for each loop
				{

					String header = cell.getStringCellValue();
					lstHeader.add(header);
					cellIndex++;
				}
			}
			rowIndex++;
		}

		final Double sampletypec = new Double((double) objJsonData.get(sampleTypeCode));
		final Double defaultypec = new Double((double) objJsonData.get(defaultTemplateTypeCode));
		final int sampletypecode = sampletypec.intValue();
		final int defaulttypecode = defaultypec.intValue();
		if (sampletypecode == nsampletypecode) {
			if (defaulttypecode == ndefaulttemplatecode) {
				if (!objJsonData.get(jsondata).toString().isEmpty()) {
					String queryExc = "";

					queryExc = " update reactregistrationtemplate set jsondata= '" + objJsonData.get("Jsondata")
					+ "' ,dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
					+ "  where   nreactregtemplatecode=" + nreactregtemplatecode + ";";

					jdbcTemplate.execute(queryExc);
					return getRegistrationTemplateById(nsampletypecode, userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NORECORDINEXCEL",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECT", userInfo.getSlanguagefilename()) + " "
								+ selecteddefaulttemplate + " " + commonFunction
								.getMultilingualMessage("IDS_TEMPLATEONLY", userInfo.getSlanguagefilename()),
								HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECT",
					userInfo.getSlanguagefilename()) + " " + selectedSampletype + " "
					+ commonFunction.getMultilingualMessage("IDS_TEMPLATEONLY", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}
}
