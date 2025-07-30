package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.credential.service.site.SiteService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/site")
public class SiteController {
	private static final Log LOGGER = LogFactory.getLog(SiteController.class);

	private SiteService siteService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param siteService    SiteService
	 */
	public SiteController(SiteService siteService, RequestContext requestContext) {
		super();
		this.siteService = siteService;
		this.requestContext = requestContext;
	}

	@RequestMapping(value = "/getSite", method = RequestMethod.POST)
	public ResponseEntity<Object> getSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return siteService.getSite(userInfo);
	}

	@RequestMapping(value = "/getSiteForFTP", method = RequestMethod.POST)
	public ResponseEntity<Object> getSiteForFTP(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteService.getSiteForFTP();

	}

	@RequestMapping(value = "/getSiteScreen", method = RequestMethod.POST)
	public ResponseEntity<Object> getSiteScreen(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();

		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return siteService.getSiteScreen(userInfo);

	}

	@RequestMapping(value = "/createSiteScreen", method = RequestMethod.POST)
	public ResponseEntity<Object> createSiteScreen(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteService.createSiteScreen(inputMap, userInfo);
	}

	@RequestMapping(value = "/deleteSiteScreen", method = RequestMethod.POST)
	public ResponseEntity<Object> deleteSiteScreen(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Site objSite = objMapper.convertValue(inputMap.get("site"), new TypeReference<Site>() {
		});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteService.deleteSiteScreen(objSite, userInfo);
	}

	@RequestMapping(value = "/getActiveSiteById", method = RequestMethod.POST)
	public ResponseEntity<Object> getActiveSiteById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nsiteCode = (Integer) inputMap.get("nsitecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return siteService.getActiveSiteById(nsiteCode, userInfo);
	}

	@RequestMapping(value = "/updateSiteScreen", method = RequestMethod.POST)
	public ResponseEntity<Object> updateSiteScreen(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return siteService.updateSiteScreen(inputMap, userInfo);

	}

	@RequestMapping(value = "/getDateFormat", method = RequestMethod.POST)
	public ResponseEntity<Object> getDateFormat(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return siteService.getDateFormat(userInfo);
	}

	@RequestMapping(value = "/getDistrict", method = RequestMethod.POST)
	public ResponseEntity<Object> getDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nregioncode = (int) inputMap.get("nregioncode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return siteService.getDistrict(nregioncode, userInfo);
	}

}
