package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.materialmanagement.model.MaterialInventoryTrans;
import com.agaramtech.qualis.registration.model.ResultCheckList;
import com.agaramtech.qualis.registration.model.ResultUsedInstrument;
import com.agaramtech.qualis.registration.model.ResultUsedMaterial;
import com.agaramtech.qualis.registration.model.ResultUsedTasks;
import com.agaramtech.qualis.resultentry.service.ResultEntryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping(value ="/resultentrybysample")
public class ResultEntryController{

//	private static final Log LOGGER = LogFactory.getLog(ResultEntryController.class);

	private RequestContext requestContext;
	private final ResultEntryService resultEntryService;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param resultEntryService ResultEntryService
	 */
	public ResultEntryController(RequestContext requestContext, ResultEntryService resultEntryService) {
		super();
		this.requestContext = requestContext;
		this.resultEntryService = resultEntryService;
	}

	@PostMapping(value ="/getResultEntryCombo")
	public ResponseEntity<Object> getResultEntryCombo(@RequestBody Map<String, Object> inputMap) throws Exception{	
		
			inputMap.put("nflag",1);
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.getResultEntryCombo(inputMap,userInfo);

	}
	
	
	@PostMapping(value ="/getRegistrationType")
	public ResponseEntity<Object> getRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception
	{
	
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.getRegistrationType(inputMap,userInfo);
	}
	
	
	@PostMapping(value ="/getRegistrationsubType")
	public ResponseEntity<Object> getRegistrationsubType(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.getRegistrationsubType(inputMap,userInfo);
	}
	
	
	@PostMapping(value ="/getApprovalConfigVersion")
	public ResponseEntity<Object> getApprovalConfigVersion(@RequestBody Map<String, Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.getApprovalConfigVersion(inputMap,userInfo);
	}
	
	
	@PostMapping(value ="/getJobStatus")
	public ResponseEntity<Object> getJobStatus(@RequestBody Map<String, Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			inputMap.put("njobstatuscode", 1);
			//userInfo.getSsitedatetime();
			return resultEntryService.getFilterStatus(inputMap, userInfo);
	}
	
	
	@PostMapping(value ="/getFilterStatus")
	public ResponseEntity<Object> getFilterStatus(@RequestBody Map<String, Object> inputMap) throws Exception
	{
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.getFilterStatus(inputMap,userInfo);
		
	}
	
	
	@PostMapping(value ="/getTestBasedOnCombo")
	public ResponseEntity<Object> getTestBasedOnCombo(@RequestBody Map<String,Object> inputMap) throws Exception
	{
	
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.getTestBasedOnCombo(inputMap,userInfo);
		
	}
	
	
	@PostMapping(value ="/getResultEntryDetails")
	public ResponseEntity<Object> getResultEntryDetails(@RequestBody Map<String, Object> inputMap) throws Exception
	{

			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.getResultEntryDetails(inputMap,userInfo);
		
	}
	
	
	@PostMapping(value ="/getResultEntrySubSampleDetails")
	public ResponseEntity<Object> getResultEntrySubSampleDetails(@RequestBody Map<String, Object> inputMap) throws Exception
	{
	
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.getResultEntrySubSampleDetails(inputMap,userInfo);
		
	}
	
	
	@PostMapping(value ="/getResultChangeHistory")
	public ResponseEntity<Object> getResultChangeHistory(@RequestBody Map<String, Object> inputMap)throws Exception
	{

			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			final String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
			return resultEntryService.getResultChangeHistory(ntransactiontestcode,userInfo);

	}
	

	@PostMapping(value ="/getTestbasedParameter")
	public ResponseEntity<Object> getTestbasedParameter(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			final String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
			return resultEntryService.getTestbasedParameter(ntransactiontestcode,userInfo);
		
	}
	
	
	@PostMapping(value ="/getResultEntryResults")
	public ResponseEntity<Object> getResultEntryResults(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			final String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
			return resultEntryService.getResultEntryResults(ntransactiontestcode,userInfo,inputMap);
		
	}

	

	@PostMapping(value ="/updateDefaultValue")
	public ResponseEntity<Object> updateDefaultValue(@RequestBody Map<String, Object> inputMap) throws Exception
	{	
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return resultEntryService.updateDefaultValue(inputMap,userInfo);	
	}


