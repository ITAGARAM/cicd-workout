package com.agaramtech.qualis.stability.service.protocol;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.global.UserInfo;

public interface ProtocolDAO {
	public ResponseEntity<Object> getProtocol(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getProtocolTemplateList(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getProtocolData(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getActiveProtocolById(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception ;
	public List<TransactionStatus> getEditFilterStatus(final UserInfo userInfo) throws Exception;
	public Map<String,Object> getEditProtocolDetails(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createProtocol(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createProtocolWithFile(final MultipartHttpServletRequest request,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> updateProtocol(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> updateProtocolWithFile(final MultipartHttpServletRequest request,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> deleteProtocol(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> completeProtocol(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> dynamicActionProtocol(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> rejectProtocol(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getCopyProtocol(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> copyProtocol(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> viewProtocolWithFile(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getProtocolFile(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getActiveProtocolFileById(final int nprotocolCode,final int nprotocolFileCode,final UserInfo userInfo) throws Exception ;
	public ResponseEntity<Object> createProtocolFile(final UserInfo userInfo, final MultipartHttpServletRequest request) throws Exception;
	public ResponseEntity<Object> updateProtocolFile(final UserInfo userInfo, final MultipartHttpServletRequest request) throws Exception;
	public ResponseEntity<Object> deleteProtocolFile(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> viewProtocolFile(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getProduct(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception ;
}
