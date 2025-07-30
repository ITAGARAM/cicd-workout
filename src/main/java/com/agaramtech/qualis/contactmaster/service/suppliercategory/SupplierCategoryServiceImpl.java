package com.agaramtech.qualis.contactmaster.service.suppliercategory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.contactmaster.model.SupplierCategory;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on
 * 'contactmastersuppliercategory' table through its DAO layer.
 * 
 * @author ATE184
 * @version 9.0.0.1
 * @since 30- Jun- 2020
 */
@Service
public class SupplierCategoryServiceImpl implements SupplierCategoryService {


	
	private final SupplierCategoryDAO suppliercategoryDAO;
	private final CommonFunction commonFunction;

	public SupplierCategoryServiceImpl(SupplierCategoryDAO suppliercategoryDAO, CommonFunction commonFunction) {
		super();
		this.suppliercategoryDAO = suppliercategoryDAO;
		this.commonFunction = commonFunction;
	}

	

	/**
	 * This method is used to retrieve list of all active suppliercategory for the
	 * specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         suppliercategory
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSupplierCategory(UserInfo userInfo) throws Exception {
		return suppliercategoryDAO.getSupplierCategory(userInfo);
	}

	
	/**
	 * This method is used to retrieve list of all active suppliercategory for the
	 * specified site through its DAO layer
	 * 
	 * @param nsuppliercode [int] key of supplier object for which the list is to be
	 *                      fetched
	 * @return response entity object holding response status and list of all active
	 *         suppliercategory
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSupplierCategoryBySupplierCode(int nmasterSiteCode, int nsuppliercode)
			throws Exception {
		return suppliercategoryDAO.getSupplierCategoryBySupplierCode(nmasterSiteCode, nsuppliercode);
	}

	
	/**
	 * This method is used to retrieve active contactmastersuppliercategory object
	 * based on the specified nsuppliercatcode through its DAO layer.
	 * 
	 * @param nsuppliercatcode [int] primary key of contactmastersuppliercategory
	 *                         object
	 * @param userInfo         [UserInfo] holding details of loggedin user
	 * @return response entity object holding response status and data of
	 *         suppliercategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSupplierCategoryById(final int nsuppliercatcode, final UserInfo userInfo)
			throws Exception {
		final SupplierCategory suppliercategory = suppliercategoryDAO.getActiveSupplierCategoryById(nsuppliercatcode,userInfo);
		if (suppliercategory == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(suppliercategory, HttpStatus.OK);
		}
	}

	
	/**
	 * This method is used to add a new entry to contactmastersuppliercategory table
	 * through its DAO layer.
	 * 
	 * @param contactmasterSupplierCategory [SupplierCategory] object holding
	 *                                      details to be added in
	 *                                      contactmastersuppliercategory table
	 * @param userInfo                      [UserInfo] holding details of loggedin
	 *                                      user
	 * @return response entity object holding response status and data of added
	 *         suppliercategory object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Transactional
	@Override
	public ResponseEntity<Object> createSupplierCategory(SupplierCategory contactmasterSupplierCategory,
			UserInfo userInfo) throws Exception {
		return suppliercategoryDAO.createSupplierCategory(contactmasterSupplierCategory, userInfo);
	}

	
	/**
	 * This method is used to update entry in contactmastersuppliercategory table
	 * through its DAO layer.
	 * 
	 * @param contactmasterSupplierCategory [SupplierCategory] object holding
	 *                                      details to be updated in
	 *                                      contactmastersuppliercategory table
	 * @param userInfo                      [UserInfo] holding details of loggedin
	 *                                      user
	 * @return response entity object holding response status and data of updated
	 *         suppliercategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateSupplierCategory(SupplierCategory contactmasterSupplierCategory,
			UserInfo userInfo) throws Exception {

		return suppliercategoryDAO.updateSupplierCategory(contactmasterSupplierCategory, userInfo);
	}

	
	/**
	 * This method is used to delete entry in contactmastersuppliercategory table
	 * through its DAO layer.
	 * 
	 * @param contactmasterSupplierCategory [SupplierCategory] object holding detail
	 *                                      to be deleted in
	 *                                      contactmastersuppliercategory table
	 * @param userInfo                      [UserInfo] holding details of loggedin
	 *                                      user
	 * @return response entity object holding response status and data of deleted
	 *         suppliercategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSupplierCategory(SupplierCategory contactmasterSupplierCategory,
			UserInfo userInfo) throws Exception {

		return suppliercategoryDAO.deleteSupplierCategory(contactmasterSupplierCategory, userInfo);
	}

}
