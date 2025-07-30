package com.agaramtech.qualis.scheduler.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class SchedularGenerateReportServiceImpl implements SchedularGenerateReportService  {

	
	private final SchedularGenerateReportDAO schedularGenerateReportDAO;
	
	public SchedularGenerateReportServiceImpl(SchedularGenerateReportDAO schedularGenerateReportDAO) {
		this.schedularGenerateReportDAO = schedularGenerateReportDAO;
	}
	
	@Override
	//@Scheduled(cron =  "0 */2 * ? * *")
	public void schedularGenerateReport() throws Exception {

		schedularGenerateReportDAO.schedularGenerateReport();
		
		
	}
	
	

}
