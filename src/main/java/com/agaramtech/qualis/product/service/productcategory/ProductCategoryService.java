package com.agaramtech.qualis.product.service.productcategory;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.ProductCategory;

public interface ProductCategoryService {

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
	public ResponseEntity<Object> getProductCategory(final UserInfo userinfo) throws Exception;

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
	public ResponseEntity<Object> getActiveProductCategoryById(final int nproductcatcode, final UserInfo userinfo)
			throws Exception;

	/**
	 * This interface declaration is used to create a new Product Category through
	 * its DAO layer.
	 * 
	 * @param inputMap [Map] object with keys of Product Category [Product Category]
	 *                 and UserInfo object entity.
	 * @return response entity for List of Product Category entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createProductCategory(final Map<String, Object> inputMap,
			final ProductCategory ProductCategory, final UserInfo UserInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in Product Category table
	 * through its DAO layer.
	 * 
	 * @param inputMap [Map] object with keys of productcategory [Product Category]
	 *                 and UserInfo object entity.
	 * @return response entity List of Product Category entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateProductCategory(final Map<String, Object> inputMap,
			final ProductCategory ProductCategory, final UserInfo UserInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in Product Category table
	 * through its DAO layer.
	 * 
	 * @param mapObject [Map] object with keys of productcategory [Product Category]
	 *                  and UserInfo object entity.
	 * @return response entity of deleted Product Category entity
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteProductCategory(final Map<String, Object> inputMap,
			final ProductCategory ProductCategory, final UserInfo UserInfo) throws Exception;

}
