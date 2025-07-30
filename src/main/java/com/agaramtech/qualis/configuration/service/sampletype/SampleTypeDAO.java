package com.agaramtech.qualis.configuration.service.sampletype;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.global.UserInfo;

public interface SampleTypeDAO {
	ResponseEntity<Object> getSampleType(final UserInfo userInfo) throws Exception;
	ResponseEntity<Object> deleteSampleType(SampleType objSample, UserInfo userInfo) throws Exception;
	SampleType getActiveSampleTypeById(int nsampletypecode, UserInfo userInfo) throws Exception;
	
	 ResponseEntity<Object> updateSampleType(SampleType reason,UserInfo userInfo,boolean DeleteExistingRecord) throws Exception;
	 
	 ResponseEntity<Object> getTransactionFilterType(final UserInfo userInfo) throws Exception;
}
