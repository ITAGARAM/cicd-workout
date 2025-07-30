package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.compentencemanagement.model.TrainingCertification;
import com.agaramtech.qualis.compentencemanagement.model.TrainingParticipants;
import com.agaramtech.qualis.compentencemanagement.service.TrainingCertificate.TrainingCertificateService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/trainingcertificate")
public class TrainingCertificateController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrainingCertificateController.class);
	private final TrainingCertificateService trainingCertificateService;

	private RequestContext requestContext;

	public TrainingCertificateController(TrainingCertificateService trainingCertificateService,
			RequestContext requestContext) {
		super();
		this.trainingCertificateService = trainingCertificateService;
		this.requestContext = requestContext;
	}

	@PostMapping(path = "/getTrainingCertificate")
	public ResponseEntity<Object> getTrainingCertificate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();

		String fromDate = "";
		String toDate = "";
		Integer ntrainingcode = null;
		if (inputMap.get("ntrainingcode") != null) {
			ntrainingcode = (Integer) inputMap.get("ntrainingcode");
		}
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}

		final String currentUIDate = (String) inputMap.get("currentdate");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getTrainingCertificate() called");
		return trainingCertificateService.getTrainingCertificate(ntrainingcode, fromDate, toDate, userInfo,
				currentUIDate);

	}

	@PostMapping(path = "/getTrainingParticipants")
	public ResponseEntity<Object> getTrainingParticipants(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int ntrainingcode = (Integer) inputMap.get("ntrainingcode");
		return trainingCertificateService.getTrainingParticipants(ntrainingcode, userInfo);

	}

	@PostMapping(path = "/createTrainingCertificate")
	public ResponseEntity<Object> createTrainingCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());

		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final String currentUIDate = (String) inputMap.get("currentdate");
		final TrainingCertification trainingcertificate = objmapper.convertValue(inputMap.get("trainingcertificate"),
				TrainingCertification.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return trainingCertificateService.createTrainingCertificate(trainingcertificate, userInfo, fromDate, toDate,
				currentUIDate);

	}

	@PostMapping(path = "/getActiveTrainingCertificateById")
	public ResponseEntity<Object> getActiveTrainingCertificateById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int ntrainingcode = (Integer) inputMap.get("ntrainingcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.getActiveTrainingCertificateById(ntrainingcode, userInfo);

	}

	@PostMapping(path = "/conductTrainingCertificate")
	public ResponseEntity<Object> conductTrainingCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final TrainingCertification trainingcertificate = objmapper.convertValue(inputMap.get("trainingcertificate"),
				TrainingCertification.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.conductTrainingCertificate(trainingcertificate, fromDate, toDate, userInfo);

	}

	@PostMapping(path = "/validateConductAndTrainingDate")
	public ResponseEntity<Object> validateConductAndTrainingDate(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final TrainingCertification trainingcertificate = objmapper.convertValue(inputMap.get("trainingcertificate"),
				TrainingCertification.class);
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.validateConductAndTrainingDate(trainingcertificate, fromDate, toDate,
				userInfo);

	}

	@PostMapping(path = "/deleteTrainingCertificate")
	public ResponseEntity<Object> deleteTrainingCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final TrainingCertification trainingcertificate = objmapper.convertValue(inputMap.get("trainingcertificate"),
				TrainingCertification.class);
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.deleteTrainingCertificate(trainingcertificate, fromDate, toDate, userInfo);

	}

	@PostMapping(path = "/updateTrainingCertificate")
	public ResponseEntity<Object> updateTrainingCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final TrainingCertification trainingcertificate = objmapper.convertValue(inputMap.get("trainingcertificate"),
				TrainingCertification.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.updateTrainingCertificate(trainingcertificate, fromDate, toDate, userInfo);

	}

	@PostMapping(path = "/rescheduleTrainingCertificate")
	public ResponseEntity<Object> rescheduleTrainingCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final TrainingCertification trainingcertificate = objmapper.convertValue(inputMap.get("trainingcertificate"),
				TrainingCertification.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.rescheduleTrainingCertificate(trainingcertificate, fromDate, toDate,
				userInfo);
	}

	@PostMapping(path = "/getActiveTrainingParticipantsById")
	public ResponseEntity<Object> getActiveTrainingParticipantsById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nparticipantcode = (Integer) inputMap.get("nparticipantcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.getActiveTrainingParticipantsById(nparticipantcode, userInfo);

	}

	@PostMapping(path = "/createTrainingParticipants")
	public ResponseEntity<Object> createTrainingParticipants(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final List<TrainingParticipants> trainingparticipants = objmapper
				.convertValue(inputMap.get("trainingparticipants"), new TypeReference<List<TrainingParticipants>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.createTrainingParticipants(trainingparticipants, userInfo);

	}

	@PostMapping(path = "/getSectionUsers")
	public ResponseEntity<Object> getSectionUsers(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nsectioncode = (Integer) inputMap.get("nsectioncode");
		final int ntrainingcode = (Integer) inputMap.get("ntrainingcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.getSectionUsers(nsectioncode, ntrainingcode, userInfo);

	}

//	@PostMapping(path = "/inviteTrainingParticipants")
//	public ResponseEntity<Object> inviteTrainingParticipants(@RequestBody Map<String, Object> inputMap) throws Exception {
//
//			final ObjectMapper objmapper = new ObjectMapper();
//			final TrainingParticipants trainingparticipants = objmapper.convertValue(inputMap.get("trainingparticipants"), TrainingParticipants.class);
//			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//			requestContext.setUserInfo(userInfo);
//			return trainingCertificateService.inviteTrainingParticipants(trainingparticipants, userInfo);
//		
//			
//	}

	@PostMapping(path = "/cancelTrainingCertificate")
	public ResponseEntity<Object> cancelTrainingCertificate(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final TrainingCertification trainingcertificate = objmapper.convertValue(inputMap.get("trainingcertificate"),
				TrainingCertification.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return trainingCertificateService.cancelTrainingCertificate(trainingcertificate, fromDate, toDate, userInfo);

	}

//	@PostMapping(path = "/cancelTrainingParticipants")
//	public ResponseEntity<Object> cancelTrainingParticipants(@RequestBody Map<String, Object> inputMap) throws Exception{
//
//			final ObjectMapper objmapper = new ObjectMapper();
//			
//			final TrainingParticipants trainingparticipants = objmapper.convertValue(inputMap.get("trainingparticipants"), TrainingParticipants.class);
//			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//			return trainingCertificateService.cancelTrainingParticipants(trainingparticipants, userInfo);
//
//	}
	@PostMapping(path = "/getInviteParticipants")
	public ResponseEntity<Object> getInviteParticipants(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.getInviteParticipants(inputMap, userInfo);

	}

	@PostMapping(path = "/inviteTrainingParticipants")
	public ResponseEntity<Object> inviteTrainingParticipants(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.inviteTrainingParticipants(inputMap, userInfo);

	}

	@PostMapping(path = "/getCancelParticipants")
	public ResponseEntity<Object> getCancelParticipants(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.getCancelParticipants(inputMap, userInfo);

	}

	@PostMapping(path = "/cancelTrainingParticipants")
	public ResponseEntity<Object> cancelTrainingParticipants(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.cancelTrainingParticipants(inputMap, userInfo);

	}

	@PostMapping(value = "/getTechnique")
	public ResponseEntity<Object> getTechnique(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return trainingCertificateService.getTechnique(userInfo);

	}

}
