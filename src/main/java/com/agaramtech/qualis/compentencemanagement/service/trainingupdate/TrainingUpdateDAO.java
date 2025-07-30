package com.agaramtech.qualis.compentencemanagement.service.trainingupdate;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.compentencemanagement.model.Technique;
import com.agaramtech.qualis.compentencemanagement.model.TraineeDocuments;
import com.agaramtech.qualis.compentencemanagement.model.TrainingCertification;
import com.agaramtech.qualis.compentencemanagement.model.TrainingDocuments;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface TrainingUpdateDAO {

	ResponseEntity<Object> getTrainingUpdate(String fromDate, String toDate, Integer ntechniquecode, UserInfo userInfo,
			String currentUIDate, Technique selectedTechinqueR) throws Exception;

	ResponseEntity<Object> deleteTrainingDoc(TrainingDocuments objTrainingDoc, UserInfo objUserInfo) throws Exception;

	ResponseEntity<Object> deleteTrainieeDoc(TraineeDocuments objTrainieeDoc, UserInfo objUserInfo) throws Exception;

	ResponseEntity<Object> createTrainingDoc(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception;

	ResponseEntity<Object> createTrainieeDoc(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception;

	Map<String, Object> getTrainingParticipants(int ntrainingcode, UserInfo objUserInfo) throws Exception;

	ResponseEntity<Object> updateTrainingUpdate(Map<String, Object> obj, UserInfo userInfo) throws Exception;

	Map<String, Object> getParticipantsAccordion(int nparticipantcode, UserInfo userInfo) throws Exception;

	TrainingCertification getTrainingUpdateId(int ntechniquecode) throws Exception;

	ResponseEntity<Object> getTrainingUpdateById(int ntrainingcode, UserInfo userInfo) throws Exception;

//	ResponseEntity<Object> attendTrainingParticipants(TrainingParticipants objParticipants, UserInfo userInfo) throws Exception;

//	ResponseEntity<Object> certifyTrainingParticipants(TrainingParticipants objParticipants, UserInfo userInfo) throws Exception;

//	ResponseEntity<Object> competentTrainingParticipants(TrainingParticipants objParticipants, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getTraningUpdateByTechnique(String fromDate, String toDate, Integer ntechniquecode,
			UserInfo userInfo, String currentUIDate, Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getInvitedParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> attendTrainingParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getAttendedParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> certifyTrainingParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getCertifiedParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> competentTrainingParticipants(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> editTrainingFile(TrainingDocuments objTraineeFile, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> viewAttachedTrainingFile(TrainingDocuments objTraineeFile, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> editTraineeFile(TraineeDocuments objTraineeFile, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> viewAttachedTraineeFile(TraineeDocuments objTraineeFile, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> updateTrainingDoc(MultipartHttpServletRequest request, UserInfo userInfo)
			throws JsonParseException, JsonMappingException, IOException, Exception;

	ResponseEntity<Object> updateTraineeDoc(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception;

}