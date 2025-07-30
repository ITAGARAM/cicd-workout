package com.agaramtech.qualis.contactmaster.service.suppliermatrix;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.contactmaster.model.SupplierMatrix;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on 'supplier' table
 * through its DAO layer.
 * @author ATE090
 * @version 
 * @since   30- Jun- 2020
 */
public interface SupplierMatrixService {

	/**
	 * This interface declaration is used to retrieve list of all active suppliermatrix for the
	 * specified site through its DAO layer
	 * @param nsupplierCode [int] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active suppliermatrix
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSupplierMatrix(final int nsupplierCode, final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active suppliermatrix object based
	 * on the specified nsuppliermatrixcode through its DAO layer.
	 * @param nsuppliermatrixcode [int] primary key of suppliermatrix object
	 * @param  userInfo [UserInfo] object holding details of loggedin user 
	 * @return response entity  object holding response status and data of suppliermatrix object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveSupplierMatrixById(final int nsuppliermatrixcode, final UserInfo userInfo) throws Exception ;
	
	/**
	* This interface declaration is used to add a new entry to suppliermatrix  table through its DAO layer.
	* @param suppliermatrix [SupplierMatrix] object holding details to be added in suppliermatrix table
	* @return response entity object holding response status and data of added suppliermatrix object
	* @throws Exception that are thrown in the DAO layer
	*/
	public ResponseEntity<Object> createSupplierMatrix(List<SupplierMatrix> supplierMatrix, UserInfo userInfo) throws Exception;
	
	 /**
	 * This interface declaration is used to update entry in suppliermatrix  table through its DAO layer.
	 * @param suppliermatrix [SupplierMatrix] object holding details to be updated in suppliermatrix table
	 * @return response entity object holding response status and data of updated suppliermatrix object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSupplierMatrix(SupplierMatrix supplierMatrix, UserInfo userInfo) throws Exception;
	
	 /**
     * This interface declaration is used to delete entry in suppliermatrix  table through its DAO layer.
	 * @param suppliermatrix [SupplierMatrix] object holding detail to be deleted in suppliermatrix table
	 * @return response entity object holding response status and data of deleted suppliermatrix object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSupplierMatrix(SupplierMatrix supplierMatrix, UserInfo userInfo) throws Exception;
	
}

