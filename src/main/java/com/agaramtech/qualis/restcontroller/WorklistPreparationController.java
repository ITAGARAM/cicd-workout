package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.worklistpreparation.model.Worklist;
import com.agaramtech.qualis.worklistpreparation.model.WorklistSample;
import com.agaramtech.qualis.worklistpreparation.service.WorklistPreparationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;


@RestController
@RequestMapping("/worklist")
public class WorklistPreparationController {

	private RequestContext requestContext;
	private final WorklistPreparationService worklistPreparationService;
	
	
	public WorklistPreparationController(RequestContext requestContext, WorklistPreparationService worklistPreparationService) {
		super();
		this.requestContext = requestContext;
		this.worklistPreparationService = worklistPreparationService;
	}
	
	@PostMapping(value="/getWorklist")
	public ResponseEntity<Object> getWorklistPreparation(@RequestBody Map<String,Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final String currentUIDate = (String) inputMap.get("currentdate");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getWorklistPreparation(userInfo,currentUIDate);		
	}	
	
	@PostMapping(value="/getSectionAndTest")
	public ResponseEntity<Object> getSectionAndTest(@RequestBody Map<String,Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final String currentUIDate = (String) inputMap.get("currentdate");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getSectionAndTest(userInfo,currentUIDate,inputMap);
	}
	
	@PostMapping(value = "/createWorklist")
	public ResponseEntity<Object> createWorklist(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.createWorklist(inputMap, userInfo);
	}
	 
	@PostMapping(value = "/refreshGetForAddComponent")
	public ResponseEntity<Object> refreshGetForAddComponent(@RequestBody Map<String, Object> inputMap)throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.refreshGetForAddComponent(inputMap);			
	}		
		
	@PostMapping(value = "/createWorklistCreation")
	public ResponseEntity<Object> createWorklistCreation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Worklist batchcreation = objMapper.convertValue(inputMap.get("worklistcreation"), Worklist.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final List<WorklistSample> batchCompList = objMapper.convertValue(inputMap.get("worklistcompcreationlist"), new TypeReference<List<WorklistSample>>(){});
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.createWorklistCreation(batchCompList, batchcreation, userInfo,(Integer) inputMap.get("ndesigntemplatemappingcode"));
	}		
		
	@PostMapping(path ="/getRegistrationTypeBySampleType")
	public ResponseEntity<Object> getRegistrationTypeBySampleType(@RequestBody Map<String, Object> inputMap) throws Exception{	
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getRegistrationTypeBySampleType(inputMap,userInfo);
	}

	@PostMapping(value = "/getWorklistSample")
	public ResponseEntity<Object> getWorklistSample(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();	
		Integer ninstCode = null;
		if (inputMap.get("nworklistcode") != null) {
			ninstCode = (Integer) inputMap.get("nworklistcode");	
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getWorklistSample(ninstCode,(Integer) inputMap.get("ndesigntemplatemappingcode"),userInfo);
	}		
		
	@PostMapping(value="/getSectionbaseTest")
	public ResponseEntity<Object> getSectionbaseTest(@RequestBody Map<String,Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getSectionbaseTest(userInfo,inputMap);
	}		
		
	@PostMapping(value = "/getWorklistDetailFilter")
	public ResponseEntity<Object> getWorklistDetailFilter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();	
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getWorklistDetailFilter(inputMap,userInfo);
	}
		
	@PostMapping(value = "/prepareWorklist")
	public ResponseEntity<Object> updateWorklistDetail(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();	
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.updateWorklistDetail(inputMap,userInfo);
	}

	@PostMapping(value = "/getEditSectionAndTest")
	public ResponseEntity<Object> getActiveInstrumentById(@RequestBody Map<String, Object> inputMap) throws Exception  {
		final ObjectMapper objmapper = new ObjectMapper();
		final Integer ninstCode = (Integer) inputMap.get("nworklistcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getEditSectionAndTest(ninstCode, userInfo);
	}		
		
	@PostMapping(value = "/deleteWorklistSample")
	public ResponseEntity<Object> deleteInstrument(@RequestBody Map<String, Object> inputMap)  throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final WorklistSample inst = objmapper.convertValue(inputMap.get("worklistsample"), WorklistSample.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.deleteWorklistSample(inst,(Integer) inputMap.get("ndesigntemplatemappingcode"),userInfo);
	}	

	@PostMapping(value = "/updateWorklist")
	public ResponseEntity<Object> updateWorklist(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.updateWorklist(inputMap, userInfo);
	}
		 
	@PostMapping(path ="/getApprovalConfigVersionByRegSubType")
	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(@RequestBody Map<String, Object> inputMap) throws Exception{	
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getApprovalConfigVersionByRegSubType(inputMap,userInfo);
	}
	
	@PostMapping(value = "/getSampleViewDetails")
	public ResponseEntity<Object> getSampleViewDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getSampleViewDetails(inputMap);
	}
	
	@PostMapping(value="/getWorklisthistory")
	public ResponseEntity<Object> getWorklisthistory(@RequestBody Map<String,Object> inputMap) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference <UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		final Integer nworklistcode = (Integer) inputMap.get("nworklistcode");	
		return worklistPreparationService.getWorklisthistory(userInfo,nworklistcode);
				
	}			
			
	@PostMapping(value = "/deleteWorklist")
	public ResponseEntity<Object> deleteWorklist(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.deleteWorklist(inputMap, userInfo);
	}	 
			 
	@PostMapping(value = "/worklistReportGenerate")
	public ResponseEntity<Object> worklistReportGenerate(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.worklistReportGenerate(inputMap, userInfo);
	}				
				
	@PostMapping(path ="/getRegistrationsubTypeByRegType")
	public Map<String,Object> getRegistrationsubTypeByRegType(@RequestBody Map<String, Object> inputMap) throws Exception{	
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getRegistrationsubTypeByRegType(inputMap,userInfo);
	}				
				
	@PostMapping(value = "/getWorklistSelectSample")
	public Map<String, Object> getWorklistSelectSample(@RequestBody Map<String, Object> inputMap)throws Exception {			
		final ObjectMapper objmapper = new ObjectMapper();
		final Integer nworklistcode = (Integer) inputMap.get("nworklistcode");
		final Integer ndesigntemplatemappingcode = (Integer) inputMap.get("ndesigntemplatemappingcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Integer nneedSampleAndHistory = (Integer) inputMap.get("nneedsampleandhistory");
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getWorklistSelectSample(nworklistcode, ndesigntemplatemappingcode, userInfo, nneedSampleAndHistory);
	}				
	
	@PostMapping(value = "/createFilterName")
	public ResponseEntity<Object> createFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.createFilterName(inputMap, userInfo);
	}				
				
	@PostMapping(value = "/getWorklistFilterDetails")
	public ResponseEntity<Object> getWorklistFilterDetails(@RequestBody Map<String, Object> inputMap) throws Exception	{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return worklistPreparationService.getWorklistFilterDetails(inputMap, userInfo);
			
	}

}
