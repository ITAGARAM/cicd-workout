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
import com.agaramtech.qualis.login.model.UserUiConfig;
import com.agaramtech.qualis.login.service.useruiconfig.UserUiConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/useruiconfig")
public class UserUiConfigConroller {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserUiConfigConroller.class);

	private RequestContext requestContext;
	private final ObjectMapper objMapper = new ObjectMapper();
	private final UserUiConfigService objUserUiConfigService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext         RequestContext to hold the request
	 * @param objUserUiConfigService UserUiConfigService
	 */
	public UserUiConfigConroller(RequestContext requestContext, UserUiConfigService objUserUiConfigService) {
		super();
		this.requestContext = requestContext;
		this.objUserUiConfigService = objUserUiConfigService;
	}

	@PostMapping(path = "/getcolormastertheme")
	public ResponseEntity<Object> getcolormastertheme(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getcolormastertheme");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return objUserUiConfigService.getcolormastertheme(userInfo);
	}

	/**
	 * @param userlimstheme
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	@PostMapping(path = "/createUserUiConfig")
	public ResponseEntity<Object> createUserUiConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final UserUiConfig useruiconfig = objMapper.convertValue(inputMap.get("useruiconfig"), UserUiConfig.class);
		return objUserUiConfigService.createUserUiConfig(useruiconfig, userInfo);
	}

	/**
	 * @param userlimstheme
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	@PostMapping(path = "/updateUserUiConfig")
	public ResponseEntity<Object> updateUserUiConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final UserUiConfig userlimstheme = objMapper.convertValue(inputMap.get("useruiconfig"), UserUiConfig.class);
		return objUserUiConfigService.updateUserUiConfig(userlimstheme, userInfo);
	}

	/**
	 * @param userlimstheme
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	@PostMapping(path = "/getActiveuserlimsthemeById")
	public ResponseEntity<Object> getActiveuserlimsthemeById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return objUserUiConfigService.getActiveuseruiconfigById(userInfo.getNusercode(), userInfo);
	}
}