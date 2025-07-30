package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.configuration.model.ReportSettings;
import com.agaramtech.qualis.configuration.service.reportsettings.ReportSettingsService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/reportsettings")
public class ReportSettingsController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportSettingsController.class);

	private RequestContext requestContext;

	private final ReportSettingsService reportSettingsService;

	public ReportSettingsController(RequestContext requestContext, ReportSettingsService reportSettingsService) {
		super();
		this.requestContext = requestContext;
		this.reportSettingsService = reportSettingsService;
	}

	

	@PostMapping(value = "/getReportSettings")
	public ResponseEntity<Object> getReportSettings(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getReportSettings called");
		return reportSettingsService.getReportSettings(userInfo);
	}

	/**
	 * This method is used to add a new entry to reportsettings table.
	 * 
	 * @param inputMap [Map] holds the unit object to be inserted
	 * @return inserted unit object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createReportSettings")
	public ResponseEntity<Object> createReportSettings(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final ReportSettings objReportSettings = objMapper.convertValue(inputMap.get("reportsettings"),
				new TypeReference<ReportSettings>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return reportSettingsService.createReportSettings(objReportSettings, userInfo);
	}

	/**
	 * This method is used to update entry in reportsettings table.
	 * 
	 * @param inputMap [Map] holds the reportsettings object to be updated
	 * @return response entity object holding response status and data of updated
	 *         reportsettings object
	 * @throws Exception
	 */
	@PostMapping(value = "/updateReportSettings")
	public ResponseEntity<Object> updateReportSettings(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ReportSettings objReportSettings = objMapper.convertValue(inputMap.get("reportsettings"),
				new TypeReference<ReportSettings>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return reportSettingsService.updateReportSettings(objReportSettings, userInfo);

	}

	/**
	 * This method id used to delete an entry in reportsettings table
	 * 
	 * @param inputMap [Map] holds the reportsettings object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         reportsettings object
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteReportSettings")
	public ResponseEntity<Object> deleteReportSettings(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final ReportSettings objReportSettings = objMapper.convertValue(inputMap.get("reportsettings"),
				new TypeReference<ReportSettings>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return reportSettingsService.deleteReportSettings(objReportSettings, userInfo);

	}

	/**
	 * This method is used to get the single record in reportsettings table
	 * 
	 * @param inputMap [Map] holds the reportsettings code to get
	 * @return response entity object holding response status and data of single
	 *         reportsettings object
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveReportSettingsById")
	public ResponseEntity<Object> getActiveReportSettingsById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nreportsettingCode = (int) inputMap.get("nreportsettingcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return reportSettingsService.getActiveReportSettingsById(nreportsettingCode, userInfo);

	}

}
