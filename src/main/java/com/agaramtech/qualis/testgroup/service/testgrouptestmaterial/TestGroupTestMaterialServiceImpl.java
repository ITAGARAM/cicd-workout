package com.agaramtech.qualis.testgroup.service.testgrouptestmaterial;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class TestGroupTestMaterialServiceImpl implements TestGroupTestMaterialService {

	private TestGroupTestMaterialDAO testGroupTestMaterialDAO;

	public TestGroupTestMaterialServiceImpl(TestGroupTestMaterialDAO testGroupTestMaterialDAO) {
		this.testGroupTestMaterialDAO = testGroupTestMaterialDAO;
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createTestGroupTestMaterial(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testGroupTestMaterialDAO.createTestGroupTestMaterial(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveTestMaterialById(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testGroupTestMaterialDAO.getActiveTestMaterialById(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> deleteTestGroupTestMaterial(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testGroupTestMaterialDAO.deleteTestGroupTestMaterial(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateTestGroupMaterial(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testGroupTestMaterialDAO.updateTestGroupMaterial(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> validationForRetiredTemplate(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testGroupTestMaterialDAO.validationForRetiredTemplate(inputMap, userInfo);
	}

	public ResponseEntity<Object> getAvailableMaterial(Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {
		return testGroupTestMaterialDAO.getAvailableMaterial(inputMap, objUserInfo);
	}
}
