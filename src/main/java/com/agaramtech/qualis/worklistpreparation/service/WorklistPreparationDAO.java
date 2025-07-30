package com.agaramtech.qualis.worklistpreparation.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationType;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Section;
import com.agaramtech.qualis.worklistpreparation.model.Worklist;
import com.agaramtech.qualis.worklistpreparation.model.WorklistSample;

public interface WorklistPreparationDAO {

	public ResponseEntity<Object> getWorklistPreparation(final UserInfo userInfo,final String currentUIDate) throws Exception;


	public List<RegistrationType> getRegistrationType(final short nsampletypecode, final UserInfo userinfo) throws Exception;

	public List<SampleType> getSampleType(final UserInfo userinfo) throws Exception;

	public List<RegistrationSubType> getRegistrationSubType(final short nregtypecode, final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> getSectionAndTest(final UserInfo userInfo, final String currentUIDate,final Map<String, Object> inputMap) throws Exception;

	public List<Section> getSection(final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> createWorklist(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> refreshGetForAddComponent(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createWorklistCreation(final Integer seqNo, final List<WorklistSample> batchCompCreationList,final Worklist batchCreation, final UserInfo userInfo,final Integer ndesigntemplatemappingcode) throws Exception;

	public ResponseEntity<Object> getRegistrationTypeBySampleType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public Map<String,Object> getRegistrationsubTypeByRegType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getWorklistSample(final Integer ninstCode, final Integer ndesigntemplatemappingcode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSectionbaseTest(final UserInfo userInfo, final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getWorklistDetailFilter(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateWorklistDetail(final Map<String, Object> inputMap, final UserInfo userInfo)	throws Exception;

	public Worklist getEditSectionAndTest(final Integer ninstrumentcode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteWorklistSample(final WorklistSample inst,final Integer ndesigntemplatemappingcode ,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateWorklist(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;
	
	public Map<String,Object> getApprovalConfigVersion(Map<String, Object> inputMap,UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getSampleViewDetails(final Map<String, Object> inputMap) throws Exception ;
	
	public ResponseEntity<Object> getWorklisthistory(final UserInfo userInfo,final Integer nworklistcode) throws Exception;
	
	public Worklist validationStatus(final Integer ninstrumentcode, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> deleteWorklist(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public Map<String,Object> WorklistSampleAndHistory(final Integer nworklistcode,final Integer ndesigntemplatemappingcode,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> worklistReportGenerate(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public Worklist getActiveWorklistById(final Integer nworklistcode,final UserInfo userInfo) throws Exception ;
	
	public WorklistSample getActiveWorklistSampleById(final Integer nworklistsamplecode,final UserInfo userInfo) throws Exception ;
	
	public List<Map<String, Object>> getActiveWorklistSampleByMultipleId(final String ntransactiontestcode, final String npreregno, final String ntransactionsamplecode, final Integer nworklistcode,final UserInfo userInfo) throws Exception ;
	
	public Map<String, Object> getWorklistSelectSample(final Integer nworklistcode, final Integer ndesigntemplatemappingcode, final UserInfo userInfo, final Integer nneedSampleAndHistory) throws Exception;

	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap,final UserInfo userInfo)throws Exception ;

	public ResponseEntity<Object> getWorklistFilterDetails(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception ;
}
