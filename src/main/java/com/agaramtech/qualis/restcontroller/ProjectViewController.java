package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.project.service.projectview.ProjectViewService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the ProjectView Service methods
 * 
 * @author ATE172
 * @version 10.0.0.2
 */
@RestController
@RequestMapping("/projectview")
public class ProjectViewController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectViewController.class);
	private RequestContext requestContext;
	private final ProjectViewService projectviewService;

	public ProjectViewController(RequestContext requestContext, ProjectViewService projectviewService) {
		super();
		this.requestContext = requestContext;
		this.projectviewService = projectviewService;
	}

	@PostMapping(value = "/getProjectView")
	public ResponseEntity<Object> getProjectView(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		LOGGER.info("getProjectView() called");
		requestContext.setUserInfo(userInfo);
		return projectviewService.getProjectView(fromDate, toDate, currentUIDate, userInfo);

	}

	@PostMapping(value = "/getProjectViewBySampleType")
	public ResponseEntity<Object> getProjectViewBySampleType(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer nsampletypecode = null;
		Integer nprojecttypecode = null;
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		if (inputMap.get("nsampletypecode") != null) {
			nsampletypecode = (Integer) inputMap.get("nsampletypecode");
		}
		if (inputMap.get("nprojecttypecode") != null) {
			nprojecttypecode = (Integer) inputMap.get("nprojecttypecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return projectviewService.getProjectViewBySampleType(fromDate, toDate, currentUIDate, nsampletypecode,
				nprojecttypecode, userInfo);
	}

	@PostMapping(value = "/getActiveProjectViewById")
	public ResponseEntity<Object> getActiveProjectViewById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		final int nprojectmastercode = (Integer) inputMap.get("nprojectmastercode");
		final int nsampletypecode = (Integer) inputMap.get("nsampletypecode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return projectviewService.getActiveProjectViewById(nsampletypecode, nprojectmastercode, userInfo);
	}
}
