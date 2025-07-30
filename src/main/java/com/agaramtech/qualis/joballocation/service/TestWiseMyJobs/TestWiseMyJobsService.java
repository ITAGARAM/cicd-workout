package com.agaramtech.qualis.joballocation.service.TestWiseMyJobs;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;


public interface TestWiseMyJobsService {
	public ResponseEntity<Object> getTestWiseMyJobs(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getSampleType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getRegistrationType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getRegistrationTypeBySampleType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getRegistrationsubType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getRegistrationsubTypeByRegType(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getFilterStatusByApproveVersion(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getSection(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getSectionByApproveVersion(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(Map<String, Object> inputMap,UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getTestCombo(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getTestComboBySection(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getDesignTemplateByApprovalConfigVersion(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getMyTestWiseJobsDetails(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> CreateAcceptTest(Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> createFilterName(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> getmyjobsFilterDetails(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;
}
