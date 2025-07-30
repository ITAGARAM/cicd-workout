package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.emailmanagement.service.emailstatus.EmailStatusService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/emailstatus")
public class EmailStatusController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailStatusController.class);

	private final EmailStatusService emailstatusService;

	private RequestContext requestContext;

	public EmailStatusController(EmailStatusService emailstatusService, RequestContext requestContext) {
		super();
		this.emailstatusService = emailstatusService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of active emailmanagement emailhost for
	 * the specified site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active emailmanagement emailhost that
	 *         are to be listed for the specified site
	 */
	@PostMapping(value = "/getEmailStatus")
	public ResponseEntity<Object> getEmailStatus(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getEmailStatus() called");
		requestContext.setUserInfo(userInfo);
		return emailstatusService.getEmailStatus(userInfo);
	}

	@PostMapping(value = "/createreSentMail")
	public ResponseEntity<Object> reSentMail(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		Integer nrunningno = null;
		if (inputMap.containsKey("nrunningno")) {
			nrunningno = (Integer) inputMap.get("nrunningno");
		}
		Integer ncontrolcode = null;
		if (inputMap.containsKey("ncontrolcode")) {
			ncontrolcode = (Integer) inputMap.get("ncontrolcode");
		}
		requestContext.setUserInfo(userInfo);
		return emailstatusService.reSentMail(nrunningno, ncontrolcode, userInfo);
	}
}
