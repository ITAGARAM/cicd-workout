package com.agaramtech.qualis.testgroup.service.testgrouprulesengine;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'sqlquery' table through its DAO layer.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 05- OCT- 2020
 */
public interface TestGroupRulesEngineService {
	public ResponseEntity<Object> getTestGroupRulesEngine(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getEditTestGroupRulesEngine(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTestGroupRulesEngineAdd(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSelectedTestGroupRulesEngine(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> approveTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getParameterResultValue(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getParameterRulesEngine(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateExecutionOrder(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getProfileRoot(UserInfo userInfo, int nproductcatcode, int nproductcode,
			int ntestcode) throws Exception;

	public ResponseEntity<Object> getSpecification(UserInfo userInfo, int ntemplatemanipulationcode, int ntestcode)
			throws Exception;

	public ResponseEntity<Object> getComponent(UserInfo userInfo, int nallottedspeccode, int ncomponentcode,
			int ntestcode) throws Exception;

	public ResponseEntity<Object> getTestBasedOnRules(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> copyTestGroupRulesEngine(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getProductCategory(UserInfo userInfo, final int ntestcode,
			final int ncategorybasedflow) throws Exception;

	public ResponseEntity<Object> getProductByProductCat(UserInfo userInfo, int nproductcatcode) throws Exception;
}
