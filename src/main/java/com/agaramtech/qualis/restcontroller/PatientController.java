package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.contactmaster.model.Patient;
import com.agaramtech.qualis.contactmaster.service.patient.PatientService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Patient Service, UserMultiRole and UserMultiDeputy service
 * methods.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 16-04- 2025
 */
@RestController
@RequestMapping("/patient")
public class PatientController {

	@SuppressWarnings("unused")
	private static final Log LOGGER = LogFactory.getLog(PatientController.class);

	private RequestContext requestContext;
	private final PatientService patientService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param PatientService patientService
	 */
	public PatientController(RequestContext requestContext, PatientService patientService) {
		super();
		this.requestContext = requestContext;
		this.patientService = patientService;
	}

	@PostMapping(value = "/getPatient" )
	public ResponseEntity<Object> getPatient(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		int needdate;
		String spatientid = null;
		if (inputMap.get("spatientid") != null) {
			spatientid = (String) inputMap.get("spatientid");
		}
		if (inputMap.containsKey("noneedfilter")) {
			needdate=(int) inputMap.get("noneedfilter");
		}else {
			needdate=-1;
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return patientService.getPatient(spatientid, userInfo, currentUIDate,needdate);
	}

	@PostMapping(value = "/getActivePatientById" )
	public ResponseEntity<Object> getActivePatientById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final String spatientid = (String) inputMap.get("spatientid");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return patientService.getActivePatientById(spatientid, userInfo);

	}

//
	@PostMapping(value = "/getGender" )
	public ResponseEntity<Object> getGender(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return patientService.getGender(userInfo);
	}
	
	
//	@PostMapping(value = "/getDiagnosticCase" )
//	public ResponseEntity<Object> getDiagnosticCase(@RequestBody Map<String, Object> inputMap) throws Exception {
//
//		final ObjectMapper objmapper = new ObjectMapper();
//		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//
//		requestContext.setUserInfo(userInfo);
//		return patientService.getDiagnosticCase(userInfo);
//	}

//	@PostMapping(value = "/getCity" )
//	public ResponseEntity<Object> getCity(@RequestBody Map<String, Object> inputMap) throws Exception{
//
//			final ObjectMapper objmapper = new ObjectMapper();
//			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//			
//			requestContext.setUserInfo(userInfo);
//			return patientService.getCity(userInfo);
//	}
//	
	@PostMapping(value = "/getCountry" )
	public ResponseEntity<Object> getCountry(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return patientService.getCountry(userInfo);
	}

//	
	@PostMapping(value = "/createPatient" )
	public ResponseEntity<Object> createPatient(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		int needdate;
		if (inputMap.containsKey("noneedfilter")) {
			needdate=(int) inputMap.get("noneedfilter");
		}else {
			needdate=-1;
		}
		final Patient patient = objMapper.convertValue(inputMap.get("patient"), Patient.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return patientService.createPatient(patient,needdate, userInfo);
	}

	@PostMapping(value = "/updatePatient" )
	public ResponseEntity<Object> updateInstrumentCalibration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Patient patient = objMapper.convertValue(inputMap.get("patient"), Patient.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return patientService.updatePatient(patient, userInfo);
	}

	@PostMapping(value = "/deletePatient" )
	public ResponseEntity<Object> deletePatient(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Patient patient = objMapper.convertValue(inputMap.get("patient"), Patient.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return patientService.deletePatient(patient, userInfo);
	}

//	@PostMapping(value = "/patientReportGenerate" )
//	public ResponseEntity<Object> patientReportGenerate(@RequestBody Map<String, Object> inputMap) throws Exception {
//	
//			ObjectMapper objMapper = new ObjectMapper();
//			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//			requestContext.setUserInfo(userInfo);
//			return patientService.patientReportGenerate(inputMap,userInfo);
//			
//		
//	}
	@PostMapping(value = "/filterByPatient" )
	public ResponseEntity<Object> filterByPatient(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final String filterquery = objMapper.convertValue(inputMap.get("filterquery"), String.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return patientService.filterByPatient(filterquery, userInfo);
	}

	@PostMapping(value = "/getRegion" )
	public ResponseEntity<Object> getRegion(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return patientService.getRegion(userInfo);
	}

	@PostMapping(value = "/getDistrict" )
	public ResponseEntity<Object> getDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nregioncode = (int) inputMap.get("nregioncode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return patientService.getDistrict(nregioncode, userInfo);
	}

	@PostMapping(value = "/getCity" )
	public ResponseEntity<Object> getCity(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int ndistrictcode = (int) inputMap.get("ndistrictcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return patientService.getCity(ndistrictcode, userInfo);
	}

	@PostMapping(value = "/getFilterByDate" )
	public ResponseEntity<Object> getFilterByDate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final String fromdate = (String) inputMap.get("formdate");
		final String todate = (String) inputMap.get("todate");
		final String casetype = (String) inputMap.get("casetype");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return patientService.getFilterByDate(fromdate, todate, userInfo, casetype);
	}

	@PostMapping(value = "/createFilterQuery" )
	public ResponseEntity<Object> createFilterQuery(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return patientService.createFilterQuery(inputMap, userInfo);
	}

	@PostMapping(value = "/getFilterQuery" )
	public ResponseEntity<Object> getFilterQuery(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final int npatientfiltercode = (int) inputMap.get("npatientfiltercode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return patientService.getFilterQuery(npatientfiltercode, userInfo);
	}

	@PostMapping(value = "/getFilterQueryList" )
	public ResponseEntity<Object> getFilterQueryList(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nfilterstatus = (int) inputMap.get("nfilterstatus");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return patientService.getFilterQueryList(nfilterstatus, userInfo);
	}
	
	@PostMapping(value = "/getPatientHistory" )
	public ResponseEntity<Object> getPatientHistory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final String spatientid = (String) inputMap.get("spatientid");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return patientService.getPatientHistory(spatientid, userInfo);
	}
	
	@PostMapping(value = "/getPatientReportHistory" )
	public ResponseEntity<Object> getPatientReportHistory(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final String spatientid = (String) inputMap.get("spatientid");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return patientService.getPatientReportHistory(spatientid, userInfo);
	}
	
	@PostMapping(value="/viewPatientReportHistory")
	public ResponseEntity<Object> viewPatientReportHistory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return patientService.viewPatientReportHistory(inputMap,userInfo);
	}
	
	
	@PostMapping(value="/updatePatientID")
	public ResponseEntity<Object> updatePatientID(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		//requestContext.setUserInfo(userInfo);
		return patientService.updatePatientID(userInfo);
	}
}
