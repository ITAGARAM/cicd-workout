package com.agaramtech.qualis.worklistpreparation.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.worklistpreparation.model.Worklist;
import com.agaramtech.qualis.worklistpreparation.model.WorklistSample;

public interface WorklistPreparationService {

	public ResponseEntity<Object> getWorklistPreparation(final UserInfo userInfo,final String currentUIDate) throws Exception;

	public ResponseEntity<Object> getSectionAndTest(final UserInfo userInfo,final String currentUIDate,final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createWorklist(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> refreshGetForAddComponent(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createWorklistCreation(final List<WorklistSample> batchCompCreationList,final Worklist batchCreation, final UserInfo userInfo,final Integer ndesigntemplatemappingcode) throws Exception;

	public ResponseEntity<Object> getRegistrationTypeBySampleType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getWorklistSample(final Integer ninstCode2,final Integer ndesigntemplatemappingcode,final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> getSectionbaseTest(final UserInfo userInfo,final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getWorklistDetailFilter(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateWorklistDetail(final Map<String, Object> inputMap, final UserInfo userInfo)	throws Exception;

	public ResponseEntity<Object> getEditSectionAndTest(final Integer ninstCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteWorklistSample(final WorklistSample inst,final Integer ndesigntemplatemappingcode,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateWorklist(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getSampleViewDetails(final Map<String, Object> inputMap) throws Exception ;
	
	public ResponseEntity<Object> getWorklisthistory(final UserInfo userInfo,final Integer nworklistcode) throws Exception;
	
	public ResponseEntity<Object> deleteWorklist(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> worklistReportGenerate(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public Map<String,Object> getRegistrationsubTypeByRegType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public Map<String, Object> getWorklistSelectSample(final Integer nworklistcode, final Integer ndesigntemplatemappingcode, final UserInfo userInfo, final Integer nneedSampleAndHistory) throws Exception ;

	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getWorklistFilterDetails(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;
}
