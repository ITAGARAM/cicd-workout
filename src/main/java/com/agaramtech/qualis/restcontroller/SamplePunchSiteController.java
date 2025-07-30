
package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.barcode.model.SamplePunchSite;
import com.agaramtech.qualis.barcode.service.samplepunchsite.SamplePunchSiteService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the SamplePunchSite Service methods
 */

@RestController
@RequestMapping("/samplepunchsite")
public class SamplePunchSiteController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SamplePunchSiteController.class);
	
	private final SamplePunchSiteService samplepunchsiteservice;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param SamplePunchSiteService samplepunchsiteservice
	 */
	public SamplePunchSiteController(SamplePunchSiteService samplepunchsiteservice, RequestContext requestContext) {
		super();
		this.samplepunchsiteservice = samplepunchsiteservice;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of available samplepunchsite(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in samplepunchsite details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all samplepunchsites
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSamplePunchSite")
	public ResponseEntity<Object> getSamplePunchSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getSamplePunchSite called");
		return samplepunchsiteservice.getSamplePunchSite(userInfo);
	}

	/**
	 * This method is used to retrieve list of available projecttype(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in projecttype details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all projecttypes
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getProjecttype")
	public ResponseEntity<Object> getProjecttype(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplepunchsiteservice.getProjecttype(userInfo);
	}

	/**
	 * This method is used to retrieve list of available sampletype(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in sampletype details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all sampletypes
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSampleType")
	public ResponseEntity<Object> getSampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nprojecttypecode = inputMap.containsKey("nprojecttypecode") ? (int) inputMap.get("nprojecttypecode")
				: -1;
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplepunchsiteservice.getSampleType(nprojecttypecode, userInfo);

	}

	/**
	 * This method will is used to make a new entry to samplepunchsite table.
	 * @param inputMap map object holding params ( 
	 * 								samplepunchsite [SamplePunchSite]  object holding details to be added in samplepunchsite table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "samplepunchsite": {"nsitecode":-1,"sprojecttypename":"test","nprojecttypecode":1,"nsamplepunchsitecode":null,"sproductname":"Excepient","nproductcode":1,"spunchdescription":"PunchDesc","ncode":1},
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 194,"nmastersitecode": -1,"nmodulecode": 76,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English", "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the samplepunchsite already exists 
	 * 			list of samplepunchsites along with the newly added samplepunchsite.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSamplePunchSite")
	public ResponseEntity<Object> createSamplePunchSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SamplePunchSite samplePunchSite = objMapper.convertValue(inputMap.get("samplepunchsite"),
				SamplePunchSite.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplepunchsiteservice.createSamplePunchSite(samplePunchSite, userInfo);
	}

	/**
	 * This method is used to retrieve a specific samplepunchsite record.
	 * @param inputMap  [Map] map object with "nsamplepunchsitecode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{"nsamplepunchsitecode":10,"userinfo":{"nusercode":-1,"nuserrole":-1,"ndeputyusercode":-1,"ndeputyuserrole":-1,
	 *                  "nmodulecode":76,"nformcode":194,"nreasoncode":0,"slanguagetypecode":"en-US","sreportlanguagecode":"en-US","nsitecode":1,
	 *                  "ntranssitecode":1,"nmastersitecode":-1,"sreason":"","susername":"QuaLIS Admin","suserrolename":"QuaLIS Admin",
	 *                  "slanguagefilename":"Msg_en_US","nusersitecode":-1,"sloginid":"system","sdeptname":"NA","ndeptcode":-1,
	 *                  "sdatetimeformat":"dd/MM/yyyy HH:mm:ss","ntimezonecode":-1,"stimezoneid":"Europe/London","ssitedatetime":"dd/MM/yyyy HH:mm:ss",
	 *                  "ssitereportdatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdate":"dd/MM/yyyy","ssitedate":"dd/MM/yyyy","nlogintypecode":1,
	 *                  "sfirstname":"QuaLIS","slastname":"Admin","ssessionid":"B64F9E69DF839160D8AFCBBD78A98745","shostip":"0:0:0:0:0:0:0:1",
	 *                  "spassword":null,"sgmtoffset":"UTC +00:00","sdeputyid":"system","ssitename":"LIMS","sformname":"Sample Punch Site",
	 *                  "smodulename":"Barcode Management","istimezoneshow":4,"sreportingtoolfilename":"en.xml","spredefinedreason":null,
	 *                  "isutcenabled":4,"nsiteadditionalinfo":4,"nissyncserver":3,"nisstandaloneserver":4,"slanguagename":"English",
	 *                  "ssitecode":"SYNC","sdeputyusername":"QuaLIS Admin","sdeputyuserrolename":"QuaLIS Admin",
	 *                  "activelanguagelist":["en-US","ru-RU","tg-TG"],"spgdatetimeformat":"dd/MM/yyyy HH24:mi:ss",
	 *                  "spgsitedatetime":"dd/MM/yyyy HH24:mi:ss","spgsitereportdatetime":"dd/MM/yyyy HH24:mi:ss",
	 *                  "sconnectionString":"Server=localhost;Port=5432;Database=LUCID_ClientDB_15052025;User=postgres;Pwd=admin@123;"}}
	 * @return ResponseEntity with SamplePunchSite object for the specified primary key / with string message as
	 * 						'Deleted' if the samplepunchsite record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveSamplePunchSiteById")
	public ResponseEntity<Object> getActiveSamplePunchSiteById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nsamplepunchsitecode = (Integer) inputMap.get("nsamplepunchsitecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return samplepunchsiteservice.getActiveSamplePunchSiteById(nsamplepunchsitecode, userInfo);
	}

	/**
	 * This method is used to update selected samplepunchsite details.
	 * @param inputMap [map object holding params(
	 * 					samplepunchsite [SamplePunchSite]  object holding details to be updated in samplepunchsite table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{"userinfo":{"nusercode":-1,"nuserrole":-1,"ndeputyusercode":-1,"ndeputyuserrole":-1,"nmodulecode":76,"nformcode":194,
	 * "nreasoncode":0,"slanguagetypecode":"en-US","sreportlanguagecode":"en-US","nsitecode":1,"ntranssitecode":1,"nmastersitecode":-1,
	 * "sreason":"","susername":"QuaLIS Admin","suserrolename":"QuaLIS Admin","slanguagefilename":"Msg_en_US","nusersitecode":-1,
	 * "sloginid":"system","sdeptname":"NA","ndeptcode":-1,"sdatetimeformat":"dd/MM/yyyy HH:mm:ss","ntimezonecode":-1,
	 * "stimezoneid":"Europe/London","ssitedatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdatetime":"dd/MM/yyyy HH:mm:ss",
	 * "ssitereportdate":"dd/MM/yyyy","ssitedate":"dd/MM/yyyy","nlogintypecode":1,"sfirstname":"QuaLIS","slastname":"Admin",
	 * "ssessionid":"B64F9E69DF839160D8AFCBBD78A98745","shostip":"0:0:0:0:0:0:0:1","spassword":null,"sgmtoffset":"UTC +00:00",
	 * "sdeputyid":"system","ssitename":"LIMS","sformname":"Sample Punch Site","smodulename":"Barcode Management","istimezoneshow":4,
	 * "sreportingtoolfilename":"en.xml","spredefinedreason":null,"isutcenabled":4,"nsiteadditionalinfo":4,"nissyncserver":3,
	 * "nisstandaloneserver":4,"slanguagename":"English","ssitecode":"SYNC","sdeputyusername":"QuaLIS Admin",
	 * "sdeputyuserrolename":"QuaLIS Admin","activelanguagelist":["en-US","ru-RU","tg-TG"],"spgdatetimeformat":"dd/MM/yyyy HH24:mi:ss",
	 * "spgsitedatetime":"dd/MM/yyyy HH24:mi:ss","spgsitereportdatetime":"dd/MM/yyyy HH24:mi:ss",
	 * "sconnectionString":"Server=localhost;Port=5432;Database=LUCID_ClientDB_15052025;User=postgres;Pwd=admin@123;"},
	 * "samplepunchsite":{"nsitecode":-1,"sprojecttypename":"test","nprojecttypecode":1,"nsamplepunchsitecode":10,
	 * "sproductname":"Excepient","nproductcode":1,"spunchdescription":"PunchDesc1","ncode":1}}
	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the samplepunchsite record is not available/ 
	 * 			list of all samplepunchsites and along with the updated samplepunchsite.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateSamplePunchSite")
	public ResponseEntity<Object> updateSamplePunchSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SamplePunchSite samplePunchSite = objMapper.convertValue(inputMap.get("samplepunchsite"),
				SamplePunchSite.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplepunchsiteservice.updateSamplePunchSite(samplePunchSite, userInfo);
	}

	/**
	 * This method is used to delete an entry in samplepunchsite table
	 * @param inputMap [Map] object with keys of samplepunchsite entity and UserInfo object.
	 * 					Input:{"samplepunchsite":{"nsamplepunchsitecode":10,"nsamplecollectiontypecode":0,
	 * "spunchdescription":"PunchDesc1","ncode":1,"nsitecode":-1,"nstatus":1,"dmodifieddate":null,"nprojecttypecode":1,
	 * "sprojecttypename":"test","smodifieddate":null,"sproductname":"Excepient","ispunchdescription":false,"iscode":false,
	 * "nproductcode":1},"userinfo":{"nusercode":-1,"nuserrole":-1,"ndeputyusercode":-1,"ndeputyuserrole":-1,"nmodulecode":76,
	 * "nformcode":194,"nreasoncode":0,"slanguagetypecode":"en-US","sreportlanguagecode":"en-US","nsitecode":1,
	 * "ntranssitecode":1,"nmastersitecode":-1,"sreason":"","susername":"QuaLIS Admin","suserrolename":"QuaLIS Admin",
	 * "slanguagefilename":"Msg_en_US","nusersitecode":-1,"sloginid":"system","sdeptname":"NA","ndeptcode":-1,
	 * "sdatetimeformat":"dd/MM/yyyy HH:mm:ss","ntimezonecode":-1,"stimezoneid":"Europe/London",
	 * "ssitedatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdate":"dd/MM/yyyy",
	 * "ssitedate":"dd/MM/yyyy","nlogintypecode":1,"sfirstname":"QuaLIS","slastname":"Admin",
	 * "ssessionid":"B64F9E69DF839160D8AFCBBD78A98745","shostip":"0:0:0:0:0:0:0:1","spassword":null,"sgmtoffset":"UTC +00:00",
	 * "sdeputyid":"system","ssitename":"LIMS","sformname":"Sample Punch Site","smodulename":"Barcode Management",
	 * "istimezoneshow":4,"sreportingtoolfilename":"en.xml","spredefinedreason":null,"isutcenabled":4,"nsiteadditionalinfo":4,
	 * "nissyncserver":3,"nisstandaloneserver":4,"slanguagename":"English","ssitecode":"SYNC","sdeputyusername":"QuaLIS Admin",
	 * "sdeputyuserrolename":"QuaLIS Admin","activelanguagelist":["en-US","ru-RU","tg-TG"],
	 * "spgdatetimeformat":"dd/MM/yyyy HH24:mi:ss","spgsitedatetime":"dd/MM/yyyy HH24:mi:ss",
	 * "spgsitereportdatetime":"dd/MM/yyyy HH24:mi:ss","sconnectionString":"Server=localhost;
	 * Port=5432;Database=LUCID_ClientDB_15052025;User=postgres;Pwd=admin@123;"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the samplepunchsite record is not available/ 
	 * 			string message as 'Record is used in....' when the samplepunchsite is associated in transaction /
	 * 			list of all samplepunchsites excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSamplePunchSite")
	public ResponseEntity<Object> deleteSamplePunchSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SamplePunchSite samplePunchSite = objMapper.convertValue(inputMap.get("samplepunchsite"),
				SamplePunchSite.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return samplepunchsiteservice.deleteSamplePunchSite(samplePunchSite, userInfo);
	}
}
