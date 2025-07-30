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
import com.agaramtech.qualis.submitter.model.Region;
import com.agaramtech.qualis.submitter.service.region.RegionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Region Service methods
 * 
 * @author ATE234
 * @version 10.0.0.2 ALPD-5645 Master screen -> Region
 */
@RestController
@RequestMapping("/region")
public class RegionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegionController.class);

	private RequestContext requestContext;
	private final RegionService regionService;

	// final Log logging = LogFactory.getLog(RegionController.class);

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param unitService    UnitService
	 */
	public RegionController(RequestContext requestContext, RegionService regionService) {
		super();
		this.requestContext = requestContext;
		this.regionService = regionService;
	}

	/**
	 * This Method is used to get the over all Region with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Region with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getRegion")
	public ResponseEntity<Object> getRegion(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("Get Controller -->"+userInfo);
		return regionService.getRegion(userInfo);
	}

	/**
	 * This method is used to add a new entry to region table.
	 * 
	 * @param inputMap [Map] holds the region object to be inserted
	 * @return inserted region object and HTTP Status on successive insert otherwise
	 *         corresponding HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createRegion")
	public ResponseEntity<Object> createRegion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Region objRegion = objmapper.convertValue(inputMap.get("region"), Region.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return regionService.createRegion(objRegion, userInfo);
	}

	/**
	 * This method is used to get the single record in region table
	 * 
	 * @param inputMap [Map] holds the nregioncode code to get
	 * @return response entity object holding response status and data of single
	 *         region object
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveRegionById")
	public ResponseEntity<Object> getActiveRegionById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nregioncode = (Integer) inputMap.get("nregioncode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return regionService.getActiveRegionById(nregioncode, userInfo);
	}

	/**
	 * This method is used to update entry in region table.
	 * 
	 * @param inputMap [Map] holds the region object to be updated
	 * @return response entity object holding response status and data of updated
	 *         region object
	 * @throws Exception
	 */
	@PostMapping(value = "/updateRegion")
	public ResponseEntity<Object> updateRegion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Region objRegion = objmapper.convertValue(inputMap.get("region"), Region.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return regionService.updateRegion(objRegion, userInfo);
	}

	/**
	 * This method id used to delete an entry in region table
	 * 
	 * @param inputMap [Map] holds the region object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         region object
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteRegion")
	public ResponseEntity<Object> deleteRegion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Region objRegion = objmapper.convertValue(inputMap.get("region"), Region.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return regionService.deleteRegion(objRegion, userInfo);

	}
}
