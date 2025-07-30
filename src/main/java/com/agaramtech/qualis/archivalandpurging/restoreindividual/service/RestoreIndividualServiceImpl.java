package com.agaramtech.qualis.archivalandpurging.restoreindividual.service;

//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
import java.util.Map;
//import java.util.stream.Collectors;

//import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartHttpServletRequest;

//import com.agaramtech.basemaster.pojo.Settings;
//import com.agaramtech.qualis.global.CommonFunction;
//import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
//import com.agaramtech.release.service.ReleaseDAO;
//import com.agaramtech.release.service.ReleaseServiceImpl;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class RestoreIndividualServiceImpl implements RestoreIndividualService  {
	
	private final RestoreIndividualDAO restoreDAO;
	//private final CommonFunction commonFunction;

	public RestoreIndividualServiceImpl(RestoreIndividualDAO restoreDAO) {
		this.restoreDAO = restoreDAO;
		//this.commonFunction = commonFunction;
	}
	
	
	
	@Override
	public ResponseEntity<Object> getRestoreIndividual(Map<String, Object> inputMap, UserInfo userInfo)throws Exception {
		
		return restoreDAO.getRestoreIndividual(inputMap,userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> createRestoreIndividual(Map<String, Object> inputMap, UserInfo userInfo)throws Exception {
		
		return restoreDAO.createRestoreIndividual(inputMap,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getRestoreSampleData(Map<String, Object> inputMap, UserInfo userInfo)throws Exception {
		
		return restoreDAO.getRestoreSampleData(inputMap,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getPurgeDate(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return restoreDAO.getPurgeDate(inputMap,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getReleasedSamples(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return restoreDAO.getReleasedSamples(inputMap,userInfo);
	}
	
//	@Override
//	public ResponseEntity<Object> getReleasedSamplesRefresh(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
//	{
//		return restoreDAO.getReleasedSamplesRefresh(inputMap,userInfo);
//	}
	
	
	@Override
	public ResponseEntity<Object> getRegistrationSampleDetails(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return restoreDAO.getRegistrationSampleDetails(inputMap,userInfo);
	}
}
