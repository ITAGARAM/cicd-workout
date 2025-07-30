package com.agaramtech.qualis.storagemanagement.service.bulkbarcodegeneration;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;

public interface BulkBarcodeGenerationService {

	/**
	 * This interface declaration is used to get the over all bulkbarcodegeneration
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of bulkbarcodegeneration and also
	 *         have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getBulkBarcodeGeneration(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to get the over all barcode config
	 * @param nprojecttype code [int] for which the list is to be fetched
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of barcode config and also
	 *         have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Map<String, Object>> getProjectBarcodceConfig(final int nprojecttypecode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to import a excel data to bulk barcode generation
	 * table.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of import
	 *         bulk barcode generation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> importBulkBarcodeGeneration(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all bulk barcode generation detail records.
	 * @param Map object holding nbulkbarcodeconfigcode, nbulkbarcodegenerationcode, fromDate, toDate, nprojecttypecode for which the list is to be fetched
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of bulk barcode generation and also
	 *         have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<? extends Object> getSelectedBarcodeGenerationDetail(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in bulk barcode generation table.
	 * 
	 * @param Map object holding the nbulkbarcodegenerationcode to be
	 *                           deleted in bulk barcode generation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         bulkbarcodegeneration object
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
	 * This interface declaration is used to delete entry in bulk barcode generation table.
	 * 
	 * @param Map object holding the nbulkbarcodegenerationcode to be
	 *                           deleted in bulk barcode generation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         bulkbarcodegeneration object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<? extends Object> deleteBarcodeData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

}
