package com.agaramtech.qualis.configuration.service.sorting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.credential.model.QualisMenu;
import com.agaramtech.qualis.credential.model.QualisModule;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation by implementing methods from its
 * interface.
 */

@RequiredArgsConstructor
@Repository
public class SortingDAOImpl implements SortingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SortingDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;

	@Override
	public ResponseEntity<Object> getSorting(UserInfo userInfo, Integer boolValue) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select nsorter,nmenucode, smenuname, nstatus"
				+ ", coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as sdisplayname from qualismenu where" + " nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nmenucode>0 order by nmenucode asc";
		List<QualisMenu> lstMenuSorting = (List<QualisMenu>) jdbcTemplate.query(strQuery, new QualisMenu());
		outputMap.put("MenuList", lstMenuSorting);
		outputMap.put("SelectedMenuSorting", lstMenuSorting.get(0));
		final String strQueryModule = "select nmodulecode, nmenucode, smodulename, "
				+ "coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as sdisplayname," + " nsorter, nstatus from qualismodule "
				+ " where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmenucode="
				+ lstMenuSorting.get(0).getNmenucode() + " order by nmodulecode asc";
		List<QualisModule> lstQualisModule = jdbcTemplate.query(strQueryModule, new QualisModule());
		outputMap.put("moduleList", lstQualisModule);
		outputMap.put("SelectedModuleSorting", lstQualisModule.get(0));
		outputMap.put("QualisModules", lstQualisModule);
		final String strQueryForms = "select qf.nformcode, qf.nmenucode, qf.nmodulecode, qf.sformname, "
				+ "coalesce(qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',qf.jsondata->'sdisplayname'->>'en-US') as sdisplayname,"
				+ " qf.sclassname, qf.surl, qf.nsorter, qf.nstatus from qualisforms qf,sitequalisforms sqf  where "
				+ " qf.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sqf.nformcode = qf.nformcode " + " and sqf.nsitecode =  " + userInfo.getNmastersitecode()
				+ " and sqf.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and qf.nmenucode =" + lstMenuSorting.get(0).getNmenucode() + " and qf.nmodulecode ="
				+ lstQualisModule.get(0).getNmodulecode() + " order by qf.nsorter asc";
		List<QualisModule> lstQualisForms = jdbcTemplate.query(strQueryForms, new QualisModule());
		outputMap.put("formsList", lstQualisForms);
		outputMap.put("QualisForms", lstQualisForms);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getFilter(UserInfo userInfo, Integer nmenuCode, Integer boolValue) throws Exception {
		final String strQueryModule = "select nmodulecode, nmenucode, smodulename,"
				+ " coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as sdisplayname,"
				+ " nsorter, nstatus from qualismodule where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmenucode =" + nmenuCode
				+ " order by nsorter asc";
		LOGGER.info("getFilter:" + strQueryModule);
		return getFilter1(userInfo, nmenuCode, null, boolValue);
	}

	@Override
	public ResponseEntity<Object> getFilter1(UserInfo userInfo, Integer nmenuCode, Integer nmoduleCode,
			Integer boolValue) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		final String strQueryMenu = "select nmenucode, smenuname," + " coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',jsondata->'sdisplayname'->>'en-US') as sdisplayname,"
				+ " nsorter, nstatus from qualismenu where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " nmenucode>0 order by nmenucode asc";
		List<QualisMenu> lstQualisMenu = jdbcTemplate.query(strQueryMenu, new QualisMenu());
		returnMap.put("MenuList", lstQualisMenu);
		final String strQueryMenu1 = "select nmenucode, smenuname," + " coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',jsondata->'sdisplayname'->>'en-US') as sdisplayname,"
				+ " nsorter, nstatus from qualismenu where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " nmenucode =" + nmenuCode
				+ "";
		List<QualisMenu> lstQualisMenu1 = jdbcTemplate.query(strQueryMenu1, new QualisMenu());
		returnMap.put("SelectedMenuSorting", lstQualisMenu1.get(0));
		final String strQueryModule = "select nmodulecode, nmenucode, smodulename, "
				+ " coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " nsorter, nstatus from qualismodule where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmenucode =" + nmenuCode
				+ " order by nmodulecode asc";
		List<QualisModule> lstQualisModule = jdbcTemplate.query(strQueryModule, new QualisModule());
		returnMap.put("moduleList", lstQualisModule);
		if (nmoduleCode == null) {
			nmoduleCode = (int) lstQualisModule.get(0).getNmodulecode();
			returnMap.put("SelectedModuleSorting", lstQualisModule.get(0));
		}
		if (boolValue == 1) {
			if (nmoduleCode != null) {
				final String strQueryModule1 = "select nmodulecode, nmenucode, smodulename, "
						+ " coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
						+ "',jsondata->'sdisplayname'->>'en-US') as sdisplayname,"
						+ " nsorter, nstatus from qualismodule where nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmenucode =" + nmenuCode
						+ " and nmodulecode =" + nmoduleCode + " order by nmodulecode asc";
				List<QualisModule> lstQualisModule1 = jdbcTemplate.query(strQueryModule1, new QualisModule());
				nmoduleCode = (int) lstQualisModule1.get(0).getNmodulecode();
				returnMap.put("SelectedModuleSorting", lstQualisModule1.get(0));
			}
			final String strQuery = "select qf.*,coalesce(qf.jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode() + "',qf.jsondata->'sdisplayname'->>'en-US') as sdisplayname,"
					+ " qm.smodulename,q.smenuname from qualisforms qf,qualismodule qm,qualismenu q,sitequalisforms sqf where qf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qm.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  sqf.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sqf.nformcode = qf.nformcode "
					+ "  and sqf.nsitecode =  " + userInfo.getNmastersitecode()
					+ " and qf.nmodulecode = qm.nmodulecode and qf.nmenucode = q.nmenucode and q.nmenucode ="
					+ nmenuCode + " and qf.nmodulecode =" + nmoduleCode + " order by qf.nsorter asc ";
			List<QualisMenu> lstQualisForms = (List<QualisMenu>) jdbcTemplate.query(strQuery, new QualisMenu());
			returnMap.put("QualisForms", lstQualisForms);
		} else if (boolValue == 2) {
			final String strQueryModule1 = "select nmodulecode, nmenucode, smodulename, "
					+ " coalesce(jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "',jsondata->'sdisplayname'->>'en-US') as sdisplayname,"
					+ " nsorter, nstatus from qualismodule where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmenucode =" + nmenuCode
					+ " order by nmodulecode asc";
			List<QualisModule> lstQualisModule1 = jdbcTemplate.query(strQueryModule1, new QualisModule());
			returnMap.put("moduleList", lstQualisModule);
			lstQualisMenu1.stream().forEach(menu -> {
				List<QualisModule> moduleList = new ArrayList<QualisModule>();
				if (lstQualisModule1.size() > 0) {
					lstQualisModule1.stream().forEach(module -> {
						if (menu.getNmenucode() == module.getNmenucode()) {
							moduleList.add(module);
						}
						returnMap.put(menu.getSmenuname(), moduleList);
					});
				} else {

					returnMap.put(menu.getSmenuname(), lstQualisModule1);
				}
			});
			returnMap.put("QualisModules", lstQualisModule1);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateForms(UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		String updateQueryString = "";
		List<Map<String, Object>> lstofMap = objMapper.convertValue(inputMap.get("changedValues"),
				new TypeReference<List<Map<String, Object>>>() {
				});
		int nsorter = 1;
		for (int i = 0; i < lstofMap.size(); i++) {
			Map<String, Object> mapObject = lstofMap.get(i);
			updateQueryString += "update qualisforms set nsorter =" + nsorter + ", dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where " + " nformcode="
					+ mapObject.get("nformcode") + ";";

			updateQueryString += "update sitequalisforms set nsorter =" + nsorter + ", dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nformcode ="
					+ mapObject.get("nformcode") + "  and  nsitecode =  " + userInfo.getNmastersitecode() + ";";
			nsorter++;
		}
		jdbcTemplate.execute(updateQueryString);
		int boolValue = 1;
		final int nmenuCode = (Integer) inputMap.get("primarykey");
		final int nmoduleCode = (Integer) inputMap.get("nmodulecode");
		return getFilter1(userInfo, nmenuCode, nmoduleCode, boolValue);
	}

	@Override
	public ResponseEntity<Object> updateModules(UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		String updateQueryString = "";
		List<Map<String, Object>> lstofMap = objMapper.convertValue(inputMap.get("changedValues"),
				new TypeReference<List<Map<String, Object>>>() {
				});
		int nsorter = 1;
		for (int i = 0; i < lstofMap.size(); i++) {
			Map<String, Object> mapObject = lstofMap.get(i);
			updateQueryString += "update qualismodule set nsorter =" + nsorter + " where nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmodulecode ="
					+ mapObject.get("nmodulecode") + ";";
			nsorter++;
		}
		jdbcTemplate.execute(updateQueryString);
		int boolValue = 2;
		final int nmenuCode = (Integer) inputMap.get("primarykey");
		return getFilter1(userInfo, nmenuCode, 0, boolValue);
	}
}
