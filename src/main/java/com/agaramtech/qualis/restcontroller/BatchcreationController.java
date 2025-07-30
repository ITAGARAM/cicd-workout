package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.batchcreation.model.Batchsample;
import com.agaramtech.qualis.batchcreation.service.BatchcreationService;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the batchcreation Service methods.
 */

@RestController
@RequestMapping("/batchcreation")

public class BatchcreationController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchcreationController.class);

	private RequestContext requestContext;
	private final BatchcreationService batchcreationService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param batchcreationService batchcreationService
	 */
	
	public BatchcreationController(RequestContext requestContext, BatchcreationService batchcreationService) {
		this.requestContext = requestContext;
		this.batchcreationService = batchcreationService;
	 }	
	
	
	@PostMapping(path ="/getBatchcreation")
	public ResponseEntity<Object> getBatchcreation(@RequestBody Map<String, Object> inputMap) throws Exception{	
		
			inputMap.put("nflag",1);
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),UserInfo.class);
			requestContext.setUserInfo(userInfo);
			LOGGER.info("UserInfo:"+ userInfo);		
			return batchcreationService.getBatchcreation(inputMap,userInfo);

	}
	
	@PostMapping(path ="/getRegistrationType")
	public ResponseEntity<Object> getRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception
	{
	

			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getRegistrationType(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getRegistrationsubType")
	public ResponseEntity<Object> getRegistrationsubType(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		

			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getRegistrationsubType(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getApprovalConfigVersion")
	public ResponseEntity<Object> getApprovalConfigVersion(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		

			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getApprovalConfigVersion(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getFilterStatus")
	public ResponseEntity<Object> getFilterStatus(@RequestBody Map<String, Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getFilterStatus(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getTestBasedOnCombo")
	public ResponseEntity<Object> getTestBasedOnCombo(@RequestBody Map<String,Object> inputMap) throws Exception
	{
		

			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getTestBasedOnCombo(inputMap,userInfo);

		
	}
	
	
	@PostMapping(path ="/getTestBasedInstrumentCat")
	public ResponseEntity<Object> getTestBasedInstrumentCat(@RequestBody Map<String,Object> inputMap) throws Exception
	{
		

			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getTestBasedInstrumentCat(inputMap,userInfo);

		
	}

	
	@PostMapping(path ="/createBatchmaster")
	public ResponseEntity<Object> createBatchmaster(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.createBatchmaster(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getInstrument")
	public ResponseEntity<Object> getInstrument(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getInstrument(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getBatchmaster")
	public ResponseEntity<Object> getBatchmaster(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getBatchmaster(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getProductcategory")
	public ResponseEntity<Object> getProductcategory(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getProductcategory(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getProduct")
	public ResponseEntity<Object> getProduct(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getProduct(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getSample")
	public ResponseEntity<Object> getSample(@RequestBody Map<String,Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		int ntestcode=(int) inputMap.get("ntestcode");
		int nbatchmastercode=(int) inputMap.get("nbatchmastercode");
		int nregtypecode=(int) inputMap.get("nregtypecode");
		int nregsubtypecode=(int) inputMap.get("nregsubtypecode");
		int addSampleID = (int) inputMap.get("addSampleID");
		int napprovalconfigversioncode = (int) inputMap.get("napprovalconfigversioncode");
		int nprojectmastercode = (int) inputMap.get("nprojectmastercode");
		Boolean nneedmyjob = (Boolean) inputMap.get("nneedmyjob");
		
		return batchcreationService.getSample(ntestcode,nbatchmastercode,userInfo,nregtypecode,nregsubtypecode,addSampleID,
				napprovalconfigversioncode,nprojectmastercode,nneedmyjob);
		
	}
	
	
	@PostMapping(path ="/getActiveSelectedBatchmaster")
	public ResponseEntity<Object> getActiveSelectedBatchmaster(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			int nbatchmastercode=Integer.parseInt(inputMap.get("nbatchmastercode").toString());
			int ndesigntemplatemappingcode =  (int) (inputMap.get("ndesigntemplatemappingcode"));
			int nsampletypecode = (int) inputMap.get("nsampletypecode");
			return batchcreationService.getActiveSelectedBatchmaster(nbatchmastercode,userInfo,ndesigntemplatemappingcode,nsampletypecode);

		
	}
	

	@PostMapping(path ="/createSample")
	public ResponseEntity<Object> createSample (@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			final List<Batchsample> batchSampleList = objMapper.convertValue(inputMap.get("sampleArray"), 
					new TypeReference<List<Batchsample>>(){});
			int ndesigntemplatemappingcode =  (int) inputMap.get("ndesigntemplatemappingcode");
			int nregtypecode =  (int) inputMap.get("nregtypecode");
			int nregsubtypecode =  (int) inputMap.get("nregsubtypecode");
			//List<Batchsample> batchSampleList = (List<Batchsample>) inputMap.get("sampleArray");
			requestContext.setUserInfo(userInfo);
			return batchcreationService.createSample(batchSampleList,userInfo,ndesigntemplatemappingcode,nregtypecode,nregsubtypecode);

		
	}
	

	@PostMapping(path ="/getSampleTabDetails")
	public ResponseEntity<Object> getSampleTabDetails(@RequestBody Map<String,Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
		int nbatchmastercode= (int) inputMap.get("nbatchmastercode");
		int ndesigntemplatemappingcode= (int) inputMap.get("ndesigntemplatemappingcode");
		requestContext.setUserInfo(userInfo);
		return batchcreationService.getSampleTabDetails(nbatchmastercode,userInfo,ndesigntemplatemappingcode);
	}
	
	
	@PostMapping(path ="/deleteSample")
	public ResponseEntity<Object> deleteSample(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.deleteSample(inputMap,userInfo);

		
	}
	
	
	@PostMapping(path ="/initiateBatchcreation")
	public ResponseEntity<Object> initiateBatchcreation(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			final List<Batchsample> samples = objMapper.convertValue(inputMap.get("batchsample"),new TypeReference<List<Batchsample>>(){});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.initiateBatchcreation(samples,inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getActiveSelectedBatchmasterByID")
	public ResponseEntity<Object> getActiveSelectedBatchmasterByID(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getActiveSelectedBatchmasterByID(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/updateBatchcreation")
	public ResponseEntity<Object> updateBatchcreation(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.updateBatchcreation(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/deleteBatchcreation")
	public ResponseEntity<Object> deleteBatchcreation(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.deleteBatchcreation(inputMap,userInfo);

		
	}
	
	
	@PostMapping(path ="/completeBatchcreation")
	public ResponseEntity<Object> completeBatchcreation(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			final List<Batchsample> samples = objMapper.convertValue(inputMap.get("samples"),new TypeReference<List<Batchsample>>(){});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.completeBatchcreation(samples,inputMap,userInfo);

		
	}
	
	
	@PostMapping(path ="/getBatchhistory")
	public ResponseEntity<Object> getBatchhistory(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			int nbatchmastercode=(int) inputMap.get("nbatchmastercode");
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getBatchhistory(nbatchmastercode,userInfo);

		
	}
	
	@PostMapping(path ="/getSection")
	public ResponseEntity<Object> getSection(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getSection(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getBatchIQC")
	public ResponseEntity<Object> getBatchIQC(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getBatchIQC(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getBatchMaterial")
	public ResponseEntity<Object> getBatchMaterial(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getBatchMaterial(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/getBatchMaterialInventory")
	public ResponseEntity<Object> getBatchMaterialInventory(@RequestBody Map<String,Object> inputMap) throws Exception
	{
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return batchcreationService.getBatchMaterialInventory(inputMap,userInfo);
	}
	
	
	@PostMapping(value = "/createIQCSample")
	public ResponseEntity<Object> createIQCSample(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return batchcreationService.createIQCSample(inputMap);

	}
	
	@PostMapping(value = "/getMaterialAvailQtyBasedOnInv")
	public ResponseEntity<Object> getMaterialAvailQtyBasedOnInv(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		int materialInvCode =  (int) inputMap.get("materialInvCode");
		int sectionCode = (int) inputMap.get("nsectioncode");
		return batchcreationService.getMaterialAvailQtyBasedOnInv(materialInvCode,sectionCode,userInfo,inputMap);

	}
	
	@PostMapping(value = "/getBatchIqcSampleAction")
	public ResponseEntity<Object> getBatchIqcSampleAction(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getBatchIqcSampleAction(inputMap,userInfo);

		
	}
	
	@PostMapping(value = "/getSpecificationDetails")
	public ResponseEntity<Object> getSpecificationDetails(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getSpecificationDetails(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/deleteIQCSample")
	public ResponseEntity<Object> deleteIQCSample(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.cancelIQCSampleAction(inputMap,userInfo);

		
	}
	
	@PostMapping(path ="/cancelBatch")
	public ResponseEntity<Object> cancelBatch(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.cancelBatch(inputMap,userInfo);

		
	}
	
	
	@PostMapping(path ="/getInstrumentID")
	public ResponseEntity<Object> getInstrumentID(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getInstrumentID(inputMap,userInfo);

		
	}
 
	@PostMapping(value = "/getBatchViewResult")
	public ResponseEntity<Object> getBatchViewResult(@RequestBody Map<String,Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getBatchViewResult(inputMap,userInfo);

		
	}

	

//	@PostMapping(value = "/getBatchTAT")
//	public ResponseEntity<Object> getBatchTAT(@RequestBody Map<String,Object> inputMap) throws Exception
//	{
//			ObjectMapper objMapper = new ObjectMapper();
//			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
//			requestContext.setUserInfo(userInfo);
//			return batchcreationService.getBatchTAT(inputMap,userInfo);
//
//		
//	}
	
	@PostMapping(path ="/getProductInstrument")
	public ResponseEntity<Object> getProductInstrument(@RequestBody Map<String,Object> inputMap) throws Exception
	   {
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return batchcreationService.getProductInstrument(inputMap,userInfo);

		
	  }
	//Added by Dhanushya RI for JIRA ID:ALPD-4999  Filter save detail --Start
			@PostMapping(path ="/getBatchCreationFilter")
			public ResponseEntity<Object> getBatchCreationFilter(@RequestBody Map<String, Object> inputMap) throws Exception
			{
				
					ObjectMapper objMapper = new ObjectMapper();
					final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),UserInfo.class);
					requestContext.setUserInfo(userInfo);
					return batchcreationService.getBatchCreationFilter(inputMap,userInfo);
		
			}
			@PostMapping(value = "/createFilterName")
			public ResponseEntity<Object> createFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
				ObjectMapper objMapper = new ObjectMapper();
				UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
				});
				requestContext.setUserInfo(userInfo);
				return batchcreationService.createFilterName(inputMap, userInfo);
			}
				
				@PostMapping(value = "/getFilterName")
				public ResponseEntity<Object> getFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
					ObjectMapper objMapper = new ObjectMapper();
					UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
					});
					requestContext.setUserInfo(userInfo);
					List<FilterName> outputMap = batchcreationService.getFilterName(userInfo);
					requestContext.setUserInfo(userInfo);
					return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
					
		}
		//End
	

}
