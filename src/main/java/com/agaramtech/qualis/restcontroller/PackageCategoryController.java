package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.stability.model.PackageCategory;
import com.agaramtech.qualis.stability.service.packagecategory.PackageCategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/packagecategory")
public class PackageCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PackageCategoryController.class);

	private RequestContext requestContext;
	private final PackageCategoryService packageCategoryService;

	public PackageCategoryController(RequestContext requestContext, PackageCategoryService packageCategoryService) {
		super();
		this.requestContext = requestContext;
		this.packageCategoryService = packageCategoryService;
	}

	/**
	 * This Method is used to get the over all packagecategory with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of packagecategory with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/getPackageCategory")
	public ResponseEntity<Object> getPackageCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getPackageCategory() called");
		requestContext.setUserInfo(userInfo);
		return packageCategoryService.getPackageCategory(userInfo);

	}

	/**
	 * This method is used to add a new entry to PackageCategory table.
	 * 
	 * @param inputMap [Map] holds the PackageCategory object to be inserted
	 * @return inserted PackageCategory object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/createPackageCategory")
	public ResponseEntity<Object> createPackageCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		PackageCategory objPackageCategory = objMapper.convertValue(inputMap.get("packagecategory"),
				new TypeReference<PackageCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return packageCategoryService.createPackageCategory(objPackageCategory, userInfo);

	}

	/**
	 * This method is used to update entry in PackageCategory table.
	 * 
	 * @param inputMap [Map] holds the PackageCategory object to be updated
	 * @return response entity object holding response status and data of updated
	 *         PackageCategory object
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/updatePackageCategory")
	public ResponseEntity<Object> updatePackageCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		PackageCategory objPackageCategory = objMapper.convertValue(inputMap.get("packagecategory"),
				new TypeReference<PackageCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return packageCategoryService.updatePackageCategory(objPackageCategory, userInfo);

	}

	/**
	 * This method id used to delete an entry in PackageCategory table
	 * 
	 * @param inputMap [Map] holds the PackageCategory object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         PackageCategory object
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/deletePackageCategory")
	public ResponseEntity<Object> deletePackageCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		PackageCategory objPackageCategory = objMapper.convertValue(inputMap.get("packagecategory"),
				new TypeReference<PackageCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return packageCategoryService.deletePackageCategory(objPackageCategory, userInfo);

	}

	/**
	 * This method is used to get the single record in PackageCategory table
	 * 
	 * @param inputMap [Map] holds the PackageCategory code to get
	 * @return response entity object holding response status and data of single
	 *         PackageCategory object
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/getActivePackageCategoryById")
	public ResponseEntity<Object> getActivePackageCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int npackageCategoryCode = (int) inputMap.get("npackagecategorycode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return packageCategoryService.getActivePackageCategoryById(npackageCategoryCode, userInfo);

	}
}
