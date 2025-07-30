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
import com.agaramtech.qualis.configuration.model.InterfacerMapping;
import com.agaramtech.qualis.configuration.service.interfacermapping.InterfacerMappingServices;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the interfacerMapping Service methods
 * 
 * @author AT-E234
 *
 */
@RestController
@RequestMapping("/interfacermapping")
public class InterfacerMappingController {
	private static final Logger LOGGER = LoggerFactory.getLogger(InterfacerMappingController.class);

	private final InterfacerMappingServices interfacingMapperService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param unitService    UnitService
	 */
	public InterfacerMappingController(InterfacerMappingServices interfacingMapperService,
			RequestContext requestContext) {
		super();
		this.interfacingMapperService = interfacingMapperService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of available interfacermapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         interfacermappings
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getInterfacerMapping")
	public ResponseEntity<Object> getInterfacerMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getInterfacerMapping called");
		return interfacingMapperService.getInterfacerMapping(userInfo);
	}

	/**
	 * This method will is used to make a new entry to interfacermapping table.
	 * 
	 * @param inputMap map object holding params ( interfacermapping
	 *                 [InterfacerMapping] object holding details to be added in
	 *                 interfacermapping table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "interfacermapping": {
	 *                 "sinterfacermappingname": "cm", "sinterfacermappingsynonym":
	 *                 "cm" }, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": - 1,"ndeputyusercode":
	 *                 -1,"ndeputyuserrole": -1, "nformcode": 200,"nmastersitecode":
	 *                 -1,"nmodulecode": 5, "nreasoncode": 0,"nsitecode":
	 *                 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename": "Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         interfacermapping already exists/ list of interfacermappings along
	 *         with the newly added interfacermapping.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createInterfacerMapping")
	public ResponseEntity<Object> createInterfacerMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InterfacerMapping InterfacerMapping = objmapper.convertValue(inputMap.get("interfacermapping"),
				InterfacerMapping.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return interfacingMapperService.createInterfacerMapping(InterfacerMapping, userInfo);
	}

	/**
	 * This method is used to delete an entry in interfacermapping table
	 * 
	 * @param inputMap [Map] object with keys of InterfacerMapping entity and
	 *                 UserInfo object. Input:{ "interfacermapping":
	 *                 {"ninterfacermappingcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 200,"nmastersitecode": -1, "nmodulecode": 5,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         interfacermapping record is not available/ string message as 'Record
	 *         is used in....' when the interfacermapping is associated in
	 *         transaction / list of all interfacermappings excluding the deleted
	 *         record
	 * @throws Exception exception
	 */

	@PostMapping(value = "/deleteInterfacerMapping")
	public ResponseEntity<Object> deleteInterfacerMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InterfacerMapping InterfacerMapping = objmapper.convertValue(inputMap.get("interfacermapping"),
				InterfacerMapping.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return interfacingMapperService.deleteInterfacerMapping(InterfacerMapping, userInfo);
	}

	/**
	 * This method is used to update selected interfacermapping details.
	 * 
	 * @param inputMap [map object holding params( interfacermapping
	 *                 [InterfacerMapping] object holding details to be updated in
	 *                 interfacermapping table, userinfo [UserInfo] holding logged
	 *                 in user details) Input:{ "unit":
	 *                 {"ninterfacermappingcode":1,"sinterfacermappingname":
	 *                 "m","sinterfacermappingsynonym": "m", "sdescription": "m",
	 *                 "ndefaultstatus": 3 }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 200,"nmastersitecode": -1, "nmodulecode": 5, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         interfacermapping record is not available/ list of all units and
	 *         along with the updated interfacermapping.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateInterfacerMapping")
	public ResponseEntity<Object> updateInterfacerMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final InterfacerMapping InterfacerMapping = objmapper.convertValue(inputMap.get("interfacermapping"),
				InterfacerMapping.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return interfacingMapperService.updateInterfacerMapping(InterfacerMapping, userInfo);
	}

	/**
	 * @param inputMap [Map] holds the interfacerMapping code to get
	 * @return response entity object holding response status and data of single
	 *         interfacerMapping object
	 * @throws Exception
	 */
	@PostMapping(value = "/getInterfaceType")
	public ResponseEntity<Object> getInterfaceType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return interfacingMapperService.getInterfaceType(userInfo);
	}

	/**
	 * @param inputMap [Map] holds the interfacerMapping code to get
	 * @return response entity object holding response status and data of single
	 *         interfacerMapping object
	 * @throws Exception
	 */
	@PostMapping(value = "/getTestMaster")
	public ResponseEntity<Object> getTestMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return interfacingMapperService.getTestMaster(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve a specific interfacermapping record.
	 * 
	 * @param inputMap [Map] map object with "ninterfacermappingcode" and "userinfo"
	 *                 as keys for which the data is to be fetched Input:{
	 *                 "ninterfacermappingcode": 1, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 200,"nmastersitecode": -1,"nmodulecode": 5,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with interfacermapping object for the specified
	 *         primary key / with string message as 'Deleted' if the
	 *         interfacermapping record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveInterfacerMappingById")
	public ResponseEntity<Object> getActiveInterfacerMappingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ninterfacemappingcode = (Integer) inputMap.get("ninterfacemappingcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return interfacingMapperService.getActiveInterfacerMappingById(ninterfacemappingcode, userInfo);
	}

}
