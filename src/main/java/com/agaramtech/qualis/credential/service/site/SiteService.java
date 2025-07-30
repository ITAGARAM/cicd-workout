package com.agaramtech.qualis.credential.service.site;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.UserInfo;

public interface SiteService {

	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSiteForFTP() throws Exception;

	public ResponseEntity<Object> getSiteScreen(final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createSiteScreen(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSiteScreen(Site objSite, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getActiveSiteById(int nsitecode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateSiteScreen(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDateFormat(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDistrict(final int nregioncode, final UserInfo userInfo) throws Exception;

}
