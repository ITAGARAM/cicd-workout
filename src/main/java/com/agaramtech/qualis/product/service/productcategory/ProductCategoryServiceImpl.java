package com.agaramtech.qualis.product.service.productcategory;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.ProductCategory;

/**
 * This class holds methods to perform CRUD operation on 'productcategory' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

	private final ProductCategoryDAO productCategoryDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param productCategoryDAO ProductCategoryDAO Interface
	 * @param commonFunction     CommonFunction holding common utility functions
	 */
	public ProductCategoryServiceImpl(ProductCategoryDAO productCategoryDAO, CommonFunction commonFunction) {
		this.productCategoryDAO = productCategoryDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This interface declaration is used to retrieve list of all active Product
	 * Category for the specified site through its DAO layer
	 * 
	 * @param inputMap [Map] map object with "userinfo" as key for which the list is
	 *                 to be fetched
	 * @return response object with list of active Product Category that are to be
	 *         listed for the specified site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProductCategory(final UserInfo userInfo) throws Exception {
		return productCategoryDAO.getProductCategory(userInfo);
	}

	/**
	 * This interface declaration is used to retrieve active Product Category object
	 * based on the specified nproductcatcode through its DAO layer.
	 * 
	 * @param inputMap [Map] holds the Primary Key["nproductcatcode"] code and
	 *                 userinfo object entity
	 * @return response entity object holding response status and data of single
	 *         Product category object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveProductCategoryById(final int nproductcatcode, final UserInfo userInfo)
			throws Exception {
		final ProductCategory objProductCategory = productCategoryDAO.getActiveProductCategoryById(nproductcatcode,
				userInfo);
		if (objProductCategory == null) {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<Object>(objProductCategory, HttpStatus.OK);
		}
	}

	/**
	 * This interface declaration is used to create a new Product Category through
	 * its DAO layer.
	 * 
	 * @param inputMap [Map] object with keys of Product Category [Product Category]
	 *                 and UserInfo object entity.
	 * @return response entity for List of Product Category entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createProductCategory(final Map<String, Object> inputMap,
			final ProductCategory productCategory, final UserInfo userInfo) throws Exception {
		return productCategoryDAO.createProductCategory(inputMap, productCategory, userInfo);
	}

	/**
	 * This interface declaration is used to update entry in Product Category table
	 * through its DAO layer.
	 * 
	 * @param inputMap [Map] object with keys of productcategory [Product Category]
	 *                 and UserInfo object entity.
	 * @return response entity List of Product Category entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateProductCategory(final Map<String, Object> inputMap,
			final ProductCategory productCategory, final UserInfo userInfo) throws Exception {
		return productCategoryDAO.updateProductCategory(inputMap, productCategory, userInfo);
	}

	/**
	 * This interface declaration is used to delete entry in Product Category table
	 * through its DAO layer.
	 * 
	 * @param mapObject [Map] object with keys of productcategory [Product Category]
	 *                  and UserInfo object entity.
	 * @return response entity of deleted Product Category entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteProductCategory(Map<String, Object> inputMap, ProductCategory productCategory,
			UserInfo userInfo) throws Exception {
		return productCategoryDAO.deleteProductCategory(inputMap, productCategory, userInfo);
	}

}
