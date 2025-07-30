package com.agaramtech.qualis.emailmanagement.service.emailhost;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.emailmanagement.model.EmailHost;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'emailhost' table through its DAO layer.
 */
public interface EmailHostService {

	/**
	 * This interface declaration is used to retrieve list of all active emailhost
	 * for the specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         emailhost
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getEmailHost(final int nmasterSiteCode) throws Exception;

	/**
	 * This interface declaration is used to retrieve active emailmanagement
	 * emailhost object based on the specified nemailhostcode through its DAO layer.
	 * 
	 * @param nemailhostcode [int] primary key of emailmanagement emailhost object
	 * @param siteCode       [int] primary key of site object
	 * @return response entity object holding response status and data of emailhost
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveEmailHostById(final int nemailhostcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to emailmanagement
	 * emailhost table through its DAO layer.
	 * 
	 * @param emailmanagementEmailHost [EmailHost] object holding details to be
	 *                                 added in emailmanagement emailhost table
	 * @return response entity object holding response status and data of added
	 *         EmailHost object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createEmailHost(EmailHost emailmanagementEmailHost, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in emailmanagement
	 * emailhost table through its DAO layer.
	 * 
	 * @param emailmanagementEmailHost [EmailHost] object holding details to be
	 *                                 updated in emailmanagement emailhost table
	 * @return response entity object holding response status and data of updated
	 *         emailhost object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateEmailHost(EmailHost emailmanagementEmailHost, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in
	 * emailmanagementemailhost table through its DAO layer.
	 * 
	 * @param emailmanagementEmailHost [EmailHost] object holding detail to be
	 *                                 deleted in emailmanagementemailhost table
	 * @return response entity object holding response status and data of deleted
	 *         emailhost object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteEmailHost(EmailHost emailmanagementEmailHost, UserInfo userInfo)
			throws Exception;

}
