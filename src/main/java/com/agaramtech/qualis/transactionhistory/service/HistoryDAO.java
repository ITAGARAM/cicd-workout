package com.agaramtech.qualis.transactionhistory.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface HistoryDAO {

	public ResponseEntity<Map<String, Object>> getTestHistory(String ntransactiontestcode,UserInfo userInfo) throws Exception;

//	public ResponseEntity<Object> getSampleHistory(String npreregno, UserInfo userInfo) throws Exception;
//
//	public ResponseEntity<Object> getSubSampleHistory(String ntransactionSampleCode, UserInfo userInfo) throws Exception;

}
