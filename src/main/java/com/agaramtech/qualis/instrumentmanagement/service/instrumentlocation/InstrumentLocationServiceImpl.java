package com.agaramtech.qualis.instrumentmanagement.service.instrumentlocation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentLocation;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'InstrumentLocation'
 * table through its DAO layer.
 * 
 * @author ATE235
 * @version 10.0.0.1
 * @since 22 - May -2023
 */

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class InstrumentLocationServiceImpl implements InstrumentLocationService {

	private final InstrumentLocationDAO instrumentlocationDAO;
	private final CommonFunction commonFunction;

	/**
	 * This method is used to retrieve list of all active InstrumentLocation for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InstrumentLocation
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getInstrumentLocation(UserInfo userInfo) throws Exception {
		return instrumentlocationDAO.getInstrumentLocation(userInfo);
	}

	/**
	 * This method is used to retrieve active Instrument location object based on
	 * the specified ninstrumentlocationcode.
	 * 
	 * @param ninstrumentlocationcode [int] primary key of InstrumentLocation object
	 * @return response entity object holding response status and data of
	 *         instrumentLocation object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveInstrumentLocationById(final int ninstrumentlocationcode,final  UserInfo userInfo)
			throws Exception {
		final InstrumentLocation instrumentlocation = instrumentlocationDAO
				.getActiveInstrumentLocationById(ninstrumentlocationcode, userInfo);
		if (instrumentlocation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(instrumentlocation, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to InstrumentLocation table. On
	 * successive insert get the new inserted record along with default status from
	 * transaction status
	 * 
	 * @param instrumentLocation [InstrumentLocation] object holding details to be
	 *                           added in InstrumentLocation table
	 * @return inserted instrumentLocation object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createInstrumentLocation(final InstrumentLocation instrumentLocation,final  UserInfo userInfo)
			throws Exception {
		return instrumentlocationDAO.createInstrumentLocation(instrumentLocation, userInfo);
	}

	/**
	 * This method is used to update entry in InstrumentLocation table.
	 * 
	 * @param instrumentLocation [InstrumentLocation] object holding details to be
	 *                           updated in InstrumentLocation table
	 * @return response entity object holding response status and data of updated
	 *         InstrumentLocation object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateInstrumentLocation(final InstrumentLocation instrumentLocation,final  UserInfo userInfo)
			throws Exception {
		return instrumentlocationDAO.updateInstrumentLocation(instrumentLocation, userInfo);
	}

	/**
	 * This method id used to delete an entry in InstrumentLocation table
	 * 
	 * @param instrumentlocation [InstrumentLocation] an Object holds the record to
	 *                           be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InstrumentLocation object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteInstrumentLocation(final InstrumentLocation instrumentlocation,final  UserInfo userInfo)
			throws Exception {
		return instrumentlocationDAO.deleteInstrumentLocation(instrumentlocation, userInfo);
	}

}
