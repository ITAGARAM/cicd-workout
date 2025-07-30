package com.agaramtech.qualis.contactmaster.service.patient;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.contactmaster.model.Patient;
import com.agaramtech.qualis.global.UserInfo;


public interface PatientService {

	public ResponseEntity<Object> getPatient(String spatientid, final UserInfo userInfo, String currentUIDate,int needdate)
			throws Exception;

	public ResponseEntity<Object> getActivePatientById(final String spatientcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getGender(final UserInfo userInfo) throws Exception;
	
	//public ResponseEntity<Object> getDiagnosticCase(final UserInfo userInfo) throws Exception;

	// public ResponseEntity<Object> getCity(final UserInfo userInfo) throws
	// Exception;

	public ResponseEntity<Object> getCountry(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createPatient(Patient patient, int needdate, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updatePatient(Patient patient, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deletePatient(Patient patient, UserInfo userInfo) throws Exception;

//	
//	//public ResponseEntity<Object> getPatientHistory(final int npatientCode, UserInfo userInfo) throws Exception;
//	
//	public ResponseEntity<Object> patientReportGenerate( Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
//
	public ResponseEntity<Object> filterByPatient(String filterquery, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDistrict(final int nregioncode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getCity(final int ndistrictcode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getFilterByDate(String fromdate, String todate, final UserInfo userInfo,
			String casetype) throws Exception;

	public ResponseEntity<Object> createFilterQuery(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getFilterQuery(final int npatientfiltercode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getFilterQueryList(final int nfilterstatus, final UserInfo objUserInfo)
			throws Exception;
	
	public ResponseEntity<Object> getPatientHistory(String spatientid, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getPatientReportHistory(String spatientid, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> viewPatientReportHistory(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;


	public ResponseEntity<Object> updatePatientID(UserInfo userInfo) throws Exception;
	
	

}
