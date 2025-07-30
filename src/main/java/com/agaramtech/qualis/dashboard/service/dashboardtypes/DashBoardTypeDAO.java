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

public interface DashBoardTypeDAO {

	public ResponseEntity<Object> getDashBoardTypes(UserInfo userInfo, int nDashBoardTypeCode) throws Exception;

	public DashBoardType getDashBoardTypeByID(final int ndashBoardTypeCode, final UserInfo userInfo) throws Exception;

	public List<ChartType> getChartTypes(final UserInfo userInfo) throws Exception;

	public List<SQLQuery> getSqlQueriesByChart(final int nChartTypeCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createDashBoardTypes(DashBoardType dashBoardType,
			List<ChartPropTransaction> chartPropTransaction, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateDashBoardTypes(DashBoardType dashBoardType,
			List<ChartPropTransaction> chartPropTransaction, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteDashBoardTypes(DashBoardType dashBoardType, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getColumnsBasedOnQuery(final int nSqlQueryCode, UserInfo userInfo) throws Exception;

	public List<ChartProperty> getChartProperty(final int nChartTypeCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createChartPropTransaction(List<ChartPropTransaction> chartPropTransaction,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateChartPropTransaction(ChartPropTransaction chartPropTransaction,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteChartPropTransaction(ChartPropTransaction chartPropTransaction,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getChartPropTransaction(final UserInfo userInfo, final int ndashBoardTypeCode,
			final int nChartTypeCode, final String sDashBoardProperty, final int nSqlQueryCode) throws Exception;

	public ResponseEntity<Object> getDashBoardDesign(final UserInfo userInfo, final int ndashBoardTypeCode,
			final int nSqlQueryCode) throws Exception;

	public ResponseEntity<Object> createDashBoardDesignConfig(List<DashBoardDesignConfig> dashBoardDesignConfig,
			final int ndashBoardTypeCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateDashBoardDesignConfig(List<DashBoardDesignConfig> dashBoardDesignConfig,
			final int ndashBoardTypeCode, UserInfo userInfo) throws Exception;

	public List<DashBoardDesignConfig> getSelectedDashBoardDesign(final UserInfo userInfo, final int ndashBoardTypeCode)
			throws Exception;

	public ResponseEntity<Object> getDashBoardView(UserInfo userInfo, int nDashBoardTypeCode) throws Exception;

	public ResponseEntity<Object> getAllSelectionDashBoardView(UserInfo userInfo, int nDashBoardTypeCode)
			throws Exception;

	public ResponseEntity<Object> getDashboardViewChartParameters(Map<String, Object> obj) throws Exception;

	public ResponseEntity<Object> getDashBoardParameterMappingComboData(final int nDashBoardTypeCode, UserInfo userInfo)
			throws Exception;

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

	public ResponseEntity<Object> checkParameteAvailableInDashBoardView(UserInfo userInfo, int nDashBoardTypeCode)
			throws Exception;

}
