package com.agaramtech.qualis.restcontroller;

import java.util.Map;
	
	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;
	import org.springframework.http.ResponseEntity;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.RequestBody;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RestController;
	
	import com.agaramtech.qualis.configuration.model.Language;
	import com.agaramtech.qualis.credential.model.UsersSite;
	import com.agaramtech.qualis.global.Enumeration;
	import com.agaramtech.qualis.global.UserInfo;
	import com.agaramtech.qualis.login.service.LoginService;
	import com.fasterxml.jackson.databind.ObjectMapper;
	
	import jakarta.servlet.http.HttpServletRequest;
	import jakarta.servlet.http.HttpServletResponse;
		
@RestController
@RequestMapping("/login")
public class LoginController {
	
		private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
	
		//private RequestContext requestContext;
		private final LoginService loginService;
	
	
		/**
		 * This constructor injection method is used to pass the object dependencies to the class.
		 * @param requestContext RequestContext to hold the request
		 * @param sectionService SectionService
		 */
		public LoginController(LoginService loginService) {
			super();
			//this.requestContext = requestContext;
			this.loginService = loginService;
		}
		
	
		
	@PostMapping(value = "/getloginInfo")
	public ResponseEntity<Object> getLoginInfo() throws Exception {
		return loginService.getLoginInfo();
	}
	
	@PostMapping(value = "/getloginvalidation")
	public ResponseEntity<Object> getLoginValidation(@RequestBody Map<String, Object> objinputmap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final Language language = objMapper.convertValue(objinputmap.get("Language"), Language.class);
		final String sloginid = (String) objinputmap.get("sloginid");
		final int nlogintypecode = (int) objinputmap.get("nlogintypecode");
		return loginService.getLoginValidation(sloginid, language, nlogintypecode);
	}
	
	//ALPD-4393 17/06/2024 Abdul Gaffoor.A To validate ads password of login User and to get ads user details and update it
	@PostMapping(value = "/validateadspassword")
	public ResponseEntity<Object> validateADSPassword(@RequestBody Map<String, Object> objinputmap) throws Exception {
	//	ObjectMapper objMapper = new ObjectMapper();
	//	final Language language = objMapper.convertValue(objinputmap.get("Language"), Language.class);
	//	final String sloginid = (String) objinputmap.get("sloginid");
	//	final int nlogintypecode = (int) objinputmap.get("nlogintypecode");
		return loginService.validateADSPassword(objinputmap);
	}
	
		
	@PostMapping(value = "/internallogin")
	public ResponseEntity<Object> collectLoginData(@RequestBody Map<String, Object> objinputmap,HttpServletRequest request, HttpServletResponse response) throws Exception {
		return loginService.collectLoginData(objinputmap, request, response);
	}
	
	@PostMapping(value = "/adsLogin")
	public ResponseEntity<Object> collectAdsLoginData(@RequestBody Map<String, Object> objinputmap) throws Exception {
		return loginService.collectAdsLoginData(objinputmap);
	}
	
	@PostMapping(value = "/createnewpassword")
	public ResponseEntity<Object> createNewPassword(@RequestBody Map<String, Object> objinputmap) throws Exception {
		return loginService.createNewPassword(objinputmap);
	}
	
	@PostMapping(value = "/changepassword")
	public ResponseEntity<Object> changePassword(@RequestBody Map<String, Object> objinputmap) throws Exception {
		return loginService.changePassword(objinputmap);
	}
	
	@PostMapping(value = "/getuserscreenrightsmenu")
	public ResponseEntity<Object> getUserScreenRightsMenu(@RequestBody Map<String, Object> objinputmap)	throws Exception {
		return loginService.getUserScreenRightsMenu(objinputmap);
	}
	
	@PostMapping(value = "/getchangerole")
	public ResponseEntity<Object> getChangeRole(@RequestBody Map<String, Object> objinputmap) throws Exception {
		return loginService.getChangeRole(objinputmap);
	}
	
	@PostMapping(value = "/getUserRoleScreenControl")
	public ResponseEntity<Object> getUserRoleScreenControl(@RequestBody Map<String, Object> objinputmap)throws Exception {
		return loginService.getUserRoleScreenControl(objinputmap);
	}
	
	@PostMapping(value = "/validateEsignCredential")
	public ResponseEntity<Object> validateEsignCredential(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String password = (String) inputMap.get("password");
		if (userInfo.getNlogintypecode() == Enumeration.LoginType.ADS.getnlogintype()) {
			return loginService.validateEsignADSCredential(userInfo, password);
		} else {
			return loginService.validateEsignCredential(userInfo, password);
		}
	}
	
