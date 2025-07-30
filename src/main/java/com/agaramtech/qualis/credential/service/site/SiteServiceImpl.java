package com.agaramtech.qualis.credential.service.site;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SiteServiceImpl implements SiteService {

	private SiteDAO siteDAO;
	CommonFunction commonFunction;

	public SiteServiceImpl(SiteDAO siteDAO, CommonFunction commonFunction) {
		this.siteDAO = siteDAO;
		this.commonFunction = commonFunction;
	}

	@Override
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception {
		return siteDAO.getSite(userInfo);
	}

	public ResponseEntity<Object> getSiteForFTP() throws Exception {
		return siteDAO.getSiteForFTP();
	}

	@Override
	public ResponseEntity<Object> getSiteScreen(final UserInfo userInfo) throws Exception {
		return siteDAO.getSiteScreen(userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createSiteScreen(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return siteDAO.createSiteScreen(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> deleteSiteScreen(Site objSite, UserInfo userInfo) throws Exception {
		return siteDAO.deleteSiteScreen(objSite, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveSiteById(final int nsitecode, final UserInfo userInfo) throws Exception {
		final Site site = (Site) siteDAO.getActiveSiteById(nsitecode, userInfo);
		if (site == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			return new ResponseEntity<>(site, HttpStatus.OK);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateSiteScreen(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		return siteDAO.updateSiteScreen(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getDateFormat(final UserInfo userInfo) throws Exception {

		return siteDAO.getDateFormat(userInfo);
	}

	public ResponseEntity<Object> getDistrict(final int nregioncode, final UserInfo userInfo) throws Exception {
		return siteDAO.getDistrict(nregioncode, userInfo);
	}
}
