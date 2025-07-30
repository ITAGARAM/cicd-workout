package com.agaramtech.qualis.basemaster.service.sampletestcomments;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.basemaster.model.SampleTestComments;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

/**
 * This class provides the implementation of the SampleTestCommentsService
 * interface. It performs CRUD operations on the 'SampleTestComments' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class SampleTestCommentsServiceImpl implements SampleTestCommentsService {

	private final SampleTestCommentsDAO sampleTestCommentsDAO;
	private final CommonFunction commonFunction;

	/**
	 * This service implementation method retrieves all available sample test
	 * comments.
	 * 
	 * @param userInfo [UserInfo] holding the logged-in user details
	 * @return a ResponseEntity containing the list of sample test comments
	 * @throws Exception if any error occurs in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleTestComments(final UserInfo userInfo) throws Exception {
		return sampleTestCommentsDAO.getSampleTestComments(userInfo);
	}

	/**
	 * This service implementation method retrieves an active sample test comment by
	 * its ID.
	 * 
	 * @param nsampletestcommentcode [int] the primary key of the sample test
	 *                               comment
	 * @param userInfo               [UserInfo] holding the logged-in user details
	 * @return a ResponseEntity containing the sample test comment if found, or an
	 *         error message if not
	 * @throws Exception if any error occurs in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleTestCommentsById(final int nsampletestcommentcode,
			final UserInfo userInfo) throws Exception {
		final SampleTestComments sampleTestComments = sampleTestCommentsDAO
				.getActiveSampleTestCommentsById(nsampletestcommentcode, userInfo);
		if (sampleTestComments == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(sampleTestComments, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method creates a new sample test comment.
	 * 
	 * @param objSampleTestComments [SampleTestComments] object holding the details
	 *                              to be created
	 * @param userInfo              [UserInfo] holding the logged-in user details
	 * @return a ResponseEntity containing the status and data of the created sample
	 *         test comment
	 * @throws Exception if any error occurs in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleTestComments(final SampleTestComments objSampleTestComments,
			final UserInfo userInfo) throws Exception {
		return sampleTestCommentsDAO.createSampleTestComments(objSampleTestComments, userInfo);
	}

	/**
	 * This service implementation method updates an existing sample test comment.
	 * 
	 * @param objSampleTestComments [SampleTestComments] object holding the updated
	 *                              details
	 * @param userInfo              [UserInfo] holding the logged-in user details
	 * @return a ResponseEntity containing the status and data of the updated sample
	 *         test comment
	 * @throws Exception if any error occurs in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleTestComments(final SampleTestComments objSampleTestComments,
			final UserInfo userInfo) throws Exception {
		return sampleTestCommentsDAO.updateSampleTestComments(objSampleTestComments, userInfo);
	}

	/**
	 * This service implementation method deletes a sample test comment.
	 * 
	 * @param objSampleTestComments [SampleTestComments] object holding the details
	 *                              of the comment to be deleted
	 * @param userInfo              [UserInfo] holding the logged-in user details
	 * @return a ResponseEntity containing the status and data of the deleted sample
	 *         test comment
	 * @throws Exception if any error occurs in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleTestComments(final SampleTestComments objSampleTestComments,
			final UserInfo userInfo) throws Exception {
		return sampleTestCommentsDAO.deleteSampleTestComments(objSampleTestComments, userInfo);
	}

	/**
	 * This service implementation method retrieves all available comment types.
	 * 
	 * @param userInfo [UserInfo] holding the logged-in user details
	 * @return a ResponseEntity containing the list of comment types
	 * @throws Exception if any error occurs in the DAO layer
	 */
	public ResponseEntity<Object> getCommentType(final UserInfo userInfo) throws Exception {
		return sampleTestCommentsDAO.getCommentType(userInfo);
	}

	/**
	 * This service implementation method retrieves all available comment subtypes.
	 * 
	 * @param userInfo [UserInfo] holding the logged-in user details
	 * @return a ResponseEntity containing the list of comment subtypes
	 * @throws Exception if any error occurs in the DAO layer
	 */
	public ResponseEntity<Object> getCommentSubType(final UserInfo userInfo) throws Exception {
		return sampleTestCommentsDAO.getCommentSubType(userInfo);
	}
}
