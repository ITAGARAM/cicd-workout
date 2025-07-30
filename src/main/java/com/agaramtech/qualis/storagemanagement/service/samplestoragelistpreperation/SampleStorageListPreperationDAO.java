package com.agaramtech.qualis.storagemanagement.service.samplestoragelistpreperation;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform only read operation on
 * 'samplestoragelocation' table
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
public interface SampleStorageListPreperationDAO {

	/**
	 * This service interface declaration is used to get all the available sample repository(s) with respect to site.
	 * @param inputMap [Map] map object holding details to be fetched data from samplestoragelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleRepositories records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getsamplestoragelistpreperation(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration is used to get all the available sample repository(s) with respect to site and filter credentials.
	 * @param inputMap [Map] map object holding details to read in bulkbarcodeconfigdetails table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleRepositories records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Map<String, Object>> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration is used to retrieve active sampleRepository Barcode
	 * object based on the specified nprojecttypecode and spositionvalue.
	 * @param inputMap [Map] map object holding details to read in view_sampleretrieval_0 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity object holding response status and data of sampleRepository Barcode
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSelectedBarcodeData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration is used to get all the available sampleRepositories for the specified nprojecttypecode and site.
	 * @param inputMap [Map] map object holding details to read in bulkbarcodeconfigdetails table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleRepositories records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Map<String, Object>> getProjectbarcodeconfig(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration is used to get all the available sample repository(s) with respect to site.
	 * @param request [MultipartHttpServletRequest] multipart request holding details of input excel date and to read bulkbarcodeconfigdetails table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampleRepositories records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getImportSampleIDData(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception;
}
