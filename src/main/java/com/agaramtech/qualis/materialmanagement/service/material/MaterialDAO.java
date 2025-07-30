package com.agaramtech.qualis.materialmanagement.service.material;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;

public interface MaterialDAO {

	public ResponseEntity<Object> getMaterialType(final UserInfo objUserInfo, final String currentUIDate)
			throws Exception;

	public ResponseEntity<Object> getMaterialcombo(final Integer nmaterialtypecode, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getMaterialAdd(final short nmaterialtypecode, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> createMaterial(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> getMaterial() throws Exception;

	public ResponseEntity<Object> getMaterialDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<? extends Object> deleteMaterial(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getMaterialEdit(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateMaterial(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> getMaterialByTypeCode(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getMaterialByID(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getMaterialSection(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> createMaterialSafetyInstructions(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getMaterialSafetyInstructions(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> createMaterialMsdsAttachment(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> editMaterialMsdsAttachment(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> updateMaterialMsdsAttachment(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> deleteMaterialMsdsAttachment(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> viewAttachedMaterialMsdsAttachment(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> getMaterialLinkMaster(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> getMaterialAcountingProperties(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> createMaterialInventory(final Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getMaterialInventory(final int nmaterialcode, final Map<String, Object> inputMap)
			throws Exception;

	public ResponseEntity<Object> getMaterialAvailableQty(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getUraniumContentType(final UserInfo userinfo) throws Exception;
}
