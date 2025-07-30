package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.service.material.MaterialService;
import com.agaramtech.qualis.materialmanagement.service.materialsection.MaterialSectionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/material")
public class MaterialController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialController.class);

	private RequestContext requestContext;
	private final MaterialService objMaterialService;
	private final MaterialSectionService objMaterialSectionService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param cityService    CityService
	 */
	public MaterialController(RequestContext requestContext, MaterialService objMaterialService,
			MaterialSectionService objMaterialSectionService) {
		super();
		this.requestContext = requestContext;
		this.objMaterialService = objMaterialService;
		this.objMaterialSectionService = objMaterialSectionService;
	}

	@PostMapping(value = "/getMaterial")
	public ResponseEntity<Object> getMaterialType(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getMaterialType");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String currentUIDate = (String) inputMap.get("currentdate");
		return objMaterialService.getMaterialType(userInfo, currentUIDate);
	}

	@PostMapping(value = "/getMaterialcombo")
	public ResponseEntity<Object> getMaterialcombo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Integer nmaterialtypecode = (Integer) inputMap.get("nmaterialtypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.getMaterialcombo(nmaterialtypecode, userInfo);
	}

	@PostMapping(value = "/getMaterialAdd")
	public ResponseEntity<Map<String, Object>> getMaterialAdd(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final short nmaterialtypecode = (short) inputMap.get("nmaterialtypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.getMaterialAdd(nmaterialtypecode, userInfo);
	}

	@PostMapping(value = "/createMaterial")
	public ResponseEntity<Object> createMaterial(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.createMaterial(inputMap, userInfo);
	}

	@PostMapping(value = "/getMaterialDetails")
	public ResponseEntity<Object> getMaterialDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.getMaterialDetails(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteMaterial")
	public ResponseEntity<? extends Object> deleteMaterial(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.deleteMaterial(inputMap, userInfo);
	}

	@PostMapping(value = "/getMaterialEdit")
	public ResponseEntity<Object> getMaterialEdit(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.getMaterialEdit(inputMap, userInfo);
	}

	@PostMapping(value = "/updateMaterial")
	public ResponseEntity<Object> updateMaterial(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.updateMaterial(inputMap, userInfo);
	}

	@PostMapping(value = "/getMaterialByTypeCode")
	public ResponseEntity<Map<String, Object>> getMaterialByTypeCode(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.getMaterialByTypeCode(inputMap, userInfo);
	}

	@PostMapping(value = "/createMaterialSection")
	public ResponseEntity<Object> createMaterialSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialSectionService.createMaterialSection(inputMap);
	}

	@PostMapping(value = "/deleteMaterialSection")
	public ResponseEntity<Object> deleteMaterialSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialSectionService.deleteMaterialSection(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveMaterialSectionById")
	public ResponseEntity<Object> getActiveMaterialSectionById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialSectionService.getActiveMaterialSectionById(inputMap, userInfo);
	}

	@PostMapping(value = "/updateMaterialSection")
	public ResponseEntity<Object> updateMaterialSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialSectionService.updateMaterialSection(inputMap, userInfo);
	}

	@PostMapping(value = "/getMaterialByID")
	public ResponseEntity<Object> getMaterialByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.getMaterialByID(inputMap);
	}

	@PostMapping(value = "/getMaterialSection")
	public ResponseEntity<Object> getMaterialSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.getMaterialSection(inputMap, userInfo);
	}

	@PostMapping(value = "/createMaterialSafetyInstructions")
	public ResponseEntity<Object> createMaterialSafetyInstructions(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.createMaterialSafetyInstructions(inputMap, userInfo);
	}

	@PostMapping(value = "/getMaterialSafetyInstructions")
	public ResponseEntity<? extends Object> getMaterialSafetyInstructions(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.getMaterialSafetyInstructions(inputMap, userInfo);
	}

	@PostMapping(value = "/createMaterialMsdsAttachment")
	public ResponseEntity<Object> createMaterialMsdsAttachment(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.createMaterialMsdsAttachment(request, userInfo);
	}

	@PostMapping(value = "/editMaterialMsdsAttachment")
	public ResponseEntity<Object> editMaterialMsdsAttachment(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.editMaterialMsdsAttachment(inputMap, userInfo);
	}

	@PostMapping(value = "/updateMaterialMsdsAttachment")
	public ResponseEntity<Object> updateMaterialMsdsAttachment(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.updateMaterialMsdsAttachment(request, userInfo);
	}

	@PostMapping(value = "/deleteMaterialMsdsAttachment")
	public ResponseEntity<Object> deleteMaterialMsdsAttachment(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.deleteMaterialMsdsAttachment(inputMap, userInfo);
	}

	@PostMapping(value = "/viewAttachedMaterialMsdsAttachment")
	public ResponseEntity<Object> viewAttachedMaterialFile(@RequestBody Map<String, Object> inputMap,
			final HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.viewAttachedMaterialMsdsAttachment(inputMap, userInfo);
	}

	@PostMapping(value = "/getMaterialLinkMaster")
	public ResponseEntity<Object> getMaterialLinkMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(objUserInfo);
		return objMaterialService.getMaterialLinkMaster(inputMap, objUserInfo);
	}

	@PostMapping(value = "/getMaterialAcountingProperties")
	public ResponseEntity<Object> getMaterialAcountingProperties(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.getMaterialAcountingProperties(inputMap, userInfo);
	}

	@PostMapping(value = "/createMaterialInventory")
	public ResponseEntity<Object> createMaterialInventory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.createMaterialInventory(inputMap, userInfo);
	}

	@PostMapping(value = "/getMaterialAvailableQty")
	public ResponseEntity<Object> getMaterialAvailableQty(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialService.getMaterialAvailableQty(inputMap, userInfo);
	}

	@PostMapping(value = "/getUraniumContentType")
	public ResponseEntity<Object> getUraniumContentType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objMaterialService.getUraniumContentType(userInfo);
	}
}