package com.agaramtech.qualis.reports.service.reportview;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.model.ReportDesignConfig;
import com.agaramtech.qualis.reports.model.ReportMaster;

public interface ReportDAO {

	public ResponseEntity<Object> getReportView(final Integer reportModuleCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> viewReport(final ReportMaster reportMaster, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getChildDataList(final ReportDesignConfig parentConfig,
			final Map<String, Object> inputFieldData, final String parentValue, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> viewReportWithParameters(final ReportMaster reportMaster,
			final Map<String, Object> inputFieldData, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> searchViewReport(final int nreportCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getStimulsoftView(final Integer reportModuleCode, final UserInfo userInfo)
			throws Exception;
}
