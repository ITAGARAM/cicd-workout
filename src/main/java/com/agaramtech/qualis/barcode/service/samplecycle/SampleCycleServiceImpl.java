package com.agaramtech.qualis.barcode.service.samplecycle;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.barcode.model.SampleCycle;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'samplecycle' table
 * through its DAO layer.
 */

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SampleCycleServiceImpl implements SampleCycleService {

	private final SampleCycleDAO sampleCycleDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param sampleCycleDAO SampleCycleDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public SampleCycleServiceImpl(SampleCycleDAO sampleCycleDAO, CommonFunction commonFunction) {
		super();
		this.sampleCycleDAO = sampleCycleDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active SampleCycle for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         SampleCycle
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getSampleCycle(final UserInfo userInfo) throws Exception {
		return sampleCycleDAO.getSampleCycle(userInfo);
	}

	/**
	 * This method is used to add a new entry to SampleCycle table.
	 * 
	 * @param sampleCycle [SampleCycle] object holding details to be added in
	 *                    SampleCycle table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return inserted sampleCycle object with HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleCycle(final SampleCycle sampleCycle, final UserInfo userInfo)
			throws Exception {
		return sampleCycleDAO.createSampleCycle(sampleCycle, userInfo);
	}

	/**
	 * This method is used to retrieve active Sample Cycle object based on the
	 * specified nsamplecyclecode.
	 * 
	 * @param nsamplecyclecode [int] primary key of SampleCycle object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         sampleCycle object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleCycleById(final int nsamplecyclecode, final UserInfo userInfo)
			throws Exception {
		final SampleCycle objsampleno = sampleCycleDAO.getActiveSampleCycleById(nsamplecyclecode, userInfo);
		if (objsampleno == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objsampleno, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to update entry in SampleCycle table.
	 * 
	 * @param sampleCycle [SampleCycle] object holding details to be updated in
	 *                    SampleCycle table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         SampleCycle object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleCycle(final SampleCycle sampleCycle, final UserInfo userInfo)
			throws Exception {
		return sampleCycleDAO.updateSampleCycle(sampleCycle, userInfo);
	}

	/**
	 * This method id used to delete an entry in SampleCycle table
	 * 
	 * @param samplecycle [SampleCycle] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with corresponding HTTP status and SampleCycle
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleCycle(final SampleCycle samplecycle, final UserInfo userInfo)
			throws Exception {
		return sampleCycleDAO.deleteSampleCycle(samplecycle, userInfo);
	}

}
