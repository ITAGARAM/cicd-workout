package com.agaramtech.qualis.stability.service.packagemaster;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.stability.model.PackageMaster;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'packagemaster' table
 * through its DAO layer.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 27 - Sep -2022
 */
@Transactional(readOnly = true, rollbackFor = Exception.class) 
@Service 
@RequiredArgsConstructor 
public class PackageMasterServiceImpl implements PackageMasterService {

	private final PackageMasterDAO packagemasterDAO;
	private final CommonFunction commonFunction;

	/**
	 * This method is used to retrieve list of all active packagemaster for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         packagemaster
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getPackageMaster(UserInfo userInfo) throws Exception {
		return packagemasterDAO.getPackageMaster(userInfo);
	}

	/**
	 * This method is used to retrieve active packagemaster object based on the
	 * specified npackagemastercode.
	 * 
	 * @param npackagemastercode [int] primary key of packagemaster object
	 * @return response entity object holding response status and data of
	 *         packagemaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActivePackageMasterById(int npackagemastercode, UserInfo userInfo)
			throws Exception {

		final PackageMaster packagemaster = packagemasterDAO.getActivePackageMasterById(npackagemastercode, userInfo);
		if (packagemaster == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(packagemaster, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to packagemaster table. On successive
	 * insert get the new inserted record along with default status from transaction
	 * status
	 * 
	 * @param objPackageMaster [PackageMaster] object holding details to be added in
	 *                         packagemaster table
	 * @return inserted packagemaster object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */

	@Transactional
	@Override
	public ResponseEntity<Object> createPackageMaster(PackageMaster objPackageMaster, UserInfo userInfo)
			throws Exception {
		return packagemasterDAO.createPackageMaster(objPackageMaster, userInfo);
	}

	/**
	 * This method is used to update entry in packagemaster table.
	 * 
	 * @param objPackageMaster [PackageMaster] object holding details to be updated
	 *                         in packagemaster table
	 * @return response entity object holding response status and data of updated
	 *         packagemaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updatePackageMaster(PackageMaster objPackageMaster, UserInfo userInfo)
			throws Exception {
		return packagemasterDAO.updatePackageMaster(objPackageMaster, userInfo);
	}

	/**
	 * This method id used to delete an entry in packagemaster table
	 * 
	 * @param objPackageMaster [PackageMaster] an Object holds the record to be
	 *                         deleted
	 * @return a response entity with corresponding HTTP status and an packagemaster
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deletePackageMaster(PackageMaster objPackageMaster, UserInfo userInfo)
			throws Exception {
		return packagemasterDAO.deletePackageMaster(objPackageMaster, userInfo);
	}
}
