package com.agaramtech.qualis.testmanagement.service.testpackage;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestPackage;

/**
 * This class holds methods to perform CRUD operation on 'TestPackage' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class TestPackageServiceImpl implements TestPackageService {
	
	private final TestPackageDAO testpackageDAO;
	private final CommonFunction commonFunction;	
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param TestPackageDAO TestPackageDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public TestPackageServiceImpl(TestPackageDAO testpackageDAO, CommonFunction commonFunction) {
		this.testpackageDAO =testpackageDAO;
		this.commonFunction = commonFunction;
	}

	@Override
	public ResponseEntity<Object> getTestPackage(final UserInfo userInfo) throws Exception
	{
		return testpackageDAO.getTestPackage(userInfo);
	}	
	
	@Transactional
	@Override
	public ResponseEntity<Object> createTestPackage(TestPackage testpackage,UserInfo userInfo) throws Exception{
		
		return testpackageDAO.createTestPackage(testpackage,userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteTestPackage(TestPackage testpackage,UserInfo userInfo) throws Exception {
		
		return testpackageDAO.deleteTestPackage(testpackage,userInfo);
	}
	@Override
	public ResponseEntity<Object> getActiveTestPackageById(final int npackagecode,final UserInfo userInfo) throws Exception {
		
			final TestPackage testpackage = (TestPackage) testpackageDAO.getActiveTestPackageById(npackagecode,userInfo);
			if (testpackage == null) {
				//status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			else {
				return new ResponseEntity<>(testpackage, HttpStatus.OK);
			}
		}
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateTestPackage(TestPackage testpackage,UserInfo userInfo) throws Exception{
		
		return testpackageDAO.updateTestPackage(testpackage,userInfo); 
	}
	
}
