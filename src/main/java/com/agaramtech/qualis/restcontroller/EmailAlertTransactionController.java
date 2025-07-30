package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.emailmanagement.service.emailalerttransaction.EmailAlertTransactionService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/emailalerttransaction")
public class EmailAlertTransactionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailAlertTransactionController.class);
	private final EmailAlertTransactionService emailalerttransactionService;
	private RequestContext requestContext;

	public EmailAlertTransactionController(EmailAlertTransactionService emailalerttransactionService,
			RequestContext requestContext) {
		super();
		this.emailalerttransactionService = emailalerttransactionService;
		this.requestContext = requestContext;
	}

	@PostMapping(value = "/getEmailAlertTransaction")
	public ResponseEntity<Object> getEmailAlertTransaction(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		LOGGER.info("getEmailAlertTransaction() called");
		final String currentUIDate = (String) inputMap.get("currentdate");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return emailalerttransactionService.getEmailAlertTransaction(fromDate, toDate, currentUIDate, userInfo);
	}
}
