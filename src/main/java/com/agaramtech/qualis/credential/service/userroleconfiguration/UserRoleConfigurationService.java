package com.agaramtech.qualis.credential.service.userroleconfiguration;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.credential.model.UserRoleConfig;
import com.agaramtech.qualis.global.UserInfo;


public interface UserRoleConfigurationService {
    public ResponseEntity<Object> getUserRoleConfiguration(final UserInfo userInfo) throws Exception;
    public ResponseEntity<Object> getActiveUserRoleConfigurationById(final int nuserRoleCode,final UserInfo userInfo) throws Exception ; 
	public ResponseEntity<Object> updateUserRoleConfiguration(UserRoleConfig userRoleConfig, UserInfo userInfo) throws Exception;

}
