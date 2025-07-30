package com.agaramtech.qualis.basemaster.service.samplingpoint;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.basemaster.model.SamplingPoint;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'samplingpoint' table
 * 
 * @author AT-E143
 * @version 10.0.0.2
 * @since 06- February- 2025
 */
public interface SamplingPointDAO {
	/**
	 * This interface declaration is used to get the over all samplingpoint with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of samplingpoint with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSamplingPoint(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all samplingpointcategory
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of samplingpointcategory with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSamplingPointCategory(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active samplingpoint object
	 * based on the specified nsamplingPointCode.
	 * 
	 * @param nsamplingPointCode [int] primary key of samplingpoint object
	 * @return response entity object holding response status and data of
	 *         samplingpoint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public SamplingPoint getActiveSamplingPointById(final int nsamplingPointCode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to samplingpoint table.
	 * 
	 * @param objSamplingPoint [SamplingPoint] object holding details to be added in
	 *                         samplingpoint table
	 * @return response entity object holding response status and data of added
	 *         samplingpoint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSamplingPoint(final SamplingPoint objSamplingPoint, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in samplingpoint table.
	 * 
	 * @param objSamplingPoint [SamplingPoint] object holding details to be updated
	 *                         in samplingpoint table
	 * @return response entity object holding response status and data of updated
	 *         samplingpoint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSamplingPoint(final SamplingPoint objSamplingPoint, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in samplingpoint table.
	 * 
	 * @param objSamplingPoint [SamplingPoint] object holding detail to be deleted
	 *                         in samplingpoint table
	 * @return response entity object holding response status and data of deleted
	 *         samplingpoint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSamplingPoint(final SamplingPoint objSamplingPoint, final UserInfo userInfo)
			throws Exception;

}
