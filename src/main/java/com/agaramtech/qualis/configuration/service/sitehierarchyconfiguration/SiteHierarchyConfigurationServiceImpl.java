package com.agaramtech.qualis.configuration.service.sitehierarchyconfiguration;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.configuration.model.SiteHierarchyConfig;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SiteHierarchyConfigurationServiceImpl implements SiteHierarchyConfigurationService {

	private final SiteHierarchyConfigurationDAO siteHierarchyConfigurationDAO;
	final CommonFunction commonFunction;

	public SiteHierarchyConfigurationServiceImpl(SiteHierarchyConfigurationDAO siteHierarchyConfigurationDAO,
			CommonFunction commonFunction) {
		this.siteHierarchyConfigurationDAO = siteHierarchyConfigurationDAO;
		this.commonFunction = commonFunction;

	}

	@Override
	public ResponseEntity<Object> getSiteHierarchyConfiguration(final UserInfo userInfo, int nsitehierarchyconfigcode)
			throws Exception {
		return siteHierarchyConfigurationDAO.getSiteHierarchyConfiguration(userInfo, nsitehierarchyconfigcode);
	}

	@Override
	public ResponseEntity<Object> getSiteHierarchy(final UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		return siteHierarchyConfigurationDAO.getSiteHierarchy(userInfo, inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createSiteHierarchyConfig(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return siteHierarchyConfigurationDAO.createSiteHierarchyConfig(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approveSiteHierarchyConfig(final UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception {
		return siteHierarchyConfigurationDAO.approveSiteHierarchyConfig(userInfo, inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteSiteHierarchyConfig(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return siteHierarchyConfigurationDAO.deleteSiteHierarchyConfig(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveSiteHierarchyConfigById(int nsitehierarchyconfigcode,
			final UserInfo userInfo) throws Exception {
		final SiteHierarchyConfig objSiteHierarchyConfig = siteHierarchyConfigurationDAO
				.getActiveSiteHierarchyConfigById(nsitehierarchyconfigcode, userInfo);
		if (objSiteHierarchyConfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objSiteHierarchyConfig, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateSiteHierarchyConfig(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return siteHierarchyConfigurationDAO.updateSiteHierarchyConfig(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> retireSiteHierarchyConfig(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return siteHierarchyConfigurationDAO.retireSiteHierarchyConfig(inputMap, userInfo);
	}

}
