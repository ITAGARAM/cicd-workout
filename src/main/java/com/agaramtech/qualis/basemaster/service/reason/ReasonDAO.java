package com.agaramtech.qualis.basemaster.service.reason;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.Reason;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'reason' table
 */

public interface ReasonDAO {
	/**
	 * This interface declaration is used to get all the available reasons with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of reason records with respect
	 *         to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getReason(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active reason object based on
	 * the specified nreasoncode.
	 * 
	 * @param nreasoncode [int] primary key of reason object
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of reason
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Reason getActiveReasonById(final int nreasoncode, UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to add a new entry to reason table.
	 * 
	 * @param objUnit  [Reason] object holding details to be added in reason table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of added
	 *         reason object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createReason(Reason reason, UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to update entry in reason table.
	 * 
	 * @param objUnit  [Reason] object holding details to be updated in reason table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of updated
	 *         reason object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateReason(Reason reason, UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to delete an entry in reason table.
	 * 
	 * @param objUnit  [Reason] object holding detail to be deleted from reason
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of deleted
	 *         reason object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteReason(Reason reason, UserInfo userInfo) throws Exception;
}
