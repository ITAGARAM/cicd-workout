package com.agaramtech.qualis.dashboard.service.sqlquery;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.dashboard.model.QueryBuilder;
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.global.UserInfo;

public interface SQLQueryDAO {

	public ResponseEntity<Object> getSQLQuery(Integer nsqlquerycode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSQLQueryByQueryTypeCode(short nquerytypecode, final UserInfo userInfo)
			throws Exception;

	public SQLQuery getActiveSQLQueryById(final int nsqlquerycode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createSQLQuery(SQLQuery sqlQuery, UserInfo userInfo, Integer nquerytypecode)
			throws Exception;

	public ResponseEntity<Object> updateSQLQuery(SQLQuery sqlQuery, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSQLQuery(SQLQuery sqlQuery, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getChartType(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getQueryTableType() throws Exception;

	public ResponseEntity<Object> getModuleFormName(final int TableTypeCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSchemaQueryOutput(final String Query, final UserInfo userInfo, String ReturnOption)
			throws Exception;

	public ResponseEntity<Object> getColumnValues(final String TableName, final String FieldName,
			final String DisplayParam) throws Exception;

	public ResponseEntity<Object> getTableColumnNames() throws Exception;

	public ResponseEntity<Object> getTablesFromSchema(int TableTypeCode, int ModuleFormCode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getColumnsFromTable(final String TableName) throws Exception;

	public ResponseEntity<Object> getValidationForEdit(int nsqlquerycode, UserInfo userInfo) throws Exception;

	// public QueryType getActiveQueryTypeById(final int nquerytypecode) throws
	// Exception ;

	// public QueryType getActiveQueryTypeCodeById(final int nquerytypecode) throws
	// Exception ;

	// public ResponseEntity<Object> getQueryType() throws Exception;

	public ResponseEntity<Object> getDatabaseTables(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDatabaseViews(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDatabaseViewsColumns(final UserInfo userInfo, final String sViewName)
			throws Exception;

	public ResponseEntity<Object> executeQuery(final String sgeneratedQuery, final UserInfo userInfo,
			final List<Map<String, Object>> columnList) throws Exception;

	public ResponseEntity<Object> getForeignTable(final String sTableName) throws Exception;

	public ResponseEntity<Object> getMasterData(final Map<String, Object> dataMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getQueryBuilder(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSelectedQueryBuilder(final int nQueryBilderCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createQueryBuilder(QueryBuilder queryBuilder, UserInfo userInfo,
			String sgeneratedQuery) throws Exception;

	public ResponseEntity<Object> updateQueryBuilder(QueryBuilder queryBuilder, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteQueryBuilder(QueryBuilder queryBuilder, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTestGroupTestParameter(int ntestgrouptestcode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTestMaster(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTestParameter(int ntestcode, UserInfo userInfo) throws Exception;
}
