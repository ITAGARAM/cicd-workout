package com.agaramtech.qualis.compentencemanagement.service.TrainingCertificate;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.compentencemanagement.model.TrainingCertification;
import com.agaramtech.qualis.compentencemanagement.model.TrainingParticipants;
import com.agaramtech.qualis.global.UserInfo;

public interface TrainingCertificateDAO {

	public ResponseEntity<Object> getTrainingCertificate(final Integer ntrainingcode, final String fromDate,
			final String toDate, final UserInfo userInfo, final String currentUIDate) throws Exception;

	public ResponseEntity<Object> getActiveTrainingCertificateById(final int ntrainingcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createTrainingCertificate(final TrainingCertification trainingcertificate,
			final UserInfo userInfo, final String fromDate, final String toDate, final String currentUIDate)
			throws Exception;

	public ResponseEntity<Object> createTrainingParticipants(List<TrainingParticipants> trainingparticipants,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> conductTrainingCertificate(final TrainingCertification trainingcertificate,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> validateConductAndTrainingDate(final TrainingCertification trainingcertificate,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteTrainingCertificate(final TrainingCertification trainingcertificate,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateTrainingCertificate(final TrainingCertification trainingcertificate,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception;

	public TrainingParticipants getActiveTrainingParticipantsById(final int nparticipantcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTrainingParticipants(Integer ntrainingcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSectionUsers(final int nsectioncode, final int ntrainingcode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> cancelTrainingCertificate(final TrainingCertification trainingcertificate,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getInviteParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> inviteTrainingParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> cancelTrainingParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getCancelParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTechnique(final UserInfo userInfo);

	public ResponseEntity<Object> rescheduleTrainingCertificate(final TrainingCertification trainingcertificate,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception;
}
