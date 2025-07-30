package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.service.samplestoragetransaction.SampleStorageTransactionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the samplestoragetransaction Service methods
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@RestController
@RequestMapping("/samplestoragetransaction")
public class SampleStorageTransactionController {

	private RequestContext requestContext;
	private final SampleStorageTransactionService samplestoragemappingService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext              RequestContext to hold the request
	 * @param samplestoragemappingService SampleStorageTransactionService
	 */
	public SampleStorageTransactionController(RequestContext requestContext,
			SampleStorageTransactionService samplestoragemappingService) {
		super();
		this.requestContext = requestContext;
		this.samplestoragemappingService = samplestoragemappingService;
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
	
	@PostMapping(value = "/getsamplestoragetransaction")
	public ResponseEntity<Map<String, Object>> getsamplestoragetransaction(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.getsamplestoragetransaction(inputMap, userInfo);

	}

	/*@PostMapping(value = "/addSampleStorageMapping")
	public ResponseEntity<Object> addSampleStorageMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.addSampleStorageMapping(inputMap, userInfo);

	}

	@PostMapping(value = "/getEditSampleStorageMapping")
	public ResponseEntity<Object> getEditSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.getEditSampleStorageMapping(inputMap, userInfo);

	}

	@PostMapping(value = "/getContainerStructure")
	public ResponseEntity<Map<String, Object>> getContainerStructure(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.getContainerStructure(inputMap, userInfo);

	}

	@PostMapping(value = "/getActiveSampleStorageMappingById")
	public ResponseEntity<Object> getActiveSampleStorageMappingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.getActiveSampleStorageMappingById(inputMap, userInfo);
	}
	*/
	
	/**
	 * This method is used to retrieve the list of available Sample Storage Structures, Sample Types, 
	 * Container Types, Container Structures, Project Types, and Selected Project Types.  
	 * 
	 * @param The inputMap is a Map object that contains multiple keys required for processing the sample storage transaction.
	 *  It includes userInfo, which holds the logged-in user details, and nmasterSiteCode, the primary key representing the site for which the 
	 *  list is to be fetched. The key nsamplestoragelocationcode represents the primary key of the cell position, 
	 *  while nsamplestoragemappingcode refers to the primary key of the sample storage mapping. The nprojecttypecode 
	 *  holds the primary code of the Project Type. Additionally, sposition is used to specify the position of the cell,
	 *  and spositionvalue holds the value associated with that cell position.	
	 *  
	 * 					Input : {"userinfo":{nmastersitecode": -1},"nsamplestoragelocationcode":1,"nsamplestoragemappingcode":1,
	 * "nprojecttypecode":1,"sposition":A3,"spositionvalue":P103}	
	 * 			
	 * @return response entity  object holding response status and list of available Cell Data and Available spaces.
	 * 
	 * @throws Exception exception
	 */

	@PostMapping(value = "/createSampleStorageTransaction")
	public ResponseEntity<Object> createSampleStorageTransaction(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.createSampleStorageTransaction(inputMap, userInfo);
	}

	/**
	 * This method is used to modify the samplestoragemapping.  
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 	which the list is to be fetched ,nsamplestoragemappingcode:Define the value of sample storage mapping code,nsamplestoragelocationcode:Define the value of storage structure code,
	 * sboxid:Define the Container id of storage.	
	 * 					Input : {"userinfo":{nmastersitecode": -1},"nsamplestoragemappingcode":1,"nsamplestoragelocationcode":1,"sboxid":"C1"}	
	 * 			
	 * @return response entity  object holding response status and list of available container id.
	 * 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSampleStorageMapping")
	public ResponseEntity<Object> updateSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.updateSampleStorageMapping(inputMap, userInfo);
	}
	/*
	@PostMapping(value = "/deleteSampleStorageMapping")
	public ResponseEntity<Map<String, Object>> deleteSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.deleteSampleStorageMapping(inputMap, userInfo);
	}

	@PostMapping(value = "/approveSampleStorageMapping")
	public ResponseEntity<Object> approveSampleStorageMapping(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.approveSampleStorageMapping(inputMap, userInfo);
	}
	
	*/
	/**
	 *This method is used to retrieve the list of available data by determining the cell position. 
	 * 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched ,isMultiSampleAdd:This method is used to determine whether the container should allow multiple showing or single showing,
	 * nsamplestoragemappingcode: Indicates the primary key or value field used for identifying each record in the sample storage mapping.
	 * 		
	 * Input : {"userinfo":{nmastersitecode": -1},"isMultiSampleAdd":false,"nsamplestoragemappingcode":1}	
	 * 			
	 * @return response entity  object holding response status and list of available Container.
	 * 
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getsamplestoragemappingSheetData")
	public ResponseEntity<Object> getsamplestoragemappingSheetData(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.getsamplestoragemappingSheetData(inputMap, userInfo);
	}
	
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
	public ResponseEntity<Object> getDynamicFilterExecuteData(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.getDynamicFilterExecuteData(inputMap, userInfo);
	}
	
	
	/**
	 * This method is used to retrieve the list of available sample storage related data.
	 * 
	* @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched ,nquantity: Specifies the field to be used as the display label in the result set,nprojecttypecode: Indicates the primary key or value field used for identifying project type,
	 * ncolumn: Defines the container of column,nrow: Defines the container of row,nsamplestoragelocationcode:Defines the storage location code,
	 * nsamplestoragemappingcode:Define the storage structure mapping code,scomments: Define the comments of storage structure,"scontainerpath":Define the storage structure path,"sprojecttypename":Define the structure of the project,"ssampleid":Define the structure of the sample id,
	 * "ssamplestoragelocationname": Define the storage location name,"sunitname":Define the storage unit name,"nquantity":Define the quantity of storage structure
	 * 					
	 * Input : {"userinfo":{nmastersitecode": -1},"ncolumn":10,"nrow":10,"nquantity":0,
	 * "nsamplestoragelocationcode":2,"nsamplestoragemappingcode":3,"scomments":"comment","scontainerpath","FZ > Rack > Tray > Cell Position":,"sunitname":"ml"}	
	 * 			
	 * @return response entity  object holding response status and list of available sample storage related data.
	 * 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSingleExport")
	public ResponseEntity<Object> getSingleExport(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.getSingleExport(inputMap, userInfo);
	}

	/**
	 * This method is make the new entry of samplestoragetransaction.
	 * 
	* @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched ,nprojecttypecode: Indicates the primary key or value field used for identifying project type,
	 * filecount: Define the no. of file count,
	 * ImportFile:Define the sample storage related data.
	 * 			
	 * Input : {"userinfo":{nmastersitecode": -1},"nprojecttypecode":10,"filecount":10
	 * 			
	 * @return response entity  object holding response status and list of available sample storage related data.
	 * 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getImportData")
	public ResponseEntity<Object> getImportData(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return samplestoragemappingService.getImportData(request, userInfo);
	}

}
