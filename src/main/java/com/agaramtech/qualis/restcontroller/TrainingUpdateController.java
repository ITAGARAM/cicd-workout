package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.compentencemanagement.model.Technique;
import com.agaramtech.qualis.compentencemanagement.model.TraineeDocuments;
import com.agaramtech.qualis.compentencemanagement.model.TrainingDocuments;
//import com.agaramtech.qualis.compentencemanagement.model.TrainingParticipants;
import com.agaramtech.qualis.compentencemanagement.service.trainingupdate.TrainingUpdateService;
//import com.agaramtech.qualis.contactmaster.model.SupplierFile;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/trainingupdate")
public class TrainingUpdateController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrainingUpdateController.class);

	private final TrainingUpdateService trainingUpdateService;

	private RequestContext requestContext;

	public TrainingUpdateController(TrainingUpdateService trainingUpdateService, RequestContext requestContext) {
		super();
		this.trainingUpdateService = trainingUpdateService;
		this.requestContext = requestContext;
	}

	@PostMapping(value = "/getTrainingUpdate")
	public ResponseEntity<Object> getTrainingUpdate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		Integer ntechniquecode = null;
		if (inputMap.get("ntechniquecode") != null) {
			ntechniquecode = (Integer) inputMap.get("ntechniquecode");
		}
		Technique selectedTechinque = null;
		if (inputMap.get("selectedTechinque") != null) {
			selectedTechinque = objmapper.convertValue(inputMap.get("selectedTechinque"), Technique.class);
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		// final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"),
		// UserInfo.class);
		LOGGER.info("getTrainingUpdate() called");
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.getTrainingUpdate(fromDate, toDate, ntechniquecode, userInfo, currentUIDate,
				selectedTechinque);
	}

	@PostMapping(value = "/getTrainingUpdateById")
	public ResponseEntity<Object> getTrainingUpdateById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		int ntrainingcode = 0; 
		if (inputMap.get("ntrainingcode") != null) {
			ntrainingcode = (int) inputMap.get("ntrainingcode");
		}
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.getTrainingUpdateById(ntrainingcode, userInfo);
	}

	@PostMapping(value = "/getTraningUpdateByTechnique")
	public ResponseEntity<Object> getTraningUpdateByTechnique(@RequestBody Map<String, Object> objmap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		if (objmap.get("fromDate") != null) {
			fromDate = (String) objmap.get("fromDate");
		}
		if (objmap.get("toDate") != null) {
			toDate = (String) objmap.get("toDate");
		}
		Integer ntechniquecode = null;
		ntechniquecode = (Integer) objmap.get("ntechniquecode");
		final String currentUIDate = (String) objmap.get("currentdate");
		UserInfo userInfo = objMapper.convertValue(objmap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.getTraningUpdateByTechnique(fromDate, toDate, ntechniquecode, userInfo,
				currentUIDate, objmap);
	}

	@PostMapping(value = "/createTrainingDoc")
	public ResponseEntity<Object> createTrainingDoc(MultipartHttpServletRequest request) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
//			TrainingDocuments objTrainingDocuments = objMapper.convertValue(((Map<String, Object>) request).get("trainingdoc"),
//					new TypeReference<TrainingDocuments>() {
//					});
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.createTrainingDoc(request, userInfo);
	}

	@PostMapping(value = "/deleteTrainingDoc")
	public ResponseEntity<Object> deleteTrainingDoc(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TrainingDocuments objdeleteTrainingDoc = objMapper.convertValue(inputMap.get("trainingdoc"),
				TrainingDocuments.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.deleteTrainingDoc(objdeleteTrainingDoc, userInfo);
	}

	@PostMapping(value = "/createTrainieeDoc")
	public ResponseEntity<Object> createTrainieeDoc(MultipartHttpServletRequest request) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.createTrainieeDoc(request, userInfo);
	}

	@PostMapping(value = "/deleteTrainieeDoc")
	public ResponseEntity<Object> deleteTrainieeDoc(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TraineeDocuments objdeleteTrainieeDoc = objMapper.convertValue(inputMap.get("trainieedoc"),
				TraineeDocuments.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.deleteTrainieeDoc(objdeleteTrainieeDoc, userInfo);
	}

	@PostMapping(value = "/getTrainingParticipants")
	public Map<String, Object> getTrainingParticipants(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		Integer ntechniquecode = (Integer) inputMap.get("ntechniquecode");
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.getTrainingParticipants(ntechniquecode, userInfo);
	}

//	@RequestMapping(value = "/attendTrainingParticipants", method = RequestMethod.POST)
//	public ResponseEntity<Object> attendTrainingParticipants(@RequestBody Map<String, Object> obj) throws Exception {
//		ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(obj.get("userinfo"), UserInfo.class);
//		final TrainingParticipants objParticipants = objMapper.convertValue(obj.get("trainingparticipants"),
//				TrainingParticipants.class);
//		requestContext.setUserInfo(userInfo);
//		return trainingUpdateService.attendTrainingParticipants(objParticipants, userInfo);
//	}

//	@RequestMapping(value = "/certifyTrainingParticipants", method = RequestMethod.POST)
//	public ResponseEntity<Object> certifyTrainingParticipants(@RequestBody Map<String, Object> obj) throws Exception {
//		ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(obj.get("userinfo"), UserInfo.class);
//		final TrainingParticipants objParticipants = objMapper.convertValue(obj.get("trainingparticipants"),
//				TrainingParticipants.class);
//		requestContext.setUserInfo(userInfo);
//		return trainingUpdateService.certifyTrainingParticipants(objParticipants, userInfo);
//	}

//	@RequestMapping(value = "/competentTrainingParticipants", method = RequestMethod.POST)
//	public ResponseEntity<Object> competentTrainingParticipants(@RequestBody Map<String, Object> obj) throws Exception {
//		ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(obj.get("userinfo"), UserInfo.class);
//		final TrainingParticipants objParticipants = objMapper.convertValue(obj.get("trainingparticipants"),
//				TrainingParticipants.class);
//		requestContext.setUserInfo(userInfo);
//		return trainingUpdateService.competentTrainingParticipants(objParticipants, userInfo);
//	}

	@PostMapping(value = "/completeTraining")
	public ResponseEntity<Object> updateTrainingUpdate(@RequestBody Map<String, Object> obj) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(obj.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.updateTrainingUpdate(obj, userInfo);
	}

	@PostMapping(value = "/getParticipantsAccordion")
	public Map<String, Object> getParticipantsAccordion(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		int nparticipantcode = (int) inputMap.get("nparticipantcode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.getParticipantsAccordion(nparticipantcode, userInfo);
	}

	@PostMapping(path = "/getInvitedParticipants")
	public ResponseEntity<Object> getInviteParticipants(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.getInvitedParticipants(inputMap, userInfo);
	}

	@PostMapping(path = "/attendTrainingParticipants")
	public ResponseEntity<Object> attendTrainingParticipants(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.attendTrainingParticipants(inputMap, userInfo);
	}

	@PostMapping(path = "/getAttendedParticipants")
	public ResponseEntity<Object> getAttendedParticipants(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.getAttendedParticipants(inputMap, userInfo);
	}

	@PostMapping(path = "/certifyTrainingParticipants")
	public ResponseEntity<Object> certifyTrainingParticipants(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.certifyTrainingParticipants(inputMap, userInfo);
	}

	@PostMapping(path = "/getCertifiedParticipants")
	public ResponseEntity<Object> getCertifiedParticipants(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.getCertifiedParticipants(inputMap, userInfo);
	}

	@PostMapping(path = "/competentTrainingParticipants")
	public ResponseEntity<Object> competentTrainingParticipants(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();

		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.competentTrainingParticipants(inputMap, userInfo);
	}

	@PostMapping(value = "/editTrainingFile")
	public ResponseEntity<Object> editTrainingFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TrainingDocuments objTraineeFile = objMapper.convertValue(inputMap.get("trainingfile"),
				TrainingDocuments.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.editTrainingFile(objTraineeFile, userInfo);
	}

	@PostMapping(value = "/viewAttachedTrainingFile")
	public ResponseEntity<Object> viewAttachedTrainingFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TrainingDocuments objTraineeFile = objMapper.convertValue(inputMap.get("trainingfile"),
				TrainingDocuments.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.viewAttachedTrainingFile(objTraineeFile, userInfo);

	}

	@PostMapping(value = "/editTraineeFile")
	public ResponseEntity<Object> editTraineeFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TraineeDocuments objTraineeFile = objMapper.convertValue(inputMap.get("traineefile"),
				TraineeDocuments.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.editTraineeFile(objTraineeFile, userInfo);
	}

	@PostMapping(value = "/viewAttachedTraineeFile")
	public ResponseEntity<Object> viewAttachedTraineeFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TraineeDocuments objTraineeFile = objMapper.convertValue(inputMap.get("traineefile"),
				TraineeDocuments.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.viewAttachedTraineeFile(objTraineeFile, userInfo);
	}

	@PostMapping(value = "/updateTrainingDoc")
	public ResponseEntity<Object> updateTrainingDoc(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.updateTrainingDoc(request, userInfo);
	}

	@PostMapping(value = "/updateTrainieeDoc")
	public ResponseEntity<Object> updateTraineeDoc(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return trainingUpdateService.updateTraineeDoc(request, userInfo);
	}

}