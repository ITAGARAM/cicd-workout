package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.basemaster.model.ContainerType;
import com.agaramtech.qualis.basemaster.service.containertype.ContainerTypeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the ContainerType Service methods.
 * 
 * @author ATE180
 * @version 9.0.0.1
 * @since 29-Jun-2020
 */
@RestController
@RequestMapping("/containertype")
public class ContainerTypeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContainerTypeController.class);
	private final ContainerTypeService containerTypeService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext       RequestContext to hold the request
	 * @param containerTypeService ContainerTypeService
	 */

	public ContainerTypeController(ContainerTypeService containerTypeService, RequestContext requestContext) {
		super();
		this.containerTypeService = containerTypeService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of active ContainerType for the
	 * specified site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active ContainerType that are to be
	 *         listed for the specified site
	 */
	@PostMapping(value = "/getContainerType")
	public ResponseEntity<Object> getContainerType(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("In getContainerType endpoint");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return containerTypeService.getContainerType(userInfo.getNmastersitecode());
	}

	/**
	 * This method is used to add new ContainerType for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of ContainerType entity. containerType
	 *                 [ContainerType] object holding details to be added in
	 *                 containerType table, userinfo [UserInfo] holding logged in
	 *                 user details ) Input:{ "ContainerTYpe": { "sunitname": "cm",
	 *                 "sunitsynonym": "cm" }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode": -
	 *                 1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 45,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return response entity of newly added ContainerType entity
	 */
	@PostMapping(value = "/createContainerType")
	public ResponseEntity<Object> createContainerType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ContainerType containerType = objmapper.convertValue(inputMap.get("containertype"), ContainerType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return containerTypeService.createContainerType(containerType, userInfo);
	}

	/**
	 * This method is used to update selected ContainerType details.
	 * 
	 * @param inputMap [map object holding params( containerType [ContainerType ]
	 *                 object holding details to be updated in ContainerType table,
	 *                 userinfo [UserInfo] holding logged in user details) Input:{
	 *                 "ContainerType ": {"nunitcode":1,"sunitname":
	 *                 "m","sunitsynonym": "m", "sdescription": "m",
	 *                 "ndefaultstatus": 3 }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 45,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
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
	 *         ContainerType record is not available/ list of all ContainerTypes and
	 *         along with the updated ContainerType .
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateContainerType")
	public ResponseEntity<Object> updateContainerType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final ContainerType containerType = objmapper.convertValue(inputMap.get("containertype"), ContainerType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return containerTypeService.updateContainerType(containerType, userInfo);
	}

	/**
	 * This method is used to retrieve a specific ContainerType record.
	 * 
	 * @param inputMap [Map] map object with "nunitcode" and "userinfo" as keys for
	 *                 which the data is to be fetched Input:{ "nContainerTypecode":
	 *                 1, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode":
	 *                 -1,"ndeputyuserrole": -1, "nformcode": 45,"nmastersitecode":
	 *                 -1,"nmodulecode": 1, "nreasoncode": 0,"nsitecode":
	 *                 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename": "Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with ContainerType object for the specified primary
	 *         key / with string message as 'Deleted' if the unit record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveContainerTypeById")
	public ResponseEntity<Object> getActiveContainerTypeById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ncontainertypecode = (Integer) inputMap.get("ncontainertypecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return containerTypeService.getActiveContainerTypeById(ncontainertypecode, userInfo);
	}

	/**
	 * This method is used to delete an entry in ContainerType table
	 * 
	 * @param inputMap [Map] object with keys of ContainerType entity and UserInfo
	 *                 object. Input:{ "unit": {"nContainerTypecode":1},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 45,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         ContainerType record is not available/ string message as 'Record is
	 *         used in....' when the ContainerType is associated in transaction /
	 *         list of all ContainerTypes excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteContainerType")
	public ResponseEntity<Object> deleteContainerType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final ContainerType containerType = objmapper.convertValue(inputMap.get("containertype"), ContainerType.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return containerTypeService.deleteContainerType(containerType, userInfo);
	}

}
