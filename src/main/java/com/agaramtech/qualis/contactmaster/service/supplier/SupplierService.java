package com.agaramtech.qualis.contactmaster.service.supplier;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.contactmaster.model.Supplier;
import com.agaramtech.qualis.contactmaster.model.SupplierContact;
import com.agaramtech.qualis.contactmaster.model.SupplierFile;
import com.agaramtech.qualis.global.UserInfo;



/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'supplier' table through its DAO layer.
 * 
 * @author ATE090
 * @version
 * @since 30- Jun- 2020
 */
public interface SupplierService {

	/**
	 * This interface declaration is used to retrieve list of all active supplier
	 * for the specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         suppliers
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSupplier(Integer nsupplierCode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active supplier object based
	 * on the specified nsupplierCode through its DAO layer.
	 * Integer
	 * @param nsupplierCode [int] primary key of supplier object
	 * @param userInfo      [UserInfo] object holding details of loggedin user
	 * @return response entity object holding response status and data of supplier
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveSupplierById(final int nsupplierCode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to supplier table
	 * through its DAO layer.
	 * 
	 * @param supplier [Supplier] object holding details to be added in supplier
	 *                 table
	 * @return response entity object holding response status and data of added
	 *         supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSupplier(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in supplier table through
	 * its DAO layer.
	 * 
	 * @param supplier [Supplier] object holding details to be updated in supplier
	 *                 table
	 * @return response entity object holding response status and data of updated
	 *         supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSupplier(Supplier supplier, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in supplier table through
	 * its DAO layer.
	 * 
	 * @param supplier [Supplier] object holding detail to be deleted in supplier
	 *                 table
	 * @return response entity object holding response status and data of deleted
	 *         supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSupplier(Supplier supplier, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to approve entry in supplier table through
	 * its DAO layer.
	 * 
	 * @param supplier [Supplier] object holding detail to be approved in supplier
	 *                 table
	 * @return response entity object holding response status and data of approved
	 *         supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> approveSupplier(Supplier supplier, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to blacklist entry in supplier table
	 * through its DAO layer.
	 * 
	 * @param supplier [Supplier] object holding detail to be blacklisted in
	 *                 supplier table
	 * @return response entity object holding response status and data of
	 *         blacklisted supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> blackListSupplier(Supplier supplier, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve list of all active approved
	 * supplier for the specified site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         suppliers
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getApprovedSupplier(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createSupplierFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> updateSupplierFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> deleteSupplierFile(final SupplierFile objSupplierFile, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> editSupplierFile(final SupplierFile objSupplierFile, final UserInfo objUserInfo)
			throws Exception;

	public Map<String, Object> viewAttachedSupplierFile(final SupplierFile objSupplierFile, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> getSupplierContact(final int nsuppliercode, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> createSupplierContact(SupplierContact objSupplierContact, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateSupplierContact(SupplierContact objSupplierContact, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteSupplierContact(SupplierContact objSupplierContact, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveSupplierContactById(final int nsuppliercontactcode, UserInfo userInfo)
			throws Exception;

}
