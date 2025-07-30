package com.agaramtech.qualis.restcontroller;

import java.util.Map;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.service.sitehierarchyconfiguration.SiteHierarchyConfigurationService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/sitehierarchyconfiguration")
public class SiteHierarchyConfigurationController {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(BulkBarcodeConfigController.class);
	private RequestContext requestContext;
	private final SiteHierarchyConfigurationService siteHierarchyConfigurationService;

	public SiteHierarchyConfigurationController(RequestContext requestContext,
			SiteHierarchyConfigurationService siteHierarchyConfigurationService) {
		super();
		this.requestContext = requestContext;
		this.siteHierarchyConfigurationService = siteHierarchyConfigurationService;
	}

	@PostMapping(value = "/getSiteHierarchyConfiguration")
	public ResponseEntity<Object> getSiteHierarchyConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		int nsitehierarchyconfigcode = -1;
		if (inputMap.get("nsitehierarchyconfigcode") != null) {
			nsitehierarchyconfigcode = Integer.valueOf(inputMap.get("nsitehierarchyconfigcode").toString());
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteHierarchyConfigurationService.getSiteHierarchyConfiguration(userInfo, nsitehierarchyconfigcode);
	}

	@PostMapping(value = "/getSiteHierarchy")
	public ResponseEntity<Object> getSiteHierarchy(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteHierarchyConfigurationService.getSiteHierarchy(userInfo, inputMap);
	}

	@PostMapping(value = "/createSiteHierarchyConfig")
	public ResponseEntity<Object> createSiteHierarchyConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return siteHierarchyConfigurationService.createSiteHierarchyConfig(inputMap, userInfo);
	}

	@PostMapping(value = "/approveSiteHierarchyConfig")
	public ResponseEntity<Object> approveSiteHierarchyConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return siteHierarchyConfigurationService.approveSiteHierarchyConfig(userInfo, inputMap);
	}

	@PostMapping(value = "/deleteSiteHierarchyConfig")
	public ResponseEntity<Object> deleteSiteHierarchyConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return siteHierarchyConfigurationService.deleteSiteHierarchyConfig(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveSiteHierarchyConfigById")
	public ResponseEntity<Object> getActiveSiteHierarchyConfigById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nsitehierarchyconfigcode = (Integer) inputMap.get("nsitehierarchyconfigcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return siteHierarchyConfigurationService.getActiveSiteHierarchyConfigById(nsitehierarchyconfigcode, userInfo);
	}

	@PostMapping(value = "/updateSiteHierarchyConfig")
	public ResponseEntity<Object> updateSiteHierarchyConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return siteHierarchyConfigurationService.updateSiteHierarchyConfig(inputMap, userInfo);
	}

	@PostMapping(value = "/retireSiteHierarchyConfig")
	public ResponseEntity<Object> retireSiteHierarchyConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return siteHierarchyConfigurationService.retireSiteHierarchyConfig(inputMap, userInfo);
	}

}
