package com.agaramtech.qualis.testmanagement.service.method;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
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
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class MethodServiceImpl implements MethodService{
	

	private final MethodDAO methodDAO;	
	
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param methodDAO MethodDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public MethodServiceImpl(MethodDAO methodDAO, CommonFunction commonFunction) {
		this.methodDAO = methodDAO;
		this.commonFunction = commonFunction;
	}
	
	/**
	 * This method is used to retrieve list of all active method for the
	 * specified site through its DAO layer
	 * @param userInfo [UserInfo] object holding details of logged in user.
	 * @return response entity  object holding response status and list of all active method
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getMethod(final Integer nmethodcode,final UserInfo userInfo) throws Exception
	{
		return methodDAO.getMethod(nmethodcode,userInfo);
	}	
	
	/**
	 * This method is used to retrieve list of all active methodvalidity for the
	 * specified site through its DAO layer
	 * @param userInfo [UserInfo] object holding details of logged in user.
	 * @return response entity  object holding response status and list of all active methodvalidity
	 * @throws Exception that are thrown in the DAO layer
	 */
//	@Override
//	public ResponseEntity<Object> getMethodValidity(final UserInfo userInfo) throws Exception
//	{
//		return methodDAO.getMethodValidity(userInfo);
//	}	
	
	/**
	 * This method is used to add a new entry to method  table through its DAO layer.
	 * @param method [Method] object holding details to be added in method table
	 * @return response entity object holding response status and data of added method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createMethod(final Method method,final MethodValidity methodval,final UserInfo userInfo) throws Exception{
		
		return methodDAO.createMethod(method,methodval,userInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> createMethodValidity(final MethodValidity methodvalidity,final UserInfo userInfo) throws Exception{
		
		return methodDAO.createMethodValidity(methodvalidity, userInfo);
	}
	
	/**
	 * This method is used to update entry in method  table through its DAO layer.
	 * @param method [Method] object holding details to be updated in update method table
	 * @return response entity object holding response status and data of updated update method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateMethod(final Method method,final MethodValidity methodval,final UserInfo userInfo) throws Exception{
		
		return methodDAO.updateMethod(method,methodval,userInfo); 
	}
	
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateMethodValidity(final MethodValidity methodvalidity,final UserInfo userInfo) throws Exception{
		
		return methodDAO.updateMethodValidity(methodvalidity, userInfo); 
	}
	
	/**
	 * This method is used to retrieve active method object based
	 * on the specified nmethodcode through its DAO layer.
	 * @param nmethodcode [int] primary key of Source object
	 * @param siteCode [int] primary key of site object 
	 * @return response entity  object holding response status and data of method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getActiveMethodById(final int nmethodcode,final UserInfo userInfo) throws Exception {
		
			final Method method = (Method) methodDAO.getActiveMethodById(nmethodcode,userInfo);
			if (method == null) {
				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				return new ResponseEntity<>(method, HttpStatus.OK);
			}
		}	
	
	
	@Override
	public ResponseEntity<Object> getActiveMethodValidityById(final int nmethodvaliditycode,final UserInfo userInfo) throws Exception {
		
//			final ResponseEntity<Object> methodvalidity = (ResponseEntity<Object>) methodDAO.getActiveMethodValidityById(nmethodvaliditycode, userInfo);
//			if (methodvalidity.getBody().equals("ALREADYDELETED")) {
//				//status code:417
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
//			}
//			else {
//				return new ResponseEntity<>(methodvalidity, HttpStatus.OK);
//			}
		return methodDAO.getActiveMethodValidityById(nmethodvaliditycode, userInfo);
		}	
		
		
	
	/**
	 * This method is used to delete entry in method  table through its DAO layer.
	 * @param method [Method] object holding detail to be deleted in method table
	 * @return response entity object holding response status and data of deleted method object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteMethod(final Method method,final UserInfo userInfo) throws Exception {
		
		return methodDAO.deleteMethod(method,userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteMethodValidity(final MethodValidity methodvalidity,final UserInfo userInfo) throws Exception {
		
		return methodDAO.deleteMethodValidity(methodvalidity, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> approveMethodValidity(final MethodValidity methodvalidity,final UserInfo userInfo) throws Exception {
		
		return methodDAO.approveMethodValidity(methodvalidity, userInfo);
	}
	

}