	@PostMapping(value  = "/updateTestParameterResult") 
	public ResponseEntity<Object> updateTestParameterResult(MultipartHttpServletRequest request) throws Exception 
	{
		
			final ObjectMapper objmapper = new ObjectMapper();
			final UserInfo userInfo = objmapper.readValue(request.getParameter("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.updateTestParameterResult(request, userInfo); 
		 

	}


	@PostMapping(value = "/completeTest") 
	public ResponseEntity<Object> completeTest(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			final ObjectMapper objmapper = new ObjectMapper();
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.createCompleteTest(inputMap, userInfo);

	}
	
	
	@PostMapping(value = "/testInitiated") 
	public ResponseEntity<Object> testInitiated(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			final ObjectMapper objmapper = new ObjectMapper();
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.testInitiated(inputMap, userInfo);
		 
	}
	
//	
//	@PostMapping(value = "/getTestMethodSource") 
//	public ResponseEntity<Object> getTestMethodSource(@RequestBody Map<String, Object> inputMap) throws Exception
//	{
//		
//			final ObjectMapper objmapper = new ObjectMapper();
//			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
//			final int ntransactiontestcode = (int) inputMap.get("ntransactiontestcode");
//			final int ntestcode = (int) inputMap.get("ntestcode");
//			final int controlCode = (int) inputMap.get("ncontrolcode");
//			requestContext.setUserInfo(userInfo);
//			return resultEntryService.getTestMethodSource(ntestcode,ntransactiontestcode, controlCode, userInfo);
//		 
//
//	}
//	
	
//	@PostMapping(value = "/updateTestMethodSource") 
//	public ResponseEntity<Object> updateTestMethodSource(@RequestBody Map<String, Object> inputMap) throws Exception
//	{
//		
//			final ObjectMapper objmapper = new ObjectMapper();
//			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
//			requestContext.setUserInfo(userInfo);
////			int ntransactiontestcode = (int) inputMap.get("ntransactiontestcode");
////			int nsourcecode = (int) inputMap.get("nsourcecode");
////			int nmethodcode = (int) inputMap.get("nmethodcode");
//			
//			return resultEntryService.updateTestMethodSource(inputMap, userInfo);
//		 
//
//	}
//	
	//Instrument

	@PostMapping(value ="/getResultUsedInstrument")
	public ResponseEntity<Object> getResultUsedInstrument(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		String ntransactiontestcode = "";
		int nresultusedinstrumentcode = -1;
		if(inputMap.containsKey("ntransactiontestcode"))
		{
			ntransactiontestcode = (String)inputMap.get("ntransactiontestcode");
		}
		if(inputMap.containsKey("nresultusedinstrumentcode"))
		{
			nresultusedinstrumentcode = (int)inputMap.get("nresultusedinstrumentcode");
		}
		return resultEntryService.getResultUsedInstrument(ntransactiontestcode,nresultusedinstrumentcode,userInfo);
		
	}


	@PostMapping(value ="/getResultUsedInstrumentNameCombo")
	public ResponseEntity<Object> getResultUsedInstrumentNameCombo(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		int nflag = 1;
		int ninstrumentcatcode = -1;
		int ncalibrationRequired = -1;
		int ntestgrouptestcode = -1;

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		if(inputMap.containsKey("nflag"))
		{
			nflag = (int)inputMap.get("nflag");
		}
		if(inputMap.containsKey("ninstrumentcatcode"))
		{
			ninstrumentcatcode = (int )inputMap.get("ninstrumentcatcode");
		}
		if(inputMap.containsKey("ncalibrationRequired")) {
			ncalibrationRequired= (int )inputMap.get("ncalibrationRequired");
		}
		if(inputMap.containsKey("ntestgrouptestcode"))
		{
			ntestgrouptestcode= (int )inputMap.get("ntestgrouptestcode");
		}
		return resultEntryService.getResultUsedInstrumentNameCombo(nflag,ninstrumentcatcode,ncalibrationRequired,ntestgrouptestcode,userInfo);
		
	}
	
	
	@PostMapping(value ="/getResultUsedInstrumentIdCombo")
	public ResponseEntity<Object> getResultUsedInstrumentIdCombo(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		int ninstrumentcatcode = -1;
		int ninstrumentnamecode = -1;
		int ncalibrationRequired = -1;
		int ntestgrouptestcode = -1;

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		
		if(inputMap.containsKey("ninstrumentcatcode"))
		{
			ninstrumentcatcode = (int )inputMap.get("ninstrumentcatcode");
		}
		if(inputMap.containsKey("ninstrumentnamecode"))
		{
			ninstrumentnamecode = (int )inputMap.get("ninstrumentnamecode");
		}
		if(inputMap.containsKey("ncalibrationRequired")) {
			ncalibrationRequired= (int )inputMap.get("ncalibrationRequired");
		}
		if(inputMap.containsKey("ntestgrouptestcode"))
		{
			ntestgrouptestcode= (int )inputMap.get("ntestgrouptestcode");
		}
		return resultEntryService.getResultUsedInstrumentIdCombo(ninstrumentcatcode,ninstrumentnamecode,ncalibrationRequired,ntestgrouptestcode,userInfo);
		
	}

	
	@PostMapping(value ="/createResultUsedInstrument")
	public ResponseEntity<Object> createResultUsedInstrument(@RequestBody Map<String, Object> inputMap) throws Exception
	{ 
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		final String transactiontestcode = (String) inputMap.get("transactiontestcode");
		final ResultUsedInstrument objresultusedInstrument = objMapper.convertValue(inputMap.get("ResultUsedInstrument"), new TypeReference <ResultUsedInstrument>() {});
		final int nregtypecode = (int) inputMap.get("nregtypecode");
		final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
		final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
		//ALPD-5032 added by Dhanushya RI,To pass jsonObject when add the instrument
        final JSONObject jsonObject = new JSONObject(inputMap.get("jsondata").toString());
		return resultEntryService.createResultUsedInstrument(objresultusedInstrument,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,transactiontestcode,jsonObject,userInfo);
		
	}


	@PostMapping(value ="/updateResultUsedInstrument")
	public ResponseEntity<Object> updateResultUsedInstrument(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
			final int nregtypecode = (int) inputMap.get("nregtypecode");
			final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
			final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
			final ResultUsedInstrument objresultusedInstrument = objMapper.convertValue(inputMap.get("ResultUsedInstrument"), new TypeReference <ResultUsedInstrument>() {});
			//ALPD-5032 added by Dhanushya RI,To pass jsonObject when edit the instrument
            final JSONObject jsonObject = new JSONObject(inputMap.get("jsondata").toString());

			return resultEntryService.updateResultUsedInstrument(objresultusedInstrument,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,ntransactiontestcode,jsonObject,userInfo);

		
	}

	@PostMapping(value ="/deleteResultUsedInstrument")
	public ResponseEntity<Object> deleteResultUsedInstrument(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			final int nresultusedinstrumentcode = (int)inputMap.get("nresultusedinstrumentcode");
			final String ntransactiontestcode = (String)inputMap.get("ntransactiontestcode");
			final int nregtypecode = (int) inputMap.get("nregtypecode");
			final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
			final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");

			return resultEntryService.deleteResultUsedInstrument(nresultusedinstrumentcode,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,ntransactiontestcode,userInfo);

		
	}
	

	//Material
	@PostMapping(value ="/getResultUsedMaterial")
	public ResponseEntity<Object> getResultUsedMaterial(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);

			final String ntransactiontestcode = (String)inputMap.get("ntransactiontestcode");
			final int nresultusedMaterialCode = inputMap .containsKey("nresultusedmaterialcode") ? (int)inputMap.get("nresultusedmaterialcode") :0;

			return resultEntryService.getResultUsedMaterial(ntransactiontestcode,nresultusedMaterialCode,userInfo);

		
	}
	

	@PostMapping(value ="/getResultUsedMaterialCombo")
	public ResponseEntity<Object> getREMaterialTypeCombo(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		short nsectioncode = Short.parseShort(inputMap.get("nsectioncode").toString());
		int ntestgroupTestcode = (int) inputMap.get("ntestgrouptestcode");
		return resultEntryService.getREMaterialTypeCombo(ntestgroupTestcode,userInfo,nsectioncode);

	}
	
	@PostMapping(value ="/getREMaterialCategoryByType")
	public ResponseEntity<Object> getREMaterialCategoryByType(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		short materialTypeCode = Short.parseShort(inputMap.get("nmaterialtypecode").toString());
		short nsectioncode = Short.parseShort(inputMap.get("nmaterialtypecode").toString());
		int ntestgroupTestcode = (int) inputMap.get("ntestgrouptestcode");
		return resultEntryService.getREMaterialCategoryByType(ntestgroupTestcode,materialTypeCode, userInfo,nsectioncode);

	}
	
	@PostMapping(value ="/getREMaterialByCategory")
	public ResponseEntity<Object> getREMaterialByCategory(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		short materialTypeCode = Short.parseShort(inputMap.get("nmaterialtypecode").toString());
		int materiaCatCode = (int)inputMap.get("nmaterialcatcode");
		int sectionCode = (int)inputMap.get("nsectioncode");
		int ntestgroupTestcode = (int) inputMap.get("ntestgrouptestcode");
		return resultEntryService.getREMaterialByCategory(ntestgroupTestcode,materialTypeCode, materiaCatCode, sectionCode, userInfo);

	}
	
	@PostMapping(value ="/getREMaterialInvertoryByMaterial")
	public ResponseEntity<Object> getREMaterialInvertoryByMaterial(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		int materialCode = (int)inputMap.get("nmaterialcode");
		int sectionCode = (int)inputMap.get("nsectioncode");
		int ntestgroupTestcode = (int) inputMap.get("ntestgrouptestcode");
		return resultEntryService.getREMaterialInvertoryByMaterial(ntestgroupTestcode,materialCode, sectionCode, userInfo);

	}
	@PostMapping(value ="/getAvailableMaterialQuantity")
	public ResponseEntity<Object> getAvailableMaterialQuantity(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		int materialInvCode = (int)inputMap.get("nmaterialinventorycode");
		int sectionCode = (int)inputMap.get("nsectioncode");
		int ntestgroupTestcode = (int) inputMap.get("ntestgrouptestcode");
		return resultEntryService.getAvailableMaterialQuantity(ntestgroupTestcode,materialInvCode, sectionCode, userInfo);

	}

	@PostMapping(value ="/createResultUsedMaterial")
	public ResponseEntity<Object> createResultUsedMaterial(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		return resultEntryService.createResultUsedMaterial(inputMap,userInfo);
	}

//	@PostMapping(value ="/getResultUsedMaterialEdit")
//	public ResponseEntity<Object> getResultUsedMaterialEdit(@RequestBody Map<String, Object> inputMap) throws Exception
//	{
//		try{
//			ObjectMapper objMapper = new ObjectMapper();
//			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),UserInfo.class);
//			final int nresultusedmaterialcode = (int) inputMap.get("nresultusedmaterialcode");
//
//			return ResultEntryService.getResultUsedMaterialEdit(inputMap,userInfo);
//
//		}catch(Exception e){
//			LOGGER.error(e.getMessage());
//			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
//
	
	@PostMapping(value ="/updateResultUsedMaterial")
	public ResponseEntity<Object> updateResultUsedMaterial(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			final ResultUsedMaterial objResultUsedMaterial=objMapper.convertValue(inputMap.get("ResultUsedMaterial"), new TypeReference<ResultUsedMaterial>(){});
			final MaterialInventoryTrans objMaterialInventoryTrans=objMapper.convertValue(inputMap.get("MaterialInventoryTrans"), new TypeReference<MaterialInventoryTrans>(){});
			return resultEntryService.updateResultUsedMaterial(objResultUsedMaterial,objMaterialInventoryTrans,inputMap,userInfo);

	}

	
	
	@PostMapping(value ="/deleteResultUsedMaterial")
	public ResponseEntity<Object> deleteResultUsedMaterial(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			final String ntransactiontestcode = (String)inputMap.get("ntransactiontestcode");
			final int nresultusedmaterialcode = (int)inputMap.get("nresultusedmaterialcode");
			return resultEntryService.deleteResultUsedMaterial(ntransactiontestcode,nresultusedmaterialcode,inputMap,userInfo);

	}

	
	//Task
		@PostMapping(value ="/getResultUsedTask")
		public ResponseEntity<Object> getResultUsedTask(@RequestBody Map<String, Object> inputMap) throws Exception
		{
			
				ObjectMapper objMapper = new ObjectMapper();
				final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
				requestContext.setUserInfo(userInfo);
				String ntransactiontestcode = "";
				int nresultusedtaskcode = -1;
				if(inputMap.containsKey("ntransactiontestcode"))
				{
					ntransactiontestcode = (String)inputMap.get("ntransactiontestcode");
				}
				if(inputMap.containsKey("nresultusedtaskcode"))
				{
					nresultusedtaskcode = (int)inputMap.get("nresultusedtaskcode");
				}
				return resultEntryService.getResultUsedTask(ntransactiontestcode,nresultusedtaskcode,userInfo);
			
		}

		
		@PostMapping(value ="/createResultUsedTasks") 
		public ResponseEntity<Object> createResultUsedTasks(@RequestBody Map<String, Object> inputMap) throws Exception
		{
			
				ObjectMapper objMapper = new ObjectMapper();
				final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
				requestContext.setUserInfo(userInfo);
				
				final String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
				final String transactiontestcode = (String) inputMap.get("ntransactiontestcode");
				final int nregtypecode = (int) inputMap.get("nregtypecode");
				final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
				final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");

				final List<ResultUsedTasks> listTest = objMapper.convertValue(inputMap.get("ResultUsedTasks"),
						new TypeReference<List<ResultUsedTasks>>() {
						});
				return resultEntryService.createResultUsedTasks(listTest,ntransactiontestcode,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,transactiontestcode,userInfo);

			
		}
//		
		@PostMapping(value ="/updateResultUsedTasks")
		public ResponseEntity<Object> updateResultUsedTasks(@RequestBody Map<String, Object> inputMap) throws Exception
		{
			
				ObjectMapper objMapper = new ObjectMapper();
				final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
				requestContext.setUserInfo(userInfo);
				final ResultUsedTasks objresultusedTask = objMapper.convertValue(inputMap.get("ResultUsedTasks"),ResultUsedTasks.class);
				final String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
				final int nregtypecode = (int) inputMap.get("nregtypecode");
				final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
				final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
				return resultEntryService.updateResultUsedTasks(objresultusedTask,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,ntransactiontestcode,userInfo);

			
		}
		
		
		@PostMapping(value ="/deleteResultUsedTasks")
		public ResponseEntity<Object> deleteResultUsedTasks(@RequestBody Map<String, Object> inputMap) throws Exception
		{
			
				ObjectMapper objMapper = new ObjectMapper();
				final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
				requestContext.setUserInfo(userInfo);
				final int nresultusedtaskcode = (int)inputMap.get("nresultusedtaskcode");
				final String ntransactiontestcode = (String)inputMap.get("ntransactiontestcode");
				final int nregtypecode = (int) inputMap.get("nregtypecode");
				final int nregsubtypecode = (int) inputMap.get("nregsubtypecode");
				final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");

				return resultEntryService.deleteResultUsedTasks(nresultusedtaskcode,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,ntransactiontestcode,userInfo);

			
		}
		
	
	@PostMapping(value ="/getParameterComments")
	public ResponseEntity<Object> getParameterComments(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			final int ntransactionresultcode = (int)inputMap.get("ntransactionresultcode");
			final String ntransactiontestcode = (String)inputMap.get("ntransactiontestcode");
			final int controlCode = (int) inputMap.get("ncontrolcode");
			final int nneedReceivedInLab = (int) inputMap.get("nneedReceivedInLab");
			return resultEntryService.getParameterComments(ntransactionresultcode,ntransactiontestcode,controlCode, userInfo,nneedReceivedInLab);

		
	}
	

	@PostMapping(value ="/updateParameterComments")
	public ResponseEntity<Object> updateParameterComments(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			final int ntransactionresultcode = (int)inputMap.get("ntransactionresultcode");
			final String ntransactiontestcode = (String)inputMap.get("ntransactiontestcode");
			final String sresultcomments = (String) inputMap.get("sresultcomment");
			final int nregtypecode = (int)inputMap.get("nregtypecode");
			final int nregsubtypecode = (int)inputMap.get("nregsubtypecode");
			final int controlCode = (int) inputMap.get("ncontrolcode");
			final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
			final int nneedReceivedInLab = (int) inputMap.get("nneedReceivedInLab");
			return resultEntryService.updateParameterComments(ntransactionresultcode,ntransactiontestcode,sresultcomments,controlCode, userInfo,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,nneedReceivedInLab);

		
	}
//		
		@PostMapping(value ="/getChecklistdesign")
		public  ResponseEntity<Object> getChecklistdesign(@RequestBody Map<String, Object> inputMap) throws Exception
		{
			
				ObjectMapper objMapper = new ObjectMapper();
				final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
				requestContext.setUserInfo(userInfo);
				final int nchecklistversioncode = (int)inputMap.get("nchecklistversioncode");
				final int ntransactionresultcode = (int)(inputMap.containsKey("ntransactionresultcode")? inputMap.get("ntransactionresultcode"):inputMap.get("napprovalparametercode"));
				final String ntransactiontestcode = (String)inputMap.get("ntransactiontestcode");
				final int controlCode = (int) inputMap.get("ncontrolcode");
				final int nneedReceivedInLab = (int) inputMap.get("nneedReceivedInLab");
				return resultEntryService.getChecklistdesign(nchecklistversioncode,ntransactiontestcode,ntransactionresultcode,controlCode, userInfo,nneedReceivedInLab);

			
		}
//		
		
		@PostMapping(value ="/createResultEntryChecklist")
		public ResponseEntity<Object> createResultEntryChecklist(@RequestBody Map<String, Object> inputMap) throws Exception
		{
			
				ObjectMapper objMapper = new ObjectMapper();
				final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
				requestContext.setUserInfo(userInfo);
				final int npreregno = (int)inputMap.get("npreregno");
				final int ntransactionresultcode = (int)inputMap.get("ntransactionresultcode");
				final String ntransactiontestcode = (String)inputMap.get("ntransactiontestcode");
				final String transactiontestcode = (String)inputMap.get("transactiontestcode");
				final int nregtypecode = (int)inputMap.get("nregtypecode");
				final int nregsubtypecode = (int)inputMap.get("nregsubtypecode");
				final int controlCode = (int) inputMap.get("ncontrolcode");
				final int ndesigntemplatemappingcode = (int) inputMap.get("ndesigntemplatemappingcode");
				final int nneedReceivedInLab = (int) inputMap.get("nneedReceivedInLab");
				//final List<ResultCheckList> lstResultCheckList = (List<ResultCheckList>) objMapper.convertValue(inputMap.get("ResultCheckList"),new TypeReference <List<ResultCheckList>>() {});
				
				final ResultCheckList objResultCheckList = objMapper.convertValue(inputMap.get("ResultCheckList"),ResultCheckList.class);
				return resultEntryService.createResultEntryChecklist(objResultCheckList,ntransactionresultcode,npreregno,ntransactiontestcode,transactiontestcode,controlCode, userInfo,nregtypecode,nregsubtypecode,ndesigntemplatemappingcode,nneedReceivedInLab);

			
		}
	
	@PostMapping(value ="/getFormulaInputs")
	public ResponseEntity<Object> getFormulaInputs(@RequestBody Map<String,Object> inputMap) throws Exception
	{
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getFormulaInputs(inputMap,userInfo);

	}
//		
//		
//		@PostMapping(value ="/getImportResultEntry")
//		public ResponseEntity<Object> getImportResultEntry(final MultipartHttpServletRequest request) throws Exception
//		{
//			
//			ObjectMapper objMapper = new ObjectMapper();
//		
//			final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), new TypeReference <UserInfo>() {});
//			requestContext.setUserInfo(userInfo);
//			return resultEntryService.getImportResultEntry(request,userInfo);
//
//		}
	
//		@PostMapping(value ="/getSampleApprovalHistory")
//		public ResponseEntity<Object> getSampleApprovalHistory(@RequestBody Map<String, Object> inputMap) throws Exception
//		{
//			
//			ObjectMapper objMapper = new ObjectMapper();
//			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
//			requestContext.setUserInfo(userInfo);
//			final String npreregno = (String) inputMap.get("npreregno");
//
//			return resultEntryService.getSampleApprovalHistory(npreregno,userInfo);
//			
//		}		
//		
//		@PostMapping(value ="/getMeanCalculationTestParameter")
//		public ResponseEntity<Object> getMeanCalculationTestParameter(@RequestBody Map<String, Object> inputMap) throws Exception
//		{
//			final ObjectMapper objMapper = new ObjectMapper();
//			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
//			requestContext.setUserInfo(userInfo);
//			final int npreRegNo = (Integer) inputMap.get("npreregno");
//			final int ntransactionResultCode = (Integer) inputMap.get("ntransactionresultcode");
//
//			return resultEntryService.getMeanCalculationTestParameter(npreRegNo, ntransactionResultCode, userInfo);
//			
//		}
	
	@PostMapping(value ="/getAverageResult")
	public ResponseEntity<Object> getAverageResult(@RequestBody Map<String,Object> inputMap) throws Exception
	{
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getAverageResult(inputMap,userInfo);

	}
	
	@PostMapping(value ="/getApproveConfigVersionRegTemplateDesign")
	public ResponseEntity<Object> getApproveConfigVersionRegTemplateDesign(@RequestBody Map<String,Object> inputMap) throws Exception
	{
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getApproveConfigVersionRegTemplateDesign(inputMap,userInfo);

	}
	@PostMapping(value ="/getPredefinedData")
	public ResponseEntity<Object> getPredefinedData(@RequestBody Map<String,Object> inputMap) throws Exception
	{
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getPredefinedData(inputMap,userInfo);

	}
	@PostMapping(value ="/updatePredefinedComments")
	public ResponseEntity<Object> updatePredefinedComments(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.updatePredefinedComments(inputMap,userInfo); 
	}

	

	@PostMapping(value ="/getELNTestValidation")
	public ResponseEntity<Object> getELNTestValidation(@RequestBody Map<String,Object> inputMap) throws Exception
	{
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
		final int npreregno = (int)inputMap.get("npreregno");
		final int ntransactiontestcode = (int)inputMap.get("ntransactiontestcode");
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getELNTestValidation(npreregno,ntransactiontestcode,userInfo);

	}
	


	@PostMapping(value ="/getAdhocParamter")
	public ResponseEntity<Object> getAdhocParamter(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getAdhocParamter(inputMap,userInfo); 
	}
	
	
	@PostMapping(value ="/createAdhocParamter")
	public ResponseEntity<Object> createAdhocParamter(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.createAdhocParamter(inputMap,userInfo); 
	}
	
	

	@PostMapping(value ="/getTestBasedBatchWorklist")
	public ResponseEntity<Object> getTestBasedBatchWorklist(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getTestBasedBatchWorklist(inputMap,userInfo); 
	}
	
	@PostMapping(value ="/getConfigurationFilter")
	public ResponseEntity<Object> getConfigurationFilter(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getConfigurationFilter(inputMap,userInfo); 
	}
	

	@PostMapping(value ="/getenforceResult")
	public ResponseEntity<Object> getenforceResult(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getenforceResult(inputMap,userInfo); 
	}
	
	
	@PostMapping(value ="/updateEnforceResult")
	public ResponseEntity<Object> updateEnforceResult(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.updateEnforceResult(inputMap,userInfo); 
	}
	
	@PostMapping(value ="/getResultEntryParameter")
	public ResponseEntity<Object> getResultEntryParameter(@RequestBody Map<String, Object> inputMap) throws Exception
	{
		
			ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			final String ntransactiontestcode = (String) inputMap.get("ntransactiontestcode");
			final int ntestcode =  (int) inputMap.get("ntestcode");
			final int nallottedspeccode= (int) inputMap.get("nallottedspeccode");
			final int nspecsampletypecode= (int) inputMap.get("nspecsampletypecode");
			return resultEntryService.getResultEntryParameter(nallottedspeccode,ntestcode,ntransactiontestcode,userInfo,nspecsampletypecode);

		
	}
	
	
	@PostMapping(value  = "/updateMultiSampleTestParameterResult") 
	public ResponseEntity<Object> updateMultiSampleTestParameterResult(MultipartHttpServletRequest request) throws Exception 
	{
		
			final ObjectMapper objmapper = new ObjectMapper();
			final UserInfo userInfo = objmapper.readValue(request.getParameter("userinfo"), new TypeReference <UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.updateMultiSampleTestParameterResult(request, userInfo); 

	}
	
	
	@PostMapping(value ="/getResultEntrySpec")
	public ResponseEntity<Object> getResultEntrySpec(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getResultEntrySpec(inputMap,userInfo); 
	}
	
	
	@PostMapping(value ="/getResultEntryComponent")
	public ResponseEntity<Object> getResultEntryComponent(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getResultEntryComponent(inputMap,userInfo); 
		
	}
	
	
	@PostMapping(value ="/getAdhocTestParamter")
	public ResponseEntity<Object> getAdhocTestParamter(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getAdhocTestParamter(inputMap,userInfo); 
		
	}
	
	@PostMapping(value ="/createAdhocTestParamter")
	public ResponseEntity<Object> createAdhocTestParamter(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.createAdhocTestParamter(inputMap,userInfo); 
		
	}
	
	@PostMapping(value ="/getAnalysedUser")
	public ResponseEntity<Object> getAnalysedUser(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getAnalysedUser(inputMap,userInfo); 
		
	}

	//Added by sonia for ALPD-4084 on May 2 2024 Export action
	@PostMapping(value ="/getExportData")
	public ResponseEntity<Object> getExportData(@RequestBody Map<String,Object> inputMap) throws Exception
	{ 
		ObjectMapper objMapper = new ObjectMapper(); 
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getExportData(inputMap,userInfo); 
		
	}
	
	//Added by sonia for ALPD-4084 on May 2 2024 Import action
	@PostMapping(value ="/getImportResultEntry")
	public ResponseEntity<Object> getImportResultEntry(MultipartHttpServletRequest request) throws Exception
	{ 
		
		ObjectMapper objMapper = new ObjectMapper(); 
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return resultEntryService.getImportResultEntry(request,userInfo); 
		
	}

	@PostMapping(value ="/updateFormulaCalculation")
	public ResponseEntity<Object> updateFormulaCalculation(@RequestBody Map<String, Object> inputMap) throws Exception
	{	
		
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return resultEntryService.updateFormulaCalculation(inputMap,userInfo);
		
	}

     //ALPD-4156--Vignesh R(15-05-2024)-->Result Entry - Option to change section for the test(s)	
		@PostMapping(value = "/getSectionChange")
		public ResponseEntity<Object> getSectionChange(@RequestBody Map<String, Object> inputMap) throws Exception {
			final ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.getSectionChange(inputMap,userInfo);
			
		}
		
    //ALPD-4156--Vignesh R(15-05-2024)-->Result Entry - Option to change section for the test(s)	
	@PostMapping(value = "/updateSectionTest")
	public ResponseEntity<Object> updateSectionTest(@RequestBody Map<String, Object> inputMap)	throws Exception {
			
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return resultEntryService.updateSectionTest(inputMap,userInfo);
			
		}
		
	
	//Added by Dhanushya RI for JIRA ID:ALPD-4870  Filter save detail --Start
		@PostMapping(value ="/getResultEntryFilter")
		public ResponseEntity<Object> getResultEntryFilter(@RequestBody Map<String, Object> inputMap) throws Exception
		{
			
				ObjectMapper objMapper = new ObjectMapper();
				final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
				requestContext.setUserInfo(userInfo);
				return resultEntryService.getResultEntryFilter(inputMap,userInfo);
	
		}
		
		
		@PostMapping(value = "/createFilterName")
		public ResponseEntity<Object> createFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
			
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			return resultEntryService.createFilterName(inputMap, userInfo);
			
		}
			
		@PostMapping(value = "/getFilterName")
		public ResponseEntity<Object> getFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
			ObjectMapper objMapper = new ObjectMapper();
			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
			requestContext.setUserInfo(userInfo);
			List<FilterName> outputMap = resultEntryService.getFilterName(userInfo);
			requestContext.setUserInfo(userInfo);
			return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
				
		}
	//End
		
}
