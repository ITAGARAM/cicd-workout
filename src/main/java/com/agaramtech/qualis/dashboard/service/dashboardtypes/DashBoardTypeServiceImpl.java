package com.agaramtech.qualis.dashboard.service.dashboardtypes;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.dashboard.model.ChartPropTransaction;
import com.agaramtech.qualis.dashboard.model.ChartProperty;
import com.agaramtech.qualis.dashboard.model.ChartType;
import com.agaramtech.qualis.dashboard.model.DashBoardDesignConfig;
import com.agaramtech.qualis.dashboard.model.DashBoardHomePriority;
import com.agaramtech.qualis.dashboard.model.DashBoardHomeTypes;
import com.agaramtech.qualis.dashboard.model.DashBoardParameterMapping;
import com.agaramtech.qualis.dashboard.model.DashBoardType;
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class DashBoardTypeServiceImpl implements DashBoardTypeService {

	private final DashBoardTypeDAO dashBoardTypeDAO;
	private final CommonFunction commonFunction;

	public DashBoardTypeServiceImpl(DashBoardTypeDAO dashBoardTypeDAO, CommonFunction commonFunction) {
		super();
		this.dashBoardTypeDAO = dashBoardTypeDAO;
		this.commonFunction = commonFunction;
	}

	@Override
	public ResponseEntity<Object> getDashBoardTypes(UserInfo userInfo, int nDashBoardTypeCode) throws Exception {
		return dashBoardTypeDAO.getDashBoardTypes(userInfo, nDashBoardTypeCode);
	}

	@Override
	public ResponseEntity<Object> getDashBoardTypeByID(int ndashBoardTypeCode, final UserInfo userInfo)
			throws Exception {
		final DashBoardType dashBoardType = (DashBoardType) dashBoardTypeDAO.getDashBoardTypeByID(ndashBoardTypeCode,
				userInfo);
		if (dashBoardType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(dashBoardType, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createDashBoardTypes(DashBoardType dashBoardType,
			List<ChartPropTransaction> chartPropTransaction, UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.createDashBoardTypes(dashBoardType, chartPropTransaction, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateDashBoardTypes(DashBoardType dashBoardType,
			List<ChartPropTransaction> chartPropTransaction, UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.updateDashBoardTypes(dashBoardType, chartPropTransaction, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteDashBoardTypes(DashBoardType dashBoardType, UserInfo userInfo)
			throws Exception {
		return dashBoardTypeDAO.deleteDashBoardTypes(dashBoardType, userInfo);
	}

	@Override
	public List<ChartType> getChartTypes(UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.getChartTypes(userInfo);
	}

	@Override
	public List<SQLQuery> getSqlQueriesByChart(int nChartTypeCode, UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.getSqlQueriesByChart(nChartTypeCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getColumnsBasedOnQuery(final int nSqlQueryCode, UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.getColumnsBasedOnQuery(nSqlQueryCode, userInfo);
	}

	@Override
	public List<ChartProperty> getChartProperty(int nChartTypeCode, UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.getChartProperty(nChartTypeCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createChartPropTransaction(List<ChartPropTransaction> chartPropTransaction,
			UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.createChartPropTransaction(chartPropTransaction, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateChartPropTransaction(ChartPropTransaction chartPropTransaction,
			UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.updateChartPropTransaction(chartPropTransaction, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteChartPropTransaction(ChartPropTransaction chartPropTransaction,
			UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.deleteChartPropTransaction(chartPropTransaction, userInfo);
	}

	@Override
	public ResponseEntity<Object> getChartPropTransaction(UserInfo userInfo, int ndashBoardTypeCode, int nChartTypeCode,
			final String sDashBoardProperty, final int nSqlQueryCode) throws Exception {
		return dashBoardTypeDAO.getChartPropTransaction(userInfo, ndashBoardTypeCode, nChartTypeCode,
				sDashBoardProperty, nSqlQueryCode);
	}

	@Override
	public ResponseEntity<Object> getDashBoardDesign(UserInfo userInfo, int ndashBoardTypeCode, final int nSqlQueryCode)
			throws Exception {
		return dashBoardTypeDAO.getDashBoardDesign(userInfo, ndashBoardTypeCode, nSqlQueryCode);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createDashBoardDesignConfig(List<DashBoardDesignConfig> dashBoardDesignConfig,
			final int ndashBoardTypeCode, final UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.createDashBoardDesignConfig(dashBoardDesignConfig, ndashBoardTypeCode, userInfo);
	}

	@Override
	public List<DashBoardDesignConfig> getSelectedDashBoardDesign(UserInfo userInfo, int ndashBoardTypeCode)
			throws Exception {
		return dashBoardTypeDAO.getSelectedDashBoardDesign(userInfo, ndashBoardTypeCode);
	}

	@Override
	public ResponseEntity<Object> getDashBoardView(UserInfo userInfo, int nDashBoardTypeCode) throws Exception {
		return dashBoardTypeDAO.getDashBoardView(userInfo, nDashBoardTypeCode);
	}

	@Override
	public ResponseEntity<Object> getAllSelectionDashBoardView(UserInfo userInfo, int nDashBoardTypeCode)
			throws Exception {
		return dashBoardTypeDAO.getAllSelectionDashBoardView(userInfo, nDashBoardTypeCode);
	}

	public ResponseEntity<Object> getDashboardViewChartParameters(Map<String, Object> obj) throws Exception {
		return dashBoardTypeDAO.getDashboardViewChartParameters(obj);
	}

	@Override
	public ResponseEntity<Object> getDashBoardParameterMappingComboData(int nDashBoardTypeCode, UserInfo userInfo)
			throws Exception {
		return dashBoardTypeDAO.getDashBoardParameterMappingComboData(nDashBoardTypeCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createDashBoardParameterMapping(List<DashBoardParameterMapping> mappingList,
			UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.createDashBoardParameterMapping(mappingList, userInfo);
	}

	@Override
	public ResponseEntity<Object> getChildDataList(DashBoardDesignConfig parentConfig,
			Map<String, Object> inputFieldData, String parentValue, UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.getChildDataList(parentConfig, inputFieldData, parentValue, userInfo);
	}

	@Override
	public ResponseEntity<Object> getHomeDashBoard(UserInfo userInfo, final int npageCode, final Boolean bpageAction)
			throws Exception {
		return dashBoardTypeDAO.getHomeDashBoard(userInfo, npageCode, bpageAction);
	}

	@Override
	public ResponseEntity<Object> getDashBoardHomePagesandTemplates(UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.getDashBoardHomePagesandTemplates(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createDashBoardHomePriority(DashBoardHomePriority mappingList,
			List<DashBoardHomeTypes> dashBoardHomeTypes, UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.createDashBoardHomePriority(mappingList, dashBoardHomeTypes, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateDashBoardHomePriority(DashBoardHomePriority mappingList,
			List<DashBoardHomeTypes> dashBoardHomeTypes, UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.updateDashBoardHomePriority(mappingList, dashBoardHomeTypes, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteDashBoardHomePriority(DashBoardHomePriority mappingList,
			List<DashBoardHomeTypes> dashBoardHomeTypes, UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.deleteDashBoardHomePriority(mappingList, dashBoardHomeTypes, userInfo);
	}

	@Override
	public ResponseEntity<Object> getDashBoardHomeConfig(UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.getDashBoardHomeConfig(userInfo);
	}

	@Override
	public ResponseEntity<Object> getDashBoardHomeConfigByID(int nprioritycode, UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.getDashBoardHomeConfigByID(nprioritycode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> checkParameteAvailableInDashBoardView(UserInfo userInfo, int nDashBoardTypeCode)
			throws Exception {
		return dashBoardTypeDAO.checkParameteAvailableInDashBoardView(userInfo, nDashBoardTypeCode);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateDashBoardDesignConfig(List<DashBoardDesignConfig> dashBoardDesignConfig,
			final int ndashBoardTypeCode, UserInfo userInfo) throws Exception {
		return dashBoardTypeDAO.updateDashBoardDesignConfig(dashBoardDesignConfig, ndashBoardTypeCode, userInfo);
	}
}
