package com.agaramtech.qualis.materialmanagement.service.materialcategory;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialCategory;

public interface MaterialCategoryDAO {

	public ResponseEntity<Object> getMaterialCategory(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getMaterialCategoryBySupplierCode(final int nmasterSiteCode, final int nsuppliercode,
			final UserInfo userInfo) throws Exception;

	public MaterialCategory getActiveMaterialCategoryById(final int nmaterialcatcode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteMaterialCategory(MaterialCategory materialCategory, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getMaterialType(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getMaterialCategoryByType(short materialTypeCode, UserInfo userInfo) throws Exception;
}
