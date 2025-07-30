package com.agaramtech.qualis.materialmanagement.service.materialcategory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialCategory;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class MaterialCategoryServiceImpl implements MaterialCategoryService {

	private MaterialCategoryDAO materialCategoryDAO;
	CommonFunction commonFunction;

	public MaterialCategoryServiceImpl(MaterialCategoryDAO materialCategoryDAO, CommonFunction commonFunction) {
		this.materialCategoryDAO = materialCategoryDAO;
		this.commonFunction = commonFunction;
	}
	
	@Override
	public ResponseEntity<Object> getMaterialCategory(final UserInfo userInfo) throws Exception {
		return materialCategoryDAO.getMaterialCategory(userInfo);
	}

	@Override
	public ResponseEntity<Object> getMaterialCategoryBYSupplierCode(final int nmasterSiteCode, final int nsuppliercode,
			final UserInfo userInfo) throws Exception {
		return materialCategoryDAO.getMaterialCategoryBySupplierCode(nmasterSiteCode, nsuppliercode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveMaterialCategoryById(final int nmaterialcatcode, final UserInfo userInfo)
			throws Exception {
		final MaterialCategory materialCategory = materialCategoryDAO.getActiveMaterialCategoryById(nmaterialcatcode, userInfo);
		if (materialCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(materialCategory, HttpStatus.OK);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception {
		return materialCategoryDAO.createMaterialCategory(materialCategory, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception {
		return materialCategoryDAO.updateMaterialCategory(materialCategory, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> deleteMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception {
		return materialCategoryDAO.deleteMaterialCategory(materialCategory, userInfo);
	}

	@Override
	public ResponseEntity<Object> getMaterialType(final UserInfo userInfo) throws Exception {
		return materialCategoryDAO.getMaterialType(userInfo);
	}

}
