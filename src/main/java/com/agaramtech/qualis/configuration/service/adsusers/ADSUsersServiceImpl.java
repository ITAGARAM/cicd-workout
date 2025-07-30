package com.agaramtech.qualis.configuration.service.adsusers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ADSUsersServiceImpl implements ADSUsersService {

	private final ADSUsersDAO adsUsersDAO;

	public ADSUsersServiceImpl(ADSUsersDAO adsUsersDAO) {
		this.adsUsersDAO = adsUsersDAO;
	}

	@Override
	public ResponseEntity<Object> getADSUsers(UserInfo userInfo) throws Exception {
		return adsUsersDAO.getADSUsers(userInfo);
	}

	@Transactional
	public ResponseEntity<Object> syncADSUsers(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return adsUsersDAO.syncADSUsers(inputMap, userInfo);
	}
}
