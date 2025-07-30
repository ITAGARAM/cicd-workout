package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.stability.service.protocol.ProtocolService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/protocol")
public class ProtocolController {

	private RequestContext requestContext;
	private final ProtocolService protocolService;
	
	public ProtocolController(RequestContext requestContext, ProtocolService protocolService) {
		super();
		this.requestContext = requestContext;
		this.protocolService = protocolService;
	}
	
	
	@PostMapping(path = "/getProtocol")
	public ResponseEntity<Object> getProtocol(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.getProtocol(inputMap, userInfo);
	}

	@PostMapping(path = "/getProtocolTemplateList")
	public ResponseEntity<Object> getProtocolTemplateList(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.getProtocolTemplateList(inputMap, userInfo);
	}
	
	@PostMapping(path = "/getProtocolData")
	public ResponseEntity<Object> getProtocolData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.getProtocolData(inputMap, userInfo);
	}
	
	@PostMapping(path = "/getActiveProtocolById")
	public ResponseEntity<Object> getActiveGoodsInById(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.getActiveProtocolById(inputMap, userInfo);
	}
	
	@PostMapping(value = "/getEditFilterStatus")
	public ResponseEntity<Object> getRegistrationFilterStatus(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<>(protocolService.getEditFilterStatus(userInfo),HttpStatus.OK);
	}
	
	@PostMapping(value = "/getEditProtocolDetails")
	public Map<String,Object> getEditRegistrationDetails(@RequestBody Map<String, Object> inputMap)	throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.getEditProtocolDetails(inputMap, userInfo);

	}
	
	@PostMapping(value = "/createProtocol")
	public ResponseEntity<Object> createGoodsInSample(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return protocolService.createProtocol(inputMap, userInfo);
	}
	
	@PostMapping(value = "/createProtocolWithFile")
	public ResponseEntity<Object> createProtocolWithFile(MultipartHttpServletRequest inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(inputMap.getParameter("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return protocolService.createProtocolWithFile(inputMap,userInfo);

	}

	
	@PostMapping(value = "/updateProtocol")
	public ResponseEntity<Object> updateProtocol(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.updateProtocol(inputMap,userInfo);

	}

	@PostMapping(value = "/updateProtocolWithFile")
	public ResponseEntity<Object> updateProtocolWithFile(MultipartHttpServletRequest inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(inputMap.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.updateProtocolWithFile(inputMap,userInfo);

	}
	
	@PostMapping(value = "/deleteProtocol")
	public ResponseEntity<Object> deleteProtocol(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return protocolService.deleteProtocol(inputMap, userInfo);
	}
	
	@PostMapping(path = "/completeProtocol")
	public ResponseEntity<Object> completeProtocol(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return protocolService.completeProtocol(inputMap, userInfo);
	}
	
	@PostMapping(value = "/dynamicActionProtocol")
	public ResponseEntity<Object> dynamicActionProtocol(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return protocolService.dynamicActionProtocol(inputMap, userInfo);
	}
	
	@PostMapping(path = "/rejectProtocol")
	public ResponseEntity<Object> rejectProtocol(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return protocolService.rejectProtocol(inputMap, userInfo);
	}
	
	@PostMapping(path = "/getCopyProtocol")
	public ResponseEntity<Object> getCopyProtocol(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return protocolService.getCopyProtocol(inputMap, userInfo);
	}
	
	@PostMapping(value = "/copyProtocol")
	public ResponseEntity<Object> copyProtocol(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.copyProtocol(inputMap, userInfo);
	}
	
	@PostMapping(value = "/viewProtocolWithFile")
	public ResponseEntity<Object> viewProtocolWithFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.viewProtocolWithFile(inputMap,userInfo);
	}
	
	@PostMapping(value = "/getProtocolFile")
	public ResponseEntity<Object> getProtocolFile(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.getProtocolFile(inputMap,userInfo);
	}
	
	@PostMapping(path = "/getActiveProtocolFileById")
	public ResponseEntity<Object> getActiveProtocolFileById(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objmapper = new ObjectMapper();
		Integer nprotocolCode=null;
		Integer nprotocolFileCode=null;
		if(inputMap.get("nprotocolcode")!=null) {
			nprotocolCode = (Integer) inputMap.get("nprotocolcode");
		}
		if(inputMap.get("nprotocolfilecode")!=null) {
			nprotocolFileCode = (Integer) inputMap.get("nprotocolfilecode");
		}
		
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.getActiveProtocolFileById(nprotocolCode,nprotocolFileCode,userInfo);
	}
	
	@PostMapping(value = "/createProtocolFile")
	public ResponseEntity<Object> createProtocolFile(MultipartHttpServletRequest request) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.createProtocolFile(userInfo, request);
	}

	@PostMapping(value = "/updateProtocolFile")
	public ResponseEntity<Object> updateProtocolFile(MultipartHttpServletRequest request) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.updateProtocolFile(userInfo, request);
	}

	@PostMapping(value = "/deleteProtocolFile")
	public ResponseEntity<Object> deleteProtocolFile(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.deleteProtocolFile(inputMap,userInfo);
	}
	
	@PostMapping(value = "/viewProtocolFile")
	public ResponseEntity<Object> viewProtocolFile(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.viewProtocolFile(inputMap,userInfo);
	}
	
	@PostMapping(path = "/getProduct")
	public ResponseEntity<Object> getProduct(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return protocolService.getProduct(inputMap, userInfo);
	}
}
