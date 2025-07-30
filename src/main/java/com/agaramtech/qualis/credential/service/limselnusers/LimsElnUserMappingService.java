package com.agaramtech.qualis.credential.service.limselnusers;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.credential.model.LimsElnUserMapping;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This service interface holds method declarations for CRUD operations on
 * 'limselnuser' (LIMS ELN User Mapping) data. These operations involve
 * fetching, creating, updating, and deleting user mappings.
 */
public interface LimsElnUserMappingService {
	/**
	 * This service method will access the DAO layer to fetch all LIMS ELN user
	 * mappings for the logged-in user.
	 * 
	 * @param userInfo [UserInfo] holds the details of the logged-in user.
	 * @return ResponseEntity containing a list of all LIMS ELN user mappings.
	 * @throws Exception if any error occurs in the DAO layer during the fetching
	 *                   process.
	 */
	public ResponseEntity<Object> getlimselnusermapping(final UserInfo userInfo) throws Exception;

	/**
	 * This service method will access the DAO layer to retrieve all available LIMS
	 * users for the logged-in user.
	 * 
	 * @param userInfo [UserInfo] holds the details of the logged-in user.
	 * @return ResponseEntity containing a list of available LIMS users.
	 * @throws Exception if any error occurs while retrieving the LIMS users from
	 *                   the DAO layer.
	 */
	public ResponseEntity<Object> getLimsUsers(final UserInfo userInfo) throws Exception;

	/**
	 * This service method will access the DAO layer to create a new LIMS ELN user
	 * mapping.
	 * 
	 * @param limselnuser [LimsElnUserMapping] object containing the details of the
	 *                    user mapping to be created.
	 * @param userInfo    [UserInfo] holds the details of the logged-in user.
	 * @return ResponseEntity containing the status and data of the newly created
	 *         user mapping.
	 * @throws Exception if any error occurs in the DAO layer during the creation
	 *                   process.
	 */
	public ResponseEntity<Object> createLimsElnUsermapping(LimsElnUserMapping limselnuser, UserInfo userInfo)
			throws Exception;

	/**
	 * This service method will access the DAO layer to delete an existing LIMS ELN
	 * user mapping.
	 * 
	 * @param limselnuser [LimsElnUserMapping] object containing the details of the
	 *                    user mapping to be deleted.
	 * @param userInfo    [UserInfo] holds the details of the logged-in user.
	 * @return ResponseEntity containing the status and data of the deleted user
	 *         mapping.
	 * @throws Exception if any error occurs in the DAO layer during the deletion
	 *                   process.
	 */
	public ResponseEntity<Object> deleteLimsElnUsermapping(final LimsElnUserMapping limselnuser,final  UserInfo userInfo)
			throws Exception;

	/**
	 * This service method will access the DAO layer to fetch an active LIMS ELN
	 * user mapping based on the provided mapping code.
	 * 
	 * @param nelnusermappingcode [int] the unique identifier of the LIMS ELN user
	 *                            mapping.
	 * @param userInfo            [UserInfo] holds the details of the logged-in
	 *                            user.
	 * @return ResponseEntity containing the status and data of the active user
	 *         mapping.
	 * @throws Exception if any error occurs in the DAO layer during the fetching
	 *                   process.
	 */
	public ResponseEntity<Object> getActiveLimsElnUsermappingById(final int nelnusermappingcode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service method will access the DAO layer to update an existing LIMS ELN
	 * user mapping.
	 * 
	 * @param limselnuser [LimsElnUserMapping] object containing the updated details
	 *                    of the user mapping.
	 * @param userInfo    [UserInfo] holds the details of the logged-in user.
	 * @return ResponseEntity containing the status and updated data of the user
	 *         mapping.
	 * @throws Exception if any error occurs in the DAO layer during the update
	 *                   process.
	 */
	public ResponseEntity<Object> updateLimsElnUsermapping(final LimsElnUserMapping limselnuser,final  UserInfo userInfo)
			throws Exception;
}
