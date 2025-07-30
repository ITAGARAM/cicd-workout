package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.model.ReportDesignConfig;
import com.agaramtech.qualis.reports.model.ReportDetails;
import com.agaramtech.qualis.reports.model.ReportImages;
import com.agaramtech.qualis.reports.model.ReportMaster;
import com.agaramtech.qualis.reports.model.ReportParameterMapping;
import com.agaramtech.qualis.reports.model.ReportType;
import com.agaramtech.qualis.reports.model.SubReportDetails;
import com.agaramtech.qualis.reports.service.controlbasedreport.ControlBasedReportService;
import com.agaramtech.qualis.reports.service.reportconfig.ReportMasterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


@RestController
@RequestMapping("/reportconfig")
public class ReportConfigController {

	private RequestContext requestContext;
	private final ReportMasterService reportService;
	private final ControlBasedReportService controlbasedreportservice;
	
	public ReportConfigController(ReportMasterService reportService,ControlBasedReportService controlbasedreportservice,RequestContext requestContext) {
		super();
		this.requestContext=requestContext;
		this.reportService=reportService;
		this.controlbasedreportservice=controlbasedreportservice;
		
	}
	
	@PostMapping(value = "/getReportDesigner")
	public ResponseEntity<Object> getReportDesigner(@RequestBody Map<String, Object> inputMap) throws Exception{

		final ObjectMapper objmapper = new ObjectMapper();		
		Integer nreportCode = null;
		if (inputMap.get("nreportcode") != null) {
			nreportCode = (Integer) inputMap.get("nreportcode");	
		}
		String nreportTypeCode = null;
		if (inputMap.get("nreporttypecode") != null) {
			nreportTypeCode = (String) inputMap.get("nreporttypecode");	
		}	
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reportService.getReportMaster(nreportCode, nreportTypeCode, userInfo);
		
	}
	
	@PostMapping(value = "/getReportMasterComboData")
	public ResponseEntity<Object> getReportMasterComboData(@RequestBody Map<String, Object> inputMap) throws Exception{

		final ObjectMapper objmapper = new ObjectMapper();		
		final Integer nreportCode = (Integer) inputMap.get("nreportcode");  
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ReportType reportType = objmapper.convertValue(inputMap.get("filterreporttype"), ReportType.class);
		requestContext.setUserInfo(userInfo);
		return reportService.getReportMasterComboData(nreportCode, userInfo, reportType);
		
	}
	
