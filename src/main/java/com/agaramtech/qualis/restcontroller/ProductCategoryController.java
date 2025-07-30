package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.ProductCategory;
import com.agaramtech.qualis.product.service.productcategory.ProductCategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Sample Category Service methods.
 * 
 * @author ATE140 -Perumalraj
 * @version
 * @since 24- Jun- 2020
 */
@RestController
@RequestMapping("/productcategory")
public class ProductCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductCategoryController.class);

	private RequestContext requestContext;
	private final ProductCategoryService productCategoryService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext         RequestContext to hold the request
	 * @param productCategoryService ProductCategoryService
	 */
	public ProductCategoryController(RequestContext requestContext, ProductCategoryService productCategoryService) {
		super();
		this.requestContext = requestContext;
		this.productCategoryService = productCategoryService;
	}

	/**
	 * This method is used to retrieve list of active Product Category for the
	 * specified site.
	 * 
	 * @param inputMap [Map] map object with "userinfo" as key for which the list is
	 *                 to be fetched
	 * @return response object with list of active Sample Category that are to be
	 *         listed for the specified site
	 * 
	 */
	@PostMapping(value = "/getProductCategory")
	public ResponseEntity<Object> getProductCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getProductCategory");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return productCategoryService.getProductCategory(userInfo);
	}

	/**
	 * This method is used to create a new Product Category for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of Product Category [Sample Category]
	 *                 and UserInfo object entity.
	 * @return response entity for List of Product Category entity
	 */
	@PostMapping(value = "/createProductCategory")
	public ResponseEntity<Object> createProductCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ProductCategory ProductCategory = objmapper.convertValue(inputMap.get("productcategory"),
				ProductCategory.class);
		final UserInfo UserInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(UserInfo);
		return productCategoryService.createProductCategory(inputMap, ProductCategory, UserInfo);
	}

	/**
	 * This method is used to update Product Category for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of productcategory [Product Category]
	 *                 and UserInfo object entity.
	 * @return response entity List of Product Category entity
	 */
	@PostMapping(value = "/updateProductCategory")
	public ResponseEntity<Object> updateProductCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final ProductCategory productCategory = objmapper.convertValue(inputMap.get("productcategory"),
				ProductCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return productCategoryService.updateProductCategory(inputMap, productCategory, userInfo);
	}

	/**
	 * This method is used to delete Product Category for the specified Site.
	 * 
	 * @param mapObject [Map] object with keys of productcategory [Product Category]
	 *                  and UserInfo object entity.
	 * @return response entity of deleted Product Category entity
	 */
	@PostMapping(value = "/deleteProductCategory")
	public ResponseEntity<Object> deleteProductCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final ProductCategory ProductCategory = objmapper.convertValue(inputMap.get("productcategory"),
				ProductCategory.class);
		final UserInfo UserInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(UserInfo);
		return productCategoryService.deleteProductCategory(inputMap, ProductCategory, UserInfo);
	}

	/**
	 * This method is used to get the single record in product category table by
	 * primary key
	 * 
	 * @param inputMap [Map] holds the Primary Key["nproductcatcode"] code and
	 *                 userinfo object entity
	 * @return response entity object holding response status and data of single
	 *         Product category object
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveProductCategoryById")
	public ResponseEntity<Object> getActiveProductCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nproductcatcode = (int) inputMap.get("nproductcatcode");
		final UserInfo UserInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(UserInfo);
		return productCategoryService.getActiveProductCategoryById(nproductcatcode, UserInfo);
	}
}
