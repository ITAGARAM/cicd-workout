package com.agaramtech.qualis.configuration.service.transactionfilterconfiguration;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.configuration.model.TransactionFilterTypeConfig;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class TransactionFilterConfigrationServiceImpl implements TransactionFilterConfigrationService {
	
	private final TransactionFilterConfigrationDAO objTransactionFilterConfigrationDAO;
	
	
	public TransactionFilterConfigrationServiceImpl(TransactionFilterConfigrationDAO objTransactionFilterConfigrationDAO) {
		this.objTransactionFilterConfigrationDAO = objTransactionFilterConfigrationDAO;
		
	}
	
	public ResponseEntity<Object> getRegistrationSubtypeConfigration(Integer ninstCode, final UserInfo userInfo) throws Exception {
		return objTransactionFilterConfigrationDAO.getRegistrationSubtypeConfigration(ninstCode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getBySampleType(Integer ninstrumentcatcode, Integer nregtypecode,UserInfo userInfo)
			throws Exception {
		return objTransactionFilterConfigrationDAO.getBySampleType(ninstrumentcatcode,nregtypecode, userInfo);
	}
	
	public ResponseEntity<Object> getDepartment(Map<String, Object> inputMap,final UserInfo userinfo) throws Exception {
		return objTransactionFilterConfigrationDAO.getDepartment(inputMap,userinfo);
	}
	
	@Override
	public ResponseEntity<Object> getDepartmentBasedUser(int nregsubtypecode,int sectionCode, UserInfo userInfo) throws Exception {
		return objTransactionFilterConfigrationDAO.getDepartmentBasedUser(nregsubtypecode,sectionCode, userInfo);

	}
	public ResponseEntity<Object> getUserRole(Map<String, Object> inputMap,final UserInfo userinfo) throws Exception {
		return objTransactionFilterConfigrationDAO.getUserRole(inputMap,userinfo);
	}
	@Override
	public ResponseEntity<Object> getUserRoleBasedUser(int nregsubtypecode,int nuserrolecode, UserInfo userInfo) throws Exception {
		return objTransactionFilterConfigrationDAO.getUserRoleBasedUser(nregsubtypecode,nuserrolecode, userInfo);

	}
	
	@Transactional
	public ResponseEntity<Object> createDepartment(Map<String, Object> inputMap,final UserInfo userinfo) throws Exception {
		return objTransactionFilterConfigrationDAO.createDepartment(inputMap,userinfo);
	}
	
	@Override
	public ResponseEntity<Object> getRegtypeBasedSampleType(int nsampletype, UserInfo userInfo) throws Exception {
		return objTransactionFilterConfigrationDAO.getRegtypeBasedSampleType(nsampletype, userInfo);

	}
	
	@Transactional
	public ResponseEntity<Object> deleteDepartment(TransactionFilterTypeConfig instSec, UserInfo userInfo) throws Exception {

		return objTransactionFilterConfigrationDAO.deleteDepartment(instSec, userInfo);
	}
	public ResponseEntity<Object> getTabdetails(TransactionFilterTypeConfig instSec, UserInfo userInfo) throws Exception {

		return objTransactionFilterConfigrationDAO.getTabdetails(instSec, userInfo);
	}
	

	public ResponseEntity<Object> getListofUsers(int nregsubtypecode, UserInfo userInfo) throws Exception {
		return objTransactionFilterConfigrationDAO.getListofUsers(nregsubtypecode, userInfo);

	}
}
