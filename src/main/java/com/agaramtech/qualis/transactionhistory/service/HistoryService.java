package com.agaramtech.qualis.transactionhistory.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface HistoryService {

	public ResponseEntity<Map<String, Object>> getTestHistory(String ntransactiontestcode,UserInfo userInfo) throws Exception;

//	public ResponseEntity<Object> getSampleHistory(String npreregno, UserInfo userInfo) throws Exception;
//
//	public ResponseEntity<Object> getSubSampleHistory(String ntransactionsamplecode, UserInfo userInfo) throws Exception;

}
