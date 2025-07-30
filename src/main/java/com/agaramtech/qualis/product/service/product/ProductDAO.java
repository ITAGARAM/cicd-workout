package com.agaramtech.qualis.product.service.product;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductFile;

public interface ProductDAO {
	/**
	 * This method is used to retrieve list of active product based upon Approval
	 * Configuration for the specified site.
	 * 
	 * @param inputMap [Map] map object with "nmastersitecode" as key for which the
	 *                 list is to be fetched
	 * @return response object with list of active Product that are to be listed for
	 *         the specified site
	 */
	public ResponseEntity<Object> getProduct(Integer nproductcode,UserInfo userInfo) throws Exception;

	/**
	 * This method is used to insert the product based upon Approval Configuration
	 * for the specified site.
	 * 
	 * @param inputMap [Map] map object with "product" object and userinfo as key
	 *                 for which the list is to be fetched
	 * @return response object with list of active Product that are to be listed for
	 *         the specified site
	 */
	public ResponseEntity<Object> createProduct(Map<String, Object> obj,Product objProduct, UserInfo objUserInfo) throws Exception;

	/**
	 * This method is used to delete the product based upon Approval Configuration
	 * for the specified site.
	 * 
	 * @param inputMap [Map] map object with "product" object and userinfo as key
	 *                 for which the list is to be fetched
	 * @return response object with list of active Product that are to be listed for
	 *         the specified site
	 */
	public ResponseEntity<Object> deleteProduct(Map<String, Object> obj,Product objProduct, UserInfo objUserInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in Product table through
	 * its DAO layer.
	 * 
	 * @param inputMap [Map] object with keys of product [Product] and UserInfo
	 *                 object entity.
	 * @return response entity List of Product entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateProduct(Map<String, Object> obj,Product objProduct, UserInfo userinfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active product object based on
	 * the specified nproductcode.
	 * 
	 * @param nproductcode [int] primary key of product object
	 * @return response entity object holding response status and data of product
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Product getActiveProductById(final int nproductcode,final UserInfo userInfo) throws Exception;
	
	/**
     * Inserts a new product file record and stores its metadata.
     *
     * @param request   multipart request containing file stream and metadata.
     * @param userInfo  user uploading the file.
     * @return success message or stored metadata.
     * @throws Exception on file I/O error or DB insert failure.
     */
	public ResponseEntity<Object> createProductFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception;

	/**
     * Updates the metadata of an existing product file (name, description, etc).
     *
     * @param productFile updated product file metadata.
     * @param userInfo    user performing the update.
     * @return success flag or updated metadata.
     * @throws Exception on DB error.
     */
	public ResponseEntity<Object> editProductFile(final Map<String, Object> inputMap,final ProductFile objProductFile, final UserInfo objUserInfo)
			throws Exception;

	/**
     * Replaces the attached file of an existing product file record.
     *
     * @param request   multipart request with new file data.
     * @param userInfo  user uploading the replacement.
     * @return success status.
     * @throws Exception if file not found or DB update fails.
     */
	public ResponseEntity<Object> updateProductFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception;

	/**
     * Deletes a product file entry.
     * May also remove the actual file from file system or FTP depending on configuration.
     *
     * @param productFile file to delete.
     * @param userInfo    user requesting deletion.
     * @return confirmation message.
     * @throws Exception on referential error or file system issue.
     */
	public ResponseEntity<Object> deleteProductFile(final Map<String, Object> inputMap,final ProductFile objProductFile, final UserInfo objUserInfo)
			throws Exception;
	
	/**
     * Retrieves a file’s data or path to be viewed/downloaded by the client.
     *
     * @param productFile file to view.
     * @param userInfo    requesting user.
     * @return map containing file data, path, name, content type.
     * @throws Exception on missing file or access restriction.
     */
	public Map<String, Object> viewAttachedProductFile(final Map<String, Object> inputMap,final ProductFile objProductFile,  UserInfo objUserInfo)
			throws Exception;

	/**
     * Retrieves a list of files attached to a product.
     *
     * @param nproductcode product ID whose files are queried.
     * @param userInfo     user requesting the list.
     * @return list of ProductFile metadata entries.
     * @throws Exception on DAO issues.
     */
	public ResponseEntity<Object> getProductFile(Integer nproductcode,UserInfo userInfo) throws Exception;
}
