package com.agaramtech.qualis.contactmaster.service.suppliermatrix;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.contactmaster.model.SupplierMatrix;
import com.agaramtech.qualis.global.UserInfo;



/**
 * This interface holds declarations to perform CRUD operation on 'supplier' table
 * through its implementation class.
 * @author ATE113
 * @version 
 * @since   12- Aug- 2020
 */
public interface SupplierMatrixDAO {

	/**
	 * This interface declaration is used to retrieve list of all active suppliermatrix for the
	 * specified site.
	 * @param nsuppliercode [int] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active suppliermatrix
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSupplierMatrix(final int nsuppliercode,final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active suppliermatrix object based
	 * on the specified nsupplierCode.
	 * @param nsuppliermatrixcode [int] primary key of supplier object
	 * @return supplier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public SupplierMatrix getActiveSupplierMatrixById(final int nsuppliermatrixcode,UserInfo userInfo) throws Exception ;
	
	 /**
		 * This interface declaration is used to add a new entry to suppliermatrix  table.
		 * @param suppliermatrix [SupplierMatrix] object holding details to be added in suppliermatrix table
		 * @return response entity object holding response status and data of added suppliermatrix object
		 * @throws Exception that are thrown in the DAO layer
		 */	
	public ResponseEntity<Object> createSupplierMatrix(List<SupplierMatrix> suppliermatrix, UserInfo userInfo) throws Exception;
	
	 /**
 	 * This interface declaration is used to update entry in suppliermatrix  table.
 	 * @param suppliermatrix [SupplierMatrix] object holding details to be updated in suppliermatrix table
 	 * @return response entity object holding response status and data of updated suppliermatrix object
 	 * @throws Exception that are thrown in the DAO layer
 	 */
	public ResponseEntity<Object> updateSupplierMatrix(SupplierMatrix supplierMatrix, UserInfo userInfo) throws Exception;
	
	/**
 	 * This interface declaration is used to delete entry in suppliermatrix  table.
 	 * @param suppliermatrix [SupplierMatrix] object holding detail to be deleted in suppliermatrix table
 	 * @return response entity object holding response status and data of deleted suppliermatrix object
 	 * @throws Exception that are thrown in the DAO layer
 	 */
	public ResponseEntity<Object> deleteSupplierMatrix(SupplierMatrix supplierMatrix, UserInfo userInfo) throws Exception;
}

