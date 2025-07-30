package com.agaramtech.qualis.configuration.service.apiservice;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;


public interface APIInterfaceService {

	ResponseEntity<Object> getAPIService(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getDynamicMaster(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getQualisFormFields(Map<String, Object> inputMap, UserInfo userInfo);

	ResponseEntity<Object> getQualisForms(Map<String, Object> inputMap, UserInfo userInfo);

	ResponseEntity<Object> getStaticMaster(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getCustomQuery(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getCustomQueryName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getSQLQueryData(Map<String, Object> inputMap,String query, UserInfo userInfo) throws Exception;

}
