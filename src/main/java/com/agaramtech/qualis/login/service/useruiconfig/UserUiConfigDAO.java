package com.agaramtech.qualis.login.service.useruiconfig;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.login.model.UserUiConfig;

public interface UserUiConfigDAO {

	public ResponseEntity<Object> getcolormastertheme(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createUserUiConfig(final UserUiConfig useruiconfig, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateUserUiConfig(final UserUiConfig useruiconfig, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveuseruiconfigById(final int nuserlimsthemecode, final UserInfo userInfo)
			throws Exception;
}
