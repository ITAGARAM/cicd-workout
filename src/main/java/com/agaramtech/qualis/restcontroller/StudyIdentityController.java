package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.barcode.model.StudyIdentity;
import com.agaramtech.qualis.barcode.service.studyidentity.StudyIdentityService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Studyidentity Service methods
 */
@RestController
@RequestMapping("/studyidentity")
public class StudyIdentityController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StudyIdentityController.class);
	
	private final StudyIdentityService studyidentityService;
	private RequestContext requestContext;

	public StudyIdentityController(RequestContext requestContext, StudyIdentityService studyidentityService) {
		super();
		this.requestContext = requestContext;
		this.studyidentityService = studyidentityService;
	}

	/**
	 * This Method is used to get the over all StudyIdentity with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Studyidentity with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getStudyIdentity")
	public ResponseEntity<Object> getStudyIdentity(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getStudyIdentity called");
		return studyidentityService.getStudyIdentity(userInfo);
	}

	/**
	 * This method is used to retrieve a specific studyIdentity record.
	 * @param inputMap  [Map] map object with "nstudyidentitycode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nstudyidentitycode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 192,"nmastersitecode": -1,"nmodulecode": 76,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with StudyIdentity object for the specified primary key / with string message as
	 * 						'Deleted' if the StudyIdentity record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveStudyIdentityById")
	public ResponseEntity<Object> getActiveStudyIdentityById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nstudyidentitycode = (Integer) inputMap.get("nstudyidentitycode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return studyidentityService.getActiveStudyIdentityById(nstudyidentitycode, userInfo);
	}

	/**
	 * This method will is used to make a new entry to studyidentity  table.
	 * @param inputMap map object holding params ( 
	 * 								studyIdentity [StudyIdentity]  object holding details to be added in studyIdentity table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "studyidentity": { "nprojecttypecode": "PTB", "sidentificationname": "ParticipantID", "scode": P },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 192,"nmastersitecode": -1,"nmodulecode": 76,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the studyIdentity already exists/ 
	 * 			list of studyIdentity along with the newly added studyIdentity.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createStudyIdentity")
	public ResponseEntity<Object> createStudyIdentity(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final StudyIdentity studyidentity = objMapper.convertValue(inputMap.get("studyidentity"), StudyIdentity.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return studyidentityService.createStudyIdentity(studyidentity, userInfo);
	}

	/**
	 * This method is used to update selected studyIdentity details.
	 * @param inputMap [map object holding params(
	 * 					studyIdentity [StudyIdentity]  object holding details to be updated in studyidentity table,
	 * 					userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"studyidentity": {"nprojecttypecode":"PTB","sidentificationname": "ParticipantID","scode": "P" },
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 192,"nmastersitecode": -1, "nmodulecode": 76,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}

	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the studyIdentity record is not available/ 
	 * 			list of all studyIdentity and along with the updated studyIdentity.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateStudyIdentity")
	public ResponseEntity<Object> updateStudyIdentity(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final StudyIdentity studyidentity = objMapper.convertValue(inputMap.get("studyidentity"), StudyIdentity.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return studyidentityService.updateStudyIdentity(studyidentity, userInfo);
	}

	/**
	 * This method is used to delete an entry in studyidentity table
	 * @param inputMap [Map] object with keys of StudyIdentity entity and UserInfo object.
	 * 					Input:{
     						"studyidentity": {"nstudyidentitycode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 192,"nmastersitecode": -1, "nmodulecode": 76,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the studyIdentity record is not available/ 
	 * 			string message as 'Record is used in....' when the studyIdentity is associated in transaction /
	 * 			list of all studyIdentitys excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteStudyIdentity")
	public ResponseEntity<Object> deleteStudyIdentity(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final StudyIdentity studyidentity = objMapper.convertValue(inputMap.get("studyidentity"), StudyIdentity.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return studyidentityService.deleteStudyIdentity(studyidentity, userInfo);
	}

}
