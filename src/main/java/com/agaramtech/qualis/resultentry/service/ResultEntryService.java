package com.agaramtech.qualis.resultentry.service;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventoryTrans;
import com.agaramtech.qualis.registration.model.ResultCheckList;
import com.agaramtech.qualis.registration.model.ResultUsedInstrument;
import com.agaramtech.qualis.registration.model.ResultUsedMaterial;
import com.agaramtech.qualis.registration.model.ResultUsedTasks;




public interface ResultEntryService {
	
	public ResponseEntity<Object> getResultEntryCombo(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getRegistrationType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationsubType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap,UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTestBasedOnCombo(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getResultEntryDetails(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getResultEntrySubSampleDetails(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTestbasedParameter(final String ntransactiontestcode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getResultEntryResults(String ntransactiontestcode,UserInfo userInfo,Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateDefaultValue(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> updateTestParameterResult(MultipartHttpServletRequest request, final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<Object> createCompleteTest(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getResultChangeHistory(final String ntransactiontestcode, UserInfo userInfo)throws Exception;
	
	public ResponseEntity<Object> getParameterComments(final int ntransactionresultcode,final String ntransactiontestcode,final int controlCode,final UserInfo userInfo,final int nneedReceivedInLab) throws Exception;
	
	public ResponseEntity<Object> updateParameterComments(final int ntransactionresultcode,final String ntransactiontestcode,final String sresultcomments,final int controlCode,final UserInfo userInfo, final int nregtypecode, final int nregsubtypecode,final int ndesigntemplatemappingcode,final int nneedReceivedInLab) throws Exception;

	public ResponseEntity<Object> getFormulaInputs(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getResultUsedInstrument(final String ntransactiontestcode, final int nresultusedinstrumentcode,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getResultUsedInstrumentNameCombo(final int nflag,final int ninstrumentcatcode,final int ncalibrationRequired,final int ntestgrouptestcode,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getResultUsedInstrumentIdCombo(final int ninstrumentcatcode,final int ninstrumentnamecode,final int ncalibrationRequired,final int ntestgrouptestcode,final UserInfo userInfo) throws Exception;
	
	//ALPD-5032 added by Dhanushya RI,To pass jsonobject when add the instrument
	public ResponseEntity<Object> createResultUsedInstrument(final ResultUsedInstrument objResultUsedInstrument,final int nregtypecode,final int nregsubtypecode,final int ndesigntemplatemappingcode,final String transactiontestcode,final JSONObject jsonData,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> updateResultUsedInstrument(final ResultUsedInstrument objresultusedinstrument,final int nregtypecode,final int nregsubtypecode,final int ndesigntemplatemappingcode,final String ntransactiontestcode,final JSONObject jsonData,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> deleteResultUsedInstrument(final int nresultusedinstrumentcode,final int nregtypecode,final int nregsubtypecode,final int ndesigntemplatemappingcode,final String ntransactiontestcode,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getREMaterialTypeCombo(final int ntestgroupTestcode,final UserInfo userInfo,final short nsectioncode) throws Exception;

	public ResponseEntity<Object> getREMaterialCategoryByType(final int ntestgroupTestcode,final short materialTypeCode, final UserInfo userInfo,final short nsectioncode) throws Exception;

	public ResponseEntity<Object> getREMaterialByCategory(final int ntestgroupTestcode,final short materialTypeCode, final int materiaCatCode, final int sectionCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getREMaterialInvertoryByMaterial(final int ntestgroupTestcode,final int materialCode,final int sectionCode,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getAvailableMaterialQuantity(final int ntestgroupTestcode,final int materialInvCode, final int sectionCode, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> createResultUsedMaterial(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getResultUsedMaterial(final String ntransactiontestcode, final int nresultusedMaterialCode,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> deleteResultUsedMaterial(final String ntransactiontestcode,final int nresultusedmaterialcode,Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> updateResultUsedMaterial(final ResultUsedMaterial objresultusedinstrument,final MaterialInventoryTrans objMaterialInventoryTrans,Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> testInitiated(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getResultUsedTask(final String ntransactiontestcode, final int nresultusedtaskcode,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> createResultUsedTasks(List<ResultUsedTasks> listTest, final String ntransactiontestcode,final int nregtypecode,final int nregsubtypecode,final int ndesigntemplatemappingcode,final String transactiontestcode, UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> updateResultUsedTasks(ResultUsedTasks objResultUsedTasks,final int nregtypecode,final int nregsubtypecode,final int ndesigntemplatemappingcode,final String ntransactiontestcode,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> deleteResultUsedTasks(final int nresultusedtaskcode,final int nregtypecode,final int nregsubtypecode,final int ndesigntemplatemappingcode,final String ntransactiontestcode,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> createResultEntryChecklist(ResultCheckList objResultCheckList,final int ntransactionresultcode,final int npreregno,final String ntransactiontestcode,final String transactiontestcode,final int controlCode,final UserInfo userInfo, final int nregtypecode, final int nregsubtypecode,final int ndesigntemplatemappingcode,final int nneedReceivedInLab) throws Exception;
	
	public ResponseEntity<Object> getChecklistdesign(final int nchecklistversioncode,final String ntransactiontestcode,final int ntransactionresultcode,final int controlCode,final UserInfo UserInfo,final int nneedReceivedInLab) throws Exception;
	
	public ResponseEntity<Object> getAverageResult(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getApproveConfigVersionRegTemplateDesign(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getPredefinedData(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updatePredefinedComments(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getELNTestValidation(final int npreregno,final int ntransactiontestcode,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getAdhocParamter(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> createAdhocParamter(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getTestBasedBatchWorklist(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getConfigurationFilter(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getenforceResult(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateEnforceResult(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getResultEntryParameter(final int nallottedspeccode,final int ntestcode,String ntransactiontestcode,UserInfo userInfo,final int nspecsampletypecode) throws Exception;

	public ResponseEntity<Object> updateMultiSampleTestParameterResult(MultipartHttpServletRequest request, final UserInfo objUserInfo) throws Exception;
	
	public ResponseEntity<Object> getResultEntrySpec(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getResultEntryComponent(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getAdhocTestParamter(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createAdhocTestParamter(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getAnalysedUser(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	//Added by sonia for ALPD-4084 on May 2 2024 Export action
	public ResponseEntity<Object> getExportData(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	//Added by sonia for ALPD-4084 on May 2 2024 Import action
	public ResponseEntity<Object> getImportResultEntry(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateFormulaCalculation(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;

	//ALPD-4156--Vignesh R(15-05-2024)-->Result Entry - Option to change section for the test(s)
	public ResponseEntity<Object> getSectionChange(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
		
	//ALPD-4156--Vignesh R(15-05-2024)-->Result Entry - Option to change section for the test(s)
	public ResponseEntity<Object> updateSectionTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	//Added by Dhanushya RI for JIRA ID:ALPD-4870  Filter save detail --Start
	public ResponseEntity<Object> getResultEntryFilter(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
    
	public ResponseEntity<Object> createFilterName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	public List<FilterName> getFilterName(UserInfo userInfo) throws Exception;
	//End
}
