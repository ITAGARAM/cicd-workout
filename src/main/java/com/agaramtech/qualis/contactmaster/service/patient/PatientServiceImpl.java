package com.agaramtech.qualis.contactmaster.service.patient;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.contactmaster.model.Patient;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class PatientServiceImpl implements PatientService {

	
	private final PatientDAO patientDAO;

	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param PatientDAO patientDAO  to hold the request
	 * @param CommonFunction commonFunction
	 */
	
	public PatientServiceImpl(PatientDAO patientDAO, CommonFunction commonFunction) {
		super();
		this.patientDAO = patientDAO;
		this.commonFunction = commonFunction;
	}

	public ResponseEntity<Object> getPatient(String spatientid, final UserInfo userInfo, String currentUIDate,int needdate)
			throws Exception {
		return patientDAO.getPatient(spatientid, userInfo, currentUIDate,needdate);
	}

	public ResponseEntity<Object> getActivePatientById(final String spatientcode, final UserInfo userInfo)
			throws Exception {

		final Patient patient = (Patient) patientDAO.getActivePatientById(spatientcode, userInfo);
		if (patient == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(patient, HttpStatus.OK);
		}
	}

	public ResponseEntity<Object> getGender(final UserInfo userInfo) throws Exception {
		return patientDAO.getGender(userInfo);
	}

//	public ResponseEntity<Object> getDiagnosticCase(final UserInfo userInfo) throws Exception {
//		return patientDAO.getDiagnosticCase(userInfo);
//	}

//	public ResponseEntity<Object> getCity(final UserInfo userInfo) throws Exception {
//		return patientDAO.getCity(userInfo);
//	}

	public ResponseEntity<Object> getCountry(final UserInfo userInfo) throws Exception {
		return patientDAO.getCountry(userInfo);
	}

	@Transactional
	public ResponseEntity<Object> createPatient(Patient patient, int needdate, UserInfo userInfo) throws Exception {
		return patientDAO.createPatient(patient,needdate, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> updatePatient(Patient patient, UserInfo userInfo) throws Exception {
		return patientDAO.updatePatient(patient, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> deletePatient(Patient patient, UserInfo userInfo) throws Exception {
		return patientDAO.deletePatient(patient, userInfo);
	}

////	public ResponseEntity<Object> getPatientHistory(final int npatientCode, UserInfo userInfo) throws Exception{
////		return patientDAO.getPatientHistory(npatientCode, userInfo);
////	}
//	
//	public ResponseEntity<Object> patientReportGenerate( Map<String, Object> inputMap,final UserInfo userInfo) throws Exception{
//		return patientDAO.patientReportGenerate(inputMap,userInfo);
//	}
	public ResponseEntity<Object> filterByPatient(String filterquery, UserInfo userInfo) throws Exception {
		return patientDAO.filterByPatient(filterquery, userInfo);
	}

	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception {
		return patientDAO.getRegion(userInfo);
	}

	public ResponseEntity<Object> getDistrict(final int nregioncode, final UserInfo userInfo) throws Exception {
		return patientDAO.getDistrict(nregioncode, userInfo);
	}

	public ResponseEntity<Object> getCity(final int ndistrictcode, final UserInfo userInfo) throws Exception {
		return patientDAO.getCity(ndistrictcode, userInfo);
	}

	public ResponseEntity<Object> getFilterByDate(String fromdate, String todate, UserInfo userInfo, String casetype)
			throws Exception {
		return patientDAO.getFilterByDate(fromdate, todate, userInfo, casetype);
	}

	@Transactional
	public ResponseEntity<Object> createFilterQuery(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return patientDAO.createFilterQuery(inputMap, userInfo);
	}

	public ResponseEntity<Object> getFilterQuery(final int npatientfiltercode, final UserInfo userInfo)
			throws Exception {
		return patientDAO.getFilterQuery(npatientfiltercode, userInfo);
	}

	public ResponseEntity<Object> getFilterQueryList(final int nfilterstatus, final UserInfo userInfo)
			throws Exception {
		return patientDAO.getFilterQueryList(nfilterstatus, userInfo);
	}
	
	public ResponseEntity<Object> getPatientHistory(String spatientid, final UserInfo userInfo)
			throws Exception {
		return patientDAO.getPatientHistory(spatientid, userInfo);
	}
	
	public ResponseEntity<Object> getPatientReportHistory(String spatientid, final UserInfo userInfo)
			throws Exception {
		return patientDAO.getPatientReportHistory(spatientid, userInfo);
	}
	
	public ResponseEntity<Object> viewPatientReportHistory(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return patientDAO.viewPatientReportHistory( inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updatePatientID(UserInfo userInfo) throws Exception{
		// TODO Auto-generated method stub
		return patientDAO.updatePatientID(userInfo);
	}

	
}
