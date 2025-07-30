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
import com.agaramtech.qualis.storagemanagement.service.samplereceiving.SampleReceivingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Unit Service methods
 * 
 * @author ATE97
 * @version 9.0.0.1
 */
@RestController
@RequestMapping("/samplereceiving")
public class SampleReceivingController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SampleReceivingController.class);
	
	private final SampleReceivingService sampleReceivingService;
	private RequestContext requestContext;

	public SampleReceivingController(SampleReceivingService sampleReceivingService, RequestContext requestContext) {
		super();
		this.sampleReceivingService = sampleReceivingService;
		this.requestContext = requestContext;
	}
	
	/**
	 * This method is used to retrieve list of all active samplereceiving for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         samplereceiving
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getSampleReceiving")
	public ResponseEntity<Object> getSampleReceiving(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		int nprojecttypecode = -1;
		if (inputMap.containsKey("nprojecttypecode")) {
			nprojecttypecode = (int) inputMap.get("nprojecttypecode");
		}
		LOGGER.info("getSampleReceiving called");
		return sampleReceivingService.getSampleReceiving(fromDate, toDate, currentUIDate, userInfo, nprojecttypecode);
	}

	/**
	 * This method is used to retrieve list of all active Barcode data for the
	 * specified project type and site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Barcode data specified project type and site.
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getBarcodeConfigData")
	public ResponseEntity<Object> getBarcodeConfigData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleReceivingService.getBarcodeConfigData(inputMap, userInfo);
	}

	/**
	 * This method is used to add a new entry to storagesamplereceiving table.
	 * 
	 * @param inputMap [Map] holds the samplereceiving object to be inserted
	 * @return inserted samplereceiving object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createSampleReceiving")
	public ResponseEntity<Object> createSampleReceiving(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		objMapper.registerModule(new JavaTimeModule());
		requestContext.setUserInfo(userInfo);

		return sampleReceivingService.createSampleReceiving(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve active samplereceiving object based on the
	 * specified nstoragesamplereceivingcode.
	 * 
	 * @param nstoragesamplereceivingcode [int] primary key of samplereceiving object
	 * @return response entity object holding response status and data of
	 *         samplereceiving object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getActiveSampleReceivingById")
	public ResponseEntity<Object> getActiveSampleReceivingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		objMapper.registerModule(new JavaTimeModule());
		requestContext.setUserInfo(userInfo);
		return sampleReceivingService.getActiveSampleReceivingById(inputMap, userInfo);
	}

	/**
	 * This method is used to update entry in storagesamplereceiving table.
	 * 
	 * @param inputMap [Map] holds the samplereceiving object to be updated
	 * @return response entity object holding response status and data of updated
	 *         samplereceiving object
	 * @throws Exception
	 */
	@PostMapping(value = "/updateSampleReceiving")
	public ResponseEntity<Object> updateSampleReceiving(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleReceivingService.updateSampleReceiving(inputMap, userInfo);
	}

	/**
	 * This method id used to delete an entry in storagesamplereceiving table
	 * 
	 * @param inputMap [Map] holds the samplereceiving object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         samplereceiving object
	 * @throws Exception
	 */

	@PostMapping(value = "/deleteSampleReceiving")
	public ResponseEntity<Object> deleteSampleReceiving(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleReceivingService.deleteSampleReceiving(inputMap, userInfo);

	}
}