package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.dashboard.model.DashBoardDesignConfig;
import com.agaramtech.qualis.dashboard.service.dashboardtypes.DashBoardTypeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the dashboard Service methods.
 * 
 * @author ATE090
 * @version
 * @since 12- Oct- 2020
 */
@RestController
@RequestMapping("/dashboardview")

public class DashBoardViewController {

	private DashBoardTypeService dashBoardTypeService;
	private RequestContext requestContext;

	public DashBoardViewController(RequestContext requestContext, DashBoardTypeService dashBoardTypeService) {
		super();
		this.requestContext = requestContext;
		this.dashBoardTypeService = dashBoardTypeService;
	}

	@RequestMapping(value = "/getDashBoardView", method = RequestMethod.POST)
	public ResponseEntity<Object> getDashBoardView(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nDashBoardTypeCode = 0;
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getDashBoardView(userInfo, nDashBoardTypeCode);
	}

	@RequestMapping(value = "/getAllSelectionDashBoardView", method = RequestMethod.POST)
	public ResponseEntity<Object> getAllSelectionDashBoardView(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nDashBoardTypeCode = (Integer) inputMap.get("ndashboardtypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getAllSelectionDashBoardView(userInfo, nDashBoardTypeCode);
	}

	@RequestMapping(value = "/checkParameteAvailableInDashBoardView", method = RequestMethod.POST)
	public ResponseEntity<Object> checkParameteAvailableInDashBoardView(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nDashBoardTypeCode = (Integer) inputMap.get("ndashboardtypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.checkParameteAvailableInDashBoardView(userInfo, nDashBoardTypeCode);
	}

	/**
	 * This method is used to get selected dashboard's design configuration details
	 */
	@RequestMapping(value = "/getSelectedDashBoardDesign", method = RequestMethod.POST)
	public List<DashBoardDesignConfig> getSelectedDashBoardDesign(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nDashBoardTypeCode = (Integer) inputMap.get("ndashBoardTypeCode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getSelectedDashBoardDesign(userInfo, nDashBoardTypeCode);
	}

	@RequestMapping(value = "/getChartParameters", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> getChartParameters(@RequestBody Map<String, Object> obj) throws Exception {

		return dashBoardTypeService.getDashboardViewChartParameters(obj);// .getChartParameters(obj);
	}

	@RequestMapping(value = "/getChildDataList", method = RequestMethod.POST)
	public ResponseEntity<Object> getChildDataList(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final DashBoardDesignConfig parentConfig = objMapper.convertValue(inputMap.get("dashboarddesignconfig"),
				DashBoardDesignConfig.class);
		final String parentValue = (String) inputMap.get("parentcode");
		final Map<String, Object> inputFieldData = objMapper.convertValue(inputMap.get("inputfielddata"),
				new TypeReference<Map<String, Object>>() {
				});
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getChildDataList(parentConfig, inputFieldData, parentValue, userInfo);
	}

	@RequestMapping(value = "/getHomeDashBoard", method = RequestMethod.POST)
	public ResponseEntity<Object> getHomeDashBoard(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int npageCode = (Integer) inputMap.get("ndashboardhomepageCode");
		final Boolean bpageAction = (Boolean) inputMap.get("pageAction");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getHomeDashBoard(userInfo, npageCode, bpageAction);

	}
}
