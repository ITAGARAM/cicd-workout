package com.agaramtech.qualis.materialmanagement.service.material;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
import com.agaramtech.qualis.materialmanagement.model.MaterialInventoryTrans;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventoryType;
import com.agaramtech.qualis.materialmanagement.model.MaterialMsdsAttachment;
import com.agaramtech.qualis.materialmanagement.model.MaterialProperties;
import com.agaramtech.qualis.materialmanagement.model.MaterialSafetyInstructions;
import com.agaramtech.qualis.materialmanagement.model.MaterialType;
import com.agaramtech.qualis.materialmanagement.service.materialsection.MaterialSectionDAO;
import com.agaramtech.qualis.organization.model.Section;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class MaterialDAOImpl implements MaterialDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final MaterialSectionDAO objMaterialSectionDAO;

	@Override
	public ResponseEntity<Object> getMaterialType(final UserInfo objUserInfo, final String currentUIDate)
			throws Exception {
		LOGGER.info("getMaterialType()");
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(objUserInfo, currentUIDate,
				"datetime", "FromDate");
		objmap.put("fromDate", mapObject.get("FromDateWOUTC"));
		objmap.put("toDate", mapObject.get("ToDateWOUTC"));
		objmap.put("fromdate", mapObject.get("FromDateWOUTC"));
		objmap.put("todate", mapObject.get("ToDateWOUTC"));
		final Instant date = dateUtilityFunction.getUTCDateTime();
		objmap.put("Currentdate", dateUtilityFunction.instantDateToStringWithFormat(date, "dd/MM/yyyy"));
		final String strQuery = "select nmaterialtypecode,jsondata->'smaterialtypename'->>'"
				+ objUserInfo.getSlanguagetypecode() + "' "
				+ "as smaterialtypename,jsondata  from materialtype where nmaterialtypecode>-1 and nstatus=1 order by nmaterialtypecode";

		final List<MaterialType> lstMaterialType = jdbcTemplate.query(strQuery, new MaterialType());
		objmap.put("MaterialType", lstMaterialType);
		objmap.put("SelectedMaterialType", lstMaterialType);
		objmap.put("SelectedMaterialTypeFilter", lstMaterialType);

		final String strquery1 = "select nmaterialcatcode,smaterialcatname,ndefaultstatus from materialcategory where "
				+ "  nmaterialtypecode=" + lstMaterialType.get(0).getNmaterialtypecode()
				+ " and nstatus=1  order by nmaterialcatcode";
		final List<MaterialCategory> lstMaterialCategory = jdbcTemplate.query(strquery1, new MaterialCategory());
		if (!lstMaterialCategory.isEmpty()) {
			objmap.put("MaterialCategoryMain", lstMaterialCategory);
			final Optional<?> materialCategoryObj = lstMaterialCategory.stream()
					.filter(t -> t.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
					.findAny();

			if (materialCategoryObj.isPresent()) {
				final MaterialCategory objMaterialCategory = (MaterialCategory) materialCategoryObj.get();
				objmap.put("nmaterialcatcode", objMaterialCategory.getNmaterialcatcode());
			} else {
				objmap.put("nmaterialcatcode", lstMaterialCategory.get(0).getNmaterialcatcode());
			}

		}
		objmap.put("nmaterialtypecode", lstMaterialType.get(0).getNmaterialtypecode());
		objmap.putAll(getMaterialByTypeCode(objmap, objUserInfo).getBody());
		objmap.putAll(getMaterialAdd((short) objmap.get("nmaterialtypecode"), objUserInfo).getBody());

		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMaterial() throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final String query1 = "select nmaterialcode,jsondata||json_build_object('nmaterialcode',nmaterialcode)::jsonb "
				+ " as jsondata from material where nmaterialcode>0 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<Material> lstMaterial = jdbcTemplate.query(query1, new Material());
		objmap.put("Material", lstMaterial);
		if (!lstMaterial.isEmpty()) {
			objmap.put("SelectedMaterial", lstMaterial.get(lstMaterial.size() - 1));
		} else {
			objmap.put("SelectedMaterial", lstMaterial);
		}
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMaterialByID(final Map<String, Object> inputMap) throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> lstMaterial = new ArrayList<Map<String, Object>>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String query1 = "select json_agg(a.jsonuidata ||jsonb_build_object('ntransactionstatus',a.ntransactionstatus))"
				+ " from (select jsonuidata||json_build_object('nmaterialcode',nmaterialcode)::jsonb "
				+ " as jsonuidata, ntransactionstatus from material where  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialcode="
				+ inputMap.get("nmaterialcode") + ")a";
		final String strMaterial = jdbcTemplate.queryForObject(query1, String.class);
		if (strMaterial != null)
			lstMaterial = objMapper.readValue(strMaterial, new TypeReference<List<Map<String, Object>>>() {
			});

		if (lstMaterial.size() == 0) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		objmap.put("SelectedMaterial", lstMaterial.size() > 0 ? lstMaterial.get(lstMaterial.size() - 1) : lstMaterial);
		final String materialtypecode = "select (jsondata->>'nmaterialtypecode')::int  from material where nmaterialcode="
				+ inputMap.get("nmaterialcode");
		final int nmaterialtypecode = jdbcTemplate.queryForObject(materialtypecode, Integer.class);

		if (nmaterialtypecode == 5) {
			objmap.putAll(getMaterialInventory((int) inputMap.get("nmaterialcode"), inputMap).getBody());
			objmap.putAll(getAvailableQtyAndCloseQty(inputMap, userInfo).getBody());

		} else {
			objmap.putAll(objMaterialSectionDAO
					.getMaterialSectionByMaterialCode((int) inputMap.get("nmaterialcode"), inputMap).getBody());
			objmap.putAll(getMaterialFile((int) inputMap.get("nmaterialcode"), userInfo));
		}
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMaterialcombo(final Integer nmaterialtypecode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final String comboget = "select nmaterialcatcode,smaterialcatname,ndefaultstatus,nmaterialtypecode from materialcategory where nmaterialtypecode="
				+ nmaterialtypecode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<MaterialCategory> lstMaterialCategory = jdbcTemplate.query(comboget, new MaterialCategory());
		objmap.put("MaterialCategoryMain", lstMaterialCategory);
		if (!lstMaterialCategory.isEmpty()) {
			final Optional<?> materialCategoryObj = lstMaterialCategory.stream()
					.filter(t -> t.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
					.findAny();
			if (materialCategoryObj.isPresent()) {
				objmap.put("SelectedMaterialCategory", materialCategoryObj.get());
			} else {
				objmap.put("SelectedMaterialCategory", lstMaterialCategory.get(0));
			}
		} else {
			objmap.put("SelectedMaterialCategory", null);
		}
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getMaterialAdd(final short nmaterialtypecode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> objmap = new HashMap<>();
		final String comboget = "SELECT *  from materialconfig where  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nformcode="
				+ objUserInfo.getNformcode() + " and nmaterialtypecode=" + nmaterialtypecode + ";";
		final List<MaterialConfig> lstMaterialCategory = jdbcTemplate.query(comboget, new MaterialConfig());
		objmap.put("selectedTemplate", lstMaterialCategory);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createMaterial(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final List<Map<String, Object>> lstaudit = new ArrayList<>();
		final JSONObject actionType = new JSONObject();
		final JSONArray jsonUidataarray = new JSONArray();
		final JSONObject jsonAuditObject = new JSONObject();
		final ObjectMapper objmapper = new ObjectMapper();
		JSONObject jsonObject = new JSONObject(inputMap.get("materialJson").toString());
		JSONObject jsonuidata = new JSONObject(inputMap.get("jsonuidata").toString());

		final List<String> lstDateField = (List<String>) inputMap.get("DateList");

		final String str = "select nmaterialcatcode from materialcategory " + " where nmaterialcatcode="
				+ inputMap.get("nmaterialcatcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final MaterialCategory ObjMatNamecheck1 = (MaterialCategory) jdbcUtilityFunction.queryForObject(str,
				MaterialCategory.class, jdbcTemplate);

		if (ObjMatNamecheck1 == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALCATEGORYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
		String strPrefix = "";
		if (!lstDateField.isEmpty()) {
			jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, lstDateField,
					false, objUserInfo);
			jsonuidata = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuidata, lstDateField,
					false, objUserInfo);
		}
		final String strcheck = " select *  from material   where lower((jsondata->>'Material Name'))=lower('"
				+ stringUtilityFunction.replaceQuote(jsonObject.get("Material Name").toString()) + "') "
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Material ObjMatNamecheck = (Material) jdbcUtilityFunction.queryForObject(strcheck, Material.class,
				jdbcTemplate);
		if (ObjMatNamecheck == null) {
			if (jsonObject.has("Prefix")) {
				strPrefix = " select (jsondata->>'Prefix')  from material" + "  where (jsondata->>'Prefix')='"
						+ stringUtilityFunction.replaceQuote(jsonObject.get("Prefix").toString())
						+ "' and (jsondata->'nmaterialtypecode')::int= " + inputMap.get("nmaterialtypecode")
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and (jsondata->>'Prefix')!='' ";
				final Material ObjcheckPrefix = (Material) jdbcUtilityFunction.queryForObject(strPrefix, Material.class,
						jdbcTemplate);

				if (ObjcheckPrefix != null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PREFIXALREADYEXISTS",
							objUserInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYEXISTS", objUserInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		int seqnomaterial = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnomaterialmanagement where stablename='material' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);
		seqnomaterial++;
		String insmat = " INSERT INTO material( nmaterialcode,nmaterialcatcode,ntransactionstatus,jsondata,jsonuidata,dmodifieddate, nsitecode, nstatus)"
				+ " VALUES (" + seqnomaterial + ", " + jsonObject.get("nmaterialcatcode") + " ,"
				+ inputMap.get("ntransactionstatus") + ",'" + stringUtilityFunction.replaceQuote(jsonObject.toString())
				+ "'::jsonb,'" + stringUtilityFunction.replaceQuote(jsonuidata.toString()) + "'::jsonb ,'"
				+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + objUserInfo.getNmastersitecode() + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

		insmat += " INSERT INTO public.seqnomaterialinventory(stablename, nsequenceno, nstatus)	VALUES ("
				+ seqnomaterial + ",0, " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

		jdbcTemplate.execute(insmat);
		jdbcTemplate.execute(
				"update seqnomaterialmanagement set nsequenceno=" + seqnomaterial + "  where stablename='material' ");
		if ((int) inputMap.get("needsectionwise") == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			jsonObject = new JSONObject(inputMap.get("materialSectionJson").toString());
			jsonObject.put("nmaterialcode", seqnomaterial);
			inputMap.put("nmaterialcode", seqnomaterial);
			inputMap.put("materialSectionJson", jsonObject);
			objMaterialSectionDAO.createMaterialSection(inputMap);
		}
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		inputMap.put("nmaterialconfigcode", jsonuidata.get("nmaterialconfigcode"));
		inputMap.put("nflag", 1);
		objmap.putAll(getMaterialByTypeCode(inputMap, userInfo).getBody());
		objmap.put("nregtypecode", -1);
		objmap.put("nregsubtypecode", -1);
		objmap.put("ndesigntemplatemappingcode", jsonuidata.get("nmaterialconfigcode"));
		actionType.put("Material", "IDS_ADDMATERIAL");
		jsonUidataarray.put(jsonuidata);
		lstaudit.add((Map<String, Object>) objmap.get("SelectedMaterial"));
		jsonAuditObject.put("Material", lstaudit);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, objmap, false, userInfo);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMaterialDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final String query1 = "select json_agg(a.jsonuidata ||jsonb_build_object('ntransactionstatus',a.ntransactionstatus))  from (select nmaterialcode,jsonuidata||json_build_object('nmaterialcode',nmaterialcode)::jsonb "
				+ " as jsonuidata, ntransactionstatus from material where  nstatus=1 and nmaterialcode="
				+ inputMap.get("nmaterialcode") + ")a";
		final String strMaterial1 = jdbcTemplate.queryForObject(query1, String.class);
		List<Map<String, Object>> lstMaterial;
		if (strMaterial1 != null) {
			lstMaterial = objmapper.readValue(strMaterial1, new TypeReference<List<Map<String, Object>>>() {
			});
		} else {
			lstMaterial = null;
		}
		final String query2 = "select json_agg(a.jsonuidata ||jsonb_build_object('ntransactionstatus',a.ntransactionstatus))  from (select m.nmaterialcode,m.jsonuidata||json_build_object('nmaterialcode',m.nmaterialcode)::jsonb "
				+ " as jsonuidata, m.ntransactionstatus from material m,materialcategory mc where  m.nstatus=1 and (m.jsondata->'nmaterialtypecode')::int="
				+ inputMap.get("nmaterialtypecode") + " and (m.jsondata->'nmaterialcatcode')::int="
				+ inputMap.get("nmaterialcatcode")
				+ " and mc.nmaterialcatcode= (jsondata -> 'nmaterialcatcode' ) :: INT" + " order by m.nmaterialcode)a";
		final String strMaterial = jdbcTemplate.queryForObject(query2, String.class);

		final List<Map<String, Object>> lstMaterial2 = objmapper.convertValue(
				projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterial, userInfo, true,
						(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
								.gettransactionstatus()
										? 1
										: (int) inputMap.get(
												"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
														.gettransactionstatus() ? 2 : 3,
						(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
								.gettransactionstatus()
										? "MaterialStandard"
										: (int) inputMap.get(
												"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
														.gettransactionstatus() ? "MaterialVolumetric"
																: "MaterialMaterialInventory"),
				new TypeReference<List<Map<String, Object>>>() {
				});

		if (lstMaterial != null) {
			objmap.put("Material", lstMaterial2);
			objmap.put("SelectedMaterial", lstMaterial.get(lstMaterial.size() - 1));
			objmap.putAll(objMaterialSectionDAO
					.getMaterialSectionByMaterialCode((int) inputMap.get("nmaterialcode"), inputMap).getBody());
			objmap.put("nmaterialcode", inputMap.get("nmaterialcode"));
			if ((int) inputMap.get("nmaterialtypecode") == 5) {

				/*
				 * final Map<String, Object> objmap1 = new LinkedHashMap<String, Object>();
				 * final String queryst =
				 * "select miv.nmaterialinventtranscode,miv.nqtyreceived as sqtyreceived,miv.nqtyissued as sqtyissued,miv.jsonuidata->>'Comments' as scomments,miv.jsonuidata->>'Transaction Date' as stransactiondate,   "
				 * + " mt.jsondata->'sinventorytypename'->>'" + userInfo.getSlanguagetypecode()
				 * + "'  as sinventorytypename" +
				 * " from materialinventorytransaction miv,materialinventory mi,materialinventorytype mt where mt.ninventorytypecode=miv.ninventorytranscode and miv.nmaterialinventorycode=mi.nmaterialinventorycode and mi.nmaterialcode="
				 * + (int) lstMaterial.get(lstMaterial.size() - 1).get("nmaterialcode") +
				 * " and (miv.jsonuidata->>'Transaction Date')::date between ('"+(String)
				 * inputMap.get("fromDate")+"')::date and ('"+(String)
				 * inputMap.get("toDate")+"')::date"; List<MaterialInventoryTrans>
				 * lstMaterialInvntory = (List<MaterialInventoryTrans>)
				 * jdbcTemplate.query(queryst, new MaterialInventoryTrans());
				 * objmap.put("MaterialSection", lstMaterialInvntory);
				 */

				/*
				 * String
				 * str="select case  when  (sum(mit.nqtyreceived)-sum(mit.nqtyissued)) isnull then 0 else ((sum(mit.nqtyreceived)-sum(mit.nqtyissued)) * (mi.jsonuidata->>'suraniumconversionfactor')::float) end as navailableuraniumqty from materialinventorytransaction mit,materialinventory mi "
				 * +" where mit.nstatus=1 and mi.nstatus=1 and   mit.nmaterialinventorycode=mi.nmaterialinventorycode and nmaterialcode="
				 * +(int) lstMaterial.get(lstMaterial.size() - 1).get("nmaterialcode")
				 * +" and (mit.jsonuidata->>'Transaction Date')::date<=('"+(String)
				 * inputMap.get("toDate")+"')::date" +
				 * " group by (mi.jsonuidata->>'suraniumconversionfactor')::float"; //double
				 * CloseQty = jdbcTemplate.queryForObject(str, double.class); final
				 * MaterialInventoryType objinventorytypeQty = (MaterialInventoryType)
				 * jdbcUtilityFunction.queryForObject(str, MaterialInventoryType.class,
				 * jdbcTemplate);
				 * 
				 * 
				 * String
				 * OpenQtystr="select case  when  (sum(mit.nqtyreceived)-sum(mit.nqtyissued)) isnull then 0 else ((sum(mit.nqtyreceived)-sum(mit.nqtyissued)) * (mi.jsonuidata->>'suraniumconversionfactor')::float) end  as navailableuraniumqty from materialinventorytransaction mit,materialinventory mi "
				 * +" where mit.nstatus=1 and mi.nstatus=1 and   mit.nmaterialinventorycode=mi.nmaterialinventorycode and nmaterialcode="
				 * +(int) lstMaterial.get(lstMaterial.size() - 1).get("nmaterialcode")
				 * +" and (mit.jsonuidata->>'Transaction Date')::date<('"+(String)
				 * inputMap.get("fromDate")+"')::date" +
				 * " group by (mi.jsonuidata->>'suraniumconversionfactor')::float"; //double
				 * OpenQty = jdbcTemplate.queryForObject(OpenQtystr, double.class);
				 * 
				 * final MaterialInventoryType objinventorytypeOpenQty = (MaterialInventoryType)
				 * jdbcUtilityFunction.queryForObject(OpenQtystr, MaterialInventoryType.class,
				 * jdbcTemplate); objmap.put("CloseQty", objinventorytypeQty !=null ?
				 * objinventorytypeQty.getNavailableuraniumqty():0); objmap.put("OpenQty",
				 * objinventorytypeOpenQty!=null ?
				 * objinventorytypeOpenQty.getNavailableuraniumqty():0);
				 */
				objmap.putAll(getMaterialInventory((int) lstMaterial.get(lstMaterial.size() - 1).get("nmaterialcode"),
						inputMap).getBody());
				objmap.putAll(getAvailableQtyAndCloseQty(inputMap, userInfo).getBody());

			} else {
				objmap.put("MaterialTypeValidation", (int) inputMap.get("nmaterialtypecode") == 5);
				objmap.putAll((Map<String, Object>) getMaterialSafetyInstructions(objmap, userInfo).getBody());
				objmap.putAll(getMaterialFile((int) inputMap.get("nmaterialcode"), userInfo));
				objmap.put("DesignMappedFeilds", getTemplateDesignForMaterial(
						(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
								.gettransactionstatus()
										? 1
										: (int) inputMap.get(
												"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
														.gettransactionstatus() ? 2 : 3,
						userInfo.getNformcode()));

			}

			return new ResponseEntity<>(objmap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<? extends Object> deleteMaterial(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		JSONObject jsonAuditOld = new JSONObject();
		Map<String, Object> mapAuditOld = new LinkedHashMap<>();
		final ObjectMapper objmapper = new ObjectMapper();
		final JSONObject actionType = new JSONObject();
		final StringBuffer sB = new StringBuffer();
		final int intcount = jdbcTemplate.queryForObject(
				"select count(nmaterialcode) from material where nmaterialcode in (" + inputMap.get("nmaterialcode")
						+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
				Integer.class);
		/*
		 * int intcount1 = jdbcTemplate
		 * .queryForObject("select count(nmaterialcode) from materialinventory where nmaterialcode in ("
		 * + inputMap.get("nmaterialcode") + ") and nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
		 * Integer.class); int intcount2 = jdbcTemplate
		 * .queryForObject("select count(nmaterialcode) from testgrouptestmaterial where nmaterialcode in ("
		 * + inputMap.get("nmaterialcode") + ") and nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
		 * Integer.class);
		 */
		/*
		 * if (intcount2 == 0) { if (intcount1 == 0) {
		 */
		if (intcount != 0) {
			jsonAuditOld = new JSONObject(jdbcTemplate.queryForObject(
					"select json_build_object('Material',json_agg(distinct m.jsonuidata||jsonb_build_object('nmaterialcode',m.nmaterialcode)), "
							+ "'materialsection',json_agg(distinct ms.jsondata||jsonb_build_object('nmaterialcode',m.nmaterialcode,'nmaterialsectioncode',ms.nmaterialsectioncode)) "
							+ ",'materialmsdsattachment',json_agg(distinct msds.jsondata||jsonb_build_object('nmaterialfilecode',msds.nmaterialfilecode,'nmaterialcode',m.nmaterialcode)),'materialproperties',json_agg(distinct mp.jsonuidata||jsonb_build_object('nmaterialcode',m.nmaterialcode,'nmaterialpropertycode',mp.nmaterialpropertycode)), "
							+ "'materialsafetyinstructions',json_agg(distinct msf.jsondata||jsonb_build_object('nmaterialcode',m.nmaterialcode,'nmaterialsafetyinstructionscode',msf.nmaterialsafetyinstructionscode)))  "
							+ "from material m  left outer join   "
							+ " materialsection ms on m.nmaterialcode=ms.nmaterialcode    " + " and m.nstatus=1 "
							+ " and ms.nstatus=1  " + " inner join material me on " + "	 me.nmaterialcode="
							+ inputMap.get("nmaterialcode") + "	and " + " me.nmaterialcode=m.nmaterialcode "
							+ " left outer join materialmsdsattachment msds  on "
							+ " m.nmaterialcode=msds.nmaterialcode  " + " left outer join materialproperties mp "
							+ " on mp.nmaterialcode=m.nmaterialcode "
							+ " left outer join materialsafetyinstructions msf "
							+ " on msf.nmaterialcode=m.nmaterialcode",
					String.class));
			mapAuditOld = objmapper.convertValue(jsonAuditOld.toMap(), new TypeReference<Map<String, Object>>() {
			});

			final String query = "select 'IDS_MATERIALINVENTORY' as Msg from materialinventory where nmaterialcode= "
					+ inputMap.get("nmaterialcode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all"
					+ " select 'IDS_TESTGROUP' as Msg from testgrouptestmaterial where nmaterialcode= "
					+ inputMap.get("nmaterialcode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all"
					+ " select 'IDS_RESULTUSEDMATERIAL' as Msg from resultusedmaterial where nmaterialcode= "
					+ inputMap.get("nmaterialcode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//							+ " union all "
//							+ " SELECT 'IDS_REGISTRATION' as Msg FROM registration r "
//							+ " JOIN jsonb_each(r.jsondata) d ON true where "
//							+ " d.value->>'pkey' ='nunitcode' and d.value->>'nquerybuildertablecode'='253' "
//							+ " and d.value->>'value'='" + objUnit.getNunitcode()  + "'"
//							+ " union all "
//							+ " SELECT 'IDS_REGISTRATIONSAMPLE' as Msg FROM registrationsample rs "
//							+ " JOIN jsonb_each(rs.jsondata) d1 ON true where "
//							+ " d1.value->>'pkey' ='nunitcode' and d1.value->>'nquerybuildertablecode'='253' "
//							+ " and d1.value->>'value'='" + objUnit.getNunitcode()  + "'";

			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				valiDatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString((int) inputMap.get("nmaterialcode")), userInfo);
				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {

				sB.append(" update material set nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nmaterialcode in (" + inputMap.get("nmaterialcode") + ");");
				sB.append(" update materialmsdsattachment set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialcode in ("
						+ inputMap.get("nmaterialcode") + ");");
				sB.append(" update materialproperties set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialcode in ("
						+ inputMap.get("nmaterialcode") + ");");
				sB.append(" update materialsafetyinstructions set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialcode in ("
						+ inputMap.get("nmaterialcode") + ");");
				sB.append(" update materialsection set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialcode in ("
						+ inputMap.get("nmaterialcode") + ");");
				sB.append(" update seqnomaterialinventory set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where stablename in ('"
						+ inputMap.get("nmaterialcode") + "');");
				jdbcTemplate.execute(sB.toString());

				inputMap.put("nregtypecode", -1);
				inputMap.put("nregsubtypecode", -1);
				inputMap.put("ndesigntemplatemappingcode", inputMap.get("nmaterialconfigcode"));
				actionType.put("materialsection", "IDS_DELETEMATERIALSECTION");
				actionType.put("Material", "IDS_DELETEMATERIAL");
				actionType.put("materialproperties", "IDS_DELETEMATERIALPROPERTIES");
				actionType.put("materialmsdsattachment", "IDS_DELETEMATERIALMSDSATTACHEMENT");
				actionType.put("materialsafetyinstructions", "IDS_DELETEMATERIALSAFETYINSTRUCTIONS");
				jsonAuditOld.put("Material", (List<Map<String, Object>>) mapAuditOld.get("Material"));
				jsonAuditOld.put("materialsection", (List<Map<String, Object>>) mapAuditOld.get("materialsection"));

				if ((((ArrayList<Map<String, Object>>) mapAuditOld.get("materialproperties")).get(0) != null)
						&& ((ArrayList<Map<String, Object>>) mapAuditOld.get("materialproperties")).get(0)
								.get("Date of Standardization") != null) {

					final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					final String dateString = (String) ((ArrayList<Map<String, Object>>) mapAuditOld
							.get("materialproperties")).get(0).get("Date of Standardization");

					final Date date = formatter.parse(dateString);
					((ArrayList<Map<String, Object>>) mapAuditOld.get("materialproperties")).get(0)
							.put("Date of Standardization", new SimpleDateFormat(userInfo.getSsitedate()).format(date));
				}
				jsonAuditOld.put("materialproperties",
						(List<Map<String, Object>>) mapAuditOld.get("materialproperties"));
				jsonAuditOld.put("materialmsdsattachment",
						(List<Map<String, Object>>) mapAuditOld.get("materialmsdsattachment"));
				jsonAuditOld.put("materialsafetyinstructions",
						(List<Map<String, Object>>) mapAuditOld.get("materialsafetyinstructions"));
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, null, actionType, inputMap, false, userInfo);
				inputMap.put("nflag", 1);
				return getMaterialByTypeCode(inputMap, userInfo);
			} else {
				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> getMaterialEdit(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		List<Map<String, Object>> lstMaterial = new ArrayList<Map<String, Object>>();
		List<Integer> lstcount = new ArrayList<Integer>();
		List<MappedTemplateFieldPropsMaterial> lstsearchFields = new LinkedList<>();
		final List<String> lstsearchField = new LinkedList<String>();
		final List<String> submittedDateFeilds = new LinkedList<String>();

		final String type = (int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
				.gettransactionstatus() ? "MaterialStandard"
						: (int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
								.gettransactionstatus() ? "MaterialVolumetric" : "MaterialMaterialInventory";
		final int typeCode = (int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
				.gettransactionstatus() ? 1
						: (int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
								.gettransactionstatus() ? 2 : 3;
		lstcount = jdbcTemplate.queryForList(
				"select nmaterialcode from material where nmaterialcode in (" + inputMap.get("nmaterialcode")
						+ ") and nstatus=" + Enumeration.TransactionStatus.NA.gettransactionstatus() + "",
				Integer.class);
		if (lstcount.size() == 0) {
			final String selectionKeyName1 = "select (jsondata->'" + userInfo.getNformcode() + "'->'" + type
					+ "datefields')::jsonb as jsondata from mappedtemplatefieldpropsmaterial  where   nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialconfigcode="
					+ typeCode;
			lstsearchFields = jdbcTemplate.query(selectionKeyName1, new MappedTemplateFieldPropsMaterial());

			final JSONArray objarray = new JSONArray(lstsearchFields.get(0).getJsondata());
			for (int i = 0; i < objarray.length(); i++) {
				final JSONObject jsonobject = new JSONObject(objarray.get(i).toString());
				if (jsonobject.has("2")) {
					lstsearchField.add((String) jsonobject.get("2"));
				}
			}
			objmap.put("DateFeildsMaterial", lstsearchField);

			final String query1 = " select json_agg(a.jsondata) from(select jsondata||json_build_object('needsectionwise',mc.needsectionwise)::jsonb as jsondata  from  material m,materialcategory"
					+ " mc where m.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and m.nmaterialcode in (" + inputMap.get("nmaterialcode")
					+ ") and (jsondata->'nmaterialcatcode')::int=mc.nmaterialcatcode)a;";
			final String objMaterial = jdbcTemplate.queryForObject(query1, String.class);
			lstMaterial = objmapper.convertValue(
					projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(objMaterial, userInfo, true,
							(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? 1
											: (int) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? 2 : 3,
							type),
					new TypeReference<List<Map<String, Object>>>() {
					});
			if (!lstMaterial.isEmpty()) {
				for (int i = 0; i < lstsearchField.size(); i++) {
					if (lstMaterial.get(i).get(lstsearchField.get(i)) != null) {
						submittedDateFeilds.add(lstsearchField.get(i));
					}
				}
			}
			if (!submittedDateFeilds.isEmpty()) {
				objmap.put("MaterialDateFeild", submittedDateFeilds);
			}

			objmap.put("EditedMaterial", lstMaterial);
			return new ResponseEntity<>(objmap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> updateMaterial(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final JSONObject jsonAuditOld = new JSONObject();
		final JSONObject jsonAuditNew = new JSONObject();
		final JSONArray newJsonUiDataArray = new JSONArray();
		final JSONObject actionType = new JSONObject();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final List<String> lstDateField = (List<String>) inputMap.get("DateList");
		List<Integer> ismaterialSectionneed = new ArrayList<Integer>();
		JSONObject jsonObject = new JSONObject(inputMap.get("materialJson").toString());
		JSONObject jsonuidata = new JSONObject(inputMap.get("jsonuidata").toString());

		final String str = "select nmaterialcatcode from materialcategory " + " where nmaterialcatcode="
				+ inputMap.get("nmaterialcatcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final MaterialCategory ObjMatNamecheck1 = (MaterialCategory) jdbcUtilityFunction.queryForObject(str,
				MaterialCategory.class, jdbcTemplate);

		if (ObjMatNamecheck1 == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALCATEGORYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

		final JSONObject jsonAuditObjectold = new JSONObject();
		List<Map<String, Object>> deleteSectionAudit = new ArrayList<>();

		if (!lstDateField.isEmpty()) {
			jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, lstDateField,
					false, userInfo);
			jsonuidata = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuidata, lstDateField,
					false, userInfo);
		}
		String strcheck = "";
		String strPrefix = "";
		boolean nflag = false;
		/*
		 * if ((int) inputMap.get("nmaterialtypecode") ==
		 * Enumeration.TransactionStatus.STANDARDTYPE .gettransactionstatus()) {
		 * strcheck = " select nmaterialcode  from material" + "  where (jsondata->>'" +
		 * Enumeration.ReturnStatus.STANDARDMATERIALNAME.getreturnstatus() + "')='" +
		 * jsonObject.get("Standard Name") +
		 * "'  and (jsondata->'nmaterialcatcode')::int= " +
		 * inputMap.get("nmaterialcatcode") + " and nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and nmaterialcode<>" + inputMap.get("nmaterialcode");
		 * 
		 * } else if ((int) inputMap.get("nmaterialtypecode") ==
		 * Enumeration.TransactionStatus.VOLUMETRICTYPE .gettransactionstatus()) {
		 * strcheck = " select nmaterialcode  from material" + "  where (jsondata->>'" +
		 * Enumeration.ReturnStatus.VOLUMETRICMATERIALNAME.getreturnstatus() + "')='" +
		 * jsonObject.get("Volumetric Name") +
		 * "' and (jsondata->'nmaterialcatcode')::int= " +
		 * inputMap.get("nmaterialcatcode") + " and nstatus=" +
		 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
		 * " and nmaterialcode<>" + inputMap.get("nmaterialcode");
		 * 
		 * } else if ((int) inputMap.get("nmaterialtypecode") ==
		 * Enumeration.TransactionStatus.MATERIALINVENTORYTYPE .gettransactionstatus())
		 * {
		 */
		// edit

		strcheck = " select nmaterialcode  from material where nmaterialcode =" + inputMap.get("nmaterialcode")
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		Material ObjMatNamecheck = (Material) jdbcUtilityFunction.queryForObject(strcheck, Material.class,
				jdbcTemplate);

		if (ObjMatNamecheck == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		strcheck = " select nmaterialcode  from material" + "  where lower((jsondata->>'Material Name'))=lower('"
				+ stringUtilityFunction.replaceQuote(jsonObject.get("Material Name").toString())
				+ "') and (jsondata->'nmaterialcatcode')::int= " + inputMap.get("nmaterialcatcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialcode<>"
				+ inputMap.get("nmaterialcode");

		// }
		ObjMatNamecheck = (Material) jdbcUtilityFunction.queryForObject(strcheck, Material.class, jdbcTemplate);

		if (ObjMatNamecheck == null) {
			if (jsonObject.has("Prefix")) {
				nflag = true;
				strPrefix = " select nmaterialcode  from material" + "  where (jsondata->>'Prefix')='"
						+ stringUtilityFunction.replaceQuote(jsonObject.get("Prefix").toString())
						+ "' and (jsondata->'nmaterialtypecode')::int= " + inputMap.get("nmaterialtypecode")
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nmaterialcode<>" + inputMap.get("nmaterialcode") + " and (jsondata->>'Prefix')!='' ";
				;
				final Material lstcheckPrefix = (Material) jdbcUtilityFunction.queryForObject(strPrefix, Material.class,
						jdbcTemplate);

				if (lstcheckPrefix != null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PREFIXALREADYEXISTS",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}

			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYEXISTS", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		final JSONArray oldJsonUiDataArray = new JSONArray(jdbcTemplate.queryForObject(
				"Select json_agg(jsonuidata||jsonb_build_object('nmaterialcode',nmaterialcode)) from material where nmaterialcode="
						+ inputMap.get("nmaterialcode") + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				String.class));

		final JSONObject needSectionWise = new JSONObject(
				jdbcTemplate.queryForObject(
						"Select jsondata from material where nmaterialcode=" + inputMap.get("nmaterialcode")
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						String.class));
		final int CheckNeedSectionWiseForOldMatCat = needSectionWise.getJSONObject("Material Category")
				.getJSONObject("item").getJSONObject("jsondata").getInt("needsectionwise");

		final String query1 = " update material set nmaterialcatcode=" + jsonObject.get("nmaterialcatcode")
				+ ",jsondata='" + stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::jsonb,jsonuidata='"
				+ stringUtilityFunction.replaceQuote(jsonuidata.toString()) + "'::jsonb,ntransactionstatus="
				+ inputMap.get("ntransactionstatus") + ", dmodifieddate ='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialcode in ("
				+ inputMap.get("nmaterialcode") + ");";
		jdbcTemplate.execute(query1);

		final String strMatCatLst = "select * from materialcategory where nmaterialcatcode = "
				+ jsonObject.get("nmaterialcatcode") + " and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Material matCatLst = (Material) jdbcUtilityFunction.queryForObject(strMatCatLst, Material.class,
				jdbcTemplate);
		objmap.put("nregtypecode", -1);
		objmap.put("nregsubtypecode", -1);
		objmap.put("ndesigntemplatemappingcode", jsonuidata.get("nmaterialconfigcode"));
		if (matCatLst != null) {
			final String strMatSectionLst = "select count(*) as ncount from materialsection where nmaterialcode = "
					+ inputMap.get("nmaterialcode") + " and nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final Material matSectonLst = (Material) jdbcUtilityFunction.queryForObject(strMatSectionLst,
					Material.class, jdbcTemplate);
			if (matCatLst.getNeedsectionwise() != Enumeration.TransactionStatus.YES.gettransactionstatus()
					&& matSectonLst.getNcount() > 0) {

				deleteSectionAudit = objmapper.convertValue(new JSONArray(jdbcTemplate.queryForObject(
						"select json_agg(ms.jsondata||m.jsonuidata::jsonb) from materialsection ms,material m where "
								+ " m.nmaterialcode=ms.nmaterialcode and ms.nmaterialcode in ("
								+ inputMap.get("nmaterialcode") + ") and m.nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ms.nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						String.class)).toList(), new TypeReference<List<Map<String, Object>>>() {
						});

				final String strDeleteSection = " update materialsection set nstatus ="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialcode in ("
						+ inputMap.get("nmaterialcode") + ") and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(strDeleteSection);

				jsonAuditObjectold.put("materialsection", deleteSectionAudit);
				final JSONObject deleteActionType = new JSONObject();
				deleteActionType.put("materialsection", "IDS_REMOVEMATERIALSECTION");
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectold, null, deleteActionType, objmap, false,
						userInfo);
			}
		}

		if ((int) inputMap.get("needsectionwise") == Enumeration.TransactionStatus.YES.gettransactionstatus()
				&& CheckNeedSectionWiseForOldMatCat == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			jsonObject = new JSONObject(inputMap.get("materialSectionJson").toString());
			jsonObject.put("nmaterialcode", inputMap.get("nmaterialcode"));
			inputMap.put("nmaterialcode", inputMap.get("nmaterialcode"));
			inputMap.put("materialSectionJson", jsonObject);
			objMaterialSectionDAO.createMaterialSection(inputMap);
			objmap.putAll(objMaterialSectionDAO
					.getMaterialSectionByMaterialCode((int) inputMap.get("nmaterialcode"), inputMap).getBody());
		}
		final String query3 = "select json_agg(a.jsonuidata ||jsonb_build_object('ntransactionstatus',a.ntransactionstatus)) from (select nmaterialcode,jsonuidata||json_build_object('nmaterialcode',nmaterialcode)::jsonb "
				+ " as jsonuidata, ntransactionstatus from material where  nstatus=1 and (jsondata->'nmaterialcatcode')::int="
				+ inputMap.get("nmaterialcatcode") + " and  nmaterialcode=" + inputMap.get("nmaterialcode") + ")a";
		// List<Material> lstMaterial = (List<Material>) jdbcTemplate.query(query3, new
		// Material());
		final String strSelectedMaterial = jdbcTemplate.queryForObject(query3, String.class);
		final List<Map<String, Object>> lstMaterial = objmapper.convertValue(
				projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strSelectedMaterial, userInfo, true,
						(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
								.gettransactionstatus()
										? 1
										: (int) inputMap.get(
												"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
														.gettransactionstatus() ? 2 : 3,
						(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
								.gettransactionstatus()
										? "MaterialStandard"
										: (int) inputMap.get(
												"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
														.gettransactionstatus() ? "MaterialVolumetric"
																: "MaterialMaterialInventory"),
				new TypeReference<List<Map<String, Object>>>() {
				});
		final String query4 = "select json_agg(a.jsonuidata ||jsonb_build_object('ntransactionstatus',a.ntransactionstatus)) from (select m.nmaterialcode,m.jsonuidata||json_build_object('nmaterialcode',m.nmaterialcode)::jsonb "
				+ " as jsonuidata, m.ntransactionstatus from material m,materialcategory mc where  m.nstatus=1 and (m.jsondata->'nmaterialtypecode')::int="
				+ inputMap.get("nmaterialtypecode") + " and (m.jsondata->'nmaterialcatcode')::int="
				+ inputMap.get("nmaterialcatcode")
				+ " and mc.nmaterialcatcode= (jsondata -> 'nmaterialcatcode' ) :: INT" + " order by m.nmaterialcode)a";
		final String strMaterial = jdbcTemplate.queryForObject(query4, String.class);
		final List<Map<String, Object>> objMaterial2 = objmapper.convertValue(
				projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterial, userInfo, true,
						(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
								.gettransactionstatus()
										? 1
										: (int) inputMap.get(
												"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
														.gettransactionstatus() ? 2 : 3,
						(int) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
								.gettransactionstatus()
										? "MaterialStandard"
										: (int) inputMap.get(
												"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
														.gettransactionstatus() ? "MaterialVolumetric"
																: "MaterialMaterialInventory"),
				new TypeReference<List<Map<String, Object>>>() {
				});
		objmap.put("Material", objMaterial2);
		objmap.put("SelectedMaterial", lstMaterial.get(0));

		final String query2 = "select nmaterialtypecode,jsondata->'smaterialtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "' "
				+ "as smaterialtypename,jsondata  from materialtype where nmaterialtypecode="
				+ inputMap.get("nmaterialtypecode") + " and nstatus=1";

		final List<MaterialType> lstMaterialType = jdbcTemplate.query(query2, new MaterialType());
		objmap.put("SelectedMaterialType", lstMaterialType);

		final String SelectedMaterialCategory = "select nmaterialcatcode,smaterialcatname,needSectionwise from materialcategory where nmaterialcatcode="
				+ inputMap.get("nmaterialcatcode") + " and nstatus=1";

		final List<MaterialCategory> lstMaterialCategory = jdbcTemplate.query(SelectedMaterialCategory,
				new MaterialCategory());
		ismaterialSectionneed = jdbcTemplate.queryForList("select needsectionwise  "
				+ "from materialcategory where nmaterialcatcode=" + inputMap.get("nmaterialcatcode"), Integer.class);
		objmap.put("ismaterialSectionneed", ismaterialSectionneed.get(0));
		if (ismaterialSectionneed.get(0) == 3) {
			objmap.put("tabScreen", "IDS_MATERIALSECTION");
		} else {
			objmap.put("tabScreen", "IDS_MATERIALMSDSATTACHMENT");
		}
		objmap.put("SelectedMaterialCategory", lstMaterialCategory.get(0));
		objmap.put("SelectedMaterialCategoryFilter", lstMaterialCategory.get(0));

		newJsonUiDataArray.put(jsonuidata);
		actionType.put("Material", "IDS_EDITMATERIAL");
		jsonAuditNew.put("Material", newJsonUiDataArray);
		jsonAuditOld.put("Material", oldJsonUiDataArray);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditOld, jsonAuditNew, actionType, objmap, false, userInfo);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getMaterialByTypeCode(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		List<Integer> ismaterialSectionneed = new ArrayList<Integer>();
		List<Map<String, Object>> lstMaterial = new ArrayList<Map<String, Object>>();
		String comboget = "";
		int materialtypecode = -1;
		if (inputMap.get("nmaterialtypecode").getClass().getSimpleName().equals("Short")) {
			materialtypecode = (short) inputMap.get("nmaterialtypecode");
		} else {
			materialtypecode = (int) inputMap.get("nmaterialtypecode");
		}

		if (inputMap.containsKey("nmaterialcatcode")) {

			/*
			 * final String query1 =
			 * "select nmaterialcode,jsondata||json_build_object('nmaterialcode',nmaterialcode)::jsonb "
			 * +
			 * " as jsondata from material where  nstatus=1 and (jsondata->'nmaterialtypecode')::int="
			 * + inputMap.get("nmaterialtypecode") +
			 * " and (jsondata->'nmaterialcatcode')::int=" +
			 * inputMap.get("nmaterialcatcode") + " order by nmaterialcode";
			 */
			/*
			 * final String query1 =
			 * "select json_agg(a.jsonuidata)  from (select m.nmaterialcode," +
			 * "m.jsonuidata||json_build_object('nmaterialcode',m.nmaterialcode)::jsonb " +
			 * " as jsonuidata from material m,materialcategory mc where  m.nstatus=1 and (m.jsondata->'nmaterialtypecode')::int="
			 * + inputMap.get("nmaterialtypecode") +
			 * " and (m.jsondata->'nmaterialcatcode')::int=" +
			 * inputMap.get("nmaterialcatcode") +
			 * " and mc.nmaterialcatcode= (jsondata -> 'nmaterialcatcode' ) :: INT" +
			 * " order by m.nmaterialcode )a";
			 */
			final String query1 = "select json_agg(a.jsonuidata ||jsonb_build_object('ntransactionstatus',a.ntransactionstatus))  from (select m.nmaterialcode,"
					+ " m.jsonuidata||json_build_object('nmaterialcode',m.nmaterialcode)::jsonb "
					+ " as jsonuidata, m.ntransactionstatus from material m,materialcategory mc where  m.nstatus=1 and (m.jsondata->'nmaterialtypecode')::int="
					+ inputMap.get("nmaterialtypecode") + " and (m.jsondata->'nmaterialcatcode')::int="
					+ inputMap.get("nmaterialcatcode")
					+ " and mc.nmaterialcatcode= (jsondata -> 'nmaterialcatcode' ) :: INT"
					+ " order by m.nmaterialcode )a";
			final String strMaterial = jdbcTemplate.queryForObject(query1, String.class);

			if (strMaterial != null)
				if (inputMap.get("nmaterialtypecode").getClass().getSimpleName().equals("Short")) {
					lstMaterial = objmapper.convertValue(projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
							strMaterial, userInfo, true,
							(short) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? 1
											: (short) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? 2 : 3,
							(short) inputMap.get("nmaterialtypecode") == Enumeration.TransactionStatus.STANDARDTYPE
									.gettransactionstatus()
											? "MaterialStandard"
											: (short) inputMap.get(
													"nmaterialtypecode") == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? "MaterialVolumetric"
																	: "MaterialMaterialInventory"),
							new TypeReference<List<Map<String, Object>>>() {
							});
				} else {
					final Integer nmaterialtypecode = (int) inputMap.get("nmaterialtypecode");
					lstMaterial = objmapper.convertValue(
							projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterial, userInfo, true,
									nmaterialtypecode == Enumeration.TransactionStatus.STANDARDTYPE
											.gettransactionstatus()
													? 1
													: nmaterialtypecode == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? 2 : 3,
									nmaterialtypecode == Enumeration.TransactionStatus.STANDARDTYPE
											.gettransactionstatus()
													? "MaterialStandard"
													: nmaterialtypecode == Enumeration.TransactionStatus.VOLUMETRICTYPE
															.gettransactionstatus() ? "MaterialVolumetric"
																	: "MaterialMaterialInventory"),
							new TypeReference<List<Map<String, Object>>>() {
							});
				}

			objmap.put("Material", lstMaterial);
			ismaterialSectionneed = jdbcTemplate.queryForList("select needsectionwise  "
					+ "from materialcategory where nmaterialcatcode=" + inputMap.get("nmaterialcatcode"),
					Integer.class);
			objmap.put("ismaterialSectionneed", ismaterialSectionneed.get(0));
			if (ismaterialSectionneed.get(0) == 3) {
				objmap.put("tabScreen", "IDS_MATERIALSECTION");
			} else {
				objmap.put("tabScreen", "IDS_MATERIALMSDSATTACHMENT");
			}

			if (!lstMaterial.isEmpty()) {
				objmap.put("SelectedMaterial", lstMaterial.get(lstMaterial.size() - 1));

				objmap.putAll(objMaterialSectionDAO
						.getMaterialSectionByMaterialCode(
								(int) lstMaterial.get(lstMaterial.size() - 1).get("nmaterialcode"), inputMap)
						.getBody());
				objmap.put("nmaterialcode", (int) lstMaterial.get(lstMaterial.size() - 1).get("nmaterialcode"));
				objmap.put("MaterialTypeValidation", materialtypecode == 5 ? true : false);
				objmap.putAll(
						getMaterialFile((int) lstMaterial.get(lstMaterial.size() - 1).get("nmaterialcode"), userInfo));
				objmap.putAll((Map<String, Object>) getMaterialSafetyInstructions(objmap, userInfo).getBody());
			} else {
				objmap.put("SelectedMaterial", lstMaterial);
			}

			final String query2 = "select nmaterialtypecode,jsondata->'smaterialtypename'->>'"
					+ userInfo.getSlanguagetypecode() + "' "
					+ "as smaterialtypename,jsondata  from materialtype where nmaterialtypecode="
					+ inputMap.get("nmaterialtypecode") + " and nstatus=1";

			final List<MaterialType> lstMaterialType = jdbcTemplate.query(query2, new MaterialType());
			objmap.put("SelectedMaterialType", lstMaterialType);
			objmap.put("SelectedMaterialTypeFilter", lstMaterialType);

			if (inputMap.containsKey("nflag")) {
				comboget = "select nmaterialcatcode,smaterialcatname,needSectionwise,ndefaultstatus"
						+ " from materialcategory where nmaterialcatcode=" + inputMap.get("nmaterialcatcode")
						+ " and nstatus=1 order by nmaterialcatcode";
			} else {
				comboget = "select nmaterialcatcode,smaterialcatname,needSectionwise,ndefaultstatus"
						+ " from materialcategory where nmaterialtypecode="
						+ lstMaterialType.get(0).getNmaterialtypecode() + " and nstatus=1 order by nmaterialcatcode";
			}
			final List<MaterialCategory> lstMaterialCategory = jdbcTemplate.query(comboget, new MaterialCategory());
			if (!lstMaterialCategory.isEmpty()) {
				if (!inputMap.containsKey("nflag")) {
					final Optional<?> materialCategoryObj = lstMaterialCategory.stream().filter(
							t -> t.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
							.findAny();
					if (materialCategoryObj.isPresent()) {
						objmap.put("SelectedMaterialCategory", materialCategoryObj.get());
						objmap.put("SelectedMaterialCategoryFilter", materialCategoryObj.get());
					} else {
						objmap.put("SelectedMaterialCategory", lstMaterialCategory.get(0));
						objmap.put("SelectedMaterialCategoryFilter", lstMaterialCategory.get(0));
					}
				} else {
					objmap.put("SelectedMaterialCategory", lstMaterialCategory.get(0));
					objmap.put("SelectedMaterialCategoryFilter", lstMaterialCategory.get(0));
				}

			}
			if (inputMap.get("nmaterialtypecode").getClass().getSimpleName().equals("Short")) {
				objmap.putAll(getMaterialAdd((short) inputMap.get("nmaterialtypecode"), userInfo).getBody());
				objmap.put("DesignMappedFeilds",
						getTemplateDesignForMaterial(
								((List<MaterialConfig>) objmap.get("selectedTemplate")).get(0).getNmaterialconfigcode(),
								userInfo.getNformcode()));
			} else {
				final Integer nmaterialtypecode = (int) inputMap.get("nmaterialtypecode");
				objmap.putAll(getMaterialAdd(nmaterialtypecode.shortValue(), userInfo).getBody());
				objmap.put("DesignMappedFeilds",
						getTemplateDesignForMaterial(
								((List<MaterialConfig>) objmap.get("selectedTemplate")).get(0).getNmaterialconfigcode(),
								userInfo.getNformcode()));
			}

		} else {
			final String query2 = "select nmaterialtypecode,jsondata->'smaterialtypename'->>'"
					+ userInfo.getSlanguagetypecode() + "' "
					+ "as smaterialtypename,jsondata  from materialtype where nmaterialtypecode="
					+ inputMap.get("nmaterialtypecode") + " and nstatus=1";

			final List<MaterialType> lstMaterialType = jdbcTemplate.query(query2, new MaterialType());
			final List<MaterialType> lstActiontype = new ArrayList<MaterialType>();
			objmap.put("SelectedMaterialType", lstMaterialType);
			objmap.put("SelectedMaterialType", lstMaterialType);
			objmap.put("Material", lstActiontype);
			objmap.put("SelectedMaterial", lstActiontype);
			objmap.put("SelectedMaterialCategory", lstActiontype);
			objmap.put("SelectedMaterialCategoryFilter", lstActiontype);
		}

		if (materialtypecode == 5) {
			if (lstMaterial.size() > 0) {
				inputMap.put("nmaterialcode", (int) lstMaterial.get(lstMaterial.size() - 1).get("nmaterialcode"));
				objmap.putAll(getMaterialInventory((int) lstMaterial.get(lstMaterial.size() - 1).get("nmaterialcode"),
						inputMap).getBody());
				objmap.putAll(getAvailableQtyAndCloseQty(inputMap, userInfo).getBody());
			} else {
				objmap.put("MaterialSection", null);
				objmap.put("CloseQty", 0);
				objmap.put("OpenQty", 0);
			}
		}
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	public String prependJsonEntry(final String Key, final String value, final String toBeMergedJson) throws Exception {
		final String concatQuery = "('{'||'\"'||'''" + Key + "'''||'\":\"'||" + value + "||'\",'  || " + "substring("
				+ toBeMergedJson + "::text from 2 for LENGTH (" + toBeMergedJson + "::text)))::json ";
		return concatQuery;
	}

	@Override
	public ResponseEntity<Object> getMaterialSection(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {
		final List<Integer> lstTestMaster = jdbcTemplate
				.queryForList(
						"select nmaterialcode from material where nmaterialcode=" + inputMap.get("nmaterialcode")
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
		String strQuery = "";
		if (lstTestMaster.size() != 0) {
			strQuery = "SELECT s.*," + "       Coalesce(ts.jsondata -> 'stransdisplaystatus' ->> '"
					+ objUserInfo.getSlanguagetypecode() + "',"
					+ "       ts.jsondata -> 'stransdisplaystatus' ->> 'en-US') AS sdisplaystatus"
					+ " FROM   SECTION s," + "       transactionstatus ts"
					+ " WHERE  s.nsectioncode IN (SELECT ( jsondata -> 'nsectioncode' ) :: INT"
					+ " FROM   materialsection " + "  WHERE  nmaterialsectioncode = "
					+ inputMap.get("nmaterialsectioncode") + "  AND nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")" + "  AND s.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  AND s.ndefaultstatus = ts.ntranscode" + " UNION ALL" + " select sc.*,"
					+ "   Coalesce(ts.jsondata -> 'stransdisplaystatus' ->> '" + objUserInfo.getSlanguagetypecode()
					+ "'," + " ts.jsondata -> 'stransdisplaystatus' ->> 'en-US') AS sdisplaystatus"
					+ "	 from sitedepartment s,departmentlab d,labsection l,section sc,transactionstatus ts"
					+ " where s.nsitedeptcode=d.nsitedeptcode and d.ndeptlabcode=l.ndeptlabcode"
					+ " and sc.nsectioncode=l.nsectioncode AND ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " AND sc.ndefaultstatus = ts.ntranscode" + " and s.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and l.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and s.nsitecode="
					+ objUserInfo.getNsitecode() + "   AND sc.nsectioncode NOT IN (SELECT s.nsectioncode"
					+ "  FROM   materialsection ms," + "  SECTION s" + "  WHERE  s.nsectioncode ="
					+ " ( ms.jsondata -> 'nsectioncode' ) :: INT" + " AND ms.nmaterialcode ="
					+ inputMap.get("nmaterialcode") + "  AND s.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ms.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ "	 group by sc.nsectioncode ,ts.jsondata";
			final List<Section> lstobj = jdbcTemplate.query(strQuery, new Section());

			if (lstobj.isEmpty()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALSECTIONNOTAVAILABLE",
						objUserInfo.getSlanguagefilename()), HttpStatus.OK);

			} else {
				return new ResponseEntity<>(lstobj, HttpStatus.OK);

			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> createMaterialSafetyInstructions(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final JSONObject jsonAuditObject = new JSONObject();
		final ObjectMapper objmapper = new ObjectMapper();
		JSONObject jsonAuditObjectnew = new JSONObject();
		final JSONObject actionType = new JSONObject();
		List<Map<String, Object>> lstMaterialInventory = new ArrayList<Map<String, Object>>();
		final List<Object> lstMaterialUIInventory = new ArrayList<Object>();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		if ((int) inputMap.get("nflag") == 1) {
			if ((int) inputMap.get("nmaterialsafetyinstructionscode") == 0) {
				int seqnomaterial = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnomaterialmanagement where stablename='materialsafetyinstructions' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
				seqnomaterial++;
				final String strQuery = "INSERT INTO public.materialsafetyinstructions("
						+ "	nmaterialsafetyinstructionscode, nmaterialcode, jsondata,jsonuidata,dmodifieddate, nsitecode, nstatus)"
						+ "	VALUES (" + seqnomaterial + ", " + inputMap.get("nmaterialcode") + ", '"
						+ stringUtilityFunction.replaceQuote(inputMap.get("materialSafetyInstructions").toString())
						+ "', '" + stringUtilityFunction.replaceQuote(inputMap.get("jsonuidata").toString()) + "','"
						+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', " + objUserInfo.getNmastersitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				jdbcTemplate.execute(strQuery);
				jdbcTemplate.execute("update seqnomaterialmanagement set nsequenceno=" + seqnomaterial
						+ "  where stablename='materialsafetyinstructions' ");
				jsonAuditObjectnew = null;

				jsonAuditObject.put("materialsafetyinstructions",
						new JSONArray().put(new JSONObject(jdbcTemplate.queryForObject(
								"select ms.jsonuidata||jsonb_build_object('nmaterialsafetyinstructionscode',ms.nmaterialsafetyinstructionscode,'nmaterialcode',m.nmaterialcode)||m.jsonuidata::jsonb as jsonuidata from materialsafetyinstructions ms,material m"
										+ " where ms.nmaterialsafetyinstructionscode=" + seqnomaterial
										+ " and m.nmaterialcode=ms.nmaterialcode",
								String.class))));
				actionType.put("materialsafetyinstructions", "IDS_ADDMATERIALSAFETYINSTRUCTIONS");
			} else {
				jsonAuditObject.put("materialsafetyinstructions",
						new JSONArray().put(new JSONObject(jdbcTemplate.queryForObject(
								"select ms.jsonuidata||jsonb_build_object('nmaterialsafetyinstructionscode',ms.nmaterialsafetyinstructionscode,'nmaterialcode',m.nmaterialcode)||m.jsonuidata::jsonb as jsonuidata  from materialsafetyinstructions ms,material m"
										+ " where ms.nmaterialsafetyinstructionscode="
										+ inputMap.get("nmaterialsafetyinstructionscode")
										+ " and m.nmaterialcode=ms.nmaterialcode",
								String.class))));
				final String strQuery = "update materialsafetyinstructions set jsondata='"
						+ stringUtilityFunction.replaceQuote(inputMap.get("materialSafetyInstructions").toString())
						+ "',jsonuidata='" + stringUtilityFunction.replaceQuote(inputMap.get("jsonuidata").toString())
						+ "', dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
						+ "' where nmaterialsafetyinstructionscode=" + inputMap.get("nmaterialsafetyinstructionscode");
				jdbcTemplate.execute(strQuery);
				jsonAuditObjectnew.put("materialsafetyinstructions",
						new JSONArray().put(new JSONObject(inputMap.get("jsonuidata").toString())));
				actionType.put("materialsafetyinstructions", "IDS_EDITMATERIALSAFETYINSTRUCTIONS");
			}
			objmap.put("nregtypecode", -1);
			objmap.put("nregsubtypecode", -1);
			objmap.put("ndesigntemplatemappingcode", inputMap.get("nmaterialconfigcode"));
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, jsonAuditObjectnew, actionType, objmap, false,
					objUserInfo);
		} else if ((int) inputMap.get("nflag") == 2) {
			final List<String> DateField = (List<String>) inputMap.get("DateList");
			JSONObject jsonObject = new JSONObject(inputMap.get("materialProperty").toString());
			JSONObject jsonuidata = new JSONObject(inputMap.get("jsonuidata").toString());
			if (!jsonObject.isEmpty()) {
				if (!DateField.isEmpty()) {
					jsonObject = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonObject, DateField,
							false, objUserInfo);
					jsonuidata = (JSONObject) dateUtilityFunction.convertJSONInputDateToUTCByZone(jsonuidata, DateField,
							false, objUserInfo);
				}
				if ((int) inputMap.get("nmaterialpropertycode") == 0) {
					int seqnomaterial = jdbcTemplate.queryForObject("select nsequenceno from "
							+ " seqnomaterialmanagement where stablename='materialproperties' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
					seqnomaterial++;
					final String strQuery = "INSERT INTO public.materialproperties("
							+ "	nmaterialpropertycode, nmaterialcode, jsondata,jsonuidata, dmodifieddate, nsitecode, nstatus)"
							+ "	VALUES (" + seqnomaterial + ", " + inputMap.get("nmaterialcode") + ", '"
							+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "','"
							+ stringUtilityFunction.replaceQuote(jsonuidata.toString()) + "', '"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
							+ objUserInfo.getNmastersitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
					jdbcTemplate.execute(strQuery);
					jdbcTemplate.execute("update seqnomaterialmanagement set nsequenceno=" + seqnomaterial
							+ "  where stablename='materialproperties' ");
					jsonAuditObjectnew = null;
					final String query1 = "select json_agg(a.jsonuidata) from ( select mp.jsonuidata||jsonb_build_object('nmaterialpropertycode',mp.nmaterialpropertycode,'nmaterialcode',m.nmaterialcode)||m.jsonuidata||jsonb_build_object('nmaterialcode',m.nmaterialcode)::jsonb as jsonuidata "
							+ " from materialproperties mp,material m " + " where mp.nmaterialpropertycode= "
							+ seqnomaterial + " " + " and mp.nmaterialcode=m.nmaterialcode)a";

					final String materialproperties = jdbcTemplate.queryForObject(query1, String.class);

					if (materialproperties != null)

						lstMaterialInventory = objmapper.convertValue(
								projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(materialproperties,
										objUserInfo, true, 4, "Materialproperty"),
								new TypeReference<List<Map<String, Object>>>() {
								});
					if (lstMaterialInventory.get(0).get("Date of Standardization") != null) {

						final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						final String dateString = (String) lstMaterialInventory.get(0).get("Date of Standardization");

						final Date date = formatter.parse(dateString);
						lstMaterialInventory.get(0).put("Date of Standardization",
								new SimpleDateFormat(objUserInfo.getSsitedate()).format(date));
					}
					jsonAuditObject.put("materialproperties", lstMaterialInventory);
					actionType.put("materialproperties", "IDS_ADDMATERIALPROPERTIES");
				} else {
					final String query2 = " select json_agg(a.jsonuidata) from (select mp.jsonuidata||m.jsonuidata::jsonb as jsonuidata "
							+ "	from materialproperties mp,material m " + " where mp.nmaterialpropertycode="
							+ inputMap.get("nmaterialpropertycode") + " " + " and mp.nmaterialcode=m.nmaterialcode)a ";

					final String materialproperties = jdbcTemplate.queryForObject(query2, String.class);

					if (materialproperties != null)
						lstMaterialInventory = objmapper.convertValue(
								projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(materialproperties,
										objUserInfo, true, 4, "Materialproperty"),
								new TypeReference<List<Map<String, Object>>>() {
								});
					if (lstMaterialInventory.get(0).get("Date of Standardization") != null) {

						final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						final String dateString = (String) lstMaterialInventory.get(0).get("Date of Standardization");

						final Date date = formatter.parse(dateString);
						lstMaterialInventory.get(0).put("Date of Standardization",
								new SimpleDateFormat(objUserInfo.getSsitedate()).format(date));
					}
					jsonAuditObject.put("materialproperties", lstMaterialInventory);

					final String strQuery = "update materialproperties set jsondata='"
							+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "',jsonuidata='"
							+ stringUtilityFunction.replaceQuote(jsonuidata.toString()) + "', dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where nmaterialpropertycode="
							+ inputMap.get("nmaterialpropertycode");
					jdbcTemplate.execute(strQuery);

					lstMaterialUIInventory.add(jsonuidata.toString());
					lstMaterialInventory = objmapper.convertValue(
							projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
									lstMaterialUIInventory.toString(), objUserInfo, true, 4, "Materialproperty"),
							new TypeReference<List<Map<String, Object>>>() {
							});
					if (lstMaterialInventory.get(0).get("Date of Standardization") != null) {

						final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						final String dateString = (String) lstMaterialInventory.get(0).get("Date of Standardization");

						final Date date = formatter.parse(dateString);
						lstMaterialInventory.get(0).put("Date of Standardization",
								new SimpleDateFormat(objUserInfo.getSsitedate()).format(date));
					}
					jsonAuditObjectnew.put("materialproperties", lstMaterialInventory);
					actionType.put("materialproperties", "IDS_EDITMATERIALPROPERTIES");
				}
			}
			objmap.put("nregtypecode", -1);
			objmap.put("nregsubtypecode", -1);
			objmap.put("ndesigntemplatemappingcode", jsonuidata.get("nmaterialconfigcode"));
			auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, jsonAuditObjectnew, actionType, objmap, false,
					objUserInfo);
		}
		return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_SUCCESSFULLYSAVED", objUserInfo.getSlanguagefilename()),
				HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getMaterialSafetyInstructions(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final ObjectMapper Objmapper = new ObjectMapper();
		if (inputMap.containsKey("nflag")) {
			if ((int) inputMap.get("nflag") == 1) {
				final String strQuery = "select * from materialsafetyinstructions where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nmaterialcode="
						+ inputMap.get("nmaterialcode");
				final List<MaterialSafetyInstructions> lstMaterial = jdbcTemplate.query(strQuery,
						new MaterialSafetyInstructions());
				objmap.put("MaterialSafetyInstructions", lstMaterial);
				final String comboget = "SELECT jsondata" + " from materialconfig" + " where "
						+ "  nstatus=1 and nmaterialconfigcode=4 ;";
				final List<MaterialConfig> lstMaterialCategory = jdbcTemplate.query(comboget, new MaterialConfig());
				objmap.put("selectedTemplateSafetyInstructions", lstMaterialCategory);
			} else if ((int) inputMap.get("nflag") == 2) {
				List<MaterialProperties> lstMaterial = new ArrayList<MaterialProperties>();
				final String strQuery = "select json_agg(json_build_object('nmaterialpropertycode', a.nmaterialpropertycode ,'nmaterialcode',a.nmaterialcode ,'jsondata', a.jsondata ,'jsonuidata',a.jsonuidata,'nsitecode', a.nsitecode ,'nstatus',a.nstatus)) from (select * from materialproperties where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nmaterialcode="
						+ inputMap.get("nmaterialcode") + ")a";

				final String strMaterial = jdbcTemplate.queryForObject(strQuery, String.class);

				if (strMaterial != null) {
					lstMaterial = Objmapper
							.convertValue(
									projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterial,
											objUserInfo, true, 5, "MaterialSafety"),
									new TypeReference<List<MaterialProperties>>() {
									});
				}
				objmap.put("MaterialProperties", lstMaterial);
				final String comboget = "SELECT jsondata  from materialconfig" + " where "
						+ "  nstatus=1 and nmaterialconfigcode=5 ;";
				final List<MaterialConfig> lstMaterialCategory = jdbcTemplate.query(comboget, new MaterialConfig());
				objmap.put("selectedTemplateProperties", lstMaterialCategory);
			}
		} else {
			final String strQuery1 = "select * from materialsafetyinstructions where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nmaterialcode="
					+ inputMap.get("nmaterialcode");
			final List<MaterialSafetyInstructions> lstMaterial1 = jdbcTemplate.query(strQuery1,
					new MaterialSafetyInstructions());
			objmap.put("MaterialSafetyInstructions", lstMaterial1);
			final String strQuery2 = "select * from materialproperties where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nmaterialcode="
					+ inputMap.get("nmaterialcode");
			final List<MaterialProperties> lstMaterial2 = jdbcTemplate.query(strQuery2, new MaterialProperties());
			objmap.put("MaterialProperties", lstMaterial2);
			String comboget = "";
			if ((boolean) inputMap.get("MaterialTypeValidation")) {
				comboget = "SELECT jsondata" + " from materialconfig" + " where " + " nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialconfigcode=12 ;";

				final String comboget1 = "SELECT jsondata" + " from materialconfig" + " where " + " nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialconfigcode=13 ;";

				final List<MaterialConfig> lstMaterialCategory1 = jdbcTemplate.query(comboget1, new MaterialConfig());
				objmap.put("selectedTemplatepowder", lstMaterialCategory1);

				final List<MaterialConfig> lstMaterialCategory3 = jdbcTemplate.query(comboget, new MaterialConfig());
				objmap.put("selectedTemplatePellet", lstMaterialCategory3);

			} else {
				comboget = "SELECT jsondata" + " from materialconfig" + " where " + " nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialconfigcode=4 ;";
			}

			final List<MaterialConfig> lstMaterialCategory = jdbcTemplate.query(comboget, new MaterialConfig());
			objmap.put("selectedTemplateSafetyInstructions", lstMaterialCategory);
			final String comboget1 = "SELECT jsondata" + " from materialconfig" + " where " + " nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialconfigcode=5 ;";
			final List<MaterialConfig> lstMaterialCategory1 = jdbcTemplate.query(comboget1, new MaterialConfig());
			objmap.put("selectedTemplateProperties", lstMaterialCategory1);
		}
		return new ResponseEntity<>(objmap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> createMaterialMsdsAttachment(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final JSONObject jsonAuditObject = new JSONObject();
		final List<Map<String, Object>> lstAudit = new ArrayList<>();
		final JSONObject jsondefaultObjectnew = new JSONObject();
		final JSONObject jsondefaultObjectold = new JSONObject();
		final JSONObject actionType = new JSONObject();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final Map<String, Object> returnmap = new LinkedHashMap<String, Object>();
		final JSONObject jsonObj1 = new JSONObject(request.getParameter("materialmsdsattachment").toString());
		jsonObj1.put("sdescription", (jsonObj1.get("sdescription").toString().replaceAll("\"", "\\\\\"")));
		jsonObj1.put("sdescription", StringEscapeUtils.unescapeJava(jsonObj1.get("sdescription").toString()));
		final String acceptMultilingual = StringEscapeUtils.unescapeJava(jsonObj1.toString());

		final String acceptMultilingual1 = acceptMultilingual.replaceAll("#r#", "\\\\n");
		final JSONObject jsonObj = new JSONObject(acceptMultilingual1);
		if (jsonObj != null && jsonObj.length() > 0) {
			final int objMaterial = jdbcTemplate.queryForObject(
					"select nmaterialcode from material where nmaterialcode=" + jsonObj.get("nmaterialcode")
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			if (objMaterial != 0) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if ((int) jsonObj.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()) {

					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select * from materialmsdsattachment where nmaterialcode = "
							+ jsonObj.get("nmaterialcode") + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus();
					final List<MaterialMsdsAttachment> lstTestFiles = jdbcTemplate.query(sQuery,
							new MaterialMsdsAttachment());

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
											+ "', 'nmaterialfilecode',msds.nmaterialfilecode)::jsonb   from   "
											+ " materialmsdsattachment msds,material m,attachmenttype a where msds.nmaterialfilecode="
											+ lstTestFiles.get(0).getNmaterialfilecode()
											+ " and msds.nmaterialcode=m.nmaterialcode"
											+ " and  a.nattachmenttypecode=(msds.jsondata->'nattachmenttypecode')::int",
											String.class))));
							final String updateQueryString = " update materialmsdsattachment set ndefaultstatus=" + " "
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " "
									+ ",jsondata=jsondata||json_build_object('sdefaultstatus','"
									+ commonFunction.getMultilingualMessage("IDS_NO",
											objUserInfo.getSlanguagefilename())
									+ "')::jsonb, dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo)
									+ "' where nmaterialfilecode =" + lstTestFiles.get(0).getNmaterialfilecode();
							jdbcTemplate.execute(updateQueryString);
							jsondefaultObjectnew.put("materialmsdsattachment", new JSONArray().put(
									new JSONObject(jdbcTemplate.queryForObject("Select  msds.jsondata||m.jsonuidata||"
											+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
											+ objUserInfo.getSlanguagetypecode()
											+ "', 'nmaterialfilecode',msds.nmaterialfilecode)::jsonb   from   "
											+ " materialmsdsattachment msds,material m,attachmenttype a where msds.nmaterialfilecode="
											+ lstTestFiles.get(0).getNmaterialfilecode()
											+ " and msds.nmaterialcode=m.nmaterialcode"
											+ " and  a.nattachmenttypecode=(msds.jsondata->'nattachmenttypecode')::int",
											String.class))));
							objmap.put("nregtypecode", -1);
							objmap.put("nregsubtypecode", -1);
							objmap.put("ndesigntemplatemappingcode", jsonObj.get("nmaterialconfigcode"));
							actionType.put("materialmsdsattachment", "IDS_EDITMATERIALMSDSATTACHMENT");
							auditUtilityFunction.fnJsonArrayAudit(jsondefaultObjectold, jsondefaultObjectnew,
									actionType, objmap, false, objUserInfo);
							actionType.clear();
						}
					}
					final Instant instantDate1 = dateUtilityFunction.getCurrentDateTime(objUserInfo)
							.truncatedTo(ChronoUnit.SECONDS);
					jsonObj.put("dcreateddate", instantDate1.toString().replace("T", " ").replace("Z", ""));
					jsonObj.put("noffsetdcreateddate",
							dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()));
					int seqnomaterial = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnomaterialmanagement where stablename='materialmsdsattachment' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqnomaterial++;
					jsonObj.remove("nstatus");
					final String strQuery = "INSERT INTO public.materialmsdsattachment("
							+ "	nmaterialfilecode, nmaterialcode, jsondata, ndefaultstatus, dmodifieddate, nsitecode, nstatus)"
							+ "	VALUES (" + seqnomaterial + ", " + jsonObj.get("nmaterialcode") + ", '"
							+ stringUtilityFunction.replaceQuote(jsonObj.toString()) + "', " + ""
							+ jsonObj.get("ndefaultstatus") + ",'" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
							+ "', " + objUserInfo.getNmastersitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
					jdbcTemplate.execute(strQuery);
					jdbcTemplate.execute("update seqnomaterialmanagement set nsequenceno=" + seqnomaterial
							+ "  where stablename='materialmsdsattachment' ");

					objmap.put("nregtypecode", -1);
					objmap.put("nregsubtypecode", -1);
					objmap.put("ndesigntemplatemappingcode", jsonObj.get("nmaterialconfigcode"));
					actionType.put("materialmsdsattachment",
							(int) jsonObj.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_ADDMATERIALMSDSATTACHMENT"
									: "IDS_ADDMATERIALMSDSLINK");
					returnmap.putAll(getMaterialFile((int) jsonObj.get("nmaterialcode"), objUserInfo));
					lstAudit.add(((List<Map<String, Object>>) returnmap.get("MaterialMsdsAttachment"))
							.get(((List<Map<String, Object>>) returnmap.get("MaterialMsdsAttachment")).size() - 1));
					jsonAuditObject.put("materialmsdsattachment", lstAudit);

					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, objmap, false,
							objUserInfo);

				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return new ResponseEntity<Object>(getMaterialFile((int) jsonObj.get("nmaterialcode"), objUserInfo),
					HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public Map<String, Object> getMaterialFile(final int nmaterialcode, final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<Map<String, Object>> lstMaterial2 = new ArrayList<Map<String, Object>>();
		final String query = "select json_agg(a.jsondata) from  (select tf.nmaterialfilecode,tf.jsondata||m.jsonuidata||json_build_object('nmaterialfilecode',tf.nmaterialfilecode"
				+ "  , 'stransdisplaystatus', coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "  ts.jsondata->'stransdisplaystatus'->>'en-US'),'slinkname',lm.jsondata->'slinkname'"
				+ ",'sattachmenttype',a.jsondata->'sattachmenttype'->>'" + objUserInfo.getSlanguagetypecode()
				+ "' )::jsonb  "
				+ " as jsondata from materialmsdsattachment tf,material m,transactionstatus ts,linkmaster lm,attachmenttype a where tf.nmaterialcode="
				+ nmaterialcode + " and tf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and  a.nattachmenttypecode=(tf.jsondata->'nattachmenttypecode')::int"
				+ " and ts.ntranscode=tf.ndefaultstatus and (tf.jsondata->'nlinkcode')::int=lm.nlinkcode and m.nmaterialcode=tf.nmaterialcode order by tf.nmaterialfilecode asc)a;";
		final String strMaterialMsds = jdbcTemplate.queryForObject(query, String.class);
		final List<String> datelist = new ArrayList<>();
		datelist.add("dcreateddate");
		if (strMaterialMsds != null) {
			outputMap.put("MaterialMsdsAttachment", projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(
					strMaterialMsds, objUserInfo, true, 5, "MaterialMsdsFile"));
		} else {
			outputMap.put("MaterialMsdsAttachment", lstMaterial2);
		}
		return outputMap;
	}

	@Override
	public ResponseEntity<Object> editMaterialMsdsAttachment(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final String check = "select count(*) from materialmsdsattachment where  nmaterialfilecode ="
				+ inputMap.get("nmaterialfilecode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int intcheck = jdbcTemplate.queryForObject(check, Integer.class);
		final String sEditQuery = "select tf.jsondata||json_build_object('nmaterialfilecode',tf.nmaterialfilecode,'slinkname',lm.jsondata->'slinkname'"
				+ " ,'ndefaultstatus',tf.ndefaultstatus	)::jsonb  as jsondata from materialmsdsattachment tf,linkmaster lm where  tf.nmaterialfilecode = "
				+ inputMap.get("nmaterialfilecode") + " and (tf.jsondata->'nlinkcode')::int=lm.nlinkcode";
		final MaterialMsdsAttachment objTF = (MaterialMsdsAttachment) jdbcUtilityFunction.queryForObject(sEditQuery,
				MaterialMsdsAttachment.class, jdbcTemplate);
		if (intcheck > 0) {
			return new ResponseEntity<Object>(objTF, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateMaterialMsdsAttachment(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final JSONObject jsonAuditObjectOld = new JSONObject();
		final JSONObject jsonAuditObjectnew = new JSONObject();
		JSONObject jsondefaultObjectnew = new JSONObject();
		JSONObject jsondefaultObjectold = new JSONObject();
		final JSONArray jsonArraynew = new JSONArray();
		final JSONArray jsonArrayold = new JSONArray();
		final JSONObject actionType = new JSONObject();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final Map<String, Object> returnmap = new LinkedHashMap<String, Object>();
		final JSONObject jsonobject1 = new JSONObject(request.getParameter("materialmsdsattachment").toString());
		jsonobject1.put("sdescription", (jsonobject1.get("sdescription").toString().replaceAll("\"", "\\\\\"")));
		jsonobject1.put("sdescription", StringEscapeUtils.unescapeJava(jsonobject1.get("sdescription").toString()));
		final String acceptMultilingual = StringEscapeUtils.unescapeJava(jsonobject1.toString());
		final String acceptMultilingual1 = acceptMultilingual.replaceAll("#r#", "\\\\n");
		final JSONObject jsonobject = new JSONObject(acceptMultilingual1);
		if (jsonobject != null && jsonobject.length() > 0) {
			final int objTestMaster = jdbcTemplate.queryForObject(
					"select nmaterialcode from material where nmaterialcode=" + jsonobject.get("nmaterialcode")
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			final JSONObject jsonoldobject = new JSONObject(jdbcTemplate.queryForObject(
					"select  msds.jsondata||m.jsonuidata||jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
							+ objUserInfo.getSlanguagetypecode()
							+ "','nmaterialfilecode',msds.nmaterialfilecode)::jsonb  from materialmsdsattachment msds,material m,attachmenttype a"
							+ " where  msds.nmaterialfilecode=" + jsonobject.get("nmaterialfilecode")
							+ " and msds.nstatus=1 and m.nmaterialcode=msds.nmaterialcode"
							+ " and  a.nattachmenttypecode=(msds.jsondata->'nattachmenttypecode')::int",
					String.class));
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
					final String sQuery = "select * from materialmsdsattachment where nmaterialfilecode = "
							+ (int) jsonobject.get("nmaterialfilecode") + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final MaterialMsdsAttachment objTF = (MaterialMsdsAttachment) jdbcUtilityFunction
							.queryForObject(sQuery, MaterialMsdsAttachment.class, jdbcTemplate);

					final String sCheckDefaultQuery = "select * from materialmsdsattachment where nmaterialcode = "
							+ (int) jsonobject.get("nmaterialcode") + " and nmaterialfilecode!="
							+ (int) jsonobject.get("nmaterialfilecode") + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<MaterialMsdsAttachment> lstDefTestFiles = jdbcTemplate.query(sCheckDefaultQuery,
							new MaterialMsdsAttachment());

					if (objTF != null) {
						String ssystemfilename = "";
						if ((int) jsonobject.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()) {
							ssystemfilename = (String) jsonobject.get("ssystemfilename");
						}

						if ((int) jsonobject.get("ndefaultstatus") == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {

							final String sDefaultQuery = "select * from materialmsdsattachment where nmaterialcode = "
									+ (int) jsonobject.get("nmaterialcode") + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<MaterialMsdsAttachment> lstTestFiles = jdbcTemplate.query(sDefaultQuery,
									new MaterialMsdsAttachment());

							if (!lstTestFiles.isEmpty()) {
								if ((int) jsonobject.get("ndefaultstatus") != (int) jsonoldobject
										.get("ndefaultstatus")) {
									jsondefaultObjectold = new JSONObject(jdbcTemplate.queryForObject(
											"Select  msds.jsondata||m.jsonuidata||"
													+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
													+ objUserInfo.getSlanguagetypecode()
													+ "', 'nmaterialfilecode',msds.nmaterialfilecode)::jsonb  from   "
													+ " materialmsdsattachment msds,material m,attachmenttype a where msds.nmaterialfilecode="
													+ lstTestFiles.get(0).getNmaterialfilecode()
													+ " and msds.nmaterialcode=m.nmaterialcode"
													+ " and  a.nattachmenttypecode=(msds.jsondata->'nattachmenttypecode')::int",
											String.class));

									final String updateQueryString = " update materialmsdsattachment set jsondata=jsondata||json_build_object('ndefaultstatus'"
											+ " ," + Enumeration.TransactionStatus.NO.gettransactionstatus()
											+ ",'sdefaultstatus','"
											+ commonFunction.getMultilingualMessage("IDS_NO",
													objUserInfo.getSlanguagefilename())
											+ "')::jsonb,ndefaultstatus=" + " "
											+ Enumeration.TransactionStatus.NO.gettransactionstatus()
											+ ", dmodifieddate =' "
											+ dateUtilityFunction.getCurrentDateTime(objUserInfo)
											+ "' where nmaterialfilecode ="
											+ lstTestFiles.get(0).getNmaterialfilecode();
									jdbcTemplate.execute(updateQueryString);
									jsondefaultObjectnew = new JSONObject(jdbcTemplate.queryForObject(
											"Select  msds.jsondata||m.jsonuidata||"
													+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
													+ objUserInfo.getSlanguagetypecode()
													+ "', 'nmaterialfilecode',msds.nmaterialfilecode)::jsonb  from   "
													+ " materialmsdsattachment msds,material m,attachmenttype a where msds.nmaterialfilecode="
													+ lstTestFiles.get(0).getNmaterialfilecode()
													+ " and msds.nmaterialcode=m.nmaterialcode"
													+ " and  a.nattachmenttypecode=(msds.jsondata->'nattachmenttypecode')::int",
											String.class));
								}
							}
						} else {
							final String sDefaultQuery = "select * from materialmsdsattachment where nmaterialcode = "
									+ (int) jsonobject.get("nmaterialcode") + " and nmaterialfilecode="
									+ (int) jsonobject.get("nmaterialfilecode") + "" + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus();
							final List<MaterialMsdsAttachment> lstTestFiles = jdbcTemplate.query(sDefaultQuery,
									new MaterialMsdsAttachment());

							if (lstDefTestFiles.size() > 0) {
								if (!lstTestFiles.isEmpty()) {
									if ((int) jsonobject.get("ndefaultstatus") != (int) jsonoldobject
											.get("ndefaultstatus")) {
										jsondefaultObjectold = new JSONObject(jdbcTemplate.queryForObject(
												"Select  msds.jsondata||m.jsonuidata||"
														+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
														+ objUserInfo.getSlanguagetypecode()
														+ "', 'nmaterialfilecode',msds.nmaterialfilecode)::jsonb  from   "
														+ " materialmsdsattachment msds,material m,attachmenttype a where msds.nmaterialfilecode="
														+ lstDefTestFiles.get(lstDefTestFiles.size() - 1)
																.getNmaterialfilecode()
														+ " and msds.nmaterialcode=m.nmaterialcode"
														+ " and  a.nattachmenttypecode=(msds.jsondata->'nattachmenttypecode')::int",
												String.class));

										final String sEditDefaultQuery = "update materialmsdsattachment set jsondata=jsondata||json_build_object('ndefaultstatus'"
												+ " ," + Enumeration.TransactionStatus.YES.gettransactionstatus() + ""
												+ ", 'sdefaultstatus','"
												+ commonFunction.getMultilingualMessage("IDS_YES",
														objUserInfo.getSlanguagefilename())
												+ "')::jsonb,ndefaultstatus = "
												+ Enumeration.TransactionStatus.YES.gettransactionstatus()
												+ ", dmodifieddate ='"
												+ dateUtilityFunction.getCurrentDateTime(objUserInfo)
												+ "' where nmaterialfilecode = " + lstDefTestFiles
														.get(lstDefTestFiles.size() - 1).getNmaterialfilecode();
										jdbcTemplate.execute(sEditDefaultQuery);
										jsondefaultObjectnew = new JSONObject(jdbcTemplate.queryForObject(
												"Select  msds.jsondata||m.jsonuidata||"
														+ " jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
														+ objUserInfo.getSlanguagetypecode()
														+ "', 'nmaterialfilecode',msds.nmaterialfilecode)::jsonb  from   "
														+ " materialmsdsattachment msds,material m,attachmenttype a where msds.nmaterialfilecode="
														+ lstDefTestFiles.get(lstDefTestFiles.size() - 1)
																.getNmaterialfilecode()
														+ " and msds.nmaterialcode=m.nmaterialcode"
														+ " and  a.nattachmenttypecode=(msds.jsondata->'nattachmenttypecode')::int",
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

						final String strQuery = "update materialmsdsattachment set jsondata='"
								+ stringUtilityFunction.replaceQuote(jsonobject.toString()) + "',ndefaultstatus='"
								+ jsonobject.get("ndefaultstatus") + "', dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where nmaterialfilecode="
								+ jsonobject.get("nmaterialfilecode");
						jdbcTemplate.execute(strQuery);
						objmap.put("nregtypecode", -1);
						objmap.put("nregsubtypecode", -1);
						objmap.put("ndesigntemplatemappingcode", jsonobject.get("nmaterialconfigcode"));
						actionType.put("materialmsdsattachment",
								(int) jsonobject.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()
										? "IDS_EDITMATERIALMSDSATTACHMENT"
										: "IDS_EDITMATERIALMSDSLINK");
						jsonArraynew.put(new JSONObject(jdbcTemplate.queryForObject(
								"select  msds.jsondata||m.jsonuidata||jsonb_build_object('sattachmenttype',a.jsondata->'sattachmenttype'->>'"
										+ objUserInfo.getSlanguagetypecode()
										+ "', 'nmaterialfilecode',msds.nmaterialfilecode)::jsonb  from materialmsdsattachment msds,material m,attachmenttype a"
										+ " where  msds.nmaterialfilecode=" + jsonobject.get("nmaterialfilecode")
										+ " and msds.nstatus=1 and m.nmaterialcode=msds.nmaterialcode"
										+ " and  a.nattachmenttypecode=(msds.jsondata->'nattachmenttypecode')::int",
								String.class)));
						if (!jsondefaultObjectnew.isEmpty())
							jsonArraynew.put(jsondefaultObjectnew);

						jsonAuditObjectOld.put("materialmsdsattachment", jsonArrayold);
						jsonAuditObjectnew.put("materialmsdsattachment", jsonArraynew);
						auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, jsonAuditObjectnew, actionType,
								objmap, false, objUserInfo);

						final Map<String, Object> MaterialMsdsAttachment = getMaterialFile(
								(int) jsonobject.get("nmaterialcode"), objUserInfo);
						final List<Map<String, Object>> matMsdsAttachmentLdt = (List<Map<String, Object>>) MaterialMsdsAttachment
								.get("MaterialMsdsAttachment");
						final Map<String, Object> selectedMatMsdsAttachment = matMsdsAttachmentLdt.stream().filter(
								x -> (int) x.get("nmaterialfilecode") == (int) jsonobject.get("nmaterialfilecode"))
								.findAny().get();

						returnmap.put("SelectedMaterialMsdsAttachment", selectedMatMsdsAttachment);
						returnmap.putAll(MaterialMsdsAttachment);

						return new ResponseEntity<Object>(returnmap, HttpStatus.OK);
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
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteMaterialMsdsAttachment(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final JSONObject jsonAuditObjectOld = new JSONObject();
		final JSONObject actionType = new JSONObject();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();

		final JSONObject jsonArray1 = new JSONObject(inputMap.get("materialmsdsattachment").toString());
		final List<Integer> lstMaterial = jdbcTemplate
				.queryForList(
						"select nmaterialcode from material where nmaterialcode=" + jsonArray1.get("nmaterialcode")
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
		if (lstMaterial.size() != 0) {
			if (jsonArray1 != null) {
				final String sQuery = "select * from materialmsdsattachment where nmaterialfilecode = "
						+ jsonArray1.get("nmaterialfilecode") + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final MaterialMsdsAttachment objTF = (MaterialMsdsAttachment) jdbcUtilityFunction.queryForObject(sQuery,
						MaterialMsdsAttachment.class, jdbcTemplate);
				final JSONObject jsonArray2 = new JSONObject(inputMap.get("materialmsdsattachment").toString());
				if (objTF != null) {
					if ((int) jsonArray1.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()) {
					} else {
						jsonArray1.put("dcreateddate", "");
					}

					if ((int) jsonArray2.get("ndefaultstatus") == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						final String sDeleteQuery = "select * from materialmsdsattachment where "
								+ " nmaterialfilecode !=" + jsonArray1.get("nmaterialfilecode") + ""
								+ " and nmaterialcode=" + jsonArray1.get("nmaterialcode") + "" + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final List<MaterialMsdsAttachment> lstTestFiles = jdbcTemplate.query(sDeleteQuery,
								new MaterialMsdsAttachment());
						String sDefaultQuery = "";
						if (lstTestFiles.isEmpty()) {
							sDefaultQuery = " update materialmsdsattachment set " + " nstatus = "
									+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
									+ ", dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(objUserInfo)
									+ "' where nmaterialfilecode = " + jsonArray1.get("nmaterialfilecode");
						} else {
							sDefaultQuery = "update materialmsdsattachment set " + " ndefaultstatus = "
									+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ", dmodifieddate ='"
									+ dateUtilityFunction.getCurrentDateTime(objUserInfo)
									+ "' where nmaterialfilecode = "
									+ lstTestFiles.get(lstTestFiles.size() - 1).getNmaterialfilecode();
						}
						jdbcTemplate.execute(sDefaultQuery);
					}
					final String sUpdateQuery = "update materialmsdsattachment set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where nmaterialfilecode = "
							+ jsonArray1.get("nmaterialfilecode");
					jdbcTemplate.execute(sUpdateQuery);

					objmap.put("nregtypecode", -1);
					objmap.put("nregsubtypecode", -1);
					objmap.put("ndesigntemplatemappingcode", jsonArray1.get("nmaterialconfigcode"));
					actionType.put("materialmsdsattachment",
							(int) jsonArray1.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_DELETEMATERIALMSDSATTACHEMENT"
									: "IDS_DELETEMATERIALMSDSLINK");
					jsonAuditObjectOld.put("materialmsdsattachment", new JSONArray(jdbcTemplate.queryForObject(
							"select json_agg(msds.jsondata||m.jsonuidata::jsonb) from materialmsdsattachment msds,material m where "
									+ " msds.nmaterialfilecode=" + jsonArray1.get("nmaterialfilecode")
									+ " and msds.nmaterialcode=m.nmaterialcode",
							String.class)));
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, null, actionType, objmap, false,
							objUserInfo);

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return new ResponseEntity<Object>(getMaterialFile((int) jsonArray1.get("nmaterialcode"), objUserInfo),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> viewAttachedMaterialMsdsAttachment(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		final Map<String, Object> auditmap = new LinkedHashMap<>();
		List<Map<String, Object>> lstdata = new ArrayList<>();
		final List<Map<String, Object>> lstaudit = new ArrayList<>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonAuditObject = new JSONObject();

		final JSONObject jsonObject = new JSONObject(inputMap.get("materialmsdsattachment").toString());

		final String strQuery = "select * from material where nmaterialcode=" + jsonObject.get("nmaterialcode") + ""
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Material objMaterial = (Material) jdbcUtilityFunction.queryForObject(strQuery, Material.class,
				jdbcTemplate);
		if (objMaterial != null) {

			String sQuery = "select * from materialmsdsattachment where nmaterialfilecode = "
					+ jsonObject.get("nmaterialfilecode") + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final MaterialMsdsAttachment objTF = (MaterialMsdsAttachment) jdbcUtilityFunction.queryForObject(sQuery,
					MaterialMsdsAttachment.class, jdbcTemplate);
			if (objTF != null) {
				if ((int) jsonObject.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(jsonObject.get("ssystemfilename").toString(), -1,
							objUserInfo, "", "");
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ jsonObject.get("nlinkcode") + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery,
							LinkMaster.class, jdbcTemplate);
					map.put("AttachLink", objlinkmaster.getSlinkname() + jsonObject.get("sfilename"));
					jsonObject.put("dcreateddate", "");
				}
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList
						.add((int) jsonObject.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWMATERIALFILE"
								: "IDS_VIEWMATERIALINK");

				lstdata = (List<Map<String, Object>>) getMaterialFile((int) inputMap.get("nmaterialcode"), objUserInfo)
						.get("MaterialMsdsAttachment");
				lstaudit.add(0,
						lstdata.stream()
								.filter(t -> t.get("nmaterialfilecode").equals(jsonObject.get("nmaterialfilecode")))
								.findAny().get());
				jsonAuditObject.put("materialmsdsattachment", lstaudit);
				auditmap.put("nregtypecode", -1);
				auditmap.put("nregsubtypecode", -1);
				auditmap.put("ndesigntemplatemappingcode", inputMap.get("ndesigntemplatemappingcode"));
				actionType.put("materialmsdsattachment",
						(int) jsonObject.get("nattachmenttypecode") == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWMATERIALMSDSFILE"
								: "IDS_VIEWMATERIAMSDSLINK");
				auditUtilityFunction.fnJsonArrayAudit(jsonAuditObject, null, actionType, auditmap, false, objUserInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMaterialLinkMaster(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> objMap = new HashMap<String, Object>();
		final List<Integer> lstMaterial = jdbcTemplate
				.queryForList(
						"select nmaterialcode from material where nmaterialcode=" + inputMap.get("nmaterialcode")
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
		if (lstMaterial.size() != 0) {
			String sQuery = "select nlinkcode,trim('\"' from (jsondata->'slinkname')::text)  as slinkname "
					+ ",ndefaultlink,nsitecode,nstatus from linkmaster" + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nlinkcode > 0"
					+ " and nsitecode = " + objUserInfo.getNmastersitecode() + ";";
			final List<LinkMaster> lst1 = jdbcTemplate.query(sQuery, new LinkMaster());
			objMap.put("LinkMaster", lst1);
			sQuery = " select nattachmenttypecode from attachmenttype" + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<LinkMaster> lst2 = jdbcTemplate.query(sQuery, new LinkMaster());

			objMap.put("AttachmentType", lst2);
			return new ResponseEntity<Object>(objMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public Map<String, Object> getTemplateDesignForMaterial(final int nmaterialconfigcode, final int nformcode)
			throws Exception {
		final String str = "select jsondata->'" + nformcode + "' as jsondata from mappedtemplatefieldpropsmaterial"
				+ " where nmaterialconfigcode=" + nmaterialconfigcode;
		final Map<String, Object> map = jdbcTemplate.queryForMap(str);
		return map;
	}

	@Override
	public ResponseEntity<Object> getMaterialAcountingProperties(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();

		final ObjectMapper Objmapper = new ObjectMapper();
		final int countcheck = jdbcTemplate.queryForObject(
				"select count(*) from material where nmaterialcode in (" + inputMap.get("nmaterialcode")
						+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
				Integer.class);
		if (countcheck > 0) {
			if (inputMap.containsKey("nflag")) {
				if ((int) inputMap.get("nflag") == 1) {
					final String strQuery = "select * from materialsafetyinstructions where nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nmaterialcode="
							+ inputMap.get("nmaterialcode");
					final List<MaterialSafetyInstructions> lstMaterial = jdbcTemplate.query(strQuery,
							new MaterialSafetyInstructions());
					objmap.put("MaterialSafetyInstructions", lstMaterial);
					final String comboget = "SELECT jsondata" + " from materialconfig" + " where "
							+ "  nstatus=1 and nmaterialconfigcode=4 ;";
					final List<MaterialConfig> lstMaterialCategory = jdbcTemplate.query(comboget, new MaterialConfig());
					objmap.put("selectedTemplateSafetyInstructions", lstMaterialCategory);
				} else if ((int) inputMap.get("nflag") == 2) {
					List<MaterialProperties> lstMaterial = new ArrayList<MaterialProperties>();
					final String strQuery = "select json_agg(json_build_object('nmaterialpropertycode', a.nmaterialpropertycode ,'nmaterialcode',a.nmaterialcode ,'jsondata', a.jsondata ,'jsonuidata',a.jsonuidata,'nsitecode', a.nsitecode ,'nstatus',a.nstatus)) from (select * from materialproperties where nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nmaterialcode="
							+ inputMap.get("nmaterialcode") + ")a";

					final String strMaterial = jdbcTemplate.queryForObject(strQuery, String.class);

					if (strMaterial != null) {
						lstMaterial = Objmapper
								.convertValue(
										projectDAOSupport.getSiteLocalTimeFromUTCForDynamicTemplate(strMaterial,
												objUserInfo, true, 5, "MaterialSafety"),
										new TypeReference<List<MaterialProperties>>() {
										});
					}

					objmap.put("MaterialProperties", lstMaterial);
					final String comboget = "SELECT jsondata  from materialconfig" + " where "
							+ "  nstatus=1 and nmaterialconfigcode=12 ;";
					final List<MaterialConfig> lstMaterialCategory = jdbcTemplate.query(comboget, new MaterialConfig());
					objmap.put("selectedTemplateProperties", lstMaterialCategory);
				}
			} else {
				final String strQuery1 = "select * from materialsafetyinstructions where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nmaterialcode="
						+ inputMap.get("nmaterialcode");
				final List<MaterialSafetyInstructions> lstMaterial1 = jdbcTemplate.query(strQuery1,
						new MaterialSafetyInstructions());
				objmap.put("MaterialSafetyInstructions", lstMaterial1);
				final String strQuery2 = "select * from materialproperties where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nmaterialcode="
						+ inputMap.get("nmaterialcode");
				final List<MaterialProperties> lstMaterial2 = jdbcTemplate.query(strQuery2, new MaterialProperties());
				objmap.put("MaterialProperties", lstMaterial2);
				final String comboget = "SELECT jsondata" + " from materialconfig" + " where " + " nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialconfigcode=4 ;";
				final List<MaterialConfig> lstMaterialCategory = jdbcTemplate.query(comboget, new MaterialConfig());
				objmap.put("selectedTemplateSafetyInstructions", lstMaterialCategory);
				final String comboget1 = "SELECT jsondata" + " from materialconfig" + " where " + " nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmaterialconfigcode=5 ;";
				final List<MaterialConfig> lstMaterialCategory1 = jdbcTemplate.query(comboget1, new MaterialConfig());
				objmap.put("selectedTemplateProperties", lstMaterialCategory1);
			}
			return new ResponseEntity<>(objmap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.OK);
		}

	}

	@Override
	public ResponseEntity<Object> createMaterialInventory(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final JSONObject jsonObjectInvTrans = new JSONObject(inputMap.get("jsonuidata").toString());
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		float nqtyreceived = 0;
		float nqtyissued = 0;
		float nuraniumqtyreceived = 0;
		double nuraniumqtyissued = 0;
		final int validate = (inputMap.get("issueaction").equals("Liquid")) ? 1000 : 100;
		if ((int) inputMap.get("inventorytype") == 2) {
			nqtyissued = Float.parseFloat(inputMap.get("quantity").toString());
			nuraniumqtyissued = (Double.parseDouble(inputMap.get("quantity").toString())
					* Double.parseDouble(inputMap.get("suraniumconversionfactor").toString())) / validate;

		} else {
			nqtyreceived = Float.parseFloat(inputMap.get("quantity").toString());
			nuraniumqtyreceived = (Float.parseFloat(inputMap.get("quantity").toString())
					* Float.parseFloat(inputMap.get("suraniumconversionfactor").toString()));
		}
		final LocalDateTime datetime = LocalDateTime.ofInstant(
				dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC);
		jsonObjectInvTrans.put("month",
				String.valueOf(datetime.getMonthValue()).length() == 1 ? "0" + String.valueOf(datetime.getMonthValue())
						: String.valueOf(datetime.getMonthValue()));
		jsonObjectInvTrans.put("year", String.valueOf(datetime.getYear()));

		final String materialinventorytransaction = "select seqnomaterialmanagement.nsequenceno+1 from seqnomaterialmanagement"
				+ " where stablename='materialinventorytransaction' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int seqNo = jdbcTemplate.queryForObject(materialinventorytransaction, Integer.class);
		final String str = "insert into materialinventorytransaction values(" + seqNo
				+ ",(select nmaterialinventorycode from materialinventory where nmaterialcode="
				+ inputMap.get("nmaterialcode") + ")," + inputMap.get("inventorytype") + ",14,-1,-1," + nqtyreceived
				+ "," + nqtyreceived + "," + nqtyissued + "," + inputMap.get("nuraniumqtyissued") + ","
				+ Float.parseFloat(inputMap.get("suraniumconversionfactor").toString()) + ",'"
				+ inputMap.get("SampleName").toString() + "','"
				+ stringUtilityFunction.replaceQuote(jsonObjectInvTrans.toString()) + "'::jsonb,'"
				+ stringUtilityFunction.replaceQuote(jsonObjectInvTrans.toString()) + "'::jsonb, '"
				+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', " + objUserInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid()) + ", "
				+ objUserInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ")";
		jdbcTemplate.execute(str);

		jdbcTemplate.execute("update seqnomaterialmanagement set nsequenceno=" + seqNo
				+ " where stablename='materialinventorytransaction'");
		objmap.putAll(getAvailableQtyAndCloseQty(inputMap, objUserInfo).getBody());
		objmap.putAll(getMaterialInventory((int) inputMap.get("nmaterialcode"), inputMap).getBody());
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getMaterialInventory(final int nmaterialcode,
			final Map<String, Object> inputMap) throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		final String queryst = "select miv.nmaterialinventtranscode,((miv.nqtyreceived)+(miv.nqtyissued))as sqtyreceived,miv.jsonuidata->>'Comments' as scomments,miv.jsonuidata->>'Transaction Date' as stransactiondate,   "
				+ " mt.jsondata->'sinventorytypename'->>'" + userInfo.getSlanguagetypecode()
				+ "'  as sinventorytypename,"
				+ " ( (miv.nuraniumqtyissued)+((miv.nuraniumqtyreceived)) ) as suraniumreceived,miv.sproductname "
				+ " from materialinventorytransaction miv,materialinventory mi,materialinventorytype mt,samplestate st "
				+ " where mt.ninventorytypecode=miv.ninventorytranscode  And ( mi.jsonuidata ->> 'nsamplestatecode' ) :: INT =st.nsamplestatecode"
				+ " and miv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mi.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and miv.nsitecode = "
				+ userInfo.getNtranssitecode()
				+ " and miv.nmaterialinventorycode=mi.nmaterialinventorycode and mi.nmaterialcode=" + nmaterialcode
				+ " and (miv.jsonuidata->>'Transaction Date')::date between ('" + (String) inputMap.get("fromDate")
				+ "')::date and ('" + (String) inputMap.get("toDate") + "')::date";

		final List<MaterialInventoryTrans> lstMaterialInvntory = jdbcTemplate.query(queryst,
				new MaterialInventoryTrans());
		objmap.put("MaterialSection", lstMaterialInvntory);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMaterialAvailableQty(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();

		final String str = "select case  when  (sum(mit.nqtyreceived)-sum(mit.nqtyissued)) isnull then 0 else sum(mit.nqtyreceived)-sum(mit.nqtyissued) end as navailableqty, "
				+ " CASE " + " WHEN ( " + " Sum(mit.nuraniumqtyreceived)-Sum(mit.nuraniumqtyissued) ) isnull THEN 0 "
				+ " ELSE " + " 	 ((sum(mit.nuraniumqtyreceived)-sum(mit.nuraniumqtyissued))) "
				+ " END AS navailableuraniumqty,(m.jsondata->>'suraniumconversionfactor')::float as suraniumconversionfactor "
				+ " from materialinventorytransaction mit,materialinventory mi,material m "
				+ " where mi.nmaterialcode=m.nmaterialcode " + " and mit.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mi.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and   mit.nmaterialinventorycode=mi.nmaterialinventorycode " + " and mi.nmaterialcode="
				+ inputMap.get("nmaterialcode") + " and mit.nsitecode = " + userInfo.getNtranssitecode()
				+ " group by (mi.jsonuidata->>'suraniumconversionfactor')::float,( mi.jsonuidata ->> 'nsamplestatecode' ) ::int,(m.jsondata->>'suraniumconversionfactor')::float";

		final MaterialInventoryType objinventorytypeQty = (MaterialInventoryType) jdbcUtilityFunction
				.queryForObject(str, MaterialInventoryType.class, jdbcTemplate);

		objmap.put("AvailableQty", objinventorytypeQty != null ? objinventorytypeQty.getNavailableqty() : 0);
		objmap.put("AvailableUraniumQty",
				objinventorytypeQty != null ? objinventorytypeQty.getNavailableuraniumqty() : 0);
		objmap.put("suraniumconversionfactor",
				objinventorytypeQty != null ? objinventorytypeQty.getSuraniumconversionfactor() : 0);

		final String inventorytypecode = "select ninventorytypecode, " + " jsondata->'sinventorytypename'->>'"
				+ userInfo.getSlanguagetypecode() + "'  as sinventorytypename"
				+ " from materialinventorytype where ninventorytypecode=2";
		final MaterialInventoryType objinventorytypecode = (MaterialInventoryType) jdbcUtilityFunction
				.queryForObject(inventorytypecode, MaterialInventoryType.class, jdbcTemplate);
		objmap.put("Inventorytype", objinventorytypecode);

		final String strproduct = "select DISTINCT(sproductname) sproductname from materialinventorytransaction order by 1 desc";
		final List<MaterialInventoryTrans> lstsproductname = jdbcTemplate.query(strproduct,
				new MaterialInventoryTrans());
		objmap.put("Productname", lstsproductname);

		return new ResponseEntity<>(objmap, HttpStatus.OK);

	}

	private ResponseEntity<Map<String, Object>> getAvailableQtyAndCloseQty(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final String closestr = "select case  when  (sum(mit.nuraniumqtyreceived)-sum(mit.nuraniumqtyissued)) isnull then 0 else "
				+ " ((sum(mit.nuraniumqtyreceived)-sum(mit.nuraniumqtyissued))) "
				+ " end as navailableuraniumqty from materialinventorytransaction mit,materialinventory mi "
				+ " where mit.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and mi.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and   mit.nmaterialinventorycode=mi.nmaterialinventorycode and nmaterialcode="
				+ (int) inputMap.get("nmaterialcode") + " and (mit.jsonuidata->>'Transaction Date')::date<=('"
				+ (String) inputMap.get("toDate") + "')::date" + " and mit.nsitecode = "
				+ objUserInfo.getNtranssitecode()
				+ " group by (mi.jsonuidata->>'suraniumconversionfactor')::float,( mi.jsonuidata ->> 'nsamplestatecode' )::int";

		final MaterialInventoryType objinventorytypeQty = (MaterialInventoryType) jdbcUtilityFunction
				.queryForObject(closestr, MaterialInventoryType.class, jdbcTemplate);
		final String OpenQtystr = "select case  when  (sum(mit.nuraniumqtyreceived)-sum(mit.nuraniumqtyissued)) isnull then 0 else "
				+ " ((sum(mit.nuraniumqtyreceived)-sum(mit.nuraniumqtyissued))) "
				+ " end as navailableuraniumqty from materialinventorytransaction mit,materialinventory mi "
				+ " where mit.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and mi.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and   mit.nmaterialinventorycode=mi.nmaterialinventorycode and nmaterialcode="
				+ (int) inputMap.get("nmaterialcode") + " and (mit.jsonuidata->>'Transaction Date')::date<('"
				+ (String) inputMap.get("fromDate") + "')::date" + " and mit.nsitecode = "
				+ objUserInfo.getNtranssitecode()
				+ " group by (mi.jsonuidata->>'suraniumconversionfactor')::float,( mi.jsonuidata ->> 'nsamplestatecode' )::int";

		final MaterialInventoryType objinventorytypeOpenQty = (MaterialInventoryType) jdbcUtilityFunction
				.queryForObject(OpenQtystr, MaterialInventoryType.class, jdbcTemplate);
		objmap.put("CloseQty", objinventorytypeQty != null ? objinventorytypeQty.getNavailableuraniumqty() : 0);
		objmap.put("OpenQty", objinventorytypeOpenQty != null ? objinventorytypeOpenQty.getNavailableuraniumqty() : 0);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getUraniumContentType(final UserInfo userInfo) throws Exception {

		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
//		final String strQuery = "select suraniumcontenttype,nuraniumcontenttypecode from uraniumcontenttype  where nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and nuraniumcontenttypecode > 0  and nsitecode = " + userInfo.getNmastersitecode();
//		List<UraniumContentType> lstUraniumContentType = (List<UraniumContentType>) jdbcTemplate.query(strQuery,
//				new UraniumContentType());
//		objmap.put("lstUraniumContentType", lstUraniumContentType);
//		DateFormatSymbols dfs = new DateFormatSymbols();
//		String[] months = dfs.getMonths();
//		HashMap<Integer, String> map = new HashMap<Integer, String>();
//		for (int i = 0; i < 12; i++) {
//			map.put(i + 1, months[i].substring(0, 3));
//		}
//		objmap.put("month", map);
//
//		final String strQuerys = "select smaterialaccountinggroupname,nmaterialaccountinggroupcode from materialaccountinggroup where nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and nsitecode = "
//				+ userInfo.getNmastersitecode();
//		List<MaterialAccountingPlantGroup> lstAccountingPlantGroup = (List<MaterialAccountingPlantGroup>) jdbcTemplate
//				.query(strQuerys, new MaterialAccountingPlantGroup());
//		objmap.put("lstAccountingPlantGroup", lstAccountingPlantGroup);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}
}
