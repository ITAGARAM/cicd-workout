package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.model.ADSSettings;
import com.agaramtech.qualis.configuration.service.adssettings.ADSSettingsService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/adssettings")
public class ADSSettingsController {

	private final ADSSettingsService adsSettingsService;
	private RequestContext requestContext;

	public ADSSettingsController(RequestContext requestContext, ADSSettingsService adsSettingsService) {
		super();
		this.requestContext = requestContext;
		this.adsSettingsService = adsSettingsService;
	}

	@PostMapping(value = "/getADSSettings")
	public ResponseEntity<Object> getADSSettings(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return adsSettingsService.getADSSettings(userInfo);
	}

	@PostMapping(value = "/getActiveADSSettingsById")
	public ResponseEntity<Object> getActiveADSSettingsById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		int adsSettingsCode = (int) inputMap.get("nadssettingscode");

		return adsSettingsService.getActiveADSSettingsById(adsSettingsCode, userInfo);
	}

	@PostMapping(value = "/createADSSettings")
	public ResponseEntity<Object> createADSSettings(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		ADSSettings adsSettings = mapper.convertValue(inputMap.get("adssettings"), new TypeReference<ADSSettings>() {
		});
		requestContext.setUserInfo(userInfo);

		return adsSettingsService.createADSSettings(adsSettings, userInfo);

	}

	@PostMapping(value = "/updateADSSettings")
	public ResponseEntity<Object> updateADSSettings(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		ADSSettings adsSettings = mapper.convertValue(inputMap.get("adssettings"), new TypeReference<ADSSettings>() {
		});
		requestContext.setUserInfo(userInfo);

		return adsSettingsService.updateADSSettings(adsSettings, userInfo);

	}

	@PostMapping(value = "/deleteADSSettings")
	public ResponseEntity<Object> deleteADSSettings(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());

		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		ADSSettings adsSettings = mapper.convertValue(inputMap.get("adssettings"), new TypeReference<ADSSettings>() {
		});
		requestContext.setUserInfo(userInfo);

		return adsSettingsService.deleteADSSettings(adsSettings, userInfo);

	}
}
