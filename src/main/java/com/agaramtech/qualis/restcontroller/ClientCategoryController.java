package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.contactmaster.model.ClientCategory;
import com.agaramtech.qualis.contactmaster.service.clientcategory.ClientCategoryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the ClientCategory Service methods.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 08- Sep- 2020
 */
@RestController
@RequestMapping("/clientcategory")
public class ClientCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CityController.class);

	private final ClientCategoryService clientCategoryService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext        RequestContext to hold the request
	 * @param clientCategoryService ClientCategoryService
	 */
	public ClientCategoryController(RequestContext requestContext, ClientCategoryService clientCategoryService) {
		super();
		this.requestContext = requestContext;
		this.clientCategoryService = clientCategoryService;
	}

	/**
	 * This method is used to retrieve list of active clientcategory for the
	 * specified site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active clientcategory that are to be
	 *         listed for the specified site
	 */
	@PostMapping(value = "/getClientCategory")
	public ResponseEntity<List<ClientCategory>> getClientCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getClientCategory");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return clientCategoryService.getClientCategory(userInfo);

	}

	/**
	 * This method is used to retrieve list of active clientcategory for the
	 * specified site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active clientcategory that are to be
	 *         listed for the specified site
	 */
	@PostMapping(value = "/getClientCategoryForPortal")
	public ResponseEntity<Object> getClientCategoryForPortal(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return clientCategoryService.getClientCategoryForPortal(userInfo);
	}

	/**
	 * This method will is used to make a new entry to clientcategory table.
	 * 
	 * @param inputMap map object holding params ( clientcategory [ClientCategory]
	 *                 object holding details to be added in clientcategory table,
	 *                 userinfo [UserInfo] holding logged in user details ) Input:{
	 *                 "clientcategory": { "nsitecode": -1, "sclientcatname":
	 *                 "Client Cat", "sdescription": "", "ndefaultstatus": 4 },
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 20,"nmastersitecode": -1,"nmodulecode": 15,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         clientcategory already exists/ list of clientcategories along with
	 *         the newly added clientcategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createClientCategory")
	public ResponseEntity<? extends Object> createClientCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ClientCategory clientCategory = objmapper.convertValue(inputMap.get("clientcategory"),
				ClientCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientCategoryService.createClientCategory(clientCategory, userInfo);
	}

	/**
	 * This method is used to retrieve a specific clientcategory record.
	 * 
	 * @param inputMap [Map] map object with "nclientcatcode" and "userinfo" as keys
	 *                 for which the data is to be fetched Input:{ "nclientcatcode":
	 *                 1, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": - 1,"ndeputyusercode":
	 *                 -1,"ndeputyuserrole": -1, "nformcode": 20,"nmastersitecode":
	 *                 -1,"nmodulecode": 15, "nreasoncode": 0,"nsitecode":
	 *                 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename": "Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with ClientCategory object for the specified primary
	 *         key / with string message as 'Deleted' if the clientcategory record
	 *         is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveClientCategoryById")
	public ResponseEntity<Object> getActiveClientCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nclientcatcode = (Integer) inputMap.get("nclientcatcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientCategoryService.getActiveClientCategoryById(nclientcatcode, userInfo);
	}

	/**
	 * This method is used to update selected clientcategory details.
	 * 
	 * @param inputMap [map object holding params( clientcategory [ClientCategory]
	 *                 object holding details to be updated in clientcategory table,
	 *                 userinfo [UserInfo] holding logged in user details) Input:{
	 *                 "clientcategory": {"nclientcatcode": 2, "sclientcatname":
	 *                 "Client Cat Upgraded", "sdescription": "MAX",
	 *                 "ndefaultstatus": 4, "nsitecode": -1, "nstatus": 1,},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 20,"nmastersitecode": -1,"nmodulecode": 15,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         clientcategory record is not available/ list of all clientcategories
	 *         and along with the updated clientcategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateClientCategory")
	public ResponseEntity<? extends Object> updateClientCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ClientCategory clientCategory = objmapper.convertValue(inputMap.get("clientcategory"),
				ClientCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientCategoryService.updateClientCategory(clientCategory, userInfo);
	}

	/**
	 * This method is used to delete an entry in clientcategory table
	 * 
	 * @param inputMap [Map] object with keys of ClientCategory entity and UserInfo
	 *                 object. Input:{ "clientcategory": {"nclientcatcode": 2,
	 *                 "sclientcatname": "Client Cat Upgraded", "sdescription":
	 *                 "MAX", "ndefaultstatus": 4, "nsitecode": -1, "nstatus": 1,
	 *                 "dmodifieddate": null, "sdisplaystatus": "No"}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 20,"nmastersitecode": -1,"nmodulecode": 15,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         clientcategory record is not available/ string message as 'Record
	 *         is used in....' when the clientcategory is associated in
	 *         transaction / list of all clientcategories excluding the deleted
	 *         record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteClientCategory")
	public ResponseEntity<? extends Object> deleteClientCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ClientCategory clientCategory = objmapper.convertValue(inputMap.get("clientcategory"),
				ClientCategory.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientCategoryService.deleteClientCategory(clientCategory, userInfo);
	}
}
