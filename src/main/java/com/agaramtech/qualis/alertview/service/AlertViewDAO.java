package com.agaramtech.qualis.alertview.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface AlertViewDAO {

	public ResponseEntity<Object> getAlertView(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getSelectedAlertView(final UserInfo userInfo, final int nsqlQueryCode)
			throws Exception;

	public ResponseEntity<Object> getAlerts(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSelectedAlert(final UserInfo userInfo, final int nsqlQueryCode) throws Exception;
}