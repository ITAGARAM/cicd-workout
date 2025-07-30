package com.agaramtech.qualis.bulkbarcodeconfiguration.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.basemaster.model.BulkBarcodeConfigDetails;
import com.agaramtech.qualis.bulkbarcodeconfiguration.model.BulkBarcodeConfig;
import com.agaramtech.qualis.global.UserInfo;

public interface BulkBarcodeConfigService {

	/**
	 * This service interface declaration will access the DAO layer that is used 
	 * to get all the available bulkbarcodeconfig with respect to site	
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for  which the list is to be fetched
	 * @return a response entity which holds the list of bulkbarcodeconfig records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */	
	public ResponseEntity<Object> getBulkBarcodeConfiguration(final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve a list of all active barcodemaster entries.
	 * @param nbulkbarcodeconfigcode[int] bulk Barcode Config
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for  which the list is to be fetched
	 * @return response entity  object holding response status and list of all barcodemaster
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getBarcodeMaster(final UserInfo userInfo, final int nbulkbarcodeconfigcode)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to make a new entry to bulkbarcodeconfig  table.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfig [BulkBarcodeConfig]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details.	
	 * @return ResponseEntity with string message as 'Already Exist' if the configuration name already exist for the selected project type / 
	 * 			list of bulkbarcodeconfig entries along with the newly added bulkbarcodeconfig .
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> createBulkBarcodeConfiguration(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to update selected bulkbarcodeconfig details.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfig [BulkBarcodeConfig]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details
	 * @return ResponseEntity with bulkbarcodeconfig object for the specified primary key / with string message as
	 * 						'Already Deleted' if the bulkbarcodeconfig record is not available/with string message as 
	 * 						'Already Exist' if the configuration name already exist for the selected project type
	 * @throws Exception exception
	 */	
	public ResponseEntity<Object> updateBulkBarcodeConfiguration(BulkBarcodeConfig objBulkBarcodeConfig,
			UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to delete an entry in bulkbarcodeconfig table
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched	
	 * @return ResponseEntity with string message as 'Already deleted' if the bulkbarcodeconfig record is not available/ 
	 * 			string message as 'Record is used in....' when the bulkbarcodeconfig is associated in transaction /
	 * 			list of all bulkbarcodeconfig excluding the deleted record 
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> deleteBulkBarcodeConfiguration(BulkBarcodeConfig objBulkBarcodeConfig,
			UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve a specific bulkbarcodeconfig record.
	 * @param inputMap  [Map] map object with "nbulkbarcodeconfigcode" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with bulkbarcodeconfig object for the specified primary key / with string message as
	 * 						'Already Deleted' if the bulkbarcodeconfig record is not available
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getActiveBulkBarcodeConfigurationById(final int nbulkbarcodeconfigcode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to update selected bulkbarcodeconfigversion table.
	 * @param inputMap  [Map] map object with "bulkbarcodeconfig" and "userInfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with string message as 'Already Deleted' if the bulkbarcodeconfig record is not available/ 
	 * list of all bulkbarcodeconfig and along with the updated bulkbarcodeconfig and bulkbarcodeconfigversion entries.	 
	 * @throws Exception exception
	 */	
	public ResponseEntity<Object> approveBulkBarcodeConfiguration(BulkBarcodeConfig objBulkBarcodeConfig,
			UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve a list of all active barcodemaster entries for the selected project type.
	 * @param inputMap  [Map] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity object containing the response status and a map of key-value pairs,
	 * which includes a list of mapped project types with their active BulkBarcodeConfig data,
	 * related processes, and additional details of the selected project type and barcode details.
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getFilterProjectType(final UserInfo userInfo, final int nprojecttypecode)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve a specific bulkbarcodeconfig record.
	 * @param inputMap  [Map] map object with "nprojecttypecode","nbulkbarcodeconfigcode" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with bulkbarcodeconfig object for the specified primary key / with string message as
	 * 						'Already Deleted' if the bulkbarcodeconfig record is not available
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getBulkBarcodeConfig(final int nbulkbarcodeconfigcode, final int nprojecttypecode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that will is used to make a new entry to bulkbarcodeconfigdetails  table for the selected bulkbarcodeconfig.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfigdetails [BulkBarcodeConfigDetails]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details
	 * @return ResponseEntity with string message as 'Already Exist' if the Sorter already exist for the selected bulkbarcodeconfig / 
	 * 			list of bulkbarcodeconfigdetails entries along with the newly added bulkbarcodeconfigdetails .
	 * @throws Exception exception
	 */
	public ResponseEntity<? extends Object> createBulkBarcodeMaster(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve a list of all active barcodemaster entries for the selected project type.
	 * @param inputMap  [Map] map object with "nprojecttypecode","stablename" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with Field Length object for the specified nprojecttypecode.
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getFieldLengthService(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to update selected bulkbarcodeconfigdetails details.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfigdetails [BulkBarcodeConfigDetails]  object holding details to be added in bulkbarcodeconfigdetails table,
	 * 								userinfo [UserInfo] holding logged in user details
	 * @return ResponseEntity with bulkbarcodeconfigdetails object for the specified primary key / with string message as
	 * 						'Already Deleted' if the bulkbarcodeconfigdetails record is not available/with string message as 
	 * 						'Already Exist' if the sorter already exist for the selected project type
	 * @throws Exception exception
	 */
	public ResponseEntity<? extends Object> updateBulkBarcodeMaster(BulkBarcodeConfigDetails objBulkBarcodeConfigDetails,
			UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to delete an entry in bulkbarcodeconfigdetails table
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with string message as 'Already deleted' if the bulkbarcodeconfigdetails record is not available/ 
	 * 			string message as 'Record is used in....' when the bulkbarcodeconfigdetails is associated in transaction /
	 * 			list of all bulkbarcodeconfigdetails excluding the deleted record 
	 * @throws Exception exception
	 */
	public ResponseEntity<? extends Object> deleteBulkBarcodeMaster(BulkBarcodeConfigDetails objBulkBarcodeConfigDetails,
			UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve a specific bulkbarcodeconfigdetails record.
	 * @param inputMap  [Map] map object with "nbulkbarcodeconfigdetailscode" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with bulkbarcodeconfigdetails object for the specified primary key / with string message as
	 * 						'Already Deleted' if the bulkbarcodeconfigdetails record is not available
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getActiveBulkBarcodeMasterById(final int nbulkbarcodeconfigdetailscode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve a list of all active ParentBarcodeDetails entries for the selected project type and Bulk Barcode Config.
	 * @param inputMap  [Map] map object with "nprojecttypecode","nbulkbarcodeconfigcode" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity object containing the response status and a map of key-value pairs,
	 * which includes a list of mapped project types with their active ParentBarcodeDetails data.
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getParentBulkBarcodeMaster(final int nbulkbarcodeconfigcode,
			final int nprojecttypecode, final UserInfo userInfo, Map<String, Object> inputMap) throws Exception;

}
