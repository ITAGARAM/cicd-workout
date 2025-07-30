package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.service.bulkbarcodegeneration.BulkBarcodeGenerationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class is used to perform CRUD Operation on "bulkbarcodegeneration"
 * table by implementing methods from its interface.
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@RestController
@RequestMapping("/bulkbarcodegeneration")
public class BulkBarcodeGenerationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkBarcodeGenerationController.class);

	private RequestContext requestContext;
	private final BulkBarcodeGenerationService bulkBarcodeGenerationService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext  RequestContext to hold the request
	 * @param bulkBarcodeGenerationService BulkBarcodeGenerationService
	 */
	public BulkBarcodeGenerationController(RequestContext requestContext,
			BulkBarcodeGenerationService bulkBarcodeGenerationService) {
		super();
		this.requestContext = requestContext;
		this.bulkBarcodeGenerationService = bulkBarcodeGenerationService;
	}

	/**
	 * This method is used to retrieve list of all active bulkbarcodegeneration
	 * for the specified site.
	 * 
	 * @param userInfo [UserInfo] ntranssitecode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         bulkbarcodegeneration
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getBulkBarcodeGeneration")
	public ResponseEntity<Object> getBulkBarcodeGeneration(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getBulkBarcodeGeneration()");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeGenerationService.getBulkBarcodeGeneration(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of all active bulkbarcodegeneration
	 * for the specified site.
	 * @param inputMap map object holding params ( 
	 * 								nprojecttypecode holding details integer value,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "nprojecttypecode": 1,
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @param nprojecttype code [int] for which the list is to be fetched
	 * @param userInfo [UserInfo] ntranssitecode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         bulkbarcodegeneration
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getProjectBarcodceConfig")
	public ResponseEntity<Map<String, Object>> getProjectBarcodceConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int nprojecttypecode = (int) inputMap.get("nprojecttypecode");
		return bulkBarcodeGenerationService.getProjectBarcodceConfig(nprojecttypecode, userInfo);
	}

	/**
	 * This method is used to import list of all active excel date to bulkbarcodegeneration table 
	 * for the specified site.
	 * 
	 * @param userInfo [UserInfo] ntranssitecode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         bulkbarcodegeneration
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/importBulkBarcodeGeneration")
	public ResponseEntity<Object> importBulkBarcodeGeneration(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeGenerationService.importBulkBarcodeGeneration(request, userInfo);
	}

	/**
	 * This method is used to retrieve list of all active bulkbarcodegeneration details records
	 * for the specified site.
	 * @param inputMap map object holding params ( 
	 * 								nbulkbarcodeconfigcode,nbulkbarcodegenerationcode,fromDate,toDate, nprojecttypecode
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "nbulkbarcodeconfigcode": 1,
									    "nbulkbarcodegenerationcode": 1,
									    "fromDate": {"2024-05-01"},
									    "toDate": {"2024-05-31"},
									    "nprojecttypecode": 1,
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @param userInfo [UserInfo] ntranssitecode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         bulkbarcodegeneration detail records.
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getSelectedBarcodeGenerationDetail")
	public ResponseEntity<? extends Object> getSelectedBarcodeGenerationDetail(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeGenerationService.getSelectedBarcodeGenerationDetail(inputMap, userInfo);
	}

	/**
	 * This method id used to delete an entry in bulkbarcode generation table
	 * 
	 * @param inputMap [Map] object with keys of nbulkbarcodegenerationcode entity and UserInfo object.
	 * 					Input:{
     						"nbulkbarcodegenerationcode": {"nbulkbarcodegenerationcode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the bulkbarcode generation record is not available/ 
	 * 			string message as 'Record is used in....' /
	 * 			list of all bulkbarcode generation excluding the deleted record 
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteBulkBarcodeGeneration")
	public ResponseEntity<Object> deleteBulkBarcodeGeneration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeGenerationService.deleteBulkBarcodeGeneration(inputMap, userInfo);
	}

	/*
	 * This method is used to printing single or multiple barcode IDs. The logic is
	 * query-based and includes an audit trail.
	 * @param Map object holds the userinfo object, printer name, sbarcodename, ncontrolcode
	 * @param inputMap [Map] object with keys of sprintername,sbarcodename,ncontrolcode entity and UserInfo object.
	 * 					Input:{
     						"sprintername": {"sprintername":"aaaa"},
     						"sbarcodename" : {"sbarcodename":"bbbb"},
     						"ncontrolcode" : {"ncontrolcode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return response entity print barcode file.
	 * @throws Exception 
	 */
	@PostMapping(value = "/printBarcode")
	public ResponseEntity<Object> printBarcode(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		return bulkBarcodeGenerationService.printBarcode(inputMap);
	}

	/**
	 * This method id used to delete an entry in bulkbarcode generation table
	 * 
	 * @param inputMap [Map] object with keys of nbulkbarcodegenerationcode entity and UserInfo object.
	 * 					Input:{
     						"nbulkbarcodegenerationcode": {"nbulkbarcodegenerationcode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the bulkbarcode generation record is not available/ 
	 * 			string message as 'Record is used in....' /
	 * 			list of all bulkbarcode generation excluding the deleted record 
	 * @throws Exception
	 */
	@PostMapping(value = "/deleteBarcodeData")
	public ResponseEntity<? extends Object> deleteBarcodeData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeGenerationService.deleteBarcodeData(inputMap, userInfo);
	}

}