package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.dashboard.service.sqlquery.SQLQueryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/sqlquery")
public class SQLQueryController {

	private final RequestContext requestContext;
	private SQLQueryService sqlQueryService;

	public SQLQueryController(SQLQueryService sqlQueryService, RequestContext requestContext) {
		super();
		this.sqlQueryService = sqlQueryService;
		this.requestContext = requestContext;
	}

	@PostMapping(value = "/getSQLQuery")
	public ResponseEntity<Object> getSQLQuery(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer nsqlQueryCode = null;
		if (inputMap.get("nsqlquerycode") != null) {
			nsqlQueryCode = (Integer) inputMap.get("nsqlquerycode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getSQLQuery(nsqlQueryCode, userInfo);

	}

	@PostMapping(value = "/getSQLQueryByQueryTypeCode")
	public ResponseEntity<Object> getSQLQueryByQueryTypeCode(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Short nquerytype = null;
		if (inputMap.get("nquerytypecode") != null) {
			nquerytype = (short) ((int) inputMap.get("nquerytypecode"));
			;
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getSQLQueryByQueryTypeCode(nquerytype, userInfo);

	}

	@PostMapping(value = "/createSQLQuery")
	public ResponseEntity<Object> createSQLQuery(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		Integer nquerytypecode = null;
		objmapper.registerModule(new JavaTimeModule());
		final SQLQuery sqlQuery = objmapper.convertValue(inputMap.get("sqlquery"), SQLQuery.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		nquerytypecode = (Integer) inputMap.get("nquerytypecode");

		requestContext.setUserInfo(userInfo);
		return sqlQueryService.createSQLQuery(sqlQuery, userInfo, nquerytypecode);
	}

	@PostMapping(value = "/getActiveSQLQueryById")
	public ResponseEntity<Object> getActiveSQLQueryById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nsqlquerycode = (Integer) inputMap.get("nsqlquerycode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getActiveSQLQueryById(nsqlquerycode, userInfo);

	}

	@PostMapping(value = "/updateSQLQuery")
	public ResponseEntity<Object> updateSQLQuery(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final SQLQuery sqlQuery = objmapper.convertValue(inputMap.get("sqlquery"), SQLQuery.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.updateSQLQuery(sqlQuery, userInfo);

	}

	@PostMapping(value = "/deleteSQLQuery")
	public ResponseEntity<Object> deleteSQLQuery(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final SQLQuery sqlQuery = objmapper.convertValue(inputMap.get("sqlquery"), SQLQuery.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.deleteSQLQuery(sqlQuery, userInfo);

	}

	@PostMapping(value = "/getChartType")
	public ResponseEntity<Object> getChartType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getChartType(userInfo);

	}

	@PostMapping(value = "/getQueryTableType")
	public ResponseEntity<Object> getQueryTableType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getQueryTableType();

	}

	@PostMapping(value = "/getModuleFormName")
	public ResponseEntity<Object> getModuleFormName(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int nTableTypeCode = (int) inputMap.get("tabletypecode");
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getModuleFormName(nTableTypeCode, userInfo);

	}

	@PostMapping(value = "/getSchemaQueryOutput")
	public ResponseEntity<Object> getSchemaQueryOutput(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String sQuery = (String) inputMap.get("query");
		final String sReturnOption = (String) inputMap.get("returnoption");
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getSchemaQueryOutput(sQuery, userInfo, sReturnOption);

	}

	@PostMapping(value = "/getColumnValues")
	public ResponseEntity<Object> getColumnValues(@RequestBody Map<String, Object> inputMap) throws Exception {
		final String sTableName = (String) inputMap.get("tablename");
		final String sFieldName = (String) inputMap.get("fieldname");
		final String sDisplayParam = (String) inputMap.get("displayparam");

		return sqlQueryService.getColumnValues(sTableName, sFieldName, sDisplayParam);

	}

	@PostMapping(value = "/getTableColumnNames")
	public ResponseEntity<Object> getTableColumnNames(@RequestBody Map<String, Object> inputMap) throws Exception {

		return sqlQueryService.getTableColumnNames();
	}

	@PostMapping(value = "/getTablesFromSchema")
	public ResponseEntity<Object> getTablesFromSchema(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int nTableTypeCode = (int) inputMap.get("tabletypecode");
		final int nmoduleformcode = (int) inputMap.get("moduleformcode");
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getTablesFromSchema(nTableTypeCode, nmoduleformcode, userInfo);

	}

	@PostMapping(value = "/getColumnsFromTable")
	public ResponseEntity<Object> getColumnsFromTable(@RequestBody Map<String, Object> inputMap) throws Exception {

		final String sTableName = (String) inputMap.get("tablename");
		return sqlQueryService.getColumnsFromTable(sTableName);

	}

	@PostMapping(value = "/getValidationForEdit")
	public ResponseEntity<Object> getValidationForEdit(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nsqlquerycode = (Integer) inputMap.get("nsqlquerycode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getValidationForEdit(nsqlquerycode, userInfo);

	}

	@PostMapping(value = "/getdatabasetables")
	public ResponseEntity<Object> getDatabaseTables(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getDatabaseTables(userInfo);

	}

	@PostMapping(value = "/getdatabaseviews")
	public ResponseEntity<Object> getDatabaseViews(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getDatabaseViews(userInfo);

	}

	@PostMapping(value = "/getdatabaseviewscolumns")
	public ResponseEntity<Object> getDatabaseViewsColumns(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String sViewName = (String) inputMap.get("sviewname");
		return sqlQueryService.getDatabaseViewsColumns(userInfo, sViewName);

	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/executequery")
	public ResponseEntity<Object> executeQuery(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userInfo"), UserInfo.class);
		final String sgeneratedQuery = (String) inputMap.get("sgeneratedquery");
		final List<Map<String, Object>> columnList = (List<Map<String, Object>>) inputMap.get("columnList");
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.executeQuery(sgeneratedQuery, userInfo, columnList);

	}

	@PostMapping(value = "/getforeigntable")
	public ResponseEntity<Object> getForeignTable(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String sTableName = (String) inputMap.get("stablename");
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getForeignTable(sTableName);

	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/getmasterdata")
	public ResponseEntity<Object> getMasterData(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Map<String, Object> dataMap = (Map<String, Object>) inputMap.get("data");
		requestContext.setUserInfo(userInfo);
		return sqlQueryService.getMasterData(dataMap, userInfo);

	}

}
