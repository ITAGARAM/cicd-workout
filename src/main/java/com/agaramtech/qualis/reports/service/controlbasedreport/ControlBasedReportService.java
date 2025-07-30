
package com.agaramtech.qualis.reports.service.controlbasedreport;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface ControlBasedReportService {

	public ResponseEntity<Object> generateControlBasedReport(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> controlBasedReportParameter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> controlBasedReportparametretable(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> controlBasedReportparametretablecolumn(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> downloadControlBasedReportparametreInsert(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;

}
