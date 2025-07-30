package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.service.materialinventory.MaterialInventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/materialinventory")
public class MaterialInventoryController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialInventoryController.class);

	final private RequestContext requestContext;
	final private MaterialInventoryService objMaterialInventoryService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param MaterialInventoryService MaterialInventoryService
	 */
	public MaterialInventoryController(RequestContext requestContext, MaterialInventoryService objMaterialInventoryService) {
		super();
		this.requestContext = requestContext;
		this.objMaterialInventoryService = objMaterialInventoryService;
	}
	
	

	@PostMapping(value = "/getMaterialInventory")
	public ResponseEntity<Object> getMaterialInventory(@RequestBody Map<String, Object> inputMap) throws Exception {
		 final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		LOGGER.info("Get Controller -->"+userInfo);
		return (ResponseEntity<Object>) objMaterialInventoryService.getMaterialInventory(userInfo);
	}

	@PostMapping(value = "/getMaterialInventorycombo")
	public ResponseEntity<Object> getMaterialInventorycombo(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		Integer nmaterialtypecode = (Integer) inputMap.get("nmaterialtypecode");
		Integer nmaterialcatcode = null;
		if (inputMap.containsKey("nmaterialcatcode")) {
			nmaterialcatcode = (int) inputMap.get("nmaterialcatcode");
		}
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return (ResponseEntity<Object>) objMaterialInventoryService.getMaterialInventorycombo(nmaterialtypecode,
				nmaterialcatcode, userInfo);
	}

	@PostMapping(value = "/getMaterialInventoryByID")
	public ResponseEntity<Object> getMaterialInventoryByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialInventoryService.getMaterialInventoryByID(inputMap, userInfo);
		
	}

	@PostMapping(value = "/getMaterialInventoryAdd")
	public ResponseEntity<Object> getMaterialInventoryAdd(@RequestBody Map<String, Object> inputMap) throws Exception {
		Integer nmaterialtypecode = (Integer) inputMap.get("nmaterialtypecode");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return (ResponseEntity<Object>) objMaterialInventoryService.getMaterialInventoryAdd(nmaterialtypecode,
				userInfo);
	}

	@PostMapping(value = "/createMaterialInventory")
	public ResponseEntity<Object> createMaterialInventory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialInventoryService.createMaterialInventory(inputMap, userInfo);
	}


	@PostMapping(value = "/getMaterialInventoryEdit")
	public ResponseEntity<Object> getMaterialInventoryEdit(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialInventoryService.getMaterialInventoryEdit(inputMap, userInfo);
	}

	@PostMapping(value = "/updateMaterialInventory")
	public ResponseEntity<Object> UpdateMaterialInventory(@RequestBody Map<String, Object> inputMap) throws Exception {
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return objMaterialInventoryService.UpdateMaterialInventory(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteMaterialInventory")
	public ResponseEntity<Object> deleteMaterialInventory(@RequestBody Map<String, Object> inputMap) throws Exception {
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return objMaterialInventoryService.deleteMaterialInventory(inputMap, userInfo);
	}

	@PostMapping(value = "/getMaterialInventoryDetails")
	public ResponseEntity<Object> getMaterialInventoryDetails(@RequestBody Map<String, Object> inputMap) throws Exception{
			final ObjectMapper objMapper = new ObjectMapper();

			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

			requestContext.setUserInfo(userInfo);
			return objMaterialInventoryService.getMaterialInventoryDetails(inputMap,userInfo);

	}

	@PostMapping(value = "/updateMaterialStatus")
	public ResponseEntity<Object> updateMaterialStatus(@RequestBody Map<String, Object> inputMap) throws Exception{
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return objMaterialInventoryService.updateMaterialStatus(inputMap,userInfo);

	}
	@PostMapping(value = "/getMaterialInventorySearchByID")
	public ResponseEntity<Object> getMaterialInventorySearchByID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return (ResponseEntity<Object>) objMaterialInventoryService.getMaterialInventorySearchByID(inputMap,userInfo);
	}
	@PostMapping(value = "/getQuantityTransaction")
	public ResponseEntity<Object> getQuantityTransaction(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return (ResponseEntity<Object>) objMaterialInventoryService.getQuantityTransaction(inputMap,userInfo);
	}
	@PostMapping(value = "/createMaterialInventoryTrans")
	public ResponseEntity<Object> createMaterialInventoryTrans(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return (ResponseEntity<Object>) objMaterialInventoryService.createMaterialInventoryTrans(inputMap,userInfo);
	}
	@PostMapping(value = "/getMaterialInventoryLinkMaster")
	public ResponseEntity<Object> getMaterialInventoryLinkMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			requestContext.setUserInfo(objUserInfo);
			return objMaterialInventoryService.getMaterialInventoryLinkMaster(inputMap,objUserInfo);
	}
	@PostMapping(value = "/editMaterialInventoryFile")
	public ResponseEntity<Object> editMaterialInventoryFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialInventoryService.editMaterialInventoryFile(inputMap, userInfo);
	}
	@PostMapping(value = "/createMaterialInventoryFile")
	public ResponseEntity<Object> createMaterialInventoryFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialInventoryService.createMaterialInventoryFile(request, userInfo);
	}
	@PostMapping(value = "/updateMaterialInventoryFile")
	public ResponseEntity<Object> updateMaterialInventoryFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialInventoryService.updateMaterialInventoryFile(request, userInfo);
	}

	@PostMapping(value = "/deleteMaterialInventoryFile")
	public ResponseEntity<Object> deleteMaterialInventoryFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialInventoryService.deleteMaterialInventoryFile(inputMap, userInfo);
	}
	@PostMapping(value = "/viewAttachedMaterialInventoryFile")
	public ResponseEntity<Object> viewAttachedMaterialInventoryFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		Map<String, Object> outputMap = objMaterialInventoryService.viewAttachedMaterialInventoryFile(inputMap, userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}
	@PostMapping(path = "getChildValuesMaterial")
	public ResponseEntity<Object> getChildValuesMaterial(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objMaterialInventoryService.getChildValuesMaterial(inputMap,userInfo);
		
	}
}
