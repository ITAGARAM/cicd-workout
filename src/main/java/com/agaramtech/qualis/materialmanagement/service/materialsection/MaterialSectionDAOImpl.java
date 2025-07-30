package com.agaramtech.qualis.materialmanagement.service.materialsection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
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
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Section;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class MaterialSectionDAOImpl implements MaterialSectionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialSectionDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> createMaterialSection(final Map<String, Object> inputMap) throws Exception {
		LOGGER.info("createMaterialSection()");
		final JSONObject jsonAuditObjectOld = new JSONObject();
		List<Map<String, Object>> lstAuditOld = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final JSONObject actionType = new JSONObject();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		Integer matsectioncheck;
		int seqnomaterial = 0;
		String nreorderlevel = "";
		final JSONObject jsonObject = new JSONObject(inputMap.get("materialSectionJson").toString());
		seqnomaterial = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnomaterialmanagement where stablename='materialsection' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);
		if (jsonObject.has("nreorderlevel")) {
			nreorderlevel = jsonObject.get("nreorderlevel") == null ? "" : jsonObject.get("nreorderlevel").toString();
		} else {
			nreorderlevel = "";
		}
		matsectioncheck = jdbcTemplate.queryForObject("select count(*) from materialsection where nsectioncode in ("
				+ jsonObject.get("nsectioncode") + ")" + " and nmaterialcode in (" + jsonObject.get("nmaterialcode")
				+ ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

		if (matsectioncheck == 0) {
			final String insmat = "INSERT INTO public.materialsection("
					+ "nmaterialsectioncode,nsectioncode,nmaterialcode, jsondata, dmodifieddate, nsitecode, nstatus) "
					+ "select (" + seqnomaterial
					+ "+ROW_NUMBER ()  OVER (ORDER BY nsectioncode))::int  as nmaterialsectioncode" + ",nsectioncode,"
					+ jsonObject.get("nmaterialcode")
					+ " as nmaterialcode ,json_build_object('nsectioncode',nsectioncode)::jsonb "
					+ "||json_build_object('ssectionname',ssectionname)::jsonb "
					+ "||json_build_object('nmaterialcode'," + jsonObject.get("nmaterialcode") + ")::jsonb "
					+ "||json_build_object('nsectioncode',nsectioncode)::jsonb "
					+ "||json_build_object('nreorderlevel','" + nreorderlevel + "')::jsonb as jsondata, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from section where nsectioncode in (" + jsonObject.get("nsectioncode") + ");";
			jdbcTemplate.execute(insmat);

			jdbcTemplate.execute(
					"update seqnomaterialmanagement set nsequenceno= (select max(nmaterialsectioncode) from  materialsection) where stablename='materialsection' ");
			objmap.putAll(getMaterialSectionByMaterialCode((int) inputMap.get("nmaterialcode"), inputMap).getBody());
		}
		final JSONArray oldJsonarray = new JSONArray(jdbcTemplate.queryForObject(
				"select json_agg(x.jsondata||jsonb_build_object('nmaterialsectioncode',x.nmaterialsectioncode)) as jsondata  from (select ("
						+ seqnomaterial + "+ROW_NUMBER ()  OVER (ORDER BY nsectioncode))::int  as nmaterialsectioncode"
						+ ",nsectioncode," + jsonObject.get("nmaterialcode")
						+ " as nmaterialcode ,json_build_object('nsectioncode',nsectioncode)::jsonb "
						+ "||json_build_object('ssectionname',ssectionname)::jsonb "
						+ "||json_build_object('nmaterialcode'," + jsonObject.get("nmaterialcode") + ")::jsonb "
						+ "||json_build_object('nsectioncode',nsectioncode)::jsonb "
						+ "||json_build_object('nreorderlevel','" + nreorderlevel
						+ "')::jsonb || (select jsonuidata from material where nmaterialcode="
						+ jsonObject.get("nmaterialcode") + ")::jsonb as jsondata,"
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from section where nsectioncode in (" + jsonObject.get("nsectioncode") + "))x;",
				String.class));

		objmap.put("nregtypecode", -1);
		objmap.put("nregsubtypecode", -1);
		objmap.put("ndesigntemplatemappingcode", jsonObject.get("nmaterialconfigcode"));
		actionType.put("materialsection", "IDS_ADDMATERIALSECTION");
		lstAuditOld = objmapper.convertValue(oldJsonarray.toList(), new TypeReference<List<Map<String, Object>>>() {
		});
		jsonAuditObjectOld.put("materialsection", lstAuditOld);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, null, actionType, objmap, false, userInfo);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getMaterialSectionByMaterialCode(final int nmaterialcode,
			final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		List<Map<String, Object>> lstMaterial = new ArrayList<Map<String, Object>>();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final String query1 = " select json_agg(a.jsondata) from(select nmaterialsectioncode,  "
				+ "			 ms.nmaterialcode,ms.jsondata|| m.jsonuidata||json_build_object('nmaterialsectioncode',ms.nmaterialsectioncode   "
				+ "			 	 ,'nmaterialcode',ms.nmaterialcode)::jsonb jsondata    from materialsection ms,material m where ms.nmaterialcode=  "
				+ nmaterialcode
				+ "				  and ms.nstatus=1   and m.nmaterialcode=ms.nmaterialcode order by ms.nmaterialsectioncode )a ";

		final String strMaterialSection = jdbcTemplate.queryForObject(query1, String.class);
		if (strMaterialSection != null)
			lstMaterial = objmapper.readValue(strMaterialSection, new TypeReference<List<Map<String, Object>>>() {
			});
		objmap.put("MaterialSection", lstMaterial);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> deleteMaterialSection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final JSONObject jsonAuditObjectOld = new JSONObject();
		final JSONObject actionType = new JSONObject();
		List<Map<String, Object>> lstAuditOld = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final JSONObject objJSONObject = new JSONObject(inputMap.get("materialsection").toString());
		final StringBuffer sB = new StringBuffer();
		List<Integer> lstcount = new ArrayList<Integer>();
		List<Integer> lstmaterialcount = new ArrayList<Integer>();
		final List<Integer> lstMaterial = jdbcTemplate
				.queryForList(
						"select nmaterialcode from material where nmaterialcode=" + objJSONObject.get("nmaterialcode")
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
		if (lstMaterial.size() != 0) {
			lstcount = jdbcTemplate
					.queryForList("select nmaterialsectioncode from materialsection where nmaterialsectioncode in ("
							+ objJSONObject.get("nmaterialsectioncode") + ") and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "", Integer.class);
			if (lstcount.size() > 0) {
				lstmaterialcount = jdbcTemplate
						.queryForList(
								"select nmaterialsectioncode from materialsection where nmaterialcode in ("
										+ objJSONObject.get("nmaterialcode") + ") and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "",
								Integer.class);
				if (lstmaterialcount.size() > 1) {
					sB.append(" update materialsection set nstatus="
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialsectioncode in ("
							+ objJSONObject.get("nmaterialsectioncode") + ");");
					jdbcTemplate.execute(sB.toString());
					objmap.putAll(getMaterialSectionByMaterialCode((int) objJSONObject.get("nmaterialcode"), inputMap)
							.getBody());
					objmap.put("nregtypecode", -1);
					objmap.put("nregsubtypecode", -1);
					objmap.put("ndesigntemplatemappingcode", inputMap.get("nmaterialconfigcode"));
					actionType.put("materialsection", "IDS_DELETEMATERIALSECTION");
					lstAuditOld = objmapper.convertValue(new JSONArray(jdbcTemplate.queryForObject(
							"select json_agg(ms.jsondata||m.jsonuidata||jsonb_build_object('nmaterialsectioncode',ms.nmaterialsectioncode,'nmaterialcode',m.nmaterialcode)::jsonb) from materialsection ms,material m where "
									+ " m.nmaterialcode=ms.nmaterialcode and ms.nmaterialsectioncode="
									+ objJSONObject.get("nmaterialsectioncode"),
							String.class)).toList(), new TypeReference<List<Map<String, Object>>>() {
							});
					jsonAuditObjectOld.put("materialsection", lstAuditOld);
					auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, null, actionType, objmap, false,
							userInfo);

					return new ResponseEntity<>(objmap, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_ATLEASTONEMATERIALSECTIONMUSTBEAVAILABLE",
									userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_MATERIALALREADYDELETED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getActiveMaterialSectionById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> objmap = new HashMap<>();
		final String check = "select count(*) from materialsection where nmaterialsectioncode="
				+ inputMap.get("primaryKeyValue") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int intcheck = jdbcTemplate.queryForObject(check, Integer.class);
		if (intcheck > 0) {
			final String comboget = "select s.* from materialsection ms,section s "
					+ " where (ms.jsondata->'nsectioncode')::int=s.nsectioncode" + " and ms.nmaterialsectioncode="
					+ inputMap.get("primaryKeyValue");

			final List<Section> lstSection = jdbcTemplate.query(comboget, new Section());

			objmap.put("MaterialSectionEditData", lstSection);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateMaterialSection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final JSONObject jsonAuditObjectOld = new JSONObject();
		final ObjectMapper objmapper = new ObjectMapper();
		List<Map<String, Object>> lstauditOld = new ArrayList<>();
		List<Map<String, Object>> lstauditNew = new ArrayList<>();
		final JSONObject jsonAuditObjectnew = new JSONObject();
		final JSONObject actionType = new JSONObject();
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final JSONArray jsonArray1 = new JSONArray(inputMap.get("materialSectionJson").toString());
		final JSONArray oldJsonArray = new JSONArray(jdbcTemplate.queryForObject(
				"	 select json_agg(ms.jsondata||m.jsonuidata||jsonb_build_object('nmaterialsectioncode',ms.nmaterialsectioncode)) from materialsection ms,material m  "
						+ "	 where ms.nmaterialsectioncode in (" + inputMap.get("nmaterialsectioncode") + " )  "
						+ "			 and m.nmaterialcode=ms.nmaterialcode 	",
				String.class));
		final String query12 = " update materialsection set jsondata='"
				+ stringUtilityFunction.replaceQuote(jsonArray1.get(0).toString()) + "'::jsonb , dmodifieddate ='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nmaterialsectioncode in ("
				+ inputMap.get("nmaterialsectioncode") + ");";
		jdbcTemplate.execute(query12);
		final JSONArray newJsonArray = new JSONArray(jdbcTemplate.queryForObject(
				"	 select json_agg(ms.jsondata||m.jsonuidata||jsonb_build_object('nmaterialsectioncode',ms.nmaterialsectioncode)) from materialsection ms,material m  "
						+ "	 where ms.nmaterialsectioncode in (" + inputMap.get("nmaterialsectioncode") + " )  "
						+ "			 and m.nmaterialcode=ms.nmaterialcode 	",
				String.class));

		final Map<String, Object> MaterialSection = getMaterialSectionByMaterialCode(
				(int) inputMap.get("nmaterialcode"), inputMap).getBody();

		final List<Map<String, Object>> matSecLst = (List<Map<String, Object>>) MaterialSection.get("MaterialSection");
		final Map<String, Object> selectedMatSecLst = matSecLst.stream()
				.filter(x -> (int) x.get("nmaterialsectioncode") == (int) inputMap.get("nmaterialsectioncode"))
				.findAny().get();

		objmap.put("SelectedMaterialSection", selectedMatSecLst);

		objmap.putAll(MaterialSection);
		objmap.put("nregtypecode", -1);
		objmap.put("nregsubtypecode", -1);
		objmap.put("ndesigntemplatemappingcode", inputMap.get("nmaterialconfigcode"));
		actionType.put("materialsection", "IDS_EDITMATERIALSECTION");

		lstauditOld = objmapper.convertValue(oldJsonArray.toList(), new TypeReference<List<Map<String, Object>>>() {
		});
		lstauditNew = objmapper.convertValue(newJsonArray.toList(), new TypeReference<List<Map<String, Object>>>() {
		});
		jsonAuditObjectOld.put("materialsection", lstauditOld);
		jsonAuditObjectnew.put("materialsection", lstauditNew);
		auditUtilityFunction.fnJsonArrayAudit(jsonAuditObjectOld, jsonAuditObjectnew, actionType, objmap, false,
				userInfo);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}
}