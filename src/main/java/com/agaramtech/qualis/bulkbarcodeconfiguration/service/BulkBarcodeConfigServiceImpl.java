package com.agaramtech.qualis.bulkbarcodeconfiguration.service;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.basemaster.model.BulkBarcodeConfigDetails;
import com.agaramtech.qualis.bulkbarcodeconfiguration.model.BulkBarcodeConfig;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'bulkbarcodeconfig','bulkbarcodeconfigdetails' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class) 
@Service 

public class BulkBarcodeConfigServiceImpl implements BulkBarcodeConfigService {
	
	
	private final BulkBarcodeConfigDAO bulkBarcodeConfigDAO;
	final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param bulkBarcodeConfigDAO BulkBarcodeConfigDAO Interface
	 * @param commonFunction CommonFunction Interface

	 */
	public BulkBarcodeConfigServiceImpl(BulkBarcodeConfigDAO bulkBarcodeConfigDAO,CommonFunction commonFunction) {
		this.bulkBarcodeConfigDAO = bulkBarcodeConfigDAO;
		this.commonFunction = commonFunction;
		
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available bulkbarcodeconfig with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of bulkbarcodeconfig records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getBulkBarcodeConfiguration(final UserInfo userInfo) throws Exception {
		return bulkBarcodeConfigDAO.getBulkBarcodeConfiguration(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available barcodemaster entries
	 * @param nprojecttypecode [int] Project Type Code.
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of barcodemaster records 
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getBarcodeMaster(final UserInfo userInfo,final int nbulkbarcodeconfigcode) throws Exception {
		return bulkBarcodeConfigDAO.getBarcodeMaster(userInfo,nbulkbarcodeconfigcode);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to bulkbarcodeconfig  table.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfig [BulkBarcodeConfig]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of added bulkbarcodeconfig object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createBulkBarcodeConfiguration(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bulkBarcodeConfigDAO.createBulkBarcodeConfiguration(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in bulkbarcodeconfig  table.
		 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfig [BulkBarcodeConfig]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details.

	 * @return response entity object holding response status and data of updated bulkbarcodeconfig object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateBulkBarcodeConfiguration(BulkBarcodeConfig objBulkBarcodeConfig,
			final UserInfo userInfo) throws Exception {
		return bulkBarcodeConfigDAO.updateBulkBarcodeConfiguration(objBulkBarcodeConfig, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in bulkbarcodeconfig  table.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfig [BulkBarcodeConfig]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of deleted bulkbarcodeconfig object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteBulkBarcodeConfiguration(BulkBarcodeConfig objBulkBarcodeConfig,
			final UserInfo userInfo) throws Exception {
		return bulkBarcodeConfigDAO.deleteBulkBarcodeConfiguration(objBulkBarcodeConfig, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active bulkbarcodeconfig object 
	 * @param inputMap  [Map] map object with "nbulkbarcodeconfigcode" and "userInfo" as keys for which the data is to be fetched
	 * @return response entity  object holding response status and data of bulkbarcodeconfig object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveBulkBarcodeConfigurationById(int nbulkbarcodeconfigcode,
			final UserInfo userInfo) throws Exception {
		final BulkBarcodeConfig objBulkBarcodeConfig = bulkBarcodeConfigDAO
				.getActiveBulkBarcodeConfigurationById(nbulkbarcodeconfigcode, userInfo);
		if (objBulkBarcodeConfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objBulkBarcodeConfig, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to update selected bulkbarcodeconfigversion table.
	 * @param inputMap  [Map] map object with "bulkbarcodeconfig" and "userInfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with string message as 'Already Deleted' if the bulkbarcodeconfig record is not available/ 
	 * list of all bulkbarcodeconfig and along with the updated bulkbarcodeconfig and bulkbarcodeconfigversion entries.	 
	 * @throws Exception exception
	 */	
	@Transactional
	@Override
	public ResponseEntity<Object> approveBulkBarcodeConfiguration(BulkBarcodeConfig objBulkBarcodeConfig,
			final UserInfo userInfo) throws Exception {
		return bulkBarcodeConfigDAO.approveBulkBarcodeConfiguration(objBulkBarcodeConfig, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve a list of all active barcodemaster table for the selected project type.
	 * @param inputMap  [Map] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of deleted bulkbarcodeconfig object
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getFilterProjectType(final UserInfo userInfo, final int nprojecttypecode)
			throws Exception {
		return bulkBarcodeConfigDAO.getFilterProjectType(userInfo, nprojecttypecode);
	}

	/**
	 *  This service implementation method will access the DAO layer that is used to retrieve a specific bulkbarcodeconfig record.
	 * @param inputMap  [Map] map object with "nprojecttypecode","nbulkbarcodeconfigcode" and "userinfo" as keys for which the data is to be fetched
	 * @return ResponseEntity with bulkbarcodeconfig object for the nbulkbarcodeconfigcode.
	 * @throws Exception that are thrown from this DAO layer.
	 */
	@Override
	public ResponseEntity<Object> getBulkBarcodeConfig(final int nbulkbarcodeconfigcode, final int nprojecttypecode,
			final UserInfo userInfo) throws Exception {
		return bulkBarcodeConfigDAO.getBulkBarcodeConfig(nbulkbarcodeconfigcode, nprojecttypecode, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to bulkbarcodeconfigdetails  table.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfigdetails [BulkBarcodeConfigDetails]  object holding details to be added in bulkbarcodeconfigdetails table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of added bulkbarcodeconfigdetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> createBulkBarcodeMaster(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bulkBarcodeConfigDAO.createBulkBarcodeMaster(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active barcodemaster entries for the selected project type. 
	 * @param userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity with Field Length object for the specified nprojecttypecode.
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getFieldLengthService(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bulkBarcodeConfigDAO.getFieldLengthService(inputMap, userInfo);
	}
	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in bulkbarcodeconfigdetails  table.
		 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfigdetails [BulkBarcodeConfigDetails]  object holding details to be added in bulkbarcodeconfigdetails table,
	 * 								userinfo [UserInfo] holding logged in user details.

	 * @return response entity object holding response status and data of updated bulkbarcodeconfigdetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> updateBulkBarcodeMaster(BulkBarcodeConfigDetails objBulkBarcodeConfigdetails,
			final UserInfo userInfo) throws Exception {
		return bulkBarcodeConfigDAO.updateBulkBarcodeMaster(objBulkBarcodeConfigdetails, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in bulkbarcodeconfigdetails  table.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfigdetails [BulkBarcodeConfigDetails]  object holding details to be added in bulkbarcodeconfigdetails table,
	 * 								userinfo [UserInfo] holding logged in user details.
	 * @return response entity object holding response status and data of deleted bulkbarcodeconfigdetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> deleteBulkBarcodeMaster(BulkBarcodeConfigDetails objBulkBarcodeConfigdetails,
			final UserInfo userInfo) throws Exception {
		return bulkBarcodeConfigDAO.deleteBulkBarcodeMaster(objBulkBarcodeConfigdetails, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active bulkbarcodeconfigdetails object 
	 * @param inputMap  [Map] map object with "nbulkbarcodeconfigdetailscode" and "userInfo" as keys for which the data is to be fetched
	 * @return response entity  object holding response status and data of bulkbarcodeconfigdetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveBulkBarcodeMasterById(int nbulkbarcodeconfigdetailscode,
			final UserInfo userInfo) throws Exception {
		final BulkBarcodeConfigDetails objBulkBarcodeConfigdetails = bulkBarcodeConfigDAO
				.getActiveBulkBarcodeMasterById(nbulkbarcodeconfigdetailscode, userInfo);
		if (objBulkBarcodeConfigdetails == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objBulkBarcodeConfigdetails, HttpStatus.OK);
		}
	}
	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active ParentBarcodeDetails object 
	 * @param inputMap  [Map] map object with "nbulkbarcodeconfigcode"[int],"nprojecttypecode"[int] and "userInfo" as keys for which the data is to be fetched
	 * @return response entity  object holding response status and data of ParentBarcodeDetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getParentBulkBarcodeMaster(final int nbulkbarcodeconfigcode, final int nprojecttypecode,
			final UserInfo userInfo,Map<String, Object> inputMap) throws Exception {
		return bulkBarcodeConfigDAO.getParentBulkBarcodeMaster(nbulkbarcodeconfigcode, nprojecttypecode, userInfo, inputMap);
	}
}
