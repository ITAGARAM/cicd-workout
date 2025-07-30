package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.configuration.service.settings.SettingsService;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/settings")
public class SettingsController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SettingsController.class);

	private final SettingsService settingsService;
	private RequestContext requestContext;

	public SettingsController(SettingsService settingsService, RequestContext requestContext) {
		super();
		this.settingsService = settingsService;
		this.requestContext = requestContext;
		
	}
	
	@PostMapping(value = "/getSettings")
	public ResponseEntity<Object> getSettings(@RequestBody Map<String, Object> inputMap) throws Exception {
        final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getSettings called");
		return settingsService.getSettings(userInfo);
	}

	/**
	 * This method is used to add a new entry to settings table.
	 * 
	 * @param inputMap [Map] holds the unit object to be inserted
	 * @return inserted unit object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createSettings")
	public ResponseEntity<Object> createSettings(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Settings objSettings = objMapper.convertValue(inputMap.get("settings"), new TypeReference<Settings>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return settingsService.createSettings(objSettings, userInfo);
	}

	/**
	 * This method is used to update entry in settings table.
	 * 
	 * @param inputMap [Map] holds the settings object to be updated
	 * @return response entity object holding response status and data of updated
	 *         settings object
	 * @throws Exception
	 */
	@PostMapping(value = "/updateSettings")
	public ResponseEntity<Object> updateSettings(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Settings objSettings = objMapper.convertValue(inputMap.get("settings"), new TypeReference<Settings>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return settingsService.updateSettings(objSettings, userInfo);
	}

	/**
	 * This method id used to delete an entry in settings table
	 * 
	 * @param inputMap [Map] holds the settings object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         settings object
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteSettings")
	public ResponseEntity<Object> deleteSettings(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Settings objSettings = objMapper.convertValue(inputMap.get("settings"), new TypeReference<Settings>() {
		});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return settingsService.deleteSettings(objSettings, userInfo);
	}

	/**
	 * This method is used to get the single record in settings table
	 * 
	 * @param inputMap [Map] holds the settings code to get
	 * @return response entity object holding response status and data of single
	 *         settings object
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveSettingsById")
	public ResponseEntity<Object> getActiveSettingsById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nsettingCode = (int) inputMap.get("nsettingcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return settingsService.getActiveSettingsById(nsettingCode, userInfo);
	}
}
