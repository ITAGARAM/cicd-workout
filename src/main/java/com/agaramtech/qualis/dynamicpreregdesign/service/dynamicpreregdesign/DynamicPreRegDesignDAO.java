package com.agaramtech.qualis.dynamicpreregdesign.service.dynamicpreregdesign;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.dashboard.model.QueryBuilderTables;
import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.global.UserInfo;


/**
 * This interface is used for Designing dynamic Pre-Register pop-up.
 * 
 * @author ATE234Jankumar
 * @since 10-04-2025
 */
public interface DynamicPreRegDesignDAO {
	/**
	 * This method is used to get the available input components such as Text
	 * Box,Combo Box etc...
	 * 
	 * @param inputMap contains UserInfo
	 * @return List of input components as JSON format
	 * @throws Exception
	 */
	public ResponseEntity<Object> getReactComponents(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This method is used to get the available input fields created before.
	 * 
	 * @param inputMap contains UserInfo
	 * @return List of available input fields created before as JSON format
	 * @throws Exception
	 */
	public ResponseEntity<Object> getReactInputFields(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationTemplate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getRegistrationTemplateById(int nreactregtemplatecode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createRegistrationTemplate(ReactRegistrationTemplate regTemplate, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateRegistrationTemplate(ReactRegistrationTemplate regTemplate, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteRegistrationTemplate(int nreactRegTemplateCode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> approveRegistrationTemplate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> copyRegistrationTemplate(ReactRegistrationTemplate regTemplate, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getComboValues(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getChildValues(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public List<QueryBuilderTables> getComboTables(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTableColumns(String tableName, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getMasterDesign(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getComboValuesForEdit(Map<String, Object> map,Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getcustomsearchfilter(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getcustomsearchfilterpredefined (Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getDynamicFilterExecuteData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> dateValidation(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReactStaticFilterTables(Map<String, Object> inputMap, UserInfo userInfo);

	public ResponseEntity<Object> validatePreview(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getExternalportalDetail(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDefaultTemplate(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getDefaultSampleType( Map<String, Object> inputMap,UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> createImportTemplate(MultipartHttpServletRequest inputMap, UserInfo userInfo) throws Exception;

	

}
