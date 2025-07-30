package com.agaramtech.qualis.storagemanagement.service.samplestoragemapping;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.service.temporarystorage.TemporaryStorageDAO;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SampleStorageMappingServiceImpl implements SampleStorageMappingService {

	
	private final SampleStorageMappingDAO sampleStorageMappingDAO;
	
	public SampleStorageMappingServiceImpl(SampleStorageMappingDAO sampleStorageMappingDAO) {
		super();
		this.sampleStorageMappingDAO = sampleStorageMappingDAO;
	}

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
	 * @throws Exception exception that are thrown in the dao layer
	 */
	@Override
	public ResponseEntity<Object> getSampleStorageMapping(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleStorageMappingDAO.getSampleStorageMapping(inputMap, userInfo);
	}
	
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
	 * @throws Exception exception that are thrown in the dao layer
	 */
	@Override
	public ResponseEntity<Object> addSampleStorageMapping(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sampleStorageMappingDAO.addSampleStorageMapping(inputMap, userInfo);
	}

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
	 * @throws Exception exception that are thrown in the dao layer
	 */
	@Override
	public ResponseEntity<Object> getEditSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleStorageMappingDAO.getEditSampleStorageMapping(inputMap, userInfo);
	}

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
	 * @throws Exception exception that are thrown in the dao layer
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getContainerStructure(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleStorageMappingDAO.getContainerStructure(inputMap, userInfo);
	}
	
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
	 * @throws Exception exception that are thrown in the dao layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleStorageMappingById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleStorageMappingDAO.getActiveSampleStorageMappingById(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched 
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception that are thrown in the dao layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> createSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleStorageMappingDAO.createSampleStorageMapping(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched 
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception that are thrown in the dao layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> updateSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleStorageMappingDAO.updateSampleStorageMapping(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched
	 *
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception that are thrown in the dao layer
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> deleteSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleStorageMappingDAO.deleteSampleStorageMapping(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available SampleStorageMapping(s).
	 *
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched
	 * @return response entity object holding response status and list of all
	 *         SampleStorageMapping
	 * @throws Exception exception that are thrown in the dao layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> approveSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sampleStorageMappingDAO.approveSampleStorageMapping(inputMap, userInfo);
	}

//	@Override
//	public ResponseEntity<Object> getsamplestoragemappingSheetData(final Map<String, Object> inputMap,
//			final UserInfo userInfo) throws Exception {
//		return sampleStorageMappingDAO.getsamplestoragemappingSheetData(inputMap, userInfo);
//	}
//
//	@Override
//	public ResponseEntity<Object> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
//			final UserInfo userInfo) throws Exception {
//		return sampleStorageMappingDAO.getDynamicFilterExecuteData(inputMap, userInfo);
//	}
}
