package com.agaramtech.qualis.restcontroller;

import java.util.Map;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductFile;
import com.agaramtech.qualis.product.service.product.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/product")
public class ProductController {
	
	//private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

	private RequestContext requestContext;
	private final ProductService productService;
	
	
	public ProductController(RequestContext requestContext, ProductService productService) {
		super();
		this.requestContext = requestContext;
		this.productService = productService;
	}


	/**
	 * This method is used to retrieve list of active product based upon Approval
	 * Configuration for the specified site.
	 * 
	 * @param inputMap [Map] map object with "userinfo" as key for which the list is
	 *                 to be fetched
	 * @return response object with list of active Product that are to be listed for
	 *         the specified site
	 */

	@RequestMapping(value = "/getProduct", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> getProduct(@RequestBody Map<String, Object> obj) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(obj.get("userinfo"), UserInfo.class);
		Integer nproductcode = null;
		
		if (obj.get("nproductcode") != null) {
			nproductcode = (Integer) obj.get("nproductcode");	
		}
		
		//LOGGER.info("Product"+nproductcode);
		requestContext.setUserInfo(userInfo);
		return  productService.getProduct(nproductcode,userInfo);
	}

	/**
	 * This method is used to insert the product based upon Approval Configuration
	 * for the specified site.
	 * 
	 * @param inputMap [Map] map object with "product" object and userinfo as key
	 *                 for which the list is to be fetched
	 * @return response object with list of active Product that are to be listed for
	 *         the specified site
	 */
	@RequestMapping(value = "/createProduct", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> createProduct(@RequestBody Map<String, Object> obj) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		
		final Product objProduct = objMapper.convertValue(obj.get("product"), Product.class);
		final UserInfo objUserInfo = objMapper.convertValue(obj.get("userinfo"), UserInfo.class);
		
		requestContext.setUserInfo(objUserInfo);
		return productService.createProduct(obj,objProduct, objUserInfo);	 

	}

	/**
	 * This method is used to delete the product based upon Approval Configuration
	 * for the specified site.
	 * 
	 * @param inputMap [Map] map object with "product" object and userinfo as key
	 *                 for which the list is to be fetched
	 * @return response object with list of active Product that are to be listed for
	 *         the specified site
	 */
	@RequestMapping(value = "/deleteProduct", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> deleteProduct(@RequestBody Map<String, Object> obj) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final Product objProduct = objMapper.convertValue(obj.get("product"), Product.class);
		final UserInfo objUserInfo = objMapper.convertValue(obj.get("userinfo"), UserInfo.class);
		
		requestContext.setUserInfo(objUserInfo);
		return productService.deleteProduct(obj,objProduct, objUserInfo);

	}

	/**
	 * This interface declaration is used to update entry in Product table through
	 * its DAO layer.
	 * 
	 * @param inputMap [Map] object with keys of product [Product] and UserInfo
	 *                 object entity.
	 * @return response entity List of Product entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	@RequestMapping(value = "/updateProduct", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> updateProduct(@RequestBody Map<String, Object> obj) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		
		final Product objProduct = objMapper.convertValue(obj.get("product"), Product.class);
		final UserInfo objUserInfo = objMapper.convertValue(obj.get("userinfo"), UserInfo.class);
		
		requestContext.setUserInfo(objUserInfo);
		return productService.updateProduct(obj,objProduct, objUserInfo);

	}
	
	/**
	 * This method is used to retrieve selected active product detail.
	 * @param inputMap  [Map] map object with "nproductcode" and "userInfo" as keys for which the data is to be fetched
	 * @return response object with selected active product
	 */
	@PostMapping(value = "/getActiveProductById")
	public ResponseEntity<Object> getActiveProductById(@RequestBody Map<String, Object> inputMap)throws Exception  {

		final ObjectMapper objMapperper = new ObjectMapper();
		
		final int nproductcode = (Integer) inputMap.get("nproductcode");
		final UserInfo userInfo = objMapperper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		
		requestContext.setUserInfo(userInfo);
		return productService.getActiveProductById(nproductcode, userInfo);
		
	}
	
	/**
	 * Uploads a new file associated with a product.
	 *
	 * @param request Multipart HTTP request containing file and "userinfo".
	 * @return ResponseEntity with upload status and file metadata.
	 * @throws Exception If file upload fails or parsing issues occur.
	 */
	@PostMapping(value = "/createProductFile")
	public ResponseEntity<Object> createProductFile(MultipartHttpServletRequest request) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		
		requestContext.setUserInfo(userInfo);
		return productService.createProductFile(request, userInfo);

	}
	
	/**
	 * Edits metadata of an existing product file.
	 *
	 * @param inputMap Map containing "productfile" and "userinfo".
	 * @return ResponseEntity with updated file metadata.
	 * @throws Exception If file metadata update fails.
	 */
	@PostMapping(value = "/editProductFile")
	public ResponseEntity<Object> editProductFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ProductFile objProductFile = objMapper.convertValue(inputMap.get("productfile"), ProductFile.class);
		
		requestContext.setUserInfo(userInfo);
		return productService.editProductFile(inputMap,objProductFile, userInfo);

	}
	
	/**
	 * Updates an existing product file. May include reupload of file.
	 *
	 * @param request Multipart HTTP request with updated file and "userinfo".
	 * @return ResponseEntity with updated file info or error message.
	 * @throws Exception If file update fails.
	 */
	@PostMapping(value = "/updateProductFile")
	public ResponseEntity<Object> updateProductFile(MultipartHttpServletRequest request) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		
		requestContext.setUserInfo(userInfo);
		return productService.updateProductFile(request, userInfo);

	}
	
	/**
	 * Deletes a product file. Handles reassignment of default if necessary.
	 *
	 * @param inputMap Map containing "productfile" and "userinfo".
	 * @return ResponseEntity with deletion status and updated file list.
	 * @throws Exception If file deletion fails or file not found.
	 */
	@PostMapping(value = "/deleteProductFile")
	public ResponseEntity<Object> deleteProductFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ProductFile objProductFile = objMapper.convertValue(inputMap.get("productfile"), ProductFile.class);
		
		requestContext.setUserInfo(userInfo);
		return productService.deleteProductFile(inputMap,objProductFile, userInfo);
	}
	
	/**
	 * Retrieves content or path of an attached product file.
	 *
	 * @param inputMap  Map containing "productfile" and "userinfo".
	 * @param response  HTTP response object to stream file if needed.
	 * @return ResponseEntity with file content or metadata.
	 * @throws Exception If file retrieval fails.
	 */
	@PostMapping(value = "/viewAttachedProductFile")
	public ResponseEntity<Object> viewAttachedProductFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ProductFile objProductFile = objMapper.convertValue(inputMap.get("productfile"), ProductFile.class);
		
		Map<String, Object> outputMap = productService.viewAttachedProductFile(inputMap,objProductFile, userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
	
	/**
	 * Retrieves list of files associated with a product.
	 *
	 * @param obj Map containing "userinfo" and optional "nproductcode".
	 * @return ResponseEntity with list of product files.
	 * @throws Exception If file list retrieval fails.
	 */
	@RequestMapping(value = "/getProductFile", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Object> getProductFile(@RequestBody Map<String, Object> obj) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(obj.get("userinfo"), UserInfo.class);
		Integer nproductcode = null;
		
		if (obj.get("nproductcode") != null) {
			nproductcode = (Integer) obj.get("nproductcode");	
		}
		requestContext.setUserInfo(userInfo);
		return  productService.getProductFile(nproductcode,userInfo);
	}
}
