package com.agaramtech.qualis.instrumentmanagement.service.instrumentlocation;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentLocation;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'InstrumentLocation' table
 * 
 * @author ATE235
 * @version 10.0.0.1
 * @since 22- May- 2023
 */
public interface InstrumentLocationService {
	/**
	 * This interface declaration is used to get the over all InstrumentLocation
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InstrumentLocation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInstrumentLocation(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active InstrumentLocation
	 * object based on the specified ninstrumentlocationcode.
	 * 
	 * @param ninstrumentlocationcode [int] primary key of InstrumentLocation object
	 * @return response entity object holding response status and data of
	 *         InstrumentLocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveInstrumentLocationById(final int ninstrumentlocationcode,final  UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in InstrumentLocation
	 * table.
	 * 
	 * @param instrumentLocation [InstrumentLocation] object holding details to be
	 *                           updated in InstrumentLocation table
	 * @return response entity object holding response status and data of updated
	 *         InstrumentLocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createInstrumentLocation(final InstrumentLocation instrumentLocation,final  UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in InstrumentLocation
	 * table.
	 * 
	 * @param instrumentLocation [InstrumentLocation] object holding detail to be
	 *                           deleted in InstrumentLocation table
	 * @return response entity object holding response status and data of deleted
	 *         InstrumentLocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateInstrumentLocation(final InstrumentLocation instrumentLocation,final  UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in InstrumentLocation
	 * table.
	 * 
	 * @param instrumentlocation [InstrumentLocation] object holding detail to be
	 *                           deleted in InstrumentLocation table
	 * @return response entity object holding response status and data of deleted
	 *         InstrumentLocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteInstrumentLocation(final InstrumentLocation instrumentlocation,final  UserInfo userInfo)
			throws Exception;

}
