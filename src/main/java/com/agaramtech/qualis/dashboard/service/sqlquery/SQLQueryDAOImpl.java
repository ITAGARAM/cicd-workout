package com.agaramtech.qualis.dashboard.service.sqlquery;

import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.dashboard.model.ChartPropTransaction;
import com.agaramtech.qualis.dashboard.model.ChartType;
import com.agaramtech.qualis.dashboard.model.DashBoardDesignConfig;
import com.agaramtech.qualis.dashboard.model.DashBoardParameterMapping;
import com.agaramtech.qualis.dashboard.model.DashBoardType;
import com.agaramtech.qualis.dashboard.model.QueryBuilder;
import com.agaramtech.qualis.dashboard.model.QueryBuilderTableColumns;
import com.agaramtech.qualis.dashboard.model.QueryBuilderTables;
import com.agaramtech.qualis.dashboard.model.QueryBuilderViews;
import com.agaramtech.qualis.dashboard.model.QueryBuilderViewsColumns;
import com.agaramtech.qualis.dashboard.model.QueryTableType;
import com.agaramtech.qualis.dashboard.model.QueryType;
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TestGroupCommonFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.product.service.product.ProductDAOImpl;
import com.agaramtech.qualis.testgroup.model.TestGroupTestParameter;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.testmanagement.model.TestParameter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;


