package com.agaramtech.qualis.archivalandpurging.restoremaster.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.archivalandpurging.restoremaster.model.RestoreMaster;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on 'restoremaster' table
 */
public interface RestoreMasterService {
	
	/**
	 * This interface declaration is used to get the over all restoremaster with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of restoremaster with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getRestoreMaster(UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to retrieve active restoremaster object based
	 * on the specified nrestoremastercode.
	 * @param nrestoremastercode [int] primary key of restoremaster object
	 * @return response entity  object holding response status and data of restoremaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveRestoreMasterById(final int nrestoremastercode,UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to add a new entry to restoremaster  table.
	 * @param objRestoreMaster [RestoreMaster] object holding details to be added in restoremaster table
	 * @return response entity object holding response status and data of added restoremaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createRestoreMaster(RestoreMaster objRestoreMaster,UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to update entry in restoremaster  table.
	 * @param objRestoreMaster [RestoreMaster] object holding details to be updated in restoremaster table
	 * @return response entity object holding response status and data of updated restoremaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateRestoreMaster(RestoreMaster objRestoreMaster,UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to delete entry in restoremaster  table.
	 * @param objRestoreMaster [RestoreMaster] object holding detail to be deleted in restoremaster table
	 * @return response entity object holding response status and data of deleted restoremaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteRestoreMaster(RestoreMaster objRestoreMaster,UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getSite(final UserInfo userinfo) throws Exception;
	
	public ResponseEntity<Object> getPurgeDate(Map<String, Object> inputMap,final UserInfo userinfo) throws Exception;
}

