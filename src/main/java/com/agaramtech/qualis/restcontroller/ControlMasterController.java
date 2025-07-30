package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.service.controlmaster.ControlMasterService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the Control Master Service methods.
 */
@RestController
@RequestMapping("/controlmaster")
public class ControlMasterController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CountryController.class);

	private final ControlMasterService controlMasterService;
	private RequestContext requestContext; 
	
	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param controlMasterService ControlMasterService
	 */
	public ControlMasterController(RequestContext requestContext, ControlMasterService controlMasterService) {
		super();
		this.requestContext = requestContext;
		this.controlMasterService = controlMasterService;
	}
	/**
	 * This method is used to retrieve list of available control master data.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1, "nformcode": 43}}
	 * @return response entity object holding response status and list of all
	 *         countries
	 * @throws Exception exception
	 */	
@PostMapping(value = "/getUploadControlsByFormCode")
public ResponseEntity<List<ControlMaster>> getUploadControlsByFormCode(@RequestBody Map<String, Object> inputMap) throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
	requestContext.setUserInfo(userInfo);
	LOGGER.info("At getUploadControlsByFormCode");
	return controlMasterService.getUploadControlsByFormCode(userInfo);
}
}
