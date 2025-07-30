package com.agaramtech.qualis.testmanagement.service.testcategory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestCategory;

/**
 * This class holds methods to perform CRUD operation on 'testcategory' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class TestCategoryServiceImpl implements TestCategoryService {

	private final TestCategoryDAO testCategoryDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * @param testCategoryDAO TestCategoryDAO Interface
	 * @param commonFunction  CommonFunction holding common utility functions
	 */
	public TestCategoryServiceImpl(TestCategoryDAO testCategoryDAO, CommonFunction commonFunction) {
		this.testCategoryDAO = testCategoryDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available testcatgorys with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of testcategory records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getTestCategory(final UserInfo userInfo) throws Exception {
		return testCategoryDAO.getTestCategory(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active testcategory object based on the specified ntestcategorycode.
	 * @param ntestcategorycode [int] primary key of testcategory object
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         testcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveTestCategoryById(final int ntestcategorycode, final UserInfo userInfo)
			throws Exception {
		final TestCategory testCategory = (TestCategory) testCategoryDAO.getActiveTestCategoryById(ntestcategorycode);
		if (testCategory == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(testCategory, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to testcategory table.
	 * @param objTestCategory [TestCategory] object holding details to be added in
	 *                        testcategory table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         testcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception {

		return testCategoryDAO.createTestCategory(testCategory, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in testcategory table.
	 * @param objTestCategory [TestCategory] object holding details to be updated in
	 *                        testcategory table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         testcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception {

		return testCategoryDAO.updateTestCategory(testCategory, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in testcategory table.
	 * @param objTestCategory [TestCategory] object holding detail to be deleted
	 *                        from testcategory table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         testcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception {

		return testCategoryDAO.deleteTestCategory(testCategory, userInfo);
	}
}
