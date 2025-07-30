package com.agaramtech.qualis.basemaster.service.sampletestcomments;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.SampleTestComments;
import com.agaramtech.qualis.global.UserInfo;

public interface SampleTestCommentsDAO {
	public ResponseEntity<Object> getSampleTestComments(final UserInfo userInfo) throws Exception;

	public SampleTestComments getActiveSampleTestCommentsById(final int nunitCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createSampleTestComments(final SampleTestComments objSampleTestComments,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateSampleTestComments(final SampleTestComments objSampleTestComments,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSampleTestComments(final SampleTestComments objSampleTestComments,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getCommentType(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getCommentSubType(final UserInfo userInfo) throws Exception;
}
