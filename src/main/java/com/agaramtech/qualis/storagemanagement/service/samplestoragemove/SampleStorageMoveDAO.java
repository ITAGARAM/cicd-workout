package com.agaramtech.qualis.storagemanagement.service.samplestoragemove;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'studyidentity' table
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
public interface SampleStorageMoveDAO {

	/**
	 * This method is used to retrieve the list of available Sample Storage Structures, Sample Types, 
	 * Container Types, Container Structures, Project Types, and Selected Project Types.  
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}	
	 * 			
	 * @return response entity  object holding response status and list of available Sample Storage Structures, Sample Types, 
	 * Container Types, Container Structures, Project Types, and Selected Project Types.
	 * 
	 * @throws Exception exception
	 */
	
	public ResponseEntity<Map<String, Object>> getsamplestoragemove(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/*
	public ResponseEntity<Object> addSampleStorageMapping(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createSampleStorageTransaction(final Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getEditSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getContainerStructure(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveSampleStorageMappingById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;
*/
	/**
	 * This method is used to transfer a container from one container to another. ,  
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched,nsourcemappingcode:Indicates the primary key or value field used for identifying the record from the source container mapping code,
	 * nsourcesamplestoragelocationcode:Indicates the primary key or value field used for identifying the record from the source container structure code,
	 * nsourceprojecttypecode:Indicates the primary key or value field used for identifying the record from the source project type code,
	 * nsamplestoragelocationcode:Indicates the primary key or value field used for identifying the record to be transferred to the source sample storage location code,
	 * nsamplestoragemappingcode:Indicates the primary key or value field used for identifying the record to be transferred to the source sample storage mapping code,
	 * nprojecttypecode:Indicates the primary key or value field used for identifying the record to be transferred to the source project type code,
	 * ssamplestoragelocationname:Indicates the primary key or value field used for identifying from the source Sample Storage location Name,
	 * ssamplestoragepathname:Indicates the primary key or value field used for identifying from the source Sample Storage location Path,
	 * stosamplestoragelocationname:Indicates the primary key or value field used for identifying the record to be transferred to the Sample Storage Location Name,
	 * stosamplestoragepathname:Indicates the primary key or value field used for identifying the record to be transferred to the Sample Storage Path,
	 * 					
	 * Input : {"userinfo":{nmastersitecode": -1},"nsourcemappingcode":3,"nsourcesamplestoragelocationcode":2,"nsourceprojecttypecode":1,"nsamplestoragelocationcode":1,"nsamplestoragemappingcode":1,"nprojecttypecode":1,"ssamplestoragelocationname":"FZ02",
	 * "ssamplestoragepathname":"root > Label > Label > Label","stosamplestoragelocationname":"FZ01","stosamplestoragepathname":"FZ > Rack > Tray > Cell Position"}	
	 * 			
	 * @return response entity  object holding response status and list of available Container Location.
	 * 
	 * @throws Exception exception
	 */
	
	public ResponseEntity<Object> updateSampleStorageTransaction(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/*
	public ResponseEntity<Object> updateSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> deleteSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> approveSampleStorageMapping(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getsamplestoragemappingSheetData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;
*/
	/**
	 * This method is used to retrieve the list of available data based on the provided filter criteria.
	 * 
	* @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched ,label: Specifies the field to be used as the display label in the result set,valuemember: Indicates the primary key or value field used for identifying each record in the sample storage mapping,
	 * filterquery: Defines the condition or filter to be applied while retrieving the data,source: Specifies the table from which the data needs to be read.	
	 * 					
	 * Input : {"userinfo":{nmastersitecode": -1},"label":"sampleStoragetransaction",valuemember:"nsamplestoragemappingcode",
	 * "filterquery":"nproductcode = 6 and nprojecttypecode=1","source":"view_samplestoragelocation"}	
	 * 			
	 * @return response entity  object holding response status and list of available Sample Storage.
	 * 
	 * @throws Exception exception
	 */
	
	public ResponseEntity<Map<String, Object>> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve the list of available Storage Structure and Sample Storage Container Path.
	 * 
	 * 	@param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched ,nsamplestoragemappingcode: Primary key of sample storage mapping,ncontainerstructurecode: Indicates the primary key or value field used for identifying Container Structure,
	 * ncontainertypecode: Indicates the primary key or value field used for identifying Container Type.	
	 * 					
	 * Input : {"userinfo":{nmastersitecode": -1},"ncontainertypecode":1,"nsamplestoragemappingcode":1}	
	 * 			
	 * @return response entity  object holding response status and list of available Storage Structure and Sample Storage Container Path.
	 * 
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getsamplemovedata(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

}
