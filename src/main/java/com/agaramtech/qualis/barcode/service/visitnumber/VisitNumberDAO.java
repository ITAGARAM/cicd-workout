package com.agaramtech.qualis.barcode.service.visitnumber;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.barcode.model.VisitNumber;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'VisitNumber'
 * table
 * 
 */
public interface VisitNumberDAO {
	/**
	 * This interface declaration is used to get the over all VisitNumber with
	 * respect to Project Type
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of VisitNumber with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */

	public ResponseEntity<Object> getVisitNumber(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to VisitNumber table.
	 * 
	 * @param visitNumber [VisitNumber] object holding details to be added in
	 *                    VisitNumber table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         VisitNumber object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> createVisitNumber(final VisitNumber visitNumber, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active VisitNumber object
	 * based on the specified nvisitnumbercode.
	 * 
	 * @param nvisitnumbercode [int] primary key of Project Type object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         VisitNumber object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public VisitNumber getActiveVisitNumberById(final int nvisitnumbercode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in VisitNumber table.
	 * 
	 * @param visitNumber [VisitNumber] object holding details to be updated in
	 *                    VisitNumber table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         VisitNumber object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateVisitNumber(final VisitNumber visitNumber, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in VisitNumber table.
	 * 
	 * @param visitNumber [VisitNumber] object holding detail to be deleted in
	 *                    VisitNumber table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         VisitNumber object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteVisitNumber(final VisitNumber visitNumber, final UserInfo userInfo)
			throws Exception;

}
