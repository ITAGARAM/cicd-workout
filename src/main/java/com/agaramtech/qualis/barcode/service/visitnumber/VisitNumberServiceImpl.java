package com.agaramtech.qualis.barcode.service.visitnumber;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.barcode.model.VisitNumber;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'visitnumber' table
 * through its DAO layer.
 * 
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class VisitNumberServiceImpl implements VisitNumberService {

	private final VisitNumberDAO visitNumberDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param visitnumberDAO VisitNumberDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public VisitNumberServiceImpl(VisitNumberDAO visitNumberDAO, CommonFunction commonFunction) {
		super();
		this.visitNumberDAO = visitNumberDAO;
		this.commonFunction = commonFunction;
	}
	
	/**
	 * This method is used to retrieve list of all active visitnumber for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         visitnumber
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getVisitNumber(final UserInfo userInfo) throws Exception {
		return visitNumberDAO.getVisitNumber(userInfo);
	}

	/**
	 * This method is used to add a new entry to visitnumber table.
	 * 
	 * @param visitNumber [VisitNumber] object holding details to be added in
	 *                    visitnumber table
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched                   
	 * @return inserted visitnumber object with HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createVisitNumber(final VisitNumber visitNumber, final UserInfo userInfo)
			throws Exception {
		return visitNumberDAO.createVisitNumber(visitNumber, userInfo);
	}

	/**
	 * This method is used to retrieve active visitnumber object based on the
	 * specified nvisitnumbercode.
	 * 
	 * @param nvisitnumbercode [int] primary key of visitnumber object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         visitnumber object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveVisitNumberById(final int nvisitnumbercode, final UserInfo userInfo)
			throws Exception {
		final VisitNumber objvisitno = visitNumberDAO.getActiveVisitNumberById(nvisitnumbercode, userInfo);
		if (objvisitno == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objvisitno, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to update entry in visitnumber table.
	 * 
	 * @param visitNumber [VisitNumber] object holding details to be updated in
	 *                    visitnumber table
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched                   
	 * @return response entity object holding response status and data of updated
	 *         visitnumber object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateVisitNumber(final VisitNumber visitNumber, final UserInfo userInfo)
			throws Exception {
		return visitNumberDAO.updateVisitNumber(visitNumber, userInfo);
	}

	/**
	 * This method id used to delete an entry in VisitNumber table
	 * 
	 * @param visitNumber [VisitNumber] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return a response entity with corresponding HTTP status and VisitNumber
	 *         object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteVisitNumber(final VisitNumber visitNumber, final UserInfo userInfo)
			throws Exception {
		return visitNumberDAO.deleteVisitNumber(visitNumber, userInfo);
	}
}
