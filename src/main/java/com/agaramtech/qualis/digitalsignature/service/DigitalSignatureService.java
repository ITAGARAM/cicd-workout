package com.agaramtech.qualis.digitalsignature.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;

public interface DigitalSignatureService {

	public ResponseEntity<Object> getDigitalSignature(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateDigitalSignature(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception;
}
