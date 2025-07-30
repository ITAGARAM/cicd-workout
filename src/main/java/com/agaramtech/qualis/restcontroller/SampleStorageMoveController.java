package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.service.samplestoragemove.SampleStorageMoveService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Studyidentity Service methods
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@RestController
@RequestMapping("/samplestoragemove")
public class SampleStorageMoveController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleStorageMoveController.class);

	private RequestContext requestContext;
	private final SampleStorageMoveService sampleStorageMoveService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext           RequestContext to hold the request
	 * @param sampleStorageMoveService SampleStorageMoveService
	 */
	public SampleStorageMoveController(RequestContext requestContext,
			SampleStorageMoveService sampleStorageMoveService) {
		super();
		this.requestContext = requestContext;
		this.sampleStorageMoveService = sampleStorageMoveService;
	}

	
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
	
	@PostMapping(value = "/getsamplestoragemove")
	public ResponseEntity<Map<String, Object>> getsamplestoragemove(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getsamplestoragemove()");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageMoveService.getsamplestoragemove(inputMap, userInfo);
	}

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
	@PostMapping(value = "/updateSampleStorageTransaction")
	public ResponseEntity<Object> updateSampleStorageTransaction(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageMoveService.updateSampleStorageTransaction(inputMap, userInfo);
	}
	/*
	
	@PostMapping(value = "/updateSampleStorageMapping")
	public ResponseEntity<Object> updateSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageMoveService.updateSampleStorageMapping(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteSampleStorageMapping")
	public ResponseEntity<Map<String, Object>> deleteSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageMoveService.deleteSampleStorageMapping(inputMap, userInfo);
	}

	@PostMapping(value = "/approveSampleStorageMapping")
	public ResponseEntity<Object> approveSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageMoveService.approveSampleStorageMapping(inputMap, userInfo);
	}

	@PostMapping(value = "/getsamplestoragemappingSheetData")
	public ResponseEntity<Object> getsamplestoragemappingSheetData(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageMoveService.getsamplestoragemappingSheetData(inputMap, userInfo);
	}*/

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
	
	@PostMapping(value = "/getdynamicfilterexecutedata")
	public ResponseEntity<Map<String, Object>> getDynamicFilterExecuteData(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageMoveService.getDynamicFilterExecuteData(inputMap, userInfo);
	}

	
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
	
	@PostMapping(value = "/getsamplemovedata")
	public ResponseEntity<Object> getsamplemovedata(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleStorageMoveService.getsamplemovedata(inputMap, userInfo);
	}

}
