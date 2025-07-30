package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.dashboard.model.DashBoardHomePriority;
import com.agaramtech.qualis.dashboard.model.DashBoardHomeTypes;
import com.agaramtech.qualis.dashboard.service.dashboardtypes.DashBoardTypeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the DashBoardHomeConfig Service methods.
 * 
 * @author ATE090
 * @version
 * @since 26- jan- 2021
 */
@RestController
@RequestMapping("/dashboardhomeconfig")
public class DashBoardHomeConfigController {

	private DashBoardTypeService dashBoardTypeService;
	private RequestContext requestContext;

	public DashBoardHomeConfigController(RequestContext requestContext, DashBoardTypeService dashBoardTypeService) {
		super();
		this.requestContext = requestContext;
		this.dashBoardTypeService = dashBoardTypeService;
	}

	@RequestMapping(value = "/getDashBoardHomeConfig", method = RequestMethod.POST)
	public ResponseEntity<Object> getDashBoardView(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getDashBoardHomeConfig(userInfo);
	}

	@RequestMapping(value = "/getDashBoardHomePagesandTemplates", method = RequestMethod.POST)
	public ResponseEntity<Object> getDashBoardHomePagesandTemplates(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getDashBoardHomePagesandTemplates(userInfo);
	}

	@RequestMapping(value = "/getDashBoardHomeConfigByID", method = RequestMethod.POST)
	public ResponseEntity<Object> getDashBoardHomeConfigByID(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ndashBoardhomeprioritycode = (Integer) inputMap.get("ndashboardhomeprioritycode");
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.getDashBoardHomeConfigByID(ndashBoardhomeprioritycode, userInfo);
	}

	@RequestMapping(value = "/createDashBoardHomeConfig", method = RequestMethod.POST)
	public ResponseEntity<Object> createDashBoardHomeConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final DashBoardHomePriority dashBoardHomePriority = objmapper
				.convertValue(inputMap.get("dashboardhomepriority"), DashBoardHomePriority.class);
		final List<DashBoardHomeTypes> dashBoardHomeTypes = objmapper.convertValue(inputMap.get("dashboardhometypes"),
				new TypeReference<List<DashBoardHomeTypes>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.createDashBoardHomePriority(dashBoardHomePriority, dashBoardHomeTypes, userInfo);
	}

	@RequestMapping(value = "/updateDashBoardHomeConfig", method = RequestMethod.POST)
	public ResponseEntity<Object> updateDashBoardHomeConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final DashBoardHomePriority dashBoardHomePriority = objmapper
				.convertValue(inputMap.get("dashboardhomepriority"), DashBoardHomePriority.class);
		final List<DashBoardHomeTypes> dashBoardHomeTypes = objmapper.convertValue(inputMap.get("dashboardhometypes"),
				new TypeReference<List<DashBoardHomeTypes>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.updateDashBoardHomePriority(dashBoardHomePriority, dashBoardHomeTypes, userInfo);
	}

	@RequestMapping(value = "/deleteDashBoardHomeConfig", method = RequestMethod.POST)
	public ResponseEntity<Object> deleteDashBoardHomeConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final DashBoardHomePriority dashBoardHomePriority = objmapper
				.convertValue(inputMap.get("dashboardhomepriority"), DashBoardHomePriority.class);
		final List<DashBoardHomeTypes> dashBoardHomeTypes = objmapper.convertValue(inputMap.get("dashboardhometypes"),
				new TypeReference<List<DashBoardHomeTypes>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return dashBoardTypeService.deleteDashBoardHomePriority(dashBoardHomePriority, dashBoardHomeTypes, userInfo);
	}
}
