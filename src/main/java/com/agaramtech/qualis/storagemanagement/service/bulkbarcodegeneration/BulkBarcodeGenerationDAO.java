package com.agaramtech.qualis.storagemanagement.service.bulkbarcodegeneration;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;

public interface BulkBarcodeGenerationDAO {

	/**
	 * This interface declaration is used to get the over all bulkbarcodegeneration.
	 * 
	  * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of bulkbarcodegeneration and also
	 *         have the HTTP response code
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getBulkBarcodeGeneration(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to get the over all barcode config based on project type.
	 *
	 * * @param nprojecttype code [int] for which the list is to be fetched
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of barcode config and also
	 *         have the HTTP response code
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Map<String, Object>> getProjectBarcodceConfig(final int nprojecttypecode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to import list of all Excel data .
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched and excel sheet with the
	 *                 data.
	 * @return response entity object holding response status and list of all
	 *         active bulkbarcode generation and status code.
	 * @throws Exception that are thrown from this DAO layer.
	 *
	 */
	public ResponseEntity<Object> importBulkBarcodeGeneration(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve list of all active bulkbarcodegeneration detail records for
	 * the specified site.
	 * 
	 * @param Map object holding nbulkbarcodeconfigcode, nbulkbarcodegenerationcode, fromDate, toDate, nprojecttypecode
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         bulkbarcodegeneration detail records and status code
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<? extends Object> getSelectedBarcodeGenerationDetail(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in bulkbarcode generation table.
	 * 
	 * @param Map object holding the nbulkbarcodegenerationcode to be
	 *                           deleted in bulk barcode generation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         bulkbarcode generation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteBulkBarcodeGeneration(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/*
	 * This interface declaration is used for printing single or multiple barcode IDs. The logic is
	 * query-based and includes an audit trail.
	 * @param Map object holds the userinfo object, printer name, sbarcodename, ncontrolcode
	 * @return response entity print barcode file.
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> printBarcode(final Map<String, Object> inputMap) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in bulkbarcodegeneration table.
	 * @param Map object holds the nbulkbarcodegenerationcode  to be
	 *                           deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with corresponding HTTP status and an
	 *         bulkbarcodegeneration detail object
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<? extends Object> deleteBarcodeData(final Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

}
