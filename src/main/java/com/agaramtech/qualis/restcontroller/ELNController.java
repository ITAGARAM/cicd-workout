package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.eln.service.ELNService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/eln")
public class ELNController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ELNController.class);

	private RequestContext requestContext;
	private final ELNService elnService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param cityService    ELNService
	 */
	public ELNController(RequestContext requestContext, ELNService elnService) {
		super();
		this.requestContext = requestContext;
		this.elnService = elnService;
	}

	@PostMapping(value = "/getLimsSDMSRecords")
	public ResponseEntity<Object> getLimsSDMSRecords(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getLimsSDMSRecords()");
		final ObjectMapper objMapper = new ObjectMapper();
		final String nsitecode = (String) inputMap.get("elnsitecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return elnService.getLimsSDMSRecords(nsitecode);
	}

	@PostMapping(value = "/updateLimsSDMSInprogress")
	public String updateLimsSDMSInprogress(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final String ntransactionresultcode = (String) inputMap.get("limsprimarycode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return elnService.updateLimsSDMSInprogress(ntransactionresultcode);
	}

	@PostMapping(value = "/updateLimsSDMSComplete")
	public String updateLimsSDMSComplete(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return elnService.updateLimsSDMSComplete(inputMap);
	}

	@PostMapping(value = "/getLimsTests")
	public ResponseEntity<Object> getLimsTests(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return elnService.getLimsTests();
	}

	@PostMapping(value = "/getLimsTestParameters")
	public ResponseEntity<Object> getLimsTestParameters(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return elnService.getLimsTestParameters();
	}

	@PostMapping(value = "/getLimsInstrument")
	public ResponseEntity<Object> getLimsInstrument(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int nsitecode = (int) inputMap.get("nsitecode");
		return elnService.getLimsInstrument(nsitecode);
	}

	@PostMapping(value = "/getLimsMaterial")
	public ResponseEntity<Object> getLimsMaterial(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int nsitecode = (int) inputMap.get("nsitecode");
		return elnService.getLimsMaterial(nsitecode);
	}
}
