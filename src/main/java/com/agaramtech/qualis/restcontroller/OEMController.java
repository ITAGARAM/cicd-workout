package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.quotation.model.OEM;
import com.agaramtech.qualis.quotation.service.oem.OEMService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Unit Service methods
 * 
 * @author ATE172
 * @version 9.0.0.1
 */
@RestController
@RequestMapping("/oem")
public class OEMController {

	private RequestContext requestContext;
	private final OEMService oemService;

	public OEMController(RequestContext requestContext, OEMService oemService) {
		super();
		this.requestContext = requestContext;
		this.oemService = oemService;
	}

	@PostMapping(value = "/getOEM")
	public ResponseEntity<Object> getOEM(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return oemService.getOEM(userInfo);
	}
	
	@PostMapping(value = "/createOEM")
	public ResponseEntity<Object> createOEM(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final OEM oem = objmapper.convertValue(inputMap.get("oem"),
				OEM.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return oemService.createOEM(oem, userInfo);
	}

	@PostMapping(value = "/getActiveOEMById")
	public ResponseEntity<Object> getActiveOEMById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int noemcode = (Integer) inputMap.get("noemcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return oemService.getActiveOEMById(noemcode, userInfo);
	}
	
	
	@PostMapping(value = "/updateOEM")
	public ResponseEntity<Object> updateOEM(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final OEM oem = objmapper.convertValue(inputMap.get("oem"),
				OEM.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return oemService.updateOEM(oem, userInfo);
	}
	
	@PostMapping(value = "/deleteOEM")
	public ResponseEntity<Object> deleteOEM(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final OEM oem = objmapper.convertValue(inputMap.get("oem"),
				OEM.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		
		return oemService.deleteOEM(oem, userInfo);
	}
}
