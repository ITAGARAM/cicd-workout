package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialCategory;
import com.agaramtech.qualis.materialmanagement.service.materialcategory.MaterialCategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/materialcategory")
public class MaterialCategoryController {

	private static final Log LOGGER = LogFactory.getLog(MaterialCategoryController.class);
	private RequestContext requestContext;
	private MaterialCategoryService materialCategoryService;

	public MaterialCategoryController(RequestContext requestContext, MaterialCategoryService materialCategoryService) {
		super();
		this.requestContext = requestContext;
		this.materialCategoryService = materialCategoryService;
	}

	@PostMapping(value = "/getMaterialCategory")
	public ResponseEntity<Object> getMaterialCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return materialCategoryService.getMaterialCategory(userInfo);
	}

	@PostMapping(value = "/getMaterialCategoryBYSupplierCode")
	public ResponseEntity<Object> getMaterialCategoryBYSupplierCode(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		Integer nsupplierCode = null;
		if (inputMap.get("nsuppliercode") != null) {
			nsupplierCode = (Integer) inputMap.get("nsuppliercode");
		} else {
			nsupplierCode = 0;
		}
		return materialCategoryService.getMaterialCategoryBYSupplierCode(userInfo.getNmastersitecode(), nsupplierCode,
				userInfo);
	}

	@PostMapping(value = "/createMaterialCategory")
	public ResponseEntity<Object> createMaterialCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final MaterialCategory materialCategory = objmapper.convertValue(inputMap.get("materialcategory"),
				MaterialCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return materialCategoryService.createMaterialCategory(materialCategory, userInfo);

	}

	@PostMapping(value = "/getActiveMaterialCategoryById")
	public ResponseEntity<Object> getActiveMaterialCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nmaterialcatcode = (Integer) inputMap.get("nmaterialcatcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return materialCategoryService.getActiveMaterialCategoryById(nmaterialcatcode, userInfo);
	}

	@PostMapping(value = "/updateMaterialCategory")
	public ResponseEntity<Object> updateMaterialCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final MaterialCategory materialCategory = objmapper.convertValue(inputMap.get("materialcategory"),
				MaterialCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return materialCategoryService.updateMaterialCategory(materialCategory, userInfo);
	}

	@PostMapping(value = "/deleteMaterialCategory")
	public ResponseEntity<Object> deleteMaterialCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final MaterialCategory materialCategory = objmapper.convertValue(inputMap.get("materialcategory"),
				MaterialCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return materialCategoryService.deleteMaterialCategory(materialCategory, userInfo);
	}

	@PostMapping(value = "/getMaterialType")
	public ResponseEntity<Object> getMaterialType(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return materialCategoryService.getMaterialType(userInfo);
	}

}
