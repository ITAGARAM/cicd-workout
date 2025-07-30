package com.agaramtech.qualis.testmanagement.service.method;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.Method;
import com.agaramtech.qualis.testmanagement.model.MethodValidity;

/**
 * This interface holds declarations to perform CRUD operation on 'method' table
 * through its implementation class.
 * @author ATE180
 * @version 9.0.0.1
 * @since   1- July- 2020
 */

public interface MethodDAO {
	
	/**
	 * This interface declaration is used to retrieve list of all active method for the
	 * specified site.
	 * @param userInfo [UserInfo] object holding details of logged in user.
	 * @return response entity  object holding response status and list of all active method
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getMethod(final Integer nmethodcode,final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve list of all active methodvalidity for the
	 * specified site.
	 * @param userInfo [UserInfo] object holding details of logged in user.
	 * @return response entity  object holding response status and list of all active methodvalidity
	 * @throws Exception that are thrown in the DAO layer
	 */
	//public ResponseEntity<Object> getMethodValidity(final UserInfo userInfo) throws Exception;
	
	
	/**
	 * This interface declaration is used to add a new entry to method  table.
	 * @param method [Method] object holding details to be added in source table
	 * @return response entity object holding response status and data of added method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createMethod(final Method method,MethodValidity methodval,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> createMethodValidity(final MethodValidity methodvalidity,final UserInfo userInfo) throws Exception;
	
	
	/**
	 * This interface declaration is used to update entry in method  table.
	 * @param method [Method] object holding details to be updated in method table
	 * @return response entity object holding response status and data of updated method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateMethod(final Method method,final MethodValidity methodval,UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> updateMethodValidity(final MethodValidity methodvalidity,final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active method object based
	 * on the specified nmethodcode.
	 * @param nmethodcode [int] primary key of source object
	 * @return response entity  object holding response status and data of method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Method getActiveMethodById(final int nmethodcode,final UserInfo userInfo) throws Exception;
	
	public  ResponseEntity<Object> getActiveMethodValidityById(final int nmethodvaliditycode,final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to delete entry in method  table.
	 * @param method [Method] object holding detail to be deleted in method table
	 * @return response entity object holding response status and data of deleted method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteMethod(final Method method,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> deleteMethodValidity(final MethodValidity methodvalidity,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> approveMethodValidity(final MethodValidity methodvalidity,final UserInfo userInfo) throws Exception;

	

}
