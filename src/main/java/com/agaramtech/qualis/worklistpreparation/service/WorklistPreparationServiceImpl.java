package com.agaramtech.qualis.worklistpreparation.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.worklistpreparation.model.Worklist;
import com.agaramtech.qualis.worklistpreparation.model.WorklistSample;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class WorklistPreparationServiceImpl implements WorklistPreparationService {

	private final WorklistPreparationDAO worklistPreparationDAO;
	private final CommonFunction commonFunction;	
	
	public WorklistPreparationServiceImpl(WorklistPreparationDAO worklistPreparationDAO, CommonFunction commonFunction) {
		this.worklistPreparationDAO = worklistPreparationDAO;
		this.commonFunction = commonFunction;
	}	
	
	public ResponseEntity<Object> getWorklistPreparation(final UserInfo userInfo, final String currentUIDate) throws Exception {
		return worklistPreparationDAO.getWorklistPreparation(userInfo, currentUIDate);
	}

	public ResponseEntity<Object> getSectionAndTest(final UserInfo userInfo, final String currentUIDate,final Map<String, Object> inputMap) throws Exception {
		return worklistPreparationDAO.getSectionAndTest(userInfo, currentUIDate,inputMap);
	}

	@Transactional
	public ResponseEntity<Object> createWorklist(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return worklistPreparationDAO.createWorklist(inputMap, userInfo);

	}

	public ResponseEntity<Object> refreshGetForAddComponent(final Map<String, Object> inputMap) throws Exception {
		return worklistPreparationDAO.refreshGetForAddComponent(inputMap);
	}

	@Transactional
	public ResponseEntity<Object> createWorklistCreation(final List<WorklistSample> batchCompCreationList,final Worklist batchCreation, final UserInfo userInfo,final Integer ndesigntemplatemappingcode) throws Exception {
		return worklistPreparationDAO.createWorklistCreation(1, batchCompCreationList, batchCreation, userInfo,ndesigntemplatemappingcode);
	}

	public ResponseEntity<Object> getRegistrationTypeBySampleType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return worklistPreparationDAO.getRegistrationTypeBySampleType(inputMap, userInfo);
	}

	public ResponseEntity<Object> getWorklistSample(final Integer ninstCode,final Integer ndesigntemplatemappingcode,final UserInfo userInfo) throws Exception {
		return worklistPreparationDAO.getWorklistSample(ninstCode, ndesigntemplatemappingcode,userInfo);
	}

	public ResponseEntity<Object> getSectionbaseTest(final UserInfo userInfo, final Map<String, Object> inputMap) throws Exception {
		return worklistPreparationDAO.getSectionbaseTest(userInfo, inputMap);
	}

	@Transactional
	public ResponseEntity<Object> getWorklistDetailFilter(final Map<String, Object> inputMap,final UserInfo userInfo)	throws Exception {
		return worklistPreparationDAO.getWorklistDetailFilter(inputMap, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> updateWorklistDetail(final Map<String, Object> inputMap, final UserInfo userInfo)	throws Exception {
		return worklistPreparationDAO.updateWorklistDetail(inputMap, userInfo);
	}

	public ResponseEntity<Object> getEditSectionAndTest(final Integer ninstCode, final UserInfo userInfo) throws Exception {
		final Worklist worklist = (Worklist) worklistPreparationDAO.validationStatus(ninstCode, userInfo);
		if(worklist.getNtransactionstatus()==Enumeration.TransactionStatus.PREPARED.gettransactionstatus())	{
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_WORKLISTALREADYPREPARED",userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}		
		final Worklist inst = (Worklist) worklistPreparationDAO.getEditSectionAndTest(ninstCode, userInfo);
		return new ResponseEntity<>(inst, HttpStatus.OK);
	}

	@Transactional
	public ResponseEntity<Object> deleteWorklistSample(final WorklistSample inst, final Integer ndesigntemplatemappingcode,final UserInfo userInfo) throws Exception {
		return worklistPreparationDAO.deleteWorklistSample(inst,ndesigntemplatemappingcode,userInfo);
	}

	@Transactional
	public ResponseEntity<Object> updateWorklist(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return worklistPreparationDAO.updateWorklist(inputMap, userInfo);
	}
	
	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return worklistPreparationDAO.getApprovalConfigVersionByRegSubType(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getSampleViewDetails(final Map<String, Object> inputMap) throws Exception{
		return worklistPreparationDAO.getSampleViewDetails(inputMap);
	}
	
	public ResponseEntity<Object> getWorklisthistory(final UserInfo userInfo,final Integer nworklistcode) throws Exception {
		return worklistPreparationDAO.getWorklisthistory(userInfo, nworklistcode);
	}

	@Transactional
	public ResponseEntity<Object> deleteWorklist(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return worklistPreparationDAO.deleteWorklist(inputMap, userInfo);
	}
	
	public ResponseEntity<Object> worklistReportGenerate(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception{
		return worklistPreparationDAO.worklistReportGenerate(inputMap,userInfo);
	}
	public Map<String,Object> getRegistrationsubTypeByRegType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return worklistPreparationDAO.getRegistrationsubTypeByRegType(inputMap,userInfo);
	}
		
	public Map<String, Object> getWorklistSelectSample(final Integer nworklistcode, final Integer ndesigntemplatemappingcode, final UserInfo userInfo, final Integer nneedSampleAndHistory) throws Exception {
		return worklistPreparationDAO.getWorklistSelectSample(nworklistcode,ndesigntemplatemappingcode,userInfo, nneedSampleAndHistory);
	}

	@Transactional
	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return worklistPreparationDAO.createFilterName(inputMap,userInfo);
	}

	public ResponseEntity<Object> getWorklistFilterDetails(final Map<String, Object> inputMap,final UserInfo userInfo)	throws Exception {
		return worklistPreparationDAO.getWorklistFilterDetails(inputMap,userInfo);
	}
}
