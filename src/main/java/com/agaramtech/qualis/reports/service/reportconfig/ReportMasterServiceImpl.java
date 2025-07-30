package com.agaramtech.qualis.reports.service.reportconfig;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.model.ReportDesignConfig;
import com.agaramtech.qualis.reports.model.ReportDetails;
import com.agaramtech.qualis.reports.model.ReportImages;
import com.agaramtech.qualis.reports.model.ReportMaster;
import com.agaramtech.qualis.reports.model.ReportParameterMapping;
import com.agaramtech.qualis.reports.model.ReportType;
import com.agaramtech.qualis.reports.model.SubReportDetails;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class ReportMasterServiceImpl implements ReportMasterService{

	private final CommonFunction commonFunction;
	private final ReportMasterDAO reportDAO;
	
	public ReportMasterServiceImpl(ReportMasterDAO reportDAO, CommonFunction commonFunction) {
		this.reportDAO = reportDAO;
		this.commonFunction = commonFunction;
	}
	
	@Override
	public ResponseEntity<Object> getReportMaster(Integer nreportCode, String reportTypeCode, final UserInfo userInfo) throws Exception {
		return reportDAO.getReportMaster(nreportCode, reportTypeCode, userInfo);		
	}
	
	@Override
	public ResponseEntity<Object> getReportMasterComboData(final Integer nreportCode, final UserInfo userInfo, final ReportType reportType) throws Exception{
		return reportDAO.getReportMasterComboData(nreportCode, userInfo, reportType);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> createReportDesigner(MultipartHttpServletRequest request, final UserInfo userInfo)throws Exception {
		return reportDAO.createReportDesigner(request, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> createReportDetails( final MultipartHttpServletRequest request,ReportDetails reportDetails, final ReportMaster reportMaster, final Integer getId, final UserInfo userInfo) throws Exception{
		return reportDAO.createReportDetails(request, reportDetails, reportMaster, getId, userInfo);
	}
		
	@Transactional
	@Override
	public ResponseEntity<Object> updateReportMaster(final ReportMaster reportMaster, final UserInfo userInfo) throws Exception{
		return reportDAO.updateReportMaster(reportMaster, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getReportDetailComboData(final Integer nreportDetailCode, final ReportMaster reportMaster, final UserInfo userInfo) throws Exception{
		return reportDAO.getReportDetailComboData(nreportDetailCode, reportMaster, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getActiveReportDetailById(final int reportDetailCode, final UserInfo userInfo) throws Exception{
		final ReportDetails reportDetails = (ReportDetails) reportDAO.getActiveReportDetailById(reportDetailCode,userInfo);
		if (reportDetails == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
			return new ResponseEntity<>(reportDetails, HttpStatus.OK);
		}
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateReportDetails(final MultipartHttpServletRequest request,final UserInfo userInfo) throws Exception{
		return reportDAO.updateReportDetails(request, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getReportDetail(final int nreportDetailCode, final UserInfo userInfo) throws Exception{
		return reportDAO.getReportDetail(nreportDetailCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getReportAddDesignComboData( final int nreportDetailCode,	UserInfo userInfo)	throws Exception{
		return reportDAO.getReportAddDesignComboData(nreportDetailCode, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> createReportDesignParameter(final List<ReportDesignConfig> designConfigList, final UserInfo userInfo) throws Exception{
		return reportDAO.createReportDesignParameter(designConfigList, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> approveReportVersion(final ReportDetails reportDetail, final UserInfo userInfo) throws Exception{
		return reportDAO.approveReportVersion(reportDetail, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteReportMaster(final ReportMaster reportMaster, final UserInfo userInfo)	throws Exception{
		return reportDAO.deleteReportMaster(reportMaster, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteReportDetails(final ReportDetails reportDetails, final UserInfo userInfo)throws Exception{
		return reportDAO.deleteReportDetails(reportDetails, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getReportDesignConfig(final int nreportDetailCode, final UserInfo userInfo) throws Exception{
		return reportDAO.getReportDesignConfig(nreportDetailCode, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> createReportParameterMapping(final ReportDetails reportDetail, final List<ReportParameterMapping> mappingList, final UserInfo userInfo) throws Exception{
		return reportDAO.createReportParameterMapping(reportDetail, mappingList, userInfo);
	}

	@Override
	public ResponseEntity<Object> getReportParameterMappingComboData( final int nreportDetailCode,UserInfo userInfo)	throws Exception {
		return reportDAO.getReportParameterMappingComboData(nreportDetailCode, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateReportStatus(final ReportMaster reportMaster, final UserInfo userInfo) throws Exception{
		return reportDAO.updateReportStatus(reportMaster, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getReportModule(final UserInfo userInfo) throws Exception{
		return reportDAO.getReportModule(userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getRegistrationSubTypeByRegType(final int nregTypeCode, final UserInfo userInfo) throws Exception {
		return reportDAO.getRegistrationSubTypeByRegType(nregTypeCode, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> createSubReportDetails( final MultipartHttpServletRequest request, final SubReportDetails reportDetails, final UserInfo userInfo) throws Exception{
		return reportDAO.createSubReportDetails(request, reportDetails, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSubReportDetails(final SubReportDetails subReportDetails, final UserInfo userInfo)throws Exception {
		return reportDAO.deleteSubReportDetails(subReportDetails, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> createReportImages( final MultipartHttpServletRequest request, ReportImages reportImages, final UserInfo userInfo) throws Exception{
		return reportDAO.createReportImages(request, reportImages, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteReportImages(final ReportImages reportImages, final UserInfo userInfo)	throws Exception{
		return reportDAO.deleteReportImages(reportImages, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getReportSubType(final int nreportTypeCode,final int nsampleTypecode,final UserInfo userInfo) throws Exception{
		return reportDAO.getCOAReportType(nreportTypeCode,nsampleTypecode,userInfo);
	}

	@Override
	public ResponseEntity<Object> getControlButton(int nformcode,UserInfo userinfo) throws Exception {
		return reportDAO.getControlButton(nformcode,userinfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationtypeForSample(int nsampletypecode,UserInfo userinfo,int nreporttypecode) throws Exception {
		return reportDAO.getRegistrationtypeForSample(nsampletypecode,userinfo,nreporttypecode);
	}
	@Override
	public ResponseEntity<Object> getReportSampleType(int nreporttypecode,UserInfo userinfo) throws Exception {
		return reportDAO.getReportSampleType(nreporttypecode,userinfo);
	}

	@Override
	public ResponseEntity<Object> getReportRegSubTypeApproveConfigVersion(int nregtypecode, int nregsubtypecode,
			UserInfo userInfo) throws Exception {
		return reportDAO.getReportRegSubTypeApproveConfigVersion( nregtypecode,  nregsubtypecode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getReportTemplate(UserInfo userinfo) throws Exception {
		return reportDAO.getReportTemplate(userinfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createReportValidation(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return reportDAO.createReportValidation(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getReportValidation(Map<String, Object> inputMap,UserInfo userInfo) throws Exception {
		return reportDAO.getReportValidation(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteReportValidation(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return reportDAO.deleteReportValidation(inputMap,userInfo);
	}


}