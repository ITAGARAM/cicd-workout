package com.agaramtech.qualis.testgroup.service.testgrouptestmaterial;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface TestGroupTestMaterialService {
	public ResponseEntity<Object> createTestGroupTestMaterial(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveTestMaterialById(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteTestGroupTestMaterial(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateTestGroupMaterial(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> validationForRetiredTemplate(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getAvailableMaterial(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

}
