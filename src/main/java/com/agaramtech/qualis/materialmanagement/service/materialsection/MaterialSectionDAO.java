package com.agaramtech.qualis.materialmanagement.service.materialsection;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface MaterialSectionDAO {

	public ResponseEntity<Object> createMaterialSection(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Map<String, Object>> getMaterialSectionByMaterialCode(final int nmaterialcode,
			final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> deleteMaterialSection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveMaterialSectionById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateMaterialSection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

}