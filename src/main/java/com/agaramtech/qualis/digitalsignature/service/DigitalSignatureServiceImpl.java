package com.agaramtech.qualis.digitalsignature.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class DigitalSignatureServiceImpl implements DigitalSignatureService {

	private final DigitalSignatureDAO digitalSignatureDAO;

	@Override
	public ResponseEntity<Object> getDigitalSignature(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return digitalSignatureDAO.getDigitalSignature(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateDigitalSignature(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		return digitalSignatureDAO.updateDigitalSignature(request, userInfo);
	}

}
