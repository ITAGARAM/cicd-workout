package com.agaramtech.qualis.materialmanagement.service.materialsection;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class MaterialSectionServiceImpl implements MaterialSectionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialSectionServiceImpl.class);
	private final MaterialSectionDAO objMaterialSectionDAO;

	@Transactional
	@Override
	public ResponseEntity<Object> createMaterialSection(final Map<String, Object> inputMap) throws Exception {
		try {
			return objMaterialSectionDAO.createMaterialSection(inputMap);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteMaterialSection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return objMaterialSectionDAO.deleteMaterialSection(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveMaterialSectionById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return objMaterialSectionDAO.getActiveMaterialSectionById(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateMaterialSection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return objMaterialSectionDAO.updateMaterialSection(inputMap, userInfo);
	}
}