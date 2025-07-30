package com.agaramtech.qualis.registration.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.configuration.model.ApprovalConfigAutoapproval;
import com.agaramtech.qualis.configuration.model.ApprovalConfigRole;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.externalorder.model.ExternalOrder;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.Registration;
import com.agaramtech.qualis.registration.model.RegistrationHistory;
import com.agaramtech.qualis.registration.model.RegistrationParameter;
import com.agaramtech.qualis.registration.model.RegistrationSample;
import com.agaramtech.qualis.registration.model.RegistrationSampleHistory;
import com.agaramtech.qualis.registration.model.RegistrationSection;
import com.agaramtech.qualis.registration.model.RegistrationTest;
import com.agaramtech.qualis.registration.model.RegistrationTestHistory;
import com.agaramtech.qualis.registration.model.SeqNoArnoGenerator;
import com.agaramtech.qualis.registration.model.SeqNoRegistration;
import com.agaramtech.qualis.registration.model.TestGroupTestForSample;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecSampleType;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFile;
import com.agaramtech.qualis.testgroup.model.TestGroupTestFormula;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testgroup.model.TestGroupTestPredefinedParameter;
import com.agaramtech.qualis.testgroup.service.testgrouptest.TestGroupTestDAO;
import com.agaramtech.qualis.testmanagement.model.Method;
import com.agaramtech.qualis.testmanagement.model.TestFile;
import com.agaramtech.qualis.testmanagement.model.TestFormula;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.testmanagement.model.TestMasterClinicalSpec;
import com.agaramtech.qualis.testmanagement.model.TestParameter;
import com.agaramtech.qualis.testmanagement.model.TestSection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class RegistrationDAOSupport {	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationDAOSupport.class);
	
	private final TransactionDAOSupport transactionDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final StringUtilityFunction stringUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	TestGroupTestDAO testGroupTestDAO;

	@SuppressWarnings("unchecked")
	public Map<String, Object> getDynamicRegistration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		
		Map<String, Object> objMap = new HashMap<String, Object>();

		short nfilterstatus;
		if (inputMap.get("nfilterstatus").getClass().getTypeName().equals("java.lang.Integer")) {
			nfilterstatus = ((Integer) inputMap.get("nfilterstatus")).shortValue();
		} else {
			nfilterstatus = (short) inputMap.get("nfilterstatus");
		}

		final String registrationMultilingual = commonFunction.getMultilingualMessage("IDS_REGNO",
				userInfo.getSlanguagefilename());

		final String query1 = "select * from fn_registrationsampleget('" + inputMap.get("FromDate") + "'::text," + "'"
							+ inputMap.get("ToDate") + "'::text" + "," + inputMap.get("nsampletypecode") + ","
							+ userInfo.getNusercode() + "," + inputMap.get("nregtypecode") + "," + inputMap.get("nregsubtypecode")
							+ "," + "" + nfilterstatus + "::integer," + userInfo.getNtranssitecode() + ",'"
							+ inputMap.get("npreregno") + "'::text," + "'" + userInfo.getSlanguagetypecode() + "'::text," + ""
							+ inputMap.get("ndesigntemplatemappingcode") + ",'" + registrationMultilingual + "'::text,"
							+ inputMap.get("napproveconfversioncode") + ")";

		LOGGER.info("Sample Start========?" + LocalDateTime.now());

		String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
		LOGGER.info("Sample end========?" + query1 + " :" + LocalDateTime.now());

		List<Map<String, Object>> lstData = new ArrayList<Map<String, Object>>();

		if (lstData1 != null) {
			lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1, userInfo, true,
					(int) inputMap.get("ndesigntemplatemappingcode"), "sample");

			LOGGER.info("Sample Size" + lstData.size());

			objMap.put("RegistrationGetSample", lstData);

			String spreregno = "";
			if (!inputMap.containsKey("ntype")) {
				
				spreregno = String.valueOf(lstData.get(lstData.size() - 1).get("npreregno"));
				inputMap.put("npreregno", spreregno);
				
				objMap.put("selectedSample", Arrays.asList(lstData.get(lstData.size() - 1)));
				inputMap.put("OrderCodeData",
						((List<Map<String, Object>>) objMap.get("selectedSample")).get(0).containsKey("OrderCodeData")
								? ((List<Map<String, Object>>) objMap.get("selectedSample")).get(0).get("OrderCodeData")
								: -1);
			} else {
				objMap.put("selectedSample", lstData);
			}

			Map<String, Object> map = (Map<String, Object>) getRegistrationSubSample(inputMap, userInfo).getBody();
			objMap.putAll(map);

			objMap.putAll(transactionDAOSupport.getActiveSampleTabData((String) inputMap.get("npreregno"), "0",
					(String) inputMap.get("activeSampleTab"), userInfo));
			objMap.put("activeSampleTab", inputMap.get("activeSampleTab"));
		} 
		else {
			objMap.put("selectedSample", lstData);
			objMap.put("RegistrationGetSample", lstData);
			objMap.put("selectedSubSample", lstData);
			objMap.put("RegistrationGetSubSample", lstData);
			objMap.put("RegistrationGetTest", lstData);
			objMap.put("selectedTest", lstData);
			objMap.put("RegistrationParameter", lstData);
			objMap.put("activeSampleTab", "IDS_SAMPLEATTACHMENTS");
			objMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
			objMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
			
			/*ALPD-4063--Vignesh R(17-05-2024)--while delete the attachments and comments 500 error occurs for specific scenario*/
			objMap.put("RegistrationAttachment", new ArrayList<>());
			objMap.put("RegistrationSampleAttachment", new ArrayList<>());
			objMap.put("RegistrationTestAttachment", new ArrayList<>());
			objMap.put("RegistrationComment", new ArrayList<>());
			objMap.put("RegistrationSampleComment", new ArrayList<>());
			objMap.put("RegistrationTestComment", new ArrayList<>());
		}
		return objMap;

	}

	
	
	public ResponseEntity<Object> getRegistrationSubSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();

		boolean subSample = (boolean) inputMap.get("nneedsubsample");

		final String query1 = "select * from fn_registrationsubsampleget('" + inputMap.get("npreregno") + "'::text,"
				+ "'" + inputMap.get("ntransactionsamplecode") + "'::text" + "," + inputMap.get("ntype") + ","
				+ userInfo.getNtranssitecode() + ",'" + userInfo.getSlanguagetypecode() + "','"
				+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "'" + ",'"
				+ commonFunction.getMultilingualMessage("IDS_SAMPLENO", userInfo.getSlanguagefilename()) + "')";

		LOGGER.info("sub sample Start========?" + LocalDateTime.now());
		LOGGER.info("sub sample query:" + query1);

		final String lstData1 = jdbcTemplate.queryForObject(query1, String.class);
		LOGGER.info("sub sample end========?" + LocalDateTime.now());

		if (lstData1 != null) {

			List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					lstData1, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "subsample");

			objMap.put("RegistrationGetSubSample", lstData);

			objMap.putAll(transactionDAOSupport.getActiveSampleTabData((String) inputMap.get("npreregno"), "0",
					(String) inputMap.get("activeSampleTab"), userInfo));
			objMap.put("activeSampleTab", inputMap.get("activeSampleTab"));
			if (!subSample || (int) inputMap.get("checkBoxOperation") == 3
					|| (int) inputMap.get("checkBoxOperation") == 7) {
				String stransactionsampleno = "";
				if (!inputMap.containsKey("ntype")) {
					stransactionsampleno = String
							.valueOf(lstData.get(lstData.size() - 1).get("ntransactionsamplecode"));
					inputMap.put("ntransactionsamplecode", stransactionsampleno);
					objMap.put("selectedSubSample", Arrays.asList(lstData.get(lstData.size() - 1)));
				} else if ((int) inputMap.get("ntype") == 2) {
					stransactionsampleno = lstData.stream().map(x -> String.valueOf(x.get("ntransactionsamplecode")))
							.collect(Collectors.joining(","));
					inputMap.put("ntransactionsamplecode", stransactionsampleno);
					objMap.put("selectedSubSample", lstData);
				} else if ((int) inputMap.get("ntype") == 3) {
					String ntransactionsamplecode = (String) inputMap.get("ntransactionsamplecode");
					stransactionsampleno = ntransactionsamplecode;
					List<String> myList = new ArrayList<String>(Arrays.asList(ntransactionsamplecode.split(",")));
					lstData = lstData.stream()
							.filter(x -> myList.stream()
									.anyMatch(y -> y.equals(String.valueOf(x.get("ntransactionsamplecode")))))
							.collect(Collectors.toList());
					inputMap.put("ntype", 4);
					objMap.put("selectedSubSample", lstData);
				} else if ((int) inputMap.get("ntype") == 5) {
					stransactionsampleno = (String) inputMap.get("ntransactionsamplecode");
					objMap.put("selectedSubSample", lstData);
				}

				else {
					stransactionsampleno = String
							.valueOf(lstData.get(lstData.size() - 1).get("ntransactionsamplecode"));
					objMap.put("selectedSubSample", Arrays.asList(lstData.get(lstData.size() - 1)));
					inputMap.put("ntransactionsamplecode", stransactionsampleno);
					if ((int) inputMap.get("checkBoxOperation") == 7 && (int) inputMap.get("ntype") == 4) {
						inputMap.put("ntype", 5);
					}

				}

				objMap.putAll(transactionDAOSupport.getActiveSubSampleTabData(stransactionsampleno,
						(String) inputMap.get("activeSubSampleTab"), userInfo));

				objMap.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));

				Map<String, Object> map = (Map<String, Object>) getRegistrationTest(inputMap, userInfo).getBody();
				objMap.putAll(map);

				final boolean outsourcedRequiredValue = inputMap.containsKey("RealSampleTypeValue")
						&& inputMap.get("RealSampleTypeValue") != null
								? ((SampleType) inputMap.get("RealSampleTypeValue"))
										.getNoutsourcerequired() == Enumeration.TransactionStatus.YES
												.gettransactionstatus()
								: inputMap.containsKey("noutsourcerequired")
										&& (int) inputMap.get("noutsourcerequired") == Enumeration.TransactionStatus.YES
												.gettransactionstatus();
				final String npreRegNo = inputMap.containsKey("selectedPreregno")
						&& inputMap.get("selectedPreregno") != null ? inputMap.get("selectedPreregno").toString()
								: inputMap.containsKey("npreregno") ? inputMap.get("npreregno").toString() : null;
				final String nexternalordercode = inputMap.containsKey("OrderCodeData")
						&& inputMap.get("OrderCodeData") != null ? inputMap.get("OrderCodeData").toString() : null;
				if (outsourcedRequiredValue && npreRegNo != null && nexternalordercode != null) {
					
//					Map<String, Object> mapExternalOrderAttachment = (Map<String, Object>) getExternalOrderAttachment(
//							nexternalordercode, npreRegNo, userInfo).getBody();
//					objMap.putAll(mapExternalOrderAttachment);
				}
			} else {
				if ((int) inputMap.get("noutsourcerequired") == Enumeration.TransactionStatus.YES
						.gettransactionstatus()) {
					if (inputMap.get("OrderCodeData") != null && inputMap.get("selectedPreregno") != null) {
						String nexternalordercode = inputMap.get("OrderCodeData").toString();
//						Map<String, Object> mapExternalOrderAttachment = (Map<String, Object>) getExternalOrderAttachment(
//								nexternalordercode, inputMap.get("selectedPreregno").toString(), userInfo).getBody();
//						objMap.putAll(mapExternalOrderAttachment);
					}
					if (inputMap.get("selectedPreregno") != null
							&& inputMap.get("selectedTransactionsamplecode") != null) {
//						Map<String, Object> mapOutsourceDetails = (Map<String, Object>) getOutsourceDetails(
//								inputMap.get("selectedPreregno").toString(),
//								inputMap.get("selectedTransactionsamplecode").toString(), userInfo).getBody();
//						objMap.putAll(mapOutsourceDetails);
					}
				}
			}

		} else {
			objMap.put("selectedSubSample", Arrays.asList());
			if (!subSample || (int) inputMap.get("checkBoxOperation") == 3) {
				objMap.put("selectedTest", Arrays.asList());
				objMap.put("RegistrationGetTest", Arrays.asList());
				objMap.put("activeSubSampleTab", "IDS_SUBSAMPLEATTACHMENTS");
				objMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
			}
		}
		return new ResponseEntity<>(objMap, HttpStatus.OK);

	}
	
	
	public ResponseEntity<Object> getRegistrationTest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap();
		// String stransactioncode = "";
		boolean subSample = false;
		if (inputMap.containsKey("nneedsubsample")) {
			subSample = (boolean) inputMap.get("nneedsubsample");
		}
		final String query ="select ssettingvalue from settings where nsettingcode ="
						+Enumeration.Settings.UPDATING_ANALYSER.getNsettingcode()
						+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "";
		final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(query, Settings.class, jdbcTemplate);
		
		boolean updateAnalyst = false;
		if(objSettings !=null) {
			updateAnalyst = Integer.valueOf(objSettings.getSsettingvalue())==Enumeration.TransactionStatus.YES.gettransactionstatus() ? true:false;

		}		
		final String query1 = "select * from fn_registrationtestget('" + inputMap.get("npreregno") + "'::text," + "'"
				+ inputMap.get("ntransactionsamplecode") + "'::text" + ",'" + inputMap.get("ntransactiontestcode")
				+ "'::text," + "" + inputMap.get("ntype") + "," + userInfo.getNtranssitecode() + ",'"
				+ userInfo.getSlanguagetypecode() + "','"
				+ commonFunction.getMultilingualMessage("IDS_REGNO", userInfo.getSlanguagefilename()) + "'" + ",'"
				+ commonFunction.getMultilingualMessage("IDS_SUBREGNO", userInfo.getSlanguagefilename()) + "',"+updateAnalyst+")";

		LOGGER.info("test Start========?" + LocalDateTime.now());
		
		final String lstData2 = jdbcTemplate.queryForObject(query1, String.class);
		
		LOGGER.info("fn_registrationtestget:" + query1);
		LOGGER.info("test end========?" + LocalDateTime.now());
		
		
		if (lstData2 != null) {

			ObjectMapper obj = new ObjectMapper();
			List<Map<String, Object>> lstData = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					lstData2, userInfo, true, (int) inputMap.get("ndesigntemplatemappingcode"), "test");
			objMap.put("RegistrationGetTest", lstData);
			String Stransactiontestcode = "";
			if (inputMap.containsKey("ntype")) {
				if ((int) inputMap.get("ntype") == 1) {
					List<Map<String, Object>> lstData1 = obj.convertValue(inputMap.get("registrationtest"),
							new TypeReference<List<Map<String, Object>>>() {
							});
					List<Map<String, Object>> filteredList = lstData.stream()
							.filter(empl -> lstData1.stream()
									.anyMatch(dept -> String.valueOf(dept.get("ntransactiontestcode"))
											.equals(String.valueOf(empl.get("ntransactiontestcode")))))
							.collect(Collectors.toList());

					Stransactiontestcode = filteredList.stream().map(x -> String.valueOf(x.get("ntransactiontestcode")))
							.collect(Collectors.joining(","));
					objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
				} else if ((int) inputMap.get("ntype") == 2) {

					Stransactiontestcode = lstData.stream().map(x -> String.valueOf(x.get("ntransactiontestcode")))
							.collect(Collectors.joining(","));
					objMap.put("selectedTest", lstData);
				}

				else if ((int) inputMap.get("ntype") == 3) {
					Stransactiontestcode = (String) inputMap.get("ntransactiontestcode");
					objMap.put("selectedTest", lstData);
				} else if ((int) inputMap.get("ntype") == 4) {
					Stransactiontestcode = (String) inputMap.get("ntransactiontestcode");
					List<String> myList = new ArrayList<String>(Arrays.asList(Stransactiontestcode.split(",")));
					lstData = lstData.stream().filter(
							x -> myList.stream().anyMatch(y -> y.equals(String.valueOf(x.get("ntransactiontestcode")))))
							.collect(Collectors.toList());
					objMap.put("selectedTest", lstData);
				} else if ((int) inputMap.get("ntype") == 5) {
					Stransactiontestcode = String.valueOf(lstData.get(lstData.size() - 1).get("ntransactiontestcode"));
					objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
				}

				else {
					Stransactiontestcode = String.valueOf(lstData.get(lstData.size() - 1).get("ntransactiontestcode"));
					objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
				}
			} else {// ntype=null
				Stransactiontestcode = String.valueOf(lstData.get(lstData.size() - 1).get("ntransactiontestcode"));
				if (inputMap.get("checkBoxOperation") != null && (int) inputMap.get("checkBoxOperation") == 7) {
					List<String> subSampleArray = Arrays
							.asList(((String) inputMap.get("ntransactionsamplecode")).split(","));
					for (Map<String, Object> map : lstData) {
						if ((int) map.get("ntransactionsamplecode") == Integer.parseInt(subSampleArray.get(0))) {
							Stransactiontestcode = map.get("ntransactiontestcode").toString();
							// break;
						}
					}
					inputMap.put("ntransactiontestcode", Stransactiontestcode);
				}

				objMap.put("selectedTest", Arrays.asList(lstData.get(lstData.size() - 1)));
			}

			if (!inputMap.containsKey("withoutgetparameter")) {
				if (!inputMap.containsKey("RulesEngineGet")) {
					objMap.putAll(transactionDAOSupport.getActiveTestTabData(Stransactiontestcode, (String) inputMap.get("npreregno"),
							(String) inputMap.get("activeTestTab"), userInfo));
					inputMap.put("activeTestTab", (String) inputMap.get("activeTestTab"));
					if (subSample) {
						objMap.putAll(transactionDAOSupport.getActiveSubSampleTabData((String) inputMap.get("ntransactionsamplecode"),
								(String) inputMap.get("activeSubSampleTab"), userInfo));
					}
				}
			}

			final boolean outsourceRequiredValue = inputMap.containsKey("noutsourcerequired")
					? (int) inputMap.get("noutsourcerequired") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()
					: inputMap.containsKey("RealSampleTypeValue") && inputMap.get("RealSampleTypeValue") != null
							&& ((SampleType) inputMap.get("RealSampleTypeValue"))
									.getNoutsourcerequired() == Enumeration.TransactionStatus.YES
											.gettransactionstatus();
			if (outsourceRequiredValue) {
				final String seletedPreRegNo = (inputMap.containsKey("selectedPreregno")
						&& inputMap.get("selectedPreregno") != null) ? inputMap.get("selectedPreregno").toString()
								: inputMap.containsKey("npreregno") ? inputMap.get("npreregno").toString() : null;
				final String selectedTransactionSampleCode = ((inputMap.containsKey("selectedTransactionsamplecode")
						&& inputMap.get("selectedTransactionsamplecode") != null)
								? inputMap.get("selectedTransactionsamplecode").toString()
								: inputMap.containsKey("ntransactionsamplecode")
										? inputMap.get("ntransactionsamplecode").toString()
										: null);
				if (seletedPreRegNo != null && selectedTransactionSampleCode != null) {
//					final Map<String, Object> mapOutsourceDetails = (Map<String, Object>) getOutsourceDetails(
//							seletedPreRegNo, selectedTransactionSampleCode, userInfo).getBody();
//					objMap.putAll(mapOutsourceDetails);
				}
			}

		} else {
			objMap.put("selectedTest", Arrays.asList());
			objMap.put("RegistrationParameter", Arrays.asList());
			objMap.put("activeTestTab", "IDS_PARAMETERRESULTS");
			objMap.put("RegistrationGetTest", Arrays.asList());

		}

		return new ResponseEntity<>(objMap, HttpStatus.OK);

	}
	
	
	
	public Map<String, Object> getRegistrationLabel(Map<String, Object> inputMap,UserInfo userInfo) throws Exception { 
		
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>(); 
		Optional<ExternalOrder> SelectedExternalOrder = null;
		List<ExternalOrder> lstExternalOrder=jdbcTemplate.query("select * from externalorder where "
				+ " nexternalordercode>0 and nordertypecode= " +Enumeration.OrderType.EXTERNAL.getOrderType()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNtranssitecode()
				+"",
				new ExternalOrder());
		List<Registration> lstRegistration=jdbcTemplate.query("select * from registration where "
				+ " npreregno in ("+inputMap.get("allPreregno")+") "
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNtranssitecode()
				+ ""
				,new Registration());

		for (int i = 0; i < lstRegistration.size(); i++) {
			Map<String, Object> objRegistration =lstRegistration.get(i).getJsondata();
			List<String> keys = new ArrayList<>(objRegistration.keySet());
			Map<String, Object> RegistrationMap = new LinkedHashMap<String, Object>();
			for (int j = 0; j < keys.size(); j++) {
				String key = keys.get(j);
				if (objRegistration.get(key).getClass().toString().equals("class java.util.HashMap")) {
					Map<String, Object> object = (Map<String, Object>) objRegistration.get(key);
					if (object.containsKey("pkey") && object.get("pkey").equals("nexternalordercode")) {
						final int nexternalordercode = (int) Integer.valueOf(object.get("value").toString());
						SelectedExternalOrder = lstExternalOrder.stream()
								.filter(x -> ((int) x.getNexternalordercode()) == nexternalordercode).findAny();
						if(SelectedExternalOrder.isPresent()) {
							RegistrationMap.put("sorderseqno", SelectedExternalOrder.get().getSorderseqno());
						}					
					}	
				}
			}
			if(!RegistrationMap.isEmpty()) {
				returnMap.put(String.valueOf(lstRegistration.get(i).getNpreregno()) , RegistrationMap);
			}
		}
		if(!returnMap.isEmpty()){

			//ALPD-4039 Checked ContainsKey For "nexternalordertypecode" before iterator 22/05/2024 By Aravindh
			//List<Registration> lstPortalExternalOrderRegistration = lstRegistration.stream().filter(lst -> (int) lst.getJsonuidata().get("nexternalordertypecode")
			List<Registration> lstPortalExternalOrderRegistration = lstRegistration.stream().filter(lst -> lst.getJsonuidata().containsKey("nexternalordertypecode") && (int) lst.getJsonuidata().get("nexternalordertypecode")
					== Enumeration.ExternalOrderType.PORTAL.getExternalOrderType()).collect(Collectors.toList());
			boolean isPortalData = lstPortalExternalOrderRegistration.isEmpty() ? false : true;
			returnMap.put("isPortalData", isPortalData);
		}

		return returnMap;

	}
	
	public List<RegistrationTest> getTestPrice(List<RegistrationTest> testGroupTestList) throws Exception {
		final String testPriceString = " select tpd.ntestcode, tpd.ncost "
										+ " from testpricedetail tpd, testpriceversion tpv where tpv.ntransactionstatus= "
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
										+ " and tpv.npriceversioncode=tpd.npriceversioncode "
										+ " and tpv.nstatus=tpd.nstatus and tpv.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<TestMaster> masterList = jdbcTemplate.query(testPriceString, new TestMaster());

		for (RegistrationTest tgt : testGroupTestList) {
			for (TestMaster tm : masterList) {
				if (tgt.getNtestcode() == tm.getNtestcode())
					tgt.setNcost(tm.getNcost().floatValue());
			}
		}
		return testGroupTestList;
	}

	
	public String fnStringFormationForAutoFlow(Map<String, Object> inputMap, String flowType, int napprovalversioncode)
			throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),
				new TypeReference<UserInfo>() {});
		int nreghistorycode = (int) inputMap.get("registrationhistory");
		//int nregsamplehistorycode = (int) inputMap.get("registrationsamplehistory");
		int ntesthistorycode = (int) inputMap.get("registrationtesthistory");

		boolean checkManatoryParameter = true;
		//int nRegistrationStatus = Enumeration.TransactionStatus.REGISTERED.gettransactionstatus();
		String insertlistpreregno = (String) inputMap.get("insertlistpreregno");

		int nregtypecode = (int) inputMap.get("nregtypecode");
		int nregsubtypecode = (int) inputMap.get("nregsubtypecode");

		ApprovalConfigRole objApprovalConfigRole = new ApprovalConfigRole();
		List<RegistrationTestHistory> listRegistrationTestHistory = checkActiveFlowSample(insertlistpreregno, userInfo);

		String strRegistrationHistory = "";
		String insertTransactiontest = "";
		String strRegTestHistory = "";
	//	String strApprovalParameter = "";
		int nSampleApprovalHistorycode = 0;
		String returnString = "Success";

		String[] strPreregno = insertlistpreregno.split(",");

		String strUserRoleCode = " (select nuserrolecode  from treetemplatetransactionrole "
								+ "	where ntemptransrolecode in (select max(ttr.ntemptransrolecode)"
								+ " from treetemplatetransactionrole ttr,approvalconfig ac,approvalconfigversion acv "
								+ "	where ac.napprovalconfigcode=ttr.napprovalconfigcode "
								+ "	and ac.napprovalconfigcode = acv.napprovalconfigcode "
								+ "	and ttr.ntreeversiontempcode = acv.ntreeversiontempcode " + " and ttr.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and acv.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ac.nregtypecode= " + nregtypecode
								+ " and ac.nregsubtypecode= " + nregsubtypecode + " and acv.napproveconfversioncode = "
								+ napprovalversioncode 
								+ " and ttr.nsitecode = " + userInfo.getNmastersitecode()
								+ " and ac.nsitecode = " + userInfo.getNmastersitecode()
								+ " and acv.nsitecode = " + userInfo.getNmastersitecode()
								+ " group by ttr.ntreeversiontempcode))";
		int nuserrolecode = jdbcTemplate.queryForObject(strUserRoleCode, Integer.class);

		if (listRegistrationTestHistory.size() > 0) {
			insertTransactiontest = stringUtilityFunction.fnDynamicListToString(listRegistrationTestHistory, "getNtransactiontestcode");

			String strParamterList = "select case when tgtp.nparametertypecode = "
								+ Enumeration.ParameterType.NUMERIC.getparametertype()
								+ " then tgtnp.sresultvalue else case when tgtp.nparametertypecode = "
								+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " then tgtpp.spredefinedname"
								+ " else case when tgtp.nparametertypecode = "
								+ Enumeration.ParameterType.CHARACTER.getparametertype()
								+ " then tgtcp.scharname else null end end end sresultvalue,"
								+ " tgtp.nparametertypecode, tgtp.ntestgrouptestparametercode, tgt.ntestgrouptestcode, tgtp.nresultmandatory"
								+ " from registrationtest rt inner join testgrouptest tgt"
								+ " on rt.ntestgrouptestcode=tgt.ntestgrouptestcode " 
								+ " and rt.nstatus=  "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
								+ " and rt.nsitecode="+ userInfo.getNtranssitecode()
								+ " inner join testgrouptestparameter tgtp on tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode "
								+ " and tgtp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgtp.nsitecode="+ userInfo.getNmastersitecode()
								+ " left outer join testgrouptestcharparameter tgtcp on tgtcp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
								+ " and tgtcp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgtcp.nsitecode="+ userInfo.getNmastersitecode()
								+ " left outer join testgrouptestnumericparameter tgtnp on tgtnp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
								+ " and tgtnp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgtnp.nsitecode="+ userInfo.getNmastersitecode()
								+ " left outer join testgrouptestpredefparameter tgtpp on tgtpp.ntestgrouptestparametercode = tgtp.ntestgrouptestparametercode "
								+ " and tgtpp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgtpp.nsitecode="+ userInfo.getNmastersitecode()
								+ " where rt.ntransactiontestcode in (" + insertTransactiontest + ") "
								+ " and tgt.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and tgt.nsitecode="+ userInfo.getNmastersitecode();

			final List<TestGroupTestParameter> lstTestGroupTestParameter = (List<TestGroupTestParameter>) jdbcTemplate.query(
					strParamterList, new TestGroupTestParameter());

			outer: for (int i = 0; i < listRegistrationTestHistory.size(); i++) {
				int ntestgrouptestcode = listRegistrationTestHistory.get(i).getNtestgrouptestcode();
				List<TestGroupTestParameter> listResultParameter1 = lstTestGroupTestParameter.stream()
						.filter(obj -> obj.getNtestgrouptestcode() == ntestgrouptestcode).collect(Collectors.toList());

				// No mandatory field reject update
				boolean stringTest = !listResultParameter1.stream()
						.filter(objnonmandyres -> objnonmandyres
								.getNresultmandatory() == Enumeration.TransactionStatus.YES.gettransactionstatus()
								&& objnonmandyres.getSresultvalue() == null)
						.collect(Collectors.toList()).isEmpty();

				checkManatoryParameter = listResultParameter1.stream()
						.anyMatch(objnonmandyres -> objnonmandyres
								.getNresultmandatory() == Enumeration.TransactionStatus.YES.gettransactionstatus()
								&& objnonmandyres.getSresultvalue() == null);

				if (checkManatoryParameter) {
					returnString = "IDS_NODEFAULTRESULT";
					return returnString;
				}
				if (stringTest) {
					strRegTestHistory = strRegTestHistory.concat("( " + (++ntesthistorycode) + " ,"
							+ listRegistrationTestHistory.get(i).getNtransactiontestcode() + ","
							+ listRegistrationTestHistory.get(i).getNtransactionsamplecode() + ","
							+ listRegistrationTestHistory.get(i).getNpreregno() + " " + "," + userInfo.getNformcode()
							+ " ," + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " ,"
							+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ","
							+ userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
							+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");

				} else {
					strRegTestHistory = strRegTestHistory.concat("( " + (++ntesthistorycode) + " ,"
							+ listRegistrationTestHistory.get(i).getNtransactiontestcode() + ","
							+ listRegistrationTestHistory.get(i).getNtransactionsamplecode() + ","
							+ listRegistrationTestHistory.get(i).getNpreregno() + " " + "," + userInfo.getNformcode()
							+ " ," + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + ","
							+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + ","
							+ userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
							+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ),");

					strRegTestHistory = strRegTestHistory.concat("( " + (++ntesthistorycode) + " ,"
							+ listRegistrationTestHistory.get(i).getNtransactiontestcode() + ","
							+ listRegistrationTestHistory.get(i).getNtransactionsamplecode() + ","
							+ listRegistrationTestHistory.get(i).getNpreregno() + " " + "," + userInfo.getNformcode()
							+ "," + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + "," + "-1,"
							+ nuserrolecode + "," + "-1," + nuserrolecode + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason())
							+ "','" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
							+ "', " + Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ),");
				}
			}
		}

		if (flowType.equals("AutoComplete")) {

			for (int k = 0; k < strPreregno.length; k++) {

				strRegistrationHistory = strRegistrationHistory.concat(" ( " + (++nreghistorycode) + " " + ", "
						+ strPreregno[k] + "," + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
						+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
						+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");

				strRegistrationHistory = strRegistrationHistory.concat(" ( " + ++nreghistorycode + " " + ", "
						+ strPreregno[k] + "," + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + "-1 ," + nuserrolecode + ",-1," + nuserrolecode + ",'"
						+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");
			}

		} else {
			//int nregdecisionhistorycode = (int) inputMap.get("registrationdecisionhistory");
			if (flowType.equals("AutoApprovalFlowWithRoleWise")) {
				objApprovalConfigRole = getAutoCompleteRoleFromConfigVersion(napprovalversioncode);
			} else {
				objApprovalConfigRole = getAutoApprovalRoleFromConfigVersion(napprovalversioncode);
			}

			nSampleApprovalHistorycode = (int) inputMap.get("sampleapprovalhistory");
			for (int k = 0; k < strPreregno.length; k++) {
				strRegistrationHistory = strRegistrationHistory.concat(" ( " + ++nreghistorycode + " " + ", "
						+ strPreregno[k] + "," + Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + "," + userInfo.getNuserrole()
						+ "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole() + ",'"
						+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");

				strRegistrationHistory = strRegistrationHistory.concat(" ( " + ++nreghistorycode + " " + ", "
						+ strPreregno[k] + "," + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ," + "-1 ," + nuserrolecode + ",-1," + nuserrolecode + ",'"
						+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");

				strRegistrationHistory = strRegistrationHistory
						.concat("( " + ++nreghistorycode + "  " + ", " + strPreregno[k] + " ,"
								+ objApprovalConfigRole.getNapprovalstatuscode() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
								+ "'," + "-1 ," + objApprovalConfigRole.getNuserrolecode() + ",-1,"
								+ objApprovalConfigRole.getNuserrolecode() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason())
								+ "'," + userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");
			}
//			String strSampleAppHistory = " insert into sampleapprovalhistory (napprovalhistorycode,npreregno,nusercode,nuserrolecode,ndeputyusercode,"
//										+ "ndeputyuserrolecode,ntransactionstatus, dtransactiondate,scomments,nsitecode, nstatus ) "
//										+ " select  " + nSampleApprovalHistorycode
//										+ " + rank() over (order by npreregno) as napprovalhistorycode , " + " npreregno npreregno,"
//										+ "-1 ," + objApprovalConfigRole.getNuserrolecode() + ",-1,"
//										+ objApprovalConfigRole.getNuserrolecode() + "," + objApprovalConfigRole.getNapprovalstatuscode()
//										+ ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "','Auto Approved'," + userInfo.getNtranssitecode() + ","
//										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//										+ "from registrationhistory rt  where rt.nstatus ="
//										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rt.npreregno in ( "
//										+ insertlistpreregno + " ) and rt.ntransactionstatus = "
//										+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ";";

//			String strDecisionHstory = " insert into registrationdecisionhistory (nregdecisionhistorycode,npreregno,ndecisionstatus,dtransactiondate,nusercode,nuserrolecode,"
//					+ "ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode, nstatus) select "
//					+ nregdecisionhistorycode
//					+ " + rank() over (order by npreregno ) as  nregdecisionhistorycode ,npreregno as npreregno, "
//					+ objApprovalConfigRole.getNautodescisionstatuscode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)// objGeneral.getUTCDateTime()
//					+ "',-1," + objApprovalConfigRole.getNuserrolecode() + ",-1,"
//					+ objApprovalConfigRole.getNuserrolecode() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "',"
//					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from registrationdecisionhistory where npreregno in (" + insertlistpreregno + " );";

			int nsampleApprovalForTestHistory = nSampleApprovalHistorycode;

			for (int k = 0; k < listRegistrationTestHistory.size(); k++) {
				++nsampleApprovalForTestHistory;
				strRegTestHistory = strRegTestHistory.concat("( " + (++ntesthistorycode) + " ,"
									+ listRegistrationTestHistory.get(k).getNtransactiontestcode() + ","
									+ listRegistrationTestHistory.get(k).getNtransactionsamplecode() + ","
									+ listRegistrationTestHistory.get(k).getNpreregno() + " " + "," + userInfo.getNformcode() + ","
									+ objApprovalConfigRole.getNapprovalstatuscode() + "," + "-1,"
									+ objApprovalConfigRole.getNuserrolecode() + "," + "-1,"
									+ objApprovalConfigRole.getNuserrolecode() + ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + nsampleApprovalForTestHistory + ","
									+ userInfo.getNtranssitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ),");
			}
		}

//		String strSampleAppHistory = "insert into registrationsamplehistory (nsamplehistorycode,ntransactionsamplecode,npreregno,ntransactionstatus "
//				+ ",dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) "
//				+ " select " + nregsamplehistorycode
//				+ "+Rank() over (order by ntransactionsamplecode) as nsamplehistorycode, ntransactionsamplecode as ntransactionsamplecode,"
//				+ " npreregno npreregno," + nRegistrationStatus + " as ntransactionstatus, '"
//				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNusercode() + ","
//				+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole()
//				+ ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from "
//				+ " registrationsamplehistory where npreregno in ( " + insertlistpreregno + ") and ntransactionstatus ="
//				+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus();// + ";");

		final String strRegTestHistoryQuery = "insert into registrationtesthistory (ntesthistorycode,ntransactiontestcode,ntransactionsamplecode,npreregno,nformcode,"
												+ "ntransactionstatus,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode "
												+ ",scomments,dtransactiondate,nsampleapprovalhistorycode,nsitecode, nstatus) values"
												+ strRegTestHistory.substring(0, strRegTestHistory.length() - 1) + ";";
		jdbcTemplate.execute(strRegTestHistoryQuery);

		final String strRegistrationHistoryQuery = " insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode "
														+ " ,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus) values"
														+ strRegistrationHistory.substring(0, strRegistrationHistory.length() - 1) + ";";

		jdbcTemplate.execute(strRegistrationHistoryQuery);

		String strResultParameter = "insert into resultparameter( ntransactionresultcode, npreregno, ntransactiontestcode, ntestgrouptestparametercode, ntestparametercode,"
									+ " nparametertypecode, nroundingdigits, nresultmandatory, nreportmandatory, ntestgrouptestformulacode, nunitcode, ngradecode, ntransactionstatus,"
									+ " sfinal, sresult, nenforcestatus, nenforceresult,ncalculatedresult, smina, sminb, smaxb, smaxa, sminlod, smaxlod, sminloq, smaxloq, sdisregard, sresultvalue,"
									+ " dentereddate, nenteredby, nenteredrole, ndeputyenteredby, ndeputyenteredrole, nfilesize, ssystemfilename, nlinkcode, nattachmenttypecode, nsitecode, nstatus)"
									+ " select ntransactionresultcode, rp.npreregno, rp.ntransactiontestcode, rp.ntestgrouptestparametercode,"
									+ " rp.ntestparametercode, rp.nparametertypecode, rp.nroundingdigits, rp.nresultmandatory, rp.nreportmandatory, rp.ntestgrouptestformulacode,"
									+ " rp.nunitcode, case when rp.nparametertypecode = "
									+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then ("
									+ " case when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is not null "
									+ " then  " + " case when cast(tgtnp.sresultvalue as float)<=tgtnp.smaxb then "
									+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
									+ " when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is null then "
									+ " case when cast(tgtnp.sresultvalue as float)<=tgtnp.smaxa then " + Enumeration.Grade.PASS.getGrade()
									+ " else " + Enumeration.Grade.OOS.getGrade() + " end "
									+ " when tgtnp.sminb is null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
									+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smaxa and tgtnp.smaxb then "
									+ Enumeration.Grade.OOS.getGrade() + " when cast(tgtnp.sresultvalue as float) > tgtnp.smaxb " + " then "
									+ Enumeration.Grade.OOT.getGrade() + " else " + Enumeration.Grade.PASS.getGrade() + " end "
					
									+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is null "
									+ " then case when cast(tgtnp.sresultvalue as float)>=tgtnp.smina then "
									+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
					
									+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is not null then "
									+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxb then "
									+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
					
									+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is null "
									+ " then case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
									+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
					
									+ " when tgtnp.sminb is null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
									+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
									+ Enumeration.Grade.PASS.getGrade()
									+ " when cast(tgtnp.sresultvalue as float) between tgtnp.smaxa and tgtnp.smaxb then "
									+ Enumeration.Grade.OOS.getGrade() + " else " + Enumeration.Grade.OOT.getGrade() + " end "
					
									+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is null then "
									+ " case when cast(tgtnp.sresultvalue as float) >= tgtnp.sminb then "
									+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
					
									+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is null and tgtnp.smaxb is not null then "
									+ " case when cast(cast(tgtnp.sresultvalue as float) as float) between tgtnp.sminb and tgtnp.smaxb then "
									+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
					
									+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is null then "
									+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smaxa then "
									+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
					
									+ " when tgtnp.sminb is not null and tgtnp.smina is null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
									+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smaxa then "
									+ Enumeration.Grade.PASS.getGrade() + " else " + Enumeration.Grade.OOS.getGrade() + " end "
					
									+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is null then "
									+ " case when cast(tgtnp.sresultvalue as float)	between tgtnp.sminb and tgtnp.smina then "
									+ Enumeration.Grade.OOS.getGrade() + " when cast(tgtnp.sresultvalue as float) < tgtnp.sminb then "
									+ Enumeration.Grade.OOS.getGrade() + " when cast(tgtnp.sresultvalue as float) >= tgtnp.smina  "
									+ " then " + Enumeration.Grade.PASS.getGrade() + " end  "
					
									+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is null and tgtnp.smaxb is not null "
									+ " then case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxb then "
									+ Enumeration.Grade.PASS.getGrade()
									+ " when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smina then "
									+ Enumeration.Grade.OOS.getGrade() + " when cast(tgtnp.sresultvalue as float) > tgtnp.smaxb"
									+ " or cast(tgtnp.sresultvalue as float) < tgtnp.sminb then " + Enumeration.Grade.OOT.getGrade()
									+ " end "
					
									+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is null  then "
									+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
									+ Enumeration.Grade.PASS.getGrade()
									+ " when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smina then "
									+ Enumeration.Grade.OOS.getGrade() + " else " + Enumeration.Grade.OOT.getGrade() + " end"
					
									+ " when tgtnp.sminb is not null and tgtnp.smina is not null and tgtnp.smaxa is not null and tgtnp.smaxb is not null then "
									+ " case when cast(tgtnp.sresultvalue as float) between tgtnp.smina and tgtnp.smaxa then "
									+ Enumeration.Grade.PASS.getGrade() + "  "
					
									+ " when cast(tgtnp.sresultvalue as float) between tgtnp.sminb and tgtnp.smaxb then "
									+ Enumeration.Grade.OOT.getGrade() + " "
									+ " when (cast(tgtnp.sresultvalue as float) < tgtnp.sminb or tgtnp.smaxb < cast(tgtnp.sresultvalue as float))"
									+ " and (tgtnp.sminb!=0 and tgtnp.smaxb!=0)then " + Enumeration.Grade.OOS.getGrade() + " else "
									+ Enumeration.Grade.PASS.getGrade() + " end else " + Enumeration.Grade.NA.getGrade() + " end)"
					
									+ " when rp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
									+ " then tgtpp.ngradecode" + " when rp.nparametertypecode = "
									+ Enumeration.ParameterType.CHARACTER.getparametertype() + " then " + Enumeration.Grade.FIO.getGrade()
									+ " " + " when rp.nparametertypecode = " + Enumeration.ParameterType.ATTACHEMENT.getparametertype()
									+ " then " + Enumeration.Grade.PASS.getGrade() + " else "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " end as ngradecode,"
					
									+ " case when (case  when rp.nparametertypecode = "
									+ Enumeration.ParameterType.NUMERIC.getparametertype()
									+ " then tgtnp.sresultvalue when rp.nparametertypecode = "
									+ Enumeration.ParameterType.PREDEFINED.getparametertype() + " then tgtpp.spredefinedname"
									+ " when rp.nparametertypecode = " + Enumeration.ParameterType.CHARACTER.getparametertype()
									+ " then tgtcp.scharname" + " when rp.nparametertypecode = "
									+ Enumeration.ParameterType.ATTACHEMENT.getparametertype() + " then NULL else NULL end) is null"
									+ " then " + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " else "
									+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " end as ntransactionstatus, "
									+ " case " + " when rp.nparametertypecode = " + Enumeration.ParameterType.NUMERIC.getparametertype()
									+ " then (case when CHARINDEX('.', tgtnp.sresultvalue, 1) < =0 then "
									+ " tgtnp.sresultvalue+'.'+dbo.RoundDigits(rp.nroundingdigits) else tgtnp.sresultvalue end)"
									+ " when rp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
									+ " then tgtpp.spredefinedname" + " when rp.nparametertypecode = "
									+ Enumeration.ParameterType.CHARACTER.getparametertype() + " then tgtcp.scharname"
									+ " when rp.nparametertypecode = " + Enumeration.ParameterType.ATTACHEMENT.getparametertype()
									+ " then NULL else NULL end as sfinal, case when rp.nparametertypecode = "
									+ Enumeration.ParameterType.NUMERIC.getparametertype() + " then tgtnp.sresultvalue"
									+ " when rp.nparametertypecode = " + Enumeration.ParameterType.PREDEFINED.getparametertype()
									+ " then tgtpp.spredefinedname" + " when rp.nparametertypecode = "
									+ Enumeration.ParameterType.CHARACTER.getparametertype() + " then tgtcp.scharname"
									+ " when rp.nparametertypecode = " + Enumeration.ParameterType.ATTACHEMENT.getparametertype()
									+ " then NULL" + " else NULL end as sresult, " + " "
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforcestatus, "
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as nenforceresult, "
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " as ncalculatedresult, "
									+ " rp.smina, rp.sminb, rp.smaxb, rp.smaxa, rp.sminlod, rp.smaxlod, rp.sminloq, rp.smaxloq, rp.sdisregard, rp.sresultvalue, "
									+ " N'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' dentereddate, "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nenteredby, " + " " + nuserrolecode
									+ " nenteredrole, " + Enumeration.TransactionStatus.NA.gettransactionstatus() + " ndeputyenteredby, "
									+ nuserrolecode + " ndeputyenteredrole, " + " "
									+ Enumeration.TransactionStatus.ALL.gettransactionstatus() + " nfilesize, NULL ssystemfilename, "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nlinkcode, " + " "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " nattachmenttypecode, "
									+ userInfo.getNtranssitecode() + " nsitecode,"
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " nstatus"
									+ " from registrationtest rt,registrationparameter rp,testgrouptest tgt,"
									+ " testgrouptestparameter tgtp   "
									+ " left outer join testgrouptestpredefparameter tgtpp on tgtp.ntestgrouptestparametercode = tgtpp.ntestgrouptestparametercode "
									+ " and tgtpp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgtpp.ndefaultstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
									+ " left outer join testgrouptestcharparameter tgtcp on tgtp.ntestgrouptestparametercode = tgtcp.ntestgrouptestparametercode "
									+ " and tgtcp.nstatus =" +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " left outer join testgrouptestnumericparameter tgtnp on tgtp.ntestgrouptestparametercode = tgtnp.ntestgrouptestparametercode "
									+ " and tgtnp.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " where "
									+ " rt.ntestgrouptestcode = tgt.ntestgrouptestcode and tgtp.ntestgrouptestcode = tgt.ntestgrouptestcode "
									+ " and rt.ntransactiontestcode=rp.ntransactiontestcode  "
									+ " and rt.nsitecode="+ userInfo.getNtranssitecode()
									+ " and rp.nsitecode ="+ userInfo.getNtranssitecode()
									+ " and rp.nsitecode=" + userInfo.getNtranssitecode()
									+ " and tgt.nsitecode="+ userInfo.getNmastersitecode()
									+ " and tgtp.nsitecode ="+ userInfo.getNmastersitecode()
									+ " and tgtpp.nsitecode=" + userInfo.getNmastersitecode()
									+ " and tgtcp.nsitecode ="+ userInfo.getNmastersitecode()
									+ " and tgtnp.nsitecode ="+ userInfo.getNmastersitecode()
									+ " and rt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgtp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgtp.nparametertypecode=rp.nparametertypecode "
									+ " and rp.ntestgrouptestparametercode =tgtp.ntestgrouptestparametercode "
									+ " and rp.ntransactiontestcode in (" + insertTransactiontest + ")";

		jdbcTemplate.execute(strResultParameter);

		strResultParameter = "insert into resultparametercomments (ntransactionresultcode,ntransactiontestcode "
							+ ",npreregno, ntestgrouptestparametercode ,ntestparametercode ,sresultcomment "
							+ ",senforcestatuscomment ,senforceresultcomment, nsitecode "
							+ ",nstatus )  select rp.ntransactionresultcode "
							+ ",rp.ntransactiontestcode,rp.npreregno,rp.ntestgrouptestparametercode,rp.ntestparametercode , NULL,NULL,NULL,"
							+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " from registrationparameter rp"
							+ " where npreregno in (" + insertlistpreregno + ")"
							+ " and rp.nsitecode=" + userInfo.getNtranssitecode()
							+ " and rp.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		jdbcTemplate.execute(strResultParameter);

		strResultParameter = "insert into approvalparameter ( " + " ntransactionresultcode "
							+ " ,npreregno,ntransactiontestcode,ntestgrouptestparametercode "
							+ " ,ntestparametercode,nparametertypecode,nroundingdigits,nresultmandatory,nreportmandatory,ntestgrouptestformulacode "
							+ " ,nunitcode,ngradecode,ntransactionstatus,sfinal,sresult,nenforcestatus,nenforceresult,ncalculatedresult,smina,sminb,smaxb,smaxa,sminlod,smaxlod "
							+ " ,sminloq,smaxloq,sdisregard,sresultvalue,dentereddate,nenteredby,nenteredrole,ndeputyenteredby "
							+ " ,ndeputyenteredrole,nfilesize,ssystemfilename,nlinkcode,nattachmenttypecode,nsitecode, nstatus ) "
							+ " select ntransactionresultcode " + " ,npreregno,ntransactiontestcode,ntestgrouptestparametercode "
							+ " ,ntestparametercode,nparametertypecode,nroundingdigits,nresultmandatory,nreportmandatory,ntestgrouptestformulacode "
							+ " ,nunitcode,ngradecode,ntransactionstatus,sfinal,sresult,nenforcestatus,nenforceresult,ncalculatedresult,smina,sminb,smaxb,smaxa,sminlod,smaxlod "
							+ " ,sminloq,smaxloq,sdisregard,sresultvalue,dentereddate,nenteredby,nenteredrole,ndeputyenteredby "
							+ " ,ndeputyenteredrole,nfilesize,ssystemfilename,nlinkcode,nattachmenttypecode,nsitecode, nstatus from resultparameter "
							+ " where ntransactiontestcode in (" + insertTransactiontest + ") "
							+ " and ntransactionstatus = "	+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() 
							+ " and nsitecode="	+ userInfo.getNtranssitecode()
							+ " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		jdbcTemplate.execute(strResultParameter);

		return returnString;
	}
	
	public List<RegistrationTestHistory> checkActiveFlowSample(String npreregno, final UserInfo userInfo)
			throws Exception {

		final String strvalidation = "select rth.ntesthistorycode,rth.ntransactionstatus,rth.ntransactiontestcode,"
							+ " rth.npreregno,rth.ntransactionsamplecode,rt.ntestgrouptestcode "
							+ " from registrationtesthistory rth,registrationtest rt where"
							+ " rth.ntransactiontestcode = rt.ntransactiontestcode"
							+ " and rth.nsitecode="+ userInfo.getNtranssitecode()
							+ " and rt.nsitecode=" + userInfo.getNtranssitecode()
							+ "	and  rth.ntesthistorycode = any ("
							+ "		select max(ntesthistorycode) from registrationtesthistory where npreregno in ("
							+ npreregno + " ) and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode=" + userInfo.getNtranssitecode()
							+ " group by npreregno,ntransactionsamplecode,ntransactiontestcode" + " ) "
							+ "	and rth.ntransactionstatus not in (" + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
							+ "," + Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")";

		final List<RegistrationTestHistory> filteredTestList = jdbcTemplate.query(strvalidation,
				new RegistrationTestHistory());
		return filteredTestList;
	}

	private ApprovalConfigRole getAutoCompleteRoleFromConfigVersion(int napprovalversioncode) throws Exception {

		final String roleWiseAutoApproval = "select acr.* from approvalconfigrole acr,approvalconfigversion acv"
											+ " where acv.napproveconfversioncode=acr.napproveconfversioncode "
											+ " and acv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and acr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and acv.napproveconfversioncode="+ napprovalversioncode;

		final List<ApprovalConfigRole> roleList = (List<ApprovalConfigRole>) jdbcTemplate.query(roleWiseAutoApproval,
				new ApprovalConfigRole());
		
		ApprovalConfigRole objApprovalConfigRole = new ApprovalConfigRole();
		/*** get the top most auto approval level details starts here ***/
		for (int i = 0; i < roleList.size(); i++) {
			if (roleList.get(i).getNautoapproval() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				objApprovalConfigRole = roleList.get(i);
			} else {
				break;
			}
		}
		return objApprovalConfigRole;
	}
	
	private ApprovalConfigRole getAutoApprovalRoleFromConfigVersion(int napprovalversioncode) throws Exception {

		final String roleWiseAutoApproval = "select acap.nautoapprovalstatus as napprovalstatuscode,"
											+ " acap.nautodescisionstatus as nautodescisionstatuscode ,acr.nuserrolecode"
											+ " from approvalconfigrole acr,approvalconfigautoapproval acap,"
											+ " approvalconfigversion acv"
											+ " where acv.napproveconfversioncode=acap.napprovalconfigversioncode "
											+ " and acap.napprovalconfigversioncode = acr.napproveconfversioncode"
											+ " and acr.nlevelno = 1 "
											+ " and acv.nsftatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and acr.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and acap.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											+ " and acv.napproveconfversioncode="+ napprovalversioncode;

		return (ApprovalConfigRole) jdbcUtilityFunction.queryForObject(roleWiseAutoApproval, ApprovalConfigRole.class, jdbcTemplate);
	}
	
	public SeqNoArnoGenerator insertSiteArnoGenerator(SeqNoArnoGenerator seqnoFormat,UserInfo userinfo,Map<String, Object> inputMap)  throws Exception{

		String Str ="select count(seqnositearnogenerator.nseqnositearnogencode) from seqnositearnogenerator "
					+ " where nsitecode="+userinfo.getNtranssitecode() 
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nseqnoarnogencode="+seqnoFormat.getNseqnoarnogencode()
					+ " and nregsubtypeversioncode="+seqnoFormat.getNregsubtypeversioncode();
		int seqnositearnogenerator = jdbcTemplate.queryForObject(Str, Integer.class);
		
		if(seqnositearnogenerator == 0)
		{
			final String insertNewSeqNo = "insert into seqnositearnogenerator(nseqnositearnogencode,nregsubtypeversioncode,nseqnoarnogencode,nsequenceno,nsitecode,nstatus, dmodifieddate) " + " values ("
										+ "(select nsequenceno+1 from seqnoregtemplateversion where "
										+ " stablename ='seqnositearnogenerator' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  + ")"
										+","+seqnoFormat.getNregsubtypeversioncode()+","
									    + seqnoFormat.getNseqnoarnogencode() + ",0," +userinfo.getNtranssitecode()+ ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 			
										+ ",'" + dateUtilityFunction.getCurrentDateTime(userinfo) + "'"
										+ ");";
						
			final String updateseqNoSite = "update seqnoregtemplateversion set nsequenceno = nsequenceno+1 where stablename = 'seqnositearnogenerator';";
			jdbcTemplate.execute(insertNewSeqNo+updateseqNoSite);
		}		

		final String strSeqnoSiteFormatQuery = "select rsc.nperiodcode,rsc.jsondata,rsc.jsondata->>'dseqresetdate' dseqresetdate,"
												+ " rsc.jsondata->>'ssampleformat' ssampleformat, sasg.nsequenceno,sag.nseqnoarnogencode,rsc.nregsubtypeversioncode"
												+ " from  approvalconfig ap, seqnoarnogenerator sag,"
												+ " regsubtypeconfigversion rsc,seqnositearnogenerator sasg "
												+ " where sag.nseqnoarnogencode = sasg.nseqnoarnogencode "
												+ " and  sag.nseqnoarnogencode = rsc.nseqnoarnogencode " 
												+ " and sag.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
												+ " and ap.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
												+ " and rsc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
												+ " and sasg.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
												+ " and nregtypecode = "+ (int) inputMap.get("nregtypecode")
												+ " and nregsubtypecode =" + (int) inputMap.get("nregsubtypecode")
												+ " and rsc.napprovalconfigcode=ap.napprovalconfigcode "
												+ " and rsc.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
												+ " and sasg.nsitecode="+userinfo.getNtranssitecode()
												+ " and ap.nsitecode="+userinfo.getNmastersitecode()
												+ " and rsc.nsitecode="+userinfo.getNmastersitecode()
												+ " and sasg.nregsubtypeversioncode="+seqnoFormat.getNregsubtypeversioncode();
		seqnoFormat = jdbcTemplate.queryForObject(strSeqnoSiteFormatQuery, new SeqNoArnoGenerator());

		return seqnoFormat;
	}
	
	public ResponseEntity<Object> getTestBasesdTestPackage(Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),
				new TypeReference<UserInfo>() {});
		String strTestGetQuery = "";
		if ((boolean) inputMap.get("specBasedComponent")) {
			// final int nspecsampletypecode = (Integer)
			// inputMap.get("nspecsampletypecode");
			strTestGetQuery = " select case when tgt.nrepeatcountno = 0 then 1 else tgt.nrepeatcountno end nrepeatcountno,"
							+ " tgt.nsectioncode,tgt.nmethodcode,tgt.ninstrumentcatcode,"
							+ " tgt.ntestgrouptestcode,tgt.nspecsampletypecode,tp.stestpackagename,"
							+ " (select count(tgtp.ntestgrouptestparametercode) "
							+ "  from testgrouptestparameter tgtp "
							+ "  where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode "
							+ " and tgtp.nstatus ="	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and tgtp.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and tgtp.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()+") as nparametercount,"
							+ " tgt.ntestcode,tgt.stestsynonym,tgt.nsectioncode,s.ssectionname, m.smethodname,"
							+ " ic.sinstrumentcatname,tgt.ncost "
							+ " from testgrouptest tgt,section s,method m,instrumentcategory ic, testmaster tm,testpackage tp"
							+ " where s.nsectioncode = tgt.nsectioncode and m.nmethodcode =tgt.nmethodcode "
							+ "	and tp.ntestpackagecode=tgt.ntestpackagecode and tm.ntestcode=tgt.ntestcode "
							+ " and tgt.ntestpackagecode=" + inputMap.get("ntestpackagecode")
							+ " and ic.ninstrumentcatcode = tgt.ninstrumentcatcode and tgt.nspecsampletypecode in (  "
							+ inputMap.get("nspecsampletypecode") + ") "
							+ " and tgt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and m.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ic.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgt.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and s.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and m.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and ic.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and tp.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and tm.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and tgt.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and tm.ntestcode = tgt.ntestcode " 
							+ " and tm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and tm.ntransactionstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " order by tgt.nsorter";

		} else {
			strTestGetQuery = " select case when tgt.nrepeatcountno = 0 then 1 else tgt.nrepeatcountno end nrepeatcountno,"
							+ " tgt.nsectioncode,tgt.nmethodcode,tgt.ninstrumentcatcode,"
							+ " tgt.ntestgrouptestcode,tgt.nspecsampletypecode,tp.stestpackagename,"
							+ " (" + "		select count(tgtp.ntestgrouptestparametercode) "
							+ "		from testgrouptestparameter tgtp "
							+ "		where  tgtp.ntestgrouptestcode=tgt.ntestgrouptestcode "
							+ " and tgtp.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and tgtp.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and  tgtp.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" ) as nparametercount,"
							+ " tgt.ntestcode,tgt.stestsynonym,tgt.nsectioncode,s.ssectionname, m.smethodname,"
							+ " ic.sinstrumentcatname "
							+ " from testgrouptest tgt,section s,method m,instrumentcategory ic, testmaster tm ,"
							+ " testgroupspecsampletype tgts, testpackage tp"
							+ " where s.nsectioncode = tgt.nsectioncode and m.nmethodcode =tgt.nmethodcode "
							+ " and tp.ntestpackagecode=tgt.ntestpackagecode and tm.ntestcode=tgt.ntestcode "
							+ " and ic.ninstrumentcatcode = tgt.ninstrumentcatcode  and tgt.nspecsampletypecode = tgts.nspecsampletypecode"
							+ " and tgt.ntestpackagecode=" + inputMap.get("ntestpackagecode") 
							+ " and tgt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and m.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ic.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgts.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgt.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and s.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and m.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and ic.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and tp.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and tm.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and tgts.nsitecode ="	+ userInfo.getNmastersitecode()
							+ " and tgt.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and tm.ntestcode = tgt.ntestcode "
							+ " and tm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and tm.ntransactionstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							+ " and tgts.nallottedspeccode="+ inputMap.get("nallottedspeccode") + " order by tgt.nsorter";
		}

		final List<TestGroupTestForSample> lstTestGroupTest = jdbcTemplate.query(strTestGetQuery,
				new TestGroupTestForSample());
		return new ResponseEntity<Object>(lstTestGroupTest, HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> createTestGroupTest(final UserInfo objUserInfo, final TestGroupSpecification objSpecification,
			final List<TestGroupSpecSampleType> lstTestGroupComponent, final List<TestGroupTest> listTestGroupTest,
			final Map<String, Object> objMap) throws Exception {
		
		final Map<String, Object> tempMap = testGroupTestDAO.insertTest(objUserInfo, null, lstTestGroupComponent,
				listTestGroupTest, objMap);
		final ObjectMapper objMapper = new ObjectMapper();

		final List<String> insertQueryArray = new ArrayList<>();
		insertQueryArray.addAll((List<String>) tempMap.get("insertqry"));

		final String[] strFinal = insertQueryArray.toArray(new String[0]);
		projectDAOSupport.executeBulkDatainSingleInsert(strFinal);


		final List<String> listString = (List<String>) tempMap.get("stestgrouptestcode");
		final String stestgrouptestcode = String.join(",", listString);
	
		String sAuditQuery ="";
		sAuditQuery = "select * from testgrouptest where ntestgrouptestcode in (" + stestgrouptestcode + ")"
					+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode ="	+ objUserInfo.getNmastersitecode()
					+ ";";
		final List<TestGroupTest> lstTestGroupTest  = jdbcTemplate.query(sAuditQuery, new TestGroupTest());

		sAuditQuery	= "select tgtp.ntestgrouptestparametercode, tgtp.ntestgrouptestcode, tgtp.ntestparametercode,"
					+ " tgtp.nparametertypecode, tgtp.nunitcode, tgtp.sparametersynonym, tgtp.nroundingdigits, tgtp.nresultmandatory,"
					+ " tgtp.nreportmandatory, tgtp.ngradingmandatory, tgtp.nchecklistversioncode, tgtp.sspecdesc, tgtp.nsorter,"
					+ " tgtp.nisadhocparameter, tgtp.nisvisible, tgtp.nsitecode, tgtp.nstatus, pt.sdisplaystatus "
					+ " from testgrouptestparameter tgtp,parametertype pt "
					+ " where ntestgrouptestcode in (" + stestgrouptestcode + ") "
					+ " and tgtp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tgtp.nsitecode ="	+ objUserInfo.getNmastersitecode()
					+ " and pt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tgtp.nparametertypecode=pt.nparametertypecode;";
		
		final List<TestGroupTestParameter> lstTestGroupTestParameter  = objMapper.convertValue(commonFunction.getMultilingualMessageList(jdbcTemplate.query(sAuditQuery, new TestGroupTestParameter()),Arrays.asList("sdisplaystatus"), objUserInfo.getSlanguagefilename()),new TypeReference<List<TestGroupTestParameter>>() {});

		sAuditQuery	= "select * from testgrouptestformula where ntestgrouptestcode in (" + stestgrouptestcode + ")"
						+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode ="	+ objUserInfo.getNmastersitecode()
						+ ";";
		final List<TestGroupTestFormula> lstTestGroupTestFormula  =jdbcTemplate.query(sAuditQuery, new TestGroupTestFormula());

		sAuditQuery	= "select * from testgrouptestfile where ntestgrouptestcode in (" + stestgrouptestcode + ")"
					+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode ="	+ objUserInfo.getNmastersitecode()
					+ ";";;
		final List<TestGroupTestFile> lstTestGroupTestFile  =jdbcTemplate.query(sAuditQuery, new TestGroupTestFile());

		sAuditQuery	= "select * from testgrouptestpredefparameter tgtpp,testgrouptestparameter tgtp "
					+ " where tgtp.ntestgrouptestparametercode = tgtpp.ntestgrouptestparametercode "
					+ " and tgtp.ntestgrouptestcode in ("+ stestgrouptestcode + ")"
					+ " and tgtpp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tgtpp.nsitecode ="	+ objUserInfo.getNmastersitecode()
					+ " and tgtp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tgtp.nsitecode ="	+ objUserInfo.getNmastersitecode()
						+ ";";;
		final List<TestGroupTestPredefinedParameter> lstTestGroupTestPredefinedParameter  =jdbcTemplate.query(sAuditQuery, new TestGroupTestPredefinedParameter());

		final List<Object>lstnewobject =new ArrayList<>();
		
		lstnewobject.add(lstTestGroupTest);
		lstnewobject.add(lstTestGroupTestParameter);
		lstnewobject.add(lstTestGroupTestFormula);
		lstnewobject.add(lstTestGroupTestFile);
		lstnewobject.add(lstTestGroupTestPredefinedParameter);

		final List<String> lstAction = new ArrayList<String>();
		lstAction.add("IDS_ADDTEST");
		lstAction.add("IDS_ADDTESTPARAMETER");
		lstAction.add("IDS_ADDTESTFORMULA");
		lstAction.add("IDS_ADDTESTFILE");
		lstAction.add("IDS_ADDPREDEFINEDPARAMETER");
		//lstAction.add("IDS_ADDCHARACTERRESULT");
		lstAction.add("IDS_ADDSPECLIMIT");
	
		final UserInfo userInfo = new UserInfo(objUserInfo);
		userInfo.setNformcode((short) Enumeration.FormCode.TESTGROUP.getFormCode());
		userInfo.setSformname(commonFunction.getMultilingualMessage("IDS_STUDYPLAN",
				userInfo.getSlanguagefilename()));
		userInfo.setSmodulename(commonFunction.getMultilingualMessage("IDS_TESTGROUPMANAGEMENT",
				userInfo.getSlanguagefilename()));	
		
		auditUtilityFunction.fnInsertListAuditAction(lstnewobject, 1, null, lstAction, userInfo);
		
		return null;
	}
	
	//ALPD-3615--Start
	public ResponseEntity<Object> getAdhocTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final Boolean needSubSample = (Boolean) inputMap.get("nneedsubsample");

		final String sQuery = "select tm.ntestcode,tm.stestsynonym as stestsynonym,tm.stestname, " 
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() +" as nisvisible " +
							" from testmaster tm where not exists ("
							+ " select 1 from testgrouptest tgt,testgroupspecsampletype tgst " 
							+ " where tgst.nspecsampletypecode = tgt.nspecsampletypecode "
							+ " and tgst.nallottedspeccode = "+inputMap.get("nallottedspeccode")
							+ " and tgt.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgt.nsitecode = "+userInfo.getNmastersitecode()
							+ " and tgst.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgst.nsitecode = "+userInfo.getNmastersitecode()
							+ " and tm.ntransactionstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tgt.nisvisible = "+Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " and tm.ntestcode = tgt.ntestcode group by tgt.ntestcode) "
							+ " and tm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and tm.nsitecode = "+userInfo.getNmastersitecode()
							+" and tm.ntestcode > 0 order by ntestcode"; 
		final List<TestGroupTest> lstAdhocTest=jdbcTemplate.query(sQuery, new TestGroupTest());
		outputMap.put("AdhocTest", lstAdhocTest);
		
		if(!needSubSample) {
			if (inputMap.containsKey("getSampleChildDetail") && (Boolean) inputMap.get("getSampleChildDetail") == true) {
				final Map<String, Object> getMapData = new HashMap<String, Object>();
	
				if (!needSubSample) {
					getMapData.put("ntype", 2);
				}
	
				getMapData.put("nflag", 2);
				getMapData.put("nsampletypecode", inputMap.get("nsampletypecode"));
				getMapData.put("nregtypecode", inputMap.get("nregtypecode"));
				getMapData.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				getMapData.put("npreregno", Integer.toString((Integer) (inputMap.get("npreregno"))));
				getMapData.put("ntransactionstatus", inputMap.get("nfilterstatus"));
				getMapData.put("napprovalconfigcode", inputMap.get("napprovalconfigcode"));
				getMapData.put("activeTestTab", inputMap.get("activeTestTab"));
				getMapData.put("activeSampleTab", inputMap.get("activeSampleTab"));
				getMapData.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));
				getMapData.put("nneedsubsample", inputMap.get("nneedsubsample"));
				getMapData.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				getMapData.put("checkBoxOperation", inputMap.get("checkBoxOperation"));
				getMapData.put("userinfo", userInfo);
	
				final Map<String, Object> childDetailMap = new HashMap<String, Object>();

				childDetailMap.putAll((Map<String, Object>) getRegistrationSubSample(getMapData, userInfo).getBody());
				outputMap.put("SampleChildDetail", childDetailMap);
			}
		}else {
			if (inputMap.containsKey("getSubSampleChildDetail")
					&& (Boolean) inputMap.get("getSubSampleChildDetail") == true) {
				final Map<String, Object> getMapData = new HashMap<String, Object>();
				getMapData.put("nsampletypecode", inputMap.get("nsampletypecode"));
				getMapData.put("nregtypecode", inputMap.get("nregtypecode"));
				getMapData.put("nregsubtypecode", inputMap.get("nregsubtypecode"));
				getMapData.put("npreregno", Integer.toString((Integer) (inputMap.get("npreregno"))));
				getMapData.put("ntransactionstatus", inputMap.get("nfilterstatus"));
				getMapData.put("napprovalconfigcode", inputMap.get("napprovalconfigcode"));
				getMapData.put("activeTestTab", inputMap.get("activeTestTab"));
				getMapData.put("activeSampleTab", inputMap.get("activeSampleTab"));
				getMapData.put("activeSubSampleTab", inputMap.get("activeSubSampleTab"));
				getMapData.put("nneedsubsample", inputMap.get("nneedsubsample"));
				getMapData.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				getMapData.put("checkBoxOperation", inputMap.get("checkBoxOperation"));
				getMapData.put("userinfo", userInfo);
				getMapData.put("ntransactionsamplecode",
						Integer.toString((Integer) inputMap.get("ntransactionsamplecode")));

				final Map<String, Object> childDetailMap = new HashMap<String, Object>();

				childDetailMap.putAll((Map<String, Object>) getRegistrationTest(getMapData, userInfo).getBody());
				outputMap.put("SubSampleChildDetail", childDetailMap);
			}
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getCreateTestGroupTestSeqNo(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		StringBuilder sb = new StringBuilder();
		List<TestGroupTest> filteredList = new ArrayList<TestGroupTest>();
	
		final List<TestGroupSpecSampleType> listTestGroupSpecSampleType = objMapper.convertValue(inputMap.get("nspecsampletypecode"),
				new TypeReference<List<TestGroupSpecSampleType>>() {});
      
		if(!listTest.isEmpty()) {
			filteredList = listTest;		
	
			String stestcode = stringUtilityFunction.fnDynamicListToString(filteredList, "getNtestcode");
			if (stestcode.isEmpty()) {
				stestcode = "0";
			}
			sb.append("select nparametertypecode from testparameter where ntestcode in (" + stestcode
					+ ") and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and nsitecode = "+userInfo.getNmastersitecode()
					+ ";");
			sb.append("select count(ntestfilecode) as ntestfilecode from testfile where ndefaultstatus= "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() 
					+ " and nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and nsitecode = "+userInfo.getNmastersitecode()
					+ " and ntestcode in(" + stestcode+ ");");
			sb.append("select count(ntestformulacode) as ntestformulacode from testformula where "
					+ " nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and nsitecode = "+userInfo.getNmastersitecode()
					+ " and ntestcode in ("+ stestcode + ");");

			String StrQuery ="select nparametertypecode from testparameter where ntestcode in ("+ stestcode+") "
							+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode = "+userInfo.getNmastersitecode()+ "";
			final List<TestParameter> lstTestParameter =jdbcTemplate.query(StrQuery, new TestParameter());
	
			StrQuery ="select ntestfilecode from testfile where ntestcode in ("+ stestcode+") "
					+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = "+userInfo.getNmastersitecode()
					+ "";
			final List<TestFile> lstTestFile =jdbcTemplate.query(StrQuery, new TestFile());
	
			StrQuery ="select ntestformulacode,ndefaultstatus from testformula where ntestcode in ("+ stestcode+") "
					+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = "+userInfo.getNmastersitecode()
					+ "";
			final List<TestFormula> lstTestFormula =jdbcTemplate.query(StrQuery, new TestFormula());
			
			StrQuery ="select nsectioncode from testsection where ntestcode in ("+ stestcode+") "
					+ " and ndefaultstatus="+Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode = "+userInfo.getNmastersitecode()
					+ "";
			final List<TestSection> lstTestSection =jdbcTemplate.query(StrQuery, new TestSection());
		
			StrQuery ="select  tsc.*,tm.ntestcode from testmaster tm ,testparameter tp,testmasterclinicalspec tsc "
					+ " where tm.ntestcode=tp.ntestcode "
					+ " and tp.ntestparametercode=tsc.ntestparametercode "
					+ " and tm.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tp.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tsc.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tm.nsitecode = "+userInfo.getNmastersitecode()
					+ " and tp.nsitecode = "+userInfo.getNmastersitecode()
					+ " and tsc.nsitecode = "+userInfo.getNmastersitecode()
					+ " and tm.ntestcode in ("+ stestcode+") ";
			final List<TestMasterClinicalSpec> lstClinialSpec =jdbcTemplate.query(StrQuery, new TestMasterClinicalSpec());
		
			final Map<String, Object> mapLists =new HashMap<String, Object>();
			mapLists.put("TestParameter", lstTestParameter);
			mapLists.put("TestFile", lstTestFile);
			mapLists.put("TestFormula", lstTestFormula);
			mapLists.put("TestMasterClinicalSpec", lstClinialSpec);

			//final Map<String, Object> mapLists = findByMultiQueryPlainSql(sb.toString(), TestParameter.class,TestFile.class, TestFormula.class);
			final List<TestParameter> lstTP = (List<TestParameter>) mapLists
					.get(TestParameter.class.getSimpleName());
			final List<TestFile> lstTF = (List<TestFile>) mapLists.get(TestFile.class.getSimpleName());
			List<TestFormula> lstTForm = (List<TestFormula>) mapLists.get(TestFormula.class.getSimpleName());
	
			sb.delete(0, sb.length());
			sb.append("select count(tpp.ntestpredefinedcode) "
					+ "from testpredefinedparameter tpp,testparameter tp where tp.ntestparametercode = tpp.ntestparametercode "
					+ " and tpp.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tp.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tpp.nsitecode = "+userInfo.getNmastersitecode()
					+ " and tp.nsitecode = "+userInfo.getNmastersitecode()
					+ " and tp.nparametertypecode=" + Enumeration.ParameterType.PREDEFINED.getparametertype()
					+ " and tp.ntestcode in(" + stestcode + ");");
			
			final List<TestMasterClinicalSpec> lstParametrClinialSpec = (List<TestMasterClinicalSpec>) mapLists.get(TestMasterClinicalSpec.class.getSimpleName());
			outputMap.put("nclinicalspeccount",
						(lstParametrClinialSpec != null && !lstParametrClinialSpec.isEmpty()) ? lstParametrClinialSpec.size() : 0);
		
			outputMap.put("ntestcount",
				(filteredList != null && !filteredList.isEmpty()) ? filteredList.size() : 0);
	
			outputMap.put("nparametercount", (lstTP != null && !lstTP.isEmpty()) ? lstTP.size() : 0);
		
			outputMap.put("npredefparamcount", jdbcTemplate.queryForObject(sb.toString(), Integer.class));
		
		
			final int ncharparamcount = (int) lstTP.stream().filter(objTP -> objTP
				.getNparametertypecode() == Enumeration.ParameterType.CHARACTER.getparametertype()).count();
		
			final int nnumericcount = (int) lstTP.stream().filter(
				objTP -> objTP.getNparametertypecode() == Enumeration.ParameterType.NUMERIC.getparametertype())
				.count();
			outputMap.put("ncharparamcount", ncharparamcount);
			outputMap.put("nnumericcount", nnumericcount);
			outputMap.put("ntestfilecount",
					(lstTF != null && !lstTF.isEmpty()) ? lstTF.get(0).getNtestfilecode() : 0);
			//modified
			
			lstTForm=lstTForm.stream().filter(x->x.getNdefaultstatus()==Enumeration.TransactionStatus.YES.gettransactionstatus()).collect(Collectors.toList());
			outputMap.put("ntestFormcount",
					(lstTForm != null && !lstTForm.isEmpty()) ? lstTForm.size() : 0);
			
			if(!listTestGroupSpecSampleType.isEmpty()) {
				String sTestQry = "select * from testgrouptest where ntestcode="+listTest.get(0).getNtestcode()+ ""
								+ " and nspecsampletypecode="+listTestGroupSpecSampleType.get(0).getNspecsampletypecode()
								+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode = "+userInfo.getNmastersitecode()
								+"";
				final List<TestGroupTest> lstTestPresent = (List<TestGroupTest>) jdbcTemplate.query(sTestQry,new TestGroupTest());
				
				if(lstTestPresent.isEmpty()) {
					outputMap.putAll(testGroupTestDAO.updateCreateComponentTestSeqNo(true, outputMap));
					int ntgtseq = (int) outputMap.get("ntgtcount");
					listTest.get(0).setNtestgrouptestcode(ntgtseq+1); 
					listTest.get(0).setNparametercount((Integer) outputMap.get("nparametercount"));
					listTest.get(0).setNsectioncode(lstTestSection.get(0).getNsectioncode()); 
					outputMap.putAll(getCreateTestSequenceNo(userInfo, listSample, listTest,
															nregtypecode, nregsubtypecode, inputMap));
					//outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					//		Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		
				}else {
					listTest.get(0).setNtestgrouptestcode(lstTestPresent.get(0).getNtestgrouptestcode());
					listTest.get(0).setNparametercount((Integer) outputMap.get("nparametercount"));
					listTest.get(0).setNsectioncode(lstTestSection.get(0).getNsectioncode()); 
					outputMap.putAll(getCreateTestSequenceNo(userInfo, listSample, listTest,
							nregtypecode, nregsubtypecode, inputMap));
				}
			}
		}
		return outputMap;
	}
	
	public ResponseEntity<Object> copySample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		
		Map<String, Object> objMap = new HashMap<>();
		final String spreregno = inputMap.get("npreregno").toString();
		String sUpdateQuery = "";
		String sQuery = "";
		
		final int regCount = (int) inputMap.get("registrationCount");
		
		final String strSelectSeqno = "select stablename,nsequenceno  from seqnoregistration where stablename in ('registrationtest','registrationtesthistory','registrationparameter','registration'"
									+ " ,'registrationhistory','registrationdecisionhistory','registrationsample',"
									+ " 'registrationsamplehistory') "
									+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<SeqNoRegistration> mapList = (List<SeqNoRegistration>) jdbcTemplate.query(strSelectSeqno,
				new SeqNoRegistration());

		final Map<String, Integer> mapSeqno = mapList.stream().collect(Collectors
				.toMap(SeqNoRegistration::getStablename, SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

		final int nseqnoregistrationtest = mapSeqno.get("registrationtest");
		final int nseqnoregistrationtesthistory = mapSeqno.get("registrationtesthistory");
		final int nseqnoregistrationparameter = mapSeqno.get("registrationparameter");
		final int nseqnoregistration = mapSeqno.get("registration");
		final int nseqnoregistrationhistory = mapSeqno.get("registrationhistory");
		final int nseqnoregistrationdecisionhistory = mapSeqno.get("registrationdecisionhistory");
		final int nseqnoregistrationsample = mapSeqno.get("registrationsample");
		final int nseqnoregistrationsamplehistory = mapSeqno.get("registrationsamplehistory");

		sQuery = "select * from registrationsample where npreregno in (" + spreregno + ") "
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = "+userInfo.getNtranssitecode();
		final List<RegistrationSample> regSampleList = (List<RegistrationSample>) jdbcTemplate.query(sQuery,
				new RegistrationSample());

		sQuery = "select * from registrationtest where npreregno in (" + spreregno + ") "
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = "+userInfo.getNtranssitecode();
		final List<RegistrationTest> regTestList = (List<RegistrationTest>) jdbcTemplate.query(sQuery,
				new RegistrationTest());
		
		sQuery = "select * from registrationparameter where npreregno in (" + spreregno + ")"
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = "+userInfo.getNtranssitecode();;
		
		final List<RegistrationParameter> regparameterList = (List<RegistrationParameter>) jdbcTemplate.query(sQuery,
				new RegistrationParameter());

		sUpdateQuery = "update seqnoregistration set nsequenceno=" + (nseqnoregistration+regCount) + " where stablename='registration' ;";
		sUpdateQuery = sUpdateQuery + "update seqnoregistration set nsequenceno=" + (nseqnoregistrationhistory+regCount)
				+ " where stablename='registrationhistory' ;";
		sUpdateQuery = sUpdateQuery + "update seqnoregistration set nsequenceno=" + (nseqnoregistrationdecisionhistory+regCount)
				+ " where stablename='registrationdecisionhistory';";
		sUpdateQuery = sUpdateQuery + "update seqnoregistration set nsequenceno=" + (nseqnoregistrationsample+regSampleList.size())
				+ " where stablename='registrationsample' ;";
		sUpdateQuery = sUpdateQuery + "update seqnoregistration set nsequenceno=" + (nseqnoregistrationsamplehistory+regSampleList.size())
				+ " where stablename='registrationsamplehistory' ;";
		sUpdateQuery = sUpdateQuery + "update seqnoregistration set nsequenceno=" + (nseqnoregistrationtest+regTestList.size())
				+ " where stablename='registrationtest' ;";
		sUpdateQuery = sUpdateQuery + "update seqnoregistration set nsequenceno=" + (nseqnoregistrationtesthistory+regTestList.size())
				+ " where stablename='registrationtesthistory' ;";
		sUpdateQuery = sUpdateQuery + "update seqnoregistration set nsequenceno=" + (nseqnoregistrationparameter+regparameterList.size())
				+ " where stablename='registrationparameter';";

		jdbcTemplate.execute(sUpdateQuery);

		final String strRegistration = "insert into registration (npreregno, nsampletypecode, nregtypecode, nregsubtypecode,"
									+ " ndesigntemplatemappingcode, nregsubtypeversioncode, ntemplatemanipulationcode, nallottedspeccode,"
									+ " nproductcatcode, nproductcode, ninstrumentcatcode, ninstrumentcode, nisiqcmaterial, nmaterialcatcode,"
									+ " nmaterialcode, nprojectmastercode, jsondata, jsonuidata, nsitecode, nstatus)" + " select "
									+ nseqnoregistration
									+ "+rank()over(order by npreregno) as npreregno , nsampletypecode, nregtypecode, nregsubtypecode,"
									+ " ndesigntemplatemappingcode, nregsubtypeversioncode, ntemplatemanipulationcode, nallottedspeccode,"
									+ " nproductcatcode, nproductcode, ninstrumentcatcode, ninstrumentcode, nisiqcmaterial, nmaterialcatcode,"
									+ " nmaterialcode, nprojectmastercode, jsondata||json_build_object('npreregno'," + nseqnoregistration
									+ "+rank()over(order by npreregno))::jsonb, jsonuidata||json_build_object('npreregno',"
									+ nseqnoregistration + "+rank()over(order by npreregno))::jsonb, " + userInfo.getNtranssitecode()
									+ " as nsitecode, " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " as nstatus from registration where npreregno in (" + spreregno + ") and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
									+ userInfo.getNtranssitecode();

		jdbcTemplate.execute(strRegistration);

		final String strRegistrationArno = "insert into registrationarno (npreregno, napprovalversioncode, sarno, nsitecode, nstatus)"
										+ " select " + nseqnoregistration
										+ "+rank()over(order by npreregno) as npreregno , napprovalversioncode,'-' as sarno,"
										+ userInfo.getNtranssitecode() + " as nsitecode, "
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " as nstatus from registrationarno where npreregno in (" + spreregno + ") "
										+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
										+ " and nsitecode="+ userInfo.getNtranssitecode();

		jdbcTemplate.execute(strRegistrationArno);

		final String strRegistrationHistory = " insert into registrationhistory (nreghistorycode,npreregno,ntransactionstatus,dtransactiondate,nusercode,nuserrolecode "
											+ " ,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode) "
											+ " select " + nseqnoregistrationhistory + "+Rank() over (order by npreregno) as nreghistorycode, "
											+ nseqnoregistration + " +rank()over(order by npreregno) npreregno,"
											+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " as ntransactionstatus, '"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNusercode() + ","
											+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole()
											+ ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
											+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
											+ " from  registrationhistory where npreregno in ( " + spreregno + ") and ntransactionstatus ="
											+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " and nsitecode="
											+ userInfo.getNtranssitecode()+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
		jdbcTemplate.execute(strRegistrationHistory);

		final String strRegistrationFdescHistory = "Insert into registrationdecisionhistory(nregdecisionhistorycode, npreregno, ndecisionstatus, dtransactiondate, "
												+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, scomments, nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode)"
												+ " select " + nseqnoregistrationdecisionhistory + " +rank()over(order by npreregno),"
												+ nseqnoregistration + "+rank()over(order by npreregno) npreregno," + " ndecisionstatus,'"
												+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'as dtransactiondate," + userInfo.getNusercode() + ","
												+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole()
												+ ",scomments," + userInfo.getNtranssitecode() + ","
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
												+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
												+ " from  registrationdecisionhistory where npreregno in ( " + spreregno + ")  and nsitecode="
												+ userInfo.getNtranssitecode()+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;

		jdbcTemplate.execute(strRegistrationFdescHistory);

		final String strRegistrationStatusBlink = "Insert into registrationflagstatus (npreregno,bflag,nsitecode,nstatus) "
												+ " select " + nseqnoregistration + " +rank()over(order by npreregno) npreregno,bflag,"
												+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " from  registrationflagstatus where npreregno in ( " + spreregno + ")  and nsitecode="
												+ userInfo.getNtranssitecode()+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;

		jdbcTemplate.execute(strRegistrationStatusBlink);
		final String strRegistrationSample = "insert into registrationsample(ntransactionsamplecode,npreregno,nspecsampletypecode,ncomponentcode,"
											+ "jsondata,jsonuidata, nsitecode,nstatus)" + " select " + nseqnoregistrationsample
											+ "+Rank() over (order by ntransactionsamplecode)," + nseqnoregistration + " "
											+ "+dense_Rank()over(order by npreregno) npreregno,nspecsampletypecode,ncomponentcode,"
											+ " jsondata,jsonuidata||json_build_object('npreregno'," + nseqnoregistration
											+ "+dense_Rank()over(order by npreregno),'ntransactionsamplecode'," + " " + nseqnoregistrationsample
											+ "+rank()over(order by ntransactionsamplecode))::jsonb," + userInfo.getNtranssitecode() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " from  registrationsample where npreregno in ( " + spreregno + ")  and nsitecode="
											+ userInfo.getNtranssitecode() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;

		jdbcTemplate.execute(strRegistrationSample);

		final String strRegistrationSampleArno = "insert into registrationsamplearno (ntransactionsamplecode,npreregno, ssamplearno, nsitecode, nstatus)"
												+ " select " + nseqnoregistrationsample + "+rank()over(order by ntransactionsamplecode) ,"
												+ nseqnoregistration + "+dense_Rank()over(order by npreregno) as npreregno ,'-' as ssamplearno,"
												+ userInfo.getNtranssitecode() + " as nsitecode, "
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " as nstatus from registrationsamplearno where npreregno in (" + spreregno + ") and nstatus="
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
												+ userInfo.getNtranssitecode()+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;

		jdbcTemplate.execute(strRegistrationSampleArno);

		final String strRegistrationSampleHistoryArno = "insert into registrationsamplehistory (nsamplehistorycode,ntransactionsamplecode,npreregno,ntransactionstatus "
														+ ",dtransactiondate,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,scomments,nsitecode,nstatus,noffsetdtransactiondate,ntransdatetimezonecode) "
														+ " select " + nseqnoregistrationsamplehistory + "+Rank() over (order by nsamplehistorycode) , "
														+ nseqnoregistrationsample + "+dense_Rank() over (order by ntransactionsamplecode) ," + " "
														+ nseqnoregistration + "+dense_Rank()over(order by npreregno) as npreregno,"
														+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " as ntransactionstatus, '"
														+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' as dtransactiondate," + userInfo.getNusercode() + ","
														+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole()
														+ ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "'," + userInfo.getNtranssitecode() + ","
														+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
														+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
														+ " from registrationsamplehistory where npreregno in ( " + spreregno + ")  " + " and nsitecode="
														+ userInfo.getNtranssitecode() + "and  ntransactionstatus = "
														+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
		jdbcTemplate.execute(strRegistrationSampleHistoryArno);

		final String strRegistrationTest = "insert into registrationtest (ntransactiontestcode,ntransactionsamplecode,npreregno,"
										+ "ntestgrouptestcode,ninstrumentcatcode,nchecklistversioncode,ntestrepeatno,ntestretestno,jsondata,nsitecode, "
										+ " nstatus,ntestcode,nsectioncode,nmethodcode,dmodifieddate) " + "select " + nseqnoregistrationtest
										+ "+RANK() over(order by ntransactiontestcode)," + nseqnoregistrationsample
										+ "+dense_Rank() over (order by ntransactionsamplecode) as ntransactionsamplecode," + nseqnoregistration
										+ "+dense_Rank()over(order by npreregno) as npreregno, ntestgrouptestcode,ninstrumentcatcode,"
										+ "nchecklistversioncode,ntestrepeatno,ntestretestno,jsondata|| json_build_object('ntransactiontestcode', "
										+ nseqnoregistrationtest + "+RANK() over(order by ntransactiontestcode),'npreregno',"
										+ nseqnoregistration + "+dense_Rank() over(order by npreregno),'ntransactionsamplecode',"
										+ nseqnoregistrationsample + "+dense_Rank() over(order by ntransactionsamplecode))::jsonb,"
										+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " ,ntestcode,nsectioncode,nmethodcode,'"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"'  from registrationtest where npreregno in ( " + spreregno + ") "
										+ " and nsitecode=" + userInfo.getNtranssitecode()+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;

		jdbcTemplate.execute(strRegistrationTest);

		final String strRegistrationTesthistory = "Insert into registrationtesthistory (ntesthistorycode, ntransactiontestcode, "
													+ "ntransactionsamplecode, npreregno, nformcode, ntransactionstatus,"
													+ "	nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,scomments,"
													+ " dtransactiondate,nsampleapprovalhistorycode, nsitecode, nstatus,noffsetdtransactiondate,ntransdatetimezonecode) (select "
													+ nseqnoregistrationtesthistory + "+RANK() over(order by ntesthistorycode)," + nseqnoregistrationtest
													+ "+dense_Rank() over(order by ntransactiontestcode)," + nseqnoregistrationsample
													+ "+dense_Rank() over (order by ntransactionsamplecode) as ntransactionsamplecode," + nseqnoregistration
													+ "+dense_Rank() over (order by npreregno)," + userInfo.getNformcode() + ","
													+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + "," + userInfo.getNusercode() + ","
													+ userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole()
													+ ",'" + stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "','" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',-1,"
													+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
													+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
													+ " from registrationtesthistory " + " where npreregno in ( " + spreregno + ")  and nsitecode="
													+ userInfo.getNtranssitecode()+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  + " and ntransactionstatus="
													+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + " );";

		jdbcTemplate.execute(strRegistrationTesthistory);

		final String strRegistrationTestParmeter = "insert into registrationparameter (ntransactionresultcode,npreregno,ntransactiontestcode,"
													+ " ntestgrouptestparametercode,ntestparametercode,nparametertypecode,ntestgrouptestformulacode,nunitcode,"
													+ " nresultmandatory,nreportmandatory,jsondata,nsitecode,nstatus) " + "select "
													+ nseqnoregistrationparameter + "+RANK() over(order by ntransactionresultcode)," + nseqnoregistration
													+ "+dense_Rank() over(order by npreregno)," + nseqnoregistrationtest
													+ " +dense_Rank() over(order by ntransactiontestcode),"
													+ " ntestgrouptestparametercode,ntestparametercode,nparametertypecode,ntestgrouptestformulacode,"
													+ " nunitcode,nresultmandatory,nreportmandatory,jsondata||jsonb_build_object('ntransactionresultcode',"
													+ nseqnoregistrationparameter + "+RANK() over(order by ntransactionresultcode),"
													+ "'ntransactiontestcode'," + nseqnoregistrationtest
													+ " +dense_Rank() over(order by ntransactiontestcode),'npreregno'," + nseqnoregistration
													+ " +dense_Rank() over(order by npreregno) )," + userInfo.getNtranssitecode()+","
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from registrationparameter"
													+ " where npreregno in ( " + spreregno + ")  and nsitecode=" + userInfo.getNtranssitecode()
													+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		jdbcTemplate.execute(strRegistrationTestParmeter);
		
	    final String query=" select STRING_AGG(npreregno::text,',') as npreregno from "
				    		+ "(select "+nseqnoregistration+"+ RANK() OVER (ORDER BY npreregno) as"
				    		+ "  npreregno  from registration WHERE npreregno in ("+spreregno+")"
				    		+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				    		+ " and nsitecode="+userInfo.getNtranssitecode()
				    		+ ") as sub";
	    
	    final String spreregNoCode=jdbcTemplate.queryForObject(query, String.class);
	   
	    inputMap.put("npreregno", String.valueOf(spreregNoCode));
		
	    objMap = getDynamicRegistration(inputMap, userInfo);
		
	    objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}
	
	public Map<String, Object> getCreateTestSequenceNo(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			Map<String, Object> inputMap) throws Exception {

		Map<String, Object> outputMap = new HashMap<String, Object>();
		final String sntestGroupTestCode = stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode");

		final boolean needJobAllocation = (boolean) inputMap.get("nneedjoballocation");
		final boolean needMyJob = (boolean) inputMap.get("nneedmyjob");
		final Boolean skipMethodValidity = (Boolean) inputMap.get("skipmethodvalidity");

		final String inactiveTestQuery = " select tgt.stestsynonym, tm.ntestcode from testgrouptest tgt,"
										+ " testmaster tm where tgt.ntestgrouptestcode in (" + sntestGroupTestCode + ") "
										+ " and tgt.ntestcode=tm.ntestcode and tgt.nstatus=tm.nstatus and tm.nstatus ="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.ntransactionstatus= "
										+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus();

		final List<TestGroupTest> inactiveTestList = jdbcTemplate.query(inactiveTestQuery, new TestGroupTest());
		if (inactiveTestList.isEmpty()) {

			List<TestGroupTest> expiredMethodTestList = new ArrayList();
			if (skipMethodValidity == false) {
				expiredMethodTestList = transactionDAOSupport.getTestByExpiredMethod(sntestGroupTestCode, userInfo);

			}
			if (expiredMethodTestList.isEmpty()) {
				String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery);

				sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery);

				sQuery = " lock  table lockcancelreject " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery);

				final String sFindSubSampleQuery = "select npreregno, ntransactionstatus, ntransactionsamplecode from registrationsamplehistory"
												+ " where nsamplehistorycode = any (select max(nsamplehistorycode) from registrationsamplehistory where ntransactionsamplecode"
												+ " in (" + String.join(",", listSample) + ")  and nsitecode=" + userInfo.getNtranssitecode()
												+ " group by ntransactionsamplecode)" + " and ntransactionstatus not in ("
												+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", "
												+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")" + "  and nsitecode="
												+ userInfo.getNtranssitecode() + ";";

				List<RegistrationSampleHistory> listAvailableSample = jdbcTemplate.query(sFindSubSampleQuery,
						new RegistrationSampleHistory());

				if (!listAvailableSample.isEmpty()) {
					final String sFindSampleQuery = "select * from registrationhistory where nreghistorycode = "
							+ " any(select max(nreghistorycode) from registrationhistory rh,"
							+ " registrationsamplehistory rs  " + " where rs.ntransactionsamplecode in ("
							+ String.join(",", listSample) + ") and rh.npreregno=rs.npreregno and rs.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rh.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rh.nsitecode=rs.nsitecode and rs.nsitecode= " + userInfo.getNtranssitecode()
							+ " group by rh.npreregno,rs.ntransactionsamplecode) and ntransactionstatus "
							// + " not in("
							// + Enumeration.TransactionStatus.CERTIFIED.gettransactionstatus() + " , "
							// + Enumeration.TransactionStatus.SENT.gettransactionstatus() + ")"
							+ " != " + Enumeration.TransactionStatus.RELEASED.gettransactionstatus()
							+ "	and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode=" + userInfo.getNtranssitecode();

					List<RegistrationSampleHistory> releasedSampleList = jdbcTemplate.query(sFindSampleQuery,
							new RegistrationSampleHistory());
					if (!releasedSampleList.isEmpty()) {
						// Samples not yet released
						final String sApprovalStatusQry = "select apr.napprovalstatuscode ,acap.nneedautocomplete from approvalconfig ap,"
								+ " approvalconfigrole apr,approvalconfigversion apv,approvalconfigautoapproval acap"
								+ " where  ap.nregsubtypecode = " + nregsubtypecode + " and ap.nregtypecode = "
								+ nregtypecode
								+ " and ap.napprovalconfigcode = apv.napprovalconfigcode and acap.napprovalconfigversioncode=apv.napproveconfversioncode "
								+ " and apr.napproveconfversioncode = apv.napproveconfversioncode  "
								+ " and acap.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " " + " and apv.ntransactionstatus = "
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and ap.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and apr.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and apv.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " order by apr.nlevelno;";

						final List<ApprovalConfigRole> approvalConfigRole = jdbcTemplate.query(sApprovalStatusQry,
								new ApprovalConfigRole());
						int nApprovalStatus = -2;
						int nAutocomplete = Enumeration.TransactionStatus.NO.gettransactionstatus();
						if (!approvalConfigRole.isEmpty()) {
							nApprovalStatus = approvalConfigRole.get(0).getNapprovalstatuscode();
							nAutocomplete = approvalConfigRole.get(0).getNneedautocomplete();
						}
						boolean isApprovedSample = false;
						if (nApprovalStatus > 0) {
							// to get the samples that reached final level of approval
							final String sSampleQuery = "select npreregno, ntransactionstatus from registrationhistory "
									+ " where nreghistorycode = any (select max(nreghistorycode)"
									+ " from registrationhistory where nsitecode= " + userInfo.getNtranssitecode()
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and npreregno in (" + stringUtilityFunction.fnDynamicListToString(listAvailableSample, "getNpreregno")
									+ ") group by npreregno) and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ntransactionstatus in (" + nApprovalStatus + ") and npreregno in ("
									+ stringUtilityFunction.fnDynamicListToString(listAvailableSample, "getNpreregno") + ")"
									+ " and nsitecode=" + userInfo.getNtranssitecode() + ";";

							List<RegistrationHistory> listFinalAvailableSample = jdbcTemplate.query(sSampleQuery,
									new RegistrationHistory());
							// to filter samples from input that not yet reached final level of approval
							listAvailableSample = listAvailableSample.stream()
									.filter(source -> listFinalAvailableSample.stream()
											.noneMatch(dest -> source.getNpreregno() == dest.getNpreregno()))
									.collect(Collectors.toList());
							if (!listFinalAvailableSample.isEmpty()) {
								// some samples reached final level of appproval
								isApprovedSample = true;
							}
						}

						List<RegistrationSampleHistory> sampleWithoutPreregQuarantineStatus = listAvailableSample
								.stream()
								.filter(x -> x.getNtransactionstatus() != Enumeration.TransactionStatus.PREREGISTER
										.gettransactionstatus()
										&& x.getNtransactionstatus() != Enumeration.TransactionStatus.QUARENTINE
												.gettransactionstatus())
								.collect(Collectors.toList());

						String sValidationString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

						List<String> listPreregNo = new ArrayList<String>();
						List<Integer> removePreregNo = new ArrayList<Integer>();
						List<String> listValidationString = new ArrayList<String>();

						// commented by L.Subashini as we need to validate job allocation and my jobs
						// existence from
						// registration sub type configuration
						// String StringSiteScreen = "select * from siteconfigscreen where nformcode in
						// ("
						// + Enumeration.QualisForms.MyJob.getqualisforms() + ","
						// + Enumeration.QualisForms.JobAllocation.getqualisforms() + ")";
						// List<SiteConfigScreen> listSiteConfigScreen =
						// jdbcTemplate.query(StringSiteScreen,
						// new SiteConfigScreen());
						// boolean boolMyJobsScreen = listSiteConfigScreen.stream()
						// .allMatch(objsiteconfigscreen -> objsiteconfigscreen
						// .getNscreenrequired() ==
						// Enumeration.TransactionStatus.NO.gettransactionstatus());

						if (!sampleWithoutPreregQuarantineStatus.isEmpty()) {

							// Samples are available btw registered and final level of approval

							final String sApprovalConfigQuery = "select acap.*, ra.npreregno, ra.sarno from registrationarno ra, approvalconfigautoapproval acap"
									+ " where acap.napprovalconfigversioncode = ra.napprovalversioncode and acap.nstatus = ra.nstatus"
									+ " and ra.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ra.npreregno in ("
									+ stringUtilityFunction.fnDynamicListToString(sampleWithoutPreregQuarantineStatus, "getNpreregno") + ")";
							final List<ApprovalConfigAutoapproval> approvalConfigAutoApprovals = jdbcTemplate
									.query(sApprovalConfigQuery, new ApprovalConfigAutoapproval());

							for (ApprovalConfigAutoapproval objApprovalConfigAutoapproval : approvalConfigAutoApprovals) {
								if (objApprovalConfigAutoapproval
										.getNneedautocomplete() == Enumeration.TransactionStatus.YES
												.gettransactionstatus()
										&& objApprovalConfigAutoapproval
												.getNneedautoapproval() == Enumeration.TransactionStatus.NO
														.gettransactionstatus()) {

									// sample needs only auto complete and but not auto approval option
									sValidationString = transactionDAOSupport.validateTestAutoComplete(listSample, listTest, userInfo);

									if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sValidationString)) {
										listPreregNo.add(String.valueOf(objApprovalConfigAutoapproval.getNpreregno()));
									} else {
										listValidationString.add(objApprovalConfigAutoapproval.getSarno());
										removePreregNo.add(objApprovalConfigAutoapproval.getNpreregno());
									}
								} else {
									listPreregNo.add(String.valueOf(objApprovalConfigAutoapproval.getNpreregno()));
								}
								// // else if (objApprovalConfigAutoapproval.getNneedautoapproval() ==
								// // Enumeration.TransactionStatus.NO
								// // .gettransactionstatus()
								// // && objApprovalConfigAutoapproval.getNneedautocomplete() ==
								// // Enumeration.TransactionStatus.NO
								// // .gettransactionstatus()) {
								// //
								// listPreregNo.add(String.valueOf(objApprovalConfigAutoapproval.getNpreregno()));
								// // }
								// else if (!needJobAllocation || (needJobAllocation &&
								// objApprovalConfigAutoapproval.getNautoallot()
								// == Enumeration.TransactionStatus.YES.gettransactionstatus()))
								//// (boolMyJobsScreen && objApprovalConfigAutoapproval.getNneedjoballocation()
								// == Enumeration.TransactionStatus.NO.gettransactionstatus()
								//// || (objApprovalConfigAutoapproval.getNneedjoballocation() ==
								// Enumeration.TransactionStatus.YES.gettransactionstatus()
								//// && objApprovalConfigAutoapproval.getNautoallot() ==
								// Enumeration.TransactionStatus.YES.gettransactionstatus()))
								// {
								// //1.Job allocation screen not provided and approval configuration also doesnt
								// have Job allocation enabled (or)
								// //2. Job allocation enabled in approval configuration with auto job allot
								// option
								// listPreregNo.add(String.valueOf(objApprovalConfigAutoapproval.getNpreregno()));
								// }
								// else if (//objApprovalConfigAutoapproval.getNneedjoballocation() ==
								// Enumeration.TransactionStatus.YES.gettransactionstatus()
								// needJobAllocation
								// && objApprovalConfigAutoapproval.getNautoallot() ==
								// Enumeration.TransactionStatus.NO.gettransactionstatus()) {
								// //Job allocation enabled in approval configuration without auto job allot
								// option
								// }
							}

							outputMap.put("approvalconfigautoapproval", approvalConfigAutoApprovals);
						} else {

						}

						if (!removePreregNo.isEmpty()) {
							listAvailableSample = listAvailableSample.stream()
									.filter(available -> removePreregNo.stream()
											.noneMatch(remove -> remove == available.getNpreregno()))
									.collect(Collectors.toList());
						}
						if (!listAvailableSample.isEmpty()) {

							final String sFindParameterQuery = "select ntestgrouptestparametercode, ntestgrouptestcode from testgrouptestparameter "
									+ " where ntestgrouptestcode in ("
									+ stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode") + ") and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
							
							final List<TestGroupTestParameter> listAvailableParameter = (List<TestGroupTestParameter>) 
										jdbcTemplate.query(sFindParameterQuery, new TestGroupTestParameter());

							final String sSampleQuery = "select npreregno, ntransactionstatus from registrationhistory where nreghistorycode = "
									+ " any (select max(nreghistorycode)" + " from registrationhistory where nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and npreregno in (" + stringUtilityFunction.fnDynamicListToString(listAvailableSample, "getNpreregno")
									+ ") and nsitecode = " + userInfo.getNtranssitecode() + " group by npreregno)"
									+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ntransactionstatus not in ("
									+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + "," + " "
									+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")"
									+ " and npreregno in (" + stringUtilityFunction.fnDynamicListToString(listAvailableSample, "getNpreregno")
									+ ")" + " and nsitecode = " + userInfo.getNtranssitecode() + ";";
							
							final List<RegistrationHistory> registrationHistory = (List<RegistrationHistory>)
									jdbcTemplate.query(sSampleQuery, new RegistrationHistory());

							final String llinterQuery = "select tgt.ntestgrouptestcode from testgrouptest tgt, instrumentcategory ic "
									+ " where ic.ninstrumentcatcode = tgt.ninstrumentcatcode" + " and tgt.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgt.ntestgrouptestcode in ("
									+ stringUtilityFunction.fnDynamicListToString(listTest, "getNtestgrouptestcode") + ")"
									// + " and ic.ninterfacetypecode = " +
									// Enumeration.InterFaceType.LOGILAB.getInterFaceType()
									+ " and tgt.ninstrumentcatcode > 0;";
							
							final List<TestGroupTest> testGroupTest = (List<TestGroupTest>)
									jdbcTemplate.query(llinterQuery, new TestGroupTest());

							// ALPD-2047
//							final List<String> lst = Arrays.asList("registrationtest",
//									"registrationtesthistory","registrationparameter","resultparametercomments",
//									"registrationsection", "registrationsectionhistory","registrationhistory" ,"joballocation",
//									"llinter","registrationdecisionhistory");
//							seqNoMaxUpdate(lst);

							final String sSeqNoQuery = "select * from seqnoregistration "
									// + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
									+ " where stablename in ('registrationtest', 'registrationtesthistory', 'registrationparameter',"
									+ "'joballocation', 'resultparametercomments', 'registrationhistory',"
									+ " 'registrationsection', 'registrationsectionhistory', 'llinter','externalordertest','registrationsamplehistory');";
							final List<SeqNoRegistration> lstSeqNo = (List<SeqNoRegistration>) 
									jdbcTemplate.query(sSeqNoQuery, new SeqNoRegistration());

//							Map<String, List<?>> mapList = (Map<String, List<?>>)projectDAOSupport.getMutlipleEntityResultsUsingPlainSql(
//									sFindParameterQuery + sSampleQuery + sSeqNoQuery + llinterQuery,jdbcTemplate,
//									TestGroupTestParameter.class, RegistrationHistory.class, SeqNoRegistration.class,
//									TestGroupTest.class);
//
//							final List<TestGroupTestParameter> listAvailableParameter = (List<TestGroupTestParameter>) mapList
//									.get("TestGroupTestParameter");
//							final List<RegistrationHistory> registrationHistory = (List<RegistrationHistory>) mapList
//									.get("RegistrationHistory");
//							final List<SeqNoRegistration> lstSeqNo = (List<SeqNoRegistration>) mapList
//									.get("SeqNoRegistration");
//							final List<TestGroupTest> testGroupTest = (List<TestGroupTest>) mapList
//									.get("TestGroupTest");

							List<RegistrationHistory> availableRegistrationHistory = registrationHistory.stream()
									.filter(source -> source
											.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
													.gettransactionstatus()
											// || (source.getNtransactionstatus() ==
											// Enumeration.TransactionStatus.CERTIFICATECORRECTION.gettransactionstatus())
											|| approvalConfigRole.stream()
													.anyMatch(check -> source.getNtransactionstatus() == check
															.getNapprovalstatuscode() && check.getNlevelno() != 1))
									.collect(Collectors.toList());

							outputMap
									.putAll(lstSeqNo.stream().collect(Collectors.toMap(SeqNoRegistration::getStablename,
											SeqNoRegistration -> SeqNoRegistration.getNsequenceno())));

							int testCount = listTest.stream()
									.mapToInt(testgrouptest -> testgrouptest.getNrepeatcountno() == 0 ? 1
											: testgrouptest.getNrepeatcountno())
									.sum();

							int parameterCount = listTest.stream()
									.mapToInt(testgrouptest -> ((testgrouptest.getNrepeatcountno() == 0 ? 1
											: testgrouptest.getNrepeatcountno()) * testgrouptest.getNparametercount()))
									.sum();

							String sUpdateSeqNoQry = "";
							int nsampletypecode = (int) inputMap.get("nsampletypecode");
							int orderType = Enumeration.OrderType.NA.getOrderType();
							String queryString = "select * from registration where npreregno in (" + inputMap.get("npreregno")+ ")"
												+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
												+ " and nsitecode = " + userInfo.getNtranssitecode();
							List<Registration> lstReg = jdbcTemplate.query(queryString, new Registration());

							if (nsampletypecode == Enumeration.SampleType.CLINICALSPEC.getType()) {
								if (!lstReg.isEmpty()) {
									if (lstReg.get(0).getJsondata().containsKey("Order Type")) {
										
										Registration reg = lstReg.get(0);
										JSONObject json = new JSONObject(reg.getJsondata());
										orderType = (int) ((JSONObject) json.get("Order Type")).getInt("value");
										
										if (orderType == Enumeration.OrderType.INTERNAL.getOrderType() 
												|| orderType == Enumeration.OrderType.NA.getOrderType()) {
											sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
													+ ((int) outputMap.get("externalordertest")// +
																								// (listAvailableSample.size()
																								// *
																								// listTest.size()))
															+ (listAvailableSample.size() * testCount))
													+ " where stablename = 'externalordertest';";
											jdbcTemplate.execute(sUpdateSeqNoQry);
										}

									}
								}
							}
							outputMap.put("ordertypecode", orderType);

							sUpdateSeqNoQry = "update seqnoregistration" + " set nsequenceno = "
									+ ((int) outputMap.get("registrationparameter")
											// + (listAvailableSample.size() * listAvailableParameter.size()))
											+ (listAvailableSample.size() * parameterCount))
									+ " where stablename = 'registrationparameter';";

							jdbcTemplate.execute(sUpdateSeqNoQry);

							sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
									+ ((int) outputMap.get("registrationtest")// + (listAvailableSample.size() *
																				// listTest.size()))
											+ (listAvailableSample.size() * testCount))
									+ " where stablename = 'registrationtest';";
							jdbcTemplate.execute(sUpdateSeqNoQry);

							final String transactionsamplecode = listAvailableSample.stream()
									.map(item -> String.valueOf(item.getNtransactionsamplecode()))
									.collect(Collectors.joining(","));

							String str = "select * from registrationsamplehistory where nsamplehistorycode in( "
									+ " select max(nsamplehistorycode) from registrationsamplehistory group by ntransactionsamplecode) "
									+ " and ntransactionstatus not in ( "
									+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.REGISTERED.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.PARTIALLY.gettransactionstatus()
									+ ") and ntransactionsamplecode in(" + transactionsamplecode + ")";
							List<RegistrationSampleHistory> lstsubsample = jdbcTemplate.query(str,
									new RegistrationSampleHistory());

							if (lstsubsample.size() > 0) {
								// final String
								// ntransactionsamplecount=transactionsamplecode+lstsubsample.size();
								sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
										+ +((int) outputMap.get("registrationsamplehistory") + (lstsubsample.size()))
										+ " where stablename = 'registrationsamplehistory';";
								jdbcTemplate.execute(sUpdateSeqNoQry);
							}

							if (nAutocomplete == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

								sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
										+ ((int) outputMap.get("registrationtesthistory")
												// + (listAvailableSample.size() * listTest.size() * 2))
												+ (listAvailableSample.size() * testCount * 2))
										+ " where stablename = 'registrationtesthistory';";
								jdbcTemplate.execute(sUpdateSeqNoQry);
							} else {
								sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
										+ ((int) outputMap.get("registrationtesthistory")
												// + (listAvailableSample.size() * listTest.size()))
												+ (listAvailableSample.size() * testCount))
										+ " where stablename = 'registrationtesthistory';";
								jdbcTemplate.execute(sUpdateSeqNoQry);
							}
							if (!sampleWithoutPreregQuarantineStatus.isEmpty()) {
								 String sScheduleSkip = transactionDAOSupport.scheduleSkip();
								 if ((sScheduleSkip.equals(String.valueOf(Enumeration.TransactionStatus.YES.gettransactionstatus()))&& !needMyJob
										 && needJobAllocation) ||(!listPreregNo.isEmpty() && !needJobAllocation)) {
								//if (!listPreregNo.isEmpty() && !needJobAllocation) {
									sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
											+ ((int) outputMap.get("joballocation") // + (listPreregNo.size() *
																					// listTest.size()))
													+ (listPreregNo.size() * testCount))
											+ " where stablename = 'joballocation';";
									jdbcTemplate.execute(sUpdateSeqNoQry);
								}

								sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
										+ (int) outputMap.get("registrationparameter")
										// + (sampleWithoutPreregQuarantineStatus.size() *
										// listAvailableParameter.size())
										+ (sampleWithoutPreregQuarantineStatus.size() * parameterCount)
										+ " where stablename = 'resultparametercomments';";
								jdbcTemplate.execute(sUpdateSeqNoQry);

								if (!availableRegistrationHistory.isEmpty()) {
									sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
											+ ((int) outputMap.get("registrationhistory")
													+ availableRegistrationHistory.size())
											+ " where stablename = 'registrationhistory';";
									jdbcTemplate.execute(sUpdateSeqNoQry);
								}

								final List<TestGroupTestParameter> llinterParameterList = listAvailableParameter
										.stream()
										.filter(source -> testGroupTest.stream().anyMatch(
												dest -> source.getNtestgrouptestcode() == dest.getNtestgrouptestcode()))
										.collect(Collectors.toList());
								if (testGroupTest.size() > 0) {
									//ALPD-5905
									sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
											+ ((int) outputMap.get("llinter") + llinterParameterList.size())
											+ " where stablename = 'llinter';";
									jdbcTemplate.execute(sUpdateSeqNoQry);

									outputMap.put("llinterParameterList", llinterParameterList.size());
								}

								if (!listPreregNo.isEmpty()) {
									Set<String> listSectionCode = listTest.stream()
											.map(obj -> String.valueOf(obj.getNsectioncode()))
											.collect(Collectors.toSet());

										final String ssectionCountQuery = " select npreregno, case when count(nsectioncode) = "
																		+ listSectionCode.size() + " then 0 else " + listSectionCode.size()
																		+ " - count(nsectioncode) end nsectioncode "
																		+ " from registrationsection rs where npreregno in ("
																		+ String.join(",", listPreregNo) + ")" + " and nsectioncode in("
																		+ String.join(",", listSectionCode) + ")" + " and nsitecode = "
																		+ userInfo.getNtranssitecode() + " and nstatus= "
																		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
																		+ " group by npreregno";

									final List<RegistrationSection> preregSectionList = jdbcTemplate
											.query(ssectionCountQuery, new RegistrationSection());

									// Sum of new sections count.
									final int sectionPreregCount = preregSectionList.stream()
											.mapToInt(regSection -> regSection.getNsectioncode()).sum();

									// Sample count for which the new sections are not available
									long unavailPreregSectionCount = listPreregNo.stream()
											.filter(item -> preregSectionList.stream()
													.noneMatch(item1 -> item1.getNpreregno() == Integer.parseInt(item)))
											.count();

									// Sum of both the above counts to be updated in sequence table for
									// registrationsection and its history.
									int prergCountToAdd = sectionPreregCount + (int) unavailPreregSectionCount;
									if (prergCountToAdd > 0) {
										sUpdateSeqNoQry = "update seqnoregistration set nsequenceno = "
												+ (prergCountToAdd + (int) outputMap.get("registrationsection"))
												+ " where stablename = 'registrationsection';";

										sUpdateSeqNoQry = sUpdateSeqNoQry
												+ ";update seqnoregistration set nsequenceno = "
												+ (prergCountToAdd + (int) outputMap.get("registrationsectionhistory"))
												+ " where stablename = 'registrationsectionhistory';";
										jdbcTemplate.execute(sUpdateSeqNoQry);
									}
								}
							}

							outputMap.put("ApprovalConfigRole", approvalConfigRole);
							outputMap.put("RegistrationHistory", registrationHistory);
							outputMap.put("parametercount", listAvailableParameter.size());
							outputMap.put("AvailableSample", listAvailableSample);
							// outputMap.put("boolMyJobsScreen", boolMyJobsScreen);
							outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
						} else {
							if (isApprovedSample) {
								outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										commonFunction.getMultilingualMessage("IDS_SAMPLESAREINFINALLEVELAPPROVE",
												userInfo.getSlanguagefilename()));
							} else {
								outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
										commonFunction.getMultilingualMessage("IDS_DEFAULTRESULTISNOTAVAILABLEFOR",
												userInfo.getSlanguagefilename()) + " "
												+ String.join(",", listValidationString));
							}
						}

					} else {
						outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
								commonFunction.getMultilingualMessage("IDS_CANNOTADDTESTFORRELEASEDSAMPLE",
										userInfo.getSlanguagefilename()));
					}
				} else {
					outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							commonFunction.getMultilingualMessage("IDS_SAMPLEISREJECTEDORCANCELLED",
									userInfo.getSlanguagefilename()));
				}
			} else {
				final String expiredMethod = stringUtilityFunction.fnDynamicListToString(expiredMethodTestList, "getSmethodname");
				// outputMap.put(key, value)
				outputMap.put("NeedConfirmAlert", true);
				final String message = commonFunction.getMultilingualMessage("IDS_TESTWITHEXPIREDMETHODCONFIRM",
						userInfo.getSlanguagefilename());
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						message.concat(" " + expiredMethod + " ?"));
				// commonFunction.getMultilingualMessage("IDS_INACTIVETESTCANNOTBEREGISTERED",
				// userInfo.getSlanguagefilename()));
			}
		} else {
			// final String inactiveTest = fndynamiclisttostring(inactiveTestList,
			// "getNtestgrouptestcode");
			outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), // inactiveTest);
					commonFunction.getMultilingualMessage("IDS_INACTIVETESTCANNOTBEREGISTERED",
							userInfo.getSlanguagefilename()));
		}
		return outputMap;
	}
	
	//ALPD-4914-To get previously saved filter details when click the filter name,done by Dhanushya RI
	public ResponseEntity<Object> getRegistrationFilter(final Map<String, Object> uiMap,final UserInfo userInfo) throws Exception {
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userinfo = objMapper.convertValue(uiMap.get("userinfo"), UserInfo.class);

		int ndesigntemplatemappingcode = -1;
		int nsampletypecode=-1;
		int nregtype=-1;
		int nregsubtype=-1;
		
		List<RegistrationType> lstRegistrationType = new ArrayList<RegistrationType>();
		List<RegistrationSubType> lstRegistrationSubType = new ArrayList<RegistrationSubType>();
		List<ReactRegistrationTemplate> lstReactRegistrationTemplate = new ArrayList<ReactRegistrationTemplate>();
		List<ApprovalConfigAutoapproval> listApprovalConfigAutoapproval = new ArrayList<ApprovalConfigAutoapproval>();
		List<TransactionStatus> listTransactionstatus = new ArrayList<TransactionStatus>();
		
		
		final String strQuery="select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+" "
				        		+ " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				        		+ " and nfilternamecode="+uiMap.get("nfilternamecode");
				    	
		final String strFilter = jdbcTemplate.queryForObject(strQuery, String.class);
    	
		final List<Map<String, Object>> listFilterNameCode = strFilter != null ? objMapper.readValue(strFilter, new TypeReference<List<Map<String, Object>>>() {
			}):new ArrayList<Map<String, Object>>();
		 
			if (!listFilterNameCode.isEmpty()) {
				final String strValidationQuery="select json_agg(jsondata || jsontempdata) as jsondata from filtername where nformcode="+userInfo.getNformcode()+" and nusercode="+userInfo.getNusercode()+" "
								        		+ " and nuserrolecode="+userInfo.getNuserrole()+" and nsitecode="+userInfo.getNtranssitecode()+" "
								        		+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								        		+ " and (jsondata->'nsampletypecode')::int="+uiMap.get("nsampletypecode")+" "  
								        		+ " and (jsondata->'nregtypecode')::int="+uiMap.get("nregtypecode")+" "  
								        		+ " and (jsondata->'nregsubtypecode')::int="+uiMap.get("nregsubtypecode")+" "  
								        		+ " and (jsontempdata->'ntranscode')::int="+uiMap.get("ntranscode")+" "  
								        		+ " and (jsontempdata->'napproveconfversioncode')::int="+uiMap.get("napproveconfversioncode")+" "
								    	        + " and (jsontempdata->'ndesigntemplatemappingcode')::int="+uiMap.get("ndesigntemplatemappingcode")+" "
								    	        + " and (jsontempdata->>'DbFromDate')='"+uiMap.get("FromDate")+"' "
								    	        + " and (jsontempdata->>'DbToDate')='"+uiMap.get("ToDate")+"' and nfilternamecode="+uiMap.get("nfilternamecode")+"" ; 
					
			    	
				final String strValidationFilter = jdbcTemplate.queryForObject(strValidationQuery, String.class);
			    	
				final List<Map<String, Object>> lstValidationFilter = strValidationFilter != null ? objMapper.readValue(strValidationFilter, new TypeReference<List<Map<String, Object>>>() {
						}):new ArrayList<Map<String, Object>>();
			    if(lstValidationFilter.isEmpty()) {
				projectDAOSupport.updateFilterDetail(uiMap,userInfo);
				
				final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userinfo.getSdatetimeformat());

				String fromDate = LocalDateTime.parse((String) listFilterNameCode.get(0).get("FromDate"), dbPattern).format(uiPattern);
				String toDate = LocalDateTime.parse((String) listFilterNameCode.get(0).get("ToDate"), dbPattern).format(uiPattern);

				returnMap.put("RealFromDate", fromDate);
				returnMap.put("RealToDate", toDate);

				fromDate = dateUtilityFunction.instantDateToString(
						dateUtilityFunction.convertStringDateToUTC((String) listFilterNameCode.get(0).get("FromDate"), userinfo, true));
				toDate = dateUtilityFunction.instantDateToString(
						dateUtilityFunction.convertStringDateToUTC((String) listFilterNameCode.get(0).get("ToDate"), userinfo, true));
				
				uiMap.put("FromDate", fromDate);
				uiMap.put("ToDate", toDate);
				 
				final List<SampleType> lstSampleType = transactionDAOSupport.getSampleType(userInfo);
				if (!lstSampleType.isEmpty()) {	

					final SampleType filterSampleType=!listFilterNameCode.isEmpty()?objMapper.convertValue(listFilterNameCode.get(0).get("sampleTypeValue"),SampleType.class):lstSampleType.get(0);
				
					nsampletypecode = filterSampleType.getNsampletypecode();

					returnMap.put("SampleTypeValue", filterSampleType);
					returnMap.put("RealSampleTypeValue", filterSampleType);
					returnMap.put("SampleType", lstSampleType);
					returnMap.put("RealSampleTypeList", lstSampleType);

					lstRegistrationType = transactionDAOSupport.getRegistrationType(nsampletypecode, userInfo);
					
					final RegistrationType filterRegType=!listFilterNameCode.isEmpty()? objMapper.convertValue(listFilterNameCode.get(0).get("regTypeValue"),RegistrationType.class):lstRegistrationType.get(0);
					 nregtype = filterRegType.getNregtypecode();
					
					returnMap.put("RealRegTypeList", lstRegistrationType);
					returnMap.put("RegistrationType", lstRegistrationType);
					returnMap.put("RegTypeValue",filterRegType);
					returnMap.put("RealRegTypeValue",filterRegType);

					lstRegistrationSubType = transactionDAOSupport.getRegistrationSubType(nregtype, userInfo);
					
					final RegistrationSubType filterRegSubType=!listFilterNameCode.isEmpty()? objMapper.convertValue(listFilterNameCode.get(0).get("regSubTypeValue"),RegistrationSubType.class):lstRegistrationSubType.get(0);	
					
					returnMap.put("RegistrationSubType", lstRegistrationSubType);
					returnMap.put("RealRegSubTypeList", lstRegistrationSubType);
					returnMap.put("nregsubtypeversioncode", filterRegSubType.getNregsubtypeversioncode());
					returnMap.put("RegSubTypeValue", filterRegSubType);
					returnMap.put("RealRegSubTypeValue", filterRegSubType);
	
					nregsubtype=filterRegSubType.getNregsubtypecode();
					
					listTransactionstatus = transactionDAOSupport.getFilterStatus(nregtype,nregsubtype, userInfo);
					
					final TransactionStatus filterTransactionStatus=!listFilterNameCode.isEmpty()? listFilterNameCode.get(0).containsKey("filterStatusValue")?objMapper.convertValue(listFilterNameCode.get(0).get("filterStatusValue"),TransactionStatus.class):listTransactionstatus.get(0):listTransactionstatus.get(0);
	
					returnMap.put("RealFilterStatuslist", listTransactionstatus);
					returnMap.put("FilterStatus", listTransactionstatus);
					returnMap.put("FilterStatusValue", filterTransactionStatus);
					returnMap.put("RealFilterStatusValue", filterTransactionStatus);
	
					listApprovalConfigAutoapproval = transactionDAOSupport.getApprovalConfigVersion(nregtype,nregsubtype,userInfo);
					
					final ApprovalConfigAutoapproval filterApprovalConfig=!listFilterNameCode.isEmpty()?listFilterNameCode.get(0).containsKey("approvalConfigValue")?
							objMapper.convertValue(listFilterNameCode.get(0).get("approvalConfigValue"),ApprovalConfigAutoapproval.class):listApprovalConfigAutoapproval.get(0):listApprovalConfigAutoapproval.get(0);	
	
					returnMap.put("ApprovalConfigVersion", listApprovalConfigAutoapproval);
					returnMap.put("RealApprovalConfigVersionList", listApprovalConfigAutoapproval);
					returnMap.put("ApprovalConfigVersionValue", filterApprovalConfig);
					returnMap.put("RealApprovalConfigVersionValue", filterApprovalConfig);
	
					returnMap.put("napproveconfversioncode", filterApprovalConfig.getNapproveconfversioncode());

					lstReactRegistrationTemplate = (List<ReactRegistrationTemplate>) transactionDAOSupport.getApproveConfigVersionRegTemplate(
							nregtype,nregsubtype,filterApprovalConfig.getNapproveconfversioncode()).getBody();
					
					final ReactRegistrationTemplate filterReactRegistrationTemplate=!listFilterNameCode.isEmpty()?listFilterNameCode.get(0).containsKey("designTemplateMappingValue")?
							objMapper.convertValue(listFilterNameCode.get(0).get("designTemplateMappingValue"),ReactRegistrationTemplate.class):lstReactRegistrationTemplate.get(0):lstReactRegistrationTemplate.get(0);		
	
					ndesigntemplatemappingcode=filterReactRegistrationTemplate.getNdesigntemplatemappingcode();
					returnMap.put("DesignTemplateMapping", lstReactRegistrationTemplate);
					returnMap.put("RealDesignTemplateMappingList", lstReactRegistrationTemplate);
	
					
					if (lstReactRegistrationTemplate.size() > 0) {
						returnMap.put("DesignTemplateMappingValue", filterReactRegistrationTemplate);
						returnMap.put("RealDesignTemplateMappingValue", filterReactRegistrationTemplate);
	
					}
					uiMap.put("npreregno", "");
					uiMap.put("nsampletypecode", nsampletypecode);
					uiMap.put("nregtypecode", nregtype);
					uiMap.put("nregsubtypecode", nregsubtype);
					uiMap.put("nfilterstatus", filterTransactionStatus.getNtransactionstatus());
					uiMap.put("nneedtemplatebasedflow", filterRegSubType.isNneedtemplatebasedflow());
					uiMap.put("napproveconfversioncode", filterApprovalConfig.getNapprovalconfigversioncode());
					uiMap.put("ndesigntemplatemappingcode", ndesigntemplatemappingcode);
					uiMap.put("nneedsubsample", filterRegSubType.isNneedsubsample());
					uiMap.put("checkBoxOperation", Enumeration.TransactionStatus.YES.gettransactionstatus());
					uiMap.put("noutsourcerequired", (int)filterSampleType.getNoutsourcerequired());			
				}				
				else {
					returnMap.put("SampleType", null);
					returnMap.put("RegistrationSubType", null);
					returnMap.put("RegSubTypeValue", null);
					returnMap.put("RegistrationType", null);
					returnMap.put("RegTypeValue",null);	
					returnMap.put("ApprovalConfigVersion", null);
					returnMap.put("ApprovalConfigVersionValue",null);
					returnMap.put("FilterStatus", null);
					returnMap.put("FilterStatusValue", null);
					returnMap.put("napproveconfversioncode", null);
					returnMap.put("DesignTemplateMapping", null);
					returnMap.put("DesignTemplateMappingValue", null);
					returnMap.put("RealSampleTypeValue", null);
					returnMap.put("RealSampleTypeList", null);
					returnMap.put("RealRegTypeList", null);
					returnMap.put("RealRegTypeValue", null);
					returnMap.put("RealRegSubTypeList",null);	
					returnMap.put("RealRegSubTypeValue", null);
					returnMap.put("RealFilterStatusValue",null);
					returnMap.put("RealFilterStatuslist", null);
					returnMap.put("RealApprovalConfigVersionList", null);
					returnMap.put("RealApprovalConfigVersionValue", null);
					returnMap.put("RealDesignTemplateMappingList", null);
					returnMap.put("RealDesignTemplateMappingValue", null);
					return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
				}
									
				final ReactRegistrationTemplate lstTemplate = (ReactRegistrationTemplate) transactionDAOSupport.getRegistrationTemplate(nregtype,
						nregsubtype).getBody();
				if (lstTemplate != null) {
					returnMap.put("ndesigntemplatemappingcode", lstTemplate.getNdesigntemplatemappingcode());
				}
	
				final ReactRegistrationTemplate lstTemplate1 = (ReactRegistrationTemplate) transactionDAOSupport.getRegistrationTemplatebasedontemplatecode(
						nregtype, nregsubtype, ndesigntemplatemappingcode).getBody();
				 
				if (lstTemplate1 != null) {
					returnMap.put("registrationTemplate", lstTemplate1);
					returnMap.put("SubSampleTemplate", transactionDAOSupport.getRegistrationSubSampleTemplate(ndesigntemplatemappingcode).getBody());
					returnMap.put("DynamicDesign", projectDAOSupport.getTemplateDesign(ndesigntemplatemappingcode, userinfo.getNformcode()));
					returnMap.putAll(getDynamicRegistration(uiMap, userinfo));
				} else {
					returnMap.put("registrationTemplate", new ArrayList<>());
					returnMap.put("SubSampleTemplate", new ArrayList<>());
					returnMap.put("DynamicDesign", null);
					returnMap.put("selectedSample", Arrays.asList());
					returnMap.put("selectedSubSample", Arrays.asList());
					returnMap.put("RegistrationGetSubSample", new ArrayList<>());
					returnMap.put("RegistrationGetSample", new ArrayList<>());
					returnMap.put("RegistrationGetTest", new ArrayList<>());
					returnMap.put("selectedTest", Arrays.asList());
					returnMap.put("RegistrationParameter", Arrays.asList());
					returnMap.put("ndesigntemplatemappingcode", null);
				}
				return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
			 }
		     else {
		    	return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDFILTERALREADYLOADED",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		    }
		}
		
		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDFILTERISNOTPRESENT",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
		
	}
	
	//Added by Dhanushya RI for JIRA ID:ALPD-5511 edit test method insert --Start
	/**
	 * This method is used to get the method list for the particular test.
	 * @param userinfo object of the method
	 * @param map object of the method
	 * @return list of method based on the specified test
	 * @throws Exception 
	 */
	public ResponseEntity<Object> getTestMethod(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
			
		Map<String, Object> rtnMap = new HashMap<>();
		final boolean jobAllocationNeeded = (boolean) (inputMap.containsKey("nneedjoballocation")?(boolean) inputMap.get("nneedjoballocation"):
			                                Enumeration.TransactionStatus.NO.gettransactionstatus()) ;  
		final boolean myJobsNeeded = (boolean) (inputMap.containsKey("nneedmyjob")?(boolean) inputMap.get("nneedmyjob"):
		                                Enumeration.TransactionStatus.NO.gettransactionstatus()) ;
		
        final String ntransationtestcode= inputMap.containsKey("ntransactiontestcode")?  (String)inputMap.get("ntransactiontestcode"):"-1";
			
        final String sValidQry = " select max(ntransactionstatus) as ntransactionstatus from registrationtesthistory "
					                + " where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +""
					                + " and nsitecode="+userInfo.getNtranssitecode()
					                +" and ntransactiontestcode="+inputMap.get("ntransactiontestcode") +" ";

		final List<RegistrationTestHistory> lstTestHistory = jdbcTemplate.query(sValidQry, new RegistrationTestHistory());
		        
        final List<Integer> lstStatus=new ArrayList<>();
        lstStatus.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
		
        if(jobAllocationNeeded && myJobsNeeded) {
			lstStatus.clear();	    	
            lstStatus.add(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
		}else if(!jobAllocationNeeded && myJobsNeeded) {
			lstStatus.clear();	    	
            lstStatus.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
		}else if(jobAllocationNeeded && ! myJobsNeeded) {
			lstStatus.clear();	    	
            lstStatus.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());	
            lstStatus.add(Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus());
        }else if(!jobAllocationNeeded && !myJobsNeeded) {
            lstStatus.clear();	    	
            lstStatus.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
        }
	    
        final boolean status = !lstTestHistory.isEmpty() && lstStatus.stream()
                        .anyMatch(data -> data.equals(lstTestHistory.get(0).getNtransactionstatus()));
		if(status) {
			final String sQry = " select m.nmethodcode,m.smethodname from testgrouptest tgt,"
									+ " testgrouptestparameter tgp,testparameter tp,registrationtest rt,method m,testmethod tm "
					                + " where rt.ntestgrouptestcode=tgt.ntestgrouptestcode "
									+ " and tgp.ntestgrouptestcode=tgt.ntestgrouptestcode "
					                + " and tgp.ntestparametercode=tp.ntestparametercode and tp.ntestcode=tm.ntestcode "
									+ " and tm.nmethodcode=m.nmethodcode "					              
									+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					                + " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					                + " and m.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +""									
					                + " and rt.nsitecode="+userInfo.getNtranssitecode()
					                + " and tgt.nsitecode = " + userInfo.getNmastersitecode()
					                + " and tgp.nsitecode = " + userInfo.getNmastersitecode()
					                + " and m.nsitecode = " + userInfo.getNmastersitecode()
					                + " and tm.nsitecode = " + userInfo.getNmastersitecode()
					                + " and tp.nsitecode = " + userInfo.getNmastersitecode()
					                +" and rt.ntransactiontestcode='"+ntransationtestcode +"' ";
	
			final List<Method> lstMethod = jdbcTemplate.query(sQry, new Method());
			rtnMap.put("TestMethod",lstMethod);
			if (inputMap.containsKey("getTestChildDetail")
					&& (Boolean) inputMap.get("getTestChildDetail") == true) {
				rtnMap.putAll((Map<String, Object>) getRegistrationTest(inputMap, userInfo).getBody());
			}
			return new ResponseEntity<Object>(rtnMap, HttpStatus.OK);

	    }
		else {
        	return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREGISTERACCEPTEDRECEIVEINITIATETEST",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
        }
		
	}
	/**
	 * This method is used to update the test method objects for the particular test.
	 * @param userinfo object of the method
	 * @param map object of the method
	 * @return object of method based on the specified test
	 * @throws Exception 
	 */			
	public ResponseEntity<Object> updateTestMethod(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		Map<String, Object> rtnMap = new HashMap<>();
        final List<String> multilingualIDList  = new ArrayList<>();		
		final List<Object> listAfterUpdate = new ArrayList<>();		
		final List<Object> listBeforeUpdate = new ArrayList<>();
		
		final String ntransationtestcode= inputMap.containsKey("ntransactiontestcode")? (String) inputMap.get("ntransactiontestcode"):"-1";
		
		final int nmethodcode= inputMap.containsKey("nmethodcode")? (int) inputMap.get("nmethodcode"):
                                       Enumeration.TransactionStatus.NA.gettransactionstatus(); 
		
		final boolean jobAllocationNeeded = (boolean) (inputMap.containsKey("nneedjoballocation")?(boolean) inputMap.get("nneedjoballocation"):
            Enumeration.TransactionStatus.NO.gettransactionstatus()) ;  
		
        final boolean myJobsNeeded = (boolean) (inputMap.containsKey("nneedmyjob")?(boolean) inputMap.get("nneedmyjob"):
        Enumeration.TransactionStatus.NO.gettransactionstatus()) ;
        
        final String sValidQry = " select max(ntransactionstatus) as ntransactionstatus from registrationtesthistory where "
                + " nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +""
                + " and nsitecode="+userInfo.getNtranssitecode()
                +" and ntransactiontestcode='"+inputMap.get("ntransactiontestcode") +"' ";

        final List<RegistrationTestHistory> lstTestHistory = jdbcTemplate.query(sValidQry, new RegistrationTestHistory());
        
        final String sAuditQry = " select m.nmethodcode,m.smethodname from method m,registrationtest rt where "
				        		+ " rt.nmethodcode=m.nmethodcode "
        		                + " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				                + " and m.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +""
				                + " and rt.nsitecode="+userInfo.getNtranssitecode()
				                + " and m.nsitecode = " + userInfo.getNmastersitecode()
				                + " and rt.ntransactiontestcode='"+inputMap.get("ntransactiontestcode") +"' ";

        final Method objOldMethod=(Method) jdbcUtilityFunction.queryForObject(sAuditQry,  Method.class, jdbcTemplate);
        
        final List<Integer> lstStatus=new ArrayList<>();
        lstStatus.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
        
		if(jobAllocationNeeded && myJobsNeeded) {
			lstStatus.clear();	    	
            lstStatus.add(Enumeration.TransactionStatus.ALLOTTED.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
		}else if(!jobAllocationNeeded && myJobsNeeded) {
			lstStatus.clear();	    	
            lstStatus.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
		}else if(jobAllocationNeeded && ! myJobsNeeded) {
			lstStatus.clear();	    	
            lstStatus.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());	
            lstStatus.add(Enumeration.TransactionStatus.RECIEVED_IN_SECTION.gettransactionstatus());
        }else if(!jobAllocationNeeded && !myJobsNeeded) {
            lstStatus.clear();	    	
            lstStatus.add(Enumeration.TransactionStatus.REGISTERED.gettransactionstatus());
            lstStatus.add(Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus());
        }
		
	    final boolean status = !lstTestHistory.isEmpty() && lstStatus.stream()
	                    .anyMatch(data -> data.equals(lstTestHistory.get(0).getNtransactionstatus()));
	    if(status) {
	        	
	        final String strQuery="select nmethodcode,smethodname from method where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nmethodcode="+nmethodcode+" ";
	        final Method objMethod=(Method) jdbcUtilityFunction.queryForObject(strQuery,  Method.class, jdbcTemplate);
	        
			final String updateQuery = " update registrationtest set nmethodcode="+nmethodcode+" "
						                 + " , jsondata=jsondata||json_build_object('smethodname' ,'"+objMethod.getSmethodname()+"')::jsonb where "
						                 + " nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +""
			                             + " and nsitecode="+userInfo.getNtranssitecode()+" and ntransactiontestcode='"+ntransationtestcode +"' ";

			jdbcTemplate.execute(updateQuery);
			
			rtnMap.putAll((Map<String, Object>) getRegistrationTest(inputMap, userInfo).getBody());
			
			listAfterUpdate.add(objMethod);
			listBeforeUpdate.add(objOldMethod);
			multilingualIDList.add("IDS_TESTMETHOD");	
			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);						
			return new ResponseEntity<Object>(rtnMap, HttpStatus.OK);
        }
        else {
        	return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREGISTERACCEPTEDRECEIVEINITIATETEST",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
        }
	}
	//End	

}
