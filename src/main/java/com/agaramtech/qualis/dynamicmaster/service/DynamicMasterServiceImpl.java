package com.agaramtech.qualis.dynamicmaster.service;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.dynamicmaster.model.DynamicMaster;
import com.agaramtech.qualis.dynamicpreregdesign.service.dynamicpreregdesign.DynamicPreRegDesignDAO;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This Service class is used to access the Dynamic Master to fetch details from
 * the 'dynamicmaster' table through DynamicMaster Entity relevant to the input
 * request.
 * 
 * @author ATE234
 * @version 1.0.0
 * @since 10-04- 2025
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class DynamicMasterServiceImpl implements DynamicMasterService {

	private final DynamicMasterDAO dynamicMasterDAO;
	private final DynamicPreRegDesignDAO dynamicPreRegDesignDAO;

	public DynamicMasterServiceImpl(DynamicMasterDAO dynamicMasterDAO, DynamicPreRegDesignDAO dynamicPreRegDesignDAO) {
		super();
		this.dynamicMasterDAO = dynamicMasterDAO;
		this.dynamicPreRegDesignDAO = dynamicPreRegDesignDAO;
	}

	@Override
	public ResponseEntity<Object> getDynamicMaster(final UserInfo userInfo) throws Exception {
		return dynamicMasterDAO.getDynamicMaster(userInfo);
	}

	@Override
	public ResponseEntity<Object> getMasterDesign(final UserInfo userInfo) throws Exception {
		return dynamicMasterDAO.getMasterDesign(userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createDynamicMaster(final MultipartHttpServletRequest inputMap,
			final UserInfo userInfo) throws Exception {
		return dynamicMasterDAO.createDynamicMaster(inputMap, userInfo);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getActiveDynamicMasterById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> map = (Map<String, Object>) dynamicMasterDAO
				.getActiveDynamicMasterById(inputMap, userInfo).getBody();
		map.putAll((Map<? extends String, ? extends Object>) dynamicPreRegDesignDAO
				.getComboValuesForEdit(map, inputMap, userInfo).getBody());
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateDynamicMaster(final MultipartHttpServletRequest inputMap,
			final UserInfo userInfo) throws Exception {
		return dynamicMasterDAO.updateDynamicMaster(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> deleteDynamicMaster(final DynamicMaster dynamicMaster, final UserInfo userInfo)
			throws Exception {
		return dynamicMasterDAO.deleteDynamicMaster(dynamicMaster, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> importDynamicMaster(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		return dynamicMasterDAO.importDynamicMaster(request, userInfo);
	}

	/**
	 * This method is used to get the list of active entries available in the
	 * specified form/screen based on the provided screen name.
	 * 
	 * @param formName [String] name of the screen/form
	 * @return Returns the entries list available in the specified screen
	 * @throws Exception when valid input is not provided or some error thrown in
	 *                   query execution
	 */
	@Override
	public ResponseEntity<Object> getDynamicMasterByForm(final String formName) throws Exception {
		return dynamicMasterDAO.getDynamicMasterByForm(formName);
	}

	@Override
	public Map<String, Object> viewDynamicMaster(final DynamicMaster objFile, final UserInfo objUserInfo,
			final Map<String, Object> inputMap) throws Exception {
		return dynamicMasterDAO.viewDynamicMaster(objFile, objUserInfo, inputMap);
	}
}