package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Component;
import com.agaramtech.qualis.product.service.component.ComponentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/component")
public class ComponentController {

	// private static final Logger LOGGER = LoggerFactory.getLogger(ComponentController.class);

	private RequestContext requestContext;
	private final ComponentService componentService;

	public ComponentController(RequestContext requestContext, ComponentService componentService) {
		super();
		this.requestContext = requestContext;
		this.componentService = componentService;
	}

	@PostMapping(value = "/getComponent")
	public ResponseEntity<Object> getComponent(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return componentService.getComponent(userInfo);
	}

	@PostMapping(value = "/getComponentPortal")
	public ResponseEntity<Object> getComponentPortal(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return componentService.getComponentPortal(userInfo);
	}

	@PostMapping(value = "/createComponent")
	public ResponseEntity<Object> createComponent(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Component component = objmapper.convertValue(inputMap.get("component"), Component.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return componentService.createComponent(inputMap, component, userInfo);
	}

	@PostMapping(value = "/getActiveComponentById")
	public ResponseEntity<Object> getActiveComponentById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ncomponentCode = (Integer) inputMap.get("ncomponentcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return componentService.getActiveComponentById(ncomponentCode, userInfo);
	}

	@PostMapping(value = "/updateComponent")
	public ResponseEntity<Object> updateComponent(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Component component = objmapper.convertValue(inputMap.get("component"), Component.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return componentService.updateComponent(inputMap, component, userInfo);
	}

	@PostMapping(value = "/deleteComponent")
	public ResponseEntity<Object> deleteComponent(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Component component = objmapper.convertValue(inputMap.get("component"), Component.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return componentService.deleteComponent(inputMap, component, userInfo);
	}

}
