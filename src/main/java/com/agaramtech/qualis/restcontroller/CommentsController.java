package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.attachmentscomments.model.RegistrationComment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationSampleComment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationTestComment;
import com.agaramtech.qualis.attachmentscomments.service.comments.CommentService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(value="comments")
public class CommentsController {

	private static final Log LOGGER = LogFactory.getLog(CommentsController.class);
	
	private final CommentService commentService;
	private RequestContext requestContext;	

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param commentService CommentService
	 */
	public CommentsController(RequestContext requestContext, CommentService commentService) {
		super();
		this.requestContext = requestContext;
		this.commentService = commentService;
	}

	
	@PostMapping(value = "/getTestComment")
	public ResponseEntity<Map<String, Object>> getTestComment(@RequestBody Map<String, Object> inputMap ) throws Exception {
	
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final String ntransactiontestcode =  (String) inputMap.get("ntransactiontestcode");
			requestContext.setUserInfo(userInfo);
			return commentService.getTestComment(ntransactiontestcode, userInfo);
			
	}
	
	@PostMapping(value = "/createTestComment")
	public ResponseEntity<? extends Object> createTestComment(@RequestBody Map<String, Object> inputMap) throws Exception {
		
			
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final List<RegistrationTestComment> listTestComment = objMapper.convertValue(inputMap.get("testcomment"),new TypeReference<List<RegistrationTestComment>>() {} );
			final String ntransactiontestcode=(String) inputMap.get("ntransactiontestcode");
			requestContext.setUserInfo(userInfo);
			return commentService.createTestComment(listTestComment, ntransactiontestcode, userInfo);
					
	}
	
	@PostMapping(value = "/getEditTestComment")
	public ResponseEntity<Object> getEditTestComment(@RequestBody Map<String, Object> inputMap) throws Exception {
	
			
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationTestComment objTestComment = objMapper.convertValue(inputMap.get("selectedrecord"), RegistrationTestComment.class);
			requestContext.setUserInfo(objUserInfo);
			return commentService.getEditTestComment(objTestComment, objUserInfo);		
	}
	
	@PostMapping(value = "/updateTestComment")
	public ResponseEntity<? extends Object> updateTestComment(@RequestBody Map<String, Object> inputMap) throws Exception {
					
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationTestComment objTestComment = objMapper.convertValue(inputMap.get("testcomment"), RegistrationTestComment.class);
			final String ntransactiontestcode=(String) inputMap.get("ntransactiontestcode");
			requestContext.setUserInfo(objUserInfo);
			return commentService.updateTestComment(objTestComment, ntransactiontestcode, objUserInfo);
				
	}
	
	
	@PostMapping(value = "/deleteTestComment")
	public ResponseEntity<? extends Object> deleteTestComment(@RequestBody Map<String, Object> inputMap) throws Exception {
					
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationTestComment objTestComment = objMapper.convertValue(inputMap.get("testcomment"), RegistrationTestComment.class);
			final String ntransactiontestcode=(String) inputMap.get("ntransactiontestcode");
			requestContext.setUserInfo(objUserInfo);
			return commentService.deleteTestComment(objTestComment,ntransactiontestcode, objUserInfo);
					
	}
	
	@PostMapping(value = "/getSampleComment")
	public ResponseEntity<Map<String,Object>> getSampleComment(@RequestBody Map<String, Object> inputMap ) throws Exception {
	
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final String npreregno =  (String) inputMap.get("npreregno");
			requestContext.setUserInfo(userInfo);
			return commentService.getSampleComment(npreregno, userInfo);
			
	}
	
	@PostMapping(value = "/createSampleComment")
	public ResponseEntity<? extends Object> createSampleComment(@RequestBody Map<String, Object> inputMap) throws Exception {
		
			
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final List<RegistrationComment> listSampleComment = objMapper.convertValue(inputMap.get("samplecomment"),new TypeReference<List<RegistrationComment>>() {} );
			final String npreregno=(String) inputMap.get("npreregno");
			requestContext.setUserInfo(userInfo);
			return commentService.createSampleComment(listSampleComment, npreregno, userInfo);
					
	}
	

	@PostMapping(value = "/getEditSampleComment")
	public ResponseEntity<Object> getEditSampleComment(@RequestBody Map<String, Object> inputMap) throws Exception {
	
			
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationComment objSampleComment = objMapper.convertValue(inputMap.get("selectedrecord"), RegistrationComment.class);
			requestContext.setUserInfo(objUserInfo);
			return commentService.getEditSampleComment(objSampleComment, objUserInfo);
				
	}
	
