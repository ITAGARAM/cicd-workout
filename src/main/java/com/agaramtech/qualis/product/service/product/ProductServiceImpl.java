package com.agaramtech.qualis.product.service.product;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductFile;



@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ProductServiceImpl implements ProductService {

	private final ProductDAO productDAO;	
	private final CommonFunction commonFunction;
	
	public ProductServiceImpl(ProductDAO productDAO, CommonFunction commonFunction) {
		this.productDAO = productDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of active product based upon Approval
	 * Configuration for the specified site.
	 * 
	 * @param inputMap [Map] map object with "nmastersitecode" as key for which the
	 *                 list is to be fetched
	 * @return response object with list of active Product that are to be listed for
	 *         the specified site
	 */
	@Override
	public ResponseEntity<Object> getProduct(Integer nproductcode,UserInfo userInfo) throws Exception {
		return productDAO.getProduct(nproductcode,userInfo);
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
	@Override
	@Transactional
	public ResponseEntity<Object> createProduct(Map<String, Object> obj,Product objProduct, UserInfo objUserInfo) throws Exception {
		return productDAO.createProduct(obj,objProduct, objUserInfo);
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
	@Override
	@Transactional
	public ResponseEntity<Object> deleteProduct(Map<String, Object> obj,Product objProduct, UserInfo objUserInfo) throws Exception {
		return productDAO.deleteProduct(obj,objProduct, objUserInfo);
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
	@Override
	@Transactional
	public ResponseEntity<Object> updateProduct(Map<String, Object> obj,Product objProduct, UserInfo userinfo) throws Exception {
		return productDAO.updateProduct(obj,objProduct, userinfo);
	}
	
	/**
	 * This method is used to retrieve active product object based
	 * on the specified nproductcode through its DAO layer.
	 * @param nproductcode [int] primary key of product object
	 * @param siteCode [int] primary key of site object 
	 * @return response entity  object holding response status and data of product object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveProductById(final int nproductcode,final UserInfo userInfo) throws Exception {
		
			final Product product = productDAO.getActiveProductById(nproductcode,userInfo);
			if (product == null) {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				return new ResponseEntity<>(product, HttpStatus.OK);
			}
		}
	
	/**
     * Creates and attaches a file to a product using multipart request through its DAO layer.
     * 
     * @param request  multipart request containing file data and metadata.
     * @param userInfo user performing the operation.
     * @return ResponseEntity indicating success or failure.
     * @throws Exception on I/O or validation error.
     */
	@Override
	@Transactional
	public ResponseEntity<Object> createProductFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		return productDAO.createProductFile(request, objUserInfo);
	}
	
	/**
     * Updates metadata for an existing product file through its DAO Layer.
     * 
     * @param inputMap     contains metadata changes.
     * @param productFile  object containing file updates.
     * @param userInfo     user performing the operation.
     * @return ResponseEntity indicating result of update.
     * @throws Exception on DAO error or file not found.
     */
	@Override
	@Transactional
	public ResponseEntity<Object> editProductFile(Map<String,Object>inputMap,ProductFile objProductFile, UserInfo objUserInfo)
			throws Exception {
		return productDAO.editProductFile(inputMap,objProductFile, objUserInfo);
	}
	
	/**
     * Updates product file including replacing the actual file if required through its DAO layer.
     * 
     * @param request  multipart request containing new file and metadata.
     * @param userInfo user performing the update.
     * @return ResponseEntity with status of operation.
     * @throws Exception on I/O error.
     */
	@Override
	@Transactional
	public ResponseEntity<Object> updateProductFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		return productDAO.updateProductFile(request, objUserInfo);
	}

	 /**
     * Deletes a file attached to a product through its DAO layer.
     * 
     * @param inputMap     request parameters including file ID.
     * @param productFile  file object to be deleted.
     * @param userInfo     user performing the operation.
     * @return ResponseEntity with confirmation.
     * @throws Exception on file access or DAO error.
     */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteProductFile(final Map<String, Object> inputMap,final ProductFile objProductFile, final UserInfo objUserInfo)
			throws Exception {
		return productDAO.deleteProductFile(inputMap,objProductFile, objUserInfo);
	}
	
	/**
     * Retrieves a file stream or URL for attached product file for download/view through its DAO Layer.
     * 
     * @param inputMap     contains product code and file ID.
     * @param productFile  file to be viewed.
     * @param userInfo     user performing the view.
     * @return map with file metadata or stream reference.
     * @throws Exception if file not found or access denied.
     */
	@Override
	@Transactional
	public Map<String, Object> viewAttachedProductFile(Map<String, Object> inputMap,ProductFile objProductFile, UserInfo objUserInfo)
			throws Exception {
		return productDAO.viewAttachedProductFile(inputMap,objProductFile, objUserInfo);
	}

	/**
     * Retrieves files attached to a specific product through its DAO layer.
     * 
     * @param nproductcode product ID whose files are to be fetched.
     * @param userInfo     user performing the query.
     * @return ResponseEntity containing list of attached files.
     * @throws Exception on DAO error.
     */
	@Override
	public ResponseEntity<Object> getProductFile(Integer nproductcode,UserInfo userInfo) throws Exception {
		return productDAO.getProduct(nproductcode,userInfo);
	}	

}
