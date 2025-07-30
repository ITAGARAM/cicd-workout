package com.agaramtech.qualis.storagemanagement.service.samplestoragemapping;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface SampleStorageMappingDAO {

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getSampleStorageMapping(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 *                 nsamplestoragelocationcode: 2
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> addSampleStorageMapping(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 *                 nsamplestoragelocationcode: 2
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getEditSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve list of available containerstructure(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}} If the
	 *                 nstoragecategorycode key not in the inputMap[],we take it as
	 *                 '0'
	 *                 ncontainertypecode: 2
	 * @return response entity object holding response status and list of all
	 *         containerstructure
	 * @throws Exception exception
	 */
	public ResponseEntity<Map<String, Object>> getContainerStructure(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,slanguagetypecode:"en-US"},nsamplestoragemappingcode:3}
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getActiveSampleStorageMappingById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched 
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	public ResponseEntity<Map<String, Object>> createSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched 
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	public ResponseEntity<Map<String, Object>> updateSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	public ResponseEntity<? extends Object> deleteSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> approveSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

//	public ResponseEntity<Object> getsamplestoragemappingSheetData(final Map<String, Object> inputMap,
//			final UserInfo userInfo) throws Exception;
//
//	public ResponseEntity<Object> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
//			final UserInfo userInfo) throws Exception;

}
