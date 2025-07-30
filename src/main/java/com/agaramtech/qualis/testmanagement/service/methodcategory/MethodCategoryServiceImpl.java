package com.agaramtech.qualis.testmanagement.service.methodcategory;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.MethodCategory;

/**
 * This class holds methods to perform CRUD operation on 'storagelocation' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class MethodCategoryServiceImpl implements MethodCategoryService {
	
	private final MethodCategoryDAO methodCategoryDAO;
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param methodCategoryDAO MethodCategoryDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	
	public MethodCategoryServiceImpl(MethodCategoryDAO methodCategoryDAO, CommonFunction commonFunction) {
		this.methodCategoryDAO = methodCategoryDAO;
		this.commonFunction = commonFunction;
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available methodcategory with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * which the list is to be fetched
	 * @return a response entity which holds the list of methodcategory records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getMethodCategory(int nmasterSiteCode) throws Exception {
		return methodCategoryDAO.getMethodCategory(nmasterSiteCode);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active methodcategory object based
	 * on the specified nmethodcatcode.
	 * @param nstorageLocationCode [int] primary key of MethodCategory object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * which the list is to be fetched
	 * @return response entity  object holding response status and data of MethodCategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getActiveMethodCategoryById(final int nmethodcatcode, final UserInfo userInfo)throws Exception {
		final MethodCategory methodCategory = methodCategoryDAO.getActiveMethodCategoryById(nmethodcatcode);
		if (methodCategory == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(methodCategory, HttpStatus.OK);
		}
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to methodcategory  table.
	 * @param methodCategory [MethodCategory] object holding details to be added in methodcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * which the list is to be fetched
	 * @return response entity object holding response status and data of added MethodCategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Override
	@Transactional 
	public ResponseEntity<Object> createMethodCategory(MethodCategory methodCategory, UserInfo userInfo)throws Exception {
		return methodCategoryDAO.createMethodCategory(methodCategory, userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in methodcategory  table.
	 * @param methodCategory [MethodCategory] object holding details to be updated in methodcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * which the list is to be fetched
	 * @return response entity object holding response status and data of updated MethodCategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Override
	@Transactional 
	public ResponseEntity<Object> updateMethodCategory(MethodCategory methodCategory, UserInfo userInfo)throws Exception {
		return methodCategoryDAO.updateMethodCategory(methodCategory, userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in methodcategory  table.
	 * @param methodCategory [MethodCategory] object holding detail to be deleted from methodcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * which the list is to be fetched
	 * @return response entity object holding response status and data of deleted MethodCategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Override
	@Transactional 
	public ResponseEntity<Object> deleteMethodCategory(MethodCategory methodCategory, UserInfo userInfo)throws Exception {
		return methodCategoryDAO.deleteMethodCategory(methodCategory, userInfo);
	}
}