	@PostMapping(value = "/getPassWordPolicy")
	public ResponseEntity<Object> getPassWordPolicy(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int nuserrolecode = (int) inputMap.get("nuserrolecode");
		return loginService.getPassWordPolicy(nuserrolecode);
	}
	
	@PostMapping(value = "/idleTimeAuditAction")
	public ResponseEntity<Object> idleTimeAuditAction(@RequestBody Map<String, Object> inputMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String password = (String) inputMap.get("password");
		final boolean flag = (boolean) inputMap.get("flag");
		final int nFlag = (int) inputMap.get("nFlag");
		return loginService.idleTimeAuditAction(userInfo, password, flag, nFlag, request, response);
	}
	
	@PostMapping(value = "/insertAuditAction")
	public ResponseEntity<Object> insertAuditAction(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String sauditaction = (String) inputMap.get("sauditaction");
		final String scomments = (String) inputMap.get("scomments");
		int nFlag = -2;
		if (inputMap.containsKey("nFlag")) {
			nFlag = (int) inputMap.get("nFlag");
		}
		return loginService.insertAuditAction(userInfo, sauditaction, scomments, nFlag);
	}
	
	@PostMapping(value = "/changeOwner")
	public ResponseEntity<Object> changeOwner(@RequestBody Map<String, Object> inputMap) throws Exception {
		return loginService.changeOwner(inputMap);
	}
	
	@PostMapping(value = "/changeSite")
	public ResponseEntity<Object> changeSite(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UsersSite usersSite = objMapper.convertValue(inputMap.get("usersSite"), UsersSite.class);
		return loginService.changeSite(usersSite);
	}
	
	@PostMapping(value = "/getlogintypevalidation")
	public ResponseEntity<Object> getlogintypevalidation(@RequestBody Map<String, Object> objinputmap)throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final Language language = objMapper.convertValue(objinputmap.get("Language"), Language.class);
		final String sloginid = (String) objinputmap.get("sloginid");
		final int nusermultisitecode = (int) objinputmap.get("nusermultisitecode");
		final int nusermultirolecode = (int) objinputmap.get("nusermultirolecode");
		final int nuserrolecode = (int) objinputmap.get("nuserrolecode");
		return loginService.getLoginTypeValidation(sloginid, language, nusermultisitecode, nusermultirolecode,nuserrolecode);
	}
	//ALPD-4102
	@PostMapping(value = "/getAboutInfo")
	public ResponseEntity<Object> getAboutInfo(@RequestBody Map<String, Object> objinputmap)throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(objinputmap.get("userinfo"), UserInfo.class);
		return loginService.getAboutInfo(userInfo);
	}
	//Gowtham R - ALPD-5190 -- Vacuum Analysis
	@PostMapping(value = "/getJavaTime")
	public ResponseEntity<Object> getJavaTime() throws Exception {
		return loginService.getJavaTime();
	}
	
	
	//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
	@PostMapping(value = "/confirmOTP")
	public ResponseEntity<Object> confirmOTP(@RequestBody Map<String,Object> objinputmap,HttpServletRequest request, HttpServletResponse response) throws Exception  {
	  
			return loginService.confirmOTP(objinputmap,request,response);
	
	    
	}
	
	//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
	@PostMapping(value = "/resendOTP")
	public ResponseEntity<Object> resendOTP(@RequestBody Map<String,Object> objinputmap) throws Exception  {
	
			return loginService.resendOTP(objinputmap);
	}
	
	//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
	@PostMapping(value = "/checkAuthenticationByUser")
	public ResponseEntity<Object> checkAuthenticationByUser(@RequestBody Map<String,Object> objinputmap) throws Exception  {
	  
			return loginService.checkAuthenticationByUser(objinputmap);
			
	}
	
	
	
	//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
	@PostMapping(value = "/getAuthenticationMFA")
	public ResponseEntity<Object> getAuthenticationMFA(@RequestBody Map<String, Object> inputMap) throws Exception {
		return loginService.getAuthenticationMFA(inputMap);
	}
	
	//ALPD-6042--Added by vignesh(09-07-2025)--for Email Authentication.
	@PostMapping(value = "/updateActiveStatusMFA")
	public ResponseEntity<Object> updateActiveStatusMFA(@RequestBody Map<String, Object> inputMap) throws Exception {
		return loginService.updateActiveStatusMFA(inputMap);
	}
	
	//ALPD-6042--Added by vignesh(21-07-2025)--for OTP send to the Email.
	@PostMapping(value = "/sendOTPMail")
	public ResponseEntity<Map<String, Object>> sendOTPMail(@RequestBody Map<String, Object> inputMap) throws Exception {
		return loginService.sendOTPMail(inputMap);
	}
	
}
