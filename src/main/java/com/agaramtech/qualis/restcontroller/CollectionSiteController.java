package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.barcode.model.CollectionSite;
import com.agaramtech.qualis.barcode.service.collectionsite.CollectionSiteService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Collection Site Service methods
 */

@RestController
@RequestMapping("/collectionsite")
public class CollectionSiteController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionSiteController.class);
	
	private final CollectionSiteService collectionSiteService;
	private RequestContext requestContext;

	public CollectionSiteController(CollectionSiteService collectionSiteService, RequestContext requestContext) {
		super();
		this.collectionSiteService = collectionSiteService;
		this.requestContext = requestContext;
	}

	/**
	 * This Method is used to get the over all Collection Site with respect to
	 * Project Type
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Collection Site with
	 *         respect to Project type and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getCollectionSite")
	public ResponseEntity<Object> getCollectionSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getCollectionSite called");
		return collectionSiteService.getCollectionSite(userInfo);
	}

	/**
	 * This method is used to add a new entry to Collection Site table.
	 * 
	 * @param inputMap [Map] holds the Collection Site object to be inserted
	 * @return inserted Collection Site object with HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createCollectionSite")
	public ResponseEntity<Object> createSampleCycle(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final CollectionSite collectionSite = objMapper.convertValue(inputMap.get("collectionsite"),
				CollectionSite.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return collectionSiteService.createCollectionSite(collectionSite, userInfo);
	}

	/**
	 * This method is used to get the single record in Collection Site table
	 * 
	 * @param inputMap [Map] holds the ncollectionsitecode to get active Collection
	 *                 Site
	 * @return response entity object holding response status and data of single
	 *         Collection Site object
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveCollectionSiteById")
	public ResponseEntity<Object> getActiveCollectionSiteById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ncollectionsitecode = (Integer) inputMap.get("ncollectionsitecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return collectionSiteService.getActiveCollectionSiteById(ncollectionsitecode, userInfo);
	}

	/**
	 * This method is used to update entry in Collection Site table.
	 * 
	 * @param inputMap [Map] holds the Collection Site object to be updated
	 * @return response entity object holding response status and data of updated
	 *         Collection Site object
	 * @throws Exception
	 */
	@PostMapping(value = "/updateCollectionSite")
	public ResponseEntity<Object> updateCollectionSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final CollectionSite collectionSite = objMapper.convertValue(inputMap.get("collectionsite"),
				CollectionSite.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return collectionSiteService.updateCollectionSite(collectionSite, userInfo);
	}

	/**
	 * This method id used to delete an entry in Collection Site table
	 * 
	 * @param inputMap [Map] holds the Collection Site object to be deleted
	 * @return response entity object holding response status after data is deleted
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteCollectionSite")
	public ResponseEntity<Object> deleteCollectionSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final CollectionSite collectionSite = objMapper.convertValue(inputMap.get("collectionsite"),
				CollectionSite.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return collectionSiteService.deleteCollectionSite(collectionSite, userInfo);
	}
}
