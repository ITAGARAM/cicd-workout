package com.agaramtech.qualis.dynamicpreregdesign.service.registrationsubtypemaster;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.configuration.model.ApprovalConfig;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegSubTypeConfigVersion;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegSubTypeConfigVersionRelease;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class RegistrationSubTypeServiceImpl implements RegistrationSubTypeService {

	private final RegistrationSubTypeDAO regSubTypeDAO;
	private final CommonFunction commonFunction;

	public RegistrationSubTypeServiceImpl(RegistrationSubTypeDAO regSubTypeDAO, CommonFunction commonFunction) {
		super();
		this.regSubTypeDAO = regSubTypeDAO;
		this.commonFunction = commonFunction;
	}
	


	@Override
	public ResponseEntity<Object> getRegistrationSubTypeInitial(UserInfo userInfo) throws Exception {
		
		return regSubTypeDAO.getRegistrationSubTypeInitial(userInfo);
		
	}

	@Override
	public ApprovalConfig getApprovalConfig(int regTypeCode, int regSubTypeCode, UserInfo userInfo) throws Exception {

		return regSubTypeDAO.getApprovalConfig(regTypeCode, regSubTypeCode, userInfo);

	}

	@Override
	public ResponseEntity<Object> getRegistrationSubType(final int regTypeCode, final UserInfo userInfo, final int regSubTypeCode)
			throws Exception {
		return regSubTypeDAO.getRegistrationSubType(regTypeCode, userInfo, regSubTypeCode);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> createRegistrationSubType(RegistrationSubType regSubType, UserInfo userInfo) throws Exception {
		
		return regSubTypeDAO.createRegistrationSubType(regSubType, userInfo);
	}


	@Transactional
	@Override
	public ResponseEntity<Object> updateRegistrationSubType(RegistrationSubType regSubType, UserInfo userInfo) throws Exception {
		
		return regSubTypeDAO.updateRegistrationSubType(regSubType, userInfo);
	}


	@Transactional
	@Override
	public ResponseEntity<Object> deleteRegistrationSubType(RegistrationSubType regSubType, UserInfo userInfo) throws Exception {
		
		return regSubTypeDAO.deleteRegistrationSubType(regSubType, userInfo);
	}

	@Override
	public ResponseEntity<Object> getAllSeqNoFormats(final UserInfo userInfo)
			throws Exception {
		return regSubTypeDAO.getAllSeqNoFormats(userInfo);

	}
	
	@Override
	public ResponseEntity<Object> getAllSeqNoFormatsByRelease(final UserInfo userInfo)
			throws Exception {
		return regSubTypeDAO.getAllSeqNoFormatsByRelease(userInfo);

	}
	
	@Override
	public ResponseEntity<Object> getPeriods(final UserInfo userInfo)
			throws Exception {
		return regSubTypeDAO.getPeriods(userInfo);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> createVersion(RegSubTypeConfigVersion version, UserInfo userInfo) throws Exception {
		
		return regSubTypeDAO.createVersion(version, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateVersion(RegSubTypeConfigVersion version, UserInfo userInfo) throws Exception {
		
		return regSubTypeDAO.updateVersion(version, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationSubTypeById(short regSubTypeCode, UserInfo userInfo) throws Exception {
		
		return regSubTypeDAO.getRegistrationSubTypeById(regSubTypeCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditRegSubType(short regSubTypeCode, UserInfo userInfo) throws Exception {
		
		RegistrationSubType regSubType = regSubTypeDAO.getRegSubTypeById(regSubTypeCode, userInfo);
		if(regSubType!=null) {
			return new ResponseEntity<>(regSubType,HttpStatus.OK);
		}else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		
	}

	@Override
	public ResponseEntity<Object> getVersionDetails(int regSubTypeConfigCode, UserInfo userInfo) throws Exception {
		
		RegSubTypeConfigVersion regSubTypeConfig = regSubTypeDAO.getVersionDetails(regSubTypeConfigCode, userInfo);
		if(regSubTypeConfig!=null) {
			return new ResponseEntity<>(regSubTypeConfig,HttpStatus.OK);
		}else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getVersionDetailsByRelease(int regSubTypeConfigCode, UserInfo userInfo) throws Exception {
		
		RegSubTypeConfigVersionRelease regSubTypeConfig = regSubTypeDAO.getVersionDetailsByRelease(regSubTypeConfigCode, userInfo);
		if(regSubTypeConfig!=null) {
			return new ResponseEntity<>(regSubTypeConfig,HttpStatus.OK);
		}else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteVersion(RegSubTypeConfigVersion version, UserInfo userInfo) throws Exception {
		
		return regSubTypeDAO.deleteVersion(version, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approveVersion(RegSubTypeConfigVersion version, UserInfo userInfo) throws Exception {
		
		return regSubTypeDAO.approveVersion(version, userInfo);
	}
	
	public RegSubTypeConfigVersion getApprovedVersion(final int napprovalConfigCode, final UserInfo userInfo) throws Exception{
		return regSubTypeDAO.getApprovedVersion(napprovalConfigCode,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getSiteWiseAllSeqNoFormats(final UserInfo userInfo)
			throws Exception {
		return regSubTypeDAO.getSiteWiseAllSeqNoFormats(userInfo);

	}
	
	@Override
	public ResponseEntity<Object> getSiteWiseAllSeqNoFormatsRelease(final UserInfo userInfo)
			throws Exception {
		return regSubTypeDAO.getSiteWiseAllSeqNoFormatsRelease(userInfo);

	}
	
	@Transactional
	public ResponseEntity<Object> updateReleaseArNoVersion(final RegSubTypeConfigVersionRelease releaseversion, final UserInfo userInfo) throws Exception {
		return regSubTypeDAO.updateReleaseArNoVersion(releaseversion,userInfo);

	}	

}
