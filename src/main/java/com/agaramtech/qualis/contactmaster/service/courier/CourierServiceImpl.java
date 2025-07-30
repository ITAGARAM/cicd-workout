package com.agaramtech.qualis.contactmaster.service.courier;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.contactmaster.model.Courier;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'courier' table through
 * its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class CourierServiceImpl implements CourierService {

	private final CourierDAO courierDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param unitDAO        UnitDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public CourierServiceImpl(CourierDAO courierDAO, CommonFunction commonFunction) {
		this.courierDAO = courierDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active courier for the specified
	 * site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         courier
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getCourier(final int nmasterSiteCode) throws Exception {
		return courierDAO.getCourier(nmasterSiteCode);
	}

	/**
	 * This method is used to add a new entry to courier table through its DAO
	 * layer.
	 * 
	 * @param courier [Courier] object holding details to be added in courier table
	 * @return response entity object holding response status and data of added
	 *         courier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createCourier(Courier courier, UserInfo userInfo) throws Exception {
		return courierDAO.createCourier(courier, userInfo);
	}

	/**
	 * This method is used to update entry in courier table through its DAO layer.
	 * 
	 * @param courier [Courier] object holding details to be updated in courier
	 *                table
	 * @return response entity object holding response status and data of updated
	 *         courier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateCourier(Courier courier, UserInfo userInfo) throws Exception {
		return courierDAO.updateCourier(courier, userInfo);
	}

	/**
	 * This method is used to retrieve active courier object based on the specified
	 * ncouriercode through its DAO layer.
	 * 
	 * @param ncouriercode [int] primary key of courier object
	 * @param siteCode     [int] primary key of site object
	 * @return response entity object holding response status and data of courier
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveCourierById(int ncouriercode, final UserInfo userInfo) throws Exception {
		final Courier courier = (Courier) courierDAO.getActiveCourierById(ncouriercode);
		if (courier == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(courier, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to delete entry in courier table through its DAO layer.
	 * 
	 * @param courier [Courier] object holding detail to be deleted in courier table
	 * @return response entity object holding response status and data of deleted
	 *         courier object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteCourier(Courier courier, UserInfo userInfo) throws Exception {
		return courierDAO.deleteCourier(courier, userInfo);
	}

	public ResponseEntity<Object> getAllActiveCourier(final int nmasterSiteCode) throws Exception {
		return courierDAO.getAllActiveCourier(nmasterSiteCode);
	}

}
