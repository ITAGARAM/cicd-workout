package com.agaramtech.qualis.reports.service.reportview;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.model.ReportDesignConfig;
import com.agaramtech.qualis.reports.model.ReportMaster;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class ReportServiceImpl implements ReportService{

	private final ReportDAO reportDAO;
	
	public ReportServiceImpl(ReportDAO reportDAO) {
		this.reportDAO=reportDAO;
	}
	
	@Override
	public ResponseEntity<Object> getReportView(final Integer reportModuleCode, final UserInfo userInfo) throws Exception{
		return reportDAO.getReportView(reportModuleCode, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> viewReport(final ReportMaster reportMaster, final UserInfo userInfo) throws Exception{
		return reportDAO.viewReport(reportMaster, userInfo);	
	}
	
	@Override
	public ResponseEntity<Object> getChildDataList(final ReportDesignConfig parentConfig, final Map<String, Object> inputFieldData, final String parentValue, final UserInfo userInfo) throws Exception{
		return reportDAO.getChildDataList(parentConfig, inputFieldData,parentValue, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> viewReportWithParameters(final ReportMaster reportMaster,final Map<String, Object> inputFieldData, final UserInfo userInfo) throws Exception{
		return reportDAO.viewReportWithParameters(reportMaster, inputFieldData, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> searchViewReport(final int nreportCode, final UserInfo userInfo) throws Exception{
		return reportDAO.searchViewReport(nreportCode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getStimulsoftView(final Integer reportModuleCode, final UserInfo userInfo) throws Exception{
		return reportDAO.getStimulsoftView(reportModuleCode, userInfo);
	}
}