	@PostMapping(value = "/updateSampleComment")
	public ResponseEntity<? extends Object> updateSampleComment(@RequestBody Map<String, Object> inputMap) throws Exception {
					
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationComment objSampleComment = objMapper.convertValue(inputMap.get("samplecomment"), RegistrationComment.class);
			final String npreregno=(String) inputMap.get("npreregno");
			requestContext.setUserInfo(objUserInfo);
			return commentService.updateSampleComment(objSampleComment, npreregno, objUserInfo);
				
	}
	
	
	@PostMapping(value = "/deleteSampleComment")
	public ResponseEntity<? extends Object> deleteSampleComment(@RequestBody Map<String, Object> inputMap) throws Exception {
					
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationComment objSampleComment = objMapper.convertValue(inputMap.get("samplecomment"), RegistrationComment.class);
			final String npreregno=(String) inputMap.get("npreregno");
			requestContext.setUserInfo(objUserInfo);
			return commentService.deleteSampleComment(objSampleComment,npreregno, objUserInfo);
				
	}
	
	

	@PostMapping(value = "/getSubSampleComment")
	public ResponseEntity<Map<String,Object>> getSubSampleComment(@RequestBody Map<String, Object> inputMap ) throws Exception {
	
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final String ntransactionsamplecode =  (String) inputMap.get("ntransactionsamplecode");
			requestContext.setUserInfo(userInfo);
			return commentService.getSubSampleComment(ntransactionsamplecode, userInfo);
			
	}
	
	@PostMapping(value = "/createSubSampleComment")
	public ResponseEntity<Map<String,Object>> createSubSampleComment(@RequestBody Map<String, Object> inputMap) throws Exception {
					
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final List<RegistrationSampleComment> listSampleComment = objMapper.convertValue(inputMap.get("subsamplecomment"),new TypeReference<List<RegistrationSampleComment>>() {} );
			final String ntransactionsamplecode=(String) inputMap.get("ntransactionsamplecode");
			requestContext.setUserInfo(userInfo);
			return commentService.createSubSampleComment(listSampleComment, ntransactionsamplecode, userInfo);
				
	}
	

	@PostMapping(value = "/getEditSubSampleComment")
	public ResponseEntity<Object> getEditSubSampleComment(@RequestBody Map<String, Object> inputMap) throws Exception {
	
			
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationSampleComment objSampleComment = objMapper.convertValue(inputMap.get("selectedrecord"), RegistrationSampleComment.class);
			requestContext.setUserInfo(objUserInfo);
			return commentService.getEditSubSampleComment(objSampleComment, objUserInfo);
				
	}
	
	@PostMapping(value = "/updateSubSampleComment")
	public ResponseEntity<? extends Object> updateSubSampleComment(@RequestBody Map<String, Object> inputMap) throws Exception {
		
			
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationSampleComment objSampleComment = objMapper.convertValue(inputMap.get("subsamplecomment"), RegistrationSampleComment.class);
			final String ntransactionsamplecode=(String) inputMap.get("ntransactionsamplecode");
			requestContext.setUserInfo(objUserInfo);
			return commentService.updateSubSampleComment(objSampleComment, ntransactionsamplecode, objUserInfo);
				
	}
	
	
	@PostMapping(value = "/deleteSubSampleComment")
	public ResponseEntity<? extends Object> deleteSubSampleComment(@RequestBody Map<String, Object> inputMap) throws Exception {
					
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final RegistrationSampleComment objSampleComment = objMapper.convertValue(inputMap.get("subsamplecomment"), RegistrationSampleComment.class);
			final String ntransactionsamplecode=(String) inputMap.get("ntransactionsamplecode");
			requestContext.setUserInfo(objUserInfo);
			return commentService.deleteSubSampleComment(objSampleComment,ntransactionsamplecode, objUserInfo);
					
	} 
	
	@PostMapping(value = "/getCommentSubType")
	public ResponseEntity<Object> getCommentSubType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),UserInfo.class);
		requestContext.setUserInfo(userInfo); 
		return commentService.getCommentSubType (userInfo, inputMap);
	}
	
	@PostMapping(value = "/getSampleTestCommentsListById")
	public ResponseEntity<Object> getSampleTestCommentsListById(@RequestBody Map<String, Object> inputMap) throws Exception{	
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),UserInfo.class);
		requestContext.setUserInfo(userInfo); 
		return commentService.getSampleTestCommentsListById (inputMap,userInfo);
	}
	
	@PostMapping(value = "/getSampleTestCommentsDescById")
	public ResponseEntity<Object> getSampleTestCommentsDescById(@RequestBody Map<String, Object> inputMap) throws Exception{	
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),UserInfo.class);
		requestContext.setUserInfo(userInfo); 
		return commentService.getSampleTestCommentsDescById (inputMap,userInfo);
	}
	
}
