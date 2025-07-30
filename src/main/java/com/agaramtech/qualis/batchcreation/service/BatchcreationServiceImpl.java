package com.agaramtech.qualis.batchcreation.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.batchcreation.model.Batchsample;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;


@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class BatchcreationServiceImpl implements BatchcreationService {

	private final BatchcreationDAO batchCreationDAO;
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param testMasterDAO TestMasterDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	
	public BatchcreationServiceImpl(BatchcreationDAO batchCreationDAO, CommonFunction commonFunction) {
		this.batchCreationDAO = batchCreationDAO;
		this.commonFunction = commonFunction;
	}
	
	
	public ResponseEntity<Object> getBatchcreation(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getBatchcreation(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getRegistrationType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getRegistrationType(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getRegistrationsubType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getRegistrationsubType(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap,UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getApprovalConfigVersion(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getFilterStatus(inputMap,userInfo);
	}

	
	public ResponseEntity<Object> getTestBasedOnCombo(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getTestBasedOnCombo(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getTestBasedInstrumentCat(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getTestBasedInstrumentCat(inputMap,userInfo);
	}
	@Transactional
	public ResponseEntity<Object> createBatchmaster(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.createBatchmaster(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getInstrument(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getInstrument(inputMap,userInfo);
	}
	
	@Transactional
	public ResponseEntity<Object> getBatchmaster(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getBatchmaster(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getProductcategory(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getProductcategory(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getProduct(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getProduct(inputMap,userInfo);
	}
	
	public ResponseEntity<Object> getSample(int ntestcode,int nbatchmastercode,final UserInfo userInfo,int nregtypecode,
			int nregsubtypecode,int addSampleID,int napprovalconfigversioncode,int nprojectmastercode,boolean nneedmyjob) throws Exception
	{
		return batchCreationDAO.getSample(ntestcode,nbatchmastercode,userInfo,nregtypecode,nregsubtypecode,addSampleID,napprovalconfigversioncode,nprojectmastercode,nneedmyjob);
	}
	
	public ResponseEntity<Object> getActiveSelectedBatchmaster(int nbatchmastercode,final UserInfo userInfo,
			    int ndesigntemplatemappingcode,int nsampletypecode) throws Exception
	{
		return batchCreationDAO.getActiveSelectedBatchmaster(nbatchmastercode,userInfo,ndesigntemplatemappingcode,nsampletypecode);
	}
	@Transactional
	public ResponseEntity<Object> createSample(final List<Batchsample> batchsample,final UserInfo userInfo,
			int ndesigntemplatemappingcode,int nregtypecode,int nregsubtypecode) throws Exception
	
	{
		final Map<String,Object> seqnoMap = batchCreationDAO.getBatchSampleCode(batchsample.size(),userInfo);
		final String returnString = (String) seqnoMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
		if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(returnString)) {
			//batchCompCreation.setNbatchcompcode((int)seqnoMap.get("nbatchcompcode"));
			return batchCreationDAO.createSample((int)seqnoMap.get("nbatchsamplecode"),batchsample,userInfo,
					                ndesigntemplatemappingcode,nregtypecode,nregsubtypecode);
		}
		else {
			return new ResponseEntity<Object>(returnString,HttpStatus.EXPECTATION_FAILED) ;
		}
	}
	
	
	public ResponseEntity<Object> getSampleTabDetails(int nbatchmastercode,final UserInfo userInfo,int ndesigntemplatemappingcode) throws Exception
	{
		return batchCreationDAO.getSampleTabDetails(nbatchmastercode,userInfo,ndesigntemplatemappingcode);
	}
	
	@Transactional
	public ResponseEntity<Object> deleteSample(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.deleteSample(inputMap,userInfo);
	}
	@Transactional
	public ResponseEntity<Object> initiateBatchcreation(List<Batchsample> batchSampleList,final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		Map<String,Object> seqNoList =  batchCreationDAO.seqNoGetforInitiate(batchSampleList.size()-1,userInfo,inputMap);
		
		if(Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(seqNoList.get("rtn"))) {
			
			inputMap.putAll(seqNoList);
			return batchCreationDAO.initiateBatchcreation(seqNoList,batchSampleList,inputMap,userInfo);
			
		}else {
			
				return new ResponseEntity<>(commonFunction.getMultilingualMessage((String) seqNoList.get("rtn"),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				
		}
		
	}
	
	
	public ResponseEntity<Object> getActiveSelectedBatchmasterByID(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getActiveSelectedBatchmasterByID(inputMap,userInfo);
	}
	
	@Transactional
	public ResponseEntity<Object> updateBatchcreation(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.updateBatchcreation(inputMap,userInfo);
	}
	
	@Transactional
	public ResponseEntity<Object> deleteBatchcreation(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.deleteBatchcreation(inputMap,userInfo);
	}
	
	@Transactional
	public ResponseEntity<Object> completeBatchcreation(List<Batchsample> batchSampleList,final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		Map<String,Object> seqNoList =  batchCreationDAO.seqNoGetforComplete(batchSampleList.size()-1,userInfo,inputMap);
		if(Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(seqNoList.get("rtn"))) {
			inputMap.putAll(seqNoList);
			return batchCreationDAO.completeBatchcreation(seqNoList,batchSampleList,inputMap,userInfo);
		}else {
		   return new ResponseEntity<>(commonFunction.getMultilingualMessage((String) seqNoList.get("rtn"),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		
	}
	
	
   public ResponseEntity<Object> getBatchhistory(int nbatchmastercode,final UserInfo userInfo) throws Exception
	{
	   return batchCreationDAO.getBatchhistory(nbatchmastercode,userInfo);
	}
	
   public ResponseEntity<Object> getSection(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
	   return batchCreationDAO.getSection(inputMap,userInfo);
	}
   
   public ResponseEntity<Object> getBatchIQC(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
  	{
  	   return batchCreationDAO.getBatchIQC(inputMap,userInfo);
  	}
   
   public ResponseEntity<Object> getBatchMaterial(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
 	{
 	   return batchCreationDAO.getBatchMaterial(inputMap,userInfo);
 	}
	
   public ResponseEntity<Object> getBatchMaterialInventory(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
  	{
  	   return batchCreationDAO.getBatchMaterialInventory(inputMap,userInfo);
  	}
   
   @Override
   @Transactional
  	public ResponseEntity<Object> createIQCSample(Map<String, Object> inputMap) throws Exception {
	   
  		Map<String, Object> objmap1 = new HashMap<>();
  		Map<String, Object> objmap = batchCreationDAO.validateAndInsertSeqNoIQCSampleData(inputMap);
  		
  		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
  			
  			inputMap.putAll(objmap);
  			objmap1 = batchCreationDAO.createIQCSample(inputMap);
  			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus().
  					equals(objmap1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
  			return new ResponseEntity<>(objmap1, HttpStatus.OK);
  			} else {
  	  			
  				objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),objmap1.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
  	  			
  	  			return new ResponseEntity<>(objmap1, HttpStatus.EXPECTATION_FAILED);
  	  		}
  			
  		} else {
  			
  			objmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
  			
  			return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
  		}
  	}
   
   
   public ResponseEntity<Object> getMaterialAvailQtyBasedOnInv(int materialInvCode, int sectionCode,final UserInfo userInfo,Map<String,Object> inputMap) throws Exception
	{
	   return batchCreationDAO.getMaterialAvailQtyBasedOnInv(materialInvCode, sectionCode,userInfo,inputMap) ;
	}
   
   public ResponseEntity<Object> getBatchIqcSampleAction(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
	   return batchCreationDAO.getBatchIqcSampleAction(inputMap,userInfo);
	}
   
   public ResponseEntity<Object> getSpecificationDetails(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
  	{
  	   return batchCreationDAO.getSpecificationDetails(inputMap,userInfo);
  	}
   
   @Transactional
   public ResponseEntity<Object> cancelIQCSampleAction(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
			{
	   
	  		return batchCreationDAO.cancelIQCSampleAction(inputMap,userInfo);
		
			}
   @Transactional
   public ResponseEntity<Object> cancelBatch(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
  		Map<String, Object> objmap = batchCreationDAO.seqNoGetforCancelBatch(userInfo,inputMap);
  		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
  			 inputMap.putAll(objmap);
	         return batchCreationDAO.cancelBatch(inputMap,userInfo);  
	         } else {
	        objmap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
	   		return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
	   		}
		}

   public ResponseEntity<Object> getInstrumentID(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
			
	        return batchCreationDAO.getInstrumentID(inputMap,userInfo); 
	        
		}
   
   

   public ResponseEntity<Object> getBatchViewResult(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
			
	        return batchCreationDAO.getBatchViewResult(inputMap,userInfo); 
	        
		}
   
//   @SuppressWarnings("unchecked")
//   public ResponseEntity<Object> getBatchTAT(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception{
//			
//	        return batchCreationDAO.getBatchTAT(inputMap,userInfo); 
//	        
//		}
   public ResponseEntity<Object> getProductInstrument(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return batchCreationDAO.getProductInstrument(inputMap,userInfo);
	}
   @Transactional
 //Added by Dhanushya RI for JIRA ID:ALPD-4999  Filter save detail --Start
 	public ResponseEntity<Object> getBatchCreationFilter(Map<String, Object> inputMap,UserInfo userInfo)  throws Exception{

 			return batchCreationDAO.getBatchCreationFilter(inputMap,userInfo);
 		}
 	
 	@Transactional
 	public ResponseEntity<Object> createFilterName(Map<String, Object> inputMap, UserInfo userInfo)throws Exception {
 			return batchCreationDAO.createFilterName(inputMap, userInfo);
 		}	
 	
 	public List<FilterName> getFilterName(UserInfo userInfo) throws Exception {
 			return batchCreationDAO.getFilterName(userInfo);
 		}
 		//End
}
