package com.agaramtech.qualis.testgroup.service.testgrouprulesengine;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'sqlquery' table
 * through its DAO layer.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 05- OCT- 2020
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class TestGroupRulesEngineServiceImpl implements TestGroupRulesEngineService {

	private TestGroupRulesEngineDAO testgrouprulesEngineDAO;

	public TestGroupRulesEngineServiceImpl(TestGroupRulesEngineDAO testgrouprulesEngineDAO) {
		this.testgrouprulesEngineDAO = testgrouprulesEngineDAO;
	}

	@Override
	public ResponseEntity<Object> getTestGroupRulesEngine(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.getTestGroupRulesEngine(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditTestGroupRulesEngine(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.getEditTestGroupRulesEngine(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestGroupRulesEngineAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.getTestGroupRulesEngineAdd(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		return testgrouprulesEngineDAO.createTestGroupRulesEngine(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSelectedTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.getSelectedTestGroupRulesEngine(inputMap, userInfo);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.updateTestGroupRulesEngine(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approveTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.approveTestGroupRulesEngine(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.deleteTestGroupRulesEngine(inputMap, userInfo);

	}

	@Override
	public ResponseEntity<Object> getParameterResultValue(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.getParameterResultValue(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getParameterRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.getParameterRulesEngine(inputMap, userInfo);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateExecutionOrder(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.updateExecutionOrder(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getProfileRoot(UserInfo userInfo, int nproductcatcode, int nproductcode,
			int ntestcode) throws Exception {
		return testgrouprulesEngineDAO.getProfileRoot(userInfo, nproductcatcode, nproductcode, ntestcode);
	}

	@Override
	public ResponseEntity<Object> getSpecification(UserInfo userInfo, int ntemplatemanipulationcode, int ntestcode)
			throws Exception {
		return testgrouprulesEngineDAO.getSpecification(userInfo, ntemplatemanipulationcode, ntestcode);
	}

	@Override
	public ResponseEntity<Object> getComponent(UserInfo userInfo, int nallottedspeccode, int ncomponentcode,
			int ntestcode) throws Exception {
		return testgrouprulesEngineDAO.getComponent(userInfo, nallottedspeccode, ncomponentcode, ntestcode);
	}

	@Override
	public ResponseEntity<Object> getTestBasedOnRules(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.getTestBasedOnRules(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> copyTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return testgrouprulesEngineDAO.copyTestGroupRulesEngine(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getProductCategory(UserInfo userInfo, final int ntestcode,
			final int ncategorybasedflow) throws Exception {
		return testgrouprulesEngineDAO.getProductCategory(userInfo, ntestcode, ncategorybasedflow);
	}

	@Override
	public ResponseEntity<Object> getProductByProductCat(UserInfo userInfo, int nproductcatcode) throws Exception {
		return testgrouprulesEngineDAO.getProductByProductCat(userInfo, nproductcatcode);
	}
}
