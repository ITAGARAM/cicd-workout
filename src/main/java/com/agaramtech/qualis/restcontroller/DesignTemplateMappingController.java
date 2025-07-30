package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.configuration.service.designtemplatemapping.DesignTemplateMappingService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping(value = "designtemplatemapping")
public class DesignTemplateMappingController {

	private final DesignTemplateMappingService designtemplatemappingService;
	private RequestContext requestContext;

	public DesignTemplateMappingController(RequestContext requestContext,
			DesignTemplateMappingService designtemplatemappingService) {
		super();
		this.requestContext = requestContext;
		this.designtemplatemappingService = designtemplatemappingService;
	}

	@PostMapping(path = "getDesignTemplateMapping")
	public ResponseEntity<Object> getDesignTemplateMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int ndesigntemplatemappingcode = -1;
		short nsampletypecode = -1;
		short nregtypecode = -1;
		short nregsubtypecode = -1;
		short formCode = -1;
		if ((inputMap.containsKey("nsampletypecode")) && (inputMap.containsKey("nregtypecode"))
				&& (inputMap.containsKey("nregsubtypecode"))) {

			if (inputMap.containsKey("ndesigntemplatemappingcode")) {
				ndesigntemplatemappingcode = Short
						.parseShort(String.valueOf(inputMap.get("ndesigntemplatemappingcode")));
			}
			nsampletypecode = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
			nregtypecode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
			nregsubtypecode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));
			formCode = inputMap.containsKey("nformcode") ? Short.parseShort(String.valueOf(inputMap.get("nformcode")))
					: -1;
		}

		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);

		return designtemplatemappingService.getDesignTemplateMapping(nsampletypecode, nregtypecode, nregsubtypecode,
				ndesigntemplatemappingcode, formCode, userInfo);

	}

	@PostMapping(path = "getDynamicPreRegDesign")
	public ResponseEntity<Object> getDynamicPreRegDesign(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return designtemplatemappingService.getDynamicPreRegDesign(inputMap, userInfo);

	}

	@PostMapping(value = "/getTemplateRegType")
	public ResponseEntity<Object> getTemplateRegType(@RequestBody Map<String, Object> inputMap) throws Exception {

		int nregtypecode = -1;
		final ObjectMapper objMapper = new ObjectMapper();
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designtemplatemappingService.getTemplateRegType(nregtypecode, nsampletypecode, userInfo);

	}

	@PostMapping(value = "/getTemplateRegSubType")
	public ResponseEntity<Object> getTemplateRegSubType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designtemplatemappingService.getTemplateRegSubType(nregtypecode, userInfo);

	}

	@PostMapping(value = "/getFilterSubmit")
	public ResponseEntity<Object> getFilterSubmit(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int ndesigntemplatemappingcode = -1;
		short nsampletypecode = -1;
		short nregtypecode = -1;
		short nregsubtypecode = -1;
		short formCode = -1;
		if ((inputMap.containsKey("nsampletypecode")) && (inputMap.containsKey("nregtypecode"))
				&& (inputMap.containsKey("nregsubtypecode"))) {

			if (inputMap.containsKey("ndesigntemplatemappingcode")) {
				ndesigntemplatemappingcode = Short
						.parseShort(String.valueOf(inputMap.get("ndesigntemplatemappingcode")));
			}
			nsampletypecode = Short.parseShort(String.valueOf(inputMap.get("nsampletypecode")));
			nregtypecode = Short.parseShort(String.valueOf(inputMap.get("nregtypecode")));
			nregsubtypecode = Short.parseShort(String.valueOf(inputMap.get("nregsubtypecode")));
			formCode = Short.parseShort(String.valueOf(inputMap.get("nformcode")));

		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return designtemplatemappingService.getDesignTemplateMapping(nsampletypecode, nregtypecode, nregsubtypecode,
				ndesigntemplatemappingcode, formCode, userInfo);
	}

	@PostMapping(path = "createDesignTemplateMapping")
	public ResponseEntity<Object> createDesignTemplateMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return designtemplatemappingService.createDesignTemplateMapping(inputMap, userInfo);

	}

	@PostMapping(path = "getDesignTemplateMappingById")
	public ResponseEntity<Object> getDesignTemplateMappingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
		return designtemplatemappingService.getDesignTemplateMappingById(ndesigntemplatemappingcode, userInfo);

	}

	@PostMapping(path = "ApproveDesignTemplateMapping")
	public ResponseEntity<Object> approveDesignTemplateMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designtemplatemappingService.approveDesignTemplateMapping(inputMap, userInfo);

	}

	@PostMapping(path = "deleteDesignTemplateMapping")
	public ResponseEntity<Object> deleteDesignTemplateMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final DesignTemplateMapping designtemplatemapping = objMapper
				.convertValue(inputMap.get("designtemplatemapping"), DesignTemplateMapping.class);
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designtemplatemappingService.deleteDesignTemplateMapping(designtemplatemapping, userInfo);

	}

	@PostMapping(path = "getMappedFieldProps")
	public ResponseEntity<Object> getMappedFieldProps(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final DesignTemplateMapping designtemplatemapping = objMapper
				.convertValue(inputMap.get("designtemplatemapping"), DesignTemplateMapping.class);
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designtemplatemappingService.getMappedFieldProps(designtemplatemapping, userInfo);

	}

	@PostMapping(path = "configureDesignTemplateMapping")
	public ResponseEntity<Object> configureDesignTemplateMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final DesignTemplateMapping designtemplatemapping = objMapper
				.convertValue(inputMap.get("designtemplatemapping"), DesignTemplateMapping.class);
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designtemplatemappingService.configureDesignTemplateMapping(designtemplatemapping, userInfo);

	}

	@PostMapping(path = "getAuditMappedFieldProps")
	public ResponseEntity<Object> getAuditMappedFieldProps(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final DesignTemplateMapping designtemplatemapping = objMapper
				.convertValue(inputMap.get("designtemplatemapping"), DesignTemplateMapping.class);
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designtemplatemappingService.getAuditMappedFieldProps(designtemplatemapping, userInfo, inputMap);

	}

	@PostMapping(path = "configureDynamicAudit")
	public ResponseEntity<Object> configureDynamicAudit(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designtemplatemappingService.configureDynamicAudit(inputMap, userInfo);

	}

	@PostMapping(path = "updateConfigureSendToStore")
	public ResponseEntity<Object> updateConfigureSendToStore(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designtemplatemappingService.updateConfigureSendToStore(inputMap, userInfo);
	}

	@RequestMapping(value = "/getMappedfield", method = RequestMethod.POST)
	public ResponseEntity<Object> getMappedfield(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
		return designtemplatemappingService.getMappedfield(ndesigntemplatemappingcode, userInfo);
	}

	@RequestMapping(value = "/getConfigureCheckList", method = RequestMethod.POST)
	public ResponseEntity<Object> getConfigureCheckList(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
		return designtemplatemappingService.getConfigureCheckList(ndesigntemplatemappingcode, userInfo);
	}

	@RequestMapping(value = "/getConfigureCheckListLatestVersion", method = RequestMethod.POST)
	public ResponseEntity<Object> getConfigureCheckListLatestVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designtemplatemappingService.getConfigureCheckListLatestVersion(inputMap, userInfo);
	}

	@RequestMapping(value = "/getReleaseSampleFilterFields", method = RequestMethod.POST)
	public ResponseEntity<Object> getReleaseSampleFilterFields(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return designtemplatemappingService.getReleaseSampleFilterFields(userInfo);
	}

}
