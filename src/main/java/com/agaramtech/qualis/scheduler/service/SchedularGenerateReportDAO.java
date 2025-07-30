package com.agaramtech.qualis.scheduler.service;

import java.util.Map;
import com.agaramtech.qualis.global.UserInfo;

public interface SchedularGenerateReportDAO {
	
	public void schedularGenerateReport() throws Exception;

	public String AsyncReportGeneration(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;


}
