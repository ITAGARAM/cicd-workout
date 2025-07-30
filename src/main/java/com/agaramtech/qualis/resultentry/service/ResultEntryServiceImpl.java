package com.agaramtech.qualis.resultentry.service;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventoryTrans;
import com.agaramtech.qualis.registration.model.ResultCheckList;
import com.agaramtech.qualis.registration.model.ResultUsedInstrument;
import com.agaramtech.qualis.registration.model.ResultUsedMaterial;
import com.agaramtech.qualis.registration.model.ResultUsedTasks;
import com.agaramtech.qualis.registration.service.RegistrationDAO;

@Service
public class ResultEntryServiceImpl implements ResultEntryService{


	private final ResultEntryDAO resultEntryDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param resultEntryDAO ResultEntryDAO Interface
	 */
	public ResultEntryServiceImpl(ResultEntryDAO resultEntryDAO, CommonFunction commonFunction) {
		this.resultEntryDAO = resultEntryDAO;
		this.commonFunction = commonFunction;
	}

	
	@Override
	public ResponseEntity<Object> getResultEntryCombo(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getResultEntryCombo(inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getRegistrationType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getRegistrationType(inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getRegistrationsubType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getRegistrationsubType(inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap,UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getApprovalConfigVersion(inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getFilterStatus(inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getTestBasedOnCombo(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getTestBasedOnCombo(inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getResultEntryDetails(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getResultEntryDetails(inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getResultEntrySubSampleDetails(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getResultEntrySubSampleDetails(inputMap,userInfo);
		
	}
	
	@Override
	public ResponseEntity<Object> getTestbasedParameter(final String ntransactiontestcode, final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getTestbasedParameter(ntransactiontestcode,userInfo);
		
	}

	public ResponseEntity<Object> getResultEntryResults(final String ntransactiontestcode,UserInfo userInfo,final Map<String, Object> inputMap) throws Exception
	{
		
		return resultEntryDAO.getResultEntryResults(ntransactiontestcode,userInfo, inputMap);
		
	}
	
	@Override
	public ResponseEntity<Object> updateDefaultValue(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		Map<String,Object> seqNoList =  resultEntryDAO.seqNoGetforDefaultValue(inputMap,userInfo);
		
		if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(seqNoList.get("rtn"))) {
			inputMap.putAll(seqNoList);
			return resultEntryDAO.updateDefaultValue(inputMap,userInfo);
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage((String) seqNoList.get("rtn"),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		
	}

	@Override
	public ResponseEntity<Object> updateTestParameterResult(MultipartHttpServletRequest request, final UserInfo userInfo) throws Exception 
	{
		
		return resultEntryDAO.updateTestParameterResult(request, userInfo);
		
	}
	
	@Override
	public ResponseEntity<Object> createCompleteTest(final Map<String, Object> inputMap,UserInfo userInfo) throws Exception
	{
		
		Map<String,Object> seqNoList =  resultEntryDAO.seqNoGetforComplete(inputMap,userInfo);
		
		if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(seqNoList.get("rtn"))) {
			inputMap.putAll(seqNoList);
			return resultEntryDAO.createCompleteTest(inputMap,userInfo);
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage((String) seqNoList.get("rtn"),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		
	}

	@Override
	public ResponseEntity<Object> getResultChangeHistory(final String ntransactiontestcode, UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getResultChangeHistory(ntransactiontestcode,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getParameterComments(final int ntransactionresultcode,final String ntransactiontestcode,final int controlCode,
			final UserInfo userInfo,final int nneedReceivedInLab) throws Exception
	{
		
		return 	resultEntryDAO.getParameterComments(ntransactionresultcode,ntransactiontestcode,controlCode, userInfo,nneedReceivedInLab);
		
	}

	@Override
	public ResponseEntity<Object> updateParameterComments(final int ntransactionresultcode,final String ntransactiontestcode,final String sresultcomments,
			final int controlCode,final UserInfo userInfo,final int nregtypecode, final int nregsubtypecode,final int ndesigntemplatemappingcode,
			final int nneedReceivedInLab) throws Exception
	{
		
		return 	resultEntryDAO.updateParameterComments(ntransactionresultcode,ntransactiontestcode,sresultcomments,controlCode, userInfo,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,nneedReceivedInLab);

	}
	
	@Override
	public ResponseEntity<Object> getFormulaInputs(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return 	resultEntryDAO.getFormulaInputs(inputMap,userInfo);
		
	}

	//Instrument
	@Override
	public ResponseEntity<Object> getResultUsedInstrument(String ntransactiontestcode, int nresultusedinstrumentcode,final UserInfo userInfo) throws Exception {

		
		return 	resultEntryDAO.getResultUsedInstrument(ntransactiontestcode,nresultusedinstrumentcode,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getResultUsedInstrumentNameCombo(final int nflag,final int ninstrumentcatcode,final int ncalibrationRequired,
			final int ntestgrouptestcode,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getResultUsedInstrumentNameCombo(nflag,ninstrumentcatcode,ncalibrationRequired,ntestgrouptestcode,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getResultUsedInstrumentIdCombo(final int ninstrumentcatcode,final int ninstrumentnamecode,final int ncalibrationRequired,
			final int ntestgrouptestcode,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getResultUsedInstrumentIdCombo(ninstrumentcatcode,ninstrumentnamecode,ncalibrationRequired,ntestgrouptestcode,userInfo);
		
	}
	
	//ALPD-5032 added by Dhanushya RI,to pass jsonObject when add the instrument
	@Override
	public ResponseEntity<Object> createResultUsedInstrument(final ResultUsedInstrument objResultUsedInstrument,final int nregtypecode,final int nregsubtypecode,
			final int ndesigntemplatemappingcode,final String transactiontestcode,final JSONObject jsonData,final UserInfo userInfo) throws Exception
	{
		
		Map<String,Object> seqNoList =  resultEntryDAO.seqNoGetforResultUsedInstrument(objResultUsedInstrument,userInfo);
		
		if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(seqNoList.get("rtn"))) {
			return resultEntryDAO.createResultUsedInstrument(objResultUsedInstrument,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,transactiontestcode,jsonData,userInfo,(int)seqNoList.get("nresultusedinstrumentcode"));
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage((String) seqNoList.get("rtn"),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		
	}
	
	//ALPD-5032 added by Dhanushya RI,to pass jsonObject when edit the instrument
	@Override
	public ResponseEntity<Object> updateResultUsedInstrument(final ResultUsedInstrument objresultusedinstrument,final int nregtypecode,
			final int nregsubtypecode,final int ndesigntemplatemappingcode,final String ntransactiontestcode,final JSONObject jsonData,final UserInfo userInfo) throws Exception
	{
		
		return  resultEntryDAO.updateResultUsedInstrument(objresultusedinstrument,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,ntransactiontestcode,
				jsonData,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> deleteResultUsedInstrument(final int nresultusedinstrumentcode,final int nregtypecode,final int nregsubtypecode,
			final int ndesigntemplatemappingcode,final String ntransactiontestcode,final UserInfo userInfo) throws Exception
	{
		
		return 	resultEntryDAO.deleteResultUsedInstrument(nresultusedinstrumentcode,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,ntransactiontestcode,userInfo);

	}

	@Override
	public ResponseEntity<Object> getREMaterialTypeCombo(final int ntestgroupTestcode, final UserInfo userInfo,final short nsectioncode) throws Exception {

		
		return resultEntryDAO.getREMaterialTypeCombo(ntestgroupTestcode,userInfo,nsectioncode);
	}

	@Override
	public ResponseEntity<Object> getREMaterialCategoryByType(final int ntestgroupTestcode,final short materialTypeCode, final UserInfo userInfo,
			final short nsectioncode) throws Exception {

		return new ResponseEntity<>(resultEntryDAO.getREMaterialCategoryByType(ntestgroupTestcode,materialTypeCode, userInfo,nsectioncode),HttpStatus.OK);
		
	}

	@Override
	public ResponseEntity<Object> getREMaterialByCategory(final int ntestgroupTestcode, final short materialTypeCode,final int materiaCatCode,final int sectionCode,
			final UserInfo userInfo) throws Exception {

		return new ResponseEntity<>(resultEntryDAO.getREMaterialByCategory(ntestgroupTestcode,materialTypeCode, materiaCatCode, sectionCode, userInfo),HttpStatus.OK);
		
	}

	@Override
	public ResponseEntity<Object> getREMaterialInvertoryByMaterial(final int ntestgroupTestcode, final int materialCode, final int sectionCode,final UserInfo userInfo) 
			throws Exception {

		return new ResponseEntity<>(resultEntryDAO.getREMaterialInvertoryByMaterial(ntestgroupTestcode,materialCode, sectionCode, userInfo),HttpStatus.OK);
		
	}

	@Override
	public ResponseEntity<Object> getAvailableMaterialQuantity(final int ntestgroupTestcode, final int materialInvCode,final int sectionCode,final UserInfo userInfo)
			throws Exception {

		return resultEntryDAO.getAvailableMaterialQuantity(ntestgroupTestcode,materialInvCode, sectionCode, userInfo);
		
	}

	@Override
	public ResponseEntity<Object> createResultUsedMaterial(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		 
		return resultEntryDAO.createResultUsedMaterial(inputMap, userInfo);
		
	}
	
	@Override
	public ResponseEntity<Object> getResultUsedMaterial(final String ntransactiontestcode,final int nresultusedinstrumentcode,final UserInfo userInfo) throws Exception {

		return 	resultEntryDAO.getResultUsedMaterial(ntransactiontestcode,nresultusedinstrumentcode,userInfo);
		
	}
	
	@Override
	public ResponseEntity<Object> deleteResultUsedMaterial(final String ntransactiontestcode,final int nresultusedmaterialcode,Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception
	{
		
		return 	resultEntryDAO.deleteResultUsedMaterial(ntransactiontestcode,nresultusedmaterialcode,inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> updateResultUsedMaterial(final ResultUsedMaterial objresultusedinstrument,final MaterialInventoryTrans objMaterialInventoryTrans,
			Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return  resultEntryDAO.updateResultUsedMaterial(objresultusedinstrument,objMaterialInventoryTrans,inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> testInitiated(final Map<String, Object> inputMap,UserInfo userInfo) throws Exception
	{
		
		Map<String,Object> seqNoList =  resultEntryDAO.seqNoGetforTestStart(inputMap,userInfo);
		
		if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(seqNoList.get("rtn"))) {
			inputMap.putAll(seqNoList);
			return resultEntryDAO.testInitiated(inputMap,userInfo);
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage((String) seqNoList.get("rtn"),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		
	}

	@Override
	public ResponseEntity<Object> getResultUsedTask(String ntransactiontestcode, int nresultusedtaskcode,UserInfo userInfo) throws Exception {

		return 	resultEntryDAO.getResultUsedTask(ntransactiontestcode,nresultusedtaskcode,userInfo);
		
	}

	@Override
	@SuppressWarnings("unused")
	public ResponseEntity<Object> createResultUsedTasks(List<ResultUsedTasks> listTest, final String ntransactiontestcode,final int nregtypecode,
			final int nregsubtypecode,final int ndesigntemplatemappingcode,final String transactiontestcode,UserInfo userInfo) throws Exception
	{
		
		return 	resultEntryDAO.createResultUsedTasks(listTest,ntransactiontestcode,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,transactiontestcode, 
				userInfo);
		
	}
	
	@Override
	public ResponseEntity<Object> updateResultUsedTasks(final ResultUsedTasks objResultUsedTasks,final int nregtypecode,final int nregsubtypecode,
			final int ndesigntemplatemappingcode,final String ntransactiontestcode,final UserInfo userInfo) throws Exception
	{
		
		return 	resultEntryDAO.updateResultUsedTasks(objResultUsedTasks,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,ntransactiontestcode,userInfo);
	
	}
	
	@Override
	public ResponseEntity<Object> deleteResultUsedTasks(final int nresultusedtaskcode,final int nregtypecode,final int nregsubtypecode,
			final int ndesigntemplatemappingcode,final String ntransactiontestcode,final UserInfo userInfo) throws Exception
	{
		
		return 	resultEntryDAO.deleteResultUsedTasks(nresultusedtaskcode,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,ntransactiontestcode,userInfo);
	
	}
	
	@Override
	public  ResponseEntity<Object> getChecklistdesign(final int nchecklistversioncode,final String ntransactiontestcode,final int ntransactionresultcode,
			final int controlCode,final UserInfo userInfo,final int nneedReceivedInLab) throws Exception
	{
		
		return 	resultEntryDAO.getChecklistdesign(nchecklistversioncode,ntransactiontestcode,ntransactionresultcode,controlCode, userInfo,nneedReceivedInLab);
	
	}

	@Override
	public ResponseEntity<Object> createResultEntryChecklist(final ResultCheckList objResultCheckList,final int ntransactionresultcode,final int npreregno,
			final String ntransactiontestcode,final String transactiontestcode,final int controlCode,final UserInfo userInfo,final int nregtypecode,
			final int nregsubtypecode,final int ndesigntemplatemappingcode,final int nneedReceivedInLab) throws Exception
	{
		
		Map<String,Object> seqNoList =  resultEntryDAO.seqNoGetforResultEntryChecklist(objResultCheckList,ntransactiontestcode,controlCode,userInfo);
		
		if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(seqNoList.get("rtn"))) {
			return 	resultEntryDAO.createResultEntryChecklist(objResultCheckList,ntransactionresultcode,npreregno,ntransactiontestcode,transactiontestcode,controlCode, userInfo,nregtypecode,nregsubtypecode,(int)seqNoList.get("nseqnoresultchecklistcode"),ndesigntemplatemappingcode,nneedReceivedInLab);
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage((String) seqNoList.get("rtn"),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getAverageResult(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return resultEntryDAO.getAverageResult(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getApproveConfigVersionRegTemplateDesign(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return resultEntryDAO.getApproveConfigVersionRegTemplateDesign(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getPredefinedData(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return resultEntryDAO.getPredefinedData(inputMap,userInfo);
		
	}
	
	@Override
	
	public ResponseEntity<Object> updatePredefinedComments(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return resultEntryDAO.updatePredefinedComments(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getELNTestValidation(final int npreregno,final int ntransactiontestcode,final UserInfo userInfo) throws Exception
	{
		
		return 	resultEntryDAO.getELNTestValidation(npreregno,ntransactiontestcode,userInfo);
		
	}


	@Override
	public ResponseEntity<Object> getAdhocParamter(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return resultEntryDAO.getAdhocParamter(inputMap,userInfo);
		
	}
	

	@Override
	public ResponseEntity<Object> createAdhocParamter(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return resultEntryDAO.createAdhocParamter(inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getTestBasedBatchWorklist(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getTestBasedBatchWorklist(inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getConfigurationFilter(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getConfigurationFilter(inputMap,userInfo);
		
	}
	
	@Override
	public ResponseEntity<Object> getenforceResult(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.getenforceResult(inputMap,userInfo);
		
	}
	
	@Override
	public ResponseEntity<Object> updateEnforceResult(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.updateEnforceResult(inputMap,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> getResultEntryParameter(final int nallottedspeccode,final int ntestcode,final String ntransactiontestcode,UserInfo userInfo,final int nspecsampletypecode) throws Exception
	{
		
		return resultEntryDAO.getResultEntryParameter(nallottedspeccode,ntestcode,ntransactiontestcode,userInfo,nspecsampletypecode);
		
	}

	@Override
	public ResponseEntity<Object> updateMultiSampleTestParameterResult(MultipartHttpServletRequest request, final UserInfo userInfo) throws Exception 
	{
		
		return resultEntryDAO.updateMultiSampleTestParameterResult(request, userInfo);
		
	}
	
	@Override
	public ResponseEntity<Object> getResultEntrySpec(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return resultEntryDAO.getResultEntrySpec(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getResultEntryComponent(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return resultEntryDAO.getResultEntryComponent(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getAdhocTestParamter(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return resultEntryDAO.getAdhocTestParamter(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> createAdhocTestParamter( Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
		
		Map<String,Object> outputMap=resultEntryDAO.getCreateParameterSeqNo(inputMap, userInfo);
		final String returnStr = (String) outputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
		
		if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(returnStr)) {
			inputMap.putAll(outputMap);
			return resultEntryDAO.createAdhocTestParamter(inputMap,userInfo);
		}else {
			return new ResponseEntity<Object>(returnStr, HttpStatus.EXPECTATION_FAILED);	
		}
	}

	@Override
	public ResponseEntity<Object> getAnalysedUser(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return resultEntryDAO.getAnalysedUser(inputMap,userInfo);
		
	}

	//Added by sonia for ALPD-4084 on May 2 2024 Export action
	@Override
	public ResponseEntity<Object> getExportData(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
		
		return resultEntryDAO.getExportData(inputMap,userInfo);
		
	}
	
	//Added by sonia for ALPD-4084 on May 2 2024 Import action
	@Override
	public ResponseEntity<Object> getImportResultEntry(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception{
		
		return resultEntryDAO.getImportResultEntry(request,userInfo);
		
	}

	@Override
	public ResponseEntity<Object> updateFormulaCalculation(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		
		return resultEntryDAO.updateFormulaCalculation(inputMap,userInfo);
		
	}

	//ALPD-4156--Vignesh R(15-05-2024)-->Result Entry - Option to change section for the test(s)
	@Override
	public ResponseEntity<Object> getSectionChange(Map<String, Object> inputMap,UserInfo userInfo)  throws Exception{

		return resultEntryDAO.getSectionChange(inputMap,userInfo);
		
	}
	
	//ALPD-4156--Vignesh R(15-05-2024)-->Result Entry - Option to change section for the test(s)
	@Override
	public ResponseEntity<Object> updateSectionTest(Map<String, Object> inputMap,UserInfo userInfo)  throws Exception{

		return resultEntryDAO.updateSectionTest(inputMap,userInfo);
		
	}
	
	//Added by Dhanushya RI for JIRA ID:ALPD-4870  Filter save detail --Start
	@Override
	public ResponseEntity<Object> getResultEntryFilter(Map<String, Object> inputMap,UserInfo userInfo)  throws Exception{

		return resultEntryDAO.getResultEntryFilter(inputMap,userInfo);
		
	}
	
	@Override
	public ResponseEntity<Object> createFilterName(Map<String, Object> inputMap, UserInfo userInfo)	throws Exception {
		
		return resultEntryDAO.createFilterName(inputMap, userInfo);
		
	}	

	@Override
	public List<FilterName> getFilterName(UserInfo userInfo) throws Exception {
		
		return resultEntryDAO.getFilterName(userInfo);
	}
	//End
}
