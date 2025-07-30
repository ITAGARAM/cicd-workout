package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.basemaster.model.SamplingPoint;
import com.agaramtech.qualis.basemaster.service.samplingpoint.SamplingPointService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Sampling Point Service methods
 * 
 * @author AT-E143
 * @version 10.0.0.2
 * @since 06- February- 2025
 */
@RestController
@RequestMapping("/samplingpoint")
public class SamplingPointController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamplingPointController.class);

	private RequestContext requestContext;
	private SamplingPointService samplingPointService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext       RequestContext to hold the request
	 * @param samplingPointService SamplingPointService
	 */
	public SamplingPointController(RequestContext requestContext, SamplingPointService samplingPointService) {
		super();
		this.requestContext = requestContext;
		this.samplingPointService = samplingPointService;
	}

	/**
	 * This Method is used to get the over all samplingpoint with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of samplingpoint with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSamplingPoint")
	public ResponseEntity<Object> getSamplingPoint(@RequestBody final Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getSamplingPoint()");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplingPointService.getSamplingPoint(userInfo);
	}

	/**
	 * This Method is used to get the over all samplingpointcategory with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of samplingpointcategory with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSamplingPointCategory")
	public ResponseEntity<Object> getSamplingPointCategory(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplingPointService.getSamplingPointCategory(userInfo);
	}

	/**
	 * This method is used to add a new entry to samplingpoint table.
	 * 
	 * @param inputMap [Map] holds the samplingpoint object to be inserted
	 * @return inserted samplingpoint object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createSamplingPoint")
	public ResponseEntity<Object> createSamplingPoint(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SamplingPoint objSamplingPoint = objMapper.convertValue(inputMap.get("samplingpoint"),
				new TypeReference<SamplingPoint>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplingPointService.createSamplingPoint(objSamplingPoint, userInfo);
	}

	/**
	 * This method is used to update entry in samplingpoint table.
	 * 
	 * @param inputMap [Map] holds the samplingpoint object to be updated
	 * @return response entity object holding response status and data of updated
	 *         samplingpoint object
	 * @throws Exception
	 */
	@PostMapping(value = "/updateSamplingPoint")
	public ResponseEntity<Object> updateSamplingPoint(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final SamplingPoint objSamplingPoint = objMapper.convertValue(inputMap.get("samplingpoint"),
				new TypeReference<SamplingPoint>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplingPointService.updateSamplingPoint(objSamplingPoint, userInfo);
	}

	/**
	 * This method id used to delete an entry in samplingpoint table
	 * 
	 * @param inputMap [Map] holds the samplingpoint object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         samplingpoint object
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteSamplingPoint")
	public ResponseEntity<Object> deleteSamplingPoint(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final SamplingPoint objSamplingPoint = objMapper.convertValue(inputMap.get("samplingpoint"),
				new TypeReference<SamplingPoint>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplingPointService.deleteSamplingPoint(objSamplingPoint, userInfo);
	}

	/**
	 * This method is used to get the single record in samplingpoint table
	 * 
	 * @param inputMap [Map] holds the nsamplingpointcode to get
	 * @return response entity object holding response status and data of single
	 *         samplingpoint object
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveSamplingPointById")
	public ResponseEntity<Object> getActiveSamplingPointById(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nsamplingPointCode = (int) inputMap.get("nsamplingpointcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplingPointService.getActiveSamplingPointById(nsamplingPointCode, userInfo);
	}
}
