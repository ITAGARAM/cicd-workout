package com.agaramtech.qualis.archivalandpurging.purge.service;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.archivalandpurging.purge.model.PurgeMaster;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on 'purgemaster' table
 * @author ATE113
 * @version 9.0.0.1 
 * @since   25- June- 2024
 */
public interface PurgeMasterService {
	
	/**
	 * This interface declaration is used to get the over all purgemaster with respect to site
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of purgemaster with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getPurge(UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to retrieve active purgemaster object based
	 * on the specified npurgemastercode.
	 * @param npurgemastercode [int] primary key of purgemaster object
	 * @return response entity  object holding response status and data of purgemaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActivePurgeMasterById(final int npurgemastercode,UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to add a new entry to purgemaster  table.
	 * @param objPurgeMaster [PurgeMaster] object holding details to be added in purgemaster table
	 * @return response entity object holding response status and data of added purgemaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createPurgeMaster(PurgeMaster objPurgeMaster,UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to update entry in purgemaster  table.
	 * @param objPurgeMaster [PurgeMaster] object holding details to be updated in purgemaster table
	 * @return response entity object holding response status and data of updated purgemaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updatePurgeMaster(PurgeMaster objPurgeMaster,UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to delete entry in purgemaster  table.
	 * @param objPurgeMaster [PurgeMaster] object holding detail to be deleted in purgemaster table
	 * @return response entity object holding response status and data of deleted purgemaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deletePurgeMaster(PurgeMaster objPurgeMaster,UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getSite(final UserInfo userinfo) throws Exception;
}

