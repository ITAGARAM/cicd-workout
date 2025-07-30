package com.agaramtech.qualis.restcontroller;

/**
 *  This class is used to perform CRUD Operation on "Sample Appearance" table . 

 * @jira ALPD-3583
 */
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.SampleAppearance;
import com.agaramtech.qualis.product.service.sampleappearance.SampleAppearanceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("sampleappearance")
public class SampleAppearanceController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SampleAppearanceController.class);
	
	private RequestContext requestContext;
	private final SampleAppearanceService sampleAppearanceService;

	public SampleAppearanceController(RequestContext requestContext, SampleAppearanceService sampleAppearanceService) {
		super();
		this.requestContext = requestContext;
		this.sampleAppearanceService = sampleAppearanceService;
	}

	/**
	 * This method is used to retrieve list of active Sample Appearance for the
	 * specified site.
	 * 
	 * @param inputMap [Map] map object with "userinfo" as key for which the list is
	 *                 to be fetched
	 * @return response object with list of active Sample Appearance that are to be
	 *         listed for the specified site
	 * 
	 */
	
	@PostMapping(value = "/getSampleAppearance")
	public ResponseEntity<Object> getSampleAppearance(@RequestBody Map<String, Object> obj) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(obj.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getSampleAppearance called");
		requestContext.setUserInfo(userInfo);
		return sampleAppearanceService.getSampleAppearance(userInfo);
	}

	/**
	 * This method is used to create a new Sample Appearance for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of Sample Appearance [Sample
	 *                 Appearance] and UserInfo object entity.
	 * @return response entity for List of Sample Appearance entity
	 */
	@PostMapping(value = "/createSampleAppearance")
	public ResponseEntity<Object> createSampleAppearance(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final SampleAppearance SampleAppearance = objmapper.convertValue(inputMap.get("sampleappearance"),
				SampleAppearance.class);
		final UserInfo UserInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(UserInfo);
		return sampleAppearanceService.createSampleAppearance(inputMap, SampleAppearance, UserInfo);
	}

    /**
     * This method is used to retrieve a specific active Sample Appearance by its ID.
     * 
     * @param inputMap [Map] Contains the "nsampleappearancecode" and "userinfo" entities.
     * @return ResponseEntity with the active Sample Appearance identified by the given ID.
     */

	@PostMapping(value = "/getActiveSampleAppearanceById")
	public ResponseEntity<Object> getActiveSampleAppearanceById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nsampleappearancecode = (int) inputMap.get("nsampleappearancecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleAppearanceService.getActiveSampleAppearanceById(nsampleappearancecode, userInfo);
	}
	 /**
     * This method is used to update an existing Sample Appearance.
     * 
     * @param inputMap [Map] Contains the updated Sample Appearance object and the UserInfo object.
     * @return ResponseEntity with the updated Sample Appearance entity.
     */
	@PostMapping(value = "/updateSampleAppearance")
	public ResponseEntity<Object> updateSampleAppearance(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final SampleAppearance objSampleAppearance = objMapper.convertValue(inputMap.get("sampleappearance"),
				new TypeReference<SampleAppearance>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleAppearanceService.updateSampleAppearance(objSampleAppearance, userInfo);
	}
	 /**
     * This method is used to delete an existing Sample Appearance.
     * 
     * @param inputMap [Map] Contains the Sample Appearance entity to be deleted and the UserInfo object.
     * @return ResponseEntity indicating the result of the deletion operation.
     */
	@PostMapping(value = "/deleteSampleAppearance")
	public ResponseEntity<Object> deleteSampleAppearance(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final SampleAppearance objSample = objMapper.convertValue(inputMap.get("sampleappearance"),
				new TypeReference<SampleAppearance>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleAppearanceService.deleteSampleAppearance(objSample, userInfo);
	}
}
