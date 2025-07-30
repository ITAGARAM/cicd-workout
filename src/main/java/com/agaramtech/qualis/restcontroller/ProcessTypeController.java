package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.service.processtype.ProcessTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/processtype")
public class ProcessTypeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessTypeController.class);
	private RequestContext requestContext;
	private final ProcessTypeService objProcessTypeService;

	public ProcessTypeController(RequestContext requestContext, ProcessTypeService objProcessTypeService) {
		super();
		this.requestContext = requestContext;
		this.objProcessTypeService = objProcessTypeService;
	}

	/**
	 * This Method is used to get the over all processtypes with respect to site
	 * 
	 @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * @return a response entity which holds the list of processtype with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getProcessType")
	public ResponseEntity<Object> getProcessType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getProcessType() called");
		return objProcessTypeService.getProcessType(userInfo);
	}
	
	/**
	 * This method is used to add a new entry to processtype table.
	 * 
	 * @param inputMap map object holding params ( 
	 * 								processtype [processtype]  object holding details to be added in processtype table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "processtype": { "sprocesstypename": "processtype", "sdescription": "NA" },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,
									    	"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 232,"nmastersitecode": -1,
									    	"nmodulecode": 71,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
									    	"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
									    	"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00",
									    	"slanguagefilename": "Msg_en_US","slanguagename": "English",
									    	"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
									    	"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
									    	"ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the processtype already exists/ 
	 * 			list of sampledonor's along with the newly added processtype.
	 * @throws Exception
	 */

	@PostMapping(value = "/createProcessType")
	public ResponseEntity<Object> createProcessType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objProcessTypeService.createProcessType(inputMap, userInfo);

	}


	/**
	 * This method is used to retrieve a specific processtype record.
	 * @param inputMap [Map] map object with "nprocesstypecode" and "userinfo" as keys for which the data is to be fetched
	 * 					Input:{
     						"nprocesstypecode": 1,
    						"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,
									    	"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 232,"nmastersitecode": -1,
									    	"nmodulecode": 71,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
									    	"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
									    	"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00",
									    	"slanguagefilename": "Msg_en_US","slanguagename": "English",
									    	"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
									    	"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
									    	"ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
						}
	 * @return ResponseEntity with processtype object for the specified primary key / with string message as
	 * 						'Deleted' if the processtype record is not available
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getActiveProcessTypeById")
	public ResponseEntity<Object> getActiveProcessTypeById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objProcessTypeService.getActiveProcessTypeById(inputMap, userInfo);

	}


	
	/**
	 * This method is used to update selected processtype details.
	 * 
	 * @param inputMap map object holding params(
	 * 					processtype [processtype]  object holding details to be updated in processtype table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"processtype": {"nprocesstypecode":1,"sprocesstypename": "Name", "sdescription": ""},
    						"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,
									    	"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 232,"nmastersitecode": -1,
									    	"nmodulecode": 71,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
									    	"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
									    	"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00",
									    	"slanguagefilename": "Msg_en_US","slanguagename": "English",
									    	"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
									    	"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
									    	"ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
						}
	 * @return ResponseEntity with string message as 'Already Deleted' if the processtype record is not available/ 
	 * 			list of all processtype and along with the updated processtype.	
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateProcessType")
	public ResponseEntity<Object> updateProcessType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objProcessTypeService.updateProcessType(inputMap, userInfo);
	}


	/**
	 * This method is used to delete an entry in processtype table
	 * 
	 * @param inputMap [Map] object with keys of processtype entity and UserInfo object.
	 * 					Input:{
     						"processtype": {"nprocesstypecode":1},
    						"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,
									    	"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 232,"nmastersitecode": -1,
									    	"nmodulecode": 71,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
									    	"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
									    	"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00",
									    	"slanguagefilename": "Msg_en_US","slanguagename": "English",
									    	"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
									    	"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
									    	"ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
						}
	 * @return ResponseEntity with string message as 'Already deleted' if the processtype record is not available/ 
	 * 			string message as 'Record is used in....' when the processtype is associated in transaction /
	 * 			list of all processtype's excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteProcessType")
	public ResponseEntity<Object> deleteProcessType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objProcessTypeService.deleteProcessType(inputMap, userInfo);
	}
}
