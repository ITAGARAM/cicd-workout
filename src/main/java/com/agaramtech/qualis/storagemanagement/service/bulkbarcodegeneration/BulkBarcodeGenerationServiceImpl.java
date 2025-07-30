package com.agaramtech.qualis.storagemanagement.service.bulkbarcodegeneration;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'bulkbarcodegeneration' table through
 * its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class BulkBarcodeGenerationServiceImpl implements BulkBarcodeGenerationService {

	private final BulkBarcodeGenerationDAO bulkBarcodeGenerationDAO;

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available bulkbarcodegeneration with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of bulk barcode generation records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getBulkBarcodeGeneration(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bulkBarcodeGenerationDAO.getBulkBarcodeGeneration(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available barcode config with respect to site
	 * @param nprojecttype code [int] for which the list is to be fetched
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of barcode config records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@Override
	public ResponseEntity<Map<String, Object>> getProjectBarcodceConfig(final int nprojecttypecode, final UserInfo userInfo)
			throws Exception {
		return bulkBarcodeGenerationDAO.getProjectBarcodceConfig(nprojecttypecode, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * import the list of all Excel data.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of import excel data
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> importBulkBarcodeGeneration(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		return bulkBarcodeGenerationDAO.importBulkBarcodeGeneration(request, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available bulk barcode generation detail records with respect to site
	 * @param Map object holding nbulkbarcodeconfigcode, nbulkbarcodegenerationcode, fromDate, toDate, nprojecttypecode
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of bulk barcode generation detail records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@Override
	public ResponseEntity<? extends Object> getSelectedBarcodeGenerationDetail(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bulkBarcodeGenerationDAO.getSelectedBarcodeGenerationDetail(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in bulkbarcode generation  table.
	 * @param Map object holding the nbulkbarcodegenerationcode to be
	 *                           deleted in bulk barcode generation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted bulk barcode generation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteBulkBarcodeGeneration(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bulkBarcodeGenerationDAO.deleteBulkBarcodeGeneration(inputMap, userInfo);
	}

	/*
	 * This service implementation method will access the DAO layer that is used to printing single or multiple barcode IDs. The logic is
	 * query-based and includes an audit trail.
	 * @param Map object holds the userinfo object, printer name, sbarcodename, ncontrolcode
	 * @return response entity print barcode file.
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> printBarcode(final Map<String, Object> inputMap) throws Exception {
		return bulkBarcodeGenerationDAO.printBarcode(inputMap);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in bulkbarcode generation  table.
	 * @param Map object holding the nbulkbarcodegenerationcode to be
	 *                           deleted in bulk barcode generation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted bulk barcode generation detail object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> deleteBarcodeData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bulkBarcodeGenerationDAO.deleteBarcodeData(inputMap, userInfo);
	}

}
