package com.agaramtech.qualis.integration.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface IntegrationDAO {
	
	public void createIntegrationRecord(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;
	
	public void interfacerRequestCall(final String url,final List<Map<String,Object>> interfacerMap,final String preregno,final UserInfo userInfo) throws Exception;
		
	public ResponseEntity<Object> updateInterfacerResult(final Map<String, Object> inputMap) throws Exception;	
	
}
