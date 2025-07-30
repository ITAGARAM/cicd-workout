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
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.misrights.model.AlertHomeRights;
import com.agaramtech.qualis.misrights.model.AlertRights;
import com.agaramtech.qualis.misrights.model.DashBoardHomeRights;
import com.agaramtech.qualis.misrights.model.DashBoardRights;
import com.agaramtech.qualis.misrights.model.ReportRights;
import com.agaramtech.qualis.misrights.service.MISRightsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/misrights")
public class MISRightsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MISRightsController.class);

	private RequestContext requestContext;
	private final MISRightsService misrightsService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext   RequestContext to hold the request
	 * @param MISRightsService misrightsService
	 */
	public MISRightsController(RequestContext requestContext, MISRightsService misrightsService) {
		super();
		this.requestContext = requestContext;
		this.misrightsService = misrightsService;
	}

	@PostMapping(value = "/getMISRights")
	public ResponseEntity<Object> getMISRights(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("Get Controller -->" + userInfo);
		return misrightsService.getMISRights(nuserrolecode, userInfo);
	}

	@PostMapping(value = "/getDashBoardTypeByUserRole")
	public ResponseEntity<Object> getDashBoardTypeByUserRole(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return misrightsService.getDashBoardTypeByUserRole(nuserrolecode, userInfo);
	}

	@PostMapping(value = "/getReportByUserRole")
	public ResponseEntity<Object> getReportByUserRole(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return misrightsService.getReportByUserRole(nuserrolecode, userInfo);
	}

	@PostMapping(value = "/createDashBoardRights")
	public ResponseEntity<Object> createDashBoardRights(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<DashBoardRights> DashBoardRights = objmapper.convertValue(inputMap.get("dashboardrights"),
				new TypeReference<List<DashBoardRights>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return misrightsService.createDashBoardRights(DashBoardRights, userInfo);
	}

	@PostMapping(value = "/createReportRights")
	public ResponseEntity<Object> createReportRights(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<ReportRights> ReportRights = objmapper.convertValue(inputMap.get("reportrights"),
				new TypeReference<List<ReportRights>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return misrightsService.createReportRights(ReportRights, userInfo);
	}

	@PostMapping(value = "/deleteDashBoardRights")
	public ResponseEntity<Object> deleteDashBoardRights(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final DashBoardRights DashBoardRights = objmapper.convertValue(inputMap.get("dashboardrights"),
				DashBoardRights.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return misrightsService.deleteDashBoardRights(DashBoardRights, userInfo);
	}

	@PostMapping(value = "/deleteReportRights")
	public ResponseEntity<Object> deleteReportRights(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ReportRights ReportRights = objmapper.convertValue(inputMap.get("reportrights"), ReportRights.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return misrightsService.deleteReportRights(ReportRights, userInfo);
	}

	@PostMapping(value = "/getAlertByUserRole")
	public ResponseEntity<Object> getAlertByUserRole(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return misrightsService.getAlertByUserRole(nuserrolecode, userInfo);
	}

	@PostMapping(value = "/createAlertRights")
	public ResponseEntity<Object> createAlertRightsRights(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<AlertRights> AlertRights = objmapper.convertValue(inputMap.get("alertrights"),
				new TypeReference<List<AlertRights>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return misrightsService.createAlertRights(AlertRights, userInfo);
	}

	@PostMapping(value = "/deleteAlertRights")
	public ResponseEntity<Object> deleteAlertRights(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final AlertRights AlertRights = objmapper.convertValue(inputMap.get("alertrights"), AlertRights.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return misrightsService.deleteAlertRights(AlertRights, userInfo);
	}

	@PostMapping(value = "/createHomeChart")
	public ResponseEntity<Object> createHomeChart(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		DashBoardRights DashBoardRights = null;
		if (inputMap.containsKey("homechart")) {
			DashBoardRights = objmapper.convertValue(inputMap.get("homechart"), DashBoardRights.class);
		}
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return misrightsService.createHomeChart(DashBoardRights, userInfo);
	}

	@PostMapping(value = "/createHomeRights")
	public ResponseEntity<Object> createHomeDashBoardRights(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final List<DashBoardHomeRights> dashBoardHomeRights = objmapper.convertValue(inputMap.get("homeRights"),
				new TypeReference<List<DashBoardHomeRights>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return misrightsService.createHomeDashBoardRights(dashBoardHomeRights, userInfo);
	}

	@PostMapping(value = "/deleteHomeRights")
	public ResponseEntity<Object> deleteDashBoardHomeRights(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final DashBoardHomeRights DashBoardHomeRights = objmapper.convertValue(inputMap.get("homeRights"),
				DashBoardHomeRights.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return misrightsService.deleteDashBoardHomeRights(DashBoardHomeRights, userInfo);
	}

	@PostMapping(value = "/getHomeDashBoardRightsByUserRole")
	public ResponseEntity<Object> getHomeDashBoardRightsByUserRole(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return misrightsService.getHomeDashBoardRightsByUserRole(nuserrolecode, userInfo);
	}

	@PostMapping(value = "/getAlertHomeRightsByUserRole")
	public ResponseEntity<Object> getAlertHomeRigntsByUserRole(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return misrightsService.getAlertHomeRigntsByUserRole(nuserrolecode, userInfo);
	}

	@PostMapping(value = "/createAlertHomeRights")
	public ResponseEntity<Object> createAlertHomeRights(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<AlertHomeRights> AlertHomeRights = objmapper.convertValue(inputMap.get("alerthomerights"),
				new TypeReference<List<AlertHomeRights>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return misrightsService.createAlertHomeRights(AlertHomeRights, userInfo);
	}

	@PostMapping(value = "/deleteAlertHomeRights")
	public ResponseEntity<Object> deleteAlertHomeRights(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final AlertHomeRights AlertHomeRights = objmapper.convertValue(inputMap.get("alerthomerights"),
				AlertHomeRights.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return misrightsService.deleteAlertHomeRights(AlertHomeRights, userInfo);
	}
}