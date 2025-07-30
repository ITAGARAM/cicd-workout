package com.agaramtech.qualis.product.service.component;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Component;

public interface ComponentService {
	
	public ResponseEntity<Object> getComponent(final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getComponentPortal(final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getActiveComponentById(final int ncomponentCode, final UserInfo userInfo) throws Exception ;
	public ResponseEntity<Object> createComponent(Map<String, Object> inputMap,Component component, UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> updateComponent(Map<String, Object> inputMap,Component component, UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> deleteComponent(Map<String, Object> inputMap,Component component, UserInfo userInfo) throws Exception;
}
