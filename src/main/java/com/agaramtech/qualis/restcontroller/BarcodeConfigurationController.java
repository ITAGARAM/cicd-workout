package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.BarcodeConfiguration.model.BarcodeConfiguration;
import com.agaramtech.qualis.BarcodeConfiguration.service.BarcodeConfiguration.BarcodeConfigurationService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/barcodeconfiguration")
public class BarcodeConfigurationController {

	private static final Log LOGGER = LogFactory.getLog(BarcodeController.class);

	private final BarcodeConfigurationService objBarcodeConfigurationService;
	private RequestContext requestContext;

	public BarcodeConfigurationController(RequestContext requestContext,
			BarcodeConfigurationService objBarcodeConfigurationService) {
		super();
		this.requestContext = requestContext;
		this.objBarcodeConfigurationService = objBarcodeConfigurationService;
	}

	@PostMapping(value = "/getBarcodeConfiguration")
	public ResponseEntity<Object> getBarcodeConfiguration(@RequestBody Map<String, Object> inputMap) throws Exception {
		Integer nbarcodeconfigurationcode = null;
		if (inputMap.containsKey("nbarcodeconfigurationcode")) {
			nbarcodeconfigurationcode = (Integer) inputMap.get("nbarcodeconfigurationcode");
		}
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.getBarcodeConfiguration(nbarcodeconfigurationcode, userInfo);
	}

	@PostMapping(value = "/getBarcodeDynamicChange")
	public ResponseEntity<Object> getBarcodeDynamicChange(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.getBarcodeDynamicChange(inputMap, userInfo);
	}

	@PostMapping(value = "/getBarcodeDynamicCombo")
	public ResponseEntity<Object> getBarcodeDynamicCombo(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.getBarcodeDynamicCombo(inputMap, userInfo);
	}

	@PostMapping(value = "/getChildValuesForBarcodeConfiguration")
	public ResponseEntity<Object> getChildValuesForBarcodeConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.getChildValuesForBarcodeConfiguration(inputMap, userInfo);
	}

	@PostMapping(value = "/getBarcodeConfigurationFilterSubmit")
	public ResponseEntity<Object> getBarcodeConfigurationFilterSubmit(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.getBarcodeConfigurationFilterSubmit(inputMap, true, userInfo);
	}

	@PostMapping(value = "/getOpenModalForBarcodeConfig")
	public ResponseEntity<Object> getOpenModalForBarcodeConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.getOpenModalForBarcodeConfig(inputMap, userInfo);
	}

	@PostMapping(value = "/getBarcodeFileParameter")
	public ResponseEntity<Object> getBarcodeFileParameter(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.getBarcodeFileParameter(inputMap, userInfo);
	}

	@PostMapping(value = "/createBarcodeConfiguration")
	public ResponseEntity<Object> createBarcodeConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.createBarcodeConfiguration(inputMap, userInfo);
	}

	@PostMapping(value = "/checkConfiguration")
	public ResponseEntity<Object> checkConfiguration(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.checkConfiguration(inputMap, userInfo);
	}

	@PostMapping(value = "/barcodeGeneration")
	public ResponseEntity<Object> barcodeGeneration(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.barcodeGeneration(inputMap, userInfo);
	}

	@PostMapping(value = "/getEditBarcodeConfigurationModal")
	public ResponseEntity<Object> getEditBarcodeConfigurationModal(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.getEditBarcodeConfigurationModal(inputMap, userInfo);

	}

	@PostMapping(value = "/updateBarcodeConfiguration")
	public ResponseEntity<Object> updateBarcodeConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.updateBarcodeConfiguration(inputMap, userInfo);

	}

	@PostMapping(value = "/approveBarcodeConfiguration")
	public ResponseEntity<Object> approveBarcodeConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final BarcodeConfiguration barcodeconfiguration = objmapper.convertValue(inputMap.get("barcodeconfiguration"),
				BarcodeConfiguration.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.approveBarcodeConfiguration(barcodeconfiguration, userInfo);

	}

	@PostMapping(value = "/retireBarcodeConfiguration")
	public ResponseEntity<Object> retireBarcodeConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final BarcodeConfiguration barcodeconfiguration = objmapper.convertValue(inputMap.get("barcodeconfiguration"),
				BarcodeConfiguration.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.retireBarcodeConfiguration(barcodeconfiguration, userInfo);

	}

	@PostMapping(value = "/deleteBarcodeConfiguration")
	public ResponseEntity<Object> deleteBarcodeConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.deleteBarcodeConfiguration(inputMap, userInfo);

	}

	@PostMapping(value = "/createsqltable")
	public ResponseEntity<Object> createsqltable(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeConfigurationService.createsqltable(inputMap, userInfo);

	}

}
