package com.agaramtech.qualis.contactmaster.service.supplier;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.contactmaster.model.Supplier;
import com.agaramtech.qualis.contactmaster.model.SupplierContact;
import com.agaramtech.qualis.contactmaster.model.SupplierFile;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'supplier' table
 * through its DAO layer.
 * 
 * @author ATE090
 * @version
 * @since 30- Jun- 2020
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SupplierServiceImpl implements SupplierService {

	
	private final SupplierDAO supplierDAO;
	private final CommonFunction commonFunction;

	public SupplierServiceImpl(SupplierDAO supplierDAO, CommonFunction commonFunction) {
		super();
		this.supplierDAO = supplierDAO;
		this.commonFunction = commonFunction;
	}


	/**
	 * This method is used to retrieve list of all active suppliers for the
	 * specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         suppliers
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSupplier(Integer nsupplierCode, final UserInfo userInfo) throws Exception {
		return supplierDAO.getSupplier(nsupplierCode,userInfo);
	}

	/**
	 * This method is used to retrieve active supplier object based on the specified
	 * nsupplierCode through its DAO layer.
	 * 
	 * @param nsupplierCode [int] primary key of supplier object
	 * @param userInfo      [UserInfo] object holding details of loggedin user
	 * @return response entity object holding response status and data of supplier
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSupplierById(int nsupplierCode, final UserInfo userInfo) throws Exception {
		final Supplier supplier = (Supplier) supplierDAO.getActiveSupplierById(nsupplierCode, userInfo);
		if (supplier == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(supplier, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to supplier table through its DAO
	 * layer.
	 * 
	 * @param supplier [Supplier] object holding details to be added in supplier
	 *                 table
	 * @return response entity object holding response status and data of added
	 *         supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSupplier(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return supplierDAO.createSupplier(inputMap, userInfo);
	}

	/**
	 * This method is used to update entry in supplier table through its DAO layer.
	 * 
	 * @param supplier [Supplier] object holding details to be updated in supplier
	 *                 table
	 * @return response entity object holding response status and data of updated
	 *         supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSupplier(Supplier supplier, UserInfo userInfo) throws Exception {
		return supplierDAO.updateSupplier(supplier, userInfo);
	}

	/**
	 * This method is used to delete entry in supplier table through its DAO layer.
	 * 
	 * @param supplier [Supplier] object holding detail to be deleted in supplier
	 *                 table
	 * @return response entity object holding response status and data of deleted
	 *         supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSupplier(Supplier supplier, UserInfo userInfo) throws Exception {
		return supplierDAO.deleteSupplier(supplier, userInfo);
	}

	/**
	 * This method is used to approve entry in supplier table through its DAO layer.
	 * 
	 * @param supplier [Supplier] object holding detail to be approved in supplier
	 *                 table
	 * @return response entity object holding response status and data of approved
	 *         supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> approveSupplier(Supplier supplier, UserInfo userInfo) throws Exception {

		return supplierDAO.approveSupplier(supplier, userInfo);
	}

	/**
	 * This method is used to blacklist entry in supplier table through its DAO
	 * layer.
	 * 
	 * @param supplier [Supplier] object holding detail to be blacklisted in
	 *                 supplier table
	 * @return response entity object holding response status and data of
	 *         blacklisted supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> blackListSupplier(Supplier supplier, UserInfo userInfo) throws Exception {

		return supplierDAO.blackListSupplier(supplier, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovedSupplier(UserInfo userInfo) throws Exception {

		return supplierDAO.getApprovedSupplier(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createSupplierFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		return supplierDAO.createSupplierFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateSupplierFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		return supplierDAO.updateSupplierFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteSupplierFile(final SupplierFile objSupplierFile, final UserInfo objUserInfo)
			throws Exception {
		return supplierDAO.deleteSupplierFile(objSupplierFile, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> editSupplierFile(SupplierFile objSupplierFile, UserInfo objUserInfo)
			throws Exception {
		return supplierDAO.editSupplierFile(objSupplierFile, objUserInfo);
	}

	@Transactional
	@Override
	public Map<String, Object> viewAttachedSupplierFile(SupplierFile objSupplierFile, UserInfo objUserInfo)
			throws Exception {
		return supplierDAO.viewAttachedSupplierFile(objSupplierFile, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getSupplierContact(final int nsuppliercode, UserInfo objUserInfo) throws Exception {
		return supplierDAO.getSupplierContact(nsuppliercode, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createSupplierContact(SupplierContact objSupplierContact, UserInfo userInfo)
			throws Exception {
		return supplierDAO.createSupplierContact(objSupplierContact, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateSupplierContact(SupplierContact objSupplierContact, UserInfo userInfo)
			throws Exception {
		return supplierDAO.updateSupplierContact(objSupplierContact, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteSupplierContact(SupplierContact objSupplierContact, UserInfo userInfo)
			throws Exception {
		return supplierDAO.deleteSupplierContact(objSupplierContact, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveSupplierContactById(int nsuppliercontactcode, final UserInfo userInfo)
			throws Exception {
		final SupplierContact objSupplierContact = (SupplierContact) supplierDAO
				.getActiveSupplierContactById(nsuppliercontactcode, userInfo);
		if (objSupplierContact == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objSupplierContact, HttpStatus.OK);
		}
	}

}
