package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.BarcodeConfiguration.model.BarcodeTemplate;
import com.agaramtech.qualis.BarcodeConfiguration.service.BarcodeTemplate.BarcodeTemplateService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/barcodetemplate")
public class BarcodeTemplateController {
	private static final Log LOGGER = LogFactory.getLog(BarcodeTemplateController.class);

	private final BarcodeTemplateService objBarcodeTemplateService;
	private RequestContext requestContext;

	public BarcodeTemplateController(RequestContext requestContext, BarcodeTemplateService objBarcodeTemplateService) {
		this.requestContext = requestContext;
		this.objBarcodeTemplateService = objBarcodeTemplateService;
	}

	@PostMapping(value = "/getBarcodeTemplate")
	public ResponseEntity<Object> getBarcodeTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		Integer nbarcodetemplatecode = null;
		if (inputMap.containsKey("nbarcodetemplatecode")) {
			nbarcodetemplatecode = (Integer) inputMap.get("nbarcodetemplatecode");
		}
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBarcodeTemplateService.getBarcodeTemplate(nbarcodetemplatecode, userInfo);
	}

	@PostMapping(value = "/getBarcodeTemplateModal")
	public ResponseEntity<Object> getBarcodeTemplateModal(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeTemplateService.getBarcodeTemplateModal(userInfo);

	}

	@PostMapping(value = "/getBarcodeTemplateControl")
	public ResponseEntity<Object> getBarcodeTemplateControl(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		Integer nformcode = (Integer) inputMap.get("nformcode");
		return objBarcodeTemplateService.getBarcodeTemplateControl(nformcode, userInfo);

	}

	@PostMapping(value = "/createBarcodeTemplate")
	public ResponseEntity<Object> createBarcodeTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final BarcodeTemplate barcodeTemplate = objmapper.convertValue(inputMap.get("barcodetemplate"),
				BarcodeTemplate.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeTemplateService.createBarcodeTemplate(barcodeTemplate, userInfo);

	}

	@PostMapping(value = "/getEditBarcodeTemplateModal")
	public ResponseEntity<Object> getEditBarcodeTemplateModal(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeTemplateService.getEditBarcodeTemplateModal(inputMap, userInfo);

	}

	@PostMapping(value = "/updateBarcodeTemplate")
	public ResponseEntity<Object> updateBarcodeTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final BarcodeTemplate barcodeTemplate = objmapper.convertValue(inputMap.get("barcodetemplate"),
				BarcodeTemplate.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeTemplateService.updateBarcodeTemplate(barcodeTemplate, userInfo);

	}

	@PostMapping(value = "/approveBarcodeTemplate")
	public ResponseEntity<Object> approveBarcodeTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final BarcodeTemplate barcodeTemplate = objmapper.convertValue(inputMap.get("barcodetemplate"),
				BarcodeTemplate.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeTemplateService.approveBarcodeTemplate(barcodeTemplate, userInfo);

	}

	@PostMapping(value = "/retireBarcodeTemplate")
	public ResponseEntity<Object> retireBarcodeTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final BarcodeTemplate barcodeTemplate = objmapper.convertValue(inputMap.get("barcodetemplate"),
				BarcodeTemplate.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeTemplateService.retireBarcodeTemplate(barcodeTemplate, userInfo);

	}

	@PostMapping(value = "/deleteBarcodeTemplate")
	public ResponseEntity<Object> deleteBarcodeTemplate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final BarcodeTemplate barcodeTemplate = objmapper.convertValue(inputMap.get("barcodetemplate"),
				BarcodeTemplate.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBarcodeTemplateService.deleteBarcodeTemplate(barcodeTemplate, userInfo);

	}

}
