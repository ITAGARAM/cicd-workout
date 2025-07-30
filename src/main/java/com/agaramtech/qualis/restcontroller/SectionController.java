package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.organization.service.section.SectionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Section Service methods.
 */
@RestController
@RequestMapping("/section")
public class SectionController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SectionController.class);

	private RequestContext requestContext;
	private final SectionService sectionService;
	

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param sectionService SectionService
	 */
	public SectionController(RequestContext requestContext, SectionService sectionService) {
		super();
		this.requestContext = requestContext;
		this.sectionService = sectionService;
	}

	/**
	 * This method is used to retrieve list of available section(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all section(s)
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getSection")
	public ResponseEntity<Object> getSection(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);	
		return sectionService.getSection(userInfo);

	}

	/**
	 * This method will is used to make a new entry to section  table.
	 * @param inputMap map object holding params ( 
	 * 								section [Section]  object holding details to be added in section table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "section": { "ssectionname": "Micro", "sdescription": "Micro Section" },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English","slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the section already exists/ 
	 * 			list of section(s) along with the newly added section.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSection")
	public ResponseEntity<Object> createSection(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final Section section = objmapper.convertValue(inputMap.get("section"), new TypeReference<Section>() {});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("Section Create");
		return sectionService.createSection(section, userInfo);
	}
	
	
	/**
	 * This method is used to update selected section details.
	 * @param inputMap [map object holding params(
	 * 					section [Section]  object holding details to be updated in section table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"section": {"nsectioncode":1,"ssectionname": "m","sdescription": "m", "ndefaultstatus": 3},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}

	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the section record is not available/ 
	 * 			list of all section(s) and along with the updated section.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateSection")
	public ResponseEntity<Object> updateSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Section section = objmapper.convertValue(inputMap.get("section"), new TypeReference<Section>() {});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);

		return sectionService.updateSection(section, userInfo);
	}


	/**
	 * This method is used to delete an entry in Section table
	 * @param inputMap [Map] object with keys of Section entity and UserInfo object.
	 * 					Input:{
     						"section": {"nsectioncode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the section record is not available/ 
	 * 			string message as 'Record is used in....' when the section is associated in transaction /
	 * 			list of all section(s) excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSection")
	public ResponseEntity<Object> deleteSection(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final Section section = objmapper.convertValue(inputMap.get("section"), new TypeReference<Section>() {});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);

		return sectionService.deleteSection(section, userInfo);
	}

	/**
	 * This method is used to retrieve a specific section record.
	 * @param inputMap  [Map] map object with "nsectioncode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nsectioncode": 1,
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
	 * @return ResponseEntity with Section object for the specified primary key / with string message as
	 * 						'Deleted' if the section record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveSectionById")
	public ResponseEntity<Object> getActiveSectionById(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		
		final ObjectMapper objmapper = new ObjectMapper();
		final int nsectionCode = (Integer) inputMap.get("nsectioncode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);

		return sectionService.getActiveSectionById(nsectionCode, userInfo);
	}
	
}
