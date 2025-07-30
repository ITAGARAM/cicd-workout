package com.agaramtech.qualis.testmanagement.service.testcategory;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestCategory;

/**
 * This interface holds declarations to perform CRUD operation on 'testcategory'
 * table
 */
public interface TestCategoryDAO {

	/**
	 * This interface declaration is used to get all the available testcategorys
	 * with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of testcategory records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getTestCategory(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active testcategorys object
	 * based on the specified ntestcategorycode.
	 * @param ntestcategorycode [int] primary key of testcategory object
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         testcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public TestCategory getActiveTestCategoryById(final int ntestcategorycode) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to testcategory table.
	 * @param objTestCategory [TestCategory] object holding details to be added in
	 *                        testcategory table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         testcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in testcategory table.
	 * @param objTestCategory [TestCategory] object holding details to be updated in
	 *                        testcategory table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         testcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in testcategory table.
	 * @param objTestCategory [TestCategory] object holding detail to be deleted
	 *                        from testcategory table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         testcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get all the available testcategory with
	 * respect to site and test master
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of testcategory records with
	 *         respect to site and test master
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getTestCategoryForTestMaster(final UserInfo objUserInfo) throws Exception;

	/**
	 * This interface declaration is used to get all the available testcategory with
	 * respect to site and test group
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of testcategory records with
	 *         respect to site and test group
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getTestCategoryForTestGroup(final UserInfo objUserInfo, int nclinicaltyperequired)
			throws Exception;

}
