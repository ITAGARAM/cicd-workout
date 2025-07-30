package com.agaramtech.qualis.attachmentscomments.service.comments;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.agaramtech.qualis.attachmentscomments.model.RegistrationComment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationSampleComment;
import com.agaramtech.qualis.attachmentscomments.model.RegistrationTestComment;
import com.agaramtech.qualis.global.UserInfo;

@Service
public class CommentServiceImpl implements CommentService {

	private final CommentDAO commentDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param commentDAO CommentDAO Interface
	 */
	public CommentServiceImpl(CommentDAO commentDAO) {
		this.commentDAO = commentDAO;
	}

	@Override
	public ResponseEntity <Map<String,Object>> getTestComment(String ntransactiontestcode, UserInfo userInfo) throws Exception {

		return commentDAO.getTestComment(ntransactiontestcode, userInfo);
	}

	@Override
	public ResponseEntity<? extends Object> createTestComment(List<RegistrationTestComment> listTestComment, String ntransactiontestcode,
			UserInfo userInfo) throws Exception {

		return commentDAO.createTestComment(listTestComment, ntransactiontestcode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditTestComment(RegistrationTestComment objTestComment, UserInfo objUserInfo)
			throws Exception {

		return commentDAO.getEditTestComment(objTestComment, objUserInfo);
	}


	@Override
	public ResponseEntity<? extends Object> updateTestComment(RegistrationTestComment objTestComment, String ntransactiontestcode,
			UserInfo objUserInfo) throws Exception {

		return commentDAO.updateTestComment(objTestComment, ntransactiontestcode, objUserInfo);
	}

	@Override
	public ResponseEntity<? extends Object> deleteTestComment(RegistrationTestComment objTestComment, String ntransactiontestcode,
			UserInfo objUserInfo) throws Exception {

		return commentDAO.deleteTestComment(objTestComment, ntransactiontestcode, objUserInfo);
	}

	@Override
	public ResponseEntity<Map<String,Object>> getSampleComment(String npreregno, UserInfo userInfo) throws Exception {

		return commentDAO.getSampleComment(npreregno, userInfo);
	}

	@Override
	public ResponseEntity<? extends Object> createSampleComment(List<RegistrationComment> listSampleComment, String npreregno,
			UserInfo userInfo) throws Exception {

		return commentDAO.createSampleComment(listSampleComment, npreregno, userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditSampleComment(RegistrationComment objSampleComment,
			UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return commentDAO.getEditSampleComment( objSampleComment,
				objUserInfo) ;
	}

	@Override
	public ResponseEntity<? extends Object> updateSampleComment(RegistrationComment objSampleComment, String npreregno,
			UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return commentDAO.updateSampleComment( objSampleComment,  npreregno,
				objUserInfo);
	}

	@Override
	public ResponseEntity<? extends Object> deleteSampleComment(RegistrationComment objSampleComment, String npreregno,
			UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return commentDAO.deleteSampleComment( objSampleComment,  npreregno,
				objUserInfo) ;
	}


	@Override
	public ResponseEntity<Map<String,Object>> getSubSampleComment(String ntransactionsamplecode, UserInfo userInfo) throws Exception {

		return commentDAO.getSubSampleComment(ntransactionsamplecode, userInfo);
	}

	@Override
	public ResponseEntity<Map<String,Object>> createSubSampleComment(List<RegistrationSampleComment> listSampleComment, String ntransactionsamplecode,
			UserInfo userInfo) throws Exception {

		return commentDAO.createSubSampleComment(listSampleComment, ntransactionsamplecode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditSubSampleComment(RegistrationSampleComment objSampleComment,
			UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return commentDAO.getEditSubSampleComment( objSampleComment,
				objUserInfo) ;
	}

	@Override
	public ResponseEntity<? extends Object> updateSubSampleComment(RegistrationSampleComment objSampleComment, String ntransactionsamplecode,
			UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return commentDAO.updateSubSampleComment( objSampleComment,  ntransactionsamplecode,
				objUserInfo);
	}

	@Override
	public ResponseEntity<? extends Object> deleteSubSampleComment(RegistrationSampleComment objSampleComment, String ntransactionsamplecode,
			UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return commentDAO.deleteSubSampleComment( objSampleComment,  ntransactionsamplecode,
				objUserInfo) ;
	}
	
	@Override
	public ResponseEntity<Object> getCommentSubType(final UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		return commentDAO.getCommentSubType(userInfo, inputMap);
	}
	
	@Override
	public ResponseEntity<Object> getSampleTestCommentsListById(Map<String, Object> inputMap, UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		return commentDAO.getSampleTestCommentsListById( inputMap,objUserInfo) ;

	}

	@Override
	public ResponseEntity<Object> getSampleTestCommentsDescById(Map<String, Object> inputMap, UserInfo userInfo) throws Exception{
		return commentDAO.getSampleTestCommentsDescById( inputMap,userInfo) ;
	}

}
