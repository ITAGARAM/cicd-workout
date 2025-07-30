package com.agaramtech.qualis.integration.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class IntegrationServiceImpl implements IntegrationService {
	
	private final IntegrationDAO integrationDAO;
	
	public IntegrationServiceImpl(IntegrationDAO integrationDAO) {
		this.integrationDAO = integrationDAO;
	}		
	
	@Transactional
	public ResponseEntity<Object> updateInterfacerResult(final Map<String, Object> inputMap) throws Exception {
		return integrationDAO.updateInterfacerResult(inputMap);
	}
}
