package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.service.templatemaster.TemplateMasterService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/templatemaster")
public class TemplateMasterController {

	private RequestContext requestContext;
	private TemplateMasterService TemplateMasterService;

	public TemplateMasterController(RequestContext requestContext, TemplateMasterService TemplateMasterService) {
		super();
		this.requestContext = requestContext;
		this.TemplateMasterService = TemplateMasterService;
	}

	@RequestMapping(value = "/getProductCategoryTemplate", method = RequestMethod.POST)
	public ResponseEntity<Object> getProductCategoryTemplate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return TemplateMasterService.getProductCategoryTemplate(userInfo);
	}

	@RequestMapping(value = "/getTemplateMaster", method = RequestMethod.POST)
	public ResponseEntity<Object> getTemplateMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return TemplateMasterService.getProductCategoryTemplate(userInfo);
	}

	@RequestMapping(value = "/getSampleTypeProductCatrgory", method = RequestMethod.POST)
	public ResponseEntity<Object> getSampleTypeProductCatrgory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return TemplateMasterService.getSampleTypeProductCatrgory(nsampletypecode, userInfo);
	}

	@RequestMapping(value = "/getTemplateMasterVersion", method = RequestMethod.POST)
	public ResponseEntity<Object> getTemplateMasterVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int ntreeversiontempcode = -1;
		final int ncategorycode = (int) inputMap.get("ncategorycode");
		final int nformcode = (int) inputMap.get("nformcode");
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return TemplateMasterService.getTemplateMasterVersion(ntreeversiontempcode, ncategorycode, nformcode,
				nsampletypecode, userInfo);
	}

	@RequestMapping(value = "/getTemplateMasterTree", method = RequestMethod.POST)
	public ResponseEntity<Object> getTemplateMasterTree(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return TemplateMasterService.getTemplateMasterTree(ntreeversiontempcode, userInfo);
	}

	@RequestMapping(value = "/getTemplateVersionById", method = RequestMethod.POST)
	public ResponseEntity<Object> getTemplateVersionById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return TemplateMasterService.getTemplateVersionById(ntreeversiontempcode, userInfo);
	}

	@RequestMapping(value = "/createTemplatemaster", method = RequestMethod.POST)
	public ResponseEntity<Object> createTemplatemaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return TemplateMasterService.createTemplatemaster(inputMap);
	}

	@RequestMapping(value = "/updateEditTemplatemasterSubmit", method = RequestMethod.POST)
	public ResponseEntity<Object> updateEditTemplatemasterSubmit(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return TemplateMasterService.updateEditTemplatemasterSubmit(inputMap);
	}

	@RequestMapping(value = "/deleteTemplateMaster", method = RequestMethod.POST)
	public ResponseEntity<Object> deleteTemplateMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final int nformcode = (int) inputMap.get("nformcode");
		final int ncategorycode = (int) inputMap.get("ncategorycode");
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntreecontrolcode = (String) inputMap.get("ntreecontrolcode");
		requestContext.setUserInfo(userInfo);
		return TemplateMasterService.deleteTemplateMaster(ntreeversiontempcode, nformcode, ncategorycode,
				nsampletypecode, userInfo, ntreecontrolcode);
	}

	@RequestMapping(value = "/approveTemplateMasterVersion", method = RequestMethod.POST)
	public ResponseEntity<Object> approveTemplateMasterVersion(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final int ntreeversiontempcode = (int) inputMap.get("ntreeversiontempcode");
		final int nformcode = (int) inputMap.get("nformcode");
		final int ncategorycode = (int) inputMap.get("ncategorycode");
		final int ntemplatecode = (int) inputMap.get("ntemplatecode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return TemplateMasterService.approveTemplateMasterVersion(ntreeversiontempcode, nformcode, ncategorycode,
				ntemplatecode, userInfo);
	}

	@RequestMapping(value = "/getSampleTypeComboChange", method = RequestMethod.POST)
	public ResponseEntity<Object> getSampleTypeComboChange(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int nsampletypecode = (int) inputMap.get("nsampletypecode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return TemplateMasterService.getSampleTypeComboChange(nsampletypecode, userInfo);
	}

}
