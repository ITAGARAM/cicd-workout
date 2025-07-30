package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.credential.model.LimsElnUserMapping;
import com.agaramtech.qualis.credential.service.limselnusers.LimsElnUserMappingService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is responsible for managing CRUD operations on
 * LimsElnUserMapping entities. It provides methods to fetch, create, update,
 * and delete LIMS ELN user mappings, as well as retrieving LIMS users
 * associated with the mapping.
 * 
 * @author ATE172
 * @version 9.0.0.1
 */
@RestController
@RequestMapping("/limselnusermapping")
public class LimsElnUserMappingController {
	private static final Logger LOGGER = LoggerFactory.getLogger(LimsElnUserMappingController.class);
	
	private  RequestContext requestContext;
	private final LimsElnUserMappingService limsElnUserMappingService;

	
	public LimsElnUserMappingController(RequestContext requestContext,
			LimsElnUserMappingService limsElnUserMappingService) {
		super();
		this.requestContext = requestContext;
		this.limsElnUserMappingService = limsElnUserMappingService;
	}

	/**
	 * This method retrieves LIMS ELN user mappings for a specified user.
	 * 
	 * @param inputMap [Map] The input data containing UserInfo and other details.
	 * @return ResponseEntity containing the result of the LIMS ELN user mapping
	 *         retrieval operation.
	 * @throws Exception if an error occurs during the processing.
	 */
	@PostMapping(value = "/getLimsElnUsermapping")
	public ResponseEntity<Object> getlimselnusermapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getLimsElnUsermapping called");
		return limsElnUserMappingService.getlimselnusermapping(userInfo);
	}

	/**
	 * This method creates a new LIMS ELN user mapping.
	 * 
	 * @param inputMap [Map] Contains the LIMS ELN user mapping details and UserInfo
	 *                 object.
	 * @return ResponseEntity indicating the result of the creation operation.
	 * @throws Exception if an error occurs during the processing.
	 */
	@PostMapping(value = "/createLimsElnUsermapping")
	public ResponseEntity<Object> createLimsElnUsermapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final LimsElnUserMapping limselnuser = objmapper.convertValue(inputMap.get("limselnusermapping"),
				LimsElnUserMapping.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return limsElnUserMappingService.createLimsElnUsermapping(limselnuser, userInfo);
	}

	/**
	 * This method retrieves a list of LIMS users.
	 * 
	 * @param inputMap [Map] Contains UserInfo object to identify the requesting
	 *                 user.
	 * @return ResponseEntity containing the list of LIMS users.
	 * @throws Exception if an error occurs during the processing.
	 */
	@PostMapping(value = "/getLimsUsers")
	public ResponseEntity<Object> getLimsUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return limsElnUserMappingService.getLimsUsers(userInfo);
	}

	/**
	 * This method deletes an existing LIMS ELN user mapping.
	 * 
	 * @param inputMap [Map] Contains the LIMS ELN user mapping details and UserInfo
	 *                 object.
	 * @return ResponseEntity indicating the result of the deletion operation.
	 * @throws Exception if an error occurs during the processing.
	 */
	@PostMapping(value = "/deleteLimsElnUsermapping")
	public ResponseEntity<Object> deleteLimsElnUsermapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final LimsElnUserMapping limselnuser = objmapper.convertValue(inputMap.get("limselnusermapping"),
				LimsElnUserMapping.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return limsElnUserMappingService.deleteLimsElnUsermapping(limselnuser, userInfo);
	}

	/**
	 * This method retrieves a specific active LIMS ELN user mapping by its ID.
	 * 
	 * @param inputMap [Map] Contains the mapping ID and UserInfo object.
	 * @return ResponseEntity with the details of the active LIMS ELN user mapping.
	 * @throws Exception if an error occurs during the processing.
	 */
	@PostMapping(value = "/getActiveLimsElnUsermappingById")
	public ResponseEntity<Object> getActiveLimsElnUsermappingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nelnusermappingcode = (Integer) inputMap.get("nelnusermappingcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return limsElnUserMappingService.getActiveLimsElnUsermappingById(nelnusermappingcode, userInfo);
	}

	/**
	 * This method updates an existing LIMS ELN user mapping.
	 * 
	 * @param inputMap [Map] Contains the updated LIMS ELN user mapping and UserInfo
	 *                 object.
	 * @return ResponseEntity with the result of the update operation.
	 * @throws Exception if an error occurs during the processing.
	 */
	@PostMapping(value = "/updateLimsElnUsermapping")
	public ResponseEntity<Object> updateLimsElnUsermapping(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final LimsElnUserMapping limselnuser = objmapper.convertValue(inputMap.get("limselnusermapping"),
				LimsElnUserMapping.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return limsElnUserMappingService.updateLimsElnUsermapping(limselnuser, userInfo);
	}
}
