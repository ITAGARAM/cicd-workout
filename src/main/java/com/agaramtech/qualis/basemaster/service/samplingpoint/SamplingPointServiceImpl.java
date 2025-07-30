package com.agaramtech.qualis.basemaster.service.samplingpoint;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.basemaster.model.SamplingPoint;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'samplingpoint' table
 * through its DAO layer.
 * 
 * @author AT-E143
 * @version 10.0.0.2
 * @since 06- February- 2025
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class SamplingPointServiceImpl implements SamplingPointService {

	private final CommonFunction commonFunction;
	private final SamplingPointDAO samplingPointDAO;

	/**
	 * This method is used to retrieve list of all active samplingpoint for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         samplingpoint
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getSamplingPoint(final UserInfo userInfo) throws Exception {
		return samplingPointDAO.getSamplingPoint(userInfo);
	}

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
		return samplingPointDAO.getSamplingPointCategory(userInfo);
	}

	/**
	 * This method is used to retrieve active samplingpoint object based on the
	 * specified nsamplingPointCode.
	 * 
	 * @param nsamplingPointCode [int] primary key of samplingpoint object
	 * @return response entity object holding response status and data of
	 *         samplingpoint object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSamplingPointById(final int nsamplingPointCode, final UserInfo userInfo)
			throws Exception {
		final SamplingPoint samplingpoint = samplingPointDAO.getActiveSamplingPointById(nsamplingPointCode, userInfo);
		if (samplingpoint == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(samplingpoint, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to samplingpoint table. On successive
	 * insert get the new inserted record along with default status from transaction
	 * status
	 * 
	 * @param objSamplingPoint [SamplingPoint] object holding details to be added in
	 *                         samplingpoint table
	 * @return inserted samplingpoint object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSamplingPoint(final SamplingPoint objSamplingPoint, final UserInfo userInfo)
			throws Exception {
		return samplingPointDAO.createSamplingPoint(objSamplingPoint, userInfo);
	}

	/**
	 * This method is used to update entry in samplingpoint table.
	 * 
	 * @param objSamplingPoint [SamplingPoint] object holding details to be updated
	 *                         in samplingpoint table
	 * @return response entity object holding response status and data of updated
	 *         samplingpoint object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSamplingPoint(final SamplingPoint objSamplingPoint, final UserInfo userInfo)
			throws Exception {
		return samplingPointDAO.updateSamplingPoint(objSamplingPoint, userInfo);
	}

	/**
	 * This method id used to delete an entry in samplingpoint table
	 * 
	 * @param objSamplingPoint [SamplingPoint] an Object holds the record to be
	 *                         deleted
	 * @return a response entity with corresponding HTTP status and an samplingpoint
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSamplingPoint(final SamplingPoint objSamplingPoint, final UserInfo userInfo)
			throws Exception {
		return samplingPointDAO.deleteSamplingPoint(objSamplingPoint, userInfo);
	}
}
