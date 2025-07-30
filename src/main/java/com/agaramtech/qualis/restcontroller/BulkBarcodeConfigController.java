package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.basemaster.model.BulkBarcodeConfigDetails;
import com.agaramtech.qualis.bulkbarcodeconfiguration.model.BulkBarcodeConfig;
import com.agaramtech.qualis.bulkbarcodeconfiguration.service.BulkBarcodeConfigService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Bulk Barcode Config Service methods
 */
@RestController
@RequestMapping("/bulkbarcodeconfiguration")
public class BulkBarcodeConfigController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkBarcodeConfigController.class);
	private RequestContext requestContext;
	private final BulkBarcodeConfigService bulkBarcodeConfigService;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param bulkBarcodeConfigService BulkBarcodeConfigService
	 */
	public BulkBarcodeConfigController(RequestContext requestContext,
			BulkBarcodeConfigService bulkBarcodeConfigService) {
		super();
		this.requestContext = requestContext;
		this.bulkBarcodeConfigService = bulkBarcodeConfigService;
	}

	/**
	 * This method is used to retrieve list of all active bulkbarcodeconfig entries for the logged in site.
	 * @param inputMap A map object containing "userinfo" as a key, which includes details required to fetch the data.
	 * 					Input : {"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}},		
	 * @return ResponseEntity object containing the response status and a map of key-value pairs,
	 * which includes a list of mapped project types with their active BulkBarcodeConfig data,
	 * related processes, and additional details of the selected project type and barcode details.	
	 * @throws Exception 
	 */
	@PostMapping(value = "/getBulkBarcodeConfiguration")
	public ResponseEntity<Object> getBulkBarcodeConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getBulkBarcodeConfiguration() called");
		return bulkBarcodeConfigService.getBulkBarcodeConfiguration(userInfo);
	}

	/**
	 * This method is used to retrieve a list of all active barcodemaster entries.
	 * @param inputMap  [Map] map object with "nbulkbarcodeconfigcode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nbulkbarcodeconfigcode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return response entity  object holding response status and list of all barcodemaster
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getBarcodeMaster")
	public ResponseEntity<Object> getBarcodeMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		int nbulkbarcodeconfigcode = (int) inputMap.get("nbulkbarcodeconfigcode");
		return bulkBarcodeConfigService.getBarcodeMaster(userInfo, nbulkbarcodeconfigcode);
	}

	
	/**
	 * This method will is used to make a new entry to bulkbarcodeconfig  table.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfig [BulkBarcodeConfig]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                           Input:{
									    "bulkbarcodeconfig": { "nbarcodelength":2,"nprojecttypecode":2, 
															   "sconfigname":"co-1", "sdescription":"e1" },
										"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"},
	 * 									{"fromDate":"2025-05-26T00:00:00.000Z"},
	 * 	 								{"toDate":"2025-06-02T23:59:59.599Z"},
	 * 									{"nprojecttypecode":1}},		
	 * @return ResponseEntity with string message as 'Already Exist' if the configuration name already exist for the selected project type / 
	 * 			list of bulkbarcodeconfig entries along with the newly added bulkbarcodeconfig .
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createBulkBarcodeConfiguration")
	public ResponseEntity<Object> createBulkBarcodeConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return bulkBarcodeConfigService.createBulkBarcodeConfiguration(inputMap, userInfo);
	}

	/**
	 * This method is used to update selected bulkbarcodeconfig details.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfig [BulkBarcodeConfig]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                           Input:{
									   "bulkbarcodeconfig": { "nbarcodelength":2,"nbulkbarcodeconfigcode":7,"nprojecttypecode":2, 
															   "sconfigname":"co-1", "sdescription":"e13" },
										"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}},		
	 * @return ResponseEntity with bulkbarcodeconfig object for the specified primary key / with string message as
	 * 						'Already Deleted' if the bulkbarcodeconfig record is not available/with string message as 
	 * 						'Already Exist' if the configuration name already exist for the selected project type
	 * @throws Exception exception
	 */		
	@PostMapping(value = "/updateBulkBarcodeConfiguration")
	public ResponseEntity<Object> updateBulkBarcodeConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final BulkBarcodeConfig objBulkBarcodeConfig = objmapper.convertValue(inputMap.get("bulkbarcodeconfig"),
				BulkBarcodeConfig.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeConfigService.updateBulkBarcodeConfiguration(objBulkBarcodeConfig, userInfo);
	}
	
	/**
	 * This method is used to delete an entry in bulkbarcodeconfig table
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfig" and "userinfo" as keys for which the data is to be fetched

	 * 					Input:{
     						"bulkbarcodeconfig": {"dmodifieddate":"2025-06-11T10:58:36Z","nbarcodelength":2,
												"nbulkbarcodeconfigcode":5,"nbulkbarcodeconfigversioncode":5,"nprojecttypecode":1,"nsitecode":-1,
												"nstatus":1,"ntransactionstatus":8,"sconfigname":"c-01",
												"sdescription":"e13","sprojecttypename":"PTB",
												"stransdisplaystatus": "Draft","sversionno":"-"},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the bulkbarcodeconfig record is not available/ 
	 * 			string message as 'Record is used in....' when the bulkbarcodeconfig is associated in transaction /
	 * 			list of all bulkbarcodeconfig excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteBulkBarcodeConfiguration")
	public ResponseEntity<Object> deleteBulkBarcodeConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final BulkBarcodeConfig objBulkBarcodeConfig = objmapper.convertValue(inputMap.get("bulkbarcodeconfig"),
				BulkBarcodeConfig.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeConfigService.deleteBulkBarcodeConfiguration(objBulkBarcodeConfig, userInfo);
	}

	/**
	 * This method is used to retrieve a specific bulkbarcodeconfig record.
	 * @param inputMap  [Map] map object with "nbulkbarcodeconfigcode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nbulkbarcodeconfigcode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with bulkbarcodeconfig object for the specified primary key / with string message as
	 * 						'Already Deleted' if the bulkbarcodeconfig record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveBulkBarcodeConfigurationById")
	public ResponseEntity<Object> getActiveBulkBarcodeConfigurationById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nbulkbarcodeconfigcode = (Integer) inputMap.get("nbulkbarcodeconfigcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeConfigService.getActiveBulkBarcodeConfigurationById(nbulkbarcodeconfigcode, userInfo);
	}
	
	/**
	 * This method is used to update selected bulkbarcodeconfigversion table.
	 * @param inputMap [map object holding params(
	 * 					bulkbarcodeconfig [BulkBarcodeConfig]  object holding details to be updated in bulkbarcodeconfigversion table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"bulkbarcodeconfig": {"dmodifieddate":"2025-06-11T13:28:28Z","nbarcodelength":2,"nbulkbarcodeconfigcode":8,"nbulkbarcodeconfigversioncode":28,
												  "nprojecttypecode":1,"nsitecode":-1,"nstatus":1,"ntransactionstatus":8,"sconfigname":"22","sdescription":"23","sprojecttypename":(null),			
												  "stransdisplaystatus":"Draft","sversion":"-"},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}

	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the bulkbarcodeconfig record is not available/ 
	 * list of all bulkbarcodeconfig and along with the updated bulkbarcodeconfig and bulkbarcodeconfigversion entries.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/approveBulkBarcodeConfiguration")
	public ResponseEntity<Object> approveBulkBarcodeConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final BulkBarcodeConfig objBulkBarcodeConfig = objmapper.convertValue(inputMap.get("bulkbarcodeconfig"),
				BulkBarcodeConfig.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeConfigService.approveBulkBarcodeConfiguration(objBulkBarcodeConfig, userInfo);
	}

	/**
	 * This method is used to retrieve a list of all active barcodemaster entries for the selected project type.
	 * @param inputMap  [Map] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nprojecttypecode": 5,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity object containing the response status and a map of key-value pairs,
	 * which includes a list of mapped project types with their active BulkBarcodeConfig data,
	 * related processes, and additional details of the selected project type and barcode details.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getFilterProjectType")
	public ResponseEntity<Object> getFilterProjectType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		int nprojecttypecode = (Integer) inputMap.get("nprojecttypecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeConfigService.getFilterProjectType(userInfo, nprojecttypecode);
	}

	/**
	 * This method is used to retrieve a specific bulkbarcodeconfig record.
	 * @param inputMap  [Map] map object with "nprojecttypecode","nbulkbarcodeconfigcode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nprojecttypecode": 1,"nbulkbarcodeconfigcode":2,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with bulkbarcodeconfig object for the specified primary key / with string message as
	 * 						'Already Deleted' if the bulkbarcodeconfig record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getBulkBarcodeConfig")
	public ResponseEntity<Object> getBulkBarcodeConfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		int nprojecttypecode = (Integer) inputMap.get("nprojecttypecode");
		int nbulkbarcodeconfigcode = Integer.valueOf(inputMap.get("nbulkbarcodeconfigcode").toString());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeConfigService.getBulkBarcodeConfig(nbulkbarcodeconfigcode, nprojecttypecode, userInfo);
	}
	
	/**
	 * This method will is used to make a new entry to bulkbarcodeconfigdetails  table for the selected bulkbarcodeconfig.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfigdetails [BulkBarcodeConfigDetails]  object holding details to be added in bulkbarcodeconfig table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                           Input:{
									    "bulkbarcodeconfigdetails": 	{"jsondata":{"isneedparent":4,"isvalidationrequired":4,"nbarcodemastercode":7,"nformcode":194,"parentData":"",
													"sfieldname":"1","sformname":"Sample Punch Site","sprimarykeyfieldname":"nsamplepunchsitecode","stablefieldname":"spunchdescription"},
													"nbulkbarcodeconfigcode":8,"nfieldlength":23,"nfieldstartposition":0,	"nneedmaster":3,"nprojecttypecode":1,
													"nquerybuildertablecode":-1,"nqueryneed":4,	"nsorter":2,"sdescription":"23","sfieldname":"1","squery=(null),
													"stablecolumnname":"ncode", "stablename":"samplepunchsite"},
										"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}},		
	 * @return ResponseEntity with string message as 'Already Exist' if the Sorter already exist for the selected bulkbarcodeconfig / 
	 * 			list of bulkbarcodeconfigdetails entries along with the newly added bulkbarcodeconfigdetails .
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createBulkBarcodeMaster")
	public ResponseEntity<? extends Object> createBulkBarcodeMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return bulkBarcodeConfigService.createBulkBarcodeMaster(inputMap, userInfo);
	}
	
	/**
	 * This method is used to retrieve a list of all active barcodemaster entries for the selected project type.
	 * @param inputMap  [Map] map object with "nprojecttypecode","stablename" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nprojecttypecode": 5,"stablename":"samplepunchsite",
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with Field Length object for the specified nprojecttypecode.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getFieldLengthService")
	public ResponseEntity<Object> getFieldLengthService(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeConfigService.getFieldLengthService(inputMap, userInfo);
	}

	
	/**
	 * This method is used to update selected bulkbarcodeconfigdetails details.
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched
	 * 								bulkbarcodeconfigdetails [BulkBarcodeConfigDetails]  object holding details to be added in bulkbarcodeconfigdetails table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                           Input:{
									   "bulkbarcodeconfigdetails": {"isvalidationrequired":4,"nbulkbarcodeconfigcode":8
																	"nbulkbarcodeconfigdetailscode":27,"nprojecttypecode":1,"nsorter":13,"sdescription":"" },
										"userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}},		
	 * @return ResponseEntity with bulkbarcodeconfigdetails object for the specified primary key / with string message as
	 * 						'Already Deleted' if the bulkbarcodeconfigdetails record is not available/with string message as 
	 * 						'Already Exist' if the sorter already exist for the selected project type
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateBulkBarcodeMaster")
	public ResponseEntity<? extends Object> updateBulkBarcodeMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final BulkBarcodeConfigDetails objBulkBarcodeConfig = objmapper
				.convertValue(inputMap.get("bulkbarcodeconfigdetails"), BulkBarcodeConfigDetails.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeConfigService.updateBulkBarcodeMaster(objBulkBarcodeConfig, userInfo);
	}
	
	/**
	 * This method is used to delete an entry in bulkbarcodeconfigdetails table
	 * @param inputMap  [inputMap] map object with "bulkbarcodeconfigdetails" and "userinfo" as keys for which the data is to be fetched

	 * 					Input:{
     						"bulkbarcodeconfigdetails": {	"dmodifieddate":"2025-06-11T13:28:28Z","isneedparent":0"isvalidationrequired":0,
														 	"jsondata":{"isneedparent":4,"isvalidationrequired":4,"nbarcodemastercode":7,"nformcode":194,"parentData:"",
														 	"sfieldname":"1","sformname":"Sample Punch Site","sprimarykeyfieldname":"nsamplepunchsitecode",
															"stablefieldname":"spunchdescription"}"nbarcodemastercode":0,"nbulkbarcodeconfigcode":8,	"nbulkbarcodeconfigdetailscode":28,
															"nfieldlength":23,"nfieldstartposition":0,"nneedmaster":3,"nparentfieldlength":0,"nparentnbarcodemastercode":0,
															"nprojecttypecode":1,"nquerybuildertablecode":-1,"nqueryneed":4,"nsitecode":-1,"nsorter":2,"nstatus":1,
															"sdescription":"23","sdisplayname":"1","sfieldname":"1","sformname":(null),"sparentformname":(null),
															"squery":(null),"stablecolumnname":"ncode", "stablename":"samplepunchsite"},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the bulkbarcodeconfigdetails record is not available/ 
	 * 			string message as 'Record is used in....' when the bulkbarcodeconfigdetails is associated in transaction /
	 * 			list of all bulkbarcodeconfigdetails excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteBulkBarcodeMaster")
	public ResponseEntity<? extends Object> deleteBulkBarcodeMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final BulkBarcodeConfigDetails objBulkBarcodeConfig = objmapper
				.convertValue(inputMap.get("bulkbarcodeconfigdetails"), BulkBarcodeConfigDetails.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeConfigService.deleteBulkBarcodeMaster(objBulkBarcodeConfig, userInfo);
	}

	/**
	 * This method is used to retrieve a specific bulkbarcodeconfigdetails record.
	 * @param inputMap  [Map] map object with "nbulkbarcodeconfigdetailscode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nbulkbarcodeconfigdetailscode": 5,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with bulkbarcodeconfigdetails object for the specified primary key / with string message as
	 * 						'Already Deleted' if the bulkbarcodeconfigdetails record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveBulkBarcodeMasterById")
	public ResponseEntity<Object> getActiveBulkBarcodeMasterById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nbulkbarcodeconfigdetailscode = (Integer) inputMap.get("nbulkbarcodeconfigdetailscode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeConfigService.getActiveBulkBarcodeMasterById(nbulkbarcodeconfigdetailscode, userInfo);
	}

	/**
	 * This method is used to retrieve a list of all active ParentBarcodeDetails entries for the selected project type and Bulk Barcode Config.
	 * @param inputMap  [Map] map object with "nprojecttypecode","nbulkbarcodeconfigcode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nprojecttypecode": 5,"nbulkbarcodeconfigcode":1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity object containing the response status and a map of key-value pairs,
	 * which includes a list of mapped project types with their active ParentBarcodeDetails data.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getParentBulkBarcodeMaster")
	public ResponseEntity<Object> getParentBulkBarcodeMaster(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		int nprojecttypecode = (Integer) inputMap.get("nprojecttypecode");
		int nbulkbarcodeconfigcode = Integer.valueOf(inputMap.get("nbulkbarcodeconfigcode").toString());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return bulkBarcodeConfigService.getParentBulkBarcodeMaster(nbulkbarcodeconfigcode, nprojecttypecode, userInfo,
				inputMap);
	}
}
