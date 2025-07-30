package com.agaramtech.qualis.BarcodeConfiguration.service.BarcodeTemplate;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.BarcodeConfiguration.model.BarcodeTemplate;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class BarcodeTemplateServiceImpl implements BarcodeTemplateService{
	
	
	private final BarcodeTemplateDAO objBarcodeTemplateDAO;
	
	public BarcodeTemplateServiceImpl(BarcodeTemplateDAO objBarcodeTemplateDAO) {
		this.objBarcodeTemplateDAO = objBarcodeTemplateDAO;
	}

	@Override
	public ResponseEntity<Object> getBarcodeTemplate(Integer nbarcodetemplatecode,UserInfo userInfo) throws Exception {
		return objBarcodeTemplateDAO.getBarcodeTemplate( nbarcodetemplatecode,userInfo);
	}

	@Override
	public ResponseEntity<Object> getBarcodeTemplateModal(UserInfo userInfo) {
		return objBarcodeTemplateDAO.getBarcodeTemplateModal(userInfo);
	}

	@Override
	public ResponseEntity<Object> getBarcodeTemplateControl(Integer nformcode, UserInfo userInfo) {
		return objBarcodeTemplateDAO.getBarcodeTemplateControl(nformcode,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo) throws Exception {
		return objBarcodeTemplateDAO.createBarcodeTemplate(barcodeTemplate,userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditBarcodeTemplateModal(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return objBarcodeTemplateDAO.getEditBarcodeTemplateModal(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo) throws Exception {
		return objBarcodeTemplateDAO.updateBarcodeTemplate(barcodeTemplate,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approveBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo)throws Exception {
		return objBarcodeTemplateDAO.approveBarcodeTemplate(barcodeTemplate,userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> retireBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo)throws Exception {
		return objBarcodeTemplateDAO.retireBarcodeTemplate(barcodeTemplate,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteBarcodeTemplate(BarcodeTemplate barcodeTemplate, UserInfo userInfo) throws Exception {
		return objBarcodeTemplateDAO.deleteBarcodeTemplate(barcodeTemplate,userInfo);
	}

}
