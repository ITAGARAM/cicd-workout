package com.agaramtech.qualis.eln.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface ELNService {

	public ResponseEntity<Object> getLimsSDMSRecords(final String nsitecode) throws Exception;

	public String updateLimsSDMSInprogress(final String ntransactionresultcode) throws Exception;

	public String updateLimsSDMSComplete(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getLimsTests() throws Exception;

	public ResponseEntity<Object> getLimsTestParameters() throws Exception;

	public ResponseEntity<Object> getLimsInstrument(final int nsitecode) throws Exception;

	public ResponseEntity<Object> getLimsMaterial(final int nsitecode) throws Exception;
}
