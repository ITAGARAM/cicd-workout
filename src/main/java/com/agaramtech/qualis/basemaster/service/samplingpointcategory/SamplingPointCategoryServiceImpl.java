package com.agaramtech.qualis.basemaster.service.samplingpointcategory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.basemaster.model.SamplingPointCategory;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'samplingpointcategory'
 * table through its DAO layer.
 * 
 * @author AT-E143
 * @version 10.0.0.2
 * @since 06- February- 2025
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class SamplingPointCategoryServiceImpl implements SamplingPointCategoryService {

	private final CommonFunction commonFunction;
	private final SamplingPointCategoryDAO samplingPointCategoryDAO;

	/**
	 * This method is used to retrieve list of all active samplingpointcategory for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         samplingpointcategory
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getSamplingPointCategory(final UserInfo userInfo) throws Exception {
		return samplingPointCategoryDAO.getSamplingPointCategory(userInfo);
	}

	/**
	 * This method is used to retrieve active samplingpointcategory object based on
	 * the specified nsamplingPointCatCode.
	 * 
	 * @param nsamplingPointCatCode [int] primary key of samplingpointcategory
	 *                              object
	 * @return response entity object holding response status and data of
	 *         samplingpointcategory object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSamplingPointCategoryById(final int nsamplingPointCatCode,
			final UserInfo userInfo) throws Exception {
		final SamplingPointCategory samplingpointcategory = samplingPointCategoryDAO
				.getActiveSamplingPointCategoryById(nsamplingPointCatCode, userInfo);
		if (samplingpointcategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(samplingpointcategory, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to samplingpointcategory table. On
	 * successive insert get the new inserted record along with default status from
	 * transaction status
	 * 
	 * @param objSamplingPointCategory [SamplingPointCategory] object holding
	 *                                 details to be added in samplingpointcategory
	 *                                 table
	 * @return inserted samplingpointcategory object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSamplingPointCategory(final SamplingPointCategory objSamplingPointCategory,
			final UserInfo userInfo) throws Exception {
		return samplingPointCategoryDAO.createSamplingPointCategory(objSamplingPointCategory, userInfo);
	}

	/**
	 * This method is used to update entry in samplingpointcategory table.
	 * 
	 * @param objSamplingPointCategory [SamplingPointCategory] object holding
	 *                                 details to be updated in
	 *                                 samplingpointcategory table
	 * @return response entity object holding response status and data of updated
	 *         samplingpointcategory object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSamplingPointCategory(final SamplingPointCategory objSamplingPointCategory,
			final UserInfo userInfo) throws Exception {
		return samplingPointCategoryDAO.updateSamplingPointCategory(objSamplingPointCategory, userInfo);
	}

	/**
	 * This method id used to delete an entry in samplingpointcategory table
	 * 
	 * @param objSamplingPointCategory [SamplingPointCategory] an Object holds the
	 *                                 record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         samplingpointcategory object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSamplingPointCategory(final SamplingPointCategory objSamplingPointCategory,
			final UserInfo userInfo) throws Exception {
		return samplingPointCategoryDAO.deleteSamplingPointCategory(objSamplingPointCategory, userInfo);
	}

}
