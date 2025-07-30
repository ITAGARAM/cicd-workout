package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.service.chainofcustody.ChainofCustodyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/chainofcustody")
public class ChainofCustodyController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChainofCustodyController.class);

	private RequestContext requestContext;
	private final ChainofCustodyService chainofCustodyService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext        RequestContext to hold the request
	 * @param chainofCustodyService ChainofCustodyService
	 */
	public ChainofCustodyController(RequestContext requestContext, ChainofCustodyService chainofCustodyService) {
		super();
		this.requestContext = requestContext;
		this.chainofCustodyService = chainofCustodyService;
	}

	@PostMapping(value = "/getChainofCustody")
	public ResponseEntity<Object> getChainofCustody(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getChainofCustody()");
		final ObjectMapper objMapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return chainofCustodyService.getChainofCustody(fromDate, toDate, currentUIDate, userInfo);
	}
}