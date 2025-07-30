package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.service.reportview.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/reststimulsoft")
public class ReportViewStimulsoftController {

	private RequestContext requestContext;
	private final ReportService reportViewService;

	public ReportViewStimulsoftController(ReportService reportViewService, RequestContext requestContext) {
		super();
		this.requestContext = requestContext;
		this.reportViewService = reportViewService;
	}

	@PostMapping(value = "/getStimulsoftView")
	public ResponseEntity<Object> getStimulsoftView(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		Integer reportModuleCode = null;
		if (inputMap.get("nreportmodulecode") != null) {
			reportModuleCode = (Integer) inputMap.get("nreportmodulecode");
		}

		return reportViewService.getStimulsoftView(reportModuleCode, userInfo);

	}

}
