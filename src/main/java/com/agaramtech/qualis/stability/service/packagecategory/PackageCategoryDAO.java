package com.agaramtech.qualis.stability.service.packagecategory;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.stability.model.PackageCategory;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'packagecategory' table
 */
public interface PackageCategoryDAO {
	/**
	 * This interface declaration is used to get the over all packagecategory with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of packagecategory with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getPackageCategory(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active packagecategory object
	 * based on the specified npackagecategorycode.
	 * 
	 * @param npackagecategorycode [int] primary key of packagecategory object
	 * @return response entity object holding response status and data of
	 *         packagecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public PackageCategory getActivePackageCategoryById(final int npackagecategorycode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to packagecategory
	 * table.
	 * 
	 * @param objPackageCategory [PackageCategory] object holding details to be
	 *                           added in packagecategory table
	 * @return response entity object holding response status and data of added
	 *         packagecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createPackageCategory(PackageCategory objPackageCategory, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in packagecategory table.
	 * 
	 * @param objPackageCategory [PackageCategory] object holding details to be
	 *                           updated in packagecategory table
	 * @return response entity object holding response status and data of updated
	 *         packagecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updatePackageCategory(PackageCategory objPackageCategory, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in packagecategory table.
	 * 
	 * @param objPackageCategory [PackageCategory] object holding detail to be
	 *                           deleted in packagecategory table
	 * @return response entity object holding response status and data of deleted
	 *         packagecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deletePackageCategory(PackageCategory objPackageCategory, UserInfo userInfo)
			throws Exception;
}
