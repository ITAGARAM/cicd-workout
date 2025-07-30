package com.agaramtech.qualis.configuration.service.sampletype;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;


@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class SampleTypeServiceImpl implements SampleTypeService {

	
	private final SampleTypeDAO sampleTypeDAO;
	private final CommonFunction commonFunction;
	
	public SampleTypeServiceImpl(SampleTypeDAO sampleTypeDAO, CommonFunction commonFunction) {
		this.sampleTypeDAO = sampleTypeDAO;
		this.commonFunction = commonFunction;
	}
	
	
	@Override
	public ResponseEntity<Object> getSampleType(final UserInfo userInfo) throws Exception {
		return sampleTypeDAO.getSampleType(userInfo);
	}
	/**
	 * 
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleType(SampleType objSite,UserInfo userInfo) throws Exception {		
		return sampleTypeDAO.deleteSampleType(objSite,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getActiveSampleTypeById(final int nsitecode, final UserInfo userInfo) throws Exception {
		final SampleType sampletype = (SampleType) sampleTypeDAO.getActiveSampleTypeById(nsitecode,userInfo);
		if (sampletype == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
			
			return new ResponseEntity<>(sampletype, HttpStatus.OK);
		}
	}
	
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleType(SampleType reason,UserInfo userInfo,boolean DeleteExistingRecord) throws Exception{
		
		return sampleTypeDAO.updateSampleType(reason,userInfo,DeleteExistingRecord); 
	}
	
	
	@Override
	public ResponseEntity<Object> getTransactionFilterType(final UserInfo userInfo) throws Exception {
		return sampleTypeDAO.getTransactionFilterType(userInfo);
	}
	
}
