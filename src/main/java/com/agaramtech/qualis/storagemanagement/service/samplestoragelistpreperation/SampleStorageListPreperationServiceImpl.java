package com.agaramtech.qualis.storagemanagement.service.samplestoragelistpreperation;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform only read operation on
 * 'samplestoragelocation' table through its DAO layer.
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class SampleStorageListPreperationServiceImpl implements SampleStorageListPreperationService {

	private final SampleStorageListPreperationDAO sampleStorageListPreperationDAO;

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available sample repository(s) with respect to site
	 * @param inputMap [Map] map object holding details to be fetched data from samplestoragelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleRepositories records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getsamplestoragelistpreperation(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleStorageListPreperationDAO.getsamplestoragelistpreperation(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available sample repository(s) with respect to site and filter credentials
	 * @param inputMap [Map] map object holding details to read in bulkbarcodeconfigdetails table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleRepositories records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleStorageListPreperationDAO.getDynamicFilterExecuteData(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active sampleRepository Barcode
	 * object based on the specified nprojecttypecode and spositionvalue.
	 * @param inputMap [Map] map object holding details to read in view_sampleretrieval_0 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity object holding response status and data of sampleRepository Barcode
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSelectedBarcodeData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleStorageListPreperationDAO.getSelectedBarcodeData(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available sampleRepositories for the specified nprojecttypecode and site.
	 * @param inputMap [Map] map object holding details to read in bulkbarcodeconfigdetails table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleRepositories records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getProjectbarcodeconfig(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleStorageListPreperationDAO.getProjectbarcodeconfig(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available sample repository(s) with respect to site
	 * @param request [MultipartHttpServletRequest] multipart request holding details of input excel date and to read bulkbarcodeconfigdetails table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleRepositories records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getImportSampleIDData(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		return sampleStorageListPreperationDAO.getImportSampleIDData(request, userInfo);
	}

}