@AllArgsConstructor
@Repository
public class SQLQueryDAOImpl implements SQLQueryDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private TestGroupCommonFunction objTestGroupCommonFunction;

	
	@Override
	public ResponseEntity<Object> getSQLQuery(Integer nsqlquerycode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		SQLQuery selectedSQLQuery = null;
		QueryType selectedQueryType = null;

		final ObjectMapper mapper = new ObjectMapper();

		if (nsqlquerycode == null) {
			final String strQuery = "Select nquerytypecode,squerytypename,nsorter,nsitecode,nstatus from querytype where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  and nsitecode="+userInfo.getNmastersitecode()+" and nquerytypecode > 0 order by nsorter ";

			final List<QueryType> queryTypeList = jdbcTemplate.query(strQuery, new QueryType());
			if (queryTypeList.isEmpty()) {
				outputMap.put("SelectedSQLQuery", null);
				outputMap.put("SQLQuery", queryTypeList);
				outputMap.put("QueryType", queryTypeList);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				final List<QueryType> modifiedTypeList = mapper
						.convertValue(
								commonFunction.getMultilingualMessageList(queryTypeList,
										Arrays.asList("squerytypename"), userInfo.getSlanguagefilename()),
								new TypeReference<List<QueryType>>() {
								});
				outputMap.put("QueryType", modifiedTypeList);
				selectedQueryType = ((QueryType) modifiedTypeList.get(0));
				outputMap.put("SelectedQueryType", selectedQueryType);
				outputMap.putAll((Map<String, Object>) getSQLQueryByQueryTypeCode(selectedQueryType.getNquerytypecode(),
						userInfo).getBody());
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			}
		} else {
			selectedSQLQuery = getActiveSQLQueryById(nsqlquerycode, userInfo);
			outputMap.put("SelectedSQLQuery", selectedSQLQuery);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	
	public ResponseEntity<Object> getSQLQueryAfterSave(short nquerytypecode, Integer nsqlquerycode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		SQLQuery selectedSQLQuery = null;

		if (nsqlquerycode == 0) {
			final String sQuery = "Select s.nsqlquerycode,s.nquerytypecode,s.ssqlqueryname,s.ssqlquery,"
					+ "s.sscreenrecordquery,s.sscreenheader,s.ncharttypecode,s.svaluemember,s.sdisplaymember,"
					+ "s.nsitecode,s.nstatus,q.squerytypename,coalesce(c.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',"
					+ " c.jsondata->'sdisplayname'->>'en-US') as schartname from sqlquery s,querytype q,charttype c"
					+ " where  s.nquerytypecode = q.nquerytypecode and s.ncharttypecode = c.ncharttypecode and s.nsitecode="+userInfo.getNmastersitecode()+" and "
					+ " s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and q.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nquerytypecode = " + nquerytypecode;
			final List<SQLQuery> sqlQueryList = jdbcTemplate.query(sQuery, new SQLQuery());
			final ObjectMapper mapper = new ObjectMapper();
			final List<SQLQuery> modifiedTypeList = mapper.convertValue(
					commonFunction.getMultilingualMessageList(sqlQueryList, Arrays.asList("squerytypename"),
							userInfo.getSlanguagefilename()),
					new TypeReference<List<SQLQuery>>() {
					});

			if (!modifiedTypeList.isEmpty()) {
				selectedSQLQuery = ((SQLQuery) modifiedTypeList.get(modifiedTypeList.size() - 1));
			}

			outputMap.put("SQLQuery", modifiedTypeList);
			outputMap.put("SelectedSQLQuery", selectedSQLQuery);
		} else {
			selectedSQLQuery = getActiveSQLQueryById(nsqlquerycode, userInfo);

			final ObjectMapper mapper = new ObjectMapper();
			final List<SQLQuery> modifiedTypeList = mapper.convertValue(
					commonFunction.getMultilingualMessageList(Arrays.asList(selectedSQLQuery),
							Arrays.asList("squerytypename"), userInfo.getSlanguagefilename()),
					new TypeReference<List<SQLQuery>>() {
					});

			outputMap.put("SelectedSQLQuery", modifiedTypeList.get(0));
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	public ResponseEntity<Object> getSQLQueryByQueryTypeCode(short nquerytypecode, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		SQLQuery selectedSQLQuery = null;

		String strQuery = "Select nquerytypecode,squerytypename,nsorter,nsitecode,nstatus from querytype where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nquerytypecode  = "
				+ nquerytypecode;

		final QueryType queryType = (QueryType) jdbcUtilityFunction.queryForObject(strQuery, QueryType.class,jdbcTemplate);

		final ObjectMapper mapper = new ObjectMapper();

		final List<QueryType> modifiedTypeList = mapper
				.convertValue(
						commonFunction.getMultilingualMessageList(Arrays.asList(queryType),
								Arrays.asList("squerytypename"), userInfo.getSlanguagefilename()),
						new TypeReference<List<QueryType>>() {
						});
		outputMap.put("SelectedQueryType", modifiedTypeList.get(0));

		strQuery = "Select s.nsqlquerycode,s.nquerytypecode,s.ssqlqueryname,s.ssqlquery,s.sscreenrecordquery,s.sscreenheader,s.ncharttypecode,s.svaluemember,s.sdisplaymember,s.nsitecode,s.nstatus,q.squerytypename,coalesce(c.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',c.jsondata->'sdisplayname'->>'en-US') as schartname from sqlquery s,querytype q,charttype c where s.nquerytypecode = q.nquerytypecode and s.ncharttypecode = c.ncharttypecode and "
				+ " s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and s.nsitecode="+userInfo.getNmastersitecode()+" and s.nquerytypecode = "
				+ nquerytypecode;

		final List<SQLQuery> sqlQueryList = jdbcTemplate.query(strQuery, new SQLQuery());

		if (sqlQueryList.isEmpty()) {
			outputMap.put("SelectedSQLQuery", null);
			outputMap.put("SQLQuery", sqlQueryList);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			final List<SQLQuery> modifiedList = mapper.convertValue(
					commonFunction.getMultilingualMessageList(sqlQueryList, Arrays.asList("squerytypename"),
							userInfo.getSlanguagefilename()),
					new TypeReference<List<SQLQuery>>() {
					});
			outputMap.put("SQLQuery", modifiedList);
			selectedSQLQuery = ((SQLQuery) modifiedList.get(modifiedList.size() - 1));

			outputMap.put("SelectedSQLQuery", selectedSQLQuery);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	public SQLQuery getActiveSQLQueryById(final int nsqlquerycode, UserInfo userInfo) throws Exception {

		final String strQuery = "Select s.nsqlquerycode,s.nquerytypecode,s.ssqlqueryname,s.ssqlquery,s.sscreenrecordquery,s.sscreenheader,s.ncharttypecode,s.svaluemember,s.sdisplaymember,s.nsitecode,s.nstatus,q.squerytypename,coalesce(c.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',c.jsondata->'sdisplayname'->>'en-US') as schartname from sqlquery s,querytype q,charttype c where s.nquerytypecode = q.nquerytypecode and s.ncharttypecode = c.ncharttypecode and "
				+ " s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode="+userInfo.getNmastersitecode()+" and s.nsqlquerycode = "
				+ nsqlquerycode;
		return (SQLQuery) jdbcUtilityFunction.queryForObject(strQuery, SQLQuery.class, jdbcTemplate);
	}

	public SQLQuery getActiveSQLQueryByQueryTypeCode(final int nquerytypecode, UserInfo userInfo) throws Exception {
		final String strQuery = "Select s.nsqlquerycode,s.nquerytypecode,s.ssqlqueryname,s.ssqlquery,s.sscreenrecordquery,s.sscreenheader,s.ncharttypecode,s.svaluemember,s.sdisplaymember,s.nsitecode,s.nstatus, q.squerytypename,coalesce(c.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',c.jsondata->'sdisplayname'->>'en-US') as schartname from sqlquery s,querytype q,charttype c where s.nquerytypecode = q.nquerytypecode and s.ncharttypecode = c.ncharttypecode and "
				+ " s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode="+userInfo.getNmastersitecode()+" and s.nquerytypecode = "
				+ nquerytypecode;
		return (SQLQuery) jdbcUtilityFunction.queryForObject(strQuery, SQLQuery.class, jdbcTemplate);
	}

	
	@Override
	public ResponseEntity<Object> createSQLQuery(SQLQuery sqlQuery, UserInfo userInfo, Integer nqueryTypeCode)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSQLQueryList = new ArrayList<>();

		// Integer nsqlquerycode=null;
		final SQLQuery sqlQueryListByName = getSQLQueryListByName(sqlQuery.getSsqlqueryname(), sqlQuery.getNsitecode(),
				sqlQuery.getNquerytypecode());

		if (sqlQueryListByName == null) {
			
			String sequencequery ="select nsequenceno from SeqNoDashBoardManagement where stablename ='sqlquery'"
					+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class,jdbcTemplate);
			nsequenceno++;
			
			String insertquery  = "Insert into sqlquery (nsqlquerycode, nquerytypecode, ssqlqueryname, ssqlquery, sscreenrecordquery, sscreenheader, ncharttypecode, svaluemember, sdisplaymember, dmodifieddate, nsitecode, nstatus) values"
					+ "("+nsequenceno+", "+sqlQuery.getNquerytypecode()+", N'"+stringUtilityFunction.replaceQuote(sqlQuery.getSsqlqueryname())+"', N'"+stringUtilityFunction.replaceQuote(sqlQuery.getSsqlquery())+"', N'"+stringUtilityFunction.replaceQuote(sqlQuery.getSscreenrecordquery())
					+ "', N'"+stringUtilityFunction.replaceQuote(sqlQuery.getSscreenheader())+"', "+sqlQuery.getNcharttypecode()+", N'"+stringUtilityFunction.replaceQuote(sqlQuery.getSvaluemember())+"', N'"+stringUtilityFunction.replaceQuote(sqlQuery.getSdisplaymember())
					+ "', '"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', "+userInfo.getNmastersitecode()+", "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")";
			jdbcTemplate.execute(insertquery);
			
			String updatequery ="update SeqNoDashBoardManagement set nsequenceno ="+nsequenceno+" where stablename='sqlquery'";
			jdbcTemplate.execute(updatequery);

			savedSQLQueryList.add(sqlQuery);
			multilingualIDList.add("IDS_ADDSQLQuery");

			auditUtilityFunction.fnInsertAuditAction(savedSQLQueryList, 1, null, multilingualIDList,userInfo);
			// status code:200

			return getSQLQueryAfterSave(sqlQuery.getNquerytypecode(), 0, userInfo);
		} else {
			// Conflict = 409 - Duplicate entry
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	
	private SQLQuery getSQLQueryListByName(final String ssqlqueryname, final int nmasterSiteCode,
			final short nqueryTypeCode) throws Exception {
		final String strQuery = "select nsqlquerycode from sqlquery where ssqlqueryname = N'"
				+ stringUtilityFunction.replaceQuote(ssqlqueryname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nquerytypecode =" + nqueryTypeCode
				+ " and nsitecode =" + nmasterSiteCode;
		return (SQLQuery) jdbcUtilityFunction.queryForObject(strQuery, SQLQuery.class, jdbcTemplate);
	}

	
	@Override
	public ResponseEntity<Object> updateSQLQuery(SQLQuery sqlQuery, UserInfo userInfo) throws Exception {

		final SQLQuery sqlQueryByID = (SQLQuery) getActiveSQLQueryById(sqlQuery.getNsqlquerycode(), userInfo);
		if (sqlQueryByID == null) {
			// status code:205
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			String updateQueryString = "";
			String queryString = "select nsqlquerycode from sqlquery where ssqlqueryname = N'"
					+ stringUtilityFunction.replaceQuote(sqlQuery.getSsqlqueryname()) + "' and nsqlquerycode <> "
					+ sqlQuery.getNsqlquerycode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode()+" and nquerytypecode="
					+ sqlQuery.getNquerytypecode();

			final SQLQuery sqlQueryVal = (SQLQuery)jdbcUtilityFunction.queryForObject(queryString, SQLQuery.class,jdbcTemplate);

			if (sqlQueryVal == null) {
				queryString = "select ndashboardtypecode,nquerycode from dashboardtype where nquerycode="
						+ sqlQuery.getNsqlquerycode() + " and nsitecode="+userInfo.getNmastersitecode()+ " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				List<DashBoardType> lstDashBoard = (List<DashBoardType>) jdbcTemplate.query(queryString,new DashBoardType());

				String query = "";
				List<?> lstAudit = null;

				if (!lstDashBoard.isEmpty()) {
					queryString = stringUtilityFunction.fnDynamicListToString(lstDashBoard, "getNdashboardtypecode");

					updateQueryString = "delete from dashboarddesignconfig where ndashboardtypecode in (" + queryString
							+ ");" + "delete from dashboardparametermapping  where ndashboardtypecode in ("
							+ queryString + ");" + "delete from chartproptransaction where ndashboardtypecode in ("
							+ queryString + ");";
					jdbcTemplate.execute(updateQueryString);
					
					query = "select * from chartproptransaction where ndashboardtypecode in (" + queryString + ");"
							+ "select * from dashboarddesignconfig where ndashboardtypecode in (" + queryString + ");"
							+ "select * from dashboardparametermapping where ndashboardtypecode in (" + queryString
							+ ");";
					
					lstAudit = projectDAOSupport.getMultipleEntitiesResultSetInList(query, jdbcTemplate,ChartPropTransaction.class,
							DashBoardDesignConfig.class, DashBoardParameterMapping.class);
					
				}

				if (lstAudit != null) {
					auditUtilityFunction.fnInsertListAuditAction(
							lstAudit, 1, null, Arrays.asList("IDS_DELETECHARTPROPTRANSACTION",
									"IDS_DELETEDASHBOARDDESIGNCONFIG", "IDS_DELETEDASHBOARDPARAMETERMAPPING"),
							userInfo);
				}

			}
			if (sqlQueryVal == null) {
				updateQueryString = " update sqlquery set ssqlqueryname=N'" + stringUtilityFunction.replaceQuote(sqlQuery.getSsqlqueryname())
						+ "', nquerytypecode =" + sqlQuery.getNquerytypecode() + ",ssqlquery = N'"
						+ stringUtilityFunction.replaceQuote(sqlQuery.getSsqlquery().trim()) + "',sscreenrecordquery = N'"
						+ stringUtilityFunction.replaceQuote(sqlQuery.getSscreenrecordquery()) + "',sscreenheader = N'"
						+ stringUtilityFunction.replaceQuote(sqlQuery.getSscreenheader()) + "',ncharttypecode = "
						+ sqlQuery.getNcharttypecode() + ",svaluemember = N'" + stringUtilityFunction.replaceQuote(sqlQuery.getSvaluemember())
						+ "',sdisplaymember = N'" + stringUtilityFunction.replaceQuote(sqlQuery.getSdisplaymember())+ "', dmodifieddate ='"+dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nsqlquerycode=" + sqlQuery.getNsqlquerycode();

				jdbcTemplate.execute(updateQueryString);

				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(sqlQuery);

				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(sqlQueryByID);

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, Arrays.asList("IDS_EDITSQLQUERY"), userInfo);

				// status code:200
				return getSQLQueryAfterSave(sqlQueryByID.getNquerytypecode(), sqlQuery.getNsqlquerycode(), userInfo);

			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}


	@Override
	public ResponseEntity<Object> deleteSQLQuery(SQLQuery sqlQuery, UserInfo userInfo) throws Exception {
		final SQLQuery sqlQueryById = (SQLQuery) getActiveSQLQueryById(sqlQuery.getNsqlquerycode(), userInfo);

		if (sqlQueryById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "select 'IDS_SQLALERTASSOCIATED' as Msg from alertrights where nsqlquerycode= "
					+ sqlQuery.getNsqlquerycode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_SQLDASHBOARDTYPEASSOCIATED' as Msg from dashboardtype where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nquerycode = "
					+ sqlQuery.getNsqlquerycode()
					+ " union all  select  'IDS_SQLREPORTASSOCIATED' as Msg from reportdesignconfig where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsqlquerycode = "
					+ sqlQuery.getNsqlquerycode()
					+ " union all select 'IDS_SQLDASHBOARDASSOCIATED' as Msg from dashboarddesignconfig where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsqlquerycode ="
					+ sqlQuery.getNsqlquerycode();

			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				final List<Object> savedSQLQueryList = new ArrayList<>();
				final String updateQueryString = "update sqlquery set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()+", dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nsqlquerycode="
						+ sqlQuery.getNsqlquerycode();

				jdbcTemplate.execute(updateQueryString);

				sqlQuery.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedSQLQueryList.add(sqlQuery);
				auditUtilityFunction.fnInsertAuditAction(savedSQLQueryList, 1, null, Arrays.asList("IDS_DELETESQLQUERY"), userInfo);
				return getSQLQueryAfterSave(sqlQueryById.getNquerytypecode(), 0, userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}


	@Override
	public ResponseEntity<Object> getChartType(UserInfo userInfo) throws Exception {

		final String strQuery = "Select ncharttypecode, nseries, dmodifieddate, nsitecode,"
				+ " nstatus, coalesce(jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',jsondata->'sdisplayname'->>'en-US') as schartname"
						+ " from charttype where ncharttypecode > 0 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return new ResponseEntity<>((List<ChartType>) jdbcTemplate.query(strQuery, new ChartType()), HttpStatus.OK);
	}

	
	@Override
	public ResponseEntity<Object> getQueryTableType() throws Exception {

		final String strQuery = "Select * from querytabletype where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return new ResponseEntity<>((List<QueryTableType>) jdbcTemplate.query(strQuery, new QueryTableType()),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getModuleFormName(int TableTypeCode, UserInfo userInfo) throws Exception {

		List<Object> lsResult = new ArrayList<Object>();
		String strQuery = "";
		if (TableTypeCode == 1) {
			strQuery = "select nmodulecode nformcode,nmenucode,smodulename sformname,jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode() + "' sdisplayname,"
					+ "nsorter,nstatus from qualismodule where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter";
		} else {
			strQuery = "select *,jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "' sdisplayname from qualisforms where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nsorter";
		}
		List<Map<String, Object>> lsData = jdbcTemplate.queryForList(strQuery);
		lsResult.add(lsData);
		return new ResponseEntity<>(lsResult, HttpStatus.OK);
	}

	
	@Override
	public ResponseEntity<Object> getSchemaQueryOutput(String Query, final UserInfo userInfo, String ReturnOption)
			throws Exception {
		List<Object> lsResult = new ArrayList<Object>();
		Query.trim();
		Query.toLowerCase();
		if (Query.startsWith("s") == true || Query.startsWith("S") == true || Query.startsWith("d") == true
				|| Query.startsWith("D") == true || Query.startsWith("e") == true || Query.startsWith("W")
				|| Query.startsWith("E")) {
			final String Querys = Query;
			String[] strArr = new String[0];
			String sValue = "";
			String sTempValue = "";
			String sUTCValue = "";
			LOGGER.info(Query);
			try {
				if (Querys.indexOf("<#") > 0) {
					strArr = Querys.split("<#");
					if (strArr.length > 1) {
						for (int i = 1; i < strArr.length; i++) {
							sTempValue = strArr[i].substring(0, strArr[i].indexOf("#>")) + "T00:00:00.000Z";
							sValue = strArr[i].substring(0, strArr[i].indexOf("#>"));
							final Instant instantDate = dateUtilityFunction.convertStringDateToUTC(sTempValue, userInfo,
									false);
							sUTCValue = instantDate.toString();
							sUTCValue = sUTCValue.replace("T", " ");
							sUTCValue = sUTCValue.replace("Z", "");
							Query = Query.replace("<#" + sValue.trim() + "#>", "'" + sUTCValue.trim() + "'");
						}
					}
				}

				List<Map<String, Object>> lsData =jdbcTemplate.queryForList(Query);

				if (!lsData.isEmpty()) {
					lsData = jdbcUtilityFunction.replaceMultiLingualByRecord(lsData, userInfo);
				}

				if (ReturnOption.equals("LIST") == true) {
					lsResult.add("Success");
					lsResult.add(lsData);
				} else {
					lsResult.add("Success");
					lsResult.add(lsData.size());
				}
				return new ResponseEntity<>(lsResult, HttpStatus.OK);

			} catch (Exception e) {
				List<Map<String, Object>> lsDat = new ArrayList<>();

				StringBuilder sb = new StringBuilder();
				sb.append("Msg 208, Level 16, State 1, Line 1").append("\n");
				sb.append("Invalid object name" + " " + "'" + Query + "'");

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("Messages", sb.toString());
				lsDat.add(map);

				lsResult.add("Warning");
				lsResult.add(lsDat);
				return new ResponseEntity<>(lsResult, HttpStatus.OK);

			}
		} else {
			final String returnString = commonFunction.getMultilingualMessage("IDS_ENTERVALIDQUERY",
					userInfo.getSlanguagefilename());

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Messages", returnString);

			List<Map<String, Object>> lsDat = new ArrayList<>();
			lsDat.add(map);
			lsResult.add("Warning");
			lsResult.add(lsDat);
			return new ResponseEntity<>(lsResult, HttpStatus.OK);
		}

	}

	public List<Map<String, Object>> getMultilingualMessageListOfMap(List<Map<String, Object>> list,
			final List<String> columnList, final List<String> dateColumn, final UserInfo userInfo) throws Exception {

		if (!columnList.isEmpty()) {
			ResourceBundle resourcebundle = commonFunction.getResourceBundle(userInfo.getSlanguagefilename(), false);
			list.stream().map(item -> {
				try {
					for (int i = 0; i < columnList.size(); i++) {
						// System.out.println("item.get(columnList.get(i).trim()):"+ columnList.get(i) +
						// " :"+ item.get(columnList.get(i).trim()));
						item.replace(columnList.get(i).trim(),
								resourcebundle.containsKey((String) item.get(columnList.get(i).trim()))
										? resourcebundle.getString((String) item.get(columnList.get(i).trim()))
										: (String) item.get(columnList.get(i).trim()));
					}
				} catch (Exception e) {
					// e.printStackTrace();
				}
				return item;
			}).collect(Collectors.toList());
		}
		if (!dateColumn.isEmpty()) {
			SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateTimeFormatter destFormat = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			list = list.stream().map(item -> {
				try {
					for (int i = 0; i < dateColumn.size(); i++) {
						String val = (String) String.valueOf(item.get(dateColumn.get(i)));
						if (userInfo.getIsutcenabled()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							LocalDateTime ldt = sourceFormat.parse(val).toInstant()
									.atZone(ZoneId.of(userInfo.getStimezoneid())).toLocalDateTime();
							item.replace(dateColumn.get(i).trim(), destFormat.format(ldt));

						} else {
							LocalDateTime ldt = sourceFormat.parse(val).toInstant().atZone(ZoneId.systemDefault())
									.toLocalDateTime();
							item.replace(dateColumn.get(i).trim(), destFormat.format(ldt));
						}

					}
				} catch (Exception e) {
					
					// e.printStackTrace();
				}
				return item;
			}).collect(Collectors.toList());
		}
		return list;
	}


	public ResponseEntity<Object> getColumnValues(String TableName, String FieldName, String DisplayParam)
			throws Exception {
		List<Object> lsResult = new ArrayList<Object>();
		String Query = "";
		try {
			if (DisplayParam == null || DisplayParam.isEmpty()) {
				Query = "Select Distinct " + FieldName + " from " + TableName + "";
			} else {
				Query = "Select Distinct " + FieldName + "," + DisplayParam + " from " + TableName + "";
			}
			List<Map<String, Object>> lsData = jdbcTemplate.queryForList(Query);

			lsResult.add("Success");
			lsResult.add(lsData);
		} catch (Exception e) {
			List<Map<String, Object>> lsDat = new ArrayList<>();
			String st = "Invalid object name" + " " + "'" + Query + "'";

			StringBuilder sb = new StringBuilder();
			sb.append("Msg 208, Level 16, State 1, Line 1").append("\n");
			sb.append(st);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Messages", sb.toString());
			lsDat.add(map);

			System.out.println(sb.toString());
			lsResult.add("Warning");
			lsResult.add(lsDat);

		}
		return new ResponseEntity<>(lsResult, HttpStatus.OK);

	}

	public ResponseEntity<Object> getTableColumnNames() throws Exception {
		List<Object> lsResult = new ArrayList<Object>();
		String Query = "";
		try {
			Query = "Select a.TABLE_NAME,b.COLUMN_NAME from INFORMATION_SCHEMA.TABLES a,INFORMATION_SCHEMA.COLUMNS b where a.TABLE_NAME = b.TABLE_NAME";

			List<Map<String, Object>> lsData = jdbcTemplate.queryForList(Query);

			lsResult.add("Success");
			lsResult.add(lsData);
		} catch (Exception e) {
			List<Map<String, Object>> lsDat = new ArrayList<>();
			String st = "Invalid object name" + " " + "'" + Query + "'";
			StringBuilder sb = new StringBuilder();
			sb.append("Msg 208, Level 16, State 1, Line 1").append("\n");
			sb.append(st);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Messages", sb.toString());
			lsDat.add(map);
			System.out.println(sb.toString());
			lsResult.add("Warning");
			lsResult.add(lsDat);
		}
		return new ResponseEntity<>(lsResult, HttpStatus.OK);

	}

	
	public ResponseEntity<Object> getTablesFromSchema(int TableTypeCode, int ModuleFormCode, UserInfo userInfo)
			throws Exception {

		List<Object> lsResult = new ArrayList<Object>();

		String Query = "";
		if (TableTypeCode == -1) {
		
			Query = "select a.stablename as stable,c.value->>'columnname' as scolumn," + "a.jsondata->'tablename'->>'"
					+ userInfo.getSlanguagetypecode() + "' as stabledisplayname,"
					+ " c.value->'displayname'->>'"+ userInfo.getSlanguagetypecode() + "' scolumndisplayname,"
					+ "cast( c.value->>'isjsoncolumn' as boolean) isjsoncolumn," + " c.value->>'parametername' parametername,"
					+ "cast(  c.value->>'ismultilingual' as boolean) ismultilingual," + " c.value->>'jsoncolumnname' jsoncolumnname, c.value->>'columndatatype' columndatatype "
					+ " from querybuildertables a,"
					+ " querybuildertablecolumns b,jsonb_array_elements(b.jsqlquerycolumns) c  "
					+ " where  a.nquerybuildertablecode = b.nquerybuildertablecode and  c.value->>'tablename'::text=b.stablename "
					+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by a.nquerybuildertablecode";



		} else if (TableTypeCode == 1) {
			Query = "select a.stablename as stable,c.value->>'columnname' as scolumn," + "a.jsondata->'tablename'->>'"
					+ userInfo.getSlanguagetypecode() + "' as stabledisplayname," + " c.value->'displayname'->>'"+ userInfo.getSlanguagetypecode() + "' scolumndisplayname,"
					+ "cast(  c.value->>'isjsoncolumn' as boolean) isjsoncolumn," + " c.value->>'parametername' parametername,"
					+ "cast(  c.value->>'ismultilingual' as boolean) ismultilingual," + " c.value->>'jsoncolumnname' jsoncolumnname, c.value->>'columndatatype' columndatatype "
					+ " from querybuildertables a,"
					+ " querybuildertablecolumns b,querytabledetails d,jsonb_array_elements(b.jsqlquerycolumns) c  "
					+ " where  a.nquerybuildertablecode = d.nquerybuildertablecode and d.nmodulecode =" + ModuleFormCode
					+ " "
					+ " and a.nquerybuildertablecode = b.nquerybuildertablecode and  c.value->>'tablename'::text=b.stablename "
					+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by a.nquerybuildertablecode";
		} else {
			Query = "select a.stablename as stable,c.value->>'columnname' as scolumn," + "a.jsondata->'tablename'->>'"
					+ userInfo.getSlanguagetypecode() + "' as stabledisplayname," + " c.value->'displayname'->>'"
					+ userInfo.getSlanguagetypecode() + "' scolumndisplayname,"
					+ " cast( c.value->>'isjsoncolumn' as boolean) isjsoncolumn," + " c.value->>'parametername' parametername,"
					+ " cast( c.value->>'ismultilingual' as boolean) ismultilingual," + " c.value->>'jsoncolumnname' jsoncolumnname , c.value->>'columndatatype' columndatatype "
					+ " from querybuildertables a,"
					+ " querybuildertablecolumns b,querytabledetails d,jsonb_array_elements(b.jsqlquerycolumns) c  "
					+ " where  a.nquerybuildertablecode = d.nquerybuildertablecode and d.nformcode =" + ModuleFormCode
					+ " "
					+ " and a.nquerybuildertablecode = b.nquerybuildertablecode and  c.value->>'tablename'::text=b.stablename "
					+ " and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by a.nquerybuildertablecode";
		}
		List<Map<String, Object>> lsData = jdbcTemplate.queryForList(Query);

		lsResult.add(lsData);
		return new ResponseEntity<>(lsResult, HttpStatus.OK);
	}

	
	public ResponseEntity<Object> getColumnsFromTable(String sTableName) throws Exception {
		final String Query = "SELECT COLUMN_NAME as scolumn FROM INFORMATION_SCHEMA.COLUMNS " + "WHERE TABLE_NAME = '"
				+ sTableName + "'";
		List<Map<String, Object>> lsData = jdbcTemplate.queryForList(Query);
		Map<String, Object> tableMap = new HashMap<String, Object>();
		tableMap.put(sTableName, lsData);
		return new ResponseEntity<>(tableMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getValidationForEdit(final int nsqlquerycode, final UserInfo userInfo)
			throws Exception {

		String strQuery = "Select s.nsqlquerycode,s.nquerytypecode,s.ssqlqueryname,s.ssqlquery,s.sscreenrecordquery,s.sscreenheader,s.ncharttypecode,s.svaluemember,s.sdisplaymember,s.nsitecode,s.nstatus,q.squerytypename,coalesce(c.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',c.jsondata->'sdisplayname'->>'en-US') as schartname from sqlquery s,querytype q,charttype c where s.nquerytypecode = q.nquerytypecode and s.ncharttypecode = c.ncharttypecode and "
				+ " s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode="+userInfo.getNmastersitecode()+" and s.nsqlquerycode = "
				+ nsqlquerycode;
		final SQLQuery sqlQuery = (SQLQuery) jdbcUtilityFunction.queryForObject(strQuery, SQLQuery.class,jdbcTemplate);
		if (sqlQuery != null) {
			if (sqlQuery.getNquerytypecode() == Enumeration.QueryType.DASHBOARD.getQuerytype()) {
				strQuery = "select nquerycode from dashboardtype where nquerycode=" + sqlQuery.getNsqlquerycode() + " "
						+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<DashBoardType> lstDashBoard = (List<DashBoardType>) jdbcTemplate.query(strQuery,
						new DashBoardType());
				if (!lstDashBoard.isEmpty()) {
					return new ResponseEntity<>("IDS_QUERYUSEDINDASHBOARD", HttpStatus.OK);
				}
				return new ResponseEntity<>("IDS_SUCCESS", HttpStatus.OK);

			}
			if (sqlQuery.getNquerytypecode() == Enumeration.QueryType.FILTER.getQuerytype()) {

				strQuery = " select 'IDS_SQLFILTERQUERYUSEDINDASHBOARD' as msg from dashboarddesignconfig where "
						+ " nsqlquerycode=" + sqlQuery.getNsqlquerycode() + " " + "and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "union all"
						+ " select 'IDS_SQLFILTERQUERYUSEDINREPORT' as msg from reportdesignconfig where "
						+ " nsqlquerycode=" + sqlQuery.getNsqlquerycode() + " " + "and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				
				validatorDel = projectDAOSupport.getTransactionInfo(strQuery, userInfo);

				if (validatorDel.getNreturnstatus() != Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.OK);
				}
				return new ResponseEntity<>("IDS_SUCCESS", HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>("IDS_SUCCESS", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDatabaseTables(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String sTableQuery = "select nquerybuildertablecode, stablename," + " COALESCE(jsondata->'tablename'->>'"
				+ userInfo.getSlanguagetypecode() + "', jsondata->'tablename'->>'en-US') displayname, "
				+ " nismastertable, nstatus, jsondata from querybuildertables where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		List<QueryBuilderTables> tableList = jdbcTemplate.query(sTableQuery, new QueryBuilderTables());
		outputMap.put("databaseTableList", tableList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDatabaseViews(UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String sViewQuery = "select nquerybuilderviewscode, sviewname," + " COALESCE(jsondata->'displayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', jsondata->'displayname'->>'en-US') sdisplayname, "
				+ " nstatus, jsondata from querybuilderviews where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		List<QueryBuilderViews> viewList = jdbcTemplate.query(sViewQuery, new QueryBuilderViews());
		outputMap.put("databaseviewList", viewList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDatabaseViewsColumns(UserInfo userInfo, String sViewName) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String sViewColumnQuery = "select sviewname, jsondata, nstatus from querybuilderviewscolumns"
				+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sviewname = N'" + sViewName + "';";
		List<QueryBuilderViewsColumns> viewColumnList = jdbcTemplate.query(sViewColumnQuery,
				new QueryBuilderViewsColumns());
		outputMap.put("viewColumnList", viewColumnList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> executeQuery(String sgeneratedQuery, UserInfo userInfo,
			List<Map<String, Object>> columnList) throws Exception {
		System.out.println("Query ---------> " + sgeneratedQuery);

		final List<String> getDateColumns = new ArrayList<>();
		if (columnList.size() > 0) {

			for (int i = 0; i < columnList.size(); i++) {
				if (columnList.get(i) != null) {
					Map<String, Object> obj = null;

					if (columnList.get(i).containsKey("items")) {
						obj = (Map<String, Object>) columnList.get(i).get("items");
					}
					if (obj != null) {
						if ((int) obj.get("columntype") == 2 || (int) obj.get("columntype") == 3) {

							getDateColumns.add((String) obj.get("columnname"));
						}
					}
				}

			}
		}
		List<Map<String, Object>> outputList = jdbcTemplate.queryForList(sgeneratedQuery);

		List<Object> outPutData = outputList.stream().collect(Collectors.toList());

		List<?> outPut = new ArrayList<>();

		if (getDateColumns.size() > 0 && outputList.size() > 0) {

			try {
				outPut = dateUtilityFunction.getSiteLocalTimeFromUTC(outPutData, getDateColumns, null, userInfo, false, null,
						false);
			} catch (Exception e) {
//				e.printStackTrace();
			}

			return new ResponseEntity<>(outPut, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(outPutData, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getForeignTable(final String sTableName) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String sForeignColQuery = "select stablename, sprimarykeyname, jstaticcolumns, jdynamiccolumns, jmultilingualcolumn, jnumericcolumns, nstatus "
				+ "from querybuildertablecolumns where stablename = N'" + sTableName + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		List<QueryBuilderTableColumns> foreignTableColumnList = jdbcTemplate.query(sForeignColQuery,
				new QueryBuilderTableColumns());
		outputMap.put("foreignTableList", foreignTableColumnList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getMasterData(Map<String, Object> dataMap, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final int nFlag = (int) dataMap.get("nflag");
		boolean isJsonDisplayName = false;
		if (dataMap.containsKey("isjsondisplayname")) {
			isJsonDisplayName = (boolean) dataMap.get("isjsondisplayname");
		}

		switch (nFlag) {
		case 1:
			final String sForeignTableName = (String) dataMap.get("sforeigntablename");
			final String sForeignColumnName = (String) dataMap.get("sforeigncolumnname");
			final List<String> data = (List<String>) dataMap.get("data");

			String sForeignDisplayName = (String) dataMap.get("sforeigndisplayname");
			sForeignDisplayName = sForeignDisplayName.replace("[", "'");
			sForeignDisplayName = sForeignDisplayName.replace("]", "'");
			if (isJsonDisplayName) {
				sForeignDisplayName += "'" + userInfo.getSlanguagetypecode() + "'";
			}
if( data!=null) {
	final String sMasterQueryByData = "select " + sForeignDisplayName + " sdisplayname, " + sForeignColumnName
			+ " from " + sForeignTableName + " where " + sForeignColumnName + " in (" + String.join(",", data)
			+ ")" + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
	List<Map<String, Object>> list = jdbcTemplate.queryForList(sMasterQueryByData);
	outputMap.put("masterdata", list);
}
else {
	final String sMasterQueryByData = "select " + sForeignDisplayName + " sdisplayname, " + sForeignColumnName
			+ " from " + sForeignTableName +" where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
	List<Map<String, Object>> list = jdbcTemplate.queryForList(sMasterQueryByData);
	outputMap.put("masterdata", list);
}
			break;

		case 2:
			final String sValueMember = "\"" + (String) dataMap.get("valuemember") + "\"";			
			String sDisplayMember = "\"" + (String) dataMap.get("displaymember") + "\"";
			final String sMasterTable = (String) dataMap.get("mastertablename");
			
			if (isJsonDisplayName) {
				sDisplayMember += "'" + userInfo.getSlanguagetypecode() + "'";
			}

			final String sMasterQueryByTable = "select " + sDisplayMember + " sdisplayname, " + sValueMember + " from "
					+ sMasterTable + " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
			outputMap.put("masterdata", jdbcTemplate.queryForList(sMasterQueryByTable));
			break;

		default:
			break;
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getQueryBuilder( UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String sQuery = "select * from querybuilder" + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  
				+ " order by nquerybuildercode desc";

		List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);

		if (list.size() > 0) {
			outputMap.put("queryBuilderList", list);
			outputMap.putAll(
					(Map<String, Object>) getSelectedQueryBuilder((int) list.get(0).get("nquerybuildercode"), userInfo)
							.getBody());

		} else {
			outputMap.put("queryBuilderList", null);
			outputMap.put("queryDataMain", null);
			outputMap.put("columnList", null);
		}
		//outputMap.put("queryTypeCode", nqueryTypeCode);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createQueryBuilder(QueryBuilder queryBuilder, UserInfo userInfo,
			String sgeneratedQuery) throws Exception {
		String insertQueryString = "";
//		queryBuilder = (QueryBuilder) insertObject(queryBuilder, SeqNoDashBoardManagement.class, "nsequenceno");
		JSONObject jsonObjectDefaultValue = new JSONObject(queryBuilder.getSdefaultvalue());
		JSONObject jsonObject = new JSONObject(queryBuilder.getJsondata());
		List<QueryBuilder> querybuilderListByName=getQueryBuilderByName(queryBuilder.getSquerybuildername() );
		if(querybuilderListByName.isEmpty()) {
		insertQueryString = " insert into querybuilder (nquerybuildercode, nquerytype, sviewname, squerywithparam,squerywithvalue, nstatus, squerybuildername,  sdefaultvalue, jsondata) values ( (select COALESCE (max( nquerybuildercode),0) + 1 from querybuilder) , "
				+ queryBuilder.getNquerytype() + "," + "'" + stringUtilityFunction.replaceQuote(queryBuilder.getSviewname()) + "','"
				+ queryBuilder.getSquerywithparam() + "','',1,'" + stringUtilityFunction.replaceQuote(queryBuilder.getSquerybuildername()) + "', '"
				+ jsonObjectDefaultValue + "', '" + jsonObject + "') ";

		jdbcTemplate.execute(insertQueryString);

		return new ResponseEntity<>(getQueryBuilder(userInfo).getBody(), HttpStatus.OK);
	}
	else {
		return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);	}
		}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSelectedQueryBuilder(int nQueryBuilderCode, UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();
		final String sQuery = "select * from querybuilder" + " where nquerybuildercode = " + nQueryBuilderCode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nquerybuildercode desc";

		List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);

		Map<String, Object> jsonData = new JSONObject(((PGobject) list.get(0).get("jsondata")).getValue()).toMap();
		Map<String, Object> sDefaultValue = new JSONObject(((PGobject) list.get(0).get("sdefaultvalue")).getValue())
				.toMap();

		List<Map<String, Object>> sDefaultValuelist = (List<Map<String, Object>>) sDefaultValue.get("sdefaultvalue");
		List<Map<String, Object>> columnList = (List<Map<String, Object>>) jsonData.get("columnList");
		List<Map<String, Object>> selectFields = (List<Map<String, Object>>) jsonData.get("selectFields");

	//if (sDefaultValuelist.size() > 0) {

			String sQueryWithParam = (String) list.get(0).get("squerywithparam");

			int ncount = 0;
			for (int i = 0; i < sDefaultValuelist.size(); i++) {
				Map<String, Object> getItems = (Map<String, Object>) sDefaultValuelist.get(i).get("items");
				Map<String, Object> getSymbol = (Map<String, Object>) sDefaultValuelist.get(i).get("symbolObject");
				Map<String, Object> getSymbolItems = (Map<String, Object>) getSymbol.get("items");

				if ((Boolean) getItems.get("needmasterdata") == true && getItems.get("mastertablename") != "") {
					if (getSymbolItems.containsKey("ismulti") && (Boolean) getSymbolItems.get("ismulti") == true) {
						List<Map<String, Object>> getValues = (List<Map<String, Object>>) sDefaultValuelist.get(i)
								.get("value");
						//String sData = "";
						for (int j = 0; j < getValues.size(); j++) {
//							sData += getValues.get(j).get("value") + ",";
							ncount++;
//							if (getValues.get(j).get("value").getClass().getName() == "java.lang.String") {
								sQueryWithParam = sQueryWithParam.replace("$p" + ncount,
										"'" +  getValues.get(j).get("value") + "'");
//							}else {
//								sQueryWithParam = sQueryWithParam.replace("$p" + ncount,
//										"" +  getValues.get(j).get("value") + "");
//							}
							
						}
						
					} else {
						ncount++;
						Map<String, Object> getValues = (Map<String, Object>) sDefaultValuelist.get(i).get("value");
						sQueryWithParam = sQueryWithParam.replace("$p" + ncount, "'" + getValues.get("value") + "'");
					}

				} else if (getItems.containsKey("sforeigncolumnname") == true
						&& (String) getItems.get("sforeigncolumnname") != "") {

					if (getSymbolItems.containsKey("ismulti") && (Boolean) getSymbolItems.get("ismulti") == true) {
						List<Map<String, Object>> getValues = (List<Map<String, Object>>) sDefaultValuelist.get(i)
								.get("value");
						//String sData = "";
						for (int j = 0; j < getValues.size(); j++) {
							ncount++;
//							sData += getValues.get(j).get("value") + ",";
							
							sQueryWithParam = sQueryWithParam.replace("$p" + ncount,
									"'" + getValues.get(j).get("value") + "'");
						}
						
					} else {
						ncount++;
						Map<String, Object> getValues = (Map<String, Object>) sDefaultValuelist.get(i).get("value");
						sQueryWithParam = sQueryWithParam.replace("$p" + ncount, "'" + getValues.get("value") + "'");
					}

				} else {
					ncount++;
					sQueryWithParam = sQueryWithParam.replace("$p" + ncount,
							"'" + sDefaultValuelist.get(i).get("value") + "'");
				}

			}
			if (sQueryWithParam.contains("$L")) {
				sQueryWithParam = sQueryWithParam.replace("$L", "'" + userInfo.getSlanguagetypecode() + "'");
			}
			outputMap.put("queryDataMain", executeQuery(sQueryWithParam, userInfo, columnList).getBody());
			outputMap.put("columnList", columnList);
			outputMap.put("selectFields", selectFields);
		//}

		if (list.size() > 0) {
			outputMap.put("selectedQueryBuilder", list.get(0));
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateQueryBuilder(QueryBuilder queryBuilder, UserInfo userInfo) throws Exception {
		String insertQueryString = "";
		JSONObject jsonObjectDefaultValue = new JSONObject(queryBuilder.getSdefaultvalue());

		insertQueryString = " update querybuilder set sdefaultvalue = '" + jsonObjectDefaultValue
				+ "' where nquerybuildercode = " + queryBuilder.getNquerybuildercode() + "";

		jdbcTemplate.execute(insertQueryString);

		return new ResponseEntity<>(getSelectedQueryBuilder(queryBuilder.getNquerybuildercode(), userInfo).getBody(),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> deleteQueryBuilder(QueryBuilder queryBuilder, UserInfo userInfo) throws Exception {
		String insertQueryString = "";
		insertQueryString = " update querybuilder set nstatus = -1 where nquerybuildercode = "
				+ queryBuilder.getNquerybuildercode() + "";

		jdbcTemplate.execute(insertQueryString);

		return new ResponseEntity<>(getQueryBuilder(userInfo).getBody(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTestGroupTestParameter(int ntestgrouptestcode, UserInfo userInfo)
			throws Exception {

		final List<TestGroupTestParameter> listTestParameter = objTestGroupCommonFunction.getTestGroupTestParameter(ntestgrouptestcode, 0, userInfo);

		return new ResponseEntity<>(listTestParameter, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTestMaster(UserInfo userInfo) throws Exception {

		final String query = "select * from testmaster where nstatus=1 and nsitecode="+userInfo.getNmastersitecode();
		List<TestMaster> lstTestMaster = jdbcTemplate.query(query, new TestMaster());

		return new ResponseEntity<>(lstTestMaster, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTestParameter(int ntestcode, UserInfo userInfo) throws Exception {

		String sQuery = "select tp.ntestparametercode,tp.ntestcode,tp.nparametertypecode,tp.nunitcode,t.stestname,pt.sparametertypename,"
				+ "pt.sdisplaystatus,u.sunitname,tp.sparametername,tp.sparametersynonym,tp.nroundingdigits from testparameter tp,testmaster t,"
				+ "unit u,parametertype pt where tp.ntestcode = t.ntestcode and tp.nunitcode = u.nunitcode "
				+ "and tp.nparametertypecode=pt.nparametertypecode and t.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " "
				+ " and tp.nsitecode="+userInfo.getNmastersitecode()+"and t.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and u.nsitecode="+userInfo.getNmastersitecode()+" and pt.nsitecode="+userInfo.getNmastersitecode()+" and tp.ntestcode=" + ntestcode
				+ " and nisvisible=" + Enumeration.TransactionStatus.YES.gettransactionstatus();

		sQuery += " order by tp.ntestparametercode;";
		List<TestParameter> listTestParameter = (List<TestParameter>) jdbcTemplate.query(sQuery, new TestParameter());

		return new ResponseEntity<>(listTestParameter, HttpStatus.OK);
	}
	
	
	public List<QueryBuilder> getQueryBuilderByName(String querybuildernam){
		final String strQuery = "select nquerybuildercode from querybuilder where squerybuildername = N'"
				+ stringUtilityFunction.replaceQuote(querybuildernam) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				
		return (List<QueryBuilder>) jdbcTemplate.query(strQuery, new QueryBuilder());
}
	

}
