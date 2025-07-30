package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.digitalsignature.service.DigitalSignatureService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the DigitalSignature Service methods
 * 
 * @author ATE219
 * @version 9.0.0.1
 *
 */
@RestController
@RequestMapping("/digitalsignature")
public class DigitalSignatureController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CityController.class);

	private RequestContext requestContext;
	private final DigitalSignatureService digitalSignatureService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext          RequestContext to hold the request
	 * @param digitalSignatureService DigitalSignatureService
	 */
	public DigitalSignatureController(RequestContext requestContext, DigitalSignatureService digitalSignatureService) {
		super();
		this.requestContext = requestContext;
		this.digitalSignatureService = digitalSignatureService;
	}

	/**
	 * This method is used to get Initial and filter change get for usermapping
	 * screen along with filter data
	 * 
	 * @param inputMap contains values of
	 *                 flag,approvalsubtypecode,regtypecode,regsubtypecode and
	 *                 userroletemplateversioncode
	 * @return ResponseEntity of site and user mapping Data with Http Status
	 */
	@PostMapping(value = "/getDigitalSignature")
	public ResponseEntity<Object> getDigitalSignature(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getDigitalSignature");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return digitalSignatureService.getDigitalSignature(inputMap, userInfo);
	}

	@PostMapping(value = "/updateDigitalSignature")
	public ResponseEntity<Object> updateDigitalSignature(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return digitalSignatureService.updateDigitalSignature(request, userInfo);
	}
}