package com.agaramtech.qualis.stability.service.protocol;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.dynamicpreregdesign.service.dynamicpreregdesign.DynamicPreRegDesignDAO;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class ProtocolServiceImpl implements ProtocolService {
	
	private final ProtocolDAO protocolDAO;
	private final DynamicPreRegDesignDAO dynamicPreRegDesignDAO;
	
	public ProtocolServiceImpl(ProtocolDAO protocolDAO, DynamicPreRegDesignDAO dynamicPreRegDesignDAO) {
		this.protocolDAO = protocolDAO;
		this.dynamicPreRegDesignDAO= dynamicPreRegDesignDAO;
	}	

	public ResponseEntity<Object> getProtocol(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return protocolDAO.getProtocol(inputMap,userInfo);
	}

	public ResponseEntity<Object> getProtocolTemplateList(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return protocolDAO.getProtocolTemplateList(inputMap,userInfo);
	}

	public ResponseEntity<Object> getProtocolData(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return protocolDAO.getProtocolData(inputMap,userInfo);
	}

	public ResponseEntity<Object> getActiveProtocolById(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return protocolDAO.getActiveProtocolById(inputMap,userInfo);		
	}

	public List<TransactionStatus> getEditFilterStatus(final UserInfo userInfo) throws Exception {
		return protocolDAO.getEditFilterStatus(userInfo); 
	}

	public Map<String,Object> getEditProtocolDetails(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		Map<String, Object> map = (Map<String, Object>) protocolDAO.getEditProtocolDetails(inputMap, userInfo);
		map.putAll((Map<? extends String, ? extends Object>) dynamicPreRegDesignDAO.getComboValuesForEdit(map, inputMap, userInfo).getBody());
		return map;
	}

	@Transactional
	public ResponseEntity<Object> createProtocol(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return protocolDAO.createProtocol(inputMap, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> createProtocolWithFile(final MultipartHttpServletRequest inputMap,final UserInfo userInfo) throws Exception {
		return protocolDAO.createProtocolWithFile(inputMap,userInfo);
	}

	@Transactional
	public ResponseEntity<Object> updateProtocol(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return protocolDAO.updateProtocol(inputMap,userInfo);
	}

	@Transactional
	public ResponseEntity<Object> updateProtocolWithFile(final MultipartHttpServletRequest inputMap,final UserInfo userInfo) throws Exception {
		return protocolDAO.updateProtocolWithFile(inputMap,userInfo);
	}

	@Transactional
	public ResponseEntity<Object> deleteProtocol(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return protocolDAO.deleteProtocol(inputMap, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> completeProtocol(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return protocolDAO.completeProtocol(inputMap, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> dynamicActionProtocol(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return protocolDAO.dynamicActionProtocol(inputMap, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> rejectProtocol(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return protocolDAO.rejectProtocol(inputMap, userInfo);
	}

	public ResponseEntity<Object> getCopyProtocol(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return protocolDAO.getCopyProtocol(inputMap, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> copyProtocol(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return protocolDAO.copyProtocol(inputMap, userInfo);
	}

	public ResponseEntity<Object> viewProtocolWithFile(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return protocolDAO.viewProtocolWithFile(inputMap, userInfo); 
	}

	public ResponseEntity<Object> getProtocolFile(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return protocolDAO.getProtocolFile(inputMap, userInfo);
	}

	public ResponseEntity<Object> getActiveProtocolFileById(final int nprotocolCode,final int nprotocolFileCode,final UserInfo userInfo) throws Exception {
		return protocolDAO.getActiveProtocolFileById(nprotocolCode,nprotocolFileCode,userInfo);	
	}

	@Transactional
	public ResponseEntity<Object> createProtocolFile(final UserInfo userInfo, final MultipartHttpServletRequest request)throws Exception {
		return protocolDAO.createProtocolFile(userInfo, request);
	}

	@Transactional
	public ResponseEntity<Object> updateProtocolFile(final UserInfo userInfo, final MultipartHttpServletRequest request) throws Exception {
		return protocolDAO.updateProtocolFile(userInfo, request);
	}

	@Transactional
	public ResponseEntity<Object> deleteProtocolFile(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return protocolDAO.deleteProtocolFile(inputMap,userInfo);
	}
		
	public ResponseEntity<Object> viewProtocolFile(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
			return protocolDAO.viewProtocolFile(inputMap, userInfo); 
	}

	public ResponseEntity<Object> getProduct(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return protocolDAO.getProduct(inputMap, userInfo);
	}
}
