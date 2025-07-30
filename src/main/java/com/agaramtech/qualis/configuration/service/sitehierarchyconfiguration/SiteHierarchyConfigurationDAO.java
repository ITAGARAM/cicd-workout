package com.agaramtech.qualis.configuration.service.sitehierarchyconfiguration;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.configuration.model.SiteHierarchyConfig;
import com.agaramtech.qualis.global.UserInfo;

public interface SiteHierarchyConfigurationDAO {

	public ResponseEntity<Object> getSiteHierarchyConfiguration(final UserInfo userInfo, int nsitehierarchyconfigcode)
			throws Exception;

	public ResponseEntity<Object> getSiteHierarchy(final UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;

	public ResponseEntity<Object> createSiteHierarchyConfig(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteSiteHierarchyConfig(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> approveSiteHierarchyConfig(UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;

	public SiteHierarchyConfig getActiveSiteHierarchyConfigById(final int nsitehierarchyconfigcode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateSiteHierarchyConfig(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> retireSiteHierarchyConfig(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

}
