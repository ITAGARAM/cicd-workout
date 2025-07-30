package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.contactmaster.model.SupplierCategory;
import com.agaramtech.qualis.contactmaster.service.suppliercategory.SupplierCategoryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the SupplierCategory Service methods.
 * 
 * @author ATE184
 * @version 9.0.0.1
 * @since 30- Jun- 2020
 */
@RestController
@RequestMapping("/suppliercategory")
public class SupplierCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SupplierCategoryController.class);

	private RequestContext requestContext;
	private final SupplierCategoryService suppliercategoryService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param unitService    UnitService
	 */
	public SupplierCategoryController(RequestContext requestContext, SupplierCategoryService suppliercategoryService) {
		super();
		this.requestContext = requestContext;
		this.suppliercategoryService = suppliercategoryService;
	}

	/**
	 * This method is used to retrieve list of active contactmaster suppliercategory
	 * for the specified site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active contactmaster suppliercategory
	 *         that are to be listed for the specified site
	 */
	@PostMapping(value = "/getSupplierCategory")
	public ResponseEntity<Object> getSupplierCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);

		return suppliercategoryService.getSupplierCategory(userInfo);
	}

	/**
	 * This method is used to retrieve list of active contactmaster suppliercategory
	 * for the specified site.
	 * 
	 * @param inputMap [Map] map object with "nsuppliercode" as key for which the
	 *                 list is to be fetched
	 * @return response object with list of active contactmaster suppliercategory
	 *         that are to be listed for the specified site
	 */
	@PostMapping(value = "/getSupplierCategoryBySupplierCode")
	public ResponseEntity<Object> getSupplierCategoryBySupplierCode(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		Integer nsupplierCode = null;
		if (inputMap.get("nsuppliercode") != null) {
			nsupplierCode = (Integer) inputMap.get("nsuppliercode");
		} else {
			nsupplierCode = 0;
		}
		requestContext.setUserInfo(userInfo);

		return suppliercategoryService.getSupplierCategoryBySupplierCode(userInfo.getNmastersitecode(), nsupplierCode);
	}

	/**
	 * This method is used to add new contactmaster suppliercategory for the
	 * specified Site.
	 * 
	 * @param inputMap [Map] object with keys of suppliercategory entity.
	 * @return response entity of newly added suppliercategory entity
	 */

	@PostMapping(value = "/createSupplierCategory")
	public ResponseEntity<Object> createSupplierCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SupplierCategory suppliercategory = objmapper.convertValue(inputMap.get("suppliercategory"),
				SupplierCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return suppliercategoryService.createSupplierCategory(suppliercategory, userInfo);
	}

	/**
	 * This method is used to retrieve selected active contactmaster
	 * suppliercategory detail.
	 * 
	 * @param inputMap [Map] map object with "nsuppliercatcode" and "userInfo" as
	 *                 keys for which the data is to be fetched
	 * @return response object with selected active contactmaster suppliercategory
	 */

	@PostMapping(value = "/getActiveSupplierCategoryById")
	public ResponseEntity<Object> getActiveSupplierCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nsuppliercatcode = (Integer) inputMap.get("nsuppliercatcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return suppliercategoryService.getActiveSupplierCategoryById(nsuppliercatcode, userInfo);
	}

	/**
	 * This method is used to update contactmaster suppliercategory for the
	 * specified Site.
	 * 
	 * @param inputMap [Map] object with keys of suppliercategory entity.
	 * @return response entity of updated suppliercategory entity
	 */
	@PostMapping(value = "/updateSupplierCategory")
	public ResponseEntity<Object> updateSupplierCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SupplierCategory suppliercategory = objmapper.convertValue(inputMap.get("suppliercategory"),
				SupplierCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return suppliercategoryService.updateSupplierCategory(suppliercategory, userInfo);
	}

	/**
	 * This method is used to delete contactmaster suppliercategory for the
	 * specified Site.
	 * 
	 * @param mapObject [Map] object with keys of suppliercategory entity.
	 * @return response entity of deleted suppliercategory entity
	 */
	@PostMapping(value = "/deleteSupplierCategory")
	public ResponseEntity<Object> deleteSupplierCategory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final SupplierCategory suppliercategory = objmapper.convertValue(inputMap.get("suppliercategory"),
				SupplierCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return suppliercategoryService.deleteSupplierCategory(suppliercategory, userInfo);
	}

}
