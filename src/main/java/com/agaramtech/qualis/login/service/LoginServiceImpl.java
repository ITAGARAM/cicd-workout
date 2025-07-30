package com.agaramtech.qualis.login.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.configuration.model.Language;
import com.agaramtech.qualis.credential.model.UsersSite;
import com.agaramtech.qualis.global.UserInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class LoginServiceImpl implements LoginService {
	
	private final LoginDAO loginDAO;

	public LoginServiceImpl(LoginDAO loginDAO) {
		this.loginDAO = loginDAO;
	}
	
	
	@Override
	public ResponseEntity<Object> getLoginInfo() throws Exception {
		return loginDAO.getLoginInfo();
	}
		
	@Override
	public ResponseEntity<Object> getLoginValidation(final String sloginid, final Language language, final int nlogintypecode) throws Exception {
		return loginDAO.getLoginValidation(sloginid, language, nlogintypecode);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> collectLoginData(Map<String, Object> objinputmap, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		return loginDAO.collectLoginData(objinputmap, request, response);
	}
		
	@Override
	@Transactional
	public ResponseEntity<Object> collectAdsLoginData(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.collectAdsLoginData(objinputmap);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> createNewPassword(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.createNewPassword(objinputmap);
	}
		
	@Override
	@Transactional
	public ResponseEntity<Object> changePassword(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.changePassword(objinputmap);
	}
		
	@Override
	@Transactional
	public ResponseEntity<Object> getUserScreenRightsMenu(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.getUserScreenRightsMenu(objinputmap);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> getChangeRole(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.getChangeRole(objinputmap);
	}
	
	@Override
	public ResponseEntity<Object> getUserRoleScreenControl(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.getUserRoleScreenControl(objinputmap);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> validateEsignCredential(final UserInfo userInfo, final String password)throws Exception {
		return loginDAO.validateEsignCredential(userInfo, password);
	}
	
	@Override
	public ResponseEntity<Object> getPassWordPolicy(final int nuserrolecode) throws Exception {
		return loginDAO.getPassWordPolicy(nuserrolecode);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> idleTimeAuditAction(final UserInfo userInfo, final String password,final boolean flag, final int nFlag, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return loginDAO.idleTimeAuditAction(userInfo, password, flag, nFlag, request, response);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> insertAuditAction(final UserInfo userInfo, final String sauditaction,final String scomments, final int nFlag) throws Exception {
		return loginDAO.insertAuditActionTable(userInfo, sauditaction, scomments, nFlag);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> changeOwner(Map<String, Object> inputMap) throws Exception {
		return loginDAO.changeOwner(inputMap);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> validateEsignADSCredential(UserInfo userInfo, String password) throws Exception {
		return loginDAO.validateEsignADSCredential(userInfo, password);
	}
	
	@Override
	public ResponseEntity<Object> changeSite(UsersSite usersSite) throws Exception {
		return loginDAO.changeSite(usersSite);
	}
	
	@Override
	public ResponseEntity<Object> getLoginTypeValidation(final String sloginid, final Language language,final int nusermultisitecode, final int nusermultirolecode, final int nuserrolecode) throws Exception {
		return loginDAO.getLoginTypeValidation(sloginid, language, nusermultisitecode, nusermultirolecode,nuserrolecode);
	}
	@Override
	public ResponseEntity<Object> getAboutInfo(UserInfo userInfo) throws Exception {
		return loginDAO.getAboutInfo(userInfo);
	}
	
	@Override
	public ResponseEntity<Object> validateADSPassword(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.validateADSPassword(objinputmap);
	}
	
	//Gowtham R - ALPD-5190 -- Vacuum Analysis
	@Override
	public ResponseEntity<Object> getJavaTime() throws Exception {
		return loginDAO.getJavaTime();
	}

	
	
	@Override
	@Transactional
	public ResponseEntity<Object> confirmOTP(Map<String, Object> objinputmap,HttpServletRequest request,HttpServletResponse response) throws Exception {
		return loginDAO.confirmOTP(objinputmap,request,response);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> resendOTP(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.resendOTP(objinputmap);
	}
	
	@Override
	@Transactional
	public ResponseEntity<Object> checkAuthenticationByUser(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.checkAuthenticationByUser(objinputmap);
	}
	
	
	@Override
	@Transactional
	public ResponseEntity<Object> getAuthenticationMFA(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.getAuthenticationMFA(objinputmap);
	}
	

	@Override
	@Transactional
	public ResponseEntity<Object> updateActiveStatusMFA(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.updateActiveStatusMFA(objinputmap);
	}
	
	//ALPDJ21-19--Added by vignesh(21-07-2025)--for OTP send to the Email.
	@Override
	@Transactional
	public ResponseEntity<Map<String, Object>> sendOTPMail(Map<String, Object> objinputmap) throws Exception {
		return loginDAO.sendOTPMail(objinputmap);
	}
}
