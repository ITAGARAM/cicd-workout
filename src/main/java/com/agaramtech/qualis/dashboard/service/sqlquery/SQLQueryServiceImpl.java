package com.agaramtech.qualis.dashboard.service.sqlquery;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.dashboard.model.QueryBuilder;
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SQLQueryServiceImpl implements SQLQueryService {

	private final SQLQueryDAO sqlQueryDAO;
	private final CommonFunction commonFunction;

	public SQLQueryServiceImpl(SQLQueryDAO sqlQueryDAO, CommonFunction commonFunction) {
		this.commonFunction = commonFunction;
		this.sqlQueryDAO = sqlQueryDAO;
	}

	@Override
	public ResponseEntity<Object> getSQLQuery(Integer nsqlquerycode, final UserInfo userInfo) throws Exception {
		return sqlQueryDAO.getSQLQuery(nsqlquerycode, userInfo);// siteCode);
	}

	@Override
	public ResponseEntity<Object> getSQLQueryByQueryTypeCode(short nquerytypecode, final UserInfo userInfo)
			throws Exception {
		return sqlQueryDAO.getSQLQueryByQueryTypeCode(nquerytypecode, userInfo);// siteCode);
	}

	@Override
	public ResponseEntity<Object> getActiveSQLQueryById(final int nsqlquerycode, final UserInfo userInfo)
			throws Exception {
		final SQLQuery sqlQuery = (SQLQuery) sqlQueryDAO.getActiveSQLQueryById(nsqlquerycode, userInfo);
		if (sqlQuery == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(sqlQuery, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createSQLQuery(SQLQuery sqlQuery, UserInfo userInfo, Integer nquerytypecode)
			throws Exception {

		return sqlQueryDAO.createSQLQuery(sqlQuery, userInfo, nquerytypecode);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateSQLQuery(SQLQuery sqlQuery, UserInfo userInfo) throws Exception {

		return sqlQueryDAO.updateSQLQuery(sqlQuery, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteSQLQuery(SQLQuery sqlQuery, UserInfo userInfo) throws Exception {

		return sqlQueryDAO.deleteSQLQuery(sqlQuery, userInfo);
	}

	@Override
	public ResponseEntity<Object> getChartType(UserInfo userInfo) throws Exception {
		return sqlQueryDAO.getChartType(userInfo);
	}

	@Override
	public ResponseEntity<Object> getQueryTableType() throws Exception {
		return sqlQueryDAO.getQueryTableType();
	}

	@Override
	public ResponseEntity<Object> getModuleFormName(final int TableTypeCode, UserInfo userInfo) throws Exception {
		return sqlQueryDAO.getModuleFormName(TableTypeCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSchemaQueryOutput(final String Query, final UserInfo userInfo, String ReturnOption)
			throws Exception {
		return sqlQueryDAO.getSchemaQueryOutput(Query, userInfo, ReturnOption);
	}

	@Override
	public ResponseEntity<Object> getColumnValues(final String TableName, final String FieldName,
			final String DisplayParam) throws Exception {
		return sqlQueryDAO.getColumnValues(TableName, FieldName, DisplayParam);
	}

	@Override
	public ResponseEntity<Object> getTableColumnNames() throws Exception {
		return sqlQueryDAO.getTableColumnNames();
	}

	@Override
	public ResponseEntity<Object> getTablesFromSchema(int TableTypeCode, int ModuleFormCode, UserInfo userInfo)
			throws Exception {
		return sqlQueryDAO.getTablesFromSchema(TableTypeCode, ModuleFormCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getColumnsFromTable(final String TableName) throws Exception {
		return sqlQueryDAO.getColumnsFromTable(TableName);
	}

	@Override
	public ResponseEntity<Object> getValidationForEdit(int nsqlquerycode, UserInfo userInfo) throws Exception {
		return sqlQueryDAO.getValidationForEdit(nsqlquerycode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getDatabaseTables(final UserInfo userInfo) throws Exception {
		return sqlQueryDAO.getDatabaseTables(userInfo);
	}

	@Override
	public ResponseEntity<Object> getDatabaseViews(UserInfo userInfo) throws Exception {
		return sqlQueryDAO.getDatabaseViews(userInfo);
	}

	@Override
	public ResponseEntity<Object> getDatabaseViewsColumns(UserInfo userInfo, String sViewName) throws Exception {
		return sqlQueryDAO.getDatabaseViewsColumns(userInfo, sViewName);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> executeQuery(final String sgeneratedQuery, final UserInfo userInfo,
			final List<Map<String, Object>> columnList) throws Exception {
		return sqlQueryDAO.executeQuery(sgeneratedQuery, userInfo, columnList);
	}

	@Override
	public ResponseEntity<Object> getForeignTable(final String sTableName) throws Exception {
		return sqlQueryDAO.getForeignTable(sTableName);
	}

	@Override
	public ResponseEntity<Object> getMasterData(final Map<String, Object> dataMap, final UserInfo userInfo)
			throws Exception {
		return sqlQueryDAO.getMasterData(dataMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getQueryBuilder(UserInfo userInfo) throws Exception {

		return sqlQueryDAO.getQueryBuilder(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createQueryBuilder(QueryBuilder queryBuilder, UserInfo userInfo,
			String sgeneratedQuery) throws Exception {

		return sqlQueryDAO.createQueryBuilder(queryBuilder, userInfo, sgeneratedQuery);
	}

	@Override
	public ResponseEntity<Object> getSelectedQueryBuilder(int nQueryBilderCode, UserInfo userInfo) throws Exception {

		return sqlQueryDAO.getSelectedQueryBuilder(nQueryBilderCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateQueryBuilder(QueryBuilder queryBuilder, UserInfo userInfo) throws Exception {

		return sqlQueryDAO.updateQueryBuilder(queryBuilder, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteQueryBuilder(QueryBuilder queryBuilder, UserInfo userInfo) throws Exception {

		return sqlQueryDAO.deleteQueryBuilder(queryBuilder, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestGroupTestParameter(int ntestgrouptestcode, UserInfo userInfo)
			throws Exception {

		return sqlQueryDAO.getTestGroupTestParameter(ntestgrouptestcode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestMaster(UserInfo userInfo) throws Exception {

		return sqlQueryDAO.getTestMaster(userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestParameter(int ntestcode, UserInfo userInfo) throws Exception {

		return sqlQueryDAO.getTestParameter(ntestcode, userInfo);
	}

}
