package com.agaramtech.qualis.contactmaster.service.courier;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.contactmaster.model.Courier;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'courier' table through its DAO layer.
 */

public interface CourierService {

	/**
	 * This interface declaration is used to retrieve list of all active Courier for
	 * the specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         Courier
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getCourier(final int nmasterSiteCode) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to courier table
	 * through its DAO layer.
	 * 
	 * @param courier [Courier] object holding details to be added in courier table
	 * @return response entity object holding response status and data of added
	 *         courier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createCourier(Courier courier, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in courier table through
	 * its DAO layer.
	 * 
	 * @param courier [Courier] object holding details to be updated in courier
	 *                table
	 * @return response entity object holding response status and data of updated
	 *         courier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateCourier(Courier courier, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active courier object based on
	 * the specified ncouriercode through its DAO layer.
	 * 
	 * @param ncouriercode [int] primary key of courier object
	 * @param siteCode     [int] primary key of site object
	 * @return response entity object holding response status and data of courier
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveCourierById(final int ncouriercode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in courier table through
	 * its DAO layer.
	 * 
	 * @param courier [Courier] object holding detail to be deleted in courier table
	 * @return response entity object holding response status and data of deleted
	 *         courier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteCourier(Courier courier, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getAllActiveCourier(final int nmasterSiteCode) throws Exception;

}
