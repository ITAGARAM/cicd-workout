package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.stability.model.PackageMaster;
import com.agaramtech.qualis.stability.service.packagemaster.PackageMasterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/packagemaster")
public class PackageMasterController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PackageMasterController.class);
	private RequestContext requestContext;
	private final PackageMasterService packageMasterService;

	public PackageMasterController(RequestContext requestContext, PackageMasterService packageMasterService) {
		super();
		this.requestContext = requestContext;
		this.packageMasterService = packageMasterService;
	}

	/**
	 * This Method is used to get the over all packagemaster with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of packagemaster with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/getPackageMaster")
	public ResponseEntity<Object> getPackageMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getPackageMaster() called");
		requestContext.setUserInfo(userInfo);
		return packageMasterService.getPackageMaster(userInfo);

	}

	/**
	 * This method is used to add a new entry to PackageMaster table.
	 * 
	 * @param inputMap [Map] holds the PackageMaster object to be inserted
	 * @return inserted PackageMaster object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/createPackageMaster")
	public ResponseEntity<Object> createPackageMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		PackageMaster objPackageMaster = objMapper.convertValue(inputMap.get("packagemaster"),
				new TypeReference<PackageMaster>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return packageMasterService.createPackageMaster(objPackageMaster, userInfo);

	}

	/**
	 * This method is used to update entry in PackageMaster table.
	 * 
	 * @param inputMap [Map] holds the PackageMaster object to be updated
	 * @return response entity object holding response status and data of updated
	 *         PackageMaster object
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/updatePackageMaster")
	public ResponseEntity<Object> updatePackageMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		PackageMaster objPackageMaster = objMapper.convertValue(inputMap.get("packagemaster"),
				new TypeReference<PackageMaster>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return packageMasterService.updatePackageMaster(objPackageMaster, userInfo);

	}

	/**
	 * This method id used to delete an entry in PackageMaster table
	 * 
	 * @param inputMap [Map] holds the PackageMaster object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         PackageMaster object
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/deletePackageMaster")
	public ResponseEntity<Object> deletePackageMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		PackageMaster objPackageMaster = objMapper.convertValue(inputMap.get("packagemaster"),
				new TypeReference<PackageMaster>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return packageMasterService.deletePackageMaster(objPackageMaster, userInfo);

	}

	/**
	 * This method is used to get the single record in PackageMaster table
	 * 
	 * @param inputMap [Map] holds the PackageMaster code to get
	 * @return response entity object holding response status and data of single
	 *         PackageMaster object
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/getActivePackageMasterById")
	public ResponseEntity<Object> getActivePackageMasterById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int npackageMasterCode = (int) inputMap.get("npackagemastercode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return packageMasterService.getActivePackageMasterById(npackageMasterCode, userInfo);
	}
}
