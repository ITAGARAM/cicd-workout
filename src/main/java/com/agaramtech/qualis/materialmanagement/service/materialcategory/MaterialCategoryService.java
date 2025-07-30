package com.agaramtech.qualis.materialmanagement.service.materialcategory;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialCategory;

public interface MaterialCategoryService {
	public ResponseEntity<Object> getMaterialCategory(final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> getMaterialCategoryBYSupplierCode(final int siteCode, final int nsuppliercode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveMaterialCategoryById(final int nmaterialcatcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getMaterialType(final UserInfo userinfo) throws Exception;

}
