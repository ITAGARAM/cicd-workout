package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.service.linkmaster.LinkMasterService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/linkmaster")
public class LinkMasterController {

	private static final Log LOGGER = LogFactory.getLog(LinkMasterController.class);

	// private RequestContext requestContext;
	private final LinkMasterService linkMasterService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param LinkMasterService linkMasterService
	 */
	public LinkMasterController(LinkMasterService linkMasterService) {
		super();
		this.linkMasterService = linkMasterService;
	}

	/**
	 * This method is used to retrieve list of available linkMaster.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         linkMasters files
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getLinkMaster")
	public ResponseEntity<Object> getLinkMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		try {
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			return linkMasterService.getLinkMaster(objUserInfo);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return new ResponseEntity<Object>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
