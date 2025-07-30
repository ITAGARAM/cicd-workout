package com.agaramtech.qualis.testmanagement.service.testcategory;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestCategory;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'testcategory' table
 */
public interface TestCategoryService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available testcategorys with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of testcategory records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getTestCategory(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active testcategory object
	 * based on the specified ntestcategorycode through its DAO layer.
	 * @param ntestcategorycode [int] primary key of testcategory object
	 * @param siteCode          [int] primary key of site object
	 * @return response entity object holding response status and data of
	 *         testcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveTestCategoryById(final int ntestcategorycode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
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
	public ResponseEntity<Object> createTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
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
	public ResponseEntity<Object> updateTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
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
	public ResponseEntity<Object> deleteTestCategory(TestCategory testCategory, UserInfo userInfo) throws Exception;

}
