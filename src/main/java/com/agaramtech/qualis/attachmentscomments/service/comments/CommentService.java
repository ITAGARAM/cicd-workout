package com.agaramtech.qualis.attachmentscomments.service.comments;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.attachmentscomments.model.RegistrationComment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationSampleComment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationTestComment;
import com.agaramtech.qualis.global.UserInfo;

public interface CommentService {

	public ResponseEntity <Map<String,Object>> getTestComment(String ntransactiontestcode,UserInfo userInfo) throws Exception;

	public ResponseEntity<? extends Object> createTestComment(List<RegistrationTestComment> listTestComment,String ntransactiontestcode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getEditTestComment(RegistrationTestComment objTestComment, UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<? extends Object> updateTestComment(RegistrationTestComment objTestComment, String ntransactiontestcode, UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<? extends Object> deleteTestComment(RegistrationTestComment objTestComment,String ntransactiontestcode,UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Map<String,Object>> getSampleComment(String npreregno, UserInfo userInfo) throws Exception;

	public ResponseEntity<? extends Object> createSampleComment(List<RegistrationComment> listSampleComment,
			String npreregno, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getEditSampleComment(RegistrationComment objSampleComment,
			UserInfo objUserInfo) throws Exception;

	public ResponseEntity<? extends Object> updateSampleComment(RegistrationComment objSampleComment, String npreregno,
			UserInfo objUserInfo) throws Exception;

	public ResponseEntity<? extends Object> deleteSampleComment(RegistrationComment objSampleComment, String npreregno,
			UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Map<String,Object>> getSubSampleComment(String ntransactionsamplecode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String,Object>> createSubSampleComment(List<RegistrationSampleComment> listSampleComment,
			String ntransactionsamplecode, UserInfo userInfo) throws Exception;

	
	public ResponseEntity<? extends Object> updateSubSampleComment(RegistrationSampleComment objSampleComment,
			String ntransactionsamplecode, UserInfo objUserInfo) throws Exception;

	public ResponseEntity<? extends Object> deleteSubSampleComment(RegistrationSampleComment objSampleComment,
			String ntransactionsamplecode, UserInfo objUserInfo) throws Exception;
	public ResponseEntity<Object> getEditSubSampleComment(RegistrationSampleComment objSampleComment, UserInfo objUserInfo) throws Exception ;	

	public ResponseEntity<Object> getSampleTestCommentsListById(Map<String, Object> inputMap, UserInfo objUserInfo) throws Exception ;		

	public ResponseEntity<Object> getCommentSubType(final UserInfo userInfo, Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> getSampleTestCommentsDescById(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

}
