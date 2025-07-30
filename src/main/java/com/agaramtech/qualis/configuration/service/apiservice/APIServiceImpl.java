package com.agaramtech.qualis.configuration.service.apiservice;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;

@Service
@Transactional(readOnly = true, rollbackFor=Exception.class)
public class APIServiceImpl implements APIInterfaceService {

	private final APIServiceDAO apiServiceDAO;


	public APIServiceImpl(APIServiceDAO apiServiceDAO) {
		super();
		this.apiServiceDAO = apiServiceDAO;
	}

	@Override
	public ResponseEntity<Object> getAPIService(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return apiServiceDAO.getAPIService(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getDynamicMaster(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return apiServiceDAO.getDynamicMaster(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getQualisFormFields(Map<String, Object> inputMap, UserInfo userInfo) {
		// TODO Auto-generated method stub
		return apiServiceDAO.getQualisFormFields(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getQualisForms(Map<String, Object> inputMap, UserInfo userInfo) {
		// TODO Auto-generated method stub
		return apiServiceDAO.getQualisForms(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStaticMaster(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return apiServiceDAO.getStaticMaster(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getCustomQuery(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return apiServiceDAO.getCustomQuery(inputMap, userInfo);
	}
	@Override
	public ResponseEntity<Object> getCustomQueryName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return apiServiceDAO.getCustomQueryName(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSQLQueryData(Map<String, Object> inputMap,String query, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return apiServiceDAO.getSQLQueryData(inputMap,query, userInfo);
	}

}
