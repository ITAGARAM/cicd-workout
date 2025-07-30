package com.agaramtech.qualis.configuration.service.modulesorting;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface ModuleSortingDAO {

	public ResponseEntity<Object> getModuleSorting(UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getActiveModuleSortingById(UserInfo userInfo, Integer nformCode)
			throws Exception;

	public ResponseEntity<Object> updateModuleSorting(UserInfo userInfo, Integer nformCode, Integer nmoduleCode,
			Integer nmenuCode) throws Exception;

}
