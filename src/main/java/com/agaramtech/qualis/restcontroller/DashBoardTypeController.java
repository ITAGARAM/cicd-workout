package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.dashboard.model.ChartPropTransaction;
import com.agaramtech.qualis.dashboard.model.ChartProperty;
import com.agaramtech.qualis.dashboard.model.ChartType;
import com.agaramtech.qualis.dashboard.model.DashBoardDesignConfig;
import com.agaramtech.qualis.dashboard.model.DashBoardParameterMapping;
import com.agaramtech.qualis.dashboard.model.DashBoardType;
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.dashboard.service.dashboardtypes.DashBoardTypeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the dashboard Service methods.
 */
@RestController
@RequestMapping("/dashboardtypes")
public class DashBoardTypeController {
	private static final Logger LOGGER = LoggerFactory.getLogger(DashBoardTypeController.class);

	private final DashBoardTypeService dashBoardTypeService;
	private RequestContext requestContext;

	public DashBoardTypeController(RequestContext requestContext, DashBoardTypeService dashBoardTypeService) {
		super();
		this.requestContext = requestContext;
		this.dashBoardTypeService = dashBoardTypeService;
	}

	@PostMapping(value = "/getDashBoardTypes")
	public ResponseEntity<Object> getDashBoardTypes(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int nDashBoardTypeCode = 0;
		LOGGER.info("getDashBoardTypes called");
		if (inputMap.get("ndashboardtypecode") != null) {
			nDashBoardTypeCode = (Integer) inputMap.get("ndashboardtypecode");
		}

		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getDashBoardTypes(userInfo, nDashBoardTypeCode);
	}

	@PostMapping(value = "/getDashBoardTypeByID")
	public ResponseEntity<Object> getDashBoardTypeByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ndashBoardTypeCode = (Integer) inputMap.get("ndashboardtypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getDashBoardTypeByID(ndashBoardTypeCode, userInfo);
	}

	@PostMapping(value = "/getChartTypes")
	public List<ChartType> getChartTypes(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getChartTypes(userInfo);
	}

	@PostMapping(value = "/getSqlQueriesByChart")
	public List<SQLQuery> getSqlQueriesByChart(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nChartTypeCode = (Integer) inputMap.get("ncharttypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getSqlQueriesByChart(nChartTypeCode, userInfo);
	}

	@PostMapping(value = "/createDashBoardTypes")
	public ResponseEntity<Object> createDashBoardTypes(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final DashBoardType dashBoardType = objmapper.convertValue(inputMap.get("dashboardtype"), DashBoardType.class);
		final List<ChartPropTransaction> chartPropTransaction = objmapper
				.convertValue(inputMap.get("chartproptransaction"), new TypeReference<List<ChartPropTransaction>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.createDashBoardTypes(dashBoardType, chartPropTransaction, userInfo);
	}

	@PostMapping(value = "/updateDashBoardTypes")
	public ResponseEntity<Object> updateDashBoardTypes(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final DashBoardType dashBoardType = objmapper.convertValue(inputMap.get("dashboardtype"), DashBoardType.class);
		final List<ChartPropTransaction> chartPropTransaction = objmapper
				.convertValue(inputMap.get("chartproptransaction"), new TypeReference<List<ChartPropTransaction>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.updateDashBoardTypes(dashBoardType, chartPropTransaction, userInfo);
	}

	@PostMapping(value = "/deleteDashBoardTypes")
	public ResponseEntity<Object> deleteDashBoardTypes(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final DashBoardType dashBoardType = objmapper.convertValue(inputMap.get("dashboardtype"), DashBoardType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.deleteDashBoardTypes(dashBoardType, userInfo);
	}

	@PostMapping(value = "/getColumnsBasedOnQuery")
	public ResponseEntity<Object> getColumnsBasedOnQuery(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nSqlQueryCode = (Integer) inputMap.get("nsqlquerycode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getColumnsBasedOnQuery(nSqlQueryCode, userInfo);
	}

	@PostMapping(value = "/getChartProperty")
	public List<ChartProperty> getChartProperty(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nChartTypeCode = (Integer) inputMap.get("ncharttypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getChartProperty(nChartTypeCode, userInfo);
	}

	@PostMapping(value = "/getChartPropTransaction")
	public ResponseEntity<Object> getChartPropTransaction(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ndashBoardTypeCode = (Integer) inputMap.get("ndashboardtypecode");
		final int nChartTypeCode = (Integer) inputMap.get("ncharttypecode");
		final int nSqlQueryCode = (Integer) inputMap.get("nsqlquerycode");
		final String sDashBoardProperty = "";
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getChartPropTransaction(userInfo, ndashBoardTypeCode, nChartTypeCode,
				sDashBoardProperty, nSqlQueryCode);
	}

	@PostMapping(value = "/getDashBoardDesign")
	public ResponseEntity<Object> getDashBoardDesign(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ndashBoardTypeCode = (Integer) inputMap.get("ndashBoardTypeCode");
		final int nSqlQueryCode = (Integer) inputMap.get("nSqlQueryCode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getDashBoardDesign(userInfo, ndashBoardTypeCode, nSqlQueryCode);
	}

	@PostMapping(value = "/createDashBoardDesignConfig")
	public ResponseEntity<Object> createDashBoardDesignConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<DashBoardDesignConfig> dashBoardDesignConfig = objmapper
				.convertValue(inputMap.get("dashboarddesignconfig"), new TypeReference<List<DashBoardDesignConfig>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ndashBoardTypeCode = (Integer) inputMap.get("ndashboardtypecode");
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.createDashBoardDesignConfig(dashBoardDesignConfig, ndashBoardTypeCode, userInfo);
	}

	@PostMapping(value = "/updateDashBoardDesignConfig")
	public ResponseEntity<Object> updateDashBoardDesignConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<DashBoardDesignConfig> dashBoardDesignConfig = objmapper
				.convertValue(inputMap.get("dashboarddesignconfig"), new TypeReference<List<DashBoardDesignConfig>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ndashBoardTypeCode = (Integer) inputMap.get("ndashboardtypecode");
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.updateDashBoardDesignConfig(dashBoardDesignConfig, ndashBoardTypeCode, userInfo);
	}

	@PostMapping(value = "/getDashBoardView")
	public ResponseEntity<Object> getDashBoardView(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nDashBoardTypeCode = (Integer) inputMap.get("ndashBoardTypeCode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getDashBoardView(userInfo, nDashBoardTypeCode);
	}

	@PostMapping(value = "/getDashBoardParameterMappingComboData")
	public ResponseEntity<Object> getDashBoardParameterMappingComboData(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nDashBoardTypeCode = (Integer) inputMap.get("ndashboardtypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getDashBoardParameterMappingComboData(nDashBoardTypeCode, userInfo);
	}

	@PostMapping(value = "/createDashBoardParameterMapping")
	public ResponseEntity<Object> createDashBoardParameterMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final List<DashBoardParameterMapping> configList = objMapper.convertValue(
				inputMap.get("dashboardparametermapping"), new TypeReference<List<DashBoardParameterMapping>>() {
				});
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.createDashBoardParameterMapping(configList, userInfo);
	}

}
