package com.agaramtech.qualis.reports.service.reportconfig;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.model.ReportDesignConfig;
import com.agaramtech.qualis.reports.model.ReportDetails;
import com.agaramtech.qualis.reports.model.ReportImages;
import com.agaramtech.qualis.reports.model.ReportMaster;
import com.agaramtech.qualis.reports.model.ReportParameterMapping;
import com.agaramtech.qualis.reports.model.ReportType;
import com.agaramtech.qualis.reports.model.SubReportDetails;

public interface ReportMasterService {

	public ResponseEntity<Object> getReportMaster(Integer nreportCode, String reportTypeCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReportMasterComboData(final Integer nreportCode, final UserInfo userInfo,
			final ReportType reportType) throws Exception;

	public ResponseEntity<Object> createReportDesigner(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createReportDetails(final MultipartHttpServletRequest request,
			ReportDetails reportDetails, final ReportMaster reportMaster, final Integer getId, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateReportMaster(final ReportMaster reportMaster, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateReportDetails(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReportDetailComboData(final Integer nreportDetailCode,
			final ReportMaster reportMaster, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveReportDetailById(final int reportDetailCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReportDetail(final int nreportDetailCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReportAddDesignComboData(final int nreportDetailCode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createReportDesignParameter(final List<ReportDesignConfig> designConfigList,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> approveReportVersion(final ReportDetails reportDetail, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteReportMaster(final ReportMaster reportMaster, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteReportDetails(final ReportDetails reportDetails, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReportDesignConfig(final int nreportDetailCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createReportParameterMapping(final ReportDetails reportDetail,
			final List<ReportParameterMapping> mappingList, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReportParameterMappingComboData(final int nreportDetailCode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateReportStatus(final ReportMaster reportMaster, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReportModule(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationSubTypeByRegType(final int nregTypeCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createSubReportDetails(final MultipartHttpServletRequest request,
			final SubReportDetails subReportDetails, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSubReportDetails(final SubReportDetails subReportDetails,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createReportImages(final MultipartHttpServletRequest request,
			ReportImages reportImages, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteReportImages(final ReportImages reportImages, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReportSubType(final int nreportTypeCode, final int nsampleTypecode,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getControlButton(int nreportTypeCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationtypeForSample(int nsampletypecode, UserInfo userInfo,
			int nreporttypecode) throws Exception;

	public ResponseEntity<Object> getReportSampleType(int nreporttypecode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReportRegSubTypeApproveConfigVersion(int nregtypecode, int nregsubtypecode,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReportTemplate(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createReportValidation(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReportValidation(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteReportValidation(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

}
