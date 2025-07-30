package com.agaramtech.qualis.synchronisation.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.synchronisation.model.SyncBatch;


public class SyncHistoryServiceImpl implements SyncHistoryService {


	private final SyncHistoryDAO syncHistoryDAO;
	private final CommonFunction commonFunction;


	public SyncHistoryServiceImpl(SyncHistoryDAO syncHistoryDAO, CommonFunction commonFunction) {
		super();
		this.syncHistoryDAO = syncHistoryDAO;
		this.commonFunction = commonFunction;
	}


	@Override
	public ResponseEntity<Object> getSyncHistory(Integer nsyncBatchHistoryCode, final UserInfo userInfo) throws Exception{
		return syncHistoryDAO.getSyncHistory(nsyncBatchHistoryCode,userInfo);
	}

	@Override
	public ResponseEntity<Object> getSyncBatchById(final int nsyncBatchHistoryCode, final UserInfo userInfo) throws Exception
	{
		final SyncBatch syncHistory = syncHistoryDAO.getSyncBatchById(nsyncBatchHistoryCode, userInfo);
		if (syncHistory == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}else {
			return new ResponseEntity<>(syncHistory, HttpStatus.OK);
		}
	}
}
