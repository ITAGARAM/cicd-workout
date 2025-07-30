package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.attachmentscomments.model.RegistrationAttachment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationSampleAttachment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationTestAttachment;
import com.agaramtech.qualis.attachmentscomments.service.attachments.AttachmentService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(value="attachment")
public class AttachmentController {
	
	private final AttachmentService attachmentService;
	private RequestContext requestContext;	

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext        RequestContext to hold the request
	 * @param attachmentService AttachmentService
	 */
	public AttachmentController(RequestContext requestContext, AttachmentService attachmentService) {
		super();
		this.requestContext = requestContext;
		this.attachmentService = attachmentService;
	}
	
	 
	
	@PostMapping(path = "/getSampleAttachment")
	public ResponseEntity<Map<String, Object>> getSampleAttachment(@RequestBody Map<String, Object> inputMap ) throws Exception {
	
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final String npreregno =  (String) inputMap.get("npreregno");
			requestContext.setUserInfo(userInfo);
			return attachmentService.getSampleAttachment(npreregno, userInfo);			
		
	}
	
	
	@PostMapping(path = "/createSampleAttachment")
	public ResponseEntity<? extends Object> createSampleAttachment(MultipartHttpServletRequest request) throws Exception {
		
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return attachmentService.createSampleAttachment(request, userInfo);			
		
	}
	
	@PostMapping(path = "/getEditSampleAttachment")
	public ResponseEntity<Object> getEditSampleAttachment(@RequestBody Map<String, Object> inputMap) throws Exception {
	
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationAttachment objSampleAttachment = objMapper.convertValue(inputMap.get("selectedrecord"), RegistrationAttachment.class);
			requestContext.setUserInfo(objUserInfo);
			return attachmentService.getEditSampleAttachment(objSampleAttachment, objUserInfo);
		
	}
	
	@PostMapping(path = "/updateSampleAttachment")
	public ResponseEntity<? extends Object> updateSampleAttachment(MultipartHttpServletRequest request) throws Exception {
		
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
			requestContext.setUserInfo(objUserInfo);
			return attachmentService.updateSampleAttachment(request, objUserInfo);		
	}
	
	
	@PostMapping(path = "/deleteSampleAttachment")
	public ResponseEntity<? extends Object> deleteSampleAttachment(@RequestBody Map<String, Object> inputMap) throws Exception {
	
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationAttachment objSampleAttachment = objMapper.convertValue(inputMap.get("sampleattachment"), RegistrationAttachment.class);
			final String npreregno=(String) inputMap.get("npreregno");
			requestContext.setUserInfo(objUserInfo);
			return attachmentService.deleteSampleAttachment(objSampleAttachment,npreregno, objUserInfo);
					
	}
	
	@PostMapping(path = "/viewSampleAttachment")
	public ResponseEntity<Object> viewSampleAttachment(@RequestBody Map<String, Object> inputMap) throws Exception {
		
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationAttachment objSampleAttachment = objMapper.convertValue(inputMap.get("file"), RegistrationAttachment.class);
			requestContext.setUserInfo(objUserInfo);
			return attachmentService.viewSampleAttachment(objSampleAttachment, objUserInfo);
					
	}
	@PostMapping(path = "/getTestAttachment")
	public ResponseEntity<Map<String, Object>> getTestAttachment(@RequestBody Map<String, Object> inputMap ) throws Exception {
		
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final String ntransactiontestcode =  (String) inputMap.get("ntransactiontestcode");
			requestContext.setUserInfo(userInfo);
			return attachmentService.getTestAttachment(ntransactiontestcode, userInfo);
					
	}
	
