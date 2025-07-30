package com.agaramtech.qualis.configuration.service.modulesorting;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class ModuleSortingServiceImpl implements ModuleSortingService {

	private final ModuleSortingDAO moduleSortingDAO;

	@Override
	public ResponseEntity<Object> getModuleSorting(UserInfo userInfo) throws Exception {
		return moduleSortingDAO.getModuleSorting(userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveModuleSortingById(UserInfo userInfo, Integer nformCode)
			throws Exception {
		return moduleSortingDAO.getActiveModuleSortingById(userInfo, nformCode);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateModuleSorting(UserInfo userInfo, Integer nformCode, Integer nmoduleCode,
			Integer nmenuCode) throws Exception {
		return moduleSortingDAO.updateModuleSorting(userInfo, nformCode, nmoduleCode, nmenuCode);
	}

}
