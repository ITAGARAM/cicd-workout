package com.agaramtech.qualis.dashboard.service.dashboardtypes;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.dashboard.model.ChartPropTransaction;
import com.agaramtech.qualis.dashboard.model.ChartProperty;
import com.agaramtech.qualis.dashboard.model.ChartType;
import com.agaramtech.qualis.dashboard.model.DashBoardDesignConfig;
import com.agaramtech.qualis.dashboard.model.DashBoardHomePriority;
import com.agaramtech.qualis.dashboard.model.DashBoardHomeTypes;
import com.agaramtech.qualis.dashboard.model.DashBoardParameterMapping;
import com.agaramtech.qualis.dashboard.model.DashBoardType; 
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.global.UserInfo;

public interface DashBoardTypeService {

	public ResponseEntity<Object> getDashBoardTypes(final UserInfo userInfo, final int nDashBoardTypeCode)
			throws Exception;

	public ResponseEntity<Object> getDashBoardTypeByID(final int ndashBoardTypeCode, final UserInfo userInfo)
			throws Exception;

	public List<ChartType> getChartTypes(final UserInfo userInfo) throws Exception;

	public List<SQLQuery> getSqlQueriesByChart(final int nChartTypeCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createDashBoardTypes(final DashBoardType dashBoardType,
			final List<ChartPropTransaction> chartPropTransaction, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateDashBoardTypes(final DashBoardType dashBoardType,
			final List<ChartPropTransaction> chartPropTransaction, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteDashBoardTypes(final DashBoardType dashBoardType, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getColumnsBasedOnQuery(final int nSqlQueryCode, final UserInfo userInfo)
			throws Exception;

	public List<ChartProperty> getChartProperty(final int nChartTypeCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createChartPropTransaction(final List<ChartPropTransaction> chartPropTransaction,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateChartPropTransaction(final ChartPropTransaction chartPropTransaction,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteChartPropTransaction(final ChartPropTransaction chartPropTransaction,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getChartPropTransaction(final UserInfo userInfo, final int ndashBoardTypeCode,
			final int nChartTypeCode, final String sDashBoardProperty, final int nSqlQueryCode) throws Exception;

	public ResponseEntity<Object> getDashBoardDesign(final UserInfo userInfo, final int ndashBoardTypeCode,
			final int nSqlQueryCode) throws Exception;

	public ResponseEntity<Object> createDashBoardDesignConfig(final List<DashBoardDesignConfig> dashBoardDesignConfig,
			final int ndashBoardTypeCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateDashBoardDesignConfig(final List<DashBoardDesignConfig> dashBoardDesignConfig,
			final int ndashBoardTypeCode, final UserInfo userInfo) throws Exception;

	public List<DashBoardDesignConfig> getSelectedDashBoardDesign(final UserInfo userInfo, final int ndashBoardTypeCode)
			throws Exception;

	public ResponseEntity<Object> getDashBoardView(final UserInfo userInfo, final int nDashBoardTypeCode)
			throws Exception;

	public ResponseEntity<Object> getAllSelectionDashBoardView(final UserInfo userInfo, final int nDashBoardTypeCode)
			throws Exception;

	public ResponseEntity<Object> getDashboardViewChartParameters(Map<String, Object> obj) throws Exception;

	public ResponseEntity<Object> getDashBoardParameterMappingComboData(final int nDashBoardTypeCode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createDashBoardParameterMapping(final List<DashBoardParameterMapping> mappingList,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getChildDataList(final DashBoardDesignConfig parentConfig,
			final Map<String, Object> inputFieldData, final String parentValue, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getHomeDashBoard(final UserInfo userInfo, final int npageCode,
			final Boolean bpageAction) throws Exception;

	public ResponseEntity<Object> getDashBoardHomeConfig(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDashBoardHomePagesandTemplates(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDashBoardHomeConfigByID(final int nprioritycode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createDashBoardHomePriority(final DashBoardHomePriority mappingList,
			final List<DashBoardHomeTypes> dashBoardHomeTypes, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateDashBoardHomePriority(final DashBoardHomePriority mappingList,
			final List<DashBoardHomeTypes> dashBoardHomeTypes, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteDashBoardHomePriority(final DashBoardHomePriority mappingList,
			final List<DashBoardHomeTypes> dashBoardHomeTypes, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> checkParameteAvailableInDashBoardView(final UserInfo userInfo,
			final int nDashBoardTypeCode) throws Exception;
}
