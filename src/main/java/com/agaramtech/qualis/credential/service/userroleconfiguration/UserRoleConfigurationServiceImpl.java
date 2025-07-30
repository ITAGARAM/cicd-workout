package com.agaramtech.qualis.credential.service.userroleconfiguration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.credential.model.UserRoleConfig;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class UserRoleConfigurationServiceImpl implements UserRoleConfigurationService{

	
	private final UserRoleConfigurationDAO userRoleConfigurationDAO;

	
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param userRoleConfigurationDAO UserRoleConfigurationDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public UserRoleConfigurationServiceImpl(UserRoleConfigurationDAO userRoleConfigurationDAO, CommonFunction commonFunction) {
		this.userRoleConfigurationDAO = userRoleConfigurationDAO;
		this.commonFunction = commonFunction;
	}
	
	
@Override
public ResponseEntity<Object> getUserRoleConfiguration(final UserInfo userInfo) throws Exception {		
	return userRoleConfigurationDAO.getUserRoleConfiguration(userInfo);
}
	
@Override
public ResponseEntity<Object> getActiveUserRoleConfigurationById(int nuserRoleCode, final UserInfo userInfo) throws Exception {
	final UserRoleConfig userRoleConfig = userRoleConfigurationDAO.getActiveUserRoleConfigurationById(nuserRoleCode);
	if (userRoleConfig == null) {
		return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	}else {
		return new ResponseEntity<>(userRoleConfig, HttpStatus.OK);
	}
}

@Transactional
@Override
public ResponseEntity<Object> updateUserRoleConfiguration(UserRoleConfig userRoleConfig, UserInfo userInfo)	throws Exception {
	return userRoleConfigurationDAO.updateUserRoleConfiguration(userRoleConfig, userInfo);
}

}
