package com.agaramtech.qualis.synchronisation.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;


public interface SynchronizationService {

	public ResponseEntity<Object> sendRecords() throws Exception;

	public ResponseEntity<Object> receiveRecords(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> syncRecords(final UserInfo userInfo, final int nsyncType) throws Exception;

}
