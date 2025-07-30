package com.agaramtech.qualis.compentencemanagement.service.trainingupdate;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.compentencemanagement.model.Technique;
import com.agaramtech.qualis.compentencemanagement.model.TraineeDocuments;
import com.agaramtech.qualis.compentencemanagement.model.TrainingDocuments;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor; 

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class TrainingUpdateServiceImpl implements TrainingUpdateService {
	 
	private static final Logger LOGGER = LoggerFactory.getLogger(TrainingUpdateServiceImpl.class); 
	private final TrainingUpdateDAO trainingUpdateDAO;

	@Override
	public ResponseEntity<Object> getTrainingUpdate(final String fromDate, final String toDate,
			final Integer ntechniquecode, final UserInfo userInfo, final String currentUIDate,
			Technique selectedTechinque) throws Exception {
		LOGGER.info("getTrainingUpdate called");
		return trainingUpdateDAO.getTrainingUpdate(fromDate, toDate, ntechniquecode, userInfo, currentUIDate,
				selectedTechinque);
	}

	@Override
	public ResponseEntity<Object> getTrainingUpdateById(int ntrainingcode, final UserInfo userInfo)
			throws Exception {
		return trainingUpdateDAO.getTrainingUpdateById(ntrainingcode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTraningUpdateByTechnique(final String fromDate, final String toDate,
			final Integer ntechniquecode, final UserInfo userInfo, final String currentUIDate,
			Map<String, Object> objmap) throws Exception {
		return trainingUpdateDAO.getTraningUpdateByTechnique(fromDate, toDate, ntechniquecode, userInfo, currentUIDate,
				objmap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTrainingDoc(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		return trainingUpdateDAO.createTrainingDoc(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTrainingDoc(final TrainingDocuments objTrainingDoc, final UserInfo objUserInfo)
			throws Exception {
		return trainingUpdateDAO.deleteTrainingDoc(objTrainingDoc, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTrainieeDoc(MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		return trainingUpdateDAO.createTrainieeDoc(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTrainieeDoc(final TraineeDocuments objTrainieeDoc, final UserInfo objUserInfo)
			throws Exception {
		return trainingUpdateDAO.deleteTrainieeDoc(objTrainieeDoc, objUserInfo);
	}

	@Override
	public Map<String, Object> getTrainingParticipants(final int ntrainingcode, final UserInfo objUserInfo)
			throws Exception {
		return trainingUpdateDAO.getTrainingParticipants(ntrainingcode, objUserInfo);
	}

//	@Override
//	public ResponseEntity<Object> attendTrainingParticipants(TrainingParticipants objParticipants, UserInfo userInfo)
//			throws Exception {
//		return TrainingUpdateDAO.attendTrainingParticipants(objParticipants, userInfo);
//
//	}

//	@Override
//	public ResponseEntity<Object> certifyTrainingParticipants(TrainingParticipants objParticipants, UserInfo userInfo)
//			throws Exception {
//		return TrainingUpdateDAO.certifyTrainingParticipants(objParticipants, userInfo);
//
//	}
//
//	
//	@Override
//	public ResponseEntity<Object> competentTrainingParticipants(TrainingParticipants objParticipants, UserInfo userInfo)
//			throws Exception {
//		return TrainingUpdateDAO.competentTrainingParticipants(objParticipants, userInfo);
//
//	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTrainingUpdate(Map<String, Object> obj, UserInfo userInfo) throws Exception {
		return trainingUpdateDAO.updateTrainingUpdate(obj, userInfo);

	}

	@Override
	public Map<String, Object> getParticipantsAccordion(int nparticipantcode, UserInfo userInfo) throws Exception {

		return trainingUpdateDAO.getParticipantsAccordion(nparticipantcode, userInfo);
	}

	public ResponseEntity<Object> getInvitedParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		return trainingUpdateDAO.getInvitedParticipants(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> attendTrainingParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return trainingUpdateDAO.attendTrainingParticipants(inputMap, userInfo);
	}

	public ResponseEntity<Object> getAttendedParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		return trainingUpdateDAO.getAttendedParticipants(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> certifyTrainingParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return trainingUpdateDAO.certifyTrainingParticipants(inputMap, userInfo);
	}

	public ResponseEntity<Object> getCertifiedParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		return trainingUpdateDAO.getCertifiedParticipants(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> competentTrainingParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return trainingUpdateDAO.competentTrainingParticipants(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> editTrainingFile(TrainingDocuments objTraineeFile, UserInfo userInfo)
			throws Exception {
		return trainingUpdateDAO.editTrainingFile(objTraineeFile, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> viewAttachedTrainingFile(TrainingDocuments objTraineeFile, UserInfo userInfo)
			throws Exception {
		return trainingUpdateDAO.viewAttachedTrainingFile(objTraineeFile, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> editTraineeFile(TraineeDocuments objTraineeFile, UserInfo userInfo) throws Exception {
		return trainingUpdateDAO.editTraineeFile(objTraineeFile, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> viewAttachedTraineeFile(TraineeDocuments objTraineeFile, UserInfo userInfo)
			throws Exception {
		return trainingUpdateDAO.viewAttachedTraineeFile(objTraineeFile, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTrainingDoc(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		return trainingUpdateDAO.updateTrainingDoc(request, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTraineeDoc(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		return trainingUpdateDAO.updateTraineeDoc(request, userInfo);
	}

}
