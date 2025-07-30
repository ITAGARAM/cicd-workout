package com.agaramtech.qualis.stability.service.packagecategory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.stability.model.PackageCategory;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'packagecategory' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class PackageCategoryServiceImpl implements PackageCategoryService {

	private final PackageCategoryDAO packagecategoryDAO;
	private final CommonFunction commonFunction;

	/**
	 * This method is used to retrieve list of all active packagecategory for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         packagecategory
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getPackageCategory(UserInfo userInfo) throws Exception {
		return packagecategoryDAO.getPackageCategory(userInfo);
	}

	/**
	 * This method is used to retrieve active packagecategory object based on the
	 * specified npackagecategorycode.
	 * 
	 * @param npackagecategorycode [int] primary key of packagecategory object
	 * @return response entity object holding response status and data of
	 *         packagecategory object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActivePackageCategoryById(int npackagecategorycode, UserInfo userInfo)
			throws Exception {
		final PackageCategory packagecategory = packagecategoryDAO.getActivePackageCategoryById(npackagecategorycode,
				userInfo);
		if (packagecategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(packagecategory, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to packagecategory table. On
	 * successive insert get the new inserted record along with default status from
	 * transaction status
	 * 
	 * @param objPackageCategory [PackageCategory] object holding details to be
	 *                           added in packagecategory table
	 * @return inserted packagecategory object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createPackageCategory(PackageCategory objPackageCategory, UserInfo userInfo)
			throws Exception {
		return packagecategoryDAO.createPackageCategory(objPackageCategory, userInfo);
	}

	/**
	 * This method is used to update entry in packagecategory table.
	 * 
	 * @param objPackageCategory [PackageCategory] object holding details to be
	 *                           updated in packagecategory table
	 * @return response entity object holding response status and data of updated
	 *         packagecategory object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updatePackageCategory(PackageCategory objPackageCategory, UserInfo userInfo)
			throws Exception {
		return packagecategoryDAO.updatePackageCategory(objPackageCategory, userInfo);
	}

	/**
	 * This method id used to delete an entry in packagecategory table
	 * 
	 * @param objPackageCategory [PackageCategory] an Object holds the record to be
	 *                           deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         packagecategory object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deletePackageCategory(PackageCategory objPackageCategory, UserInfo userInfo)
			throws Exception {
		return packagecategoryDAO.deletePackageCategory(objPackageCategory, userInfo);
	}
}
