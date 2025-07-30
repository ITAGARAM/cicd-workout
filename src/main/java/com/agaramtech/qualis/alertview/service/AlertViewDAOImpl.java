package com.agaramtech.qualis.alertview.service;

import java.time.Instant;
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
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.misrights.model.AlertHomeRights;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class AlertViewDAOImpl implements AlertViewDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlertViewDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;

	@Override
	public ResponseEntity<Object> getAlertView(final UserInfo userInfo) throws Exception {
		final Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final String str = " select ar.nalertrightscode, sq.* from alertrights ar, sqlquery sq where ar.nsqlquerycode = sq.nsqlquerycode "
				+ " and sq.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ar.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ar.nsitecode = " + userInfo.getNmastersitecode() + " and ar.nuserrolecode = "
				+ userInfo.getNuserrole();
		final List<SQLQuery> lstAlerts = jdbcTemplate.query(str, new SQLQuery());
		int nsqlQueryCode = 0;
		if (lstAlerts.size() > 0) {
			nsqlQueryCode = lstAlerts.get(lstAlerts.size() - 1).getNsqlquerycode();
			objMap.put("SelectedAlert", lstAlerts.get(lstAlerts.size() - 1));
		}
		objMap.put("AlertView", lstAlerts);
		objMap.putAll((Map<String, Object>) getSelectedAlertView(userInfo, nsqlQueryCode).getBody());
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getSelectedAlertView(final UserInfo userInfo, final int nsqlQueryCode)
			throws Exception {
		final List<Map<String, Object>> objlstMap = new ArrayList<Map<String, Object>>();
		final Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final String sQuery = " select * from  sqlquery where nsqlquerycode = " + nsqlQueryCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode();
		final SQLQuery objSqlQuery = (SQLQuery) jdbcUtilityFunction.queryForObject(sQuery, SQLQuery.class,
				jdbcTemplate);
		objMap.put("SelectedAlert", objSqlQuery);

		if (objSqlQuery != null && objSqlQuery.getSsqlquery().length() > 0) {
			try {
				final ObjectMapper objMap1 = new ObjectMapper();
				final Map<String, Object> mapUserInfo = objMap1.convertValue(userInfo, Map.class);
				String sSQLQuery = objSqlQuery.getSsqlquery();
				sSQLQuery = queryReplace(sSQLQuery, mapUserInfo);
				List<Map<String, Object>> listDataList = jdbcTemplate.queryForList(sSQLQuery);
				listDataList = jdbcUtilityFunction.replaceMultiLingualByRecord(listDataList, userInfo);
				objlstMap.addAll(listDataList);
				objMap.put("ReturnStatus", true);
			} catch (final Exception e) {
				LOGGER.error(e.getMessage());
				objMap.put("ReturnStatus", false);
			}
		}
		objMap.put("selectedAlertView", objlstMap);
		objMap.put("sqlQueryName", objSqlQuery.getSsqlqueryname());

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	public String queryReplace(String sSQLQuery, final Map<String, Object> mapUserInfo) throws Exception {
		final StringBuffer sb = new StringBuffer();
		sb.delete(0, sb.length());
		String sOldKey = "";
		String sData = "NULL";
		// sSQLQuery = Query.replaceAll("\\s", "");
		sb.append(sSQLQuery);

		while (sSQLQuery.contains("<@") || sSQLQuery.contains("<#")) {
			int nStart = -1;
			int nEnd = -1;
			if (sSQLQuery.contains("<@")) {
				nStart = sSQLQuery.indexOf("<@");
				nEnd = sSQLQuery.indexOf("@>");
			} else if (sSQLQuery.contains("<#")) {
				nStart = sSQLQuery.indexOf("<#");
				nEnd = sSQLQuery.indexOf("#>");
			}
			final String sParam = sSQLQuery.substring(nStart + 2, nEnd);
			if (!sParam.equals(sOldKey)) {

				if (mapUserInfo.containsKey(sParam)) {
					if (mapUserInfo.get(sParam).getClass().getName() == "java.lang.Integer") {
						sData = String.valueOf(mapUserInfo.get(sParam));
						sb.replace(nStart, nEnd + 2, sData);
						sSQLQuery = sb.toString();
					} else if (mapUserInfo.get(sParam).getClass().getName() == "java.lang.String") {
						sData = "'" + (String) mapUserInfo.get(sParam) + "'";
						sb.replace(nStart, nEnd + 2, sData);
						sSQLQuery = sb.toString();
					} else if (mapUserInfo.get(sParam).getClass().getName() == "java.lang.Double") {
						sData = String.valueOf(mapUserInfo.get(sParam));
						sb.replace(nStart, nEnd + 2, sData);
						sSQLQuery = sb.toString();
					} else if (mapUserInfo.get(sParam).getClass().getName() == "java.lang.Float") {
						sData = String.valueOf(mapUserInfo.get(sParam));
						sb.replace(nStart, nEnd + 2, sData);
						sSQLQuery = sb.toString();
					} else if (mapUserInfo.get(sParam).getClass().getName() == "java.lang.Long") {
						sData = String.valueOf(mapUserInfo.get(sParam));
						sb.replace(nStart, nEnd + 2, sData);
						sSQLQuery = sb.toString();
					} else if (mapUserInfo.get(sParam).getClass().getName() == "java.lang.Date") {
						sData = String.valueOf(mapUserInfo.get(sParam));
						sb.replace(nStart, nEnd + 2, sData);
						sSQLQuery = sb.toString();
					} else if (mapUserInfo.get(sParam).getClass().getName().equals("java.lang.Short")) {
						sData = String.valueOf(mapUserInfo.get(sParam));
						sb.replace(nStart, nEnd + 2, sData);
						sSQLQuery = sb.toString();
					}
				}
			} else {
				sb.replace(nStart, nEnd + 2, sData);
				sSQLQuery = sb.toString();
			}
			sOldKey = sParam;
		}
		return sSQLQuery;
	}

	@Override
	public ResponseEntity<Object> getAlerts(final UserInfo userInfo) throws Exception {
		final Map<String, Object> objMap = new HashMap<>();
		final ObjectMapper objMap1 = new ObjectMapper();
		objMap1.registerModule(new JavaTimeModule());
		final String query = "Select  dt.ssqlquery,dt.sscreenheader,dr.nsqlquerycode,dr.nalerthomerightscode,dr.nuserrolecode,dr.nsitecode"
				+ "  from alerthomerights dr,sqlquery dt where dt.nquerytypecode="
				+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ""
				+ " and  dt.nsqlquerycode=dr.nsqlquerycode and dt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and dt.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and dr.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and dr.nuserrolecode=" + userInfo.getNuserrole();
		final List<AlertHomeRights> lstTotalAlerts = jdbcTemplate.query(query, new AlertHomeRights());
		final List<Map<String, Object>> objlstMap = new ArrayList<Map<String, Object>>();
		final List<AlertHomeRights> lstValidAlerts = new ArrayList<AlertHomeRights>();
		if (!lstTotalAlerts.isEmpty()) {
			final Map<String, Object> mapUserInfo = objMap1.convertValue(userInfo, Map.class);
			List<Map<String, Object>> listDataList = null;
			System.out.println("Alert Loop start=>" + Instant.now());
			for (int j = 0; j < lstTotalAlerts.size(); j++) {
				String sSQLQuery = lstTotalAlerts.get(j).getSsqlquery();
				if (lstTotalAlerts.get(j).getSsqlquery().length() > 0) {
					sSQLQuery = queryReplace(sSQLQuery, mapUserInfo);
				}
//				listDataList = getJdbcTemplate().queryForList(sSQLQuery);
//				lstTotalAlerts.get(j).setNcount(listDataList.size());
				String countQuery = "";
				final int firstIndex = sSQLQuery.indexOf("select");
				final int lastIndex = sSQLQuery.indexOf("from");
				if (firstIndex == 0) {
					countQuery = "select count(*) " + sSQLQuery.substring(lastIndex);
					final int count = jdbcTemplate.queryForObject(countQuery, Integer.class);
					lstTotalAlerts.get(j).setNcount(count);
				} else {
					final List<Map<String, Object>> countList = jdbcTemplate.queryForList(sSQLQuery);
					lstTotalAlerts.get(j).setNcount(countList.size());
				}
				if (lstTotalAlerts.get(j).getNcount() > 0) {
					if (lstValidAlerts.isEmpty()) {
						listDataList = jdbcTemplate.queryForList(sSQLQuery);
						listDataList = jdbcUtilityFunction.replaceMultiLingualByRecord(listDataList, userInfo);
						objlstMap.addAll(listDataList);
					}
					lstValidAlerts.add(lstTotalAlerts.get(j));
				}
			}
			System.out.println("Alert Loop  END=>" + Instant.now());
			objMap.put("selectedAlertView1", objlstMap);
			objMap.put("selectedAlertView", lstValidAlerts.get(0));
		} else {
			objMap.put("selectedAlertView1", lstTotalAlerts);
			objMap.put("selectedAlertView", null);
		}
		objMap.put("alert", lstValidAlerts);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSelectedAlert(final UserInfo userInfo, final int nsqlQueryCode) throws Exception {
		final ObjectMapper objMap1 = new ObjectMapper();
		final List<Map<String, Object>> objlstMap = new ArrayList<Map<String, Object>>();
		final Map<String, Object> objMap = new LinkedHashMap<String, Object>();

		final String sQuery = " select * from  sqlquery where nsqlquerycode = " + nsqlQueryCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode();

		final SQLQuery objSqlQuery = (SQLQuery) jdbcUtilityFunction.queryForObject(sQuery, SQLQuery.class,
				jdbcTemplate);

		if (objSqlQuery != null && objSqlQuery.getSsqlquery().length() > 0) {
			String sSQLQuery = objSqlQuery.getSsqlquery();
			final Map<String, Object> mapUserInfo = objMap1.convertValue(userInfo, Map.class);
			sSQLQuery = queryReplace(sSQLQuery, mapUserInfo);
			List<Map<String, Object>> listDataList = jdbcTemplate.queryForList(sSQLQuery);
			listDataList = jdbcUtilityFunction.replaceMultiLingualByRecord(listDataList, userInfo);
			objlstMap.addAll(listDataList);
		}
		objMap.put("selectedAlertView1", objlstMap);
		objMap.put("selectedAlertView", objSqlQuery);
		objMap.put("sqlQueryName", objSqlQuery.getSsqlqueryname());
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}
}