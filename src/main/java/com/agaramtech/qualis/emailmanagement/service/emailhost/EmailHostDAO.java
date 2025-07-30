package com.agaramtech.qualis.emailmanagement.service.emailhost;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.emailmanagement.model.EmailHost;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'emailhost'
 * table through its implementation class.
 */
public interface EmailHostDAO {

	/**
	 * This interface declaration is used to retrieve list of all active emailhost
	 * for the specified site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         emailhost
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getEmailHost(final int nmasterSiteCode) throws Exception;

	/**
	 * This interface declaration is used to retrieve active
	 * emailmanagementemailhost object based on the specified nemailhostcode.
	 * 
	 * @param nemailhostcode [int] primary key of emailmanagementemailhost object
	 * @return response entity object holding response status and data of emailhost
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public EmailHost getActiveEmailHostById(final int nemailhostcode) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to
	 * emailmanagementemailhost table.
	 * 
	 * @param emailmanagementEmailHost [EmailHost] object holding details to be
	 *                                 added in emailmanagementemailhost table
	 * @return response entity object holding response status and data of added
	 *         emailhost object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createEmailHost(EmailHost emailmanagementEmailHost, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in
	 * emailmanagementemailhost table.
	 * 
	 * @param emailmanagementEmailHost [EmailHost] object holding details to be
	 *                                 updated in emailmanagementemailhost table
	 * @return response entity object holding response status and data of updated
	 *         emailhost object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateEmailHost(EmailHost emailmanagementEmailHost, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in
	 * emailmanagementemailhost table.
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
