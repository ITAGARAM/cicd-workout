package com.agaramtech.qualis.archivalandpurging.restoreindividual.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
//import org.springframework.web.multipart.MultipartHttpServletRequest;
//import com.agaramtech.basemaster.pojo.Settings;
import com.agaramtech.qualis.global.UserInfo;

/**
 * 
 * @author ATE113
 *
 */
public interface RestoreIndividualDAO {

	public ResponseEntity<Object> getRestoreIndividual(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;
	
	public ResponseEntity<Object> createRestoreIndividual(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getRestoreSampleData(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;
	
	public ResponseEntity<Object> getPurgeDate(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getReleasedSamples(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	//public ResponseEntity<Object> getReleasedSamplesRefresh(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	
	public ResponseEntity<Object> getRegistrationSampleDetails(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
}

