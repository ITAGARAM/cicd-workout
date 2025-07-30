package com.agaramtech.qualis.attachmentscomments.service.attachments;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.attachmentscomments.model.RegistrationAttachment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationSampleAttachment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationTestAttachment;
import com.agaramtech.qualis.global.UserInfo;

@Service
public class AttachmentServiceImpl implements AttachmentService {

	private final AttachmentDAO attachmentDAO;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param attachmentDAO UnitDAO Interface
	 */
	public AttachmentServiceImpl(AttachmentDAO attachmentDAO) {
		this.attachmentDAO = attachmentDAO;
	}
	
	@Override
	public ResponseEntity<Map<String, Object>> getSampleAttachment(String npreregno, UserInfo userInfo) throws Exception {
		
		return attachmentDAO.getSampleAttachment(npreregno, userInfo);
		
	}
	

	@Override
	public ResponseEntity<? extends Object> createSampleAttachment(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		
		return attachmentDAO.createSampleAttachment(request, userInfo);
	}

	@Override
	public ResponseEntity<? extends Object> updateSampleAttachment(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		
		return attachmentDAO.updateSampleAttachment(request, objUserInfo);
	}

	@Override
	public ResponseEntity<? extends Object> deleteSampleAttachment(RegistrationAttachment objSampleAttachment,String npreregno,
			UserInfo objUserInfo) throws Exception {
		
		return attachmentDAO.deleteSampleAttachment(objSampleAttachment,npreregno, objUserInfo);
	}


	@Override
	public ResponseEntity<Object> getEditSampleAttachment(RegistrationAttachment objSampleAttachment, UserInfo objUserInfo)
			throws Exception {
		
		return attachmentDAO.getEditSampleAttachment(objSampleAttachment, objUserInfo);
	}


	@Override
	public ResponseEntity<Object> viewSampleAttachment(RegistrationAttachment objSampleAttachment,UserInfo objUserInfo) throws Exception {
		
		return attachmentDAO.viewSampleAttachment(objSampleAttachment, objUserInfo);
	}
	
	@Override
	public ResponseEntity<Map<String, Object>> getTestAttachment(String ntransactiontestcode, UserInfo userInfo) throws Exception {
		
		return attachmentDAO.getTestAttachment(ntransactiontestcode, userInfo);
		
	}
	

	@Override
	public ResponseEntity<? extends Object> createTestAttachment(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		
		return attachmentDAO.createTestAttachment(request, userInfo);
	}

	@Override
	public ResponseEntity<? extends Object> updateTestAttachment(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		
		return attachmentDAO.updateTestAttachment(request, objUserInfo);
	}

	@Override
	public ResponseEntity<? extends Object> deleteTestAttachment(RegistrationTestAttachment objTestAttachment,String npreregno,
			UserInfo objUserInfo) throws Exception {
		
		return attachmentDAO.deleteTestAttachment(objTestAttachment,npreregno, objUserInfo);
	}


	@Override
	public ResponseEntity<Object> getEditTestAttachment(RegistrationTestAttachment objTestAttachment, UserInfo objUserInfo)
			throws Exception {
		
		return attachmentDAO.getEditTestAttachment(objTestAttachment, objUserInfo);
	}


	@Override
	public ResponseEntity<Object> viewTestAttachment(RegistrationTestAttachment objTestAttachment,UserInfo objUserInfo) throws Exception {
		
		return attachmentDAO.viewTestAttachment(objTestAttachment, objUserInfo);
	}


	@Override
	public ResponseEntity<Map<String, Object>> getSubSampleAttachment(String ntransactionsamplecode, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return attachmentDAO.getSubSampleAttachment(ntransactionsamplecode, userInfo);
	}


	@Override
	public ResponseEntity<? extends Object> createSubSampleAttachment(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return attachmentDAO.createSubSampleAttachment(request, userInfo);
	}


	@Override
	public ResponseEntity<Object> getEditSubSampleAttachment(RegistrationSampleAttachment objSubSampleAttachment,
			UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return attachmentDAO.getEditSubSampleAttachment(objSubSampleAttachment, objUserInfo);
	}


	@Override
	public ResponseEntity<? extends Object> updateSubSampleAttachment(MultipartHttpServletRequest request, UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return attachmentDAO.updateSubSampleAttachment(request, objUserInfo);
	}


	@Override
	public ResponseEntity<? extends Object> deleteSubSampleAttachment(RegistrationSampleAttachment objSampleAttachment,
			String ntransactionsamplecode, UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return attachmentDAO.deleteSubSampleAttachment(objSampleAttachment,ntransactionsamplecode, objUserInfo);
	}


	@Override
	public ResponseEntity<Object> viewSubSampleAttachment(RegistrationSampleAttachment objSampleAttachment,
			UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return  attachmentDAO.viewSubSampleAttachment(objSampleAttachment, objUserInfo);
	}
	

}
