package com.agaramtech.qualis.synchronisation.service;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface SyncHistoryService {

	public ResponseEntity<Object> getSyncHistory(Integer nsyncBatchHistoryCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSyncBatchById(final int nsyncBatchHistoryCode, final UserInfo userInfo)
			throws Exception;
}
