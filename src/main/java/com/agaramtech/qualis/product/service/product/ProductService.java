package com.agaramtech.qualis.product.service.product;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductFile;

public interface ProductService {
	
	/**
	 * This method is used to retrieve list of active product based upon Approval Configuration for the specified site.
	 * @param inputMap  [Map] map object with "nmastersitecode" as key for which the list is to be fetched
	 * @return response object with list of active Product that are to be listed for the specified site
	 */
	public ResponseEntity<Object> getProduct(Integer nproductcode,UserInfo userInfo) throws Exception;

 	/**
	 * This method is used to insert the product based upon Approval Configuration for the specified site.
	 * @param inputMap  [Map] map object with "product"  object and userinfo as key for which the list is to be fetched
	 * @return response object with list of active Product that are to be listed for the specified site
	 */
	public ResponseEntity<Object> createProduct(Map<String, Object> obj,Product objProduct,UserInfo objUserInfo) throws Exception;
	
 	/**
	 * This method is used to delete the product based upon Approval Configuration for the specified site.
	 * @param inputMap  [Map] map object with "product" object and userinfo as key for which the list is to be fetched
	 * @return response object with list of active Product that are to be listed for the specified site
	 */
	public ResponseEntity<Object> deleteProduct(Map<String, Object> obj,Product objProduct,UserInfo objUserInfo) throws Exception;
	
	/**
	 * This interface declaration is used to update entry in Product  table through its DAO layer.
	 * @param inputMap [Map] object with keys of product [Product] and UserInfo object entity.
	 * @return response entity List of  Product entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateProduct(Map<String, Object> obj,Product objProduct,UserInfo userinfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active product object based
	 * on the specified nproductcode through its DAO layer.
	 * @param nproductcode [int] primary key of product object
	 * @param siteCode [int] primary key of site object 
	 * @return response entity  object holding response status and data of product object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveProductById(final int nproductcode,final UserInfo userInfo) throws Exception ;
	
	/**
	 * Handles the creation/upload of a file associated with a product.
	 *
	 * @param request  Multipart request containing the file and user info.
	 * @param userInfo The user performing the upload.
	 * @return ResponseEntity with upload status and file info.
	 * @throws Exception if file processing fails.
	 */
	public ResponseEntity<Object> createProductFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception;

	/**
	 * Edits metadata of an existing product file.
	 *
	 * @param inputMap     Map containing file and request metadata.
	 * @param productFile  The product file with updated metadata.
	 * @param userInfo     The user requesting the update.
	 * @return ResponseEntity with updated file info.
	 * @throws Exception if update fails.
	 */
	public ResponseEntity<Object> editProductFile(final Map<String, Object> inputMap,final ProductFile objProductFile, final UserInfo objUserInfo)
			throws Exception;

	/**
	 * Updates the file content or metadata of an existing product file.
	 *
	 * @param request  Multipart request containing updated file and user info.
	 * @param userInfo The user performing the update.
	 * @return ResponseEntity with update status.
	 * @throws Exception if file update fails.
	 */
	public ResponseEntity<Object> updateProductFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception;

	/**
	 * Deletes a product file associated with a product.
	 *
	 * @param inputMap     Map containing file and request metadata.
	 * @param productFile  The product file to delete.
	 * @param userInfo     The user requesting the deletion.
	 * @return ResponseEntity with deletion result.
	 * @throws Exception if file deletion fails.
	 */
	public ResponseEntity<Object> deleteProductFile(final Map<String, Object> inputMap,final ProductFile objProductFile, final UserInfo objUserInfo)
			throws Exception;
	
	/**
	 * Retrieves the contents and metadata of an attached product file.
	 *
	 * @param inputMap     Map containing product and file metadata.
	 * @param productFile  The file to view.
	 * @param userInfo     The user requesting the file.
	 * @return Map containing the file data or metadata.
	 * @throws Exception if file retrieval fails.
	 */
	public Map<String, Object> viewAttachedProductFile(final Map<String, Object> inputMap, final ProductFile objProductFile,  UserInfo objUserInfo)
			throws Exception;
		
	/**
	 * Fetches all files attached to a specific product.
	 *
	 * @param nproductcode The product code to retrieve files for.
	 * @param userInfo     The user requesting the files.
	 * @return ResponseEntity with the list of product files.
	 * @throws Exception if retrieval fails.
	 */
	public ResponseEntity<Object> getProductFile(Integer nproductcode,UserInfo userInfo) throws Exception;
}
