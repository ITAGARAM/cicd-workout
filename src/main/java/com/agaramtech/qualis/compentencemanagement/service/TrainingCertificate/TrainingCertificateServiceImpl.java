package com.agaramtech.qualis.compentencemanagement.service.TrainingCertificate;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.compentencemanagement.model.TrainingCertification;
import com.agaramtech.qualis.compentencemanagement.model.TrainingParticipants;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class TrainingCertificateServiceImpl implements TrainingCertificateService {

	private static final Log LOGGER = LogFactory.getLog(TrainingCertificateServiceImpl.class);
	
	private final TrainingCertificateDAO trainingcertificationDAO;
	private final CommonFunction commonFunction;

	public ResponseEntity<Object> getTrainingCertificate(final Integer ntrainingcode, final String fromDate,
			final String toDate, final UserInfo userInfo, String currentUIDate) throws Exception {
		LOGGER.info("getTrainingCertificate called");
		return trainingcertificationDAO.getTrainingCertificate(ntrainingcode, fromDate, toDate, userInfo,
				currentUIDate);
	}

	@Override
	public ResponseEntity<Object> getTrainingParticipants(final Integer ntrainingcode, final UserInfo userInfo)
			throws Exception// final int nmasterSiteCode
	{
		return trainingcertificationDAO.getTrainingParticipants(ntrainingcode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveTrainingCertificateById(int ntrainingcode, final UserInfo userInfo)
			throws Exception {
		final TrainingCertification trainingcertification = (TrainingCertification) trainingcertificationDAO
				.getActiveTrainingCertificateById(ntrainingcode, userInfo).getBody();
		if (trainingcertification == null
				|| trainingcertification.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
						.gettransactionstatus()
				|| trainingcertification.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
						.gettransactionstatus()
				|| trainingcertification.getNtransactionstatus() == Enumeration.TransactionStatus.CANCELED
						.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					trainingcertification == null ? Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus()
							: trainingcertification.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
									.gettransactionstatus()
											? "IDS_TRAININGALREADYCOMPLETED"
											: trainingcertification
													.getNtransactionstatus() == Enumeration.TransactionStatus.CONDUCTED
															.gettransactionstatus() ? "IDS_TRAININGALREADYCONDUCTED"
																	: "IDS_TRAININGALREADYCANCELED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(trainingcertification, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTrainingCertificate(final TrainingCertification trainingcertification,
			final UserInfo userInfo, final String fromDate, final String toDate, final String currentUIDate)
			throws Exception {
		return trainingcertificationDAO.createTrainingCertificate(trainingcertification, userInfo, fromDate, toDate,
				currentUIDate);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTrainingParticipants(List<TrainingParticipants> trainingparticipants,
			final UserInfo userInfo) throws Exception {
		return trainingcertificationDAO.createTrainingParticipants(trainingparticipants, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> conductTrainingCertificate(final TrainingCertification trainingcertification,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception {

		return trainingcertificationDAO.conductTrainingCertificate(trainingcertification, fromDate, toDate, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> validateConductAndTrainingDate(final TrainingCertification trainingcertification,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception {

		return trainingcertificationDAO.validateConductAndTrainingDate(trainingcertification, fromDate, toDate,
				userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTrainingCertificate(final TrainingCertification trainingcertification,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception {

		return trainingcertificationDAO.deleteTrainingCertificate(trainingcertification, fromDate, toDate, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTrainingCertificate(final TrainingCertification trainingcertification,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception {

		return trainingcertificationDAO.updateTrainingCertificate(trainingcertification, fromDate, toDate, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveTrainingParticipantsById(int nparticipantcode, final UserInfo userInfo)
			throws Exception {
		final TrainingParticipants trainingparticipants = (TrainingParticipants) trainingcertificationDAO
				.getActiveTrainingParticipantsById(nparticipantcode, userInfo);

		if (trainingparticipants == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(trainingparticipants, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getSectionUsers(final int nsectioncode, final int ntrainingcode,
			final UserInfo userInfo) throws Exception {

		return trainingcertificationDAO.getSectionUsers(nsectioncode, ntrainingcode, userInfo);
	}

//	  @Override
//		public ResponseEntity<Object> inviteTrainingParticipants(TrainingParticipants trainingparticipants,  UserInfo userInfo) throws Exception {
//			
//			return trainingcertificationDAO.inviteTrainingParticipants(trainingparticipants,  userInfo);
//		}

	@Transactional
	@Override
	public ResponseEntity<Object> cancelTrainingCertificate(final TrainingCertification trainingcertification,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception {

		return trainingcertificationDAO.cancelTrainingCertificate(trainingcertification, fromDate, toDate, userInfo);
	}

	@Override
	public ResponseEntity<Object> getInviteParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return trainingcertificationDAO.getInviteParticipants(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> inviteTrainingParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return trainingcertificationDAO.inviteTrainingParticipants(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> cancelTrainingParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return trainingcertificationDAO.cancelTrainingParticipants(inputMap, userInfo);
	}

	public ResponseEntity<Object> getCancelParticipants(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return trainingcertificationDAO.getCancelParticipants(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTechnique(final UserInfo userInfo) {
		return trainingcertificationDAO.getTechnique(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> rescheduleTrainingCertificate(final TrainingCertification trainingcertification,
			final String fromDate, final String toDate, final UserInfo userInfo) throws Exception {

		return trainingcertificationDAO.rescheduleTrainingCertificate(trainingcertification, fromDate, toDate,
				userInfo);
	}
}
