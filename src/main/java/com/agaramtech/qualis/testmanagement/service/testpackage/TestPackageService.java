package com.agaramtech.qualis.testmanagement.service.testpackage;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestPackage;

/**
 * This interface declaration holds methods to perform CRUD operation on 'testpackage' table
 */
public interface TestPackageService {
	
	/**
	 * This service interface declaration will access the DAO layer that is used 
	 * to get all the available testpackages with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of testpackage records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public  ResponseEntity<Object> getTestPackage(final UserInfo userInfo) throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to testpackage  table.
	 * @param objtestpackage [testpackage] object holding details to be added in testpackage table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added testpackage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createTestPackage(TestPackage testpackage,UserInfo userInfo) throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to delete an entry in testpackage  table.
	 * @param objtestpackage [testpackage] object holding detail to be deleted from testpackage table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted testpackage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteTestPackage(TestPackage testpackage,UserInfo userInfo) throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve active testpackage object based
	 * on the specified ntestpackageCode.
	 * @param ntestpackageCode [int] primary key of testpackage object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of testpackage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveTestPackageById(final int npackagecode,final UserInfo userInfo) throws Exception ;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 *  update entry in testpackage  table.
	 * @param objtestpackage [testpackage] object holding details to be updated in testpackage table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated testpackage object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateTestPackage(TestPackage testpackage,UserInfo userInfo) throws Exception;
}
