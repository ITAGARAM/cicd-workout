package com.agaramtech.qualis.dynamicpreregdesign.service.registrationsubtypemaster;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.configuration.model.ApprovalConfig;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegSubTypeConfigVersion;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegSubTypeConfigVersionRelease;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.global.UserInfo;


public interface RegistrationSubTypeDAO {

	public ResponseEntity<Object> getRegistrationSubTypeInitial(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationSubTypeById(final short regSubTypeCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationSubType(final int regTypeCode,final UserInfo userInfo, int regSubTypeCode) throws Exception;
	
	public ApprovalConfig getApprovalConfig(final int regTypeCode, final int regSubTypeCode, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getAllSeqNoFormats(final UserInfo userInfo)throws Exception;
	
	public ResponseEntity<Object> getAllSeqNoFormatsByRelease(final UserInfo userInfo)throws Exception;

	public ResponseEntity<Object> getPeriods(final UserInfo userInfo)throws Exception;

	public ResponseEntity<Object> createVersion(final RegSubTypeConfigVersion version, final UserInfo userInfo) throws Exception ;

	public ResponseEntity<Object> updateVersion(final RegSubTypeConfigVersion version, final UserInfo userInfo) throws Exception ;

	public ResponseEntity<Object> createRegistrationSubType(final RegistrationSubType regSubType, final UserInfo userInfo) throws Exception ;

	public ResponseEntity<Object> updateRegistrationSubType(final RegistrationSubType regSubType, final UserInfo userInfo) throws Exception ;

	public ResponseEntity<Object> deleteRegistrationSubType(final RegistrationSubType regSubType, final UserInfo userInfo) throws Exception ;

	public RegistrationSubType getRegSubTypeById(final short regSubTypeCode, final UserInfo userInfo) throws Exception;

	public RegSubTypeConfigVersion getVersionDetails(final int regSubTypeConfigCode, final UserInfo userInfo)throws Exception ;

	public RegSubTypeConfigVersionRelease getVersionDetailsByRelease(final int regSubTypeConfigCode, final UserInfo userInfo)throws Exception ;

	public ResponseEntity<Object> deleteVersion(final RegSubTypeConfigVersion version, final UserInfo userInfo)throws Exception ;

	public ResponseEntity<Object> approveVersion(final RegSubTypeConfigVersion version, final UserInfo userInfo)throws Exception ;
	
	public RegSubTypeConfigVersion getApprovedVersion(final int napprovalConfigCode, final UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getSiteWiseAllSeqNoFormats(final UserInfo userInfo)throws Exception;
	ResponseEntity<Object> getSiteWiseAllSeqNoFormatsRelease(final UserInfo userInfo)throws Exception;

	public ResponseEntity<Object> updateReleaseArNoVersion(final RegSubTypeConfigVersionRelease releaseversion, final UserInfo userInfo) throws Exception ;

}
