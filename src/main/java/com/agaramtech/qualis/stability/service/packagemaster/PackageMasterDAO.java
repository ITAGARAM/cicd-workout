package com.agaramtech.qualis.stability.service.packagemaster;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.stability.model.PackageMaster;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'packagemaster' table
 */
public interface PackageMasterDAO {
	/**
	 * This interface declaration is used to get the over all packagemaster with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of packagemaster with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getPackageMaster(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active packagemaster object
	 * based on the specified npackagemastercode.
	 * 
	 * @param npackagemastercode [int] primary key of packagemaster object
	 * @return response entity object holding response status and data of
	 *         packagemaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public PackageMaster getActivePackageMasterById(final int npackagemastercode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to packagemaster table.
	 * 
	 * @param objPackageMaster [PackageMaster] object holding details to be added in
	 *                         packagemaster table
	 * @return response entity object holding response status and data of added
	 *         packagemaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createPackageMaster(PackageMaster objPackageMaster, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in packagemaster table.
	 * 
	 * @param objPackageMaster [PackageMaster] object holding details to be updated
	 *                         in packagemaster table
	 * @return response entity object holding response status and data of updated
	 *         packagemaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updatePackageMaster(PackageMaster objPackageMaster, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in packagemaster table.
	 * 
	 * @param objPackageMaster [PackageMaster] object holding detail to be deleted
	 *                         in packagemaster table
	 * @return response entity object holding response status and data of deleted
	 *         packagemaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deletePackageMaster(PackageMaster objPackageMaster, UserInfo userInfo)
			throws Exception;
}
