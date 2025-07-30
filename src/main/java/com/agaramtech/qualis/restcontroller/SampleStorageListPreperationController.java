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
import com.agaramtech.qualis.storagemanagement.service.samplestoragelistpreperation.SampleStorageListPreperationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the SampleStorageListPreperationService Service methods
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@RestController
@RequestMapping("/samplestoragelistpreperation")
public class SampleStorageListPreperationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleStorageListPreperationController.class);

	private RequestContext requestContext;
	private SampleStorageListPreperationService samplestoragelistpreperationservice;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext                      RequestContext to hold the request
	 * @param samplestoragelistpreperationservice SampleStorageListPreperationService
	 */
	public SampleStorageListPreperationController(RequestContext requestContext,
			SampleStorageListPreperationService samplestoragelistpreperationservice) {
		super();
		this.requestContext = requestContext;
		this.samplestoragelistpreperationservice = samplestoragelistpreperationservice;
	}

	/**
	 * This method is used to retrieve list of available sample repository(s). 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1, "slanguagetypecode": "en-US", "ntranssitecode": 1}}				
	 * @return response entity object holding response status and list of all sampleRepositories
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getsamplestoragelistpreperation")
	public ResponseEntity<Object> getsamplestoragelistpreperation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		LOGGER.info("getsamplestoragelistpreperation()");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragelistpreperationservice.getsamplestoragelistpreperation(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of available sample repository(s). 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input :{"label": "samplestoragelistpreperation","valuemember": "nsamplestoragetransactioncode",
  								 "filterquery": "nsamplestoragelocationcode IS NOT NULL and nprojecttypecode=1",
  								 "source": "view_samplelistprep_",
  								 "userinfo":{nmastersitecode": -1, "slanguagetypecode": "en-US", "ntranssitecode": 1}
  							}
	 * @return response entity object holding response status and list of all sampleRepositories
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getdynamicfilterexecutedata")
	public ResponseEntity<Map<String, Object>> getDynamicFilterExecuteData(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragelistpreperationservice.getDynamicFilterExecuteData(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available sample repository(s). 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 					which the list is to be fetched 	
	 * 					Input : {
	 * 						"nprojecttypecode": 1,
	 * 						"spositionvalue": 1,
	 * 						"userinfo":{nmastersitecode": -1, "slanguagetypecode": "en-US", "ntranssitecode": 1}
	 * 						}	
	 * @return response entity object holding response status and list of all sampleRepositories
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSelectedBarcodeData")
	public ResponseEntity<Object> getSelectedBarcodeData(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragelistpreperationservice.getSelectedBarcodeData(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of available sample repository(s) for the specified nprojecttypecode and site. 
	 * @param inputMap [Map] map object with "nprojecttypecode" and "userinfo" as keys for which the data is to be fetched	
	 * 					Input : {
	 * 						"nprojecttypecode": 1,
	 * 						"userinfo":{nmastersitecode": -1, "slanguagetypecode": "en-US", "ntranssitecode": 1}
	 * 						}				
	 * @return response entity object holding response status and list of all sampleRepositories
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getProjectbarcodeconfig")
	public ResponseEntity<Map<String, Object>> getProjectbarcodeconfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplestoragelistpreperationservice.getProjectbarcodeconfig(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve sample storage details based on imported excel.
	 * 
	 * @param request [MultipartHttpServletRequest] multipart request with
	 *                parameters:
	 *                ImportFile,nformcode,userinfo,nprojecttypecode,source,fieldName,label,valuemember
	 *                
	 *                Input: {"nformcode":198,"nprojecttypecode":1,"source":"view_samplelistprep_","fieldName":"Sample Id",
						"label":"samplestoragelistpreperation","valuemember":"nsamplestoragetransactioncode",
						"userinfo":{"activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,
							"ndeputyuserrole": -1, "nformcode": 198,"nmastersitecode": -1,"nmodulecode": 71,  "nreasoncode": 0,
							"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
							"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00",
							"slanguagefilename": "Msg_en_US","slanguagename": "English","slanguagetypecode": "en-US",
							"spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss","spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
							"sreason": "","ssitedate": "dd/MM/yyyy","ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
						}
	 * @return response entity object holding response status and list of sample storage details 
	 * 			based on imported excel.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getimportsampleiddata")
	public ResponseEntity<Object> getImportSampleIDData(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return samplestoragelistpreperationservice.getImportSampleIDData(request, userInfo);
	}
}