	@PostMapping(value = "/createReportDesigner")
	public ResponseEntity<Object> createReportDesigner(MultipartHttpServletRequest request) throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo objUserInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(objUserInfo);
		return reportService.createReportDesigner(request, objUserInfo);
		
	}
	
	@PostMapping(value = "/createReportDetails")
	public ResponseEntity<Object> createReportDetails(MultipartHttpServletRequest request) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReportMaster reportMaster = objMapper.readValue(request.getParameter("reportmaster"), new TypeReference<ReportMaster>() {});
		final ReportDetails reportDetails = objMapper.readValue(request.getParameter("reportdetails"), new TypeReference<ReportDetails>() {});
		
		return reportService.createReportDetails(request, reportDetails, reportMaster, reportMaster.getNreportcode(),userInfo);
			
		
	}
	
	@PostMapping(value = "/updateReportMaster")
	public ResponseEntity<Object> updateReportMaster(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReportMaster reportMaster = objMapper.convertValue(inputMap.get("reportmaster"), ReportMaster.class);
		return reportService.updateReportMaster(reportMaster, userInfo);
		
	}
	
	@PostMapping(value = "/updateReportDetails")
	public ResponseEntity<Object> updateReportDetails(MultipartHttpServletRequest request) throws Exception{
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final UserInfo objUserInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(objUserInfo);
		return reportService.updateReportDetails(request, objUserInfo);
		
	}
	
	@PostMapping(value = "/getReportDetailComboData")
	public ResponseEntity<Object> getReportDetailComboData(@RequestBody Map<String, Object> inputMap) throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();		
		objMapper.registerModule(new JavaTimeModule());

		final Integer nreportDetailCode = (Integer) inputMap.get("nreportdetailcode");  
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReportMaster reportMaster = objMapper.convertValue(inputMap.get("reportmaster"), ReportMaster.class);
					
		return reportService.getReportDetailComboData(nreportDetailCode, reportMaster, userInfo);
		
	}
	
	@PostMapping(value = "/getActiveReportDetailById")
	public ResponseEntity<Object> getActiveReportDetailById(@RequestBody Map<String, Object> inputMap) throws Exception{

		final ObjectMapper objmapper = new ObjectMapper();		
		Integer nreportDetailCode = null;
		if (inputMap.get("nreportdetailcode") != null) {
			nreportDetailCode = (Integer) inputMap.get("nreportdetailcode");	
		}
				
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reportService.getActiveReportDetailById(nreportDetailCode, userInfo);
		
	}
	
	@PostMapping(value = "/getReportDetail")
	public ResponseEntity<Object> getReportDetail(@RequestBody Map<String, Object> inputMap) throws Exception{

		final ObjectMapper objmapper = new ObjectMapper();		
		Integer nreportDetailCode = null;
		if (inputMap.get("nreportdetailcode") != null) {
			nreportDetailCode = (Integer) inputMap.get("nreportdetailcode");	
		}
				
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reportService.getReportDetail(nreportDetailCode, userInfo);
		
	}
	
	@PostMapping(value = "/getReportAddDesignComboData")
	public ResponseEntity<Object> getReportAddDesignComboData(@RequestBody Map<String, Object> inputMap) throws Exception{

		final ObjectMapper objmapper = new ObjectMapper();	
		final int nreportDetailCode = (Integer) inputMap.get("nreportdetailcode");	
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reportService.getReportAddDesignComboData(nreportDetailCode, userInfo);
		 
	}
	

	@PostMapping(value = "/configReportDesignParameter")
	public ResponseEntity<Object> createReportDesignParameter(@RequestBody Map<String, Object> inputMap)throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final List<ReportDesignConfig> configList = objMapper.convertValue(inputMap.get("reportdesignconfig"), 
							new TypeReference<List<ReportDesignConfig>>(){});
		return reportService.createReportDesignParameter(configList, userInfo);
		
	}
	
	@PostMapping(value = "/approveReportVersion")
	public ResponseEntity<Object> approveReportVersion(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReportDetails reportDetails = objMapper.convertValue(inputMap.get("reportdetails"),ReportDetails.class);
		
		return reportService.approveReportVersion(reportDetails, userInfo);
		
	}
	
	@PostMapping(value = "/deleteReportMaster")
	public ResponseEntity<Object> deleteReportMaster(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReportMaster reportMaster = objMapper.convertValue(inputMap.get("reportmaster"), 
												ReportMaster.class);
		
		return reportService.deleteReportMaster(reportMaster, userInfo);
	
	}
	
	@PostMapping(value = "/deleteReportDetails")
	public ResponseEntity<Object> deleteReportDetails(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReportDetails reportDetails = objMapper.convertValue(inputMap.get("reportdetails"), 
												ReportDetails.class);
		
		return reportService.deleteReportDetails(reportDetails, userInfo);
		
	}
	
	@PostMapping(value = "/getReportDesignConfig")
	public ResponseEntity<Object> getReportDesignConfig(@RequestBody Map<String, Object> inputMap) throws Exception{

		final ObjectMapper objmapper = new ObjectMapper();	
		final int nreportDetailCode = (Integer) inputMap.get("nreportdetailcode");	
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		return reportService.getReportDesignConfig(nreportDetailCode, userInfo);
		
		
	}
	
	@PostMapping(value = "/createReportParameterMapping")
	public ResponseEntity<Object> createReportParameterMapping(@RequestBody Map<String, Object> inputMap)throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		objMapper.registerModule(new JavaTimeModule());
		requestContext.setUserInfo(userInfo);
		final List<ReportParameterMapping> configList = objMapper.convertValue(inputMap.get("reportparametermapping"), 
							new TypeReference<List<ReportParameterMapping>>(){});
		final ReportDetails reportDetails = objMapper.convertValue(inputMap.get("reportdetails"), 
				ReportDetails.class);
		return reportService.createReportParameterMapping(reportDetails, configList, userInfo);
		
	}
	

	@PostMapping(value = "/getReportParameterMappingComboData")
	public ResponseEntity<Object> getReportParameterMappingComboData(@RequestBody Map<String, Object> inputMap) throws Exception{

		final ObjectMapper objmapper = new ObjectMapper();	
		final int nreportDetailCode = (Integer) inputMap.get("nreportdetailcode");	
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		return reportService.getReportParameterMappingComboData(nreportDetailCode, userInfo);
		
	}
	
	@PostMapping(value = "/toggleReportStatus")
	public ResponseEntity<Object> updateReportStatus(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReportMaster reportMaster = objMapper.convertValue(inputMap.get("reportmaster"), ReportMaster.class);
		
		return reportService.updateReportStatus(reportMaster, userInfo);
		
	}
	
	@PostMapping(value = "/getReportRegistrationSubType")
	public ResponseEntity<Object> getReportRegistrationSubType(@RequestBody Map<String, Object> inputMap)throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nregTypeCode = (Integer) inputMap.get("nregtypecode");	
		
		return reportService.getRegistrationSubTypeByRegType(nregTypeCode, userInfo);
		
	}
	
	@PostMapping(value = "/createSubReportDetails")
	public ResponseEntity<Object> createSubReportDetails(MultipartHttpServletRequest request) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final SubReportDetails subReportDetails = objMapper.readValue(request.getParameter("subreportdetails"), new TypeReference<SubReportDetails>() {});
		
		return reportService.createSubReportDetails(request, subReportDetails, userInfo);
			
		
	}
	
	@PostMapping(value = "/deleteSubReportDetails")
	public ResponseEntity<Object> deleteSubReportDetails(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final SubReportDetails reportDetails = objMapper.convertValue(inputMap.get("subreportdetails"), 
												SubReportDetails.class);
		
		return reportService.deleteSubReportDetails(reportDetails, userInfo);
		
	}
	
	@PostMapping(value = "/createReportImages")
	public ResponseEntity<Object> createReportImages(MultipartHttpServletRequest request) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReportImages reportImages = objMapper.readValue(request.getParameter("reportimages"), new TypeReference<ReportImages>() {});
		
		return reportService.createReportImages(request, reportImages, userInfo);
		
	}
	
	@PostMapping(value = "/deleteReportImages")
	public ResponseEntity<Object> deleteReportImages(@RequestBody Map<String, Object> inputMap) throws Exception{
		
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final ReportImages reportImages = objMapper.convertValue(inputMap.get("reportimages"), 
												ReportImages.class);
		
		return reportService.deleteReportImages(reportImages, userInfo);
		
	}
	
	@PostMapping(value = "/getReportSubType")
	public ResponseEntity<Object> getReportSubType(@RequestBody Map<String, Object> inputMap)throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nreportTypeCode = (Integer) inputMap.get("nreporttypecode");	
		final int nsampleTypecode = (Integer) inputMap.get("nsampletypecode");	

		return reportService.getReportSubType(nreportTypeCode,nsampleTypecode,userInfo);
		
	}
	
	@PostMapping(value = "/getControlButton")
	public ResponseEntity<Object> getControlButton(@RequestBody Map<String, Object> inputMap)throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();
		final int nformcode = (Integer) inputMap.get("nformcode");	
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		return reportService.getControlButton(nformcode,  userInfo);
		
	}
	
	@PostMapping(value = "/getRegistrationtypeForSample")
	public ResponseEntity<Object> getRegistrationtypeForSample(@RequestBody Map<String, Object> inputMap)throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();
		final int nsampletypecode = (Integer) inputMap.get("nsampletypecode");	
		final int nreporttypecode = (Integer) inputMap.get("nreporttypecode");	
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		return reportService.getRegistrationtypeForSample(nsampletypecode,userInfo,nreporttypecode);
		 
	}
	
	@PostMapping(value = "/getReportRegSubTypeApproveConfigVersion")
	public ResponseEntity<Object> getReportRegSubTypeApproveConfigVersion(@RequestBody Map<String, Object> inputMap)throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();
		final int nregtypecode = (Integer) inputMap.get("nregtypecode");	
		final int nregsubtypecode = (Integer) inputMap.get("nregsubtypecode");	
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		return reportService.getReportRegSubTypeApproveConfigVersion(nregtypecode,nregsubtypecode,userInfo);
		 
	}
	
	@PostMapping(value = "/getReportSampleType")
	public ResponseEntity<Object> getReportSampleType(@RequestBody Map<String, Object> inputMap)throws Exception{

		final ObjectMapper objMapper = new ObjectMapper();
		final int nreporttypecode = (Integer) inputMap.get("nreporttypecode");	
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		
		return reportService.getReportSampleType(nreporttypecode,userInfo);
		 
	}
	
	@PostMapping(value = "/getReportTemplate")
	public ResponseEntity<Object> getReportTemplate(@RequestBody Map<String, Object> inputMap)throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reportService.getReportTemplate(userInfo);	 
	}
	
	@PostMapping(value = "/getReportValidation")
	public ResponseEntity<Object> getReportValidation(@RequestBody Map<String, Object> inputMap)throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reportService.getReportValidation(inputMap,userInfo);	 
	}
	
	@PostMapping(value = "/createReportValidation")
	public ResponseEntity<Object>createReportValidation(@RequestBody Map<String, Object> inputMap)throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reportService.createReportValidation(inputMap,userInfo);	 
	}
	
	@PostMapping(value = "/deleteReportValidation")
	public ResponseEntity<Object>deleteReportValidation(@RequestBody Map<String, Object> inputMap)throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return reportService.deleteReportValidation(inputMap,userInfo);	 
	}
	
	@PostMapping(value = "/createControlBasedReportparametreInsert")
	public ResponseEntity<Object>downloadControlBasedReportparametreInsert(@RequestBody Map<String, Object> inputMap)throws Exception{
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return controlbasedreportservice.downloadControlBasedReportparametreInsert(inputMap,userInfo);	 
	}
	

}
