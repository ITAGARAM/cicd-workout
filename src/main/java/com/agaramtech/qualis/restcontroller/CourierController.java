package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.contactmaster.model.Courier;
import com.agaramtech.qualis.contactmaster.service.courier.CourierService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access theCourier Service methods.
 */

@RestController
@RequestMapping("/courier")
public class CourierController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CourierController.class);

	private final CourierService courierService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param unitService    UnitService
	 */
	public CourierController(CourierService courierService, RequestContext requestContext) {
		super();
		this.courierService = courierService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of active courier for the specified
	 * site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active courier that are to be listed for
	 *         the specified site
	 */
	@PostMapping(value = "/getCourier")
	public ResponseEntity<Object> getCourier(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getCourier called");
		return courierService.getCourier(userInfo.getNmastersitecode());
	}

	/**
	 * This method will is used to make a new entry to courier table.
	 * 
	 * @param inputMap map object holding params ( courier [courier] object holding
	 *                 details to be added in unit table, userinfo [UserInfo]
	 *                 holding logged in user details ) Input:{ "courier": {
	 *                 "scouriername": "--", "scouriersynonym": "--" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 163,"nmastersitecode": -1,"nmodulecode": 54,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the courier
	 *         already exists/ list of couriers along with the newly added courier.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createCourier")
	public ResponseEntity<Object> createCourier(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Courier courier = objmapper.convertValue(inputMap.get("courier"), Courier.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return courierService.createCourier(courier, userInfo);
	}

	/**
	 * This method is used to update selected courier details.
	 * 
	 * @param inputMap [map object holding params( courier [Courier] object holding
	 *                 details to be updated in courier table, userinfo [UserInfo]
	 *                 holding logged in user details) Input:{ "courier":
	 *                 {"ncouriercode":1,"scouriername": "--","scouriersynonym":
	 *                 "---", "sdescription": "--", "ndefaultstatus": 3 },
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 163,"nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         courier record is not available/ list of all couriers and along with
	 *         the updated courier.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateCourier")
	public ResponseEntity<Object> updateCourier(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Courier courier = objmapper.convertValue(inputMap.get("courier"), Courier.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return courierService.updateCourier(courier, userInfo);
	}

	/**
	 * This method is used to retrieve a specific courier record.
	 * 
	 * @param inputMap [Map] map object with "nunitcode" and "userinfo" as keys for
	 *                 which the data is to be fetched Input:{ "nunitcode": 1,
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 163,"nmastersitecode": -1,"nmodulecode": 54,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with Courier object for the specified primary key /
	 *         with string message as 'Deleted' if the unit record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveCourierById")
	public ResponseEntity<Object> getActiveCourierById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final int ncouriercode = (Integer) inputMap.get("ncouriercode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return courierService.getActiveCourierById(ncouriercode, userInfo);
	}

	/**
	 * This method is used to delete an entry in courier table
	 * 
	 * @param inputMap [Map] object with keys of courier entity and UserInfo object.
	 *                 Input:{ "courier": {"nunitcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 163,"nmastersitecode": -1, "nmodulecode":
	 *                 54, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         courier record is not available/ string message as 'Record is used
	 *         in....' when the courier is associated in transaction / list of all
	 *         couriers excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteCourier")
	public ResponseEntity<Object> deleteCourier(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final Courier courier = objmapper.convertValue(inputMap.get("courier"), Courier.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return courierService.deleteCourier(courier, userInfo);
	}

	@PostMapping(value = "/getAllActiveCourier")
	public ResponseEntity<Object> getAllActiveCourier(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return courierService.getAllActiveCourier(userInfo.getNmastersitecode());
	}
}
