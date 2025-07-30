package com.agaramtech.qualis.integration.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface IntegrationService {
	public ResponseEntity<Object> updateInterfacerResult(final Map<String, Object> inputMap) throws Exception;

}
