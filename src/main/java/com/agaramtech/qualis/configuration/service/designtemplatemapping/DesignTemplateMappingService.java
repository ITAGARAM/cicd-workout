package com.agaramtech.qualis.configuration.service.designtemplatemapping;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.global.UserInfo;

public interface DesignTemplateMappingService {

	public ResponseEntity<Object> getDesignTemplateMapping(int nsampletypecode, int nregtypecode, int nregsubtypecode,
			int ndesigntemplatemappingcode, short formCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDynamicPreRegDesign(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTemplateRegType(int nregtypecode, int nsampletypecode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createDesignTemplateMapping(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> approveDesignTemplateMapping(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getDesignTemplateMappingById(int ndesigntemplatemappingcode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteDesignTemplateMapping(DesignTemplateMapping designtemplatemapping,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getMappedFieldProps(DesignTemplateMapping designtemplatemapping, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTemplateRegSubType(int nregtypecode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> configureDesignTemplateMapping(DesignTemplateMapping designtemplatemapping,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getAuditMappedFieldProps(DesignTemplateMapping designtemplatemapping,
			UserInfo userInfo, Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> configureDynamicAudit(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateConfigureSendToStore(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getMappedfield(final int ndesigntemplatemappingcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getConfigureCheckList(int ndesigntemplatemappingcode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getConfigureCheckListLatestVersion(Map<String, Object> inputMap, UserInfo userInfo);

	public ResponseEntity<Object> getReleaseSampleFilterFields(UserInfo userInfo) throws Exception;

}
