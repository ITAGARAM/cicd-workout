package com.agaramtech.qualis.batchcreation.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.batchcreation.model.Batchsample;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.UserInfo;

public interface BatchcreationDAO {

	public ResponseEntity<Object> getBatchcreation(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getRegistrationType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getRegistrationsubType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getTestBasedOnCombo(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getTestBasedInstrumentCat(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createBatchmaster(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getInstrument(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getBatchmaster(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getProductcategory(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getProduct(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getSample(int ntestcode,int nbatchmastercode,final UserInfo userInfo,int nregtypecode,int nregsubtypecode,
			int addSampleID,int napprovalconfigversioncode,int nprojectmastercode,boolean nneedmyjob) throws Exception;
	public ResponseEntity<Object> getActiveSelectedBatchmaster(int nbatchmastercode,final UserInfo userInfo,int ndesigntemplatemappingcode,int nsampletypecode) throws Exception;
	public ResponseEntity<Object> createSample(final int Seqno,final List<Batchsample> batchsample,
			final UserInfo userInfo,int ndesigntemplatemappingcode,int nregtypecode,int nregsubtypecode) throws Exception;
	public Map<String,Object> getBatchSampleCode(final int totalCount, final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getSampleTabDetails(int nbatchmastercode,final UserInfo userInfo,int ndesigntemplatemappingcode) throws Exception;
	public ResponseEntity<Object> deleteSample(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> initiateBatchcreation(final Map<String,Object>seqNoList,final List<Batchsample> batchSampleList,final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public Map<String, Object> seqNoGetforInitiate(final int totalCount, UserInfo userInfo,final Map<String,Object> inputMap) throws Exception;
	public ResponseEntity<Object> getActiveSelectedBatchmasterByID(final Map<String,Object> inputMap,final UserInfo userInfo) 
			throws Exception;
	public ResponseEntity<Object> updateBatchcreation(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> deleteBatchcreation(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public Map<String, Object> seqNoGetforComplete(final int totalCount, UserInfo userInfo,final Map<String,Object> inputMap) throws Exception;
	public ResponseEntity<Object> completeBatchcreation(final Map<String,Object>seqNoList,final List<Batchsample> batchSampleList,final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getBatchhistory(int nbatchmastercode,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getSection(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getBatchIQC(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getBatchMaterial(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getBatchMaterialInventory(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public Map<String, Object> validateAndInsertSeqNoIQCSampleData(Map<String, Object> inputMap) throws Exception;
	public Map<String, Object> createIQCSample(Map<String, Object> inputMap) throws Exception;
	public ResponseEntity<Object> getMaterialAvailQtyBasedOnInv(int materialInvCode,int sectionCode,final UserInfo userInfo,Map<String,Object> inputMap) throws Exception;
	public ResponseEntity<Object> getBatchIqcSampleAction(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getRegistrationSubSample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getSpecificationDetails(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> cancelIQCSampleAction(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	//public Map<String, Object> cancelSeqnoForMaterialInv(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> cancelBatch(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public Map<String, Object> seqNoGetforCancelBatch(UserInfo userInfo,final Map<String,Object> inputMap) throws Exception;
	public ResponseEntity<Object> getInstrumentID(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getBatchViewResult(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	//public ResponseEntity<Object> getBatchTAT(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getProductInstrument(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	//Added by Dhanushya RI for JIRA ID:ALPD-4999  Filter save detail --Start
	public ResponseEntity<Object> getBatchCreationFilter(Map<String, Object> inputMap,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createFilterName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	public List<FilterName> getFilterName(UserInfo userInfo) throws Exception;
	//End
}
