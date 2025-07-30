package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.barcode.model.SampleDonor;
import com.agaramtech.qualis.barcode.service.sampledonor.SampleDonorService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class is used to perform CRUD Operation on "sampledonor" table by
 * implementing methods from its interface.
 * 
 * @author ATE236
 * @version 10.0.0.2
 */

@RestController
@RequestMapping("/sampledonor")
public class SampleDonorController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleDonorController.class);

	private final RequestContext requestContext;

	private final SampleDonorService sampleDonorService;

	final ObjectMapper objMapper = new ObjectMapper();

	public SampleDonorController(RequestContext requestContext, SampleDonorService sampleDonorService) {
		super();
		this.requestContext = requestContext;
		this.sampleDonorService = sampleDonorService;
	}

	/**
	 * This Method is used to get the over all sampledonor with respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}	
	 * @return a response entity which holds the list of sampledonor with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSampleDonor")
	public ResponseEntity<Object> getSampleDonor(@RequestBody Map<String, Object> inputMap) throws Exception {

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getSampleDonor called");
		requestContext.setUserInfo(userInfo);
		return sampleDonorService.getSampleDonor(userInfo);

	}

	/**
	 * This method is used to add a new entry to sampledonor table.
	 * 
	 * @param inputMap map object holding params ( 
	 * 								sampledonor [SampleDonor]  object holding details to be added in sampledonor table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "sampledonor": { "nprojcttype": 1, "ssampledonor": "Mother", "ncode": 1 },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,
									    	"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 191,"nmastersitecode": -1,
									    	"nmodulecode": 76,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
									    	"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
									    	"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00",
									    	"slanguagefilename": "Msg_en_US","slanguagename": "English",
									    	"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
									    	"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
									    	"ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the sampledonor already exists/ 
	 * 			list of sampledonor's along with the newly added sampledonor.
	 * @throws Exception
	 */
	@PostMapping(value = "/createSampleDonor")
	public ResponseEntity<Object> createSampleDonor(@RequestBody Map<String, Object> inputMap) throws Exception {

		final SampleDonor objSampleDonor = objMapper.convertValue(inputMap.get("sampledonor"),
				new TypeReference<SampleDonor>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleDonorService.createSampleDonor(objSampleDonor, userInfo);

	}

	/**
	 * This method is used to update selected sampledonor details.
	 * 
	 * @param inputMap map object holding params(
	 * 					sampledonor [SampleDonor]  object holding details to be updated in sampledonor table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"sampledonor": {"nsampledonorcode":1,"ssampledonor": "Father","nprojecttypecode": 2, "ncode": 3},
    						"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,
									    	"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 191,"nmastersitecode": -1,
									    	"nmodulecode": 76,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
									    	"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
									    	"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00",
									    	"slanguagefilename": "Msg_en_US","slanguagename": "English",
									    	"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
									    	"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
									    	"ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
						}
	 * @return ResponseEntity with string message as 'Already Deleted' if the sampledonor record is not available/ 
	 * 			list of all sampledonor's and along with the updated sampledonor.	
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSampleDonor")
	public ResponseEntity<Object> updateSampleDonor(@RequestBody Map<String, Object> inputMap) throws Exception {

		objMapper.registerModule(new JavaTimeModule());
		final SampleDonor objSampleDonor = objMapper.convertValue(inputMap.get("sampledonor"),
				new TypeReference<SampleDonor>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleDonorService.updateSampleDonor(objSampleDonor, userInfo);

	}

	/**
	 * This method is used to delete an entry in sampledonor table
	 * 
	 * @param inputMap [Map] object with keys of SampleDonor entity and UserInfo object.
	 * 					Input:{
     						"sampledonor": {"nsampledonorcode":1},
    						"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,
									    	"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 191,"nmastersitecode": -1,
									    	"nmodulecode": 76,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
									    	"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
									    	"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00",
									    	"slanguagefilename": "Msg_en_US","slanguagename": "English",
									    	"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
									    	"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
									    	"ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
						}
	 * @return ResponseEntity with string message as 'Already deleted' if the sampledonor record is not available/ 
	 * 			string message as 'Record is used in....' when the sampledonor is associated in transaction /
	 * 			list of all sampledonor's excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSampleDonor")

	public ResponseEntity<Object> deleteSampleDonor(@RequestBody Map<String, Object> inputMap) throws Exception {
		objMapper.registerModule(new JavaTimeModule());
		final SampleDonor objSampleDonor = objMapper.convertValue(inputMap.get("sampledonor"),
				new TypeReference<SampleDonor>() {
				});

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleDonorService.deleteSampleDonor(objSampleDonor, userInfo);

	}

	/**
	 * This method is used to retrieve a specific sampledonor record.
	 * @param inputMap [Map] map object with "nsampledonorcode" and "userinfo" as keys for which the data is to be fetched
	 * 					Input:{
     						"nsampledonorcode": 1,
    						"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,
									    	"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 191,"nmastersitecode": -1,
									    	"nmodulecode": 76,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
									    	"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
									    	"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00",
									    	"slanguagefilename": "Msg_en_US","slanguagename": "English",
									    	"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
									    	"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
									    	"ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
						}
	 * @return ResponseEntity with sampleDonor object for the specified primary key / with string message as
	 * 						'Deleted' if the sampledonor record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveSampleDonorById")
	public ResponseEntity<Object> getActiveSampleDonorById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final int nsampledonorcode = (int) inputMap.get("nsampledonorcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleDonorService.getActiveSampleDonorById(nsampledonorcode, userInfo);

	}

}
