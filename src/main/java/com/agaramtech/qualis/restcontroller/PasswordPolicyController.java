package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.model.PasswordPolicy;
import com.agaramtech.qualis.configuration.service.passwordpolicy.PasswordPolicyService;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the passwordpolicy Service methods.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 06- Jul- 2020
 */
@RestController
@RequestMapping("/passwordpolicy")
public class PasswordPolicyController {

//	private static final Log LOGGER = LogFactory.getLog(SiteController.class);

	private RequestContext requestContext;
	private PasswordPolicyService passwordPolicyService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext        RequestContext to hold the request
	 * @param passwordPolicyService PasswordPolicyService
	 */
	public PasswordPolicyController(PasswordPolicyService passwordPolicyService, RequestContext requestContext) {
		super();
		this.passwordPolicyService = passwordPolicyService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of available passwordpolicy(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * Input : {"userinfo":{"nusercode":-1,"nuserrole":-1,"ndeputyusercode":-1,"ndeputyuserrole":-1,
	 * 		"nmodulecode":3,"nformcode":51,"nreasoncode":0,"slanguagetypecode":"en-US","sreportlanguagecode":"en-US","nsitecode":1,
	 * 		"ntranssitecode":1,"nmastersitecode":-1,"sreason":"","susername":"QuaLIS Admin","suserrolename":"QuaLIS Admin",
	 * 		"slanguagefilename":"Msg_en_US","nusersitecode":-1,"sloginid":"system","sdeptname":"NA","ndeptcode":-1,
	 * 		"sdatetimeformat":"dd/MM/yyyy HH:mm:ss","ntimezonecode":-1,"stimezoneid":"Europe/London",
	 * 		"ssitedatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdate":"dd/MM/yyyy",
	 * 		"ssitedate":"dd/MM/yyyy","nlogintypecode":1,"sfirstname":"QuaLIS","slastname":"Admin",
	 * 		"ssessionid":"0D1F3F5882DFBA78BADA2DAEDF92A6D2","shostip":"0:0:0:0:0:0:0:1","spassword":null,"sgmtoffset":"UTC +00:00",
	 * 		"sdeputyid":"system","ssitename":"LIMS","sformname":"Password Policy","smodulename":"User Management",
	 * 		"istimezoneshow":4,"sreportingtoolfilename":"en.xml","spredefinedreason":null,"isutcenabled":4,"nsiteadditionalinfo":4,
	 * 		"nissyncserver":3,"nisstandaloneserver":4,"slanguagename":"English","ssitecode":"SYNC","sdeputyusername":"QuaLIS Admin",
	 * 		"sdeputyuserrolename":"QuaLIS Admin","activelanguagelist":["en-US","ru-RU","tg-TG"],
	 * 		"spgdatetimeformat":"dd/MM/yyyy HH24:mi:ss","spgsitedatetime":"dd/MM/yyyy HH24:mi:ss",
	 * 		"spgsitereportdatetime":"dd/MM/yyyy HH24:mi:ss","sconnectionString":"Server=localhost;Port=5432;
	 * 		Database=LUCID_ClientDB_15052025;User=postgres;Pwd=admin@123;"},"settings":{"1":"15","2":"3","3":"10","4":"10,20,30",
	 * 		"5":"/SharedFolder/UserProfile/","6":"/SharedFolder/FileUpload/","7":"4","8":"10",
	 * 		"9":"C:\\\\Program Files\\\\Apache Software Foundation\\\\Tomcat 9.0_Tomcat9_9096_LUCID\\\\webapps\\\\ROOT\\\\SharedFolder\\\\Barcode\\\\",
	 * 		"10":"C:\\\\Program Files\\\\Apache Software Foundation\\\\Tomcat 9.0_Tomcat9_9096_LUCID\\\\webapps\\\\ROOT\\\\SharedFolder\\\\AuditFiles",
	 * 		"11":"\\SharedFolder\\AuditFiles","12":"5","13":"5,10,20","14":"10","15":"10,20,30","16":"50","17":"50,100,150","19":"4",
	 * 		"20":"4","21":"4","22":"4","23":"4","24":"http://localhost:9090/portal-0.0.1-SNAPSHOT","25":"3","26":"3",
	 * 		"27":"/SharedFolder/UserSign/","28":"/SharedFolder/UserProfile/","29":"4","30":"SYNC","31":"4","32":".png,.jpeg,.jpg",
	 * 		"33":"3","34":"3","35":"2023-10-26 05:50:05","36":"3","37":"14","38":"140px","39":"26","40":"3","41":"3",
	 * 		"42":"https://tpttajik.imonitorplus.com/service/api/lims/","43":"3","44":"http://localhost:9021/QuaLIS","45":"4",
	 * 		"46":"4","47":"4","48":"5","49":"4","50":"3","51":"2","52":"4","53":"3","54":"","55":"5",
	 * 		"56":"http://localhost:9096/SharedFolder/FileUpload/","57":"30","58":"4","59":"90","60":"6",
	 * 		"61":"\\SharedFolder\\NFColdReports\\","62":"\\webapps\\ROOT","63":"4","64":"4","65":"4",
	 * 		"66":"http://localhost:9096/SharedFolder/ReportView/","67":"TOMCAT_HOME_LUCID","68":"2","69":"3","70":"4","71":"4",
	 * 		"72":"5","73":"4","74":"2","75":"1","78":"4","79":"3","80":"4"},"currentdate":"2025-06-11T15:47:10.457Z"}			
	 * @return response entity  object holding response status and list of all passwordpolicies
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getPasswordPolicy")
	public ResponseEntity<Object> getPasswordPolicy(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		Integer npolicyCode = null;
		if (inputMap.get("npolicycode") != null) {
			npolicyCode = (Integer) inputMap.get("npolicycode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return passwordPolicyService.getPasswordPolicy(npolicyCode, userInfo);// siteCode);
	}

	/**
	 * This method is used to retrieve list of active passwordpolicy for the
	 * specified site.
	 * 
	 * @param inputMap [Map] map object with "nuserrolecode" as key for which the
	 *                 list is to be fetched
	 * Input : {"nuserrolecode":3,"userinfo":{"nusercode":-1,"nuserrole":-1,"ndeputyusercode":-1,"ndeputyuserrole":-1,
	 * 		"nmodulecode":3,"nformcode":51,"nreasoncode":0,"slanguagetypecode":"en-US","sreportlanguagecode":"en-US","nsitecode":1,
	 * 		"ntranssitecode":1,"nmastersitecode":-1,"sreason":"","susername":"QuaLIS Admin","suserrolename":"QuaLIS Admin",
	 * 		"slanguagefilename":"Msg_en_US","nusersitecode":-1,"sloginid":"system","sdeptname":"NA","ndeptcode":-1,
	 * 		"sdatetimeformat":"dd/MM/yyyy HH:mm:ss","ntimezonecode":-1,"stimezoneid":"Europe/London",
	 * 		"ssitedatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdate":"dd/MM/yyyy",
	 * 		"ssitedate":"dd/MM/yyyy","nlogintypecode":1,"sfirstname":"QuaLIS","slastname":"Admin",
	 * 		"ssessionid":"0D1F3F5882DFBA78BADA2DAEDF92A6D2","shostip":"0:0:0:0:0:0:0:1","spassword":null,"sgmtoffset":"UTC +00:00",
	 * 		"sdeputyid":"system","ssitename":"LIMS","sformname":"Password Policy","smodulename":"User Management","istimezoneshow":4,
	 * 		"sreportingtoolfilename":"en.xml","spredefinedreason":null,"isutcenabled":4,"nsiteadditionalinfo":4,"nissyncserver":3,
	 * 		"nisstandaloneserver":4,"slanguagename":"English","ssitecode":"SYNC","sdeputyusername":"QuaLIS Admin",
	 * 		"sdeputyuserrolename":"QuaLIS Admin","activelanguagelist":["en-US","ru-RU","tg-TG"],
	 * 		"spgdatetimeformat":"dd/MM/yyyy HH24:mi:ss","spgsitedatetime":"dd/MM/yyyy HH24:mi:ss",
	 * 		"spgsitereportdatetime":"dd/MM/yyyy HH24:mi:ss","sconnectionString":"Server=localhost;Port=5432;
	 * 		Database=LUCID_ClientDB_15052025;User=postgres;Pwd=admin@123;"}}
	 * @return response object with list of active passwordpolicy that are to be
	 *         listed for the specified site
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getPasswordPolicyByUserRoleCode")
	public ResponseEntity<Object> getPasswordPolicyByUserRoleCode(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolepolicycode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolepolicycode = (Integer) inputMap.get("nuserrolecode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return passwordPolicyService.getPasswordPolicyByUserRoleCode(nuserrolepolicycode, userInfo);// siteCode);
	}

	/**
	 * This method is used to add new passwordpolicy for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of passwordpolicy entity.
	 * Input : {"userinfo":{"nusercode":-1,"nuserrole":-1,"ndeputyusercode":-1,"ndeputyuserrole":-1,"nmodulecode":3,
	 * 		"nformcode":51,"nreasoncode":0,"slanguagetypecode":"en-US","sreportlanguagecode":"en-US","nsitecode":1,
	 * 		"ntranssitecode":1,"nmastersitecode":-1,"sreason":"","susername":"QuaLIS Admin","suserrolename":"QuaLIS Admin",
	 * 		"slanguagefilename":"Msg_en_US","nusersitecode":-1,"sloginid":"system","sdeptname":"NA","ndeptcode":-1,
	 * 		"sdatetimeformat":"dd/MM/yyyy HH:mm:ss","ntimezonecode":-1,"stimezoneid":"Europe/London",
	 * 		"ssitedatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdate":"dd/MM/yyyy",
	 * 		"ssitedate":"dd/MM/yyyy","nlogintypecode":1,"sfirstname":"QuaLIS","slastname":"Admin",
	 * 		"ssessionid":"0D1F3F5882DFBA78BADA2DAEDF92A6D2","shostip":"0:0:0:0:0:0:0:1","spassword":null,"sgmtoffset":"UTC +00:00",
	 * 		"sdeputyid":"system","ssitename":"LIMS","sformname":"Password Policy","smodulename":"User Management","istimezoneshow":4,
	 * 		"sreportingtoolfilename":"en.xml","spredefinedreason":null,"isutcenabled":4,"nsiteadditionalinfo":4,"nissyncserver":3,
	 * 		"nisstandaloneserver":4,"slanguagename":"English","ssitecode":"SYNC","sdeputyusername":"QuaLIS Admin",
	 * 		"sdeputyuserrolename":"QuaLIS Admin","activelanguagelist":["en-US","ru-RU","tg-TG"],
	 * 		"spgdatetimeformat":"dd/MM/yyyy HH24:mi:ss","spgsitedatetime":"dd/MM/yyyy HH24:mi:ss",
	 * 		"spgsitereportdatetime":"dd/MM/yyyy HH24:mi:ss","sconnectionString":"Server=localhost;Port=5432;
	 * 		Database=LUCID_ClientDB_15052025;User=postgres;Pwd=admin@123;"},"passwordpolicy":{"spolicyname":"Policy",
	 * 		"nminnoofnumberchar":1,"nminnooflowerchar":0,"nminnoofupperchar":0,"nminnoofspecialchar":0,"nminpasslength":1,
	 * 		"nmaxpasslength":10,"nnooffailedattempt":99,"nexpirypolicy":0,"nremainderdays":0},"nuserrolecode":9}
	 * @return response entity of newly added passwordpolicy entity
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createPasswordPolicy")
	public ResponseEntity<Object> createPasswordPolicy(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		Integer nuserrolecode = null;
		final PasswordPolicy passwordPolicy = objmapper.convertValue(inputMap.get("passwordpolicy"),
				PasswordPolicy.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		return passwordPolicyService.createPasswordPolicy(passwordPolicy, userInfo, nuserrolecode);
	}

	/**
	 * This method is used to retrieve selected active passwordpolicy detail.
	 * 
	 * @param inputMap [Map] map object with "npolicycode" and "userInfo" as keys
	 *                 for which the data is to be fetched
	 * Input : {"npolicycode":7,"userinfo":{"nusercode":-1,"nuserrole":-1,"ndeputyusercode":-1,"ndeputyuserrole":-1,
	 * 		"nmodulecode":3,"nformcode":51,"nreasoncode":0,"slanguagetypecode":"en-US","sreportlanguagecode":"en-US","nsitecode":1,
	 * 		"ntranssitecode":1,"nmastersitecode":-1,"sreason":"","susername":"QuaLIS Admin","suserrolename":"QuaLIS Admin",
	 * 		"slanguagefilename":"Msg_en_US","nusersitecode":-1,"sloginid":"system","sdeptname":"NA","ndeptcode":-1,
	 * 		"sdatetimeformat":"dd/MM/yyyy HH:mm:ss","ntimezonecode":-1,"stimezoneid":"Europe/London",
	 * 		"ssitedatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdate":"dd/MM/yyyy",
	 * 		"ssitedate":"dd/MM/yyyy","nlogintypecode":1,"sfirstname":"QuaLIS","slastname":"Admin",
	 * 		"ssessionid":"0D1F3F5882DFBA78BADA2DAEDF92A6D2","shostip":"0:0:0:0:0:0:0:1","spassword":null,"sgmtoffset":"UTC +00:00",
	 * 		"sdeputyid":"system","ssitename":"LIMS","sformname":"Password Policy","smodulename":"User Management",
	 * 		"istimezoneshow":4,"sreportingtoolfilename":"en.xml","spredefinedreason":null,"isutcenabled":4,"nsiteadditionalinfo":4,
	 * 		"nissyncserver":3,"nisstandaloneserver":4,"slanguagename":"English","ssitecode":"SYNC","sdeputyusername":"QuaLIS Admin",
	 * 		"sdeputyuserrolename":"QuaLIS Admin","activelanguagelist":["en-US","ru-RU","tg-TG"],
	 * 		"spgdatetimeformat":"dd/MM/yyyy HH24:mi:ss","spgsitedatetime":"dd/MM/yyyy HH24:mi:ss",
	 * 		"spgsitereportdatetime":"dd/MM/yyyy HH24:mi:ss","sconnectionString":"Server=localhost;Port=5432;
	 * 		Database=LUCID_ClientDB_15052025;User=postgres;Pwd=admin@123;"}}
	 * @return response object with selected active passwordpolicy
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActivePasswordPolicyById")
	public ResponseEntity<Object> getActivePasswordPolicyById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int npolicycode = (Integer) inputMap.get("npolicycode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return passwordPolicyService.getActivePasswordPolicyById(npolicycode, userInfo);

	}

	/**
	 * This method is used to update passwordpolicy for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of passwordpolicy entity.
	 * Input : {"userinfo":{"nusercode":-1,"nuserrole":-1,"ndeputyusercode":-1,"ndeputyuserrole":-1,"nmodulecode":3,
	 * 		"nformcode":51,"nreasoncode":0,"slanguagetypecode":"en-US","sreportlanguagecode":"en-US","nsitecode":1,
	 * 		"ntranssitecode":1,"nmastersitecode":-1,"sreason":"","susername":"QuaLIS Admin","suserrolename":"QuaLIS Admin",
	 * 		"slanguagefilename":"Msg_en_US","nusersitecode":-1,"sloginid":"system","sdeptname":"NA","ndeptcode":-1,
	 * 		"sdatetimeformat":"dd/MM/yyyy HH:mm:ss","ntimezonecode":-1,"stimezoneid":"Europe/London",
	 * 		"ssitedatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdate":"dd/MM/yyyy",
	 * 		"ssitedate":"dd/MM/yyyy","nlogintypecode":1,"sfirstname":"QuaLIS","slastname":"Admin",
	 * 		"ssessionid":"0D1F3F5882DFBA78BADA2DAEDF92A6D2","shostip":"0:0:0:0:0:0:0:1","spassword":null,
	 * 		"sgmtoffset":"UTC +00:00","sdeputyid":"system","ssitename":"LIMS","sformname":"Password Policy",
	 * 		"smodulename":"User Management","istimezoneshow":4,"sreportingtoolfilename":"en.xml","spredefinedreason":null,
	 * 		"isutcenabled":4,"nsiteadditionalinfo":4,"nissyncserver":3,"nisstandaloneserver":4,"slanguagename":"English",
	 * 		"ssitecode":"SYNC","sdeputyusername":"QuaLIS Admin","sdeputyuserrolename":"QuaLIS Admin",
	 * 		"activelanguagelist":["en-US","ru-RU","tg-TG"],"spgdatetimeformat":"dd/MM/yyyy HH24:mi:ss",
	 * 		"spgsitedatetime":"dd/MM/yyyy HH24:mi:ss","spgsitereportdatetime":"dd/MM/yyyy HH24:mi:ss",
	 * 		"sconnectionString":"Server=localhost;Port=5432;Database=LUCID_ClientDB_15052025;User=postgres;Pwd=admin@123;"},
	 * 		"passwordpolicy":{"npolicycode":7,"spolicyname":"Policy","nminnoofnumberchar":1,"nminnooflowerchar":0,
	 * 		"nminnoofupperchar":0,"nminnoofspecialchar":0,"nminpasslength":1,"nmaxpasslength":10,"nnooffailedattempt":99,
	 * 		"nexpirypolicyrequired":4,"nexpirypolicy":0,"nremainderdays":0,"scomments":"","nstatus":1,"dmodifieddate":null,
	 * 		"nsitecode":-1,"nuserrolepolicycode":7,"stransstatus":"Draft","ntransactionstatus":8,"numrnnooffailedattempt":0,
	 * 		"nuserrolecode":9,"sexpirystatus":"No","suserrolename":null}}
	 * @return response entity of updated passwordpolicy entity
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updatePasswordPolicy")
	public ResponseEntity<Object> updatePasswordPolicy(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final PasswordPolicy passwordPolicy = objmapper.convertValue(inputMap.get("passwordpolicy"),
				PasswordPolicy.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return passwordPolicyService.updatePasswordPolicy(passwordPolicy, userInfo);

	}

	/**
	 * This method is used to delete passwordpolicy for the specified Site.
	 * @param inputMap [Map] object with keys of passwordpolicy entity.
	 * Input : {"passwordpolicy":{"npolicycode":7,"spolicyname":"Policy","nminnoofnumberchar":1,"nminnooflowerchar":0,
	 * 		"nminnoofupperchar":0,"nminnoofspecialchar":0,"nminpasslength":1,"nmaxpasslength":10,"nnooffailedattempt":99,
	 * 		"nexpirypolicyrequired":4,"nexpirypolicy":0,"nremainderdays":0,"scomments":"","nstatus":1,"dmodifieddate":null,
	 * 		"nsitecode":-1,"nuserrolepolicycode":7,"stransstatus":"Draft","ntransactionstatus":8,"numrnnooffailedattempt":0,
	 * 		"nuserrolecode":9,"sexpirystatus":"No","suserrolename":null},"userinfo":{"nusercode":-1,"nuserrole":-1,
	 * 		"ndeputyusercode":-1,"ndeputyuserrole":-1,"nmodulecode":3,"nformcode":51,"nreasoncode":0,"slanguagetypecode":"en-US",
	 * 		"sreportlanguagecode":"en-US","nsitecode":1,"ntranssitecode":1,"nmastersitecode":-1,"sreason":"",
	 * 		"susername":"QuaLIS Admin","suserrolename":"QuaLIS Admin","slanguagefilename":"Msg_en_US","nusersitecode":-1,
	 * 		"sloginid":"system","sdeptname":"NA","ndeptcode":-1,"sdatetimeformat":"dd/MM/yyyy HH:mm:ss","ntimezonecode":-1,
	 * 		"stimezoneid":"Europe/London","ssitedatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdatetime":"dd/MM/yyyy HH:mm:ss",
	 * 		"ssitereportdate":"dd/MM/yyyy","ssitedate":"dd/MM/yyyy","nlogintypecode":1,"sfirstname":"QuaLIS","slastname":"Admin",
	 * 		"ssessionid":"0D1F3F5882DFBA78BADA2DAEDF92A6D2","shostip":"0:0:0:0:0:0:0:1","spassword":null,
	 * 		"sgmtoffset":"UTC +00:00","sdeputyid":"system","ssitename":"LIMS","sformname":"Password Policy",
	 * 		"smodulename":"User Management","istimezoneshow":4,"sreportingtoolfilename":"en.xml","spredefinedreason":null,
	 * 		"isutcenabled":4,"nsiteadditionalinfo":4,"nissyncserver":3,"nisstandaloneserver":4,"slanguagename":"English",
	 * 		"ssitecode":"SYNC","sdeputyusername":"QuaLIS Admin","sdeputyuserrolename":"QuaLIS Admin",
	 * 		"activelanguagelist":["en-US","ru-RU","tg-TG"],"spgdatetimeformat":"dd/MM/yyyy HH24:mi:ss",
	 * 		"spgsitedatetime":"dd/MM/yyyy HH24:mi:ss","spgsitereportdatetime":"dd/MM/yyyy HH24:mi:ss",
	 * 		"sconnectionString":"Server=localhost;Port=5432;Database=LUCID_ClientDB_15052025;User=postgres;Pwd=admin@123;"}}
	 * @return response entity of deleted passwordpolicy entity
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deletePasswordPolicy")
	public ResponseEntity<Object> deletePasswordPolicy(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final PasswordPolicy passwordPolicy = objmapper.convertValue(inputMap.get("passwordpolicy"),
				PasswordPolicy.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return passwordPolicyService.deletePasswordPolicy(passwordPolicy, userInfo);
	}

	/**
	 * This method is used to approve userrolepolicy for the specified Site.
	 * @param inputMap [Map] object with keys of userrolepolicy entity.
	 * Input : {"userinfo":{"nusercode":-1,"nuserrole":-1,"ndeputyusercode":-1,"ndeputyuserrole":-1,"nmodulecode":3,
	 * 		"nformcode":51,"nreasoncode":0,"slanguagetypecode":"en-US","sreportlanguagecode":"en-US","nsitecode":1,
	 * 		"ntranssitecode":1,"nmastersitecode":-1,"sreason":"","susername":"QuaLIS Admin","suserrolename":"QuaLIS Admin",
	 * 		"slanguagefilename":"Msg_en_US","nusersitecode":-1,"sloginid":"system","sdeptname":"NA","ndeptcode":-1,
	 * 		"sdatetimeformat":"dd/MM/yyyy HH:mm:ss","ntimezonecode":-1,"stimezoneid":"Europe/London",
	 * 		"ssitedatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdate":"dd/MM/yyyy",
	 * 		"ssitedate":"dd/MM/yyyy","nlogintypecode":1,"sfirstname":"QuaLIS","slastname":"Admin",
	 * 		"ssessionid":"0D1F3F5882DFBA78BADA2DAEDF92A6D2","shostip":"0:0:0:0:0:0:0:1","spassword":null,
	 * 		"sgmtoffset":"UTC +00:00","sdeputyid":"system","ssitename":"LIMS","sformname":"Password Policy",
	 * 		"smodulename":"User Management","istimezoneshow":4,"sreportingtoolfilename":"en.xml","spredefinedreason":null,
	 * 		"isutcenabled":4,"nsiteadditionalinfo":4,"nissyncserver":3,"nisstandaloneserver":4,"slanguagename":"English",
	 * 		"ssitecode":"SYNC","sdeputyusername":"QuaLIS Admin","sdeputyuserrolename":"QuaLIS Admin",
	 * 		"activelanguagelist":["en-US","ru-RU","tg-TG"],"spgdatetimeformat":"dd/MM/yyyy HH24:mi:ss",
	 * 		"spgsitedatetime":"dd/MM/yyyy HH24:mi:ss","spgsitereportdatetime":"dd/MM/yyyy HH24:mi:ss",
	 * 		"sconnectionString":"Server=localhost;Port=5432;Database=LUCID_ClientDB_15052025;User=postgres;Pwd=admin@123;"},
	 * 		"passwordpolicy":{"npolicycode":8,"spolicyname":"v","nminnoofnumberchar":1,"nminnooflowerchar":0,
	 * 		"nminnoofupperchar":0,"nminnoofspecialchar":0,"nminpasslength":1,"nmaxpasslength":10,"nnooffailedattempt":99,
	 * 		"nexpirypolicyrequired":4,"nexpirypolicy":0,"nremainderdays":0,"scomments":null,"nstatus":1,"dmodifieddate":null,
	 * 		"nsitecode":-1,"nuserrolepolicycode":8,"stransstatus":"Draft","ntransactionstatus":8,"numrnnooffailedattempt":0,"nuserrolecode":9,"sexpirystatus":"No","suserrolename":null}}
	 * @return response entity of deleted userrolepolicy entity
	 * @throws Exception exception
	 */
	@PostMapping(value = "/approveUserRolePolicy")
	public ResponseEntity<Object> approveUserRolePolicy(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final PasswordPolicy passwordPolicy = objmapper.convertValue(inputMap.get("passwordpolicy"),
				PasswordPolicy.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return passwordPolicyService.approveUserRolePolicy(passwordPolicy, userInfo);
	}

	/**
	 * This method is used to retrieve list of active UserRolePolicy for the
	 * specified site.
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active UserRolePolicy that are to be
	 *         listed for the specified site
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getUserRolePolicy")
	public ResponseEntity<Object> getUserRolePolicy(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return passwordPolicyService.getUserRolePolicy(userInfo);// siteCode);

	}

	/**
	 * This method is used to copy the active passwordpolicy to the specified
	 * userrole.
	 * 
	 * @param npolicyCode [int] primary key of passwordpolicy object for which the
	 *                    reocord is copied to specified userrole
	 * Input : {"userinfo":{"nusercode":-1,"nuserrole":-1,"ndeputyusercode":-1,"ndeputyuserrole":-1,"nmodulecode":3,
	 * 		"nformcode":51,"nreasoncode":0,"slanguagetypecode":"en-US","sreportlanguagecode":"en-US","nsitecode":1,
	 * 		"ntranssitecode":1,"nmastersitecode":-1,"sreason":"","susername":"QuaLIS Admin","suserrolename":"QuaLIS Admin",
	 * 		"slanguagefilename":"Msg_en_US","nusersitecode":-1,"sloginid":"system","sdeptname":"NA","ndeptcode":-1,
	 * 		"sdatetimeformat":"dd/MM/yyyy HH:mm:ss","ntimezonecode":-1,"stimezoneid":"Europe/London",
	 * 		"ssitedatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdatetime":"dd/MM/yyyy HH:mm:ss","ssitereportdate":"dd/MM/yyyy",
	 * 		"ssitedate":"dd/MM/yyyy","nlogintypecode":1,"sfirstname":"QuaLIS","slastname":"Admin",
	 * 		"ssessionid":"0D1F3F5882DFBA78BADA2DAEDF92A6D2","shostip":"0:0:0:0:0:0:0:1","spassword":null,
	 * 		"sgmtoffset":"UTC +00:00","sdeputyid":"system","ssitename":"LIMS","sformname":"Password Policy",
	 * 		"smodulename":"User Management","istimezoneshow":4,"sreportingtoolfilename":"en.xml","spredefinedreason":null,
	 * 		"isutcenabled":4,"nsiteadditionalinfo":4,"nissyncserver":3,"nisstandaloneserver":4,"slanguagename":"English",
	 * 		"ssitecode":"SYNC","sdeputyusername":"QuaLIS Admin","sdeputyuserrolename":"QuaLIS Admin",
	 * 		"activelanguagelist":["en-US","ru-RU","tg-TG"],"spgdatetimeformat":"dd/MM/yyyy HH24:mi:ss",
	 * 		"spgsitedatetime":"dd/MM/yyyy HH24:mi:ss","spgsitereportdatetime":"dd/MM/yyyy HH24:mi:ss",
	 * 		"sconnectionString":"Server=localhost;Port=5432;Database=LUCID_ClientDB_15052025;User=postgres;Pwd=admin@123;"},
	 * 		"userrole":[{"nuserrolecode":25,"suserrolename":"Approvernbn","sdescription":"a","nsitecode":-1}],
	 * 		"npolicycode":11,"spolicyname":"11"}
	 * @return response entity object holding response status and list of all active
	 *         passwordpolicy
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/copyPasswordPolicyToSelectedRole")
	public ResponseEntity<Object> copyPasswordPolicyToSelectedRole(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		Integer npolicycode = null;
		String spolicyname = null;
		final List<UserRole> userRole = objmapper.convertValue(inputMap.get("userrole"),
				new TypeReference<List<UserRole>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		npolicycode = (Integer) inputMap.get("npolicycode");
		spolicyname = (String) inputMap.get("spolicyname");
		requestContext.setUserInfo(userInfo);
		return passwordPolicyService.copyPasswordPolicyToSelectedRole(userRole, npolicycode, userInfo, spolicyname);

	}
}
