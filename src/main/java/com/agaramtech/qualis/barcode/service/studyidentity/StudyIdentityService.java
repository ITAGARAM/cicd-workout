package com.agaramtech.qualis.barcode.service.studyidentity;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.barcode.model.StudyIdentity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'studyidentity' table
 */
public interface StudyIdentityService {
	/**
	 * This interface declaration is used to get the over all StudyIdentity with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details  
	 * @return a response entity which holds the list of District with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getStudyIdentity(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active StudyIdentity object
	 * based on the specified nstudyidentitycode.
	 * 
	 * @param nstudyidentitycode [int] primary key of District object
	 * @param userInfo [UserInfo] holding logged in user details  
	 * @return response entity object holding response status and data of
	 *         StudyIdentity object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveStudyIdentityById(final int nstudyidentitycode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to StudyIdentity table.
	 * 
	 * @param objStudyIdentity [StudyIdentity] object holding details to be added in
	 *                         District table
	 *@param userInfo [UserInfo] holding logged in user details  
	 * @return response entity object holding response status and data of added
	 *         StudyIdentity object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createStudyIdentity(final StudyIdentity objStudyIdentity, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in StudyIdentity table.
	 * 
	 * @param objStudyIdentity [StudyIdentity] object holding details to be updated
	 *                         in StudyIdentity table
	 * @param userInfo [UserInfo] holding logged in user details                                           
	 * @return response entity object holding response status and data of updated
	 *         StudyIdentity object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateStudyIdentity(final StudyIdentity objStudyIdentity, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in studyidentity table.
	 * 
	 * @param objStudyIdentity [studyidentity] object holding detail to be deleted
	 *                         in studyidentity table
	 * @param userInfo [UserInfo] holding logged in user details                         
	 * @return response entity object holding response status and data of
	 *         studyidentity object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteStudyIdentity(final StudyIdentity objStudyIdentity, UserInfo userInfo)
			throws Exception;
}
