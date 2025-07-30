package com.agaramtech.qualis.contactmaster.service.suppliermatrix;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.contactmaster.model.SupplierMatrix;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;



/**
 * This class holds methods to perform CRUD operation on 'suppliermatrix' table
 * through its DAO layer.
 * @author ATE113
 * @version 
 * @since   12- Aug- 2020
 */
@Service
public class SupplierMatrixServiceImpl implements SupplierMatrixService{


	
	private final SupplierMatrixDAO supplierMatrixDAO;
	private final CommonFunction commonFunction;

	public SupplierMatrixServiceImpl(SupplierMatrixDAO supplierMatrixDAO, CommonFunction commonFunction) {
		super();
		this.supplierMatrixDAO = supplierMatrixDAO;
		this.commonFunction = commonFunction;
	}

	
	/**
	 * This method is used to retrieve list of all active suppliermatrix for the
	 * specified site through its DAO layer
	 * @param nsupplierCode [int] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active suppliermatrix
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSupplierMatrix(final int nsupplierCode, final UserInfo userInfo) throws Exception {		
		return supplierMatrixDAO.getSupplierMatrix(nsupplierCode,userInfo);
	}

	/**
	 * This method is used to retrieve active suppliermatrix object based
	 * on the specified nsuppliermatrixCode through its DAO layer.
	 * @param nsuppliermatrixCode [int] primary key of suppliermatrix object
	 * @param userInfo [UserInfo] object holding details of loggedin user
	 * @return response entity  object holding response status and data of suppliermatrix object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSupplierMatrixById(int nsuppliermatrixCode, final UserInfo userInfo) throws Exception {		
		final SupplierMatrix supplierMatrix = (SupplierMatrix) supplierMatrixDAO.getActiveSupplierMatrixById(nsuppliermatrixCode,userInfo);
		if (supplierMatrix == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
			return new ResponseEntity<>(supplierMatrix, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to suppliermatrix  table through its DAO layer.
	 * @param suppliermatrix [SupplierMatrix] object holding details to be added in suppliermatrix table
	 * @return response entity object holding response status and data of added suppliermatrix object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSupplierMatrix(List<SupplierMatrix> supplierMatrix, UserInfo userInfo) throws Exception {		
		return supplierMatrixDAO.createSupplierMatrix(supplierMatrix, userInfo);
	}

	/**
	 * This method is used to update entry in supplierMatrix  table through its DAO layer.
	 * @param suppliermatrix [SupplierMatrix] object holding details to be updated in suppliermatrix table
	 * @return response entity object holding response status and data of updated suppliermatrix object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSupplierMatrix(SupplierMatrix supplierMatrix, UserInfo userInfo) throws Exception {		
		return supplierMatrixDAO.updateSupplierMatrix(supplierMatrix, userInfo);
	}

	/**
	 * This method is used to delete entry in suppliermatrix  table through its DAO layer.
	 * @param suppliermatrix [SupplierMatrix] object holding detail to be deleted in suppliermatrix table
	 * @return response entity object holding response status and data of deleted suppliermatrix object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSupplierMatrix(SupplierMatrix supplierMatrix, UserInfo userInfo) throws Exception {		
		return supplierMatrixDAO.deleteSupplierMatrix(supplierMatrix, userInfo);
	}
	
}

