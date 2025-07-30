package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.basemaster.model.StorageCondition;
import com.agaramtech.qualis.basemaster.service.storagecondition.StorageConditionService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/storagecondition")
public class StorageConditionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CityController.class);

	private RequestContext requestContext;
	private final StorageConditionService storageconditionService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext          RequestContext to hold the request
	 * @param storageconditionService StorageConditionService
	 */
	public StorageConditionController(RequestContext requestContext, StorageConditionService storageconditionService) {
		super();
		this.requestContext = requestContext;
		this.storageconditionService = storageconditionService;
	}

	/**
	 * This method is used to retrieve list of available storagecondition(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         storageconditions
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getStorageCondition")
	public ResponseEntity<Object> getStorageCondition(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getStorageCondition");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return storageconditionService.getStorageCondition(userInfo);

	}

	/**
	 * This method will is used to make a new entry to storagecondition table.
	 * 
	 * @param inputMap map object holding params ( storagecondition
	 *                 [StorageCondition] object holding details to be added in
	 *                 storagecondition table, userinfo [UserInfo] holding logged in
	 *                 user details ) Input:{ "storagecondition": {
	 *                 "sstorageconditionname": "sc", "sdescription": "sc" },
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 34,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         storagecondition already exists/ list of storageconditions along with
	 *         the newly added storagecondition.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createStorageCondition")
	public ResponseEntity<Object> createStorageCondition(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final StorageCondition storagecondition = objmapper.convertValue(inputMap.get("storagecondition"),
				StorageCondition.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return storageconditionService.createStorageCondition(storagecondition, userInfo);
	}

	/**
	 * This method is used to retrieve a specific storagecondition record.
	 * 
	 * @param inputMap [Map] map object with "nstorageconditioncode" and "userinfo"
	 *                 as keys for which the data is to be fetched Input:{
	 *                 "nstorageconditioncode": 1, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 34,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with StorageCondition object for the specified primary
	 *         key / with string message as 'Deleted' if the storagecondition record
	 *         is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveStorageConditionById")
	public ResponseEntity<Object> getActiveStorageConditionById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nstorageconditioncode = (Integer) inputMap.get("nstorageconditioncode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return storageconditionService.getActiveStorageConditionById(nstorageconditioncode, userInfo);
	}

	/**
	 * This method is used to update selected storagecondition details.
	 * 
	 * @param inputMap [map object holding params( storagecondition
	 *                 [StorageCondition] object holding details to be updated in
	 *                 storagecondition table, userinfo [UserInfo] holding logged in
	 *                 user details) Input:{ "storagecondition":
	 *                 {"nstorageconditioncode":1,"sstorageconditionname": "scm",
	 *                 "sdescription": "SCM", "ndefaultstatus": 3 }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 34,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         storagecondition record is not available/ list of all
	 *         storageconditions and along with the updated storagecondition.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateStorageCondition")
	public ResponseEntity<Object> updateStorageCondition(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final StorageCondition storagecondition = objmapper.convertValue(inputMap.get("storagecondition"),
				StorageCondition.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return storageconditionService.updateStorageCondition(storagecondition, userInfo);
	}

	/**
	 * This method is used to delete an entry in StorageCondition table
	 * 
	 * @param inputMap [Map] object with keys of StorageCondition entity and
	 *                 UserInfo object. Input:{ "storagecondition":
	 *                 {"nstorageconditioncode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 34,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         storagecondition record is not available/ string message as 'Record
	 *         is used in....' when the storagecondition is associated in
	 *         transaction / list of all storageconditions excluding the deleted
	 *         record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteStorageCondition")
	public ResponseEntity<Object> deleteStorageCondition(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final StorageCondition storagecondition = objmapper.convertValue(inputMap.get("storagecondition"),
				StorageCondition.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return storageconditionService.deleteStorageCondition(storagecondition, userInfo);
	}

}