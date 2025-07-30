package com.agaramtech.qualis.configuration.service.adsusers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface ADSUsersService {

	public ResponseEntity<Object> getADSUsers(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> syncADSUsers(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

}
