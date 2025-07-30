package com.agaramtech.qualis.transactionhistory.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.agaramtech.qualis.global.UserInfo;

@Service
public class HistoryServiceImpl implements HistoryService {

	private final HistoryDAO historyDAO;

	public HistoryServiceImpl(HistoryDAO historyDAO) {
		this.historyDAO = historyDAO;
	}
	
	@Override
	public ResponseEntity<Map<String, Object>> getTestHistory(String ntransactiontestcode, UserInfo userInfo) throws Exception {
		
		return historyDAO.getTestHistory(ntransactiontestcode, userInfo);
	}

//	@Override
//	public ResponseEntity<Object> getSampleHistory(String npreregno, UserInfo userInfo) throws Exception {
//		
//		return historyDAO.getSampleHistory(npreregno, userInfo);
//	}
//
//	@Override
//	public ResponseEntity<Object> getSubSampleHistory(String ntransactionsamplecode, UserInfo userInfo) throws Exception {
//		
//		return historyDAO.getSubSampleHistory(ntransactionsamplecode, userInfo);
//	}
}
