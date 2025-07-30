package com.agaramtech.qualis.BarcodeConfiguration.service.BarcodeTemplate;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.BarcodeConfiguration.model.BarcodeTemplate;
import com.agaramtech.qualis.global.UserInfo;

public interface BarcodeTemplateService {

	ResponseEntity<Object> getBarcodeTemplate(Integer nbarcodetemplatecode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getBarcodeTemplateModal(UserInfo userInfo);

	ResponseEntity<Object> getBarcodeTemplateControl(Integer nformcode, UserInfo userInfo);

	ResponseEntity<Object> createBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEditBarcodeTemplateModal(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> updateBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> approveBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> retireBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> deleteBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo) throws Exception;

}
