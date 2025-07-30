package com.agaramtech.qualis.configuration.service.transactionfilterconfiguration;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.configuration.model.TransactionFilterTypeConfig;
import com.agaramtech.qualis.dynamicpreregdesign.model.RegistrationSubType;
import com.agaramtech.qualis.global.UserInfo;


public interface TransactionFilterConfigrationDAO {
	ResponseEntity<Object> getRegistrationSubtypeConfigration(Integer ninstCode, final UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getBySampleType(Integer ninstrumentcatcode,Integer nregtypecode,UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getDepartment(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getDepartmentBasedUser(int nregsubtypecode,int sectionCode, UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getUserRole(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getUserRoleBasedUser(int nregsubtypecode,int nuserrolecode, UserInfo userInfo) throws Exception;
	ResponseEntity<Object> createDepartment(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getRegtypeBasedSampleType(int nsampletype, UserInfo userInfo) throws Exception;
	ResponseEntity<Object> deleteDepartment(TransactionFilterTypeConfig instSec, UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getTabdetails(TransactionFilterTypeConfig instSec, UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getDepartmentAndUsers(RegistrationSubType instSec, UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getListofUsers(int nregsubtypecode, UserInfo userInfo) throws Exception;
}
