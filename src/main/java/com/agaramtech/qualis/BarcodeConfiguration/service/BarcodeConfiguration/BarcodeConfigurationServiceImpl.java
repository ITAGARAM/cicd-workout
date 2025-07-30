package com.agaramtech.qualis.BarcodeConfiguration.service.BarcodeConfiguration;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.BarcodeConfiguration.model.BarcodeConfiguration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class BarcodeConfigurationServiceImpl implements BarcodeConfigurationService{

    private final BarcodeConfigurationDAO objBarcodeConfigurationDAO;
	
	public BarcodeConfigurationServiceImpl(BarcodeConfigurationDAO objBarcodeConfigurationDAO) {
		this.objBarcodeConfigurationDAO = objBarcodeConfigurationDAO;
	}
	
	@Override
	public ResponseEntity<Object> getBarcodeConfiguration(Integer nbarcodeconfigurationcode, UserInfo userInfo) throws Exception {
		return objBarcodeConfigurationDAO.getBarcodeConfiguration(nbarcodeconfigurationcode,userInfo);
	}

	@Override
	public ResponseEntity<Object> getBarcodeDynamicChange(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return objBarcodeConfigurationDAO.getBarcodeDynamicChange(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getBarcodeDynamicCombo(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return objBarcodeConfigurationDAO.getBarcodeDynamicCombo(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getChildValuesForBarcodeConfiguration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBarcodeConfigurationDAO.getChildValuesForBarcodeConfiguration(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getBarcodeConfigurationFilterSubmit(Map<String, Object> inputMap,boolean check ,UserInfo userInfo) {
		return objBarcodeConfigurationDAO.getBarcodeConfigurationFilterSubmit(inputMap,check,userInfo);
	}

	@Override
	public ResponseEntity<Object> getOpenModalForBarcodeConfig(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return objBarcodeConfigurationDAO.getOpenModalForBarcodeConfig(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getBarcodeFileParameter(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return objBarcodeConfigurationDAO.getBarcodeFileParameter(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createBarcodeConfiguration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return objBarcodeConfigurationDAO.createBarcodeConfiguration(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> checkConfiguration(Map<String, Object> inputMap, UserInfo userInfo)throws Exception {
		return objBarcodeConfigurationDAO.checkConfiguration(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> barcodeGeneration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return objBarcodeConfigurationDAO.barcodeGeneration(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditBarcodeConfigurationModal(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBarcodeConfigurationDAO.getEditBarcodeConfigurationModal(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateBarcodeConfiguration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBarcodeConfigurationDAO.updateBarcodeConfiguration(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approveBarcodeConfiguration(BarcodeConfiguration barcodeConfiguration, UserInfo userInfo)
			throws Exception {
		return objBarcodeConfigurationDAO.approveBarcodeConfiguration(barcodeConfiguration,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> retireBarcodeConfiguration(BarcodeConfiguration barcodeConfiguration, UserInfo userInfo)
			throws Exception {
		return objBarcodeConfigurationDAO.retireBarcodeConfiguration(barcodeConfiguration,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteBarcodeConfiguration(Map<String,Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBarcodeConfigurationDAO.deleteBarcodeConfiguration(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createsqltable(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return objBarcodeConfigurationDAO.createsqltable(inputMap,userInfo);
	}

}
