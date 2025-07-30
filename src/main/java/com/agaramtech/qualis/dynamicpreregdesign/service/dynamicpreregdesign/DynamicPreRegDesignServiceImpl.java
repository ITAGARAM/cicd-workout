package com.agaramtech.qualis.dynamicpreregdesign.service.dynamicpreregdesign;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.dynamicpreregdesign.model.ReactRegistrationTemplate;
import com.agaramtech.qualis.global.UserInfo;


/**
 * This class is used for Designing dynamic Pre-Register pop-up.
 * @author ATE234 Janakumar
 * @since 09-04-2025
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class DynamicPreRegDesignServiceImpl implements DynamicPreRegDesignService {

	final DynamicPreRegDesignDAO dynamicPreRegDesignDAO;

	public DynamicPreRegDesignServiceImpl(DynamicPreRegDesignDAO dynamicPreRegDesignDAO) {
		super();
		this.dynamicPreRegDesignDAO = dynamicPreRegDesignDAO;
	}
	
	/**
	 * This method is used to get the available input components such as Text Box,Combo Box etc...
	 * @param inputMap contains UserInfo
	 * @return List of input components as JSON format
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getReactComponents(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return dynamicPreRegDesignDAO.getReactComponents(inputMap,userInfo);
	}
	/**
	 * This method is used to get the available input fields created before.
	 * @param inputMap contains UserInfo
	 * @return List of available input fields created before as JSON format
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getReactInputFields(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return dynamicPreRegDesignDAO.getReactInputFields(inputMap, userInfo);
	}
	@Override
	public ResponseEntity<Object> getRegistrationTemplate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		
		return dynamicPreRegDesignDAO.getRegistrationTemplate(inputMap, userInfo);
		
	}
	@Override
	public ResponseEntity<Object> getRegistrationTemplateById(int nreactregtemplatecode, UserInfo userInfo)
			throws Exception {
		
		return dynamicPreRegDesignDAO.getRegistrationTemplateById(nreactregtemplatecode,userInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> createRegistrationTemplate(ReactRegistrationTemplate regTemplate, UserInfo userInfo)
			throws Exception {
		
		return dynamicPreRegDesignDAO.createRegistrationTemplate(regTemplate, userInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> updateRegistrationTemplate(ReactRegistrationTemplate regTemplate, UserInfo userInfo)
			throws Exception {
		
		return dynamicPreRegDesignDAO.updateRegistrationTemplate(regTemplate, userInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> deleteRegistrationTemplate(int nreactRegTemplateCode, UserInfo userInfo)
			throws Exception {
		return dynamicPreRegDesignDAO.deleteRegistrationTemplate(nreactRegTemplateCode, userInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> approveRegistrationTemplate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return dynamicPreRegDesignDAO.approveRegistrationTemplate(inputMap, userInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> copyRegistrationTemplate(ReactRegistrationTemplate regTemplate, UserInfo userInfo)
			throws Exception {
		return dynamicPreRegDesignDAO.copyRegistrationTemplate(regTemplate, userInfo);
	}
	@Override
	public ResponseEntity<Object> getComboTables(UserInfo userInfo) throws Exception {
		return null;
	}
	@Override
	public ResponseEntity<Object> getTableColumns(final String tableName,final UserInfo userInfo) throws Exception {
		return dynamicPreRegDesignDAO.getTableColumns(tableName, userInfo);
	}

	@Override
	public ResponseEntity<Object> getComboValues(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		
		return dynamicPreRegDesignDAO.getComboValues(inputMap, userInfo);
	}
	@Override
	public ResponseEntity<Object> getChildValues(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		
		return dynamicPreRegDesignDAO.getChildValues(inputMap, userInfo);
	}
	@Override
	public ResponseEntity<Object> getMasterDesign(UserInfo userInfo) throws Exception {
		
		return dynamicPreRegDesignDAO.getMasterDesign(userInfo);
	}
	@Override
	public ResponseEntity<Object> getcustomsearchfilter(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return dynamicPreRegDesignDAO.getcustomsearchfilter(inputMap,userInfo);
	}
	@Override
	public ResponseEntity<Object> getcustomsearchfilterpredefined(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		 return dynamicPreRegDesignDAO.getcustomsearchfilterpredefined(inputMap,userInfo);
	}
	@Override
	public ResponseEntity<Object> getDynamicFilterExecuteData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		 return dynamicPreRegDesignDAO.getDynamicFilterExecuteData(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> dateValidation(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		 return dynamicPreRegDesignDAO.dateValidation(inputMap,userInfo);
	}
	@Override
	public ResponseEntity<Object> getReactStaticFilterTables(Map<String, Object> inputMap, UserInfo userInfo) {
		// TODO Auto-generated method stub
		 return dynamicPreRegDesignDAO.getReactStaticFilterTables(inputMap,userInfo);
	}
	@Override
	public ResponseEntity<Object> validatePreview(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		 return dynamicPreRegDesignDAO.validatePreview(inputMap,userInfo);
	}
	@Override
	public ResponseEntity<Object> getExternalportalDetail(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		 return dynamicPreRegDesignDAO.getExternalportalDetail(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getDefaultTemplate(Map<String, Object> inputMap, UserInfo userInfo) throws Exception
	{
		return dynamicPreRegDesignDAO.getDefaultTemplate(inputMap, userInfo);
	}
	public ResponseEntity<Object> getDefaultSampleType( Map<String, Object> inputMap,UserInfo userInfo) throws Exception
	{
		return dynamicPreRegDesignDAO.getDefaultSampleType( inputMap,userInfo);
	}
	@Transactional
	public ResponseEntity<Object> createImportTemplate(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		return dynamicPreRegDesignDAO.createImportTemplate( request, userInfo);
	}

}
