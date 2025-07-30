package com.agaramtech.qualis.reports.service.controlbasedreport;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class ControlBasedReportServiceImpl implements ControlBasedReportService {
	
	private final ControlBasedReportDAO  controlBasedReportDAO;
	
	public ControlBasedReportServiceImpl(ControlBasedReportDAO  controlBasedReportDAO) {
		this.controlBasedReportDAO=controlBasedReportDAO;
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> generateControlBasedReport(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return controlBasedReportDAO.generateControlBasedReport(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> controlBasedReportParameter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return controlBasedReportDAO.controlBasedReportParameter(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> controlBasedReportparametretable(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return controlBasedReportDAO.controlBasedReportparametretable(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> controlBasedReportparametretablecolumn(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		return controlBasedReportDAO.controlBasedReportparametretablecolumn(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> downloadControlBasedReportparametreInsert(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		return controlBasedReportDAO.downloadControlBasedReportparametreInsert(inputMap,userInfo);
	}


}
