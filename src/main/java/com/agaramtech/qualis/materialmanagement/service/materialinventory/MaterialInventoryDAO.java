package com.agaramtech.qualis.materialmanagement.service.materialinventory;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;


public interface MaterialInventoryDAO {
	ResponseEntity<Object> getMaterialInventory(UserInfo objUserInfo) throws Exception;

	ResponseEntity<Object> getMaterialInventorycombo(Integer nmaterialtypecode, Integer nmaterialcatcode,
			UserInfo objUserInfo) throws Exception;

	ResponseEntity<Object> getMaterialInventoryByID(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getMaterialInventoryAdd(Integer nmaterialtypecode, UserInfo objUserInfo) throws Exception;

	ResponseEntity<Object> createMaterialInventory(Map<String, Object> inputMap, UserInfo objUserInfo) throws Exception;

	ResponseEntity<Object> getMaterialInventoryEdit(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> UpdateMaterialInventory(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> deleteMaterialInventory(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getMaterialInventoryDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> updateMaterialStatus(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getMaterialInventorySearchByID(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getQuantityTransaction(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getQuantityTransactionByMaterialInvCode(int nmaterialinventorycode,
			Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	Object getPrecision();

	ResponseEntity<Object> createMaterialInventoryTrans(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getMaterialInventoryLinkMaster(Map<String, Object> inputMap, UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> editMaterialInventoryFile(Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception;
	public ResponseEntity<Object> createMaterialInventoryFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception;
	public ResponseEntity<Object> updateMaterialInventoryFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception;

	ResponseEntity<Object> deleteMaterialInventoryFile(Map<String, Object> inputMap, UserInfo objUserInfo) throws Exception;
	public Map<String, Object> viewAttachedMaterialInventoryFile(Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception;
	public ResponseEntity<Object> getChildValuesMaterial(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

}
