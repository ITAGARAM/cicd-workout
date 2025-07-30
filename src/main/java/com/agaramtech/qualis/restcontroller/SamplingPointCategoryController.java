package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.basemaster.model.SamplingPointCategory;
import com.agaramtech.qualis.basemaster.service.samplingpointcategory.SamplingPointCategoryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Sampling Point Category Service methods
 * 
 * @author AT-E143
 * @version 10.0.0.2
 * @since 06- February- 2025
 */
@RestController
@RequestMapping("/samplingpointcategory")
public class SamplingPointCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SamplingPointCategoryController.class);

	private RequestContext requestContext;
	private final SamplingPointCategoryService samplingPointCategoryService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext               RequestContext to hold the request
	 * @param samplingPointCategoryService SamplingPointCategoryService
	 */
	public SamplingPointCategoryController(RequestContext requestContext,
			SamplingPointCategoryService samplingPointCategoryService) {
		super();
		this.requestContext = requestContext;
		this.samplingPointCategoryService = samplingPointCategoryService;
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
		LOGGER.info("getSamplingPointCategory()");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplingPointCategoryService.getSamplingPointCategory(userInfo);
	}

	/**
	 * This method is used to add a new entry to samplingpointcategory table.
	 * 
	 * @param inputMap [Map] holds the samplingpointcategory object to be inserted
	 * @return inserted samplingpointcategory object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createSamplingPointCategory")
	public ResponseEntity<Object> createSamplingPointCategory(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SamplingPointCategory objSamplingPointCategory = objMapper
				.convertValue(inputMap.get("samplingpointcategory"), new TypeReference<SamplingPointCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplingPointCategoryService.createSamplingPointCategory(objSamplingPointCategory, userInfo);
	}

	/**
	 * This method is used to update entry in samplingpointcategory table.
	 * 
	 * @param inputMap [Map] holds the samplingpointcategory object to be updated
	 * @return response entity object holding response status and data of updated
	 *         samplingpointcategory object
	 * @throws Exception
	 */
	@PostMapping(value = "/updateSamplingPointCategory")
	public ResponseEntity<Object> updateSamplingPointCategory(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final SamplingPointCategory objSamplingPointCategory = objMapper
				.convertValue(inputMap.get("samplingpointcategory"), new TypeReference<SamplingPointCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplingPointCategoryService.updateSamplingPointCategory(objSamplingPointCategory, userInfo);

	}

	/**
	 * This method id used to delete an entry in samplingpointcategory table
	 * 
	 * @param inputMap [Map] holds the samplingpointcategory object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         samplingpointcategory object
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteSamplingPointCategory")
	public ResponseEntity<Object> deleteSamplingPointCategory(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final SamplingPointCategory objSamplingPointCategory = objMapper
				.convertValue(inputMap.get("samplingpointcategory"), new TypeReference<SamplingPointCategory>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplingPointCategoryService.deleteSamplingPointCategory(objSamplingPointCategory, userInfo);

	}

	/**
	 * This method is used to get the single record in samplingpointcategory table
	 * 
	 * @param inputMap [Map] holds the nsamplingpointcatcode to get
	 * @return response entity object holding response status and data of single
	 *         samplingpointcategory object
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveSamplingPointCategoryById")
	public ResponseEntity<Object> getActiveSamplingPointCategoryById(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nsamplingPointCatCode = (int) inputMap.get("nsamplingpointcatcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplingPointCategoryService.getActiveSamplingPointCategoryById(nsamplingPointCatCode, userInfo);
	}
}
