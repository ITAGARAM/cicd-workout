package com.agaramtech.qualis.materialmanagement.service.materialinventory;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.dynamicpreregdesign.model.Combopojo;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.materialmanagement.model.MappedTemplateFieldPropsMaterial;
import com.agaramtech.qualis.materialmanagement.model.Material;
import com.agaramtech.qualis.materialmanagement.model.MaterialCategory;
import com.agaramtech.qualis.materialmanagement.model.MaterialConfig;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventory;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventoryFile;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventoryTrans;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventoryType;
import com.agaramtech.qualis.materialmanagement.model.MaterialType;
import com.agaramtech.qualis.scheduler.model.SchedulerSampleDetail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;


@AllArgsConstructor
@Repository
public class MaterialInventoryDAOImpl implements MaterialInventoryDAO {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialInventoryDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	//private final MaterialSectionDAO objMaterialSectionDAO;
	//private final MaterialDAO objMaterialDAOImpl;
	//private final DynamicPreRegDesignDAO objDynamicPreRegDesignDAO;
	//private final AgaramtechGeneralfunction objGeneral;
	//private final public DynamicPreRegDesignDAOImpl objDynamicPreRegDesignDAOImpl;



	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getMaterialInventory(UserInfo objUserInfo) throws Exception {
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		String strquery2 = "";
		String strQuery = "select nmaterialtypecode,jsondata->'smaterialtypename'->>'"
				+ objUserInfo.getSlanguagetypecode() + "' "
				+ "as smaterialtypename,jsondata  from materialtype where nmaterialtypecode>-1 and nstatus=1 and nmaterialtypecode!=5 order by nmaterialtypecode";

		List<MaterialType> lstMaterialType = jdbcTemplate.query(strQuery,new MaterialType() );
		
		objmap.put("MaterialType", lstMaterialType);
		String strquery1 = "select nmaterialcatcode,smaterialcatname from materialcategory where "
				+ "  nmaterialtypecode=" + lstMaterialType.get(0).getNmaterialtypecode()
				+ " and nstatus=1 order by nmaterialcatcode";
		List<MaterialCategory> lstMaterialCategory = jdbcTemplate.query(strquery1, new MaterialCategory());
		List<MaterialType> lstMaterialType1 = new ArrayList<MaterialType>();
		lstMaterialType1.add(lstMaterialType.get(0));
		objmap.put("SelectedMaterialType", lstMaterialType1);
		if (!lstMaterialCategory.isEmpty()) {
			objmap.put("MaterialCategoryMain", lstMaterialCategory);
			objmap.put("nmaterialcatcode", lstMaterialCategory.get(0).getNmaterialcatcode());
			
			strquery2 = "select nmaterialcode,json_build_object ('nmaterialcode',nmaterialcode,'Material Name',jsondata->'Material Name') as jsondata  from material where "
					+ "   (jsondata->'nmaterialcatcode')::int=" + lstMaterialCategory.get(0).getNmaterialcatcode()
					+ " and nstatus=1 order by nmaterialcode";

			List<Material> lstMaterial = jdbcTemplate.query(strquery2, new Material());
			objmap.put("MaterialCombo", lstMaterial);
			objmap.putAll(
					(Map<String, Object>) getMaterialInventoryAdd((int) lstMaterialType.get(0).getNmaterialtypecode(),
							objUserInfo).getBody());
			@SuppressWarnings("removal")
			Short intType = new Short(lstMaterialType.get(0).getNmaterialtypecode());
			objmap.put("nmaterialtypecode", intType.intValue());
			objmap.put("nmaterialcatcode", lstMaterialCategory.get(0).getNmaterialcatcode());
			if (!lstMaterial.isEmpty()) {
				objmap.put("nmaterialcode", lstMaterial.get(0).getNmaterialcode());
			}
			objmap.putAll((Map<String, Object>) getMaterialInventoryByID(objmap, objUserInfo).getBody());
		}
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMaterialInventorycombo(Integer nmaterialtypecode, Integer nmaterialcatcode,
			UserInfo objUserInfo) throws Exception {
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		String strquery2 = "";
		String comboget = "select nmaterialcatcode,smaterialcatname from materialcategory where nmaterialtypecode="
				+ nmaterialtypecode + " and nstatus=1";
		List<MaterialCategory> lstMaterialCategory = jdbcTemplate.query(comboget, new MaterialCategory());
		if (!lstMaterialCategory.isEmpty()) {
			
				strquery2 = nmaterialcatcode == null
						? "select json_build_object ('nmaterialcode',nmaterialcode,'Material Name',jsondata->'Material Name') as jsondata  from material where "
								+ "   (jsondata->'nmaterialcatcode')::int="
								+ lstMaterialCategory.get(0).getNmaterialcatcode()
								+ " and ( jsondata -> 'nmaterialtypecode' ) :: INT=" + nmaterialtypecode
								+ " and nstatus=1 and ( jsondata -> 'nmaterialtypecode' ) :: INT=" + nmaterialtypecode
						: "select json_build_object ('nmaterialcode',nmaterialcode,'Material Name',jsondata->'Material Name') as jsondata  from material where "
								+ "   (jsondata->'nmaterialcatcode')::int=" + nmaterialcatcode
								+ " and nstatus=1 and ( jsondata -> 'nmaterialtypecode' ) :: INT=" + nmaterialtypecode;

			List<Material> lstMaterial = jdbcTemplate.query(strquery2, new Material());
			objmap.put("MaterialCombo", lstMaterial);
		} else {
			objmap.put("MaterialCombo", lstMaterialCategory);
		}
		if (nmaterialcatcode == null) {
			objmap.put("MaterialCategoryMain", lstMaterialCategory);
		}
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getMaterialInventoryByID(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstMaterialInventory = new ArrayList<Map<String, Object>>();
		String comboget = "";
		String strquery2 = "";
		if (inputMap.containsKey("nmaterialcode")) {
			
			int needSectionWise = jdbcTemplate.queryForObject("select needsectionwise from materialcategory where "
					+ " nmaterialcatcode=" + inputMap.get("nmaterialcatcode"), Integer.class);
			String nsectioncodestr = "";
			if (needSectionWise == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				nsectioncodestr = "  ( select  " + " s.nsectioncode " + " from " + " sectionusers su, "
						+ " labsection l, " + " SECTION s " + " where su.nlabsectioncode = l.nlabsectioncode "
						+ " AND l.nsectioncode = s.nsectioncode " + " AND su.nsitecode =  "
						+ userInfo.getNtranssitecode() + " AND su.nusercode =   " + userInfo.getNusercode()
						+ " and su.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and l.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) ";
			} else {
				nsectioncodestr = " (" + Enumeration.TransactionStatus.NA.gettransactionstatus() + ") ";
			}

			String query1 = "select json_agg(a.jsonuidata) from  (select mi.nmaterialinventorycode,mi.jsonuidata||json_build_object('nmaterialinventorycode',mi.nmaterialinventorycode ,'status'"
					+ ",(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "')"
					+ ",'Section',s.ssectionname,'Site',si.ssitename )::jsonb " + " "
					+ "as jsonuidata  from materialinventory mi,materialcategory mc,material m,"
					+ " transactionstatus ts,section s,site si  where " + " (mi.jsondata->'nmaterialcode')::int="
					+ inputMap.get("nmaterialcode") + "" + "  and (mi.jsondata->'nmaterialtypecode')::int="
					+ inputMap.get("nmaterialtypecode") + " and  (mi.jsondata->'nmaterialcatcode')::int="
					+ inputMap.get("nmaterialcatcode") + " AND ts.ntranscode = (select   ntransactionstatus "
					+ "					 from  materialinventoryhistory where "
					+ "					 nmaterialinventoryhistorycode=(select   max(nmaterialinventoryhistorycode) "
					+ "					 from  materialinventoryhistory where "
					+ "					 nmaterialinventorycode=mi.nmaterialinventorycode) ) " + " and mi.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND mc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					// +" and mt.nmaterialinventorycode=mi.nmaterialinventorycode"
					+ " and (m.jsondata->'nmaterialcatcode')::INT=mc.nmaterialcatcode"
					+ " and m.nmaterialcode=mi.nmaterialcode" + " and s.nsectioncode=mi.nsectioncode "
					+ " and si.nsitecode=mi.nsitecode " + " and mi.nsectioncode in " + nsectioncodestr
					+ " and mi.nsitecode = " + userInfo.getNtranssitecode() + " order by mi.nmaterialinventorycode)a";

			String strMaterialInventory = jdbcTemplate.queryForObject(query1, String.class);
			if (strMaterialInventory != null)
				lstMaterialInventory = objmapper.convertValue(projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterialInventory,
						userInfo, true,
						(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
								.gettransactionstatus()
										? 6
										: (int) inputMap.get(
												"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
														.gettransactionstatus() ? 7 : 8,
						(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
								.gettransactionstatus()
										? "MaterialInvStandard"
										: (int) inputMap.get(
												"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
														.gettransactionstatus() ? "MaterialInvVolumetric"
																: "MaterialInvMaterialInventory"),
						new TypeReference<List<Map<String, Object>>>() {
						});
			objmap.put("MaterialInventory", lstMaterialInventory);
			if (!lstMaterialInventory.isEmpty()) {
				if (!(inputMap.containsKey("nflag"))) {
					
					objmap.put("SelectedMaterialInventory", lstMaterialInventory.get(lstMaterialInventory.size() - 1));
					inputMap.put("nsectioncode",
							lstMaterialInventory.get(lstMaterialInventory.size() - 1).get("nsectioncode"));

					objmap.putAll(
							(Map<String, Object>) getQuantityTransactionByMaterialInvCode((int) lstMaterialInventory
									.get(lstMaterialInventory.size() - 1).get("nmaterialinventorycode"), inputMap,
									userInfo).getBody());
					objmap.putAll((Map<String, Object>) getMaterialFile((int) lstMaterialInventory
							.get(lstMaterialInventory.size() - 1).get("nmaterialinventorycode"), userInfo));
					objmap.putAll((Map<String, Object>) getResultUsedMaterial((int) lstMaterialInventory
							.get(lstMaterialInventory.size() - 1).get("nmaterialinventorycode"), userInfo).getBody());
					objmap.putAll((Map<String, Object>) getMaterialInventoryhistory((int) lstMaterialInventory
							.get(lstMaterialInventory.size() - 1).get("nmaterialinventorycode"), userInfo));

				} else {
					String query3 = "select json_agg(a.jsonuidata) from  (select mi.nmaterialinventorycode,mi.jsonuidata||json_build_object('nmaterialinventorycode',mi.nmaterialinventorycode"
							+ " ,'status'" + "	,(ts.jsondata->'stransdisplaystatus'->>'"
							+ userInfo.getSlanguagetypecode()
							+ "'),'Available Quantity',mt.jsonuidata->'Available Quantity')::jsonb " + " "
							+ "as jsonuidata  from materialinventory mi,transactionstatus ts,materialinventorytransaction mt"
							+ " where  mi.nmaterialinventorycode=" + inputMap.get("nmaterialinventorycode")
							+ " and mt.nmaterialinventorycode=mi.nmaterialinventorycode"
							+ " AND ts.ntranscode = ( mi.jsondata ->> 'ntranscode' ) :: INT " + " and mi.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")a";

					strMaterialInventory = jdbcTemplate.queryForObject(query3, String.class);
					List<Map<String, Object>> lstMaterialInventory1 = new ArrayList<Map<String, Object>>();
					lstMaterialInventory1 = objmapper.convertValue(projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
							strMaterialInventory, userInfo, true,
							(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? 6
											: (int) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? 7 : 8,
							(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? "MaterialInvStandard"
											: (int) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? "MaterialInvVolumetric"
																	: "MaterialInvMaterialInventory"),
							new TypeReference<List<Map<String, Object>>>() {
							});
					// Commented For JSONUIDATA
					objmap.put("SelectedMaterialInventory", lstMaterialInventory1.get(0));
					inputMap.put("nsectioncode", lstMaterialInventory1.get(0).get("nsectioncode"));
					if (inputMap.containsKey("sprecision")) {
						inputMap.put("sprecision", inputMap.get("sprecision"));
					}
					if (inputMap.containsKey("needsectionwise")) {
						inputMap.put("needsectionwise", inputMap.get("needsectionwise"));
					}
					objmap.putAll((Map<String, Object>) getQuantityTransactionByMaterialInvCode(
							(int) lstMaterialInventory1.get(0).get("nmaterialinventorycode"), inputMap, userInfo)
							.getBody());
					objmap.putAll((Map<String, Object>) getResultUsedMaterial(
							(int) lstMaterialInventory1.get(0).get("nmaterialinventorycode"), userInfo).getBody());
					objmap.putAll((Map<String, Object>) getMaterialFile(
							(int) lstMaterialInventory1.get(0).get("nmaterialinventorycode"), userInfo));
					objmap.putAll((Map<String, Object>) getMaterialInventoryhistory(
							(int) lstMaterialInventory1.get(0).get("nmaterialinventorycode"), userInfo));

				}
			} else {
				objmap.put("SelectedMaterialInventory", lstMaterialInventory);
			}
		}
		String query2 = "select nmaterialtypecode,jsondata->'smaterialtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "' " + "as smaterialtypename,jsondata  from materialtype where nmaterialtypecode="
				+ inputMap.get("nmaterialtypecode") + " and nstatus=1";

		List<MaterialType> lstMaterialType = jdbcTemplate.query(query2, new MaterialType());
		objmap.put("SelectedMaterialType", lstMaterialType);

		if (inputMap.containsKey("nmaterialcatcode")) {
			comboget = "select nmaterialcatcode,smaterialcatname,needsectionwise from materialcategory where nmaterialcatcode="
					+ inputMap.get("nmaterialcatcode") + " and nstatus=1";
		} else {
			comboget = "select nmaterialcatcode,smaterialcatname,needsectionwise from materialcategory where nmaterialtypecode="
					+ inputMap.get("nmaterialtypecode") + " and nstatus=1";
		}
		List<MaterialCategory> lstMaterialCategory = jdbcTemplate.query(comboget, new MaterialCategory());
		if (!lstMaterialCategory.isEmpty()) {
			if (inputMap.containsKey("nmaterialcatcode")) {
				objmap.put("SelectedMaterialCategory", lstMaterialCategory.get(0));
			} else {
				objmap.put("SelectedMaterialCategory", lstMaterialCategory.get(0));
			}
		}

		if (inputMap.containsKey("nmaterialcode")) {
			
			strquery2 = "select json_build_object ('nmaterialcode',m.nmaterialcode,'Material Name',m.jsondata->'Material Name','nunitcode',"
					+ " m.jsondata->'Basic Unit' ,'ntranscode',m.jsondata->'Quarantine','isExpiryNeed',m.jsondata->>'Expiry Validations'"
					+ "	, 'Open Expiry Need',m.jsondata->>'Open Expiry Need','needsectionwise',"
					+ lstMaterialCategory.get(0).getNeedSectionwise()
					+ " ,'sdisplaystatus',ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "'):: jsonb ||m.jsondata :: jsonb  as jsondata  from material m,transactionstatus ts where "
					+ "   m.nmaterialcode=" + inputMap.get("nmaterialcode")
					+ " and ts.ntranscode= ( case when (m.jsondata->'Quarantine')::int=3 then "
					+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + " else "
					+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus() + " end)::int and  m.nstatus=1";
		
			List<Material> lstMaterialCategory1 = jdbcTemplate.query(strquery2, new Material());
			if (!lstMaterialCategory1.isEmpty()) {
				if (inputMap.containsKey("nmaterialcode")) {
					objmap.put("SelectedMaterialCrumb", lstMaterialCategory1.get(0));
				} else {
					objmap.put("SelectedMaterialCrumb", lstMaterialCategory1.get(0));
				}
			}
		}
		objmap.putAll((Map<String, Object>) getQuantityTransactionTemplate(userInfo).getBody());
		objmap.putAll((Map<String, Object>) getMaterialInventoryAdd((int) inputMap.get("nmaterialtypecode"), userInfo)
				.getBody());
		objmap.put("DesignMappedFeilds", getTemplateDesignForMaterial(/*
																		 * (int) inputMap .get("nmaterialtypecode") ==
																		 * Enumeration.TransactionStatus.STANDARDTYPE.
																		 * gettransactionstatus() ? 6 : (int)
																		 * inputMap.get("nmaterialtypecode") ==
																		 * Enumeration.TransactionStatus.VOLUMETRICTYPE
																		 * .gettransactionstatus() ? 7 : 8
																		 */
				((List<MaterialConfig>) objmap.get("selectedTemplate")).get(0).getNmaterialconfigcode(),
				userInfo.getNformcode()));
		List<TransactionStatus> lstTransactionStatus = jdbcTemplate.query(
				"select ntranscode,jsondata from   transactionstatus where ntranscode in ("
						+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.ISSUE.gettransactionstatus() + ") order by 1",
				new TransactionStatus());
		objmap.put("TransactionType", lstTransactionStatus);
		List<MaterialInventoryType> lstMaterialInventoryType = jdbcTemplate.query(
				"select ninventorytypecode,jsondata from   materialinventorytype where ninventorytypecode in (1,2) order by 1",
				new MaterialInventoryType());
		objmap.put("MaterialInventoryType", lstMaterialInventoryType);
		objmap.put("DesignMappedFeildsQuantityTransaction", getTemplateDesignForMaterial(9, userInfo.getNformcode()));

		return new ResponseEntity<>(objmap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getMaterialInventoryAdd(Integer nmaterialtypecode, UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> objmap = new HashMap<>();
		String comboget = "";
		
		comboget = "SELECT *  from materialconfig  where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nformcode="
				+ objUserInfo.getNformcode() + " and  nmaterialtypecode=" + nmaterialtypecode;

		List<MaterialConfig> lstMaterialConfig = jdbcTemplate.query(comboget, new MaterialConfig());
		objmap.put("selectedTemplate", lstMaterialConfig);
		return new ResponseEntity<>(objmap, HttpStatus.OK);

	}

	public JSONObject getmaterialquery(JSONObject objMaterialInventory, int nflag) throws Exception {
		JSONObject objMatNextvalidationperiod = null;
		JSONObject objMatOpenExpiryPeriod = null;
		JSONObject objMatExpiryPolicyperiod = null;
		String strQuerymat = "select * from material  where nmaterialcode= " + objMaterialInventory.get("nmaterialcode")
				+ " ";
		List<Material> lstMaterial = (List<Material>) jdbcTemplate.query(strQuerymat, new Material());
		JSONObject objMat = new JSONObject(lstMaterial.get(0).getJsondata());
		if (objMat.has("Next Validation Period")) {
			objMatNextvalidationperiod = new JSONObject(objMat.get("Next Validation Period").toString());
		}
		if (objMat.has("Open Expiry Period")) {
			objMatOpenExpiryPeriod = new JSONObject(objMat.get("Open Expiry Period").toString());
		}
		if (objMat.has("Expiry Policy Period")) {
			objMatExpiryPolicyperiod = new JSONObject(objMat.get("Expiry Policy Period").toString());
		}
		if (lstMaterial != null && lstMaterial.size() > 0) {
			// SET THE NEXT VALIDATION DATE
			if (nflag == 1) {
				if (objMat.has("Next Validation")) {
					if (!objMat.get("Next Validation").equals("")) {
						if ((int) objMat.get("Next Validation") > 0) {
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							if (objMaterialInventory.has("Received Date & Time")) {
								if (!objMaterialInventory.get("Received Date & Time").toString().equals("")) {
									Date date1 = df.parse(objMaterialInventory.get("Received Date & Time").toString());
									if (date1 != null) {
										Calendar cal1 = Calendar.getInstance();
										cal1.setTime(date1);
										if (objMatNextvalidationperiod != null) {
											if ((int) objMatNextvalidationperiod
													.get("value") == Enumeration.TransactionStatus.DAYPERIOD
															.gettransactionstatus())
												cal1.add(Calendar.DATE, (int) objMat.get("Next Validation"));
											if ((int) objMatNextvalidationperiod
													.get("value") == Enumeration.TransactionStatus.MONTHPERIOD
															.gettransactionstatus())
												cal1.add(Calendar.MONTH, (int) objMat.get("Next Validation"));
											if ((int) objMatNextvalidationperiod
													.get("value") == Enumeration.TransactionStatus.YEARPERIOD
															.gettransactionstatus())
												cal1.add(Calendar.YEAR, (int) objMat.get("Next Validation"));
											date1 = cal1.getTime();
										}
										objMaterialInventory.put("dretestdate", df.format(date1));
									}
								}
							}
						}
					}
				}
				// SET THE POLICY EXPIRY DATE
				if (objMat.has("Expiry Policy Days")) {
					if (!objMat.get("Expiry Policy Days").equals("")) {
						if ((int) objMat.get("Expiry Policy Days") > 0) {
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							if (objMaterialInventory.has("Expiry Date & Time")) {
								if (!objMaterialInventory.get("Expiry Date & Time").toString().equals("")) {
									Date date2 = df.parse(objMaterialInventory.get("Expiry Date & Time").toString());
									if (date2 != null) {
										Calendar cal2 = Calendar.getInstance();
										cal2.setTime(date2);
										if (objMatExpiryPolicyperiod != null) {
											if ((int) objMatExpiryPolicyperiod.get("value") == Enumeration.Period.Hours
													.getPeriod())
												cal2.add(Calendar.HOUR_OF_DAY, (int) objMat.get("Expiry Policy Days"));
											if ((int) objMatExpiryPolicyperiod.get("value") == Enumeration.Period.Days
													.getPeriod())
												cal2.add(Calendar.DATE, (int) objMat.get("Expiry Policy Days"));
											if ((int) objMatExpiryPolicyperiod.get("value") == Enumeration.Period.Month
													.getPeriod())
												cal2.add(Calendar.MONTH, (int) objMat.get("Expiry Policy Days"));
											if ((int) objMatExpiryPolicyperiod.get("value") == Enumeration.Period.Years
													.getPeriod())
												cal2.add(Calendar.YEAR, (int) objMat.get("Expiry Policy Days"));
											date2 = cal2.getTime();
										}
										objMaterialInventory.put("dexpirypolicydate", df.format(date2));
										objMaterialInventory.put("Expiry Date & Time", df.format(date2));
									}
								}
							}
						}
					}
				}
			}
			if (nflag == 2) {
				// SET THE POLICY EXPIRY DATE
				if (objMat.has("Open Expiry")) {
					if (!objMat.get("Open Expiry").equals("")) {
						if ((int) objMat.get("Open Expiry") > 0) {
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							if (objMaterialInventory.has("Open Date")) {
								if (!objMaterialInventory.get("Open Date").toString().equals("")) {
									Date date2 = df.parse(objMaterialInventory.get("Open Date").toString());
									if (date2 != null) {
										Calendar cal2 = Calendar.getInstance();
										cal2.setTime(date2);
										if (objMatOpenExpiryPeriod != null) {
											if ((int) objMatOpenExpiryPeriod
													.get("value") == Enumeration.TransactionStatus.DAYPERIOD
															.gettransactionstatus())
												cal2.add(Calendar.DATE, (int) objMat.get("Open Expiry"));
											if ((int) objMatOpenExpiryPeriod
													.get("value") == Enumeration.TransactionStatus.MONTHPERIOD
															.gettransactionstatus())
												cal2.add(Calendar.MONTH, (int) objMat.get("Open Expiry"));
											if ((int) objMatOpenExpiryPeriod
													.get("value") == Enumeration.TransactionStatus.YEARPERIOD
															.gettransactionstatus())
												cal2.add(Calendar.YEAR, (int) objMat.get("Open Expiry"));
											date2 = cal2.getTime();
										}
										objMaterialInventory.put("dopenexpirydate", df.format(date2));
									}
								}
							}
						}
					}
				}
			}
		}
		return objMaterialInventory;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> createMaterialInventory(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {

		final String sTableLockQuery = " lock  table lockmaterialinventory "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sTableLockQuery);

		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();
		JSONArray jsonUidataarray = new JSONArray();
		JSONArray jsonUidataarrayTrans = new JSONArray();
		final ObjectMapper objmapper = new ObjectMapper();
		List<Map<String, Object>> lstAudit = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lstAudittrans = new ArrayList<Map<String, Object>>();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		List<String> lstDateField = (List<String>) inputMap.get("DateList");
		JSONObject jsonObject = new JSONObject(inputMap.get("materialInventoryJson").toString());
		JSONObject jsonObjectInvTrans = new JSONObject(inputMap.get("MaterialInventoryTrans").toString());

		JSONObject jsonuidata = new JSONObject(inputMap.get("jsonuidata").toString());
		JSONObject jsonuidataTrans = new JSONObject(inputMap.get("jsonuidataTrans").toString());

		jsonObject.put("parent", true);
		jsonObjectInvTrans.put("parent", true);

		jsonObject.put("currentDateTimeOffset", dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()));
		jsonObject.put("timezonecode", objUserInfo.getNtimezonecode());
		jsonuidata.put("currentDateTimeOffset", dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()));
		jsonuidata.put("timezonecode", objUserInfo.getNtimezonecode());
		objmap.putAll((Map<String, Object>) getQuantityTransactionTemplate(userInfo).getBody());
		jsonObject.put("sprecision", objmap.get("sprecision"));
		jsonuidata.put("sprecision", objmap.get("sprecision"));
		jsonObject.put("needsectionwise", inputMap.get("needsectionwise"));
		jsonuidata.put("needsectionwise", inputMap.get("needsectionwise"));
		jsonObjectInvTrans.put("sprecision", objmap.get("sprecision"));
		jsonuidataTrans.put("sprecision", objmap.get("sprecision"));
		jsonObjectInvTrans.put("needsectionwise", inputMap.get("needsectionwise"));
		jsonuidataTrans.put("needsectionwise", inputMap.get("needsectionwise"));

		jsonObject.put("Site", new JSONObject(
				"{\"label\":\"" + userInfo.getSsitename() + "\",\"value\":" + userInfo.getNsitecode() + "}"));
		jsonuidata.put("Site", userInfo.getSsitename());
		jsonObjectInvTrans.put("Site", new JSONObject(
				"{\"label\":\"" + userInfo.getSsitename() + "\",\"value\":" + userInfo.getNsitecode() + "}"));
		jsonuidataTrans.put("Site", userInfo.getSsitename());

		Date receiveDate = null;
		Date expiryDate = null;
		Date manufDate = null;
		String strcheck = "";
		String strformat = "";
		String updatestr = "";
		String insmat = "";
		boolean nflag = false;
		String sformattype = jdbcTemplate.queryForObject(
				"select sformattype from seqnoformatgeneratorstatic where stablename='materialinventory'",
				String.class);
		String strPrefix = jdbcTemplate.queryForObject(
				"select jsondata->>'Prefix' from material where nmaterialcode= " + (int) inputMap.get("nmaterialcode"),
				String.class);
		String strtypePrefix = jdbcTemplate
				.queryForObject("select jsondata->>'prefix' from materialtype where nmaterialtypecode= "
						+ jsonObject.get("nmaterialtypecode"), String.class);
		final String dtransactiondate = dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS).toString()
				.replace("T", " ").replace("Z", "");
		if (!lstDateField.isEmpty()) {
			jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, lstDateField, false, objUserInfo);
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String isExpiryNeed = jdbcTemplate.queryForObject("select (jsondata->>'Expiry Validations') from "
				+ " material where nmaterialcode=" + (Integer) inputMap.get("nmaterialcode"), String.class);
		if (isExpiryNeed != null) {
			if (isExpiryNeed.equals("Expiry date") && !jsonObject.has("Received Date & Time")
					&& !jsonObject.has("Expiry Date & Time")) {
				String msg = "";
				if ((int) jsonObject.get("nmaterialtypecode") == Enumeration.MaterialType.VOLUMETRIC_TYPE
						.getMaterialType()) {
					msg = "IDS_ENTEREXPIRYDATEASEXPIRYDATEPROVIDED";
				} else {
					msg = "IDS_ATLEASTADDEXPIRYDATEORRECEIVEDDATE";
				}
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(msg, userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			} else if (isExpiryNeed.equals("Expiry policy") && !jsonObject.has("Received Date & Time")
					&& !jsonObject.has("Expiry Date & Time")) {
				String msg = "";
				if ((int) jsonObject.get("nmaterialtypecode") == Enumeration.MaterialType.VOLUMETRIC_TYPE
						.getMaterialType()) {
					msg = "IDS_ENTEREXPIRYDATEASEXPIRYPOLICYPROVIDED";
				} else {
					msg = "IDS_POLICYPROVIDEDADDRECEIVEDDATEOREXPIRYDATE";
				}
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(msg, userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
		if (jsonObject.has("Received Date & Time")) {
			receiveDate = df.parse(jsonObject.get("Received Date & Time").toString());
			if (receiveDate != null && expiryDate != null) {
				if (receiveDate.compareTo(expiryDate) == 1) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_RECEIVEDATELESSTHANEXPEDATE",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
			if (receiveDate != null && manufDate != null) {
				if (receiveDate.compareTo(manufDate) == -1) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_RECEIVEDATEGREATERTHANMANUFEDATE",
									userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}
		if (jsonObject.has("Expiry Date & Time")) {
			expiryDate = df.parse(jsonObject.get("Expiry Date & Time").toString());
			if (receiveDate != null && expiryDate != null) {
				if (expiryDate.compareTo(receiveDate) == -1) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							"IDS_EXPDATEGREATERTHANRECEIVEDATE", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
			if (expiryDate != null && manufDate != null) {
				if (expiryDate.compareTo(manufDate) == -1) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_EXPDATEGREATERTHANMANUFDATE",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
		}
		if (jsonObject.has("Date Of Manufacturer")) {
			manufDate = df.parse(jsonObject.get("Date Of Manufacturer").toString());
			if (manufDate != null && expiryDate != null) {
				if (manufDate.compareTo(expiryDate) == 1) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MANUFDATELESSTHANEXPEDATE",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
			if (manufDate != null && receiveDate != null) {
				if (manufDate.compareTo(receiveDate) == 1) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							"IDS_MANUFDATELESSTHANRECEIVEEDATE", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
		}

		int seqnomaterialinv = jdbcTemplate.queryForObject(
				"select nsequenceno from  seqnomaterialmanagement where stablename='materialinventory'", Integer.class);
		int seqnomaterialinvtrans = jdbcTemplate.queryForObject(
				"select nsequenceno from  seqnomaterialmanagement where stablename='materialinventorytransaction'",
				Integer.class);
		seqnomaterialinv++;
		seqnomaterialinvtrans++;
		inputMap.put("nmaterialinventorycode", seqnomaterialinv);
		JSONObject objmat = new JSONObject(jdbcTemplate.queryForObject(
				"select jsondata from  material where nmaterialcode=" + inputMap.get("nmaterialcode"), String.class));
		if (objmat.has("Reusable")) {
			int reusableNeed = jdbcTemplate
					.queryForObject("select (jsondata->'Reusable')::int from  material where nmaterialcode="
							+ inputMap.get("nmaterialcode"), Integer.class);
			if (reusableNeed == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				nflag = true;
				int matseq = jdbcTemplate
						.queryForObject("select nsequenceno from seqnomaterialinventory where stablename= '"
								+ (int) inputMap.get("nmaterialcode") + "'", Integer.class);
				int reusableCount = jsonObject.getInt("Received Quantity");
				jsonObjectInvTrans.put("Transaction Date & Time", dtransactiondate);
				jsonObjectInvTrans.put("noffsetTransaction Date & Time",
						dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));
				jsonObjectInvTrans.put("ntransactiontype",
						Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());
				jsonObjectInvTrans.put("ninventorytranscode",
						Enumeration.TransactionStatus.OUTSIDE.gettransactionstatus());
				jsonObjectInvTrans.put("Received Quantity",
						Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				jsonObjectInvTrans.put("nqtyissued", 0);
				jsonObjectInvTrans.put("namountleft",
						jdbcTemplate.queryForObject("select Cast(" + jsonObjectInvTrans.get("Received Quantity")
								+ " as decimal (" + objmap.get("sprecision") + "))", String.class));
				jsonObject.put("Received Quantity", Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				jsonObject.put("nqtyissued", 0);
				jsonObject.put("namountleft",
						jdbcTemplate.queryForObject("select Cast(" + jsonObject.get("Received Quantity")
								+ " as decimal (" + objmap.get("sprecision") + "))", String.class));
				jsonObject.put("ntransactiontype", Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());
				jsonObject = getmaterialquery(jsonObject, 1);

				jsonuidata.put("Received Quantity", Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				jsonuidata.put("nqtyissued", 0);
				jsonuidata.put("namountleft",
						jdbcTemplate.queryForObject("select Cast(" + jsonuidata.get("Received Quantity")
								+ " as decimal (" + objmap.get("sprecision") + "))", String.class));
				jsonuidata.put("ntransactiontype", Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());
				jsonuidata = getmaterialquery(jsonuidata, 1);

				jsonuidataTrans.put("Transaction Date & Time", dtransactiondate);
				jsonuidataTrans.put("noffsetTransaction Date & Time",
						dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));

				jsonuidataTrans.put("ntransactiontype", Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());
				jsonuidataTrans.put("ninventorytranscode",
						Enumeration.TransactionStatus.OUTSIDE.gettransactionstatus());
				jsonuidataTrans.put("Received Quantity", Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				jsonuidataTrans.put("nqtyissued", 0);
				jsonuidataTrans.put("namountleft",
						jdbcTemplate.queryForObject("select Cast(" + jsonuidataTrans.get("Received Quantity")
								+ " as decimal (" + objmap.get("sprecision") + "))", String.class));

				SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:SS");
				Date resultdate = new Date(System.currentTimeMillis());
				System.out.println("  Start  date :" + sdf.format(resultdate));

				for (int i = 0; i < reusableCount; i++) {
					matseq++;
					if (strPrefix != null && !strPrefix.equals("")) {
						strformat = strtypePrefix + "/" + strPrefix + "/" + projectDAOSupport.getfnFormat(matseq, sformattype);
					} else {
						strformat = strtypePrefix + "/" + projectDAOSupport.getfnFormat(matseq, sformattype);
					}
					jsonObject.put("Inventory ID", strformat);
					jsonuidata.put("Inventory ID", strformat);

					jsonObjectInvTrans.put("Inventory ID", strformat);
					jsonuidataTrans.put("Inventory ID", strformat);
					insmat = "INSERT INTO materialinventory( nmaterialinventorycode,nmaterialcode,ntransactionstatus,"
							+ " nsectioncode,jsondata,jsonuidata, dmodifieddate, nsitecode, nstatus)" + " VALUES ("
							+ seqnomaterialinv + "," + inputMap.get("nmaterialcode") + ", "
							+ inputMap.get("ntransactionstatus") + "," + inputMap.get("nsectioncode") + ",'"
							+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::jsonb, '" + stringUtilityFunction.replaceQuote(jsonuidata.toString())
							+ "'::jsonb, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

					if ((int) inputMap.get("needsectionwise") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						jsonuidataTrans.put("Description",
								commonFunction.getMultilingualMessage("IDS_RECEIVED", userInfo.getSlanguagefilename())
										+ " "
										+ commonFunction.getMultilingualMessage("IDS_BY",
												userInfo.getSlanguagefilename())
										+ " "
										+ new JSONObject(jsonObjectInvTrans.get("Section").toString()).get("label"));
					} else {
						jsonuidataTrans.put("Description",
								commonFunction.getMultilingualMessage("IDS_RECEIVED", userInfo.getSlanguagefilename())
										+ " "
										+ commonFunction.getMultilingualMessage("IDS_BY",
												userInfo.getSlanguagefilename())
										+ " " + commonFunction.getMultilingualMessage(userInfo.getSsitename(),
												userInfo.getSlanguagefilename()));
					}
					jdbcTemplate.execute("INSERT INTO public.materialinventorytransaction("
							+ "	nmaterialinventtranscode, nmaterialinventorycode,ninventorytranscode"
							+ ",ntransactiontype,nsectioncode, nresultusedmaterialcode,nqtyreceived,nqtyissued, jsondata,jsonuidata,dtransactiondate,ntztransactiondate,noffsetdtransactiondate ,nsitecode, nstatus)"
							+ "	VALUES (" + seqnomaterialinvtrans + ", " + seqnomaterialinv + ","
							+ jsonObjectInvTrans.get("ninventorytranscode") + ","
							+ jsonObjectInvTrans.get("ntransactiontype") + "," + jsonObjectInvTrans.get("nsectioncode")
							+ ", -1,Cast(" + jsonObjectInvTrans.get("Received Quantity") + " as decimal ("
							+ objmap.get("sprecision") + ")),Cast(" + jsonObjectInvTrans.get("nqtyissued")
							+ " as decimal (" + objmap.get("sprecision") + ")), '"
							+ stringUtilityFunction.replaceQuote(jsonObjectInvTrans.toString()) + "'::jsonb,'"
							+ stringUtilityFunction.replaceQuote(jsonuidataTrans.toString()) + "'::jsonb, '" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "', " + userInfo.getNtimezonecode() + ", "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + userInfo.getNtranssitecode()
							+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");");
					jdbcTemplate.execute(insmat);
					seqnomaterialinv++;
					seqnomaterialinvtrans++;
					jsonUidataarray.put(new JSONObject(jsonuidata.toString()));
					jsonUidataarrayTrans.put(new JSONObject(jsonuidataTrans.toString()));
				}
				inputMap.put("reusableCount", reusableCount);
				inputMap.put("ntranscode", inputMap.get("ntransactionstatus"));

				createMaterialInventoryhistory(inputMap, userInfo);
				jdbcTemplate.execute("update seqnomaterialinventory set nsequenceno=" + matseq
						+ "  where stablename='" + (int) inputMap.get("nmaterialcode") + "' ");
			}
		}
		if (nflag == false) {
			int matseq = jdbcTemplate
					.queryForObject("select nsequenceno from seqnomaterialinventory where stablename= '"
							+ (int) inputMap.get("nmaterialcode") + "'", Integer.class);
			matseq++;
			if (strPrefix != null && !strPrefix.equals("")) {
				strformat = strtypePrefix + "/" + strPrefix + "/" + projectDAOSupport.getfnFormat(matseq, sformattype);
			} else {
				strformat = strtypePrefix + "/" + projectDAOSupport.getfnFormat(matseq, sformattype);
			}
			jsonObject.put("Inventory ID", strformat);
			jsonObject.put("ntransactiontype", Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());
			jsonObject = getmaterialquery(jsonObject, 1);

			jsonuidata.put("Inventory ID", strformat);
			jsonuidata.put("ntransactiontype", Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());
			jsonuidata = getmaterialquery(jsonuidata, 1);

			jdbcTemplate.execute("update seqnomaterialinventory set nsequenceno=" + matseq + "  where stablename='"
					+ (int) inputMap.get("nmaterialcode") + "' ");

			insmat = "INSERT INTO materialinventory("
					+ " nmaterialinventorycode,nmaterialcode,ntransactionstatus,nsectioncode,jsondata,jsonuidata, dmodifieddate, nsitecode, nstatus)"
					+ " VALUES (" + seqnomaterialinv + "," + inputMap.get("nmaterialcode") + ", "
					+ inputMap.get("ntransactionstatus") + "," + inputMap.get("nsectioncode") + ",'"
					+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::jsonb, '" + stringUtilityFunction.replaceQuote(jsonuidata.toString())
					+ "'::jsonb, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			inputMap.put("nmaterialinventorycode", seqnomaterialinv);
			inputMap.put("ntranscode", inputMap.get("ntransactionstatus"));
			createMaterialInventoryhistory(inputMap, userInfo);
			jsonObjectInvTrans.put("Inventory ID", strformat);
			jsonObjectInvTrans.put("ntransactiontype", Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());
			jsonObjectInvTrans.put("Transaction Date & Time", dtransactiondate);
			jsonObjectInvTrans.put("noffsetTransaction Date & Time",
					dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));

			jsonObjectInvTrans.put("ninventorytranscode", Enumeration.TransactionStatus.OUTSIDE.gettransactionstatus());
			jsonObjectInvTrans.put("nqtyissued", 0);
			jsonObjectInvTrans.put("namountleft",
					jdbcTemplate.queryForObject("select Cast(" + jsonObjectInvTrans.get("Received Quantity")
							+ " as decimal (" + objmap.get("sprecision") + "))", String.class));

			jsonuidataTrans.put("Inventory ID", strformat);
			jsonuidataTrans.put("ntransactiontype", Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());
			jsonuidataTrans.put("Transaction Date & Time", dtransactiondate);
			jsonuidataTrans.put("noffsetTransaction Date & Time", dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));

			jsonuidataTrans.put("ninventorytranscode", Enumeration.TransactionStatus.OUTSIDE.gettransactionstatus());
			jsonuidataTrans.put("nqtyissued", 0);
			jsonuidataTrans.put("namountleft",
					jdbcTemplate.queryForObject("select Cast(" + jsonuidataTrans.get("Received Quantity")
							+ " as decimal (" + objmap.get("sprecision") + "))", String.class));
			jsonuidataTrans.put("nmaterialinventtranscode", seqnomaterialinvtrans);
			if ((int) inputMap.get("needsectionwise") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				jsonuidataTrans.put("Description",
						commonFunction.getMultilingualMessage("IDS_RECEIVED", userInfo.getSlanguagefilename()) + " "
								+ commonFunction.getMultilingualMessage("IDS_BY", userInfo.getSlanguagefilename()) + " "
								+ commonFunction.getMultilingualMessage(userInfo.getSsitename(),
										userInfo.getSlanguagefilename())
								+ " (" + new JSONObject(jsonObjectInvTrans.get("Section").toString()).get("label")
								+ ") ");
			} else {
				jsonuidataTrans.put("Description",
						commonFunction.getMultilingualMessage("IDS_RECEIVED", userInfo.getSlanguagefilename()) + " "
								+ commonFunction.getMultilingualMessage("IDS_BY", userInfo.getSlanguagefilename()) + " "
								+ commonFunction.getMultilingualMessage(userInfo.getSsitename(),
										userInfo.getSlanguagefilename())
								+ " ("
								+ commonFunction.getMultilingualMessage("IDS_NA", userInfo.getSlanguagefilename())
								+ ") ");
			}
			jdbcTemplate.execute("INSERT INTO public.materialinventorytransaction("
					+ "	nmaterialinventtranscode, nmaterialinventorycode,ninventorytranscode"
					+ ",ntransactiontype,nsectioncode, nresultusedmaterialcode,nqtyreceived,nqtyissued, jsondata,jsonuidata,dtransactiondate,ntztransactiondate,noffsetdtransactiondate, nsitecode, nstatus)"
					+ "	VALUES (" + seqnomaterialinvtrans + ", " + seqnomaterialinv + ","
					+ jsonObjectInvTrans.get("ninventorytranscode") + "," + jsonObjectInvTrans.get("ntransactiontype")
					+ "," + jsonObjectInvTrans.get("nsectioncode") + ", -1,Cast("
					+ jsonObjectInvTrans.get("Received Quantity") + " as decimal (" + objmap.get("sprecision")
					+ ")),Cast(" + jsonObjectInvTrans.get("nqtyissued") + " as decimal (" + objmap.get("sprecision")
					+ ")), '" + stringUtilityFunction.replaceQuote(jsonObjectInvTrans.toString()) + "'::jsonb,'"
					+ stringUtilityFunction.replaceQuote(jsonuidataTrans.toString()) + "'::jsonb, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");");
			jdbcTemplate.execute(insmat);
			jsonUidataarray.put(jsonuidata);
			jsonUidataarrayTrans.put(jsonuidataTrans);
		}
		updatestr = "update seqnomaterialmanagement set nsequenceno=" + seqnomaterialinv
				+ "  where stablename='materialinventory' ;";
		updatestr += "update seqnomaterialmanagement set nsequenceno=" + seqnomaterialinvtrans
				+ "  where stablename='materialinventorytransaction' ;";

		jdbcTemplate.execute(updatestr);
		objmap.putAll((Map<String, Object>) getMaterialInventoryByID(inputMap, userInfo).getBody());
		
		if((int)inputMap.get("ntransactionstatus")==Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() )
		{
		  int nmaterialcode= -1;
		  ObjectMapper objectMapper = new ObjectMapper();
		  if (inputMap.containsKey("nmaterialcode") ) {
			  nmaterialcode= (int)inputMap.get("nmaterialcode");
		  }
		  final String sampleSchedulerConfigTypeCodeQry="select nsampleschedulerconfigtypecode from sampleschedulerconfigtype where nsampletypecode="+Enumeration.SampleType.MATERIAL.getType()
		             + " and nschedulerconfigtypecode="+ Enumeration.SchedulerConfigType.INTERNAL.getSchedulerConfigType()
		             + " and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ";";
		  
		  int nsampleschedulerconfigtypecode= jdbcTemplate
					.queryForObject(sampleSchedulerConfigTypeCodeQry, Integer.class);
		  String str="select nschedulersamplecode,nschedulecode,jsondata,jsonuidata from schedulersampledetail where nmaterialcode = "+nmaterialcode 
				  +" and ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				  +" and nsampleschedulerconfigtypecode="+nsampleschedulerconfigtypecode
				  +" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
		  List<SchedulerSampleDetail> schedulerSampleDetailList = jdbcTemplate.query(str,
					new SchedulerSampleDetail());
			if (!schedulerSampleDetailList.isEmpty()) {
				
				JSONObject ssdjsondata=new JSONObject(objectMapper.writeValueAsString(schedulerSampleDetailList.get(0).getJsondata())); 
				JSONObject ssdjsonuidata=new JSONObject(objectMapper.writeValueAsString(schedulerSampleDetailList.get(0).getJsonuidata()));
				
				String qrybuilderstr="select nquerybuildertablecode from querybuildertables where stablename='materialinventory' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int nquerybuildertablecode= jdbcTemplate
						.queryForObject(qrybuilderstr, Integer.class);
				JSONObject newMaterialInventory = new JSONObject();
                newMaterialInventory.put("pkey", "nmaterialinventorycode");
                newMaterialInventory.put("label", strformat);
                newMaterialInventory.put("value", seqnomaterialinv);
                newMaterialInventory.put("source", "materialinventory");
                newMaterialInventory.put("nmaterialinventorycode", seqnomaterialinv);
                newMaterialInventory.put("nquerybuildertablecode", nquerybuildertablecode);
                
                String regTemplateQry="select jsondata from reactregistrationtemplate rrt join designtemplatemapping dtm on dtm.nreactregtemplatecode=rrt.nreactregtemplatecode and dtm.nstatus="
                        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
                        + " join approvalconfigversion acv  on dtm.ndesigntemplatemappingcode = acv.ndesigntemplatemappingcode "
                        + "and acv.napproveconfversioncode="+ssdjsonuidata.getInt("napproveconfversioncode") ;
                 String jsondatastr=jdbcTemplate.queryForObject(regTemplateQry, String.class);
                 JSONArray rrtjsondata= new JSONArray(jsondatastr);
                 String matInvLabel=findLabel(rrtjsondata, "materialinventory");
                		 
                ssdjsondata.put(matInvLabel, newMaterialInventory); 
                ssdjsondata.put("nmaterialinventorycode",seqnomaterialinv );
                ssdjsonuidata.put(matInvLabel, strformat);	
                
                String seqnoschedulertransaction="select nsequenceno from seqnoscheduler where stablename='schedulertransaction'";
                int seqno=jdbcTemplate
						.queryForObject(seqnoschedulertransaction, Integer.class);
                seqno= seqno+1;
                
                String schedulerqry = " INSERT INTO public.schedulertransaction("
                		+ "	nschedulertransactioncode, nschedulecode, nsampletypecode, jsondata, jsonuidata,"
                		+ " nschedulersamplecode, npreregno, dscheduleoccurrencedate, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, ntransactionstatus, nsitecode, nstatus)"
                		+ "	VALUES ("+seqno+", "
                		+ schedulerSampleDetailList.get(0).getNschedulecode()+", "
                		+ Enumeration.SampleType.MATERIAL.getType()+","
                		+ " '"+stringUtilityFunction.replaceQuote(ssdjsondata.toString()) +"','"
                		+ stringUtilityFunction.replaceQuote(ssdjsonuidata.toString()) +"', "
                		+ schedulerSampleDetailList.get(0).getNschedulersamplecode()+","
                		+ " -1, '"
                		+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ,"
                		+ "'"+dateUtilityFunction.getCurrentDateTime(userInfo) + "' ,"
                		+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "," + userInfo.getNtimezonecode()
                		+ ", "+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() +", "
                		+ userInfo.getNtranssitecode()+", "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +");";
                jdbcTemplate.execute(schedulerqry);
                schedulerqry="update seqnoscheduler set nsequenceno="+seqno+" where stablename='schedulertransaction' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
                jdbcTemplate.execute(schedulerqry);
                
                jdbcTemplate.execute("call sp_materialscheduler(" + seqno
				+ "," + schedulerSampleDetailList.get(0).getNschedulersamplecode() + "," + userInfo.getNtranssitecode() + ")");
                
                }
		}
		objmap.put("nregtypecode", -1);
		objmap.put("nregsubtypecode", -1);
		objmap.put("ndesigntemplatemappingcode", jsonuidata.get("nmaterialconfigcode"));
		actionType.put("materialinventory", "IDS_ADDMATERIALINVENTORY");
		actionType.put("materialinventorytransaction", "IDS_ADDMATERIALINVENTORYTRANSACTION");
		lstAudit.add((Map<String, Object>) objmap.get("SelectedMaterialInventory"));
		Map<String, Object> returnmap = new LinkedHashMap<>();
		returnmap.put("materialinventory", lstAudit);
		jsonAuditObject.put("materialinventory", (List<Map<String, Object>>) returnmap.get("materialinventory"));
		lstAudittrans = objmapper.convertValue(jsonUidataarrayTrans.toList(),
				new TypeReference<List<Map<String, Object>>>() {
				});
		jsonAuditObject.put("materialinventorytransaction", lstAudittrans);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, objmap, false, userInfo);
		return new ResponseEntity<>(objmap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getMaterialInventoryEdit(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

			Map<String, Object> objmap = new LinkedHashMap<String, Object>();
			List<Map<String, Object>> lstMaterialInventory = new ArrayList<Map<String, Object>>();
			final ObjectMapper objmapper = new ObjectMapper();
			List<Integer> lstcount = new ArrayList<Integer>();
			lstcount = jdbcTemplate.queryForList(
					"select nmaterialinventorycode from materialinventory where nmaterialinventorycode in ("
							+ inputMap.get("nmaterialinventorycode") + ") and nstatus="
							+ Enumeration.TransactionStatus.NA.gettransactionstatus() + "",
					Integer.class);
			if (lstcount.size() == 0) {
				int ntransacode = isMaterialInvRetired((int) inputMap.get("nmaterialinventorycode"));

				if (ntransacode != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {

				String query1 = " select json_agg(a.jsondata) from ( select * from  materialinventory where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nmaterialinventorycode in (" + inputMap.get("nmaterialinventorycode") + "))a;";
				String objMaterial = jdbcTemplate.queryForObject(query1, String.class);
			
				List<MappedTemplateFieldPropsMaterial> lstsearchFields = new LinkedList<>();
				List<String> lstsearchField = new LinkedList<String>();
				String type = (int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
						.gettransactionstatus()
								? "MaterialInvStandard"
								: (int) inputMap
										.get("nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
												.gettransactionstatus() ? "MaterialInvVolumetric"
														: "MaterialInvMaterialInventory";
				int typeCode = (int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
						.gettransactionstatus()
								? 6
								: (int) inputMap
										.get("nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
												.gettransactionstatus() ? 7 : 8;
				String selectionKeyName1 = "select (jsondata->'" + userInfo.getNformcode() + "'->'" + type
						+ "datefields')::jsonb as jsondata from mappedtemplatefieldpropsmaterial  where   nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialconfigcode="
						+ typeCode;
				lstsearchFields = jdbcTemplate.query(selectionKeyName1, new MappedTemplateFieldPropsMaterial());

				JSONArray objarray = new JSONArray(lstsearchFields.get(0).getJsondata());
				for (int i = 0; i < objarray.length(); i++) {
					JSONObject jsonobject = new JSONObject(objarray.get(i).toString());
					if (jsonobject.has("2")) {
						lstsearchField.add((String) jsonobject.get("2"));
					}
				}

				objmap.put("DateFeildsInventory", lstsearchField);
				List<String> submittedDateFeilds = new LinkedList<String>();
				lstMaterialInventory = objmapper.convertValue(projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(objMaterial,
						userInfo, true,
						(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
								.gettransactionstatus()
										? 6
										: (int) inputMap.get(
												"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
														.gettransactionstatus() ? 7 : 8,
						type), new TypeReference<List<Map<String, Object>>>() {
						});
				if (!lstMaterialInventory.isEmpty()) {
					for (int i = 0; i < lstsearchField.size(); i++) {
						if (lstMaterialInventory.get(0).get(lstsearchField.get(i)) != null) {
							submittedDateFeilds.add(lstsearchField.get(i));
						}
					}
				}
				if (!submittedDateFeilds.isEmpty()) {
					objmap.put("MaterialInventoryDateFeild", submittedDateFeilds);
				}
				// Commented For JSONUIDATA
				objmap.put("EditedMaterialInventory", lstMaterialInventory);
				return new ResponseEntity<>(objmap, HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTQUARENTINEINVENTORY",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		 
	}

	@SuppressWarnings({ "unused", "unchecked"})
	@Override
	public ResponseEntity<Object> UpdateMaterialInventory(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		JSONObject jsonAuditOld = new JSONObject();
		JSONObject jsonAuditNew = new JSONObject();
		List<Map<String, Object>> lstAuditold = new ArrayList<>();
		List<Map<String, Object>> lstAuditnew = new ArrayList<>();
		List<Map<String, Object>> lstAuditoldtransold = new ArrayList<>();
		List<Map<String, Object>> lstAuditoldtranstnew = new ArrayList<>();

		JSONObject actionType = new JSONObject();
		ObjectMapper objmapper = new ObjectMapper();
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final List<Object> savedMaterialsList = new ArrayList<>();
		final List<Object> beforeMaterialsList = new ArrayList<>();
		Date receiveDate = null;
		Date expiryDate = null;
		Date manufDate = null;//IDS_MATERIALINVENTORYRETIRED
		
		List<MaterialInventory> materialCheckStatus = (List<MaterialInventory>) checkMaterialStatus((int) inputMap.get("nmaterialinventorycode"));
		if(materialCheckStatus.isEmpty()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}else if(materialCheckStatus.get(0).getNtransactionstatus() == Enumeration.TransactionStatus.RETIRED.gettransactionstatus()){
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALINVENTORYRETIRED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			
		}else if(materialCheckStatus.get(0).getNtransactionstatus() == Enumeration.TransactionStatus.RELEASED.gettransactionstatus()){
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALINVENTORYRELEASED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			
		}else {

			
		List<String> lstDateField = (List<String>) inputMap.get("DateList");
		final String dtransactiondate = dateUtilityFunction.getCurrentDateTime(userInfo).truncatedTo(ChronoUnit.SECONDS).toString()
				.replace("T", " ").replace("Z", "");

		JSONObject jsonObject = new JSONObject(inputMap.get("materialInventoryJson").toString());
		JSONObject jsonuidata = new JSONObject(inputMap.get("jsonuidata").toString());

		JSONObject jsonObjectTrans = new JSONObject(inputMap.get("MaterialInventoryTrans").toString());
		JSONObject jsonuidataTrans = new JSONObject(inputMap.get("jsonuidataTrans").toString());

		jsonObject.put("Site", new JSONObject(
				"{\"label\":\"" + userInfo.getSsitename() + "\",\"value\":" + userInfo.getNsitecode() + "}"));
		jsonuidata.put("Site", userInfo.getSsitename());
		jsonObjectTrans.put("Site", new JSONObject(
				"{\"label\":\"" + userInfo.getSsitename() + "\",\"value\":" + userInfo.getNsitecode() + "}"));
		jsonuidataTrans.put("Site", userInfo.getSsitename());

		if (!lstDateField.isEmpty()) {
			jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, lstDateField, false, userInfo);
			jsonuidata = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuidata, lstDateField, false, userInfo);
			jsonObjectTrans = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObjectTrans, lstDateField, false, userInfo);
			jsonuidataTrans = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuidataTrans, lstDateField, false, userInfo);
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (jsonObject.has("Received Date & Time")) {
			receiveDate = df.parse(jsonObject.get("Received Date & Time").toString());
			if (receiveDate != null && expiryDate != null) {
				if (receiveDate.compareTo(expiryDate) == 1) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_RECEIVEDATELESSTHANEXPEDATE",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
			if (receiveDate != null && manufDate != null) {
				if (receiveDate.compareTo(manufDate) == -1) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_RECEIVEDATEGREATERTHANMANUFEDATE",
									userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}
		if (jsonObject.has("Expiry Date & Time")) {
			expiryDate = df.parse(jsonObject.get("Expiry Date & Time").toString());
			if (receiveDate != null && expiryDate != null) {
				if (expiryDate.compareTo(receiveDate) == -1) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							"IDS_EXPDATEGREATERTHANRECEIVEDATE", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
			if (expiryDate != null && manufDate != null) {
				if (expiryDate.compareTo(manufDate) == -1) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_EXPDATEGREATERTHANMANUFDATE",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
		}
		if (jsonObject.has("Date Of Manufacturer")) {
			manufDate = df.parse(jsonObject.get("Date Of Manufacturer").toString());
			if (manufDate != null && expiryDate != null) {
				if (manufDate.compareTo(expiryDate) == 1) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MANUFDATELESSTHANEXPEDATE",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
			if (manufDate != null && receiveDate != null) {
				if (manufDate.compareTo(receiveDate) == 1) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							"IDS_MANUFDATELESSTHANRECEIVEEDATE", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
		}
		jsonObject.put("ntransactiontype", Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());
		jsonuidata.put("ntransactiontype", Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());

		jsonAuditOld.put("materialinventory", new JSONArray().put(new JSONObject(jdbcTemplate.queryForObject(
				"select jsonuidata||jsonb_build_object('nmaterialinventorycode',nmaterialinventorycode) as jsonuidata from"
						+ " materialinventory where nmaterialinventorycode=" + inputMap.get("nmaterialinventorycode"),
				String.class))));

		String query1 = " update materialinventory set jsondata='" + stringUtilityFunction.replaceQuote(jsonObject.toString())
				+ "'::jsonb,jsonuidata='" + stringUtilityFunction.replaceQuote(jsonuidata.toString()) + "'::jsonb,nsectioncode=("
				+ inputMap.get("nsectioncode") + ")::int, dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
				+ "' where nmaterialinventorycode in (" + inputMap.get("nmaterialinventorycode") + ");";
		jsonObjectTrans.put("Transaction Date & Time", dtransactiondate);
		jsonObjectTrans.put("ninventorytranscode", Enumeration.TransactionStatus.OUTSIDE.gettransactionstatus());
		jsonObjectTrans.put("nqtyissued", 0);
		String strReceivedQty = jdbcTemplate.queryForObject("select Cast(" + jsonObjectTrans.get("Received Quantity")
				+ " as decimal (" + inputMap.get("sprecision") + "))", String.class);

		jsonObjectTrans.put("namountleft", strReceivedQty);

		jsonuidataTrans.put("Transaction Date & Time", dtransactiondate);
		jsonuidataTrans.put("ninventorytranscode", Enumeration.TransactionStatus.OUTSIDE.gettransactionstatus());
		jsonuidataTrans.put("nqtyissued", 0);
		jsonuidataTrans.put("nmaterialinventtranscode", inputMap.get("nmaterialinventorycode"));
		jsonuidataTrans.put("namountleft", strReceivedQty);
		if ((int) inputMap.get("needsectionwise") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			jsonuidataTrans.put("Description",
					commonFunction.getMultilingualMessage("IDS_RECEIVED", userInfo.getSlanguagefilename()) + " "
							+ commonFunction.getMultilingualMessage("IDS_BY", userInfo.getSlanguagefilename()) + " "
							+ new JSONObject(jsonObjectTrans.get("Section").toString()).get("label"));
		} else {
			jsonuidataTrans.put("Description",
					commonFunction.getMultilingualMessage("IDS_RECEIVED", userInfo.getSlanguagefilename()) + " "
							+ commonFunction.getMultilingualMessage("IDS_BY", userInfo.getSlanguagefilename()) + " "
							+ commonFunction.getMultilingualMessage(userInfo.getSsitename(),
									userInfo.getSlanguagefilename()));
		}
		jsonAuditOld.put("materialinventorytransaction", new JSONArray().put(new JSONObject(jdbcTemplate.queryForObject(
				"select jsonuidata||jsonb_build_object('nmaterialinventorycode',nmaterialinventorycode,'nmaterialinventtranscode',nmaterialinventtranscode) as jsonuidata  from"
						+ " materialinventorytransaction where nmaterialinventorycode="
						+ inputMap.get("nmaterialinventorycode"),
				String.class))));
		query1 += " update materialinventorytransaction set jsondata='" + stringUtilityFunction.replaceQuote(jsonObjectTrans.toString())
				+ "'::jsonb,nsectioncode=" + jsonObjectTrans.get("nsectioncode") + ",nqtyreceived=Cast("
				+ jsonObjectTrans.get("Received Quantity") + " as decimal (" + inputMap.get("sprecision")
				+ "	)),nqtyissued=Cast(" + jsonObjectTrans.get("nqtyissued") + " as decimal ("
				+ inputMap.get("sprecision") + "	)),jsonuidata='" + stringUtilityFunction.replaceQuote(jsonuidataTrans.toString())
				+ "'::jsonb, dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntztransactiondate="
				+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " where nmaterialinventorycode in ("
				+ inputMap.get("nmaterialinventorycode") + ");";
		jdbcTemplate.execute(query1);
		inputMap.put("nflag", Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
		objmap.putAll((Map<String, Object>) getMaterialInventoryByID(inputMap, userInfo).getBody());

		objmap.put("nregtypecode", -1);
		objmap.put("nregsubtypecode", -1);
		objmap.put("ndesigntemplatemappingcode", jsonuidata.get("nmaterialconfigcode"));
		actionType.put("materialinventory", "IDS_EDITMATERIALINVENTORY");
		actionType.put("materialinventorytransaction", "IDS_EDITMATERIALINVENTORYTRANSACTION");
		jsonAuditNew.put("materialinventory", new JSONArray().put(new JSONObject(jsonuidata.toString())));
		jsonAuditNew.put("materialinventorytransaction",
				new JSONArray().put(new JSONObject(jsonuidataTrans.toString())));

		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, objmap, false, userInfo);

		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}
}


	private List<MaterialInventory>checkMaterialStatus(int materialCode) {
		final String strQuery = "select ntransactionstatus from materialinventory where nmaterialinventorycode="+materialCode+"  and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
		return (List<MaterialInventory>) jdbcTemplate.query(strQuery, new MaterialInventory());
		//return (MaterialInventory) jdbcTemplate.query(strQuery, new MaterialInventory());
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteMaterialInventory(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		JSONObject actionType = new JSONObject();
		ObjectMapper objmapper = new ObjectMapper();
		JSONObject jsonAuditOld = new JSONObject();
		Map<String, Object> mapAuditOld = new LinkedHashMap<>();
		StringBuffer sB = new StringBuffer();
		List<Integer> lstcount = new ArrayList<Integer>();
		List<Map<String, Object>> lstMaterialInventory = new ArrayList<Map<String, Object>>();
		lstcount = jdbcTemplate
				.queryForList("select nmaterialinventorycode from materialinventory where nmaterialinventorycode in ("
						+ inputMap.get("nmaterialinventorycode") + ") and nstatus="
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + "", Integer.class);
		if (lstcount.size() == 0) {
			

			int ntransacode = isMaterialInvRetired((int) inputMap.get("nmaterialinventorycode"));

			if (ntransacode != Enumeration.TransactionStatus.RETIRED.gettransactionstatus() && ntransacode != Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {

				jsonAuditOld = new JSONObject(jdbcTemplate.queryForObject(
						"select json_build_object('materialinventory',json_agg(distinct m.jsonuidata||jsonb_build_object('nmaterialinventorycode',m.nmaterialinventorycode)),"
								+ " 'materialinventorytransaction',json_agg(distinct ms.jsonuidata||jsonb_build_object('nmaterialinventtranscode',ms.nmaterialinventtranscode))"
								+ ", 'materialinventoryfile',json_agg(distinct msds.jsondata||jsonb_build_object('nmaterialinventoryfilecode',msds.nmaterialinventoryfilecode)),'materialproperties',json_agg(distinct msds.jsondata))::jsonb"
								+ " from materialinventory m  left outer join"
								+ " materialinventorytransaction ms on m.nmaterialinventorycode=ms.nmaterialinventorycode"
								+ " and m.nstatus=1 " + " and ms.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ms.nsitecode="
								+ userInfo.getNtranssitecode() + " inner join materialinventory me on "
								+ "	me.nmaterialinventorycode=" + inputMap.get("nmaterialinventorycode") + "	and "
								+ " me.nmaterialinventorycode=m.nmaterialinventorycode "
								+ " left outer join materialinventoryfile msds  on "
								+ " m.nmaterialinventorycode=msds.nmaterialinventorycode  ",
						String.class));

				String Qyr = "Select json_agg(jsonuidata||jsonb_build_object('nmaterialinventorycode',nmaterialinventorycode)) from materialinventory where nmaterialinventorycode="
						+ inputMap.get("nmaterialinventorycode");

				String strMaterialInventory = jdbcTemplate.queryForObject(Qyr, String.class);
				if (strMaterialInventory != null)
					lstMaterialInventory = objmapper.convertValue(projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
							strMaterialInventory, userInfo, true, (int) inputMap.get("nmaterialconfigcode"),
							(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? "MaterialInvStandard"
											: (int) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? "MaterialInvVolumetric"
																	: "MaterialInvMaterialInventory"),
							new TypeReference<List<Map<String, Object>>>() {
							});

				jsonAuditOld.put("materialinventory", lstMaterialInventory);

				boolean validRecord = false;

				ValidatorDel objDeleteValidation = projectDAOSupport.validateDeleteRecord(
						Integer.toString((int) inputMap.get("nmaterialinventorycode")), userInfo);
				if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
				if (validRecord) {
					sB.append(" update materialinventory set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialinventorycode in ("
							+ inputMap.get("nmaterialinventorycode") + ");");
					sB.append(" update materialinventorytransaction set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntztransactiondate=" + userInfo.getNtimezonecode()
							+ ", noffsetdtransactiondate=" + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nmaterialinventorycode in (" + inputMap.get("nmaterialinventorycode") + ");");
					sB.append(" update materialinventoryfile set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", ntztransactiondate="
							+ userInfo.getNtimezonecode() + " where nmaterialinventorycode in ("
							+ inputMap.get("nmaterialinventorycode") + ");");
					// Audit For Delete
					inputMap.put("nregtypecode", -1);
					inputMap.put("nregsubtypecode", -1);
					inputMap.put("ndesigntemplatemappingcode", inputMap.get("nmaterialconfigcode"));
					actionType.put("materialinventory", "IDS_DELETEMATERIALINVENTORY");
					actionType.put("materialinventorytransaction", "IDS_DELETEMATERIALINVENTORYTRANSACTION");
					actionType.put("materialinventoryfile", "IDS_DELETEMATERIALINVENTORYFILE");
					mapAuditOld = objmapper.convertValue(jsonAuditOld.toMap(),
							new TypeReference<Map<String, Object>>() {
							});
					jsonAuditOld.put("materialinventory",
							(List<Map<String, Object>>) mapAuditOld.get("materialinventory"));
					jsonAuditOld.put("materialinventorytransaction",
							(List<Map<String, Object>>) mapAuditOld.get("materialinventorytransaction"));
					jsonAuditOld.put("materialinventoryfile",
							(List<Map<String, Object>>) mapAuditOld.get("materialinventoryfile"));
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, null, actionType, inputMap, false, userInfo);
					jdbcTemplate.execute(sB.toString());
					return getMaterialInventoryByID(inputMap, userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYDELETED",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTQUARENTINEINVENTORY",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getMaterialInventoryDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		ObjectMapper objmapper = new ObjectMapper();
		List<Map<String, Object>> lstMaterialInventory = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lstMaterialInventory1 = new ArrayList<Map<String, Object>>();

		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		int needSectionWise = jdbcTemplate.queryForObject("select needsectionwise from materialcategory where "
				+ " nmaterialcatcode=" + inputMap.get("nmaterialcatcode"), Integer.class);
		String nsectioncodestr = "";
		if (needSectionWise == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			nsectioncodestr = "  ( select  " + " s.nsectioncode " + " from " + " sectionusers su, " + " labsection l, "
					+ " SECTION s " + " where su.nlabsectioncode = l.nlabsectioncode "
					+ " AND l.nsectioncode = s.nsectioncode " + " AND su.nsitecode =  " + userInfo.getNtranssitecode()
					+ " AND su.nusercode =   " + userInfo.getNusercode() + " and su.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and l.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) ";
		} else {
			nsectioncodestr = " (" + Enumeration.TransactionStatus.NA.gettransactionstatus() + ") ";
		}

		String query1 = "select json_agg(a.jsonuidata) from  (select mi.nmaterialinventorycode,mi.jsonuidata||json_build_object('nmaterialinventorycode',mi.nmaterialinventorycode ,'status'"
				+ ",(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "')"
				+ ",'Section',s.ssectionname,'Site',si.ssitename )::jsonb " + " "
				+ "as jsonuidata  from materialinventory mi,materialcategory mc,material m,"
				+ " transactionstatus ts,section s,site si  where " + " (mi.jsondata->'nmaterialcode')::int="
				+ inputMap.get("nmaterialcode") + "" + "  and (mi.jsondata->'nmaterialtypecode')::int="
				+ inputMap.get("nmaterialtypecode") + " and  (mi.jsondata->'nmaterialcatcode')::int="
				+ inputMap.get("nmaterialcatcode") + " AND ts.ntranscode = (select   ntransactionstatus "
				+ "					 from  materialinventoryhistory where "
				+ "					 nmaterialinventoryhistorycode=(select   max(nmaterialinventoryhistorycode) "
				+ "					 from  materialinventoryhistory where "
				+ "					 nmaterialinventorycode=mi.nmaterialinventorycode) ) " + " and mi.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND mc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				// +" and mt.nmaterialinventorycode=mi.nmaterialinventorycode"
				+ " and (m.jsondata->'nmaterialcatcode')::INT=mc.nmaterialcatcode"
				+ " and m.nmaterialcode=mi.nmaterialcode" + " and s.nsectioncode=mi.nsectioncode "
				+ " and si.nsitecode=mi.nsitecode " + " and mi.nsectioncode in " + nsectioncodestr
				+ " and mi.nsitecode = " + userInfo.getNtranssitecode() + " order by mi.nmaterialinventorycode)a";

		String strMaterialInventory = jdbcTemplate.queryForObject(query1, String.class);
		if (strMaterialInventory != null)
			lstMaterialInventory = objmapper.convertValue(
					projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterialInventory, userInfo, true,
							(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? 6
											: (int) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? 7 : 8,
							(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? "MaterialInvStandard"
											: (int) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? "MaterialInvVolumetric"
																	: "MaterialInvMaterialInventory"),
					new TypeReference<List<Map<String, Object>>>() {
					});
		objmap.put("MaterialInventory", lstMaterialInventory);
		
		String query2 = "select json_agg(a.jsonuidata) from (select mi.nmaterialinventorycode,mi.jsonuidata||json_build_object('nmaterialinventorycode',mi.nmaterialinventorycode,'status'"
				+ ",(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'),'Available Quantity',mt.jsonuidata->'Available Quantity')::jsonb " + " "
				+ " as jsonuidata  from materialinventory mi,transactionstatus ts,materialinventorytransaction mt where "
				+ " (mi.jsondata->'nmaterialcode')::int=" + inputMap.get("nmaterialcode") + ""
				+ "  and (mi.jsondata->'nmaterialtypecode')::int=" + inputMap.get("nmaterialtypecode")
				+ " and  (mi.jsondata->'nmaterialcatcode')::int=" + inputMap.get("nmaterialcatcode")
				+ " and mt.nmaterialinventorycode=mi.nmaterialinventorycode "
				+ " AND ts.ntranscode = ( mi.jsondata ->> 'ntranscode' ) :: INT  and mi.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mi.nmaterialinventorycode="
				+ inputMap.get("nmaterialinventorycode") + ")a";
		String strMaterialInventory1 = jdbcTemplate.queryForObject(query2, String.class);

		// Commented For JSONUIDATA
		if (strMaterialInventory1 != null)
			lstMaterialInventory1 = objmapper.convertValue(
					projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterialInventory1, userInfo, true,
							(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? 6
											: (int) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? 7 : 8,
							(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? "MaterialInvStandard"
											: (int) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? "MaterialInvVolumetric"
																	: "MaterialInvMaterialInventory"),
					new TypeReference<List<Map<String, Object>>>() {
					});

		// Commented For JSONUIDATA
		if (strMaterialInventory1 != null) {
			objmap.put("SelectedMaterialInventory", lstMaterialInventory1.get(0));
			inputMap.put("nsectioncode", lstMaterialInventory1.get(0).get("nsectioncode"));
			objmap.putAll((Map<String, Object>) getQuantityTransactionByMaterialInvCode(
					(int) inputMap.get("nmaterialinventorycode"), inputMap, userInfo).getBody());
			objmap.putAll(
					(Map<String, Object>) getMaterialFile((int) inputMap.get("nmaterialinventorycode"), userInfo));
			objmap.putAll(
					(Map<String, Object>) getResultUsedMaterial((int) inputMap.get("nmaterialinventorycode"), userInfo)
							.getBody());
			objmap.putAll((Map<String, Object>) getMaterialInventoryhistory(
					(int) inputMap.get("nmaterialinventorycode"), userInfo));
			return new ResponseEntity<>(objmap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateMaterialStatus(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		JSONObject jsonAuditObjectOld = new JSONObject();
		JSONObject jsonAuditObjectnew = new JSONObject();
		JSONArray jsonAuditArrayOld = new JSONArray();
		JSONArray jsonAuditArraynew = new JSONArray();
		JSONObject actionType = new JSONObject();
		String sdisplaystatus;
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		List<String> lstDateField = new ArrayList<>();
		List<Integer> lstcount = new ArrayList<Integer>();
		lstcount = jdbcTemplate
				.queryForList("select nmaterialinventorycode from materialinventory where nmaterialinventorycode in ("
						+ inputMap.get("nmaterialinventorycode") + ") and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "", Integer.class);
		if (lstcount.size() != 0) {

			String jsonstr = jdbcTemplate.queryForObject(
					"select jsondata||jsonb_build_object('nmaterialinventorycode',nmaterialinventorycode) as jsondata from materialinventory "
							+ " where nmaterialinventorycode=" + inputMap.get("nmaterialinventorycode")
							+ " and nstatus="

							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					String.class);
			String jsonUistr = jdbcTemplate.queryForObject(
					"select jsonuidata||jsonb_build_object('nmaterialinventorycode',nmaterialinventorycode) as jsonuidata from materialinventory "
							+ " where nmaterialinventorycode=" + inputMap.get("nmaterialinventorycode")
							+ " and nstatus="

							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					String.class);
			int jsonint = jdbcTemplate.queryForObject("select jsondata->>'ntranscode' from materialinventory "
					+ " where nmaterialinventorycode=" + inputMap.get("nmaterialinventorycode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			JSONObject jsonObject = new JSONObject(jsonstr.toString());
			JSONObject jsonuidata = new JSONObject(jsonUistr.toString());
			inputMap.put("nsectioncode", jsonuidata.get("nsectioncode"));
			if ((int) inputMap.get("nflag") == 1) {
				if (jsonint != Enumeration.TransactionStatus.EXPIRED.gettransactionstatus()) {
					if (jsonint != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
						if (jsonint != Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {
							sdisplaystatus = jdbcTemplate.queryForObject(
									"select jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
											+ "' from" + "  transactionstatus where ntranscode="
											+ Enumeration.TransactionStatus.RELEASED.gettransactionstatus(),
									String.class);
							jsonObject.put("ntranscode", Enumeration.TransactionStatus.RELEASED.gettransactionstatus());
							jsonuidata.put("ntranscode", Enumeration.TransactionStatus.RELEASED.gettransactionstatus());
							jsonuidata.put("sdisplaystatus", sdisplaystatus);
							jsonAuditArrayOld.put(new JSONObject(jdbcTemplate.queryForObject(
									"select jsonuidata||jsonb_build_object('nmaterialinventorycode',nmaterialinventorycode) as jsonuidata from materialinventory "
											+ "where nmaterialinventorycode=" + inputMap.get("nmaterialinventorycode"),
									String.class)));
							jdbcTemplate.execute(
									"update materialinventory set jsondata='" + stringUtilityFunction.replaceQuote(jsonObject.toString())
											+ "',jsonuidata='" + stringUtilityFunction.replaceQuote(jsonuidata.toString())
											+ "',ntransactionstatus=(" + jsonObject.get("ntranscode")
											+ ")::smallint, dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
											+ "' where nmaterialinventorycode=" + inputMap.get("nmaterialinventorycode")
											+ ";");
							inputMap.put("ntranscode", jsonObject.get("ntranscode"));
							createMaterialInventoryhistory(inputMap, userInfo);
							jsonAuditArraynew.put(jsonuidata);
							actionType.put("materialinventory", "IDS_MATERIALINVENTORYRELEASED");
						} else {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYRELEASED",
									userInfo.getSlanguagefilename()), HttpStatus.ALREADY_REPORTED);
						}
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_THEINVENTORYALREADYRETIRED",
										userInfo.getSlanguagefilename()),
								HttpStatus.ALREADY_REPORTED);

					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THEINVENTORYALREADYEXPIRED",
							userInfo.getSlanguagefilename()), HttpStatus.ALREADY_REPORTED);

				}
			} else if ((int) inputMap.get("nflag") == 2) {
				if (jsonint != Enumeration.TransactionStatus.EXPIRED.gettransactionstatus()) {
					if (jsonint != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
						sdisplaystatus = jdbcTemplate.queryForObject("select jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "' from" + "  transactionstatus where ntranscode="
								+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus(), String.class);
						jsonObject.put("ntranscode", Enumeration.TransactionStatus.RETIRED.gettransactionstatus());
						jsonuidata.put("ntranscode", Enumeration.TransactionStatus.RETIRED.gettransactionstatus());
						jsonuidata.put("sdisplaystatus", sdisplaystatus);
						jsonAuditArrayOld.put(
								new JSONObject(jdbcTemplate.queryForObject("select jsonuidata from materialinventory "
										+ "where nmaterialinventorycode=" + inputMap.get("nmaterialinventorycode"),
										String.class)));
						jdbcTemplate
								.execute("update materialinventory set jsondata='"
										+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "',jsonuidata='"
										+ stringUtilityFunction.replaceQuote(jsonuidata.toString()) + "',ntransactionstatus=("
										+ jsonObject.get("ntranscode") + ")::smallint, dmodifieddate ='"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialinventorycode="
										+ inputMap.get("nmaterialinventorycode") + ";");
						inputMap.put("ntranscode", jsonObject.get("ntranscode"));
						createMaterialInventoryhistory(inputMap, userInfo);
						jsonAuditArraynew.put(jsonuidata);
						actionType.put("materialinventory", "IDS_MATERIALINVENTORYRETIRED");
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_THEINVENTORYALREADYRETIRED",
										userInfo.getSlanguagefilename()),
								HttpStatus.ALREADY_REPORTED);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THEINVENTORYALREADYEXPIRED",
							userInfo.getSlanguagefilename()), HttpStatus.ALREADY_REPORTED);

				}
			} else if ((int) inputMap.get("nflag") == 3) {
				if (jsonint != Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus()) {
					if (jsonint != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
							&& jsonint != Enumeration.TransactionStatus.EXPIRED.gettransactionstatus()) {
						if (inputMap.containsKey("Open Date")) {
							jsonObject.put("Open Date", inputMap.get("Open Date"));
							jsonObject.put("tzOpen Date", inputMap.get("tzOpen Date"));

							jsonuidata.put("Open Date", inputMap.get("Open Date"));
							jsonuidata.put("tzOpen Date", inputMap.get("tzOpen Date"));

							lstDateField.add("Open Date");
							jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, lstDateField, false,
									userInfo);
							jsonuidata = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuidata, lstDateField, false,
									userInfo);
							jsonObject = getmaterialquery(jsonObject, 2);
							jsonuidata = getmaterialquery(jsonuidata, 2);
						}
						jsonAuditArrayOld.put(new JSONObject(jdbcTemplate.queryForObject(
								"select jsonuidata||jsonb_build_object('nmaterialinventorycode',nmaterialinventorycode)as jsonuidata from materialinventory "
										+ "where nmaterialinventorycode=" + inputMap.get("nmaterialinventorycode"),
								String.class)));
						jdbcTemplate
								.execute("update materialinventory set jsondata='" + stringUtilityFunction.replaceQuote(jsonObject.toString())
										+ "',jsonuidata='" + stringUtilityFunction.replaceQuote(jsonuidata.toString()) + "', dmodifieddate ='"
										+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialinventorycode="
										+ inputMap.get("nmaterialinventorycode"));
						jsonAuditArraynew.put(jsonuidata);
						actionType.put("materialinventory", "IDS_OPENDATE");
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_PLEASESELECTRELEASEDRECORD",
										userInfo.getSlanguagefilename()),
								HttpStatus.ALREADY_REPORTED);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PLEASESELECTRELEASEDRECORD",
							userInfo.getSlanguagefilename()), HttpStatus.ALREADY_REPORTED);
				}
			}

			DateFormat df = null;
			String str = "";
			if (!jsonAuditArrayOld.getJSONObject(0).optString("Open Date").isEmpty()) {
				str = jsonAuditArrayOld.getJSONObject(0).getString("Open Date");
				df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = df.parse(str);

				jsonAuditArrayOld.getJSONObject(0).put("Open Date",
						new SimpleDateFormat(userInfo.getSsitedate()).format(date));
			}
			if (!jsonAuditArraynew.getJSONObject(0).optString("Open Date").isEmpty()) {
				str = jsonAuditArraynew.getJSONObject(0).getString("Open Date");
				df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = df.parse(str);
				jsonAuditArraynew.getJSONObject(0).put("Open Date",
						new SimpleDateFormat(userInfo.getSsitedate()).format(date));
			}

			objmap.put("nregtypecode", -1);
			objmap.put("nregsubtypecode", -1);
			objmap.put("ndesigntemplatemappingcode", jsonuidata.get("nmaterialconfigcode"));

			jsonAuditObjectOld.put("materialinventory", jsonAuditArrayOld);
			jsonAuditObjectnew.put("materialinventory", jsonAuditArraynew);
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, jsonAuditObjectnew, actionType, objmap, false, userInfo);

			objmap.putAll((Map<String, Object>) getMaterialInventoryByID(inputMap, userInfo).getBody());
			return new ResponseEntity<>(objmap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALINVENTORYALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	@SuppressWarnings({ "unchecked" })
	public ResponseEntity<Object> getMaterialInventorySearchByID(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		ObjectMapper objmapper = new ObjectMapper();
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstMaterialInventory = new ArrayList<Map<String, Object>>();
		;
		final String query1 = "select json_agg(a.jsonuidata) from (select mi.nmaterialinventorycode,mi.jsonuidata||json_build_object('nmaterialinventorycode',mi.nmaterialinventorycode"
				+ " ,'status'" + "	,(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'),'Available Quantity',mt.jsondata->'Available Quantity')::jsonb " + " as jsonuidata "
				+ " from materialinventory mi,transactionstatus ts,materialinventorytransaction mt"
				+ "  where  mi.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND ts.ntranscode = ( mi.jsondata ->> 'ntranscode' ) :: INT " + " and mi.nmaterialinventorycode="
				+ inputMap.get("nmaterialinventorycode") + " and mi.nmaterialinventorycode=mt.nmaterialinventorycode)a";

		String strMaterialInventory = jdbcTemplate.queryForObject(query1, String.class);
		if (strMaterialInventory != null)
		{
			lstMaterialInventory = objmapper.convertValue(
					projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterialInventory, userInfo, true,
							(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? 6
											: (int) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? 7 : 8,
							(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? "MaterialInvStandard"
											: (int) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? "MaterialInvVolumetric"
																	: "MaterialInvMaterialInventory"),
					new TypeReference<List<Map<String, Object>>>() {
					});
		// Commented For JSONUIDATA
		objmap.put("SelectedMaterialInventory", lstMaterialInventory.get(lstMaterialInventory.size() - 1));
		inputMap.put("nsectioncode", lstMaterialInventory.get(lstMaterialInventory.size() - 1).get("nsectioncode"));
		objmap.putAll((Map<String, Object>) getQuantityTransactionByMaterialInvCode(
				(int) inputMap.get("nmaterialinventorycode"), inputMap, userInfo).getBody());
		objmap.putAll((Map<String, Object>) getMaterialFile((int) inputMap.get("nmaterialinventorycode"), userInfo));
		objmap.putAll(
				(Map<String, Object>) getResultUsedMaterial((int) inputMap.get("nmaterialinventorycode"), userInfo)
						.getBody());
		objmap.putAll((Map<String, Object>) getMaterialInventoryhistory((int) inputMap.get("nmaterialinventorycode"),
				userInfo));
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	
		}
		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
	}
}

	public Object getPrecision() {
		String str = "select ssettingstring from settings where nsettingid=47 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(str, Integer.class);
	}

	public ResponseEntity<Object> getQuantityTransactionTemplate(final UserInfo userInfo) throws Exception {
		String siteLabelName = "";
		String sprecision = "";
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		String templateGet = "SELECT jsondata from materialconfig  where   nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nformcode="
				+ userInfo.getNformcode() + " and  nmaterialtypecode="
				+ Enumeration.TransactionStatus.NA.gettransactionstatus();
		//List<MaterialConfig> lstMaterialConfig = jdbcTemplate.query(templateGet, new MaterialConfig());
		JSONArray Layout = new JSONArray(jdbcTemplate.queryForObject(templateGet, String.class));
		for (int L = 0; L < Layout.length(); L++) {
			JSONObject row = new JSONObject(Layout.get(L).toString());
			if (row.has("children")) {
				JSONArray column = new JSONArray(row.get("children").toString());
				for (int c = 0; c < column.length(); c++) {
					JSONObject component = new JSONObject(column.get(c).toString());
					if (component.has("children")) {
						JSONArray maincomponent = new JSONArray(component.get("children").toString());
						for (int m = 0; m < maincomponent.length(); m++) {
							JSONObject feilds = new JSONObject(maincomponent.get(m).toString());
							if (feilds.has("source")) {
								if (feilds.get("source").equals("site")) {
									siteLabelName = (String) feilds.get("label");
								}
							} else if (feilds.has("nprecision")) {
								sprecision = feilds.getString("nprecision");
							}
						}
					}
				}
			}
		}
		if (siteLabelName != "") {
			String siteName = jdbcTemplate.queryForObject(
					"select ssitename from site where nsitecode=" + userInfo.getNsitecode(), String.class);
			objmap.put("siteName", siteName);
			objmap.put("siteLabelName", siteLabelName);
		}
		objmap.put("siteLabelName", siteLabelName);
		objmap.put("sprecision", sprecision);
		objmap.put("nprecision", sprecision.substring(sprecision.indexOf(",") + 1));
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked" })
	public ResponseEntity<Object> getQuantityTransaction(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		Map<String, Object> paramObj = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstMap = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lstMapparent = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lstMapForParentCombo = new ArrayList<Map<String, Object>>();
		String filterQueryComponents = "";
		String concatstr = "";
		String sprecision = "";
		String QtyTransactionpopup = "";
		int ntransactiontype = 0;
		String templateGet = "SELECT jsondata from materialconfig  where   nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nformcode="
				+ userInfo.getNformcode() + " and  nmaterialtypecode="
				+ Enumeration.TransactionStatus.NA.gettransactionstatus();
		List<MaterialConfig> lstMaterialConfig = jdbcTemplate.query(templateGet, new MaterialConfig());
		String sourceSection = jdbcTemplate.queryForObject("select  " + "string_agg((s.nsectioncode)::text, ',') "
				+ " from   " + " sectionusers su, " + " labsection l, " + " SECTION s "
				+ " where su.nlabsectioncode = l.nlabsectioncode " + " AND l.nsectioncode = s.nsectioncode "
				+ " AND su.nsitecode =" + userInfo.getNtranssitecode() + " AND su.nusercode =" + userInfo.getNusercode()
				+ " and su.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and l.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), String.class);
		if ((int) inputMap.get("nflag") == 1) {
			int statusCheck = jdbcTemplate.queryForObject(
					"select (jsondata->>'ntranscode')::int from materialinventory where nmaterialinventorycode="
							+ inputMap.get("nmaterialinventorycode"),
					Integer.class);
			if (statusCheck == Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {
				JSONArray Layout = new JSONArray(jdbcTemplate.queryForObject(templateGet, String.class));
				for (int L = 0; L < Layout.length(); L++) {
					JSONObject row = new JSONObject(Layout.get(L).toString());
					if (row.has("children")) {
						JSONArray column = new JSONArray(row.get("children").toString());
						for (int c = 0; c < column.length(); c++) {
							JSONObject component = new JSONObject(column.get(c).toString());
							if (component.has("children")) {
								JSONArray maincomponent = new JSONArray(component.get("children").toString());
								for (int m = 0; m < maincomponent.length(); m++) {
									JSONObject feilds = new JSONObject(maincomponent.get(m).toString());
									if (feilds.has("parent")) {
										if ((boolean) feilds.get("parent") == true) {
											// map.put((String) feilds.get("label"), feilds);
											lstMap.add(feilds.toMap());
										} else if ((boolean) feilds.get("parent") == false) {
											lstMapForParentCombo.add(feilds.toMap());
										}
									}
									if (feilds.has("nprecision")) {
										sprecision = feilds.getString("nprecision");
									}
									if (feilds.has("nsqlquerycode")) {
										// filterQueryComponents.add(m, feilds.get("nsqlquerycode").toString());
										filterQueryComponents += feilds.get("nsqlquerycode").toString() + ',';
									}
								}
							}
						}
					}
				}

				List<MappedTemplateFieldPropsMaterial> lstsearchField = new LinkedList<>();
				List<String> lstsearchFields = new LinkedList<>();
				
				String selectionKeyName1 = "select (jsondata->'" + userInfo.getNformcode()
						+ "'->'QuantityTransactiondatefields')::jsonb as jsondata from mappedtemplatefieldpropsmaterial  where   nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialconfigcode=9";
				
				lstsearchField = jdbcTemplate.query(selectionKeyName1, new MappedTemplateFieldPropsMaterial());
				
				JSONArray objarray = new JSONArray(lstsearchField.get(0).getJsondata());
				for (int i = 0; i < objarray.length(); i++) {
					JSONObject jsonobject = new JSONObject(objarray.get(i).toString());
					if (jsonobject.has("2")) {
						lstsearchFields.add((String) jsonobject.get("2"));
					}
				}
				objmap.put("DateFeildsProperties", lstsearchFields);
				
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTRELEASEDINVENTORY",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		}
		if ((int) inputMap.get("nflag") == 1) {
			if (inputMap.get("needsectionwise").equals(Enumeration.TransactionStatus.YES.gettransactionstatus())) {
				concatstr = " and mt.nsectioncode in (" + sourceSection + ")";
			} else {
				concatstr = " and mt.nsectioncode in (" + Enumeration.TransactionStatus.NA.gettransactionstatus() + ")";
			}
			QtyTransactionpopup = jdbcTemplate
					.queryForObject("select  (COALESCE(Cast(Sum(mt.nqtyreceived)as decimal (" + sprecision + ")), 0) "
							+ " -COALESCE(Sum(mt.nqtyissued), 0) )::text  "
							+ " as navailableqty from materialinventorytransaction mt,materialinventory mi "
							+ " where mi.nmaterialinventorycode = mt.nmaterialinventorycode and  mt.nmaterialinventorycode = "
							+ inputMap.get("nmaterialinventorycode") + " " + " and mi.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mt.nstatus= "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " and ((mi.jsondata->'ntranscode')::int != "
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + concatstr
							+ "	and mt.nsitecode=" + userInfo.getNtranssitecode()
							+ " and (mi.jsondata->'ntranscode')::int != "
							+ Enumeration.TransactionStatus.EXPIRED.gettransactionstatus() + ")"
							+ " and (mi.jsondata->'ntransactiontype')::int !="
							+ Enumeration.TransactionStatus.USED.gettransactionstatus() + "; ", String.class);

			objmap.put("navailableqty", QtyTransactionpopup);

			paramObj.put("nmaterialinventorycode", inputMap.get("nmaterialinventorycode"));
			paramObj.put("nsitecode", userInfo.getNtranssitecode());
			paramObj.put("nusercode", userInfo.getNusercode());
			objmap.put("filterQueryComponents", filterQueryComponents.substring(0, filterQueryComponents.length() - 1));
			objmap.put("parameters", paramObj);
			for (int i = 0; i < lstMapForParentCombo.size(); i++) {
				lstMapparent.add(lstMap.get(i));
				objmap.put("parentcolumnlist", lstMapparent);
				objmap.put("childcolumnlist", lstMapForParentCombo.get(i));
				objmap.putAll((Map<String, Object>) getComboValuesMaterial(objmap, userInfo).getBody());
				lstMapparent.clear();
			}
			objmap.put("childcolumnlist", lstMapForParentCombo);
			objmap.put("parentcolumnlist", lstMap);
			objmap.put("nprecision", sprecision.substring(sprecision.indexOf(",") + 1));
			objmap.put("QuantityTransactionTemplate", lstMaterialConfig);
		} else if ((int) inputMap.get("nflag") == 2) {
			String transtypechange = "";
			if (inputMap.containsKey("ntransactiontype")) {
				ntransactiontype = (int) inputMap.get("ntransactiontype");
			}

			if (ntransactiontype == Enumeration.TransactionStatus.ISSUE.gettransactionstatus()) {
				if (inputMap.get("needsectionwise").equals(Enumeration.TransactionStatus.YES.gettransactionstatus())) {
					concatstr = " and mt.nsectioncode in (" + sourceSection + ")";
				} else {
					concatstr = " and mt.nsectioncode in (" + Enumeration.TransactionStatus.NA.gettransactionstatus()
							+ ")";
				}
				transtypechange = jdbcTemplate.queryForObject(
						"select  (COALESCE(Cast(Sum(mt.nqtyreceived)as decimal (" + inputMap.get("sprecision")
								+ ")), 0) " + " -COALESCE(Cast(Sum(mt.nqtyissued)as decimal ("
								+ inputMap.get("sprecision") + ")), 0))::text"
								+ " as navailableqty from materialinventorytransaction mt,materialinventory mi "
								+ " where mi.nmaterialinventorycode = mt.nmaterialinventorycode and  mt.nmaterialinventorycode = "
								+ inputMap.get("nmaterialinventorycode") + " " + " and mi.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mt.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and ((mi.jsondata->'ntranscode')::int != "
								+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + concatstr
								+ "	and mt.nsitecode=" + userInfo.getNtranssitecode()
								+ " and (mi.jsondata->'ntranscode')::int != "
								+ Enumeration.TransactionStatus.EXPIRED.gettransactionstatus() + ")"
								+ " and (mi.jsondata->'ntransactiontype')::int !="
								+ Enumeration.TransactionStatus.USED.gettransactionstatus() + "; ",
						String.class);
			} else if (ntransactiontype == Enumeration.TransactionStatus.RETURN.gettransactionstatus()) {
			
				concatstr = " and mt.nsectioncode in (" + inputMap.get("sourceSection") + ")";

				transtypechange = jdbcTemplate.queryForObject(
						"select  (COALESCE(Cast(Sum(mt.nqtyreceived) as decimal (" + inputMap.get("sprecision")
								+ ")), 0 ) " + " - COALESCE(Cast(Sum(mt.nqtyissued)  as decimal ("
								+ inputMap.get("sprecision") + ")), 0) )::text "
								+ " AS navailableqty from materialinventorytransaction mt,materialinventory mi "
								+ " where mi.nmaterialinventorycode = mt.nmaterialinventorycode and  (mt.jsondata->>'nmaterialcode')::int = "
								+ " (select nmaterialcode from materialinventory where nmaterialinventorycode in ("
								+ inputMap.get("nmaterialinventorycode") + ")) "
								+ " and (mt.jsondata->'ntransactiontype')::int in ("
								+ Enumeration.TransactionStatus.ISSUE.gettransactionstatus() + ", "
								+ Enumeration.TransactionStatus.RETURN.gettransactionstatus() + ", "
								+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")"
								+ " and mi.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and mt.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and (mi.jsondata->'ntranscode')::int != "
								+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + concatstr
								// + " and mt.nsitecode=" + inputMap.get("nsitecode")
								+ "	and mt.nsitecode=" + inputMap.get("nsitecode")
								+ " and (mi.jsondata->'ntranscode')::int != "
								+ Enumeration.TransactionStatus.EXPIRED.gettransactionstatus() + ""
								+ " and (mt.jsondata->'ntransactiontype')::int !="
								+ Enumeration.TransactionStatus.USED.gettransactionstatus() + "; ",
						String.class);
			}
			objmap.put("navailableqty", transtypechange);
		} else if ((int) inputMap.get("nflag") == 3) {
			String transtypechange = "";
			if (inputMap.containsKey("ntransactiontype")) {
				ntransactiontype = (int) inputMap.get("ntransactiontype");
			}
			if (ntransactiontype == Enumeration.TransactionStatus.REJECTED.gettransactionstatus()) {
				if (inputMap.get("needsectionwise").equals(Enumeration.TransactionStatus.YES.gettransactionstatus())) {
					concatstr = " and mt.nsectioncode in (" + sourceSection + ")";
				} else {
					concatstr = " and mt.nsectioncode in (" + Enumeration.TransactionStatus.NA.gettransactionstatus()
							+ ")";
				}
				transtypechange = jdbcTemplate.queryForObject(
						"select  (COALESCE(Cast(Sum(mt.nqtyreceived)as decimal (" + inputMap.get("sprecision")
								+ ")), 0)" + "	 -COALESCE(Cast(Sum(mt.nqtyissued)as decimal ("
								+ inputMap.get("sprecision") + ")), 0) )::text"
								+ " as navailableqty from materialinventorytransaction mt,materialinventory mi "
								+ " where mi.nmaterialinventorycode = mt.nmaterialinventorycode and  mt.nmaterialinventorycode = "
								+ inputMap.get("nmaterialinventorycode") + " " + " and mi.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mt.nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
								+ " and ((mi.jsondata->'ntranscode')::int != "
								+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + concatstr
								+ "	and mt.nsitecode=" + userInfo.getNtranssitecode()
								+ " and (mi.jsondata->'ntranscode')::int != "
								+ Enumeration.TransactionStatus.EXPIRED.gettransactionstatus() + ")"
								+ " and (mt.jsondata->'ntransactiontype')::int !="
								+ Enumeration.TransactionStatus.USED.gettransactionstatus() + "; ",
						String.class);
				objmap.put("navailableqty", transtypechange);

			}
			transtypechange = jdbcTemplate.queryForObject("select  (COALESCE(Cast(Sum(mt.nqtyreceived)as decimal ("
					+ inputMap.get("sprecision") + ")), 0)" + "	 -COALESCE(Cast(Sum(mt.nqtyissued)as decimal ("
					+ inputMap.get("sprecision") + ")), 0) )::text"
					+ " as navailableqty from materialinventorytransaction mt,materialinventory mi "
					+ " where mi.nmaterialinventorycode = mt.nmaterialinventorycode and  mt.nmaterialinventorycode = "
					+ inputMap.get("nmaterialinventorycode") + " " + " and mi.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mt.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and ((mi.jsondata->'ntranscode')::int != "
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " and mt.nsectioncode in ("
					+ sourceSection + ")	and mt.nsitecode=" + inputMap.get("nsitecode")
					+ " and (mi.jsondata->'ntranscode')::int != "
					+ Enumeration.TransactionStatus.EXPIRED.gettransactionstatus() + ")"
					+ " and (mt.jsondata->'ntransactiontype')::int !="
					+ Enumeration.TransactionStatus.USED.gettransactionstatus() + "; ", String.class);
			objmap.put("navailableqtyref", transtypechange);
		}

		objmap.put("sourceSection", sourceSection);
		objmap.put("QuantityTransactionTemplate", lstMaterialConfig);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}


	@Override
	public ResponseEntity<Object> getQuantityTransactionByMaterialInvCode(int nmaterialinventorycode,
			Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		ObjectMapper Objmapper = new ObjectMapper();
		List<Map<String, Object>> lstMaterialInventoryTrans = new ArrayList<Map<String, Object>>();
		//List<String> submittedDateFeilds = new LinkedList<String>();
		String overallavailableqty = "";
		String allavailableqty = "";
		String concatstrsection = "";
		
		JSONObject jsonobj = new JSONObject(jdbcTemplate.queryForObject(
				"select jsonuidata  from materialinventorytransaction " + " where nmaterialinventorycode="
						+ nmaterialinventorycode + " fetch first 1 rows only ",
				String.class));
		String sprecision = jsonobj.get("sprecision").toString();

		concatstrsection = "  mt.nsectioncode in (select nsectioncode from materialinventory where nmaterialinventorycode="
				+ nmaterialinventorycode + " ) and ";

		overallavailableqty = jdbcTemplate.queryForObject(" select ( Coalesce(CAST(Sum(mt.nqtyreceived)AS DECIMAL ("
				+ sprecision + ")), 0) - " + "  Coalesce( " + "  CAST(Sum(mt.nqtyissued)AS DECIMAL (" + sprecision
				+ ")), 0) ) from materialinventorytransaction mt where   (mt.jsondata->>'nmaterialcode')::int in ("
				+ " (select nmaterialcode from materialinventory where nmaterialinventorycode=" + nmaterialinventorycode
				+ "  ))" + " and mt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				, String.class);

		allavailableqty = jdbcTemplate.queryForObject(" select ( Coalesce(CAST(Sum(mt.nqtyreceived)AS DECIMAL ("
				+ sprecision + ")), 0) - " + "  Coalesce( " + "  CAST(Sum(mt.nqtyissued)AS DECIMAL (" + sprecision
				+ ")), 0) ) from materialinventorytransaction mt where  " + concatstrsection
				+ "	  mt.nmaterialinventorycode=" + nmaterialinventorycode + " and mt.nsitecode="
				+ userInfo.getNtranssitecode(), String.class);

		final String query1 = "select json_agg(a.jsonuidata) from ( select mt.nmaterialinventtranscode,"
				+ " mt.nmaterialinventorycode,mt.jsonuidata||json_build_object('nmaterialinventtranscode',"
				+ "	 mt.nmaterialinventtranscode," + "	 'Inventory ID',mi.jsonuidata->'Inventory ID'"
				+ " ,'Available Quantity', " + allavailableqty + ",'Overall Available Quantity'," + overallavailableqty
				+ ",'Issued Quantity',(mt.nqtyissued)::text,'Received Quantity'"
				+ " ,(mt.nqtyreceived)::text,'Section',s.ssectionname )"
				+ " ::jsonb jsonuidata  from  materialinventorytransaction mt,materialinventory mi, SECTION s"
				+ "  where " + " mi.nmaterialinventorycode=mt.nmaterialinventorycode"
				+ " and s.nsectioncode=mi.nsectioncode " + " and mt.nmaterialinventorycode=" + nmaterialinventorycode
				+ " and mt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() // + " and
																									// mt.nsitecode="+userInfo.getNtranssitecode()
				+ " and mi.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ")a";

		String strMaterialInventoryTrans = jdbcTemplate.queryForObject(query1, String.class);
		
		// Commented For JSONUIDATA
		if (strMaterialInventoryTrans != null)

			lstMaterialInventoryTrans = Objmapper
					.convertValue(projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterialInventoryTrans, userInfo, true, 9,
							"QuantityTransaction"), new TypeReference<List<Map<String, Object>>>() {
							});
		// Commented For JSONUIDATA
		String templateGet = "SELECT jsondata from materialconfig  where   nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nformcode="
				+ userInfo.getNformcode() + " and  nmaterialtypecode="
				+ Enumeration.TransactionStatus.NA.gettransactionstatus();
		List<MaterialConfig> lstMaterialConfig = jdbcTemplate.query(templateGet, new MaterialConfig());
		objmap.put("QuantityTransactionTemplate", lstMaterialConfig);
		objmap.put("MaterialInventoryTrans", lstMaterialInventoryTrans);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "removal" })
	@Override

	public ResponseEntity<Object> createMaterialInventoryTrans(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String sTableLockQuery = " lock  table lockmaterialinventory "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sTableLockQuery);

		JSONObject jsonAuditObject = new JSONObject();
		JSONArray jsonuidataArray = new JSONArray();
		JSONObject actionType = new JSONObject();
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		ObjectMapper Objmapper = new ObjectMapper();
		JSONObject json = new JSONObject();
		String sectionDescription = "";
		String InsertStr = "";
		int sourcesection = -1;
		List<String> lstDateField = (List<String>) inputMap.get("DateList");
		final String dtransactiondate = dateUtilityFunction.getCurrentDateTime(userInfo).truncatedTo(ChronoUnit.SECONDS).toString()
				.replace("T", " ").replace("Z", "");
		List<MaterialInventoryTrans> lstsourceSection = new ArrayList<>();
		JSONObject insJsonObj = new JSONObject(inputMap.get("MaterialInventoryTrans").toString());

		JSONObject jsonuidata = new JSONObject(inputMap.get("jsonuidata").toString());

		insJsonObj.put("Transaction Date & Time", dtransactiondate);
		insJsonObj.put("noffsetTransaction Date & Time", dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));
		jsonuidata.put("Transaction Date & Time", dtransactiondate);
		jsonuidata.put("noffsetTransaction Date & Time", dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));

		objmap.putAll((Map<String, Object>) getQuantityTransactionTemplate(userInfo).getBody());
		insJsonObj.put("sprecision", objmap.get("sprecision"));
		jsonuidata.put("sprecision", objmap.get("sprecision"));
		insJsonObj.put("needsectionwise", inputMap.get("needsectionwise"));
		jsonuidata.put("needsectionwise", inputMap.get("needsectionwise"));
		if ((int) insJsonObj.get("ntransactiontype") == Enumeration.TransactionStatus.ISSUE.gettransactionstatus()
				|| (int) insJsonObj.get("ntransactiontype") == Enumeration.TransactionStatus.REJECTED
						.gettransactionstatus()) {

			BigDecimal bigDecimalava = new BigDecimal(insJsonObj.get("navailableqty").toString());
			BigDecimal bigDecimalrec = new BigDecimal(insJsonObj.get("Received Quantity").toString());
			double namountleft = (insJsonObj.get("navailableqty") instanceof String
					? Double.parseDouble((String) insJsonObj.get("navailableqty"))
					: Double.valueOf((Integer) bigDecimalava.intValue()))
					- (insJsonObj.get("Received Quantity") instanceof String
							? Double.parseDouble((String) insJsonObj.get("Received Quantity"))
							: Double.valueOf((Integer) bigDecimalrec.intValue()));

			String amtleft = new Double(namountleft).toString();
			insJsonObj.put("namountleft", jdbcTemplate.queryForObject(
					"select Cast(" + amtleft + " as decimal (" + objmap.get("sprecision") + "))", String.class));
			jsonuidata.put("namountleft", jdbcTemplate.queryForObject(
					"select Cast(" + amtleft + " as decimal (" + objmap.get("sprecision") + "))", String.class));
		} else if ((int) insJsonObj.get("ntransactiontype") == Enumeration.TransactionStatus.RETURN
				.gettransactionstatus()) {
			String amntgetqry = "select jsondata->>'namountleft' from materialinventorytransaction where nmaterialinventorycode="
					+ inputMap.get("nmaterialinventorycode")
					+ " and nstatus=1 order by nmaterialinventtranscode desc fetch first 1 rows only";
			double namountleft = jdbcTemplate.queryForObject(amntgetqry, Double.class)
					+ Double.parseDouble((String) insJsonObj.get("Received Quantity"));
			String amtleft = new Double(namountleft).toString();
			amtleft = jdbcTemplate.queryForObject(
					"select Cast(" + amtleft + " as decimal (" + objmap.get("sprecision") + "))", String.class);
			insJsonObj.put("namountleft", amtleft);
			jsonuidata.put("namountleft", amtleft);
		} else {
			double availqty = Double.parseDouble((String) insJsonObj.get("navailableqty"));
			double namountleft = availqty + Double.parseDouble((String) insJsonObj.get("Received Quantity"));
			String strnamountleft = jdbcTemplate.queryForObject(
					"select Cast(" + namountleft + " as decimal (" + objmap.get("sprecision") + "))", String.class);
			insJsonObj.put("namountleft", strnamountleft);
			jsonuidata.put("namountleft", strnamountleft);
		}

		if ((int) insJsonObj.get("ntransactiontype") == Enumeration.TransactionStatus.ISSUE.gettransactionstatus()
				|| (int) insJsonObj.get("ntransactiontype") == Enumeration.TransactionStatus.REJECTED
						.gettransactionstatus()) {
			if (insJsonObj.get("Received Quantity").toString().contains(".")) {
				insJsonObj.put("nqtyissued", Double.parseDouble(insJsonObj.get("Received Quantity").toString()));
				jsonuidata.put("nqtyissued", Double.parseDouble(insJsonObj.get("Received Quantity").toString()));
			} else {
				insJsonObj.put("nqtyissued", Integer.parseInt(insJsonObj.get("Received Quantity").toString()));
				jsonuidata.put("nqtyissued", Integer.parseInt(insJsonObj.get("Received Quantity").toString()));
			}
			insJsonObj.put("Received Quantity", 0);
			jsonuidata.put("Received Quantity", 0);
		} else if ((int) insJsonObj.get("ntransactiontype") == Enumeration.TransactionStatus.RECEIVED
				.gettransactionstatus()
				|| (int) insJsonObj.get("ntransactiontype") == Enumeration.TransactionStatus.RETURN
						.gettransactionstatus()) {
			if (insJsonObj.get("Received Quantity").toString().contains(".")) {
				insJsonObj.put("Received Quantity", Double.parseDouble(insJsonObj.get("Received Quantity").toString()));
				jsonuidata.put("Received Quantity", Double.parseDouble(jsonuidata.get("Received Quantity").toString()));
			} else {
				insJsonObj.put("Received Quantity", Integer.parseInt(insJsonObj.get("Received Quantity").toString()));
				jsonuidata.put("Received Quantity", Integer.parseInt(jsonuidata.get("Received Quantity").toString()));
			}
			insJsonObj.put("nqtyissued", 0);
			jsonuidata.put("nqtyissued", 0);

		}
		insJsonObj = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(insJsonObj, lstDateField, false, userInfo);
		jsonuidata = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuidata, lstDateField, false, userInfo);

		int matInvseq = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnomaterialmanagement where stablename= 'materialinventory'", Integer.class);

		int matseq = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnomaterialmanagement where stablename= 'materialinventorytransaction'",
				Integer.class);
		matseq++;
		lstsourceSection = jdbcTemplate
				.query("select s.nsectioncode,mt.jsondata from materialinventorytransaction mt,sectionusers su, "
						+ " labsection l, " + "SECTION s  " + " where mt.nmaterialinventorycode="
						+ inputMap.get("nmaterialinventorycode") + "  and " + "su.nlabsectioncode = l.nlabsectioncode "
						+ " AND l.nsectioncode = s.nsectioncode " + " and s.nsectioncode=mt.nsectioncode "
						+ " AND su.nsitecode =  " + userInfo.getNtranssitecode() + " AND su.nusercode =  "
						+ userInfo.getNusercode() + " and su.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and l.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by mt.nmaterialinventtranscode", new MaterialInventoryTrans());
		JSONObject insJsonObjcopy = new JSONObject(insJsonObj.toString());
		JSONObject jsonUidataObjcopy = new JSONObject(jsonuidata.toString());

		insJsonObjcopy.put("Transaction Date & Time", dtransactiondate);
		insJsonObjcopy.put("noffsetTransaction Date & Time", dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));
		jsonUidataObjcopy.put("Transaction Date & Time", dtransactiondate);
		jsonUidataObjcopy.put("noffsetTransaction Date & Time", dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()));

		if ((int) inputMap.get("needsectionwise") == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			insJsonObjcopy.put("Section", new JSONObject("{\"label\":\"Default\",\"value\":-1}"));
			jsonUidataObjcopy.put("Section", "Default");
			jsonUidataObjcopy.put("Site", userInfo.getSsitename());
			sectionDescription = userInfo.getSsitename() + " ("
					+ commonFunction.getMultilingualMessage("IDS_NA", userInfo.getSlanguagefilename()) + ") ";

		} else {
			insJsonObjcopy.put("Section",
					new JSONObject(Objmapper.writeValueAsString(lstsourceSection.get(0).getJsondata().get("Section"))));
			jsonUidataObjcopy.put("Section",
					new JSONObject(Objmapper.writeValueAsString(lstsourceSection.get(0).getJsondata().get("Section")))
							.get("label"));
			jsonUidataObjcopy.put("Site", userInfo.getSsitename());
			sectionDescription = commonFunction.getMultilingualMessage(userInfo.getSsitename(),
					userInfo.getSlanguagefilename()) + " ("
					+ new JSONObject(Objmapper.writeValueAsString(lstsourceSection.get(0).getJsondata().get("Section")))
							.get("label").toString()
					+ ") ";
			sourcesection = lstsourceSection.get(0).getNsectioncode();

		}
		if ((int) insJsonObj.get("ninventorytranscode") == Enumeration.TransactionStatus.INHOUSE
				.gettransactionstatus()) {
			json.put("label", "Received");
			json.put("value", Enumeration.TransactionStatus.RECEIVED.gettransactionstatus());
			String sectionstr = "";
			if (insJsonObj.get("Section").getClass().toString().equals("class org.json.JSONObject")) {
				sectionstr = new JSONObject(insJsonObj.get("Section").toString()).get("label").toString();
			} else {
				sectionstr = insJsonObj.get("Section").toString();
			}

			sectionstr = new JSONObject(insJsonObj.get("Site").toString()).get("label").toString() + " (" + sectionstr
					+ ") ";
			if ((int) insJsonObj.get("ntransactiontype") == Enumeration.TransactionStatus.ISSUE
					.gettransactionstatus()) {

				jsonUidataObjcopy.put("Description",
						new JSONObject(insJsonObjcopy.get("Transaction Type").toString()).get("label") + " "
								+ commonFunction.getMultilingualMessage("IDS_FROM", userInfo.getSlanguagefilename())
								+ " " + sectionDescription + " "
								+ commonFunction.getMultilingualMessage("IDS_TO", userInfo.getSlanguagefilename()) + " "
								+ sectionstr);
				jsonuidata.put("Description",
						commonFunction.getMultilingualMessage("IDS_RECEIVED", userInfo.getSlanguagefilename()) + " "
								+ commonFunction.getMultilingualMessage("IDS_BY", userInfo.getSlanguagefilename()) + " "
								+ sectionstr + " "
								+ commonFunction.getMultilingualMessage("IDS_FROM", userInfo.getSlanguagefilename())
								+ " " + sectionDescription);
				jsonUidataObjcopy.put("nmaterialinventtranscode", matseq);
				jsonuidata.put("nmaterialinventtranscode", matseq);

				/** A section should need to have only one Inventory ID **/
				String validationStr = "";
				if ((int) inputMap.get("needsectionwise") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

					validationStr = "select * from materialinventory where nsitecode="
							+ ((JSONObject) insJsonObj.get("Site")).get("value") + " and nsectioncode="
							+ insJsonObj.get("nsectioncode") + " and (jsondata->>'Inventory ID')::text='"
							+ insJsonObj.get("Inventory ID") + "'";
				} else {

					validationStr = "select * from materialinventory where nsitecode="
							+ ((JSONObject) insJsonObj.get("Site")).get("value")
							+ " and (jsondata->>'Inventory ID')::text='" + insJsonObj.get("Inventory ID") + "'";
				}
				List<MaterialInventory> validationLst = jdbcTemplate.query(validationStr, new MaterialInventory());
				int destinationInventorycode = 0;

				if (validationLst.size() == 0) {
					matInvseq++;
					InsertStr = " INSERT INTO materialinventory( nmaterialinventorycode,nmaterialcode,ntransactionstatus,"
							+ " nsectioncode,jsondata,jsonuidata, dmodifieddate, nsitecode, nstatus)" + "select "
							+ matInvseq + ",nmaterialcode,ntransactionstatus," + insJsonObj.get("nsectioncode")
							+ ",jsondata,jsonuidata,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ ((JSONObject) insJsonObj.get("Site")).get("value") + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from materialinventory where nmaterialinventorycode="
							+ inputMap.get("nmaterialinventorycode") + ";";

					jdbcTemplate.execute("update seqnomaterialmanagement set nsequenceno=" + (matInvseq)
							+ "  where stablename='materialinventory' ;");

					Map<String, Object> inputhistoryMap = new HashMap<String, Object>();
					inputhistoryMap.put("ntranscode", Enumeration.TransactionStatus.RELEASED.gettransactionstatus());
					inputhistoryMap.put("nmaterialcode", insJsonObj.get("nmaterialcode"));
					inputhistoryMap.put("nmaterialinventorycode", matInvseq);
					createMaterialInventoryhistory(inputhistoryMap, userInfo);
				}
				if (validationLst.size() > 0) {
					destinationInventorycode = ((MaterialInventory) validationLst.get(0)).getNmaterialinventorycode();
				}
				
				InsertStr += "INSERT INTO public.materialinventorytransaction("
						+ "	nmaterialinventtranscode, nmaterialinventorycode,ninventorytranscode"
						+ ",ntransactiontype,nsectioncode, nresultusedmaterialcode,nqtyreceived,nqtyissued, jsondata,jsonuidata,dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)"
						+ "	VALUES (" + matseq + ", " + inputMap.get("nmaterialinventorycode") + ","
						+ insJsonObj.get("ninventorytranscode") + "," + insJsonObj.get("ntransactiontype") + ","
						+ sourcesection + ", -1,Cast(" + insJsonObj.get("Received Quantity") + " as decimal ("
						+ objmap.get("sprecision") + ")),Cast(" + insJsonObj.get("nqtyissued") + " as decimal ("
						+ objmap.get("sprecision") + ")), '" + stringUtilityFunction.replaceQuote(insJsonObjcopy.toString()) + "'::jsonb, '"
						+ stringUtilityFunction.replaceQuote(jsonUidataObjcopy.toString()) + "'::jsonb,'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + userInfo.getNtranssitecode()
						+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")," + " ("
						+ (matseq + 1)
						+ ", " + (validationLst.size() == 0 ? matInvseq : destinationInventorycode) + ","
						+ insJsonObj.get("ninventorytranscode") + ","
						+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ","
						+ insJsonObj.get("nsectioncode") + ", -1,Cast(" + insJsonObj.get("nqtyissued") + " as decimal ("
						+ objmap.get("sprecision") + ")),Cast(" + insJsonObj.get("Received Quantity")
						+ "	 as decimal (" + objmap.get("sprecision") + ")), '" + stringUtilityFunction.replaceQuote(insJsonObj.toString())
						+ "'::jsonb, '" + stringUtilityFunction.replaceQuote(jsonuidata.toString()) + "'::jsonb, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ ((JSONObject) insJsonObj.get("Site")).get("value") + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				inputMap.put("nsectioncode", sourcesection);
				jsonuidataArray.put(jsonUidataObjcopy);
			} else {
				jsonuidata.put("Description",
						new JSONObject(insJsonObjcopy.get("Transaction Type").toString()).get("label") + " "
								+ commonFunction.getMultilingualMessage("IDS_FROM", userInfo.getSlanguagefilename())
								+ " " + sectionstr + " "
								+ commonFunction.getMultilingualMessage("IDS_TO", userInfo.getSlanguagefilename()) + " "
								+ sectionDescription);
				jsonUidataObjcopy.put("Description",
						commonFunction.getMultilingualMessage("IDS_RECEIVED", userInfo.getSlanguagefilename()) + " "
								+ commonFunction.getMultilingualMessage("IDS_BY", userInfo.getSlanguagefilename()) + " "
								+ sectionDescription + " "
								+ commonFunction.getMultilingualMessage("IDS_FROM", userInfo.getSlanguagefilename())
								+ " " + sectionstr);
				InsertStr = "INSERT INTO public.materialinventorytransaction("
						+ "	nmaterialinventtranscode, nmaterialinventorycode,ninventorytranscode"
						+ ",ntransactiontype,nsectioncode, nresultusedmaterialcode,nqtyreceived,nqtyissued, jsondata,jsonuidata,dtransactiondate,ntztransactiondate,noffsetdtransactiondate, nsitecode, nstatus)"
						+ "	VALUES (" + (matseq) + ", " + inputMap.get("nmaterialinventorycode") + ","
						+ insJsonObj.get("ninventorytranscode") + "," + insJsonObj.get("ntransactiontype") + ","
						+ sourcesection + ", -1," + "Cast(" + insJsonObj.get("Received Quantity") + "	as decimal ("
						+ objmap.get("sprecision") + ")), " + "Cast(" + insJsonObj.get("nqtyissued") + " as decimal ("
						+ objmap.get("sprecision") + ")),"
						+ "'" + stringUtilityFunction.replaceQuote(insJsonObj.toString()) + "'::jsonb, " + " '"
						+ stringUtilityFunction.replaceQuote(jsonuidata.toString()) + "'::jsonb,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
						+ userInfo.getNtimezonecode() + ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", " + userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),(" + (matseq + 1) + ", "
						+ inputMap.get("nmaterialinventorycode") + "," + insJsonObj.get("ninventorytranscode") + ","
						+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ","
						+ insJsonObj.get("nsectioncode") + ", -1," + "Cast(" + insJsonObj.get("nqtyissued")
						+ " as decimal (" + objmap.get("sprecision") + "))," + "Cast("
						+ insJsonObj.get("Received Quantity") + " as decimal (" + objmap.get("sprecision") + ")),"
						+ " '" + stringUtilityFunction.replaceQuote(insJsonObjcopy.toString()) + "'::jsonb, '"
						+ stringUtilityFunction.replaceQuote(jsonUidataObjcopy.toString()) + "'::jsonb,'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ ((JSONObject) insJsonObj.get("Site")).get("value") + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				inputMap.put("nsectioncode", sourcesection);
				jsonuidataArray.put(jsonUidataObjcopy);
			}
			jdbcTemplate.execute(InsertStr);
			jdbcTemplate.execute("update seqnomaterialmanagement set nsequenceno=" + (matseq + 1)
					+ "  where stablename='materialinventorytransaction' ;");
		} else {
			jsonUidataObjcopy.put("Description",
					new JSONObject(insJsonObjcopy.get("Transaction Type").toString()).get("label") + " "
							+ commonFunction.getMultilingualMessage("IDS_BY", userInfo.getSlanguagefilename()) + " "
							+ sectionDescription);
			jdbcTemplate.execute("INSERT INTO public.materialinventorytransaction("
					+ "	nmaterialinventtranscode, nmaterialinventorycode,ninventorytranscode"
					+ ",ntransactiontype,nsectioncode, nresultusedmaterialcode,nqtyreceived,nqtyissued, jsondata,jsonuidata,dtransactiondate,ntztransactiondate,noffsetdtransactiondate, nsitecode, nstatus)"
					+ "	VALUES (" + matseq + ", " + inputMap.get("nmaterialinventorycode") + ","
					+ insJsonObj.get("ninventorytranscode") + "," + insJsonObj.get("ntransactiontype") + ","
					+ sourcesection + ", -1,Cast(" + insJsonObj.get("Received Quantity") + " as decimal ("
					+ objmap.get("sprecision") + ")),Cast(" + insJsonObj.get("nqtyissued") + " as decimal ("
					+ objmap.get("sprecision") + ")), '" + stringUtilityFunction.replaceQuote(insJsonObjcopy.toString()) + "'::jsonb, " + "'"
					+ stringUtilityFunction.replaceQuote(jsonUidataObjcopy.toString()) + "'::jsonb,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");");
			jdbcTemplate.execute("update seqnomaterialmanagement set nsequenceno=" + matseq
					+ "  where stablename='materialinventorytransaction' ;");
			inputMap.put("nsectioncode", insJsonObj.get("nsectioncode"));
			jsonuidataArray.put(jsonUidataObjcopy);
		}
		objmap.put("nregtypecode", -1);
		objmap.put("nregsubtypecode", -1);
		objmap.put("ndesigntemplatemappingcode", jsonuidata.get("nmaterialconfigcode"));
		actionType.put("materialinventorytransaction", "IDS_ADDMATERIALINVENTORYTRANSACTION");
		jsonAuditObject.put("materialinventorytransaction", jsonuidataArray);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, objmap, false, userInfo);

		Map<String, Object> inventoryDetailsMap = Objmapper.readValue(insJsonObj.toString(),
				new TypeReference<Map<String, Object>>() {
				});
		objmap.putAll((Map<String, Object>) getMaterialInventoryDetails(inventoryDetailsMap, userInfo).getBody());

		objmap.putAll((Map<String, Object>) getQuantityTransactionByMaterialInvCode(
				(int) inputMap.get("nmaterialinventorycode"), inputMap, userInfo).getBody());
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ResponseEntity<Object> getMaterialInventoryLinkMaster(Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {

		Map<String, Object> objMap = new HashMap();
		List<Integer> lstMaterial = jdbcTemplate
				.queryForList("select nmaterialinventorycode from materialinventory where nmaterialinventorycode="
						+ inputMap.get("nmaterialinventorycode") + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
		if (lstMaterial.size() != 0) {

			int ntransacode = isMaterialInvRetired((int) inputMap.get("nmaterialinventorycode"));

			if (ntransacode != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {


				String sQuery = "select nlinkcode,trim('\"' from (jsondata->'slinkname')::text)  as slinkname "
						+ ",ndefaultlink,nsitecode,nstatus from linkmaster" + " where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nlinkcode > 0"
						+ " and nsitecode = " + objUserInfo.getNmastersitecode() + ";";
				List<LinkMaster> lst1 = jdbcTemplate.query(sQuery, new LinkMaster());
				objMap.put("LinkMaster", lst1);
				sQuery = " select nattachmenttypecode from attachmenttype" + " where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				List<LinkMaster> lst2 = jdbcTemplate.query(sQuery, new LinkMaster());

				objMap.put("AttachmentType", lst2);
				return new ResponseEntity<Object>(objMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THEINVENTORYALREADYEXPIRED",
						objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} else {
		}
		// status code:417
		return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALINVENTORYALREADYDELETED",
				objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	}

	@Override
	public ResponseEntity<Object> editMaterialInventoryFile(Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {

		String check = "select count(*) from materialinventoryfile where  nmaterialinventoryfilecode ="
				+ inputMap.get("nmaterialinventoryfilecode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ objUserInfo.getNtranssitecode();
		int intcheck = jdbcTemplate.queryForObject(check, Integer.class);
		final String sEditQuery = "select tf.jsondata||json_build_object('nmaterialinventoryfilecode',tf.nmaterialinventoryfilecode,'slinkname',lm.jsondata->'slinkname'"
				+ " ,'ndefaultstatus',tf.ndefaultstatus	)::jsonb  as jsondata from materialinventoryfile tf,linkmaster lm where  tf.nmaterialinventoryfilecode = "
				+ inputMap.get("nmaterialinventoryfilecode") + " and (tf.jsondata->'nlinkcode')::int=lm.nlinkcode";
		final MaterialInventoryFile objTF = (MaterialInventoryFile) jdbcUtilityFunction.queryForObject(sEditQuery, MaterialInventoryFile.class,jdbcTemplate);
		
		if (intcheck > 0) {
			int ntransacode = isMaterialInvRetired((int) inputMap.get("nmaterialinventorycode"));

			if (ntransacode != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()) {
				return new ResponseEntity<Object>(objTF, HttpStatus.OK);
			}

			else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THEINVENTORYALREADYEXPIRED",
						objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

			}
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> createMaterialInventoryFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {

		final String sTableLockQuery = " lock  table materialinventoryfile "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sTableLockQuery);

		JSONObject jsonAuditObject = new JSONObject();
		JSONObject jsondefaultObjectnew = new JSONObject();
		JSONObject jsondefaultObjectold = new JSONObject();
		JSONArray jsonArraynew = new JSONArray();
		JSONArray jsonArrayold = new JSONArray();
		JSONObject actionType = new JSONObject();
		Map<String, Object> returnmap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstaudit = new ArrayList<>();
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		JSONObject jsonObj1 = new JSONObject(request.getParameter("materialinventoryfile").toString());
		ObjectMapper objmap1 = new ObjectMapper();
		jsonObj1.put("sdescription", (jsonObj1.get("sdescription").toString().replaceAll("\"", "\\\\\"")));
		jsonObj1.put("sdescription", (StringEscapeUtils.unescapeJava(jsonObj1.get("sdescription").toString())));
		 String acceptMultilingual=StringEscapeUtils.unescapeJava(jsonObj1.toString());
		String acceptMultilingual1 = acceptMultilingual.replaceAll("#r#", "\\\\n");
		JSONObject jsonObj = new JSONObject(acceptMultilingual1);
		if (jsonObj != null && jsonObj.length() > 0) {
			int objMaterial = jdbcTemplate
					.queryForObject("select nmaterialinventorycode from materialinventory where nmaterialinventorycode="
							+ jsonObj.get("nmaterialinventorycode") + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			if (objMaterial != 0) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if ((int) jsonObj.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()) {

					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo); 

				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select * from materialinventoryfile where nmaterialinventorycode = "
							+ jsonObj.get("nmaterialinventorycode") + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ objUserInfo.getNtranssitecode() + " and ndefaultstatus = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus();
					final List<MaterialInventoryFile> lstTestFiles = jdbcTemplate.query(sQuery,
							new MaterialInventoryFile());

					if (lstTestFiles.isEmpty()) {
						jsonObj.remove("ndefaultstatus");
						jsonObj.put("ndefaultstatus", Enumeration.TransactionStatus.YES.gettransactionstatus());
						jsonObj.put("sdefaultstatus",
								commonFunction.getMultilingualMessage("IDS_YES", objUserInfo.getSlanguagefilename()));

					} else if (lstTestFiles != null) {

						if ((int) jsonObj.get("ndefaultstatus") == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
							jsondefaultObjectold.put("materialmsdsattachment", new JSONArray().put(
									new JSONObject(jdbcTemplate.queryForObject("Select  msds.jsondata||m.jsonuidata||"
											+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
											+ objUserInfo.getSlanguagetypecode()
											+ "', 'nmaterialinventoryfilecode',msds.nmaterialinventoryfilecode)::jsonb   from   "
											+ " materialinventoryfile msds,materialinventory m,attachmenttype a "
											+ " where msds.nmaterialinventoryfilecode="
											+ lstTestFiles.get(0).getNmaterialinventoryfilecode()
											+ " and msds.nmaterialinventorycode=m.nmaterialinventorycode"
											+ " and  a.nattachmenttypecode=(msds.jsondata->'nattachmenttypecode')::int",
											String.class))));
							final String updateQueryString = " update materialinventoryfile set ndefaultstatus=" + " "
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " "
									+ ",jsondata=jsondata||json_build_object('sdefaultstatus','"
									+ commonFunction.getMultilingualMessage("IDS_NO",
											objUserInfo.getSlanguagefilename())
									+ "')::jsonb || json_build_object('ndefaultstatus',"
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ")::jsonb"
									+ ", dtransactiondate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
									+ "', ntztransactiondate=" + objUserInfo.getNtimezonecode()
									+ ", noffsetdtransactiondate="
									+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
									+ " where nmaterialinventoryfilecode ="
									+ lstTestFiles.get(0).getNmaterialinventoryfilecode();
							jdbcTemplate.execute(updateQueryString);
							jsondefaultObjectnew.put("materialmsdsattachment", new JSONArray().put(
									new JSONObject(jdbcTemplate.queryForObject("Select  msds.jsondata||m.jsonuidata||"
											+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
											+ objUserInfo.getSlanguagetypecode()
											+ "', 'nmaterialinventoryfilecode',msds.nmaterialinventoryfilecode)::jsonb   from   "
											+ " materialinventoryfile msds,materialinventory m,attachmenttype a "
											+ " where msds.nmaterialinventoryfilecode="
											+ lstTestFiles.get(0).getNmaterialinventoryfilecode()
											+ " and msds.nmaterialinventorycode=m.nmaterialinventorycode"
											+ " and  a.nattachmenttypecode=(msds.jsondata->'nattachmenttypecode')::int",
											String.class))));
							objmap.put("nregtypecode", -1);
							objmap.put("nregsubtypecode", -1);
							objmap.put("ndesigntemplatemappingcode", jsonObj.get("nmaterialconfigcode"));
							actionType.put("materialinventoryfile", "IDS_EDITMATERIALINVENTORYFILE");
							auditUtilityFunction.fnJsonArrayAudit(jsondefaultObjectold, jsondefaultObjectnew, actionType, objmap, false,
									objUserInfo);
							actionType.clear();
						}
					} else {
						jsonObj.put("ndefaultstatus", Enumeration.TransactionStatus.NO.gettransactionstatus());
					}
					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
					final Instant instantDate1 = dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS);
					jsonObj.put("dcreateddate", instantDate1.toString().replace("T", " ").replace("Z", ""));
					jsonObj.put("noffsetdcreateddate", dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()));

					int seqnomaterial = jdbcTemplate.queryForObject(
							"select nsequenceno from  seqnomaterialmanagement where stablename='materialinventoryfile'",
							Integer.class);
					seqnomaterial++;
					jsonObj.remove("nstatus");
					String strQuery = "INSERT INTO public.materialinventoryfile("
							+ "	nmaterialinventoryfilecode, nmaterialinventorycode, jsondata, ndefaultstatus,dtransactiondate,nsitecode,nstatus,ntztransactiondate,noffsetdtransactiondate)"
							+ "	VALUES (" + seqnomaterial + ", " + jsonObj.get("nmaterialinventorycode") + ", '"
							+ stringUtilityFunction.replaceQuote(jsonObj.toString()) + "', " + "" + jsonObj.get("ndefaultstatus") + ",'"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', " + objUserInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
							+ objUserInfo.getNtimezonecode() + ", "
							+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + ");";
					jdbcTemplate.execute(strQuery);
					jdbcTemplate.execute("update seqnomaterialmanagement set nsequenceno=" + seqnomaterial
							+ "  where stablename='materialinventoryfile' ");
					objmap.put("nregtypecode", -1);
					objmap.put("nregsubtypecode", -1);
					objmap.put("ndesigntemplatemappingcode", jsonObj.get("nmaterialconfigcode"));
					actionType.put("materialinventoryfile", "IDS_ADDMATERIALINVENTORYFILE");

					returnmap.putAll(getMaterialFile((int) jsonObj.get("nmaterialinventorycode"), objUserInfo));
					lstaudit.add(((List<Map<String, Object>>) returnmap.get("MaterialInventoryFile"))
							.get(((List<Map<String, Object>>) returnmap.get("MaterialInventoryFile")).size() - 1));

					jsonAuditObject.put("materialinventoryfile", lstaudit);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, objmap, false, objUserInfo);
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> listObject = new ArrayList<Object>();
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALINVENTORYALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return new ResponseEntity<Object>(getMaterialFile((int) jsonObj.get("nmaterialinventorycode"), objUserInfo),
					HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}


	public Map<String, Object> getMaterialFile(final int nmaterialinventorycode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		List<Map<String, Object>> lstMaterial2 = new ArrayList<Map<String, Object>>();
		String query = "select json_agg(a.jsondata) from  (select tf.nmaterialinventoryfilecode,"
				+ " tf.jsondata||mi.jsonuidata||json_build_object('nmaterialinventoryfilecode',tf.nmaterialinventoryfilecode"
				+ "  , 'stransdisplaystatus', coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ objUserInfo.getSlanguagetypecode() + "'," + "  ts.jsondata->'stransdisplaystatus'->>'"
				+ objUserInfo.getSlanguagetypecode()
				+ "'),'slinkname',lm.jsondata->'slinkname'  ,'sattachmenttype',a.jsondata->'sattachmenttype'->>'"
				+ objUserInfo.getSlanguagetypecode()
				+ "', 'nmaterialinventoryfilecode',tf.nmaterialinventoryfilecode   )::jsonb "
				+ " as jsondata from materialinventoryfile tf,transactionstatus ts,linkmaster lm,materialinventory mi,attachmenttype a where tf.nmaterialinventorycode="
				+ nmaterialinventorycode + " and tf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nsitecode="
				+ objUserInfo.getNtranssitecode()
				+ " and  a.nattachmenttypecode=(tf.jsondata->'nattachmenttypecode')::int"
				+ " and ts.ntranscode=tf.ndefaultstatus and mi.nmaterialinventorycode=tf.nmaterialinventorycode and (tf.jsondata->'nlinkcode')::int=lm.nlinkcode order by tf.nmaterialinventoryfilecode)a;";
		String strMaterialInventoryFile = jdbcTemplate.queryForObject(query, String.class);

		// Commented For JSONUIDATA
		if (strMaterialInventoryFile != null)
			lstMaterial2 = objMapper.convertValue(projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterialInventoryFile,
					objUserInfo, true, 9, "MaterialInventoryFile"), new TypeReference<List<Map<String, Object>>>() {
					});
		outputMap.put("MaterialInventoryFile", lstMaterial2);

		return outputMap;
	}

	@SuppressWarnings( "unused")
	@Override
	public ResponseEntity<Object> updateMaterialInventoryFile(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		JSONObject jsonAuditObjectOld = new JSONObject();
		JSONObject jsonAuditObjectnew = new JSONObject();
		ObjectMapper objmapper = new ObjectMapper();

		List<Map<String, Object>> lstAuditOld = new ArrayList<>();
		List<Map<String, Object>> lstAuditNew = new ArrayList<>();
		JSONObject jsondefaultObjectnew = new JSONObject();
		JSONObject jsondefaultObjectold = new JSONObject();
		JSONArray jsonArraynew = new JSONArray();
		JSONArray jsonArrayold = new JSONArray();
		JSONObject actionType = new JSONObject();
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		JSONObject jsonobject1 = new JSONObject(request.getParameter("materialinventoryfile").toString());
		jsonobject1.put("sdescription", (jsonobject1.get("sdescription").toString().replaceAll("\"", "\\\\\"")));
		jsonobject1.put("sdescription", StringEscapeUtils.unescapeJava(jsonobject1.get("sdescription").toString()));
		 String acceptMultilingual=StringEscapeUtils.unescapeJava(jsonobject1.toString());
		String acceptMultilingual1 = acceptMultilingual.replaceAll("#r#", "\\\\n");
		JSONObject jsonobject = new JSONObject(acceptMultilingual1);
		if (jsonobject != null && jsonobject.length() > 0) {
			int objTestMaster = jdbcTemplate
					.queryForObject("select nmaterialinventorycode from materialinventory where nmaterialinventorycode="
							+ jsonobject.get("nmaterialinventorycode") + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			JSONObject jsonoldobject = new JSONObject(jdbcTemplate.queryForObject("Select  mf.jsondata||mi.jsonuidata||"
					+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
					+ objUserInfo.getSlanguagetypecode()
					+ "', 'nmaterialfilecode',mf.nmaterialinventoryfilecode)::jsonb   from   "
					+ " materialinventoryfile mf,materialinventory mi,attachmenttype a where "
					+ "mf.nmaterialinventoryfilecode=" + jsonobject.get("nmaterialinventoryfilecode")
					+ " and mf.nmaterialinventorycode=mi.nmaterialinventorycode"
					+ " and  a.nattachmenttypecode=(mf.jsondata->'nattachmenttypecode')::int", String.class));
			jsonArrayold.put(jsonoldobject);
			if (objTestMaster != 0) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if ((int) jsonobject.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select * from materialinventoryfile where nmaterialinventoryfilecode = "
							+ (int) jsonobject.get("nmaterialinventoryfilecode") + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ objUserInfo.getNtranssitecode();
					final MaterialInventoryFile objTF = (MaterialInventoryFile) jdbcTemplate.queryForObject(sQuery,
							MaterialInventoryFile.class);

					final String sCheckDefaultQuery = "select * from materialinventoryfile where nmaterialinventorycode = "
							+ (int) jsonobject.get("nmaterialinventorycode") + " and nmaterialinventoryfilecode!="
							+ (int) jsonobject.get("nmaterialinventoryfilecode") + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ objUserInfo.getNtranssitecode();
					final List<MaterialInventoryFile> lstDefTestFiles = jdbcTemplate.query(sCheckDefaultQuery,
							new MaterialInventoryFile());

					if (objTF != null) {
						String ssystemfilename = "";
						if ((int) jsonobject.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()) {
							ssystemfilename = (String) jsonobject.get("ssystemfilename");
						}

						if ((int) jsonobject.get("ndefaultstatus") == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {

							final String sDefaultQuery = "select * from materialinventoryfile where nmaterialinventorycode = "
									+ (int) jsonobject.get("nmaterialinventorycode") + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
									+ objUserInfo.getNtranssitecode() + " and ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<MaterialInventoryFile> lstTestFiles = jdbcTemplate.query(sDefaultQuery,
									new MaterialInventoryFile());

							if (!lstTestFiles.isEmpty()) {
								if ((int) jsonobject.get("ndefaultstatus") != (int) jsonoldobject
										.get("ndefaultstatus")) {
									jsondefaultObjectold = new JSONObject(jdbcTemplate.queryForObject(
											"Select  mf.jsondata||mi.jsonuidata||"
													+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
													+ objUserInfo.getSlanguagetypecode()
													+ "', 'nmaterialfilecode',mf.nmaterialinventoryfilecode)::jsonb   from   "
													+ " materialinventoryfile mf,materialinventory mi,attachmenttype a where "
													+ "mf.nmaterialinventoryfilecode="
													+ lstTestFiles.get(0).getNmaterialinventoryfilecode()
													+ " and mf.nmaterialinventorycode=mi.nmaterialinventorycode"
													+ " and  a.nattachmenttypecode=(mf.jsondata->'nattachmenttypecode')::int",
											String.class));
									final String updateQueryString = " update materialinventoryfile set jsondata=jsondata||json_build_object('ndefaultstatus'"
											+ " ," + Enumeration.TransactionStatus.NO.gettransactionstatus()
											+ ",'sdefaultstatus','"
											+ commonFunction.getMultilingualMessage("IDS_NO",
													objUserInfo.getSlanguagefilename())
											+ "')::jsonb,ndefaultstatus=" + " "
											+ Enumeration.TransactionStatus.NO.gettransactionstatus()
											+ ", dtransactiondate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
											+ "', ntztransactiondate=" + objUserInfo.getNtimezonecode()
											+ ", noffsetdtransactiondate = "
											+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
											+ " where nmaterialinventoryfilecode ="
											+ lstTestFiles.get(0).getNmaterialinventoryfilecode();
									jdbcTemplate.execute(updateQueryString);
									jsondefaultObjectnew = new JSONObject(jdbcTemplate.queryForObject(
											"Select  mf.jsondata||mi.jsonuidata||"
													+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
													+ objUserInfo.getSlanguagetypecode()
													+ "', 'nmaterialfilecode',mf.nmaterialinventoryfilecode)::jsonb   from   "
													+ " materialinventoryfile mf,materialinventory mi,attachmenttype a where "
													+ "mf.nmaterialinventoryfilecode="
													+ lstTestFiles.get(0).getNmaterialinventoryfilecode()
													+ " and mf.nmaterialinventorycode=mi.nmaterialinventorycode"
													+ " and  a.nattachmenttypecode=(mf.jsondata->'nattachmenttypecode')::int",
											String.class));
								}
							}
						} else {
							final String sDefaultQuery = "select * from materialinventoryfile where nmaterialinventorycode = "
									+ (int) jsonobject.get("nmaterialinventorycode")
									+ " and nmaterialinventoryfilecode="
									+ (int) jsonobject.get("nmaterialinventoryfilecode") + "" + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
									+ objUserInfo.getNtranssitecode() + " and ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<MaterialInventoryFile> lstTestFiles = jdbcTemplate.query(sDefaultQuery,
									new MaterialInventoryFile());

							if (lstDefTestFiles.size() > 0) {
								if (!lstTestFiles.isEmpty()) {
									if ((int) jsonobject.get("ndefaultstatus") != (int) jsonoldobject
											.get("ndefaultstatus")) {
										jsondefaultObjectold = new JSONObject(jdbcTemplate.queryForObject(
												"Select  mf.jsondata||mi.jsonuidata||"
														+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
														+ objUserInfo.getSlanguagetypecode()
														+ "', 'nmaterialinventoryfilecode',mf.nmaterialinventoryfilecode)::jsonb   from   "
														+ " materialinventoryfile mf,materialinventory mi,attachmenttype a where "
														+ "mf.nmaterialinventoryfilecode="
														+ lstDefTestFiles.get(lstDefTestFiles.size() - 1)
																.getNmaterialinventoryfilecode()
														+ " and mf.nmaterialinventorycode=mi.nmaterialinventorycode"
														+ " and  a.nattachmenttypecode=(mf.jsondata->'nattachmenttypecode')::int",
												String.class));

										final String sEditDefaultQuery = "update materialinventoryfile set jsondata=jsondata||json_build_object('ndefaultstatus'"
												+ " ," + Enumeration.TransactionStatus.YES.gettransactionstatus() + ""
												+ ", 'sdefaultstatus','"
												+ commonFunction.getMultilingualMessage("IDS_YES",
														objUserInfo.getSlanguagefilename())
												+ "')::jsonb,ndefaultstatus = "
												+ Enumeration.TransactionStatus.YES.gettransactionstatus()
												+ ", dtransactiondate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
												+ "', ntztransactiondate=" + objUserInfo.getNtimezonecode()
												+ ", noffsetdtransactiondate="
												+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
												+ " where nmaterialinventoryfilecode = "
												+ lstDefTestFiles.get(lstDefTestFiles.size() - 1)
														.getNmaterialinventoryfilecode();
										jdbcTemplate.execute(sEditDefaultQuery);
										jsondefaultObjectnew = new JSONObject(jdbcTemplate.queryForObject(
												"Select  mf.jsondata||mi.jsonuidata||"
														+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
														+ objUserInfo.getSlanguagetypecode()
														+ "', 'nmaterialinventoryfilecode',mf.nmaterialinventoryfilecode)::jsonb   from   "
														+ " materialinventoryfile mf,materialinventory mi,attachmenttype a where "
														+ "mf.nmaterialinventoryfilecode="
														+ lstDefTestFiles.get(lstDefTestFiles.size() - 1)
																.getNmaterialinventoryfilecode()
														+ " and mf.nmaterialinventorycode=mi.nmaterialinventorycode"
														+ " and  a.nattachmenttypecode=(mf.jsondata->'nattachmenttypecode')::int",
												String.class));
									}
								}
							} else {
								return new ResponseEntity<>(commonFunction.getMultilingualMessage(
										Enumeration.ReturnStatus.DEFAULTCANNOTCHANGED.getreturnstatus(),
										objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
							}
						}

						if (!jsondefaultObjectold.isEmpty())
							jsonArrayold.put(jsondefaultObjectold);
						
						String strQuery = "update materialinventoryfile set jsondata='"
								+ stringUtilityFunction.replaceQuote(jsonobject.toString()) + "',ndefaultstatus='"
								+ jsonobject.get("ndefaultstatus") + "', dtransactiondate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', ntztransactiondate="
								+ objUserInfo.getNtimezonecode() + ", noffsetdtransactiondate="
								+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
								+ " where nmaterialinventoryfilecode=" + jsonobject.get("nmaterialinventoryfilecode");
						jdbcTemplate.execute(strQuery);
						objmap.put("nregtypecode", -1);
						objmap.put("nregsubtypecode", -1);
						objmap.put("ndesigntemplatemappingcode", jsonobject.get("nmaterialconfigcode"));
						actionType.put("materialinventoryfile", "IDS_EDITMATERIALINVENTORYFILE");
						jsonArraynew.put(new JSONObject(jdbcTemplate.queryForObject(
								"Select  mf.jsondata||mi.jsonuidata||"
										+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
										+ objUserInfo.getSlanguagetypecode()
										+ "', 'nmaterialinventoryfilecode',mf.nmaterialinventoryfilecode)::jsonb   from   "
										+ " materialinventoryfile mf,materialinventory mi,attachmenttype a where "
										+ "mf.nmaterialinventoryfilecode="
										+ jsonobject.get("nmaterialinventoryfilecode")
										+ " and mf.nmaterialinventorycode=mi.nmaterialinventorycode"
										+ " and  a.nattachmenttypecode=(mf.jsondata->'nattachmenttypecode')::int",
								String.class)));
						if (!jsondefaultObjectnew.isEmpty())
							jsonArraynew.put(jsondefaultObjectnew);
						
						jsonAuditObjectOld.put("materialinventoryfile", jsonArrayold);
						jsonAuditObjectnew.put("materialinventoryfile", jsonArraynew);
						auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, jsonAuditObjectnew, actionType, objmap, false,
								objUserInfo);

						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList.add(
								(int) jsonobject.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()
										? "IDS_EDITTESTFILE"
										: "IDS_EDITTESTLINK");
						lstOldObject.add(objTF);
						return new ResponseEntity<Object>(
								getMaterialFile((int) jsonobject.get("nmaterialinventorycode"), objUserInfo),
								HttpStatus.OK);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALINVENTORYALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}


	@Override
	public ResponseEntity<Object> deleteMaterialInventoryFile(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {
		JSONObject jsonAuditObjectOld = new JSONObject();
		JSONObject actionType = new JSONObject();
		ObjectMapper objmapper = new ObjectMapper();
		List<Map<String, Object>> lstaudit = new ArrayList<>();

		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		JSONObject jsonArray1 = new JSONObject(inputMap.get("materialinventoryfile").toString());
		List<Integer> lstMaterial = jdbcTemplate
				.queryForList("select nmaterialinventorycode from materialinventory where nmaterialinventorycode="
						+ jsonArray1.get("nmaterialinventorycode") + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
		if (lstMaterial.size() != 0) {
			if (jsonArray1 != null) {
				final String sQuery = "select * from materialinventoryfile where nmaterialinventoryfilecode = "
						+ jsonArray1.get("nmaterialinventoryfilecode") + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ objUserInfo.getNtranssitecode();
				final MaterialInventoryFile objTF = (MaterialInventoryFile) jdbcTemplate.queryForObject(sQuery,
						MaterialInventoryFile.class);
				JSONObject jsonArray2 = new JSONObject(inputMap.get("materialinventoryfile").toString());
				if (objTF != null) {
					if ((int) jsonArray1.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()) {
					} else {
						jsonArray1.put("dcreateddate", "");
					}

					if ((int) jsonArray2.get("ndefaultstatus") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						final String sDeleteQuery = "select * from materialinventoryfile where "
								+ " nmaterialinventoryfilecode !=" + jsonArray1.get("nmaterialinventoryfilecode") + ""
								+ " and nmaterialinventorycode=" + jsonArray1.get("nmaterialinventorycode") + ""
								+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode=" + objUserInfo.getNtranssitecode();
						List<MaterialInventoryFile> lstTestFiles = jdbcTemplate.query(sDeleteQuery,
								new MaterialInventoryFile());
						String sDefaultQuery = "";
						if (lstTestFiles.isEmpty()) {
							sDefaultQuery = " update materialinventoryfile set " + " nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									+ ", dtransactiondate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
									+ "', ntztransactiondate=" + objUserInfo.getNtimezonecode()
									+ ", noffsetdtransactiondate="
									+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
									+ " where nmaterialinventoryfilecode = "
									+ jsonArray1.get("nmaterialinventoryfilecode");
						} else {
							sDefaultQuery = "update materialinventoryfile set " + " ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ", dtransactiondate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', ntztransactiondate="
									+ objUserInfo.getNtimezonecode() + ", noffsetdtransactiondate="
									+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
									+ " where nmaterialinventoryfilecode = "
									+ lstTestFiles.get(lstTestFiles.size() - 1).getNmaterialinventoryfilecode();
						}
						jdbcTemplate.execute(sDefaultQuery);
					}
					final String sUpdateQuery = "update materialinventoryfile set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', ntztransactiondate="
							+ objUserInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid())
							+ " where nmaterialinventoryfilecode = " + jsonArray1.get("nmaterialinventoryfilecode");
					jdbcTemplate.execute(sUpdateQuery);
					objmap.put("nregtypecode", -1);
					objmap.put("nregsubtypecode", -1);
					objmap.put("ndesigntemplatemappingcode", jsonArray1.get("nmaterialconfigcode"));
					actionType.put("materialinventoryfile", "IDS_DELETEMATERIALINVENTORYFILE");
					lstaudit = objmapper.convertValue((new JSONArray(jdbcTemplate.queryForObject(
							"select json_agg(mf.jsondata||mi.jsonuidata::jsonb) from materialinventoryfile mf,materialinventory mi where "
									+ " nmaterialinventoryfilecode=" + jsonArray1.get("nmaterialinventoryfilecode")
									+ " and mi.nmaterialinventorycode=mf.nmaterialinventorycode",
							String.class))).toList(), new TypeReference<List<Map<String, Object>>>() {
							});
					jsonAuditObjectOld.put("materialinventoryfile", lstaudit);

					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, null, actionType, objmap, false, objUserInfo);
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return new ResponseEntity<Object>(
					getMaterialFile((int) jsonArray1.get("nmaterialinventorycode"), objUserInfo), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALINVENTORYALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public Map<String, Object> viewAttachedMaterialInventoryFile(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();

		Map<String, Object> auditmap = new LinkedHashMap<>();
		List<Map<String, Object>> lstdata = new ArrayList<>();
		List<Map<String, Object>> lstaudit = new ArrayList<>();
		JSONObject actionType = new JSONObject();
		JSONObject jsonAuditObject = new JSONObject();

		JSONObject jsonObject = new JSONObject(inputMap.get("materialinventoryfile").toString());
		if (jsonObject != null) {
			String sQuery = "select * from materialinventoryfile where nmaterialinventoryfilecode = "
					+ jsonObject.get("nmaterialinventoryfilecode") + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ objUserInfo.getNtranssitecode();
			final MaterialInventoryFile objTF = (MaterialInventoryFile) jdbcTemplate.queryForObject(sQuery,
					MaterialInventoryFile.class);
			if (objTF != null) {
				if ((int) jsonObject.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(jsonObject.get("ssystemfilename").toString(), -1, objUserInfo, "", "");
				} else {
					sQuery = "select jsondata->>'slinkname' from linkmaster where nlinkcode="
							+ jsonObject.get("nlinkcode") + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					LinkMaster objlinkmaster = (LinkMaster) jdbcTemplate.queryForObject(sQuery, LinkMaster.class);
					map.put("AttachLink", objlinkmaster.getSlinkname() + jsonObject.get("sfilename"));
					jsonObject.put("dcreateddate", "");
				}
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList
						.add((int) jsonObject.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWMATERIALINVENTORYFILE"
								: "IDS_VIEWMATERIALINVENTORYLINK");
			} else {
				// status code:417
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()));
				return map;
			}
		}

		lstdata = (List<Map<String, Object>>) getMaterialFile((int) inputMap.get("nmaterialinventorycode"), objUserInfo)
				.get("MaterialInventoryFile");
		lstaudit.add(0,
				((java.util.Optional<Map<String, Object>>) lstdata.stream().filter(
						t -> t.get("nmaterialinventoryfilecode").equals(jsonObject.get("nmaterialinventoryfilecode")))
						.findAny()).get());
		jsonAuditObject.put("materialinventoryfile", lstaudit);
		auditmap.put("nregtypecode", -1);
		auditmap.put("nregsubtypecode", -1);
		auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
		actionType.put("materialinventoryfile",
				(int) jsonObject.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()
						? "IDS_VIEWMATERIALINVENTORYFILE"
						: "IDS_VIEWMATERIAINVENTORYLINK");
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, objUserInfo);

		return map;
	}

	public Map<String, Object> getTemplateDesignForMaterial(int nmaterialconfigcode, int nformcode) throws Exception {

		final String str = "select jsondata->'" + nformcode + "' as jsondata from mappedtemplatefieldpropsmaterial"
				+ " where nmaterialconfigcode=" + nmaterialconfigcode;

		Map<String, Object> map = jdbcTemplate.queryForMap(str);
		return map;

	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getResultUsedMaterial(int nmaterialinventorycode, UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstResultUsedMaterial = new ArrayList<>();
		String str = "select json_AGG(jsondata) jsondata from resultusedmaterial where ninventorycode="
				+ nmaterialinventorycode;
		try {
			String lstData1 = jdbcTemplate.queryForObject(str, String.class);
			if (lstData1 != null)
				lstResultUsedMaterial = (List<Map<String, Object>>) projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(lstData1,
						objUserInfo, true, -1, "ResultEntryTransaction");
			objmap.put("ResultUsedMaterial", lstResultUsedMaterial);

		} catch (EmptyResultDataAccessException e) {
			objmap.put("ResultUsedMaterial", lstResultUsedMaterial);
		}

		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	public Map<String, Object> getMaterialInventoryhistory(final int nmaterialinventorycode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		List<Map<String, Object>> lstMaterialInventoryHistory = new ArrayList<Map<String, Object>>();
		String query = " select json_agg(json_build_object('Inventory ID',mi.jsondata->>'Inventory ID','nmaterialinventoryhistorycode',m.nmaterialinventoryhistorycode, "
				+ "'nmaterialinventorycode',m.nmaterialinventorycode, "
				+ "	  'ntransactionstatus',m.ntransactionstatus,'stransdisplaystatus',ts.jsondata->'stransdisplaystatus'->>'"
				+ objUserInfo.getSlanguagetypecode()
				+ "', 'dnextvalidationdate',case when TO_CHAR(m.dnextvalidationdate,'"
				+ objUserInfo.getSpgsitedatetime() + "') is null then '-' else TO_CHAR(m.dnextvalidationdate,' "
				+ objUserInfo.getSpgsitedatetime()
				+ "') end ,'noffsetdnextvalidationdate',m.noffsetdnextvalidationdate )) "
				+ "	  from materialinventoryhistory m,materialinventory mi,transactionstatus ts where ts.ntranscode=m.ntransactionstatus and"
				+ " m.nmaterialinventorycode=" + nmaterialinventorycode
				+ " and mi.nmaterialinventorycode=m.nmaterialinventorycode	  and m.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nsitecode="
				+ objUserInfo.getNtranssitecode() + " and mi.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		String strMaterialInventoryHistory = jdbcTemplate.queryForObject(query, String.class);
		if (strMaterialInventoryHistory != null)
			lstMaterialInventoryHistory = objMapper
					.convertValue(projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterialInventoryHistory, objUserInfo, false,
							-1, "MaterialInventoryHistory"), new TypeReference<List<Map<String, Object>>>() {
							});
		outputMap.put("MaterialInventoryHistory", lstMaterialInventoryHistory);
		return outputMap;
	}

	public void createMaterialInventoryhistory(Map<String, Object> inputmap, final UserInfo objUserInfo)
			throws Exception {

		final String sTableLockQuery = " lock  table lockmaterialinventory "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sTableLockQuery);

		JSONObject jsonobject = new JSONObject();
		String snextvalidation;
		String insertupdatestr = "";
		Instant nextvalidationdate = dateUtilityFunction.getCurrentDateTime(objUserInfo);
		int materialinventoryhistorycode = jdbcTemplate
				.queryForObject("select  nsequenceno from seqnomaterialmanagement  where stablename "
						+ "in ('materialinventoryhistory');", Integer.class);
		materialinventoryhistorycode++;
		snextvalidation = jdbcTemplate.queryForObject(
				"select  jsondata  from material  where nmaterialcode " + "in (" + inputmap.get("nmaterialcode") + ");",
				String.class);
		jsonobject = new JSONObject(snextvalidation.toString());
		if ((jsonobject.get("Next Validation Need")).equals(Enumeration.TransactionStatus.YES.gettransactionstatus())) {
			int nperioddata = (int) jsonobject.get("Next Validation");
			Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo);

			if (((JSONObject) jsonobject.get("Next Validation Period")).get("value")
					.equals(Enumeration.Period.Weeks.getPeriod())) {
				nextvalidationdate = instantDate.plus((168 * nperioddata), ChronoUnit.HOURS)
						.truncatedTo(ChronoUnit.SECONDS);
			}
			if (((JSONObject) jsonobject.get("Next Validation Period")).get("value")
					.equals(Enumeration.Period.Days.getPeriod())) {
				
				nextvalidationdate = instantDate.plus((24 * nperioddata), ChronoUnit.HOURS)
						.truncatedTo(ChronoUnit.SECONDS);
			}
			if (((JSONObject) jsonobject.get("Next Validation Period")).get("value")
					.equals(Enumeration.Period.Hours.getPeriod())) {

				nextvalidationdate = instantDate.plus((nperioddata), ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
			}
		}

		if (!inputmap.containsKey("reusableCount")) {
			insertupdatestr = "INSERT INTO public.materialinventoryhistory("
					+ "	nmaterialinventoryhistorycode, nmaterialinventorycode, ntransactionstatus, dnextvalidationdate, "
					+ "noffsetdnextvalidationdate, ntznextvalidationdate, dtransactiondate, noffsetdtransactiondate, ntztransactiondate, nsitecode, nstatus) "
					+ "	VALUES (" + materialinventoryhistorycode + ", " + inputmap.get("nmaterialinventorycode") + ", "
					+ inputmap.get("ntranscode") + ",  "
					+ ((jsonobject.get("Next Validation").equals("")
							|| inputmap.get("ntranscode")
									.equals(Enumeration.TransactionStatus.RETIRED.gettransactionstatus())
							|| inputmap.get("ntranscode")
									.equals(Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus())) ? null
											: "'" + nextvalidationdate + "'")
					+ " , " + dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + ", "
					+ objUserInfo.getNtimezonecode() + ", '" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
					+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + ", " + objUserInfo.getNtimezonecode()
					+ ", " + objUserInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
		} else {
			insertupdatestr = "INSERT INTO public.materialinventoryhistory("
					+ "	nmaterialinventoryhistorycode, nmaterialinventorycode, ntransactionstatus, dnextvalidationdate, "
					+ "noffsetdnextvalidationdate, ntznextvalidationdate, dtransactiondate, noffsetdtransactiondate, ntztransactiondate, nsitecode, nstatus) "
					+ "	select   generate_series(" + materialinventoryhistorycode + ",(" + materialinventoryhistorycode
					+ "+" + inputmap.get("reusableCount") + ")-1), generate_series("
					+ inputmap.get("nmaterialinventorycode") + ",(" + inputmap.get("nmaterialinventorycode") + "+"
					+ inputmap.get("reusableCount") + ")-1), " + inputmap.get("ntranscode") + ",  "
					+ ((jsonobject.get("Next Validation").equals("")
							|| inputmap.get("ntranscode")
									.equals(Enumeration.TransactionStatus.RETIRED.gettransactionstatus())
							|| inputmap.get("ntranscode")
									.equals(Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus())) ? null
											: "'" + nextvalidationdate + "'")
					+ " , " + dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + ", "
					+ objUserInfo.getNtimezonecode() + ", '" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
					+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + ", " + objUserInfo.getNtimezonecode()
					+ ", " + objUserInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";
		}
		insertupdatestr += "update seqnomaterialmanagement set nsequenceno= (select max(nmaterialinventoryhistorycode) from materialinventoryhistory) where stablename='materialinventoryhistory';";
		jdbcTemplate.execute(insertupdatestr);

	}

	public boolean checkleapYear(int year) {
		// If a year is multiple of 400,
		// then it is a leap year
		if (year % 400 == 0)
			return true;

		// Else If a year is multiple of 100,
		// then it is not a leap year
		if (year % 100 == 0)
			return false;

		// Else If a year is multiple of 4,
		// then it is a leap year
		if (year % 4 == 0)
			return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getComboValuesMaterial(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		String tableName = "";
		final ObjectMapper objmapper = new ObjectMapper();
		List<Map<String, Object>> srcData = (List<Map<String, Object>>) inputMap.get("parentcolumnlist");
		Map<String, Object> childData = (Map<String, Object>) inputMap.get("childcolumnlist");
		Map<String, Object> parameters = (Map<String, Object>) inputMap.get("parameters");
		String getJSONKeysQuery = "";
		String Keysofparam = "";
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
			tableName = sourceName.toLowerCase();
			final String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
					+ tableName + "' and data_type = 'jsonb'";
			String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
			jsonField = jsonField != null ? "||" + jsonField : "";

			final String getDateFieldQuery = "select string_agg(column_name ,',')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
					+ tableName + "' and data_type = 'timestamp without time zone'";
			String jsonDateField = jdbcTemplate.queryForObject(getDateFieldQuery, String.class);
			List<String> lstDateField = new ArrayList<>();
			if (jsonDateField != null) {
				lstDateField = Arrays.asList(jsonDateField.split(","));
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
							+ " from " + tableName + " where nstatus =1 and " + srcData.get(i).get("valuemember")
							+ " > 0 " + conditionString + " ;";
				} else {
					getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb" + jsonField + " as jsondata"
							+ " from " + tableName + " where nstatus =1 " + conditionString + " ;";
				}
			}
			List<Combopojo> data = jdbcTemplate.query(getJSONKeysQuery, new Combopojo());
			String label = (String) srcData.get(i).get("label");
			if (!data.isEmpty() && childData != null && childData.containsKey(label)) {
				for (int j = 0; j < data.size(); j++) {

					Map<String, Object> childDatas = data.get(j).getJsondata();
					if (childDatas.containsKey("ndefaultstatus")) {
						if ((int) childDatas.get("ndefaultstatus") == 3) {
							if (srcData.get(i).containsKey("child")) {
								((Map<String, Object>) inputMap.get("parameters")).put("nsitecode",
										childDatas.get("nsitecode"));
								((Map<String, Object>) srcData.get(i)).put("parameters", inputMap.get("parameters"));
							}
							Map<String, Object> datas = recursiveChildComboMaterial(childDatas, childData,
									srcData.get(i), label, (String) srcData.get(i).get("valuemember"), tableName,
									userInfo);
							returnObject.putAll(datas);
							break;
						}
					} else {
						break;
					}

				}
			}
			if (!lstDateField.isEmpty()) {
				
			}
			returnObject.put(label, data);
		}
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> recursiveChildComboMaterial(Map<String, Object> childDatas,
			Map<String, Object> childData, Map<String, Object> map, String label, String valueMember, String tableName,
			UserInfo userInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Map<String, Object> objReturn = new HashMap();
		//String getJSONKeysQuery = "";
		String Keysofparam = "";
		List<Map<String, Object>> filterQueryComponentsQueries = null;
		List<Map<String, Object>> childcomboData = (List<Map<String, Object>>) childData.get(label);
		List lstnsqlquerycode = childcomboData.stream().filter(t -> t.containsKey("nsqlquerycode"))
				.map(x -> x.get("nsqlquerycode")).map(Object::toString).collect(Collectors.toList());
		String ssqlquerycode = (String) lstnsqlquerycode.stream().collect(Collectors.joining(","));
		Map<String, Object> parameters = null;
		if (map.containsKey("parameters"))
			parameters = (Map<String, Object>) map.get("parameters");

		if (!ssqlquerycode.equals("")) {
			filterQueryComponentsQueries = jdbcTemplate.queryForList("select nsqlquerycode,ssqlquery from "
					+ " sqlquery where nsqlquerycode in (" + ssqlquerycode + ")");
		}
		for (int z = 0; z < childcomboData.size(); z++) {
			final String sourceName1 = (String) childcomboData.get(z).get("source");
			String labelchild = (String) childcomboData.get(z).get("label");
			final String valuememberchild = (String) childcomboData.get(z).get("valuemember");
			final String tableNamechild = sourceName1.toLowerCase();
			String conditionString = childcomboData.get(z).containsKey("conditionstring")
					? (String) childcomboData.get(z).get("conditionstring")
					: "";
			String valuememberData = valueMember;
			String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
					+ tableNamechild + "' and data_type = 'jsonb'";
			String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
			final String getDateFieldQuery = "select string_agg(column_name ,',')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
					+ tableName + "' and data_type = 'timestamp without time zone'";
			String jsonDateField = jdbcTemplate.queryForObject(getDateFieldQuery, String.class);
			List<String> lstDateField = new ArrayList<>();
			if (jsonDateField != null) {
				lstDateField = Arrays.asList(jsonDateField.split(","));

			}
			if (jsonField != null) {
				final String jsonColumnsQry = "select string_agg(jsondata,',')" + "from ("
						+ " select jsonb_object_keys(" + jsonField + ") jsondata from ( " + "  select "
						+ jsonField.replace("||", ",") + " from " + tableNamechild + " where nstatus=1 "
						+ conditionString + " order by 1 desc limit 1" + " )a" + ")b;";
				final String jsonColumns = jdbcTemplate.queryForObject(jsonColumnsQry, String.class);
				if (jsonColumns != null) {
					if (jsonColumns.contains(valueMember)) {
						valueMember = " jsonuidata->> '" + valueMember + "' ";
					} else if (!jsonField.contains(valueMember)) {
						getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
								+ tableNamechild + "' ";
						final String jsonchildField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
						if (!jsonchildField.contains(valueMember)) {
							List<Map<String, Object>> childtablecolumnname = (List<Map<String, Object>>) map
									.get("child");
							String tablecolumnname = (String) childtablecolumnname.stream()
									.filter(x -> x.get("label").equals(labelchild)).map(y -> y.get("tablecolumnname"))
									.findFirst().orElse("");
							valueMember = valuememberchild;
							valuememberData = tablecolumnname;
						} else {

						}
					}
				} else {
					valueMember = " jsonuidata->> '" + valueMember + "' ";
				}
			} else {
				getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
						+ tableNamechild + "' ";
				final String jsonchildField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
				if (!jsonchildField.contains(valueMember)) {
					List<Map<String, Object>> childtablecolumnname = (List<Map<String, Object>>) map.get("child");
					String tablecolumnname = (String) childtablecolumnname.stream()
							.filter(x -> x.get("label").equals(labelchild)).map(y -> y.get("tablecolumnname"))
							.findFirst().orElse("");
					valueMember = valuememberchild;
					valuememberData = tablecolumnname;
				}
			}
			jsonField = jsonField != null ? "||" + jsonField : "";
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
				defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
			}
			if (childcomboData.get(z).containsKey("nsqlquerycode")) {
				final int j = z;
				getJSONFieldQuery = (String) filterQueryComponentsQueries.stream()
						.filter(temp -> temp.get("nsqlquerycode").equals(childcomboData.get(j).get("nsqlquerycode")))
						.map(temp1 -> temp1.get("ssqlquery")).findFirst().orElse("");
				while (getJSONFieldQuery.contains("P$")) {
					StringBuilder sb = new StringBuilder(getJSONFieldQuery);
					Map<String, Object> userInfoMap = objmapper.convertValue(userInfo,
							new TypeReference<Map<String, Object>>() {
							});
					int firstindex = sb.indexOf("P$");
					int lastindex = sb.indexOf("$P");
					Keysofparam = sb.substring(firstindex + 2, lastindex);
					if (userInfoMap.containsKey(Keysofparam) && !parameters.containsKey(Keysofparam)) {
						sb.replace(firstindex, lastindex + 2, userInfoMap.get(Keysofparam).toString());
					} else if (parameters.containsKey(Keysofparam)) {
						sb.replace(firstindex, lastindex + 2, parameters.get(Keysofparam).toString());
					} else {
						LOGGER.error(
								"PARAMETER NOT FOUND IN THE QUERY ===== Parameter cannot be replaced." + Keysofparam);
					}
					getJSONFieldQuery = sb.toString();
				}

			} else {
				getJSONFieldQuery = "select TO_JSON(" + tableNamechild + ".*)::jsonb" + jsonField + "  as jsondata"
						+ " from " + tableNamechild + " where nstatus =1 and " + valueMember + defaultvalues
						+ conditionString + ";";
			}

			List<Combopojo> data = jdbcTemplate.query(getJSONFieldQuery, new Combopojo());
			if (!data.isEmpty() && childData != null && childData.containsKey(labelchild)) {
				for (int j = 0; j < data.size(); j++) {
					Map<String, Object> child = data.get(j).getJsondata();
					if (child.containsKey("ndefaultstatus")) {
						if ((int) child.get("ndefaultstatus") == 3) {
							final Map<String, Object> datas = recursiveChildComboMaterial(child, childData,
									childcomboData.get(z), labelchild, valuememberchild, tableNamechild, userInfo);
							objReturn.putAll(datas);
							break;
						}
					} else {
						break;
					}
				}
			}
			if (!lstDateField.isEmpty()) {
				
			}
			objReturn.put(labelchild, data);
		}
		return objReturn;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getChildValuesMaterial(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		String tableName = "";
		final ObjectMapper objmapper = new ObjectMapper();
		final List<Map<String, Object>> srcData = (List<Map<String, Object>>) inputMap.get("parentcolumnlist");
		Map<String, Object> childData = (Map<String, Object>) inputMap.get("childcolumnlist");
		Map<String, Object> parentData = (Map<String, Object>) inputMap.get("parentdata");
		final Map<String, Object> returnObject = new HashMap<>();
		String Keysofparam = "";
		List<Map<String, Object>> filterQueryComponentsQueries = null;
		String valuememberData = "";
		//List
		List<String> lstnsqlquerycode = srcData.stream().filter(t -> t.containsKey("nsqlquerycode"))
				.map(x -> x.get("nsqlquerycode")).map(Object::toString).collect(Collectors.toList());
		String ssqlquerycode = (String) lstnsqlquerycode.stream().collect(Collectors.joining(","));
		Map<String, Object> parameters = null;
		if (inputMap.containsKey("parameters"))
			parameters = (Map<String, Object>) inputMap.get("parameters");

		if (!ssqlquerycode.equals("")) {
			filterQueryComponentsQueries = jdbcTemplate.queryForList("select nsqlquerycode,ssqlquery from "
					+ " sqlquery where nsqlquerycode in (" + ssqlquerycode + ")");
		}
		for (int i = 0; i < srcData.size(); i++) {
			String valuemember = (String) inputMap.get("valuemember");
			final String sourceName = (String) srcData.get(i).get("source");
			final String valuememberChild = (String) srcData.get(i).get("valuemember");
			final String label = (String) srcData.get(i).get("label");
			String conditionString = srcData.get(i).containsKey("conditionstring")
					? (String) srcData.get(i).get("conditionstring")
					: "";
			tableName = sourceName.toLowerCase();
			valuememberData = valuemember;
			String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
					+ tableName + "' and data_type = 'jsonb'";
			String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
			final String getDateFieldQuery = "select string_agg(column_name ,',')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
					+ tableName + "' and data_type = 'timestamp without time zone'";
			String jsonDateField = jdbcTemplate.queryForObject(getDateFieldQuery, String.class);
			List<String> lstDateField = new ArrayList<>();
			if (jsonDateField != null) {
				lstDateField = Arrays.asList(jsonDateField.split(","));

			}
			if (jsonField != null) {
				final String jsonColumnsQry = "select string_agg(jsondata,',')" + "from ("
						+ " select jsonb_object_keys(" + jsonField + ") jsondata from ( " + "  select "
						+ jsonField.replace("||", ",") + " from " + tableName + " where nstatus=1 " + conditionString
						+ " order by 1 desc limit 1" + " )a" + ")b;";
				final String jsonColumns = jdbcTemplate.queryForObject(jsonColumnsQry, String.class);
				if (jsonColumns != null) {
					if (jsonColumns.contains(valuemember)) {
						valuemember = " jsonuidata->> '" + valuemember + "' ";
					} else if (!jsonField.contains(valuemember)) {
						getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
								+ tableName + "' ";
						final String jsonchildField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
						if (!jsonchildField.contains(valuemember)) {
							List<Map<String, Object>> childList = (List<Map<String, Object>>) inputMap.get("child");
							String tablecolumnname = (String) childList.stream()
									.filter(x -> x.get("label").equals(label)).map(y -> y.get("tablecolumnname"))
									.findFirst().orElse("");
							valuemember = valuememberChild;
							valuememberData = tablecolumnname;
						} else {
							valuemember = " jsonuidata->> '" + valuemember + "' ";
						}
					}
				} else {
					valuemember = " jsonuidata->> '" + valuemember + "' ";
				}
			} else {
				getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
						+ tableName + "' ";
				String jsonchildField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
				if (!jsonchildField.contains(valuemember)) {
					List<Map<String, Object>> childList = (List<Map<String, Object>>) inputMap.get("child");
					String tablecolumnname = (String) childList.stream().filter(x -> x.get("label").equals(label))
							.map(y -> y.get("tablecolumnname")).findFirst().orElse("");
					valuemember = valuememberChild;
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
				defaultvalues = " = '" + parentData.get(valuememberData) + "'";
			}
			if (srcData.get(i).containsKey("nsqlquerycode")) {
				final int j = i;
				getJSONFieldQuery = (String) filterQueryComponentsQueries.stream()
						.filter(temp -> temp.get("nsqlquerycode").equals(srcData.get(j).get("nsqlquerycode")))
						.map(temp1 -> temp1.get("ssqlquery")).findFirst().orElse("");
				while (getJSONFieldQuery.contains("P$")) {
					StringBuilder sb = new StringBuilder(getJSONFieldQuery);
					Map<String, Object> userInfoMap = objmapper.convertValue(userInfo,
							new TypeReference<Map<String, Object>>() {
							});
					int firstindex = sb.indexOf("P$");
					int lastindex = sb.indexOf("$P");
					Keysofparam = sb.substring(firstindex + 2, lastindex);
					if (userInfoMap.containsKey(Keysofparam) && !parameters.containsKey(Keysofparam)) {
						sb.replace(firstindex, lastindex + 2, userInfoMap.get(Keysofparam).toString());
					} else if (parameters.containsKey(Keysofparam)) {
						sb.replace(firstindex, lastindex + 2, parameters.get(Keysofparam).toString());
					} else {
						LOGGER.error(
								"PARAMETER NOT FOUND IN THE QUERY ===== Parameter cannot be replaced." + Keysofparam);
					}
					getJSONFieldQuery = sb.toString();
				}

			} else {
				getJSONFieldQuery = "select TO_JSON(" + tableName + ".*)::jsonb" + jsonField + "  as jsondata"
						+ " from " + tableName + " where nstatus =1 and " + valuemember + defaultvalues
						+ conditionString + ";";
			}
			List<Combopojo> data = jdbcTemplate.query(getJSONFieldQuery, new Combopojo());
			if (!data.isEmpty() && childData != null && childData.containsKey(label)) {
				for (int j = 0; j < data.size(); j++) {
					Map<String, Object> child = data.get(j).getJsondata();
					if (child.containsKey("ndefaultstatus")) {
						if ((int) child.get("ndefaultstatus") == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {
							Map<String, Object> datas = recursiveChildComboMaterial(child, childData, srcData.get(i),
									label, valuememberChild, tableName, userInfo);
							returnObject.putAll(datas);
							break;
						}
					} else {
						break;
					}
				}
			}
			if (!lstDateField.isEmpty()) {
			
			}
			returnObject.put(label, data);
		}
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	public int  isMaterialInvRetired(int nmaterialinventorycode) {
		String  ntranscode = "select jsondata->'ntranscode' from materialinventory " + " where nmaterialinventorycode="
				+ nmaterialinventorycode + " and nstatus="
				+  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		int  nmaterialInv = jdbcTemplate.queryForObject(ntranscode,  Integer.class);

		return nmaterialInv;
	}
	
// ALPD-5332 Added by Abdul Material Scheduler
	// Recursive function to search for the object by name and return its label
    private static String findLabel(Object json, String targetName) {
        if (json instanceof JSONArray) {
            JSONArray array = (JSONArray) json;
            for (int i = 0; i < array.length(); i++) {
                String result = findLabel(array.get(i), targetName);
                if (result != null) {
                    return result;
                }
            }
        } else if (json instanceof JSONObject) {
            JSONObject obj = (JSONObject) json;

            // Check if the object has the target name
            if (obj.has("name") && obj.getString("name").equals(targetName) && obj.has("label")) {
                return obj.getString("label");
            }

            // Recursively search in children arrays
            if (obj.has("children")) {
                return findLabel(obj.get("children"), targetName);
            }
            if (obj.has("child")) {
                return findLabel(obj.get("child"), targetName);
            }
        }
        return null;
    }

}
