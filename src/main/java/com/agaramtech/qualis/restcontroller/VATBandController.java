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
import com.agaramtech.qualis.quotation.model.VATBand;
import com.agaramtech.qualis.quotation.service.vatband.VATBandService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * VATBandController handles the HTTP requests related to VAT Band operations
 * such as retrieving, creating, updating, and deleting VAT Bands. It calls the
 * corresponding methods in the VATBandService class.
 * 
 * @author ATE172
 * @version 9.0.0.1
 */
@RestController
@RequestMapping("/vatband")
public class VATBandController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VATBandController.class);
	
	private RequestContext requestContext;
	private final VATBandService vatbandService;

	public VATBandController(RequestContext requestContext, VATBandService vatbandService) {
		super();
		this.requestContext = requestContext;
		this.vatbandService = vatbandService;
	}

	/**
	 * Retrieves the VAT Band details based on user information provided in the
	 * inputMap.
	 * 
	 * @param inputMap [Map] Contains the user information to fetch the VAT Band.
	 * @return ResponseEntity containing the VAT Band details.
	 * @throws Exception if any error occurs during the process.
	 */
	@PostMapping(value = "/getVATBand")
	public ResponseEntity<Object> getVATBand(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getVATBand called");
		requestContext.setUserInfo(userInfo);
		return vatbandService.getVATBand(userInfo);
	}

	/**
	 * Creates a new VAT Band based on the information provided in the inputMap.
	 * 
	 * @param inputMap [Map] Contains VAT Band data and user information.
	 * @return ResponseEntity with the result of the creation operation.
	 * @throws Exception if any error occurs during the process.
	 */
	@PostMapping(value = "/createVATBand")
	public ResponseEntity<Object> createVATBand(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final VATBand vatBand = objmapper.convertValue(inputMap.get("vatband"), VATBand.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return vatbandService.createVATBand(vatBand, userInfo);
	}

	/**
	 * Retrieves the active VAT Band details by its ID.
	 * 
	 * @param inputMap [Map] Contains VAT Band ID ("nvatbandcode") and user
	 *                 information.
	 * @return ResponseEntity containing the VAT Band details.
	 * @throws Exception if any error occurs during the process.
	 */
	@PostMapping(value = "/getActiveVATBandById")
	public ResponseEntity<Object> getActiveVATBandById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nvatbandcode = (int) inputMap.get("nvatbandcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return vatbandService.getActiveVATBandById(nvatbandcode, userInfo);
	}

	/**
	 * Updates an existing VAT Band with the details provided in the inputMap.
	 * 
	 * @param inputMap [Map] Contains the updated VAT Band data and user
	 *                 information.
	 * @return ResponseEntity with the result of the update operation.
	 * @throws Exception if any error occurs during the process.
	 */
	@PostMapping(value = "/updateVATBand")
	public ResponseEntity<Object> updateVATBand(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final VATBand vatband = objmapper.convertValue(inputMap.get("vatband"), VATBand.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return vatbandService.updateVATBand(vatband, userInfo);
	}

	/**
	 * Deletes an existing VAT Band based on the provided data in the inputMap.
	 * 
	 * @param inputMap [Map] Contains the VAT Band data to be deleted and user
	 *                 information.
	 * @return ResponseEntity with the result of the deletion operation.
	 * @throws Exception if any error occurs during the process.
	 */
	@PostMapping(value = "/deleteVATBand")
	public ResponseEntity<Object> deleteVATBand(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final VATBand vatband = objmapper.convertValue(inputMap.get("vatband"), VATBand.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return vatbandService.deleteVATBand(vatband, userInfo);
	}
}
