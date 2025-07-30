package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.barcode.model.SampleCycle;
import com.agaramtech.qualis.barcode.service.samplecycle.SampleCycleService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Sample Cycle Service methods
 */
@RestController
@RequestMapping("/samplecycle")
public class SampleCycleController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleCycleController.class);
	private RequestContext requestContext;
	private final SampleCycleService samplecycleService;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param samplecycleService SampleCycleService
	 */
	public SampleCycleController(RequestContext requestContext, SampleCycleService samplecycleService) {
		super();
		this.requestContext = requestContext;
		this.samplecycleService = samplecycleService;
	}

	/**
	 * This method is used to retrieve list of available Samplecycle(s) records.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched 
	 *                 Input :{
	 *                      "userinfo":{nmastersitecode": -1}
	 *                      }
	 * @return response entity object holding response status and list of all Sample
	 *         Cycles
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSampleCycle")
	public ResponseEntity<Object> getSampleCycle(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getSampleCycle() called");
		requestContext.setUserInfo(userInfo);
		return samplecycleService.getSampleCycle(userInfo);

	}

	/**
	 * This method will is used to make a new entry to Sample Cycle table.
	 * 
	 * @param inputMap map object holding params( 
	 * 				   samplecycle [SampleCycle] object holding details to be added in samplecycle, 
	 *                 userinfo [UserInfo] holding logged in user details ) 
	 *                 Input:{
	 *                		 "samplecycle": { "ncode": 1, "ssamplecyclename":
	 *                						 "collection", nprojecttypecode:1 }, 
	 *                       "userinfo":{
	 *                 		         "activelanguagelist": ["en-US"], "isutcenabled":4,
	 *                               "ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":-1, 
	 *                               "nformcode": 189,"nmastersitecode": -1,"nmodulecode": 76,
	 *                               "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                               "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
	 *                               "nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                               "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                               "Msg_en_US","slanguagename": "English", "slanguagetypecode": "en-US",
	 *                               "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                               "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "",
	 *                               "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
	 *                          }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         samplecycle already exists/ list of sample cycle along with the newly
	 *         added Sample Cycle.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSampleCycle")
	public ResponseEntity<Object> createSampleCycle(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final SampleCycle sampleCycle = objMapper.convertValue(inputMap.get("samplecycle"), SampleCycle.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplecycleService.createSampleCycle(sampleCycle, userInfo);
	}

	/**
	 * This method is used to retrieve a specific samplecycle record.
	 * 
	 * @param inputMap [Map] map object with "nsamplecyclecode" and "userinfo" as
	 *                 keys for which the data is to be fetched 
	 *                 Input:{
	 *                     "nsamplescyclecode": 1, 
	 *                     "userinfo":{ 
	 *                           "activelanguagelist":["en-US"],
	 *                           "isutcenabled": 4,"ndeptcode": -1,
	 *                           "ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 189,
	 *                           "nmastersitecode": -1,"nmodulecode": 76, "nreasoncode": 0,
	 *                           "nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,
	 *                           "nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
	 *                           "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC +00:00",
	 *                           "slanguagefilename": "Msg_en_US",
	 *                           "slanguagename": "English", "slanguagetypecode": "en-US", 
	 *                           "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                           "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "",
	 *                           "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                            HH:mm:ss"}}
	 * @return ResponseEntity with SampleCycle object for the specified primary key
	 *         / with string message as 'Already Deleted' if the samplecycle record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveSampleCycleById")
	public ResponseEntity<Object> getActiveSampleCycleById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nsamplecyclecode = (Integer) inputMap.get("nsamplecyclecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return samplecycleService.getActiveSampleCycleById(nsamplecyclecode, userInfo);
	}

	/**
	 * This method is used to update selected samplecycle details.
	 * 
	 * @param inputMap [map object holding params( 
	 * 				 samplecycle [SampleCycel] object holding details to be updated in samplecycle table, 
	 *               userinfo  [UserInfo] holding logged in user details)
	 *                  Input:{ 
	 *                     "samplecycle": {
	 *                            "ncode":1,"ssamplecyclename":
	 *                            "collection","nprojecttyepcode": 1, }, 
	 *                     "userinfo":{
	 *                            "activelanguagelist": ["en-US"],"isutcenabled": 4,
	 *                            "ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
	 *                            "nformcode": 189,"nmastersitecode": -1, "nmodulecode": 76, 
	 *                            "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                            "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
	 *                            "nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                            "sgmtoffset": "UTC +00:00", "slanguagefilename":"Msg_en_US",
	 *                            "slanguagename": "English", "slanguagetypecode": "en-US",
	 *                            "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                            "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                            "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         sample cycle record is not available/ list of all sample cycle and
	 *         along with the updated sample cycle.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSampleCycle")
	public ResponseEntity<Object> updateSampleCycle(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final SampleCycle sampleCycle = objMapper.convertValue(inputMap.get("samplecycle"), SampleCycle.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplecycleService.updateSampleCycle(sampleCycle, userInfo);
	}

	/**
	 * This method is used to delete an entry in SampleCycle table
	 * 
	 * @param inputMap [Map] object with keys of SampleCycle entity and UserInfo object.
	 *                 Input:{ 
	 *                     "samplecycle": {"nsamplecyclecode":1}, 
	 *                     "userinfo":{
	 *                           "activelanguagelist": ["en-US"],"isutcenabled": 4,
	 *                           "ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
	 *                           "nformcode": 189,"nmastersitecode": -1, "nmodulecode": 76,
	 *                           "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                           "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
	 *                           "nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                           "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US",
	 *                           "slanguagename": "English", "slanguagetypecode":"en-US", 
	 *                           "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                           "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                           "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 *                           
	 * @return ResponseEntity with string message as 'Already deleted' if the sample cycle
	 *         record is not available/ string message as 'Record is used in....'
	 *         when the sample cycle is associated in transaction / list of all sample cycle
	 *         excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSampleCycle")
	public ResponseEntity<Object> deleteSampleCycle(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final SampleCycle sampleCycle = objMapper.convertValue(inputMap.get("samplecycle"), SampleCycle.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplecycleService.deleteSampleCycle(sampleCycle, userInfo);
	}
}
