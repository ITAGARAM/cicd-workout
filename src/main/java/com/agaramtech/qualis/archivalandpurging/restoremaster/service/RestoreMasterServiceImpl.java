package com.agaramtech.qualis.archivalandpurging.restoremaster.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.archivalandpurging.restoremaster.model.RestoreMaster;
import com.agaramtech.qualis.basemaster.service.unit.UnitDAO;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
/**
 * This class holds methods to perform CRUD operation on 'restoremaster' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class RestoreMasterServiceImpl implements RestoreMasterService {

	private final RestoreMasterDAO restoreMasterDAO;
	private final CommonFunction commonFunction;
	
	public RestoreMasterServiceImpl(RestoreMasterDAO restoreMasterDAO, CommonFunction commonFunction) {
		this.restoreMasterDAO = restoreMasterDAO;
		this.commonFunction = commonFunction;
	}
	
	
	/**
	 * This method is used to retrieve list of all active restoremaster for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active restoremaster
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getRestoreMaster(UserInfo userInfo) throws Exception {
		
		return restoreMasterDAO.getRestoreMaster(userInfo);
	}

	/**
	 * This method is used to retrieve active restoremaster object based on the specified nrestoremastercode.
	 * @param nrestoremastercode [int] primary key of restoremaster object
	 * @return response entity  object holding response status and data of restoremaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveRestoreMasterById(int nrestoremastercode,UserInfo userInfo) throws Exception {
		 
		return restoreMasterDAO.getActiveRestoreMasterById(nrestoremastercode,userInfo);
	}

	/**
	 * This method is used to add a new entry to restoremaster table.
	 * On successive insert get the new inserted record along with default status from transaction status 
	 * @param objRestoreMaster [RestoreMaster] object holding details to be added in restoremaster table
	 * @return inserted restoremaster object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createRestoreMaster(RestoreMaster objRestoreMaster,UserInfo userInfo) throws Exception {

		return restoreMasterDAO.createRestoreMaster(objRestoreMaster,userInfo);
	}

	/**
	 * This method is used to update entry in restoremaster  table.
	 * @param objRestoreMaster [RestoreMaster] object holding details to be updated in restoremaster table
	 * @return response entity object holding response status and data of updated restoremaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateRestoreMaster(RestoreMaster objRestoreMaster,UserInfo userInfo) throws Exception {
		 
		return restoreMasterDAO.updateRestoreMaster(objRestoreMaster,userInfo);
	}

	/**
	 * This method id used to delete an entry in restoremaster table
	 * @param objRestoreMaster [RestoreMaster] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an restoremaster object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteRestoreMaster(RestoreMaster objRestoreMaster,UserInfo userInfo) throws Exception {		
		return restoreMasterDAO.deleteRestoreMaster(objRestoreMaster,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception
	{
		return restoreMasterDAO.getSite(userInfo);
	}

	@Override
	public ResponseEntity<Object> getPurgeDate(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception
	{
		return restoreMasterDAO.getPurgeDate(inputMap,userInfo);
	}
}


