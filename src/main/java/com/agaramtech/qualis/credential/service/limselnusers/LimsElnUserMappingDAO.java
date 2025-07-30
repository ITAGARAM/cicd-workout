package com.agaramtech.qualis.credential.service.limselnusers;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.credential.model.LimsElnUserMapping;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface provides method declarations to perform CRUD operations on the
 * 'limselnuser' (LIMS ELN User Mapping) table in the database. The operations
 * include fetching, creating, updating, and deleting user mappings.
 */
public interface LimsElnUserMappingDAO {
	/**
	 * This method is used to retrieve all LIMS ELN user mappings for a specific
	 * user.
	 * 
	 * @param userInfo [UserInfo] holding the logged-in user details.
	 * @return ResponseEntity containing a list of all LIMS ELN user mappings.
	 * @throws Exception if any error occurs while fetching the mappings.
	 */
	public ResponseEntity<Object> getlimselnusermapping(final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to create a new LIMS ELN user mapping.
	 * 
	 * @param limselnuser [LimsElnUserMapping] object holding the details to be
	 *                    added.
	 * @param userInfo    [UserInfo] holding the logged-in user details.
	 * @return ResponseEntity containing the status and data of the created user
	 *         mapping.
	 * @throws Exception if any error occurs during the creation process.
	 */
	public ResponseEntity<Object> createLimsElnUsermapping(final LimsElnUserMapping limselnuser,
			final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve all available LIMS users for the given user.
	 * 
	 * @param userInfo [UserInfo] holding the logged-in user details.
	 * @return ResponseEntity containing the list of available LIMS users.
	 * @throws Exception if any error occurs while fetching the list of users.
	 */
	public ResponseEntity<Object> getLimsUsers(final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to delete an existing LIMS ELN user mapping.
	 * 
	 * @param limselnuser [LimsElnUserMapping] object holding the details of the
	 *                    mapping to be deleted.
	 * @param userInfo    [UserInfo] holding the logged-in user details.
	 * @return ResponseEntity containing the status and data of the deleted user
	 *         mapping.
	 * @throws Exception if any error occurs during the deletion process.
	 */
	public ResponseEntity<Object> deleteLimsElnUsermapping(final LimsElnUserMapping limselnuser,
			final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve an active LIMS ELN user mapping by its ID.
	 * 
	 * @param nreasoncode [int] the primary key of the LIMS ELN user mapping.
	 * @return LimsElnUserMapping object representing the requested user mapping.
	 * @throws Exception if any error occurs while retrieving the user mapping.
	 */
	public LimsElnUserMapping getActiveLimsElnUsermappingById(final int nreasoncode) throws Exception;

	/**
	 * This method is used to update an existing LIMS ELN user mapping.
	 * 
	 * @param limselnuser [LimsElnUserMapping] object containing the updated data.
	 * @param userInfo    [UserInfo] holding the logged-in user details.
	 * @return ResponseEntity containing the status and updated data of the user
	 *         mapping.
	 * @throws Exception if any error occurs during the update process.
	 */
	public ResponseEntity<Object> updateLimsElnUsermapping(final LimsElnUserMapping limselnuser,
			final UserInfo userInfo) throws Exception;

}
