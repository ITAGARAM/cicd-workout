package com.agaramtech.qualis.testgroup.service.testgrouptestmaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.TreeVersionTemplate;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TestGroupCommonFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupTestMaterial;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TestGroupTestMaterialDAOImpl implements TestGroupTestMaterialDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestGroupTestMaterialDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private TestGroupCommonFunction testGroupCommonFunction;

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createTestGroupTestMaterial(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table locktestgrouptestmaterial "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTestMaterialList = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final TestGroupTestMaterial objTestMaterial = objMapper.convertValue(inputMap.get("testgrouptestmaterial"),
				TestGroupTestMaterial.class);
		final List<TestGroupTestMaterial> testMaterialByName = getTestMaterialListByName(
				objTestMaterial.getNtestgrouptestcode(), objTestMaterial.getNmaterialtypecode(),
				objTestMaterial.getNmaterialcatcode(), objTestMaterial.getSmaterialname(), userInfo);
		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);
		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			if (testMaterialByName.isEmpty()) {
				String materialSeq = "";
				materialSeq = "select nsequenceno from seqnotestgroupmanagement where stablename='testgrouptestmaterial' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
				int seqNo = jdbcTemplate.queryForObject(materialSeq, Integer.class);

				seqNo = seqNo + 1;
				String materialInsert = "";
				String sremarks = objTestMaterial.getSremarks() == null ? "" : objTestMaterial.getSremarks();

				materialInsert = "insert into testgrouptestmaterial(ntestgrouptestmaterialcode,ntestgrouptestcode,nmaterialtypecode,nmaterialcatcode,nmaterialcode,"
						+ "sremarks,dmodifieddate,nstatus,nsitecode)values (" + seqNo + ","
						+ objTestMaterial.getNtestgrouptestcode() + "," + "" + objTestMaterial.getNmaterialtypecode()
						+ "," + objTestMaterial.getNmaterialcatcode() + "," + objTestMaterial.getNmaterialcode() + ","
						+ " N'" + stringUtilityFunction.replaceQuote(sremarks) + "','"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + objTestMaterial.getNstatus() + ","
						+ userInfo.getNmastersitecode() + ")";
				jdbcTemplate.execute(materialInsert);

				materialSeq = "update seqnotestgroupmanagement set nsequenceno=" + seqNo
						+ " where stablename='testgrouptestmaterial' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";

				jdbcTemplate.execute(materialSeq);
				Map<String, Object> outputMap = new HashMap<String, Object>();

				outputMap.put("TestGroupTestMaterial", testGroupCommonFunction
						.getTestGroupTestMaterial(objTestMaterial.getNtestgrouptestcode(), 0, userInfo));
				outputMap.put("selectedMaterial", ((List<TestGroupTestMaterial>) outputMap.get("TestGroupTestMaterial"))
						.get(((List<TestGroupTestMaterial>) outputMap.get("TestGroupTestMaterial")).size() - 1));
				objTestMaterial.setNtestgrouptestmaterialcode(seqNo);
				savedTestMaterialList.add(objTestMaterial);
				multilingualIDList.add("IDS_ADDTESTGROUPTESTMATERIAL");

				auditUtilityFunction.fnInsertAuditAction(savedTestMaterialList, 1, null, multilingualIDList, userInfo);

				return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
			} else {
				// Conflict = 409 - Duplicate entry --getSlanguagetypecode
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getActiveTestMaterialById(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {

		final int testGroupTestMaterialCode = (Integer) inputMap.get("ntestgrouptestmaterialcode");

		final String sQuery = "select tgtm.*,mt.nmaterialtypecode,coalesce(mt.jsondata->'smaterialtypename'->>'"
				+ objUserInfo.getSlanguagetypecode() + "' ,"
				+ " mt.jsondata->'smaterialtypename'->>'en-US') as smaterialtypename,mc.nmaterialcatcode,mc.smaterialcatname,m.nmaterialcode,"
				+ " coalesce(m.jsondata->>'Material Name') as smaterialname "
				+ " from testgrouptestmaterial tgtm,materialtype mt,materialcategory mc,material m"
				+ " where mt.nmaterialtypecode=tgtm.nmaterialtypecode and mc.nmaterialcatcode=tgtm.nmaterialcatcode"
				+ " and m.nmaterialcode=tgtm.nmaterialcode and " + " tgtm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " m.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " mt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " mc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " tgtm.ntestgrouptestmaterialcode=" + testGroupTestMaterialCode + " and mt.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and mc.nsitecode=" + objUserInfo.getNmastersitecode()
				+ " and m.nsitecode=" + objUserInfo.getNmastersitecode();
		LOGGER.info(sQuery);
		final TestGroupTestMaterial objTGTM = (TestGroupTestMaterial) jdbcUtilityFunction.queryForObject(sQuery,
				TestGroupTestMaterial.class, jdbcTemplate);
		if (objTGTM != null) {
			return new ResponseEntity<Object>(objTGTM, HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteTestGroupTestMaterial(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final TestGroupTestMaterial objTestMaterial = objMapper.convertValue(inputMap.get("testgrouptestmaterial"),
				TestGroupTestMaterial.class);
		final List<Object> deletedMaterialList = new ArrayList<>();
		final String sQuery = "select tgtm.* from testgrouptestmaterial tgtm" + " where  tgtm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " tgtm.ntestgrouptestmaterialcode=" + objTestMaterial.getNtestgrouptestmaterialcode();
		final TestGroupTestMaterial oldMaterialbyID = (TestGroupTestMaterial) jdbcUtilityFunction.queryForObject(sQuery,
				TestGroupTestMaterial.class, jdbcTemplate);
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);
		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {

			if (oldMaterialbyID != null) {
				String updateQuery = "update testgrouptestmaterial set nstatus = "
						+ Enumeration.TransactionStatus.NA.gettransactionstatus() + "," + "dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'"
						+ " where ntestgrouptestmaterialcode = " + objTestMaterial.getNtestgrouptestmaterialcode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";
				jdbcTemplate.execute(updateQuery);
				Map<String, Object> outMap = new HashMap<String, Object>();
				final List<TestGroupTestMaterial> lstMaterial = testGroupCommonFunction
						.getTestGroupTestMaterial(objTestMaterial.getNtestgrouptestcode(), 0, objUserInfo);
				if (lstMaterial != null && lstMaterial.size() > 0) {
					outMap.put("selectedMaterial", lstMaterial.get(lstMaterial.size() - 1));

				}
				outMap.put("TestGroupTestMaterial", lstMaterial);
				objTestMaterial.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedMaterialList.add(oldMaterialbyID);
				auditUtilityFunction.fnInsertAuditAction(deletedMaterialList, 1, null,
						Arrays.asList("IDS_DELETETESTGROUPTESTMATERIAL"), objUserInfo);

				return new ResponseEntity<Object>(outMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateTestGroupMaterial(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final List<Object> beforeSavedInstList = new ArrayList<>();
		final List<Object> savedInstList = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();
		final TestGroupTestMaterial objTestMaterial = objMapper.convertValue(inputMap.get("testgrouptestmaterial"),
				TestGroupTestMaterial.class);
		inputMap.put("ntestgrouptestmaterialcode", objTestMaterial.getNtestgrouptestmaterialcode());
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);
		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			final TestGroupTestMaterial testMaterial = (TestGroupTestMaterial) getActiveTestMaterialById(inputMap,
					userInfo).getBody();

			if (testMaterial == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final String sQuery = "select tgtm.ntestgrouptestmaterialcode "
						+ " from testgrouptestmaterial tgtm,materialtype mt,materialcategory mc,material m "
						+ " where tgtm.nmaterialtypecode=" + objTestMaterial.getNmaterialtypecode()
						+ " and tgtm.nmaterialcatcode=" + objTestMaterial.getNmaterialcatcode() + ""
						+ "  and tgtm.nmaterialcode=" + objTestMaterial.getNmaterialcode() + " and "
						+ " tgtm.ntestgrouptestcode=" + objTestMaterial.getNtestgrouptestcode() + " and "
						+ "  tgtm.ntestgrouptestmaterialcode != " + objTestMaterial.getNtestgrouptestmaterialcode()
						+ " and mt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and mc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and m.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and tgtm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final List<TestGroupTestMaterial> testMaterialList = (List<TestGroupTestMaterial>) jdbcTemplate
						.query(sQuery, new TestGroupTestMaterial());

				if (testMaterialList.isEmpty()) {

					String query = "update testgrouptestmaterial set " + "sremarks=N'"
							+ stringUtilityFunction.replaceQuote(objTestMaterial.getSremarks()) + "'"
							+ ",nmaterialcode=" + objTestMaterial.getNmaterialcode() + "," + "nmaterialcatcode="
							+ objTestMaterial.getNmaterialcatcode() + " " + ", nmaterialtypecode="
							+ objTestMaterial.getNmaterialtypecode() + ",dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
							+ " where ntestgrouptestmaterialcode=" + objTestMaterial.getNtestgrouptestmaterialcode()
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					jdbcTemplate.execute(query);

					final List<TestGroupTestMaterial> lstMaterial = testGroupCommonFunction.getTestGroupTestMaterial(
							objTestMaterial.getNtestgrouptestcode(), objTestMaterial.getNtestgrouptestmaterialcode(),
							userInfo);
					Optional<TestGroupTestMaterial> SelectedTestMaterial = lstMaterial.stream()
							.filter(x -> ((int) x.getNtestgrouptestmaterialcode()) == objTestMaterial
									.getNtestgrouptestmaterialcode())
							.findAny();
					objMap.put("selectedMaterial", SelectedTestMaterial.get());
					objMap.put("TestGroupTestMaterial", lstMaterial);
					savedInstList.add(objTestMaterial);
					beforeSavedInstList.add(testMaterial);

					auditUtilityFunction.fnInsertAuditAction(savedInstList, 2, beforeSavedInstList,
							Arrays.asList("IDS_EDITTESTGROUPTESTMATERIAL"), userInfo);

					return new ResponseEntity<>(objMap, HttpStatus.OK);
				}

				else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("ALREADYEXIST", userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	private List<TestGroupTestMaterial> getTestMaterialListByName(int ntestgrouptestcode, int nmaterialtypecode,
			int nmaterialcatcode, String smaterialname, UserInfo userInfo) throws Exception {

		final String sQuery = "select tgtm.ntestgrouptestmaterialcode "
				+ " from testgrouptestmaterial tgtm,materialtype mt,materialcategory mc,material m "
				+ " where mt.nmaterialtypecode=" + nmaterialtypecode + " and mc.nmaterialcatcode=" + nmaterialcatcode
				+ "" + " and lower((m.jsondata->>'Material Name'))=lower('"
				+ stringUtilityFunction.replaceQuote(smaterialname) + "') and m.nmaterialcode=tgtm.nmaterialcode  "
				+ " and tgtm.ntestgrouptestcode=" + ntestgrouptestcode + " and " + " tgtm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " tgtm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " tgtm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " tgtm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nsitecode="
				+ userInfo.getNmastersitecode() + " and tgtm.nsitecode=" + userInfo.getNmastersitecode();

		return (List<TestGroupTestMaterial>) jdbcTemplate.query(sQuery, new TestGroupTestMaterial());
	}

	@Override
	public ResponseEntity<Object> validationForRetiredTemplate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);
		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			return new ResponseEntity<>(Enumeration.ReturnStatus.SUCCESS.getreturnstatus(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getAvailableMaterial(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> objMap = new LinkedHashMap<String, Object>();

		final int nmaterialtypecode = (int) inputMap.get("nmaterialtypecode");
		final int nmaterialcatcode = (int) inputMap.get("nmaterialcatcode");
		;
		final String sQuery = "select json_build_object ('nmaterialcode',nmaterialcode,'Material Name',jsondata->'Material Name') as jsondata,(jsondata->'Material Name') as smaterialname  from material m where "
				+ "  (jsondata->'nmaterialcatcode')::int=" + nmaterialcatcode
				+ " and m.nstatus=1 and ( jsondata -> 'nmaterialtypecode' ) :: INT=" + nmaterialtypecode
				+ " and m.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				//+ " and m.nsitecode=" + objUserInfo.getNmastersitecode();

		final List<TestGroupTestMaterial> lstAvailablelist = (List<TestGroupTestMaterial>) jdbcTemplate.query(sQuery,
				new TestGroupTestMaterial());
		objMap.put("MaterialCombo", lstAvailablelist);

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

}
