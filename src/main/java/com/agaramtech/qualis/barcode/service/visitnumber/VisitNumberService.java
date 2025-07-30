package com.agaramtech.qualis.barcode.service.visitnumber;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.barcode.model.VisitNumber;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'VisitNumber' table
 * 
 */
public interface VisitNumberService {
	/**
	 * This interface declaration is used to get the over all VisitNumber with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of VisitNumber with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getVisitNumber(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add entry in VisitNumber table.
	 * 
	 * @param VisitNumber [VisitNumber] object holding details to be updated in
	 *                    VisitNumber table
	 * @param userInfo [UserInfo]                  
	 * @return response entity object holding response status and data of updated
	 *         VisitNumber object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createVisitNumber(final VisitNumber visitnumber, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active VisitNumber object
	 * based on the specified nvisitnumbercode.
	 * 
	 * @param nvisitnumbercode [int] primary key of VisitNumber object
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of
	 *         VisitNumber object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveVisitNumberById(final int nvisitnumbercode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in VisitNumber table.
	 * 
	 * @param VisitNumber [VisitNumber] object holding details to be updated in
	 *                    VisitNumber table
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of updated
	 *         VisitNumber object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateVisitNumber(final VisitNumber visitnumber, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in VisitNumber table.
	 * 
	 * @param VisitNumber [VisitNumber] object holding detail to be deleted in
	 *                    VisitNumber table
	 * @param userInfo [UserInfo]                   
	 * @return response entity object holding response status and data of deleted
	 *         VisitNumber object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteVisitNumber(final VisitNumber visitnumber, final UserInfo userInfo)
			throws Exception;

}
