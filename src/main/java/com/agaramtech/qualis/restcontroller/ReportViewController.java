package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.model.ReportDesignConfig;
import com.agaramtech.qualis.reports.model.ReportMaster;
import com.agaramtech.qualis.reports.service.reportview.ReportService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/reportview")
public class ReportViewController {

	private RequestContext requestContext;
	private final ReportService reportViewService;

	public ReportViewController(ReportService reportViewService, RequestContext requestContext) {
		super();
		this.requestContext = requestContext;
		this.reportViewService = reportViewService;
	}

	@PostMapping(value = "/getReportView")
	public ResponseEntity<Object> getReportView(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		Integer reportModuleCode = null;
		if (inputMap.get("nreportmodulecode") != null) {
			reportModuleCode = (Integer) inputMap.get("nreportmodulecode");
		}
		return reportViewService.getReportView(reportModuleCode, userInfo);

	}

	@PostMapping(value = "/viewReport")
	public ResponseEntity<Object> viewReport(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReportMaster reportMaster = objMapper.convertValue(inputMap.get("reportmaster"), ReportMaster.class);
		return reportViewService.viewReport(reportMaster, userInfo);

	}

	@PostMapping(value = "/searchViewReport")
	public ResponseEntity<Object> searchViewReport(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int reportCode = (Integer) inputMap.get("nreportcode");
		return reportViewService.searchViewReport(reportCode, userInfo);

	}

	@PostMapping(value = "/getChildDataList")
	public ResponseEntity<Object> getChildDataList(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReportDesignConfig parentConfig = objMapper.convertValue(inputMap.get("reportdesignconfig"),
				ReportDesignConfig.class);
		final String parentValue = (String) inputMap.get("parentcode");
		final Map<String, Object> inputFieldData = objMapper.convertValue(inputMap.get("inputfielddata"),
				new TypeReference<Map<String, Object>>() {
				});
		return reportViewService.getChildDataList(parentConfig, inputFieldData, parentValue, userInfo);

	}

	@PostMapping(value = "/viewReportWithParameters")
	public ResponseEntity<Object> viewReportWithParameters(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		objMapper.registerModule(new JavaTimeModule());
		final ReportMaster reportMaster = objMapper.convertValue(inputMap.get("reportmaster"), ReportMaster.class);
		final Map<String, Object> inputFieldData = objMapper.convertValue(inputMap.get("inputfielddata"),
				new TypeReference<Map<String, Object>>() {
				});
		return reportViewService.viewReportWithParameters(reportMaster, inputFieldData, userInfo);

	}

}