	@PostMapping(path = "/createTestAttachment")
	public ResponseEntity<? extends Object> createTestAttachment(MultipartHttpServletRequest request) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return attachmentService.createTestAttachment(request, userInfo);			
		
	}
	
	
	@PostMapping(path = "/getEditTestAttachment")
	public ResponseEntity<Object> getEditTestAttachment(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final RegistrationTestAttachment objTestAttachment = objMapper.convertValue(inputMap.get("selectedrecord"), RegistrationTestAttachment.class);
		requestContext.setUserInfo(objUserInfo);
		return attachmentService.getEditTestAttachment(objTestAttachment, objUserInfo);		
	}
	
	@PostMapping(path = "/updateTestAttachment")
	public ResponseEntity<? extends Object> updateTestAttachment(MultipartHttpServletRequest request) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(objUserInfo);
		return attachmentService.updateTestAttachment(request, objUserInfo);			
		
	}	
	
	@PostMapping(path = "/deleteTestAttachment")
	public ResponseEntity<? extends Object> deleteTestAttachment(@RequestBody Map<String, Object> inputMap) throws Exception {
	
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationTestAttachment objTestAttachment = objMapper.convertValue(inputMap.get("testattachment"), RegistrationTestAttachment.class);
			final String npreregno=(String) inputMap.get("ntransactiontestcode");
			requestContext.setUserInfo(objUserInfo);
			return attachmentService.deleteTestAttachment(objTestAttachment,npreregno, objUserInfo);		
	
	}
	
	
	@PostMapping(path = "/viewTestAttachment")
	public ResponseEntity<Object> viewTestAttachment(@RequestBody Map<String, Object> inputMap) throws Exception {
		
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationTestAttachment objTestAttachment = objMapper.convertValue(inputMap.get("file"), RegistrationTestAttachment.class);
			requestContext.setUserInfo(objUserInfo);
			return attachmentService.viewTestAttachment(objTestAttachment, objUserInfo);	
		
	}
	
	
	@PostMapping(path = "/getSubSampleAttachment")
	public ResponseEntity<Map<String, Object>> getSubSampleAttachment(@RequestBody Map<String, Object> inputMap ) throws Exception {
		
		    final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final String ntransactionsamplecode =  (String) inputMap.get("ntransactionsamplecode");
			requestContext.setUserInfo(userInfo);
			return attachmentService.getSubSampleAttachment(ntransactionsamplecode, userInfo);		
	}
	

	
	@PostMapping(path = "/createSubSampleAttachment")
	public ResponseEntity<? extends Object> createSubSampleAttachment(MultipartHttpServletRequest request) throws Exception {
		
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
			requestContext.setUserInfo(userInfo);
			return attachmentService.createSubSampleAttachment(request, userInfo);
			
		
	}
	
	@PostMapping(path = "/getEditSubSampleAttachment")
	public ResponseEntity<Object> getEditSubSampleAttachment(@RequestBody Map<String, Object> inputMap) throws Exception {
	
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationSampleAttachment objSubSampleAttachment = objMapper.convertValue(inputMap.get("selectedrecord"), RegistrationSampleAttachment.class);
			requestContext.setUserInfo(objUserInfo);
			return attachmentService.getEditSubSampleAttachment(objSubSampleAttachment, objUserInfo);
				
	}
	
	@PostMapping(path = "/updateSubSampleAttachment")
	public ResponseEntity<? extends Object> updateSubSampleAttachment(MultipartHttpServletRequest request) throws Exception {
		
			final ObjectMapper objMapper = new ObjectMapper();
			UserInfo objUserInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
			requestContext.setUserInfo(objUserInfo);
			return attachmentService.updateSubSampleAttachment(request, objUserInfo);
			
	}
	
	
	@PostMapping(path = "/deleteSubSampleAttachment")
	public ResponseEntity<? extends Object> deleteSubSampleAttachment(@RequestBody Map<String, Object> inputMap) throws Exception {
	
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationSampleAttachment objSampleAttachment = objMapper.convertValue(inputMap.get("subsampleattachment"), RegistrationSampleAttachment.class);
			final String ntransactionsamplecode=(String) inputMap.get("ntransactionsamplecode");
			requestContext.setUserInfo(objUserInfo);
			return attachmentService.deleteSubSampleAttachment(objSampleAttachment,ntransactionsamplecode, objUserInfo);
					
	}
	
	@PostMapping(path = "/viewSubSampleAttachment")
	public ResponseEntity<Object> viewSubSampleAttachment(@RequestBody Map<String, Object> inputMap) throws Exception {
		
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationSampleAttachment objSampleAttachment = objMapper.convertValue(inputMap.get("file"), RegistrationSampleAttachment.class);
			requestContext.setUserInfo(objUserInfo);
			return attachmentService.viewSubSampleAttachment(objSampleAttachment, objUserInfo);
					
	}
	
}
