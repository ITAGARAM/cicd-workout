package com.agaramtech.qualis.login.service.useruiconfig;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.login.model.UserUiConfig;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class UserUiConfigServiceImpl implements UserUiConfigService {

	private final UserUiConfigDAO UserUiConfigDAO;

	@Override
	public ResponseEntity<Object> getcolormastertheme(final UserInfo userInfo) throws Exception {
		return UserUiConfigDAO.getcolormastertheme(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createUserUiConfig(final UserUiConfig useruiconfig, final UserInfo userInfo)
			throws Exception {
		return UserUiConfigDAO.createUserUiConfig(useruiconfig, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateUserUiConfig(final UserUiConfig useruiconfig, final UserInfo userInfo)
			throws Exception {
		return UserUiConfigDAO.updateUserUiConfig(useruiconfig, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveuseruiconfigById(final int nusercode, final UserInfo userInfo)
			throws Exception {
		return UserUiConfigDAO.getActiveuseruiconfigById(nusercode, userInfo);
	}
}
