package com.agaramtech.qualis.dynamicmaster.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.dynamicmaster.model.DynamicMaster;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This Service interface is used to access the Dynamic Master to fetch details
 * from the 'dynamicmaster' table through DynamicMaster Entity relevant to the
 * input request.
 * 
 * @author ATE234
 * @version 1.0.0
 * @since 10-04- 2025
 */
public interface DynamicMasterService {

	public ResponseEntity<Object> getDynamicMaster(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getMasterDesign(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createDynamicMaster(final MultipartHttpServletRequest inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveDynamicMasterById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateDynamicMaster(final MultipartHttpServletRequest inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteDynamicMaster(final DynamicMaster dynamicMaster, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> importDynamicMaster(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface is used to get the list of active entries available in the
	 * specified form/screen based on the provided screen name through its
	 * implementation method.
	 * 
	 * @param formName [String] name of the screen/form
	 * @return Returns the entries list available in the specified screen
	 * @throws Exception when valid input is not provided or some error thrown in
	 *                   query execution
	 * 
	 */
	public ResponseEntity<Object> getDynamicMasterByForm(final String formName) throws Exception;

	public Map<String, Object> viewDynamicMaster(final DynamicMaster objFile, final UserInfo objUserInfo,
			final Map<String, Object> inputMap) throws Exception;
}