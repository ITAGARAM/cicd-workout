package com.agaramtech.qualis.attachmentscomments.service.attachments;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.attachmentscomments.model.RegistrationAttachment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationSampleAttachment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationTestAttachment;
import com.agaramtech.qualis.global.UserInfo;

public interface AttachmentService {

	public ResponseEntity<Map<String, Object>> getSampleAttachment(String npreregno,UserInfo userInfo) throws Exception;

	public ResponseEntity<? extends Object> createSampleAttachment(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getEditSampleAttachment(RegistrationAttachment objSampleAttachment, UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<? extends Object> updateSampleAttachment(MultipartHttpServletRequest request, UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<? extends Object> deleteSampleAttachment(RegistrationAttachment objSampleAttachment,String npreregno,UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> viewSampleAttachment(RegistrationAttachment objSampleAttachment,UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getTestAttachment(String ntransactiontestcode,UserInfo userInfo) throws Exception;

	public ResponseEntity<? extends Object> createTestAttachment(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getEditTestAttachment(RegistrationTestAttachment objTestAttachment, UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<? extends Object> updateTestAttachment(MultipartHttpServletRequest request, UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<? extends Object> deleteTestAttachment(RegistrationTestAttachment objTestAttachment,String npreregno,UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> viewTestAttachment(RegistrationTestAttachment objTestAttachment,UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getSubSampleAttachment(String ntransactionsamplecode, UserInfo userInfo) throws Exception;

	public ResponseEntity<? extends Object> createSubSampleAttachment(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getEditSubSampleAttachment(RegistrationSampleAttachment objSubSampleAttachment,
			UserInfo objUserInfo) throws Exception;

	public ResponseEntity<? extends Object> updateSubSampleAttachment(MultipartHttpServletRequest request, UserInfo objUserInfo) throws Exception;

	public ResponseEntity<? extends Object> deleteSubSampleAttachment(RegistrationSampleAttachment objSampleAttachment,
			String ntransactionsamplecode, UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> viewSubSampleAttachment(RegistrationSampleAttachment objSampleAttachment,
			UserInfo objUserInfo) throws Exception;


//  public ResponseEntity<Object> getBatchCreationFile(int nreleasebatchcode, UserInfo userInfo) throws Exception;

//	public ResponseEntity<Object> createBatchCreationFile(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception;
//	
//	public ResponseEntity<Object> getEditBatchCreationFile(BatchCreationFile batchCreationFile, UserInfo objUserInfo) throws Exception;
//
//	public ResponseEntity<Object> updateBatchCreationFile(MultipartHttpServletRequest request, UserInfo objUserInfo) throws Exception;
//	
//	public ResponseEntity<Object> deleteBatchCreationFile(BatchCreationFile batchCreationFile, int nreleasebatchcode, UserInfo objUserInfo) throws Exception;
//	
//	public ResponseEntity<Object> viewBatchCreationFile(BatchCreationFile batchCreationFile, UserInfo objUserInfo) throws Exception;
	
}
