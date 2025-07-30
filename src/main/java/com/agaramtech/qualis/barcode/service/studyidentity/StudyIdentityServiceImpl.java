package com.agaramtech.qualis.barcode.service.studyidentity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.barcode.model.StudyIdentity;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'studyidentity' table
 * through its DAO layer.
 */

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class StudyIdentityServiceImpl implements StudyIdentityService {

	private final StudyIdentityDAO studyidentityDAO;
	private final CommonFunction commonFunction;

	public StudyIdentityServiceImpl(StudyIdentityDAO studyidentityDAO, CommonFunction commonFunction) {
		super();
		this.studyidentityDAO = studyidentityDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active studyidentity for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         studyidentity
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getStudyIdentity(final UserInfo userInfo) throws Exception {
		return studyidentityDAO.getStudyIdentity(userInfo);
	}

	/**
	 * This method is used to retrieve active studyidentity object based on the
	 * specified nstudyidentitycode.
	 * 
	 * @param nstudyidentitycode [int] primary key of studyidentity object
	 * @param userInfo [UserInfo] holding logged in user details  
	 * @return response entity object holding response status and data of
	 *         studyidentity object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveStudyIdentityById(final int nstudyidentitycode, final UserInfo userInfo)
			throws Exception {
		final StudyIdentity objStudyIdentity = studyidentityDAO.getActiveStudyIdentityById(nstudyidentitycode,
				userInfo);
		if (objStudyIdentity == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objStudyIdentity, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to studyidentity table.
	 * 
	 * @param objStudyIdentity [studyidentity] object holding details to be added in
	 *                         studyidentity table
	 * @param userInfo [UserInfo] holding logged in user details                         
	 * @return inserted studyidentity object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createStudyIdentity(StudyIdentity objStudyIdentity, UserInfo userInfo)
			throws Exception {
		return studyidentityDAO.createStudyIdentity(objStudyIdentity, userInfo);
	}

	/**
	 * This method is used to update entry in studyidentity table.
	 * 
	 * @param objStudyIdentity [studyidentity] object holding details to be updated
	 *                         in studyidentity table
	 * @param userInfo [UserInfo] holding logged in user details                        
	 * @return response entity object holding response status and data of updated
	 *         studyidentity object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateStudyIdentity(StudyIdentity objStudyIdentity, UserInfo userInfo)
			throws Exception {
		return studyidentityDAO.updateStudyIdentity(objStudyIdentity, userInfo);
	}

	/**
	 * This method id used to delete an entry in studyidentity table
	 * 
	 * @param objStudyIdentity [studyidentity] an Object holds the record to be
	 *                         deleted
	 * @param userInfo [UserInfo] holding logged in user details                         
	 * @return a response entity with corresponding HTTP status and an studyidentity
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteStudyIdentity(StudyIdentity objStudyIdentity, UserInfo userInfo)
			throws Exception {
		return studyidentityDAO.deleteStudyIdentity(objStudyIdentity, userInfo);
	}

}
