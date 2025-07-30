package com.agaramtech.qualis.instrumentmanagement.service.instrumentlocation;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentLocation;

/**
 * This interface holds declarations to perform CRUD operation on 'Instrument location' table
 * @author ATE235
 * @version 10.0.0.1
 * @since 22-May -2023
 */
public interface InstrumentLocationDAO {
	/**
	 * This interface declaration is used to get the over all Instrument location with respect to Instrument
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of instrument location with respect to site and also have the HTTP response code 
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInstrumentLocation(final UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to retrieve active instrument location object based
	 * on the specified ninstrumentlocationcode.
	 * @param ninstrumentlocationcode [int] primary key of Instrument location object
	 * @return response entity  object holding response status and data of instrument location object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public InstrumentLocation getActiveInstrumentLocationById(final int ninstrumentlocationcode,final  UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to add a new entry to Instrument location  table.
	 * @param instrumentLocation [Instrument location] object holding details to be added in Instrument location table
	 * @return response entity object holding response status and data of added Instrument location object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createInstrumentLocation(final InstrumentLocation instrumentLocation,final UserInfo userInfo) throws Exception;
	/**
	 * This interface declaration is used to update entry in Instrument location  table.
	 * @param instrumentLocation [Instrument location] object holding details to be updated in Instrument location table
	 * @return response entity object holding response status and data of updated Instrument location object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateInstrumentLocation(final InstrumentLocation instrumentLocation,final  UserInfo userInfo)throws Exception;
	/**
	 * This interface declaration is used to delete entry in Instrument location  table.
	 * @param instrumentLocation [Instrument location] object holding detail to be deleted in Instrument location table
	 * @return response entity object holding response status and data of deleted Instrument location object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteInstrumentLocation(final InstrumentLocation instrumentlocation,final  UserInfo userInfo)throws Exception;

	}
