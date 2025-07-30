package com.agaramtech.qualis.configuration.service.sorting;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation through its DAO layer.
 */

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class SortingServiceImpl implements SortingService {
	
	private final SortingDAO sortingDAO;
	
	/**
	 * This method is used to retrieve list of all active forms and modules for the
	 * specified language.
	 * 
	 * @param userInfo [UserInfo] primary key of language object for which the list
	 *                 is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         forms and modules
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getSorting(UserInfo userInfo, Integer boolValue) throws Exception {
		return sortingDAO.getSorting(userInfo, boolValue);
	}

	@Override
	public ResponseEntity<Object> getFilter(UserInfo userInfo, Integer nmenuCode, Integer boolValue) throws Exception {
		return sortingDAO.getFilter(userInfo, nmenuCode, boolValue);
	}

	@Override
	public ResponseEntity<Object> getFilter1(UserInfo userInfo, Integer nmenuCode, Integer nmoduleCode,
			Integer boolValue) throws Exception {
		return sortingDAO.getFilter1(userInfo, nmenuCode, nmoduleCode, boolValue);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateForms(UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		return sortingDAO.updateForms(userInfo, inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateModules(UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		return sortingDAO.updateModules(userInfo, inputMap);
	}

}
