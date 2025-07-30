package com.agaramtech.qualis.alertview.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class AlertViewServiceImpl implements AlertViewService {

	private final AlertViewDAO alertViewDAO;

	@Override
	public ResponseEntity<Object> getAlertView(final UserInfo userInfo) throws Exception {
		return alertViewDAO.getAlertView(userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getSelectedAlertView(final UserInfo userInfo, final int nsqlQueryCode)
			throws Exception {
		return alertViewDAO.getSelectedAlertView(userInfo, nsqlQueryCode);
	}

	@Override
	public ResponseEntity<Object> getAlerts(final UserInfo userInfo) throws Exception {
		return alertViewDAO.getAlerts(userInfo);
	}

	@Override
	public ResponseEntity<Object> getSelectedAlert(final UserInfo userInfo, final int nsqlQueryCode) throws Exception {
		return alertViewDAO.getSelectedAlert(userInfo, nsqlQueryCode);
	}

}
