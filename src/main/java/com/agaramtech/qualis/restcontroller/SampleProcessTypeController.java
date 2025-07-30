package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.service.sampleprocesstype.SampleProcessTypeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/sampleprocesstype")
public class SampleProcessTypeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleProcessTypeController.class);
	private RequestContext requestContext;
	private final SampleProcessTypeService sampleProcessTypeService;

	public SampleProcessTypeController(RequestContext requestContext,
			SampleProcessTypeService sampleProcessTypeService) {
		super();
		this.requestContext = requestContext;
		this.sampleProcessTypeService = sampleProcessTypeService;
	}

	/**
	 * This Method is used to get the over all sampleprocesstype with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of sampleprocesstype with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getSampleProcessType")
	public ResponseEntity<Object> getSampleProcessType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getSampleProcessType() called");
		requestContext.setUserInfo(userInfo);
		return sampleProcessTypeService.getSampleProcessType(userInfo);

	}

	/**
	 * This Method is used to get the over all Project Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of projecttype with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	
	@PostMapping(value = "/getProjectType")
	public ResponseEntity<Object> getProjecttype(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessTypeService.getProjecttype(userInfo);
	}
	
	
	/**
	 * This Method is used to get the over all Sample Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of Sample Type with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getSampleType")
	public ResponseEntity<Object> getSampleType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessTypeService.getSampleType(inputMap, userInfo);
	}
	
	
	/**
	 * This Method is used to get the over all Collection Tube Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of Collection Tube Type with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getCollectionTubeType")
	public ResponseEntity<Object> getCollectionTubeType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessTypeService.getCollectionTubeType(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the over all Process Type with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key "userinfo":{"nmastersitecode":-1}  which holds the value of
	 *                 respective site code,
	 * @return a response entity which holds the list of Process Type with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getProcessType")
	public ResponseEntity<Object> getProcessType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessTypeService.getProcessType(inputMap, userInfo);
	}

	
	/**
	 * This method is used to add a new entry to sampleprocesstype table.
	 * 
	 * @param inputMap [Map] holds the following keys 
	 * "userinfo":{"nmastersitecode":-1} key which holds the value of respective site code,
	 * The keyTubeValue holds the value of the collection tube type,The projecttypevalue holds the value of the project  type,
	 * The productvalue holds the value of the Product,The processtypevalue holds the value of the Process Type	
	 * The processtime holds the value of the proccessing time,The processperiodtime hold the value of processing period time,
	 * The gracetime holds the value of the proccessing grace time,The graceperiodtime holds the value of the processing grace period time,
	 * The executionorder holds the value that defines the sequence in which the sample processing steps should be executed
	 * The sdescription key holds the value of Description.
	 * @return inserted sampleprocesstype object and HTTP Status on successive
	 *  insert otherwise corresponding HTTP Status
	 *  
	 * @throws Exception
	 */
	
	@PostMapping(value = "/createSampleProcessType")
	public ResponseEntity<Object> createSampleProcessType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessTypeService.createSampleProcessType(inputMap, userInfo);
	}

	/**
	 * This method id used to delete an entry in sampleprocesstype table
	 * 
	 * @param inputMap [Map] holds the following keys
	 * "userinfo":{"nmastersitecode":-1} which holds the value of respective site code,
	 * The sampleProcessType holds the objects used to delete the sample processing mapping-related data. 
	 * @return response entity object holding response status and data of deleted
	 *         sampleprocesstype object
	 * @throws Exception
	 */

	@PostMapping(value = "/deleteSampleProcessType")
	public ResponseEntity<Object> deleteSampleProcessType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessTypeService.deleteSampleProcessType(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve active sampleprocesstype object based on the
	 * specified nprocesstypeCode.
	 * 
	 *@param inputMap [Map] holds the following keys
	 *"userinfo":{"nmastersitecode":-1} which holds the value of respective site code,
	 * nsampleprocesstypecode holds the value used to identify which record to retrieve.
	 *
	 * @return response entity object holding response status and data of
	 *         sampleprocesstype object
	 *         
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getActiveSampleProcessTypeById")
	public ResponseEntity<Object> getActiveSampleProcessTypeById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessTypeService.getActiveSampleProcessTypeById(inputMap, userInfo);
	}

	/**
	 * This method is used to update entry in sampleprocesstype table.
	 * 
	 * @param inputMap [Map] holds the following keys 
	 * "userinfo":{"nmastersitecode":-1} key which holds the value of respective site code,
	 * The keyTubeValue holds the value of the collection tube type,The projecttypevalue holds the value of the project  type,
	 * The productvalue holds the value of the Product,The processtypevalue holds the value of the Process Type	
	 * The processtime holds the value of the proccessing time,The processperiodtime hold the value of processing period time,
	 * The gracetime holds the value of the proccessing grace time,The graceperiodtime holds the value of the processing grace period time,
	 * The executionorder holds the value that defines the sequence in which the sample processing steps should be executed
	 * The sdescription key holds the value of Description.
	 * 
	 * @return response entity object holding response status and data of updated
	 *         sampleprocesstype object
	 *         
	 * @throws Exception
	 */
	@PostMapping(value = "/updateSampleProcessType")
	public ResponseEntity<Object> updateSampleProcessType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleProcessTypeService.updateSampleProcessType(inputMap, userInfo);
	}
}
