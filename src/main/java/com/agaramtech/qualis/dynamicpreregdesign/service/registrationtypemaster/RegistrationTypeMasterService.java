package com.agaramtech.qualis.dynamicpreregdesign.service.registrationtypemaster;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.UserInfo;


public interface RegistrationTypeMasterService {

	public ResponseEntity<Object> getSampleType(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationTypeById(int regTypeCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createRegistrationType(RegistrationType registrationType, UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;

	public ResponseEntity<Object> updateRegistrationType(RegistrationType registrationType, UserInfo userInfo, Map<String, Object> inputMap)
			throws Exception;

	public ResponseEntity<Object> getRegistrationType(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteRegistrationType(RegistrationType objRegistrationType, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getRegistrationTypeBySampleType(int sampleTypeCode, UserInfo userInfo)
			throws Exception;
}
