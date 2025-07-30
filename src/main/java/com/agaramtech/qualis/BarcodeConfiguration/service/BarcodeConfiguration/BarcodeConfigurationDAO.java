package com.agaramtech.qualis.BarcodeConfiguration.service.BarcodeConfiguration;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.BarcodeConfiguration.model.BarcodeConfiguration;
import com.agaramtech.qualis.global.UserInfo;

public interface BarcodeConfigurationDAO {

	ResponseEntity<Object> getBarcodeConfiguration(Integer nbarcodeconfigurationcode, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getBarcodeDynamicChange(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getBarcodeDynamicCombo(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getChildValuesForBarcodeConfiguration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getBarcodeConfigurationFilterSubmit(Map<String, Object> inputMap, boolean check,
			UserInfo userInfo);

	ResponseEntity<Object> getOpenModalForBarcodeConfig(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getBarcodeFileParameter(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createBarcodeConfiguration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> checkConfiguration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> barcodeGeneration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> deleteBarcodeConfiguration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> retireBarcodeConfiguration(BarcodeConfiguration barcodeConfiguration, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> approveBarcodeConfiguration(BarcodeConfiguration barcodeConfiguration, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> updateBarcodeConfiguration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEditBarcodeConfigurationModal(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> createsqltable(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

}
