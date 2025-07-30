package com.agaramtech.qualis.configuration.service.modulesorting;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface ModuleSortingService {

	public ResponseEntity<Object> getModuleSorting(UserInfo userinfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getActiveModuleSortingById(UserInfo userinfo, Integer nformcode)
			throws Exception;

	public ResponseEntity<Object> updateModuleSorting(UserInfo userinfo, Integer nformcode, Integer nmodulecode,
			Integer nmenucode) throws Exception;

}
