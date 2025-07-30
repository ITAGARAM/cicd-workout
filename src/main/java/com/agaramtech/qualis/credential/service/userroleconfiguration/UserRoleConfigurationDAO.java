package com.agaramtech.qualis.credential.service.userroleconfiguration;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.credential.model.UserRoleConfig;
import com.agaramtech.qualis.global.UserInfo;


public interface UserRoleConfigurationDAO {
	public ResponseEntity<Object> getUserRoleConfiguration(final UserInfo userInfo) throws Exception;
	public UserRoleConfig getActiveUserRoleConfigurationById(final int nuserRoleCode) throws Exception ;
	public ResponseEntity<Object> updateUserRoleConfiguration(UserRoleConfig userConfiguration, UserInfo userInfo) throws Exception;
	
	
	
}
