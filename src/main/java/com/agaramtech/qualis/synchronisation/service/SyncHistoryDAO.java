package com.agaramtech.qualis.synchronisation.service;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.synchronisation.model.SyncBatch;

public interface SyncHistoryDAO {

	public ResponseEntity<Object> getSyncHistory(Integer nsyncBatchHistoryCode, final UserInfo userInfo) throws Exception;

	public SyncBatch getSyncBatchById(final int nsyncBatchHistoryCode, final UserInfo userInfo) throws Exception;
}
