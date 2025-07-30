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
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentLocation;
import com.agaramtech.qualis.instrumentmanagement.service.instrumentlocation.InstrumentLocationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Instrument Location Service methods
 */
@RestController
@RequestMapping("/instrumentlocation")
public class InstrumentLocationController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentLocationController.class);
	
	private RequestContext requestContext;
	private final InstrumentLocationService instrumentlocationService;

	public InstrumentLocationController(RequestContext requestContext,
			InstrumentLocationService instrumentlocationService) {
		super();
		this.requestContext = requestContext;
		this.instrumentlocationService = instrumentlocationService;
	}

	/**
	 * This Method is used to get the over all instrument location with respect to
	 * instrument
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of instrument locations with
	 *         respect to instrument and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getInstrumentLocation")
	public ResponseEntity<Object> getInstrumentLocation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userinfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userinfo);
		LOGGER.info("getInstrumentLocation called");
		return instrumentlocationService.getInstrumentLocation(userinfo);
	}

	/**
	 * This method is used to add a new entry to instrument location table.
	 * 
	 * @param inputMap [Map] holds the instrument locations object to be inserted
	 * @return inserted instrument location object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception
	 */
	@PostMapping(value = "/createInstrumentLocation")
	public ResponseEntity<Object> createInstrumentLocation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InstrumentLocation instrumentLocation = objmapper.convertValue(inputMap.get("instrumentlocation"),
				InstrumentLocation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return instrumentlocationService.createInstrumentLocation(instrumentLocation, userInfo);
	}

	/**
	 * This method is used to get the single record in instrument location table
	 * 
	 * @param inputMap [Map] holds the instrument locations code to get
	 * @return response entity object holding response status and data of single
	 *         instrument location object
	 * @throws Exception
	 */
	@RequestMapping(value = "/getActiveInstrumentLocationById")
	public ResponseEntity<Object> getActiveInstrumentLocationById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int ninstrumentlocationcode = (int) inputMap.get("ninstrumentlocationcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return instrumentlocationService.getActiveInstrumentLocationById(ninstrumentlocationcode, userInfo);
	}

	/**
	 * This method is used to update entry in instrument location table.
	 * 
	 * @param inputMap [Map] holds the instrument locations object to be updated
	 * @return response entity object holding response status and data of updated
	 *         instrument location object
	 * @throws Exception
	 */
	@PostMapping(value = "/updateInstrumentLocation")
	public ResponseEntity<Object> updateInstrumentLocation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentLocation instrumentlocation = objmapper.convertValue(inputMap.get("instrumentlocation"),
				InstrumentLocation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return instrumentlocationService.updateInstrumentLocation(instrumentlocation, userInfo);
	}

	/**
	 * This method id used to delete an entry in instrument location table
	 * 
	 * @param inputMap [Map] holds the instrument location object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         instrument location object
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteInstrumentLocation")
	public ResponseEntity<Object> deleteInstrumentLocation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final InstrumentLocation instrumentlocation = objmapper.convertValue(inputMap.get("instrumentlocation"),
				InstrumentLocation.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return instrumentlocationService.deleteInstrumentLocation(instrumentlocation, userInfo);
	}
}
