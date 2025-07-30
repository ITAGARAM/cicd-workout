package com.agaramtech.qualis.testmanagement.service.method;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.Method;
import com.agaramtech.qualis.testmanagement.model.MethodValidity;

/**
 * This interface declaration holds methods to perform CRUD operation on 'method' table
 * through its DAO layer.
 * @author ATE180
 * @version 9.0.0.1
 * @since   1- July- 2020
 */

public interface MethodService {
	
	
	/**
	 * This interface declaration is used to retrieve list of all active method for the
	 * specified site through its DAO layer
	 * @param userInfo [UserInfo] object holding details of logged in user.
	 * @return response entity  object holding response status and list of all active method
	 * @throws Exception that are thrown in the DAO layer
	 */
	public  ResponseEntity<Object> getMethod(final Integer nmethodcode,final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve list of all active methodvalidity for the
	 * specified site through its DAO layer
	 * @param userInfo [UserInfo] object holding details of logged in user.
	 * @return response entity  object holding response status and list of all active methodvalidity
	 * @throws Exception that are thrown in the DAO layer
	 */
	//public  ResponseEntity<Object> getMethodValidity(final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to add a new entry to method  table through its DAO layer.
	 * @param method [Method] object holding details to be added in method table
	 * @return response entity object holding response status and data of added method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createMethod(final Method objMethod,final MethodValidity objMethodValidity,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> createMethodValidity(final MethodValidity objMethodValidity,final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to update entry in method  table through its DAO layer.
	 * @param method [Method] object holding details to be updated in method table
	 * @return response entity object holding response status and data of updated method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateMethod(final Method objMethod,final MethodValidity objMethodValidity,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> updateMethodValidity(final MethodValidity objMethodValidity,final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active method object based
	 * on the specified nmethodcode through its DAO layer.
	 * @param nmethodcode [int] primary key of method object
	 * @param siteCode [int] primary key of site object 
	 * @return response entity  object holding response status and data of method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveMethodById(final int nmethodcode,final UserInfo userInfo) throws Exception ;
	
	public ResponseEntity<Object> getActiveMethodValidityById(final int nmethodvaliditycode,final UserInfo userInfo) throws Exception ;
	
	/**
	 * This interface declaration is used to delete entry in method  table through its DAO layer.
	 * @param method [Method] object holding detail to be deleted in method table
	 * @return response entity object holding response status and data of deleted method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteMethod(final Method objMethod,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> deleteMethodValidity(final MethodValidity objMethodValidity,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> approveMethodValidity(final MethodValidity objMethodValidity,final UserInfo userInfo) throws Exception;

}
