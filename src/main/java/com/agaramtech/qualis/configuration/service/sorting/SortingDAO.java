package com.agaramtech.qualis.configuration.service.sorting;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation.
 */
public interface SortingDAO {

	public ResponseEntity<Object> getSorting(UserInfo userInfo, Integer boolValue) throws Exception;

	public ResponseEntity<Object> getFilter(UserInfo userInfo, Integer nmenuCode, Integer boolValue) throws Exception;

	public ResponseEntity<Object> getFilter1(UserInfo userInfo, Integer nmenuCode, Integer nmoduleCode,
			Integer boolValue) throws Exception;

	public ResponseEntity<Object> updateForms(UserInfo userInfo, Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateModules(UserInfo userInfo, Map<String, Object> inputMap) throws Exception;

}
